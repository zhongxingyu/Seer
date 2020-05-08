 package xtc.oop;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.FileReader;
 
 import java.util.*; //ut oh, is Grimm going to be mad? Yes. TODO import only what we need
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 import xtc.tree.Printer;
 
 import xtc.lang.JavaFiveParser;
 
 //OUR IMPORTS
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.io.BufferedReader;
 import java.io.Reader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.regex.*;
 
 import xtc.oop.helper.Bubble;   //NEED TO UPDATE TO OUR NEW DATA STRUCTURES
 import xtc.oop.helper.Mubble;
 import xtc.oop.helper.Pubble;
 
 public class ImplementationParser extends xtc.tree.Visitor //aka IMPL
 {
 
     public static ArrayList<Bubble> bubbleList;
     public static ArrayList<Pubble> pubbleList;
     public static ArrayList<Mubble> mubbleList;
     public static ArrayList<Mubble> langList;
     public static ArrayList<String> parsed; //keeps track of what ASTs have been parsed
 
     public NewTranslator t; //used for the parse method
 
     public ImplementationParser(NewTranslator t, ArrayList<Pubble> pubbleList, ArrayList<Mubble> mubbleList, ArrayList<Bubble> bubbleList, ArrayList<Mubble> langList, ArrayList<String> parsed)
     {
         this.pubbleList = pubbleList;
         this.mubbleList = mubbleList;
         this.bubbleList = bubbleList;
         this.langList   = langList;
         this.parsed     = parsed;
         this.t = t;
     }
 
 
     public static String findFile(String query) {//{{{
 
         String sep = System.getProperty("file.separator");
         String cp = System.getProperty("java.class.path");
         //Hardcoded as the working directory, otherwise real classpath
         cp = ".";
 
         query = query.replace(".",sep).concat(".java");
         //System.out.println("+++++"+query);
         return findFile(cp, query);
     }//}}}
 
     public static String findFile(String cp, String query) {//{{{
         String sep = System.getProperty("file.separator");
         File f = new File(cp);
         File [] files = f.listFiles();
         for(int i = 0; i < files.length; i++) {
             //System.out.println(sep+(cp.equals(".") ? "\\\\" : "")+cp+sep);
             //////////////////////////////////////
             //Hardcoding that sep is / and cp is .
             //////////////////////////////////////
             //System.out.println(query);
             if(files[i].isDirectory()) {
                 String a = findFile(files[i].getAbsolutePath(), query);
                 if(!a.equals(""))
                     return a;
             }
             else if(files[i].getAbsolutePath().replaceAll("/\\./",sep).endsWith(query))
                 return files[i].getAbsolutePath();
         }
         return "";
     }//}}}
 
     public void visit(Node n)//{{{
     {
         int counter = 1;
         if(n.hasProperty("parent0")) {
             Node temp = (Node)n.getProperty("parent0");
 
             while(temp != null) {
                 n.setProperty("parent"+(counter++), temp.getProperty("parent0"));
                 temp = (Node)temp.getProperty("parent0");
             }
         }
 
         for (Object o : n){
             if (o instanceof Node){
                 ((Node)o).setProperty("parent_name", n.getName() );
                 ((Node)o).setProperty("parent0", n );
                 dispatch((Node)o);
             }
         }
     }
     //}}}
 
     public void visitSubscriptExpression(GNode n)
     {
         visit(n);
         String arrName = n.getNode(0).getString(0);
         String index = n.getNode(1).getString(0);
         methodString += arrName + "->__data[" + index + "]";
     }
 
     String tan;
     boolean inArrayExpress = false;
 
     public void visitFieldDeclaration(GNode n){
         if (onMeth) {
             tan = "";
         }
 
         visit(n);
 
         if (onMeth) {
             String[] z = tan.trim().split("\\s+");
             // this could be fucked up
             String type = z[0];
             //System.out.println("Z"+tan+"\n");
             for (int i = 1; i < z.length; i++) {
                 table.put(z[i], type);
 
             }
         }
 
 
         if (onMeth) {
             if(inArray)
             {
                 //getting type
                 String arrType = n.getNode(1).getNode(0).getString(0);
                 if(arrType.equals("int"))
                     arrType = "int32_t";
                 if(arrType.equals("boolean"))
                     arrType = "bool";
 
                 String arrName = n.getNode(2).getNode(0).getString(0);
                 methodString += "__rt::Array<" + arrType + ">* " + arrName;
                 table.put(arrName, "__rt::Array<" + arrType + ">* ");
                 if(inArrayExpress)
                 {
                     String size = n.getNode(2).getNode(0).getNode(2).getNode(1).getNode(0).getString(0);
                     methodString += "= new __rt::Array<" + arrType + ">(" + size + ")";
                     inArrayExpress = false;
                 }
                 methodString += ";\n";
 
                 inArray = false;
             }
             String[] z = tan.split("\\s+");
             // this could be fucked up
             String type = z[0];
             for (int i = 1; i < z.length; i++) {
                 table.put(z[i], type);
             }
 
             //System.out.println(tan);
         }
     }
 
     public void visitNewArrayExpression(GNode n)
     {
         visit(n);
         inArrayExpress = true;
     }
 
     public void visitDimensions(GNode n) {
         visit(n);
     }
 
     public void visitModifiers(GNode n){
         visit(n);
     }
 
     String tempString = "";
     String tmpCode = "";
     boolean onMeth = false;
     Mubble curMub = null;
     String methodString = "";
     String cName = "";
 
     HashMap<String, String> table;
 
     public void visitMethodDeclaration(GNode n)
     {
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)parent0.getProperty("parent0");
 
         //Parent 1 Should be class decl
         String classname = parent1.getString(1);
 
         //setting global class name
         cName = classname;
         String methodname = n.getString(3);
         for(Mubble m : mubbleList){
             if(m.getClassName() == null || m.getName() == null || methodname == null || classname == null)
                 System.out.println("****************YOURE FUCKED**************");
             if(m.getClassName().equals(classname) && m.getName().trim().equals(methodname))
             {
                 curMub = m;
             }
 
         }
 
         //visit
         visit(n);
 
     }
 
     public void visitModifier(GNode n){
         visit(n);
 
     }
 
     public void visitConstructorDeclaration(GNode n)
     {
 
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)parent0.getProperty("parent0");
 
         //Parent 1 Should be class decl
         String classname = parent1.getString(1);
         String methodname = n.getString(2);
 
         int constructorCount = 0;
         for(Mubble m : mubbleList){
             if(m.getClassName().trim().equals(classname.trim()) && m.isConstructor())
             {
                 constructorCount++;
                 curMub = m;
             }
             //System.out.println("Constructor Count is: "+ constructorCount);
             //System.out.println("_V_V_V_V_V_V_V_V_V_V_V_V_V_V_V_V_");
         }
 
         visit(n);
 
     }
     String mName;
     boolean debugEvaluateExpression = false;
 
     //LOL this is not needed at all VV
     public String evaluateExpressionForPrint(Node n){//{{{
 
         //TODO adding numbers
         //TODO newClassExpression
         String ret = "";
         if(n.getName().endsWith("Literal")){
             ret += n.getString(0);
         }else if(n.hasName("AdditiveExpression")){
             ret += evaluateExpressionForPrint(n.getNode(0));
             ret += " << ";
             ret += evaluateExpressionForPrint(n.getNode(2));
             //TODO - if the additive expression is not supposed to be
             //handled like this ^^
         }else if(n.hasName("Arguments")){
             ret += evaluateExpressionForPrint(n.getNode(0));
         }else if(n.hasName("NewClassExpression")){
             //wtf
             //probably need to create the class before any of this cout business...right?
         }else if(n.hasName("PrimaryIdentifier")){
             ret += n.getString(0);
         }else if(n.hasName("PostfixExpression")){
             Node primaryIdentifier = (Node)n.get(0);
             if(debugEvaluateExpression) System.out.println(primaryIdentifier.getString(0) + n.getString(1));
             ret += primaryIdentifier.getString(0) + n.getString(1); //PostfixExpression(PrimaryIdentifier("i"), "++" )
         }
         else{
             System.out.println("errror: :(" + n.getName());
         }
         //eval
         return ret;
     }//}}}
 
     boolean debugCallExpression = false;
     boolean inPrintStatement = false;
     public void visitCallExpression(GNode n) {
         //visit(n);
         boolean hasVisited = false;
         if (onMeth) {
             mName = n.getString(2);
             String tmp = "";
 
             dispatchBitch(n);
             //Dealing with System.out.print*
             if(n.getNode(0).hasName("SelectionExpression") &&
                     n.getNode(0).getNode(0).hasName("PrimaryIdentifier") &&
                     n.getNode(0).getNode(0).getString(0).equals("System") &&
                     n.getNode(0).getString(1).equals("out") &&
                     (n.getString(2).equals("print") ||n.getString(2).equals("println"))
               ){
                 if(debugCallExpression) System.out.println("Call Expression n.getNode(3): " + n.getNode(3) );
                 methodString += "cout << ({"; //+ evaluateExpressionForPrint(n.getNode(3));
                 inPrintStatement = true;
                 visit(n);
                 inPrintStatement = false;
                 hasVisited = true;
 
                 methodString += ";})";//will this "go wrong" when dealing with method chaining?
                 //solution = abide by "inPrintStatement" rules for last ';'
                 if(n.getString(2).equals("println")){
                     methodString += " << endl";
                 }
               }
             else{
                 dispatch(n.getNode(0));
 
 
                 //need to fix casting for first arg
                 if(n.getNode(0) != null) {
                     methodString += "->__vptr->"+n.getString(2);
                     methodString += "(";
                     //should cast self to expected type
                     //not doing now because castify is not a method,
                     //too complicated right now
                     dispatch(n.getNode(0));//adding self
                     //methodString += ", "; //error
                 }
                 else {
                     methodString += n.getString(2) + "(";
 
                 }
                 dispatch(n.getNode(3));
             }
         }
         else {
             if(!hasVisited)
                 visit(n);
         }
     }
 
     public void visitEmptyStatement(GNode n) {
         if (onMeth) {
             methodString += ";\n";
         }
         visit(n);
     }
 
     public void visitConditionalStatement(GNode n) {
         if (onMeth) {
             dispatchBitch(n);
             Node parent = (Node)n.getProperty("parent0");
             if (parent.hasName("ConditionalStatement")) {
                 methodString += "\n}\nelse ";
             }
             methodString += "if (";
             dispatch(n.getNode(0));
             methodString += ") {\n";
             for (int i = 1; i < n.size()-1; i++) {
                 dispatch(n.getNode(i));
             }
             if (n.getNode(n.size()-1) != null) {
                 Node parent1 = (Node)parent.getProperty("parent0");
                 if (!n.getNode(n.size()-1).getName()
                         .equals("ConditionalStatement")) {
                     methodString += "\n}\nelse {\n";
                         }
                 /*
                    if (!parent.getName().equals("ConditionalStatement")) {
                    methodString += "\n}\nelse {\n";
                    }
                    */
                 dispatch(n.getNode(n.size()-1));
             }
             if (!parent.hasName("ConditionalStatement")) {
                 methodString += "\n}\n";
             }
             //methodString += "}\n";
         }
         else {
             visit(n);
         }
     }
 
     public void visitConditionalExpression(GNode n) {
         if (onMeth) {
             dispatchBitch(n);
             methodString += "(";
             dispatch(n.getNode(0));
             methodString += " ? ";
             dispatch(n.getNode(1));
             methodString += " : ";
             dispatch(n.getNode(2));
             methodString += ")";
         }
         else {
             visit(n);
         }
     }
 
     public void visitDeclarators(GNode n) {
         visit(n);
 
         if (onMeth && !((Node)n.getProperty("parent0")).getName()
                 .equals("BasicForControl") && !inArray) {
             methodString += ";\n";
 
                 }
     }
 
     public void visitBooleanLiteral(GNode n) {
         if (onMeth) {
             methodString += n.getString(0);
         }
         visit(n);
     }
 
     public void visitDeclarator(GNode n) {
         if (onMeth && !inArray) {
             methodString += " " + n.getString(0);
             Object third = n.get(2);
             if (third instanceof Node) {
                 methodString += " = ";
             }
             tan += n.getString(0);
         }
         visit(n);
         if (onMeth) {
             //methodString += ";\n";
         }
     }
 
     public void visitEqualityExpression(GNode n) {
         if(onMeth) {
             dispatchBitch(n);
             dispatch(n.getNode(0));
             methodString += " " + n.getString(1) + " ";
             dispatch(n.getNode(2));
         }
         else {
             visit(n);
         }
     }
 
 
     public void visitIntegerLiteral(GNode n) {
         Node parent0 = (Node)n.getProperty("parent0");
         if (onMeth && !inArray) {
             if(!parent0.hasName("SubscriptExpression"))
                 methodString += n.getString(0);
         }
         visit(n);
     }
 
     public void visitClassBody(GNode n){
         visit(n);
     }
 
     public void visitClassDeclaration(GNode n){
 
         String className = n.getString(1);
 
         visit(n);
 
         //at this point all the mubbles of bubble have been filled
         for(Bubble b : bubbleList){
             if(b.getName().equals(className)) {
                 b.setIsFilled(true);
             }
         }
     }
 
     public void visitFormalParameters(GNode n){
         visit(n);
     }
 
     public void visitFormalParameter(GNode n) {
 
         visit(n);
     }
 
     public void visitCompilationUnit(GNode n){
         visit(n);
 
         //Check if any bubbles haven't been filled (we have not parsed their AST yet)
         for(Bubble b : bubbleList){
             if (!(b.isBuilt())){
                 try{
                     String fileName = findFile(b.getName());
                     File f = new File(fileName);
                     FileInputStream fi = new FileInputStream(f);
                     Reader in = new BufferedReader(new InputStreamReader(fi));
                     Node leNode = t.parse(in, f);
                     this.dispatch(leNode);
                 }catch(Exception e){System.out.println("error" + e); }
             }
         }
     }
 
     public String outputFormat(String s) {
         //to turn int to int32_t in a string:
         s = s.replaceAll("(^int(?=\\s+))|(?<=\\s+)int(?=\\s+)","int32_t");
 
         //to turn boolean to bool in a string:
         s = s.replaceAll("(^boolean(?=\\s+))|(?<=\\s+)boolean(?=\\s+)","bool");
 
         //to turn final to const in a string:
         //s = s.replaceAll("(?<=\\s+)final(?=\\s+)","const");
 
         //turn systemoutprints into printf
         //TODO @ALott can you handle turning this to std::cout?
         //s = s.replaceAll("System->out->__vptr->println\\(System->out,\\s?","printf(");
         //s = replaceSystemPrintln(s);
 
 
         //turn mains into right format
         //s = s.replaceAll("void\\s[\\w$_]*::main\\([\\w$_]*\\s__this,","int main(");
         return s;
     }
 
     //replaces System.out.println's with cout
     public String replaceSystemPrintln(String s)
     {
 
         if (s.contains("vptr->println"))
         {
             System.out.println("s1");
             //get everything to be printed
             String toPrint = getStringBetween(s, "println(System->out,", ");");
             System.out.println("s2");
             toPrint = toPrint.replace("+", "<<");
             System.out.println("s3");
             return "std::cout << " + toPrint + " << std::endl;";
         }
         else
             return s;
 
     }
 
     //getStringBetween("-hi*", "-" , "*") returns hi
     public String getStringBetween(String src, String start, String end)
     {
         int lnStart;
         int lnEnd;
         String ret = "";
         lnStart = src.indexOf(start);
         lnEnd = src.indexOf(end);
         if(lnStart != -1 && lnEnd != -1)
             ret = src.substring(lnStart + start.length(), lnEnd);
 
         return ret;
     }
 
     public String inNameSpace(String obj) {
         if (obj.equals("Object") || obj.equals("String") || obj.equals("Class")) {
             return "java lang";
         }
 
         String ns1 = "";
         String ns2 = "";
         //System.out.println("COMPARING "+obj+" and "+cName);
         for( Bubble b : bubbleList) {
             //System.out.println(b.getName() + ":" + b.getPackageName());
             //doesn't account for multiple classes of the same name
             if (b.getName().equals(obj)) {
                 ns1 = b.getPackageName();
             }
             if (b.getName().equals(cName)) {
                 ns2 = b.getPackageName();
             }
         }
         //System.out.println("hello");
 
         //if(ns1 == null) {
         //    return "java lang";
         //}
         //else if(ns1.equals(ns2)) {
         if (ns1.equals(ns2)) {
             return null;
         }
         else {
             return ns1;
         }
         }
 
     public void visitQualifiedIdentifier(GNode n){
 
         if (onMeth) {
             Node parent0 = (Node)n.getProperty("parent0");
             Node parent1 = (Node)parent0.getProperty("parent0");
             if(parent1.hasName("FieldDeclaration")) {
                 tan += n.getString(0) + " ";
             }
 
             if(parent1.hasName("FieldDeclaration")){
                 for(Object o : parent0){
                     if (o instanceof Node ){
                         if(((Node)o).hasName("Dimensions"))
                             inArray = true;
                     }
                 }
             }
 
             String s = inNameSpace(n.getString(0));
             if (s != null && !inArray) {
                 //using absolute namespace
                 System.out.println("4");
                 //check to see if Bubble is found by inNameSpace
                 methodString += "::"+s.trim().replaceAll("\\s+", "::")
                     +"::";
             }
             if(!inArray)
                 methodString += n.getString(0);
         }
 
         visit(n);
 
         //Find out if we need to find/parse the AST for the class mentioned
         boolean filled = false;
         for(Bubble b : bubbleList){
             if(b.getName().equals(n.getString(n.size()-1))) {
                 if(b.isFilled()){
                     filled = true;
                 }
                 b.setIsFilled(true);
             }
         }
 
         if(!filled && !n.getString(n.size()-1).equals("String")){
 
             String path = "";
             for(int i = 0; i < n.size(); i++) {
                 path+="."+n.getString(i);
             }
 
             System.out.println("IMPL is about to call findFile on: " + path.substring(1));
             path = findFile(path);
 
             if(!path.equals("")){
                 //System.out.println(path);
                 try{
                     t.process(path);
                 } catch (Exception e) {System.out.println("error: " + e);}
             }
         }
     }
 
     public void visitNewClassExpression(GNode n) {
         if (onMeth) {
             dispatchBitch(n);
             methodString += "new ";
             dispatch(n.getNode(0));
             dispatch(n.getNode(1));
 
             if(n.getNode(2).getString(0).equals("Object") ||
                     n.getNode(2).getString(0).equals("String") ||
                     n.getNode(2).getString(0).equals("Class")) {
                 methodString += "_";
                     }
 
             methodString += "_";
             dispatch(n.getNode(2));
             methodString += "(";
             dispatch(n.getNode(3));
             dispatch(n.getNode(4));
         }
         else {
             visit(n);
         }
     }
 
     public void visitImportDeclaration(GNode n){
         visit(n);
     }
 
     public void visitForStatement(GNode n)
     {
         if (onMeth) {
             methodString += "for(";
         }
         visit(n);
         if (onMeth) {
             methodString += "}\n";
         }
     }
 
     public void visitLogicalAndExpression(GNode n) {
         if(onMeth) {
             dispatchBitch(n);
             methodString += "(";
             dispatch(n.getNode(0));
             methodString += ") && (";
             dispatch(n.getNode(1));
             methodString += ")";
         }
         else {
             visit(n);
         }
     }
 
     public void visitExpression(GNode n) {
         if (onMeth) {
             dispatchBitch(n);
             dispatch(n.getNode(0));
             methodString += " " + n.getString(1) + " ";
             dispatch(n.getNode(2));
 
         } else {
             visit(n);
         }
     }
 
     public void visitExpressionStatement(GNode n) {
         visit(n);
         if (onMeth) {
             methodString += ";\n";
         }
     }
 
     public void visitLogicalOrExpression(GNode n) {
         if(onMeth) {
             dispatchBitch(n);
             methodString += "(";
             dispatch(n.getNode(0));
             methodString += ") || (";
             dispatch(n.getNode(1));
             methodString += ")";
         }
         else {
             visit(n);
         }
     }
 
     public void visitBasicForControl(GNode n)
     {
         if(onMeth) {
             dispatchBitch(n);
             for(int i = 0; i < n.size(); i++) {
                 dispatch(n.getNode(i));
                 if( i >= 2 && i < n.size()-1) {
                     methodString += "; ";
                 }
             }
             methodString += ") {\n";
         }
         else {
             visit(n);
         }
     }
 
     public void visitBlock(GNode n) {
         if(((Node)n.getProperty("parent0")).getName()
                 .equals("MethodDeclaration") ||
                 ((Node)n.getProperty("parent0")).getName().equals("ConstructorDeclaration")) {
             onMeth = true;
             table = new HashMap<String, String>();
 
             visit(n);
             onMeth = false;
 
             curMub.addCode(outputFormat(methodString));
             //System.out.println("method string:" + methodString);
 
             methodString = "";
             table = null;
                 }
         else {
             visit(n);
         }
     }
 
     public void visitPostfixExpression(GNode n) {
         if (onMeth) {
             visit(n);
             methodString += n.getString(1);
         }
         else {
             visit(n);
         }
     }
 
     public void visitPrimaryIdentifier(GNode n) {
         Node parent0 = (Node)n.getProperty("parent0");
         if (onMeth) {
             if(!parent0.hasName("SubscriptExpression"))
                 if(!(n.getString(0).equals("System") && parent0.getString(1).equals("out")))
                     methodString += n.getString(0);
             //key = n.getString(0);
         }
         visit(n);
     }
 
     boolean inArray = false;
     public void visitPrimitiveType(GNode n) {
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
 
         if(parent1.hasName("FieldDeclaration"))
         {
             for(Object o : parent0)
             {
                 if (o instanceof Node )
                 {
                     if(((Node)o).hasName("Dimensions"))
                         inArray = true;
                 }
 
             }
         }
         if (onMeth && !inArray) {
             methodString += n.getString(0);
         }
 
 
     }
 
     public void visitStringLiteral(GNode n) {
         if (onMeth) {
             /*
                Node parent0 = (Node)n.getProperty("parent0");
                Node parent1 = (Node)n.getProperty("parent1");
                Node parent2 = (Node)n.getProperty("parent2");
                if (parent0.hasName("Declarator") && parent1.hasName("Declarators")
                && parent2.hasName("FieldDeclaration")) {
                Node tt = parent2.getNode(1);
                if (tt.hasName("Type")) {
                if (tt.getNode(0).hasName("QualifiedIdentifier") &&
                tt.getNode(0).getString(0).equals("String")) {
                methodString += "__rt::literal(" + n.getString(0) + ")";
                }
                }
                }
                */
 
             methodString += "__rt::literal(" + n.getString(0) + ")";
         }
 
         visit(n);
     }
 
     public void visitSwitchStatement(GNode n) {
         if(onMeth) {
             dispatchBitch(n);
             methodString += "switch(";
             dispatch(n.getNode(0));
             methodString += ") {\n";
             for(int i = 1; i < n.size(); i++) {
                 dispatch(n.getNode(i));
             }
             methodString += "}\n";
         }
         else {
             visit(n);
         }
     }
 
     public void visitDoWhileStatement(GNode n) {
         if(onMeth) {
             dispatchBitch(n);
             methodString += "do {\n";
             dispatch(n.getNode(0));
             methodString += "} while(";
             dispatch(n.getNode(1));
             methodString += ");\n";
         }
         else {
             visit(n);
         }
     }
 
     public void visitDefaultClause(GNode n) {
         if(onMeth) {
             methodString += "default:\n";
         }
         visit(n);
     }
 
     public void visitBreakStatement(GNode n) {
         if(onMeth) {
             methodString += "break ";
             visit(n);
             methodString += ";\n";
         }
         else {
             visit(n);
         }
     }
 
     public void visitCaseClause(GNode n) {
         if(onMeth) {
             dispatchBitch(n);
             methodString += "case ";
             dispatch(n.getNode(0));
             methodString += ":\n";
             for(int i = 1; i < n.size(); i++) {
                 dispatch(n.getNode(i));
             }
         }
         else {
             visit(n);
         }
     }
 
     //String key;
     public void visitArguments(GNode n) {
 
         if (onMeth) {
             String key = "";
             dispatchBitch(n);
             String type = "";
             Node callex = (Node)n.getProperty("parent0");
             if (callex.hasName("CallExpression") &&
                     callex.getNode(0) != null && callex
                     .getNode(0).hasName("PrimaryIdentifier")) {
                 key = callex.getNode(0).getString(0);
                 type = table.get(key);
                 //System.out.println(key + " " + type);
                     }
 
 
             String mSign = "";
             for (Mubble m : mubbleList) {
 
                 if ((m.getClassName().equals(type) || type.equals("")) &&
                         m.getName().equals(mName)) {
                     mSign = m.forward();
                         }
             }
 
             for (Mubble m : langList) {
 
                 if (m.getClassName().equals(type) &&
                         m.getName().equals(mName)) {
                     mSign = m.forward();
                         }
             }
             Matcher m = Pattern.compile("(?<=,\\s)\\S*(?=\\s*)").matcher(mSign);
             String p = "";
 
             while(m.find()){
                 p+= " " + m.group();
             }
 
             String [] par = p.trim().split("\\s");
             /* WHY DO WE NEED THIS
                for( String g : par)
                System.out.println(g);
                */
 
             String s = "";
 
             //CASTING
             if (n.size() > 0) {
                 s = inNameSpace(par[0]);
                 if(!par[0].trim().equals("")) {
                     methodString += "((";
 
 
                     if (s != null && !s.trim().equals("")) {
                         //using absolute namespace
                         methodString += "::"+s.trim().replaceAll("\\s+", "::")+"::";
 
                     }
                     //System.out.println(par[0]);
                     methodString += par[0]+") ";
                     dispatch(n.getNode(0));
                     methodString += ")";
                 }
                 else {
                     if(!inPrintStatement)
                         methodString += ", ";
                     dispatch(n.getNode(0));
                 }
 
             }
 
             for(int i = 1; i < n.size(); i++) {
                 //DONT KNOW WHY WE NEED THIS
                 //if(!par[i].trim().equals("")) {
                 //    methodString += ", (("+(par.length > i ? par[i] : "")
                 //        +") ";
                 //    dispatch(n.getNode(i));
                 //   methodString += ")";
                 //}
                 //else{
                 methodString += ", ";
 
                 dispatch(n.getNode(i));
                 //}
             }
 
             if(!inPrintStatement)
                 methodString += ")";
 
         }
         else {
             visit(n);
         }
 
     }
 
     public void visitSelectionExpression(GNode n) {
         if (onMeth) {
             visit(n);
             if (n.get(1) != null) {
                if(!(((Node)n.get(0)).getString(0).equals("System") && n.getString(1).equals("out")))
                    methodString += "->" + n.getString(1);
             }
         }
         else {
             visit(n);
         }
     }
 
     public void visitReturnStatement(GNode n) {
         if (onMeth) {
             methodString += "return ";
         }
         visit(n);
         if (onMeth) {
             methodString += ";\n";
         }
     }
 
     public void visitUnaryExpression(GNode n) {
         if (onMeth) {
             methodString += n.getString(0);
             visit(n);
         }
         else {
             visit(n);
         }
     }
 
     public void visitWhileStatement(GNode n) {
         if (onMeth) {
             dispatchBitch(n);
             methodString += "while(";
             dispatch(n.getNode(0));
             methodString += ") {\n";
             for(int i = 1; i < n.size(); i++) {
                 dispatch(n.getNode(i));
             }
             methodString += "}\n";
         }
         else {
             visit(n);
         }
     }
 
     public void visitFloatingPointLiteral(GNode n) {
         if (onMeth) {
             methodString += n.getString(0);
         }
         visit(n);
     }
 
     public void visitCharacterLiteral(GNode n) {
         if (onMeth) {
             methodString += n.getString(0);
         }
         visit(n);
     }
     //hmm..
     public void visitNullLiteral(GNode n) {
         if (onMeth) {
             methodString += "__rt::null()";
         }
         visit(n);
     }
 
     public void visitAdditiveExpression(GNode n) {
         if (onMeth) {
             dispatchBitch(n);
             dispatch(n.getNode(0));
             methodString += " "+n.getString(1)+" ";
             dispatch(n.getNode(2));
         }
         else {
             visit(n);
         }
     }
 
     public void visitMultiplicativeExpression(GNode n) {
         if (onMeth) {
             dispatchBitch(n);
             dispatch(n.getNode(0));
             methodString += " "+n.getString(1)+" ";
             dispatch(n.getNode(2));
         }
         else {
             visit(n);
         }
     }
 
     public void visitCastExpression(GNode n) {
         if (onMeth) {
             dispatchBitch(n);
             methodString += "((";
             dispatch(n.getNode(0));
             methodString += ") ";
             dispatch(n.getNode(1));
             methodString += ")";
         }
         else {
             visit(n);
         }
     }
 
     //dunno why second child is null
     public void visitThisExpression(GNode n) {
         if (onMeth) {
             methodString += "__this";
         }
         visit(n);
     }
 
     public void visitBasicCastExpression(GNode n) {
         if (onMeth) {
             dispatchBitch(n);
             methodString += "((";
             dispatch(n.getNode(0));
             methodString += ") ";
             //not sure if there can be more children, just in case
             for(int i = 1; i < n.size(); i++) {
                 dispatch(n.getNode(i));
             }
             methodString += ")";
         }
         else {
             visit(n);
         }
     }
 
     //////////////////
     //need to actually implement instanceof???
     //////////////////
     /////////////////
     ////will getstring always work?
     ////////////////
     public void visitInstanceOfExpression(GNode n) {
         if (onMeth) {
             dispatchBitch(n);
             dispatch(n.getNode(0));
             methodString += "->__vptr->isInstance("+n.getNode(0).getString(0)+", ";
             dispatch(n.getNode(1));
             methodString += ")";
         }
         else {
             visit(n);
         }
     }
 
     public void visitLogicalNegationExpression(GNode n) {
         if(onMeth) {
             methodString += "!(";
             visit(n);
             methodString += ")";
         }
         else {
             visit(n);
         }
     }
 
     public void visitType(GNode n)
     {
         visit(n);
     }
 
     public void visitExpressionList(GNode n)
     {
         visit(n);
     }
 
     public void visitRelationalExpression(GNode n)
     {
         if (onMeth) {
             dispatchBitch(n);
             dispatch(n.getNode(0));
             methodString += " " + n.getString(1) + " ";
             dispatch(n.getNode(2));
             //methodString += ";";
         }
 
         //visit(n);
     }
 
     public void dispatchBitch(Node n) {
         int counter = 1;
         if(n.hasProperty("parent0")) {
             Node temp = (Node)n.getProperty("parent0");
 
             while(temp != null) {
                 //System.out.println(temp);
                 //temp = (Node)temp.getProperty("parent0");
 
                 n.setProperty("parent"+(counter++), temp.getProperty("parent0"));
                 temp = (Node)temp.getProperty("parent0");
                 //if(n.getProperty("parent2") == null)
                 //System.out.println(temp);
             }
         }
         //don't need this, but not deleting.
         for (String s : n.properties()) {
             //System.out.println(n.getProperty(s));
         }
 
         for (Object o : n){
             if (o instanceof Node){
                 ((Node)o).setProperty("parent_name", n.getName() );
                 ((Node)o).setProperty("parent0", n );
                 //dispatch((Node)o);
             }
         }
     }
     }
