 package ch13.ex04;
 
 import java.util.ArrayList;
 
 public class TypeReader {
 	public static void convert(String[][] input) throws ClassNotFoundException {
 		ArrayList list = new ArrayList();
 		Class<?> clazz = null;
 		for (int i = 0; i < input.length; i++) {
 			String str = input[i][0];
			clazz = Class.forName(str); //^̃`FbN
 			if (str.equals("java.lang.Boolean")) {
 				list.add(Boolean.parseBoolean(input[i][1]));
 			} else if (str.equals("java.lang.Integer")) {
 				list.add(Integer.parseInt(input[i][1]));
 			} else if (str.equals("java.lang.Long")) {
 				list.add(Long.parseLong(input[i][1]));
 			} else if (str.equals("java.lang.Float")) {
 				list.add(Float.parseFloat(input[i][1]));
 			} else if (str.equals("java.lang.Double")) {
 				list.add(Double.parseDouble(input[i][1]));
 			} else if (str.equals("java.lang.Character")) {
 				list.add(input[i][1].charAt(0));
 			}
 		}
 		
 		for (int i = 0; i < list.size(); i++) {
 			System.out.println(list.get(i));
 		}
 	}
 	
 	public static void main(String[] args) throws ClassNotFoundException {
 		String[][] input = {{"java.lang.Boolean", "0"}, {"java.lang.Integer", "180"}, {"java.lang.Long", "90"}, {"java.lang.Float", "100"}, {"java.lang.Double", "60"}, {"java.lang.Character", "a"}};
 		TypeReader.convert(input);
 	}
 }
