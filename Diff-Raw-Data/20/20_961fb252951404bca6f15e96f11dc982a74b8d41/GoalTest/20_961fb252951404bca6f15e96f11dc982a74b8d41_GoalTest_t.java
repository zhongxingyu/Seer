 package com.sas.comp.models;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.sas.comp.hibernate.Hibernate;
 
 public class GoalTest {
 
	@Test
	public void testScorer() {
		final Goal goal = Hibernate.getInstance().find(Goal.class, 1);
		Assert.assertNotNull(goal.getId());
	}
 }
