 package mpicbg.spim.data.sequence;
 
 /**
  * Defines an angle which is part of the ViewSetup
  * 
  * @author Stephan Preibisch (stephan.preibisch@gmx.de)
  */
 public class Angle implements Comparable< Angle >
 {
 	/**
 	 * The approximate rotation axis from the microscope metadata (if available, otherwise null)
 	 */
 	protected double[] rotationAxis;
 	
 	/**
 	 * The approximate rotation angle from the microscope metadata  (if available, otherwise NaN)
 	 */
 	protected double rotationAngle;
 	
 	/**
 	 * The name of this angle, for example used to replace it in filenames when opening
 	 * individual 3d-stacks (e.g. SPIM_TL20_Angle45.tif)
 	 */
 	protected String name;
 	
 	/**
 	 * The unique id of this angle, defines for example the position in a file
 	 */
 	protected int id;
 	
 	public Angle( final int id, final String name, final double rotationAngle, final double[] rotationAxis )
 	{
 		this.name = name;
 		this.id = id;
 		this.rotationAngle = rotationAngle;
 		this.rotationAxis = rotationAxis;
 	}
 
 	public Angle( final int id, final String name, final double rotationAngle )
 	{
 		this( id, name, rotationAngle, null );
 	}
 
 	public Angle( final int id, final String name )
 	{
		this( id, Double.NaN );
 	}
 
 	public Angle( final int id, final double rotationAngle )
 	{
 		this( id, Integer.toString( id ), rotationAngle );
 	}
 
 	public Angle( final int id )
 	{
 		this( id, Integer.toString( id ) );
 	}
 	
 	public void setName( final String name ) { this.name = name; }
 	public void setRotationAxis( final double[] rotationAxis ) { this.rotationAxis = rotationAxis; }
 	public void setRotationAngle( final double rotationAngle ) { this.rotationAngle = rotationAngle; }
 	
 	/**
 	 * @return the approximate rotation axis as defined by the metadata (or null)
 	 */
 	public double[] getRotationAxis() { return rotationAxis; }
 	
 	/**
 	 * @return the approximate rotation angle from the microscope metadata (or NaN)
 	 */
 	public double getRotationAngle() { return rotationAngle; }
 	public String getName() { return name; }
 	public int getId() { return id; }
 
 	@Override
 	public int compareTo( final Angle angle ) { return getId() - angle.getId(); }
 }
