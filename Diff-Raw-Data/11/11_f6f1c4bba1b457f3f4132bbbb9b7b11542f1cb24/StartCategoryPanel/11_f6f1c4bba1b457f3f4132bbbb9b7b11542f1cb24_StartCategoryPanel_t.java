 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import model.CategoryModel;
 import utility.GraphicConstants;
 import controller.CategoryPanelController;
 
 public class StartCategoryPanel extends JPanel {
 
 	private CategoryModel categoryModel; 
 	private JLabel categoryLabel;
 	
 	/**
 	 * Create the panel.
 	 * @param categoryModel 
 	 */
 	public StartCategoryPanel(CategoryModel categoryModel) {
 		
 		this.categoryModel = categoryModel;
 		
 		this.setBackground(GraphicConstants.BACKGROUND);
 		setMinimumSize(new Dimension(200, 40));
 		setMaximumSize(new Dimension(200, 40));
 		setPreferredSize(new Dimension(200, 40));
 		
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 146, 41 , 0};
 		gridBagLayout.rowHeights = new int[]{111, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		categoryLabel = new JLabel("");
 		categoryLabel.setMaximumSize(new Dimension(120,30));
 		categoryLabel.setMinimumSize(new Dimension(120,30));
 		categoryLabel.setPreferredSize(new Dimension(120,30));
 		categoryLabel.setForeground(GraphicConstants.FOREGROUND);
 		
 		GridBagConstraints gbc_defaultLabel = new GridBagConstraints();
 		gbc_defaultLabel.anchor = GridBagConstraints.WEST;
 		gbc_defaultLabel.gridx = 1;
 		gbc_defaultLabel.gridy = 0;
 		
 		add(categoryLabel, gbc_defaultLabel);
 		setVisible(true);
 		
 		
 	}
	
 	public void setTitle(String title){
 		categoryLabel.setText(title);
 		
 	}
 	
 	public String getTitle(){
 		return categoryLabel.getText();
 		
 	}
 	public void setController(CategoryPanelController controller){
 		this.addMouseListener(controller);
 		
 	}
 	
 	public CategoryModel getModel(){
 		return categoryModel;
 		
 	}
 	
 	public void setTextColor(Color c){
 		categoryLabel.setForeground(c);
 		
 	}
 	
 }
