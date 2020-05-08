 package gov.nih.nci.caintegrator.analysis.server;
 
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.IdGroup;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.rosuda.JRclient.REXP;
 import org.rosuda.JRclient.RFileInputStream;
 import org.rosuda.JRclient.RSrvException;
 import org.rosuda.JRclient.Rconnection;
 
 /**
  * This is the base class for all analysis tasks that are implemented in R. This class
  * relies on Rserve (see http://stats.math.uni-augsburg.de/Rserve/) to handle communication with an 
  * R process.
  * 
  * @author harrismic
  *
  */
 
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
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
 	 * @throws AnalysisServerException 
 	 * @throws RSrvException 
 	 */
 	protected void doRvoidEval(String command) throws AnalysisServerException  {
 		if (debugRcommands) {
 			logger.debug(command);
 		}
 		try {
 			rConnection.voidEval(command);
 		} catch (RSrvException e) {
 			logger.error("doRvoidEval threw RSrvException when executing command=" + command);
 			logger.error(e);
 			throw new AnalysisServerException("Internal Error. command=" + command);
 		}
 	}
 
 	/**
 	 * Execute an R command with a return value
 	 * 
 	 * @param c
 	 * @param command
 	 * @return
 	 * @throws AnalysisServerException 
 	 */
 	protected REXP doREval(String command) throws AnalysisServerException {
 		REXP returnVal = null;
 		try {
 			if (debugRcommands) {
 			  logger.debug(command);
 			}
 			returnVal = rConnection.eval(command);
 		} catch (RSrvException e) {
 		  logger.error("doREval threw RSrvException when executing command=" + command);
 		  logger.error(e);
 		  throw new AnalysisServerException("Internal Error. command=" + command);
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
 	 * @param rName The name that R should use for the group
 	 * @param group The group of IDs to use.
 	 * @return A string containing the R command to create the group.
 	 */
 	public static String getRgroupCmd(String rName, IdGroup group) {
 		StringBuffer sb = new StringBuffer();
 		sb.append(rName);
 		sb.append(" <- c(");
 		String id;
		
		if (group != null) {
			for (Iterator i = group.iterator(); i.hasNext();) {
				id = (String) i.next();
				sb.append("\"").append(id).append("\"");
				if (i.hasNext()) {
					sb.append(",");
				} 
 			}
 		}
		sb.append(")");
 		return sb.toString();
 	}
 	
 	
 	/**
 	 * Get the byte representation of the image created with the plot command.
 	 * This code follows the example of how to transfer an image using Rserve in the Rserve examples. 
 	 * 
 	 * @param plotCmd
 	 * @param imgHeight the height of the image to create
 	 * @param imgWidth the width of the image to create
 	 * @return a byte array containing the image. 
 	 * @throws AnalysisServerException 
 	 */
 	public byte[] getImageCode(String plotCmd, int imgHeight, int imgWidth) throws AnalysisServerException {
 
 		byte[] imgCode = new byte[0];
 		
 		String fileName = null;
 
 		try {
 			fileName = "image_" + getRequest().getSessionId() + "_"
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
 			logger.error("Caught IOException in getImageCode. FileName=" + fileName);
 			logger.error(ex);
 			throw new AnalysisServerException("Internal Error. Caught IOException in getImageCode plotCmd=" + plotCmd);
 		} catch (RSrvException e) {
 			logger.error("Caught RSrvException in getImageCode. FileName=" + fileName);
 			logger.error(e);
 			throw new AnalysisServerException("Internal Error. Caught IOException in getImageCode plotCmd=" + plotCmd);
 		} catch (Exception exg) {
 			logger.error("Caught exception in getImageCode. ex=" + exg.getMessage());
 			logger.error(exg);
 			throw new AnalysisServerException("Internal Error. Caught Exception in getImageCode plotCmd=" + plotCmd);
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
 		logger.error("Caught IOException in getBytes method.");
 	    logger.error(ex);
 	  }
 	  byte[] returnArray = byteStream.toByteArray();
 	  logger.debug("getBytes returning numbytes=" + returnArray.length);
 	  return byteStream.toByteArray();
 	}
 }
