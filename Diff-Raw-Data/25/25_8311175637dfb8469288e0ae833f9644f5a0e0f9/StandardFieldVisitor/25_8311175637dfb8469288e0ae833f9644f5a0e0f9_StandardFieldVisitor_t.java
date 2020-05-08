 package com.redshape.search.index.visitor.field;
 
 import com.redshape.search.annotations.AggregatedEntity;
 import com.redshape.search.annotations.SearchableField;
 import com.redshape.search.annotations.SearchableFieldSerializer;
 import com.redshape.search.index.IIndex;
 import com.redshape.search.index.IIndexField;
 import com.redshape.search.index.IndexingType;
 import com.redshape.search.index.builders.IIndexBuilder;
 import com.redshape.search.index.visitor.VisitorException;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.Arrays;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nikelin
  * Date: Aug 13, 2010
  * Time: 2:52:05 PM
  * To change this template use File | Settings | File Templates.
  */
 public class StandardFieldVisitor implements IFieldVisitor {
 
 	@Autowired( required = true )
 	private IIndexBuilder indexBuilder;
 
 	public IIndexBuilder getIndexBuilder() {
 		return indexBuilder;
 	}
 
 	public void setIndexBuilder(IIndexBuilder indexBuilder) {
 		this.indexBuilder = indexBuilder;
 	}
 
 	@Override
    public void visitField( IIndex index, Class<?> entityClass, String field ) throws VisitorException {
        try {
            Field classField = entityClass.getField(field);
            if ( classField.getAnnotation(AggregatedEntity.class) != null ) {
                this.visitAggregatedField( index, entityClass, classField );
            } else {
                this.visitSimpleField( index, entityClass, classField );
            }
        } catch ( Throwable e ) {
            throw new VisitorException();
         }
     }
 
     protected void visitAggregatedField( IIndex index, Class<?> entityClass, Field field ) throws VisitorException {
         try {
             AggregatedEntity annotation = field.getAnnotation( AggregatedEntity.class );
 
             switch ( annotation.type() ) {
                 case COMPOSED:
                     this._aggregatedComposition( index, entityClass, field );
                 break;
                 case ID:
                     this._aggregatedById( index, entityClass, field );
                 break;
             }
         } catch ( Throwable e ) {
             throw new VisitorException( e.getMessage(), e );
         }
     }
 
     private void _aggregatedComposition( IIndex index, Class<?> entity, Field field ) throws VisitorException {
         try {
             AggregatedEntity annotation = field.getAnnotation( AggregatedEntity.class );
 
             Class<?> aggregatedType;
             if ( annotation.targetEntity() != null ) {
                 aggregatedType = annotation.targetEntity();
             } else {
                 Class<?> fieldType = field.getType();
                 if ( fieldType.isInterface() || Modifier.isAbstract( fieldType.getModifiers() ) ) {
                     throw new VisitorException("Aggregated field cannot be interface or must be specificated by targetEntity()");
                 }
 
                 aggregatedType = (Class<?>) fieldType;
             }
 
             for ( IIndexField aggregatedField : this.getIndexBuilder().getIndex( aggregatedType ).getFields() ) {
                 if ( annotation.exclude().length > 0 &&
                         Arrays.binarySearch( annotation.exclude(), aggregatedField ) != -1 ){
                     continue;
                 }
 
                 if ( annotation.include().length > 0
                         && Arrays.binarySearch( annotation.include(), aggregatedField ) == -1 ) {
                     continue;
                 }
 
                 index.addField( aggregatedField );
             }
         } catch ( Throwable e ) {
             throw new VisitorException( e.getMessage(), e );
         }
     }
 
     private void _aggregatedById( IIndex index, Class<?> entityClass, Field field ) throws VisitorException {
         IIndexField indexField = index.createField();
 
         String fieldName;
         SearchableField fieldAnnotation = field.getAnnotation( SearchableField.class );
         if ( fieldAnnotation == null ) {
             fieldName = field.getName();
         } else {
             fieldName = fieldAnnotation.name();
         }
 
         indexField.setType(IndexingType.NUMERICAL);
         indexField.setName( fieldName );
         indexField.markAnalyzable(false);
         indexField.markBinary(false);
         indexField.markStored(true);
 
         index.addField( indexField );
     }
 
     protected void visitSimpleField( IIndex index, Class<?> entityClass, Field field ) {
         IIndexField indexPart = index.createField();
 
         SearchableField fieldMeta = field.getAnnotation( SearchableField.class );
 		if ( fieldMeta == null ) {
 			return;
 		}
 
         indexPart.setName( fieldMeta.name() );
         indexPart.setType( fieldMeta.type() );
         indexPart.setRank( fieldMeta.rank() );
         indexPart.markAnalyzable( fieldMeta.analyzable() );
         indexPart.markBinary( fieldMeta.binary() );
         indexPart.markStored( fieldMeta.stored() );
 
         SearchableFieldSerializer serializerMeta = field.getAnnotation( SearchableFieldSerializer.class );
         if ( serializerMeta != null ) {
             indexPart.setSerializer( serializerMeta.serializer() );
         }
 
         index.addField( indexPart );
     }
 
 }
