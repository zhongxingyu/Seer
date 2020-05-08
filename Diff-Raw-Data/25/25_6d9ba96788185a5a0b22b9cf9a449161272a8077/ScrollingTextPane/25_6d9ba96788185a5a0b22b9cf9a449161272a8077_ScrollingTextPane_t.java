 package telnet;
 
 import java.awt.Color;
 
import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.border.LineBorder;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
 
 @SuppressWarnings("serial")
 public class ScrollingTextPane extends JScrollPane {
 
 	private TextPane textPane;
 
 	public ScrollingTextPane() {
 		super();
 		textPane = new TextPane();
 		setBorder(new LineBorder(Color.BLACK));
		setVerticalScrollBar(new ScrollBar());
 		setViewportView(textPane);
 	}
 
 	public TextPane getTextPane() {
 		return textPane;
 	}
 
 	public class TextPane extends JTextPane {
 
 		public TextPane() {
 			super();
 			setBackground(Color.BLACK);
 			setForeground(Color.GREEN);
 			setCaretColor(Color.RED);
 		}
 
 		public synchronized void append(String s) {
 			setText(getText() + s + "\n");
 		}
 
 		public synchronized void setText(String s) {
 			super.setText(s);
 			setCaretPosition(getDocument().getLength());
 		}
 	}
	private class ScrollBar extends JScrollBar {
		public ScrollBar(){
			super();
			setUI(new CoolScrollBarUI());
		}
		
		private class CoolScrollBarUI extends BasicScrollBarUI{
			protected void configureScrollBarColors(){
				
			}
		}
	}
 }
