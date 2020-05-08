 package uk.ac.ebi.fg.biosd.model.xref;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import org.hibernate.annotations.Index;
 
 import uk.ac.ebi.fg.core_model.resources.Const;
 import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
 import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
 import uk.ac.ebi.fg.core_model.xref.XRef;
 
 /**
  * A link about a record on an external repository/database, such as a web page on PRIDE about an experiment or a sample.
  * This corresponds to the Database element in the SampleTAB format.
  * 
  * This is a simpler version of the combination of {@link XRef} and {@link ReferenceSource}, which we find better for
  * managing this particular type of information. We also plan to replace this with <a href = ''>myEquivalents</a> 
  *
  * <dl><dt>date</dt><dd>Jul 17, 2012 (completely changed in Jan 24, 2014)</dd></dl>
  * @author Marco Brandizi
  *
  */
 @Table ( name = "db_rec_ref", uniqueConstraints = @UniqueConstraint ( columnNames = { "db_name", "acc", "version" } ) )
 @Entity
 public class DatabaseRecordRef extends Identifiable
 {
   private String dbName;
   private String acc;
   private String version;
   private String url;
   private String title;
  
	protected DatabaseRecordRef () {
		super ();
	}
 
 	public DatabaseRecordRef ( String dbName, String acc, String version, String url, String title )
 	{
 		this.dbName = dbName;
 		this.acc = acc;
 		this.version = version;
 		this.url = url;
 		this.title = title;
 	}
 
 	public DatabaseRecordRef ( String dbName, String acc, String version )
 	{
 		this ( dbName, acc, version, null, null );
 	}	
 
 
   @Index ( name = "dbrec_name" )
   @Column ( name = "db_name", length = Const.COL_LENGTH_M )
 	public String getDbName ()
 	{
 		return dbName;
 	}
 
 	protected void setDbName ( String dbName )
 	{
 		this.dbName = dbName;
 	}
 
   @Index ( name = "dbrec_acc" )
 	@Column( unique = false, nullable = false, length = Const.COL_LENGTH_S) 
 	public String getAcc ()
 	{
 		return acc;
 	}
 
   protected void setAcc ( String acc )
 	{
 		this.acc = acc;
 	}
 
   @Column ( length = Const.COL_LENGTH_S )
   @Index ( name = "dbrec_ver" )
 	public String getVersion ()
 	{
 		return version;
 	}
 
   protected void setVersion ( String version )
 	{
 		this.version = version;
 	}
 
   @Column ( length = 2000 )
   @Index ( name = "dbrec_url" )
 	public String getUrl ()
 	{
 		return url;
 	}
 
 	public void setUrl ( String url )
 	{
 		this.url = url;
 	}
 
   @Column ( length = Const.COL_LENGTH_L )
   @Index ( name = "dbrec_title" )
 	public String getTitle ()
 	{
 		return title;
 	}
 
 	public void setTitle ( String title )
 	{
 		this.title = title;
 	}
 	
 	
 	
   /**
    * If any of database name or accession is null (which should never happen), they're different. If they've non null
    * equal dbName/acc, it compares the versions too.
    * 
    */
   @Override
   public boolean equals ( Object o ) 
   {
   	if ( o == null ) return false;
   	if ( this == o ) return true;
   	if ( this.getClass () != o.getClass () ) return false;
   	
   	DatabaseRecordRef that = (DatabaseRecordRef) o;
 
   	if ( this.getDbName () == null ) return false;
   	if ( !this.dbName.equals ( that.getDbName () ) ) return false;
   	
   	if ( this.getAcc() == null ) return false;
   	if ( !this.acc.equals ( that.getAcc () ) ) return false;
   	
     return this.getVersion () != null ? this.version.equals ( that.getVersion () ) : that.getVersion () == null;
   }
   
   @Override
   public int hashCode() 
   {
   	if ( this.getDbName () == null || this.getAcc () == null ) return super.hashCode ();
   	
   	int result = this.dbName.hashCode ();
   	result = 31 * result + this.acc.hashCode ();
   	result = 31 * result + ( this.getVersion () == null ? 0 : this.version.hashCode () );
   	
   	return result;
   }	
 	
 	
 	@Override
 	public String toString ()
 	{
 		return String.format ( 
 			"%s { id: %d, dbName: '%s', acc: '%s', version: '%s', url: '%s', title: '%.15s' }",
 			this.getClass ().getSimpleName (), getId (), getDbName (), getAcc (), getVersion (), getUrl (), getTitle () 
 		);
 	}
 	
 }
