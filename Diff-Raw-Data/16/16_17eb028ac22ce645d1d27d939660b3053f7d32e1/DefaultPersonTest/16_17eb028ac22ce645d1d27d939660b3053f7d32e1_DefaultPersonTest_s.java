 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Map;
 import java.util.TreeMap;
 
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

 public class DefaultPersonTest {
 
     private TreeMap treeMap;
 
    @Before
    public void setUp() throws Exception {
         final DefaultPerson defaultPerson1 = new DefaultPerson("Lily", 20, 3, 8976);
         final DefaultPerson defaultPerson2 = new DefaultPerson("Lily", 20, 3, 8976);
         final DefaultPerson defaultPerson3 = new DefaultPerson("Lily", 20, 3, 8976);
         final DefaultPerson defaultPerson4 = new DefaultPerson("Lily", 20, 3, 8976);
         treeMap = new TreeMap(){{
             put(defaultPerson1,"1");
             put(defaultPerson2,"2");
             put(defaultPerson3,"3");
             put(defaultPerson4,"4");
         }};
    }

    @Test
    public void should_return() throws Exception {
         Map.Entry entry = treeMap.firstEntry();

        assertThat((String)entry.getValue(),is("1"));
     }
 
 }
