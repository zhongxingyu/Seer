 package net.bensdeals.utils;
 
import net.bensdeals.model.Deal;
 import net.bensdeals.support.RobolectricTestRunnerWithInjection;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
import java.util.List;

 import static com.pivotallabs.robolectricgem.expect.Expect.expect;
import static net.bensdeals.utils.StringUtil.responseAsStream;
 
 @RunWith(RobolectricTestRunnerWithInjection.class)
 public class StringUtilTest {
 
     @Test
     public void shouldReadStreamToString() throws Exception {
        List<Deal> deals = Deal.parseXml(responseAsStream("homepage"));
        expect(deals.size()).toEqual(20);
     }
 }
