 package ar.proyecto.gui;
 
 import java.awt.Color;
import java.awt.Font;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class BottomPanelString extends JPanel {
 	private JLabel result;
 
 	public BottomPanelString() {
 		super();
 		this.setBackground(new Color(Integer.parseInt("ffece3",16)));
 		this.setBorder(BorderFactory.createTitledBorder("Result"));
 		this.result = new JLabel("");
 		this.add(this.result);
 		this.setVisible(true);
 	}
 
 	public void setResult(String result){
 		//modifica el resultado 
 		this.remove(this.result);
 		this.result = new JLabel(convertToMultiligne(result));
 		this.add(this.result);
 		this.revalidate();
 		this.repaint();
 	}
 
	private String convertToMultiligne(String messageToChange) {
 		// TODO Auto-generated method stub
		String message = messageToChange.replaceAll("\n", "<br/>");
		message = message.replaceAll(" ", "&nbsp;");
		return "<html><font face=\"Monospace\" size=\"3\">" + message + "</html>";
 	}
 	
 }
