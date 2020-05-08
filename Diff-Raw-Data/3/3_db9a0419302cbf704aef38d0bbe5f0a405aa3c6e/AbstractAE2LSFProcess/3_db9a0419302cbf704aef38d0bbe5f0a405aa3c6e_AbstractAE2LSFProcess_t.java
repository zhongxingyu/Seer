 package uk.ac.ebi.fgpt.conan.process.ae2;
 
 import uk.ac.ebi.arrayexpress2.exception.exceptions.AE2Exception;
 import uk.ac.ebi.arrayexpress2.exception.manager.ExceptionManager;
 import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
 import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
 
 /**
  * An abstract AE2 specific implementation of an AbstractLSFProcess that remaps LSF exit codes to informative AE2
  * specific error messages.
  *
  * @author Tony Burdett
  * @date 24/06/11
  */
 public abstract class AbstractAE2LSFProcess extends AbstractLSFProcess {
     protected ProcessExecutionException interpretExitValue(int exitValue) {
         if (exitValue == 0) {
             return null;
         }
         else {
             AE2Exception cause = ExceptionManager.getException(getComponentName(), exitValue);
//            String message = "Failed at " + getName() + ": " + cause.getDefaultMessage();
            String message = "Failed at " + getName(); // replaces generic message from ae2 exceptions, binding is usually wrong
             getLog().debug("Generating ProcessExecutionException...\n" +
                                    "exitValue = " + exitValue + ",\n" +
                                    "message = " + message + ",\n" +
                                    "cause = " + cause.getClass().getSimpleName());
             return new ProcessExecutionException(exitValue, message, cause);
         }
     }
 }
