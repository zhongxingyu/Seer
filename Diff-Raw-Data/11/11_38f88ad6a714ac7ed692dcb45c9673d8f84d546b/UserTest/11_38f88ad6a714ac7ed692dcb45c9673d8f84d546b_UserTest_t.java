 package drinkcounter.model;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.junit.Before;
 import static org.junit.Assert.*;
 
 public class UserTest {
     private User man80 = new User();
     private User man110 = new User();
     private User woman60 = new User();
     private User woman90 = new User();
 
     private static final float TOLERANCE = 0.1f;
 
     @Before
     public void setUp () {
         man80.setWeight(80);
         man80.setSex(User.Sex.MALE);
 
         man110.setWeight(110);
         man110.setSex(User.Sex.MALE);
 
         woman60.setWeight(60);
         woman60.setSex(User.Sex.FEMALE);
 
         woman90.setWeight(90);
         woman90.setSex(User.Sex.FEMALE);
     }
 
     // Basic tests
     @Test
     public void testBloodAlcohol() {
         User user = new User();
         Drink drink = new Drink();
         user.drink(drink);
         assertEquals((float)AlcoholCalculator.STANDARD_DRINK_ALCOHOL_GRAMS, user.getBloodAlcoholGrams(), 0.1f);
     }
 
     // DRINK COURSES:
     
     // Drink course 1:
     // - One beer (0.33 ml, 4.7%) 30 minutes ago
     private static final float MAN_80_DRINK_COURSE_1 = 0.1f;
     private static final float MAN_110_DRINK_COURSE_1 = 0.0f;
     private static final float WOMAN_60_DRINK_COURSE_1 = 0.2f;
     private static final float WOMAN_90_DRINK_COURSE_1 = 0.1f;
 
     public void drinkCourse1(User user) {
         drinkADrinkNMinutesAgo(user, 30);
     }
     
     @Test
     public void testPromilles_man80_drinkCourse1() {
         drinkCourse1(man80);
         assertEquals(MAN_80_DRINK_COURSE_1, man80.getPromilles(), TOLERANCE);
     }
     
     @Test
     public void testPromilles_man110_drinkCourse1() {
         drinkCourse1(man110);
         assertEquals(MAN_110_DRINK_COURSE_1, man110.getPromilles(), TOLERANCE);
     }
 
     @Test
     public void testPromilles_woman60_drinkCourse1() {
         drinkCourse1(woman60);
         assertEquals(WOMAN_60_DRINK_COURSE_1, woman60.getPromilles(), TOLERANCE);
     }
     
     @Test
     public void testPromilles_woman90_drinkCourse1() {
         drinkCourse1(woman90);
         assertEquals(WOMAN_90_DRINK_COURSE_1, woman90.getPromilles(), TOLERANCE);
     }
 
     // Drink course 2:
     // - One beer (0.33 ml, 4.7%) 60 minutes ago
     // - One beer (0.33 ml, 4.7%) 40 minutes ago
     // - One beer (0.33 ml, 4.7%) 20 minutes ago
     private static final float MAN_80_DRINK_COURSE_2 = 0.4f;
     private static final float MAN_110_DRINK_COURSE_2 = 0.3f;
     private static final float WOMAN_60_DRINK_COURSE_2 = 0.7f;
     private static final float WOMAN_90_DRINK_COURSE_2 = 0.4f;
     
     public void drinkCourse2(User user) {
         drinkADrinkNMinutesAgo(user, 60);
         drinkADrinkNMinutesAgo(user, 40);
         drinkADrinkNMinutesAgo(user, 20);
     }
     
     @Test
     public void testPromilles_man80_drinkCourse2() {
        drinkCourse2(man80);
         assertEquals(MAN_80_DRINK_COURSE_2, man80.getPromilles(), TOLERANCE);
     }
     
     @Test
     public void testPromilles_man110_drinkCourse2() {
        drinkCourse2(man110);
         assertEquals(MAN_110_DRINK_COURSE_2, man110.getPromilles(), TOLERANCE);
     }
 
     @Test
     public void testPromilles_woman60_drinkCourse2() {
        drinkCourse2(woman60);
         assertEquals(WOMAN_60_DRINK_COURSE_2, woman60.getPromilles(), TOLERANCE);
     }
     
     @Test
     public void testPromilles_woman90_drinkCourse2() {
        drinkCourse2(woman90);
         assertEquals(WOMAN_90_DRINK_COURSE_2, woman90.getPromilles(), TOLERANCE);
     }
 
     // Drink course 3:
     // - One beer (0.33 ml, 4.7%) 180 minutes ago
     // - One beer (0.33 ml, 4.7%) 160 minutes ago
     // - One beer (0.33 ml, 4.7%) 140 minutes ago
     // - One beer (0.33 ml, 4.7%) 120 minutes ago
     // - One beer (0.33 ml, 4.7%) 100 minutes ago
     // - One beer (0.33 ml, 4.7%) 80 minutes ago
     private static final float MAN_80_DRINK_COURSE_3 = 0.9f;
     private static final float MAN_110_DRINK_COURSE_3 = 0.6f;
     private static final float WOMAN_60_DRINK_COURSE_3 = 1.5f;
     private static final float WOMAN_90_DRINK_COURSE_3 = 0.9f;
     
     public void drinkCourse3(User user) {
         drinkADrinkNMinutesAgo(user, 180);
         drinkADrinkNMinutesAgo(user, 160);
         drinkADrinkNMinutesAgo(user, 140);
         drinkADrinkNMinutesAgo(user, 120);
         drinkADrinkNMinutesAgo(user, 100);
         drinkADrinkNMinutesAgo(user, 80);
     }
     
     @Test
     public void testPromilles_man80_drinkCourse3() {
         drinkCourse3(man80);
         assertEquals(MAN_80_DRINK_COURSE_3, man80.getPromilles(), TOLERANCE);
     }
     
     @Test
     public void testPromilles_man110_drinkCourse3() {
         drinkCourse3(man110);
         assertEquals(MAN_110_DRINK_COURSE_3, man110.getPromilles(), TOLERANCE);
     }
 
     @Test
     public void testPromilles_woman60_drinkCourse3() {
         drinkCourse3(woman60);
         assertEquals(WOMAN_60_DRINK_COURSE_3, woman60.getPromilles(), TOLERANCE);
     }
     
     @Test
     public void testPromilles_woman90_drinkCourse3() {
         drinkCourse3(woman90);
         assertEquals(WOMAN_90_DRINK_COURSE_3, woman90.getPromilles(), TOLERANCE);
     }  
     
     // Helper functions
     public void drinkADrinkNMinutesAgo(User user, int minutes) {
         Drink drink = new Drink();
         drink.setTimeStamp(new DateTime().minusMinutes(minutes).toDate());
         user.drink(drink);        
     }
 }
