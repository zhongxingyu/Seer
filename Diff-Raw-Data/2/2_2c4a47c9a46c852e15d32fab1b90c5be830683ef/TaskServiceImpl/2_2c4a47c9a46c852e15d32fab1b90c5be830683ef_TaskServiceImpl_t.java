 package fr.kevinya.todolist.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import fr.kevinya.todolist.dao.TaskDao;
 import fr.kevinya.todolist.model.Task;
 
 @Service
 public class TaskServiceImpl implements TaskService {
 	
 	@Autowired
 	TaskDao taskDao;
 
 	@Transactional
 	public Task create(String name) {
 		Task task = new Task(name, 0, 1);
 		return create(task);
 	}
 
 	@Override
 	public Task create(Task task) {
		Task currentTask = new Task(task.getName(), task.getStatus(), task.getVersion() + 1);
 		return taskDao.create(currentTask);
 	}
 
 	@Transactional
 	public Task update(Integer id, String name, Integer status, Integer version) {
 		Task task = find(id);
 		if (task != null) {
 			task.setName(name);
 			task.setStatus(status);
 			task.setVersion(version);
 			task = taskDao.update(task);
 		}
 		return task;
 	}
 
 	@Transactional
 	public Task update(Integer id, Task task) {
 		Task currentTask = find(id);
 		if (currentTask != null) {
 			currentTask.setName(task.getName());
 			currentTask.setStatus(task.getStatus());
 			currentTask.setVersion(task.getVersion());
 			currentTask = taskDao.update(currentTask);
 		}
 		return currentTask;
 	}
 
 	@Transactional
 	public void delete(Integer id) {
 		Task task = find(id);
 		if (task != null) {
 			taskDao.delete(task);
 		}
 	}
 
 	@Transactional
 	public Task find(Integer id) {
 		return taskDao.find(id);
 	}
 
 	@Transactional
 	public List<Task> findAll() {
 		return taskDao.findAll();
 	}
 
 	@Transactional
 	public List<Task> findNotDeleted() {
 		return taskDao.findNotDeleted();
 	}
 
 }
