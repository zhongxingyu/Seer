 package au.net.netstorm.boost.demo.spider.resolve;
 
 public final class BlueprintDemoTest extends ResolverDemooooTest {
     {
         registry.multiple(Town.class, Weipa.class);
         registry.single(FlyingDoctor.class, FnqFlyingDoctor.class);
     }
 
     public void testSingle() {
         Town t1 = resolveTown();
         Town t2 = resolveTown();
         checkDifferent(t1, t2);
         FlyingDoctor d1 = getDoctor(t1);
         FlyingDoctor d2 = getDoctor(t2);
         checkSame(d1, d2);
     }
 
     private Town resolveTown() {
         return resolver.resolve(Town.class);
     }
 
     private FlyingDoctor getDoctor(Town t1) {
         return (FlyingDoctor) grapher.get(t1, "doctor");
     }
 
     private void checkSame(Object o1, Object o2) {
         assertNotNull(o1);
         assertEquals(true, o1 == o2);
     }
 
     private void checkDifferent(Object o1, Object o2) {
         assertNotNull(o1);
         assertNotNull(o2);
         assertEquals(true, o1 != o2);
     }
 }
