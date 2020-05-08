 package gui;
 
 import ui.ExpressionInterpreter;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JTextField;
 
 public final class ExpressionView
     extends JTextField {
 
   public static final long serialVersionUID = 1L;
 
   public static final ExpressionView instance = new ExpressionView();
 
   private ExpressionView() {
    // This is a singleton.
   }
   
  JTextField jtfExpression = new JTextField();  
 
   class ExpressionFieldListener implements ActionListener {
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String str = jtfExpression.getText();
 	}
 	  
   }
   
 }
