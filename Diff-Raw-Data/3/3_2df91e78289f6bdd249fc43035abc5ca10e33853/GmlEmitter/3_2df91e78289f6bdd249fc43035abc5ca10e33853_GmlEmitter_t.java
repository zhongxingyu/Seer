 package nl.helixsoft.graph;
 
 import java.io.BufferedOutputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 /**
  * Emitter that produces properly formatted GML to an OutputStream
  */
 public class GmlEmitter implements Emitter
 {
 	private final PrintStream out;
 
 	public GmlEmitter(OutputStream out)
 	{
 		this.out = new PrintStream (new BufferedOutputStream(out));
 	}
 
 	private String indent = "";
 
 	public void startList(String key)
 	{	
 		out.print(indent);
 		out.print(key);
 		out.println(" [");
 		indent += " ";
 	}
 	
 	public void closeList()
 	{
 		indent = indent.substring(1);
 		out.print(indent);
 		out.println ("] ");
 	}
 	
 	public void stringLiteral (String key, String value)
 	{
 		out.print(indent);
 		out.print (key);
 		out.print (" \"");
		//TODO: replace all ISO-8859-1 characters characters above ASCII code 127 this way
		out.print (value.replaceAll ("&", "&amp;").replaceAll ("\"", "&quot;"));
 		out.println ("\"");			
 	}
 
 	public void intLiteral(String key, int value)
 	{
 		out.print(indent);
 		out.print (key);
 		out.println(" " + value);						
 	}
 	
 	public void doubleLiteral(String key, double value)
 	{
 		out.print(indent);
 		out.print (key);
 		out.println(" " + value);						
 	}
 
 	public void numberLiteral(String key, Number value) 
 	{
 		out.print(indent);
 		out.print (key);
 		out.println(" " + value);								
 	}
 
 	public void close() 
 	{
 		out.close();
 	}
 
 }
