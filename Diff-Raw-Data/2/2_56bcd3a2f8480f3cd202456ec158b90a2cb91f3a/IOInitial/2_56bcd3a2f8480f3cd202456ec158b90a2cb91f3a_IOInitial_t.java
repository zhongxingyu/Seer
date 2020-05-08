 package lad.game;
 
 import java.util.List;
 import java.util.ListIterator;
 import lad.data.UserExp;
 import lad.data.UserExpTarget;
 import lad.data.Weapon;
 import lad.db.EXPManager;
 
 /**
  * Handles initial connection with users to the java module.
  *
  * TODO: Create view for user battle statistics
  * TODO: Create view for trainer battle statistics
  * TODO: Create view for granting user exp levels
  * TODO: Add more data to weapon selector dialog
  * TODO: Split EXP view to general/specific weapons
  * TODO: Tests
  *
  * @author msflowers
  */
 public class IOInitial extends MessageHandler
 {
     /**
      * Calls super
      */
     private IOInitial()
     {
         super();
     }
 
     /**
      * Returns singleton
      *
      * @return Singleton.
      */
     public static IOInitial getInstance()
     {
         return IOInitialHolder.INSTANCE;
     }
 
     /**
      * Login piece for comparing to
      */
     private final static MessagePiece
             loginPiece = new MessagePiece( "login" );
 
     /**
      * Piece for viewing the EXP the user has for comparing to
      */
     private final static MessagePiece
             viewexpPiece = new MessagePiece( "viewuserexp" );
 
     /**
      * Piece for getting the JS
      */
     private final static MessagePiece getjsPiece = new MessagePiece( "getJS" );
 
     /**
      * Piece for getting the CSS
      */
     private final static MessagePiece
             getcssPiece = new MessagePiece( "getCSS" );
 
     /**
      * Cached version of the JS
      */
     private static String cachedJS = null;
 
     /**
      * Cached version of the CSS
      */
     private static String cachedCSS = null;
 
     /**
      * Handleable pieces.
      *
      * Piece: loginPiece
      * Piece: viewexpPiece
      * Piece: getjsPiece
      * Piece: getcssPiece
      *
      * @return List with all of the above pieces
      */
     @Override
     public MessageList getPieces()
     {
         MessageList pieces = new MessageList();
         pieces.add( loginPiece );
         pieces.add( viewexpPiece );
         pieces.add( getjsPiece );
         pieces.add( getcssPiece );
         return pieces;
     }
 
     /**
      * Handles pieces based on their pieces
      *
      * @throws InterruptedException Thrown if a sub function throws it
      */
     @Override
     public void handle( MessageList pieces, int userid )
                    throws InterruptedException
     {
 
         if( pieces.contains( loginPiece ) )
         {
             IOTrainer.getInstance().outputMainView( userid );
         }
         else if( pieces.contains( viewexpPiece ) )
         {
             // Simple output of all the EXP's
             List< UserExp > userexp = EXPManager.getExpByUserID( userid );
             ListIterator< UserExp > iter = userexp.listIterator();
             
             write( "$.lad.userexp.overview([" );
 
             while( iter.hasNext() )
             {
                 UserExp curr = iter.next();
                 write( "['" + curr.getTarget().toString() + "','" +
                        curr.getType().toString() + "'," + curr.getLevel() +
                        "," + curr.getExp() + ']');
 
                 if( iter.hasNext() )
                 {
                    write( ",\n" );
                 }
             }
 
             write( "],'userexp');" );
         }
         else if( pieces.contains( getjsPiece ) )
         {
             if( cachedJS == null )
             {
                 String js = readPackagedFile( "lad/files/game.js" );
                 StringBuffer buffer = new StringBuffer( 200 );
 
                 // Weapon strings
                 buffer.append( "return [ " );
                 for( Weapon w : Weapon.values() )
                 {
                     buffer.append( "\"" );
                     buffer.append( w.toString() );
                     buffer.append( "\"," );
                 }
                 buffer.deleteCharAt( buffer.length() - 1 );
                 buffer.append( "];" );
                 js = magicComment( "WEAPON STRING", js, buffer );
 
                 // Weapon objects
                 buffer.setLength( 0 );
                 buffer.append( "return [ " );
                 for( Weapon w : Weapon.values() )
                 {
                     // Type
                     UserExpTarget gen = UserExpTarget.generalFromWeapon( w );
 
                     buffer.append( "this.weapon( \"" );
                     buffer.append( w.toString() );
                     buffer.append( "\"," );
 
                     buffer.append( gen.getValue() );
                     buffer.append( ")," );
                 }
                 buffer.deleteCharAt( buffer.length() -1 );
                 buffer.append( "];" );
                 js = magicComment( "WEAPON OBJECTS", js, buffer );
                 
                 // Cache it
                 cachedJS = js;
             }
 
             // Outputs the js
             write( cachedJS );
             
             // Also include this so that the view works
             write( "addMenuButton('LAD','ui-icon-home',function(){" +
                    "$.lad.window();});" );
         }
         else if( pieces.contains( getcssPiece ) )
         {
             if( cachedCSS == null )
             {
                 cachedCSS = readPackagedFile( "lad/files/game.css" );
             }
             write( cachedCSS );
         }
     }
 
     /**
      * Finds the location of a pair of matching magic comments.
      *
      * Searches the given string for a comment matching //# and the given
      * string.  The second string searched for is one matching //# END and the
      * given string.  The final parameter is replaced into the string. Returns
      * the resulting string.
      *
      * @param needle      String to search for
      * @param haystack    String to search in
      * @param replacement String to replace into
      * @return Resulting string
      */
     private String magicComment( CharSequence needle, String haystack,
                                  CharSequence replacement )
     {
         int startIndex = haystack.indexOf( "//# " + needle );
         String endString = "//# END " + needle;
         int endIndex = haystack.indexOf( endString ) + endString.length();
         CharSequence region = haystack.subSequence( startIndex, endIndex + 1 );
 
         return haystack.replace( region, replacement );
     }
 
     private static class IOInitialHolder
     {
         private static final IOInitial INSTANCE = new IOInitial();
     }
 }
