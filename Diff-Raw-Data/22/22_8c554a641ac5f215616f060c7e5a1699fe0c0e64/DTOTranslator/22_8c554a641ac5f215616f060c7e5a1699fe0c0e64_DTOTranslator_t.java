 package com.axiomalaska.hibernatetoolsdto.translator;
 
 import java.sql.Clob;
 import java.util.HashMap;
 import java.util.Map;
import java.util.UUID;
 import java.util.regex.Pattern;
 
 import org.hibernate.mapping.Property;
 import org.hibernate.tool.hbm2x.Cfg2JavaTool;
 import org.hibernate.tool.hbm2x.pojo.BasicPOJOClass;
 import org.hibernate.tool.hbm2x.pojo.POJOClass;
 
 public class DTOTranslator extends AbstractTranslator {
 	// Important to remember:
 	// pojo.getJavaTypeName adds types to import context
 	// c2j.getJavaTypeName does not
 	
 	private static final Map<String, DTOType> DTO_TYPES = new HashMap<String, DTOType>();
 
     static {
         //Clob
     	DTOType clobType = new DTOType(
     		 "String"
     		,true
     	);    	
     	DTO_TYPES.put( Clob.class.getName(), clobType );
     	DTO_TYPES.put( Clob.class.getSimpleName(), clobType );
     	DTO_TYPES.put( Clob.class.getCanonicalName(), clobType );
 
         //Geometry
     	DTOType geometryType = new DTOType(
     		 "Geometry"
     		,true
     	);    	
     	DTO_TYPES.put( "Geometry", geometryType );
     	DTO_TYPES.put( "com.vividsolutions.jts.geom.Geometry", geometryType );    

    	//UUID
    	DTOType uuidType = new DTOType(
    		 "String"
    		,false
    		,"UUID.fromString( ? )"
    		,"?.toString()"
    	);    	
    	DTO_TYPES.put( UUID.class.getName(), uuidType );
    	DTO_TYPES.put( UUID.class.getSimpleName(), uuidType );
    	DTO_TYPES.put( UUID.class.getCanonicalName(), uuidType );

     }	
 	
     public DTOTranslator(POJOClass pojo, Cfg2JavaTool c2j) {
 		super(pojo, c2j);
 	}
 
     public String getJavaTypeName( Property p, boolean jdk5, String suffix ){
         String typeName = pojo.getJavaTypeName( p, jdk5 );
         DTOType dtoType = DTO_TYPES.get( typeName );        
         if( dtoType != null ){
         	typeName = dtoType.getDtoTypeName();
         }
         typeName = addTypeSuffix( typeName, suffix ); 
         return pojo.importType( typeName );
     }
     
     
     public boolean excludeFromDto( Property p ){
     	String typeName = c2j.getJavaTypeName( p, false );
         DTOType dtoType = DTO_TYPES.get( typeName );
         if( dtoType != null && dtoType.isExclude() ){
         	return true;
         }
         return false;
     }
     
     public String getPojoToDto( Property p, boolean jdk5 ){
     	String typeName = pojo.getJavaTypeName( p, jdk5 );
     	
     	BasicPOJOClass bpc = null;
     	if( pojo instanceof BasicPOJOClass ){
     		bpc = (BasicPOJOClass) pojo;
     	} else {
     		return null;
     	}
         String getterSig = bpc.getGetterSignature( p ) + "()";
         
         DTOType dtoType = DTO_TYPES.get( typeName );        
         if( dtoType != null && dtoType.getSetConversion() != null ){
         	getterSig = dtoType.getSetConversion().replaceAll( Pattern.quote("?"), getterSig );
         }
         return getterSig;
     }
 
     public String getDtoToPojo( Property p, boolean jdk5 ){
    	return getDtoToPojo( null, p, jdk5 );
    }
    
    public String getDtoToPojo( String getterPrefix, Property p, boolean jdk5 ){
     	String typeName = pojo.getJavaTypeName( p, jdk5 );
     	
     	BasicPOJOClass bpc = null;
     	if( pojo instanceof BasicPOJOClass ){
     		bpc = (BasicPOJOClass) pojo;
     	} else {
     		return null;
     	}
        String getterSig = getterPrefix + bpc.getGetterSignature( p ) + "()";
         
         DTOType dtoType = DTO_TYPES.get( typeName );        
         if( dtoType != null && dtoType.getGetConversion() != null ){
         	getterSig = dtoType.getGetConversion().replaceAll( Pattern.quote("?"), getterSig );
         }
         return getterSig;        
     }
 
 	public String getFieldInitialization( Property prop, boolean jdk5 ){
 		return getFieldInitialization( prop, jdk5, null );
 	}
 	
 	public String getFieldInitialization( Property prop, boolean jdk5, String suffix ){
 		return addTypeSuffix( pojo.getFieldInitialization( prop, jdk5 ), suffix );
 	}
 
 	private String addTypeSuffix( String typeStr, String suffix ){
 		if( suffix != null && typeStr.indexOf('>') > -1 ){
 			typeStr = new StringBuffer(typeStr).insert( typeStr.indexOf('>'), suffix ).toString();
 		}
 		return typeStr;
 	}  
 }
