 package view;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import controller.TaskController;
 
import model.AllTaskListModel;
 import model.CategoryModel;
 import utility.*;
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.List;
 /**
  * A class to represent a the view which will interact 
  * with the user when adding tasks.
  *
  */
 public class ListView extends JScrollPane {
 	
 	public JPanel panelInScroll;
 	
 	
 	/**
 	 * Create the listview with a new panel in it.
 	 */
 	public ListView() {
 		
 		this.setBackground(GraphicConstants.BACKGROUND);
 		this.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
 		this.setMinimumSize(new Dimension (300, 500));
 		this.setMaximumSize(new Dimension (300, 500));
 		this.setPreferredSize(new Dimension (300, 500));
		
 		panelInScroll = new JPanel();
 		panelInScroll.setBackground(GraphicConstants.BACKGROUND);
 		panelInScroll.setLayout(new BoxLayout(panelInScroll, BoxLayout.Y_AXIS));
 		
//		for(int i=0; i<AllTaskListModel.getInstance().size(); i++){
//			TaskPanel panel= new TaskPanel(AllTaskListModel.getInstance().get(i));
//			panelInScroll.add(panel);
//		}
		
 		getViewport().setView(panelInScroll);
 		
 	}
 	
 	/**
 	 * Updates the the view, except every individual task panels in the view.
 	 */
 	public void updateView(){
 		
 //		this.updatePanels();
 		
 		panelInScroll.revalidate();
 		panelInScroll.validate();
 		panelInScroll.repaint();
 		
 		this.revalidate();
 		this.repaint();
 		
 	}	
 	
 	/**
 	 * Updates the task panels in list view with the task models data. 
 	 */
 	public void updatePanels(){
 		
 		Component[] taskPanels = new Component[panelInScroll.getComponents().length];
 		taskPanels = (Component[]) panelInScroll.getComponents();
 
 		for(int i=0; i<taskPanels.length; i++){
 			((TaskPanel)taskPanels[i]).setTitle();
 			((TaskPanel)taskPanels[i]).setDeadline();
 			taskPanels[i].validate();
 			taskPanels[i].repaint();
 		}
 	}
 	
 	public void displayTasks(CategoryModel catModel, TaskSettingView taskSetting, CategoryListView catListView){
 		
 		panelInScroll.removeAll();
 
 		
 		int size = catModel.getTaskList().size();
 		
 		for(int i = size - 1; i >= 0; i = i - 1){
 			
 			TaskPanel taskPanel = new TaskPanel(catModel.getTaskList().get(i));
 
 			panelInScroll.add(taskPanel);
 			this.updateView();
 			System.out.println("display task: " + catModel.getTaskList().get(i).getTitle());
 			TaskController taskController = new TaskController(this, taskPanel, taskSetting, catListView);
 			
 		}
 		
 		this.updateView();
 	}
 	
 }
