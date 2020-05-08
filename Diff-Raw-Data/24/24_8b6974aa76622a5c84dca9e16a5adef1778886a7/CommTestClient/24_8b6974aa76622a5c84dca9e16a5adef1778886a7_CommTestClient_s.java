 package test;
 
 import helpclasses.CommEnum;
 import helpclasses.CommMessage;
 import helpclasses.Owner;
 import helpclasses.Sheep;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import client.*;
 
 public class CommTestClient {
 	
 	public static void main(String asdasd[])
 	{
 		
 		
 		SocketClient sockCli = new SocketClient("localhost", 6667);
 		
		sockCli.run();
 		
 		
 		
 		ArrayList<String> loginInfo = new ArrayList<String>();
 		loginInfo.add("Ferrari3049");
 		loginInfo.add("Sheep");
 		
 		
 		sockCli.sendMessage(new CommMessage<String>(CommEnum.LOGIN, loginInfo));
 		
 		
 		
 		
 		ArrayList<Owner> ownerList = new ArrayList<Owner>();
 		ownerList.add(new Owner(149, "Ferrari3049", "Sheep", "Kari Musum", "Krokstad", 30499581, "karimusumkorkstad@ntnu.no", 79686355, "martenordfjord@ntnu.no"));
 		sockCli.sendMessage(new CommMessage<Owner>(CommEnum.GETMESSAGES, ownerList));
 		
 		
 		ArrayList<Sheep> sheepList = new ArrayList<Sheep>();
 		sheepList.add(new Sheep(0, "Gunnar", 1950, 1, 1));
 		
 		sockCli.sendMessage(new CommMessage<Sheep>(CommEnum.ADDSHEEP, sheepList));
 		while(true)
 		{
 			continue;
 		}
 		//sockCli.close();
 
 		
 	}
 
 
 }
