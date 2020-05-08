 package alma.logoot.network;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class GetData
  */
 public class GetData extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public GetData() {
 		super();
 	}
 
 	private void proceed(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		System.out
 				.println("Begin for session: "
						+ request.getSession(true).getId());
 		response.setHeader("pragma", "no-cache,no-store");
 		response.setHeader("cache-control",
 				"no-cache,no-store,max-age=0,max-stale=0");
 		response.setHeader("Accept-Charset", "utf-8");
 		response.setContentType("text/event-stream;charset=utf-8;");
 		response.setCharacterEncoding("UTF-8");
 		PrintWriter out = response.getWriter();
 		// int messagesSent = 0;
 		/*
 		 * ouverture socket
 		 */
 		String clientSentence;
 		System.out.println("GetData : Creation de la socket sur le port 9990");
 		ServerSocket welcomeSocket = new ServerSocket(NetworkServiceImpl.PORTCLIENT);
 		while (true) {
 			System.out.println("GetData : Acceptation de la socket");
 			Socket connectionSocket = welcomeSocket.accept();
 			BufferedReader inFromClient = new BufferedReader(
 					new InputStreamReader(connectionSocket.getInputStream()));
 //			clientSentence = inFromClient.readLine();
 			char[] buffer=new char[4096];
 			inFromClient.read(buffer);
 			clientSentence=(new String(buffer).trim().replaceAll("\n", "<br>"));
 			out.print("data: " + clientSentence + "\n\n");
 			out.flush();
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		System.out.println("here");
 		proceed(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		System.out.println("here");
 		proceed(request, response);
 	}
 
 }
