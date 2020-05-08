 package cs444.parser.symbols.ast.factories;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.ATerminal;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.ast.AInterfaceOrClassSymbol;
 import cs444.parser.symbols.ast.ClassSymbol;
 import cs444.parser.symbols.ast.EmptyClassSymbol;
 import cs444.parser.symbols.ast.InterfaceSymbol;
 import cs444.parser.symbols.ast.NameSymbol;
 import cs444.parser.symbols.exceptions.IllegalModifierException;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 
 public class ClassInterfaceFactory extends ASTSymbolFactory{
 
     @Override
     protected ISymbol convert(ISymbol from) throws UnsupportedException, IllegalModifierException {
         List<NameSymbol> pkgImports = new LinkedList<NameSymbol>();
 
         if(from.getName().equalsIgnoreCase("CompilationUnit")){
             ANonTerminal nonTerm = (ANonTerminal) from;
             from = nonTerm.firstOrDefault("TypeDeclaration");
             NameSymbol pkg = (NameSymbol) nonTerm.firstOrDefault("Name");
             if(pkg != null) pkgImports.add(pkg);
             nonTerm = (ANonTerminal) nonTerm.firstOrDefault("IMPORTDECLARATIONS");
             if(nonTerm != null){
                 for(ISymbol name : nonTerm.getAll("Name")) pkgImports.add((NameSymbol) name);
             }
             if(from == null) return new EmptyClassSymbol(nonTerm, pkgImports);
         }
 
         if(!from.getName().equalsIgnoreCase("TypeDeclaration")) return from;
 
         ANonTerminal nonTerm = (ANonTerminal) from;
         ANonTerminal declaration = (ANonTerminal) nonTerm.firstOrDefault("ClassDeclaration");
 
         String bodyName;
         boolean isInterface;
 
         if(declaration != null){
             bodyName = "ClassBodyDeclarations";
             isInterface = false;
         }else{
             declaration = (ANonTerminal)nonTerm.firstOrDefault("InterfaceDeclaration");
             bodyName = "InterfaceMemberDeclarations";
             isInterface = true;
         }
 
         AInterfaceOrClassSymbol classInterface = null;
 
         String classOrInterfaceName = ((ATerminal)declaration.firstOrDefault("Name")).value;
         ANonTerminal interfaces =(ANonTerminal) declaration.firstOrDefault("InterfaceTypeList");
         ANonTerminal body =(ANonTerminal) declaration.firstOrDefault(bodyName);
 
         List<String> interfaceNames = new LinkedList<String>();
 
         if(interfaces != null){
             try{
                 for(ISymbol child : interfaces.children){
                     NameSymbol nameSymbol = (NameSymbol) child;
                     interfaceNames.add(nameSymbol.value);
                 }
             }catch (ClassCastException e){
                 throw new UnsupportedException("extend or implement basic types");
             }
         }
 
         List<ISymbol> children = Collections.emptyList();
         if(body != null) children = body.children;
 
         if(isInterface){
             classInterface = new InterfaceSymbol(classOrInterfaceName, declaration, interfaceNames, children, pkgImports);
         }else{
            ANonTerminal superName = (ANonTerminal)declaration.firstOrDefault("ClassType");
             String superType = null;
             if(superName != null){
                 superType = ((NameSymbol) superName.firstOrDefault("Name")).value;
             }
             classInterface = new ClassSymbol(classOrInterfaceName, declaration, interfaceNames, children, superType, pkgImports);
         }
         classInterface.validate();
         return classInterface;
     }
 }
