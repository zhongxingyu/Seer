 package view;
 
 import javax.swing.*;
 import model.*;
 import net.miginfocom.swing.MigLayout;
 
 public class CategoryPanel extends JPanel{
 
 	private CategoryModel model;
 	
 	public CategoryPanel(CategoryModel model) {
 		this.model=model;
		setLayout(new MigLayout("", "[][][][][]", "[][][][][][]"));
		
		JButton btnNewButton = new JButton("New button");
		add(btnNewButton, "cell 2 0");
 		
 		JLabel catLabel = new JLabel(model.getCatTitle());
		add(catLabel, "cell 2 2,growy");
 	}
 
 }
