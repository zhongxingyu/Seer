 package org.i5y.chack;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 
 public class Driver {
 
 	public static AtomicInteger amount = new AtomicInteger(0);
 	public static AtomicInteger itemsUsed = new AtomicInteger(0);
 	public static CopyOnWriteArrayList<Integer> amountOverTime = new CopyOnWriteArrayList<Integer>();
 	public static CopyOnWriteArrayList<Integer> itemsOverTime = new CopyOnWriteArrayList<Integer>();
 
 	public static class DataSource extends HttpServlet {
 
 		@Override
 		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 			resp.setContentType("application/json");
 			resp.addHeader("Cache-Control", "no-store, max-age=0");
 			resp.getWriter().write("{");
 			resp.getWriter().write("\"amount\":" + amount.get() + ",");
 			resp.getWriter().write("\"items_used\":" + itemsUsed.get());
 			resp.getWriter().write("}");
 		}
 	}
 
 	public static class DataUpload extends HttpServlet {
 
 		@Override
 		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 			System.out.println("got a post!");
 			int increment = Integer.parseInt(req.getParameter("quantity"));
 			if (itemsUsed.addAndGet(increment) > 2) {
 				TwitterFactory factory = new TwitterFactory();
 				Twitter twitter = factory.getInstance();
 				twitter.setOAuthConsumer(consumerKey, consumerSecret);
 				twitter.setOAuthAccessToken(new AccessToken(accessToken,
 						accessTokenSecret));
 				try {
 					Status status = twitter.updateStatus("We hit a limit");
 				} catch (TwitterException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public static class PaypalButton extends HttpServlet {
 
 		final String FILE;
 
 		public PaypalButton() throws Exception {
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					getClass().getClassLoader().getResourceAsStream(
 							"button.html")));
 
 			String line = br.readLine();
 			String file = "";
 			while (line != null) {
 				file += line;
 				line = br.readLine();
 			}
 			FILE = file;
 		}
 
 		@Override
 		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 			resp.setContentType("text/html");
 			resp.addHeader("Cache-Control", "no-store, max-age=0");
 			resp.getWriter().write(FILE);
 		}
 	}
 
 	public static class PaypalCallback extends HttpServlet {
 
 		@Override
 		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 
 			String authToken = "ZjmIRL-WoHTIP6XvX2Ika6RUXAL3vkCjMCVYVPhI7VYaWaaF2P7CvEAB2WC";
 			String tx = req.getParameter("tx");
 			String amt = req.getParameter("amt");
 			amount.addAndGet((int) (Double.parseDouble(amt) * 100));
			resp.getWriter().write("Thank you for your payment. Your transaction has been completed, and a receipt for your purchase has been emailed to you. You may log into your account at www.sandbox.paypal.com/us to view details of this transaction.");
 		}
 	}
 
 	static final String consumerKey = System.getenv("CONSUMER_KEY");
 	static final String consumerSecret = System.getenv("CONSUMER_SECRET");
 	static final String accessToken = System.getenv("ACCESS_TOKEN");
 	static final String accessTokenSecret = System
 			.getenv("ACCESS_TOKEN_SECRET");
 
 	public static void main(String[] args) throws Exception {
 
 		Timer t = new Timer();
 		t.schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				if (amountOverTime.size() >= 10) {
 					amountOverTime.remove(0);
 					itemsOverTime.remove(0);
 				}
 				amountOverTime.add(amount.get());
 				itemsOverTime.add(itemsUsed.get());
 				System.out.println(amountOverTime + " " + itemsOverTime);
 			}
 
 		}, 0, 60 * 1000);
 		Server server = new Server(9777);
 
 		ServletContextHandler context = new ServletContextHandler(
 				ServletContextHandler.SESSIONS);
 		context.setContextPath("/");
 
 		context.addServlet(new ServletHolder(new PaypalButton()), "/button");
 		context.addServlet(new ServletHolder(new DataSource()), "/data");
 		context.addServlet(new ServletHolder(new DataUpload()), "/data-update");
 		context.addServlet(new ServletHolder(new PaypalCallback()),
 				"/paypalCallback");
 
 		ServletHolder defaultServletHolder = new ServletHolder(
 				new DefaultServlet());
 		defaultServletHolder.setInitParameter("resourceBase", "target/classes");
 		context.addServlet(defaultServletHolder, "/");
 
 		HandlerList handlers = new HandlerList();
 		handlers.setHandlers(new Handler[] { context });
 		server.setHandler(handlers);
 
 		server.start();
 		server.join();
 	}
 }
