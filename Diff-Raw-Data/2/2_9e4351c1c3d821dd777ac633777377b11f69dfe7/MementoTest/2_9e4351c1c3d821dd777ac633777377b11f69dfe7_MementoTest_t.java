 package toolbox.design.patterns.behavioral.memento;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 
 import junit.framework.TestCase;
 
 /**
 * Unit test for the Memento behavioral design pattern. Acts also as the pattern's Caretaker role.
  *
  * @author billy
  *
  */
 public class MementoTest extends TestCase {
 
     /**
      * @throws java.lang.Exception
      */
     @Override
     @Before
     public void setUp() throws Exception {
     }
 
     /**
      * @throws java.lang.Exception
      */
     @Override
     @After
     public void tearDown() throws Exception {
     }
 
     public void testIt() {
         List<Editor.EditorState> savedStates = new ArrayList<Editor.EditorState>();
 
         Editor editor = new Editor();
 
         editor.setState("State 1");
         assertEquals("State 1", editor.getState());
 //        System.out.println("Editor state: " + editor.getState());
 
         editor.setState("State 2");
         assertEquals("State 2", editor.getState());
 //        System.out.println("Editor state: " + editor.getState());
 
         savedStates.add(editor.saveToMemento());
         assertEquals("State 2", editor.getState());
 //        System.out.println("Editor state: " + editor.getState());
 
         editor.setState("State 3");
         assertEquals("State 3", editor.getState());
 //        System.out.println("Editor state: " + editor.getState());
 
         savedStates.add(editor.saveToMemento());
         assertEquals("State 3", editor.getState());
 //        System.out.println("Editor state: " + editor.getState());
 
         editor.setState("State 4");
         assertEquals("State 4", editor.getState());
 //        System.out.println("Editor state: " + editor.getState());
 
         editor.restoreFromMemento(savedStates.get(1));
         assertEquals("State 3", editor.getState());
 //        System.out.println("Editor state: " + editor.getState());
     }
 }
