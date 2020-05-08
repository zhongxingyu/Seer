 package xtc.oop;
 import java.util.ArrayList;
 import xtc.tree.GNode;
 
 public class Declaration{
 	public ArrayList<String> modifiers;
 	public String returntype;
 	public String name;
 	public ArrayList<Fparam> params;
 	public String ownerClass;
 	public GNode bnode;
 	public ArrayList<LocalVariable> variables;
 	public boolean isVirtual;
 	public int overloadNum;
 	public int specificity;
 
         //constructor for Class and Object methods
 	Declaration(String rtype, String mname, String sclass,ArrayList<Fparam> fparams) {
             name = mname;
             returntype = rtype;
             params = new ArrayList<Fparam>(fparams);
             ownerClass = sclass;
             modifiers = new ArrayList<String>(0);
             overloadNum = 0;
             specificity = 0;
 	}
 	
 	Declaration(int ol, String rtype, String mname, String sclass, ArrayList<Fparam> fparams,
                     GNode node, ArrayList<LocalVariable> lvar) {
             this( rtype, mname,sclass, fparams);
             bnode = node;
             modifiers = new ArrayList<String>();
             overloadNum = ol;
 	}
 
 	Declaration(int ol,ArrayList<String> mods, boolean virtual, String rtype, String mname, 
                     String sclass,ArrayList<Fparam> fparams,GNode node,ArrayList<LocalVariable> lvar){
             this(ol,rtype, mname, sclass, fparams, node, lvar);
             isVirtual = virtual;
             variables = new ArrayList<LocalVariable>(lvar);
             modifiers = new ArrayList<String>(mods);
 	}
 	
 	/**
 	 * will cycle through all variables for name
 	 * returns the type of that name
 	 *
 	 */
 	public ArrayList<String> search_for_type(String name){
             for (LocalVariable i : variables) {
                 if(name.equals(i.name)){
                     ArrayList<String> type = new ArrayList<String>(i.packages);
                     type.add(i.type);
                     return type;
                 }
             }
             return null; //error type does not exist
 	}
 	
 	/**
 	 * will cycle throu all variables for name
 	 * and then update the type to the new type
 	 * will create a new LocalVariable if none exists
 	 */
 	public void update_type(String name, ArrayList<String> newpack, String newtype) {
             boolean found = false;
 
             for (LocalVariable v : variables) {
                 if (name.equals(v.name)) {
                     found = true;
                     v.type = newtype;
                     v.packages = newpack;
                 }
             }
             if (!found) {
                 variables.add(new LocalVariable(newpack,newtype,name));
             }
 	}
 	public boolean is_static(){
             return modifiers.contains("static");
 	}
 
 }
 class LocalVariable {
 	ArrayList<String> packages;
 	String name;
 	String type;
 
 	LocalVariable(ArrayList<String> packages, String type, String name) {
             this.packages = packages;
             this.name = name;
             this.type = type;
 	}
 
 }
 class Fparam {
 	public ArrayList<String> modifiers;
 	public String type;
 	public String var_name;
         Fparam(String type, String var_name) {
             this.type = type;
             this.var_name = var_name;
            this.modifiers = new ArrayList<String>();
         }
 	Fparam(ArrayList<String> mods, String type, String var_name) {
             this(type, var_name);
             this.modifiers = mods;   
 	}
 }
