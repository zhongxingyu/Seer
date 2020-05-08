 /* Created Mar 23, 2009 by Andrew */
 package org.muis.browser;
 
 import org.muis.core.MuisDocument;
 import org.muis.core.MuisElement;
 import org.muis.core.event.MuisEvent;
 import org.muis.core.mgr.MuisMessage;
 
 /** A browser that renders MUIS documents */
 public class MuisBrowser extends javax.swing.JPanel {
 	javax.swing.JTextField theAddressBar;
 
 	private MuisContentPane theContentPane;
 
 	private boolean theDataLock;
 
 	private org.muis.core.event.MuisEventListener<MuisMessage> theMessageListener;
 
 	/** Creates a MUIS browser */
 	public MuisBrowser() {
 		setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
 		theAddressBar = new javax.swing.JTextField(250);
 		add(theAddressBar);
 		theAddressBar.setMaximumSize(theAddressBar.getPreferredSize());
 		theContentPane = new MuisContentPane();
 		add(theContentPane);
 		theContentPane.setPreferredSize(new java.awt.Dimension(800, 600));
 		theAddressBar.addKeyListener(new java.awt.event.KeyAdapter() {
 			@Override
 			public void keyReleased(java.awt.event.KeyEvent evt) {
 				if(evt.getKeyCode() != java.awt.event.KeyEvent.VK_ENTER)
 					return;
 				goToAddress(theAddressBar.getText());
 			}
 		});
 		theMessageListener = new org.muis.core.event.MuisEventListener<MuisMessage>() {
 			@Override
 			public void eventOccurred(MuisEvent<MuisMessage> event, MuisElement element) {
 				printMessage(event.getValue());
 			}
 
 			@Override
 			public boolean isLocal() {
 				return false;
 			}
 		};
 	}
 
 	/** @param address The address of the document to get */
 	public void goToAddress(String address) {
 		if(!theDataLock && !theAddressBar.getText().equals(address)) {
 			theDataLock = true;
 			try {
 				theAddressBar.setText(address);
 			} finally {
 				theDataLock = false;
 			}
 		}
 		java.net.URL url;
 		try {
 			url = new java.net.URL(address);
 		} catch(java.net.MalformedURLException e) {
 			throw new IllegalArgumentException(address + " is not a valid URL", e);
 		}
 		org.muis.core.MuisEnvironment env = new org.muis.core.MuisEnvironment();
 		env.setParser(new org.muis.core.parser.MuisDomParser(env));
 		env.setContentCreator(new org.muis.core.parser.MuisContentCreator());
 		MuisDocument muisDoc;
 		try {
 			org.muis.core.parser.MuisDocumentStructure docStruct = env.getParser().parseDocument(env, url,
 				new java.io.InputStreamReader(url.openStream()));
 			muisDoc = new MuisDocument(env, env.getParser(), docStruct.getLocation(), docStruct.getHead());
 			muisDoc.setGraphics(new org.muis.core.MuisDocument.GraphicsGetter() {
 				@Override
 				public java.awt.Graphics2D getGraphics() {
 					return (java.awt.Graphics2D) getContentPane().getGraphics();
 				}
 			});
 			env.getContentCreator().fillDocument(muisDoc, docStruct.getContent());
 		} catch(java.io.IOException e) {
 			throw new IllegalArgumentException("Could not access address " + address, e);
 		} catch(org.muis.core.parser.MuisParseException e) {
 			throw new IllegalArgumentException("Could not parse XML document at " + address, e);
 		}
 
		muisDoc.postCreate();
		theContentPane.setContent(muisDoc);
		if(getParent() instanceof java.awt.Frame)
			((java.awt.Frame) getParent()).setTitle(muisDoc.getHead().getTitle());
		repaint();
 		muisDoc.getRoot().addListener(org.muis.core.MuisConstants.Events.MESSAGE_ADDED, theMessageListener);
 		for(MuisMessage msg : env.msg())
 			printMessage(msg);
 		for(MuisMessage msg : muisDoc.allMessages())
 			printMessage(msg);
 	}
 
 	private void printMessage(MuisMessage msg) {
 		switch (msg.type) {
 		case FATAL:
 		case ERROR:
 		case WARNING:
 			System.err.println((msg.element == null ? "" : msg.element.toString() + "\n\t") + msg.type + ": " + msg.text);
 			if(msg.exception != null)
 				msg.exception.printStackTrace();
 			break;
 		case INFO:
 			System.out.println((msg.element == null ? "" : msg.element.toString() + "\n\t") + msg.type + ": " + msg.text);
 			if(msg.exception != null)
 				msg.exception.printStackTrace(System.out);
 			break;
 		}
 	}
 
 	/** @return The content pane that renders content pointed to from this browser */
 	public MuisContentPane getContentPane() {
 		return theContentPane;
 	}
 
 	/**
 	 * Starts up a MuisBrowser
 	 *
 	 * @param args Command-line arguments. If any are supplied, the first one is used as the initial address of the MUIS document to
 	 *            display.
 	 */
 	public static void main(String [] args) {
 		MuisBrowser browser = new MuisBrowser();
 		javax.swing.JFrame frame = new javax.swing.JFrame("MUIS Browser");
 		frame.setContentPane(browser);
 		frame.setBounds(0, 0, 900, 600);
 		frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 		if(args.length > 0)
 			browser.goToAddress(args[0]);
 	}
 }
