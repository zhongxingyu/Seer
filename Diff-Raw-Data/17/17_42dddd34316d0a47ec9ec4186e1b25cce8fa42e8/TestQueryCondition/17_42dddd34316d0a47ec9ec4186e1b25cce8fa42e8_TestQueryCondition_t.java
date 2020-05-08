 package org.objectquery.builder;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.objectquery.builder.domain.Person;
 
 public class TestQueryCondition {
 
 	@Test
 	public void testPathValueQueryBuild() {
 		TestQueryBuilder builder = new TestQueryBuilder();
 		ObjectQuery<Person> query = new AbstractObjectQuery<Person>(builder, Person.class);
 		Person toSearch = query.target();
 		query.condition(toSearch.getHome().getAddress(), ConditionType.EQUALS, toSearch.getDog().getHome().getAddress());
		query.condition(toSearch.getDud().getHome(), ConditionType.EQUALS, toSearch.getDog().getHome());
 		query.condition(toSearch.getMum().getName(), ConditionType.EQUALS, toSearch.getDog().getOwner().getName());
 		query.order(toSearch.getName());
 
 		builder.build();
		Assert.assertEquals("There is more conditions than expected", builder.getConditionsString().size(), 3);
 		Assert.assertEquals("There is more orders than expected", builder.getOrdersString().size(), 1);
 
 		Assert.assertEquals("Not present expected condition", "home.address EQUALS dog.home.address", builder.getConditionsString().get(0));
		Assert.assertEquals("Not present expected condition", "dud.home EQUALS dog.home", builder.getConditionsString().get(1));
		Assert.assertEquals("Not present expected condition", "mum.name EQUALS dog.owner.name", builder.getConditionsString().get(2));
 		Assert.assertEquals("Not present expected order", builder.getOrdersString().get(0), "name");
 	}
 
 	public void testConditionsTypes() {
 		TestQueryBuilder builder = new TestQueryBuilder();
 		ObjectQuery<Person> query = new AbstractObjectQuery<Person>(builder, Person.class);
 		Person toSearch = query.target();
 		query.condition(toSearch.getMum().getName(), ConditionType.EQUALS, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.CONTAINS, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.IN, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.LIKE, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.MAX, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.MAX_EQUALS, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.MIN, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.MIN_EQUALS, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.NOT_EQUALS, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.NOT_IN, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.NOT_CONTAINS, toSearch.getDud().getName());
 		query.condition(toSearch.getMum().getName(), ConditionType.NOT_LIKE, toSearch.getDud().getName());
 
 		Assert.assertEquals("mum.name EQUALS dud.name", builder.getConditionsString().get(0));
 		Assert.assertEquals("mum.name CONTAINS dud.name", builder.getConditionsString().get(1));
 		Assert.assertEquals("mum.name IN dud.name", builder.getConditionsString().get(2));
 		Assert.assertEquals("mum.name LIKE dud.name", builder.getConditionsString().get(3));
 		Assert.assertEquals("mum.name MAX dud.name", builder.getConditionsString().get(4));
 		Assert.assertEquals("mum.name MAX_EQUALS dud.name", builder.getConditionsString().get(5));
 		Assert.assertEquals("mum.name MIN dud.name", builder.getConditionsString().get(6));
 		Assert.assertEquals("mum.name MIN_EQUALS dud.name", builder.getConditionsString().get(7));
 		Assert.assertEquals("mum.name NOT_EQUALS dud.name", builder.getConditionsString().get(8));
 		Assert.assertEquals("mum.name NOT_IN dud.name", builder.getConditionsString().get(9));
 		Assert.assertEquals("mum.name NOT_CONTAINS dud.name", builder.getConditionsString().get(10));
 		Assert.assertEquals("mum.name NOT_LIKE dud.name", builder.getConditionsString().get(11));
 
 	}
 }
