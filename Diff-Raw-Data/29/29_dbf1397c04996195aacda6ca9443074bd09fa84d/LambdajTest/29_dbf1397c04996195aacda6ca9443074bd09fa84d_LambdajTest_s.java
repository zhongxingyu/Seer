 package cz.querity.qibernate.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import static ch.lambdaj.Lambda.*;
 import static org.hamcrest.Matchers.*;
 
 import cz.querity.qibernate.model.Cat;
 
 public class LambdajTest {
 	@Test
 	public void testLambdaj() {
 		final List<Cat> cats = new ArrayList<Cat>();
 		for (int i = 0; i < 10; i++) {
			final Cat cat = new Cat("name" + i, null, (int) (Math.random() * 100));
 			cats.add(cat);
 		}
 
		final List<Cat> filteredCats = select(cats, having(on(Cat.class).getAge(),
				equalTo(selectMin(cats, on(Cat.class).getAge()))));
		System.out.println(filteredCats);
 	}
 }
