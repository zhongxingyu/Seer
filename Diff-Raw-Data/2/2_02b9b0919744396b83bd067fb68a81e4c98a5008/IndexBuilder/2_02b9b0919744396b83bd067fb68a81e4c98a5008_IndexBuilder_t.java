 package com.redshape.search.index.builders;
 
 import com.redshape.search.annotations.Searchable;
 import com.redshape.search.index.IIndex;
 import com.redshape.search.index.Index;
 import com.redshape.search.index.visitor.VisitorException;
 import org.apache.log4j.Logger;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nikelin
  * Date: Jun 29, 2010
  * Time: 4:06:17 PM
  * To change this template use File | Settings | File Templates.
  */
 public class IndexBuilder extends AbstractIndexBuilder {
     private static final Logger log = Logger.getLogger( IndexBuilder.class );
 
     private Map< Class<?>, IIndex> indexes = new HashMap();
 
     private boolean isSupported( Class<?> subject ) {
 		return subject.getAnnotation(Searchable.class) != null;
 	}
 
 	protected void checkAssertions( Class<?> subject ) {
 		if ( subject == null ) {
 			throw new IllegalArgumentException("<null>");
 		}
 
 		if ( !this.isSupported(subject) ) {
 			throw new IllegalArgumentException("Object is not supported");
 		}
 	}
 
     @Override
     public IIndex getIndex( Class<?> searchable ) throws BuilderException {
 		this.checkAssertions(searchable);
 
         IIndex index = this.indexes.get( searchable);
         if ( index != null ) {
             return index;
         }
 
         index = this.buildIndex( searchable );
 
         this.indexes.put( searchable, index );
 
         return index;
     }
 
     protected IIndex buildIndex( Class<?> searchable ) throws BuilderException {
         IIndex index = new Index();
 
         Searchable meta = searchable.getAnnotation( Searchable.class );
         if ( meta == null ) {
             return null;
         }
 
         index.setName( meta.name() );
 
         for ( Field field : searchable.getDeclaredFields() ) {
             try {
                this.getFieldVisitor().visitField( index, searchable, field.getName() );
             } catch ( VisitorException e ) {
                 log.error("Index builder exception", e );
                 throw new BuilderException( e.getMessage(), e );
             }
         }
 
         return index;
     }
 
 }
