 package ch.zhaw.jtodo.controller;
 
 import java.util.Observer;
 
 import ch.zhaw.jtodo.dao.DAOFactory;
 import ch.zhaw.jtodo.domain.Task;
 import ch.zhaw.jtodo.model.DataHandler;
 import ch.zhaw.jtodo.model.IDataHandler;
 import ch.zhaw.jtodo.view.JtodoView;
 
 public class GUIController implements IGUIController {
 
 	private IDataHandler model;
 	private JtodoView view;
 
 	public GUIController(IDataHandler model, JtodoView view) {
 		this.model = model;
 		this.view = view;
 	}
 
 	public void addObserver(Observer view) {
 		model.addNewObserver(view);
 	}
 
 	@Override
 	public void getCategory(int catID) {
 		if (catID == 0) {
 			model.getAllTasks();
 		} else {
 			model.getTaskByCategory(catID);
 		}
 	}
 
 	@Override
 	public void getPriority(int prioID) {
 		if (prioID == 0) {
 			model.getAllTasks();
 		} else {
 			model.getTaskByPriority(prioID);
 		}
 	}
 
 	public void getInitalData() {
 		model.getAllPrioritys();
 		model.getAllCategorys();
 		model.getAllTasks();
 	}
 
 	@Override
 	public void addTask(Task task) {
		model.createTask(task);
 		// notify
 		model.getAllTasks();
 	}
 
 	@Override
 	public void update(Task task) {
		model.updateTask(task);
 	}
 }
