 package uk.ac.ebi.fg.core_model.xref;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Index;
 
 import uk.ac.ebi.fg.core_model.resources.Const;
 import uk.ac.ebi.fg.core_model.toplevel.Accessible;
 import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
 
 /**
  * 
  * A cross reference is something like a publication or a database entry. In terms of the core model, it is an 
  * {@link #acc accession}, referring to the context of a {@link ReferenceSource}. This is not an {@link Accessible}, 
  * despite having an accession property, since the accession for an XRef is unique only within in its context.
  *
  * <dl><dt>date</dt><dd>Jun 14, 2012</dd></dl>
  * @author Marco Brandizi
  *
  */
 @Entity
 @Table ( name = "xref" )
 public class XRef extends Identifiable
 {
 	private String acc;
 
 	private ReferenceSource source;
 
 	protected XRef () {
 		super ();
 	}
 
 	public XRef ( String acc, ReferenceSource source ) {
 		super ();
 		this.acc = acc;
 		this.source = source;
 	}
 
 	@Index ( name = "xr_acc" )
   @Column ( length = Const.COL_LENGTH_S )
 	public String getAcc () {
 		return this.acc;
 	}
 
 	public void setAcc ( String acc ) {
 		this.acc = acc;
 	}
 
 	@ManyToOne ( 
 		targetEntity = ReferenceSource.class, 
 		cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } 
 	)
 	@JoinColumn ( name = "source_id" )
 	public ReferenceSource getSource () {
 		return this.source;
 	}
 
	protected void setSource ( ReferenceSource source ) {
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
   	XRef that = (XRef) o;
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
   
 	public String toString () 
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
