 package dk.statsbiblioteket.doms.updatetracker.webservice;
 
 import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
 import dk.statsbiblioteket.doms.webservices.configuration.ConfigCollection;
 
 import javax.annotation.Resource;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 import java.lang.String;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Update tracker library. Provides upper layers of DOMS with info on changes
  * to objects in Fedora. Used by DOMS Server aka. Central to provide Summa with
  * said info.
  */
 public class UpdateTrackerWebserviceLib implements UpdateTrackerWebservice {
 
     @Resource
     WebServiceContext context;
 
     private DateFormat fedoraFormat = new SimpleDateFormat(
             "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
     private DateFormat alternativefedoraFormat = new SimpleDateFormat(
             "yyyy-MM-dd'T'HH:mm:ss'Z'");
 
     public UpdateTrackerWebserviceLib() throws MethodFailedException {
 
     }
 
     /**
      * Lists the entry objects of views (records) in Fedora, in the given
      * collection, that have changed since the given time.
      *
      * @param collectionPid The PID of the collection in which we are looking
      *                      for changes.
      * @param viewAngle     ...TODO doc
      * @param beginTime     The time since which we are looking for changes.
      * @param state         ...TODO doc
      * @return returns java.util.List<dk.statsbiblioteket.doms.updatetracker
      *         .webservice.PidDatePidPid>
      * @throws MethodFailedException
      * @throws InvalidCredentialsException
      */
     public List<PidDatePidPid> listObjectsChangedSince(
             String collectionPid,
             String viewAngle,
             long beginTime,
             String state,
             Integer offset,
             Integer limit)
 
 
             throws InvalidCredentialsException, MethodFailedException
 
     {
 
         return getModifiedObjects(collectionPid,
                                   viewAngle,
                                   beginTime,
                                   state,
                                   offset,
                                   limit,
                                   false);
     }
 
     public List<PidDatePidPid> getModifiedObjects(String collectionPid,
                                                   String viewAngle,
                                                   long beginTime,
                                                   String state,
                                                   Integer offset,
                                                   Integer limit,
                                                   boolean reverse
     )
             throws InvalidCredentialsException, MethodFailedException {
         List<PidDatePidPid> result = new ArrayList<PidDatePidPid>();
 
         List<String> allEntryObjectsInRadioTVCollection;
         Fedora fedora;
         String fedoralocation = ConfigCollection.getProperties().getProperty(
                 "dk.statsbiblioteket.doms.updatetracker.fedoralocation");
         fedora = new Fedora(getCredentials(), fedoralocation);
 
 
         if (state == null) {
             state = "Published";
         }
         if (state.equals("Published")) {
             state =  "and\n"
                      + "$object <fedora-model:state> <fedora-model:Active> \n";
 
         } else if (state.equals("InProgress")) {
             state =  "and\n"
                      + "$object <fedora-model:state> <fedora-model:Inactive> \n";
         } else if (state.equals("NotDeleted")){
             state =  "and\n"
                    + "( $object <fedora-model:state> <fedora-model:Inactive> \n"
                    + " or \n"
                    + " $object <fedora-model:state> <fedora-model:Active> )\n";
         }
 
 
         String query = "select $object $cm $date\n"
                        + "from <#ri>\n"
                        + "where\n"
                        + "$object <fedora-model:hasModel> $cm\n"
                        + "and\n"
                        + "$cm <http://ecm.sourceforge.net/relations/0/2/#isEntryForViewAngle> '"
                        + viewAngle + "'\n"
                        + "and\n"
                        + "$object <http://doms.statsbiblioteket.dk/relations/default/0/1/#isPartOfCollection> <info:fedora/"
                        + collectionPid + ">\n"
                        + state
                        + "and\n"
                        + "$object <fedora-view:lastModifiedDate> $date \n";
 
 
         if (beginTime != 0){
             String beginTimeDate
                     = fedoraFormat.format(new Date(beginTime));
             query = query + "and \n $date <mulgara:after> '"+beginTimeDate+"'^^<xml-schema:dateTime> in <#xsd> \n";
         }
 
 
         if (reverse){
             query = query + "order by $date desc";
         } else {
             query = query + "order by $date asc";
         }
 
         if (limit != 0) {
             query = query + "\n limit " + limit;
         }
         if (offset != 0) {
             query = query + "\n offset " + offset;
         }
 
 
         try {
             allEntryObjectsInRadioTVCollection
                     = fedora.query(query);
         } catch (BackendInvalidCredsException e) {
             throw new InvalidCredentialsException("Invalid credentials", "", e);
         } catch (BackendMethodFailedException e) {
             throw new MethodFailedException("Method failed", "", e);
         }
 
         for (String line : allEntryObjectsInRadioTVCollection) {
             line = line.trim();
             if (line.isEmpty()){
                 continue;
             }
             String[] splitted = line.split(",");
             String lastModifiedFedoraDate = splitted[2];
             long lastChangedTime;
             try {
                 lastModifiedFedoraDate = normalizeFedoraDate(lastModifiedFedoraDate);
                 lastChangedTime = fedoraFormat.parse(
                         lastModifiedFedoraDate).getTime();
             } catch (ParseException e) {
                 throw new MethodFailedException(
                         "Failed to parse date for object",
                         e.getMessage(),
                         e);
             }
 
             if (lastChangedTime < beginTime) {
                 continue;
             }
 
             PidDatePidPid objectThatChanged = new PidDatePidPid();
             String pid = splitted[0];
             String entryCMPid = splitted[1];
             objectThatChanged.setPid(pid);
             objectThatChanged.setCollectionPid(collectionPid);
             objectThatChanged.setEntryCMPid(entryCMPid);
             objectThatChanged.setLastChangedTime(lastChangedTime);
 
             result.add(objectThatChanged);
         }
 
         return result;
     }
 
     private String normalizeFedoraDate(String lastModifiedFedoraDate) {
        if (lastModifiedFedoraDate.matches(".*\\.\\d{3}Z$")){
             return lastModifiedFedoraDate;
         } else if (lastModifiedFedoraDate.matches(".*\\.\\d{2}Z$")){
             return lastModifiedFedoraDate.substring(0,lastModifiedFedoraDate.length()-1)+"0Z";
         }else if (lastModifiedFedoraDate.matches(".*\\.\\d{1}Z$")){
             return lastModifiedFedoraDate.substring(0,lastModifiedFedoraDate.length()-1)+"00Z";
         }else if (lastModifiedFedoraDate.matches(".*:\\d\\dZ$")){
             return lastModifiedFedoraDate.substring(0,lastModifiedFedoraDate.length()-1)+".000Z";
         }
         return lastModifiedFedoraDate;
     }
 
     /**
      * Return the last time a view/record conforming to the content model of the
      * given content model entry, and in the given collection, has been changed.
      *
      * @param collectionPid The PID of the collection in which we are looking
      *                      for the last change.
      * @param viewAngle     ...TODO doc
      * @return The date/time of the last change.
      * @throws InvalidCredentialsException
      * @throws MethodFailedException
      */
     public long getLatestModificationTime(
             String collectionPid,
             String viewAngle,
             String state)
             throws InvalidCredentialsException, MethodFailedException
     {
 
         List<PidDatePidPid> lastChanged = getModifiedObjects(collectionPid,
                                                              viewAngle,
                                                              0,
                                                              state,
                                                              0,
                                                              1,
                                                              true);
 
         if (!lastChanged.isEmpty()){
             return lastChanged.get(0).getLastChangedTime();
         } else {
             throw new MethodFailedException("Did not find any elements in the collection","No elements in the collection");
         }
     }
 
     /**
      * TODO doc
      *
      * @return TODO doc
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
