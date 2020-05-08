 package guavadojo;
 
import org.junit.Test;
 
import com.google.common.base.Preconditions;
 
public class PreconditionTest {
  
  @Test(expected = IllegalArgumentException.class)
  public void should_throw_illegalArgumentException_when_param1_is_not_valid() {
    int param = 1;
    Preconditions.checkArgument(param == 2, "param1 should be equal to 2");
  }
  
 }
