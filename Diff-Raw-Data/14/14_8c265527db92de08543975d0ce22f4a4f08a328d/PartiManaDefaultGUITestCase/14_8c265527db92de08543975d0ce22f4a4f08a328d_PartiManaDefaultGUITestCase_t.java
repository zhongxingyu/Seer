 package com.github.croesch.partimana;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.util.ArrayDeque;
 import java.util.Arrays;
 import java.util.Queue;
 
 import org.fest.swing.junit.testcase.FestSwingJUnitTestCase;
 
 import com.github.croesch.partimana.actions.ActionObserver;
 import com.github.croesch.partimana.actions.UserAction;
 
 /**
  * Default gui test case for the partimana program.
  * 
  * @author croesch
  * @since Date: Jul 11, 2011
  */
 public abstract class PartiManaDefaultGUITestCase extends FestSwingJUnitTestCase implements ActionObserver {
 
   private final Queue<UserAction> actions = new ArrayDeque<UserAction>();
 
   protected static Queue<Integer> pIds = new ArrayDeque<Integer>(Arrays.asList(999, 9999));
 
   @Override
   protected final void onSetUp() {
     this.actions.clear();
     before();
   }
 
   protected abstract void before();
 
   @Override
   protected final void onTearDown() {
    assertNoActionPerformed();
 
     try {
       after();
     } finally {
       super.onTearDown();
     }
   }
 
   protected void after() {};
 
   @Override
   public final void performAction(final UserAction action) {
     this.actions.add(action);
   }
 
   protected UserAction poll() {
     return this.actions.poll();
   }
 
   protected void assertNoActionPerformed() {
     assertThat(this.actions).isEmpty();
   }
 }
