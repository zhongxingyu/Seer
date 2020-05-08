 package frontend;
 
 import intermediate.IntermediateCode;
 import intermediate.SymbolTableEntry;
 import intermediate.SymbolTableEntryAttribute;
 import intermediate.SymbolTableStack;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Stack;
 
 public class Parser {
     protected SymbolTableStack symbolTableStack;
     protected ArrayList<IntermediateCode> topLevelLists;
     protected Scanner scanner;
     private Stack<Integer> parenthesisCount;
 
     public Parser(Scanner scanner) {
         topLevelLists = new ArrayList<IntermediateCode>();
         symbolTableStack = new SymbolTableStack();
         this.scanner = scanner;
        parenthesisCount = new Stack<Integer>();
     }
 
     public IntermediateCode parse() throws IOException {
         System.out.println("\n----------Printing Tokens---------\n");
 
         while (scanner.peekChar() != Source.EOF) {
             IntermediateCode root = parseList();
             topLevelLists.add(root);
         }
 
         return null;
     }
 
     public IntermediateCode parseList() {
         IntermediateCode rootNode = new IntermediateCode();
 
         try {
             Token token = nextToken();
             boolean noError = true;
 
             System.out.print("\t" + token.getText() + "\t");
 
             if (TokenType.RESERVED_WORDS.containsKey(token.getText())) {
                 System.out.println("Reserved Word");
             }
             else if (TokenType.RESERVED_SYMBOLS.containsKey(token.getText())) {
                 System.out.println("Reserved Symbol");
             }
             else if (token.getType() == TokenType.REGULAR_SYMBOL) {
                 System.out.println("Symbol");
             }
             else if (token.getType() == TokenType.INTEGER) {
                 System.out.println("Integer");
             }
             else if (token.getType() == TokenType.REAL) {
                 System.out.println("Real");
             }
 
             if (scanner.getPosition() == 0) {
                 System.out.println(scanner);
             }
 
             switch (token.getType()) {
                 case LEFT_PAREN:
                     if (!parenthesisCount.empty()) {
                         parenthesisCount.push(parenthesisCount.pop() + 1);
                     }
 
                     rootNode.setCar(parseList());
                     rootNode.setCdr(parseList());
 
                     // Linking parse tree with symbol table
                     if (rootNode.getCar() != null && rootNode.getCar().getType() != null) {
                         switch (rootNode.getCar().getType()) {
                             case DEFINE:
                                 //SymbolTableEntry entry = symbolTableStack.lookup(rootNode.getCdr().getCar().getText());
                                 //entry.put(SymbolTableEntryAttribute.VALUE, rootNode.getCdr().getCdr());
                                 break;
                             case LAMBDA:
                                 //SymbolTableEntry entry = symbolTableStack.lookup(rootNode.getCdr().getCdr().getCar().getText());
                                 //entry.put(SymbolTableEntryAttribute.VALUE, rootNode.getCdr().getCdr());
                                 break;
                             case LET:
                         }
                     }
 
                     break;
                 case RIGHT_PAREN:
                     if (parenthesisCount.peek() > 0) {
                         parenthesisCount.push(parenthesisCount.pop() - 1);
 
                         if (parenthesisCount.peek() == 0) {
                             parenthesisCount.pop();
 
                             // TODO: Comment this line out if you want to see parse tree for debugging.
                             // TODO: This should stay in the final version.
                             // symbolTableStack.pop();
                         }
                     }
                 case END_OF_FILE:
                     return null;
                 case DEFINE:
                 case LAMBDA:
                 case LET:
                     rootNode.setText(token.getText());
                     rootNode.setType(token.getType());
                     symbolTableStack.push();
 
                     if (!parenthesisCount.empty()) {
                         parenthesisCount.push(parenthesisCount.pop() - 1);
                     }
 
                     parenthesisCount.push(1);
                     break;
                 case RESERVED_SYMBOL:
                 case REGULAR_SYMBOL:
                     SymbolTableEntry symbol = symbolTableStack.lookupLocal(token.getText());
 
                     if (symbol != null) {
                         // If it gets in here, that means a variable was defined twice in the same SymbolTable.
                         // This would be an error.
 
                         //noError = false;
                     }
                     else {
                         symbol = symbolTableStack.lookup(token.getText());
 
                         if (symbol != null) {
                             // If it gets in here, that means the symbol has been defined elsewhere. So use it!
 
                             // TODO: This line is for debugging only. It should NOT be entered into local symbol table.
                             symbolTableStack.enterLocal(token.getText());
                         }
                         else {
                             symbolTableStack.enterLocal(token.getText());
                         }
                     }
                 default:
                     if (noError) {
                         rootNode.setCar(new IntermediateCode());
                         rootNode.getCar().setText(token.getText());
                         rootNode.getCar().setType(token.getType());
                     }
                     else {
                         noError = true;
                     }
 
                     rootNode.setCdr(parseList());
             }
         }
         catch (IOException e) {
             e.printStackTrace();
         }
 
         return rootNode;
     }
 
     /*
     public IntermediateCode parseList() throws IOException {
         IntermediateCode rootNode = null;
 
         try {
             Token token = nextToken();
             rootNode = new IntermediateCode();
             IntermediateCode newNode;
             SymbolTableEntry symbol;
 
             switch (token.getType()) {
                 case LEFT_PAREN:
                     rootNode.setCar(parseList());
                     rootNode.setCdr(parseList());
                     break;
                 case RIGHT_PAREN:
                     break;
                 case END_OF_FILE:
                     break;
                 case DEFINE:
                     newNode = new IntermediateCode();
                     newNode.setText(token.getText());
                     rootNode.setCar(newNode);
                     newNode = new IntermediateCode();
                     rootNode.setCdr(newNode);
                     token = nextToken(); // Consume define
                     newNode.setCar(new IntermediateCode());
                     newNode.getCar().setText(token.getText());
                     symbol = new SymbolTableEntry(token.getText(), symbolTable);
                     symbolTable.put(token.getText(), symbol);
                     token = nextToken(); // Consume identifier
                     newNode.setCdr(new IntermediateCode());
                     newNode.getCdr().setCar(parseList());
                     break;
                 case LAMBDA:
                     newNode = new IntermediateCode();
                     newNode.setText(token.getText());
                     rootNode.setCar(newNode);
                     newNode = new IntermediateCode();
                     rootNode.setCdr(newNode);
                     newNode = rootNode.getCdr();
                     newNode.setCar(new IntermediateCode());
                     newNode = newNode.getCar();
                     token = nextToken(); // Consume lambda
                     token = nextToken(); // Consume (
 
                     while (token.getType() == TokenType.REGULAR_SYMBOL) {
                         IntermediateCode temp = new IntermediateCode();
                         temp.setText(token.getText());
                         newNode.setCar(temp);
                         symbol = new SymbolTableEntry(token.getText(), symbolTable);
                         symbolTable.put(token.getText(), symbol);
                         token = nextToken(); // Consume identifier
 
                         if (token.getType() == TokenType.REGULAR_SYMBOL) {
                             newNode.setCdr(new IntermediateCode());
                             newNode = newNode.getCdr();
                         }
                     }
 
                     token = nextToken(); // Consume )
                     rootNode.getCdr().setCdr(new IntermediateCode());
                     rootNode.getCdr().getCdr().setCar(parseList());
                     break;
                 case LET:
                     newNode = new IntermediateCode();
                     newNode.setText(token.getText());
                     rootNode.setCar(newNode);
                     newNode = new IntermediateCode();
                     rootNode.setCdr(newNode);
                     newNode = rootNode.getCdr();
                     IntermediateCode resumeNode = newNode;
                     newNode.setCar(new IntermediateCode());
                     newNode = newNode.getCar();
                     token = nextToken(); // Consume let
                     token = nextToken(); // Consume (
 
                     while (token.getType() == TokenType.LEFT_PAREN) {
                         token = nextToken(); // Consume (
                         newNode.setCar(new IntermediateCode());
                         newNode.getCar().setText(token.getText());
                         symbol = new SymbolTableEntry(token.getText(), symbolTable);
                         symbolTable.put(token.getText(), symbol);
                         token = nextToken(); // Consume identifier
 
                         if (token.getType() == TokenType.LEFT_PAREN) {
                             newNode.setCdr(parseList());
                         }
                         else {
                             newNode.setCdr(new IntermediateCode());
                             newNode.getCdr().setCar(new IntermediateCode());
                             newNode.getCdr().getCar().setText(token.getText());
                         }
                     }
 
                     resumeNode.setCdr(parseList());
                     break;
                 case RESERVED_SYMBOL:
                 case REGULAR_SYMBOL:
                     symbol = new SymbolTableEntry(token.getText(), symbolTable);
                     symbolTable.put(token.getText(), symbol);
                     newNode = new IntermediateCode();
                     newNode.setText(token.getText());
                     rootNode.setCar(newNode);
                     token = nextToken(); // Consume the identifier
                     // Do something
                     break;
                 default:
                     // Do something else if not one of the above
             }
         } catch (IOException ex) { ex.printStackTrace(); }
 
         return rootNode;
     }
     */
 
     /*
     public IntermediateCode parseList() {
         IntermediateCode newNode = null;
 
         if(!initial && counter == 0) {
             initial = true;
             return null;
         }
         initial = false;
 
         try {
             Token token = nextToken(); // Consume (
             System.out.print("\t" + token.getText() + "\t");
 
             if (TokenType.RESERVED_WORDS.containsKey(token.getText())) {
                 System.out.println("Reserved Word");
             }
             else if (TokenType.RESERVED_SYMBOLS.containsKey(token.getText())) {
                 System.out.println("Reserved Symbol");
             }
             else if (token.getType() == TokenType.REGULAR_SYMBOL) {
                 System.out.println("Symbol");
             }
             else if (token.getType() == TokenType.INTEGER) {
                 System.out.println("Integer");
             }
             else if (token.getType() == TokenType.REAL) {
                 System.out.println("Real");
             }
 
             if (scanner.getPosition() == 0) {
                 System.out.println(scanner);
             }
 
             switch (token.getType()) {
                 case LEFT_PAREN:
                     counter++;
                     newNode = new IntermediateCode();
                     newNode.setCar(parseList());
                     newNode.setCdr(parseList());
                     break;
                 case RIGHT_PAREN:
                     counter--;
                 case END_OF_FILE:
                     break;
                 case REGULAR_SYMBOL:
                     SymbolTableEntry entry = new SymbolTableEntry(token.getText(), symbolTable);
                     symbolTable.put(token.getText(), entry);
                 default:
                     newNode = new IntermediateCode();
                     newNode.setText(token.getText());
                     newNode.setType(token.getType());
                     newNode.setCdr(parseList());
             }
         }
         catch (IOException e) {
             e.printStackTrace();
         }
 
         return newNode;
     }
     */
 
     public Scanner getScanner() {
         return scanner;
     }
 
     public ArrayList<IntermediateCode> getICodes() {
         return topLevelLists;
     }
 
     public SymbolTableStack getSymTabStack() {
         return symbolTableStack;
     }
 
     public Token currentToken() {
         return scanner.currentToken();
     }
 
     public Token nextToken() throws IOException {
         return scanner.nextToken();
     }
 }
