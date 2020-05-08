 import java.io.*;
 
 public class GraphvizMake {
 	public static  Graphvizu(String args[])
   {
   try{
   // Create file 
   FileWriter fstream = new FileWriter("graph.dot");
   BufferedWriter out = new BufferedWriter(fstream);
   out.write("graph G {\n");
   }catch (Exception e){//Catch exception if any
   System.err.println("Error: " + e.getMessage());
   }
   }
   public void addEdge(String x, String y) {
	out.write(x&"--"&y&";");
 	out.write("\n");
   }
   public void end(){
 	out.write("}");
 	out.close();
   }
 }
