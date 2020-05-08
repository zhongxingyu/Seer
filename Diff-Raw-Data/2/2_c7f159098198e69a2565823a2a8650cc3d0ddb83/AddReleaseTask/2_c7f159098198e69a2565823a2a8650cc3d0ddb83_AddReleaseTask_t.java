 /*
  * $Id$
  */
package org.xins.util.ant.sourceforge;
 
 import java.io.IOException;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPReply;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 
 
 /**
  * Apache Ant task that creates a new XINS release at the SourceForge site.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.127
  */
 public class AddReleaseTask extends Task {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AddReleaseTask</code>.
     */
    public AddReleaseTask() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Called by the project to let the task do its work.
     *
     * @throws BuildException
     *    if something goes wrong with the build.
     */
    public void execute() throws BuildException {
 
       FTPClient ftp = new FTPClient();
 
       final String REMOTE_HOST = "upload.sourceforge.net";
 
       try {
          log("Connecting to FTP server \"" + REMOTE_HOST + "\".", Project.MSG_DEBUG);
          ftp.connect(REMOTE_HOST);
          log("Connected to FTP server \"" + REMOTE_HOST + "\".", Project.MSG_VERBOSE);
          log("FTP server \"" + REMOTE_HOST + "\" returned reply string \"" + ftp.getReplyString() + "\".", Project.MSG_DEBUG);
 
          log("Setting file type to binary.", Project.MSG_DEBUG);
          ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
          log("Set file type to binary.", Project.MSG_DEBUG);
 
          // Check reply code to verify success
          int reply = ftp.getReplyCode();
 
          if(!FTPReply.isPositiveCompletion(reply)) {
             ftp.disconnect();
             throw new BuildException("FTP server \"" + REMOTE_HOST + "\" refused connection.");
          }
       } catch(IOException e) {
          log("Could not connect to FTP server \"" + REMOTE_HOST + "\" due to " + e.getClass().getName() + ", message is: " + e.getMessage());
          if(ftp.isConnected()) {
             try {
                ftp.disconnect();
             } catch(IOException e2) {
                // Ignore
             }
          }
       }
    }
 }
