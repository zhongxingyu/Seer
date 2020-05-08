 package edu.rit.taskers.controller;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.rit.taskers.model.Actionable;
 import edu.rit.taskers.model.Task;
 import edu.rit.taskers.persistence.TaskDao;
 
 /**
  * Handles requests with a space related to tasks.
  */
 @Controller
 @RequestMapping("/tasks")
 public class TaskController {
 
     @Autowired
     private TaskDao taskDao;
 
 	private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
 
 	/**
 	 * Fetch all tasks that belong to session space id
 	 */
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView getAllTasks() {
 		//TODO - Session space ID
 		ModelAndView tasksMainPage = new ModelAndView("tasks", "alltasks", taskDao.findAll());
 		return tasksMainPage;
 	}
 
 	/**
 	 * Retrieve the task details from a selected task in the list
 	 */
 	@RequestMapping(value="/{id}", method=RequestMethod.GET)
 	public ModelAndView getEventDetails(@PathVariable int id) {
 		ModelAndView taskEditPage = new ModelAndView("edittask", "task", taskDao.findById(id));
 		return taskEditPage;
 	}
 
 	/**
 	 * Delete a task by id
 	 */
 	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
 	public String delete(@PathVariable int id) {
 		Task retrievedTask = taskDao.findById(id);
 		taskDao.delete(retrievedTask);
 		return "redirect:/tasks";
 	}
 
 	/**
 	 * Update tasks details from a selected task in the list
 	 */
 	@RequestMapping(value="/{id}", method=RequestMethod.POST)
 	public @ResponseBody String updateTask(@PathVariable int id, @RequestParam(value="title") String taskName,
 								 @RequestParam(value="targetdate") String targetDate,
 								 @RequestParam(value="priority") String priority,
 								 @RequestParam(value="description") String desc) {
 		
 		try {
 			DateFormat df = new SimpleDateFormat(Actionable.ACTION_UI_DATEFORMAT);
 			
 			if ( isEmpty( taskName ) ) {
 				return "Please specify a title.";
 			} else if ( targetDate == null ) {
 				return "Please specify a date.";
 			}
 	
 			// Create the new task object and persist it
 			Task fetchedTask = taskDao.findById(id);
 			fetchedTask.setName(taskName);
 			fetchedTask.setTargetDate(df.parse(targetDate));
 			fetchedTask.setPriority(priority);
 			fetchedTask.setDescription(desc);
 	
			taskDao.save( fetchedTask );
 			return "Task successfully updated!"; 
 			
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return "Invalid date specified.";
 		}
 	}
 
 	/**
 	 * Create New Task page requested
 	 */
 	@RequestMapping(value="/new", method=RequestMethod.GET)
 	public ModelAndView getCreateTaskPage() {
 		ModelAndView newTaskPage = new ModelAndView("newtask");
 		return newTaskPage;
 	}
 
 	/**
 	 * Create New Task from js function with form data parameters
 	 */
 	@RequestMapping(value="/new", method=RequestMethod.POST)
 	public @ResponseBody String createNewTask(@RequestParam(value="title") String taskName,
 								 @RequestParam(value="targetdate") String targetDate,
 								 @RequestParam(value="priority") String priority,
 								 @RequestParam(value="description") String desc) {
 		try {
 			DateFormat df = new SimpleDateFormat(Actionable.ACTION_UI_DATEFORMAT);
 			
 			if ( isEmpty( taskName ) ) {
 				return "Please specify a title.";
 			} else if ( isEmpty( targetDate ) ) {
 				return "Please specify a date.";
 			}
 	
 			// Create the new task object and persist it
 			Task newTask = new Task();
 			newTask.setName(taskName);
 			newTask.setTargetDate(df.parse(targetDate));
 			newTask.setPriority(priority);
 			newTask.setDescription(desc);
 	
 			taskDao.save( newTask );
 			return "Task successfully created!"; 
 			
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return "Invalid date specified.";
 		}
 	}
 
 	/**
 	 * String isEmpty helper method
 	 */
 	private boolean isEmpty(String s) {
 		return ( s == null ) || ( "".equals(s) ) || ( s.length() == 0 );
 	}
 }
