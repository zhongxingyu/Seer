 package oop;
 
 import java.util.LinkedList;
 
 import xtc.tree.GNode;
 
 public class ccMethod {
 	private String name;
 	private final ccClass parentClass;
 	private final String access;
 	private final String returnType;
 	private final String[] parameterType;
 	private final String[] parameterName;
 	private final boolean isStatic;
 	private ccBlock block;
 	
 	// The first constructor only makes dummy methods, and will not actually be used
 	public ccMethod(String mName, ccClass mClass){
 		name = mName;
 		parentClass = mClass;
 		access = "";
 		returnType = "";
 		parameterType = new String[0];
 		parameterName = new String[0];
 		isStatic = false;
 	}
 	public ccMethod(String mName, ccClass mClass, String mAccess, String mReturnType, String[] mparameterType, String[] mparameterName){
 		name = mName;
 		parentClass = mClass;
 		access = mAccess;
 		returnType = mReturnType;
 		parameterType = mparameterType;
 		parameterName = mparameterName;
 		isStatic = false;
 	}
 	public ccMethod(String mName, ccClass mClass, String mAccess, String mReturnType, String[] mparameterType, String[] mparameterName, boolean mIsStatic){
 		name = mName;
 		parentClass = mClass;
 		access = mAccess;
 		returnType = mReturnType;
 		parameterType = mparameterType;
 		parameterName = mparameterName;
 		isStatic = mIsStatic;
 	}
 
 	public void setBlock(ccBlock blk){
 		block = blk;
 	}
 	
 	public void mangleName(){
 		name = parentClass + "__" + name;
 		for (int i = 0; i < parameterType.length; i++){
 			name += "__" + parameterType[i];
 		}
 	}
 	
 	public String getName(){
 		return name;
 	}
 	
 	public String publishDeclaration(){
 		String decl = "";
		if(isStatic){
			return decl;
 		}
		else{
			decl = ccHelper.convertType(returnType) + " " + parentClass.getName() + "::" + name  + "(";
			for (int i = 0; i < parameterType.length; i++){
				if(i != 0) decl += ", ";
				decl += parameterType[i] + " " + parameterName[i];
			}
			decl += ")";
			return decl;
 		}
 	}
 	
 	public LinkedList<String> publishBlock(){
 //		System.out.println(block.blockLines);
 		return block.publish();
 	}
 	
 	public String toString(){
 		String s = "" + access + " method Name:\"" + name + "\" Return Type:\"" + returnType + "\" Parameters:(";
 		for (int i = 0; i < parameterType.length; i++){
 			if(i != 0) s += ", ";
 			s += parameterType[i] + " " + parameterName[i];
 		}
 		s += ") Static:" + isStatic;
 		return s;
 	}
 }
