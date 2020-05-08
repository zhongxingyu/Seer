 package ${package};
 
import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
import android.widget.TextView;
 
 @RunWith(org.robolectric.RobolectricTestRunner.class)
 public class MainActivityTest {
     
     @Test
    public void testHelloWorld() {
         MainActivity mainActivity = new MainActivity();
         mainActivity.onCreate(null);
        TextView helloTextView = (TextView) mainActivity
                .findViewById(R.id.helloWorld);
        Assert.assertEquals(helloTextView.getText().toString(), "Hello world!");
     }
 }

