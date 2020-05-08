 /*
  * 
  */
 package org.smartly.packages.mongo.impl.db.entity;
 
 import com.mongodb.DBObject;
 import org.smartly.IConstants;
 import org.smartly.commons.util.CollectionUtils;
 import org.smartly.commons.util.StringUtils;
 import org.smartly.packages.mongo.impl.MongoObject;
 import org.smartly.packages.mongo.impl.db.entity.item.MongoAddress;
 import org.smartly.packages.mongo.impl.util.MongoUtils;
 
 import java.util.List;
 
 
 /**
  * @author angelo.geminiani
  */
 public class MongoUser extends MongoObject {
 
     // ------------------------------------------------------------------------
     //                      Constants
     // ------------------------------------------------------------------------
     public static String COLLECTION = "users";
 
 
     //-- objects --//
     public static final String DATA = IMongoEntityConstants.DATA; // custom fields
     //-- fields --//
     // account
     public static final String UID = IMongoEntityConstants.UID;
     public static final String ENABLED = IMongoEntityConstants.ENABLED;
     public static final String REMOVED = IMongoEntityConstants.REMOVED;
     public static final String FINDABLE = IMongoEntityConstants.FINDABLE;
     public static final String LANG = IMongoEntityConstants.LANG;
     public static final String COUNTRY_ID = IMongoEntityConstants.COUNTRY_ID;
     public static final String COUNTRY = IMongoEntityConstants.COUNTRY; // late initialized (after login)
     public static final String USERNAME = IMongoEntityConstants.USERNAME;
     public static final String PASSWORD = IMongoEntityConstants.PASSWORD;
     public static final String EMAIL = IMongoEntityConstants.EMAIL;
     public static final String IMAGE = IMongoEntityConstants.IMAGE;
     // billing
     public static final String REALNAME = IMongoEntityConstants.REALNAME;
     public static final String COMPANY = IMongoEntityConstants.COMPANY;
     public static final String VAT = IMongoEntityConstants.VAT;
     public static final String ADDRESS_DELIVERY = IMongoEntityConstants.ADDRESS_DELIVERY; // object
     public static final String ADDRESS_BILLING = IMongoEntityConstants.ADDRESS_BILLING; // object
     // advanced
     public static final String GENDER = IMongoEntityConstants.GENDER;
     public static final String BIRTHDATE = IMongoEntityConstants.BIRTHDATE;
     public static final String EMAILOFFICE = IMongoEntityConstants.EMAILOFFICE;
     public static final String PHONEMOBILE = IMongoEntityConstants.PHONEMOBILE;
     public static final String PHONEHOME = IMongoEntityConstants.PHONEHOME;
     public static final String PHONEOFFICE = IMongoEntityConstants.PHONEOFFICE;
     public static final String JOB = IMongoEntityConstants.JOB;
     // price list
     public static final String DISCOUNT = IMongoEntityConstants.DISCOUNT;
     //-- collections --//
     public static final String PRODUCTS = IMongoEntityConstants.PRODUCTS;
     public static final String PRODUCT_LINKS = IMongoEntityConstants.PRODUCT_LINKS;
     public static final String ROLES = IMongoEntityConstants.ROLES;
     public static final String PROFILES = IMongoEntityConstants.PROFILES;
     // internal use only
     public static final String SMSCOUNTER = "smscounter";
     public static final String SMSLIMIT = "smslimit";  // max number of SMS for user
 
     // ------------------------------------------------------------------------
     //                      Constructor
     // ------------------------------------------------------------------------
     public MongoUser() {
         this.init();
     }
 
     public MongoUser(final DBObject object) {
         super(object);
     }
 
     // ------------------------------------------------------------------------
     //                      p u b l i c
     // ------------------------------------------------------------------------
 
     public String getUid() {
         return super.getString(UID);
     }
 
     public void setUid(String value) {
         super.append(UID, value);
     }
 
     public String getLang() {
         return super.getString(LANG);
     }
 
     public void setLang(String value) {
         super.append(LANG, value);
     }
 
     public String getCountryId() {
         return super.getString(COUNTRY_ID);
     }
 
     public void setCountryId(String value) {
         super.append(COUNTRY_ID, value);
     }
 
     public String getUsername() {
         return super.getString(USERNAME);
     }
 
     public void setUsername(String value) {
         super.append(USERNAME, value);
     }
 
     public String getPassword() {
         return super.getString(PASSWORD);
     }
 
     public void setPassword(String value) {
         super.append(PASSWORD, value);
     }
 
     public String getEmail() {
         return super.getString(EMAIL);
     }
 
     public void setEmail(String value) {
         super.append(EMAIL, value);
     }
 
     public String getEmailoffice() {
         return super.getString(EMAILOFFICE);
     }
 
     public void setEmailoffice(String value) {
         super.append(EMAILOFFICE, value);
     }
 
     public String getImage() {
         return super.getString(IMAGE);
     }
 
     public void setImage(String value) {
         super.append(IMAGE, value);
     }
 
     public String getPhonemobile() {
         return super.getString(PHONEMOBILE);
     }
 
     public void setPhonemobile(String value) {
         super.append(PHONEMOBILE, value);
     }
 
     public String getPhonehome() {
         return super.getString(PHONEHOME);
     }
 
     public void setPhonehome(String value) {
         super.append(PHONEHOME, value);
     }
 
     public String getPhoneoffice() {
         return super.getString(PHONEOFFICE);
     }
 
     public void setPhoneoffice(String value) {
         super.append(PHONEOFFICE, value);
     }
 
     public String getRealname() {
         return super.getString(REALNAME);
     }
 
     public void setRealname(String value) {
         super.append(REALNAME, value);
     }
 
     public String getGender() {
         return super.getString(GENDER);
     }
 
     public void setGender(String value) {
         super.append(GENDER, value);
     }
 
     public String getBirthdate() {
         return super.getString(BIRTHDATE);
     }
 
     public void setBirthdate(String value) {
         super.append(BIRTHDATE, value);
     }
 
     public String getJob() {
         return super.getString(JOB);
     }
 
     public void setJob(String value) {
         super.append(JOB, value);
     }
 
     public String getCompany() {
         return super.getString(COMPANY);
     }
 
     public void setCompany(String value) {
         super.append(COMPANY, value);
     }
 
     public int getSmscounter() {
         return super.getInt(SMSCOUNTER);
     }
 
     public void setSmscounter(int value) {
         super.append(SMSCOUNTER, value);
     }
 
     public int getSmslimit() {
         return super.getInt(SMSLIMIT);
     }
 
     public void setSmslimit(int value) {
         super.append(SMSLIMIT, value);
     }
 
     public boolean getEnabled() {
         return super.getBoolean(ENABLED);
     }
 
     public void setEnabled(boolean value) {
         super.append(ENABLED, value);
     }
 
     // ------------------------------------------------------------------------
     //                      p r i v a t e
     // ------------------------------------------------------------------------
     private void init() {
         final String id = MongoUtils.createUUID();
         this.append(ID, id);
         this.append(LANG, IConstants.DEF_LANG);
         this.append(UID, id);
         this.append(COUNTRY_ID, IConstants.DEF_COUNTRY);
         this.append(ENABLED, false);
         this.append(REMOVED, false);
         this.append(USERNAME, "");
         this.append(PASSWORD, "");
         this.append(EMAIL, "");
         this.append(EMAILOFFICE, "");
         this.append(BIRTHDATE, "");
         this.append(COMPANY, "");
         this.append(GENDER, "");
         this.append(IMAGE, "");
         this.append(JOB, "");
         this.append(PHONEHOME, "");
         this.append(PHONEMOBILE, "");
         this.append(PHONEOFFICE, "");
         this.append(PRODUCTS, "");
         this.append(PRODUCT_LINKS, "");
         this.append(REALNAME, "");
         this.append(VAT, "");
         this.append(SMSCOUNTER, "0");
         this.append(SMSLIMIT, "-1");
         this.append(DISCOUNT, 0.0);
     }
 
     // ------------------------------------------------------------------------
     //                      S T A T I C
     // ------------------------------------------------------------------------
     public static String getId(final DBObject item) {
         return MongoUtils.getString(item, IMongoEntityConstants.ID);
     }
 
     public static void setId(final DBObject item, final String value) {
         MongoUtils.put(item, IMongoEntityConstants.ID, value);
     }
 
     public static DBObject getData(final DBObject item) {
         return MongoUtils.getDBObject(item, DATA);
     }
 
     public static void setData(final DBObject item, final DBObject value) {
         MongoUtils.put(item, DATA, value);
     }
 
     public static String getUid(final DBObject item) {
         return MongoUtils.getString(item, UID);
     }
 
     public static void setUid(final DBObject item, final String value) {
         MongoUtils.put(item, UID, value);
     }
 
     public static boolean getEnabled(final DBObject item) {
         return MongoUtils.getBoolean(item, ENABLED);
     }
 
     public static void setEnabled(final DBObject item, final boolean value) {
         MongoUtils.put(item, ENABLED, value);
     }
 
     public static boolean getRemoved(final DBObject item) {
         return MongoUtils.getBoolean(item, REMOVED, false);
     }
 
     public static void setRemoved(final DBObject item, final boolean value) {
         MongoUtils.put(item, REMOVED, value);
     }
 
     public static boolean getFindable(final DBObject item) {
         return MongoUtils.getBoolean(item, FINDABLE, false);
     }
 
     public static void setFindable(final DBObject item, final boolean value) {
         MongoUtils.put(item, FINDABLE, value);
     }
 
     public static String getEmail(final DBObject item) {
         return MongoUtils.getString(item, EMAIL);
     }
 
     public static void setEmail(final DBObject item, final String value) {
         MongoUtils.put(item, EMAIL, value);
     }
 
     public static String getEmailOffice(final DBObject item) {
         return MongoUtils.getString(item, EMAILOFFICE);
     }
 
     public static void setEmailOffice(final DBObject item, final String value) {
         MongoUtils.put(item, EMAILOFFICE, value);
     }
 
     public static String getPassword(final DBObject item) {
         return MongoUtils.getString(item, PASSWORD);
     }
 
     public static void setPassword(final DBObject item, final String value) {
         MongoUtils.put(item, PASSWORD, value);
     }
 
     public static String getLang(final DBObject item) {
         return MongoUtils.getString(item, LANG);
     }
 
     public static String getLang(final DBObject item, final String defaultValue) {
         return MongoUtils.getString(item, LANG, defaultValue);
     }
 
     public static void setLang(final DBObject item, final String value) {
         MongoUtils.put(item, LANG, value);
     }
 
     public static String getCountryId(final DBObject item) {
        final String result = MongoUtils.getString(item, COUNTRY_ID);
        return StringUtils.hasText(result)?result:MongoUtils.getString(item, "countryid");
     }
 
     public static void setCountryId(final DBObject item, final String value) {
         MongoUtils.put(item, COUNTRY_ID, value);
     }
 
     public static DBObject getCountry(final DBObject item) {
         return MongoUtils.getDBObject(item, COUNTRY);
     }
 
     public static void setCountry(final DBObject item, final DBObject value) {
         MongoUtils.put(item, COUNTRY, value);
     }
 
     public static String getUserName(final DBObject item) {
         return MongoUtils.getString(item, USERNAME);
     }
 
     public static void setUserName(final DBObject item, final String value) {
         MongoUtils.put(item, USERNAME, value);
     }
 
     public static String getBirthDate(final DBObject item) {
         return MongoUtils.getString(item, BIRTHDATE);
     }
 
     public static void setBirthdate(final DBObject item, final String value) {
         MongoUtils.put(item, BIRTHDATE, value);
     }
 
     public static String getCompnay(final DBObject item) {
         return MongoUtils.getString(item, COMPANY);
     }
 
     public static void setCompany(final DBObject item, final String value) {
         MongoUtils.put(item, COMPANY, value);
     }
 
     public static String getGender(final DBObject item) {
         return MongoUtils.getString(item, GENDER);
     }
 
     public static void setGender(final DBObject item, final String value) {
         MongoUtils.put(item, GENDER, value);
     }
 
     public static String getImage(final DBObject item) {
         return MongoUtils.getString(item, IMAGE);
     }
 
     public static void setImage(final DBObject item, final String value) {
         MongoUtils.put(item, IMAGE, value);
     }
 
     public static String getJob(final DBObject item) {
         return MongoUtils.getString(item, JOB);
     }
 
     public static void setJob(final DBObject item, final String value) {
         MongoUtils.put(item, JOB, value);
     }
 
     public static String getPhoneHome(final DBObject item) {
         return MongoUtils.getString(item, PHONEHOME);
     }
 
     public static void setPhoneHome(final DBObject item, final String value) {
         MongoUtils.put(item, PHONEHOME, value);
     }
 
     public static String getPhoneMobile(final DBObject item) {
         return MongoUtils.getString(item, PHONEMOBILE);
     }
 
     public static void setPhoneMobile(final DBObject item, final String value) {
         MongoUtils.put(item, PHONEMOBILE, value);
     }
 
     public static String getPhoneOffice(final DBObject item) {
         return MongoUtils.getString(item, PHONEOFFICE);
     }
 
     public static void setPhoneOffice(final DBObject item, final String value) {
         MongoUtils.put(item, PHONEOFFICE, value);
     }
 
     public static String getRealName(final DBObject item) {
         return MongoUtils.getString(item, REALNAME);
     }
 
     public static void setRealName(final DBObject item, final String value) {
         MongoUtils.put(item, REALNAME, value);
     }
 
     public static String getSmsCounter(final DBObject item) {
         return MongoUtils.getString(item, SMSCOUNTER);
     }
 
     public static void setSmsCounter(final DBObject item, final String value) {
         MongoUtils.put(item, SMSCOUNTER, value);
     }
 
     public static String getSmsLimit(final DBObject item) {
         return MongoUtils.getString(item, SMSLIMIT);
     }
 
     public static void setSmsLimit(final DBObject item, final String value) {
         MongoUtils.put(item, SMSLIMIT, value);
     }
 
     public static double getDiscount(final DBObject item) {
         return MongoUtils.getDouble(item, DISCOUNT);
     }
 
     public static void setDiscount(final DBObject item, final double value) {
         MongoUtils.put(item, DISCOUNT, value);
     }
 
     public static String getAtLeastUsername(final DBObject item) {
         if (null != item) {
             final String id = MongoUtils.getString(item, ID);
             final String realname = MongoUtils.getString(item, REALNAME);
             final String username = MongoUtils.getString(item, USERNAME);
             try {
                 final String result = StringUtils.hasText(realname)
                         ? realname
                         : username;
                 if (!result.startsWith("00")) {
                     return result;
                 } else {
                     return "[Reserved]";
                 }
             } catch (Throwable t) {
             }
             return id + "";
         }
         return "";
     }
 
     public static String getFirstname(final DBObject item) {
         final String result = getAtLeastUsername(item);
         final String[] tokens = StringUtils.split(result, " ");
         if (tokens.length > 0) {
             if (tokens.length > 2) {
                 return tokens[0] + " " + tokens[1];
             } else {
                 return tokens[0];
             }
         }
         return result;
     }
 
     public static String getExcerpt(final DBObject item) {
         final StringBuilder result = new StringBuilder();
         final String id = MongoUtils.getString(item, ID);
         final String gender = MongoUtils.getString(item, GENDER);
         final String birthdate = MongoUtils.getString(item, BIRTHDATE);
         final String emailoffice = MongoUtils.getString(item, EMAILOFFICE);
         final String phoneoffice = MongoUtils.getString(item, PHONEOFFICE);
         final String company = MongoUtils.getString(item, COMPANY);
         final String job = MongoUtils.getString(item, JOB);
         // user Name
         final String name = getAtLeastUsername(item);
         if (StringUtils.hasText(name)) {
             StringUtils.append("Username: " + name, result);
         } else {
             StringUtils.append("User ID: " + id, result);
         }
         // Gender
         if (StringUtils.hasText(gender)) {
             StringUtils.append("Gender: " + gender, result);
         }
         // Birthdate
         if (StringUtils.hasText(birthdate)) {
             StringUtils.append("Birthdate: " + birthdate, result);
         }
         // office email
         if (StringUtils.hasText(emailoffice)) {
             StringUtils.append("EMail Office: " + emailoffice, result);
         }
         // Phone Office
         if (StringUtils.hasText(phoneoffice)) {
             StringUtils.append("Phone Office: " + phoneoffice, result);
         }
         // company
         if (StringUtils.hasText(company)) {
             StringUtils.append("Company: " + company, result);
         }
         // job
         if (StringUtils.hasText(job)) {
             StringUtils.append("Job: " + job, result);
         }
 
         return result.toString();
     }
 
     public static String getVat(final DBObject item) {
         return MongoUtils.getString(item, VAT);
     }
 
     public static void setVat(final DBObject item, final String value) {
         MongoUtils.put(item, VAT, value);
     }
 
     public static MongoAddress getAddressBilling(final DBObject item) {
         final DBObject result = MongoUtils.getDBObject(item, ADDRESS_BILLING);
         return new MongoAddress(result);
     }
 
     public static void setAddressBilling(final DBObject item, final DBObject value) {
         MongoUtils.put(item, ADDRESS_BILLING, value);
     }
 
     public static MongoAddress getAddressDelivery(final DBObject item) {
         final DBObject result = MongoUtils.getDBObject(item, ADDRESS_DELIVERY);
         return new MongoAddress(result);
     }
 
     public static void setAddressDelivery(final DBObject item, final DBObject value) {
         MongoUtils.put(item, ADDRESS_DELIVERY, value);
     }
 
     public static List<String> getRoles(final DBObject item) {
         return MongoUtils.getList(item, ROLES);
     }
 
     public static void setRoles(final DBObject item, final List<String> value) {
         MongoUtils.put(item, ROLES, value);
     }
 
     public static void setRoles(final DBObject item, final String commasepRoles) {
         final String[] roles = StringUtils.split(commasepRoles, ",");
         setRoles(item, roles);
     }
 
     public static void setRoles(final DBObject item, final String[] roles) {
         setRoles(item, CollectionUtils.toList(roles));
     }
 
     public static List<DBObject> getProfiles(final DBObject item) {
         return MongoUtils.getList(item, PROFILES);
     }
 
     public static void setProfiles(final DBObject item, final List<DBObject> value) {
         MongoUtils.put(item, PROFILES, value);
     }
 
     public static List<String> getProducts(final DBObject item) {
         return MongoUtils.getList(item, PRODUCTS);
     }
 
     public static void setProducts(final DBObject item, final List<String> value) {
         MongoUtils.put(item, PRODUCTS, value);
     }
 
     public static List<String> getProductLinks(final DBObject item) {
         return MongoUtils.getList(item, PRODUCT_LINKS);
     }
 
     public static void setProductLinks(final DBObject item, final List<String> value) {
         MongoUtils.put(item, PRODUCT_LINKS, value);
     }
 }
