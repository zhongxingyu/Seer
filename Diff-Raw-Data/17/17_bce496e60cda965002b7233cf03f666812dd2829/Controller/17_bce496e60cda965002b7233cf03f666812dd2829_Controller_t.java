 package Sergi.MVC.Controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import Sergi.MVC.Model.Model;
 import Sergi.MVC.Model.Task;
 import Sergi.MVC.Viewer.AddEditDialog;
 import Sergi.MVC.Viewer.ButtonNames;
 import Sergi.MVC.Viewer.MainFrame;
 
 public class Controller implements ActionListener, MainFrameObserverInterface,
 		ListSelectionListener {
 
 	static SimpleDateFormat sdf;
 	private static Model model;
 	private static MainFrame mainFrame;
 	private Task taskForEdit;
 	private AddEditDialog addDialog;
 
 	/**
 	 * 
 	 * @throws ClassNotFoundException
 	 *             if the LookAndFeel class could not be found
 	 * @throws InstantiationException
 	 *             if a new instance of the class couldn't be created
 	 * @throws IllegalAccessException
 	 *             if the class or initializer isn't accessible
 	 * @throws UnsupportedLookAndFeelException
 	 *             if lnf.isSupportedLookAndFeel() is false
 	 */
 	public Controller() throws ModelException {
 		model = new Model();
 		try {
 			setLookAndFeel();
 		} catch (ModelException e) {
 			MainFrame.showErrorMessage(mainFrame, e.toString());
 		}
 		mainFrame = new MainFrame(" ");
 		mainFrame.initComponents();
 		this.update(model.getTaskList());
 		model.registerObserver(this);
 		mainFrame.addActionListener(this);
 		mainFrame.addListSelections(this);
 
 		model.startTaskCheking();
 		mainFrame.setVisible(true);
 
 	}
 
 	/*
 	 *          , 
 	 *    .
 	 * 
 	 * -    TaskObserver.       
 	 *  .
 	 */
 	public static void main(String[] args) {
 		try {
 			new Controller();
 		} catch (ModelException e) {
 			MainFrame.showErrorMessage(mainFrame, e.toString());
 		}
 	}
 
 	public void showAddDialog(int i) {
 		addDialog = new AddEditDialog(mainFrame, false);
 		if (i == -1) {
 			addDialog.setTask(new Task());
 		} else {
 			taskForEdit = model.getTaskList().get(i);
 			addDialog.setTask(taskForEdit);
 		}
 		addDialog.addListeners(this);
 		addDialog.setVisible(true);
 
 	}
 
 	public void findTaskIndex(String text) {
 		int findedTaskInList = model.getTaskIndex(text);
 		if (findedTaskInList != -1)
 			mainFrame.enableInList(findedTaskInList);
 	}
 
 	public void removeTask(List<Object> list) {
 		for (Object task : list) {
 			model.removeTask((Task) task);
 		}
 	}
 
 	public void exit() throws ModelException {
 		model.writeTasksToFile(model.getArrayTaskList());//   
 															// 
 		System.exit(0);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		ButtonNames buttonName = ButtonNames.getType(event.getActionCommand());
 		switch (buttonName) {
 		case BUTTON_NAME_ADD_DIALOG: {
 			this.showAddDialog(-1);
 			break;
 		}
 		case BUTTON_NAME_REPALCE: {
 			if (mainFrame.getSelectedIndicies().length == 1) {
 				this.showAddDialog(mainFrame.getSelectedIndex());
 			} else
 				showErrorMessage("     !");
 			break;
 		}
 		case BUTTON_NAME_REMOVE: {
 			this.removeTask(mainFrame.getSelectasValuesList());
 			break;
 		}
 		case BUTTON_NAME_EXIT: {
 			try {
 				this.exit();
 			} catch (ModelException e) {
 				MainFrame.showErrorMessage(mainFrame, e.getMessage());
 			}
 			break;
 		}
 		case BUTTON_NAME_FIND: {
 			this.findTaskIndex(mainFrame.getFindString());
 			break;
 		}
 		case BUTTON_NAME_ADD_TASK: {
 			if (taskForEdit != null)
 				model.removeTask(taskForEdit);
 			model.addNewTask(addDialog.getTask());
 			addDialog.dispose();
 			model.checkTasks();
 			break;
 		}
 		case BUTTON_NAME_CANCEL_TASK: {
 			addDialog.dispose();
 			break;
 		}
 		}
 
 		mainFrame.setVisible(false);
 		mainFrame.repaint();
 		mainFrame.setVisible(true);
 	}
 
 	@Override
 	public void update(Object value) {
 		if (value instanceof Task)
 			JOptionPane.showMessageDialog(null, "It`s time for task \""
 					+ ((Task) value).getTitle() + "\".",
 					((Task) value).getTitle(), JOptionPane.DEFAULT_OPTION);
 		else if (value instanceof ArrayList<?>)
 			mainFrame.updateList(model.getArrayTaskList());
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent arg0) {
 		mainFrame.setButtonEnabled(
 				ButtonNames.BUTTON_NAME_REMOVE.getTypeValue(), true);
 	}
 
 	public static void showErrorMessage(String errorString) {
 		MainFrame.showErrorMessage(mainFrame, errorString);
 	}
 
 	/**
 	 * This work if using java 1.7
 	 * 
 	 * @throws ModelException
 	 */
 	private static void setLookAndFeel() throws ModelException {
 		try {
 			UIManager
 					.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
 		} catch (ClassNotFoundException e) {
			throw new ModelException(e, "    ");
 		} catch (InstantiationException e) {
 			throw new ModelException(e);
 		} catch (IllegalAccessException e) {
 			throw new ModelException(e);
 		} catch (UnsupportedLookAndFeelException e) {
 			throw new ModelException(e);
 		}
 	}
 }
