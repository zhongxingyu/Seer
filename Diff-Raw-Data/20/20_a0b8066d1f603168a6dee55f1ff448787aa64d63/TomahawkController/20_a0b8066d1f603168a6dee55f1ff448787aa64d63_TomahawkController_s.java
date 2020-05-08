 package dgu.bufsizing.control;
 
 /**
  * Interacts with the Tomahawk client.
  * @author David Underhill
  */
 public class TomahawkController extends Controller {
     public enum TomahawkCmd {
         CMD_FLOWS((byte)0),
         CMD_BPS((byte)1),
         CMD_INTERVAL((byte)2),
         CMD_EXIT((byte)3);
         
         public final byte code;
         TomahawkCmd( byte code ) { this.code = code; }
     }
     
     public TomahawkController( int port ) {
         super( port );
     }
 
     public String getTypeString() {
         return "Tomahawk";
     }
     
     /**
      * Executes the specified command.
      * 
      * @param cmd        the command to execute
      * @param value      the value to send with this command
     * 
     * @return the value returned by the command (0 if the command is a SET command)
      */
     public synchronized void command( TomahawkCmd cmd, int value ) {
         sendCommand( cmd.code, value );
     }
 }
