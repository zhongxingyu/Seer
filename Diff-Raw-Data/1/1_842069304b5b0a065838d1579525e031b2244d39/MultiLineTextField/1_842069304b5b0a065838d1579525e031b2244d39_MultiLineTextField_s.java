 package sciuto.corey.milltown.map.swing.components;
 
 import java.awt.Dimension;
 
 import javax.swing.BorderFactory;
 import javax.swing.JTextArea;
 import javax.swing.border.TitledBorder;
 
 public class MultiLineTextField extends JTextArea {
 
 	public MultiLineTextField(String name, int xSize, int ySize){
 		this.setName(name);
 		this.setMaximumSize(new Dimension(xSize,ySize));
 		
 		this.setBorder(BorderFactory.createTitledBorder(null, getName(), TitledBorder.CENTER,TitledBorder.TOP));
 		
 		this.setEditable(false);
 	}
 	
 }
