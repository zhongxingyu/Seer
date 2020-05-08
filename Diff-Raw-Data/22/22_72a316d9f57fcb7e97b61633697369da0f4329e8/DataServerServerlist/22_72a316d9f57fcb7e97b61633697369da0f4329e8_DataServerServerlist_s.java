 package server.database;
 
 public class DataServerServerlist extends DataServer
 {
 	public void UpdateServer()
 	{
		boolean connection = sendData("http://jchat.ted80.net/?type=SERVER_UPDATE&name=SERVERNAME&data=SERVERIP");
 		
 		if(connection == false)
 		{
 			//database is down
 		}
 	}
 }
