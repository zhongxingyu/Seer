 package org.sukrupa.student;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 public class StudentClassTest {
     @Test
     public void shouldPromoteStudent(){
 
        assertEquals(StudentClass.PRESCHOOL, StudentClass.DAYCARE.next());
        assertEquals(StudentClass.LKG, StudentClass.PRESCHOOL.next());
         assertEquals(StudentClass.UKG, StudentClass.LKG.next());
         assertEquals(StudentClass.ONE_STD, StudentClass.UKG.next());
         assertEquals(StudentClass.TWO_STD, StudentClass.ONE_STD.next());
         assertEquals(StudentClass.THREE_STD, StudentClass.TWO_STD.next());
         assertEquals(StudentClass.FOUR_STD, StudentClass.THREE_STD.next());
         assertEquals(StudentClass.FIVE_STD, StudentClass.FOUR_STD.next());
         assertEquals(StudentClass.SIX_STD, StudentClass.FIVE_STD.next());
         assertEquals(StudentClass.SEVEN_STD, StudentClass.SIX_STD.next());
         assertEquals(StudentClass.EIGHT_STD, StudentClass.SEVEN_STD.next());
         assertEquals(StudentClass.NINE_STD, StudentClass.EIGHT_STD.next());
         assertEquals(StudentClass.TEN_STD, StudentClass.NINE_STD.next());
     }
 
     @Test
     public void shouldNotPromoteStudentInTEN_STD(){
         assertEquals(StudentClass.TEN_STD, StudentClass.TEN_STD.next());
     }
 
     @Test
     public void shouldCreateAStudentClassFromDisplayName() {
        assertEquals(StudentClass.DAYCARE, StudentClass.fromDisplayName("Daycare"));
         assertEquals(StudentClass.LKG, StudentClass.fromDisplayName("LKG"));
         assertEquals(StudentClass.UKG, StudentClass.fromDisplayName("UKG"));
         assertEquals(StudentClass.ONE_STD, StudentClass.fromDisplayName("1 Std"));
         assertEquals(StudentClass.TWO_STD, StudentClass.fromDisplayName("2 Std"));
         assertEquals(StudentClass.THREE_STD, StudentClass.fromDisplayName("3 Std"));
         assertEquals(StudentClass.FOUR_STD, StudentClass.fromDisplayName("4 Std"));
         assertEquals(StudentClass.FIVE_STD, StudentClass.fromDisplayName("5 Std"));
         assertEquals(StudentClass.SIX_STD, StudentClass.fromDisplayName("6 Std"));
         assertEquals(StudentClass.SEVEN_STD, StudentClass.fromDisplayName("7 Std"));
         assertEquals(StudentClass.EIGHT_STD, StudentClass.fromDisplayName("8 Std"));
         assertEquals(StudentClass.NINE_STD, StudentClass.fromDisplayName("9 Std"));
         assertEquals(StudentClass.TEN_STD, StudentClass.fromDisplayName("10 Std"));
     }
 }
