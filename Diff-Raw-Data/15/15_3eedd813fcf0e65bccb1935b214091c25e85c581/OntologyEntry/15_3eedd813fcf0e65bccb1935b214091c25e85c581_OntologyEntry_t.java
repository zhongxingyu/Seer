 package uk.ac.ebi.fg.core_model.terms;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Index;
 
 import uk.ac.ebi.fg.core_model.resources.Const;
 import uk.ac.ebi.fg.core_model.toplevel.Accessible;
 import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
 import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
 
 
 /**
  * A reference ontology term, which can be identified by an accession and a reference to the ontology source.
  * Note that accessions are not unique within any OE table, since they're assigned by 3rd parties and are unique only
  * together with the term's {@link ReferenceSource} (ie, only in their own ontology). For this same reason, this 
  * class doesn't extend {@link Accessible}. 
  * <p/>
  *
  * 
  * @author Nataliya Sklyar
  * @author mdylag
  * @author Tony Burdett
  * @author Marco Brandizi
  * 
  * @date Jul 12, 2007, imported from the AE2 model in 2012
  */
 @Entity
 @Table( name = "onto_entry" )
 @Inheritance ( strategy = InheritanceType.SINGLE_TABLE )
 @DiscriminatorColumn ( name = "term_category" )
 @DiscriminatorValue ( "generic" )
 @SequenceGenerator( name = "hibernate_seq", sequenceName = "onto_entry_seq" )
 public class OntologyEntry extends Identifiable
 {
 	private String acc, label; 
 	
 	private ReferenceSource source;
 	
 	protected OntologyEntry ()
 	{
 		super ();
 	}
 
 	public OntologyEntry ( String acc, ReferenceSource source )
 	{
 		super ();
 		this.acc = acc;
 		this.source = source;
 	}
 
   @Index( name = "oe_acc" )
  @Column ( length = Const.COL_LENGTH_S, nullable = false )
 	public String getAcc () {
 		return acc;
 	}
 
 	protected void setAcc ( String acc ) {
 		this.acc = acc;
 	}
 
   @Index( name = "oe_label" )
   @Column ( length = Const.COL_LENGTH_M )
 	public String getLabel () {
 		return label;
 	}
 
 	public void setLabel ( String label ) {
 		this.label = label;
 	}
 	
 	@ManyToOne ( targetEntity = ReferenceSource.class, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
 	@JoinColumn( name = "source_id" )
 	public ReferenceSource getSource ()
 	{
 		return source;
 	}
 
 	protected void setSource ( ReferenceSource source )
 	{
 		this.source = source;
 	}
 	
 	
   /**
    * If both accessions and reference sources are non-null, compares them, else uses object identity. 
    */
   @Override
   public boolean equals ( Object o ) 
   {
   	if ( o == null ) return false;
   	if ( this == o ) return true;
   	if ( this.getClass () != o.getClass () ) return false;
   	
     // Compare accessions if both are non-null, use identity otherwise
   	OntologyEntry that = (OntologyEntry) o;
     if ( this.getAcc () == null || that.getAcc () == null ) return false;
     if ( this.getSource () == null || that.getSource () == null ) return false;
     return this.acc.equals ( that.acc ) && this.source.equals ( that.source );
   }
   
   @Override
   public int hashCode() 
   {
   	return this.getAcc () == null || this.getSource () == null 
   		? super.hashCode () 
   		: this.source.hashCode () * 31 + this.acc.hashCode ();
   }
 
   @Override
   public String toString() 
   {
 		return String.format ( 
 			"%s { id: %d, acc: '%s', source: %s }" , 
 			this.getClass ().getSimpleName (), getId (), getAcc (), 
 			getSource () == null 
 			  ? null 
 			  : String.format ( "{ id: %d, '%s', ver: '%s' }", source.getId (), source.getAcc (), source.getVersion () ) 
 		);
   }
 	
 }
