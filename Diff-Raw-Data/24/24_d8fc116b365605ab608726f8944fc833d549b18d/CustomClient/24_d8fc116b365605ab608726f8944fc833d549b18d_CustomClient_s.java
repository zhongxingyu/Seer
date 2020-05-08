 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 
 public class CustomClient {
 
 	public static void main(String[] pArgs) 
 	{		
 
 		int nBoard = 100;
 		int nSolved = 0;
 		int nNotSolved = 0;
 		for(int board=1; board<=nBoard; board++) {
 			if(pArgs.length<2)
 			{
 				System.out.println("usage: java Client host port");
 				return;
 			}
 
 			try
 			{
 				Socket lSocket=new Socket(pArgs[0],Integer.parseInt(pArgs[1]));
 				PrintWriter lOut=new PrintWriter(lSocket.getOutputStream());
 				BufferedReader lIn=new BufferedReader(new InputStreamReader(lSocket.getInputStream()));
 
 				//			lOut.println(pArgs[2]);
 				lOut.println(board);
 				lOut.flush();
 
 				String lLine=lIn.readLine();
 
 				//read number of rows
 				int lNumRows=Integer.parseInt(lLine);
 
 				ArrayList<String> lines = new ArrayList<String>();
 				System.out.println("From server:");
 				for(int i=0;i<lNumRows;i++)
 				{
 					lLine=lIn.readLine();
 					lines.add(lLine);
 					System.out.println(lLine);
 				}
 
				String sol = Solver.solveCombo(lines, new Deadline(1000));
 
 				System.out.println(sol);
 
 				//send the solution to the server
 				lOut.println(sol);
 				lOut.flush();
 
 				//read answer from the server
 				lLine=lIn.readLine();
 				System.out.println(lLine);
 
 				if(lLine.compareTo("CORRECT SOLUTION") == 0) {
 					nSolved++;
 				}
 				else {
 					nNotSolved++;
 				}
 				
 				System.out.println("Solved: "+nSolved+"/"+(nNotSolved+nSolved));
 
 				lSocket.close();
 			}
 			catch(Throwable t)
 			{
 				t.printStackTrace();
 			}
 		}
 		
 	}
 }
