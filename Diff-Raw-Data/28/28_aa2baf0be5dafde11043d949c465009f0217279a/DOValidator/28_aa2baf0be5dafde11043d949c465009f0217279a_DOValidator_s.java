 package fedora.server.validation;
 
import fedora.server.errors.ObjectValidityException;
import fedora.server.errors.GeneralException;
 import fedora.server.errors.ServerException;

 import java.io.InputStream;
 import java.io.File;
 
 /**
  *
  * <p><b>Title:</b> DOValidator.java</p>
  * <p><b>Description:</b> Validates a digital object.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author payette@cs.cornell.edu
  * @version $Id$
  */
 public interface DOValidator {
 
   /**
    * <p>Validates a digital object.</p>
    *
    * @param in The digital object provided as a bytestream.
    * @param validationLevel The level of validation to perform on the digital object.
   *        This is an integer from 0-3 with the following meanings:
   *        0 = do all validation levels
   *        1 = perform only XML Schema validation
   *        2 = perform only Schematron Rules validation
   *        3 = perform only referential integrity checks for the object
    * @param phase The stage in the work flow for which the validation should be contextualized.
    *        "ingest" = the object is in the submission format for the ingest stage phase
    *        "store" = the object is in the authoritative format for the final storage phase
    * @throws ServerException If validation fails for any reason.
    */
     public void validate(InputStream in, String format, int validationLevel, String phase)
        //throws ObjectValidityException, GeneralException;
         throws ServerException;
 
   /**
    * <p>Validates a digital object.</p>
    *
    * @param in The digital object provided as a file.
    * @param validationLevel The level of validation to perform on the digital object.
   *        This is an integer from 0-3 with the following meanings:
   *        0 = do all validation levels
   *        1 = perform only XML Schema validation
   *        2 = perform only Schematron Rules validation
   *        3 = perform only referential integrity checks for the object
    * @param phase The stage in the work flow for which the validation should be contextualized.
    *        "ingest" = the object is in the submission format for the ingest stage phase
    *        "store" = the object is in the authoritative format for the final storage phase
    * @throws ServerException If validation fails for any reason.
    */
     public void validate(File in, String format, int validationLevel, String phase)
        //throws ObjectValidityException, GeneralException;
         throws ServerException;
 
 }
