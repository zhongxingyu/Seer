 package fr.Lala2012;
 
 import java.awt.Color;
 
 import javax.swing.JButton;
 
 
/** petit commentaire pour test */
 public class GuiButton extends JButton {
 	
 	private static final long serialVersionUID = 1L;
 
 	public enum ButtonType{
 		ERROR,
 		WARNING, 
 		SAVE,
 		SUBMIT,
 		DEFAULT;		
 	}
 	
 	
 	public GuiButton(String title, ButtonType type)
 	{
 		
 		
 		super(title);
 		switch (type){
 		case ERROR:
 			this.setBackground(Color.RED);
 			break;
 		case WARNING:
 			this.setBackground(Color.ORANGE);
 			break;
 		case SAVE:
 			this.setBackground(Color.GREEN);
 			break;
 		case DEFAULT:
 			this.setBackground(Color.GRAY);
 			break;
 		case SUBMIT:
 			this.setBackground(Color.GREEN);
 			break;
 		default:
 			break;
 		}
 
 		
 		
 	}
 	
 
 }
