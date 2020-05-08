 package dk.statsbiblioteket.doms.updatetracker.webservice;
 
 import dk.statsbiblioteket.doms.webservices.Credentials;
 
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.ws.handler.MessageContext;
 import javax.xml.ws.WebServiceContext;
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import java.lang.String;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 import java.net.MalformedURLException;
 
 /**
  * Update tracker webservice. Provides upper layers of DOMS with info on changes
  * to objects in Fedora. Used by DOMS Server aka. Central to provide Summa with
  * said info.
  */
 @WebService(endpointInterface
         = "dk.statsbiblioteket.doms.updatetracker.webservice"
           + ".UpdateTrackerWebservice")
 public class UpdateTrackerWebserviceImpl implements UpdateTrackerWebservice{
 
     @Resource
     WebServiceContext context;
 
     private XMLGregorianCalendar lastChangedTime;
 
     public UpdateTrackerWebserviceImpl() throws MethodFailedException {
 
 
         GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone(
                 "Europe/Copenhagen"));
         calendar.set(GregorianCalendar.YEAR, 1999);
         calendar.set(GregorianCalendar.MONTH, 12);
         calendar.set(GregorianCalendar.DAY_OF_MONTH, 31);
         calendar.set(GregorianCalendar.HOUR_OF_DAY, 23);
         calendar.set(GregorianCalendar.MINUTE, 59);
         calendar.set(GregorianCalendar.SECOND, 59);
         calendar.set(GregorianCalendar.MILLISECOND, 999);
 
         try {
             lastChangedTime
                     = DatatypeFactory.newInstance().newXMLGregorianCalendar(
                     calendar);
         } catch (DatatypeConfigurationException e) {
             throw new MethodFailedException(
                     "Could not make new XMLGregorianCalendar", "");
         }
     }
 
     /**
      * Lists the entry objects of views (records) in Fedora, in the given
      * collection, that have changed since the given time.
      *
      * @param collectionPid The PID of the collection in which we are looking
      * for changes.
      * @param entryCMPid The PID of the content model which all listed records
      * should adhere to.
      * @param viewAngle ...TODO doc
      * @param beginTime The time since which we are looking for changes.
      * @return returns java.util.List<dk.statsbiblioteket.doms.updatetracker
      * .webservice.PidDatePidPid>
      *
      * @throws MethodFailedException
      * @throws InvalidCredentialsException
      *
      */
     public List<PidDatePidPid> listObjectsChangedSince(
             @WebParam(name = "collectionPid", targetNamespace = "")
             String collectionPid,
             @WebParam(name = "entryCMPid", targetNamespace = "")
             String entryCMPid,
             @WebParam(name = "viewAngle", targetNamespace = "")
             String viewAngle,
             @WebParam(name = "beginTime", targetNamespace = "")
             XMLGregorianCalendar beginTime)
             throws InvalidCredentialsException, MethodFailedException {
 
         // TODO Un-mockup this class please :-)
 
         List<PidDatePidPid> result = new ArrayList<PidDatePidPid>();
 
         // Mockup: If wanted beginTime is AFTER our hardcoded lastChangedTime,
         // we just return no objects/views/records at all.
         if (beginTime.toGregorianCalendar().after(
                 lastChangedTime.toGregorianCalendar())) {
             return result;
         }
         // Mockup: If wanted beginTime is BEFORE (or =) our hardcoded
         // lastChangedTime, connect to ECM and get a list of all entry objects
         // in (hardcoded:) our RadioTVCollection. Return all these.
 
         String pidOfCollection = "doms:RadioTV_Collection";
         List<String> allEntryObjectsInRadioTVCollection;
         ECM ecmConnector;
         try {
             ecmConnector = new ECM(getCredentials(), "http://alhena:7980/ecm");
         } catch (MalformedURLException e) {
             throw new MethodFailedException("Malformed URL", "", e);
         }
 
         // TODO Mockup by calling the getAllEntryObjectsInCollection method in
         // ECM with collectionPID to get <PID, collectionPID, entryPID>.
 
         try {
             allEntryObjectsInRadioTVCollection
                     = ecmConnector.getAllEntryObjectsInCollection(
                    pidOfCollection, "", "");
         } catch (BackendInvalidCredsException e) {
             throw new InvalidCredentialsException("Invalid credentials", "", e);
         } catch (BackendMethodFailedException e) {
             throw new MethodFailedException("Method failed", "", e);
         }
 
         for (String pid : allEntryObjectsInRadioTVCollection) {
             PidDatePidPid objectThatChanged = new PidDatePidPid();
             objectThatChanged.setPid(pid);
             objectThatChanged.setLastChangedTime(lastChangedTime);
             objectThatChanged.setCollectionPid(collectionPid);
             objectThatChanged.setEntryCMPid(entryCMPid);
 
             result.add(objectThatChanged);
         }
 
         return result;
     }
 
     /**
      * Return the last time a view/record conforming to the content model of the
      * given content model entry, and in the given collection, has been changed.
      *
      * @param collectionPid The PID of the collection in which we are looking
      * for the last change.
      * @param entryCMPid The PID of the entry object of the content model which
      * our changed record should adhere to.
      * @param viewAngle ...TODO doc
      * @return The date/time of the last change.
      * @throws InvalidCredentialsException
      * @throws MethodFailedException
      */
     public XMLGregorianCalendar getLatestModificationTime(
             @WebParam(name = "collectionPid", targetNamespace = "")
             String collectionPid,
             @WebParam(name = "entryCMPid", targetNamespace = "")
             String entryCMPid,
             @WebParam(name = "viewAngle", targetNamespace = "")
             String viewAngle)
             throws InvalidCredentialsException, MethodFailedException {
 
         return lastChangedTime;
     }
 
     /**
      * TODO doc
      *
      * @return  TODO doc
      */
     private Credentials getCredentials() {
         HttpServletRequest request = (HttpServletRequest) context
                 .getMessageContext()
                 .get(MessageContext.SERVLET_REQUEST);
         Credentials creds = (Credentials) request.getAttribute("Credentials");
         if (creds == null) {
 //            log.warn("Attempted call at Central without credentials");
             creds = new Credentials("", "");
         }
         return creds;
     }
 
 }
