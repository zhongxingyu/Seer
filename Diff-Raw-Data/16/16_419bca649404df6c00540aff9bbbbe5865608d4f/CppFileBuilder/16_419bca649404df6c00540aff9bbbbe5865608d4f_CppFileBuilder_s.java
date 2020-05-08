 /* creates a c++ class datalayout headerfile and a c++ method definition .cpp file
  * then populates these files with the proper information stored in each InheritanceTree object
  *
  * (C) 2010 P.Hammer, A.Krebs, L. Pelka, P.Ponzeka
  */
 
 package xtc.oop;
 
 import java.io.File;
 import java.util.ArrayList;
 
 public class CppFileBuilder{
 	public final boolean DEBUG=false;
 	
 	FileMaker h;
 	FileMaker cpp;
 	DependencyFinder fileinfo;
 	ArrayList<ClassStruct> allClasses;
 	public boolean VERBOSE = true;
 	
 	private File jfile;
 
 	
 	CppFileBuilder(DependencyFinder fileinfo, ArrayList<ClassStruct> allClasses){
 		/*
 		 *creates new cc file h
 		 *copies start of translation.h into h
 		 */
                 this.fileinfo = fileinfo;
                 this.allClasses = allClasses;
 			
 		jfile = new File(fileinfo.getFilePath());
 
                 String namespaceName = DependencyFinder.getNamespace(allClasses, fileinfo.getFilePath());
		h = (new FileMaker(namespaceName,"_dataLayout","h"));
 		h.write("/* Object-Oriented Programming\n"+
               "* Copyright (C) 2010 Robert Grimm\n"+
               "*\n"+
               "* This program is free software; you can redistribute it and/or\n"+
               "* modify it under the terms of the GNU General Public License\n"+
               "* version 2 as published by the Free Software Foundation.\n"+
               "*\n"+
               "* This program is distributed in the hope that it will be useful,\n"+
               "* but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
               "* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"+
               "* GNU General Public License for more details.\n"+
               "*\n"+
               "* You should have received a copy of the GNU General Public License\n"+
               "* along with this program; if not, write to the Free Software\n"+
               "* Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,\n"+
               "* USA.\n"+
               "*/\n\n"+
 
              "#pragma once\n\n");
 
             // #includes all files its dependent on, then using declares them
             for (String importDeclaration : fileinfo.getCppIncludeDecs(allClasses, DependencyOrigin.IMPORT) ) {
                     h.write(importDeclaration+"\n");
             }
             h.write("\n");
 
             for (String usingDeclaration : fileinfo.getCppUsingDeclarations(allClasses)) {
                 h.write(usingDeclaration+"\n");
                 //h.write(DependencyFinder.getNamespace(fileinfo.getFileClasses(), fileinfo.getFilePath())+"\n");
             }
             h.write("\n");
 
             for(String p : fileinfo.getPackageToNamespace()){
                     h.write("namespace "+p+" {\n");
             }
 			for (ClassStruct c : fileinfo.getAllClassesInPackage(null,fileinfo.getPackageName())) {
                 h.write("\n\tstruct __" + c.className + "; \n"
                         +/**/ "\tstruct __" + c.className + "_VT;\n"
                         +/**/ "\ttypedef __rt::Ptr<__" + c.className + "> " + c.className + ";\n"
                         + "\ttypedef __rt::Ptr<__Array<" + c.className + "> > ArrayOf" + c.className + ";\n\n");
             }
 
 		/*
 		 *creates new cc file cc_methoddef
 		 *copies start of translation.cc into cc_classdef
 		 */
		cpp =(new FileMaker(namespaceName,"_methoddef","cpp"));
 		cpp.write(
 			"/* Object-Oriented Programming\n"+
 			"* Copyright (C) 2010 Robert Grimm\n"+
 			"*\n"+
 			"* This program is free software; you can redistribute it and/or\n"+
 			"* modify it under the terms of the GNU General Public License\n"+
 			"* version 2 as published by the Free Software Foundation.\n"+
 			"*\n"+
 			"* This program is distributed in the hope that it will be useful,\n"+
 			"* but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
 			"* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"+
 			"* GNU General Public License for more details.\n"+
 			"*\n"+
 			"* You should have received a copy of the GNU General Public License\n"+
 			"* along with this program; if not, write to the Free Software\n"+
 			"* Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,\n"+
 			"* USA.\n"+
 			"*/\n\n"+
 			
 			"#include \""+h.cFile.getName()+"\"\n\n"+
 
 			"#include <sstream>\n\n");
 		for(String p: fileinfo.getPackageToNamespace()){
 					cpp.write("namespace "+p+" {\n");
 		}
 		cpp.write("\n\n");
 
 	}//end of constructor
 	/*
 	 * writes to the h file GNode n's class declaration and Vtable
 	 *	same structure as http://cs.nyu.edu/rgrimm/teaching/fa10-oop/1007/java_lang.h from class notes
 	 */
 	public void addClassdef(InheritanceTree t){
 		
 		String ClassName = t.className;
 		h.write("//data layout for "+fileinfo.getPackageName()+ClassName);
 		h.write(/* CLASS STRUCT DECLARATION*/
 				"\n\tstruct __"+ClassName+"{ \n"+
 				"\t\t__"+ClassName+"_VT* __vptr;\n");
 
 				/* FEILDS ---> ex: int x;  */
 		        write_all_feilds(t); h.write("\n\n");
 
 				
 				/*CONSTRUCTOR(S)*/
 				write_all_constructors(t); 
 		
                                 
 		        /*  ALL INSTANCE METHODS */
 				write_all_methods(t);
 
 				h.write(
 				/*create instance of VTABLE*/
 				"\n\t\tstatic __"+ClassName+"_VT __vtable;\n"+
 				"\t};"+
 		/* ---------------------start of stuct __ClassName_VT in .h file -------------------*/
 				"\n\n//vtable layout for "+fileinfo.getPackageName()+t.className+"\n"+                      
 						 
 				"\tstruct __"+ClassName+"_VT{\n");
 				
 				/* DECLARE METHOD PTRS ---> ",methodreturnType (*methodname)(methodparameters)",\n" */
 				write_all_method_ptrs(t); h.write("\n\n"+/**/
 						
 				/*  VT CONSTRUCTOR*/		 
 				"\t\t__"+ClassName+"_VT():\n");
 						 
 				/* INITIALIZE METHOD PTRS ---> methodname"(&__"+ClassName+"::"+methodname+"),\n" */
 				write_assign_method_ptrs(t); h.write( /**/
 				"{}\n"+
 						 
 			"\t};\n\n"
 		/* -----------end of stuct __ClassName_VT in .h file -------------------*/
 						 
 		);// end of writing
 		
 		//now create .cpp file
 		addMethodDef(t);
 					
 	}//end of addClassdef
 	
 	
 	/**
 	 * will write a string of all feild declarations in this class.
 	 */
 	private void write_all_feilds(InheritanceTree t) {
 		
             //loops through fields and prints out in proper syantax
             for (InstanceField f : t.fields) {
 				h.write("\t\t");
                 for(String m : f.modifiers) {
 					//ignore protection bc we assume correct
 					if((!m.equals("public"))&&(!m.equals("private"))&&(!m.equals("protected"))&&(!m.equals("const"))
 					   &&(!m.equals("static")))//cannot get static initializatin to work yet
 						h.write(m+" ");
                 }
 				h.write(f.type+" "+f.var_name+";\n");//do not assign the value!!!
             }
 		h.write("\t\t"+t.className+" __this;\n");//global THIS for local virutal methods
 
 	}
 	
 	/**
 	 * will write all the constructor's into the datalayout for this class.
 	 * adds no argument constructor to class's without at least one constructor (main file)
 	 *
 	 */	
 	private void write_all_constructors(InheritanceTree t){
 		String FQclassName = t.className;//to change from classname to fully qualified easier
 
 		
 		//class with main method needs constructor
 		if(t.constructors.size()==0){
 			h.write("\t\t__"+t.className+"():__vptr(&__vtable)");
 			// intialize all the instance fields
 			for (InstanceField f : t.fields) {
 				h.write(","+f.var_name+"("+f.var_name+")");
 			}
 			h.write("{};\n\n"+
 					"\t\tvoid init("+t.className+" __passedthis){\n\t\t\t"+
 					"__this = __passedthis;\n\t\t};");
 		
 		}
 		else{
             for (Declaration constr : t.constructors) {
                 h.write("\t\t");
 
                 //loop through constructor modifiers
                 for (String modifier : constr.modifiers){
                     //h.write(modifier+": ");
                 }
                 //write className
                 h.write("__"+t.className);
 
                 h.write("(");
 
                 //loop through formal parameter
                 for (int i=0;i<constr.params.size();i++) {
                     Fparam fp = constr.params.get(i);
 
                     if (i>0) h.write(","); //comma
 
                     //loop through formal parameter's modifiers
                     for (String modifier : fp.modifiers) {
                         h.write(modifier+" ");
                     }
                     //writes formal parameter's type and name
                     h.write(fp.type+" "+fp.var_name);
                 }
                 //intialize __vptr
                 h.write("):__vptr(&__vtable)");
 
                 // intialize all the instance fields
                 for (InstanceField f : t.fields) {
 					if(f.value.equals("")) h.write(","+f.var_name+"("+f.var_name+")");
 					else h.write(","+f.var_name+"("+f.value+")");
                 }
                 h.write("{");//3 tabs for Ewalk
                 h.write("\n\t\t//empty constructor. All work done in init");
                 h.write("\n\t   }\n\n");
 
 			}
 		}
 	}
 	
 	/**
 	 * will write all local method declarations in this class
 	 * syntax --->  "static "+ returntype +" "+methodName+" ("+className+","+..other paramaterTypes,..+");\n"
 	 *
 	 */		
 	private void write_all_methods(InheritanceTree t){
 
 		for(Declaration c: t.constructors){
 			h.write("\t\tvoid init("+t.className);
 			for(Fparam cp: c.params){
 				h.write(","+cp.type);
 			}
 			h.write(");\n");
 		}
             //loops through local methods and prints out in proper syantax
             for (Declaration method : t.local) {
                 if (method.name.equals("main")) {
                     buildMain(method);
                     h.write("\t\tstatic int32_t "+method.name);
                     if(method.overloadNum!=0)
                             h.write("_"+method.overloadNum);
                     h.write("(int32_t, char**);\n");
                 }
                 else {
                     for (String modifier : method.modifiers) {
                        //h.write("\t"+modifier+": \n");
                     }
                     h.write("\t\tstatic "+method.returntype+" "+method.name);
                     if(method.overloadNum!=0)
                             h.write("_"+method.overloadNum);
                     h.write("(");
                     for (int j=0; j<method.params.size();j++) {
 						Fparam param= method.params.get(j);
                         if(j==0)h.write(param.type);//write without ","
                         else h.write(","+param.type);
                     }
                     h.write(");\n");
                 }
             }
 	}
 
 	/** 
 	 * Handles main method.
 	 * Creates a file called main.cpp that contains:
 	 * main(int argc, char *argv[])
 	 * 
 	 *updates the declaration with proper parameters
 	 *
 	 * @param GNode A method declaration GNode
 	 */ 
 	private void buildMain(Declaration n) {
 		FileMaker mainWriter = new FileMaker(jfile, "main.cpp");
 		//change parameters for c++
 		
 		n.returntype = "int32_t";
 		String arrayName = n.params.get(0).var_name;
 		n.params.set(0,new Fparam("int32_t","argc"));
 		n.params.add(new Fparam("char**",arrayName));
 		
 		//create the main.cpp
 		mainWriter.write("#include <iostream>\n\n"+
 						 "#include \""+h.cFile.getName()+"\"\n\n");
                                                  ArrayList<String> namespace = fileinfo.getPackageToNamespace();
 						 for (String p : namespace) {
 							 int size = namespace.size();
 							 if (p.equals(namespace.get(0))) mainWriter.write("using namespace "+p);
 							 else mainWriter.write("::"+p);
 							 if (p.equals(namespace.get(size-1))) mainWriter.write(";");
 						 }
 						 mainWriter.write("\n\n\n"
 						 +"int32_t main(int32_t argc, char *argv[]){\n\n\t"
 						 +n.ownerClass+" forward_main = new __"+n.ownerClass+"();\n\t"
 					     +"forward_main->init(forward_main);\n\t"
 						 +"forward_main->main");
 						if(n.overloadNum!=0)
 							mainWriter.write("_"+n.overloadNum);
 						mainWriter.write("(argc,argv);\n\treturn 0;\n}");
 		mainWriter.close();
 	}
 	
 	
 	
 	/**
 	 * will print a string of all methods ptr declarations from superclass's Vtable
 	 * +  method ptr declarations of PUBLIC NON-STATIC methods in this class. 
 	 *
 	 * syntax --->  ",methodreturnType (*methodname)(methodparameters)",\n"
 	 */	
 	private void write_all_method_ptrs(InheritanceTree t){
 		String FQclassName = t.className;//to change from classname to fully qualified easier
 
 		
 		//ptr for __class()
 		h.write("\t\tClass __isa;\n");
 					
 		//ptr for __delete()
 		h.write("\t\tvoid (*__delete)(__"+t.className+"*);\n");
 		
 					
 		//loops through vtable and prints out in proper syntax
 		int size = t.Vt_ptrs.size();
 		for (int i=2;i<size;i++) {
 			Declaration method = t.Vt_ptrs.get(i);
 			h.write("\t\t"+method.returntype+" (*"+method.name);
 			if(method.overloadNum!=0)
 				h.write("_"+method.overloadNum);
 			h.write(")(");
 			int fpsize = method.params.size();
 			for (int j=0;j<fpsize;j++) {
 				String type = method.params.get(j).type;
 				
 				if (j==0){ h.write(FQclassName);}
 				else h.write(", "+type);
 			}
 			h.write(");\n");
 			
 		}
 	}
 	
 	/*
 	 * prints out string of all initalized method ptrs from superclass's Vtable 
 	 * + initialized method ptrs of public and protected methods in this class to the .h file.
 	 *
 	 * syntax --->  methodname"(&__"+ClassName+"::"+methodname+"),\n"
 	 * or syntax ---> methodname+"(("+returntype+"(*)("+classname+","+...other params+"))&__"+ownerclass+"::"+methodname+")"
 	 *
 	 */	
 	private void write_assign_method_ptrs(InheritanceTree t){
 		String FQclassName = t.className;//to change from classname to fully qualified easier
 
 		
 		//ptr for __class()
 		h.write("\t\t\t"+t.Vt_ptrs.get(0).name+"(__"+t.Vt_ptrs.get(0).ownerClass+"::__class()),\n");
 		
 		//ptr for __delete()
 		h.write("\t\t\t__delete(&__rt::__delete<__"+t.className+">)");
 		
 		//loops through vtable and prints out in proper syntax
 		int size = t.Vt_ptrs.size();
 		for (int i=2;i<size;i++) {
 			Declaration method = t.Vt_ptrs.get(i);
                     //syntax for an overridden method
                     if ((method.ownerClass).equals(t.className)) {
 
                         h.write(",\n\t\t\t"+method.name);
                         if(method.overloadNum!=0)
                                 h.write("_"+method.overloadNum);
                         h.write("(&__"+t.className+"::"+method.name);
                         if(method.overloadNum!=0)
                                 h.write("_"+method.overloadNum);
                         h.write(")");
                     }
                     //syntax for a method that needs a this class casting
                     else{
                         h.write(",\n\t\t\t"+method.name);
                         if (method.overloadNum!=0)
                             h.write("_"+method.overloadNum);
                         h.write("(("+method.returntype+"(*)(");
 
 						int fpsize = method.params.size();
                         for (int j=0;j<fpsize;j++) {
 							String type = method.params.get(j).type;
 							if (j==0) h.write(t.className);
                             else h.write(", "+type);
                         }
                         h.write("))&__"+method.ownerClass+"::"+method.name);
                         if (method.overloadNum!=0)
                             h.write("_"+method.overloadNum);
                         h.write(")");
                     }
 			
 		}
 	}
 
 	
 	/**
 	 *writes to the ccp_methoddef all of GNode's methods functionality
 	 *same structure as http://cs.nyu.edu/rgrimm/teaching/fa10-oop/1007/java_lang.cc from class notes
 	 * syntax -->  returntype+" __"+className+"::__"+methodname+"("+className+"__this,"+other params+"){"
 	 *    CALLS TO CPPMETHOD to build the body
 	 *	 then ends method "}"
 	 */
 	public void addMethodDef(InheritanceTree t){
 		String FQclassName = t.className;//to change from classname to fully qualified easier
 		//writes the __class() method
 		cpp.write("\t"+t.local.get(0).returntype+" __"+t.className+
 							"::"+t.local.get(0).name+"(){"+
 							"\n\t\tstatic Class k = new __Class(__rt::stringify(\""+fileinfo.getPackageName()+"."+t.className+"\"),__rt::null());"+
 							"\n\t\treturn k;\n\t"+
 							"}\n");
 		for(Declaration constr: t.constructors){
 			// create init() method
 			cpp.write("\t\tvoid __"+t.className+"::init");
 			cpp.write("("+t.className+" __passedthis");
 		
 			// create init's formal parameters
 			for (int i=0;i<constr.params.size();i++) {
 				cpp.write(","); //comma
 				Fparam fp = constr.params.get(i);
 			
 				//loop through formal parameter's modifiers
 				for (String modifier : fp.modifiers) {
 					cpp.write(modifier+" ");
 				}
 				//writes formal parameter's type and name
 				cpp.write(fp.type+" "+fp.var_name);
 			}
 			
 			cpp.write(") {\n"+
 					  "\t\t\t__passedthis->__this = __passedthis;\n");
 		
 			//**  EWalk is called on constructor's block node  **//
 			EWalk change = new EWalk(t,constr,constr.bnode);
 			//System.out.println("BEFORE PRINTER IS CALLED: " + constr.bnode);
 			CppPrinter printinit = new CppPrinter(constr.bnode);
 			cpp.write(printinit.getString().toString().replaceAll("\n","\n\t\t"));//write body of the constructor in init() method
 			cpp.write("\t\t}\n\n");
 		}
 	//--- adds all methods to METHODDEF	
 		int size = t.local.size();
 		for (int j =1;j<size;j++) {
 			Declaration method= t.local.get(j);
 			if(VERBOSE){System.out.print("translating method "+t.getCppPkg()+t.className+"::"+method.name+"(");
 				for(Fparam p: method.params) System.out.print(p.type+" ");
 				System.out.println(")");
 			}
                     //method syntax
                     cpp.write("\t"+method.returntype+" __"+t.className+
                                                             "::"+method.name);
                     if(method.overloadNum!=0)
                             cpp.write("_"+method.overloadNum);
                     cpp.write("(");
 					
                     for(int i=0;i<method.params.size();i++){
                         Fparam param = method.params.get(i);
 
                         //first param without ","
                         if(i==0)cpp.write(param.type+" "+param.var_name);
                         else cpp.write(","+param.type+" "+param.var_name);
                     }
                     //calls to CppMethod to create the body of the method
                     cpp.write("){\n");
 
                     //**  cppBlock is called on method's block node  **//
                     //cppMethod mblock = new cppMethod(t.local.get(index).mnode);
                     EWalk changes = new EWalk(t,method,method.bnode);
                     CppPrinter mblock=new CppPrinter(method.bnode);
                     cpp.write(mblock.getString().toString());//write body of the method
                     cpp.write("\n\t}\n\n");
 
 		}
 		// invokes Vtable constructor
 		cpp.write("\t__"+t.className+"_VT __"+t.className+"::__vtable;\n\n");
 		
 		//close current package
 		for(String p: fileinfo.getPackageToNamespace()){
 			cpp.write("}");
 		}
 		cpp.write("//close "+t.getCppPkg()+t.className+"\n");
 		//open up java::lang to write template for Array<__className>
 		cpp.write("\tnamespace java {\n\tnamespace lang {");
 		//writes the template<> ... __Array<classname>::__class() method
 		cpp.write("\n\t\ttemplate<>\n"+
 							"\t\tClass __Array<"+t.getCppPkg()+t.className+">::__class() {\n"+
 							"\t\t\tstatic Class k = new __Class(__rt::stringify(\"[L"+fileinfo.getPackageName()+"."+t.className+"\"),\n"+
 							"\t\t\t\t\t\t\t\t\t__Array<"+t.superclass.getCppPkg()+t.superclass.className+">::__class(),\n"+
 							"\t\t\t\t\t\t\t\t\t"+t.getCppPkg()+"__"+t.className+"::__class());\n"+
 							"\t\t\treturn k;\n"+
 							"\t\t}\n\n");
 							
 		//close java::lang
 		cpp.write("\t}\n\t} // closing java::lang for arrays\n"+
 				  "\t//===========================================================================\n\n");
 		
 		//reopen current package
 		for(String p: fileinfo.getPackageToNamespace()){
 			cpp.write("namespace "+p+" {\n");
 		}
 							
 	}
 	/*
 	 *closes both files
 	 *
 	 */
 	public void close(){
 		for(String p: fileinfo.getPackageToNamespace()){
 			h.write("}\n");
 			cpp.write("}\n");
 		}
 		cpp.close();
 		h.close();
 	}
 	//--------------end of methods -------------------------
 	
 	
 }// end of CppFileBuilder
 
 
