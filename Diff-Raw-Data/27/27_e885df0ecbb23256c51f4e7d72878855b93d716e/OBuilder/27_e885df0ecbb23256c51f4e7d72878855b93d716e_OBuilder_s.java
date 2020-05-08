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
 import org.w3c.dom.Node;
 import org.w3c.dom.Document;
 
 import javax.wsdl.PortType;
 import javax.wsdl.Part;
 import javax.xml.namespace.QName;
 import java.lang.reflect.Method;
 import java.util.*;
 
 /**
  * TODO foo = receive(...) expressions
  * TODO e4x templates
  * TODO allow javascript blocks instead of just hooking on equal, otherwise you can't do stuff like
  *          operations.appendChild(<operand>2</operand>);
  *          operations.appendChild(<operand>3</operand>);
  * TODO support generic variable declarations that aren't mapped to a property (unique)
  */
 public class OBuilder extends BaseCompiler {
     private static final Logger __log = Logger.getLogger(OBuilder.class);
     private static final String SIMPEL_NS = "http://ode.apache.org/simpel/1.0/definition";
 
     private OExpressionLanguage _exprLang;
     private OExpressionLanguage _konstExprLang;
     private String _processNS;
     private HashMap<String,String> namespaces = new HashMap<String,String>();
     private HashMap<String, OPartnerLink> partnerLinks = new HashMap<String,OPartnerLink>();
     private HashMap<String,OScope.Variable> variables = new HashMap<String,OScope.Variable>();
     private HashSet<String> typedVariables = new HashSet<String>();
     private boolean firstReceive = true;
 
     public OBuilder() {
         HashMap<String, String> exprRuntime = new HashMap<String, String>();
         exprRuntime.put("runtime-class", "org.apache.ode.simpel.expr.E4XExprRuntime");
         _exprLang = new OExpressionLanguage(_oprocess, exprRuntime);
         _exprLang.expressionLanguageUri = SIMPEL_NS + "/exprLang";
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
 
         final OScope processScope = new OScope(_oprocess, null);
         processScope.name = "__PROCESS_SCOPE:" + name;
         _oprocess.processScope = processScope;
         return buildScope(processScope, null);
     }
 
     public StructuredActivity<OScope> buildScope(final OScope oscope, OScope parentScope) {
         return new StructuredActivity<OScope>(oscope) {
             public void run(OActivity child) {
                 oscope.activity = child;
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
 
     public SimpleActivity buildPickReceive(OPickReceive receive, OScope oscope, String partnerLink, String operation) {
         OPickReceive.OnMessage onMessage = new OPickReceive.OnMessage(_oprocess);
         onMessage.partnerLink = buildPartnerLink(oscope, partnerLink, operation, true, true);
         onMessage.operation = onMessage.partnerLink.myRolePortType.getOperation(operation, null, null);
 
         if (firstReceive) {
             firstReceive = false;
             onMessage.partnerLink.addCreateInstanceOperation(onMessage.operation);
             receive.createInstanceFlag = true;
         }
 
         onMessage.activity = new OEmpty(_oprocess, receive);
         receive.onMessages.add(onMessage);
 
         return new SimpleActivity<OPickReceive>(receive);
     }
 
     public SimpleActivity buildInvoke(OInvoke invoke, OScope oscope, String partnerLink,
                                       String operation, String incomingMsg) {
         invoke.partnerLink = buildPartnerLink(oscope, partnerLink, operation, false, incomingMsg != null);
         invoke.operation = invoke.partnerLink.partnerRolePortType.getOperation(operation, null, null);
         if (incomingMsg != null) invoke.inputVar = resolveVariable(oscope, incomingMsg, operation, true);
 
         return new SimpleActivity<OInvoke>(invoke);
     }
 
     public StructuredActivity buildSequence(final OSequence seq, OScope oscope) {
         return new StructuredActivity<OSequence>(seq) {
             public void run(OActivity child) {
                 seq.sequence.add(child);
             }
         };
     }
 
     public SimpleActivity buildAssign(OAssign oassign, OScope oscope, String lexpr, SimPELExpr rexpr) {
         OAssign.Copy ocopy = new OAssign.Copy(_oprocess);
         oassign.operations.add(ocopy);
 
         OAssign.VariableRef vref = new OAssign.VariableRef(_oprocess);
         String lvar = lexpr.split("\\.")[0];
         vref.variable = resolveVariable(oscope, lvar);
         // Don't worry, it's all type safe, therefore it's correct
         if (vref.variable.type instanceof OMessageVarType)
             vref.part = ((OMessageVarType)vref.variable.type).parts.values().iterator().next();
         ocopy.to = vref;
 
         rexpr.setLValue(lexpr);
         rexpr.setLVariable(lvar);
         rexpr.expressionLanguage = _exprLang;
         ocopy.from = new OAssign.Expression(_oprocess, rexpr);
         return new SimpleActivity<OAssign>(oassign);
     }
 
     public SimpleActivity buildReply(OReply oreply, OScope oscope, OPickReceive oreceive,
                                      String var, String partnerLink, String operation) {
         oreply.variable = resolveVariable(oscope, var, operation, false);
         if (partnerLink == null) {
             if (oreceive == null) throw new RuntimeException("No parent receive but reply with var " + var +
                     " has no partnerLink/operation information.");
             oreply.partnerLink = oreceive.onMessages.get(0).partnerLink;
             oreply.operation = oreceive.onMessages.get(0).operation;
             buildPartnerLink(oscope, oreply.partnerLink.name, oreply.operation.getName(), true, false);
         } else {
             oreply.partnerLink = buildPartnerLink(oscope, partnerLink, operation, true, false);
             oreply.operation = oreply.partnerLink.myRolePortType.getOperation(operation, null, null);
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
             rec.variable = resolveVariable(oscope, varName, rec.operation.getName(), true);
             if (rec.matchCorrelation != null) {
                 // Setting the message variable type associated with the correlation expression
                 for (PropertyExtractor extractor : rec.matchCorrelation.getExtractors()) {
                     ((SimPELExpr)extractor).getReferencedVariable(extractor.getMessageVariableName()).type = rec.variable.type;
                 }
             }
         } else if (oact instanceof OInvoke) {
             OInvoke inv = (OInvoke)oact;
             inv.outputVar = resolveVariable(oscope, varName, inv.operation.getName(), false);
             buildPartnerLink(oscope, inv.partnerLink.name, inv.operation.getName(), false, false);
         } else __log.warn("Can't set block parameter on activity " + oact);
     }
 
     public void addExprVariable(OScope oscope, SimPELExpr expr, String varName) {
         if (expr == null) {
             // TODO Temporary plug until all activities are implemented
             __log.warn("Skipping expression building, null expr");
             return;
         }
         expr.addVariable(resolveVariable(oscope, varName));
     }
 
     public void addVariableDecl(String varName, String modifiers) {
         if (variables.get(varName) != null)
             throw new RuntimeException("Duplicate definition of variable " + varName);
 
         if (modifiers.indexOf("unique") >= 0) {
             OProcess.OProperty oproperty = new OProcess.OProperty(_oprocess);
             oproperty.name = new QName(varName);
             _oprocess.properties.add(oproperty);
 
             OPropertyVarType propType = new OPropertyVarType(_oprocess);
             OScope.Variable propVar = new OScope.Variable(_oprocess, propType);
             propVar.name = varName;
             propVar.declaringScope = _oprocess.processScope;
             variables.put(varName, propVar);
 
             // TODO get rid of this dummy correlation set, we should be able to access properties directly
             OScope.CorrelationSet set = new OScope.CorrelationSet(_oprocess);
             set.name = varName;                    
             set.properties.add(oproperty);
             set.declaringScope = _oprocess.processScope;
             _oprocess.processScope.addCorrelationSet(set);
         }
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
 
 }
