 package guavadojo;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.junit.Test;

public class PreconditionTest {


    public void notEmptyParamMethod(String param1) {
        Preconditions.checkArgument(Strings.isNullOrEmpty(param1), "param1 is empty");
    }


    @Test(expected = IllegalArgumentException.class)
    public void should_throw_illegalArgumentException_when_param1_empty(){
        notEmptyParamMethod("");
    }

 
 
 
 }
