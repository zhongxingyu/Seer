 package aic.manager.interfaces;
 
 import java.rmi.RemoteException;
 
 import aic.manager.model.SentimentTask;
 
 public interface ITaskProcessor extends Runnable {
 	void addSentimentTask(SentimentTask task) throws RemoteException;
 
 	void changeSubscriber(String serviceUrl);
 }
