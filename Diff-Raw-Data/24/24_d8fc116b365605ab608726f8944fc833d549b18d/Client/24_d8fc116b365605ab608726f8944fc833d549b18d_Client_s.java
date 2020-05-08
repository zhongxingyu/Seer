 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 
 public class Client {
 
 	public static void main(String[] pArgs) 
 	{
 		if(pArgs.length<3)
 		{
 			System.out.println("usage: java Client host port boardnum");
 			return;
 		}
 	
 		try
 		{
 			Socket lSocket=new Socket(pArgs[0],Integer.parseInt(pArgs[1]));
 			PrintWriter lOut=new PrintWriter(lSocket.getOutputStream());
 			BufferedReader lIn=new BufferedReader(new InputStreamReader(lSocket.getInputStream()));
 	
             lOut.println(pArgs[2]);
             lOut.flush();
 
             String lLine=lIn.readLine();
 
             //read number of rows
             int lNumRows=Integer.parseInt(lLine);
 
             //read each row
             ArrayList<String> lines = new ArrayList<String>();
             for(int i=0;i<lNumRows;i++)
             {
                 lines.add(lIn.readLine());
             }
     
             //we've found our solution
            String lMySol = Solver.solveForward(lines, new Deadline(3600000));
             //these formats are also valid:
             //String lMySol="URRUULDLLULLDRRRRLDDRURUDLLUR";
             //String lMySol="0 3 3 0 0 2 1 2 2 0 2 2 1 3 3 3 3 2 1 1 3 0 3 0 1 2 2 0 3";
 
             //send the solution to the server
             lOut.println(lMySol);
             lOut.flush();
     
             //read answer from the server
             lLine=lIn.readLine();
     
             System.out.println(lLine);
 		}
 		catch(Throwable t)
 		{
 			t.printStackTrace();
 		}
 	}
 }
