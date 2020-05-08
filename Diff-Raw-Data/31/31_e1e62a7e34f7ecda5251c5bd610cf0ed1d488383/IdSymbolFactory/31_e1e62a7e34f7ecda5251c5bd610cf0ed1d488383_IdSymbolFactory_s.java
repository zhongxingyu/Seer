 package cs444.parser.symbols.ast.factories;
 
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.Terminal;
 import cs444.parser.symbols.ast.IdSymbol;
 import cs444.parser.symbols.ast.IdSymbol.Type;
import cs444.parser.symbols.ast.ListedSymbol;
 import cs444.parser.symbols.exceptions.OutOfRangeException;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 
 public class IdSymbolFactory extends ASTSymbolFactory{
 
     @Override
     protected ANonTerminal convert(ANonTerminal from) throws OutOfRangeException, UnsupportedException {
 
         boolean isImport = from.getName().equalsIgnoreCase("ImportDeclaration");
 
         if(!from.getName().toUpperCase().equals("QUALIFIEDIDENTIFIER") &&
                 !from.getName().equalsIgnoreCase("BasicType")
                 && !isImport) return from;
 
 
         Terminal first = (Terminal)from.firstOrDefault("ID");
         if(first == null){
             return new IdSymbol((Terminal)from.children.get(0));
         }
 
        ListedSymbol rest = (ListedSymbol) from.firstOrDefault("N_dot_Id_0");
 
         IdSymbol id = new IdSymbol(first, rest);
 
         if(isImport) id.type = null == from.firstOrDefault("star") ? Type.IMPORT : Type.STAR_IMPORT;
 
         return id;
     }
 
 }
