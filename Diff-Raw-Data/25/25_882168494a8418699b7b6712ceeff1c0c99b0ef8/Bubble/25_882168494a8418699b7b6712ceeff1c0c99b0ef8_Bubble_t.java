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
 	    this.mubbles.add(m);
     }
 
     //Set the name of this Bubble
     public void setName(String name) {
 	this.name = name;
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
 		//why do we need two typedefs? one with underscores one without?
         /* Not sure if this is correct... changing to below -af
 		return "typedef " + pkgpath + this.name + " " + this.name + ";\n" +
 			"typedef " + pkgpath + "_"+this.name + " _" + this.name + ";\n";
         */
 
 		return "typedef " + this.name + "* " + this.name + ";\n";
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
 		for(Mubble constructor : mubbles) {
 			if(constructor.isConstructor())
 				ret += "_"+constructor.getName()+"();\n";
 		}
 		ret+="\n//Forward declaration of methods\n";
 		//Hardcoding the vt and class
 		ret += "static Class __class();\n" +
 			"static _"+this.name+"_VT __vtable;\n";
 		for(Mubble m : mubbles) {
 			//HARDCODING STATIC, MAY NEED TO CHANGE
 			ret+= "static "+m.forward() + "\n";
 		}
 		//unindent
 		ret += "};\n";
 		return ret;
 	}
 
 	public String getStructVT() {
 		String ret = "// The vtable layout for "+this.name+".\n";
 		ret += "struct _"+this.name+"_VT {\n";
 		//indent
 		//Hardcoding class
		ret += "Class __isa;\n";
 		for(Mubble m : mubbles) {
 			ret += m.vTable1()+"\n";
 		}
 		//Make VT constructor in-line, hardcoding class (indent?)
 		ret+="\n_"+this.name+"_VT()\n: __isa(_"+this.name+"::__class())";
 		for(Mubble m : mubbles) {
 			ret += ",\n"+m.vTable2();
 		}
 		ret+=" {\n";
 		//unindent
 		ret+="}\n";
 		//unindent
 		ret+="};\n";
 		return ret;
 	}
 
 
     public String getC() {
         //returns an
        return "todo: getC method in Bubble";
     }
 }//}}}
