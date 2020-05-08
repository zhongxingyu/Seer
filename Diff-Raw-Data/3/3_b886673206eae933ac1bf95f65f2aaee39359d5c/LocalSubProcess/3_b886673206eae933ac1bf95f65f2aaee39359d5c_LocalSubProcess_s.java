 package de.sectud.ctf07.scoringsystem;
 
 import java.io.IOException;
 
 import de.sectud.ctf07.scoringsystem.ReturnCode.ErrorValues;
 import de.sectud.ctf07.scoringsystem.ReturnCode.Success;
 
 /**
  * A local subprocess.
  * 
  * @author Hans-Christian Esperer
  * @email hc@hcesperer.org
  * 
  */
 public class LocalSubProcess implements SubProcess {
 	/**
 	 * The runtime; cached for performance
 	 */
 	private static Runtime rt = Runtime.getRuntime();
 
 	/**
 	 * TIMEOUT returncode
 	 */
 	private final ReturnCode RETCODE_TIMEOUT = ReturnCode.makeReturnCode(
 			Success.FAILURE, ErrorValues.TIMEOUT);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.sectud.ctf07.scoringsystem.SubProcess#runTestscript(java.lang.String)
 	 */
 	public ServiceStatus runTestscript(String scriptAndParams)
 			throws ExecutionException {
 		Process p;
 		try {
 			p = rt.exec("scripts/" + scriptAndParams);
 		} catch (IOException e) {
 			throw new ExecutionException(e);
 		}
 		CompleteReader cr = new CompleteReader(p.getInputStream());
 		try {
 			int max = 0;
 			ReturnCode retCode = RETCODE_TIMEOUT;
 			while (max++ < 60) {
 				try {
 					int retval = p.exitValue();
 					retCode = ReturnCode.fromOrdinal(retval);
 				} catch (IllegalThreadStateException e) {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e1) {
 						throw new ExecutionException(e1);
 					}
 					retCode = RETCODE_TIMEOUT;
 				}
 			}
			p.destroy();
 			ServiceStatus ss = new ServiceStatus(retCode, cr.getReadData(), -1);
 			return ss;
 		} catch (Throwable t) {
 			t.printStackTrace();
 			try {
 				cr.interrupt();
 			} catch (Throwable t2) {
 			}
 		}
 		return null;
 	}
 }
