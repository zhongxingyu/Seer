 package gov.nih.nci.caintegrator.analysis.server;
 
import gov.nih.nci.caintegrator.analysis.messaging.AnalysisRequest;
import gov.nih.nci.caintegrator.analysis.messaging.IdGroup;

import java.io.ByteArrayOutputStream;
 import java.io.IOException;
import java.io.InputStream;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.rosuda.JRclient.REXP;
 import org.rosuda.JRclient.RFileInputStream;
 import org.rosuda.JRclient.RSrvException;
 import org.rosuda.JRclient.Rconnection;
 
 public abstract class AnalysisTaskR extends AnalysisTask {
 
 	private Rconnection rConnection = null;
 
 	private boolean debugRcommands = false;
 	
 	private static Logger logger = Logger.getLogger(AnalysisTaskR.class);
 
 	public AnalysisTaskR(AnalysisRequest request) {
 		super(request);
 	}
 
 	public AnalysisTaskR(AnalysisRequest request, boolean debugRcommands) {
 		super(request);
 		this.debugRcommands = debugRcommands;
 	}
 
 	/**
 	 * The run method implemented in each of the subclasses will call
 	 * getRconnection() to get a connection to the Rserve and perform the
 	 * computation.
 	 */
 	public abstract void run();
 
 	public void setRconnection(Rconnection connection) {
 		this.rConnection = connection;
 	}
 
 	public Rconnection getRconnection() {
 		return rConnection;
 	}
 
 	public boolean getDebugRcommands() {
 		return debugRcommands;
 	}
 
 	public void setDebugRcommands(boolean debugRcommands) {
 		this.debugRcommands = debugRcommands;
 	}
 
 	/**
 	 * Evaluate an R command with no return value
 	 * 
 	 * @param c
 	 * @param command
 	 */
 	protected void doRvoidEval(String command) {
 		if (debugRcommands) {
 			logger.debug(command);
 		}
 		try {
 			rConnection.voidEval(command);
 		} catch (RSrvException e) {
 			logger.error(e);
 		}
 	}
 
 	/**
 	 * Execute an R command with a return value
 	 * 
 	 * @param c
 	 * @param command
 	 * @return
 	 */
 	protected REXP doREval(String command) {
 		REXP returnVal = null;
 		try {
 			if (debugRcommands) {
 			  logger.debug(command);
 			}
 			returnVal = rConnection.eval(command);
 		} catch (RSrvException e) {
 		  logger.error(e);
 		}
 		return returnVal;
 	}
 
 	public static String getQuotedString(String inputStr) {
 		return "\"" + inputStr + "\"";
 	}
 
 	/**
 	 * This method will take a SampleGroup and generate the R command for to
 	 * create the sampleId list. The returned lists can then be used as input
 	 * parameters to the statistical methods (for example ttest).
 	 * 
 	 * @param group
 	 * @return
 	 */
 	public static String getRgroupCmd(String rName, IdGroup group) {
 		StringBuffer sb = new StringBuffer();
 		sb.append(rName);
 		sb.append(" <- c(");
 		String id;
 		for (Iterator i = group.iterator(); i.hasNext();) {
 			id = (String) i.next();
 			sb.append("\"").append(id).append("\"");
 			if (i.hasNext()) {
 				sb.append(",");
 			} else {
 				sb.append(")");
 			}
 		}
 		return sb.toString();
 	}
 	
 	
 	/**
 	 * Get the byte representation of the image created with the plot command.
 	 * May be able to change this to return a Java Image object.  This code follows the
 	 * example of how to transfer an image using Rserve in the Rserve examples. 
 	 * 
 	 * @param request
 	 * @param plotCmd
 	 * @return
 	 */
 	public byte[] getImageCode(String plotCmd, int imgHeight, int imgWidth) {
 
 		byte[] imgCode = new byte[0];
 
 		try {
 			String fileName = "image_" + getRequest().getSessionId() + "_"
 					+ System.currentTimeMillis() + ".png";
			
		    fileName = fileName.replace(' ','_');  //should never have spaces but just to be sure
			
 			REXP xp = null;
 
 			xp = doREval("try(bitmap(\"" + fileName
 					+ "\", height = " + imgHeight + ", width = " + imgWidth + ", res = 72 ))");
 
 			if (xp.asString() != null) { // if there's a string then we have
 											// a problem, R sent an error
 				logger.error("Problem getting the graphics device:\n"
 						+ xp.asString());
 				// this is analogous to 'warnings', but for us it's sufficient
 				// to get just the 1st warning
 				REXP w = doREval("if (exists(\"last.warning\") && length(last.warning)>0) names(last.warning)[1] else 0");
 				if (w.asString() != null)
 					logger.warn(w.asString());
 				return new byte[0];
 			}
 
 			//do the plot
 			doRvoidEval(plotCmd);
 			doRvoidEval("dev.off()");
 
 			RFileInputStream is = rConnection.openFile(fileName);
 			imgCode = getBytes(is);
 			is.close();
 			rConnection.removeFile(fileName);
 		} catch (IOException ex) {
 			logger.error(ex);
 		} catch (RSrvException e) {
 			logger.error(e);
 		}
 
 		logger.info("getImageCode returning image numBytes=" + imgCode.length);
 		return imgCode;
 
 	}
 
 	/**
 	 * Get an array of bytes from a stream
 	 * @param is
 	 * @return
 	 */
 	private byte[] getBytes(InputStream is) {
 	  ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
 	  int numRead = -1;
 	  try {
 		  //using the buffer size from the Rserve example. Not sure why they are
 		  //setting it to this value.
 		  byte[] buff = new byte[65536];
 		  while ((numRead = is.read(buff)) != -1) {
 		    byteStream.write(buff, 0, numRead);
 		  }
 	  }
 	  catch (IOException ex) {
 	    logger.error(ex);
 	  }
 	  byte[] returnArray = byteStream.toByteArray();
 	  logger.debug("getBytes returning numbytes=" + returnArray.length);
 	  return byteStream.toByteArray();
 	}
 }
