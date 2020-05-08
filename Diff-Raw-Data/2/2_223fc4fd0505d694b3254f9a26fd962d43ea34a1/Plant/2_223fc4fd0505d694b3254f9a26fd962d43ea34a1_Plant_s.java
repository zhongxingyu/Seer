 package de.hswt.hrm.plant.model;
 
 import com.google.common.base.Optional;
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 import de.hswt.hrm.place.model.Place;
 
 /**
  * Represents a plant
  */
 
 public class Plant {
 
     // mandatory fields
     private int id;
     private int inspectionInterval;
     // Laut Anforderung: Anzahl der Elemente (ergibt sich aus der schematischen Bezeichnung) Wie ist
     // das gemeint?
     private int numberOfElements;
     private String description;
     
 
     // optional
     private Place place;
     private int constructionYear;
     private String manufactor;
     private String type;
     private String airPerformance;
     private String motorPower;
     private String motorRpm;
     private String ventilatorPerformance;
     private String current;
     private String voltage;
     private String note;
 
     private static final String IS_MANDATORY = "Field is a mandatory.";
     private static final String INVALID_NUMBER = "%d is an invalid number.%n Must be greater 0";
 
     public Plant(int inspectionInterval, final String description) {
 
         this(-1, inspectionInterval,  description);
 
     }
 
     public Plant(int id, int inspectionInterval, final String description) {
 
         this.id = id;
         setInspectionInterval(inspectionInterval);
         setDescription(description);
     }
 
     public int getId() {
         return id;
     }
 
     public int getInspectionInterval() {
         return inspectionInterval;
     }
 
     public void setInspectionInterval(int inspectionInterval) {
         checkArgument(inspectionInterval > 0, INVALID_NUMBER, inspectionInterval);
         this.inspectionInterval = inspectionInterval;
     }
 
     // TODO abklären siehe oben
     public int getNumberOfElements() {
         return numberOfElements;
     }
 
     // TODO abklären siehe oben
     public void setNumberOfElements(int numberOfElements) {
         this.numberOfElements = numberOfElements;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
        checkArgument(isNullOrEmpty(description), IS_MANDATORY);
         this.description = description;
     }
 
     public Optional<Place> getPlace() {
         return Optional.fromNullable(place);
     }
 
     public void setPlace(Place place) {
         this.place = place;
     }
 
     public Optional<Integer> getConstructionYear() {
         return Optional.fromNullable(constructionYear);
     }
 
     public void setConstructionYear(int constructionYear) {
         this.constructionYear = constructionYear;
     }
 
     public Optional<String> getManufactor() {
         return Optional.fromNullable(manufactor);
     }
 
     public void setManufactor(String manufactor) {
         this.manufactor = manufactor;
     }
 
     public Optional<String> getType() {
         return Optional.fromNullable(type);
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public Optional<String> getAirPerformance() {
         return Optional.fromNullable(airPerformance);
     }
 
     public void setAirPerformance(String airPerformance) {
         this.airPerformance = airPerformance;
     }
 
     public Optional<String> getMotorPower() {
         return Optional.fromNullable(motorPower);
     }
 
     public void setMotorPower(String motorPower) {
         this.motorPower = motorPower;
     }
 
     public Optional<String> getMotorRpm() {
         return Optional.fromNullable(motorRpm);
     }
 
     public void setMotorRpm(String motorRpm) {
         this.motorRpm = motorRpm;
     }
 
     public Optional<String> getVentilatorPerformance() {
         return Optional.fromNullable(ventilatorPerformance);
     }
 
     public void setVentilatorPerformance(String ventilatorPerformance) {
         this.ventilatorPerformance = ventilatorPerformance;
     }
 
     public Optional<String> getCurrent() {
         return Optional.fromNullable(current);
     }
 
     public void setCurrent(String current) {
         this.current = current;
     }
 
     public Optional<String> getVoltage() {
         return Optional.fromNullable(voltage);
     }
 
     public void setVoltage(String voltage) {
         this.voltage = voltage;
     }
 
     public Optional<String> getNote() {
         return Optional.fromNullable(note);
     }
 
     public void setNote(String note) {
         this.note = note;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((airPerformance == null) ? 0 : airPerformance.hashCode());
         result = prime * result + constructionYear;
         result = prime * result + ((current == null) ? 0 : current.hashCode());
         result = prime * result + ((description == null) ? 0 : description.hashCode());
         result = prime * result + id;
         result = prime * result + inspectionInterval;
         result = prime * result + ((manufactor == null) ? 0 : manufactor.hashCode());
         result = prime * result + ((motorPower == null) ? 0 : motorPower.hashCode());
         result = prime * result + ((motorRpm == null) ? 0 : motorRpm.hashCode());
         result = prime * result + ((note == null) ? 0 : note.hashCode());
         result = prime * result + numberOfElements;
         result = prime * result + ((place == null) ? 0 : place.hashCode());
         result = prime * result + ((type == null) ? 0 : type.hashCode());
         result = prime * result
                 + ((ventilatorPerformance == null) ? 0 : ventilatorPerformance.hashCode());
         result = prime * result + ((voltage == null) ? 0 : voltage.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Plant other = (Plant) obj;
         if (airPerformance == null) {
             if (other.airPerformance != null) {
                 return false;
             }
         }
         else if (!airPerformance.equals(other.airPerformance)) {
             return false;
         }
         if (constructionYear != other.constructionYear) {
             return false;
         }
         if (current == null) {
             if (other.current != null) {
                 return false;
             }
         }
         else if (!current.equals(other.current)) {
             return false;
         }
         if (description == null) {
             if (other.description != null) {
                 return false;
             }
         }
         else if (!description.equals(other.description)) {
             return false;
         }
         if (id != other.id) {
             return false;
         }
         if (inspectionInterval != other.inspectionInterval) {
             return false;
         }
         if (manufactor == null) {
             if (other.manufactor != null) {
                 return false;
             }
         }
         else if (!manufactor.equals(other.manufactor)) {
             return false;
         }
         if (motorPower == null) {
             if (other.motorPower != null) {
                 return false;
             }
         }
         else if (!motorPower.equals(other.motorPower)) {
             return false;
         }
         if (motorRpm == null) {
             if (other.motorRpm != null) {
                 return false;
             }
         }
         else if (!motorRpm.equals(other.motorRpm)) {
             return false;
         }
         if (note == null) {
             if (other.note != null) {
                 return false;
             }
         }
         else if (!note.equals(other.note)) {
             return false;
         }
         if (numberOfElements != other.numberOfElements) {
             return false;
         }
         if (place == null) {
             if (other.place != null) {
                 return false;
             }
         }
         else if (!place.equals(other.place)) {
             return false;
         }
         if (type == null) {
             if (other.type != null) {
                 return false;
             }
         }
         else if (!type.equals(other.type)) {
             return false;
         }
         if (ventilatorPerformance == null) {
             if (other.ventilatorPerformance != null) {
                 return false;
             }
         }
         else if (!ventilatorPerformance.equals(other.ventilatorPerformance)) {
             return false;
         }
         if (voltage == null) {
             if (other.voltage != null) {
                 return false;
             }
         }
         else if (!voltage.equals(other.voltage)) {
             return false;
         }
         return true;
     }
 
 }
