 package jpcamara.da.cobol;
 
 import jpcamara.da.DataAreaNode;
 import jpcamara.da.DataAreaToken;
 import jpcamara.da.DataAreaTokenType;
 import jpcamara.da.DataAreaValueType;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class CobolDATokenizer {
     private String currentStringValue;
     private String contents;
     private int line;
     private int col;
     private int offset;
     private int currentCol;
     private int currentStartOffset;
     private int currentEndOffset;
     private String symbolReplace;
     private DataAreaTokenType type;
     private List<String> COBOL_KEYWORDS = Arrays.asList(
         "PIC", "REDEFINES", "OCCURS", "TIMES", "INDEXED", "BY" //"VALUE"
     );
 
     public CobolDATokenizer(String contents, String symbolReplace) {
         this.contents = contents;
         this.line = 1;
         this.col = 1;
         this.offset = 0;
         this.currentStartOffset = 0;
         this.currentEndOffset = 0;
         this.currentStringValue = null;
         this.symbolReplace = symbolReplace;
     }
 
     public boolean hasMoreTokens() {
         return moveToNextToken();
     }
 
     private boolean moveToNextToken() {
         eatWhitespace();
 
         if (atEndOfInput()) {
             return false;
         }
 
         currentStartOffset = offset;
         currentCol = col;
 
         if (consumeEightyEight()) {
             //remove 88's
             type = DataAreaTokenType.COMMENT;
         }
         else if (consumeNumberDef()) {
             type  = DataAreaTokenType.NUMBER_DEF;
         }
         else if (consumeStringDef()) {
             type  = DataAreaTokenType.STRING_DEF;
         }
         else if (consumeNumber()) {
             type  = DataAreaTokenType.NUMBER;
         }
         else if (consumeOperator()) {
             type  = DataAreaTokenType.OPERATOR;
         }
         else if (consumeKeyword()) {
             type  = DataAreaTokenType.KEYWORD;
         }
         else if (consumeSymbol()) {
             type  = DataAreaTokenType.SYMBOL;
         }
         else if (consumeComment()) {
             type  = DataAreaTokenType.COMMENT;
         }
         else {
             type  = DataAreaTokenType.UNKNOWN;
             consumeChar();
         }
 
         currentEndOffset = offset;
         currentStringValue = contents.substring(currentStartOffset, currentEndOffset);
         if (type == DataAreaTokenType.SYMBOL) {
             currentStringValue = currentStringValue.replaceFirst(symbolReplace, "");
         }
 
         return true;
     }
 
     private boolean consumeStringDef() {
         if (type == DataAreaTokenType.KEYWORD && currentStringValue.equals("PIC")
                 && currentChar() == 'X') {
             while (currentChar() == 'X' || currentChar() == '('
                     || currentChar() == ')' || Character.isDigit(currentChar())) {
                 incrementOffset();
             }
             return true;
         }
         return false;
     }
 
     private boolean consumeNumberDef() {
         if (type == DataAreaTokenType.KEYWORD && currentStringValue.equals("PIC")
                 && currentChar() == '9') {
             while (currentChar() == 'V' || currentChar() == '('
                     || currentChar() == ')' || Character.isDigit(currentChar())) {
                 incrementOffset();
             }
             return true;
         }
         return false;
     }
 
     //TODO algorithm could prove faulty in some cases, and we may want to leave 88's
     private boolean consumeEightyEight() {
         if (!atEndOfInput() && currentChar() == '8' && canPeek(1) && peek() == '8') {
             consumeLine();
             return true;
         }
         return false;
     }
 
     //TODO double loop for each keyword + kinda nasty processing == bogus
     private boolean consumeKeyword() {
         for (String key : COBOL_KEYWORDS) {
             char[] keyword = key.toCharArray();
             boolean hasKeyword = false;
             for (int i = 0; 
                  i < keyword.length && !atEndOfInput() && canPeek(i); i++) {
                 char peeked = peek(i);
                 if (keyword[i] == peek(i)) {
                     hasKeyword = true;
                 } else {
                     hasKeyword = false;
                     break;
                 }
             }
             if (hasKeyword) {
                 if (!atEndOfInput() && canPeek(keyword.length) &&
                         Character.isWhitespace(peek(keyword.length)) || peek(keyword.length) == '.') {
                     for (int i = 0; i < keyword.length; i++) {
                         consumeChar();
                     }
                     return true;
                 }
             }
         }
         
         return false;
     }
 
     private boolean consumeNumber() {
         if (Character.isDigit(currentChar())) {
             consumeDigit();
             return true;
         }
         return false;
     }
 
     private boolean consumeComment()
     {
         if (!atEndOfInput() && currentChar() == '*') {
             consumeLine();
             return true;
         }
         return false;
     }
 
     private void consumeLine()
     {
         while( !atEndOfInput() && currentChar() != '\n' )
         {
             incrementOffset();
         }
     }
 
     private void consumeChar() {
         incrementOffset();
     }
 
     private char peek() {
         return contents.charAt(offset + 1);
     }
 
     private void consumeDigit() {
         while (!atEndOfInput() && Character.isDigit(currentChar())) {
             incrementOffset();
         }
     }
 
     private boolean consumeString() {
         return false;
     }
 
     private boolean consumeSymbol() {
         //TODO remove replacement chars like :P: instead of consuming them in the symbol
         //TODO made sure the previous token was a number
         if (//_type == CobolDATokenType.NUMBER
 //                ||
 //                (_type == CobolDATokenType.KEYWORD && "REDEFINES".equals(_currentStringValue)))
                 //&&
                 Character.isLetter(currentChar()) || currentChar() == ':') {
             incrementOffset();
             //TODO java identifier does not conform to cobol identifiers - need to create cobol identifier list
             while (!atEndOfInput() &&
                     (Character.isJavaIdentifierPart(currentChar()) || currentChar() == '-' || currentChar() == ':')) {
                 incrementOffset();
             }
             return true;
         }
         return false;
     }
 
     private boolean consumeOperator() {
         if (currentChar() == '.') {
             incrementOffset();
             return true;
         }
         return false;
     }
 
     private char peek(int i) {
         return contents.charAt(offset + i);
     }
 
     private boolean canPeek(int count) {
         return offset + count < contents.length();
     }
 
 
     private boolean atEndOfInput() {
         return contents == null || offset >= contents.length();
     }
 
     private void eatWhitespace() {
         while (!atEndOfInput() && Character.isWhitespace(currentChar())) {
            if ('\n' == currentChar()) {
                 line++;
                 col = 0;
             }
             incrementOffset();
         }
     }
 
     private void incrementOffset() {
         offset++;
         col++;
     }
 
     private char lastChar() {
         return contents.charAt(offset - 1);
     }
 
     private char currentChar() {
         return contents.charAt(offset);
     }
 
     public DataAreaToken nextToken() {
         return new DataAreaToken(type, currentStringValue, line, currentCol, currentStartOffset, currentEndOffset);
     }
     
     public static void main(String[] args) throws Exception {
         String newLine = System.getProperty("line.separator");
         String da = "";
 
         long startTime = System.currentTimeMillis();
         DataAreaNode node = new CobolDAParser(da, ":P:").parseDA();
         long endTime = System.currentTimeMillis();
         System.out.println(endTime - startTime);
 
         startTime = System.currentTimeMillis();
         node = new CobolDAParser(da, ":P:").parseDA();
         endTime = System.currentTimeMillis();
         System.out.println(endTime - startTime);
 //        CobolDAToken token = CobolDAToken.tokenize(da);
 //        while (token.nextToken() != null) {
 //            if (token.isSymbol()) {
 //                System.out.println(token.getValue());
 //            }
 //            token = token.nextToken();
 //        }
 
 //        System.out.println();
 //        print(node, "");
 //        createLayout(node, "", 0);
 //        System.out.println(node.getLength());
     }
 
     private static int createLayout(DataAreaNode node, String indexSoFar, int offset) {
         int newOffset = offset;
         for (DataAreaNode child : node.getChildren()) {
             DataAreaNode currentChild = child;
             if (currentChild.isRedefine()) {
                 continue;
             }
 //            if (child.isRedefine()) {
 //                continue;
 //            }
             int occurs = currentChild.getOccurs();
             String name = currentChild.getName().replaceAll(":P:-", "").replaceAll("-", "_").toLowerCase() + indexSoFar.replaceAll("\\[", "_").replaceAll("\\]", "");
             if (currentChild.getType() == DataAreaValueType.NONE) {
                 if (occurs == 1) {
                     newOffset = createLayout(currentChild, indexSoFar, newOffset);
                 } else {
                     for (int i = 0; i < occurs; i++) {
                         newOffset = createLayout(currentChild, indexSoFar + "[" + i + "]", newOffset);
                     }
                 }
             } else {
                 if (occurs == 1) {
                     newOffset += currentChild.getLength();
                     System.out.println(name + "=" + currentChild.getName() + indexSoFar + ";" + newOffset + ";" + currentChild.getLength());
                 } else {
                     for (int i = 0; i < occurs; i++) {
                         newOffset += currentChild.getLength();
                         System.out.println(name + "=" + currentChild.getName() + indexSoFar + ";" + "[" + i + "]" + newOffset + ";" + currentChild.getLength());
                     }
                 }
             }
         }
         return newOffset;
     }
 
     private static void print(DataAreaNode currentNode, String indent) {
         System.out.println(indent + currentNode.getName() + " Length = " + currentNode.getLength() +
             " Redefine = " + currentNode.isRedefine() + " Occurs = " + currentNode.getOccurs());
         for (DataAreaNode child : currentNode.getChildren()) {
             print(child, indent + "  ");
         }
     }
 }
