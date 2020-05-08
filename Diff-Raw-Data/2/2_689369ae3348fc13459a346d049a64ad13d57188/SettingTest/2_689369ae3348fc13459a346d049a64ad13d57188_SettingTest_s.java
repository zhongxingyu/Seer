 /**
  * 
  */
 package org.melati.poem.test;
 
 import java.sql.Date;
 
 import org.melati.poem.PoemThread;
 import org.melati.poem.PoemTypeFactory;
 import org.melati.poem.Setting;
 import org.melati.poem.Setting.SettingTypeMismatchException;
 import org.melati.poem.Setting.SettingValidationException;
 
 /**
  * @author timp
  * 
  */
public class SettingTest extends PoemTestCase {
 
   /**
    * Constructor for SettingTest.
    * 
    * @param name
    */
   public SettingTest(String name) {
     super(name);
   }
 
   /**
    * @see PoemTestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
   }
 
   /**
    * @see PoemTestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
   }
 
   /**
    * @see org.melati.poem.Setting#setValue(String)
    */
   public void testSetValue() {
     Setting stringSetting = getDb().getSettingTable().ensure("stringSetting",
         "set", "String", "A set string setting");
     stringSetting.setValue("new");
     assertEquals("new", stringSetting.getCooked());
     stringSetting.setRaw("new2");
     assertEquals("new2", stringSetting.getCooked());
     stringSetting.delete();
 
     Setting integerSetting = getDb().getSettingTable().ensure("integerSetting",
         12, "Integer Setting", "A set Integer setting");
     integerSetting.setRaw(new Integer(13));
     assertEquals(new Integer(13), integerSetting.getCooked());
     try {
       integerSetting.setValue("ghgh");
       fail("Should have blown up");
     } catch (SettingValidationException e) {
       e = null;
     }
 
     try { 
       integerSetting.setRaw("ghgh");
       fail("Should have blown up");
     } catch (SettingValidationException e) {
       e = null;
     }
     integerSetting.delete();
 
   }
 
   /**
    * @see org.melati.poem.Setting#getValueField()
    */
   public void testGetValueField() {
     Setting s = new Setting(PoemTypeFactory.STRING.getCode(), "testSetting",
             "eggs", "Test Setting", "A test setting");
     getDb().getSettingTable().create(s);
     assertEquals("eggs", s.getValueField().getCooked());
 
     s.delete();
 
     s = new Setting(PoemTypeFactory.PASSWORD.getCode(), "columnSetting",
             "0", "Password", "A test setting");
     getDb().getSettingTable().create(s);
     s.setRaw(getDb().getUserTable().administratorUser().getPasswordField());
     assertEquals("password: FIXME", s.getValueField().getCooked());
     s.delete();
     
 
  
   }
 
   /**
    * @see org.melati.poem.Setting#Setting(Integer, String, String, String,
    *      String)
    */
   public void testSettingIntegerStringStringStringString() {
     Setting s = new Setting(PoemTypeFactory.STRING.getCode(), "testSetting",
             "eggs", "Test Setting", "A test setting");
         getDb().getSettingTable().create(s);
         assertEquals("eggs", s.getValue());
         s.delete();
   }
 
   /**
    * @see org.melati.poem.Setting#setRaw(Object)
    */
   public void testSetRawObject() {
     Setting s = new Setting(PoemTypeFactory.STRING.getCode(), "testSetting",
         "eggs", "Test Setting", "A test setting");
     getDb().getSettingTable().create(s);
     assertEquals("eggs", s.getValue());
     s.delete();
 
     Setting integerSetting = getDb().getSettingTable().ensure("integerSetting",
         12, "Integer", "A set Integer setting");
     integerSetting.setRaw(new Integer(13));
     assertEquals(new Integer(13), integerSetting.getCooked());
     try {
       integerSetting.setRaw("pp");
       fail("Should have bombed.");
     } catch (SettingValidationException e) {
       e = null;
     }
 
     integerSetting.delete();
     
     Setting timestampSetting = getDb().getSettingTable().ensure("timestampSetting",
             PoemTypeFactory.TIMESTAMP, new Date(System.currentTimeMillis()), "Timestamp", "A timestamp setting");
     try { 
       timestampSetting.setRaw("Not a date");
       fail("Should have bombed");
     } catch (SettingValidationException e) { 
       e = null;
     }
     timestampSetting.delete();
   }
 
