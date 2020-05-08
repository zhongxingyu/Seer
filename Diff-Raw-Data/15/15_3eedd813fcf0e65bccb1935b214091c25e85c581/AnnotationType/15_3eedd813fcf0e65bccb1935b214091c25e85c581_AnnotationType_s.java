 package uk.ac.ebi.fg.core_model.terms;
 
 import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import uk.ac.ebi.fg.core_model.toplevel.Annotation;
 
 
 /**
  * Used to assign a type to an {@link Annotation}. Values for this usually come from controlled vocabulary (i.e. 
  * enumerations).
  * 
  * <dl><dt>Date</dt><dd>Jan 4, 2008</dd></dl>
  * 
  * @author Nataliya Sklyar
  */
 @Entity
 @Table ( name = "annotation_type" )
 public final class AnnotationType extends CVTerm 
 {
 	protected AnnotationType () {
 		super ();
 	}
 
 	public AnnotationType ( String name )
 	{
 		super ( name );
 	}
 }
