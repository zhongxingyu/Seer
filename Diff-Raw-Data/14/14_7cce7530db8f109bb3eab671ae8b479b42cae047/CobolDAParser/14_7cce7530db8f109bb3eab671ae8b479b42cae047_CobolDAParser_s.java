 package jpcamara.da.cobol;
 
 import jpcamara.da.DataAreaNode;
 import jpcamara.da.DataAreaToken;
 import jpcamara.da.DataAreaTokenType;
 import jpcamara.da.DataAreaValueType;
 
 import java.io.*;
 import java.util.LinkedList;
 
 public class CobolDAParser {
     private DataAreaToken currentToken;
     private int fillerIncrement = 1;
     private int redefineIncrement = 1;
 
     public CobolDAParser(String content, String symbolReplace) {
         currentToken = DataAreaToken.tokenize(content, symbolReplace).removeTokens(DataAreaTokenType.COMMENT);
     }
 
     public CobolDAParser(File fileContent, String symbolReplace) throws IOException {
         StringBuilder content = new StringBuilder();
         BufferedReader reader = new BufferedReader(new FileReader(fileContent));
         String line = null;
         while ((line = reader.readLine()) != null) {
            content.append(line);
         }
         currentToken = DataAreaToken.tokenize(content.toString(), symbolReplace).removeTokens(DataAreaTokenType.COMMENT);
     } 
 
     public DataAreaNode parseDA() {
         if (currentToken.isEOF()) {
             throw new RuntimeException("DA is EOF at the beginning. " + currentToken);
         }
 
         LinkedList<Integer> levels = new LinkedList<Integer>();
         DataAreaNode head = new DataAreaNode();
         DataAreaNode currentNode = head;
         
         while (currentToken != null && !currentToken.isEOF()) {
             DataAreaToken previous = currentToken.previousToken();
 
             //parse level + start new node
             if (previous == null || previous.isTerminator()) {
                 if (currentNode.getName() == null) {
                     String name = currentNode.isRedefine() ?
                             "REDEFINES-" + (redefineIncrement++) :
                             "FILLER-" + (fillerIncrement++);
                     currentNode.setName(name);
                 }
                 if (!currentToken.isNumber()) {
                     throw new RuntimeException("DA Node does not start with a level. " + currentToken);
                 }
                 currentNode = parseLevelInfo(currentNode, levels);
             }
             //get name of the node
             else if (previous != null
                     && previous.isNumber()
                     && currentToken.isSymbol()) {
                 currentNode.setName(currentToken.getValue());
             }
             //get PIC info about the node
             else if (previous != null && previous.isKeyword() && previous.getValue().equals("PIC")) {
                 parsePicInfo(currentNode);
             }
             //parse OCCURS
             else if (previous != null && previous.isKeyword() && previous.getValue().equals("OCCURS")) {
                 if (!currentToken.isNumber()) {
                     throw new RuntimeException("Expected OCCURS to be followed by a number.");
                 }
                 currentNode.setOccurs(Integer.parseInt(currentToken.getValue()));
             }
             //parse REDEFINES
             else if (previous != null && previous.isKeyword() && previous.getValue().equals("REDEFINES")) {
                 String redefinedSibling = currentToken.getValue();
                 DataAreaNode parentNode = currentNode.getParent(), redefined = null;
                 for (DataAreaNode child : parentNode.getChildren()) {
                     if (child.getName().equals(redefinedSibling)) {
                         redefined = child;
                         break;
                     }
                 }
                 if (redefined == null) {
                     throw new RuntimeException("No definition found to redefine. " + redefinedSibling);
                 }
                 currentNode.setRedefine(true);
                 currentNode.setRedefinedNode(redefined);
             }
             consumeToken();
         }
 
         calculateLengths(head);
 
         return head;
     }
 
     private int calculateLengths(DataAreaNode currentNode) {
         int length = 0;
         for (DataAreaNode child : currentNode.getChildren()) {
             if (child.getOccurs() > 1 && child.getLength() != 0) {
                 System.out.println();
             }
 //            if (child.isRedefine()) {
 //                continue;
 //            }
             if (!child.getChildren().isEmpty()) {
                 int childrenLength = calculateLengths(child);
                 if (!child.isRedefine()) {
                     length += childrenLength;
                 }
             } else {
                 if (!child.isRedefine()) {
                     length += child.getLength() * child.getOccurs();
                 }
 
             }
         }
         if (currentNode.getChildren().isEmpty()) {
             length = currentNode.getLength();
         }
         length *= currentNode.getOccurs();
         currentNode.setLength(length);
         return length;
     }
 
     private void parsePicInfo(DataAreaNode currentNode) {
         if (currentToken.isNumberDef()) {
             currentNode.setType(DataAreaValueType.NUMBER);
         } else {
             currentNode.setType(DataAreaValueType.STRING);
         }
         currentNode.setLength(parseLength());
     }
     
     private int parseLength() {
         char[] definition = currentToken.getValue().toCharArray();
         char picChar = currentToken.isNumberDef() ? '9' : 'X';
         int length = 0, i = 0;
         
         while (i < definition.length) {
             if (definition[i] == picChar) {
                 if ((i + 1) < definition.length && definition[i + 1] == '(') {
                     //don't increment or we count it too many times
                 } else {
                     length++;
                 }
             }
             if (currentToken.isNumberDef() && definition[i] == 'V') { //implied decimal!
                 i++;
             }
             if (definition[i] == '(') {
                 StringBuilder number = new StringBuilder();
                 while (definition[i] != ')') {
                     if (Character.isDigit(definition[i])) {
                         number.append(definition[i]);
                     }
                     i++;
                 }
                 length += Integer.parseInt(number.toString());
             }
             i++;
         }
         return length;
     }
 
     private DataAreaNode parseLevelInfo(DataAreaNode currentNode, LinkedList<Integer> levels) {
         DataAreaNode tempNode = new DataAreaNode();
         DataAreaNode currentNodeLocal = currentNode;
 
         int level = Integer.parseInt(currentToken.getValue());
         if (levels.peek() == null) { //first element
             levels.push(level);
             return currentNodeLocal;
         }
         else if (levels.peek() == level) { //same level
             tempNode.setParent(currentNodeLocal.getParent());
             currentNodeLocal.getParent().getChildren().add(tempNode);
             currentNodeLocal.setSibling(tempNode);
             return tempNode;
         }
         else if (levels.peek() < level) { //lower level
             levels.push(level);
             tempNode.setParent(currentNodeLocal);
             currentNodeLocal.getChildren().add(tempNode);
             return tempNode;
         }
         else if (levels.peek() > level) { //going back up, find which level
             while (levels.peek() != null && !(levels.peek() < level)) {
                 currentNodeLocal = currentNodeLocal.getParent();
                 levels.pop();
             }
             return parseLevelInfo(currentNodeLocal, levels); //reuse previous ifs by recursion
         }
         throw new RuntimeException("Level info didn't make sense.");
     }
 
     protected boolean match(String val) {
         boolean match = currentToken.match(val);
         if (match) {
             consumeToken();
         }
         return match;
     }
 
     private void consumeToken() {
         currentToken = currentToken.nextToken();
     }
 }
