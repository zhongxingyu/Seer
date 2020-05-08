 package scs.georesults.logic.utils;
 
 import java.util.Enumeration;
 import java.util.Properties;
 import scs.javax.collections.List;
 import scs.javax.io.IOException;
 import scs.javax.rdb.RdbException;
 import scs.javax.web.RequestBeanBase;
 import scs.javax.web.SessionTimeoutException;
 import scs.georesults.GeoDbSession;
 import scs.georesults.GeoException;
 import scs.georesults.common.Constants;
 import scs.georesults.common.ValueLabelPair;
 import scs.georesults.common.szotar.GlobalSzotar;
 import scs.georesults.config.ConfigUtils;
 import scs.georesults.om.kozos.*;
 import scs.georesults.om.verseny.Verseny;
 
 /**
  * <p>Általános beviteli űrlapok működését stámogató osztály.</p>
  */
 public class FormUtils extends RequestBeanBase
 {
 
   /**
    * A program által használt adatbázis-objektum
    */
   private GeoDbSession db;
 
   /**
    * Az aktuális nyelvhez tartozó szótár
    */
   private GlobalSzotar globalSzotar;
 
   public FormUtils ()
   {}
 
   /**
    * Inicializálja az objektumot.
    */
   public void init () throws GeoException, SessionTimeoutException
   {
     this.db = GeoDbSession.getCurrentInstance();
     this.globalSzotar = GlobalSzotar.getCurrentInstance( pageContext );
   }
 
   /**
    * Olyan listát ad vissza, amely a rendszerben szereplő országok kódját
    * és nevüket mint név-felirat párokat tartalmazza.
    */
   public List getOrszagok () throws RdbException
   {
     List src = Orszag.loadAll( db );
     List result = new List( src.size() );
     for ( int i = 0; i < src.size(); ++i ) {
       Orszag o = ( Orszag ) src.get( i );
       String title = globalSzotar.getValue( OrszagImpl.BUNDLE_PREFIX + o.getCountry() );
       result.add( new ValueLabelPair( o.getCountry(), title ) );
     }
     return result;
   }
 
   /**
    * A rendszerben szereplő országok kódjait tartalmazó listát ad vissza
    */
   public List getOrszagKodok () throws RdbException
   {
     List src = Orszag.loadAll( db );
     List result = new List( src.size() );
     for ( int i = 0; i < src.size(); ++i ) {
       Orszag o = ( Orszag ) src.get( i );
       result.add( OrszagImpl.BUNDLE_PREFIX + o.getCountry() );
     }
     return result;
   }
 
   /**
    * Olyan listát ad vissza, amely a rendszerben szereplő nyelvek kódját
    * és nevüket mint név-felirat párokat tartalmazza.
    */
   public List getNyelvek () throws RdbException
   {
     List src = Nyelv.loadAll( db );
     List result = new List( src.size() );
     for ( int i = 0; i < src.size(); ++i ) {
       Nyelv ny = ( Nyelv ) src.get( i );
       String title = globalSzotar.getValue( NyelvImpl.BUNDLE_PREFIX + ny.getLang() );
       result.add( new ValueLabelPair( ny.getLang(), title ) );
     }
     return result;
   }
 
   /**
    * A rendszerben szereplő nyelvek kódjait tartalmazó listát ad vissza
    */
   public List getNyelvKodok () throws RdbException
   {
     List src = Nyelv.loadAll( db );
     List result = new List( src.size() );
     for ( int i = 0; i < src.size(); ++i ) {
       Nyelv o = ( Nyelv ) src.get( i );
       result.add( NyelvImpl.BUNDLE_PREFIX + o.getLang() );
     }
     return result;
   }
 
   /**
    * Az adatbázisban szereplő versenyeket adja vissza
    */
   public List getVersenyek () throws GeoException, RdbException
   {
     return Verseny.loadAll( db );
   }
 
   /**
    * A rendszerben telepített sablonok listáját adja vissza név-felirat
    * párok formájában, ahol a név a sablont tartalmazó fájl neve.
    */
   public List getSablonok () throws IOException, SessionTimeoutException
   {
     try {
       List results = new List();
      Properties sablonok = ConfigUtils.loadProperties( "sablonok.properties" );
       for ( Enumeration en = sablonok.propertyNames(); en.hasMoreElements(); ) {
         String filename = ( String ) en.nextElement();
         String title = sablonok.getProperty( filename );
         if ( title.startsWith( "@" ) ) {
           title = globalSzotar.resolve( pageContext, title.substring( 1 ) );
         }
         results.add( new ValueLabelPair( filename, title ) );
       }
       return results;
     }
     catch ( java.io.IOException ex ) {
       throw new IOException( ex );
     }
   }
 
   /**
    * A sorrendfüggő feladat ellenőrzésének módját adja vissza. Az ellenőrzési módot a HTTP kérés paramétereként keresi, és ha szükséges, 1000-et levon az értékéből. Ha nem talál ilyen paramétert,
    * akkor "nincs ellenőrzés" móddal tér vissza
    */
   public int getCheckmode ()
   {
     String param = pageContext.getRequest().getParameter( "ellenorzesTipus" );
     if ( param != null ) {
       int ellenorzesTipus = Integer.parseInt( param );
       if ( ellenorzesTipus >= 1000 ) ellenorzesTipus -= 1000;
       return ellenorzesTipus;
     } else return Constants.ETAPFELADAT_ELLENORZES_TIPUS_NINCS;
   }
 
 }
