 package ikrs.httpd;
 
 import java.util.logging.Level;
 
 import ikrs.typesystem.BasicType;
 import ikrs.util.DefaultCommand;
 
 /**
  * @author Ikaros Kappler
  * @date 2013-01-09
  * @modified 2013-04-17 Ikaros Kappler (shared handler instance added).
  * @version 1.0.0
  **/
 
 
 public class ModuleCommand
     extends DefaultCommand {
 
     /* The constants for the commands (strings) */
     public static final String COMMAND_HEXDUMP       = "HEXDUMP";
     public static final String COMMAND_STATUS        = "STATUS";
 
     private int offset;
 
     /**
      * Create a new LocalCommand (for the yucca/ikrs.httpd command line).
      *
      * @param name The command's name.
      * @param params The command's params.
      **/
     public ModuleCommand( String name,
 			  BasicType[] params,
 			  int offset ) {
 
 	super( name, params );
 
 	this.offset = offset;
 
     }
 
     public int getOffset() {
 	return offset;
     }
 
     public int execute() {
 	
 	if( HTTPHandler.sharedInstance == null ) {
 	    
 	    System.out.println( "Error: cannot run this command because there is no shared HTTPHandler instance." );
 	    return -1;
 	}
 
 	
 
 	if( this.getName().equalsIgnoreCase(ModuleCommand.COMMAND_HEXDUMP) ) {
 
 	    if( this.offset >= this.getParamCount() ) {
 		HTTPHandler.sharedInstance.getLogger().log( Level.WARNING,
 							    getClass().getName() + ".execute()",
 							    "Too few arguments for command '" + this.getName() + "'. Add 'FORMAT <format>'." 
 							    );
 	    }
 	    String param_1 = this.getParamAt( this.offset ).getString();
 	    if( param_1.equalsIgnoreCase("FORMAT") ) {
 
 		if( this.offset+1 >= this.getParamCount() ) {
 		    HTTPHandler.sharedInstance.getLogger().log( Level.WARNING,
 							    getClass().getName() + ".execute()",
 							    "Too few arguments for command '" + this.getName() + " " + param_1 + "'. Add the format string as a coma separated integer list. Example: 8,8,0,8,8,0,0,8,8,0,8,8." 
 							    );
 		    return -3; // implies error
 		}
 		
 		String formatString = CustomUtil.stripQuotes( this.getParamAt( this.offset+1 ).getString() );
 		HTTPHandler.sharedInstance.perform_hexdumpFormat( formatString );
 		return 0;  // implies success
 
 	    } else {
 
 		return -2; // implies error
 
 	    }
 
 	} else if( this.getName().equalsIgnoreCase(ModuleCommand.COMMAND_STATUS) ) {
 
 	    HTTPHandler.sharedInstance.perform_status();	    
 	    return 0;  // implies success
 
 	} else {
 
 	    System.out.println( "Unknown argument '" + this.getName() +"'. Use 'STATUS' instead." );
 
 	    return -1; // imlies error
 
 	}
     }
 
 
 
 }
