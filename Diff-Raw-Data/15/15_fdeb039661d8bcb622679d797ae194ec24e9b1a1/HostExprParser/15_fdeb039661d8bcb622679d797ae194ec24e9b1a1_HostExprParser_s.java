 package com.pivotal.hamster.common;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class HostExprParser {
   
   static enum NodeType {
     PLAIN,
     ENUM
   }
   
   static class Node {
     String[] content;
     NodeType type;
     
     public Node(String content) {
       this.content = new String[] { content };
       this.type = NodeType.PLAIN;
     }
     
     public Node(String[] content, NodeType type) {
       this.content = content;
       this.type = type;
     }
   }
   
   static void processSlice(String slice, List<String> list) throws IOException {
     // remove leading and trailing spaces
     slice = slice.trim(); 
     
     if (slice.indexOf('-') >= 0) {
       // is it a xyz-abc style?
       boolean isRange = true;
       
       String left = slice.substring(0, slice.indexOf('-'));
       String right = slice.substring(slice.indexOf('-') + 1, slice.length());
       
       isRange = ((left != null) && (!left.isEmpty()) && (right != null) && (!right.isEmpty()));
       
       if (isRange) {
         // check if left, right are all number
         for (int i = 0; i < left.length(); i++) {
           if (!Character.isDigit(left.charAt(i))) {
             isRange = false;
             break;
           }
         }
       }
       
       if (isRange) {
         for (int i = 0; i < right.length(); i++) {
           if (!Character.isDigit(right.charAt(i))) {
             isRange = false;
             break;
           }
         }
       }
       
       if (isRange) {
         int leftInt = Integer.parseInt(left);
         int rightInt = Integer.parseInt(right);
         if (leftInt > rightInt) {
           throw new IOException(String.format("left bound:%d is larger than right bound:%d", leftInt, rightInt));
         }
         
         // check if length of left is <= length of right
         if (left.length() > right.length()) {
           throw new IOException(String.format("left string:[%s] length is larger than right string:[%s] length", left, right));
         }
         
         // check if left or right is negtive
         if (leftInt < 0 || rightInt < 0) {
           throw new IOException(String.format("find negtive int in range, left:%d, right%d", leftInt, rightInt));
         }
         
         // check if we have padding '0' in starting of left 
        if (left.startsWith("0")) {
           // if there's padding in left, we need make sure left and right has same length
           if (left.length() != right.length()) {
             throw new IOException(String.format("there's padding in left, so you need make sure left:[%s] and right:[%s] has same length", left, right));
           }
           
           // do it in padding way
           for (int i = leftInt; i <= rightInt; i++) {
             String numStr = String.valueOf(i);
             if (numStr.length() < left.length()) {
               int paddingNum = left.length() - numStr.length();
               for (int j = 0; j < paddingNum; j++) {
                 numStr = "0" + numStr;
               }
             }
             list.add(numStr);
           }
         } else {
           // do it in non-padding way
           for (int i = leftInt; i <= rightInt; i++) {
             list.add(String.valueOf(i));
           }
         }
         
         return;
       }
     }
     
     // it's just a string, add it
     list.add(slice);
   }
   
   static void getResult(List<Node> nodes, List<String> result, int offset, String current) {
     if (offset == nodes.size()) {
       result.add(current);
     } else {
       Node node = nodes.get(offset);
       if (node.type == NodeType.PLAIN) {
         getResult(nodes, result, offset + 1, current + node.content[0]);
       } else {
         for (int i = 0; i < node.content.length; i++) {
           getResult(nodes, result, offset + 1, current + node.content[i]);
         }
       }
     }
   }
   
   /**
    * parse host expr to comma ',' splitted host list, without spaces in output 
    * @throws IOException
    */
   public static String parse(String hostExpr) throws IOException {
     int l = 0;
     int r = 0;
     List<Node> nodes = new ArrayList<Node>();
     
     while (l < hostExpr.length()) { 
       if (hostExpr.charAt(l) == '[') {
         // it's a expr to be expand
         r = hostExpr.indexOf(']', l);
         if (r == -1) {
           // report error
           throw new IOException("square brackets mismatch!");
         } else {
           // we need check if there's any ']' before ']' occurs
           int tmp = hostExpr.indexOf('[', l + 1);
           if (tmp > 0 && tmp < r) {
             throw new IOException("square brackets mismatch");
           }
         }
         
         // ok, we have matched '[' and ']'
         String expr = hostExpr.substring(l + 1, r);
         List<String> list = new ArrayList<String>();
         for (String slice : expr.split(",")) {
            processSlice(slice, list);
         }
         
         nodes.add(new Node(list.toArray(new String[0]), NodeType.ENUM));
         l = r + 1;
       } else if (hostExpr.charAt(l) != ']') {
         // it's a plain text
         r = hostExpr.indexOf('[', l);
         if (r < 0) {
           // no more expr(s)
           // we will double check if there's ']'
           if (hostExpr.indexOf(']', l) >= 0) {
             throw new IOException("square brackets mismatch");
           }
           
           // just treat the following text are plain text
           String text = hostExpr.substring(l, hostExpr.length());
           nodes.add(new Node(text));
           l = hostExpr.length();
         } else {
           // we will treat string from there to char before '[' are plain text
           String text = hostExpr.substring(l, hostExpr.indexOf('[', l));
           nodes.add(new Node(text));
           l = hostExpr.indexOf('[', l);
         }
       } else {
         throw new IOException("square brackets mismatch");
       }
     }
     
     // assemble results 
     List<String> result = new ArrayList<String>();
     getResult(nodes, result, 0, "");
     
     // get final comma splited string
     String finalResult = "";
     for (int i = 0; i < result.size() - 1; i++) {
       finalResult = finalResult + result.get(i) + ",";
     }
     finalResult = finalResult + result.get(result.size() - 1);
     
     return finalResult;
   }
 }
