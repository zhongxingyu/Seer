 package org.crowdball;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class CrowdballServlet extends HttpServlet {
 
 	private static 	Ball ball = new Ball();
 
 	public enum Side {
 		A, B, S, P
 	};
 
 	public CrowdballServlet() {
 		Thread foo = new Thread() {
 			public void run() {
 				while (true) {
 					ball.position();
 					System.out.println(ball);
 					try {
 						Thread.sleep(5000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			};
 		};
 		foo.start();
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		Side side = parseUrl(req.getRequestURI());
 		if (side == null) {
 			throw new ServletException("Side not specified");
 		} else {
 			switch (side) {
 			case S:
 				System.out.println("Resetting and starting ball");
 				ball.resetAndStart();
 				break;
 			case A:
 				hitBallFromA(resp.getWriter());
 				break;
 			case B:
 				hitBallFromB(resp.getWriter());
 				break;
 			case P:
 				resp.setHeader("Content-Type", "application/json");
 				returnPosition(resp.getWriter());
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	private void returnPosition(PrintWriter writer) {
 		int position = ball.position;
		writer.print("{position: " + position + "}");
 		System.out.println("Sending position information: " + position);
 	}
 
 	private void hitBallFromB(PrintWriter responseWriter) {
 		ball.hitRecievedFromB();
 		responseWriter.print("Hitting ball from B");
 		System.out.println("Hitting ball from B");
 	}
 
 	private void hitBallFromA(PrintWriter responseWriter) {
 		ball.hitRecievedFromA();
 		responseWriter.print("Hitting ball from A");
 		System.out.println("Hitting ball from A");
 	}
 
 	private Side parseUrl(String incomingURL) {
 		int lastSlashIndex = incomingURL.lastIndexOf('/');
 
 		Side result = null;
 		if (lastSlashIndex < incomingURL.length()) {
 			String sideParam = incomingURL.substring(lastSlashIndex + 1);
 			result = Side.valueOf(sideParam);
 		}
 		return result;
 	}
 }
