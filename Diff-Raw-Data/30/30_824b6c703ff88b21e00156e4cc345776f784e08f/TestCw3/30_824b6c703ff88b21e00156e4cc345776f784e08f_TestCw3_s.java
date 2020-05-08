 package pl.edu.pjwstk;
 
 import edu.pjwstk.jps.result.IIntegerResult;
 import edu.pjwstk.jps.result.ISingleResult;
 import edu.pjwstk.jps.result.IStringResult;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import pl.edu.pjwstk.datastore.SBAStore;
 import pl.edu.pjwstk.interpreter.qres.QresStack;
 import pl.edu.pjwstk.result.*;
 
 import java.net.URL;
 
 /**
  * Unit test for simple App.
  */
 public class TestCw3
     extends TestCase
 {
     /**
      * Create the test case
      *
      * @param testName name of the test case
      */
     public TestCw3(String testName)
     {
         super( testName );
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite()
     {
         return new TestSuite( TestCw3.class );
     }
 
     /**
      * Plik TestCw3_test1.xml
      *
     *
     *
     *
      <i0, baza, {i1, i2}> (ComplexObject)
      <i1, liczbaTestowa, 10> (IntegerObject)
      <i2, osoba, {i3,i4,i5,i6,i7,i11}> (ComplexObject)
      <i3, imie, „Anna”> (StringObject)
      <i4, nazwisko, „Kowalska”> (StringObject)
      <i5, pensja, 4050.35> (DoubleObject)
      <i6, ubezpieczenie, true> (BooleanObject)
      <i7, adres, {i6, i7, i8}> (ComplexObject)
      <i8, ulica, „Jerozolimskie> (StringObject)
      <i9, kod, „00-000”> (StringObject)
      <i10, miasto, „Warszawa”> (StringObject)
      <i11, ksiazka, {i10, i11}> (ComplexObject)
      <i12, autor, „Adam Mickiewicz”> (StringObject)
      <i13, tytul, „Pan Tadeusz”> (StringObject)
      */
     public void test1(){
        URL url =  getClass().getClassLoader().getResource("TestCw3_test1.xml");
         SBAStore sbaStore = new SBAStore();
         sbaStore.loadXML(url.getPath());
         System.out.println(sbaStore.toString());
         assertTrue(true);
 
     }
 
 
 }
