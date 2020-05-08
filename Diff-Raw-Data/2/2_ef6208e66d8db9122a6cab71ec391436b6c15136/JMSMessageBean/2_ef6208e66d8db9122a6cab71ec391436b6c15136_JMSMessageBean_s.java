 package ru.lukdiman.ejb.core.jmsmessage;
 
 import javax.ejb.EJBException;
 import javax.ejb.SessionBean;
 import javax.ejb.SessionContext;
 import javax.jms.*;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 public class JMSMessageBean implements SessionBean {
 	private SessionContext sessionContext;
 	private QueueConnectionFactory connectionFactory;
     private Queue destination;
 
 	public boolean processMessage(String message) {
         QueueConnection jmsConnection = null;
         QueueSession jmsSession = null;
 
         try {
             jmsConnection = connectionFactory.createQueueConnection();
             jmsSession = jmsConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
 
             TextMessage jmsMessage = jmsSession.createTextMessage();
             jmsMessage.setText(message);
 
             jmsSession.createSender(destination).send(jmsMessage);
         } catch (JMSException e) {
             return false;
         } finally {
             try {
                 if (jmsSession != null) {
                     jmsSession.close();
                 }
 
                 if (jmsConnection != null) {
                     jmsConnection.close();
                 }
             } catch (JMSException e) {
                 e.printStackTrace();
             }
         }
 
         return true;
 	}
 
 	public void ejbCreate() throws EJBException {
         try {
             Context ctx = new InitialContext();
            connectionFactory = (QueueConnectionFactory) ctx.lookup("java:comp/env/LQTest");
             destination = (Queue) ctx.lookup("java:comp/env/jms/HOME.TO.ES");
         } catch (NamingException e) {
             throw new EJBException(e);
         }
 	}
 
 	@Override
 	public void ejbActivate() throws EJBException {
 
 	}
 
 	@Override
 	public void ejbPassivate() throws EJBException {
 
 	}
 
 	@Override
 	public void ejbRemove() throws EJBException {
 
 	}
 
 	@Override
 	public void setSessionContext(SessionContext sessionContext) throws EJBException {
 		this.sessionContext = sessionContext;
 	}
 }
