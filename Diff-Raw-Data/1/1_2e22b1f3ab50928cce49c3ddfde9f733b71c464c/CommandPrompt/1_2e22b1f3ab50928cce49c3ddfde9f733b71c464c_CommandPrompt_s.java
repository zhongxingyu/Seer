 package UI;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 
 public class CommandPrompt extends JTextArea implements KeyListener, CaretListener{
 	private StringBuffer buffer = null;
 	private JFrame frame;
 	
 	public CommandPrompt(){
 		super(30,50);
 		this.setEditable(true);
 		this.setLineWrap(true);
 		this.addKeyListener(this);
 		this.addCaretListener(this);
 		this.setOpaque(true); //content panes must be opaque
 		
 		frame = new JFrame("Kitchen Inventory Tracker");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JScrollPane pane = new JScrollPane(this);
 		frame.setContentPane(pane);
 		
 		buffer = new StringBuffer();
 	}
 	
 	public void showGUI() {
         frame.pack();
         frame.setVisible(true);
     }
 	
 	@Override
 	public void keyPressed(KeyEvent arg0) {};
 	@Override
 	public void keyReleased(KeyEvent arg0) {};
 	@Override
 	public void keyTyped(KeyEvent e) {
 		char c = e.getKeyChar();
 		
 		if(c == '\b' && buffer.length() > 0){
 			return;
 		}else if(c == '\n' || c == '\r'){
 			synchronized (buffer){
 				buffer.notify();
 			}
 		}
 		buffer.append(c);
 	}
 	
 	public void writeMessage(String s){
 		this.append(s);
 		moveCaretToEnd();
 	}
 
 	public String getUserInput(){
 		String ret="";
 		try {
 			synchronized (buffer){
 				buffer.wait();
 			}
 		} catch (InterruptedException e) {}
 		
 		ret = buffer.toString();
 		buffer = new StringBuffer();
 		
 		return ret;
 	}
 	
 	@Override
 	public void caretUpdate(CaretEvent e) {
 		/* there is a bug in the API where rapid succession of clicks to move caret causes
 		 * and overflow exception
 		 */
 		moveCaretToEnd();
 	}
 	
 	private void moveCaretToEnd(){
 		this.setCaretPosition(this.getDocument().getEndPosition().getOffset()-1);
 	}
 	
 	public static void main(String args[]){
 		final CommandPrompt prompt = new CommandPrompt();
 		
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 prompt.showGUI();
             }
         });
 	}
 	public void close(){
 		frame.dispose();
 	}
 }
