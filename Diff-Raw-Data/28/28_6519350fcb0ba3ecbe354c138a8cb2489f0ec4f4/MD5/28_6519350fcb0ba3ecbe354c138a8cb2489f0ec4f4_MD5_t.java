 package fm.audiobox.sync.task;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import fm.audiobox.sync.util.AsyncTask;
 
 public class MD5 extends AsyncTask {
 
     private File _file = null;
     private static final int CHUNK = 8192;
    private String result = "";
 
     private static Log log = LogFactory.getLog( MD5.class );
 
     public MD5(File file){
         this._file = file;
     }
 
 
     @Override
     protected synchronized void doTask() {
 
    	FileInputStream fis = null;
		try {
			fis = new FileInputStream( this._file );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
    	
         try {
             MessageDigest digest = MessageDigest.getInstance("MD5");
 
             long file_length = this._file.length();
             long completed = 0;
 
             while ( true ){
 
                 byte[] bytes = new byte[ CHUNK ];
 
                 int read = -1;
 
 
                 if ( ( read = fis.read( bytes ) ) <= 0 ) break;
 
                 if ( read < CHUNK )
                     bytes = Arrays.copyOf( bytes , read);
 
                 digest.update(bytes , 0 ,read );
 
                 completed += read;
                 this.getThreadListener().onProgress( this , file_length , completed, file_length - completed, this._file );
 
 
                 if ( read < CHUNK )
                     break;
             }
 
             byte[] bytes = digest.digest();
             this.result = "";
             for ( byte _byte : bytes ){
             	String _code = Integer.toHexString(0xFF & _byte);
             	if (_code.length() < 2) _code = "0" + _code;
                 this.result += _code;
             }
 
         } catch ( NoSuchAlgorithmException nsae ) {
            log.error( "NoSuchAlgorithm for file: " + this._file.getPath() , nsae );
         } catch ( FileNotFoundException fnfe ) {
        	log.error( "FileNotFound for file: " + this._file.getPath() , fnfe );
         } catch ( IOException ioe ) {
        	log.error( "IO for file: " + this._file.getPath() , ioe );
        } finally {
        	try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
         }
 
     }
 
 
     public synchronized String digest(){
         this.start();
         this.doTask();
         return this.end();
     }
 
 
     @Override
     protected synchronized String end() {
         return this.result.toLowerCase();
     }
 
     @Override
     protected synchronized void start() {
 
     }
 
 
 }
