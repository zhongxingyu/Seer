 package xtc.oop.helper;
 import java.util.ArrayList;
 
 public class Bubble{
     ArrayList<Bubble> bubbles; //ArrayList of the children of this Bubble
     ArrayList<Mubble> mubbles; //ArrayList of Mubbles in this class
     ArrayList<Field> dataFields; // Field is a new structure created to hold both variable name, type, and modifiers
     String name; //This class' name
     Bubble parentBubble; //This Bubble's parent Bubble
     Pubble parentPubble; //This class' package (reference)
     boolean isBuilt = false; //determine whether a bubble has been completely filled in (excluding code)
     boolean isFilled = false; //determine whether all the code portions of this bubble's mubbles has been filled in
     //Removed Constructor arraylist because mubbles have a flag for that
 
     /* DO WE NEED THIS?:
        String visibility; //The visibility for this class
        */
 
     ////////////////////
     /* CONSTRUCTOR(S) */
     ////////////////////
 
     public Bubble(String name) {
         //TODO: Make this constructor, what are the params?
         //What's the context it's called from?
 
         this.name = name;
         this.bubbles = new ArrayList<Bubble>();
         this.dataFields = new ArrayList<Field>();
         this.mubbles = new ArrayList<Mubble>();
     }
 
     public Bubble(){
         this.bubbles = new ArrayList<Bubble>();
         this.dataFields = new ArrayList<Field>();
         this.mubbles = new ArrayList<Mubble>();
     }
 
 
     ////////////////
     /* Other Meth */
     ////////////////
     public boolean hasName(String name)
     {
         if (this.name.equals(name))
             return true;
         else
             return false;
     }
 
     /////////////
     /* SETTERS */
     /////////////
 
 
     //Add a Bubble to the Bubble list
     public void addBubble(Bubble b) {
         this.bubbles.add(b);
     }
 
     //Add a data field
     public void addField(Field f) {
         this.dataFields.add(f);
     }
 
     //Add a Mubble to this Bubble
     public void addMubble(Mubble m) {
         m.setBubble(this);
         this.mubbles.add(m);
     }
 
     //Set the name of this Bubble
     public void setName(String name) {
         this.name = name;
     }
 
     public Mubble findMethod(String methodName, ArrayList<String> para) {
         Mubble mub = null;
         int match = 10000;
         for (Mubble m : mubbles) {
             if (m.belongToGroup(methodName)) {
                 int min = 0;
                 ArrayList<String> p = m.getParameterTypes();
                 if (p.size() == para.size()) {
                     for (int i = 0; i < p.size(); i++) {
                         if (!p.equals(para)) { // needs fixing
                             min++;
                             /*
                                primitive types, objects
                                e.g. methods       function calls
                                m(long)       m(int) -> 1
                                m(long)       m(Long) -> 2??
                                m(long)       m(Shape) -> INF
                                m(A)          m(B) -> 1
                                m(A)          m(C) -> 2
                                m(A)          m(AAAA) -> INF
                                */
                         }
                     }
                     if (min < match) {
                         mub = m;
                     }
                 }
                 else {
                     continue;
                 }
             }
         }
         return mub; //do i want to return string?
     }
 
     //Set the parent Bubble of this Bubble
     public void setParentBubble(Bubble b) {
         this.parentBubble = b;
     }
 
     //Set the parent Pubble of this Bubble
     public void setParentPubble(Pubble p) {
         this.parentPubble = p;
     }
 
     public Bubble setIsBuilt(boolean bool)
     {
         isBuilt = bool;
         return this;
     }
 
     public Bubble setIsFilled(boolean bool)
     {
         isFilled = bool;
         return this;
     }
 
 
     /* Setter for visibility
     //Set the visibility of this Bubble
     public void getVisibility(String visibility) {
     this.visibility = visibility;
     }
     */
 
     /////////////
     /* GETTERS */
     /////////////
 
     public boolean isBuilt(){
         return isBuilt;
     }
 
     public boolean isFilled(){
         return isFilled;
     }
 
     //Returns ArrayList of child Bubbles
     public ArrayList<Bubble> getBubbles() {
         return this.bubbles;
         //return this.bubbles.toArray();
     }
 
     //Returns ArrayList of dataFields
     public ArrayList<Field> getDataFields() {
         return this.dataFields;
     }
 
     //Returns ArrayList of Mubbles
     public ArrayList<Mubble> getMubbles() {
         return this.mubbles;
         //return this.mubbles.toArray();
     }
 
     //Returns the name of this class
     public String getName() {
         return this.name;
     }
 
     //Returns this class' parent class
     public Bubble getParentBubble() {
         return this.parentBubble;
     }
 
     //Returns this class' package
     public Pubble getParentPubble() {
         return this.parentPubble;
     }
 
     public String getPackageName(){
         return this.parentPubble.getName();
     }
 
 
     /* Getter for visibility
     //Returns the visibility of this class (public, private, etc.)
     public String getVisibility() {
     return this.visibility;//{{{
     }
     */
 
     ///////////////////
     /* MISC. METHODS */
     ///////////////////
 
     public String getTypeDef() {
         String pkgpath = "";
         Pubble p = this.parentPubble;
         //e.g. java::lang::Object
         while(p != null) {
             pkgpath = p.getName() + "::" + pkgpath;
             p = p.getParent();
         }
         /* Not sure if this is correct... changing to below -af
            return "typedef " + pkgpath + this.name + " " + this.name + ";\n" +
            "typedef " + pkgpath + "_"+this.name + " _" + this.name + ";\n";
            */
 
         return "typedef _" + this.name + "* " + this.name + ";\n";
     }
 
     public void inheritMethods(){
         //takes parents methods for vtable.
         ArrayList<Mubble> newMethodsList = new ArrayList<Mubble>(parentBubble.getMubbles());
         for(int i = 0; i < newMethodsList.size(); i++){
             Mubble m = newMethodsList.get(i);
             if(m.isMain() || m.isStatic() || m.isConstructor())
                 newMethodsList.remove(i--);
         }
         for(Mubble m : newMethodsList){
             m.setFlag('i');
             m.setBubble(this);
         }
         //kick out overwritten old methods
         for(int i = 0; i < newMethodsList.size(); i ++){
             Mubble m = newMethodsList.get(i);
             for(Mubble n : mubbles){
                 if (m.getName().equals(n.getName()) && paramMatch(m, n)) {
                     newMethodsList.remove(i);
                     n.setFlag('w');
                     newMethodsList.add(i, n);
                 }
             }
         }
 
         //add new methods
         for(Mubble m : mubbles){
             if(!(m.getFlag() == 'w')){
                 newMethodsList.add(m);
             }
         }
         this.mubbles = newMethodsList;
     }
 
     public boolean paramMatch(Mubble one, Mubble two){
         //checks if two mubbles have identical parameter types
         ArrayList<String> o = one.getParameterTypes();
         ArrayList<String> t = two.getParameterTypes();
         if(o.size() != t.size())
             return false;
         for(int i = 0; i < o.size(); i ++){
             if(!o.get(i).equals(t.get(i)))
                 return false;
         }
         return true;
     }
 
     public String getFDeclStruct() {
         return "struct _"+this.name+";\n"+
             "struct _"+this.name+"_VT;\n";
     }
 
     public String getStruct() {
         String ret = "struct _" + this.name + " {\n";
         //indent
         ret += "//Data fields\n";
         //add the VT vptr
         ret += "_"+this.name+"_VT* __vptr;\n";
         //iterate through datafields, print them
         for(int i = 0; i < this.dataFields.size(); i++) {
             //output data fields
         }
         ret+="\n//Constructors\n";
         //loop through methods once to see if there are any constructors
         //if not create a default one
         boolean encounteredConstructor = false;
         for(Mubble m: mubbles){
             if(m.isConstructor())
                 encounteredConstructor = true;
         }
         if(!encounteredConstructor) //if there was no constructor in the java file, create default one
         {
             ret += "_" + name + "(); \n\n";
         }
         else
         {
             for(Mubble constructor : mubbles) {
                 if(constructor.isConstructor())
                     ret += "_"+constructor.getName()+"();\n";
             }
         }
         //static void __delete(__Object*);
         ret += "\n//The destructor\n";
         ret += "static void __delete(_" + this.name + "*);\n";
 
         ret+="\n//Forward declaration of methods\n";
         //Hardcoding the vt and class
         ret += "static Class __class();\n" +
             "static _"+this.name+"_VT __vtable;\n";
         for(Mubble m : mubbles) {
             //HARDCODING STATIC, MAY NEED TO CHANGE
            if(!m.isConstructor() && m.getFlag() != 'i' && !m.isDelete()) //if its a constructor, inherited method, or delete, don't print it
                 ret += m.forward() + "\n";
         }
         //unindent
         ret += "};\n";
         return ret;
     }
 
     public String getStructVT() {
         String ret = "\n// The vtable layout for "+this.name+".\n";
         ret += "struct _"+this.name+"_VT {\n";
         //indent
         //Hardcoding class
         ret += "Class __isa;\n";
         //ret +=  "void (*__delete)(_" + this.name + "*);\n";
         for(Mubble m : mubbles) {
             if(!m.isConstructor()) //if its a constructor
                 ret += m.vTable1()+"\n";
         }
         //Make VT constructor in-line, hardcoding class
         ret+="\n_"+this.name+"_VT()\n: __isa(_"+this.name+"::__class())";
         //ret += "__delete(&_" + this.name + "::__delete)";
         for(Mubble m : mubbles) {
             if(!m.isConstructor() && !m.isMain())
                 ret += ",\n"+m.vTable2();
         }
         ret+=" {\n";
         //unindent
         ret+="}\n";
         //unindent
         ret+="};\n";
         return ret;
     }
 
 
     public String getCC() {
         //returns a complete .cc entry for this class
         String ret = "";
 
         //loop through methods once to see if there are any constructors
         //if not create a default one
         boolean encounteredConstructor = false;
         for(Mubble m: mubbles){
             if(m.isConstructor())
                 encounteredConstructor = true;
         }
         if(!encounteredConstructor) //if there was no constructor in the java file, create default one
         {
             ret += "_" + name + "::_" + name + "(): __vptr(&__vtable){} \n\n";
         }
         for(Mubble m: mubbles){
             //if the method is not inherited
             if(m.getFlag() != 'i')
                 ret += m.getCC() + "\n\n";
         }
 
 
         return ret;
         //return "todo: getC method in Bubble";
     }
     }//}}}
