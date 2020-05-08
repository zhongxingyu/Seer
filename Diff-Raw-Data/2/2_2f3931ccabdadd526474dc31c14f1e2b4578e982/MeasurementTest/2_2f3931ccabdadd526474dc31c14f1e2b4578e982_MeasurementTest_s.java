 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ThoughtWorks
  * Date: 8/14/13
  * Time: 7:19 AM
  * To change this template use File | Settings | File Templates.
  */
 public class MeasurementTest {
     @Test
     public void shouldCompareSameUnit() {
         assertThat(new Measurement(2, Unit.FOOT), is(new Measurement(2, Unit.FOOT)));
     }
 
     @Test
     public void shouldCompareDifferentUnits() {
         assertThat(new Measurement(2, Unit.FOOT), is(new Measurement(24, Unit.INCH)));
         assertThat(new Measurement(1, Unit.YARD), is(new Measurement(36, Unit.INCH)));
         Measurement meas1 = new Measurement(2, Unit.YARD);
        Measurement meas2 = new Measurement(71, Unit.INCH);
         assertThat("This one failed because " + meas1.getBaseUnits() + " is not " + meas2.getBaseUnits(), meas1, is(meas2));
     }
 
 
 }
