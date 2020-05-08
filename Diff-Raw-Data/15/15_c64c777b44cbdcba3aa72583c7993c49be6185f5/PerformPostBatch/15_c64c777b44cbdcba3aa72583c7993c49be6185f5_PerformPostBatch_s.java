     import java.io.*;
     import java.net.*;
 
     /** a simple class that connects to kindle touch's http://localhost:9101/change,
         then use post method to put a series of commands in file ./commands to update collections
      */
     public class PerformPostBatch {
 	    public static void main(String[] argv) throws Exception {
 		    URL url = new URL("http://localhost:9101/change");
 
 		    String line,line_in;
 		    BufferedReader f 
 			    = new BufferedReader(new FileReader("./commands"));
 
 		    BufferedReader in;
                      
 		    while ((line = f.readLine()) != null) {
			    System.out.println(line);
 			    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 			    connection.setRequestMethod("POST");
 			    connection.setDoOutput(true);
 			    PrintWriter out = new PrintWriter(connection.getOutputStream());
 
 			    out.println(line);
 		    	    out.close();
 
 			    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
 			    while ((line_in = in.readLine()) != null) {
				    System.out.println(line_in);
 			    }
 			    in.close();
 		    }
 
 	    }
     }
