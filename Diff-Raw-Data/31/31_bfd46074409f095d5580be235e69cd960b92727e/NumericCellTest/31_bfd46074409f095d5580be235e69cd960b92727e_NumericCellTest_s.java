 package org.oddjob.dido.poi.layouts;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 
 import junit.framework.TestCase;
 
 import org.apache.log4j.Logger;
 import org.oddjob.arooa.ArooaBeanDescriptor;
 import org.oddjob.arooa.ConfiguredHow;
 import org.oddjob.arooa.life.SimpleArooaClass;
 import org.oddjob.arooa.reflect.ArooaClass;
 import org.oddjob.arooa.reflect.BeanOverview;
 import org.oddjob.arooa.reflect.PropertyAccessor;
 import org.oddjob.arooa.standard.StandardArooaSession;
 import org.oddjob.dido.DataException;
 import org.oddjob.dido.DataReader;
 import org.oddjob.dido.DataWriter;
 import org.oddjob.dido.ValueNode;
 import org.oddjob.dido.bio.DirectBinding;
 import org.oddjob.dido.poi.data.PoiWorkbook;
 
 public class NumericCellTest extends TestCase {
 	private static final Logger logger = Logger.getLogger(NumericCellTest.class);
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		logger.info("-------------------------   " + getName()  + 
 				"   ----------------------");
 	}
 	
 	public void testReadWrite() throws DataException {
 		
 		PoiWorkbook workbook = new PoiWorkbook();
 		
 		NumericCell test = new NumericCell();
 		test.setArooaSession(new StandardArooaSession());
 		test.bind(new DirectBinding());
 
 		DataRows rows = new DataRows();
 		rows.setOf(0, test);
 		
 		DataBook book = new DataBook();
 		book.setOf(0, rows);
 		
 		DataWriter writer = book.writerFor(workbook); 
 		
 		writer.write(12.3);
 		
 		assertEquals(1, test.getIndex());
 
 		writer.close();
 		
 		// Read side.
 		
 		rows.reset();
 		
 		DataReader reader = book.readerFor(workbook);
 
 		Object result = reader.read();
 		
 		assertEquals(new Double(12.3), result);
 		
 		assertNull(reader.read());
 		
 		reader.close();
 	}
 	
 	public static class ParameterisedBean<T> {
 		
 		public T getFoo() {
 			return null;
 		}
 		
 		public void setFoo(T foo) {
 			
 		}
 	}
 	
 	public static class StringBean extends ParameterisedBean<String> {
 		
 		public String getFoo() {
 			return null;
 		}
 		
 		public void setFoo(String foo) {
 			
 		}
 	}
 	
 	/**
 	 * Tracking down a really weird problem where tests from Ant failed to
 	 * recognise a that value was an attribute but test from eclipse worked
 	 * fine.
 	 * <p>
 	 * All to do with a Jave bug. See {@link ValueNode}.
 	 * 
 	 * @throws IntrospectionException 
 	 */
 	public void testValueConfiguredHow() throws IntrospectionException {
 		
		logger.info("java.version=" + System.getProperty("java.version"));
 		logger.info("java.specification.version=" + System.getProperty("java.specification.version"));
 		logger.info("java.vm.version=" + System.getProperty("java.vm.version"));
 		logger.info("java.home=" + System.getProperty("java.home"));
 		
 		BeanInfo beanInfo = Introspector.getBeanInfo(StringBean.class);
 		PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
 
 		for (PropertyDescriptor descriptor : descriptors) {
 			logger.info(descriptor.getPropertyType() + " " + descriptor.getName());
 		}
 		
 		StandardArooaSession session = new StandardArooaSession();
 		
 		PropertyAccessor accessor = 
 			session.getTools().getPropertyAccessor();
 		
 		ArooaClass arooaClass = new SimpleArooaClass(StringBean.class);
 		
 		BeanOverview overview = arooaClass.getBeanOverview(accessor);
 		
		// Would expect String!
		assertEquals(Object.class, overview.getPropertyType("foo"));
		
 		ArooaBeanDescriptor descriptor = 			
			session.getArooaDescriptor().getBeanDescriptor(
				arooaClass, accessor);
		
		// Would expect Attribute!
		assertEquals(ConfiguredHow.ELEMENT, 
				descriptor.getConfiguredHow("foo"));
 	}
 }
