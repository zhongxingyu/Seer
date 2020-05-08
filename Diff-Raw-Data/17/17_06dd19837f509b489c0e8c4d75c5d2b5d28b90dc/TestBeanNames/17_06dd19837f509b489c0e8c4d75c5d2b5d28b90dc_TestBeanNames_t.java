 package testcases.beanNames;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.testng.annotations.Test;
 
 import de.jpaw.bonaparte.core.BonaPortable;
 import de.jpaw.bonaparte.pojos.beanNames.BeanAndSimpleNames;
 import de.jpaw.bonaparte.pojos.beanNames.OnlyBeanNames;
 import de.jpaw.bonaparte.pojos.beanNames.OnlySimpleNames;
 
 @Test
 public class TestBeanNames {
     static private final List<String> ALL_GETTERS = Arrays.asList("getUrl", "geturl", "getmTimestamp", "getMTimestamp");
     
     private void testMethods(BonaPortable x, String prefix, List<String> names, List<Boolean> included) throws Exception {
         assert(names.size() == included.size());  // parameter plausi
         
         Method[] arrayOfMethods = x.getClass().getDeclaredMethods();
        List<String> listOfMethods = new ArrayList<String>(20);
         
         for (int i = 0; i < arrayOfMethods.length; ++i) {
             if (arrayOfMethods[i].getName().startsWith(prefix))
                 listOfMethods.add(arrayOfMethods[i].getName());
         }
         for (int i = 0; i < names.size(); ++i) {
             boolean found = listOfMethods.contains(names.get(i));
             if (found != included.get(i).booleanValue()) {
                 throw new Exception(x.getClass().getSimpleName() + ": mismatch for " + names.get(i) + ": found = " + found);
             }
         }
     }
     
     public void testBeanAndSimpleNames() throws Exception {
         testMethods(new BeanAndSimpleNames(), "get", ALL_GETTERS, Arrays.asList(true, false, true, true));
     }
     public void testBeanNames() throws Exception {
         testMethods(new OnlyBeanNames(), "get", ALL_GETTERS, Arrays.asList(true, false, true, false));
     }
     public void testSimpleNames() throws Exception {
         testMethods(new OnlySimpleNames(), "get", ALL_GETTERS, Arrays.asList(true, false, false, true));
     }
 }
