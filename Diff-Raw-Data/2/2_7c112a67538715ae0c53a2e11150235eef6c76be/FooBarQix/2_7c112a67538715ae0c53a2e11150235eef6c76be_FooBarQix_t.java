 package main;
 
 public class FooBarQix {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		FooBarQix fooBarQix = new FooBarQix();
 		for(int i =1;i<101;i++){
 			System.out.println(fooBarQix.fooBarQixiseInteger(i));
 		}
 	}
 	
 	
 	public String fooBarQixiseInteger(int i){
 		StringBuffer result = new StringBuffer("");
 		for(EnumFooBarQix value: EnumFooBarQix.values()){
 			result.append(divisible(i, value));
 		}
 		
 		result.append(replaceChars(Integer.toString(i)));
 		
		if(result.length()==0){
 			result.append(i);
 		}
 		return result.toString();
 	}
 	
 	public String divisible(int i, EnumFooBarQix enumValue){
 		String result ="";
 		if(i % enumValue.getValue() == 0){
 			result += enumValue.name();
 		}
 		return result;
 	}
 	
 	public String replaceChars(String integerString){
 		String result ="";
 		if(integerString.length()>0){
 			int integerI = Integer.parseInt(integerString.substring(0,1));
 			for(EnumFooBarQix value: EnumFooBarQix.values()){
 				result += equalsAndReplace(integerI, value);
 			}
 			result += replaceChars(integerString.substring(1));
 		}
 		return result;
 	}
 	
 	public String equalsAndReplace(int i, EnumFooBarQix enumValue){
 		String result ="";
 		if(i==enumValue.getValue()){
 			result += enumValue.name();
 		}
 		return result;
 	}
 
 }
