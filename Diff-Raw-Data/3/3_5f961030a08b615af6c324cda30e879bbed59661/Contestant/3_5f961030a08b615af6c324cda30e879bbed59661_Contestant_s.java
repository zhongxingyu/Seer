 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.io.IOException;
 class Contestant extends NoTippingPlayer 
 {
   public Contestant(int port) 
   {
     super(port);
     System.out.println("Starting on :" + port);
   }
   protected String process(String command) 
   {
     String move=null;
     Runtime runTime = Runtime.getRuntime();
     Process p = null;
     try 
     {
       System.out.println("Running C++ program.");
       p = runTime.exec("./no_tipping_game");
 
       String[] lines = command.split("\n");
       java.io.PrintWriter cppStdin = new java.io.PrintWriter(p.getOutputStream(), true);
       
       for(String s : lines)
         cppStdin.println(s);
 
       cppStdin.println("STATE END");
       
       System.out.println("Waiting for program to terminate.");
       p.waitFor();
     } 
     catch (Exception e) 
     {
       System.out.println("error executing " + command);
     }
     try
     {
       System.out.println("Capturing output");
       InputStream in = p.getInputStream();
       InputStreamReader insr = new InputStreamReader(in);
       BufferedReader br = new BufferedReader(insr);
       String output;
       StringBuffer sb = new StringBuffer();
       while((output=br.readLine())!=null)
       {
         sb.append(output+"\n");
       }
       move = sb.toString();
       System.out.println("Output: " + move);
     }
     catch(IOException ex)
     {
       System.out.println("Error reading the stream: "+ex.getMessage());
     }
     return move;
   }
 
   public static void main(String[] args) throws Exception 
   {
     if(args.length==1)
     {
       int port = Integer.parseInt(args[0]);
       System.out.println("Creating server.");
       new Contestant(port);
     }
     else
     {
       System.out.println("Usage: Contestant.java <port-number>");
     }
   }
 }
