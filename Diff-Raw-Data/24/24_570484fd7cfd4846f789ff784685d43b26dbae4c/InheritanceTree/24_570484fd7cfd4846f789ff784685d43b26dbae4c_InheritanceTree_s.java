 /**
  * Builds a tree structure so subclasses can access superclass's vtables when
  * building their own vtables. links all the classes together in a heiarchy for
  * easy searchability
  *
  * (C) 2010 P.Hammer, A.Krebs, L. Pelka, P.Ponzeka
  */
 package xtc.oop;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 public class InheritanceTree{
 	public InheritanceTree root;
 	public String packageName;//ex: xtc.oop 
 	public final String className;
 	public ArrayList<InstanceField> fields;
 	public ArrayList<Declaration> constructors;
 	public ArrayList<Declaration> local; //all methods defined IN THIS CLASS virtual or not!
 	public ArrayList<Declaration> Vt_ptrs; //all methods in vtable
 	public InheritanceTree superclass;
 	public ArrayList<InheritanceTree> subclasses;
 	
 	boolean VERBOSE = false;
 	
 
 	/**
 	 * The constructor for creating the Object class Inheritance tree.
 	 * @param none
 	 * 
 	 */
 	InheritanceTree(){
 		this.packageName = "java.lang";
 		this.root = this;
 		
 		className = "Object";
 		superclass = null; //no superclass for Object
 
 		fields = new ArrayList<InstanceField>(); //no instancefields in Object
 		Vt_ptrs = new ArrayList<Declaration>(); // for Vtable
 		
 		//add __isa to Vtable
 		Vt_ptrs.add(new Declaration("Class", "__isa",
                                     className,new ArrayList<Fparam>(0)));
 		
 		//add __delete to Vtable
 		ArrayList<Fparam> p = new ArrayList<Fparam>();
 		p.add(new Fparam(new ArrayList<String>(),"__Object*","__this"));
 		Vt_ptrs.add(new Declaration("void", "__delete",
                                     "Object", p));
 		
 		//add hashcode to Vtable
 		p.clear();
 		p.add(new Fparam(new ArrayList<String>(),"Object","__this"));
 		Vt_ptrs.add(new Declaration("int32_t", "hashCode",
                                     "Object", p));
 		//add equals to Vtable
 		p.clear();
 		p.add(new Fparam(new ArrayList<String>(),"Object","__this"));
 		p.add(new Fparam(new ArrayList<String>(),"Object","other"));
 		Vt_ptrs.add(new Declaration("bool", "equals",
                                     "Object", p));
 		//add getClass to Vtable
 		p.clear();
 		p.add(new Fparam(new ArrayList<String>(),"Object","__this"));
 		Vt_ptrs.add(new Declaration("Class","getClass",
                                     "Object",p));
 		//add toString to Vtable
 		p.clear();
 		p.add(new Fparam(new ArrayList<String>(),"Object","__this"));
 		Vt_ptrs.add(new Declaration("String","toString",
                                     "Object",p));
 
 		//subclass are initalized to a 0 element arraylist
 		subclasses = new ArrayList<InheritanceTree>(0);
 
 		
 		
 	}
 	
 	/**
 	 * The constructor for creating the Class class.
 	 * @param InheritanceTree (the Object inheritance tree)
 	 */
 	InheritanceTree(InheritanceTree supr,String string_or_class){
 		this.root = supr.root;
 		this.packageName = supr.packageName;
 		this.superclass = supr;
 		this.className = string_or_class;
 		
 		//copies the superclass's Vtable into virtual Vtable
 		this.Vt_ptrs = new ArrayList<Declaration>(supr.Vt_ptrs);
 		
 		
 		//change __isa field to point to this className's feild
 		ArrayList<Fparam> p = new ArrayList<Fparam>(0);
 		Vt_ptrs.set(0,new Declaration("Class", "__isa",
 									  className,p));
 					
 		//change __delete field to point to this classNames's feild
 		p.add(new Fparam(new ArrayList<String>(),"__"+className+"*","__this"));
 		Vt_ptrs.set(1,new Declaration("void", "__delete",
                                     className, p));
 		
 		//change toString to point to className's toString method
 		p.clear();
 		p.add(new Fparam(new ArrayList<String>(),className,"__this"));
 		Vt_ptrs.set(5,new Declaration("String","toString",
                                     className,p));
 		
 
 		
 		if(string_or_class.equals("Class")){
 		
 		
 			//adds virtual Class methods ptrs to virtual Vtable
 			p.clear();
 			p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 			Vt_ptrs.add(new Declaration("String","getName",
 									 "Class",p));
 		
 			p.clear();
 			p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 			Vt_ptrs.add(new Declaration("String","getSuperclass",
                                     "Class",p));
 		
 			p.clear();
 			p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 			Vt_ptrs.add(new Declaration("String","getComponentType",
                                     "Class",p));
 		
 			p.clear();
 			p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 			Vt_ptrs.add(new Declaration("String","isPrimitive",
                                     "Class",p));
 		
 			p.clear();
 			p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 			Vt_ptrs.add(new Declaration("String","isArray",
                                     "Class",p));
 		
 			p.clear();
 			p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 			p.add(new Fparam(new ArrayList<String>(),"Object","o"));
 			Vt_ptrs.add(new Declaration("String","isInstance",
                                     "Class",p));
 		}
 		else{
 			//change hashCode to point to className's hashCode
 			p.clear();
 			p.add(new Fparam(new ArrayList<String>(),className,"__this"));
 			Vt_ptrs.set(2,new Declaration("int32_t", "hashCode",
 										className, p));
 			//change equals to point to className's equals
 			p.clear();
 			p.add(new Fparam(new ArrayList<String>(),className,"__this"));
 			p.add(new Fparam(new ArrayList<String>(),"Object","other"));
 			Vt_ptrs.set(3,new Declaration("bool", "equals",
 										className, p));
 			
 			//add virtual String methods ptrs to virtual Vtable
 			p.clear();
 			p.add(new Fparam(new ArrayList<String>(),"String","__this"));
 			Vt_ptrs.add(new Declaration("int32_t","length",
 										"String",p));
 			p.clear();
 			p.add(new Fparam(new ArrayList<String>(),"String","__this"));
 			p.add(new Fparam(new ArrayList<String>(),"int32_t","idx"));
 			Vt_ptrs.add(new Declaration("char","charAt",
 										"String",p));
 		
 		}
 		
 		//subclass are initalized to a 0 element arraylist
 		subclasses = new ArrayList<InheritanceTree>(0);
 		
 		//add this tree to the superclasses' subclasses
 		supr.subclasses.add(this);
 		
 		
 	}
 	/**
 	 * The constructor for all InheritanceTree's that do not represent Object or Class.
 	 * will create vtable for class and then attach the tree to the superclass tree.
 	 * @param GNode (classdeclaration), InheritanceTree (superclass's).
 	 */
 	InheritanceTree(String packageName, GNode n, InheritanceTree supr){
 		this.root = supr.root;
 		this.packageName = packageName;
 		this.superclass = supr;
 		this.className = n.getString(1);
 		this.constructors = new ArrayList<Declaration>(0);
 		this.local = new ArrayList<Declaration>(0);
 		if(VERBOSE)System.out.println("TREE NODE "+packageName+" "+className);
 		//copies the superclass's Vtable into virtual arraylist
 		this.Vt_ptrs = new ArrayList<Declaration>(supr.Vt_ptrs);
 
 		//change __isa field to point to this className's feild
 		ArrayList<Fparam> p = new ArrayList<Fparam>(0);
 		Vt_ptrs.set(0,new Declaration("Class", "__isa",
 									  className,p));
 		
 		//change __delete field to point to this classNames's feild
 		p.add(new Fparam(new ArrayList<String>(),"__"+className+"*","__this"));
 		Vt_ptrs.set(1,new Declaration("void", "__delete",
 									  className, p));
 
 		//add __class() to local methods
 		local.add(new Declaration("Class","__class",className,								 
 								  new ArrayList<Fparam>(0)));
 
 		
 		//constructors defined
 		//local methods defined
 		//virtual methods defined
 		//alters Vt_ptr if overridding exists
 		//gives unique # if overloading exists
 		ArrayList<Declaration> virtual = addmethoddeclarations(n);
 		
 		
 		//add virtual methods to vtable
 		Vt_ptrs.addAll(virtual);
 		
 
 		//field arraylist defined with all field declarations
 		this.fields = addfielddeclarations(n);
 		
 		//add instancevariables to the localvariables of each method in this class
 		for(Declaration d: Vt_ptrs) d.initializeLocalVariables(fields);
 		for(Declaration d: local) d.initializeLocalVariables(fields);
 			
 		//subclass are initalized to a 0 element arraylist
 		this.subclasses = new ArrayList<InheritanceTree>(0);
 		
 		//add this tree to the superclasses' subclasses
 		supr.subclasses.add(this);
 
 	}
 	/**
 	 *looks in Vt_ptrs and local for methods with same name but diff params
 	 * if multiple are found keeps method with max overloadNum stored
 	 * does overridden test and changes Vt_ptr if true
 	 * returns max [overloadNum,boolean add_to_virtual]
 	 */
 	private int[] overloaded_ridden_check(String method, ArrayList<Fparam> params, boolean is_virtual,GNode body) {
 		int max = -1;
 		for (Declaration i : Vt_ptrs) {
         
 			//same name test
 			if (method.equals(i.name)) {
 				//diff params test
 				if ((params.size() == i.params.size())&&(is_virtual)&&(typetest(i.params, params))){
 					this.Vt_ptrs.set(Vt_ptrs.indexOf(i),new Declaration(i.overloadNum,i.modifiers,i.isVirtual,i.returntype,i.name,
 															this.className,params,body,new ArrayList<Variable>(0)));
 						
 					if(VERBOSE){System.out.print(this.className+"\tOVERRIDING vtptr slot #"+Vt_ptrs.indexOf(i)+" with:\n\t -"+method+"(");
 						for(int x=0; x<params.size();x++)
 							System.out.print(params.get(x).type+" ");System.out.println(")");
 					}
 					//signify to NOT ADD TO VIRTUAL
 						//bc its overridden
 					return new int[]{i.overloadNum-1,0};
 				}
 				else {
 					max = Math.max(i.overloadNum, max);
 				
 				}	
 			}
 		}
 		for (Declaration j : local) {
 			//same name test
 			if (method.equals(j.name))  {
 					//impossible for a local method to be overriden by a local method
 					//so must be overloaded
 					max = Math.max(j.overloadNum, max); //store max overloadNum
 				if(VERBOSE){System.out.print(this.className+"\tOVERLOADING current max # = "+max+" \n\t -"+method+"(");
 					for(int x=0; x<params.size();x++)
 						System.out.print(params.get(x).type+" ");System.out.println(")");}
 			}
 		}
 		if((VERBOSE)&&(max==-1)) System.out.println(this.className+"\tfirst method named "+method+" in this scope");
 		
 		return new int[]{max,1};
 
 	}
 	/**
 	 *helper method used in overload__ridden_check 
 	 *  returns true if both params arraylist have same types through and through
 	 */
 	private boolean typetest(ArrayList<Fparam> vptrs, ArrayList<Fparam> other){
 		for (int j = 1; j<vptrs.size(); j++) {//ignore __this param checking
                     //if types do not match up return FALSE
                     if (!(vptrs.get(j).type.equals(other.get(j).type)))
                         return false;//***this means that this is an overloaded method
 		}
 		return true;
 	}
 	/**
 	 * uses visitors to create the Declarations for virtual methods.
 	 * also documents local methods that are not virtual for definition later
 	 *@param GNode (always a classDeclaration)
 	 */
 	private ArrayList<Declaration> addmethoddeclarations(GNode n){;
 		// cast to a node
 		Node node = n;
 		//declare a virutal arraylist<Declaration>
 		final ArrayList<Declaration> virtual = new ArrayList<Declaration>(0);
 		final InheritanceTree myclass = this;
 		
 		//inline visitor for methoddeclarations
 		new Visitor(){
 			
 			//declare initalize variables 
 			ArrayList<String> modifiers = new ArrayList<String>(0);
 			String retrn = "";
 			String className= "";
 			String methodname="";
 			ArrayList<Fparam> params = new ArrayList<Fparam>(0);
 			ArrayList<String> mods = new ArrayList<String>(0);
 			String fptype ="";
 			String fpname ="";
 			int overloaded = 0;
 			int[] overloaded_ridden;
 			GNode block;
 			boolean is_fparam=false;
 			boolean is_virtual=false;
 			boolean is_constructor =false;
 			
 			public void visitClassDeclaration(GNode n){
 				className = n.getString(1);
 				visit(n);
 			}
 			public void visitConstructorDeclaration(GNode n){
 				is_constructor=true;
 				
 				//clear variables
 				modifiers.clear();
 				retrn="";
 				methodname = n.getString(2);
 				params.clear();
 				block=null;
 				is_virtual = true;
 				overloaded=0;
 				
 				//get info from tree
 				visit(n);
 				overloaded = constructors.size();
 				//add declaration to constructor
 				constructors.add(new Declaration(overloaded, modifiers, is_virtual, retrn, methodname, className, params,
 								block, new ArrayList<Variable>(0)));
 				
 				is_constructor = false;
 			}
 			/** ---METHOD DECLARATIONS -------------------------------------------------------------
 			 */
 			public void visitMethodDeclaration(GNode n){
 				//clear variables
 				modifiers.clear();
 				retrn = "";
 				methodname = n.getString(3);
 				params.clear();
 				block = null;
 				is_virtual = true;//virtual until proven false
 				
 				//go into tree to get info
 				visit(n);
 				
 				//search in Vt_ptrs and local for OVERLOADnum and if it is OVERRIDEN
 				overloaded_ridden = overloaded_ridden_check(methodname,params,is_virtual,block);
 				
 				//add it to virtual if it is_virtual and it was NOT overridden
 				if((is_virtual)&&(overloaded_ridden[1]==1)) 
 					virtual.add(new Declaration(overloaded_ridden[0]+1,modifiers,is_virtual,retrn,methodname,className,params,
 								block,new ArrayList<Variable>(0)));
 				
 				//add it to local no matter what
 				local.add(new Declaration(overloaded_ridden[0]+1,modifiers,is_virtual,retrn,methodname,className,params,
 								block,new ArrayList<Variable>(0)));
 									
 			}// end of METHOD DECLARATIONS ----------------------------------------------------------
 			public void visitBlock(GNode n){
 				block = n;
 			}
 			public void visitModifiers(GNode n){
 				visit(n);
 			}
 			public void visitModifier(GNode n){
 				if (is_fparam) mods.add(n.getString(0));//add to fparam modifier
 				 
 				//notes that private/staic methods are not virtual and should not go into Vtable
 				else if ((n.getString(0).equals("private"))||(n.getString(0).equals("static"))){
 					is_virtual = false;
 					modifiers.add(n.getString(0));
 				}
 				else if(n.getString(0).equals("final")) modifiers.add("const");//final/const conversion
 				else modifiers.add(n.getString(0));//add to method modifiers
 			}
 			public void visitVoidType(GNode n){
 				retrn = "void";
 			}
 			public void visitQualifiedIdentifier(GNode n){
 				String type =n.getString(0);
 				int size = n.size();
 				for(int i=1;i<size;i++) 
 					type+="::"+n.getString(i);
 				if(n.size()==1)type = FQify(type);//make it fully qualified
 				
 				if(is_fparam) fptype=type;
 				else retrn = type;
 			}
 			
 			public void visitPrimitiveType(GNode n){
 				String type=n.getString(0);//primitive converstions
 				if(n.getString(0).equals("int")) type="int32_t";
 				if(n.getString(0).equals("boolean")) type="bool";
 				if(n.getString(0).equals("long")) type="int64_t";
 				if(n.getString(0).equals("short")) type="int16_t";
 				if(n.getString(0).equals("byte")) type="int8_t";
 
 				if(is_fparam) fptype = type;
 				else retrn = type;
 			}
 			public void visitDimensions(GNode n){
 				if(is_fparam){ 
 					//all arrays defined in java.lang
 					fptype ="java::lang::ArrayOf"+forArrays(fptype);
 				}
 				else{ 
 					//all arrays defined in java.lang
 					retrn ="java::lang::ArrayOf"+forArrays(retrn);
 				}
 			}
 			public void visitFormalParameters(GNode n){
 				//add __this parameter for virtual methods
 				if((is_virtual)&&(!is_constructor)) 
 					params.add(new Fparam(new ArrayList<String>(0),className,"__this"));
 				visit(n);
 			}
 			public void visitFormalParameter(GNode n){
 				is_fparam =true;
 				mods.clear();
 				fpname =n.getString(3);//variable name
 				fptype="";
 				visit(n);
 				params.add(new Fparam(mods,fptype,fpname));//add formal parameter
 				is_fparam=false;
 					
 			}
 			public void visit(Node n) {
 				for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 			}
 			/** returns String to add on to ArrayOf..
 			 */
 			public String forArrays(String name){
 
 				if(name.contains("::"))return name.substring(name.lastIndexOf(":")+1);
 				if(name.equals("int32_t")) return "Int";
 				if(name.equals("bool")) return "Boolean";
 				if(name.equals("int16_t"))return"Short";
 				if(name.equals("int64_t")) return"Long";
 				if(name.equals("int8_t")) return"Byte";
 				if((name.equals("char"))||(name.equals("float"))||(name.equals("double"))){
 					char c = (char)(name.charAt(0)-32);//uppercase first letter
 					return c + name.substring(1);
 				}
 				return name;
 			}
 			/** returns String of fully qualified type 
 			 */
 			public String FQify(String type){
 				if((type.equals("Object"))||(type.equals("Class"))||(type.equals("String")))
 					return "java::lang::"+type;
 				else {
 					InheritanceTree t = myclass.root.search(type);
 					if((t!=null)&&(!t.packageName.equals(""))){
 						return t.packageName.replace(".","::")+"::"+type;
 					}
 					else return type;
 				}
 			}
 			
 		}.dispatch(n);
 	
 		return virtual;
 	}
 	/**
 	 * returns ArrayList<InstanceField> that holds all the fields fieldDeclaration info.
 	 * @param GNode n
 	 * however instance fields are defined or declared outside the constructor 
 	 *		is how they will be stored here.
 	 *
 	 */
 	private ArrayList<InstanceField> addfielddeclarations(GNode n){
 		Node node = n;
 		final InheritanceTree myclass = this;
 		final ArrayList<InstanceField> f = new ArrayList<InstanceField>(0);
 		final java.util.List<String> modifiers = java.util.Arrays.asList(new String[]
 					{"public:","static","private:","const","protected:"});
 		
 		//---adds all non-private/static fields from superclass to this class
 		for(InstanceField i: superclass.fields){
 			if(i.modifiers.size()==0)f.add(i);//inherit the field 
 			for(String mod: i.modifiers){
 				if((mod.equals("private"))||(mod.equals("static"))){}//do nothing
 				else f.add(i);//inherit the field 
 			}
 		}
 		//inline visitor sends FieldDeclaration GNodes to EWalk and parses the returned strings
 		//solves the need for more complex visitors (in InheritanceTree) to handle all possible initialized values of fields
 		new Visitor(){
 			String classname;
 			public void visitClassDeclaration(GNode n){
 				classname = n.getString(1);
 				visit(n);
 			}
 			/** FieldDeclarations use EWalk to  get their values--------------------------------
 			 */
 			public void visitFieldDeclaration(GNode n){
 				EWalk e =new EWalk(myclass,new Declaration(classname,f),n);
 				CppPrinter field =new CppPrinter(n);
 				
 				String s =field.getString().toString();
 				s=s.replaceAll("\n","");
 				if(VERBOSE)System.out.println("EWALK= "+s);
 				ArrayList<String> mods = new ArrayList<String>(0);
 				String value, modsAndNames;//variables to be filled with info
 				String pkg="";//needs initialization
 				String type="";//needs initialization
 				if(s.contains("=")){
 					String[] valueSplit =s.split("=");
 					modsAndNames = valueSplit[0];
 					value = valueSplit[1];
 				}
 				else{ 
 					value="";
 					modsAndNames =s;
 				}
 				String[] slist =modsAndNames.split(" ");
 				
 				int j =0;
 				while (modifiers.contains(slist[j])){j++;}//find the type element
 				
 				for(int m=0;m<slist.length;m++){
 					if(VERBOSE)System.out.print(" ("+slist[m]+")");
 					if(m<j)mods.add(slist[m]);//modifiers
 					else if(m>j){//add the field
 						f.add(new InstanceField(pkg,mods,type,slist[m],value));
 						if(VERBOSE)System.out.println("\nADDED FIELD---"+f.get(f.size()-1).var_name+"-to->"+myclass.className);
 					}
 					else if(slist[m].contains(".")){//package and type
 						pkg = slist[m].substring(0,slist[m].lastIndexOf("."));
 						type = slist[m].substring(slist[m].lastIndexOf("."));
 					}
 					else {type = slist[m]; pkg="";}
 				}
 				if(VERBOSE)System.out.println();
 			}//end of FieldDeclarations -------------------------------------------------------
 			
 			public void visitClassBody(GNode n){visit(n);}
 			public void visitMethodDeclaration(GNode n){/*do not visit below here*/}
 			
 		/** returns String of fully qualified type 
 		*/
 			public String FQify(String type){
 				if((type.equals("Object"))||(type.equals("Class"))||(type.equals("String")))
 					return "java::lang::"+type;
 				else {
 					InheritanceTree t = myclass.root.search(type);
 					if(!packageName.equals("")){
 						return t.packageName.replace(".","::")+"::"+type;
 					}
 					else return type;
 				}
 			}		   	   
 			public void visit(Node n) {
 				for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 			}
 		}.dispatch(node);
 		
 		return f;
 	
 	}
 	
 	/** search will return a InheritanceTree of mathing fullyqualifiedname
 	  * or a null inheritanctree if no tree exists yet as child of the root
 	  */
 	public InheritanceTree search(String fullyqualifiedname){
 		if(VERBOSE)System.out.println("SEARCHINGfullyqualifiedname for "+fullyqualifiedname+" against "+this.className);
 
 		InheritanceTree found = null;
 		//check if we did not get a fullyqualified name to search by
 		if((!fullyqualifiedname.contains("."))&&(!fullyqualifiedname.contains("::"))){
 			return searchUnqualified(fullyqualifiedname);
 		}
 		if ((this.packageName+"."+this.className).equals(fullyqualifiedname))
 			return this;
 		for (InheritanceTree i : subclasses) {
 			found = i.search(fullyqualifiedname);
 			if (found!=null) return found;
 		}
 		return found;
 	
 	}/** will check the unqualified name to see if there are matches
 	  * will return the first found match not 
 	  */
 	public InheritanceTree searchUnqualified(String type){
 		if(VERBOSE)System.out.println("SEARCHINGunqualifiedname for "+type+" against "+this.className);
 
 		InheritanceTree found = null;
 		if ((this.className).equals(type))
 			return this;
 		for (InheritanceTree i : subclasses) {
 			found = i.searchUnqualified(type);
 			if (found!=null) return found;
 		}
 		return found;
 	
 	}
 	/**
 	 * method used to retrieve the corret overloaded method and append its overloaded number
 	 */
 	public String[] search_for_method(boolean on_instance,ArrayList<String> paramtyps, 
 																String method_name){
 		LinkedList<Declaration> possible= new LinkedList<Declaration>();
 		
 		//-----ACCESSABLE/SAME NAME/SAME #PARAMS CHECK
 		//looks in local(non virtual, non-private[if on_instance])and VT_ptrs 
 		//for same named methods and same number of parameters
 		for (Declaration j : Vt_ptrs) {
 			if ((j.name.equals(method_name)) && ((j.params.size()-1) == paramtyps.size()))
 				possible.add(j);
 		}
 		for (Declaration l : local ) {
 			if ((l.name.equals(method_name)) && (!l.isVirtual)&&(!((on_instance)&&(l.isprivate())))&&
 				 (l.params.size() == paramtyps.size())) 
 				possible.add(l);
 		}
 		//if only one left 
 		//RETURN [returntype,accessor+NAME+_number]
 		if (possible.size()==1) {
 			Declaration choosen = possible.get(0);
 			return new String[]{choosen.returntype, make_name(on_instance, choosen)};
 		}
 		//----SPECIFICITY CHECK
 		//need to zero out specificity for next check
 		for(Declaration d : possible){
 			d.specificity =0;
 		}
 		int size= paramtyps.size();
 		for (int m=0; m<size; m++) {
 				String type = paramtyps.get(m);
 				for (Declaration c : possible) {
 					String pos_type = c.params.get(m).type;
 					if (!pos_type.equals(type));
 					else {
 						int casting = gouptree(pos_type,type);
 						if (casting == -1) { possible.remove(c);}//not castable
 						else c.specificity = c.specificity + casting;
 					}
 				}
 		}
 		//find method with smallest number MUST BE ONLY ONE (MIN)
 		Declaration min = possible.get(0);
 		for(Declaration n : possible ){
                     if(min.specificity>n.specificity)
                         min=n;
 		}
 		//RETURN [returntype,accessor+NAME+_number]
 		return new String[]{min.returntype, make_name(on_instance,min)};
 	}
 	/**
 	 * helper method finds how specific the casting is 
 	 * by way of distance from castee to casted_to
 	 *
 	 */
 	private int gouptree(String casted_to, String castee){
 		int distance = 0;
         
 		if (castee.equals("char")) {
 			if (casted_to.equals("short"))
 				return -1;
 			else
 				castee = "short"; // has same precidence as a short, but can't be casted to short
 		} 
 		else if (casted_to.equals("char")) 
 			return -1;//char cannot be implicitly casted to 
 		
 		final java.util.List<String> primatives = java.util.Arrays.asList(new String[]
 				{"double", "float", "long", "int", "short", "byte" });
             
 		if (primatives.contains(casted_to) && primatives.contains(castee))
 			return primatives.indexOf(casted_to) - primatives.indexOf(castee);
 		
 		/*  if EWALK is using already translated prim types USE THIS ONE
 		 
 		 if (castee.equals("char")) {
 		 if (casted_to.equals("int16_t"))
 		 return -1;
 		 else
 		 castee = "int16_t"; // has same precidence as a short, but can't be casted to short
 		 } 
 		 else if (casted_to.equals("char")) 
 		 return -1;//char cannot be implicitly casted to 
 		 
 		 final java.util.List<String> primatives = java.util.Arrays.asList(new String[]
 		 {"double", "float", "int64_t", "int32_t", "int16_t", "int8_t" });
 		 
 		 
 		 if (primatives.contains(casted_to) && primatives.contains(castee))
 		 return primatives.indexOf(casted_to) - primatives.indexOf(castee);
 		 
 		 
 		 */
 		
 		//search on root of tree for castee class
 		InheritanceTree type = this.root.search(castee);
 		while( !type.className.equals(casted_to) ){
                     distance++;
                     type = type.superclass;
 		}
 		return distance;
 	}	
 	/**
 	 * helper method for search_for_method returns proper method call
 	 * 
 	 */
 	private String make_name(boolean on_instance,Declaration d){
 		String result="";
 		if ((on_instance) && (d.isVirtual))
                     result= "->__vptr->";
 		else if ((on_instance) && (!d.isVirtual))
                     result=".";
 		else
                     result= "";
 		result+= d.name;
 		if(d.overloadNum==0)return result;
 		else return result+"_"+d.overloadNum;
 	}
 	/** will return packageName in cpp syntax "::"
 	 */
 	public String getCppPkg(){
 		if (this.packageName.equals("")) return "";
 		return this.packageName.replace(".","::")+"::";
 	}
 
 }//end of inheritancetree
