 import java.awt.Point;
 import java.io.*;
 import java.net.*;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 
 import data.GameBoard;
 
 import search.TreeSearcher;
 
 
 public class Client {
 	private static GameBoard gameBoard;
 	public Client(){
 		gameBoard = null;
 	}
 	//method that reads map from local file
 	public void readFromFile(String fileName){
 		BufferedReader br = null;
 		try {
 			String lLine;
 			int lNumRows=0;
 			//
 			br = new BufferedReader(new FileReader("Maps/"+fileName));
 			String[] lBoard = null;
 			while(lNumRows==0&&(lLine = br.readLine()) != null){
 				lNumRows=Integer.parseInt(lLine);
 				lBoard = new String[lNumRows];
 			}
 
 			//read each row
 
 			for(int i=0;i<lNumRows;i++)
 			{
 				lLine=br.readLine();
 				lBoard[i] = lLine;
 			}
 			gameBoard = new GameBoard(lBoard);
 			TreeSearcher treeSearcher = new TreeSearcher(gameBoard);
 
 			treeSearcher.totalSearch();
 
 			//System.out.println(lLine);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] pArgs) 
 	{
 		Client agent= new Client();
		agent.readFromFile("test_board_1");
 		if(pArgs.length<3)
 		{
 			System.out.println("usage: java Client host port boardnum");
 			return;
 		}
 		try{
 			Socket lSocket=new Socket(pArgs[0],Integer.parseInt(pArgs[1]));
 			PrintWriter lOut=new PrintWriter(lSocket.getOutputStream());
 			BufferedReader lIn=new BufferedReader(new InputStreamReader(lSocket.getInputStream()));
 
 			lOut.println(pArgs[2]);
 			lOut.flush();
 
 			String lLine=lIn.readLine();
 
 			//read number of rows
 			int lNumRows=Integer.parseInt(lLine);
 			lLine=lIn.readLine();
 			int lNumCols=lLine.length();
 			String[] lBoard = null;
 
 			//read each row
 			//agent.readFromFile("test_board_1");
 			for(int i=0;i<lNumRows;i++)
 			{
 				lLine=lIn.readLine();
 				lBoard[i] = lLine;
 			}
 			gameBoard = new GameBoard(lBoard);
 			TreeSearcher treeSearcher = new TreeSearcher(gameBoard);
 
 			treeSearcher.totalSearch();
 			//here, we would store the row somewhere, to build our board
 			//in this demo, we just print it
 			System.out.println(lLine);
 			//System.out.println(agent.BFS());
 			//String path=agent.gameBoard.BFS();
 			//System.out.println(path);
 			//if(path==null)
 			//	path="";
 			//now, we should find a path from the player to any goal
 
 			//we've found our solution
 			//String lMySol="U R R U U L D L L U L L D R R R R L D D R U R U D L L U R";
 			//these formats are also valid:
 			//String lMySol="URRUULDLLULLDRRRRLDDRURUDLLUR";
 			//String lMySol="0 3 3 0 0 2 1 2 2 0 2 2 1 3 3 3 3 2 1 1 3 0 3 0 1 2 2 0 3";
 
 			//send the solution to the server
 			//lOut.println(path);
 			//lOut.flush();
 
 			//read answer from the server
 			//lLine=lIn.readLine();
 
 			//System.out.println(lLine);
 		}
 		catch(Throwable t)
 		{
 			t.printStackTrace();
 		}
 	}
 }
