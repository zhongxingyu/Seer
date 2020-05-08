 package circulo.circulo_resource_controller.resource;
 
 import java.util.List;
 
 import javax.ws.rs.PathParam;
 
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

 import circulo.circulo_controller.ControllerProvider;
 
 abstract class Resource<T> {
	final ControllerProvider controller;
 
	public Resource() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		System.out.println("El controller tiene este valor=" + ctx);
		controller = (ControllerProvider) ctx.getBean("Controller");
 	}
 
 	public abstract List<T> findAll();
 
 	public abstract List<T> findByName(@PathParam("query") String query);
 
 	public abstract T findById(@PathParam("id") String id);
 
 	public abstract T create(T t);
 
 	public abstract T update(T t);
 
 	public abstract void remove(@PathParam("id") int id);
 }
