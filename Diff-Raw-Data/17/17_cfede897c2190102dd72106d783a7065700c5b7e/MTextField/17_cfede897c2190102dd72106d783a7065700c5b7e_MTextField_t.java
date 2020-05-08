 package javax.swing;
 
 import java.awt.Color;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 
 
 public class MTextField extends JPasswordField implements FocusListener,KeyListener{
 	private boolean password;
 	private boolean gray = false;
 	private final char bullet = '\u2022';
 	private final char noBullet = (char)0;
 	private  String prompt = null;
 	
 	
 	
 	public MTextField(){
 		init();
 	}
 	public MTextField(Document doc, String txt, int columns){
 		super(doc,txt,columns);
 		init();
 	}
 	public MTextField(int columns) {
 		super(columns);
 		init();
 	}
 	public MTextField(String text){
 		super(text);
 		init();
 	}
 	public MTextField(String text, int columns){
 		super(text,columns);
 		init();
 		
 	}
 	
 	private void init(){
 		setEchoChar(noBullet);
 		setupListeners();
 	}
 	
 	private void setupListeners(){
 		addFocusListener(this);
 		addKeyListener(this);
 	}
 	
 	private void setGrayText(String text){
 		setText(text);
 		
 		setForeground(Color.gray);
 
 		gray = true;
		if(password)
			setEchoChar(noBullet);
 	}
 	
 	public void setPrompt(String text){
 		prompt = text;
 		
 		addKeyListener(this);
 		
 		setGrayText(text);
 		
 	}
	
	public void setHidden(boolean hidden){
		password = hidden;
		if(password && !gray)
			setEchoChar(bullet);
		else
			setEchoChar(noBullet);
	}
	
 	@Override
 	public void focusGained(FocusEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void focusLost(FocusEvent e) {
 		if(prompt != null && getText().equals("") ){
 			setGrayText(prompt);
 		}
 	}
 	
 	@Override 
 	public String getText(){
 		return new String(getPassword());
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		if(gray){
 			setText("");
 			setForeground(Color.black);
 			gray = false;
			if(password)
				setEchoChar(bullet);
 		}
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		if( prompt != null && getText().equals("")){
 			setGrayText(prompt);
 			setCaretPosition(0);
 		}
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 }
