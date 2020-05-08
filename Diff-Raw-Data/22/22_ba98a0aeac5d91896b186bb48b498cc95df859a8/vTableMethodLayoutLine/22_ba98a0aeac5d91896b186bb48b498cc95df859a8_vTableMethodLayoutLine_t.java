 package oop;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileWriter;
 
 public class vTableMethodLayoutLine {
 	String modifier;
 	String returntype;
 	String methodname;
 	String parameters;
 	String referencetype;
 	int parametercount;
 	String vTableLine;
 	String visibility;
 	boolean staticcheck;
 	boolean finalcheck;
 	boolean overloaded;
 	String namemanglename;
 	
 	vTableClass parent;
 	vTableLayoutLine matchinglayout;
 	vTableAddressLine matchingaddress;
 	
 	public vTableMethodLayoutLine(vTableClass parentable){
 		parent = parentable;
 		parametercount = 0;
 		overloaded = false;
 		modifier = "";
 		returntype = "";
 		methodname = "";
 		parameters = "";
 		referencetype = "";
 		visibility = "protected";
 		staticcheck = false;
 		finalcheck = false;
 	}
 	
 	public void setModifer(String modifiable){
 //		if(modifiable.equals("final")){
 //			modifier = "const";
 //		}
 		if(modifiable.equals("public") || modifiable.equals("private") || modifiable.equals("protected")){
 			visibility = modifiable;
 		}
 		else if(modifiable.equals("final")){
 			finalcheck = true;
 		}
 		else{  //most likely static
 			if(modifiable.equals("static")){
 				staticcheck = true;
 			}
 			else{
 				System.out.println("invalid modifier given");
 			}
 		}
 		//modifier = modifiable;
 	}
 	
 	public void setReturnType(String returntypable){
 		returntype = returntypable;
 	}
 	
 	public void setReferenceType(String reference){
 		referencetype = reference;
 	}
 	
 	public void setMethodName(String namable){
 		methodname = namable;
 	}
 	
 	public void setParameters(String parameter){
 		if(parametercount == 0){
 			parameters = "," + parameter;
 			parametercount = 1;
 		}
 		else{
 			parameters = parameters + "," + parameter;
 			parametercount = parametercount + 1;
 		}
 		
 	}
 	
 	public void setOverload(){
 		overloaded = true;
 		matchinglayout.setOverload();
 		matchingaddress.setOverload();
 	}
 	
 	public void setMatching(vTableLayoutLine layout, vTableAddressLine address){
 		matchinglayout = layout;
 		matchingaddress = address;
 	}
 	
 	public void setVisiblity(String visible){
 		if(visible.equals("private") || visible.equals("public") || visible.equals("protected"))
 			visibility = visible;
 	}
 	
 	//obsolete method
 	/*
 	public void createVTableLine(){
 		vTableLine = modifier + " " + returntype + " " + methodname + "(" + referencetype;
 		if (parametercount > 0){
 			vTableLine = vTableLine + parameters;
 		}
 		vTableLine = vTableLine + "); \r";
 	}
 	*/
 	public void writeFile(BufferedWriter writer){
 		if(overloaded == true){
			vTableLine = "static " + returntype + " " + methodname +  //removed modifier inclusion for now
 			parameters.replace(",", "_") + "(" + referencetype;
 		}
 		else{
 			vTableLine = "static " + returntype + " " + methodname + "(" + referencetype;
 		}
 		
 		if (parametercount > 0){
 			vTableLine = vTableLine + parameters;
 		}
 		vTableLine = vTableLine + "); \r";
 		
 		try {
 			//File file = new File("/home/user/xtc/src/xtc/JavaGrinder/TestCase1.crazy");
 			//file.createNewFile();
 			//FileWriter writee = new FileWriter(file);
 			writer.write(vTableLine);
 			writer.flush();
 			//writer2.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void printLine(){
 		if(overloaded == true){
 			vTableLine = "static " + modifier + " " + returntype + " " + methodname + "_" + 
 			parameters.replace(",", "_") + "(" + referencetype;
 		}
 		else{
 			vTableLine = "static " + modifier + " " + returntype + " " + methodname + "(" + referencetype;
 		}
 		
 		if (parametercount > 0){
 			vTableLine = vTableLine + parameters;
 		}
 		vTableLine = vTableLine + "); \r";
 		
 		System.out.print(vTableLine);
 	}
 }
