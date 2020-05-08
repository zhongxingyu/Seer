 package see.functions;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.MutableClassToInstanceMap;
 import org.junit.Test;
 import see.evaluation.Context;
 import see.evaluation.evaluators.SimpleContext;
 import see.evaluation.scopes.Scopes;
 import see.functions.service.IsDefined;
 
 import java.util.Map;
 
 import static com.google.common.collect.ImmutableList.of;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class IsDefinedTest {
     private IsDefined isDefined = new IsDefined();
 
    @Test
     public void testOnStaticContext() throws Exception {
           Map<String, Object> contents = ImmutableMap.<String, Object>of("c", 9);
           Context context = fromMutable(contents);
 
           assertTrue(isDefined.apply(context).apply(of("c")));
           assertFalse(isDefined.apply(context).apply(of("a")));
     }
 
     private Context fromMutable(Map<String, ?> contents) {
         return SimpleContext.create(Scopes.fromMap(contents), MutableClassToInstanceMap.create());
     }
 
 }
