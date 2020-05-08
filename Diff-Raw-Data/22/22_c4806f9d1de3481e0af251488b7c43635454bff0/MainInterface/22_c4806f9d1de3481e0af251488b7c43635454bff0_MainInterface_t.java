 package recipes;
 
 import javax.swing.JCheckBox;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class MainInterface extends JPanel {
 	String title;
 	JCheckBox checkBox1;
 	JTextField textBox1;
 	JButton button1;
 	JTextField solidIngredientTextBox;
 	JTextField solidQuantityTextBox;
 	JTextField liquidIngredientTextBox;
 	JTextField liquidQuantityTextBox;
 	
 	public MainInterface(String title) {
 		this.title = title;
 		checkBox1 = new JCheckBox(title);
 		checkBox1.addItemListener(new CheckBoxListener());
 		this.add(checkBox1);
 		
 		button1 = new JButton("Save Recipe");
        button1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				System.out.println("You clicked the button");
				
			}
         });      
  
 		
 		
 		
 		
 		
 		liquidIngredientTextBox = new JTextField("Liquid Ingredient");
 		liquidQuantityTextBox = new JTextField("Liquid Quantity");
 		solidIngredientTextBox = new JTextField("Solid Ingredient");
 		solidQuantityTextBox = new JTextField("Solid Quantity");
 		
 		this.add(liquidIngredientTextBox);
 		this.add(liquidQuantityTextBox);
 		this.add(solidIngredientTextBox);
 		this.add(solidQuantityTextBox);
 		this.add(button1);
 	}
 	
 	public class CheckBoxListener implements ItemListener  {
 		
 		   public void itemStateChanged(ItemEvent e) {
			   System.out.println("Changed");
		   }
 		
 	}
 }
