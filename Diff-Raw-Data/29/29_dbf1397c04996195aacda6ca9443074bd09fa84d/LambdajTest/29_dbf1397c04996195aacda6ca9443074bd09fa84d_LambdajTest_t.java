 package cz.querity.qibernate.test;
 
import static org.junit.Assert.*;

 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import static ch.lambdaj.Lambda.*;
 import static org.hamcrest.Matchers.*;
 
 import cz.querity.qibernate.model.Cat;
 
 public class LambdajTest {
 	@Test
 	public void testLambdaj() {
		int expectedMinAge = Integer.MAX_VALUE;
 		final List<Cat> cats = new ArrayList<Cat>();
 		for (int i = 0; i < 10; i++) {
			final int age = (int) (Math.random() * 10);
			final Cat cat = new Cat("name" + i, null, age);
 			cats.add(cat);
			if (age < expectedMinAge) {
				expectedMinAge = age;
			}
 		}
 
		int minAge = ((Cat) selectMin(cats, on(Cat.class).getAge())).getAge();
		final List<Cat> catsWithMinAge = select(cats, having(on(Cat.class).getAge(),
				equalTo(minAge)));
		final List<Cat> catsWithoutMinAge = select(cats, having(on(Cat.class).getAge(),
				not(equalTo(minAge))));

		for (final Cat cat : catsWithMinAge) {
			assertEquals(expectedMinAge, cat.getAge());
		}
		for (final Cat cat : catsWithoutMinAge) {
			assertFalse(expectedMinAge == cat.getAge());
		}
		assertEquals(cats.size(), catsWithMinAge.size() + catsWithoutMinAge.size());
 	}
 }
