 /*
  * Jan 19, 2006
  */
 package com.thinkparity.browser.application.browser;
 
 import com.thinkparity.model.parity.api.events.*;
 import com.thinkparity.model.parity.model.message.system.SystemMessage;
 
 /**
  * The browser's event dispatcher.
  * 
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 class EventDispatcher {
 
 	/**
 	 * The browser.
 	 * 
 	 */
 	protected final Browser browser;
 
 	/** The document listener. */
 	private DocumentListener documentListener;
 
 	/**
 	 * The parity session key event listener.
 	 * 
 	 */
 	private KeyListener sessionKeyListener;
 
 	/**
 	 * The parity session event listener.
 	 * 
 	 */
 	private SessionListener sessionSessionListener;
 
 	/**
 	 * The parity system message event listener.
 	 * 
 	 */
 	private SystemMessageListener systemMessageListener;
 
 	/**
 	 * Create an EventDispatcher.
 	 * 
 	 */
 	EventDispatcher(final Browser browser) {
 		super();
 		this.browser = browser;
 	}
 
 	/**
 	 * End the event dispatcher.
 	 *
 	 */
 	void end() {
 		browser.getDocumentModel().removeListener(documentListener);
 		documentListener = null;
 		
 		browser.getSessionModel().removeListener(sessionKeyListener);
 		sessionKeyListener = null;
 
 		browser.getSessionModel().removeListener(sessionSessionListener);
 		sessionSessionListener = null;
 
 		browser.getSystemMessageModel().removeListener(systemMessageListener);
 		systemMessageListener = null;
 	}
 
 	/**
 	 * Start the event dispatcher.
 	 *
 	 */
 	void start() {
 		documentListener = createDocumentListener();
 		browser.getDocumentModel().addListener(documentListener);
 
 		sessionKeyListener = createSessionModelKeyListener();
 		browser.getSessionModel().addListener(sessionKeyListener);
 
 		sessionSessionListener = createSessionModelSessionListener();
 		browser.getSessionModel().addListener(sessionSessionListener);
 
 		systemMessageListener = createSystemMessageListener();
 		browser.getSystemMessageModel().addListener(systemMessageListener);
 	}
 
 	private DocumentListener createDocumentListener() {
 		return new DocumentAdapter() {
             public void confirmationReceived(final DocumentEvent e) {
                 if(e.isRemote()) {
                     browser.fireDocumentConfirmationReceived(e.getDocument().getId());
                 }
             }
             public void documentCreated(final DocumentEvent e) {
                 if(e.isRemote()) {
                     browser.fireDocumentCreated(e.getDocument().getId(), Boolean.TRUE);
                 }
             }
             public void documentClosed(final DocumentEvent e) {
                 if(e.isRemote()) {
                     browser.getArtifactModel().removeFlagSeen(e.getDocument().getId());
                     browser.fireDocumentUpdated(e.getDocument().getId());
                 }
             }
             public void documentUpdated(final DocumentEvent e) {
                 if(e.isRemote()) {
                    browser.fireDocumentUpdated(e.getDocument().getId());
                 }
             }
 		};
 	}
 
 	private KeyListener createSessionModelKeyListener() {
 		return new KeyListener() {
 			public void keyRequestAccepted(final KeyEvent e) {
 				browser.fireDocumentUpdated(e.getArtifactId(), Boolean.TRUE);
 			}
 			public void keyRequestDenied(final KeyEvent e) {
 				browser.fireDocumentUpdated(e.getArtifactId(), Boolean.TRUE);
 			}
 			public void keyRequested(final KeyEvent e) {
 				browser.fireDocumentUpdated(e.getArtifactId(), Boolean.TRUE);
 			}
 		};
 	}
 
 	private SessionListener createSessionModelSessionListener() {
 		return new SessionListener() {
 			public void sessionEstablished() {}
 			public void sessionTerminated() {}
 			public void sessionTerminated(final Throwable cause) {
 				sessionTerminated();
 			}
 		};
 	}
 
 	private SystemMessageListener createSystemMessageListener() {
 		return new SystemMessageListener() {
 			public void systemMessageCreated(
 					final SystemMessageEvent systemMessageEvent) {
 				browser.fireSystemMessageCreated(((SystemMessage) systemMessageEvent.getSource()).getId());
 			}
 		};
 	}
 }
