 package br.com.wobr.reports.jasper;
 
 import net.sf.jasperreports.engine.JRDataSource;
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRField;
 
 import com.webobjects.eocontrol.EOEnterpriseObject;
 import com.webobjects.foundation.NSArray;
 
 /**
  * Implementation of <code>JRDataSource</code> to support
  * <code>EOEnterpriseObject</code>s. This class provides a way to iterate over
  * <code>EOEnterpriseObject</code>s and collect the required data through
  * keyValue coding pattern.
  * <p>
  * You can map Jasper fields to properties and relationships from the
  * <code>EOEnterpriseObject<code>s provided. You must follow the
  * same pattern used for keyPath navigation.
  * <p>
  * If you have two objects like these:
  * 
  * <pre>
  * Foo            ->       FooRelated
  *  |-bar                    `-relatedBar
  *  `-relatedRelationship
  * </pre>
  * 
  * You can map the following fields:
  * <ul>
  * <li>bar</li>
  * <li>relatedRelationship.relatedBar</li>
  * </ul>
  * 
  * @author <a href="mailto:hprange@gmail.com">Henrique Prange</a>
  */
 public class JasperEnterpriseObjectDataSource implements JRDataSource
 {
 	private int index = -1;
 
	private final NSArray<? extends EOEnterpriseObject> objects;
 
 	public JasperEnterpriseObjectDataSource( final EOEnterpriseObject... objects )
 	{
 		this( new NSArray<EOEnterpriseObject>( objects ) );
 	}
 
	public JasperEnterpriseObjectDataSource( final NSArray<? extends EOEnterpriseObject> objects )
 	{
 		if( objects == null )
 		{
 			throw new IllegalArgumentException( "The array of enterprise objects cannot be null" );
 		}
 
 		this.objects = objects;
 	}
 
 	public Object getFieldValue( final JRField field ) throws JRException
 	{
 		EOEnterpriseObject object = objects.objectAtIndex( index );
 
 		return object.valueForKeyPath( field.getName() );
 	}
 
 	public boolean next() throws JRException
 	{
 		index++;
 
 		return index < objects.size();
 	}
 }
