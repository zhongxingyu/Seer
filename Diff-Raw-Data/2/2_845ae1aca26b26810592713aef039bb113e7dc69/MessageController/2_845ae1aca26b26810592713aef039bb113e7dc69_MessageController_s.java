 package svm.logic.implementation.controller;
 
 import svm.domain.abstraction.DomainFacade;
 import svm.domain.abstraction.modelInterfaces.IMember;
 import svm.logic.abstraction.controller.IMessageController;
 import svm.logic.abstraction.exception.IllegalGetInstanceException;
 import svm.logic.abstraction.jmsobjects.IMemberMessage;
 import svm.logic.abstraction.jmsobjects.IMessageObserver;
 import svm.logic.abstraction.jmsobjects.ISubTeamMessage;
 import svm.logic.abstraction.transferobjects.IHasModel;
 import svm.logic.abstraction.transferobjects.ITransferAuth;
import svm.logic.abstraction.transferobjects.impl.TransferAuth;
 import svm.logic.jms.SvmJMSPublisher;
 import svm.persistence.abstraction.exceptions.ExistingTransactionException;
 import svm.persistence.abstraction.exceptions.NoSessionFoundException;
 import svm.persistence.abstraction.exceptions.NoTransactionException;
 import svm.persistence.abstraction.exceptions.NotSupportedException;
 
 import javax.jms.*;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Projectteam : Team C
  * Date: 21.11.12
  */
 public class MessageController implements IMessageController, MessageListener {
     private ITransferAuth user;
     private TopicConnectionFactory tcf;
     private TopicConnection tc1;
     private Topic topicMember;
     private Topic topicSubTeam;
     private TopicSession ts1;
     private List<IMessageObserver> observers = new LinkedList<IMessageObserver>();
     private TopicConnection tc2;
     private TopicSession ts2;
 
     public MessageController(ITransferAuth user) {
         this.user = user;
     }
 
     @Override
     public void start() throws NoSessionFoundException, IllegalGetInstanceException, RemoteException, NotSupportedException, InstantiationException, IllegalAccessException {
         //Init Context
         try {
             Context context = null;
 
             context = this.getInitialContext();
 
             //Hole topic factory
             tcf = (TopicConnectionFactory) context.lookup(SvmJMSPublisher.factoryName);
             //Hole Topic
             topicMember = (Topic) context.lookup(SvmJMSPublisher.memberTopic);
             topicSubTeam = (Topic) context.lookup(SvmJMSPublisher.subTeamTopic);
             //Create Topic verbindung
             tc1 = tcf.createTopicConnection();
             tc2 = tcf.createTopicConnection();
 
 
             //Setzen der Client ID
             String id = user.getUsername();
             tc1.setClientID(id + "" + SvmJMSPublisher.subTeamTopic);
             tc1.start();
             tc2.setClientID(id + "" + SvmJMSPublisher.memberTopic);
             tc2.start();
             //Topic Session starten
             ts1 = tc1.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
             ts2 = tc2.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
 
             ts1.createDurableSubscriber(topicSubTeam, id).setMessageListener(this);
             ts2.createDurableSubscriber(topicMember, id).setMessageListener(this);
         } catch (NamingException e) {
             e.printStackTrace();
             throw new RemoteException(e.getMessage(), e);
         } catch (JMSException e) {
             e.printStackTrace();
             throw new RemoteException(e.getMessage(), e);
         }
     }
 
     @Override
     public void commit() throws ExistingTransactionException, NoSessionFoundException, NoTransactionException, RemoteException {
         try {
             ts1.setMessageListener(null);
             ts1.close();
             tc1.close();
         } catch (JMSException e) {
             e.printStackTrace();
             throw new RemoteException(e.getMessage(), e);
         }
     }
 
     @Override
     public void abort() throws ExistingTransactionException, NoSessionFoundException, NoTransactionException, RemoteException {
         commit();
     }
 
     private Context getInitialContext() throws NamingException {
         return new InitialContext(SvmJMSPublisher.getContextTable());
     }
 
     @Override
     public void onMessage(Message message) {
         ObjectMessage msg = (ObjectMessage) message;
         try {
             Serializable x = msg.getObject();
             if (x instanceof IMemberMessage) {
                 receiveMemberMessage((IMemberMessage) x);
             } else if (x instanceof ISubTeamMessage) {
                 receiveSubTeamMessage((ISubTeamMessage) x);
             }
         } catch (JMSException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (NoSessionFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     private void receiveSubTeamMessage(ISubTeamMessage x) throws NoSessionFoundException {
         if (mySubTeamMessage(x)) {
             informObserver(x);
         }
     }
 
     private boolean mySubTeamMessage(ISubTeamMessage message) throws NoSessionFoundException {
         return (((IHasModel<IMember>) message.getMember()).getModel().equals(((TransferAuth) user).getModel()));
     }
 
     private void receiveMemberMessage(IMemberMessage x) throws NoSessionFoundException {
         if (myMemberMessage(x)) {
             informObserver(x);
         }
     }
 
     private boolean myMemberMessage(IMemberMessage message) throws NoSessionFoundException {
         int sessionId = DomainFacade.generateSessionId();
         try {
             IMember messageMember = ((IHasModel<IMember>) message.getMember()).getModel();
             DomainFacade.reattachObjectToSession(sessionId, messageMember.getSport());
             DomainFacade.reattachObjectToSession(sessionId, messageMember.getSport().getDepartment());
             messageMember = messageMember.getSport().getDepartment().getDepartmentHead();
             return (((TransferAuth) user).getModel().equals(messageMember));
         } finally {
             DomainFacade.closeSession(sessionId);
         }
     }
 
     private void informObserver(IMemberMessage message) {
         for (IMessageObserver o : observers) {
             o.updateMemberMessage(message);
         }
     }
 
     private void informObserver(ISubTeamMessage message) {
         for (IMessageObserver o : observers) {
             o.updateSubTeamMessage(message);
         }
     }
 
     @Override
     public void addObserver(IMessageObserver o) throws RemoteException {
         this.observers.add(o);
     }
 
     @Override
     public void removeObserver(IMessageObserver o) throws RemoteException {
         this.observers.remove(o);
     }
 }
