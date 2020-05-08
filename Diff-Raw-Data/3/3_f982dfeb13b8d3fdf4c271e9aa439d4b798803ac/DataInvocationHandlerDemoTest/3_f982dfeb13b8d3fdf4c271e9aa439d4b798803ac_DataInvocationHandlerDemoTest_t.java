 package au.net.netstorm.boost.nursery.proxy;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Proxy;
 import au.net.netstorm.boost.test.automock.HasFixtures;
 import au.net.netstorm.boost.test.automock.InteractionTestCase;
 import au.net.netstorm.boost.util.introspect.DefaultFieldValueSpec;
 import au.net.netstorm.boost.util.introspect.FieldValueSpec;
 import au.net.netstorm.boost.util.type.DefaultInterface;
 import au.net.netstorm.boost.util.type.Interface;
 
 public class DataInvocationHandlerDemoTest extends InteractionTestCase implements HasFixtures {
     private static final String NUM_LEGS = "numLegs";
     private static final String NAME = "name";
     private Centipede critter1;
     private Centipede critter2;
     private DefaultFieldValueSpec legsSpec;
     private DefaultFieldValueSpec nameSpec;
     String expectedName = null;
     Integer expectedLegs = null;
     RealCentipede realCritter = new RealCentipede("dollar", 1000);
     private static final int NUM_LOOPS = 10000000;
 
     public void setUpFixtures() {
         legsSpec = new DefaultFieldValueSpec(NUM_LEGS, expectedLegs);
         nameSpec = new DefaultFieldValueSpec(NAME, expectedName);
         critter1 = proxy(nameSpec, legsSpec);
         critter2 = proxy(nameSpec, legsSpec);
     }
 
     // FIX DATAPROXY 2130 Get this working
 //    public void testMissingFieldsThrowsException() {
 //        FieldValueSpec[] fields = new FieldValueSpec[0];
 //        checkThrowsWithMessage(Centipede.class, fields, "No field supplied for method ");
 //    }
 
     public void testExtraFieldsThrowsException() {
         FieldValueSpec extra = new DefaultFieldValueSpec("extra", "extra");
         FieldValueSpec[] fields = {legsSpec, nameSpec, extra};
         checkThrowsWithMessage(Centipede.class, fields, "Number of methods(2) and fields(3) differ");
     }
 
     // FIX DATAPROXY 2130 Get this working
 //    public void testWrongTypeThrowsException() {
 //        FieldValueSpec wrong = new DefaultFieldValueSpec(NUM_LEGS, "should be an int");
 //        FieldValueSpec[] fields = {wrong, nameSpec};
 //        checkThrowsWithMessage(Centipede.class, fields, "No field supplied for method ");
 //    }
 
     // FIX DATAPROXY 2130 Get this working
 //    public void testPrimitives() {
 //        FieldValueSpec[] fields = {new DefaultFieldValueSpec("someInt", new Integer(1))};
 //        checkThrowsWithMessage(PrimativeTestInterface.class, fields, "Primitive return types not supported");
 //    }
 
     public void testGetStuff() {
         int actualLegs = critter1.numLegs().intValue();
         assertEquals(expectedLegs, actualLegs);
         String actualName = critter1.name();
         assertEquals(expectedName, actualName);
     }
 
     // FIX DATAPROXY 2130 Get this working
 //    public void testObjectStuff() {
 //        assertEquals(critter1.hashCode(), critter2.hashCode());
 //        assertEquals(critter1, critter2);
 //        assertEquals(critter1.toString(), critter2.toString());
 //    }
 
     public void testPerformance() {
         long proxyTime = loop(NUM_LOOPS, critter1);
         long realTime = loop(NUM_LOOPS, realCritter);
        // FIX DATAPROXY 2130 Is this an appropriate ratio? (NOTE: 3x doesn't pass on geekscape.)
        assertEquals(true, proxyTime < 4 * realTime);
     }
 
     private void checkThrowsWithMessage(Class iFace, FieldValueSpec[] fields, String messageStart) {
         try {
             createProxy(iFace, fields);
             fail();
         } catch (IllegalArgumentException actual) {
             String message = actual.getMessage();
             assertEquals(true, message.startsWith(messageStart));
         }
     }
 
     private long loop(int numLoops, Centipede centipede) {
         long start = System.currentTimeMillis();
         for (int i = 0; i < numLoops; i++) {
             centipede.name();
         }
         return System.currentTimeMillis() - start;
     }
 
     private Centipede proxy(FieldValueSpec name, FieldValueSpec legs) {
         FieldValueSpec[] fields = new FieldValueSpec[]{name, legs};
         return createProxy(Centipede.class, fields);
     }
 
     private Centipede createProxy(Class type, FieldValueSpec[] fields) {
         InvocationHandler handler = createHandler(type, fields);
         ClassLoader classLoader = getClass().getClassLoader();
         Class[] proxyClasses = new Class[]{type};
         return (Centipede) Proxy.newProxyInstance(classLoader, proxyClasses, handler);
     }
 
     private InvocationHandler createHandler(Class type, FieldValueSpec[] fields) {
         Interface iFace = new DefaultInterface(type);
         return new DataInvocationHandler(iFace, fields);
     }
 
     private interface PrimativeTestInterface {
         int someInt();
     }
 }
