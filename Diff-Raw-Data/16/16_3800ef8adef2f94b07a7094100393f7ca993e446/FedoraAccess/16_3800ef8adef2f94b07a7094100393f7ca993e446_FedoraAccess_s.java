 package fedora.server.access;
 
 import java.util.Calendar;
 import java.util.Vector;
 
 import fedora.server.errors.ServerException;
 import fedora.server.storage.types.MethodDef;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.ObjectMethodsDef;
 import fedora.server.storage.types.Property;
 
 /**
  * <p>Title: FedoraAccess.java</p>
  * <p>Description: Fedora Access Interface. Defines the interface for the
  * Fedora Access API as described in the API-A WSDL. The Access API exposes
  * methods to explore the structure of digital objects in the repository
  * via object reflection and to enable dissemination of behaviors associated
  * with the object.</p>
  *
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 public interface FedoraAccess
 {
 
   /**
    * <p>Gets the persistent identifiers or PIDs of all Behavior Definition
    * objects associated with the specified digital object.</p>
    *
    * @param PID The persistent identifier of the digitla object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return An Array containing the list of Behavior Definition object PIDs.
    */
  public String[] GetBehaviorDefinitions(String PID, Calendar asOfDateTime)
      throws ServerException;
 
   /**
    * <p>Gets the method definitions associated with the specified Behavior
    * Definition object.</p>
    *
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return An Array containing the list of method definitions.
    */
   public MethodDef[] GetBehaviorMethods(String PID, String bDefPID,
       Calendar asOfDateTime) throws ServerException;
 
   /**
    * <p>Gets the method definitions associated with the specified Behavior
    * Definition object.</p>
    *
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return A MIME-typed stream containing the method definitions in the form
    * of an XML fragment obtained from the WSDL in the associated Behavior
    * Mechanism object.
    */
   public MIMETypedStream GetBehaviorMethodsAsWSDL(String PID, String bDefPID,
       Calendar asOfDateTime) throws ServerException;
 
 
   /**
    * <p>Disseminates the content produced by executing the specified method
    * of the associated Behavior Mechanism object of the specified digital
    * object.</p>
    *
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param methodName The name of the method to be executed.
    * @param userParms An array of user-supplied method parameters consisting
    * of name/value pairs.
    * @param asOfDateTime The versioning datetime stamp.
    * @return A MIME-typed stream containing the result of the dissemination.
    */
   public MIMETypedStream GetDissemination (String PID, String bDefPID,
      String methodName, Property[] userParms, Calendar asOfDateTime)
      throws ServerException;
 
   /**
    * <p>Gets a list of all Behavior Definition object PIDs and method names
    * associated with the specified digital object.</p>
    *
    * @param PID The persistent identifier of the digital object
    * @param asOfDateTime The versioning datetime stamp
    * @return An array of all methods associated with the specified
    * digital object.
    */
   public ObjectMethodsDef[] GetObjectMethods (String PID,
        Calendar asOfDateTime) throws ServerException;
 }
