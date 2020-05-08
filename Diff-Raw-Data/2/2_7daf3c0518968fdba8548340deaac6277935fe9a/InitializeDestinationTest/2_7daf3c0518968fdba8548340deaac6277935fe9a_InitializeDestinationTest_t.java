 package cz.cvut.fel.init;
 
 import cz.cvut.fel.model.Destination;
 import cz.cvut.fel.util.DatabaseTest;
 import lombok.extern.slf4j.Slf4j;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.util.Date;
 
 import static cz.cvut.fel.util.ArquillianDataProvider.provide;
 import static cz.cvut.fel.utils.DateUtils.date;
 import static org.testng.Assert.*;
 
 /** @author Karel Cemus */
 @Slf4j
@Test( groups = "initialization", dependsOnMethods = "cz.cvut.fel.init.InitializeEmptyDatabaseTest.truncateTable" )
 public class InitializeDestinationTest extends DatabaseTest {
 
     @Test( dataProvider = "destinationProvider" )
     public void insert( String code, String name, Date validUntil ) {
 
         Destination destination = new Destination();
         destination.setCode( code );
         destination.setName( name );
         destination.setValidUntil( validUntil );
 
         log.trace( "Saving '{}'", destination );
         em.persist( destination );
 
         assertFalse( destination.getId() == 0 );
     }
 
     @DataProvider
     public Object[][] destinationProvider() {
         return provide(
                 "InitializeDestinationTest#destinationProvider",
                 new Object[][]{
                         new Object[]{ "PRG", "Prague", null },
                         new Object[]{ "MAD", "Madrid", null },
                         new Object[]{ "LHR", "London (Heathrow)", null },
                         new Object[]{ "INN", "Innsburck", null },
                         new Object[]{ "VIE", "Vienna", null },
                         new Object[]{ "RUS", "Russia", date( 1, 1, 2012 ) },
                         new Object[]{ "PRG", "Prague", date( 1, 2, 2012 ) },
                         new Object[]{ "MAD", "Madrid", date( 1, 1, 2012 ) },
                         new Object[]{ "LHR", "London (Heathrow)", date( 1, 1, 2012 ) },
                 } );
     }
 
     @Test( dataProvider = "invalidDestinationProvider", expectedExceptions = javax.persistence.PersistenceException.class )
     public void insertInvalid( String code, String name, Date validUntil ) {
 
         Destination destination = new Destination();
         destination.setCode( code );
         destination.setName( name );
         destination.setValidUntil( validUntil );
 
         log.trace( "Saving '{}'", destination );
         em.persist( destination );
 
         assertFalse( destination.getId() == 0 );
     }
 
     @DataProvider
     public Object[][] invalidDestinationProvider() {
         return provide(
                 "InitializeDestinationTest#invalidDestinationProvider",
                 new Object[][]{
                         new Object[]{ "PRG", "Prague", null },
                         new Object[]{ "MAD", "Madrid", date( 1, 1, 2012 ) },
                 } );
     }
 }
