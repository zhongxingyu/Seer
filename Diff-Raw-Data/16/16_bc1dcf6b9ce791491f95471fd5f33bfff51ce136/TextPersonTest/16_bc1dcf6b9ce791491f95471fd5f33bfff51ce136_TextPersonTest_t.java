 package net.sf.laja.cdd.testgen;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 import static net.sf.laja.cdd.testgen.HairColor.BROWN;
 import static net.sf.laja.cdd.testgen.PersonCreator.createPerson;
 
 public class TextPersonTest {
 
     @Test
     public void ensureTwoInstancesFromDifferentContextIsEqual() {
         PersonCreator creator = createPerson().name("Carl").hairColor(BROWN).children().defaults();
 
        Person person = creator.asPerson();
         TextPerson textPerson = creator.asTextPerson();
 
        assertEquals(textPerson, person);
     }
 }
