 package net.dovemq.transport.session;
 
 import java.io.IOException;
 import java.util.Collection;
 
 public class SessionCommand implements SessionCommandMBean
 {
     @Override
     public void registerFactory(String factoryName)
     {
         SysTestCommandReceiverFactory commandReceiverFactory = new SysTestCommandReceiverFactory(factoryName);        
         CAMQPSessionManager.registerLinkReceiverFactory(commandReceiverFactory);
         System.out.println("Registered factory: " + factoryName);
     }
 
     @Override
     public void sessionCreate(String targetContainerId)
     {
         CAMQPSessionFactory.createCAMQPSession(targetContainerId);
     }
 
     @Override
     public void sessionCreateMT(String targetContainerId, int numThreads)
     {
         SessionCommand command = new SessionCommand();
         try
         {
             SessionIOTestUtils.createSessions(numThreads, targetContainerId, command);
         }
         catch (InterruptedException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }        
     }
 
     @Override
     public Collection<Integer> getChannelId(String targetContainerId)
     {
         Collection<Integer> sessionList = CAMQPSessionManager.getAllAttachedChannels(targetContainerId);
         return sessionList;
     }
 
     @Override
     public void sessionIO(String targetContainerId, String source, String dest)
     {
         Collection<Integer> sessionList = CAMQPSessionManager.getAllAttachedChannels(targetContainerId);
         for (int channelId : sessionList)
         {
             CAMQPSession session = CAMQPSessionManager.getSession(targetContainerId, channelId);
             if (session == null)
             {
                 System.out.println("Null session for channel: " + channelId);
                 return;
             }
             String destfile = String.format("%s.%d", dest, channelId);
             try
             {
                SessionIOTestUtils.transmitFile(session, source, destfile);
             }
             catch (InterruptedException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             catch (IOException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void sessionClose(String targetContainerId, int sessionAttachedChannelId)
     {
         CAMQPSession session = CAMQPSessionManager.getSession(targetContainerId, sessionAttachedChannelId);
         if (session != null)
             session.close();
         else
             System.out.println("Session with channelId: " +  sessionAttachedChannelId + " to container: " + targetContainerId + " is already closed");
     }
 
     @Override
     public boolean isIODone()
     {       
         SysTestCommandReceiverFactory commandReceiverFactory  = (SysTestCommandReceiverFactory) CAMQPSessionManager.getLinkReceiverFactory();
         return commandReceiverFactory.isDone();
     }
 }
