 package view;
 
 import javax.swing.*;
 import model.*;
 import net.miginfocom.swing.MigLayout;
 
 public class CategoryPanel extends JPanel{
 
 	private CategoryModel model;
 	
 	public CategoryPanel(CategoryModel model) {
 		this.model=model;
		setLayout(new MigLayout("", "[][]", "[]"));
 		
 		JLabel catLabel = new JLabel(model.getCatTitle());
		add(catLabel, "cell 1 0");
 	}
 
 }
