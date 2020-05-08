 package org.kisst.gft.admin;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.Queue;
 import javax.jms.QueueBrowser;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.kisst.gft.GftContainer;
 import org.kisst.jms.JmsSystem;
 import org.kisst.jms.MultiListener;
 import org.kisst.util.XmlNode;
 
 public class ListenerServlet extends BaseServlet {
 	public ListenerServlet(GftContainer gft) { super(gft);	}
 
 	public void handle(HttpServletRequest request, HttpServletResponse response)
 	throws IOException {
 		if (getUser(request, response)==null)
 			return;
 		response.setContentType("text/html;charset=utf-8");
 		//response.setStatus(HttpServletResponse.SC_OK);
 		PrintWriter out = response.getWriter();
 		String url=request.getRequestURI();
 		String name=url.substring("/listener/".length());
 		String queuenames="input,error,retry";
 		int pos=name.indexOf("/");
 		if (pos>0) {
 			queuenames=name.substring(pos+1);
 			name=name.substring(0,pos);
 		}
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		out.println("<h1>Listener "+name);
 
 		MultiListener lstnr = (MultiListener) gft.listeners.get(name);
 		Session session=null;
 		try {
 			session = ((JmsSystem)gft.queueSystem).getConnection().createSession(true, Session.AUTO_ACKNOWLEDGE);
 			for (String qname:queuenames.split("[,]")) {
 				String q;
 				if ("input".equals(qname))
 					q=lstnr.getQueue();
 				else if ("error".equals(qname))
 					q=lstnr.getErrorQueue();
 				else if ("retry".equals(qname))
 					q=lstnr.getRetryQueue();
 				else
 					throw new RuntimeException("Invalid queuename "+qname);
 
 				out.println("<h2>"+q+"</h2>");
 				out.println("<ul>");
 				Queue destination = session.createQueue(q);
 				QueueBrowser browser = session.createBrowser(destination);
 				Enumeration<?> e = browser.getEnumeration();
 				while (e.hasMoreElements()) {
 					Message msg = (Message) e.nextElement();
 					out.println("<li> "+format.format(new Date(msg.getJMSTimestamp())));
 					try {
 						XmlNode xml=new XmlNode(((TextMessage)msg).getText()).getChild("Body/transferFile");
 						out.println("kanaal: "+xml.getChildText("kanaal")+" bestand: "+xml.getChildText("bestand"));
 					}
 					catch (RuntimeException ex) {
 						out.println("Unknown format, id="+msg.getJMSMessageID());
 					}
 				}
 				out.println("</ul>");
 			}
 		}
 		catch (JMSException e) {throw new RuntimeException(e); }
 		finally {
 			try {
 				if (session!=null)
 					session.close();
 			}
 			catch (JMSException e) {throw new RuntimeException(e); }
 		}
 	}
 
 }
