 package org.cgiar.ilri.mistro.farmer.carrier;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.jar.JarInputStream;
 
 /**
  * Created by jason on 8/5/13.
  */
 public class Cow implements Parcelable, Serializable {
     public static final String TAG = "Cow";
    public static final int SEX_MALE = 0;
    public static final int SEX_FEMALE = 1;
     public static final int AGE_TYPE_DAY = 0;
     public static final int AGE_TYPE_WEEK = 1;
     public static final int AGE_TYPE_YEAR = 2;
     private String name;
     private String earTagNumber;
     private String dateOfBirth;
     private int age;
     private int ageType;
     private List<String> breeds;
     private int sex;
     private List<String> deformities;
     private Sire sire;
     private Dam dam;
     private String countryOfOrigin;
     private boolean isNotDamOrSire;
 
     public Cow(boolean isNotDamOrSire) {
         name = "";
         earTagNumber = "";
         dateOfBirth = "";
         age = -1;
         ageType = -1;
         this.breeds = new ArrayList<String>();
         sex = -1;
         this.deformities = new ArrayList<String>();
         this.isNotDamOrSire = isNotDamOrSire;
         if (isNotDamOrSire)//LOL, brings StackOverflowError if you init sire object inside sire object
         {
             sire = new Sire();
             dam = new Dam();
         }
         countryOfOrigin = "";
     }
 
     public Cow(Parcel in) {
         this(true);
         readFromParcel(in);
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setEarTagNumber(String earTagNumber) {
         this.earTagNumber = earTagNumber;
     }
 
     public void setDateOfBirth(String dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
     }
 
     public void setAge(int age) {
         this.age = age;
     }
 
     public void setAgeType(int ageType) {
         this.ageType = ageType;
     }
 
     public void setBreeds(String[] breeds) {
         this.breeds = new ArrayList<String>();
         for (int i = 0; i < breeds.length; i++) {
             this.breeds.add(breeds[i]);
         }
     }
 
     public void addBreed(String breed) {
         this.breeds.add(breed);
     }
 
     public void setSex(int sex) {
         this.sex = sex;
     }
 
     public void setDeformities(String[] deformities) {
         this.deformities = new ArrayList<String>();
         for (int i = 0; i < deformities.length; i++) {
             this.deformities.add(deformities[i]);
         }
     }
 
     public void addDeformity(String deformity) {
         this.deformities.add(deformity);
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
 
     public String getEarTagNumber() {
         return earTagNumber;
     }
 
     public String getDateOfBirth() {
         return dateOfBirth;
     }
 
     public int getAge() {
         return age;
     }
 
     public int getAgeType() {
         return ageType;
     }
 
     public List<String> getBreeds() {
         return breeds;
     }
 
     public int getSex() {
         return sex;
     }
 
     public List<String> getDeformities() {
         return deformities;
     }
 
     public Sire getSire() {
         return sire;//TODO: handle nullpointerexception
     }
 
     public Dam getDam() {
         if (dam == null) {
             Log.d(TAG, "dam is null");
         }
         return dam;
     }
 
     public String getCountryOfOrigin() {
         return countryOfOrigin;
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeString(name);
         dest.writeString(earTagNumber);
         dest.writeString(dateOfBirth);
         dest.writeInt(age);
         dest.writeInt(ageType);
         dest.writeStringList(breeds);
         dest.writeInt(sex);
         dest.writeStringList(deformities);
         if (isNotDamOrSire) {
             dest.writeInt(1);
             dest.writeSerializable(sire);
             dest.writeSerializable(dam);
         } else {
             dest.writeInt(0);
         }
 
         dest.writeString(countryOfOrigin);
     }
 
     public void readFromParcel(Parcel in) {
         name = in.readString();
         earTagNumber = in.readString();
         dateOfBirth = in.readString();
         age = in.readInt();
         ageType = in.readInt();
         in.readStringList(breeds);
         sex = in.readInt();
         in.readStringList(deformities);
         int x = in.readInt();
         if (x == 1)//isnotsireordam
         {
             this.isNotDamOrSire = true;
             sire = (Sire) in.readSerializable();
             dam = (Dam) in.readSerializable();
         } else {
             this.isNotDamOrSire = false;
         }
 
         countryOfOrigin = in.readString();
     }
 
     public static final Creator<Cow> CREATOR = new Creator<Cow>() {
         @Override
         public Cow createFromParcel(Parcel source) {
             return new Cow(source);
         }
 
         @Override
         public Cow[] newArray(int size) {
             return new Cow[size];
         }
     };
 
     public JSONObject getJsonObject() {
         JSONObject jsonObject = new JSONObject();
         try {
             jsonObject.put("name", ((name == null) ? "" : name));
             jsonObject.put("earTagNumber", ((earTagNumber == null) ? "" : earTagNumber));
             jsonObject.put("dateOfBirth", ((dateOfBirth == null) ? "" : dateOfBirth));
             jsonObject.put("age", age);
             jsonObject.put("ageType", ageType);
             JSONArray breedJsonArray = new JSONArray();
             for (int i = 0; i < breeds.size(); i++) {
                 breedJsonArray.put(i, breeds.get(i));
             }
             jsonObject.put("breeds", breedJsonArray);
             jsonObject.put("sex", sex);
             JSONArray deformityJsonArray = new JSONArray();
             for (int i = 0; i < deformities.size(); i++) {
                 deformityJsonArray.put(i, deformities.get(i));
             }
             jsonObject.put("deformities", deformityJsonArray);
             if (isNotDamOrSire) {
                 jsonObject.put("type", "cow");
                 jsonObject.put("sire", ((sire == null) ? "" : sire.getJsonObject()));
                 jsonObject.put("dam", ((dam == null) ? "" : dam.getJsonObject()));
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return jsonObject;
     }
 }
