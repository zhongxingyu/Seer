 package de.thiemonagel.vegdroid;
 
 import java.util.Date;
 
 // all public members are initialized to non-null values
 public class Venue {
     public  volatile String name              = "";
     public  volatile String shortDescription  = "";
     public  volatile String longDescription   = "";
 
     public  volatile Date   closeDate         = new Date( ~0l>>>1 );
     public  volatile float  rating            = 0.f;
 
     public  volatile String address1          = "";
     public  volatile String address2          = "";
     public  volatile String city              = "";
     public  volatile String postCode          = "";
     public  volatile String neighborhood      = "";
     public  volatile String phone             = "";
     public  volatile String website           = "";
 
     private volatile int    mid               = -1;
 
 
     // for use in geocoding
     public String locString() {
         String loc = "";
         loc += ( address1.equals("") ? "" : (loc.equals("")?"":", ") + address1 );
         loc += ( address2.equals("") ? "" : (loc.equals("")?"":", ") + address2 );
         loc += ( city    .equals("") ? "" : (loc.equals("")?"":", ") + city     );
         loc += ( postCode.equals("") ? "" : (loc.equals("")?"":", ") + postCode );
         return loc;
     }
 
     // for display use
     public String locHtml() {
         String a  = "";
         a += ( address1    .equals("") ? "" : (a.equals("")?"":"<br />") + address1 );
         a += ( address2    .equals("") ? "" : (a.equals("")?"":"<br />") + address2 );
         a += ( city        .equals("") ? "" : (a.equals("")?"":"<br />") + city     );
         a += ( postCode    .equals("") ? "" : (a.equals("")?"":"<br />") + postCode );
         a += ( neighborhood.equals("") ? "" : (a.equals("")?"":"<br />") + "<i>" + neighborhood + "</i>" );
         return a;
     }
 
     public void setId( String uri ) {
         String u = uri.trim();
 
         try {
             // remove trailing slashes
             while ( u.endsWith("/") ) {
                 u = u.substring( 0, u.length()-1 );
             }
            mid = Integer.parseInt( u.substring( u.lastIndexOf('/')+1 ) );
         } catch ( IndexOutOfBoundsException e ) {
             // don't update mid in case of parse error
         } catch ( NumberFormatException e ) {
             // don't update mid in case of parse error
         }
     }
 
     public int getId() throws IllegalStateException {
         if ( mid == -1 )
             throw new IllegalStateException("Id has not been set!");
         return mid;
     }
 }
