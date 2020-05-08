 package br.com.wobr.reports.jasper;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import net.sf.jasperreports.engine.JRField;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import br.com.wobr.reports.model.Foo;
 import br.com.wobr.reports.model.FooRelated;
import br.com.wobr.unittest.rules.TemporaryEditingContextProvider;
 
 import com.webobjects.eocontrol.EOEditingContext;
 import com.webobjects.eocontrol.EOEnterpriseObject;
 import com.webobjects.foundation.NSArray;
 import com.webobjects.foundation.NSKeyValueCoding;
 
 /**
  * @author <a href="mailto:hprange@gmail.com">Henrique Prange</a>
  */
 @RunWith( MockitoJUnitRunner.class )
 public class TestJasperEnterpriseObjectDataSource
 {
 	@Rule
 	public final TemporaryEditingContextProvider editingContextProvider = new TemporaryEditingContextProvider( "Sample" );
 
 	protected Foo mockEO;
 
 	@Mock
 	protected JRField mockField;
 
 	protected JasperEnterpriseObjectDataSource dataSource;
 
 	private EOEditingContext editingContext;
 
 	@Test( expected = NSKeyValueCoding.UnknownKeyException.class )
 	public void exceptionIfCannotFindTheKey() throws Exception
 	{
 		Mockito.when( mockField.getName() ).thenReturn( "invalidKey" );
 
 		dataSource.next();
 		dataSource.getFieldValue( mockField );
 	}
 
 	@Test( expected = IllegalArgumentException.class )
 	public void exceptionIfNullNSArrayProvided()
 	{
 		new JasperEnterpriseObjectDataSource( (NSArray<EOEnterpriseObject>) null );
 	}
 
 	@Test
 	public void getFieldValueForKey() throws Exception
 	{
 		mockEO.setBar( "result" );
 
 		Mockito.when( mockField.getName() ).thenReturn( "bar" );
 
 		dataSource.next();
 
 		Object result = dataSource.getFieldValue( mockField );
 
 		assertThat( (String) result, is( "result" ) );
 	}
 
 	@Test
 	public void getFieldValueForRelationship() throws Exception
 	{
 		FooRelated mockRelatedObject = FooRelated.createFooRelated( editingContext );
 
 		mockRelatedObject.setBar( "expected result" );
 
 		mockEO.setRelatedRelationship( mockRelatedObject );
 
 		Mockito.when( mockField.getName() ).thenReturn( "related.bar" );
 
 		dataSource.next();
 
 		String result = (String) dataSource.getFieldValue( mockField );
 
 		assertThat( result, is( "expected result" ) );
 	}
 
 	@Test
 	public void iterateOverMultipleRecords() throws Exception
 	{
 		dataSource = new JasperEnterpriseObjectDataSource( mockEO, mockEO, mockEO );
 
 		assertThat( dataSource.next(), is( true ) );
 		assertThat( dataSource.next(), is( true ) );
 		assertThat( dataSource.next(), is( true ) );
 		assertThat( dataSource.next(), is( false ) );
 	}
 
 	@Test
 	public void iterateOverMultipleRecordsFromNSArray() throws Exception
 	{
 		dataSource = new JasperEnterpriseObjectDataSource( new NSArray<EOEnterpriseObject>( new EOEnterpriseObject[] { mockEO, mockEO } ) );
 
 		assertThat( dataSource.next(), is( true ) );
 		assertThat( dataSource.next(), is( true ) );
 		assertThat( dataSource.next(), is( false ) );
 	}
 
 	@Test
 	public void iterateOverOneRecord() throws Exception
 	{
 		assertThat( dataSource.next(), is( true ) );
 		assertThat( dataSource.next(), is( false ) );
 	}
 
 	@Before
 	public void setup()
 	{
 		editingContext = editingContextProvider.editingContext();
 
 		mockEO = Foo.createFoo( editingContext );
 
 		dataSource = new JasperEnterpriseObjectDataSource( mockEO );
 	}
 }
