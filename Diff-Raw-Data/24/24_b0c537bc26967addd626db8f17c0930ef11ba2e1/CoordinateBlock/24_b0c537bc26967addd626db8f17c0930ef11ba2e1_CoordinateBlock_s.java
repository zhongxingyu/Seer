 public class CoordinateBlock {
 	private Coordinate mcLower;
 	private Coordinate mcUpper;
 	private boolean bHasReversedCoordinates ()
 	{
 		boolean bRetVal;
 		if (mcLower.getX () > mcUpper.getX () ||
 				mcLower.getY () > mcUpper.getY () ||
 				mcLower.getX () > mcUpper.getX ())
 		{
 			bRetVal = true;
 		}
 		else
 		{
 			bRetVal = false;
 		}
 		
 		return bRetVal;
 	}
 	// TODO: include this in some try/catch blocks below 
 	public CoordinateBlock (Coordinate cLower, Coordinate cUpper)
 	{
 		mcLower = cLower;
 		mcUpper = cUpper;
 	}
 	public CoordinateBlock (int LowerX, int LowerY, int LowerZ, 
 													int UpperX, int UpperY, int UpperZ)
 	{
 		mcLower = new Coordinate (LowerX, LowerY, LowerZ);
 		mcUpper = new Coordinate (UpperX, UpperY, UpperZ);
 	}
 	public CoordinateBlock (Coordinate cLower,
 													int UpperX, int UpperY, int UpperZ)
 	{
 		mcLower = cLower;
 		mcUpper = new Coordinate (UpperX, UpperY, UpperZ);
 	}
 	public CoordinateBlock (int LowerX, int LowerY, int LowerZ,
 													Coordinate cUpper)
 	{
 		mcLower = new Coordinate (LowerX, LowerY, LowerZ);
 		mcUpper = cUpper;
 	}
 	public Coordinate getLower ()
 	{
 		return mcLower;
 	}
 	public Coordinate getUpper ()
 	{
 		return mcUpper;
 	}
 	public int getLowerX ()
 	{
 		return mcLower.getX ();
 	}
 	public int getLowerY ()
 	{
 		return mcLower.getY ();
 	}
 	public int getLowerZ ()
 	{
 		return mcLower.getZ ();
 	}
 	public int getUpperX ()
 	{
 		return mcUpper.getX ();
 	}
 	public int getUpperY ()
 	{
 		return mcUpper.getY ();
 	}
 	public int getUpperZ ()
 	{
 		return mcUpper.getZ ();
 	}
 	public void setLower (Coordinate cLower)
 	{
 		mcLower = cLower;
 	}
 	public void setUpper (Coordinate cUpper)
 	{
 		mcLower = cUpper;
 	}
 	public void setLowerX (int X)
 	{
 		mcLower.setX (X);
 	}
 	public void setLowerY (int Y)
 	{
 		mcLower.setY (Y);
 	}
 	public void setLowerZ (int Z)
 	{
 		mcLower.setZ (Z);
 	}
 	public void setUpperX (int X)
 	{
 		mcUpper.setX (X);
 	}
 	public void setUpperY (int Y)
 	{
 		mcUpper.setY (Y);
 	}
 	public void setUpperZ (int Z)
 	{
 		mcUpper.setZ (Z);
 	}
 	public double getXRange ()
 	{
 		return mcUpper.getX () - mcLower.getX ();
 	}
 	public double getYRange ()
 	{
 		return mcUpper.getY () - mcLower.getY ();
 	}
 	public double getZRange ()
 	{
 		return mcUpper.getZ () - mcLower.getZ ();
 	}
 	public long getTotalArea ()
 	{
 		return (long) (getXRange () * getYRange () * getZRange ());
 	}
 	public void nudgeXUp ()
 	{
 		mcUpper.setX (mcUpper.getX () + 1);
 	}
 	public void nudgeXDown ()
 	{
 		mcUpper.setX (mcUpper.getX () - 1);
 	}
 	public void nudgeYUp ()
 	{
 		mcUpper.setY (mcUpper.getY () + 1);
 	}
 	public void nudgeYDown ()
 	{
 		mcUpper.setY (mcUpper.getY () - 1);
 	}
		public void nudgeZUp ()
 	{
 		mcUpper.setZ (mcUpper.getZ () + 1);
 	}
 	public void nudgeZDown ()
 	{
 		mcUpper.setZ (mcUpper.getZ () - 1);
	}
		public void nudgeXUp ()
	{
		mcUpper.setX (mcUpper.getX () + 1);
 	}
 	public void shiftX (int shiftVal)
 	{
 		mcUpper.setX (mcUpper.getX () + shiftVal);
 	}
 	public void shiftY (int shiftVal)
 	{
 		mcUpper.setY (mcUpper.getY () + shiftVal);
 	}
 	public void shiftZ (int shiftVal)
 	{
 		mcUpper.setZ (mcUpper.getZ () + shiftVal);
 	}
 }
