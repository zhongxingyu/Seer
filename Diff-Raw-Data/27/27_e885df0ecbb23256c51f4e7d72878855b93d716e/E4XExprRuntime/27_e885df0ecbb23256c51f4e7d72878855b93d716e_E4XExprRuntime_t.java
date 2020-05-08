 package org.apache.ode.simpel.expr;
 
 import org.apache.ode.bpel.rtrep.v2.*;
 import org.apache.ode.bpel.rtrep.common.ConfigurationException;
 
 import org.apache.ode.bpel.common.FaultException;
 import org.apache.ode.simpel.omodel.SimPELExpr;
 import org.apache.ode.utils.DOMUtils;
 import org.apache.ode.utils.xsd.Duration;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Delegator;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.serialize.ScriptableInputStream;
 import org.mozilla.javascript.xmlimpl.XMLLibImpl;
 import org.mozilla.javascript.xml.XMLLib;
 import org.mozilla.javascript.xml.XMLObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 
 import javax.xml.namespace.QName;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.io.ObjectInputStream;
 import java.io.ByteArrayInputStream;
 
 /**
  * @author Matthieu Riou <mriou@apache.org>
  */
 public class E4XExprRuntime implements ExpressionLanguageRuntime {
 
    private static ConcurrentHashMap<String, Scriptable> globalStateCache = new ConcurrentHashMap<String, Scriptable>();
 
     public void initialize(Map map) throws ConfigurationException {
     }
 
     public String evaluateAsString(OExpression oExpression, EvaluationContext evaluationContext) throws FaultException {
         return null;
     }
 
     public boolean evaluateAsBoolean(OExpression oexpr, EvaluationContext evaluationContext) throws FaultException {
         // TODO context caching
         Context cx = ContextFactory.getGlobal().enterContext();
         ODEDelegator scope = new ODEDelegator(cx.initStandardObjects(), evaluationContext, (SimPELExpr)oexpr, cx);
 
         // First evaluating the assignment
         SimPELExpr expr = (SimPELExpr) oexpr;
         Object res = cx.evaluateString(scope, expr.getExpr(), "<expr>", 0, null);
         if (res instanceof Boolean) return (Boolean)res;
         else throw new FaultException(new QName("e4xEvalFailure"), "Failed to evaluate "
                 + expr.getExpr() + " as a boolean value");
     }
 
     public Number evaluateAsNumber(OExpression oExpression, EvaluationContext evaluationContext) throws FaultException {
         return null;
     }
 
     public List evaluate(OExpression oexpr, EvaluationContext evaluationContext) throws FaultException {
         // TODO context caching
         Context cx = ContextFactory.getGlobal().enterContext();
         cx.setOptimizationLevel(-1);
 
         Scriptable parentScope;
         if (oexpr.getOwner().globalState != null) {
            parentScope = globalStateCache.get(oexpr.getOwner().getGuid());
             if (parentScope == null) {
                 Scriptable sharedScope = cx.initStandardObjects();
                 try {
                     ObjectInputStream in = new ScriptableInputStream(new ByteArrayInputStream(oexpr.getOwner().globalState), sharedScope);
                     parentScope = (Scriptable) in.readObject();
                     in.close();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                globalStateCache.put(oexpr.getOwner().getGuid(), parentScope);
             }
         } else {
             parentScope = cx.initStandardObjects();
         }
 
         ODEDelegator scope = new ODEDelegator(parentScope, evaluationContext, (SimPELExpr)oexpr, cx);
 
         // First evaluating the assignment
         SimPELExpr expr = (SimPELExpr) oexpr;
         String forged = expr.getExpr();
         if (expr.getLValue() != null)
             forged = expr.getLValue() + " = " + expr.getExpr();
 
         Object res = cx.evaluateString(scope, forged, "<expr>", 0, null);
         // Second extracting the resulting variable value
         if (expr.getLValue() != null)
             res = scope.getEnv().get(expr.getLVariable());
 
         ArrayList<Node> resList = new ArrayList<Node>(1);
         if (res instanceof String || res instanceof Number) {
             Document doc = DOMUtils.newDocument();
             resList.add(doc.createTextNode(res.toString()));
         } else if (res instanceof XMLObject) {
             try {
                 // Only content is copied, need to wrap
                 Document doc = DOMUtils.newDocument();
                 Element wrapper = doc.createElement("assignWrapper");
                 wrapper.appendChild(doc.importNode(XMLLibImpl.toDomNode(res), true));
                 resList.add(wrapper);
             } catch (IllegalArgumentException e) {
                 // Rhino makes it pretty hard to use it sXML impl, XML and XMLList are package level
                 // classes so I can't test on them but toDomNode doesn't accept XMLList
                 Document doc = DOMUtils.newDocument();
                 resList.add(doc.createTextNode(res.toString()));
             }
         } else if (res instanceof Node) resList.add((Node) res);
         return resList;
     }
 
     public Calendar evaluateAsDate(OExpression oExpression, EvaluationContext evaluationContext) throws FaultException {
         return null;
     }
 
     public Duration evaluateAsDuration(OExpression oExpression, EvaluationContext evaluationContext) throws FaultException {
         return null;
     }
 
     public Node evaluateNode(OExpression oExpression, EvaluationContext evaluationContext) throws FaultException {
         return (Node) evaluate(oExpression, evaluationContext).get(0);
     }
 
     private class ODEDelegator extends Delegator  {
         private EvaluationContext _evaluationContext;
         private XMLLib _xmlLib;
         private SimPELExpr _expr;
         private Context _cx;
         private HashMap<String,Object> _env = new HashMap<String,Object>();
 
         private ODEDelegator(Scriptable obj, EvaluationContext evaluationContext, SimPELExpr expr, Context cx) {
             super(obj);
             _evaluationContext = evaluationContext;
             _expr = expr;
             _cx = cx;
         }
 
         public void setXmlLib(XMLLib _xmlLib) {
             this._xmlLib = _xmlLib;
         }
 
         public Object get(String name, Scriptable start) {
             try {
                 OScope.Variable v = _expr.getReferencedVariable(name);
                 if (v == null) return super.get(name, start);
 
                 // Handling of variables pointing to property values
                 if (v.type instanceof OPropertyVarType) {
                     System.out.println("Property value access.");
                 }
 
                 if (_env.get(name) != null) return _env.get(name);
                 // TODO this assumes message type with a single part for all variables, valid?
                 Node node;
                 try {
                     if (v.type instanceof OMessageVarType)
                         node = _evaluationContext.readVariable(v,((OMessageVarType)v.type).parts.values().iterator().next());
                     else
                         node = _evaluationContext.readVariable(v, null);
                 } catch (FaultException e) {
                     if (e.getQName().getLocalPart().equals("uninitializedVariable")) return super.get(name, start);
                     else throw e;
                 }
                 // Simple types
                 if (node.getNodeValue() != null) return node.getNodeValue();
                 else if (node.getNodeType() == Node.ELEMENT_NODE) {
                     Element nodeElmt = (Element) node;
                     if (DOMUtils.getFirstChildElement(nodeElmt) == null) return nodeElmt.getTextContent();
                     else node = DOMUtils.getFirstChildElement((Element)node);
                 }
 
                 // Have to remove the xml header otherwise it confuses Rhino
                 String[] xmlArr = DOMUtils.domToString(node).split("\n");
                 // Going back to the evaluation loop to get a Rhino XML object, their XML API just doesn't have any
                 // public method allowing an XML construction.
                 String[] newXmlArr = new String[xmlArr.length - 1];
                 System.arraycopy(xmlArr, 1, newXmlArr, 0, xmlArr.length - 1);
                 Object xmlObj = _cx.evaluateString(start, join(newXmlArr) , "<expr>", 0, null);
                 _env.put(name, xmlObj);
                 return xmlObj;
             } catch (Exception e) {
                 throw new RuntimeException("Error accessing variable " + name + ".", e);
             }
         }
 
         public boolean has(String name, Scriptable start) {
             OScope.Variable v = _expr.getReferencedVariable(name);
             if (v == null) return super.has(name, start);
 
             Node node;
             try {
                 if (v.type instanceof OMessageVarType)
                     node = _evaluationContext.readVariable(v,((OMessageVarType)v.type).parts.values().iterator().next());
                 else
                     node = _evaluationContext.readVariable(v, null);
             } catch (FaultException e) {
                 return false;
             }
             return node != null;
         }
 
         public void put(String name, Scriptable start, Object value) {
             _env.put(name, value);
         }
 
         
         public HashMap<String, Object> getEnv() {
             return _env;
         }
 
         public Scriptable getObj() {
             return obj;
         }
     }
 
     // Can someone tell me why I have to implement this? The Java API just sucks.
     public static String join(String[] ss) {
         StringBuffer buffer = new StringBuffer();
         for (String s : ss) buffer.append(s);
         return buffer.toString();
     }
 }
