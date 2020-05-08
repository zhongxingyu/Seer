 package Core.RequestResponse;
 
 import Core.ServerRunner;
 
 import java.io.*;
 import java.net.*;
 
 public class RequestResponseHandler implements Runnable
 {
     private ServerSocket server;
     private RequestParser requestParser;
     private ResponseProvider responseProvider;
 
     private RequestResponseHandler(ServerSocket serverSocket, String rootDirectory)
     {
         server = serverSocket;
         requestParser = RequestParser.create();
         responseProvider = ResponseProvider.create(rootDirectory);
     }
 
     public static RequestResponseHandler createFrom(ServerSocket serverSocket, String rootDirectory)
     {
         return new RequestResponseHandler(serverSocket, rootDirectory);
     }
 
     public void run()
     {
         ServerRunner.log("CTRL-C to end it.\r\n");
         boolean continueListening = true;
         while(continueListening)
             manageCommunicationCycle();
     }
 
     public void manageCommunicationCycle()
     {
         try
         {
             Socket clientSocket = server.accept();
             RequestInformation info = readRequest(clientSocket);
             sendResponse(clientSocket, info);
             clientSocket.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     private RequestInformation readRequest(Socket clientSocket)
         throws IOException
     {
         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         StringBuffer buffer = new StringBuffer();
         while (in.ready()) {
             int ch = in.read();
             buffer.append((char) ch);
         }
        return requestParser.parseInfoFromRequest(buffer.toString());
     }
 
     private boolean isEndOfRequestHeader(String currentHeaderLineValue)
     {
         return currentHeaderLineValue.equals("");
     }
 
     private void sendResponse(Socket clientSocket, RequestInformation request)
         throws IOException
     {
         String[] response = responseProvider.getResponseFrom(request);
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
         for(int i = 0; i < response.length; i++)
             out.println(response[i]);
         out.flush();
     }
 }
