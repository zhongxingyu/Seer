 package cs444.parser;
 
 import java.util.Arrays;
 
 import cs444.parser.symbols.ANonTerminal;
import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.ast.AInterfaceOrClassSymbol;
 import cs444.parser.symbols.ast.factories.ASTSymbolFactory;
 import cs444.parser.symbols.ast.factories.ClassInterfaceFactory;
 import cs444.parser.symbols.ast.factories.FieldSymbolFactory;
 import cs444.parser.symbols.ast.factories.IntegerLiteralFactory;
 import cs444.parser.symbols.ast.factories.ListedSymbolFactory;
 import cs444.parser.symbols.ast.factories.NameSymbolFactory;
 import cs444.parser.symbols.ast.factories.OneChildFactory;
 import cs444.parser.symbols.ast.factories.StringLiteralFactory;
 import cs444.parser.symbols.ast.factories.TypeSymbolFactory;
 
 public class JoosASTBuilder implements IASTBuilder{
     private static final Iterable<ASTSymbolFactory> simplifications = Arrays.asList(new ASTSymbolFactory [] {
             new ListedSymbolFactory(), new OneChildFactory(),  new NameSymbolFactory(), new TypeSymbolFactory(),
             new ClassInterfaceFactory(), new FieldSymbolFactory(),
             new IntegerLiteralFactory(), new StringLiteralFactory()});
 
     public Iterable<ASTSymbolFactory> getSimplifcations() {
         return simplifications;
     }
 
     @Override
     public boolean isValidFileName(String fileName, ANonTerminal start) {
         //make sure the file name is correct and matches the class/interface
         AInterfaceOrClassSymbol publicClassInterface;
         if(AInterfaceOrClassSymbol.class.isInstance(start)){
             publicClassInterface = (AInterfaceOrClassSymbol) start;
         }else{
            ISymbol tmp = start.firstOrDefault("N_TYPEDECLARATION_0");
            if(tmp != null) start = (ANonTerminal) tmp;
             publicClassInterface =
                 (AInterfaceOrClassSymbol) start.firstOrDefault("ClassDeclaration", "InterfaceDeclaration");
         }
 
         return fileName.equals(publicClassInterface.dclName + ".java");
     }
 }
