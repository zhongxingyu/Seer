 /*
  * Copyright (c) 2013. AgileApes (http://www.agileapes.scom/), and
  * associated organization.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this
  * software and associated documentation files (the "Software"), to deal in the Software
  * without restriction, including without limitation the rights to use, copy, modify,
  * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies
  * or substantial portions of the Software.
  */
 
 package com.agileapes.couteau.xml.print;
 
 import com.agileapes.couteau.graph.tree.node.TreeNode;
 import com.agileapes.couteau.xml.node.NodeType;
 import com.agileapes.couteau.xml.node.XmlNode;
 
 import java.io.PrintStream;
 
 /**
  * This class facilitates the process of printing out correctly indented XML documents to the output
  *
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (2013/7/30, 1:02)
  */
 public class XmlPrinter {
 
     private final PrintStream output;
 
     public XmlPrinter() {
         this(System.out);
     }
 
     public XmlPrinter(PrintStream output) {
         this.output = output;
     }
 
     private static boolean printInline(XmlNode node) {
         if (node == null) {
             return false;
         }
         for (TreeNode treeNode : node.getChildren()) {
             final XmlNode xmlNode = (XmlNode) treeNode;
             if (xmlNode.getNodeType().equals(NodeType.TEXT_NODE)) {
                 return true;
             }
         }
         return printInline((XmlNode) node.getParent());
     }
 
     public void print(XmlNode node) {
         if (node == null) {
             return;
         }
         if (node.getNodeType().equals(NodeType.TEXT_NODE)) {
             output.print(node.getNodeValue());
             return;
         }
         final int indent = node.getDepth();
         if (!printInline((XmlNode) node.getParent())) {
             for (int i = 0; i < indent; i ++) {
                 output.print("\t");
             }
         }
         output.print("<" + node.getNodeName());
         for (String attribute : node.getAttributeNames()) {
             output.print(" " + attribute + "=\"");
             final String value = node.getAttribute(attribute);
             if (value != null) {
                output.print(value.replace("\"", "&quote;"));
             }
             output.print('"');
         }
         if (node.getChildren().isEmpty()) {
             output.print(" /");
         }
         output.print(">");
         if (!printInline(node)) {
             output.println();
         }
         for (TreeNode treeNode : node.getChildren()) {
             print((XmlNode) treeNode);
         }
         if (!node.getChildren().isEmpty()) {
             if (!printInline(node)) {
                 for (int i = 0; i < indent; i ++) {
                     output.print("\t");
                 }
             }
             output.print("</" + node.getNodeName() + ">");
             if (!printInline((XmlNode) node.getParent())) {
                 output.println();
             }
         }
     }
 
 }
