 package org.test.restlet.ocl;
 
 import org.junit.Test;
 import org.test.restlet.TestRestletDefaultDataCreator;
 import org.tuml.root.Root;
 import org.tuml.runtime.collection.TinkerBag;
 import org.tuml.runtime.collection.TinkerOrderedSet;
 import org.tuml.runtime.collection.ocl.BodyExpressionEvaluator;
 import org.tuml.runtime.test.BaseLocalDbTest;
 import org.tuml.test.Finger;
 import org.tuml.test.Hand;
 import org.tuml.test.Human;
 import org.tuml.test.Ring;
 
 /**
  * Date: 2013/02/13
  * Time: 1:21 PM
  */
 public class TestOcl extends BaseLocalDbTest  {
 
     @Test
     public void testocl() {
         TestRestletDefaultDataCreator testRestletDefaultDataCreator = new TestRestletDefaultDataCreator();
         testRestletDefaultDataCreator.createData();
         Human human = Root.INSTANCE.getHuman().get(0);
         TinkerBag<Human> humans = execute(human);
         System.out.println(humans.size());
 
     }
 
     private TinkerBag<Human> execute(Human human) {
         TinkerBag<Human> result = human.getHand().<Finger, TinkerOrderedSet<Finger>>collect(new BodyExpressionEvaluator<TinkerOrderedSet<Finger>, Hand>() {
             @Override
             public TinkerOrderedSet<Finger> evaluate(Hand temp1) {
                 return temp1.getFinger();
             }
         }).<Ring, Ring>collect(new BodyExpressionEvaluator<Ring, Finger>() {
             @Override
             public Ring evaluate(Finger temp2) {
                 return temp2.getRing();
             }
         }).<Human, Human>collect(new BodyExpressionEvaluator<Human, Ring>() {
             @Override
             public Human evaluate(Ring temp3) {
                 return temp3.getHuman();
             }
         });
         return result;
     }
 }
