 package server.database;
 
import server.JServer;

 public class DataServerServerlist extends DataServer
 {
 	public void UpdateServer()
 	{
 		
		
		boolean connection = sendData("http://" + JServer.database + "/?type=SERVER_UPDATE&name=" + JServer.serverName + "&data=" + JServer.serverIP);
 		if(connection == false)
 		{
 			//database is down
 		}
 	}
 }
