 /**
  * Commander.java
  *
  *  This class is designed to be to command interpreter
  * and pareser.  The keyword is initially parsed and
  * the appropriate method is called.
  *
  * @author Nick DeRossi
  * Group:  Nick DeRossi, Tyler Janowski, Nick Grauel
  */
 
 import java.util.*;
 import java.io.*;
 
 public class Commander
 {
   
   // instance variable
   private Scanner s = null;
   private People pers;
   private Providers ps;
   private Tours ts;
   private Offerings os;
 
   /**
    * Constructor for the command processor
    *  Paremeter is a Scanner object
    */
   public Commander( Scanner sc )
   {
     s = sc;
     pers = new People();
     ps = new Providers();
     ts = new Tours();
     os = new Offerings();
   }
 
   /**
    * Method to handle the actual keyword
    *  identification and parsing.
    */
   public void action()
   {
     String input = null;
     while( s.hasNext() )
     {
       input = s.next();
       if( input.equals( "person" ) )
         definePerson();
       else if( input.equals( "provider" ) )
         defineProvider();
       else if( input.equals( "tour" ) )
         defineTour();
       else if( input.equals( "offering" ) )
         defineOffer();
       else if( input.equals( "reservation" ) )
         defineReserv();
       else if( input.equals( "#" ) )
         System.out.println( "#" + s.nextLine() );
       else
         s.nextLine();
     }
     System.out.println( "\nPeople:\n----------------" );
     System.out.println( pers.toString() );
     System.out.println( "Providers:\n---------------" );
     System.out.println( ps.toString() );
     System.out.println( "\nTours:\n---------------" );
     System.out.println( ts.toString() );
     System.out.println( "\nOfferings:\n---------------" );
     System.out.println( os.toString() );
   }
 
   /**
    * Method to parse and define the
    * person object.
    */
   public void definePerson()
   {
     Person p = null;
     String type = s.next();
     String name = s.next();
     int age = s.nextInt();
     String gender = s.next();
     int fitLevel = s.nextInt();
     if( type.equals( "solo" ) )
       p = new Solo( name, age, gender, fitLevel );
     else if( type.equals( "single" ) )
       p = new Single( name, age, gender, fitLevel );
     else if( type.equals( "double" ) )
     {
       String name2 = s.next();
       int age2 = s.nextInt();
       String gender2 = s.next();
       int f2 = s.nextInt();
       Person tmp = new Single( name, age, gender, fitLevel );
       p = new Double( tmp, new Single( name2, age2, gender2, f2 ) );
     }
     else
       System.out.println( "Invalid person type!" );
     pers.insertPerson( p );
     s.nextLine();
   }
   
   /**
    * method to define a provider object
    *  according to the file input
    */
   public void defineProvider()
   {
     String serv = s.next();
     String name = s.next();
     String location = s.next();
     int cap = s.nextInt();
     int opTime = s.nextInt();
     int clTime = s.nextInt();
     int secs = opTime%100;
     opTime = opTime/100;
     int hours = opTime%100;
     GregorianCalendar op = new GregorianCalendar( 2011, 11, 10, hours, secs );
     secs = clTime%100;
     clTime = clTime/100;
     hours = clTime%100;
     GregorianCalendar cl = new GregorianCalendar( 2011, 11, 10, hours, secs );
     Provider p = new Provider( name, serv, location, op, cl, cap );
     ps.add( p );
     // go through offerings and update individual events providers
     
   }
 
   /**
    * Method to handle the parsing and
    *  declaration of a Tour object
    */
   public void defineTour()
   {
     Tour t = null;
     Event e = null;
     String comp = s.next();
     String id = s.next();
     String startLoc = s.next();
     String endLoc = s.next();
     int days = s.nextInt();
     int fit = s.nextInt();
     int cap = s.nextInt();
     int sT = 0;
     int eT = 0;
     int hours = 0;
     int secs = 0;
     s.nextLine();
    s.nextLine();
     t = new Tour( comp, id, startLoc, endLoc, cap, days, fit );
     for( int i = 0; i<days; i++ )
     {
       while( s.hasNext() && s.next().equals( "event" ) )
       {
 
         String tmp = s.next();
 
         if( tmp.equals( "travel" ) )
         {
           String mode = s.next();
           startLoc = s.next();
           endLoc = s.next();
           sT = s.nextInt();
           eT = s.nextInt();
           secs = sT%100;
           sT = sT/100;
           hours = sT%100;
           GregorianCalendar s = new GregorianCalendar( 2011, 11, 10+i, hours, secs );
           secs = eT%100;
           eT = eT/100;
           hours = eT%100;
           GregorianCalendar end = new GregorianCalendar( 2011, 11, 10+i, hours, secs );
           e = new TravelEvent( mode, startLoc, endLoc, s, end, i+1, ps, mode );
         }
         else
         {
           String act = s.next();
           sT = s.nextInt();
           eT = s.nextInt();
           fit = s.nextInt();
           secs = sT%100;
           sT = sT/100;
           hours = sT%100;
           sT = sT/100;
           GregorianCalendar s = new GregorianCalendar( 2011, 11, 10+i, hours, secs );
           secs = eT%100;
           eT = eT/100;
           hours = eT%100;
           GregorianCalendar end = new GregorianCalendar( 2011, 11, 10+i, hours, secs );
           e = new ActivityEvent( act, s, end, fit, i+1, ps, act );
         }
 
         if( t.addEvent( e ) )
           System.err.println( "event Does not conflict!" );
         else
           System.err.println( "event Does conflict" );
        //s.nextLine();
 
       }
      //s.nextLine();
     }
     ts.addTour( t );
   }
 
   /**
    * This method is used to parse and correctly
    *  create, schedule, and store a tour offering.
    */
   private void defineOffer()
   {
     System.err.println( "OFFERING!" );
     String tourId = s.next();
     int sDate = s.nextInt();
     int year = sDate%10000;
     sDate = sDate/10000;
     int day = sDate%100;
     sDate = sDate/100;
     int month = sDate%100;
     Tour t = ts.get( tourId );
     if( t != null )
     {
       Offering o = new Offering( t, year, month, day );
       os.add( o );
       t.addOffering( year, month, day );
     }
     else
       System.err.println( "Tour " + tourId + " has not been defined!" );
     s.nextLine();
   }
 
   /**
    * This method is used to parse and correctly
    *  create, schedule, and store a reservation.
    */
   private void defineReserv()
   {
     System.err.println( "RESERVATION!" );
     String persName = s.next();
     String tourId = s.next();
     int sDate = s.nextInt();
     int year = sDate%10000;
     sDate = sDate/10000;
     int day = sDate%100;
     sDate = sDate/100;
     int mon = sDate%100;
     Person p1 = pers.findPerson( persName );
     if( p1 != null )
     {
       Tour t1 = ts.get( tourId );
       if( t1 != null )
       {
         Offering of = t1.findOffering( year, mon, day );
         if( of != null )
         {
           Reservation r = new Reservation( p1, of );
           if( p1.addReserv( r ) )
             System.err.println( "Successfully added reservation!" );
           else
             System.err.println( "Confliction error with current reservations!" );
         }
       }
       else
         System.err.println( "Tour " + tourId + " has not been defined!" );
     }
     else
       System.err.println( "Person " + persName + " has not been defined!" );
     s.nextLine();
   }
 
 }
