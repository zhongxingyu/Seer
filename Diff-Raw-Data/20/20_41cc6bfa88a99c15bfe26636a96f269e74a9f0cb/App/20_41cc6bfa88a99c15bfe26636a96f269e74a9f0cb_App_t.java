 package edu.ibs.core;
 
 import edu.ibs.common.enums.AccountRole;
 import edu.ibs.common.enums.Fraction;
 import edu.ibs.core.controller.CSUIDJpaController;
import edu.ibs.core.entity.*;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  * Hello world!
  *
  */
 public class App {
 
 	static {
 		PropertyConfigurator.configure(App.class.getClassLoader().getResource("log4j.properties"));
 	}
 
 	public static void main(String[] args) throws Exception {
 		CSUIDJpaController jpa = new CSUIDJpaController();
		User u = new User("Вадим", "Мартос", "AB1953782");
        jpa.insert(u);
        User u1 = jpa.select(u.getClass(), u.getId());
        System.out.println(u1.getFirstName());
        jpa.delete(u.getClass(), u.getId());
 	}
 }
