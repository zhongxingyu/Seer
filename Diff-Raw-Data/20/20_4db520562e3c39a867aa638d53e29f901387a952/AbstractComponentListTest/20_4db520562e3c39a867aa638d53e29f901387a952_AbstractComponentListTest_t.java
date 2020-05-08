 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.techlogic.BaSliGC.util;
 
 import de.techlogic.BaSliGC.components.MainCharacter;
 import de.techlogic.BaSliGC.components.PlainImage;
 import de.techlogic.BaSliGC.decorated.Clickable;
 import de.techlogic.BaSliGC.decorated.Dragable;
 import de.techlogic.BaSliGC.util.gamecomponent.Character;
 import de.techlogic.BaSliGC.util.gamecomponent.GameComponent;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 /**
  *
  * @author Nils
  */
 public class AbstractComponentListTest {
 
     private AbstractComponentList list;
     private CollisionChecker checker;
     private PlainImage image;
 
     public AbstractComponentListTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
         checker = new CollisionChecker();
         list = new AbstractComponentList(checker) {
         };
         image = new PlainImage(null, 1, 1, 1, 1);
 
 
     }
 
     @After
     public void tearDown() {
         checker = null;
         list = null;
         image = null;
     }
 
     @Test
     public void testAddComponent() {
         list.addComponent(image);
         assertTrue("List size is smaller then 1", list.getSize() == 1);
         assertTrue("Image isn't contain in Component List", list.contains(image));
     }
 
     @Test
     public void testAddClickable() {
         Clickable click = new Clickable(image);
         list.addClickable(click);
         assertTrue("List size is smaller then 1", list.getSize() == 1);
         assertTrue("Image isn't contain in Component List", list.contains(click));
         assertTrue("Image isn't contain in Clickable List", list.containsClickable(click));
 
     }
 
     @Test
     public void testAddDragable() {
         Dragable drag = new Dragable(image);
         list.addDragable(drag);
         assertTrue("List size is smaller then 1", list.getSize() == 1);
         assertTrue("Image isn't contain in Component List", list.contains(drag));
         assertTrue("Image isn't contain in Clickable List", list.containsDragable(drag));
 
     }
 
     @Test
     public void testAddCharacter() {
        try {
            Character character = new MainCharacter(10f, 10f, new Image("res/Character.png"), new Image("res/Character.png"), new Image("res/Character.png"), new Image("res/Character.png"));
            list.addCharacter(character);
            assertTrue("List size is smaller then 1", list.getSize() == 1);
            assertTrue("Image isn't contain in Clickable List", list.containsCharacter(character));
        } catch (SlickException ex) {
            Logger.getLogger(AbstractComponentListTest.class.getName()).log(Level.SEVERE, null, ex);
        }
 
 
     }
 }
