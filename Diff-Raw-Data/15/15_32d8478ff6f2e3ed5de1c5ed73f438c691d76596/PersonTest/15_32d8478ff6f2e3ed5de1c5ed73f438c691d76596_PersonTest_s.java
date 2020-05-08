 package de.htwg.seapal.person.models.mock;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import de.htwg.seapal.person.controllers.IPersonController;
 import de.htwg.seapal.person.database.IPersonDatabase;
 import de.htwg.seapal.person.models.IPerson;
 import de.htwg.seapal.person.models.IPersonTest;
 
 public class PersonTest extends IPersonTest {
 
 	@Override
 	protected Injector createInjector() {
 		Injector tInj = Guice.createInjector(new AbstractModule() {
 
 			@Override
 			protected void configure() {
 				bind(IPerson.class).to(
 						de.htwg.seapal.person.models.mock.Person.class);
 				bind(IPersonController.class)
 						.to(de.htwg.seapal.person.controllers.mock.PersonController.class);
 				bind(IPersonDatabase.class)
 						.to(de.htwg.seapal.person.database.mock.PersonDatabase.class);
 			}
 		});
 		return tInj;
 	}
 
 }
