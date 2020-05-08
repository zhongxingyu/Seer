 package com.novel.odisp;
 
 
 /**       .
  * @author (C) 2004 <a href="mailto:valeks@novel-il.ru">Valentin A. Alekseev</a>
 * @version $Id: Sender.java,v 1.4 2004/05/14 13:35:46 valeks Exp $
  */
 public class Sender extends Thread {
 	/**  ,    . */
 	private int messageCounter = 0;
 	/**  . */
 	private static int counter = 0;
 	/**   . */
 	private boolean doExit = false;
 	/**    . */
 	private StandartObjectManager oman = null;
 	/**  . 
 	 * @param oman     
 	 */
 	public Sender(StandartObjectManager noman) {
 		super("sender" + (counter++));
 		setDaemon(true);
 		oman = noman;
 		start();
 	}
 
 	/**   ,   . */
 	public void run() {
 		while (!doExit) {
 			try {
 				synchronized (this) {
 					wait(100);
 				}
 			} catch (InterruptedException e) {
 				doExit = true;
 			}
 			SendRecord toSend = null; 
 			synchronized (oman) {
 				toSend = oman.getNextPendingMessage();
 			}
 			if (toSend != null) {
 				toSend.getObject().addMessage(toSend.getMessage());
 				messageCounter++;
				System.err.println("Sender [" + getName() + "] message processed: " + toSend);
 			}
 		}
 	}
 
 	/**    . */
 	public void quit() {
 		doExit = true;
 		synchronized (this) {
 			notify();
 		}
 	}
 
 	/**      . */
 	public int getCounter() {
 		return messageCounter;
 	}
 } // Sender
