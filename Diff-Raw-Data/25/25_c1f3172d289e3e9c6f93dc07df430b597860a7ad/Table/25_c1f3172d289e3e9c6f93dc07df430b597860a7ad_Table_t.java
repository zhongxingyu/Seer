 package ecologylab.serialization.library.html;
 
 import java.util.ArrayList;
 
 public class Table extends HtmlElement
 {
 	public Table(){}
 	public String open()
 	{
		String open = "<table";
		if (this.getCssClass() != null && this.getCssClass().length() > 0)
			open += " class=\"" + this.getCssClass() + "\"";
		open += ">";
		
		return open;
 	}
 	
 	public static String close()
 	{
 		return "</table>";
 	}
 }
