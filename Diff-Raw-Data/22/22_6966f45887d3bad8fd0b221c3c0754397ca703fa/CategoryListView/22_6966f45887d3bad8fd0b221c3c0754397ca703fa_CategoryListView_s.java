 package view;
 
 import java.awt.Dimension;
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 import utility.GraphicConstants;
 
 import controller.*;
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
 	 *	 for the added categorypanels 
 	 */
 	
 	public JPanel panel;
 	
 	private Deque<StartCategoryPanel> catDeque = new ArrayDeque<StartCategoryPanel>();
 	private Deque<StartCategoryPanel> lastCatDeque = new ArrayDeque<StartCategoryPanel>();
 	private CategoryModel defaultCategory;
 	private CategoryModel finishedCategory;
 	
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
 	
 	public void addToCatList(StartCategoryPanel catPanel){
 		catDeque.addLast(catPanel);
 		
 	}
 	
 	/**
 	 * Sets a default category to use when creating new tasks in HeaderController
 	 * in case no category panel is marked
 	 */
 	public void setDefaultCategory(CategoryModel catModel){
 		this.defaultCategory = catModel;
 		
 	}
 	
 	/**
 	 * Returns the default category
 	 * 
 	 * @return the default StartCategoryPanel
 	 */
 	public CategoryModel getDefaultCategory(){
 		return defaultCategory;
 	}
 	
 	public void setFinishedCategory(CategoryModel catModel){
 		this.finishedCategory = catModel;
 		
 	}
 	
 	
 	public CategoryModel getFinishedCategory(){
 		return finishedCategory;
 		
 	}
 	
 	/**
 	 * Marks a categorypanel in deque
 	 * @param catPanel
 	 */
 	
 	public void markCategory(StartCategoryPanel catPanel){
 		catDeque.remove(catPanel);
 		catDeque.addFirst(catPanel);
 		System.out.println("marked");
 	}
 	
 	/**
 	 * Returns the marked categorypanel from deque
 	 * @return
 	 */
 	
 	public StartCategoryPanel getMarkedPanel(){
 		if(catDeque.getFirst().equals(null)){
 			return null;
 		}else{
 			
 			System.out.println("getpanel working");
 			return catDeque.getFirst();
 		}
 	}
 	
 	
 	public void markLastCategory(StartCategoryPanel catPanel){
 		lastCatDeque.addFirst(catPanel);
 	}
 	
 	public StartCategoryPanel getLastMarkedPanel(){
 		if(lastCatDeque.getFirst().equals(null)){
 			return null;
 		}else{
 			
 			return lastCatDeque.getFirst();
 		}
 	}
 	
 	public Deque<StartCategoryPanel> getMarkedList(){
 		return catDeque;
 		
 	}
 	
 	public Deque<StartCategoryPanel> getLastMarkedList(){
 		return lastCatDeque;
 		
 	}
 	
 }
