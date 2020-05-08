 package ch.unibe.iam.scg.bootstrap;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.TimeZone;
 
 import javassist.ClassPool;
 
 import ch.unibe.iam.scg.ContextClassLoader;
 
 public class RunJetty {
 	ClassPool cp = ClassPool.getDefault();
 	
 	public static void main(String[] args) throws Exception {
 		new RunJetty().testServer();
 	}
 	
 	public void testServer() throws Exception
 	{
		//org.mortbay.jetty.Main.main(new String[]{ "9000", "/Users/ewernli/Downloads/" } );
 		
 		ContextClassLoader loader = new ContextClassLoader("$$1");
 		Class clazz = loader.loadClass( "ch.unibe.iam.scg.test.core.org.mortbay.jetty.Main$$1" );
 		Object server = clazz.newInstance();
		invoke1( server, "main", String[].class, new String[]{ "9000", "/Users/ewernli/Downloads/"  }); 
 	}
 	
 	private Object invoke( Object receiver, String method ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
 	{
 		Class clazz = receiver.getClass();
 		Method meth = clazz.getMethod( method, new Class[0] );
 		return meth.invoke( receiver, new Object[0] );
 	}
 	
 	private Object invoke1( Object receiver, String method, Class c1, Object p1 ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
 	{
 		Class clazz = receiver.getClass();
 		Method meth = clazz.getMethod( method, new Class[ ] { c1 } );
 		return meth.invoke( receiver, new Object[]{ p1 } );
 	}
 }
