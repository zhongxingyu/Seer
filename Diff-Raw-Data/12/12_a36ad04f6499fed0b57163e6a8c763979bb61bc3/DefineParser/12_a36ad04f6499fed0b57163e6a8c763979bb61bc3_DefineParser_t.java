 package frontend.parsers;
 
 import frontend.Parser;
 import frontend.Scanner;
 import frontend.Token;
 import intermediate.IntermediateCode;
 import intermediate.SymbolTableStack;
 
 import java.io.IOException;
 
 public class DefineParser extends Parser {
     public DefineParser(SymbolTableStack symbolTableStack, Scanner scanner) {
         super(symbolTableStack, scanner);
     }
 
     @Override
     public IntermediateCode parse() throws IOException {
         IntermediateCode rootNode = null;
 
         try {
             Token token = currentToken();
             rootNode = new IntermediateCode();
             IntermediateCode newNode = new IntermediateCode(); // Root node
             newNode.setText(token.getText());
             rootNode.setCar(newNode); // CAR node containing text = "define"
             token = nextToken(); // Consume define
             newNode = new IntermediateCode();
             rootNode.setCdr(newNode); // CDR node which has a CAR node containing identifier name
             newNode.setCar(new IntermediateCode()); // CADR of root with text = <identifier name>
             newNode.getCar().setText(token.getText());
            token = nextToken(); // Consume identifier name
 
             switch (token.getType()) {
                 case LEFT_PAREN:
                    newNode.setCdr((new ListParser(symbolTableStack, scanner)).parse());
                     break;
                case RESERVED_SYMBOL:
                 case REGULAR_SYMBOL:
                     // Enter something into symbol table
                     break;
                 default:
                     // Do something else if not one of the above
             }
         } catch (IOException ex) { ex.printStackTrace(); }
 
         return rootNode;
     }
 }
