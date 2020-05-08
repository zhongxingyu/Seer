 package ar.proyecto.gui;
 
 import java.awt.Color;
 
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
 
 	private String convertToMultiligne(String result2) {
 		// TODO Auto-generated method stub
 		String message = result2.replaceAll("\n", "<br/>");
 //		message = message.replaceAll(" ", "_");
 		return "<html> " + message + "</html>";
 	}
 	
 }
