 package com.mymed.tests.stress;
 
 import java.util.LinkedList;
 
 import com.mymed.model.data.session.MSessionBean;
 
 public class SessionThread extends Thread implements NumberOfElements {
 	private final LinkedList<MSessionBean> sessionList = new LinkedList<MSessionBean>();
 	private final SessionTest sessionTest;
 	private final Thread addSession;
 	private final Thread removeSession;
 	private final boolean remove;
 
 	/**
 	 * Create the new session thread
 	 */
 	public SessionThread() {
 		this(true, NUMBER_OF_ELEMENTS);
 	}
 
 	/**
 	 * Create the new session thread, but do not perform the remove thread, only
 	 * add new session to the database
 	 * 
 	 * @param remove
 	 *            if to perform the remove thread or not
 	 * @param maxElements
 	 *            the maximum number of elements to create
 	 */
 	public SessionThread(final boolean remove, final int maxElements) {
 		super();
 		this.remove = remove;
 
 		sessionTest = new SessionTest(maxElements);
 
 		addSession = new Thread("addSession") {
 			@Override
 			public void run() {
 				System.err.println("Starting the add session thread...");
 
 				synchronized (sessionList) {
 					try {
 						while (sessionList.isEmpty()) {
 							final MSessionBean sessionBean = sessionTest.createSessionBean();
 
 							if (sessionBean == null) {
 								break;
 							}
 
 							sessionList.add(sessionBean);
 
 							try {
 								sessionTest.createSession(sessionBean);
 							} catch (final Exception ex) {
 								ex.printStackTrace();
 								interrupt();
 								break;
 							}
 
 							// To execute only if we do not perform the remove
 							// thread
 							if (!remove) {
 								sessionList.pop();
 							}
 
 							sessionList.notifyAll();
 
 							if (remove) {
 								sessionList.wait();
 							}
 						}
 
 						sessionList.notifyAll();
 					} catch (final Exception ex) {
 						ex.printStackTrace();
 					}
 				}
 			}
 		};
 
 		removeSession = new Thread() {
 			@Override
 			public void run() {
 				System.err.println("Starting the remove session thread...");
 
 				synchronized (sessionList) {
 					try {
 						while (sessionList.isEmpty()) {
 							sessionList.wait();
 						}
 
 						while (!sessionList.isEmpty()) {
 							final MSessionBean sessionBean = sessionList.pop();
 
 							try {
 								sessionTest.removeSession(sessionBean);
 							} catch (final Exception ex) {
 								ex.printStackTrace();
 								interrupt();
 								break;
 							}
 
 							sessionList.notifyAll();
 							sessionList.wait();
 						}
 
 						sessionList.notifyAll();
 					} catch (final Exception ex) {
 						ex.printStackTrace();
 					}
 				}
 			}
 		};
 	}
 	@Override
 	public void run() {
 		addSession.start();
 		if (remove) {
 			removeSession.start();
 		}
 	}
 }
