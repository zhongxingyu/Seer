 package ch.demo.web;
 
 import java.io.IOException;
 
 import javax.annotation.Resource;
 import javax.inject.Inject;
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.JMSException;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 
 @WebServlet("/registration")
 public class RegistrationServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * The logger for the class. The logger is produced by the LoggerProducer
 	 * and then injected here
 	 */
 	@Inject
 	private transient Logger logger;
 	
 	@Resource(mappedName = "MyConnectionFactory")
 	private ConnectionFactory connectionFactory;
 
 	@Resource(mappedName = "MyQueue")
 	private Queue queue;
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		try {
 			Connection connection;
 			connection = connectionFactory.createConnection();
 
 			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 			MessageProducer messageProducer = session.createProducer(queue);
 
 			TextMessage message = session.createTextMessage();
 
 			for (int i = 0; i < 100; i++) {
				String msg = new StringBuilder("This is message ").append(i++).toString();
				message.setText(msg);
				logger.info("Sending message: {0}", message.getText());
 				messageProducer.send(message);
 			}
 		} catch (JMSException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
 		this.doGet(arg0, arg1);
 	}
 }
