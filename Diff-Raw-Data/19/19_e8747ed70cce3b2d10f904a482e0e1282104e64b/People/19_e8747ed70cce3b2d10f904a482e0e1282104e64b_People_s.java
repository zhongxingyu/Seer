 /**
  * People.java
  *
  *  This class is designed to be a collection of all
  * 3 different types of people objects, each type is
  * stored in its own collection.
  *
  * @author Nick DeRossi
  * Group:  Nick DeRossi, Tyler Janowski, Nick Grauel
  */
 
 import java.util.*;
 
 public class People
 {
 
   // An instance ArrayList collection
   //  for each type of Person objects
   private TreeMap< String, Person > doubles;
   private TreeMap< String, Person > others;
 
   /**
    * Constructor for a new People collection
    */
   public People()
   {
     doubles = new TreeMap< String, Person >();
     others = new TreeMap< String, Person >();
   }
 
   /**
    * Method to add a person to the People
    * collection
    *  Parameter is:
    *   Person
    *  Return type:
    *   int
    */
   public int insertPerson( Person p )
   {
     if( p != null )
     {
       switch( p.getType() )
       {
         case 0: others.put( p.getName(), p ); break; 
         case 1: others.put( p.getName(), p );   break;
         case 2: doubles.put( p.getName(), p ); break;
       }
       return( 0 );
     }
     else
       return( 1 );
   }
 
   /**
    * Method to find and remove a Solo
    *  Person object from the collection
    *  the removed object is returned.
    */
   public Person removeSolo( Person p )
   {
     if( p != null )
       return( others.remove( p.getName() ) );
     return( null );
   }
 
   /**
    * Used to be used for the demonstration
    *  but fatal logic flaw in matching
    *  solos before any reservations are
    *  made. DEPRECATED.
    */
   private void tryMatchSolo( Person p )
   {
     // iterate through others collection
     //  if can pair make double
     //   remove partner and form
     //   double object for collection
     //  if cant
     //   insert into others collection
     Collection col = others.values();
     Iterator   itr = col.iterator();
     String gend = p.getGender();
 
     while( itr.hasNext() )
     {
       Person tmp = ( Person )itr.next();
       if( tmp.getType() == 0 )
       {
         if( tmp.getGender().equals( gend ) )
         {
           others.remove( tmp.getName() );
           Double db = new Double( p, tmp );
           doubles.put( db.getName(), db ); 
           return;
         } 
       }      
     }
     others.put( p.getName(), p );
   }
 
   /**
    * Method to search the specific collection
    * of that Person's type to see if they are
    * present, Person object returned on success,
    * NULL on failure.
    */
   public Person findPerson( String name )
   {
     Person temp = others.get( name );
     if( temp == null )
       return( doubles.get( name ) );
     else
       return( temp );
   }
 
   /**
    * Method to return a String representation
    * of the entire People collection.
    */
   public String toString()
   {
     String str = "";
     Collection c1 = doubles.values();
     Iterator itr1 = c1.iterator();
     while( itr1.hasNext() )
       str += itr1.next().toString() + "\n";
     
     Collection c2 = others.values();
     itr1 = c2.iterator();
     while( itr1.hasNext() )
       str += itr1.next().toString() + "\n";
 
     return( str );
   }
 
 }
