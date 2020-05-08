 package com.akjava.lib.common.param;
 
 public class ParameterUtils {
 
 	public static Parameter parse(String line){
 	return parse(line,':');	
 	}
 	public static Parameter parse(String line,char separator){
 		int start=line.indexOf("(");
 		if(start==-1){
 			return new Parameter(line);
 		}else{
 			String name=line.substring(0,start);
 			int end=line.lastIndexOf(")");
 			String inside;
 			if(end==-1){
 				inside=line.substring(start+1);
 			}else{
 				inside=line.substring(start+1,end);
 			}
 			Parameter p=new Parameter(name);
 			if(inside.isEmpty()){
 				return p;
 			}
			String[] atts=inside.split(""+separator);
 			
 			
 			for(String at:atts){
 				p.add(at);
 			}
 			return p;
 		}
 	}
 }
