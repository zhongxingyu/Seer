 package org.cgiar.ilri.mistro.farmer.carrier;
 
 import java.util.Calendar;
 import java.util.Date;
 import org.json.me.JSONArray;
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 
 /**
  * Created by jason on 8/5/13.
  */
 public class Cow {
     public static final String SEX_MALE = "Male";
     public static final String SEX_FEMALE = "Female";
     public static final String AGE_TYPE_DAY = "Days";
     public static final String AGE_TYPE_WEEK = "Weeks";
     public static final String AGE_TYPE_YEAR = "Years";
     public static final String MODE_ADULT_COW_REGISTRATION = "adultCowRegistration";
     public static final String MODE_BORN_CALF_REGISTRATION = "bornCalfRegistration";
     public static final String SERVICE_TYPE_BULL = "Bull";
     public static final String SERVICE_TYPE_AI = "Artificial Insemination";
     public static final String SERVICE_TYPE_ET = "Embryo Transfer";
     private String name;
     private String earTagNumber;
     private String dateOfBirth;
     private int age;
     private String ageType;
     private String[] breeds;
     private String sex;
     private String[] deformities;
     private Sire sire;
     private Dam dam;
     private String countryOfOrigin;
     private boolean isNotDamOrSire;
     private String mode;
     private String serviceType;
     private String otherDeformity;
     private String piggyBack;
     private Date dateOfBirthDate;
     private MilkProduction[] milkProduction;
     private Event[] events;
 
     public Cow(boolean isNotDamOrSire) {
         name = "";
         earTagNumber = "";
         dateOfBirth = "";
         age = -1;
         ageType = "";
         breeds = new String[0];
         sex = "";
         deformities = new String[0];
         this.isNotDamOrSire = isNotDamOrSire;
         if (isNotDamOrSire)//LOL, brings StackOverflowError if you init sire object inside sire object
         {
             sire = new Sire();
             dam = new Dam();
         }
         mode = "";
         countryOfOrigin = "";
         serviceType = "";
         otherDeformity = "";
         piggyBack = "";
     }
     
     public Cow(JSONObject cowJSONObject){
         try {
             name = cowJSONObject.getString("name");
             earTagNumber = cowJSONObject.getString("ear_tag_number");
             dateOfBirth = cowJSONObject.getString("date_of_birth");
             if(cowJSONObject.getString("age")!=null && cowJSONObject.getString("age").trim().length() > 0)
                 System.out.println(cowJSONObject.getString("age"));
                 age = Integer.parseInt(cowJSONObject.getString("age"));
             ageType = cowJSONObject.getString("age_type");
             breeds = new String[0];
             sex = cowJSONObject.getString("sex");
             deformities = new String[0];
             this.isNotDamOrSire = true;
             if (isNotDamOrSire)//LOL, brings StackOverflowError if you init sire object inside sire object
             {
                 sire = new Sire();
                 dam = new Dam();
             }
             mode = "";
             countryOfOrigin = "";
             serviceType = cowJSONObject.getString("service_type");
             otherDeformity = "";
             piggyBack = "";
             
             JSONArray milkProductionArray = cowJSONObject.getJSONArray("milk_production");
             if(milkProductionArray!=null && sex.equals(SEX_FEMALE)){
                 milkProduction = new MilkProduction[milkProductionArray.length()];
                 for(int i = 0; i < milkProductionArray.length(); i++){
                     milkProduction[i] = new MilkProduction(milkProductionArray.getJSONObject(i));
                 }
             }
             else{
                 milkProduction = new MilkProduction[0];
             }
             
             JSONArray eventsArray = cowJSONObject.getJSONArray("cow_events");
             if(eventsArray!=null){
                 events = new Event[eventsArray.length()];
                 for(int i = 0; i< eventsArray.length(); i++ ){
                     events[i] = new Event(eventsArray.getJSONObject(i));
                 }
             }
             else{
                 events = new Event[0];
             }
         } 
         catch (JSONException ex) {
             ex.printStackTrace();
         }
     }
 
     public void setMode(String mode) {
         this.mode = mode;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setEarTagNumber(String earTagNumber) {
         this.earTagNumber = earTagNumber;
     }
 
     public void setDateOfBirth(Date dateOfBirth) {
         this.dateOfBirthDate = dateOfBirth;
         Calendar calendar  = Calendar.getInstance();
         calendar.setTime(dateOfBirth);
         StringBuffer dateStringBuffer = new StringBuffer();
         dateStringBuffer.append(calendar.get(Calendar.DATE)).append("/");
        dateStringBuffer.append(calendar.get(Calendar.MONTH)).append("/");
         dateStringBuffer.append(calendar.get(Calendar.YEAR));
         this.dateOfBirth = dateStringBuffer.toString();
     }
 
     public void setAge(int age) {
         this.age = age;
     }
 
     public void setAgeType(String ageType) {
         this.ageType = ageType;
     }
 
     public void setBreeds(String[] breeds) {
         this.breeds = breeds;
     }
 
     public void setPiggyBack(String piggyBack) {
         this.piggyBack = piggyBack;
     }
 
     public void setServiceType(String serviceType) {
         this.serviceType = serviceType;
     }
 
     public void setOtherDeformity(String otherDeformity) {
         this.otherDeformity = otherDeformity;
     }
 
     public void setSex(String sex) {
         this.sex = sex;
     }
 
     public void setDeformities(String[] deformities) {
         this.deformities = deformities;
     }
 
     public void setSire(Sire sire) {
         this.sire = sire;
     }
 
     public void setDam(Dam dam) {
         this.dam = dam;
     }
 
     public void setCountryOfOrigin(String countryOfOrigin) {
         this.countryOfOrigin = countryOfOrigin;
     }
 
     public String getName() {
         return name;
     }
 
     public String getMode() {
         return mode;
     }
 
     public String getEarTagNumber() {
         return earTagNumber;
     }
 
     public String getDateOfBirth() {
         return dateOfBirth;
     }
     
     public long getDateOfBirthMilliseconds() {
         if(dateOfBirthDate != null)
             return dateOfBirthDate.getTime();
         else return -1;
     }
 
     public int getAge() {
         return age;
     }
 
     public String getPiggyBack() {
         return piggyBack;
     }
 
     public String getAgeType() {
         return ageType;
     }
 
     public String getServiceType() {
         return serviceType;
     }
 
     public String getOtherDeformity() {
         return otherDeformity;
     }
 
     public String[] getBreeds() {
         return breeds;
     }
 
     public String getSex() {
         return sex;
     }
 
     public String[] getDeformities() {
         return deformities;
     }
 
     public Sire getSire() {
         return sire;//TODO: handle nullpointerexception
     }
 
     public Dam getDam() {
         return dam;
     }
 
     public String getCountryOfOrigin() {
         return countryOfOrigin;
     }
     
     public MilkProduction[] getMilkProduction(){
         return milkProduction;
     }
     
     public Event[] getEvents(){
         return events;
     }
 
     public JSONObject getJsonObject() {
         JSONObject jsonObject = new JSONObject();
         try {
             jsonObject.put("name", ((name == null) ? "" : name));
             jsonObject.put("earTagNumber", ((earTagNumber == null) ? "" : earTagNumber));
             jsonObject.put("dateOfBirth", ((dateOfBirth == null) ? "" : dateOfBirth));
             jsonObject.put("age", age);
             jsonObject.put("ageType", ageType);
             JSONArray breedJsonArray = new JSONArray();
             if(breeds!=null){
                 for (int i = 0; i < breeds.length; i++) {
                     breedJsonArray.put(i, breeds[i]);
                 }
                 jsonObject.put("breeds", breedJsonArray);
             }
             else{
                 jsonObject.put("breeds", new JSONArray());
             }
             
             jsonObject.put("sex", sex);
             JSONArray deformityJsonArray = new JSONArray();
             if(deformities!=null){
                 for (int i = 0; i < deformities.length; i++) {
                     deformityJsonArray.put(i, deformities[i]);
                 }
                 jsonObject.put("deformities", deformityJsonArray);
             }
             else{
                 jsonObject.put("deformities", new JSONArray());
             }
             
             jsonObject.put("mode", ((mode == null) ? "" : mode));
             jsonObject.put("serviceType", ((serviceType == null) ? "" : serviceType));
             jsonObject.put("otherDeformity", ((otherDeformity == null) ? "" : otherDeformity));
             jsonObject.put("countryOfOrigin", ((countryOfOrigin == null) ? "" : countryOfOrigin));
             if (isNotDamOrSire) {
                 jsonObject.put("type", "cow");
                 if(sire == null) jsonObject.put("sire",  "");
                 else jsonObject.put("sire",sire.getJsonObject());
                 
                 if(dam == null )jsonObject.put("dam", "");
                 else jsonObject.put("dam", dam.getJsonObject());
             }
             if(piggyBack!=null && piggyBack.length()>0) {
                 jsonObject.put("piggyBack",piggyBack);
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return jsonObject;
     }
}
