 package gui;
 
 import ui.ExpressionInterpreter;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JTextField;
 
 public final class ExpressionView
     extends JTextField {
 
   public static final long serialVersionUID = 1L;
 
   public static final ExpressionView instance = new ExpressionView();
 
  private JTextField jtfExpression;  

   private ExpressionView() {
	  jtfExpression = this;
  }
  
  public void setExpressionText(String text) {
	  this.setText(text);
   }
   
 
   class ExpressionFieldListener implements ActionListener {
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String str = jtfExpression.getText();
 	}
	
 	  
   }
   
 }
