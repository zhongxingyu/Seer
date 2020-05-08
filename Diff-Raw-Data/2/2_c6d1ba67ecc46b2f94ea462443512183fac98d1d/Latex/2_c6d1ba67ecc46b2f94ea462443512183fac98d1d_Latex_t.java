 package view;
 
 import java.awt.Insets;
  	
 import org.scilab.forge.jlatexmath.*;
 
 public class Latex {
 	public static boolean isLatex(String string){
 		try{
 			new TeXFormula(string);
 			return true;
 		}
 		catch(Exception e){
 			return false;
 		}
 	}
 	
 	public static TeXIcon getLatex(String latexString){
 		String[] stringSplit = latexString.split("\\$\\$");
 		StringBuilder out = new StringBuilder();
 		for (int i = 0; i < stringSplit.length; i++ ){
 			if (i % 2 == 0){
 				String escaped = escape(stringSplit[i]);
 				out.append(escaped);
 			}
 			else{
 				out.append(stringSplit[i]);
 			}
 		}
 		String string = out.toString();
 		System.out.println(string);
 		TeXIcon icon = new TeXFormula(string)
 					.createTeXIcon(TeXConstants.STYLE_DISPLAY,20);
 		icon.setInsets(new Insets(5,5,5,5));
 		return icon;
 	}
 
 	public static String escape(String string) {
 //        "#"=>"\\#",
 //        "$"=>"\\$",
 //        "%"=>"\\%",
 //        "&"=>"\\&",
 //        "~"=>"\\~{}",
 //        "_"=>"\\_",
 //        "^"=>"\\^{}",
 //        "\\"=>"\\textbackslash",
 //        "{"=>"\\{",
 //        "}"=>"\\}",
		string = string.replaceAll("\\\\(?!n)", "\\\\backslash");
 		string = string.replaceAll("~", "\\\\~");
 		string = string.replaceAll("#", "\\\\#");
 		string = string.replaceAll("\\$", "\\\\\\$");
 		string = string.replaceAll("%", "\\\\%");
 		string = string.replaceAll("_", "\\\\_");
 		string = string.replaceAll("\\{", "\\\\{");
 		string = string.replaceAll("\\}", "\\\\}");
 		string = string.replaceAll("\\^","\\\\^{}");
 		string = string.replaceAll("\\@","\\\\@");
 		string = string.replaceAll("\\&","\\\\&");
 		StringBuilder out = new StringBuilder();
 		String[] lines = string.split("\\n");
 		for (int i = 0; i < lines.length; i++){
 			out.append("\\textrm{");
 			out.append(lines[i]);
 			out.append("} \\\\ ");
 		}
 		//TODO add support for newline. probably will need to split string on \n
 		return out.toString();
 	}
 	
 }
