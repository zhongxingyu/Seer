 package edu.hawaii.systemh.frontend.page.aquaponics;
 
 import edu.hawaii.ihale.api.ApiDictionary.IHaleRoom;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleState;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.api.repository.SystemStateListener;
 import edu.hawaii.systemh.frontend.components.panel.SystemPanel.SystemHStatus;
 
 /**
  * A listener for aquaponics that the UI uses to learn when the database has changed state.
  * 
  * @author Philip Johnson
  * @author Kylan Hughes
  * @author Chuan Lun Hung
  */
 public class AquaponicsListener extends SystemStateListener {
 
   // private static final String SYSTEM_NAME = "aquaponics";
   // private static final String TEMPERATURE_KEY = "temp";;
   // private static final String PH_KEY = "ph";
   // private static final String OXYGEN_KEY = "oxygen";
 
   private Integer temp = -1;
   private Integer tempCommand = -1;
   private Double pH = -1.0;
   private Double pHCommand = -1.0;
   private Double oxygen = -1.0;
   private Double circulation = -1.0;
   private Integer deadFish = -1;
   private Double conductivity = -1.0;
   private Double nutrients = -1.0;
   private Double turbidity = -1.0;
   private Integer waterLevel = -1;
   private Integer waterLevelCommand = -1;
   private Double feedFishAmount = -1.0;
   private Integer fishToHarvest = -1;
 
   // Range values for each system
   private static final long TEMPERATURE_RANGE_START = 27;
   private static final long TEMPERATURE_RANGE_END = 30;
   private static final long TEMPERATURE_ALERT_RANGE_START = 20;
   private static final long TEMPERATURE_ALERT_RANGE_END = 45;
   private static final double PH_RANGE_START = 6.8;
   private static final double PH_RANGE_END = 8.0;
   private static final double PH_ACCEPTED_DIFFERENCE = 1.0;
   private static final double OXYGEN_RANGE_START = 4.50;
   private static final double OXYGEN_RANGE_END = 5.50;
   private static final double OXYGEN_ACCEPTED_DIFFERENCE = 0.2;
   private static final double EC_RANGE_START = 10.0;
   private static final double EC_RANGE_END = 20.0;
   private static final double EC_ACCEPTED_DIFFERENCE = 2.0;
   private static final int LEVEL_RANGE_START = 36;
   private static final int LEVEL_RANGE_END = 48;
   private static final int LEVEL_ACCEPTED_DIFFERENCE = 2;
   private static final int CIRCULATION_RANGE_START = 60;
   private static final int CIRCULATION_RANGE_END = 100;
   private static final int CIRCULATION_ACCEPTED_DIFFERENCE = 10;
   private static final int TURBIDITY_RANGE_START = 0;
   private static final int TURBIDITY_RANGE_END = 100;
   private static final int TURBIDITY_ACCEPTED_DIFFERENCE = 5;
 
   private int circulationStatusLevel = 0;
   private int conductivityStatusLevel = 0;
   private int deadFishStatusLevel = 0;
   private int oxygenStatusLevel = 0;
   private int phStatusLevel = 0;
   private int temperatureStatusLevel = 0;
   private int turbidityStatusLevel = 0;
   private int waterLevelStatusLevel = 0;
 
   private SystemHStatus aquaponicsStatus;
 
   /**
    * Provide a default constructor that indicates that this listener is for Aquaponics.
    */
   public AquaponicsListener() {
     super(IHaleSystem.AQUAPONICS);
   }
 
   /**
    * Runs when the Aquaponics state changes.
    * 
    * @param state One of the aquaponics state values.
    * @param room Always null for the Aquaponics system.
    * @param timestamp The time when this state change occurred.
    * @param value The value associated with this state change.
    */
   @Override
   public void entryAdded(IHaleState state, IHaleRoom room, Long timestamp, Object value) {
 
     switch (state) {
     case CIRCULATION:
       circulation = (Double) value;
 
       // Check circulation's value to populate warnings if applicable.
       if (Math.abs(circulation - CIRCULATION_RANGE_START) < CIRCULATION_ACCEPTED_DIFFERENCE
           || Math.abs(circulation - CIRCULATION_RANGE_END) < CIRCULATION_ACCEPTED_DIFFERENCE) {
         circulationStatusLevel = 1;
       }
       else if (circulation < CIRCULATION_RANGE_START || circulation > CIRCULATION_RANGE_END) {
         circulationStatusLevel = 2;
       }
       else {
         circulationStatusLevel = 0;
       }
 
       break;
     case DEAD_FISH:
       deadFish = (Integer) value;
 
       // Check dead fish's value to populate warnings if applicable.
       if (deadFish >= 1) {
         deadFishStatusLevel = 2;
       }
       else {
         deadFishStatusLevel = 0;
       }
       break;
 
     case FEED_FISH_COMMAND:
       feedFishAmount = (Double) value;
       break;
 
     case ELECTRICAL_CONDUCTIVITY:
       conductivity = (Double) value;
 
       // Check electrical conductivity's value to populate warnings if applicable.
       if ((conductivity <= (EC_RANGE_START) && conductivity >= Math.abs(EC_RANGE_START
           - EC_ACCEPTED_DIFFERENCE))
           || (conductivity >= (EC_RANGE_END) && conductivity <= Math.abs(EC_RANGE_END
               + EC_ACCEPTED_DIFFERENCE))) {
         conductivityStatusLevel = 1;
       }
       else if (conductivity < Math.abs(EC_RANGE_START - EC_ACCEPTED_DIFFERENCE)
           || conductivity > Math.abs(EC_RANGE_END + EC_ACCEPTED_DIFFERENCE)) {
         conductivityStatusLevel = 2;
       }
       else {
         conductivityStatusLevel = 0;
       }
 
       break;
 
     case HARVEST_FISH_COMMAND:
       fishToHarvest = (Integer) value;
       break;
 
     case NUTRIENTS:
       nutrients = (Double) value;
       break;
 
     case SET_NUTRIENTS_COMMAND:
       nutrients = (Double) value;
       break;
 
     case OXYGEN:
       oxygen = (Double) value;
 
       // Check oxygen's value to populate warnings if applicable.
       if (Math.abs(oxygen - OXYGEN_RANGE_START) < OXYGEN_ACCEPTED_DIFFERENCE
           || Math.abs(oxygen - OXYGEN_RANGE_END) < OXYGEN_ACCEPTED_DIFFERENCE) {
         oxygenStatusLevel = 1;
       }
       else if (oxygen < OXYGEN_RANGE_START || oxygen > OXYGEN_RANGE_END) {
         oxygenStatusLevel = 2;
       }
       else {
         oxygenStatusLevel = 0;
       }
       break;
 
     case PH:
       pH = (Double) value;
 
       // Check pH's value to populate warnings if applicable.
       if ((pH <= (PH_RANGE_START) && pH >= Math.abs(PH_RANGE_START - PH_ACCEPTED_DIFFERENCE))
           || (pH >= (PH_RANGE_END) && pH <= Math.abs(PH_RANGE_END + PH_ACCEPTED_DIFFERENCE))) {
         phStatusLevel = 1;
       }
       else if (pH < Math.abs(PH_RANGE_START - PH_ACCEPTED_DIFFERENCE)
           || pH > Math.abs(PH_RANGE_END + PH_ACCEPTED_DIFFERENCE)) {
         phStatusLevel = 2;
       }
       else {
         phStatusLevel = 0;
       }
       break;
 
     case SET_PH_COMMAND:
       pHCommand = (Double) value;
       break;
 
     case TEMPERATURE:
       temp = (Integer) value;
 
       // Check temperature's value to populate warnings if applicable.
       if ((temp < TEMPERATURE_RANGE_START && temp > TEMPERATURE_ALERT_RANGE_START)
           || (temp > TEMPERATURE_RANGE_END && temp < TEMPERATURE_ALERT_RANGE_END)) {
         temperatureStatusLevel = 1;
       }
       else if (temp <= TEMPERATURE_ALERT_RANGE_START || temp >= TEMPERATURE_ALERT_RANGE_END) {
         temperatureStatusLevel = 2;
       }
       else {
         temperatureStatusLevel = 0;
       }
 
       break;
 
     case SET_TEMPERATURE_COMMAND:
       tempCommand = (Integer) value;
       break;
 
     case TURBIDITY:
       turbidity = (Double) value;
 
       // Check turbidity's value to populate warnings if applicable.
       if (Math.abs(turbidity - TURBIDITY_RANGE_START) < TURBIDITY_ACCEPTED_DIFFERENCE
           || Math.abs(turbidity - TURBIDITY_RANGE_END) < TURBIDITY_ACCEPTED_DIFFERENCE) {
         turbidityStatusLevel = 1;
       }
       else if (turbidity < TURBIDITY_RANGE_START || turbidity > TURBIDITY_RANGE_END) {
         turbidityStatusLevel = 2;
       }
       else {
         turbidityStatusLevel = 0;
       }
 
       break;
 
     case WATER_LEVEL:
       waterLevel = (Integer) value;
 
       // Check water level's value to populate warnings if applicable.
       if (Math.abs(waterLevel - LEVEL_RANGE_START) < LEVEL_ACCEPTED_DIFFERENCE
           || Math.abs(waterLevel - LEVEL_RANGE_END) < LEVEL_ACCEPTED_DIFFERENCE) {
         waterLevelStatusLevel = 1;
       }
       else if (waterLevel < LEVEL_RANGE_START || waterLevel > LEVEL_RANGE_END) {
         waterLevelStatusLevel = 2;
       }
       else {
         waterLevelStatusLevel = 0;
       }
 
       break;
 
     case SET_WATER_LEVEL_COMMAND:
       waterLevel = (Integer) value;
       break;
 
     default:
       System.out.println("Unhandled aquaponics state: " + state);
 
     }
 
     // Generate the warnings for the system, WARNING level takes top priority.
     if (circulationStatusLevel == 2 || conductivityStatusLevel == 2 || deadFishStatusLevel == 2
         || oxygenStatusLevel == 2 || phStatusLevel == 2 || temperatureStatusLevel == 2
         || turbidityStatusLevel == 2 || waterLevelStatusLevel == 2) {
       aquaponicsStatus = SystemHStatus.WARNING;
     }
     else if (circulationStatusLevel == 1 || conductivityStatusLevel == 1 || oxygenStatusLevel == 1
         || phStatusLevel == 1 || temperatureStatusLevel == 1 || turbidityStatusLevel == 1
         || waterLevelStatusLevel == 1) {
       aquaponicsStatus = SystemHStatus.CAUTION;
     }
     else {
       aquaponicsStatus = SystemHStatus.OK;
     }
   }
 
   /**
    * Return the temperature.
    * 
    * @return The temperature Celsius.
    */
   public Integer getTemp() {
     return temp;
   }
 
   /**
    * Return the temperature value set by user.
    * 
    * @return The temperature Celsius.
    */
   public Integer getTempCommand() {
     return tempCommand;
   }
 
   /**
    * Return the ph value.
    * 
    * @return The ph value.
    */
   public Double getPH() {
     return pH;
   }
 
   /**
    * Return the ph value set by user.
    * 
    * @return The ph value.
    */
   public Double getPHCommand() {
     return pHCommand;
   }
 
   /**
    * Return the oxygen.
    * 
    * @return The oxygen
    */
   public Double getOxygen() {
     return oxygen;
   }
 
   /**
    * Return the circulation.
    * 
    * @return The circulation
    */
   public Double getCirculation() {
     return circulation;
   }
 
   /**
    * Return the number of dead fish.
    * 
    * @return The number of dead fish
    */
   public Integer getDeadFish() {
     return deadFish;
   }
 
   /**
    * Return the conductivity.
    * 
    * @return The conductivity
    */
   public Double getConductivity() {
     return conductivity;
   }
 
   /**
    * Return the nutrients.
    * 
    * @return The nutrients
    */
   public Double getNutrients() {
     return nutrients;
   }
 
   /**
    * Return the turbidity.
    * 
    * @return The turbidity
    */
   public Double getTurbidity() {
     return turbidity;
   }
 
   /**
    * Return the water level.
    * 
    * @return The water level
    */
   public Integer getWaterLevel() {
     return waterLevel;
   }
 
   /**
    * Return the water level set by user.
    * 
    * @return The water level
    */
   public Integer getWaterLevelCommand() {
     return waterLevelCommand;
   }
 
   /**
    * Return the amount fish are fed.
    * 
    * @return The amount in grams that fish are fed.
    */
   public Double getFeedFishAmount() {
     return feedFishAmount;
   }
 
   /**
    * Return the amount of fish to harvest.
    * 
    * @return The amount of fish to harvest.
    */
   public Integer getFishToHarvest() {
     return fishToHarvest;
   }
 
   /**
    * Gets this Aquaponics status.
    * 
    * @return SystemHStatus
    */
   public SystemHStatus getAquaponicsStatus() {
     return this.aquaponicsStatus;
   }
 }
