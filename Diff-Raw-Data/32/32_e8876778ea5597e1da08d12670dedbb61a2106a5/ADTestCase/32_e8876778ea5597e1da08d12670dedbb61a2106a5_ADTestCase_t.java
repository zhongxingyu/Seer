 package generated.test120;
 
 import java.io.IOException;
 
 import org.caesarj.runtime.CaesarThread;
 
 import junit.framework.TestCase;
 
 /**
  * Test deployment of multiple objects on
  * inherited registries
  *
  * @author Vaidas Gasiunas
  */
 
 public class ADTestCase extends TestCase
 {
     public ADTestCase()
     {
         super("test");
     }
 


     public static StringBuffer result = new StringBuffer();
 
 	public String expectedResult =
		":cutDA-a:cutBA-a:cutCA-a:cutAA-a:A.A-1" +
    	":cutDA-a:cutDA-b:cutBA-a:cutBA-b:cutCA-a:cutCA-b:cutAA-a:cutAA-b:A.A-1" +
 
     	// 1st checkpoint -> thread A
 
    	":cutDA-c:cutBA-c:cutCA-c:cutAA-c:A.A-2" +
    	":cutDA-c:cutDA-d:cutBA-c:cutBA-d:cutCA-c:cutCA-d:cutAA-c:cutAA-d:A.A-2" +
 
     	// 2nd checkpoint -> main thread
 
    	":cutDA-a:cutDA-b:cutBA-a:cutBA-b:cutCA-a:cutCA-b:cutAA-a:cutAA-b:A.A-1" +
 
     	// 3rd checkpoint -> thread A
 
    	":cutDA-c:cutBA-c:cutCA-c:cutAA-c:A.A-2" +
     	":A.A-2" +
 
     	// 4th checkpoint -> main thread
 
    	":cutDA-a:cutDA-b:cutBA-a:cutBA-b:cutCA-a:cutCA-b:cutAA-a:cutAA-b:A.A-1" +
    	":cutDA-a:cutBA-a:cutCA-a:cutAA-a:A.A-1" +
     	":A.A-1";
 
     public void test()
     {
 		System.out.println("-------> ADTest 20: Deploy Multiple Objects: start");
 
         new DeployA().test();
 
         System.out.println(result);
         assertEquals(expectedResult, result.toString());
 
         System.out.println("-------> ADTest 20: end");
     }
 }
 
 public cclass DeployA
 {
     public void test()
     {
 		Runnable threadA = new ThreadA();
         new Thread(threadA).start();
 
 		OuterA oa = new OuterA().init("1");
 
 		AspectD a = (AspectD)new AspectD().init("a");
 		AspectD b = (AspectD)new AspectD().init("b");
 
 		deploy(a)
         {
 			oa.doA(); // a deployed
 
 			deploy(b)
 			{
 				oa.doA(); // a & b deployed
 
 				Barrier.getInstance().check(); // 1st checkpoint
 				Barrier.getInstance().check(); // 2nd checkpoint
 
 				oa.doA(); // a, b, c & d deployed
 
 				Barrier.getInstance().check(); // 3rd checkpoint
 				Barrier.getInstance().check(); // 4th checkpoint
 
 				oa.doA(); // a & b deployed
 			}
 
 			oa.doA(); // a deployed
         }
 
         oa.doA(); // none deployed
     }
 }
 
 public cclass ThreadA implements Runnable
 {
 	public void run()
 	{
 		OuterA oa = new OuterA().init("2");
 
 		AspectD c = (AspectD)new AspectD().init("c");
 		AspectD d = (AspectD)new AspectD().init("d");
 
 		Barrier.getInstance().check(); // 1st checkpoint
 
 		deploy(c)
 		{
 			oa.doA(); // a, b & c deployed
 
 			deploy(d)
 			{
 				oa.doA(); // a, b, c & d deployed
 
 				Barrier.getInstance().check(); // 2nd checkpoint
 				Barrier.getInstance().check(); // 3rd checkpoint
 			}
 
 			oa.doA(); // a, b & c deployed
 		}
 
 		oa.doA(); // a & b deployed
 
 		Barrier.getInstance().check(); // 4th checkpoint
 	}
 }
 
 public cclass AspectA
 {
 	protected String _id;
 
 	public AspectA init(String id)
 	{
 		_id = id;
 		return this;
 	}
 
 	pointcut cutAA() : call(* OuterA+.doA(..));
 
 	before() : cutAA()
 	{
 		ADTestCase.result.append(":cutAA-" + _id);
 	}
 }
 
 public cclass AspectB extends AspectA
 {
 	pointcut cutBA() : call(* OuterA+.doA(..));
 
 	before() : cutBA()
 	{
 		ADTestCase.result.append(":cutBA-" + _id);
 	}
 }
 
 public cclass AspectC extends AspectA
 {
 	pointcut cutCA() : call(* OuterA+.doA(..));
 
 	before() : cutCA()
 	{
 		ADTestCase.result.append(":cutCA-" + _id);
 	}
 }
 
 public cclass AspectD extends AspectB & AspectC
 {
 	pointcut cutDA() : call(* OuterA+.doA(..));
 
 	before() : cutDA()
 	{
 		ADTestCase.result.append(":cutDA-" + _id);
 	}
 }
 
 public cclass OuterA
 {
 	protected String _id;
 
 	public OuterA init(String id)
 	{
 		_id = id;
 		return this;
 	}
 
 	public void doA()
 	{
 		ADTestCase.result.append(":A.A-" + _id);
 	}
 }
 
