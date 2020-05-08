 package org.hibernate.kmet.handlerequalstest;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.hibernate.engine.jdbc.internal.JdbcResourceRegistryImpl;
 import org.hibernate.engine.jdbc.internal.proxy.AbstractProxyHandler;
 import org.hibernate.engine.jdbc.internal.proxy.ProxyBuilder;
 import org.junit.Assert;
 import org.junit.Test;
 
 import static org.mockito.Mockito.*;
 
 public class HandlerEqualsTest {
 	
 	public class ProxyHandler extends AbstractProxyHandler {
 
 		public ProxyHandler(int hashCode) {
 			super(hashCode);
 		}
 
 		@Override
 		protected Object continueInvocation(Object proxy, Method method,
 				Object[] args) throws Throwable {
 			//NOP
 			return null;
 		}
 		
 	}
 	
 	public interface StubInterface {
 	}
 	
 	@Test
 	public void equalsReflexiveTest() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
 		
 		@SuppressWarnings("unchecked")
 		Class<StubInterface> proxyClass = (Class<StubInterface>) Proxy.getProxyClass(this.getClass().getClassLoader(),  
 				new Class<?>[] { StubInterface.class });
 
 		StubInterface proxy = proxyClass.getConstructor(InvocationHandler.class).newInstance(new ProxyHandler(1));
 		
 		Assert.assertTrue(proxy.equals(proxy));
 	}
 	
 	
 	
 	//ATTENTION: this test fails only when run on IBM JDK 
 	@Test
	public void releasedButNotRemoved() throws Exception {
 		Statement baseStatemet = mock(Statement.class);
 		Statement proxyedStatement = ProxyBuilder.buildImplicitStatement(baseStatemet, null, null);
 		
 		JdbcResourceRegistryImpl jdbcResourceRegistryImpl = new JdbcResourceRegistryImpl(null);
 		
 		jdbcResourceRegistryImpl.register(proxyedStatement);
 		jdbcResourceRegistryImpl.release(proxyedStatement);
 		Field xrefField = JdbcResourceRegistryImpl.class.getDeclaredField("xref");
 		xrefField.setAccessible(true);
 		
 		@SuppressWarnings("unchecked")
 		Map<Statement,Set<ResultSet>> xref = (Map<Statement, Set<ResultSet>>) xrefField.get(jdbcResourceRegistryImpl);
 		Assert.assertTrue(xref.isEmpty());
 	}
 	
 	
 	
 
 }
