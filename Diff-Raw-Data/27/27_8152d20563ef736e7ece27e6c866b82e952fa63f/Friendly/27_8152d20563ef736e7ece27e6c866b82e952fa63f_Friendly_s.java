 package com.friendly;
 
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Properties;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 import com.friendly.client.RemotePersonQuestionFactory;
 import com.friendly.people.LocalPersonQuestionFactory;
 import com.friendly.people.MockPeopleProvider;
 import com.friendly.people.PeopleProvider;
 import com.friendly.people.PersonQuestion;
 import com.friendly.swing.QuestionPanel;
 import com.friendly.swing.event.QuestionAnswerEvent;
 import com.friendly.swing.event.QuestionAnswerEventListener;
 
 /**
  * Friendly client application.
  * 
  * @author Ben Burton
  */
 public class Friendly extends JFrame implements QuestionAnswerEventListener {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final String PROPERTIES_FILE = "friendly.properties";
 	private static final int PAUSE_AFTER_ANSWER = 3;
 
 	private boolean useLocal;
 
 	public Friendly() {
 		setupFrame();
 		useLocal = Boolean.valueOf(getProperty("uselocal"));
 		add(getQuestionPanel());
 		setVisible(true);
 	}
 
 	public static String getProperty(String key) {
		Properties properties = new Properties();
		try {
			URL url = ClassLoader.getSystemResource(PROPERTIES_FILE);
			if (url != null) {
				properties.load(url.openStream());
 			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
 		}
 		return properties.getProperty(key);
 	}
 
 	private QuestionPanel getQuestionPanel() {
 		QuestionPanel questionPanel = new QuestionPanel(getQuestion());
 		questionPanel.addQuestionAnswerEventListener(this);
 		return questionPanel;
 	}
 
 	private PersonQuestion getQuestion() {
 		if (useLocal) {
 			PeopleProvider provider = new MockPeopleProvider();
 			return LocalPersonQuestionFactory.getQuestion(provider);
 		}
 		return RemotePersonQuestionFactory.getQuestion();
 	}
 
 	private void setupFrame() {
 		setTitle("Friendly!");
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		setSize(500, 300);
 		centerWindow(dim);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	private void centerWindow(Dimension dim) {
 		int w = this.getSize().width;
 		int h = this.getSize().height;
 		int x = (dim.width - w) / 2;
 		int y = (dim.height - h) / 2;
 		setLocation(x, y);
 	}
 
 	public static void main(String[] args) {
 		new Friendly();
 	}
 
 	@Override
 	public void questionAnswerEventOccurred(
 			QuestionAnswerEvent questionAnswerEvent) {
 		pauseAndExit();
 	}
 
 	private void pauseAndExit() {
 		new Timer(PAUSE_AFTER_ANSWER * 1000, new AbstractAction() {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				closeApp();
 			}
 
 		}).start();
 	}
 
 	private void closeApp() {
 		for (Frame frame : getFrames()) {
 			frame.dispose();
 		}
 	}
 
 }
