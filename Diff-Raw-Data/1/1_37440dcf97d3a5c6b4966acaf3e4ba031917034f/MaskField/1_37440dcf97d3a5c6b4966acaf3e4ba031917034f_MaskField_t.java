 package com.sickle.medu.ms.client.datasource.field;
 
 import com.smartgwt.client.data.DataSourceField;
 import com.smartgwt.client.types.DateDisplayFormat;
 import com.smartgwt.client.types.FieldType;
 
 
 public class MaskField implements Comparable<MaskField>
 {
 
 	private int mask,index = -1;
 	
 	public static final String STYPE = "stype", MASK = "smask";
 	
 	private DataSourceField field;
 	
 	/**
 	 * @param name
 	 * @param simpleType
 	 * @param title
 	 */
 	public MaskField( String name, String simpleType, String title, int index)
 	{
 		this(name,simpleType,title,-1,index);
 	}
 	
 	
 	
 	/**
 	 * @param name
 	 * @param simpleType
 	 * @param title
 	 */
 	public MaskField( String name, String simpleType, String title, int mask,int _index )
 	{
 //		super( name, new SimpleType(), title );
 		if( simpleType.equals( com.sickle.uireflect.FieldType.DateTime.toString( )  ))
 		{
 			field = new DataSourceField( name, FieldType.DATETIME, title );
 			field.setDateFormatter( DateDisplayFormat.TOSERIALIZEABLEDATE );
 		}
 		else if( simpleType.equals( com.sickle.uireflect.FieldType.Date.toString( )  ))
 		{
 			field = new DataSourceField( name, FieldType.DATE, title );
			field.setDateFormatter( DateDisplayFormat.TOJAPANSHORTDATE );
 		}
 		else
 		{
 			field = new DataSourceField( name, FieldType.TEXT, title );
 		}
 		this.mask = mask;
 		this.index = _index;
 		field.setAttribute( STYPE, simpleType );
 		field.setAttribute( MASK, mask );
 	}
 
 
 	
 	/**
 	 * @return the mask
 	 */
 	public int getMask( )
 	{
 		return mask;
 	}
 
 	
 	/**
 	 * @param mask the mask to set
 	 */
 	public void setMask( int mask )
 	{
 		this.mask = mask;
 		field.setAttribute( MASK, mask );
 	}
 
 
 
 	@Override
 	public int compareTo( MaskField o )
 	{
 		return this.index - o.index ;
 	}
 	
 	public DataSourceField getField()
 	{
 		return field;
 	}
 	
 }
