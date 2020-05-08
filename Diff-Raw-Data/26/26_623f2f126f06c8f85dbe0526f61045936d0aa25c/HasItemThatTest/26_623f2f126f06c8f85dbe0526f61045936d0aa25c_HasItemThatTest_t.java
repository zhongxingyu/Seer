 package edu.teco.dnd.tests;
 
 import static edu.teco.dnd.tests.HasItemThat.hasItemThat;
 import static edu.teco.dnd.tests.HasItemThat.hasNoItemThat;
 import static edu.teco.dnd.tests.MatcherTests.EMPTY_LIST;
 import static edu.teco.dnd.tests.MatcherTests.INTEGER_LIST;
 import static edu.teco.dnd.tests.MatcherTests.STRING_LIST;
 import static org.hamcrest.CoreMatchers.anything;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Test;
 
 public class HasItemThatTest {
 	@Test
 	public void testItemExists() {
 		assertThat(INTEGER_LIST, hasItemThat(is(equalTo(1))));
 		assertThat(INTEGER_LIST, hasItemThat(is(equalTo(2))));
 		assertThat(INTEGER_LIST, hasItemThat(is(equalTo(3))));
 		assertThat(STRING_LIST, hasItemThat(is(equalTo("foo"))));
 		assertThat(STRING_LIST, hasItemThat(is(equalTo("bar"))));
 		assertThat(STRING_LIST, hasItemThat(is(equalTo("foobar"))));
 		assertThat(STRING_LIST, hasItemThat(is(equalTo("baz"))));
 	}
 
 	@Test
 	public void testItemDoesNotExist() {
 		assertThat(EMPTY_LIST, hasNoItemThat(is(not(anything()))));
		assertThat(INTEGER_LIST, hasNoItemThat(is(not(isA(Integer.class)))));
		assertThat(STRING_LIST, hasNoItemThat(is(not(isA(String.class)))));
 	}
 
 	@Test
 	public void testMultipleItemsMatching() {
		assertThat(INTEGER_LIST, hasItemThat(isA(Integer.class)));
		assertThat(STRING_LIST, hasItemThat(isA(String.class)));
 	}
 }
