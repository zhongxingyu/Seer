 package xtc.oop.helper;
 import java.util.ArrayList;
 import xtc.tree.GNode;
 
 //import helper.Pubble;
 //import helper.Bubble;
 //import helper.Field;
 
 public class Mubble{
     //possible values for flag
     private final char INHERITED = 'i';
     private final char NEW = 'n';
     private final char OVERLOADED = 'l';
     private final char OVERWRITTEN = 'w';
     
     GNode constructorNode = null;
 
     boolean constructor;
     boolean superConstructorCalled; //whether the super constructor was in the constructor
     ArrayList<String> superParams; //parameter types of super constructor
     boolean main;
     boolean staticMethod;
     char flag;
 
     Bubble className;
     Pubble packageName;
 
     String methodName;
     String group;
     String returnType; //if none and not a constructor -->> void
     String visibility;
     String originallyFrom;
     ArrayList<String> paraName;
     ArrayList<String> paraType;
     ArrayList<String> paraMod;
     ArrayList<Field> parameters;
 
     String code;
 
     public Mubble(String methodName) { // constructor with a method name
         parameters = new ArrayList<Field>();
         paraType = new ArrayList<String>();
         paraName = new ArrayList<String>();
         paraMod = new ArrayList<String>();
         superParams = new ArrayList<String>();
         constructor = false;
         superConstructorCalled = false;
         main = false;
         staticMethod = false;
         visibility = "public"; //default
         group = this.methodName = methodName;
         if(methodName.equals("main")){
             main = true;
         }
         code = "";
         flag = '@';
     }
 
     public Mubble(Mubble m){
         this.code = m.getCode();
         this.methodName = m.getName();
         superConstructorCalled = false;
     }
 
     public boolean isPrivate(){
         if(this.visibility.equals("private"))
             return true;
         else
             return false;
     }
     
     public ArrayList<String> getSuperParams(){
         return this.superParams;
     }
     public void setSuperParams(ArrayList<String> params){
         this.superParams = params;
     }
 
     public void addCode(String code){
 	    if(!isMain() || !this.code.equals("")) { //if string already has something
 	        this.code += code;
 	    }
 	    else {
 	        String p = paraName.get(0);
 	        this.code = "/*\n"+
 		    "__rt::Ptr<__rt::Array<String> > "+p+" = new __rt::Array<String>(argc-1);\n"+
 		    "for(int i = 1; i < argc; i++){\n"+
 		    "(*"+p+")[i-1] = argv[i];\n"+
 		    "}\n*/\n";
 	        this.code += code;
 	    }
     }
 
     //adds code to the beginning of this mubbles code
     public void prependCode(String ncode){
         this.code = ncode + this.code;
     }
 
     public void setSuperConstructorCalled(boolean bla){
         this.superConstructorCalled = bla;
     }
     public boolean getSuperConstructorCalled(){
         return this.superConstructorCalled;
     }
     public void addParameter(Field parameter){
         parameters.add(parameter);
     }
 
     public void mangleName(String name2) {
         methodName = name2;
     }
 
     public boolean belongToGroup(String g) {
         //System.out.println("Calling belongs to group: " + g + "==" + group + "?");
         return group.equals(g);
     }
 
     public int rank(ArrayList<String> type) {
         if (paraType.size() != type.size()) {
             return -1; // should it return a big number?
         }
         int sum = 0;
         /*
            for (int i = 0; i < paraType.size(); i++) {
            how do i access bubbles???
            }
            */
         return 0;
     }
     
    //gets constructor node
     public GNode getConstructorNode(){
         return this.constructorNode;
     }
     public void setConstructorNode(GNode g){
         this.constructorNode = g;
     }
 
     public String getCode() {
         return this.code;
     }
 
     public String findBirthPlace() {
 	return originallyFrom;
     }
 
     public String getCC(){
         if(main)
             return "";
         String ret = ccHeader() + "{\n";
         ret += getCode() + "\n}";
         return ret;
     }
 
     /* generates header for .cc files */
     public String ccHeader() {
         StringBuilder s = new StringBuilder("");
         if (staticMethod) {
             // working? yeah
             s.append(returnType).append(" _").append(getClassName()).
                 append("::").append(methodName).append("(");
             for (int i = 0; i < paraType.size(); i++) {
                 if (i != 0) {
                     s.append(", ").append(paraType.get(i) + " " + paraName.get(i));
                 }
                 else {
                     s.append(paraType.get(i)+  " " + paraName.get(i));
                 }
             }
             s.append(")");
         }
         else {
             if(!this.isConstructor())
             {
                 s.append(returnType);
                 s.append(" _").append(getClassName()).
                 append("::").append(methodName).append("(");
                 if(this.isDelete())
                     s.append("_");
                 s.append(getClassName());
                 if(this.isDelete())
                     s.append("*");
                 s.append(" __this");
 				/*
                 for (String para : paraType) {
                     s.append(", ").append(para).append();
 				}
 				*/
 
 				for (int i = 0; i < paraType.size(); i++) {
 					s.append(", ").append(paraType.get(i) + " " + paraName.get(i));
 				}
 
 
 
                 s.append(")");
             }
             else //it IS a constructor
             {
                 s.append("_" + className.getName() + "::_" + className.getName() + "(");
                 boolean isFirst = true;
                 for(Field f : this.getParameters()){
                     if(!isFirst)
                         s.append(", ");
                     if(isFirst)
                         isFirst = false;
                     s.append(f.type + " " + f.name);
                 }
 
                 s.append("): __vptr(&__vtable)");
             }
         }
         return s.toString();
     }
 
     public String forward() {
         if(main){
             return "";
         }
         StringBuilder s = new StringBuilder();
         if(staticMethod) {
             s.append("static ");
             s.append(returnType).append(" ").append(methodName).append("(");
 
             for (int i = 0; i< paraType.size(); i++) {
 				if(i == 0 && isStatic())
 					s.append(paraType.get(i));
 				else
 					s.append(", ").append(paraType.get(i));
             }
             s.append(");");
 
             return s.toString();
         }
 	s.append("static ");
         s.append(returnType).append(" ").append(methodName).append("(");
         if(this.isDelete())
             s.append("_");
         s.append(getClassName());
         if(this.isDelete())
             s.append("*");
         for (String para : paraType) {
             s.append(", ").append(para);
         }
         s.append(");");
 
         return s.toString();
     }
 
     public char getFlag() {
         return flag;
     }
 
     public String getClassName(){
         return className.getName();
     }
     public Bubble getBubble() {
         return className;
     }
 
     public String getGroup(){
         return group;
     }
 
     public Pubble getPackage() {
         return packageName;
     }
 
     public String getPackageName() {
         return packageName.getName();
     }
 
     public String getName() {
         return methodName;
     }
 
     public String getReturnType() {
         return returnType;
     }
 
     public String getVisibility() {
         return visibility;
     }
 
     public ArrayList<String> getParameterNames() {
         return paraName;
     }
 
     public ArrayList<String> getParameterModifier() {
         return paraMod;
     }
 
     public ArrayList<String> getParameterTypes() {
         return paraType;
     }
 
     public ArrayList<Field> getParameters() {
         return parameters;
     }
 
     public boolean isConstructor() { // returns true if this is constructor
         return constructor;
     }
 
     public boolean isDelete(){ //returns true if this is the delete method
         return this.methodName.equals("__delete");
     }
     public boolean isMain() { // returns ture if this is main method
         return main;
     }
 
     public boolean isStatic() { // returns true if this is static method
         return staticMethod;
     }
 
     public Mubble setBubble(Bubble className) {
         this.className = className;
         return this;
     }
 
     public Mubble setConstructor(boolean constructor) {
         this.constructor = constructor;
         return this;
     }
 
     public Mubble setFlag(char flag) {
         this.flag = flag;
         if (flag == NEW) {
             originallyFrom = className.getName();
         }
         return this;
     }
 
     public Mubble setMain(boolean main) {
         this.main = main;
         return this;
     }
 
     public Mubble setPackage(Pubble packageName) {
         this.packageName = packageName;
         return this;
     }
 
     public Mubble setReturnType(String returnType) {
         this.returnType = returnType;
         return this;
     }
 
     public Mubble setStatic(boolean staticMethod) {
         this.staticMethod = staticMethod;
         return this;
     }
 
     public Mubble setVisibility(String visibility) {
         this.visibility = visibility;
         return this;
     }
 
     public Mubble setParameters() {
         for (Field f : parameters) {
             paraName.add(f.name);
             paraType.add(f.type);
             if (f.modifiers.size() == 1) {
                 paraMod.add(f.modifiers.get(0));
             }
             else if (f.modifiers.size() == 0) {
                 paraMod.add("");
             }
             else {
                 System.out.println("Error size cannot be bigger than 2");
             }
         }
         return this;
     }
 
     public Mubble setParameterNames(ArrayList<String> paraName) {
         this.paraName = paraName;
         return this;
     }
 
     public Mubble setParameterTypes(ArrayList<String> paraType) {
         this.paraType = paraType;
         return this;
     }
 
     /* generates entry for vtable1 */
     public String vTable1() {
         if(main || staticMethod){
             return "";
         }
         StringBuilder s = new StringBuilder();
         s.append(returnType).append(" (*");
         s.append(methodName).append(")(");
         if (!isStatic()) {
             if(this.isDelete())
                 s.append("_");
             s.append(getClassName());
             if(this.isDelete())
                 s.append("*");
             for (String para : paraType) {
                 s.append(", ").append(para);
             }
         }
         else {
             // not sure what to do with static methods
             if (paraType.size() > 0) {
                 s.append(paraType.get(0));
             }
 
             for (int i = 1; i < paraType.size(); i++) {
                 s.append(", ").append(paraType.get(i));
             }
         }
 
         s.append(");");
 
         return s.toString();
     }
 
     /* generates entry for vtable.
      */
     public String vTable2() {
         if(main || staticMethod){
             return "";
         }
         //StringBuilder type = new StringBuilder();
         /*
            if (from == INHERITED) {
            type.append("(").append(returnType).append("(*)");
            type.append(getClassName());
            for (String para : paraType) {
            type.append(",").append(para);
            }
            type.append(")");
            }
            */
 
         StringBuilder s = new StringBuilder();
         s.append(methodName).append("(");
         //if (type != null) {
         //    s.append(type.toString()).append(")");
         //}
 
         if (flag == INHERITED) { // this line is not quite right
             s.append("(").append(returnType).append("(*)(");
             s.append(getClassName());
             for (String para : paraType) {
                 s.append(",").append(para);
             }
             /*
             if(className.getName().equals("String") || className.getName().equals("Object") )
                 s.append(")").append("&__");
             else
             */
 
             s.append("))").append("&");
             Bubble ancestor = className.getParentBubble();
 	    Bubble anc = null;
             String inheritedfrom = "Object";
             boolean found = true;
             while (ancestor != null && found) {
                 ArrayList<Mubble> mubbles = ancestor.getMubbles();
                 for (Mubble mub : mubbles) {
                     if (mub.getName().equals(methodName) && mub.getFlag() != INHERITED) {
                         inheritedfrom = ancestor.getName();
 			anc = ancestor;
                         found = false;
                     }
                 }
                 ancestor = ancestor.getParentBubble();
             }
             if (inheritedfrom.equals("Object") || inheritedfrom.equals("String") ||
                     inheritedfrom.equals("Class")) {
                 s.append("java::lang::__").append(inheritedfrom);
                     }
             else {
 		String pack = anc.getPackageName().trim().replace(" ", "::");
 
                 s.append("_").append(inheritedfrom);
             }
         }
         else {
             s.append("&_").append(getClassName());
         }
         s.append("::").append(methodName);
         s.append(")");
 
         return s.toString();
     }
 
     public Mubble copy() {
 	Mubble clone = new Mubble(this.getName());
 	clone.setConstructor(this.isConstructor());
 	clone.setMain(this.isMain());
 	clone.setStatic(this.isStatic());
 	clone.setBubble(this.getBubble());
 	clone.setPackage(this.getPackage());
 	clone.setFlag(this.getFlag());
 	//methodname set by constructor
 	//group set by constructor
 	clone.setReturnType(this.getReturnType());
 	clone.setVisibility(this.getVisibility());
 
 	//String originallyFrom?????????
 
 	for(Field f : this.getParameters()) {
 	    clone.addParameter(f.deepCopy());
 	}
 	clone.setParameters(); //does paraName, paraType, paraMod
 	//ArrayList<String> paraName
 	//ArrayList<String> paraType
 	//ArrayList<String> paraMod
 	//ArrayList<Field> parameters
 	return clone;
     }
 }
 
