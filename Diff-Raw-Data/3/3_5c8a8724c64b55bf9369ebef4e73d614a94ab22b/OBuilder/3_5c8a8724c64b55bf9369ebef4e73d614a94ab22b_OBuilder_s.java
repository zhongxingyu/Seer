 package org.apache.ode.simpel.omodel;
 
 import org.apache.log4j.Logger;
 
 import org.apache.ode.bpel.compiler.v2.BaseCompiler;
 import org.apache.ode.bpel.rtrep.v2.*;
 
 import org.apache.ode.bpel.compiler.bom.Bpel20QNames;
 import org.apache.ode.bpel.rapi.PropertyExtractor;
 import org.apache.ode.simpel.wsdl.SimPELInput;
 import org.apache.ode.simpel.wsdl.SimPELOperation;
 import org.apache.ode.simpel.wsdl.SimPELOutput;
 import org.apache.ode.simpel.wsdl.SimPELPortType;
 import org.apache.ode.utils.GUID;
 import org.apache.ode.Descriptor;
 
 import javax.wsdl.PortType;
 import javax.xml.namespace.QName;
 import java.lang.reflect.Method;
 import java.util.*;
 
 /**
  * TODO e4x templates
  * TODO allow javascript blocks instead of just hooking on equal, otherwise you can't do stuff like
  *          operations.appendChild(<operand>2</operand>);
  *          operations.appendChild(<operand>3</operand>);
  * TODO resources aren't available if they're not used
  */
 public class OBuilder extends BaseCompiler {
     private static final Logger __log = Logger.getLogger(OBuilder.class);
     private static final String SIMPEL_NS = "http://ode.apache.org/simpel/1.0/definition";
 
     private Descriptor _desc;
     private OExpressionLanguage _exprLang;
     private OExpressionLanguage _konstExprLang;
     private String _processNS;
     private HashMap<String,String> namespaces = new HashMap<String,String>();
     private HashMap<String, OPartnerLink> partnerLinks = new HashMap<String,OPartnerLink>();
     private HashMap<String,OScope.Variable> variables = new HashMap<String,OScope.Variable>();
     private HashMap<String,ResourceDesc> webResources = new HashMap<String,ResourceDesc>();
     private HashSet<String> typedVariables = new HashSet<String>();
 
     public OBuilder(Descriptor desc) {
         HashMap<String, String> exprRuntime = new HashMap<String, String>();
         exprRuntime.put("runtime-class", "org.apache.ode.simpel.expr.E4XExprRuntime");
         _exprLang = new OExpressionLanguage(_oprocess, exprRuntime);
         _exprLang.expressionLanguageUri = SIMPEL_NS + "/exprLang";
         _desc = desc;
     }
 
     public StructuredActivity build(Class oclass, OScope oscope, StructuredActivity parent, Object... params) {
         try {
             OActivity oactivity = (OActivity) oclass
                     .getConstructor(OProcess.class, OActivity.class).newInstance(_oprocess, parent.getOActivity());
             Method buildMethod = null;
             for (Method method : OBuilder.class.getMethods())
                 if (method.getName().equals("build" + oclass.getSimpleName().substring(1))) buildMethod = method;
 
             if (buildMethod == null) throw new RuntimeException("No builder for class " + oclass.getSimpleName());
 
             Object[] buildParams = new Object[params.length + 2];
             System.arraycopy(params, 0, buildParams, 2, params.length);
             buildParams[0] = oactivity;
             buildParams[1] = oscope;
             StructuredActivity result = (StructuredActivity) buildMethod.invoke(this, buildParams);
             if (result != null) parent.run((OActivity) result.getOActivity());
             return result;
         } catch (Exception e) {
             throw new RuntimeException("Couldn't build activity of type " + oclass, e);
         }
     }
 
     public static abstract class StructuredActivity<T> {
         private T _oact;
         public StructuredActivity(T oact) {
             _oact = oact;
         }
         public T getOActivity() {
             return _oact;
         }
         public abstract void run(OActivity child);
     }
     public static class SimpleActivity<T> extends StructuredActivity<T> {
         public SimpleActivity(T oact) {
             super(oact);
         }
         public void run(OActivity child) { /* Do nothing */ }
     }
 
     public StructuredActivity<OScope> buildProcess(String prefix, String name) {
         _oprocess = new OProcess(Bpel20QNames.NS_WSBPEL2_0_FINAL_EXEC);
         _oprocess.processName = name;
         _oprocess.guid = new GUID().toString();
         _oprocess.constants = makeConstants();
         _oprocess.compileDate = new Date();
         if (namespaces.get(prefix) == null) _oprocess.targetNamespace = SIMPEL_NS;
         else _oprocess.targetNamespace = namespaces.get(prefix);
 
         _oprocess.expressionLanguages.add(_exprLang);
         _processNS = SIMPEL_NS + "/" + name;
 
         _konstExprLang = new OExpressionLanguage(_oprocess, null);
         _konstExprLang.expressionLanguageUri = "uri:www.fivesight.com/konstExpression";
         _konstExprLang.properties.put("runtime-class",
                 "org.apache.ode.bpel.rtrep.v2.KonstExpressionLanguageRuntimeImpl");
         _oprocess.expressionLanguages.add(_konstExprLang);
 
         // Implicit scope that wraps a process
         final OScope processScope = new OScope(_oprocess, null);
         processScope.name = "__PROCESS_SCOPE:" + name;
         _oprocess.processScope = processScope;
 
         // Implicit self variable pointing to the instance resource for RESTful processes
         if (_desc.isRestful()) {
             SimPELExpr expr = new SimPELExpr(_oprocess);
             expr.setExpr(_desc.getAddress() != null ? ("\""+_desc.getAddress()+"\"") : "\"/\"");
             addResourceDecl(processScope, "self", expr, null);
         }
 
         return buildScope(processScope, null);
     }
 
     public StructuredActivity<OScope> buildScope(final OScope oscope, OScope parentScope) {
         return new StructuredActivity<OScope>(oscope) {
             public void run(OActivity child) {
                 if (child instanceof OEventHandler.OEvent) {
                     if (oscope.eventHandler == null)
                         oscope.eventHandler = new OEventHandler(_oprocess);
                     oscope.eventHandler.onMessages.add((OEventHandler.OEvent) child);
                 } else {
                     oscope.activity = child;
                 }
             }
         };
     }
 
     public StructuredActivity<OSwitch> buildSwitch(final OSwitch oswitch, OScope parentScope, SimPELExpr condExpr) {
         final OSwitch.OCase success = new OSwitch.OCase(_oprocess);
         success.expression = condExpr;
         success.expression.expressionLanguage = _exprLang;
         oswitch.addCase(success);
 
         return new StructuredActivity<OSwitch>(oswitch) {
             public void run(OActivity child) {
                 if (success.activity == null) success.activity = child;
                 else {
                     OSwitch.OCase opposite = new OSwitch.OCase(_oprocess);
                     opposite.expression = booleanExpr(true);
                     opposite.activity = child;
                     oswitch.addCase(opposite);
                 }
             }
         };
     }
 
     public StructuredActivity<OWhile> buildWhile(final OWhile owhile, OScope parentScope, SimPELExpr whileExpr) {
         owhile.whileCondition = whileExpr;
         owhile.whileCondition.expressionLanguage = _exprLang;
         return new StructuredActivity<OWhile>(owhile) {
             public void run(OActivity child) {
                 owhile.activity = child;
             }
         };
     }
 
     public SimpleActivity buildPickReceive(OPickReceive receive, OScope oscope, String partnerLinkOrResource,
                                            String operation, SimPELExpr expr) {
         OPickReceive.OnMessage onMessage = new OPickReceive.OnMessage(_oprocess);
         if (operation == null) {
             onMessage.resource = copyResource(webResources.get(partnerLinkOrResource), "POST");
             onMessage.resource.setInbound(true);
             if (onMessage.resource == null)
                 throw new RuntimeException("Unknown resource declared in receive: " + partnerLinkOrResource);
             _oprocess.providedResources.add(onMessage.resource);
         } else {
             onMessage.partnerLink = buildPartnerLink(oscope, partnerLinkOrResource, operation, true, true);
             onMessage.operation = onMessage.partnerLink.myRolePortType.getOperation(operation, null, null);
         }
 
         if (_oprocess.firstReceive == null) {
             if (onMessage.partnerLink != null)
                 onMessage.partnerLink.addCreateInstanceOperation(onMessage.operation);
             receive.createInstanceFlag = true;
             _oprocess.firstReceive = receive;
             if (onMessage.resource != null) {
                 webResources.get(partnerLinkOrResource).setInstantiating(true);
             }
         }
 
         // Is this receive part of an assignment? In this case the input var is the lvalue.
         if (expr != null) {
             onMessage.variable = resolveVariable(oscope, expr.getLValue(),
                     onMessage.operation != null ? onMessage.operation.getName() : null, true);
         }
 
         onMessage.activity = new OEmpty(_oprocess, receive);
         receive.onMessages.add(onMessage);
 
         return new SimpleActivity<OPickReceive>(receive);
     }
 
     public StructuredActivity buildEvent(final OEventHandler.OEvent oevent, OScope oscope, String resource, String method) {
         oevent.resource = copyResource(webResources.get(resource), method);
         _oprocess.providedResources.add(oevent.resource);
         OScope eventScope = new OScope(_oprocess, oevent);
         oevent.activity = eventScope;
 
         return new StructuredActivity<OEventHandler.OEvent>(oevent) {
             public void run(OActivity child) {
                 ((OScope)oevent.activity).activity = child;
             }
         };
     }
 
     public SimpleActivity buildInvoke(OInvoke invoke, OScope oscope, Object exprOrPlink, String methOrOp, String inputMsg, SimPELExpr outputMsg) {
         if (exprOrPlink instanceof SimPELExpr) return buildRequest(invoke, oscope, (SimPELExpr) exprOrPlink, methOrOp, inputMsg, outputMsg);
         else return buildWSInvoke(invoke, oscope, (String) exprOrPlink, methOrOp, inputMsg, outputMsg);
     }
 
     public SimpleActivity buildRequest(OInvoke invoke, OScope oscope, SimPELExpr expr, String method, String outgoingMsg, SimPELExpr responseMsg) {
         if (method != null && (!method.equalsIgnoreCase("get") || method.equalsIgnoreCase("put")
                  || method.equalsIgnoreCase("post") || method.equalsIgnoreCase("delete")))
             throw new RuntimeException("Invalid HTTP method: " + method);
 
         // TODO hack warning: because of the way deepText works in SimPELWalker, the expr we get is the whole thing
         // i.e. request(foo+"/order"), the expr should be changed to return its string instead of the following
         String exprStr = expr.getExpr();
         int openParens = exprStr.indexOf("(");
         int closeExpr = Math.max(exprStr.indexOf(")"), exprStr.indexOf(","));
         expr.setExpr(exprStr.substring(openParens+1, closeExpr));
         expr.expressionLanguage = _exprLang;
 
         invoke.resource = new OResource(_oprocess);
         invoke.resource.setSubpath(expr);
         invoke.resource.setMethod(method == null ? "get" : method);
         invoke.resource.setInbound(false);
         invoke.resource.setDeclaringScope(oscope);
         if (outgoingMsg != null)
             invoke.inputVar = resolveVariable(oscope, outgoingMsg, null, true);
         if (responseMsg != null)
             invoke.outputVar = resolveVariable(oscope, responseMsg.getLValue(), null, false);
 
         return new SimpleActivity<OInvoke>(invoke);
     }
 
     public SimpleActivity buildWSInvoke(OInvoke invoke, OScope oscope, String plink, String op, String outgoingMsg, SimPELExpr responseMsg) {
         invoke.partnerLink = buildPartnerLink(oscope, plink, op, false, outgoingMsg != null);
         invoke.operation = invoke.partnerLink.partnerRolePortType.getOperation(op, null, null);
 
         if (outgoingMsg != null)
             invoke.inputVar = resolveVariable(oscope, outgoingMsg, invoke.operation.getName(), true);
         if (responseMsg != null)
             invoke.outputVar = resolveVariable(oscope, responseMsg.getLValue(), invoke.operation.getName(), false);
 
         return new SimpleActivity<OInvoke>(invoke);
     }
 
     public StructuredActivity buildSequence(final OSequence seq, OScope oscope) {
         return new StructuredActivity<OSequence>(seq) {
             public void run(OActivity child) {
                 seq.sequence.add(child);
             }
         };
     }
 
     public SimpleActivity buildAssign(OAssign oassign, OScope oscope, SimPELExpr rexpr) {
         OAssign.Copy ocopy = new OAssign.Copy(_oprocess);
         oassign.operations.add(ocopy);
 
         OAssign.VariableRef vref = new OAssign.VariableRef(_oprocess);
         String lvar = rexpr.getLValue().split("\\.")[0];
         vref.variable = resolveVariable(oscope, lvar);
         // Don't worry, it's all type safe, therefore it's correct
         if (vref.variable.type instanceof OMessageVarType)
             vref.part = ((OMessageVarType)vref.variable.type).parts.values().iterator().next();
         ocopy.to = vref;
 
         rexpr.setLVariable(lvar);
         rexpr.expressionLanguage = _exprLang;
         ocopy.from = new OAssign.Expression(_oprocess, rexpr);
         return new SimpleActivity<OAssign>(oassign);
     }
 
     public SimpleActivity buildWait(OWait owait, OScope oscope, SimPELExpr rexpr) {
         // TODO time based computation to allow wait until
         owait.forExpression = rexpr;
         rexpr.expressionLanguage = _exprLang;
         return new SimpleActivity<OWait>(owait);
     }
 
     public SimpleActivity buildReply(OReply oreply, OScope oscope, OComm ocomm,
                                      String var, String plinkOrRes, String operation) {
         oreply.variable = resolveVariable(oscope, var, operation, false);
         if (plinkOrRes == null) {
             if (ocomm == null) throw new RuntimeException("No parent receive but reply with var " + var +
                     " has no plinkOrRes/operation or resource information.");
             if (ocomm.isRestful()) {
                 oreply.resource = ocomm.getResource();
             } else {
                 oreply.partnerLink = ocomm.getPartnerLink();
                 oreply.operation = ocomm.getOperation();
                 buildPartnerLink(oscope, oreply.partnerLink.name, oreply.operation.getName(), true, false);
             }
         } else {
             if (operation == null) {
                 ResourceDesc res = webResources.get(plinkOrRes);
                 if (res == null) throw new RuntimeException("Couldn't resolve the reply using partner link " +
                         "or resource " + plinkOrRes +". Either an operation is missing or the resource isn't recognized.");
                 oreply.resource = res.latest;
             } else {
                 oreply.partnerLink = buildPartnerLink(oscope, plinkOrRes, operation, true, false);
                 oreply.operation = oreply.partnerLink.myRolePortType.getOperation(operation, null, null);
             }
         }
         // Adding partner role
         return new SimpleActivity<OReply>(oreply);
     }
 
     public void setBlockParam(OScope oscope, OSequence blockActivity, String varName) {
         // The AST for block activities is something like:
         //    (SEQUENCE (activity) (SEQUENCE varIds otherActivities))
         // The blockActivity here is the second sequence so we just set the varIds on the previous activity
         if (blockActivity == null) {
             __log.warn("Can't set block parameter with block parent activity " + blockActivity);
             return;
         }
         List<OActivity> parentList = ((OSequence)blockActivity.getParent()).sequence;
         OActivity oact = parentList.get(parentList.indexOf(blockActivity) - 1);
         if (oact instanceof OPickReceive) {
             OPickReceive.OnMessage rec = ((OPickReceive)oact).onMessages.get(0);
             rec.variable = resolveVariable(oscope, varName, rec.operation != null ? rec.operation.getName() : null, true);
             if (rec.matchCorrelation != null) {
                 // Setting the message variable type associated with the correlation expression
                 for (PropertyExtractor extractor : rec.matchCorrelation.getExtractors()) {
                     ((SimPELExpr)extractor).getReferencedVariable(extractor.getMessageVariableName()).type = rec.variable.type;
                 }
             }
         } else if (oact instanceof OInvoke) {
             OInvoke inv = (OInvoke)oact;
             inv.outputVar = resolveVariable(oscope, varName, inv.operation != null ? inv.operation.getName() : null, false);
             buildPartnerLink(oscope, inv.partnerLink.name, inv.operation != null ? inv.operation.getName() : null, false, false);
         } else __log.warn("Can't set block parameter on activity " + oact);
     }
 
     public void addExprVariable(OScope oscope, SimPELExpr expr, String varName) {
         if (expr == null) {
             // TODO Temporary plug until all activities are implemented
             __log.warn("Skipping expression building, null expr");
             return;
         }
         if (varName.indexOf(".") > 0) varName = varName.split("\\.")[0];
         expr.addVariable(resolveVariable(oscope, varName));
     }
 
     public void addVariableDecl(String varName, String modifiers) {
         if (variables.get(varName) != null)
             throw new RuntimeException("Duplicate definition of variable " + varName);
         if (modifiers == null) return;
 
         if (modifiers.indexOf("unique") >= 0) {
             OProcess.OProperty oproperty = new OProcess.OProperty(_oprocess);
             oproperty.name = new QName(varName);
             _oprocess.properties.add(oproperty);
 
             OPropertyVarType propType = new OPropertyVarType(_oprocess);
             OScope.Variable propVar = new OScope.Variable(_oprocess, propType);
             propVar.name = varName;
             propVar.declaringScope = _oprocess.processScope;
             variables.put(varName, propVar);
             typedVariables.add(varName);
 
             // TODO get rid of this dummy correlation set, we should be able to access properties directly
             OScope.CorrelationSet set = new OScope.CorrelationSet(_oprocess);
             set.name = varName;                    
             set.properties.add(oproperty);
             set.declaringScope = _oprocess.processScope;
             _oprocess.processScope.addCorrelationSet(set);
         }
     }
 
     public void addResourceDecl(OScope scope, String resourceName, SimPELExpr pathExpr, String resourceRef) {
         ResourceDesc res = new ResourceDesc();
         res.name = resourceName;
         res.declaringScope = scope;
 
         if (pathExpr != null) {
             pathExpr.expressionLanguage = _exprLang;
             res.subpath = pathExpr;
         }
 
         if (resourceRef != null) {
             ResourceDesc reference = webResources.get(resourceRef);
             if (reference == null) throw new RuntimeException("Unknown resource reference " + resourceRef +
                     " in the definition of resource " + resourceName);
             res.reference = reference;
         }
 
         // Creating a variable to make the resource accessible as one
         OXsdTypeVarType resourceVarType = new OXsdTypeVarType(_oprocess);
         resourceVarType.simple = true;
         resourceVarType.xsdType = new QName("http://www.w3.org/2001/XMLSchema", "anyURI");
         OScope.Variable resourceVar = new OScope.Variable(_oprocess, resourceVarType);
         resourceVar.name = resourceName;
         resourceVar.declaringScope = scope;
         variables.put(resourceName, resourceVar);
         typedVariables.add(resourceName);
 
         webResources.put(resourceName, res);
     }
 
     public void addCorrelationMatch(OActivity receive, List match) {
         // TODO multiple values match
         OScope.CorrelationSet cset = _oprocess.processScope.getCorrelationSet((String) match.get(1));
         OPickReceive.OnMessage rec = ((OPickReceive)receive).onMessages.get(0);
 
         // Creating an expression that will return the correlation value by applying the correlation
         // function to the input variable (bound to __msg__)
         OScope.Variable msgVar = new OScope.Variable(_oprocess, null);
         msgVar.name = "__msg" + rec.getId() + "__";
         msgVar.declaringScope = _oprocess.processScope;
         SimPELExpr expr = new SimPELExpr(_oprocess);
         expr.setExpr(match.get(0) + "(" + msgVar.name + ")");
         expr.addVariable(msgVar);
         expr.expressionLanguage = _exprLang;
         cset.extractors.add(expr);
 
         rec.matchCorrelation = cset;
         rec.partnerLink.addCorrelationSetForOperation(rec.operation, cset);
     }
 
     public OProcess getProcess() {
         return _oprocess;
     }
 
     private OPartnerLink buildPartnerLink(OScope oscope, String name, String operation, boolean myRole, Boolean input) {
         // TODO Handle partnerlinks declared with an associated endpoint
         OPartnerLink resolved = partnerLinks.get(name);
         // TODO this will not work in case of variable name conflicts in different scopes
         if (resolved == null) {
             resolved = new OPartnerLink(_oprocess);
             resolved.name = name;
             resolved.declaringScope = oscope;
             partnerLinks.put(name, resolved);
             _oprocess.allPartnerLinks.add(resolved);
             oscope.partnerLinks.put(name, resolved);
         }
         PortType pt;
         if (myRole) {
             pt = resolved.myRolePortType;
             if (pt == null) {
                 pt = resolved.myRolePortType = new SimPELPortType();
                 pt.setQName(new QName(SIMPEL_NS, name));
             }
         } else {
             pt = resolved.partnerRolePortType;
             if (pt == null) {
                 pt = resolved.partnerRolePortType = new SimPELPortType();
                 pt.setQName(new QName(SIMPEL_NS, name));
             }
         }
 
         SimPELOperation op = (SimPELOperation) pt.getOperation(operation, null, null);
         if (op == null) {
             op = new SimPELOperation(operation);
             pt.addOperation(op);
         }
         // Java's three-way boolean
         if (input != null) {
             if (input) {
                 op.setInput(new SimPELInput(new QName(_processNS, operation + "Request")));
             } else {
                 op.setOutput(new SimPELOutput(new QName(_processNS, operation + "Response")));
             }
         }
         return resolved;
     }
 
     private OScope.Variable resolveVariable(OScope oscope, String name) {
         return resolveVariable(oscope, name, null, false);
     }
 
     private OScope.Variable resolveVariable(OScope oscope, String name, String operation, boolean request) {
         OScope.Variable resolved = variables.get(name);
         // TODO this will not work in case of variable name conflicts in different scopes
         if (resolved == null) {
             LinkedList<OMessageVarType.Part> parts = new LinkedList<OMessageVarType.Part>();
             parts.add(new OMessageVarType.Part(_oprocess, "payload",
                     new OElementVarType(_oprocess, new QName(_processNS, "simpelWrapper"))));
             OMessageVarType omsgType = new OMessageVarType(_oprocess, new QName(_processNS, "simpelMessage"), parts);
             resolved = new OScope.Variable(_oprocess, omsgType);
             resolved.name = name;
             resolved.declaringScope = oscope;
             oscope.addLocalVariable(resolved);
             variables.put(name, resolved);
         }
 
         // If an operation name has been provided with which to associate this variable, we
         // use a better naming for the part element.
         if (operation != null && !typedVariables.contains(name)) {
             String elmtName = operation + (request ? "Request" : "Response");
             OMessageVarType varType = (OMessageVarType)resolved.type;
             varType.messageType = new QName(_processNS, operation);
             OMessageVarType.Part part = varType.parts.values().iterator().next();
             part.name = elmtName;
             part.type = new OElementVarType(_oprocess, new QName(_processNS, elmtName));
             varType.parts.clear();
             varType.parts.put(part.name, part);
             typedVariables.add(name);
         }
         return resolved;
     }
 
     private OExpression booleanExpr(boolean value) {
         OConstantExpression ce = new OConstantExpression(_oprocess, value ? Boolean.TRUE : Boolean.FALSE);
         ce.expressionLanguage = _konstExprLang;
         return ce;
     }
 
     protected String getBpwsNamespace() {
         return "http://ode.apache.org/simpel/1.0";
     }
 
     private OResource copyResource(ResourceDesc res, String method) {
         OResource newRes = res.methods.get(method);
         if (newRes != null) return newRes;
 
         newRes = new OResource(_oprocess);
         newRes.setMethod(method);
         newRes.setDeclaringScope(res.declaringScope);
         newRes.setInstantiateResource(res.instantiating);
         newRes.setName(res.name);
         if (res.reference != null) newRes.setReference(copyResource(res.reference, method));
         newRes.setSubpath(res.subpath);
         res.declaringScope.resource.put(newRes.getName(), newRes);
         res.methods.put(method, newRes);
         res.latest = newRes;
 
         return newRes;
     }
 
     private static class ResourceDesc {
         String name;
         private boolean instantiating;
         ResourceDesc reference;
         OExpression subpath;
         OScope declaringScope;
         OResource latest;
         HashMap<String,OResource> methods = new HashMap<String,OResource>();
 
         void setInstantiating(boolean i) {
             instantiating = i;
             for (OResource resource : methods.values()) {
                 resource.setInstantiateResource(i);
             }
         }
     }
 }