   /**
    * FIXME Should we be able to change nullability?
    * You cannot change the type, so hard to see how 
    * Exception could be thrown in getRaw().
    * 
    * @see org.melati.poem.Setting#getRaw()
    */
   public void testGetRaw() {
     Setting s = new Setting(PoemTypeFactory.STRING.getCode(), "testSetting",
         "eggs", "Test Setting", "A test setting");
     getDb().getSettingTable().create(s);
     s.setNullable(true);
     //System.err.println("Type:"+ s.getType());
     //System.err.println("Nullable:"+ s.toTypeParameter().getNullable());
     assertEquals("eggs", s.getValue());
     s.setValue(null);
     s.setNullable(false);
     PoemThread.commit();
     //System.err.println("Nullable:"+ s.toTypeParameter().getNullable());
     //System.err.println("Type:"+ s.getType());
     try {
       s.getRaw();
      // fail("Should have blown up.");
     } catch (SettingValidationException e) {
       e.printStackTrace();
       e = null;
     }
     s.delete();
 
   }
 
   /**
    * @see org.melati.poem.Setting#getCooked()
    */
   public void testGetCooked() {
 
   }
 
   /**
    * @see org.melati.poem.Setting#getIntegerCooked()
    */
   public void testGetIntegerCooked() {
     Setting integerSetting = getDb().getSettingTable().ensure("integerSetting",
         12, "Integer", "A set Integer setting");
     integerSetting.setRaw(new Integer(13));
     assertEquals(new Integer(13), integerSetting.getCooked());
     assertEquals(new Integer(13), integerSetting.getIntegerCooked());
     integerSetting.setNullable(true);
     integerSetting.setRaw(null);
     assertNull(integerSetting.getIntegerCooked());
     integerSetting.setNullable(false);
     try {
       integerSetting.getIntegerCooked();
       fail("Should have bombed.");
     } catch (SettingTypeMismatchException e) {
       e = null;
     }
 
     integerSetting.delete();
   }
 
   /**
    * @see org.melati.poem.Setting#getStringCooked()
    */
   public void testGetStringCooked() {
     Setting stringSetting = getDb().getSettingTable().ensure("stringSetting",
         "set", "String", "A set String setting");
     stringSetting.setRaw("v2");
     assertEquals("v2", stringSetting.getCooked());
     assertEquals("v2", stringSetting.getStringCooked());
     stringSetting.setNullable(true);
     stringSetting.setRaw(null);
     assertNull(stringSetting.getStringCooked());
     stringSetting.setNullable(false);
     try {
       stringSetting.getStringCooked();
       fail("Should have bombed.");
     } catch (SettingTypeMismatchException e) {
       e = null;
     }
 
     stringSetting.delete();
 
   }
 
   /**
    * @see org.melati.poem.Setting#getBooleanCooked()
    */
   public void testGetBooleanCooked() {
     Setting booleanSetting = getDb().getSettingTable().ensure("booleanSetting",
         false, "Boolean", "A set boolean setting");
     booleanSetting.setRaw(Boolean.TRUE);
     assertEquals(Boolean.TRUE, booleanSetting.getCooked());
     assertEquals(Boolean.TRUE, booleanSetting.getBooleanCooked());
     booleanSetting.setNullable(true);
     booleanSetting.setRaw(null);
     assertNull(booleanSetting.getBooleanCooked());
     booleanSetting.setNullable(false);
     try {
       booleanSetting.getBooleanCooked();
       fail("Should have bombed.");
     } catch (SettingTypeMismatchException e) {
       e = null;
     }
 
     booleanSetting.delete();
   }
 
   /**
    * @see org.melati.poem.Setting#getValue()
    */
   public void testGetValue() {
     Setting userSetting = getDb().getSettingTable().ensure("userSetting",
         PoemTypeFactory.TROID,
         getDb().guestUser().getTroid(),
         "User", "A User setting");
     assertEquals(new Integer(0),userSetting.getCooked());
     userSetting.delete();
   }
 
 }
