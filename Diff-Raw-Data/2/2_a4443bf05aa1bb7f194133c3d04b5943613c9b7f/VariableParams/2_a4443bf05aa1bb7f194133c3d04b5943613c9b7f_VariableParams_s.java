 package net.ooici.eoi.netcdf;
 
 import net.ooici.eoi.datasetagent.obs.IObservationGroup.DataType;
 
 
 
 
 
 /**
  * This class represents the "naming" parameters of a NetcdfDataset.  These naming parameters include elements such as long_name, and short_name,
  * and are used to store well-formed variables into NetcdfDataset objects.
  * 
  * @author tlarocque
  *
  */
 public class VariableParams implements Comparable<VariableParams> {
 
 	/** Static Fields */
 	public static final VariableParams WATER_TEMPERATURE = new VariableParams("water_temperature", "temperature",
 		"temperature at position, in degrees celsius.", "degree_Celsius");
 	public static final VariableParams RIVER_STREAMFLOW = new VariableParams("water_volume_transport_into_sea_water_from_rivers",
 		"streamflow", "water volume transport into sea water from rivers at position, in cubic feet per second.", "ft3 s-1");
 	public static final VariableParams RIVER_WATER_SURFACE_HEIGHT = new VariableParams("water_surface_height_above_reference_datum",
 		"water_height", "water surface height in feet", "ft");
 	public static final VariableParams RIVER_WATER_SURFACE_REF_DATUM_ALTITUDE = new VariableParams("water_surface_reference_datum_altitude",
 		"water_height_datum", "the (geometric) height above the geoid, which is the reference geopotential surface. The geoid is similar to mean sea level. 'Water surface reference datum altitude' means the altitude of the arbitrary datum referred to by a quantity with standard name 'water_surface_height_above_reference_datum'", "ft");
 	public static final VariableParams RIVER_PRECIPITATION = new VariableParams("precipitation_total",
 		"precipitation_total", "total precipitation at gauge location in feet", "ft");
 	public static final VariableParams SEA_WATER_CONDUCTIVITY = new VariableParams("sea_water_electrical_conductivity",
 		"specific_conductance", "specific conductance of water at 25 degrees C", "S cm-1");
 	public static final VariableParams SEA_WATER_TEMPERATURE = new VariableParams("sea_water_temperature", "temperature",
 		"water temperature at location", "degree_Celsius");
 	public static final VariableParams SEA_WATER_SALINITY = new VariableParams("sea_water_salinity", "salinity",
		"water salinity at location", "1");
 	public static final VariableParams SEA_SURFACE_HEIGHT = new VariableParams("sea_surface_height", "ssh",
 		"sea surface height above sea level at location (originally 'sea level anomaly'", "m");
     public static VariableParams AIR_TEMPERATURE = new VariableParams("air_temperature", "air temp",
 		"air temperature", "degree_Celsius");
     public static VariableParams AIR_PRESSURE_AT_SEA_LEVEL = new VariableParams("air_pressure_at_sea_level", "air pressure",
 		"air pressure at sea level", "Pa");
 
 
 	/** Instance Fields */
 	private String standardName;
 	private String shortName;
 	private String description;
 	private String units;
 	private DataType dataType;
 
 
 	/**
 	 * Constructs a new set of VariableParams to represent the "naming" parameters of an NC variable.
 	 * 
 	 * @param standardName
 	 *            The CF compliant standard_name for this NcDataName
 	 * @param shortName
 	 *            The short_name attribute for this NcDataName
 	 * @param description
 	 *            The long_name attribute for this NcDataName
 	 * @param units
 	 *            The units attribute for this NcDataName
 	 */
 	public VariableParams(String standardName, String shortName, String description, String units) {
 	    this(standardName, shortName, description, units, DataType.DOUBLE);
 	}
 
 	
 	/**
      * Constructs a new set of VariableParams to represent the "naming" parameters of an NC variable.
      * 
      * @param standardName
      *            The CF compliant standard_name for this NcDataName
      * @param shortName
      *            The short_name attribute for this NcDataName
      * @param description
      *            The long_name attribute for this NcDataName
      * @param units
      *            The units attribute for this NcDataName
      */
     public VariableParams(String standardName, String shortName, String description, String units, DataType dataType) {
         this.standardName = standardName;
         this.shortName = shortName;
         this.description = description;
         this.units = units;
         this.dataType = dataType;
     }
 
     public VariableParams(VariableParams attribs, DataType dataType) {
         this.standardName = attribs.standardName;
         this.shortName = attribs.shortName;
         this.description = attribs.description;
         this.units = attribs.units;
         this.dataType = dataType;
     }
 
 	/**
 	 * Main entry point (for testing purposes only
 	 * )
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		/* TODO: this should become a unit test */
 
 		VariableParams v1 =
 			new VariableParams("water_temperature", "temperature", "temperature at position, in degrees celcius.", "celcius");
 		VariableParams v2 =
 			new VariableParams("water_temperature", "temperature", "temperature at position, in degrees celcius.", "celcius");
 		VariableParams v3 =
 			new VariableParams("water_volume_transport_into_sea_water_from_rivers", "streamflow",
 				"water volume transport into sea water from rivers at position, in cubic meters per second.", "m3 s-1");
 
 
 		System.out.println(v1.getStandardName());
 		System.out.println(v2.getStandardName());
 		System.out.println(v3.getStandardName());
 
 		System.out.println();
 
 		System.out.println(v1.hashCode());
 		System.out.println(v2.hashCode());
 		System.out.println(v3.hashCode());
 
 		System.out.println();
 
 		System.out.println(v1.compareTo(v2));
 		System.out.println(v2.compareTo(v1));
 		System.out.println(v1.compareTo(v3));
 		System.out.println(v3.compareTo(v1));
 		System.out.println(v2.compareTo(v3));
 		System.out.println(v3.compareTo(v2));
 
 		System.out.println();
 
 		System.out.println(v1.equals(v2));
 		System.out.println(v1.equals(v3));
 		System.out.println(v2.equals(v1));
 		System.out.println(v2.equals(v3));
 		System.out.println(v3.equals(v1));
 		System.out.println(v3.equals(v2));
 
 
 	}
 
 	/**
 	 * @return the standard_name attribute
 	 */
 	public String getStandardName() {
 		return standardName;
 	}
 
 
 	/**
 	 * @return the short_name attribute
 	 */
 	public String getShortName() {
 		return shortName;
 	}
 
 
 	/**
 	 * @return the description attribute
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 
 	/**
 	 * @return the units attribute
 	 */
 	public String getUnits() {
 		return units;
 	}
 
 	
 	/**
 	 * @return this variable's data type
 	 */
 	public DataType getDataType() {
 	    return dataType;
 	}
 
 
 	@Override
 	public int hashCode() {
 		int hash = 0;
 
 		hash += (null == standardName) ? (0) : (standardName.hashCode());
 
 		hash *= 31;
 		hash += (null == shortName) ? (0) : (shortName.hashCode());
 
 		hash *= 31;
 		hash += (null == description) ? (0) : (description.hashCode());
 
 		hash *= 31;
 		hash += (null == units) ? (0) : (units.hashCode());
 
 		hash *= 31;
 		hash += (null == dataType) ? (0) : (dataType.hashCode());
 
 
 		return hash;
 	}
 
 
 	@Override
 	public int compareTo(VariableParams arg0) {
 		int h1 = hashCode();
 		int h2 = arg0.hashCode();
 
 		return (h1 == h2) ? (0) : ((h1 > h2) ? (1) : (-1));
 	}
 
 
 	@Override
 	public boolean equals(Object o) {
 		return (o instanceof VariableParams) ? (compareTo((VariableParams) o) == 0) : (false);
 	}
 
 }
