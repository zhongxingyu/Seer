 package de.quiz.Servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.AsyncContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletResponse;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.json.JSONObject;
 
 import de.fhwgt.quiz.application.Quiz;
 import de.fhwgt.quiz.error.QuizError;
 import de.quiz.LoggingManager.ILoggingManager;
 import de.quiz.ServiceManager.ServiceManager;
 import de.quiz.User.IUser;
 import de.quiz.UserManager.IUserManager;
 
 /**
  * Servlet implementation class PlayerServlet
  */
 @WebServlet(description = "handles everything which has to do with players", urlPatterns = { "/SSEServlet" })
 public class SSEServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private AsyncContext asyncContext;
	private static ArrayList<AsyncContext> asyncArr = new ArrayList<AsyncContext>();
 	//synchronisierte ArrayList benutzen concurrentpaket
 	private int index;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public SSEServlet() {
 		super();
 		asyncContext=null;
 		//asyncArr=new AsyncContext[10];
 		index=0;
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		// cache abstellen
 		response.setHeader("pragma", "no-cache,no-store");  
         response.setHeader("cache-control", "no-cache,no-store,max-age=0,max-stale=0");  
         
         // Protokoll auf Server Sent Events einstellen
 		response.setContentType("text/event-stream");
 		response.setCharacterEncoding("UTF-8");
 		
 		System.out.println("AsyncSupported: "+request.isAsyncSupported());
 		
 		AsyncContext sse= request.startAsync(request,response);	
 		
 		System.out.println("Timeout: "+sse.getTimeout());
 		
 		sse.setTimeout(0);     // kein Timeout!!
 		asyncArr.add(sse);
 		
 		// so geht es zwar. Ist aber nicht im Standard vorgesehen
 		// clientliste.add(response);
 		
 		//AsyncContext channel;
 		
 		//final Asyn
 		final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
 		//executorService.scheduleWithFixedDelay(new ServerSendEvent(asyncContext.getResponse()), 0, 2,TimeUnit.SECONDS)
 		for (final AsyncContext ctx : asyncArr) {
 			executorService.execute(new Runnable() {
 				public void run() {
 					try {
 
 							if(ctx!= null){
 								ctx.getResponse().setContentType("text/event-stream");
 								ctx.getResponse().setCharacterEncoding("UTF-8");
 								PrintWriter out = ctx.getResponse().getWriter();
 								 JSONObject json = ServiceManager.getInstance().getService(IUserManager.class).getPlayerList();
 								
 								 //IUser currentUser = ServiceManager.getInstance().getService(IUserManager.class).getUserById(String.valueOf(i));
 								 //out.print(json);
 								 int i = 0;
 								 boolean data;
 								 out.write("event: simpleEvent\n");
 								 out.write("data: {\n");
 								 out.write("data: \"id\": 6");
 								 for(i=0;i<6;i++){
 									 if(json.has("name"+i)){
 										 out.write(",\n");
 										 out.write("data: \"name"+i+"\": \""+json.get("name"+i)+"\"");
 									 }
 								 }
 								 out.write("\n");
 								 out.write("data: }\n\n");
 								 out.flush();
 								 System.out.println("SSE zu Client ");
 							}else {
 								System.out.println("failed");
 							}
 							//asyncContext[j].complete();
 						//executorService.shutdown();
 					}catch (Exception e){
 						e.printStackTrace();
 						System.out.println("geht nicht");
 					}
 				}
 
 			});
 		}
 		executorService.shutdown();
//		executorService.execute(new Runnable() {
//			@Override
//			public void run() {
//
//			}
//		});
	}
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
 	}
 
 }
