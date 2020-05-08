 package view;
 
 import java.awt.Dimension;
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 import utility.GraphicConstants;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 
 
 import model.CategoryModel;
 
 /**
  * A class to keep a graphical list of panels each representing a category.
  * The list will be represented in a scrollpane
  * @author Emma
  *
  */
 
 public class CategoryListView extends JScrollPane{ 
 	
 	/**
 	 * 	Create a panel that will be the actual container 
 	 *	 for the categorypanels 
 	 */
 	
 	public JPanel panel;
 	
 	private Deque<CategoryPanel> catDeque = new ArrayDeque<CategoryPanel>();
 	
 	/**
 	 * 	Set all the graphical data for the scrollpane
 	 */
 	
 	public CategoryListView(){
 		
 		this.setBackground(GraphicConstants.BACKGROUND);
 		this.setMinimumSize(new Dimension(210, 500));
 		this.setMaximumSize(new Dimension(210, 500));
 		this.setPreferredSize(new Dimension(210, 500));
 		this.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
 		this.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
 		
 		panel = new JPanel();
 		panel.setBackground(GraphicConstants.BACKGROUND);
 		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS ));
 		
 		DefaultCategoryPanelAll allDefaultCatPan = new DefaultCategoryPanelAll();
 		panel.add(allDefaultCatPan);
 		
 		
 		DefaultCategoryPanel completedDefaultCatPan = new DefaultCategoryPanel();
 		panel.add(completedDefaultCatPan);
 		
 		
 		getViewport().setView(panel);
 		
 	}
 	
 	/**
 	 *  Updatemethod that will be invoked each time anything is changed in the 
 	 *  categorylist
 	 */
 
 	public void updateView(){
 		panel.revalidate();
 		panel.repaint();
 		panel.validate();
 		
 		this.revalidate();
 		this.repaint();
 		this.setVisible(true);
 		System.out.println("view updated");
 	}
 	
 	/**
 	 * Adds category in deque
 	 * @param catPanel
 	 */
 	
 	public void addToCatList(CategoryPanel catPanel){
 		catDeque.addLast(catPanel);
 		
 	}
 	
//	/**
//	 * Adds defaultcategory in deque
//	 */
//	public void addDefaultToCatList(CategoryPanel catPanel){
//		catDeque.addFirst(catPanel);
//	}
 	
 	/**
 	 * Marks a categorypanel in deque
 	 * @param catPanel
 	 */
 	
 	public void markCategory(CategoryPanel catPanel){
 		catDeque.addFirst(catPanel);
 		System.out.println("marked");
 	}
 	
 	/**
 	 * Returns the marked categorypanel from deque
 	 * @return
 	 */
 	
 	public CategoryPanel getMarkedPanel(){
 		System.out.println("getpanel working");
		return null;
		//return catDeque.getFirst();
 	}
 }
