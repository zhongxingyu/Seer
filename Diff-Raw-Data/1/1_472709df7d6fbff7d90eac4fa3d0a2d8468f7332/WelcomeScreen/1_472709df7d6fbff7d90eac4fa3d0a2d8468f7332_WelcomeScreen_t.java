 package org.geworkbench.engine;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.engine.config.GUIFramework;
 import org.geworkbench.engine.config.VisualPlugin;
 import org.geworkbench.engine.skin.Skin;
 
 /**
  * Welcome Screen.
  * This by default shows up when you start
  * a new version of geWorkbench. When the user clicks the 'Hide' button, it
  * will go away unless the user opens it again from help menu.
  * 
  * @author yc2480
  * @author zji
  * @version $Id$
  */
 public class WelcomeScreen extends JPanel implements VisualPlugin {
 	private static final long serialVersionUID = -4675518732204562565L;
 
 	static Log log = LogFactory.getLog(WelcomeScreen.class);
 
 	/*
 	 * The name of the welcome file. This file can either be a plain text file
 	 * or a html file.
 	 */
 	private static final String WELCOMETEXT_FILENAME = "welcometext.html";
 
 	/*
 	 * The panel to hold JEditorPane, so the text field will expand to the whole
 	 * visual area. (Using BorderLayout.CENTER)
 	 */
 	private JPanel textFieldPanel = null;
 
 	/* The text area to show welcome message */
 	private JEditorPane textField = null;
 
 	/* The button to remove welcome component. */
 	private JButton button = new JButton("Hide");;
 
 	/*
 	 * will be at the bottom of the visual area, and will use default layout, so
 	 * button can be in the middle of it.
 	 */
 	private JPanel buttonPanel = null;
 
 	/**
 	 * This constructor will load the welcome file and display it in visual
 	 * area.
 	 */
 	public WelcomeScreen() {
 		textFieldPanel = new JPanel();
 		textFieldPanel.setLayout(new BorderLayout());
 		String filename = WELCOMETEXT_FILENAME;
 		try {
 			/* Try load the welcome file */
 			textField = new JEditorPane();
 			textField.setContentType("text/html");
 			textField.read(new BufferedReader(new FileReader(filename)),
 					filename);
 			textFieldPanel.add(textField, BorderLayout.CENTER);
			textField.setEditable(false);
 		} catch (IOException e1) {
 			/*
 			 * If we can not load the welcome file, disable welcome component
 			 * for failover.
 			 */
 			log.error("File " + filename + " not found.");
 			removeSelf();
 			return;
 		}
 		
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				removeSelf();
 			}
 		});
 		buttonPanel = new JPanel();
 		buttonPanel.add(button);
 		textFieldPanel.add(buttonPanel, BorderLayout.SOUTH);
 	}
 
 	private void removeSelf() {
 		Skin skin = (Skin)GUIFramework.getFrame();
 		skin.hideWelcomeScreen();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
 	 */
 	public Component getComponent() {
 		return textFieldPanel;
 	}
 
 }
