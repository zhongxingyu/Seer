 package net.sf.laja.cdd.behaviour;
 
 import org.joda.time.DateMidnight;
 import org.junit.Test;
 
 import static net.sf.laja.cdd.AddressCreator.buildAddress;
 import static net.sf.laja.cdd.PersonCreator.*;
import static net.sf.laja.cdd.state.AddressState.IllegalAddressStateStreetNameIsNullException;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class PersonTest {
 
     @Test
    public void shouldBePossibleToBuildIllegalMutableState() {
        buildPerson().getMutableState();
    }

    @Test(expected = IllegalAddressStateStreetNameIsNullException.class)
    public void shouldNotBePossibleToBuildIllegalImmutableState() {
        buildPerson().getState();
    }

    @Test
     public void hasShortName() {
         assertThat(defaultPerson().withName("Carl").asPerson().hasLongName(), is(false));
     }
 
     @Test
     public void hasLongName() {
         assertThat(defaultPerson().withName("Angelique").asPerson().hasLongName(), is(true));
     }
 
     @Test
     public void preserveHandWrittenChainedAttributes() {
         createPerson().name("Carl").birthday(1991,2,3).children().defaultAddress();
     }
 
     private PersonBuilder defaultPerson() {
         PersonBuilder builder = buildPerson()
                 .withBirthday(new DateMidnight(1999, 9, 9))
                 .withAddress(buildAddress().withCity("Stockholm").withStreetName("First street"));
         return builder;
     }
 }
