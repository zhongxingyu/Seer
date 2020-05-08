 package aic.manager.interfaces;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import aic.manager.model.SentimentTask;
 
 public interface IManager extends Remote {
 	// add new task to compute
 	void addSentimentAnalysisTask(SentimentTask task) throws RemoteException;
 
 	// add new subscriber
	void subscribe(String serviceUrl) throws RemoteException;
 }
