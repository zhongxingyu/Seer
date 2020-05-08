 package proai.service;
 
 import java.util.*;
 import java.text.*;
 import org.apache.log4j.*;
 
 import proai.*;
 import proai.cache.*;
 import proai.error.*;
 
 /**
  * Provides transport-neutral responses to OAI-PMH requests.
  *
  * <p>A single <code>Responder</code> instance handles multiple concurrent
  * OAI-PMH requests and provides responses without regard to the transport
  * protocol.
  *
  * <p>Responses are provided via <code>ResponseData</code> objects that can
  * write their XML to a given PrintWriter.  The XML provided does not include
  * an XML declaration or an OAI-PMH response header -- this is the
  * responsibility of higher-level application code.
  *
  * <p>At this level, errors are signaled by exceptions.
  * Serializing them for transport is the responsibility of higher-level
  * application code.
  *
  * @author Chris Wilper
  */
 public class Responder {
 
     private static final String _PFX = "proai.";
 
     public static final String PROP_INCOMPLETESETLISTSIZE = _PFX + "incompleteSetListSize";
     public static final String PROP_INCOMPLETERECORDLISTSIZE = _PFX + "incompleteRecordListSize";
     public static final String PROP_INCOMPLETEIDENTIFIERLISTSIZE = _PFX + "incompleteRecordListSize";
 
     public static final String ERR_MISSING_IDENTIFIER  = "identifier must be specified";
     public static final String ERR_MISSING_PREFIX      = "metadataPrefix must be specified";
     public static final String ERR_ITEM_DOESNT_EXIST   = "the indicated item does not exist";
     public static final String ERR_BAD_FORMAT_FOR_ITEM = "the indicated item does not support that metadata format";
     public static final String ERR_NO_FORMATS_FOR_ITEM = "the indicated item has no metadata formats";
     public static final String ERR_NO_SET_HIERARCHY = "there are no sets in the repository";
     public static final String ERR_RESUMPTION_EXCLUSIVE = "the resumptionToken argument may only be specified by itself";
     public static final String ERR_DATE_FORMAT = "specified date is not syntactically valid";
     public static final String ERR_FROM_UNTIL = "from date cannot be greater than until date";
     public static final String ERR_NO_SUCH_FORMAT = "the metadataPrefix is unrecognized";
     public static final String ERR_NO_RECORDS_MATCH = "no records match your selection criteria";
 
     private static final Logger logger =
             Logger.getLogger(Responder.class.getName());
 
     private RecordCache m_cache;
 
     private int m_incompleteIdentifierListSize;
     private int m_incompleteRecordListSize;
     private int m_incompleteSetListSize;
 
     private SessionManager m_sessionManager;
 
     public Responder(Properties props) throws ServerException {
         init(new RecordCache(props),
              new SessionManager(props),
              nonNegativeValue(props, PROP_INCOMPLETEIDENTIFIERLISTSIZE, true),
              nonNegativeValue(props, PROP_INCOMPLETERECORDLISTSIZE, true),
              nonNegativeValue(props, PROP_INCOMPLETESETLISTSIZE, true));
     }
 
     private int nonNegativeValue(Properties props, String name, boolean nonZero) throws ServerException {
         String v = props.getProperty(name);
         if (v == null) throw new ServerException("Required property missing: " + name);
         try {
             int val = Integer.parseInt(v);
             if (val < 0) throw new ServerException("Property value cannot be negative: " + name);
             if (nonZero && val == 0) {
                 throw new ServerException("Property value cannot be zero: " + name);
             }
             return val;
         } catch (NumberFormatException e) {
             throw new ServerException("Bad integer '" + v + "' specified for property: " + name);
         }
     }
 
     public Responder(RecordCache cache,
                      SessionManager sessionManager,
                      int incompleteIdentifierListSize,
                      int incompleteRecordListSize,
                      int incompleteSetListSize) throws ServerException {
         init(cache, 
              sessionManager,
              incompleteIdentifierListSize,
              incompleteRecordListSize,
              incompleteSetListSize);
     }
 
     private void init(RecordCache cache, 
                       SessionManager sessionManager,
                       int incompleteIdentifierListSize,
                       int incompleteRecordListSize,
                       int incompleteSetListSize) {
         m_cache = cache;
         m_sessionManager = sessionManager;
         m_incompleteIdentifierListSize = incompleteIdentifierListSize;
         m_incompleteRecordListSize = incompleteRecordListSize;
         m_incompleteSetListSize = incompleteSetListSize;
     }
 
     /**
      * Get the response for a GetRecord request.
      *
      * @param identifier the item identifier.
      * @param metadataPrefix the format of the record (oai_dc, etc).
      * @throws BadArgumentException if either of the required parameters are null.
      * @throws CannotDisseminateFormatException if the value of the metadataPrefix 
      *         argument is not supported by the item identified by the value of the 
      *         identifier argument.
      * @throws IdDoesNotExistException if the value of the identifier argument is 
      *         unknown or illegal in this repository.
      * @throws ServerException if a low-level (non-protocol) error occurred.
      */
     public ResponseData getRecord(String identifier,
                                   String metadataPrefix) 
             throws BadArgumentException,
                    CannotDisseminateFormatException,
                    IdDoesNotExistException,
                    ServerException {
 
         if (logger.isDebugEnabled()) {
             logger.debug("Entered getRecord(" + q(identifier) + ", " 
                                               + q(metadataPrefix) + ")");
         }
 
         try {
 
             checkIdentifier(identifier);
             checkMetadataPrefix(metadataPrefix);
 
             Writable content = m_cache.getRecordContent(identifier, metadataPrefix);
 
             if ( content == null ) {
                 checkItemExists(identifier);
                 throw new CannotDisseminateFormatException(ERR_BAD_FORMAT_FOR_ITEM);
             } else {
                 ResponseDataImpl data = new ResponseDataImpl(content);
                 return data;
             }
         } finally {
             if (logger.isDebugEnabled()) {
                 logger.debug("Exiting getRecord(" + q(identifier) + ", " 
                                                   + q(metadataPrefix) + ")");
             }
         }
     }
 
     /**
      * Get the response for an Identify request.
      *
      * @throws ServerException if a low-level (non-protocol) error occurred.
      */
     public ResponseData identify() throws ServerException {
 
         logger.debug("Entered identify()");
 
         try {
             ResponseData data = new ResponseDataImpl(m_cache.getIdentifyContent());
             return data;
         } finally {
             logger.debug("Exiting identify()");
         }
     }
 
     /**
      * Get the response for a ListIdentifiers request.
      *
      * @param from optional UTC date specifying a lower bound for 
      *        datestamp-based selective harvesting.
      * @param until optional UTC date specifying an upper bound for
      *        datestamp-based selective harvesting.
      * @param metadataPrefix specifies that headers should be returned 
      *        only if the metadata format matching the supplied metadataPrefix 
      *        is available (or has been deleted).
      * @param set optional argument with a setSpec value, which specifies 
      *        set criteria for selective harvesting.
      * @param resumptionToken exclusive argument with a value that is the 
      *        flow control token returned by a previous ListIdentifiers 
      *        request that issued an incomplete list.
      * @throws BadArgumentException if resumptionToken is specified with
      *         any other parameters, or if resumptionToken is unspecified
      *         and any required parameters are not.
      * @throws BadResumptionTokenException if the value of the resumptionToken 
      *         argument is invalid or expired.
      * @throws CannotDisseminateFormatException if the value of the 
      *         metadataPrefix argument is not supported by the repository.
      * @throws NoRecordsMatchException if the combination of the values of 
      *         the from, until, and set arguments results in an empty list.
      * @throws NoSetHierarchyException if set is specified and the repository 
      *         does not support sets.
      * @throws ServerException if a low-level (non-protocol) error occurred.
      */
     public ResponseData listIdentifiers(String from,
                                         String until,
                                         String metadataPrefix,
                                         String set,
                                         String resumptionToken) 
             throws BadArgumentException,
                    BadResumptionTokenException,
                    CannotDisseminateFormatException,
                    NoRecordsMatchException,
                    NoSetHierarchyException,
                    ServerException {
 
         if (logger.isDebugEnabled()) {
             logger.debug("Entered listIdentifiers(" + q(from) + ", "
                                                     + q(until) + ", "
                                                     + q(metadataPrefix) + ", "
                                                     + q(set) + ", "
                                                     + q(resumptionToken) + ")");
         }
 
         try {
             return listRecords(from, until, metadataPrefix, set, 
                     resumptionToken, true, m_incompleteIdentifierListSize);
         } finally {
             if (logger.isDebugEnabled()) {
                 logger.debug("Exiting listIdentifiers(" + q(from) + ", "
                                                         + q(until) + ", "
                                                         + q(metadataPrefix) + ", "
                                                         + q(set) + ", "
                                                         + q(resumptionToken) + ")");
             }
         }
     }
 
     private ResponseData listRecords(String from,
                                      String until,
                                      String metadataPrefix,
                                      String set,
                                      String resumptionToken,
                                      boolean identifiersOnly,
                                      int incompleteListSize)
             throws BadArgumentException,
                    BadResumptionTokenException,
                    CannotDisseminateFormatException,
                    NoRecordsMatchException,
                    NoSetHierarchyException,
                    ServerException {
 
         if (resumptionToken == null) {
 
             Date fromDate = validDate(from);
             Date untilDate = validDate(until);
            checkGranularity(from, until);
             checkFromUntil(fromDate, untilDate);
             checkMetadataPrefix(metadataPrefix);
             ListProvider provider = new RecordListProvider(m_cache,
                                                            incompleteListSize,
                                                            identifiersOnly,
                                                            fromDate,
                                                            untilDate,
                                                            metadataPrefix,
                                                            set);
             return m_sessionManager.list(provider);
         } else {
             if (from != null || until != null || metadataPrefix != null || set != null) {
                 throw new BadArgumentException(ERR_RESUMPTION_EXCLUSIVE);
             }
             return m_sessionManager.getResponseData(resumptionToken);
         }
     }
 
     /**
      * Get the response for a ListMetadataFormats request.
      *
      * @param identifier an optional argument that specifies the unique 
      *        identifier of the item for which available metadata formats 
      *        are being requested. If this argument is omitted, then the 
      *        response includes all metadata formats supported by this 
      *        repository.
      * @throws IdDoesNotExistException if the value of the identifier 
      *         argument is unknown or illegal in this repository.
      * @throws NoMetadataFormats there are no metadata formats available 
      *         for the specified item.
      * @throws ServerException if a low-level (non-protocol) error occurred.
      */
     public ResponseData listMetadataFormats(String identifier)
             throws IdDoesNotExistException,
                    NoMetadataFormatsException,
                    ServerException {
 
         if (logger.isDebugEnabled()) {
             logger.debug("Entered listMetadataFormats(" + q(identifier) + ")");
         }
 
         try {
             Writable content = null;
             content = m_cache.getMetadataFormatsContent(identifier);
             if (content == null && identifier != null) {
                 checkItemExists(identifier);
                 throw new NoMetadataFormatsException(ERR_NO_FORMATS_FOR_ITEM);
             }
             ResponseData data = new ResponseDataImpl(content);
             return data;
         } finally {
             if (logger.isDebugEnabled()) {
                 logger.debug("Exiting listMetadataFormats(" + q(identifier) + ")");
             }
         }
     }
 
     /**
      * Get the response for a ListRecords request.
      *
      * @param from optional UTC date specifying a lower bound for 
      *        datestamp-based selective harvesting.
      * @param until optional UTC date specifying an upper bound for
      *        datestamp-based selective harvesting.
      * @param metadataPrefix specifies that records should be returned 
      *        only if the metadata format matching the supplied metadataPrefix 
      *        is available (or has been deleted).
      * @param set optional argument with a setSpec value, which specifies 
      *        set criteria for selective harvesting.
      * @param resumptionToken exclusive argument with a value that is the 
      *        flow control token returned by a previous ListIdentifiers 
      *        request that issued an incomplete list.
      * @throws BadArgumentException if resumptionToken is specified with
      *         any other parameters, or if resumptionToken is unspecified
      *         and any required parameters are not.
      * @throws BadResumptionTokenException if the value of the resumptionToken 
      *         argument is invalid or expired.
      * @throws CannotDisseminateFormatException if the value of the 
      *         metadataPrefix argument is not supported by the repository.
      * @throws NoRecordsMatchException if the combination of the values of 
      *         the from, until, and set arguments results in an empty list.
      * @throws NoSetHierarchyException if set is specified and the repository 
      *         does not support sets.
      * @throws ServerException if a low-level (non-protocol) error occurred.
      */
     public ResponseData listRecords(String from,
                                     String until,
                                     String metadataPrefix,
                                     String set,
                                     String resumptionToken)
             throws BadArgumentException,
                    BadResumptionTokenException,
                    CannotDisseminateFormatException,
                    NoRecordsMatchException,
                    NoSetHierarchyException,
                    ServerException {
 
         if (logger.isDebugEnabled()) {
             logger.debug("Entered listRecords(" + q(from) + ", "
                                                 + q(until) + ", "
                                                 + q(metadataPrefix) + ", "
                                                 + q(set) + ", "
                                                 + q(resumptionToken) + ")");
         }
         try {
             return listRecords(from, until, metadataPrefix, set, 
                     resumptionToken, false, m_incompleteRecordListSize);
         } finally {
             if (logger.isDebugEnabled()) {
                 logger.debug("Exiting listRecords(" + q(from) + ", "
                                                     + q(until) + ", "
                                                     + q(metadataPrefix) + ", "
                                                     + q(set) + ", "
                                                     + q(resumptionToken) + ")");
             }
         }
     }
 
     /**
      * Get the response for a ListSets request.
      *
      * @param resumptionToken exclusive argument with a value that is the 
      *        flow control token returned by a previous ListSets request that 
      *        issued an incomplete list.
      * @throws BadResumptionTokenException if the value of the resumptionToken 
      *         argument is invalid or expired.
      * @throws NoSetHierarchyException if the repository does not support sets.
      * @throws ServerException if a low-level (non-protocol) error occurred.
      */
     public ResponseData listSets(String resumptionToken)
             throws BadResumptionTokenException,
                    NoSetHierarchyException,
                    ServerException {
 
         if (logger.isDebugEnabled()) {
             logger.debug("Entered listSets(" + q(resumptionToken) + ")");
         }
         try {
             if (resumptionToken == null) {
                 ListProvider provider = new SetListProvider(m_cache, m_incompleteSetListSize);
                 return m_sessionManager.list(provider);
             } else {
                 return m_sessionManager.getResponseData(resumptionToken);
             }
         } finally {
             if (logger.isDebugEnabled()) {
                 logger.debug("Exiting listSets(" + q(resumptionToken) + ")");
             }
         }
     }
 
     private static String q(String s) {
         if (s == null) {
             return null;
         } else {
             return "\"" + s + "\"";
         }
     }
 
     /**
      * Release any resources held by the session manager and the cache.
      */
     public void close() throws ServerException {
         m_sessionManager.close();
         m_cache.close();
     }
 
     ///////////////////////////////////////////////////////////////////////////
 
     /**
      * Throw a <code>BadArgumentException<code> if <code>identifier</code>
      * is <code>null</code> or empty.
      */
     private static void checkIdentifier(String identifier) throws BadArgumentException {
         if (identifier == null || identifier.length() == 0) {
             throw new BadArgumentException(ERR_MISSING_IDENTIFIER);
         }
     }
 
     /**
      * Throw a <code>BadArgumentException<code> if <code>metadataPrefix</code>
      * is <code>null</code> or empty.
      */
     private static void checkMetadataPrefix(String metadataPrefix) throws BadArgumentException {
         if (metadataPrefix == null || metadataPrefix.length() == 0) {
             throw new BadArgumentException(ERR_MISSING_PREFIX);
         }
     }
 
     /**
      * Parse and return a <code>Date</code> for the given string, or return
      * <code>null</code> if the string is <code>null</code>.
      *
      * Validation ensures that the string is in one of the two formats
      * recognized by the protocol.
      */
     private static Date validDate(String dateString) throws BadArgumentException {
         if (dateString == null) return null; 
         DateFormat parser;
         if (dateString.length() == 10) {
             parser = new SimpleDateFormat("yyyy-MM-dd");
         } else {
             parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         }
         try {
             return parser.parse(dateString);
         } catch (Exception e) {
             throw new BadArgumentException(ERR_DATE_FORMAT);
         }
     }
 
     /**
     * Checks that the granularity of the from and until arguments match.
     */
    private static void checkGranularity(String from, String until)
            throws BadArgumentException {
        if (from!= null && until!=null) {
            if ( ( (from.endsWith("Z")) 
                    && (!until.endsWith("Z")) )
                    || ( (until.endsWith("Z"))
                    && (!from.endsWith("Z")) ) ) {
                throw new BadArgumentException("Date granularities of from and "
                        + "until arguments do not match.");
            }
        }
    }

    /**
      * If <code>from</code> is later than <code>until</code>, throw a
      * <code>BadArgumentException</code>.
      *
      * If either is <code>null</code>, no exception will be thrown.
      */
     private static void checkFromUntil(Date from, Date until) throws BadArgumentException {
         if (from != null 
                 && until != null
                 && from.getTime() > until.getTime()) {
             throw new BadArgumentException(ERR_FROM_UNTIL);
         }
     }
 
     /**
      * Throw an <code>IdDoesNotExistException<code> if the given item does
      * not exist in the cache.
      */
     private void checkItemExists(String identifier) 
             throws IdDoesNotExistException,
                    ServerException {
         if (!m_cache.itemExists(identifier)) {
             throw new IdDoesNotExistException(ERR_ITEM_DOESNT_EXIST);
         }
     }
 
 }
