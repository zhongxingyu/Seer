 /**
  * 
  */
 package ie.cit.cloudapp.web;
 
 import java.util.List;
 
 import ie.cit.cloudapp.ToDoRepository;
 import ie.cit.cloudapp.Todo;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * @author fiona
  * 
  */

@RequestMapping("todos")
 @Controller
 public class TodoController {
 	
 	@Autowired
 	private ToDoRepository repo;
 
 	@RequestMapping (method = RequestMethod.GET)
	public String listTodos(Model model) {
 		model.addAttribute("todos", repo.getTodos());
		return "output_list.jsp";
 	}
 	
 	@RequestMapping(method = RequestMethod.POST)
	public String createTodo(Model model, @RequestParam String text) {
 		Todo todo = new Todo();
		todo.setText(text);
 		repo.addTodo(todo);
 		model.addAttribute("todos", repo.getTodos());
		return "output_list.jsp";
 	}
 
 	@RequestMapping(method = RequestMethod.DELETE)
	public String deleteTodo(Model model, @RequestParam int todoId) {
 		repo.getTodos().remove(todoId - 1);
 		model.addAttribute("todos", repo.getTodos());
		return "output_list.jsp";
 	}
 
 	@RequestMapping(method = RequestMethod.PUT)
	public String updateTodo(Model model, @RequestParam int todoId) {
 		Todo todo = repo.getTodos().get(todoId - 1);
 		todo.setDone(!todo.isDone());
 		model.addAttribute("todos", repo.getTodos());
		return "output_list.jsp";
 	}
 
 }
 
