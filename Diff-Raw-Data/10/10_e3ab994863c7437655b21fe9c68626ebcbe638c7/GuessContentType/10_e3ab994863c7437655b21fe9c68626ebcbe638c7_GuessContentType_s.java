 package util;
 
 import java.io.File;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.libs.IO;
 import exception.QuickException;
 
 /**
  * Guess the file content type (text/binary) and the mime-type using 
  * the Linux 'file' tool
  * 
  * @author Paolo Di Tommaso
  *
  */
 public class GuessContentType {
 
 	File fFile;
 	int fResult;
 	String fStdOut;
 	String fStdErr;
 	
 	String fMimeType;
 	String fCharset;
 	
 	
 	GuessContentType() { } // only for test 
 	
 	public GuessContentType( File file ) { 
 		this.fFile = file;
 		
 		
 		String[] cmd = {};
 		try {
			cmd = new String[] { "file", "-bIdk", file.getAbsolutePath() }; 
 			Process p = Runtime.getRuntime().exec(cmd);
 			fResult = p.waitFor();
 			
 			fStdOut = IO.readContentAsString(p.getInputStream());
 			fStdErr = IO.readContentAsString(p.getErrorStream());
 			
 		} 
 		catch (Exception e) {
 			throw new QuickException(e, "Cannot execute command: ", Utils.asString(cmd) );
 		}
 
 		if( fResult != 0 ) { 
 			String err = StringUtils.isNotEmpty(fStdErr) ? fStdErr : fStdOut;
  			throw new QuickException("'file' command terminated with non-zero exit code: %d - %s", fResult, err); 
 		}
 
 		/*
 		 * parse result
 		 */
 		parse(fStdOut);
 	
 	}
 	
 	
 
 	void parse(String value) {
 		if( value == null ) return;
 		
 		int p = value.indexOf(';');
 		if( p == -1 ) { 
 			fMimeType = value;
 			fCharset = null;
 		}
 		else { 
 			fMimeType = value.substring(0,p);
 			String chr = value.substring(p+1).trim();
 			p = chr.indexOf('=');
 			if( p == -1 ) { 
 				fCharset = chr;
 			}
 			else { 
 				fCharset = chr.substring(p+1);
 			}
 			if( StringUtils.isEmpty(fCharset) ) { fCharset = null; }
 		}
 		
 	}
 	
 	
 	public String getMimeType() { 
 		return fMimeType;
 	}
 	
 	public String getCharset() { 
 		return fCharset;
 	}
 	
 	public boolean isText() { 
		return fMimeType != null && fMimeType.startsWith("text/");
 	}
 	
 	public boolean isBinary () { 
 		return !isText();
 	}
 }
