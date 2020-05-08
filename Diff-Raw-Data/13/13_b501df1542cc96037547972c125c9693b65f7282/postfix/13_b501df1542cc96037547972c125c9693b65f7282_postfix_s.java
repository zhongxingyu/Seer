 import java.util.ArrayList;
 import java.util.List;
 import java.util.Arrays;
 import java.util.Stack;
 
public class program2 {
   private static final String[] OPERATORS = new String[] {"+", "-", "*", "/"};
   private static final int NUM_OPERANDS = 2;
   private static Stack<String> stack = new Stack<String>();
   private static Node top;
   private static Stack<Node> treeStack = new Stack<Node>();
   private static int result;
 
   private static class Node {
     public Node left;
     public Node right;
     public String value;
 
     public Node(String val, Node l, Node r) {
       value = val;
       left = l;
       right = r;
     }
 
     public Node(String val) {
       this(val, null, null);
     }
   }
 
   private static String calculate(String leftStr, String rightStr, String operator) {
     int left = Integer.parseInt(leftStr);
     int right = Integer.parseInt(rightStr);
    int result = null;
     switch (Arrays.asList(OPERATORS).indexOf(operator)) {
       case 0:
         result = left + right;
         break;
       case 1:
         result = left - right;
         break;
       case 2:
         result = left * right;
         break;
       case 3:
         result = left / right;
         break;
     }
     return String.valueOf(result);
   }
 
   private static boolean isOperator(String str) {
     return Arrays.asList(OPERATORS).contains(str);
   }
 
   private static void printError() {
     System.out.println("Expression error!");
   }
 
   private static List preOrder(Node node) {
     List preList = new ArrayList();
     if (node == null) {
       return preList;
     }
     preList.add(node.value);
     preList.addAll(preOrder(node.left));
     preList.addAll(preOrder(node.right));
     return preList;
   }
 
   private static List inOrder(Node node) {
     List inList = new ArrayList();
     if (node == null) {
       return inList;
     }
     inList.addAll(inOrder(node.left));
     inList.add(node.value);
     inList.addAll(inOrder(node.right));
     return inList;
   }
 
   private static String listToString(List list) {
     String str = "";
     for (int i = 0; i < list.size();i++) {
       if (i != list.size()) {
         str += list.get(i) + " ";
       } else {
         str += list.get(i);
       }
     }
     return str;
   }
 
   public static void main(String [] args) {
     if (args.length < 1) {
       System.out.println("Please input expression!");
       return;
     }
     System.out.println("Input expression: " + args[0] + "\n");
     String[] postArray = args[0].split(" ");
     for(int i = 0;i < postArray.length;i++) {
       if (isOperator(postArray[i])) {
         if (stack.size() < NUM_OPERANDS) {
           printError();
           return;
         } else {
           // Calculate the result
           String rightStr = stack.pop();
           String leftStr = stack.pop();
           String subResult = calculate(leftStr, rightStr, postArray[i]);
           if (subResult == "") {
             printError();
             return;
           } else {
             stack.push(subResult);
           }
 
           // Build tree
           Node rNode = treeStack.pop();
           Node lNode = treeStack.pop();
           Node subNode = new Node(postArray[i], lNode, rNode);
           treeStack.push(subNode);
         }
       } else {
         // Is a number
         treeStack.push(new Node(postArray[i]));
         stack.push(postArray[i]);
       }
     }
     if (stack.size() == 1) {
       result = Integer.parseInt(stack.pop());
       top = treeStack.pop();
     } else {
       printError();
       return;
     }
     List preList = preOrder(top);
     List inList = inOrder(top);
    System.out.println("Pre-fix: " + listToString(preList));
    System.out.println("In-fix: " + listToString(inList));
     System.out.println("Post-fix: " + args[0]);
     System.out.println("Calculating result: " + result);
   }
 }
