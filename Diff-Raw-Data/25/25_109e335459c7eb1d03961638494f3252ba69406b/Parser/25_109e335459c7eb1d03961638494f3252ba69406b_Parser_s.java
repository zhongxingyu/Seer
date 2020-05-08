 package frontend;
 
import frontend.parsers.*;
 import intermediate.IntermediateCode;
 import intermediate.SymbolTable;
 import intermediate.SymbolTableEntry;
 import intermediate.SymbolTableStack;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Parser {
     protected SymbolTableStack symbolTableStack;
     protected SymbolTable symbolTable;
     protected ArrayList<IntermediateCode> topLevelLists;
     protected Scanner scanner;
     private  int counter = 0;
     private boolean initial = true;
 
     public Parser(Scanner scanner) {
         topLevelLists = new ArrayList<IntermediateCode>();
         symbolTableStack = new SymbolTableStack();
         symbolTable = new SymbolTable();
         symbolTableStack.push(symbolTable);
         this.scanner = scanner;
     }
 
     public Parser(SymbolTableStack symbolTableStack, Scanner scanner) {
         this.symbolTableStack = symbolTableStack;
         this.scanner = scanner;
         topLevelLists = new ArrayList<IntermediateCode>();
         symbolTable = new SymbolTable();
         symbolTableStack.push(symbolTable);
     }
 
     public IntermediateCode parse() throws IOException {
         System.out.println("\n----------Printing Tokens---------\n");
         Token token = nextToken(); // Get first character
 
         while (scanner.peekChar() != Source.EOF) {
             IntermediateCode root = parseList();
             topLevelLists.add(root);
         }
 
         return null;
     }
 
     public IntermediateCode parseList() throws IOException {
         IntermediateCode rootNode = null;
 
         try {
             Token token = nextToken(); // Consume (
             rootNode = new IntermediateCode();
             IntermediateCode newNode;
             SymbolTableEntry symbol;
 
             switch (token.getType()) {
                 case LEFT_PAREN:
                    rootNode.setCar(parse());
                    rootNode.setCdr(parse());
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
                    newNode.setCdr(parse());
                     break;
                 case LAMBDA:
                     newNode = new IntermediateCode();
                     newNode.setText(token.getText());
                     rootNode.setCar(newNode);
                     newNode = new IntermediateCode();
                     rootNode.setCdr(newNode);
                     token = nextToken(); // Consume lambda
                     token = nextToken(); // Consume (
 
                     while (token.getType() == TokenType.REGULAR_SYMBOL) {
                         IntermediateCode temp = new IntermediateCode();
                         temp.setText(token.getText());
                         newNode.setCar(temp);
                         symbol = new SymbolTableEntry(token.getText(), symbolTable);
                         symbolTable.put(token.getText(), symbol);
                         token = nextToken();
 
                         if (token.getType() == TokenType.REGULAR_SYMBOL) {
                             newNode.setCdr(new IntermediateCode());
                             newNode = newNode.getCdr();
                         }
                     }
 
                     token = nextToken(); // Consume )
                    rootNode.getCdr().setCdr(parse());
                     break;
                 case LET:
                     newNode = new IntermediateCode();
                     newNode.setText(token.getText());
                     rootNode.setCar(newNode);
                     token = nextToken(); // Consume let
                    rootNode.setCdr(parse());
                     break;
                 case RESERVED_SYMBOL:
                 case REGULAR_SYMBOL:
                     symbol = new SymbolTableEntry(token.getText(), symbolTable);
                     symbolTable.put(token.getText(), symbol);
                     // Do something
                     break;
                 default:
                     // Do something else if not one of the above
             }
         } catch (IOException ex) { ex.printStackTrace(); }
 
         return rootNode;
     }
 
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
