 package org.i5y.chack;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
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
 
 	/**
 	 * 
 	 * THIS IS COMPLETELY MADE UP DATA item cost per item  number of items
 	 * already funded number of items needed in next month funding gap 
 	 * hostel beds 0.18 1200 4000 720 peanut paste 0.2 10000 23000 4600 tetanus
 	 * vaccine 0.12 23000 50000 6000 measles vaccine 0.15 23000 50000 7500
 	 * school kits 150 150 320 48000 rhino protection unit 3500 5 2 7000
 	 * 
 	 */
 
 	public static AtomicInteger amount = new AtomicInteger(0);
 	public static AtomicInteger itemsUsed = new AtomicInteger(0);
 	public static CopyOnWriteArrayList<Integer> amountOverTime = new CopyOnWriteArrayList<Integer>();
 	public static CopyOnWriteArrayList<Integer> itemsOverTime = new CopyOnWriteArrayList<Integer>();
 	public static CopyOnWriteArrayList<String> recentDonations = new CopyOnWriteArrayList<String>();
 
 	public static class DataSource extends HttpServlet {
 
 		@Override
 		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 			resp.setContentType("application/json");
 			resp.addHeader("Cache-Control", "no-store, max-age=0");
 			resp.getWriter().write("{");
 			resp.getWriter().write("\"amount\":" + amount.get() + ",");
 			resp.getWriter().write("\"items_used\":" + itemsUsed.get()+",");
 			resp.getWriter().write("\"item_price\":" +  "0.18");
 			resp.getWriter().write("}");
 		}
 	}
 
 	public static class DataSource2 extends HttpServlet {
 
 		@Override
 		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 			resp.setContentType("application/json");
 			resp.addHeader("Cache-Control", "no-store, max-age=0");
 			resp.getWriter().write("{");
 			resp.getWriter().write("\"item_price\":" +  "0.18");
 			resp.getWriter().write("\"values\": [");
 			for (int i = 0; i < amountOverTime.size(); i++) {
 				resp.getWriter().write(
 						"{\"amount\": " + amountOverTime.get(i)
 								+ ", \"items_used\":" + itemsOverTime.get(i)
 								+ "}");
 				if (i + 1 < amountOverTime.size())
 					resp.getWriter().write(",");
 			}
 			resp.getWriter().write("]}");
 		}
 	}
 
 	public static class RecentDonations extends HttpServlet {
 
 		@Override
 		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 			resp.setContentType("application/json");
 			resp.addHeader("Cache-Control", "no-store, max-age=0");
 			resp.getWriter().write("{");
 			resp.getWriter().write("\"values\": [");
 			
 			for (int i = 0; i < recentDonations.size(); i++) {
 				resp.getWriter().write(recentDonations.get(i));
 				if (i + 1 < recentDonations.size())
 					resp.getWriter().write(",");
 			}
 			resp.getWriter().write("]}");
 		}
 	}
 	
 	public static class DataUpload extends HttpServlet {
 
 		private final AtomicLong lastTweetAt = new AtomicLong();
 
 		@Override
 		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 			System.out.println("got a post!");
 			int increment = Integer.parseInt(req.getParameter("quantity"));
 			itemsUsed.addAndGet(increment);
 
 			// Need to decide if the current status is worth a warning...
 			// If rate of growth in need is more than rate of growth in
 			// payments...
 
 			double ratio = 1 / 0.18;
 			double growthItems = (itemsOverTime.get(itemsOverTime.size() - 1) * ratio)
 					- (itemsOverTime.get(0) * ratio);
 			double growthAmount = amountOverTime.get(amountOverTime.size() - 1)
 					- amountOverTime.get(0);
 
 			// If it's at least 30 seconds since we tweeted.... and the growth
 			// of need is much faster than the amount
 			// of cash we have...
 			if (lastTweetAt.get() < (System.currentTimeMillis() - (1000 * 30))
 					&& growthItems > growthAmount) {
 
 				String message = "Thanks for your help so far for XX. The funds for XX are now low - so we're reaching out to see if you could make a top up.";
 				if (growthItems > (growthAmount * 3)) {
 					message =   "We wanted to let you know the charity project you're funding needs your help. They now need to top up funding to reach XX more.";
 				} else if (growthItems > (growthAmount * 2)) {
 					message =   "Since we last contacted you we've helped XX people with your help. That's amazing, but there's still more to do and funds are running low.";
 				}
 
 				TwitterFactory factory = new TwitterFactory();
 				Twitter twitter = factory.getInstance();
 				twitter.setOAuthConsumer(consumerKey, consumerSecret);
 				twitter.setOAuthAccessToken(new AccessToken(accessToken,
 						accessTokenSecret));
 				try {
 					Status status = twitter.updateStatus(message);
 				} catch (TwitterException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				lastTweetAt.set(System.currentTimeMillis());
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
 				file += line + "\n";
 				line = br.readLine();
 			}
 			FILE = file;
 		}
 
 		@Override
 		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 			resp.setContentType("text/html");
 			resp.addHeader("Cache-Control", "no-store, max-age=0");
 
 			String amount = req.getParameter("amount");
 			if (amount == null || amount == "") {
 				amount = "10.0";
 			}
 			String replaced = FILE.replaceAll("inputamount", amount);
 			resp.getWriter().write(replaced);
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
 			resp.setHeader("Location",
 					"http://107.21.242.232/charity/?donated=true&value=" + amt);
			recentDonations.add("A generous donations of "+amt+" by Anonymous at "+new Date());
 			resp.setStatus(302);
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
 		context.addServlet(new ServletHolder(new DataSource2()), "/data-ext");
 		context.addServlet(new ServletHolder(new RecentDonations()), "/recent-donations");
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
