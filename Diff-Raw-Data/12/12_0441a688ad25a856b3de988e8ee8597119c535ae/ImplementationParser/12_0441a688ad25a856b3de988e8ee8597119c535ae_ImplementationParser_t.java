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
 import java.util.Stack;
 
 import xtc.oop.helper.Bubble;   //NEED TO UPDATE TO OUR NEW DATA STRUCTURES
 import xtc.oop.helper.Mubble;
 import xtc.oop.helper.Pubble;
 import xtc.oop.helper.Field;
 import xtc.oop.helper.EvalCall;
 
 import xtc.util.SymbolTable;
 
 public class ImplementationParser extends xtc.tree.Visitor //aka IMPL
 {
 
     public static ArrayList<Bubble> bubbleList;
     public static ArrayList<Pubble> pubbleList;
     public static ArrayList<Mubble> mubbleList;
     public static ArrayList<Mubble> langList;
     public static ArrayList<String> parsed; //keeps track of what ASTs have been parsed
     boolean onMeth = false;
     Mubble curMub = null;
     String tempString = "";
     String tmpCode = "";
     String methodString = "";
     String cName = "";
 
     public NewTranslator t; //used for the parse method
 
     public ImplementationParser(NewTranslator t, ArrayList<Pubble> pubbleList, ArrayList<Mubble> mubbleList, ArrayList<Bubble> bubbleList, ArrayList<Mubble> langList, ArrayList<String> parsed)
     {
         this.pubbleList = pubbleList;
         this.mubbleList = mubbleList;
         this.bubbleList = bubbleList;
         this.langList   = langList;
         this.parsed     = parsed;
         this.t = t;
 
         if(false) //printing out bubble and methods/method parent
             {
                 System.out.println("*****IMPL PARSER***********");
                 for(Bubble b : bubbleList){
                     if(!(b.getName().equals("String") || b.getName().equals("Object"))){
                         System.out.println("Bubble " + b.getName());
                         for(Mubble m : b.getMubbles()){
                             System.out.println("\tMubble: " + m.getName());
                             if(true) //print out info about mubble's fields
                                 {
                                     for(Field f: m.getParameters()){
                                         System.out.println("\t\t" + f.getType() + " " + f.getName());
                                     }
 
                                 }
                             System.out.println("\tisPrivate: " + m.isPrivate());
                             System.out.println("\tClass: " + m.getClassName() + "\n");
                         }
                     }
                 }
             }
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
 
     /* Helper method to resolve assignents that happen in datafields. This adds the code on the right
        side of a dataField assignment to the first line of the appropriate constructor for that node
        NOTE: Should be called after implementation parser is complete
     */
     boolean debugDFAssignments = false;
     boolean resolvingShit = false;
     public void resolveDatafieldAssignments()
     {
         resolvingShit = true;
         String add2Constructor = "";
         for(Bubble b : bubbleList) //for every class
             {
                 if(debugDFAssignments) System.out.println("Resolving DataField Assignments For: " + b.getName());
                 for(Field f : b.getDataFields()) //for each of it's dataFields
                     {
                         if(f.hasAssignment()) //if there is an assignment
                             {
                                 if(debugDFAssignments) System.out.println("\t Resolving Code for  " + f.name);
                                 if(debugDFAssignments) System.out.println("\t Visiting: " + f.getAssignmentNode());
                                 curMub = new Mubble("Temp Mubble");
                                 onMeth = true; //so that nodes are run correctly
                                 if(f.isArray)
                                     inArray = true;
                                 methodString = "";
                                 visit(f.getAssignmentNode()); //should add all assignment code to curMub's code
                                 if(debugDFAssignments) System.out.println("\tCode: ");
                                 if(debugDFAssignments) System.out.println("\t\t" + f.name + " = " + curMub.getCode());
                                 onMeth = false; //resetting
                                 inArray = false; //resetting
                                 if(debugDFAssignments) System.out.println("\tCode: ");
                                 if(debugDFAssignments) System.out.println("\t\t" + f.name + " = " + methodString);
 
                                 if(f.isStatic())
                                     {System.out.println(f.name + " is static");} //todo: where to I put static dataMethods??
                                 else //put them in the top of the constructor
                                     {
                                         if(methodString.endsWith("\n"))
                                             add2Constructor += f.name + " = " + methodString;
                                         else
                                             add2Constructor += f.name + " = " + methodString + ";\n";
                                     }
 
                             }
                     }
                 for(Mubble m : b.getMubbles())
                     {
                         if(m.isConstructor())
                             {
                                 m.prependCode(add2Constructor + "\n\n");
                                 break; //found constructor
                             }
                     }
             }
         resolvingShit = false;
     }
 
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
 
         /*todo: FIX, only works for one dimensional arrays
           if(n.getNode(0).hasName(SubscriptExpression) //if it is a multidimensional array
           visitSubscriptExpression(n.getNode(0))
           else
           String arrName = n.getNode(0).getString(0);//SubscriptExpression(PrimaryIdentifier("e"
           String index = n.getNode(1).getString(0);//IntegerLiteral("0")
           methodString += arrName + "->__data[" + index + "]";
         */
 
         //f[0] = arrName->data[0]
         //f[1][0] = arrName->data[0]->data[1]
         ArrayList<String> arrInfo = resolveArray((GNode)n);
         methodString += arrInfo.get(0);
         for(int i=1; i< arrInfo.size(); i++)
             methodString += "->__data[" + arrInfo.get(i) + "]";
     }
 
     //params: the base Subscript expression node as a parameter
     //return: an arrayList of info about the array
     //          ArrayList[0] = name of array
     //          ArrayList[1...] = the actual place in the array wanting to be accessed
     //ex. f[1][2] would return an ArrayList with Arr[0] = 'f' ,Arr[1] = 1, Arr[2] = 2
     public ArrayList<String> resolveArray(GNode n)
     {
         if(false) //debug
             {
                 System.out.println("test1: " + n.getNode(0));
                 System.out.println("test2: " + n.getNode(0).hasName("SubscriptExpression"));
                 System.out.println("test3: " + n.getNode(0).hasName("PrimaryIdentifier"));
                 System.out.println("test4: " + n.getNode(1).hasName("IntegerLiteral"));
                 System.out.println("test5: " + n.getNode(1).getString(0));
                 System.out.println("test6: " + n.getNode(0).getString(0));
             }
 
         ArrayList<String> info = new ArrayList<String>();
         //resolving my name and any childarrays
         if(n.getNode(0).hasName("SubscriptExpression")) //if multidimensional array
             {
                 //resolve child array
                 ArrayList<String> childArray = resolveArray((GNode)n.getNode(0));
                 //add all the info from my child array
                 for(String s : childArray)
                     info.add(s);
             }
         else if(n.getNode(0).hasName("PrimaryIdentifier")) //single-dimension array
             {
                 info.add(n.getNode(0).getString(0)); //sets name
             }
         else
             System.out.println("Error Resolving Array for " + n.getName());
 
 
         //resolving parent-array's index
         if(n.getNode(1).hasName("IntegerLiteral"))
             info.add(n.getNode(1).getString(0));
         else
             System.out.println("Error Resolving Array: n.getNode(1) not integer literal" +
                                "in " + n.getName());
         return info;
     }
 
 
     String tan;
 
     //Calvin: because we are concatinating to one big method string, the code to print
     //the an array assignment is in reverse order. To solve this, we will be writting to
     //arrayString, and adding that at the end of Field Decl
     String arrayString = "";
     public void visitFieldDeclaration(GNode n){
 
         if (onMeth) { tan = ""; }
 
         visit(n);
         if (onMeth) {
             String[] z = tan.trim().split("\\s+");
             String type = z[0];
             /*
               for (int i = 1; i < z.length; i++) {
               table.put(z[i], type);
               }
             */
             if(inArray){
                 //getting type
                 String arrType = n.getNode(1).getNode(0).getString(0);
                 if(arrType.equals("int"))
                     arrType = "int32_t";
                 if(arrType.equals("boolean"))
                     arrType = "bool";
 
                 String arrName = n.getNode(2).getNode(0).getString(0);
                 methodString += "__rt::Array<" + arrType + ">* " + arrName + arrayString;
                 //table.put(arrName, "__rt::Array<" + arrType + ">* ");
                 //removed if(inNewArrayExpression) code and placed into visitNewArrayExpression -Calvin 12-11
                 methodString += ";\n";
                 inArray = false;
                 //System.out.println("inArray = false in visitFieldDecl");
             }
         }
 
         if(inArray) //for arrays in constructors
             {
                 inArray = false;
 
                 //System.out.println("inArray = false in visitFieldDecl");
             }
         /*//{{{
           if (onMeth) {
           String[] z = tan.split("\\s+");
           // this could be fucked up
           String type = z[0];
           for (int i = 1; i < z.length; i++) {
           table.put(z[i], type);
           }
           }
         *///}}}
     }
 
     boolean inNewArrayExpress = false;
     public void visitNewArrayExpression(GNode n)
     {
         //changed 12-11 by Calvin. inNewArrayExpress was set to true AFTER VISIT, was giving errors
         //I assumed it was a mistake, and was meant to be above visit, so
         //I made it true before and false after, dunno if this causes problems somewhere else
         inNewArrayExpress = true;
 
 
         String arrType = n.getNode(0).getString(0); //getNode(0) is either QualifiedIdentifier or Primitive Type
         if(arrType.equals("int"))
             arrType = "int32_t";
         if(arrType.equals("boolean"))
             arrType = "bool";
 
         //todo: FIX FOR 2 dimensional arrays...iterate through children of concrete dimensions
         //ConcreteDimensions.IntegerLiteral.("1")
         String size = n.getNode(1).getNode(0).getString(0);
 
         if(resolvingShit) //if we are resolving dataFields, add it straight to the methodString -Calvin
             methodString += "new __rt::Array<" + arrType + ">(" + size + ")";
         else //add it to arrayString, which will be added to methodString at the end of visitFieldDeclaration()
             arrayString = " = new __rt::Array<" + arrType + ">(" + size + ")";
 
 
         visit(n);
         inNewArrayExpress = false;
 
     }
 
     public void visitDimensions(GNode n) {
         visit(n);
     }
 
     public void visitModifiers(GNode n){
         visit(n);
     }
 
     public void visitMethodDeclaration(GNode n)
     {
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)parent0.getProperty("parent0");
 
         //Parent 1 Should be class decl
         String classname = parent1.getString(1);
 
         //setting global class name
         cName = classname;
         String methodname = n.getString(3);
 
         Node formalParameters = n.getNode(4);
         ArrayList<String> param = new ArrayList<String>();
         for (int i = 0; i < formalParameters.size(); i++) {
             Node parameter = formalParameters.getNode(i);
             //Node type = parameter.getNode(1);
             Node temp = parameter.getNode(1).getNode(0);
             if (temp.hasName("PrimitiveType")) {
                 String prim = temp.getString(0);
                 if (prim.equals("boolean")) {
                     param.add("bool");
                 }
                 else if (prim.equals("byte")) {
                     param.add("byte"); // not sure
                 }
                 else if (prim.equals("short")) {
                     param.add("int16_t");
                 }
                 else if (prim.equals("int")) {
                     param.add("int32_t");
                 }
                 else if (prim.equals("long")) {
                     param.add("int64_t");
                 }
                 else { // char, float, double
                     param.add(prim);
                 }
             }
             else if (temp.hasName("QualifiedIdentifier")) {
                 param.add(temp.getString(0));
             }
         }
         //System.out.println("---------------------------------------------------------------1");
         //for (int i = 0; i < param.size(); i++) {
         //    System.out.println("|||||||||||||||||||||| " + param.get(i) + " |||||||||||||||||||||||||");
         //}
         //System.out.println("---------------------------------------------------------------1");
         curMub = curBub.findMethod(bubbleList, methodname, param);
         ArrayList<String> temp = curMub.getParameterTypes();
         //System.out.println("---------------------------------------------------------------2");
         //for (int i = 0; i < temp.size(); i++) {
         //    System.out.println("|||||||||||||||||||||| " + temp.get(i) + " |||||||||||||||||||||||||");
         //}
         //System.out.println("---------------------------------------------------------------2");
         //System.out.println("----------------------------WHAT IS GOING ON? " + curMub.getName());
 
 
         /*
           for(Mubble m : mubbleList){
           if(m.getClassName() == null || m.getName() == null || methodname == null || classname == null)
           System.out.println("****************YOURE FUCKED************** BECAUSE SOMETHING DIDNT WORK IN visitMETHODDECL");
           if(m.getClassName().equals(classname) && m.getName().trim().equals(methodname))
           {
           Node formalParameters = n.getNode(4);
           ArrayList<String> param = new ArrayList<String>();
           for (Node parameter : formalParameters) {
           //Node type = parameter.getNode(1);
           Node temp = parameter.getNode(1).getNode(0);
           if (temp.hasName("PrimitiveType")) {
           String prim = temp.getString(0);
           if (prim.equals("boolean")) {
           param.add("bool");
           }
           else if (prim.equals("byte")) {
           param.add("byte"); // not sure
           }
           else if (prim.equals("short")) {
           param.add("int16_t");
           }
           else if (prim.equals("int")) {
           param.add("int32_t");
           }
           else if (prim.equals("long")) {
           param.add("int64_t");
           }
           else { // char, float, double
           param.add(prim);
           }
           }
           else if (temp.hasName("QualifiedIdentifier")) {
           param.add(temp.getString(0));
           }
           }
           //curMub = findMethod(bubbles, m;
           }
 
           }
         */
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
     /*
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
     */
 
     boolean debugCallExpression = false;
     boolean inPrintStatement = false;
 
     String returns = "";
 
 
     public void visitCallExpression(GNode n) {
         System.out.println("V_V_V_V_V_V_V_CALL EXPR V_V_V_V_V_V_V_V_V_V_V_");
         //visit(n);
         boolean hasVisited = false;
         if (onMeth) {
             mName = n.getString(2);
             String tmp = "";
 
             dispatchBitch(n);
             //Dealing with System.out.print*
             if((n.getNode(0) != null && n.getNode(0).hasName("SelectionExpression")) &&//{{{
                n.getNode(0).getNode(0).hasName("PrimaryIdentifier") &&
                n.getNode(0).getNode(0).getString(0).equals("System") &&
                n.getNode(0).getString(1).equals("out") &&
                (n.getString(2).equals("print") ||n.getString(2).equals("println"))
                ){
                 if(debugCallExpression) System.out.println("Call Expression n.getNode(3): " + n.getNode(3) );
                 methodString += "std::cout << ({";
                 inPrintStatement = true;
                 visit(n);
                 inPrintStatement = false;
                 hasVisited = true;
 
                 methodString += ";})";//will this "go wrong" when dealing with method chaining?
                 //solution = abide by "inPrintStatement" rules for last ';'
                 if(n.getString(2).equals("println")){
                     methodString += " << std::endl";
                 }
             }//}}}
             else{
 
                 //want to know if this method is static
                 //TODO deal with SelectionExpression or New Class Expression
                 boolean isStaticMethod = true;
                 String theName = "";
 
                 if (n.getNode(0) != null && n.getNode(0).hasName("CallExpression")){
                     EvalCall j = new EvalCall(curBub, bubbleList, symbolTable);
                     theName = (String)(j.dispatch(n.getNode(0)));
                     //System.out.println("evaled chained method and the type of return is || " + theName);
                 }
                 else if (n.getNode(0) != null && n.getNode(0).hasName("SelectionExpression")){
                     //Non trivial examine later re: chained linking also could be Call expression
                     theName = n.getNode(0).getString(0);
                 }
                 else if (n.getNode(0) != null && n.getNode(0).hasName("NewClassExpression")){
                     theName = n.getNode(0).getNode(2).getString(0);
                 }
                 else if (n.getNode(0) != null && n.getNode(0).hasName("PrimaryIdentifier")){
                     theName = n.getNode(0).getString(0);
                 }else{
                     System.out.println("last else fall through");
                     theName = curBub.getName();
                     System.out.println("theName is " + theName);
                 }
 
                 //theName should
 
 
                 EvalCall e = new EvalCall(curBub, bubbleList, symbolTable);
                 Node argNode = n.getNode(3); //eVal Call should be dispatched on the Args Node
                 //System.out.println("Calling e.dispatch on: " + argNode.getName());
 
                 String[] params = ((String)(e.dispatch(argNode))).trim().split(" "); //RETURNING VOID
 
                 //remove empty strings
                 int scount = 0;
                 for(String s : params){
                     if(s.equals(""))
                         scount++;
                 }
                 String[] nparams = new String[params.length - scount];
                 int ind = 0;
                 for(int i = 0; i< params.length; i ++){
                     if(!(params[i].equals(""))){
                         nparams[ind] = params[i];
                         ind++;
                     }
                 }
 
 
                 ArrayList<String> pList = new ArrayList<String>(Arrays.asList(nparams));
                 isStaticMethod = isStatic(dynamicTypeTable, theName, mName, pList);
 
                 Bubble trueBubble = new Bubble();
                 String a = null;
                 if(isStaticMethod){
                    a = (String)symbolTable.lookup(theName);
 
                 }else{
                    a = (String)dynamicTypeTable.lookup(theName);
                 }
 
                 if(a == null || a.equals("constructor"))
                     a = theName;

                     for(Bubble b : bubbleList){
                         if(b.getName().equals(a))
                             trueBubble = b;
                     }

 
 
                 //resolve mangled methods (overloading)
                 Mubble trueMub = trueBubble.findMethod(bubbleList, mName, pList);
                 System.out.println(trueMub);
                 stack.push(trueMub);
 
                 String trueName = trueMub.getName();
                 System.out.println(trueName);
                 //TODO VV check this/ finish this shit
 
                 boolean isPrivate = trueMub.isPrivate();
 
 
                 if(!isStaticMethod && !isPrivate){
                     dispatch(n.getNode(0));
                     //need to fix casting for first arg
                     if(n.getNode(0) != null) {
                         methodString += "->__vptr->"+trueName;
                         methodString += "(";
                         //should cast self to expected type
                         //not doing now because castify is not a method,
                         //too complicated right now
                         dispatch(n.getNode(0));//adding self
                         //methodString += ", "; //error
                     }
                     else {
                         methodString += n.getString(2) + "(" + curBub.getName();
                     }
                     if(n.getNode(3).size() != 0)
                         methodString += ",";
                     dispatch(n.getNode(3));
                     stack.pop();
                     methodString += ")";
 
                 }
                 else {//is static
                     if(n.getNode(0) != null){ //if a class is explicitly defined
                         methodString += "_";
                         //need to know which static class we are talking about
                         String cname = (String)symbolTable.lookup(theName);
                         if (cname == null){//theName must be a reference to a class
                             cname = theName;
                         }
                         System.out.println();
                         System.out.println("");
                         Bubble papa = new Bubble();
                         for(Bubble b : bubbleList){
                             if(b.getName().equals(cname))
                                 papa = b;
                         }
                         System.out.println(papa.getName() + "::" + n.getString(2));
                         methodString += papa.getName() + "::" + trueName;
                     }
                     else{ //the static method is part of THIS class
                         System.out.println("_" + curBub.getName() + "::" + n.getString(2));
                         //methodString += "_" + curBub.getName() + "::" + n.getString(2);
                         methodString += "_" + curBub.getName() + "::" + trueName;
                     }
                     methodString += "(";
 
                     if(isPrivate){
                         //private methods need an implicit this even though they get called as if they were static
                         if(n.getNode(0) != null)
                             dispatch(n.getNode(0));
                         else
                             methodString += curBub.getName();
 
                         if(n.getNode(3).size() != 0){
                             methodString += ",";
                         }
                     }
                     dispatch(n.getNode(3));
                     stack.pop();
                     methodString += ")";
                 }
                 /*
                   dispatch(n.getNode(3));
 
                   if(!resolvingShit)
                   methodString += ")";
                 */
 
                 /* METHOD CHAINING FIX LATER//{{{
                    Node firstChild = n.getNode(0);
                    String tempString = "";
                    if (firstChild == null) { // static
                    tempString += n.getString(2) + "(";
                    dispatch(n.getNode(3));
                    tempString += ")";
                    }
                    else if (firstChild.hasName("CallExpression")) {
                    dispatch(n.getNode(0));
                    tempString += returns + "->__vptr->" + n.getString(2) +
                    "(" + returns + ", "; //maybe need a comma
                    dispatch(n.getNode(3));
                    tempString += ")";
                    returns = "";
                    }
                    else {
                    //dispatch(n.getNode(0));
                    String ob = "";
                    if (firstChild.hasName("PrimaryIdentifier")) {
                    ob = firstChild.getString(0);
                    }
                    else { // new Object()
                    ob = "tmp";
                    methodString += ob + " = ";
                    dispatch(n.getNode(0));
                    methodString += ";\n";
                    }
                    tempString += ob + "->__vptr->" + n.getString(2) +
                    "(" + ob + ", ";
                    dispatch(n.getNode(3));
                    tempString += ")";
                    }
                    Node parent0 = (Node)n.getProperty("parent0");
                    if (parent0.hasName("CallExpression")) {
                    returns = "tmp";
                    methodString += returns + " = " + tempString + ";\n";
                    }
                    else {
                    methodString += tempString +";\n";
                    }
                 *///}}}
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
         //System.out.println("INSIDE visitIntegerLiteral");
         Node parent0 = (Node)n.getProperty("parent0");
         if (onMeth && !inArray && !inNewArrayExpress) {
             if(!parent0.hasName("SubscriptExpression"))
                 methodString += n.getString(0);
         }
         else
             //methodString += "onMeth:" + onMeth + " inArray:" + inArray + " inNewArrayExpress:" + inNewArrayExpress;
             visit(n);
     }
 
     Stack<Mubble> stack;
     public void visitClassBody(GNode n){
         stack = new Stack<Mubble>();
         visit(n);
         stack = null;
     }
 
     Bubble curBub;
     SymbolTable symbolTable;
     SymbolTable dynamicTypeTable;
     public void visitClassDeclaration(GNode n){
 
         String className = n.getString(1);
         for (Bubble b : bubbleList) {
             if (b.getName().equals(className)) {
                 curBub = b;
                 break;
             }
         }
         symbolTable = curBub.getTable();
         dynamicTypeTable = curBub.getDynamicTypeTable();
 
         visit(n);
         symbolTable = null;
         dynamicTypeTable = null;
 
         //at this point all the mubbles of bubble have been filled
         for(Bubble b : bubbleList){
             if(b.getName().equals(className)) {
                 b.setIsFilled(true);
             }
         }
     }
 
     public void visitFormalParameters(GNode n){
         Node parent0 = (Node)n.getProperty("parent0");
         if (parent0.hasName("ConstructorDeclaration")) {
             symbolTable.enter(parent0.getString(2));
             dynamicTypeTable.enter(parent0.getString(2));
         }
         else if (parent0.hasName("MethodDeclaration")) {
             symbolTable.enter(parent0.getString(3));
             dynamicTypeTable.enter(parent0.getString(3));
         }
         visit(n);
         symbolTable.exit();
         dynamicTypeTable.exit();
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
                             //System.out.println("inArray = true in visitQualifiedIdenifier");
                             inArray = true;
                     }
                 }
             }
 
             String s = inNameSpace(n.getString(0));
             System.out.println(s);
             if (s != null && !inArray && !inNewClassExpression ) {
                 //using absolute namespace
                 //System.out.println("4");
                 //check to see if Bubble is found by inNameSpace\
                 methodString += "::"+s.trim().replaceAll("\\s+", "::")
                     +"::";
             }
             else if (s != null && inNewClassExpression)
                 {
                     methodString += s.trim().replaceAll("\\s+", "::")
                         +"::";
                     //if it is part of java.lang, need two underscores here
                     if(s.contains("java lang"))
                         {
                             System.out.println("********");
                             methodString+= "__";
                         }
                     else
                         System.out.println("=======\n"+s+"======\n");
 
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
 
     boolean inNewClassExpression = false;
     public void visitNewClassExpression(GNode n) {
         inNewClassExpression = true;
         if (onMeth) {
             dispatchBitch(n);
             methodString += "new ";
             dispatch(n.getNode(0));
             dispatch(n.getNode(1));
 
             if(!(n.getNode(2).getString(0).equals("Object") ||
                  n.getNode(2).getString(0).equals("String") ||
                  n.getNode(2).getString(0).equals("Class"))) {
                 methodString += "_";
             }
 
             //methodString += "_";
             dispatch(n.getNode(2));
             methodString += "(";
             dispatch(n.getNode(3));
             dispatch(n.getNode(4));
             methodString += ")";
 
         }
         else {
             visit(n);
         }
         inNewClassExpression = false;
     }
 
     public void visitImportDeclaration(GNode n){
         visit(n);
     }
 
     public void visitForStatement(GNode n)
     {
         if (onMeth) {
             methodString += "for(";
         }
         symbolTable.enter("for");
         dynamicTypeTable.enter("for");
         visit(n);
         symbolTable.exit();
         dynamicTypeTable.exit();
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
         if (onMeth && !(n.getNode(2).hasName("NewArrayExpression"))) {
             dispatchBitch(n);
             dispatch(n.getNode(0));
             methodString += " " + n.getString(1) + " ";
             dispatch(n.getNode(2));
 
         }
         else if(onMeth && n.getNode(2).hasName("NewArrayExpression")){
             dispatch(n.getNode(0));
             dispatch(n.getNode(2));
             methodString += arrayString;
         }
         else {
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
         Node parent0 = (Node)n.getProperty("parent0");
         boolean hasEntered = false;
         if (parent0.hasName("WhileStatement")) {
             symbolTable.enter("while");
             dynamicTypeTable.enter("while");
             hasEntered = true;
         }
         if (parent0.hasName("DoWhileStatement")) {
             symbolTable.enter("dowhile");
             dynamicTypeTable.enter("dowhile");
             hasEntered = true;
         }
         if (parent0.hasName("ConditionalStatement")) {
             symbolTable.enter("if-else");
             dynamicTypeTable.enter("if-else");
             hasEntered = true;
         }
         if (parent0.hasName("Block")) {
             symbolTable.enter("block");
             dynamicTypeTable.enter("block");
             hasEntered = true;
         }
         if (parent0.hasName("SwitchStatement")) {
             symbolTable.enter("switch");
             dynamicTypeTable.enter("switch");
             hasEntered = true;
         }
         if (parent0.hasName("TryCatchFinallyStatement")) {
             symbolTable.enter("try-finally");
             dynamicTypeTable.enter("try-finally");
             hasEntered = true;
         }
         if (parent0.hasName("CatchClause")) {
             symbolTable.enter("catch");
             dynamicTypeTable.enter("catch");
             hasEntered = true;
         }
 
         if(((Node)n.getProperty("parent0")).getName()
            .equals("MethodDeclaration") ||
            ((Node)n.getProperty("parent0")).getName().equals("ConstructorDeclaration")) {
             onMeth = true;
             //table = new HashMap<String, String>();
 
             visit(n);
             onMeth = false;
 
             curMub.addCode(outputFormat(methodString));
             //System.out.println("method string:" + methodString);
 
             methodString = "";
             //table = null;
         }
         else {
             visit(n);
         }
         if (hasEntered){
             symbolTable.exit();
             dynamicTypeTable.exit();
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
                                 //System.out.println("inArray = true in visitPrimitiveType");
                             }
                     }
             }
         if (onMeth && !inArray & !inNewArrayExpress) { //a little sloppy of a fix
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
 
         //System.out.println("got here 1");
         if (onMeth) {
             String key = "";
             //System.out.println("got here 2");
             dispatchBitch(n);
             String type = "";
             //System.out.println("got here 3");
             Node callex = (Node)n.getProperty("parent0");
             if (callex.hasName("CallExpression") &&
                 callex.getNode(0) != null && callex
                 .getNode(0).hasName("PrimaryIdentifier")) {
                 //System.out.println("got here 4");
                 key = callex.getNode(0).getString(0);
                 //System.out.println("key is ::" + key);
                 type = (String)symbolTable.lookup(key);
                 //System.out.println(type);
                 //if type is null here it is a static method, and the Class is what is important
                 //--AF
                 if(type == null || type.equals("constructor"))
                     type = key;
                 //System.out.println(key + " " + type);
             }
 	    //visit(n);
 	    if(n.size() > 0) {
 		dispatch(n.getNode(0));
 	    }
 	    for (int i = 1; i < n.size(); i++) {
 		methodString += ", ";
 		dispatch(n.getNode(i));
 	    }
 	}
 	    //System.out.println("before peek?");
 
 	    //Mubble m = stack.peek();
 	    //System.out.println(m);
 
 	    //System.out.println("stack.peek");
             /*
               String mSign = "";
               for (Mubble m : mubbleList) {
 
               //System.out.println(m);
               //System.out.println(m.getName());
               //System.out.println(type);
 
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
               System.out.println(mSign);
               Matcher m = Pattern.compile("(?<=,\\s)\\S*(?=\\s*)").matcher(mSign);
               String p = "";
 
               while(m.find()){
               p+= " " + m.group();
               }
 
               String [] par = p.trim().split("\\s");
 	      System.out.println();
 	      System.out.println();
 	      System.out.println();
               for (int i = 0; i < par.length; i++) {
               System.out.println(par[i]);
               }
 	      System.out.println();
 	      System.out.println();
 	      System.out.println();
 	      */
             /* WHY DO WE NEED THIS
                for( String g : par)
                System.out.println(g);
             */
 	    /*
             ArrayList<String> ppp = m.getParameterTypes();
             String[] par = new String[ppp.size()];
 	    System.out.println();
 	    System.out.println();
 	    System.out.println();
             for (int i = 0; i < ppp.size(); i++) {
                 par[i] = ppp.get(i);
 		System.out.println(par[i]);
             }
 	    System.out.println();
 	    System.out.println();
 	    System.out.println();
 	    */
 	    /*
             String s = "";
 
 	    System.out.println("before casting");
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
                     //methodString += "dick";
                     dispatch(n.getNode(0));
                     methodString += ")";
                 }
                 else {
                     if(!inPrintStatement && !inNewClassExpression)
                         methodString += " ";
                     dispatch(n.getNode(0));
                 }
 
             }
 
             for(int i = 1; i < n.size(); i++) {
                 methodString += ", ";
                 dispatch(n.getNode(i));
             }
 
 
 
               //if(!inPrintStatement && !resolvingShit)
               //methodString += ")";
 
 
         }
         else {
             visit(n);
         }
 	    */
 	    else {
 
 	    visit(n);
 	    }
     }
 
     public void visitSelectionExpression(GNode n) {
         if (onMeth) {
             visit(n);
             //EDIT think this is fixed
             if (n.get(1) != null) {
                 //System.out.println("n.get(0).getString(0) :: "+((Node)n.get(0)).getString(0) );
                 //System.out.println("n.getString(1) :: " + n.getString(1));
                 //first child is null or second string is null or !(sys out)
                 //if first child exists && first child is PrimaryIdentifier && its first string is system
                 //&& second child is not null and is out
                 if(  n.get(0)==null || n.getString(1)==null  || n.getNode(0).get(0) == null ||
                      !(((Node)n.get(0)).getString(0).equals("System") && n.getString(1).equals("out")))
 
                     methodString += (checkAncestor(n,"ConstructorDeclaration") ? "" : "->") + n.getString(1);
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
         //putting parentheses around everything in an additive expression
 
         if (onMeth) {
             methodString += "(";
             dispatchBitch(n);
             dispatch(n.getNode(0));
             methodString += " "+n.getString(1)+" ";
             dispatch(n.getNode(2));
             methodString += ")";
         }
         else {
             visit(n);
         }
 
     }
 
     public void visitMultiplicativeExpression(GNode n) {
         if (onMeth) {
             methodString += "(";
             dispatchBitch(n);
             dispatch(n.getNode(0));
             methodString += " "+n.getString(1)+" ";
             dispatch(n.getNode(2));
             methodString += ")";
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
 
     public void visitThisExpression(GNode n) {
         if (onMeth && !checkAncestor(n, "ConstructorDeclaration")) {
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
 
     ///////HELPER METHODS////////
 
     public boolean isStatic(SymbolTable a, String vcName, String methName, ArrayList<String> params){
         //queries whether a given method with a variable is a static method
         //TODO need to pass parameters
 
         //System.out.println("Is static being called with vari/class name || " + vcName + "|| and methName || " + methName);
         String cname = (String)a.lookup(vcName);
         if (cname == null || cname.equals("constructor")){//must reference a class
             cname = vcName;
         }
 
         Bubble papa = new Bubble();
         for(Bubble b : bubbleList){
             if(b.getName().equals(cname))
                 papa = b;
         }
 
         //Mubble mama = new Mubble();
         //System.out.println("bout to call find method for || " + methName + " || with bubble name :: " + papa.getName());
         Mubble mama = papa.findMethod(bubbleList, methName, params);
         if(mama == null)
             System.out.println("Problem finding mubble with bub.findMethod");
         return mama.isStatic();
     }
 
     public boolean checkAncestor(Node n, String name){
         //this is to check if any of the nodes parents are of type name
         if (n.hasName(name)){
             return true;
         }
         Node parent0 = (Node)n.getProperty("parent0");
         if (parent0 == null){
             return false;
         }
         return checkAncestor(parent0, name);
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
