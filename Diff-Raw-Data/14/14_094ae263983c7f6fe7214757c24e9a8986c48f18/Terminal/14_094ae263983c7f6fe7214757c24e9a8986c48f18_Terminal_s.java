 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 
 import akka.actor.UntypedActor;
 
 /**
  * Actor with the sole purpose of receiving messages, formatting them and 
  * printing them to a log. 
  * 
  * @author Andrew Lyne
  */
 public class Terminal extends UntypedActor{
 	
 	private BufferedWriter buff = null;
 	
	
 	public Terminal( String filename ){
 		if(filename == null){
 			buff = new BufferedWriter( new OutputStreamWriter( System.out));
 		}else{
 			try{
 				buff = new BufferedWriter( new FileWriter(filename));
 			}
 			catch(IOException e){
 				System.err.println(e.getMessage());
 				buff = new BufferedWriter( new OutputStreamWriter( System.out));
 			}
 		}
 
 	}
 	public void onReceive( Object message ){
 		if( message instanceof Message){
 			
 			Message content = (Message) message;
 			String toPrint = "";
 			for(int i=0; i < content.getIndentSize(); i++){
 				toPrint = toPrint.concat(" ");
 			}
 			toPrint = toPrint.concat(content.getContents());
 			try{
 				buff.write( toPrint + "\n" );
 			}
 			catch( IOException e ){
 				System.err.println(e.getMessage());
 				System.out.println(toPrint);
 			}
 		}else
 		
 		if(message instanceof EndDay ){
 			try{
				buff.write("Terminal recieved end of say message.");
 				buff.newLine();
 			}
 			catch( IOException e ){
 				System.err.println(e.getMessage());
 			}finally{
 				getContext().stop();
 			}
 		}
 		else{
 			System.err.println("Terminal recieved invalid message: " +
 				message.toString());	
 		}
 	}
 		
     /**
 	 * Override of default stop function in actor. Prints a message to 
 	 * Terminal actor before shutting down.  
 	 */
 	@Override
 	public void postStop() {
 			try {
 				buff.write( "Print terminal shutting down" );
 				buff.newLine();
 				buff.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	}	
 		
 	/**
 	 * Override of default start function in actor. Prints a message to 
 	 * standard out upon start up.  
 	 */
 	@Override
 	public void preStart() {
 			try {
 				buff.write( "Log Terminal Online" );
 				buff.newLine();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	}
 }
 
 
