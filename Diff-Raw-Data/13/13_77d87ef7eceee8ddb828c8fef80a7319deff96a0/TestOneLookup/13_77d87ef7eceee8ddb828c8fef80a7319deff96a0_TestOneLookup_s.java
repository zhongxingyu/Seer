 package org.umlg.tinker.lookup;
 
 import junit.framework.Assert;
 import org.junit.Test;
 import org.umlg.collectiontest.Nightmare;
 import org.umlg.concretetest.God;
 import org.umlg.constraints.ConstraintChild1;
 import org.umlg.constraints.ConstraintChild2;
 import org.umlg.constraints.ConstraintRoot;
 import org.umlg.interfacetest.*;
 import org.umlg.lookup.Devil1;
 import org.umlg.lookup.Devil2;
 import org.umlg.lookup.Level1;
 import org.umlg.lookup.Level2;
 import org.umlg.runtime.collection.TinkerSet;
 import org.umlg.runtime.collection.ocl.BodyExpressionEvaluator;
 import org.umlg.runtime.collection.ocl.BooleanExpressionEvaluator;
 import org.umlg.runtime.test.BaseLocalDbTest;
 
 public class TestOneLookup extends BaseLocalDbTest {
 
 	@Test
 	public void testCompositeParent() {
 		God g = new God(true);
 		g.setName("G");
 		Creature c1 = new Creature(g);
 		c1.setName("c1");
 		Creature c2 = new Creature(g);
 		c2.setName("c2");
 		Spook s1 = new Spook(g);
 		s1.setName("s1");
 		Spook s2 = new Spook(g);
 		s2.setName("s2");
 		c1.addToSpook(s1);
 //		c2.addToSpook(s2);
         db.commit();
 		Assert.assertEquals(1, new Creature(c1.getVertex()).lookupFor_creature_spook().size());
 	}
 
 	@Test
 	public void testCompositeParentOfManyToManies() {
 		God g = new God(true);
 		ManyA manyA1 = new ManyA(g);
 		ManyA manyA2 = new ManyA(g);
 		ManyA manyA3 = new ManyA(g);
 		ManyA manyA4 = new ManyA(g);
 
 		ManyB manyB1 = new ManyB(g);
 		ManyB manyB2 = new ManyB(g);
 		ManyB manyB3 = new ManyB(g);
 		ManyB manyB4 = new ManyB(g);
 		manyA1.addToIManyB(manyB1);
 		manyA1.addToIManyB(manyB2);
 		manyA1.addToIManyB(manyB3);
 		manyA1.addToIManyB(manyB4);
 		manyA2.addToIManyB(manyB1);
 		manyA2.addToIManyB(manyB2);
 		manyA2.addToIManyB(manyB3);
 		manyA2.addToIManyB(manyB4);
 		manyA3.addToIManyB(manyB1);
 		manyA3.addToIManyB(manyB2);
 		manyA3.addToIManyB(manyB3);
 		manyA3.addToIManyB(manyB4);
 		manyA4.addToIManyB(manyB1);
 		manyA4.addToIManyB(manyB2);
 		manyA4.addToIManyB(manyB3);
 		manyA4.addToIManyB(manyB4);
         db.commit();
		Assert.assertEquals(4, new ManyA(manyA4.getVertex()).lookupFor_iManyA_iManyB().size());
		Assert.assertEquals(4, new ManyB(manyB4.getVertex()).lookupFor_iManyB_iManyA().size());
 	}
 
 	@Test
 	public void testLookupStrategy() {
 		God g = new God(true);
 		g.setName("G");
 		Creature c1 = new Creature(g);
 		c1.setName("c1");
 		Creature c2 = new Creature(g);
 		c2.setName("c2");
         db.commit();
 
 		TinkerSet<Creature> result = g.getBeing().<Creature, Creature> collect(new BodyExpressionEvaluator<Creature, Being>() {
 			@Override
 			public Creature evaluate(Being e) {
 				if (e instanceof Creature) {
 					return (Creature) e;
 				}
 				return null;
 			}
 		}).asSet();
 		Assert.assertEquals(2, result.size());
 
 		TinkerSet<Being> creatures = g.getBeing().select(new BooleanExpressionEvaluator<Being>() {
 			@Override
 			public Boolean evaluate(Being e) {
 				return e instanceof Creature;
 			}
 		});
 		Assert.assertEquals(2, creatures.size());
 	}
 
 	@Test
 	public void testLookup1() {
 		God g = new God(true);
 		g.setName("G");
 		Creature c1 = new Creature(g);
 		c1.setName("c1");
 		Creature c2 = new Creature(g);
 		c2.setName("c2");
 		Spook s1 = new Spook(g);
 		s1.setName("s1");
 		Spook s2 = new Spook(g);
 		s2.setName("s2");
 		c1.addToSpook(s1);
 		c2.addToSpook(s2);
 		Creature c3 = new Creature(g);
 		Spook s3 = new Spook(g);
         db.commit();
 
 		Assert.assertEquals(1, c1.lookupFor_creature_spook().size());
 		Assert.assertEquals(1, c2.lookupFor_creature_spook().size());
 		Assert.assertEquals(1, s1.lookupFor_spook_creature().size());
 		Assert.assertEquals(1, s2.lookupFor_spook_creature().size());
 	}
 
 	@Test
 	public void testLookupWithNonCompositeLookupInCompositeTree() {
 		God g = new God(true);
 		g.setName("G");
 		Nightmare n1 = new Nightmare(true);
 		n1.setName("n1");
 		g.addToNightmare(n1);
 		Nightmare n2 = new Nightmare(true);
 		n2.setName("n2");
 		g.addToNightmare(n2);
 		Nightmare n3 = new Nightmare(true);
 		n3.setName("n3");
 		g.addToNightmare(n3);
         db.commit();
 		Assert.assertEquals(3, g.lookupFor_godOfMemory_memory().size());
 	}
 
 	@Test
 	public void testUniqueManyLookup() {
 		God g = new God(true);
 		Level1 l1_0 = new Level1(g);
 		Level1 l1_1 = new Level1(g);
 		Level1 l1_2 = new Level1(g);
 
 		Level2 l2_0_0 = new Level2(l1_0);
 		Level2 l2_0_1 = new Level2(l1_0);
 		Level2 l2_0_2 = new Level2(l1_0);
 
 		Level2 l2_1_0 = new Level2(l1_1);
 		Level2 l2_1_1 = new Level2(l1_1);
 		Level2 l2_1_2 = new Level2(l1_1);
 
 		Level2 l2_2_0 = new Level2(l1_2);
 		Level2 l2_2_1 = new Level2(l1_2);
 		Level2 l2_2_2 = new Level2(l1_2);
 
 		Devil1 d1_0 = new Devil1(g);
 		Devil1 d1_1 = new Devil1(g);
 		Devil1 d1_2 = new Devil1(g);
 
 		Devil2 d2_0_0 = new Devil2(d1_0);
 		Devil2 d2_0_1 = new Devil2(d1_0);
 		Devil2 d2_0_2 = new Devil2(d1_0);
 
 		Devil2 d2_1_0 = new Devil2(d1_1);
 		Devil2 d2_1_1 = new Devil2(d1_1);
 		Devil2 d2_1_2 = new Devil2(d1_1);
 
 		Devil2 d2_2_0 = new Devil2(d1_2);
 		Devil2 d2_2_1 = new Devil2(d1_2);
 		Devil2 d2_2_2 = new Devil2(d1_2);
 
         db.commit();
 		Assert.assertEquals(9, l2_0_0.lookupFor_level2_devil2().size());
 
 		l2_0_0.addToDevil2(d2_0_0);
         db.commit();
 
 		Assert.assertEquals(8, l2_0_0.lookupFor_level2_devil2().size());
 	}
 
     @Test
     public void testLookupWithConstraint() {
         ConstraintRoot constraintRoot1 = new ConstraintRoot(true);
         constraintRoot1.setName("constraintRoot1");
         ConstraintChild1 constraintChild11 = new ConstraintChild1(constraintRoot1);
         constraintChild11.setName("constraintChild11");
         ConstraintChild1 constraintChild12 = new ConstraintChild1(constraintRoot1);
         constraintChild12.setName("constraintChild12");
         ConstraintChild1 constraintChild13 = new ConstraintChild1(constraintRoot1);
         constraintChild13.setName("constraintChild13");
         ConstraintChild1 constraintChild14 = new ConstraintChild1(constraintRoot1);
         constraintChild14.setName("constraintChild14");
 
         ConstraintChild2 constraintChild21 = new ConstraintChild2(constraintRoot1);
         constraintChild21.setName("constraintChild21");
         ConstraintChild2 constraintChild22 = new ConstraintChild2(constraintRoot1);
         constraintChild22.setName("constraintChild22");
         ConstraintChild2 constraintChild23 = new ConstraintChild2(constraintRoot1);
         constraintChild23.setName("constraintChild23");
         ConstraintChild2 constraintChild24 = new ConstraintChild2(constraintRoot1);
         constraintChild24.setName("constraintChild24");
 
         ConstraintRoot constraintRoot2 = new ConstraintRoot(true);
         constraintRoot1.setName("constraintRoot1");
         ConstraintChild2 constraintChild212 = new ConstraintChild2(constraintRoot2);
         constraintChild212.setName("constraintChild212");
         ConstraintChild2 constraintChild222 = new ConstraintChild2(constraintRoot2);
         constraintChild222.setName("constraintChild222");
         ConstraintChild2 constraintChild232 = new ConstraintChild2(constraintRoot2);
         constraintChild232.setName("constraintChild232");
         ConstraintChild2 constraintChild242 = new ConstraintChild2(constraintRoot2);
         constraintChild242.setName("constraintChild242");
 
         db.commit();
         Assert.assertEquals(4, constraintChild11.lookupFor_constraintChild1_constraintChild2().size());
         Assert.assertEquals(4, constraintChild21.lookupFor_constraintChild2_constraintChild1().size());
 
     }
 
 }
