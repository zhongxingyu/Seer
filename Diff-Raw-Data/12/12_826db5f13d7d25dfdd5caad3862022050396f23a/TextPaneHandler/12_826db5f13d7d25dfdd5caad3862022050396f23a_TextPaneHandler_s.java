 package gamesincommon;
 
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultCaret;
 import javax.swing.text.Document;
 import javax.swing.text.SimpleAttributeSet;
 
 public class TextPaneHandler extends Handler {
 
 	private JTextPane textPane;
 	
 	private SimpleAttributeSet attributes;
 
 	public TextPaneHandler() {
 		super();
 		textPane = new JTextPane();
 		textPane.setEditable(false);
 		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
 		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 		
 		attributes = new SimpleAttributeSet();
 		// #TODO: Set attributes so the font size isn't massive
 	}
 
 	@Override
 	public void publish(final LogRecord record) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				// add new line
 				Document tempDoc = textPane.getDocument();
 				int offset = tempDoc.getLength();
 				try {
					tempDoc.insertString(0, getFormatter().format(record), attributes);
 				} catch (BadLocationException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public JTextPane getTextPane() {
 		return this.textPane;
 	}
 
 	@Override
 	public void close() throws SecurityException {
 		// no code required here, as no significant memory/file/network assets are held during runtime
 	}
 
 	@Override
 	public void flush() {
 		// no buffering takes place (input is appended straight to textPane thus no action is needed during flush) 
 	}
 
 }
