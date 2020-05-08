 package view;
 
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JTextField;
 
 public class HintTextField extends JTextField implements FocusListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private String hint;
 	private int col;
 	
 	public HintTextField(String hint, int col){
 		
 		super(hint, col);
 		this.hint=hint;
 		this.col=col;
 		super.addFocusListener(this);
 	}
 
     @Override
     public void focusGained(FocusEvent e) {
     	super.setEnabled(true);
         if(this.getText().isEmpty()) {
             super.setText("");
         }
     }
    
     @Override
     public void focusLost(FocusEvent e) {
         if(this.getText().isEmpty()) {
             super.setText(hint);
         }
     }
 
     @Override
     public String getText() {
         String typed = super.getText();
         return typed.equals(hint) ? "" : typed;
     }
    
 }
 
