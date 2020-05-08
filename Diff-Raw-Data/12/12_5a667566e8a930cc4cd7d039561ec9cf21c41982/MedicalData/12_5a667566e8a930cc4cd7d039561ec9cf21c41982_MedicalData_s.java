 package heartdoctor.DataModel;
 
 /**
  * Klasa do reprezentacji danych z bazy. 
  * Dodane enumy dla mapowania i ułatwienia integracji z GUI.
  * @author michal
  */
 public class MedicalData {
 
     private int dbID;
     private double age;
     private Sex sex;
     private ChestPain chestPain;
     private double bloodPressure;
     private double cholestoral;
     private double bloodSugar;
     private RestingECGResult restecg;
     private double maxHeartRate;
     private double angina;
     private double oldpeak;
     private Slope slope;
     private double ca;
     private Thal thal;
     private double diagnosis; //zweryfikowana przez lekarza, -1 jesli nie potwierdzono
     private double programDiagnosis = -1; //diagnoza wystawiona przez program, -1 jesli nie diagnozowano
 
     /**
      * Tworzy obiekt MedicalData
      */
     public MedicalData(int dbID, double age, Sex sex, ChestPain chestPain,
             double bloodPressure, double cholestoral, double bloodSugar,
             RestingECGResult restecg, double maxHeartRate, double angina,
             double oldpeak, Slope slope, double ca, Thal thal, double diagnosis) {
         this.dbID = dbID;
         this.age = age;
         this.sex = sex;
         this.chestPain = chestPain;
         this.bloodPressure = bloodPressure;
         this.cholestoral = cholestoral;
         this.bloodSugar = bloodSugar;
         this.restecg = restecg;
         this.maxHeartRate = maxHeartRate;
         this.angina = angina;
         this.oldpeak = oldpeak;
         this.slope = slope;
         this.ca = ca;
         this.thal = thal;
         this.diagnosis = diagnosis;
     }
 
     public MedicalData() {
     }
 
     public double getProgramDiagnosis() {
         return programDiagnosis;
     }
 
     public void setProgramDiagnosis(double verifiedDiagnosis) {
         this.programDiagnosis = verifiedDiagnosis;
     }
 
     public void setChestPain(ChestPain chestPain) {
         this.chestPain = chestPain;
     }
 
     public void setRestecg(RestingECGResult restecg) {
         this.restecg = restecg;
     }
 
     public void setSex(Sex sex) {
         this.sex = sex;
     }
 
     public void setSlope(Slope slope) {
         this.slope = slope;
     }
 
     public void setThal(Thal thal) {
         this.thal = thal;
     }
 
     public double getAge() {
         return age;
     }
 
     public void setAge(double age) {
         this.age = age;
     }
 
     public double getAngina() {
         return angina;
     }
 
     public void setAngina(double angina) {
         this.angina = angina;
     }
 
     public double getBloodPressure() {
         return bloodPressure;
     }
 
     public void setBloodPressure(double bloodPressure) {
         this.bloodPressure = bloodPressure;
     }
 
     public double getBloodSugar() {
         return bloodSugar;
     }
 
     public void setBloodSugar(double bloodSugar) {
         this.bloodSugar = bloodSugar;
     }
 
     public double getCa() {
         return ca;
     }
 
     public void setCa(double ca) {
         this.ca = ca;
     }
 
     public double getChestPain() {
         return chestPain.getValue();
     }
 
     public void setChestPain(double chestPain) {
         switch ((int) chestPain) {
             case 1:
                 this.chestPain = ChestPain.TypicalAngina;
                 break;
             case 2:
                 this.chestPain = ChestPain.AntypicalAngina;
                 break;
             case 3:
                 this.chestPain = ChestPain.NonAnginal;
                 break;
             case 4:
                 this.chestPain = ChestPain.Asymptomatic;
                 break;
             default:
                 this.chestPain = null;
         }
     }
 
     public int getDbID() {
         return dbID;
     }
 
     public void setDbID(int dbID) {
         this.dbID = dbID;
     }
 
     public double getCholestoral() {
         return cholestoral;
     }
 
     public void setCholestoral(double cholestoral) {
         this.cholestoral = cholestoral;
     }
 
     public double getDiagnosis() {
         return diagnosis;
     }
 
     public void setDiagnosis(double diagnosis) {
         this.diagnosis = diagnosis;
     }
 
     public double getMaxHeartRate() {
         return maxHeartRate;
     }
 
     public void setMaxHeartRate(double maxHeartRate) {
         this.maxHeartRate = maxHeartRate;
     }
 
     public double getOldpeak() {
         return oldpeak;
     }
 
     public void setOldpeak(double oldpeak) {
         this.oldpeak = oldpeak;
     }
 
     public double getRestecg() {
         return restecg.getValue();
     }
 
     public void setRestecg(double restecg) {
         switch ((int) restecg) {
             case 0:
                 this.restecg = RestingECGResult.Normal;
                 break;
             case 1:
                 this.restecg = RestingECGResult.STTAbnormal;
                 break;
             case 2:
                 this.restecg = RestingECGResult.leftVentricularHypertrophy;
                 break;
             default:
                 this.restecg = null;
         }
     }
 
     public double getSex() {
         return sex.getValue();
     }
 
     public void setSex(double sex) {
         switch ((int) sex) {
             case 0:
                 this.sex = Sex.Female;
                 break;
             case 1:
                 this.sex = Sex.Male;
                 break;
             default:
                 this.sex = null;
         }
     }
 
     public double getSlope() {
         return slope.getValue();
     }
 
     public void setSlope(double slope) {
         switch ((int) slope) {
             case 1:
                 this.slope = Slope.UpSloping;
                 break;
             case 2:
                 this.slope = Slope.Flat;
                 break;
             case 3:
                 this.slope = Slope.DownSloping;
                 break;
             default:
                 this.slope = null;
         }
     }
 
     public double getThal() {
         return thal.getValue();
     }
 
     public void setThal(double thal) {
         switch ((int) thal) {
             case 3:
                 this.thal = Thal.Normal;
                 break;
             case 6:
                 this.thal = Thal.FixedDefect;
                 break;
             case 7:
                 this.thal = Thal.ReversableDefect;
                 break;
             default:
                 this.thal = null;
         }
     }
 
     public ChestPain getChestPainEnum() {
         return chestPain;
     }
 
     public Sex getSexEnum() {
         return sex;
     }
 
     public RestingECGResult RestingECGResultEnum() {
         return restecg;
     }
 
     public Slope getSlopeEnum() {
         return slope;
     }
 
     /**
      * returns value for select in view
      * @return
      */
     public int getSlopeForSelect() {
         if (slope == null) {
             return 0; //default value
         } else {
             return (int) slope.ordinal();
         }
     }
 
     /**
      * returns value for select in view
      * @return
      */
     public int getThalForSelect() {
         if (thal == null) {
             return 0; //default value
         } else {
             return (int) thal.ordinal();
         }
     }
 
     /**
      * returns value for select in view
      * @return
      */
     public int getChestPainForSelect() {
         if (chestPain == null) {
             return 0; //default value
         } else {
             return (int) chestPain.ordinal();
         }
     }
 
     public Thal getThalEnum() {
         return thal;
     }
 
     public enum ChestPain {
 
         TypicalAngina,
         AntypicalAngina,
         NonAnginal,
         Asymptomatic;
 
         public double getValue() {
             return ordinal() + 1;
         }
     }
 
     public enum Sex {
 
         Female,
         Male;
 
         public double getValue() {
             return ordinal();
         }
     }
 
     public enum RestingECGResult {
 
         Normal,
         STTAbnormal,
         leftVentricularHypertrophy;
 
         public double getValue() {
             return ordinal();
         }
     }
 
     public enum Slope {
 
         UpSloping,
         Flat,
         DownSloping;
 
         public double getValue() {
             return ordinal() + 1;
         }
     }
 
     public enum Thal {
 
         Normal(3),
         FixedDefect(6),
         ReversableDefect(7);
         private int mapping;
 
         Thal(int nbr) {
             mapping = nbr;
         }
 
         public double getValue() {
             return mapping;
         }
     }
 }
