 	package view;
 
 import java.awt.*;
 
 import utility.*;
 import controller.*;
 
 import javax.swing.*;
 
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.border.LineBorder;
 import javax.swing.GroupLayout.Alignment;
 
 
 public class CategoryView extends JPanel {
 	
 	private JButton addNewCategoryButton;
 	private JTextField categoryTitleInput;
 	
 	/**
 	 * A class to represent the view which will interact with the user when adding new categories
 	 */
 	
 	public CategoryView(){
 		
 		this.setBackground(GraphicConstants.BACKGROUND);
 		setPreferredSize(new Dimension(210, 70));
 		setMinimumSize(new Dimension(210, 70));
 		setMaximumSize(new Dimension(210, 70));
 		setLayout(new MigLayout("", "[129.00px][75px]", "[49.00px]"));
 		
 		categoryTitleInput = new HintTextField("Add category...", 20);
 		categoryTitleInput.setForeground(Color.gray);
 		add(categoryTitleInput, "cell 0 0,alignx left,aligny center");
 		
 		categoryTitleInput.setFont(GraphicConstants.REGULARFONT);
 		categoryTitleInput.setActionCommand("newCategory");
 		
 		addNewCategoryButton = new JButton("Ok");
 		addNewCategoryButton.setBorder(null);
 		addNewCategoryButton.setMargin(new Insets(0, 1, 0, 1));
 		addNewCategoryButton.setPreferredSize(new Dimension(50, 29));
 		addNewCategoryButton.setMinimumSize(new Dimension(50, 29));
		addNewCategoryButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		add(addNewCategoryButton, "cell 1 0,alignx center,aligny center");
 		addNewCategoryButton.setBorderPainted(false);
 		addNewCategoryButton.setOpaque(true);
 		addNewCategoryButton.setMaximumSize(new Dimension(50, 29));
 		addNewCategoryButton.setForeground(new Color(255, 255, 255));
 		addNewCategoryButton.setBackground(new Color(30, 144, 255));
 		addNewCategoryButton.setFont(new Font("Tahoma", Font.BOLD, 12));
 		addNewCategoryButton.setActionCommand("newCategory");
 	}
 	
 	/**
 	 * Set controller for the button and textfield
 	 * @param controller
 	 */
 	public void setController(CategoryController controller){
 		
 		addNewCategoryButton.addActionListener(controller);
 		
 		categoryTitleInput.addActionListener(controller);
 	}
 	
 	/**
 	 * Returns the categorytitle that the user has typed in
 	 * @return
 	 */
 	public JTextField getTextField(){
 		
 		return categoryTitleInput;
 	}
 
 	
 }
