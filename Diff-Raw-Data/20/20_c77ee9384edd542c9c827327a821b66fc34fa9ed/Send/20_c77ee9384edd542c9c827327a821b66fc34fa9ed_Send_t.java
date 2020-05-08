 package client.sender;
 
 
 public class Send 
 {
	//private static Socket socket;
 	
 	public void sendInfoToServer(String info, String ip, int port)
 	{
 		System.out.println(info + " " + ip + ":" + port);
 		
 		/*
         try 
         {
 			socket = new Socket(ip, port);
             OutputStream os = socket.getOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(os);
             BufferedWriter bw = new BufferedWriter(osw);
             bw.write(info);
             bw.flush();
 
             os.close();
             osw.close();
             bw.close();
             socket.close();
         }
         catch (Exception exception) 
         {
             exception.printStackTrace();
         }
         */
 	}
 }
