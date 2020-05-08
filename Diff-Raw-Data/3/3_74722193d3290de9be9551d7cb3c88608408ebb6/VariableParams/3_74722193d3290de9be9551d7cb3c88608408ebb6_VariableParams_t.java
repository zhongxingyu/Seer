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
 
     public enum StandardVariable {
 
         WATER_TEMPERATURE(new VariableParams(null, "water_temperature", "temperature of the water at position, in degrees celsius", "degree_Celsius")),
         RIVER_STREAMFLOW(new VariableParams("water_volume_transport_into_sea_water_from_rivers", "streamflow", "water volume transport into sea water from rivers at position, in cubic feet per second.", "ft3 s-1")),
         RIVER_WATER_SURFACE_HEIGHT(new VariableParams("water_surface_height_above_reference_datum",
         "water_height", "water surface height in feet", "ft")),
         RIVER_WATER_SURFACE_REF_DATUM_ALTITUDE(new VariableParams("water_surface_reference_datum_altitude",
         "water_height_datum", "the (geometric) height above the geoid, which is the reference geopotential surface. The geoid is similar to mean sea level. 'Water surface reference datum altitude' means the altitude of the arbitrary datum referred to by a quantity with standard name 'water_surface_height_above_reference_datum'", "ft")),
        RIVER_PRECIPITATION(new VariableParams(null, "precipitation_total", "total precipitation at gauge location in inches", "in")),
         SEA_WATER_TEMPERATURE(new VariableParams("sea_water_temperature", "sw_temp",
         "water temperature at location", "degree_Celsius")),
         SEA_WATER_SALINITY(new VariableParams("sea_water_salinity", "salinity",
         "temperature of the water at position, in degrees celsius", "0.001")),
         SEA_SURFACE_HEIGHT(new VariableParams("sea_surface_height", "ssh",
         "sea surface height above sea level at location (originally 'sea level anomaly'", "m")),
         AIR_PRESSURE_AT_SEA_LEVEL(new VariableParams("air_pressure_at_sea_level", "air_pressure",
         "air pressure at sea level", "hPa")),
         /* SOS "sea_water_electrical_conductivity" parameter */
         SEA_WATER_CONDUCTIVITY(new VariableParams("sea_water_electrical_conductivity",
         "specific_conductance", "specific conductance of water at 25 degrees C", "mS cm-1")),
         /* SOS air_temperature parameter */
         AIR_TEMPERATURE(new VariableParams("air_temperature", "air_temp",
         "air temperature", "degree_Celsius")),
         /* SOS "WaterLevel" parameter */
         SEA_FLOOR_DEPTH_BELOW_SEA_SURFACE(new VariableParams("sea_floor_depth_below_sea_surface", "sea_floor_depth",
         "the vertical distance between the sea surface and the seabed as measured at a given point in space including the variance caused by tides and possibly waves", "m")),
         /* SOS "Current" parameter */
         DIR_SEA_WATER_VELOCITY(new VariableParams("direction_of_sea_water_velocity", "curr_dir",
         "direction of seawater velocity in degrees", "degree")),
         SPEED_SEA_WATER_VELOCITY(new VariableParams("sea_water_speed", "curr_spd",
         "magnitude of seawater velocity in cm/s", "cm s-1")),
         UPWARD_SEA_WATER_VELOCITY(new VariableParams("upward_sea_water_velocity", "up_curr_spd",
         "magnitude of seawater velocity in the vertical (up being positive) in cm/s", "cm s-1")),
         PLATFORM_ORIENTATION(new VariableParams("platform_orientation", "platform_orientation",
         "direction in which the \"front\" or longitudinal axis of the platform is pointing", "degrees")),
         PLATFORM_ROLL_ANGLE(new VariableParams("platform_roll_angle", "platform_roll_angle",
         "roll angle of the platform", "degrees")),
         PLATFORM_PITCH_ANGLE(new VariableParams("platform_pitch_angle", "platform_pitch_angle",
         "pitch angle of the platform", "degrees")),
         /* SOS "Waves" parameter */
         SEA_SURFACE_WAVE_SIGNIFICANT_HEIGHT(new VariableParams("sea_surface_wave_significant_height", "sea_surface_wave_significant_height",
         "significant height of sea surface waves", "m")),
         SEA_SURFACE_WAVE_TO_DIRECTION(new VariableParams("sea_surface_wave_to_direction", "sea_surface_wave_to_direction",
         "direction towards which sea surface waves are moving", "degree")),
         SEA_SURFACE_SWELL_WAVE_SIGNIFICANT_HEIGHT(new VariableParams("sea_surface_swell_wave_significant_height", "sea_surface_swell_wave_significant_height",
         "significant height of sea surface swell wave", "m")),
         SEA_SURFACE_SWELL_WAVE_PERIOD(new VariableParams("sea_surface_swell_wave_period", "sea_surface_swell_wave_period",
         "period of sea surface swell wave", "s")),
         SEA_SURFACE_SWELL_WAVE_TO_DIRECTION(new VariableParams("sea_surface_swell_wave_to_direction", "sea_surface_swell_wave_to_direction",
         "direction towards which sea surface swell waves are moving", "degree")),
         SEA_SURFACE_WIND_WAVE_SIGNIFICANT_HEIGHT(new VariableParams("sea_surface_wind_wave_significant_height", "sea_surface_wind_wave_significant_height",
         "significant height of sea surface wind wave", "m")),
         SEA_SURFACE_WIND_WAVE_PERIOD(new VariableParams("sea_surface_wind_wave_period", "sea_surface_wind_wave_period",
         "period of sea surface wind wave", "s")),
         SEA_SURFACE_WIND_WAVE_TO_DIRECTION(new VariableParams("sea_surface_wind_wave_to_direction", "sea_surface_wind_wave_to_direction",
         "direction towards which sea surface wind waves are moving", "degree")),
         /* SOS "Winds" parameter */
         WIND_FROM_DIRECTION(new VariableParams("wind_from_direction", "wind_from_direction",
         "direction from which wind is blowing", "degree")),
         WIND_SPEED(new VariableParams("wind_speed", "wind_speed",
         "magnitude of wind", "m s-1")),
         WIND_SPEED_OF_GUST(new VariableParams("wind_speed_of_gust", "wind_speed_of_gust",
         "magnitude of wind gust", "m s-1")),
         UPWARD_AIR_VELOCITY(new VariableParams("upward_air_velocity", "upward_air_velocity",
         "magnitude of wind in the vertical", "m s-1")),
         
         
         ;
         
 
         private VariableParams param;
         StandardVariable(VariableParams param) {
             this.param = param;
         }
         public VariableParams getVariableParams() {
             return this.param;
         }
         
         public static StandardVariable parseStandardName(String standardName) {
             String n;
             for(StandardVariable sv : StandardVariable.values()) {
                 n = sv.getVariableParams().getStandardName();
                 if(n != null && standardName.toLowerCase().equals(n)) {
                     return sv;
                 }
             }
             
             throw new IllegalArgumentException("Could not find StandardVariable with the standardName \"" + standardName + "\"");
         }
     }
     /** Static Fields */
 //    public static final VariableParams WATER_TEMPERATURE = new VariableParams("water_temperature", "temperature",
 //            "temperature at position, in degrees celsius.", "degree_Celsius");
 //    public static final VariableParams RIVER_STREAMFLOW = new VariableParams("water_volume_transport_into_sea_water_from_rivers",
 //            "streamflow", "water volume transport into sea water from rivers at position, in cubic feet per second.", "ft3 s-1");
 //    public static final VariableParams RIVER_WATER_SURFACE_HEIGHT = new VariableParams("water_surface_height_above_reference_datum",
 //            "water_height", "water surface height in feet", "ft");
 //    public static final VariableParams RIVER_WATER_SURFACE_REF_DATUM_ALTITUDE = new VariableParams("water_surface_reference_datum_altitude",
 //            "water_height_datum", "the (geometric) height above the geoid, which is the reference geopotential surface. The geoid is similar to mean sea level. 'Water surface reference datum altitude' means the altitude of the arbitrary datum referred to by a quantity with standard name 'water_surface_height_above_reference_datum'", "ft");
 //    public static final VariableParams RIVER_PRECIPITATION = new VariableParams("precipitation_total",
 //            "precipitation_total", "total precipitation at gauge location in feet", "ft");
 //    public static final VariableParams SEA_WATER_CONDUCTIVITY = new VariableParams("sea_water_electrical_conductivity",
 //            "specific_conductance", "specific conductance of water at 25 degrees C", "mS cm-1");
 //    public static final VariableParams SEA_WATER_TEMPERATURE = new VariableParams("sea_water_temperature", "sw_temp",
 //            "water temperature at location", "degree_Celsius");
 //    public static final VariableParams SEA_WATER_SALINITY = new VariableParams("sea_water_salinity", "salinity",
 //            "water salinity at location", "0.001");
 //    public static final VariableParams SEA_SURFACE_HEIGHT = new VariableParams("sea_surface_height", "ssh",
 //            "sea surface height above sea level at location (originally 'sea level anomaly'", "m");
 //    public static final VariableParams SEA_FLOOR_DEPTH_BELOW_SEA_SURFACE = new VariableParams("sea_floor_depth_below_sea_surface", "sea_floor_depth",
 //            "the vertical distance between the sea surface and the seabed as measured at a given point in space including the variance caused by tides and possibly waves", "m");
 //    public static final VariableParams AIR_TEMPERATURE = new VariableParams("air_temperature", "air_temp",
 //            "air temperature", "degree_Celsius");
 //    public static final VariableParams AIR_PRESSURE_AT_SEA_LEVEL = new VariableParams("air_pressure_at_sea_level", "air_pressure",
 //            "air pressure at sea level", "Pa");
 //    public static final VariableParams DIR_SEA_WATER_VELOCITY = new VariableParams("direction_of_sea_water_velocity", "curr_dir",
 //            "direction of seawater velocity in degrees", "degree");
 //    public static final VariableParams SPEED_SEA_WATER_VELOCITY = new VariableParams("sea_water_speed", "curr_spd",
 //            "magnitude of seawater velocity in cm/s", "cm s-1");
 //    public static final VariableParams UPWARD_SEA_WATER_VELOCITY = new VariableParams("upward_sea_water_velocity", "up_curr_spd",
 //            "magnitude of seawater velocity in the vertical (up being positive) in cm/s", "cm s-1");
 //    public static final VariableParams PLATFORM_ORIENTATION = new VariableParams("platform_orientation", "platform_orientation",
 //            "direction in which the \"front\" or longitudinal axis of the platform is pointing", "degrees");
 //    public static final VariableParams PLATFORM_ROLL_ANGLE = new VariableParams("platform_roll_angle", "platform_roll_angle",
 //            "roll angle of the platform", "degrees");
 //    public static final VariableParams SEA_SURFACE_WAVE_SIGNIFICANT_HEIGHT = new VariableParams("sea_surface_wave_significant_height", "sea_surface_wave_significant_height",
 //            "significant height of sea surface waves", "m");
     /** Instance Fields */
     private String standardName;
     private String varName;
     private String description;
     private String units;
     private DataType dataType;
 
     /**
      * Constructs a new set of VariableParams to represent the "naming" parameters of an NC variable.
      * 
      * @param standardName
      *            The CF compliant standard_name for this NcDataName
      * @param varName
      *            The name for this VaraiableParams - applied as the name of the variable in datasets
      * @param longName
      *            The long_name attribute for this VaraiableParams
      * @param units
      *            The units attribute for this VaraiableParams
      */
     public VariableParams(String standardName, String varName, String longName, String units) {
         this(standardName, varName, longName, units, DataType.DOUBLE);
     }
 
     /**
      * Constructs a new set of VariableParams to represent the "naming" parameters of an NC variable.
      * 
      * @param standardName
      *            The CF compliant standard_name for this NcDataName
      * @param varName
      *            The name for this VaraiableParams - applied as the name of the variable in datasets
      * @param description
      *            The long_name attribute for this VaraiableParams
      * @param units
      *            The units attribute for this VaraiableParams
      * @param dataType 
      *            The DataType for this VariableParams
      */
     public VariableParams(String standardName, String varName, String description, String units, DataType dataType) {
         this.standardName = standardName;
         this.varName = varName;
         this.description = description;
         this.units = units;
         this.dataType = dataType;
     }
 
     public VariableParams(VariableParams attribs, DataType dataType) {
         this.standardName = attribs.standardName;
         this.varName = attribs.varName;
         this.description = attribs.description;
         this.units = attribs.units;
         this.dataType = dataType;
     }
     
     public VariableParams(StandardVariable sv, DataType dataType) {
         this(sv.getVariableParams(), dataType);
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
         return varName;
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
         hash += (null == varName) ? (0) : (varName.hashCode());
 
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
