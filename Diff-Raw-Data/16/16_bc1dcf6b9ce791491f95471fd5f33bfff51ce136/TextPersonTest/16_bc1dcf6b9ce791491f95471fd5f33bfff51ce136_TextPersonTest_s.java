 package net.sf.laja.cdd.testgen;
 
import net.sf.laja.cdd.testgen.PersonCreator;
import net.sf.laja.cdd.testgen.SpecialPerson;
import net.sf.laja.cdd.testgen.TextPerson;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 import static net.sf.laja.cdd.testgen.HairColor.BROWN;
 import static net.sf.laja.cdd.testgen.PersonCreator.createPerson;
 
 public class TextPersonTest {
 
     @Test
     public void ensureTwoInstancesFromDifferentContextIsEqual() {
         PersonCreator creator = createPerson().name("Carl").hairColor(BROWN).children().defaults();
 
         TextPerson textPerson = creator.asTextPerson();
        SpecialPerson specialPerson = creator.asSpecialPerson();
 
        assertEquals(textPerson, specialPerson);
     }
 }
