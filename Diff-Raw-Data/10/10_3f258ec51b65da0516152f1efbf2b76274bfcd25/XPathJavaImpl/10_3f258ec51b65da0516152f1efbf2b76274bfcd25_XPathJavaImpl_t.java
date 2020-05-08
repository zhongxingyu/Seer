 /*******************************************************************************
  *
  * Copyright (c) 2012 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors: 
  *
  *    Bob Foster
  *     
  *******************************************************************************/ 
 
 package org.hudsonci.xpath.impl;
 
 import org.hudsonci.xpath.XPathAPI;
 import org.hudsonci.xpath.XNamespaceContext;
 import org.hudsonci.xpath.XVariableContext;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 import javax.xml.namespace.QName;
 import javax.xml.xpath.*;
 import org.hudsonci.xpath.XPathException;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Implementation of XPathAPI using javax.xml.xpath.
  * 
  * @author Bob Foster
  */
 public class XPathJavaImpl implements XPathAPI {
 
   String expr;
   String workingExpr;
   javax.xml.xpath.XPath xpath;
   XVariableContext varContext;
   XNamespaceContext nsContext;
   XPathExpression xpathExpr;
   
   public XPathJavaImpl(String expr) {
     this.expr = expr;
   }
   
   private class VarResolver implements XPathVariableResolver {
 
     public Object resolveVariable(QName qname) {
       try {
         Object obj = varContext.getVariableValue(qname.getNamespaceURI(), 
                 qname.getPrefix(), qname.getLocalPart());
         if (obj instanceof org.dom4j.Node)
           obj = nodeToNode((org.dom4j.Node) obj);
         return obj;
       } catch (Exception ex) {
         throw new IllegalStateException(ex);
       }
     }
     
   }
   
   private Dom2Dom d2d;
   
   private Node nodeToNode(org.dom4j.Node d4jNode) throws XPathException {
     d2d = new Dom2Dom();
     return d2d.dom2Dom(d4jNode, true);
   }
   
   private org.dom4j.Node unmap(Node node) {
     return d2d.getOriginalNode(node);
   }
   
   private XPathExpression getXPathExpression() throws XPathException {
     if (xpathExpr == null) {
       XPathFactory factory = XPathFactory.newInstance();
       if (varContext != null)
         factory.setXPathVariableResolver(new VarResolver());
       xpath = factory.newXPath();
       if (nsContext != null)
         xpath.setNamespaceContext(nsContext);
       try {
         xpathExpr = xpath.compile(workingExpr);
       } catch (XPathExpressionException ex) {
         throw new XPathException(ex);
       }
     }
     return xpathExpr;
   }
   
   /**
    * Set variable context used when evaluating XPath expression.
    * @param varContext 
    */
   public void setVariableContext(XVariableContext varContext) {
     this.varContext = varContext;
   }
   
   /**
    * Get variable context.
    * @return previously set variable context.
    */
   public XVariableContext getVariableContext() {
     return varContext;
   }
   
   /**
    * Get namespace context used when evaluating XPath expression.
    * @param nsContext 
    */
   public void setNamespaceContext(XNamespaceContext nsContext) {
     this.nsContext = nsContext;
   }
   
   /**
    * Get namespace context.
    * @return previously set namespace context
    */
   public XNamespaceContext getNamespaceContext() {
     return nsContext;
   }
   
   private Node getNode(Object xpathContext) throws XPathException {
     // A common idiom in Jelly tests is to assign a document (or other node)
     // to a variable and ask for an XPath using that variable to be
     // evaluated with no context. This doesn't work in Xalan.
     // We attempt to deal with the common case of this by rewriting
     // an expression with exactly one node-valued variable, using the
     // node as context and replacing it with '.' in the expression.
     // A more complex scheme might attempt to extract all variables,
     // find the outermost and rewrite the expression in terms of
     // paths from the outermost node. Not yet.
     
     workingExpr = expr;
     
     org.dom4j.Node node = null;
     
     if (xpathContext instanceof org.dom4j.Node)
       node = (org.dom4j.Node) xpathContext;
     
     else if (xpathContext == null) {
       try {
         // javax XPath doesn't like a null context. Try to fix.
         Rewriter rewriter = new Rewriter();
         Pair<String,org.dom4j.Node> pair = rewriter.rewriteExpression(expr, varContext, nsContext);
         node = pair.getRight();
         workingExpr = pair.getLeft();
       } catch (Exception ex) {
         throw new XPathException(ex);
       }
     }
     
     if (node != null)
       return nodeToNode(node);
     else
       throw new XPathException(xpathContext.getClass().getName()+" xpathContext must be subclass of org.dom4j.Node");
   }
   
   /**
    * Evaluate expression and return an appropriate value, one of: Node, NodeList,
    * String, Boolean or Double.
    * 
    * @param xpathContext must be an org.dom4j.Node or null if expression contains
    * a Node-valued variable that can be used to establish the context.
    * @return result of evaluation
    * @throws XPathException if errors detected
    */
   public Object evaluate(Object xpathContext) throws XPathException {
     // javax.xml.xpath doesn't support "return whatever"
     // hack around to satisfy Jelly
     Node context = getNode(xpathContext);
     XPathExpression xexpr = getXPathExpression();
     Object obj = null;
     try {
       obj = xexpr.evaluate(context, XPathConstants.NODESET);
     } catch (XPathExpressionException ex) {
       try {
         // Anything that will evaluate to a non-node will also evaluate as a string
         obj = xexpr.evaluate(context);
       } catch (XPathExpressionException ex1) {
         throw new XPathException(ex1);
       }
     }
     if (obj instanceof NodeList) {
       NodeList list = (NodeList) obj;
       int len = list.getLength();
       if (len == 1)
         return unmap(list.item(0));
       return makeList(list);
     }
     // try some other alternatives
     String s = ((String) obj).trim();
     if ("true".equals(s))
       return Boolean.TRUE;
     if ("false".equals(s))
       return Boolean.FALSE;
     Double d = tryNumber(s);
     if (d != null)
       return d;
     return s;
   }
   
   private Double tryNumber(String s) {
     if ("INF".equals(s) || "+INF".equals(s))
       s = "Infinity";
     else if ("-INF".equals(s))
       s = "-Infinity";
     try {
       return Double.valueOf(s);
     } catch (NumberFormatException e) {
       return null;
     }
     
   }
 
   /**
    * Return boolean result of evaluating expression.
    * 
    * @param xpathContext must be an org.dom4j.Node or null if expression contains
    * a Node-valued variable that can be used to establish the context.
    * @return boolean
    * @throws XPathException if errors detected
    */
   public boolean booleanValueOf(Object xpathContext) throws XPathException {
     Node context = getNode(xpathContext);
     XPathExpression xexpr = getXPathExpression();
     Object obj;
     try {
       obj = xexpr.evaluate(context, XPathConstants.BOOLEAN);
     } catch (XPathExpressionException ex) {
       throw new XPathException(ex);
     }
     if (obj instanceof Boolean)
       return (Boolean) obj;
     else
       throw new XPathException("result not a Boolean, returned "+obj.getClass().getName());
   }
 
   /**
    * Return double result of evaluating expression. Any number is converted to double.
    * @param xpathContext must be an org.dom4j.Node or null if expression contains
    * a Node-valued variable that can be used to establish the context.
    * @return double
    * @throws XPathException 
    */
   public double numberValueOf(Object xpathContext) throws XPathException {
     Node context = getNode(xpathContext);
     XPathExpression xexpr = getXPathExpression();
     Object obj;
     try {
       obj = xexpr.evaluate(context, XPathConstants.BOOLEAN);
     } catch (XPathExpressionException ex) {
       throw new XPathException(ex);
     }
     if (obj instanceof Double)
       return (Double) obj;
     else
       throw new XPathException("result not a Double, returned "+obj.getClass().getName());
   }
 
   /**
    * Return String result of evaluating expression.
    * @param xpathContext must be an org.dom4j.Node or null if expression contains
    * a Node-valued variable that can be used to establish the context.
    * @return String
    * @throws XPathException 
    */
   public String stringValueOf(Object xpathContext) throws XPathException {
     Node context = getNode(xpathContext);
     XPathExpression xexpr = getXPathExpression();
     try {
       return xexpr.evaluate(xpathContext);
     } catch (XPathExpressionException ex) {
       throw new XPathException(ex);
     }
   }
 
   /**
    * Return Node result of evaluating expression. If the result has more than
    * one Node, one of the Nodes is returned. If result has no Nodes,
    * throws XPathException.
    * 
    * @param xpathContext must be an org.dom4j.Node or null if expression contains
    * a Node-valued variable that can be used to establish the context.
    * @return Node
    * @throws XPathException 
    */
   public org.dom4j.Node selectSingleNode(Object xpathContext) throws XPathException {
     Node context = getNode(xpathContext);
     XPathExpression xexpr = getXPathExpression();
     Object obj;
     try {
       obj = xexpr.evaluate(context, XPathConstants.NODE);
     } catch (XPathExpressionException ex) {
       throw new XPathException(ex);
     }
     if (obj instanceof Node)
       return unmap((Node)obj);
     if (obj instanceof NodeList) {
       NodeList nodeList = (NodeList) obj;
       if (nodeList.getLength() == 0)
         throw new XPathException("returned empty NodeList");
       return unmap(nodeList.item(0));
     } else
      throw new XPathException("result not a node, returned "+(obj == null ? "null" : obj.getClass().getName()));
   }
   
   /**
    * Return List of Node result of evaluating expression.
    * 
    * @param xpathContext must be an org.dom4j.Node or null if expression contains
    * a Node-valued variable that can be used to establish the context.
    * @return List of Nodes, may be empty
    * @throws XPathException 
    */
   public List selectNodes(Object xpathContext) throws XPathException {
     Node context = getNode(xpathContext);
     XPathExpression xexpr = getXPathExpression();
     Object obj;
     try {
      obj = xexpr.evaluate(context, XPathConstants.NODESET);
     } catch (XPathExpressionException ex) {
       throw new XPathException(ex);
     }
     if (obj instanceof Node)
       return makeList((Node) obj);
     if (obj instanceof NodeList)
       return makeList((NodeList) obj);
    else if (obj == null)
      return Collections.EMPTY_LIST;
     else
       throw new XPathException("result not a node list, returned "+obj.getClass().getName());
   }
   
   private List makeList(NodeList nodeList) {
       List nodes = new ArrayList();
       for (int i = 0, n = nodeList.getLength(); i < n; i++)
         nodes.add(unmap(nodeList.item(i)));
       return nodes;
   }
   
   private List makeList(Node node) {
       org.dom4j.Node d4jNode = unmap((Node) node);
       ArrayList<org.dom4j.Node> list = new ArrayList<org.dom4j.Node>();
       list.add(d4jNode);
       return list;
   }
   
   /**
    * @return XPath expression
    */
   public String toString() {
     return expr;
   }
 
   /**
    * Reset the XPath expression. Used for debugging.
    * @param expr 
    */
   public void setExpr(String expr) {
     this.expr = expr;
     xpathExpr = null;
   }
 }
