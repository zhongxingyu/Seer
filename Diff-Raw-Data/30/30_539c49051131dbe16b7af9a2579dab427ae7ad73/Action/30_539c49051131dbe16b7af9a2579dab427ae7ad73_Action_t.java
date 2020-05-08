 package gui;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 
 import org.apache.log4j.Logger;
 
 import logic.JIDLogic;
 import data.Task;
 
 /**
  * Action class for using with key binding
  * 
  * @author Ramon
  *
  */
 public class Action {
 	
 	private static Logger logger=Logger.getLogger(JIDLogic.class);
 	
 	
 	static class ExitAction extends AbstractAction {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JIDLogic.JIDLogic_close();
 			System.exit(0);
 		}
 	}
 	
     static class UndoAction extends AbstractAction {
         @Override
         public void actionPerformed(ActionEvent e) {
         	logger.debug("*****EXECMD: UNDO*******");
         	JIDLogic.setCommand("UNDO");
         	Task[] task = JIDLogic.executeCommand("UNDO");
         	if(task == null)
        		;
        		//MainJFrame.showPopup("UNDO unsuccessfully!");
         	else {
         		MainJFrame.showPopup("UNDO: "+task[0].getName());
             	ExpandComponent.updateJTable();
         	}
         }
     }
 	
     static class DeleteAction extends AbstractAction {
     	Task[] task;
         @Override
         public void actionPerformed(ActionEvent e) {
         	Task[] taskList = ExpandComponent.getSeletedTask();
         	
         	if(taskList.length == 0)
         		return;
 	        else {
 	        	String exeCmd = "DELETE ";
 	        	for(Task t: taskList) {
 	        		exeCmd += t.getTaskId() + " ";
 	        	}
 	        	
 	        	JIDLogic.setCommand("DELETE");
 	        	Task[] result = JIDLogic.executeCommand(exeCmd);
 	        	
 	        	if(result.length == 1){
 	        		UIController.showTopPopUpMsg("DELETE: " + result[0]);
 	        	}
 	        	else {
 	        		UIController.showTopPopUpMsg("DELETE: " + result.length + " tasks.");
 	        	}
 	        	
 	        	UIController.refresh();
 	        }
         }
     }
     
     static class CompletedAction extends AbstractAction {
     	Task[] task;
         @Override
         public void actionPerformed(ActionEvent e) {
         	Task[] taskList = ExpandComponent.getSeletedTask();
         	
         	if(taskList.length == 0)
         		return;
 	        else {
 	        	String exeCmd = "COMPLETED ";
 	        	for(Task t: taskList) {
 	        		exeCmd += t.getTaskId() + " ";
 	        	}
 	        	
 	        	JIDLogic.setCommand("COMPLETED");
 	        	Task[] result = JIDLogic.executeCommand(exeCmd);
 	        	
 	        	if(result.length == 1){
 	        		UIController.showTopPopUpMsg("COMPLETED: " + result[0]);
 	        	}
 	        	else {
 	        		UIController.showTopPopUpMsg("COMPLETED: " + result.length + " tasks.");
 	        	}
 	        	
 	        	UIController.refresh();
 	        }
         }
     }
     
     static class ImportantAction extends AbstractAction {
     	Task[] task;
         @Override
         public void actionPerformed(ActionEvent e) {
         	Task[] taskList = ExpandComponent.getSeletedTask();
         	
         	if(taskList.length == 0)
         		return;
 	        else {
 	        	String exeCmd = "IMPORTANT ";
 	        	for(Task t: taskList) {
 	        		exeCmd += t.getTaskId() + " ";
 	        	}
 	        	
 	        	JIDLogic.setCommand("IMPORTANT");
 	        	Task[] result = JIDLogic.executeCommand(exeCmd);
 	        	
 	        	if(result.length == 1){
 	        		UIController.showTopPopUpMsg("IMPORTANT: " + result[0]);
 	        	}
 	        	else {
 	        		UIController.showTopPopUpMsg("IMPORTANT: " + result.length + " tasks.");
 	        	}
 	        	
 	        	UIController.refresh();
 	        }
         }
     }
     
     static class OverdueAction extends AbstractAction {
     	@Override
     	public void actionPerformed(ActionEvent e) {
     		JIDLogic.setCommand("overdue");
     		logger.debug("*********exeCmd(inside Action): Overdue");
     		Task[] task = JIDLogic.executeCommand("OVERDUE");
     		
     		logger.debug(task[0].toString());
     		
     		UIController.showTopPopUpMsg(task.length + " task(s) overdue.");
     		ExpandComponent.updateJTableWithTasks(task);
     	}
     }
     
     static class RedoAction extends AbstractAction {
     	@Override
     	public void actionPerformed(ActionEvent e) {
     		JIDLogic.setCommand("redo");
     		logger.debug("******exeCmd(inside Action: Redo");
     		Task[] task = JIDLogic.executeCommand("redo");
     		
         	if(task == null)
        		;
        		//MainJFrame.showPopup("REDO unsuccessfully!");
         	else {
         		MainJFrame.showPopup("REDO: "+task[0].getName());
             	ExpandComponent.updateJTable();
         	}
     		
     		UIController.refresh();
     	}
     }
     
     static class ListAction extends AbstractAction {
     	@Override
     	public void actionPerformed(ActionEvent e) {
     		UIController.refresh();
     		UIController.expandFrame();
     	}
     }
     
     static class ExpandAction extends AbstractAction {
     	@Override
     	public void actionPerformed(ActionEvent e) {
     		if(UIController.isFrameExpand())
     			UIController.contractFrame();
     		else
     			UIController.expandFrame();
     	}
     }
     
     static class HelpAction extends AbstractAction {
     	public void actionPerformed(ActionEvent e) {
     		//call help dialog
     	}
     }
 
     static class GCalendarAction extends AbstractAction {
     	@Override
     	public void actionPerformed(ActionEvent e) {
     		new LogInDialog(UIController.mainJFrame
     				, UIController.mainJFrame.getLocation().x + 60
     				, UIController.mainJFrame.getLocation().y - 120
     				);
     	}
     }
 }
