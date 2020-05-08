 import org.junit.*;
 import java.util.*;
 import play.test.*;
 import models.Line;
 
 import play.modules.morphia.Blob;
 import play.modules.morphia.MorphiaPlugin;
 
 public class LineTest extends UnitTest {
 	@Test
   public void aVeryImportantThingToTest() {
     assertEquals(2, 1 + 1);
   }
 
   @Test
   public void testStoreLine(){
   	Line l = new Line(39, "derp@derp.com", 12, "test", 100.1, -1, "income", 0);
   	l.save();
   	l.delete();
   }
 
   @Test
   public void testGetSublines(){
   	Line line = new Line(39, "derp@derp.com", 11, "test", 100.1, 0, "income", 0);
   	line.save();
  	long id = line.getId();
   	System.out.println(id);
   	Line subline = new Line(39, "colby@colby.com", 11, "test2", 200.1, id, "income", 1);
   	subline.save();
 
   	List<Line> sublines = line.getSublines(id);
   	System.out.println(sublines);
 
   	Line result = sublines.get(0);
   	assertEquals(result, subline);
 
   	subline.delete();
   	line.delete();
   }
 }
