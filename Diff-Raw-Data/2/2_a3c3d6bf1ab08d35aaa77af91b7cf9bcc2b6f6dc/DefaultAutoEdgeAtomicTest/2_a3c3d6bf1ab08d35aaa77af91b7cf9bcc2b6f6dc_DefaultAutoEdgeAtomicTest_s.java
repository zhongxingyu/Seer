 package au.net.netstorm.boost.nursery.autoedge;
 
 import java.io.InputStream;
 import java.lang.reflect.Method;
 
 import au.net.netstorm.boost.edge.java.lang.EdgeClass;
 import au.net.netstorm.boost.edge.java.lang.reflect.EdgeMethod;
 import au.net.netstorm.boost.nursery.autoedge.testfixtures.EdgeStreamFixture;
 import au.net.netstorm.boost.sniper.core.LifecycleTestCase;
 import au.net.netstorm.boost.sniper.marker.HasFixtures;
 import au.net.netstorm.boost.sniper.marker.InjectableSubject;
 import au.net.netstorm.boost.sniper.marker.InjectableTest;
 import au.net.netstorm.boost.sniper.marker.LazyFields;
 
 public final class DefaultAutoEdgeAtomicTest extends LifecycleTestCase implements HasFixtures, InjectableTest, InjectableSubject, LazyFields {
     private AutoEdge subject;
     private Method unedge;
     private Method toString;
     EdgeStreamFixture fixture;
 
     MethodWarp warperMock;
     EdgeMethod invokerMock;
     Unedger unedgerMock;
     EdgeClass classer;
 
     public void setUpFixtures() {
         subject = new DefaultAutoEdge(InputStream.class, fixture.stream());
         unedge = classer.getDeclaredMethod(Edge.class, "unedge");
         toString = classer.getDeclaredMethod(Object.class, "toString");
     }
 
     public void testInvoke() {
         byte[] result = new byte[fixture.length()];
         Object[] args = {result};
         expect.oneCall(warperMock, fixture.trg(), "warp", InputStream.class, fixture.src());
         expect.oneCall(invokerMock, fixture.length(), "invoke", fixture.trg(), fixture.stream(), args);
         expect.oneCall(unedgerMock, args, "unedge", new Object[] {args});
         Object length = subject.invoke(fixture.stream(), fixture.src(), args);
         assertEquals(fixture.length(), length);
     }
 
     public void testInvokeNoArgs() {
         String expected = "result";
         expect.oneCall(warperMock, toString, "warp", InputStream.class, toString);
         expect.oneCall(invokerMock, expected, "invoke", toString, fixture.stream(), null);
         expect.oneCall(unedgerMock, VOID, "unedge", (Object) null);
         Object result = subject.invoke(fixture.stream(), toString, null);
         assertEquals(expected, result);
     }
 
     public void testInvokeUnedge() {
         Object[] args = {};
         Object result = subject.invoke(null, unedge, args);
         assertEquals(true, result instanceof InputStream);
         assertSame(fixture.stream(), result);
     }
 }
