 package org.extensiblecatalog.ncip.v2.aleph.restdlf;
 
 //TODO: remove unused RESTful APIs constants (migrated from X-Service)
 public class AlephConstants {
 
 	// node and xml response parsing constants
 	public static final String LABEL_ATTRIBUTE = "label";
 	public static final String CODE_ATTRIBUTE = "code";
 	public static final String ERROR_NODE = "error";
 	public static final String REPLY_NODE = "reply";
 	public static final String REPLY_CODE_NODE = "reply-code";
 	public static final String STATUS_NODE = "status";
 	public static final String TOTAL_NODE = "total";
 
 	public static final String DUE_DATE_NODE = "due-date";
 	public static final String Z30_BARCODE = "z30-barcode";
 	public static final String Z30_CALL_NUMBER_NODE = "z30-call-no";
 	public static final String Z30_COPY_ID_NODE = "z30-copy-id";
 	public static final String Z13_BIB_ID_NODE = "z13-doc-number";
 	public static final String Z13_AUTHOR_NODE = "z13-author";
 	public static final String Z13_TITLE_NODE = "z13-title";
 	public static final String Z13_PUBLISHER_NODE = "z13-imprint";
 	public static final String Z13_ISBN_NODE = "z13-isbn-issn";
 	public static final String Z13_NODE = "z13";
 	public static final String Z37_NODE = "z37";
 	public static final String Z37_DOC_NUMBER_NODE = "z37-doc-number";
 	public static final String Z37_ITEM_SEQUENCE_NODE = "z37-item-sequence";
 	public static final String Z37_REQUEST_NUMBER_NODE = "z37-request-number";
 	public static final String Z37_REQUEST_DATE_NODE = "z37-request-date";
 	public static final String Z37_HOLD_DATE_NODE = "z37-hold-date";
 	public static final String Z36_NODE = "z36";
 	public static final String Z36_DOC_NUMBER_NODE = "z36-doc-number";
 	public static final String Z36_ITEM_SEQUENCE_NODE = "z36-item-sequence";
 	public static final String Z36_MATERIAL_NODE = "z36-material";
 	public static final String Z36_SUB_LIBRARY_NODE = "z36-sub-library";
 
 	// circ status
 	public static final String CIRC_STATUS_NODE = "circ-status";
 	public static final String Z30_DESCRIPTION_NODE = "z30-description";
 	public static final String DUE_HOUR_NODE = "due-hour";
 	public static final String SUB_LIBRARY_NODE = "sub-library";
 	public static final String LOCATION_NODE = "location";
 	public static final String Z36_ID_NODE = "z36-id";
 	public static final String Z36_STATUS_NODE = "z36-status";
 	public static final String Z36_DUE_DATE_NODE = "z36-due-date";
 	public static final String Z36_LOAN_DATE_NODE = "z36-loan-date";
 	public static final String Z36_RECALL_DATE_NODE = "z36-recall-date";
 	public static final String Z36_RECALL_DUE_DATE_NODE = "z36-recall-due-date";
 	public static final String Z30_NODE = "z30";
 	public static final String Z30_DOC_NUMBER_NODE = "z30-doc-number";
 	public static final String Z30_ITEM_SEQUENCE_NODE = "z30-item-sequence";
 	public static final String Z30_MATERIAL_NODE = "z30-material";
 	public static final String Z30_SUB_LIBRARY_NODE = "z30-sub-library";
 	public static final String Z30_OPEN_DATE_NODE = "z30-open-date";
 	// could be hold id, not sure?
 	public static final String Z30_HOLD_DOC_NUMBER_NODE = "z30-hol-doc-number";
 	// get fine data
 	public static final String Z31_FINE_DATE_NODE = "z31-date";
 	public static final String Z31_FINE_STATUS_NODE = "z31-status";
 	public static final String Z31_CREDIT_DEBIT_NODE = "z31-credit-debit";
 	public static final String Z31_NET_SUM_NODE = "z31-net-sum";
 	public static final String Z30_NODE_ID = "Z30";
 	public static final String Z30_TRANSLATE_CHANGE_ACTIVE_LIBRARY_NODE = "translate-change-active-library";
 	public static final String Z30_COLLECTION_NODE = "z30-collection";
 
 	public static final String Z31_NODE = "z31";
 	public static final String Z31_FINE_ACCRUAL_DATE_NODE = "z31-date";
 	public static final String SESSION_ID_NODE = "session-id";
 	public static final String ITEM_DATA_NODE = "item-data";
 	public static final String READ_ITEM_NODE = "read-item";
 	public static final String ITEM_NODE = "item";
 	public static final String GET_ITEM_LIST_NODE = "get-item-list";
 	public static final String ITEMS_NODE = "items";
 	public static final String SUBFIELD_NODE = "subfield";
 	public static final String LOAN_STATUS_NODE = "loan-status";
 	public static final String ITEM_STATUS_NODE = "item-status";
 	public static final String REC_KEY_NODE = "rec-key";
 	public static final String BARCODE_NODE = "barcode";
 	public static final String HOLD_ITEM_NODE = "item-h";
 	public static final String HOLD_REQ_ITEM_NODE = "hold-req";
 	public static final String LIST_RECORDS_NODE = "ListRecords";
 	public static final String HEADER_NODE = "header";
 	public static final String METADATA_NODE = "metadata";
 	public static final String IDENTIFIER_NODE = "identifier";
 	public static final String DATAFIELD_NODE = "datafield";
 	public static final String TAG_ATTRIBUTE = "tag";
 	public static final String AVAILABLE_TAG_ATTR_VALUE = "AVA";
 	public static final String AVAILABLE_CODE_ATTR_VALUE = "e";
 
 	public static final String CALL_NUMBER_LABEL1_ATTR_VALUE = "h";
 	public static final String CALL_NUMBER_LABEL2_ATTR_VALUE = "i";
 	public static final String LOCATION_LABEL_ATTR_VALUE = "A";
 	public static final String MEDIUM_LABEL_ATTR_VALUE = "m";
 	public static final String ID_NODE_ATTR = "id";
 	public static final String HREF_NODE_ATTR = "href";
 	public static final String TYPE_NODE_ATTR = "type";
 	public static final String AUTHOR_NODE_ID = "100";
 	public static final String ISBN_NODE_ID = "020";
 	public static final String TITLE_NODE_ID = "245";
 	public static final String CALL_NUMBER_NODE_ID = "852";
 	public static final String PUBLISHER_NODE_ID = "260";
 	public static final String DESCRIPTION1_NODE_ID = "300";
 	public static final String DESCRIPTION2_NODE_ID = "500";
 	public static final String SERIES_NODE_ID = "490";
 	public static final String ELECTRONIC_RESOURCE_NODE_ID = "856";
 	public static final String BIB_ID_NODE_ID = "LKR";
 	public static final String BIB_ID_NODE_ATTR_VALUE = "b";
 	// get alephitem data for a fine
 	public static final String FINE_ITEM_NODE = "fine";
 	// loan item nodes
 	public static final String LOAN_ITEM_NODE = "loan";
 
 	public static final String Z304_NODE = "z304";
 	public static final String Z305_NODE = "z305";
 	public static final String Z303_ID_NODE = "z303-id";
 	public static final String Z304_EMAIL_NODE = "z304-email-address";
 	public static final String Z303_NAME_NODE = "z303-name";
 	public static final String Z304_ADDRESS_1_NODE = "z304-address-1";
 	public static final String Z304_ADDRESS_2_NODE = "z304-address-2";
 	public static final String Z304_ADDRESS_3_NODE = "z304-address-3";
 	public static final String Z304_ADDRESS_4_NODE = "z304-address-4";
 	public static final String Z304_ADDRESS_5_NODE = "z304-address-5";
 	public static final String Z304_ADDRESS_6_NODE = "z304-address-6";
 	// global block and notes fields
 	public static final String Z303_DELINQUENCY_1_NODE = "z303-delinq-1";
 	public static final String Z303_DELINQUENCY_NOTE_1_NODE = "z303-delinq-n-1";
 	public static final String Z303_DELINQUENCY_1_UPDATE_DATE_NODE = "z303-delinq-1-update-date";
 	public static final String Z303_DELINQUENCY_2_NODE = "z303-delinq-2";
 	public static final String Z303_DELINQUENCY_NOTE_2_NODE = "z303-delinq-n-2";
 	public static final String Z303_DELINQUENCY_2_UPDATE_DATE_NODE = "z303-delinq-2-update-date";
 	public static final String Z303_DELINQUENCY_3_NODE = "z303-delinq-3";
 	public static final String Z303_DELINQUENCY_NOTE_3_NODE = "z303-delinq-n-3";
 	public static final String Z303_DELINQUENCY_3_UPDATE_DATE_NODE = "z303-delinq-3-update-date";
 	public static final String Z303_FIELD_NOTE_1_NODE = "z303-field-1";
 	public static final String Z303_FIELD_NOTE_2_NODE = "z303-field-2";
 	public static final String Z303_FIELD_NOTE_3_NODE = "z303-field-3";
 	public static final String Z303_ADDITIONAL_NOTE_1_NODE = "z303-note-1";
 	public static final String Z303_ADDITIONAL_NOTE_2_NODE = "z303-note-2";
 	// local block and notes fields
 	public static final String Z305_LOAN_PERMISSION_NODE = "z305-loan-permission";
 	public static final String Z305_HOLD_PERMISSION_NODE = "z305-hold-permission";
 	public static final String Z305_RENEW_PERMISSION_NODE = "z305-renew-permission";
 	public static final String Z305_DELINQUENCY_1_NODE = "z305-delinq-1";
 	public static final String Z305_DELINQUENCY_NOTE_1_NODE = "z305-delinq-n-1";
 	public static final String Z305_DELINQUENCY_1_UPDATE_DATE_NODE = "z305-delinq-1-update-date";
 	public static final String Z305_DELINQUENCY_2_NODE = "z305-delinq-2";
 	public static final String Z305_DELINQUENCY_NOTE_2_NODE = "z305-delinq-n-2";
 	public static final String Z305_DELINQUENCY_2_UPDATE_DATE_NODE = "z305-delinq-2-update-date";
 	public static final String Z305_DELINQUENCY_3_NODE = "z305-delinq-3";
 	public static final String Z305_DELINQUENCY_NOTE_3_NODE = "z305-delinq-n-3";
 	public static final String Z305_DELINQUENCY_3_UPDATE_DATE_NODE = "z305-delinq-3-update-date";
 	public static final String Z305_FIELD_NOTE_1_NODE = "z305-field-1";
 	public static final String Z305_FIELD_NOTE_2_NODE = "z305-field-2";
 	public static final String Z305_FIELD_NOTE_3_NODE = "z305-field-3";
 	public static final String Z305_BOR_STATUS_CODE_NODE = "z305-bor-status-code";
 	public static final String Z305_BOR_STATUS_NODE = "z305-bor-status";
 	public static final String Z305_BOR_TYPE_NODE = "z305-bor-type";
 	public static final String BALANCE_NODE = "balance";
 
 	// value constants
 	public static final String STATUS_OK = "ok";
 	public static final String UNAVAILABLE_STATUS = "unavailable";
 	public static final String AVAILABLE_STATUS = "available";
 	public static final String BLOCK_NONE_CODE = "00";
 
 	public static final String YES = "Y";
 	public static final String NO = "N";
 
 	public static enum Availability {
 		UNKNOWN, NOT_AVAILABLE, POSSIBLY_AVAILABLE, AVAILABLE, DOESNT_EXIST, DOESNT_EXIST_REMOVED_MANUALLY
 	}
 
 	public static enum MessageTypes {
 		LOOKUP, UPDATE, NOTIFICATION
 	}
 
 	public static final String CIRC_STATUS_CHECKED_OUT = "A";
 
 	public static final int BIB_ID_LENGTH = 9;
 	public static final int HOLDINGS_ID_LENGTH = 9;
 	public static final int DOC_NUMBER_LENGTH = 9;
 	public static final int SEQ_NUMBER_LENGTH = 6;
 	public static final int ITEM_ID_UNIQUE_PART_LENGTH = 6;
 	public static final int LIBRARY_PARAM_LENGTH = 5;
 	public static final String HOLD_DATE_FORMAT = "MM/dd/yyyy";
 	public static final String RENEW_DUE_DATE_FORMAT = "yyyyMMdd";
 	public static final String LOAN_DUE_DATE_FORMAT = "MM/dd/yyyy";
 	public static final String FINE_ACCRUAL_DATE_FORMAT = "MM/dd/yyyy";
 	public static final String CIRC_STATUS_DUE_DATE_FORMAT = "MM/dd/yy";
	public static final String STATUS_DATE_FORMAT = "dd/mmm/yyyy";
 	public static final String FINE_STATUS_PAID = "Paid";
 	public static final String FINE_STATUS_UNPAID = "Unpaid";
 	public static final String FINE_CREDIT = "C";
 	public static final String FINE_DEBIT = "D";
 
 	// AlephAPI errors
 	public static final String ERROR_ITEM_NOT_CHECKED_OUT = "Item is not checked out.";
 	public static final String ERROR_BOR_ID_VER = "Both Bor_Id and Verification must be supplied";
 	public static final String ERROR_BOR_ID_MISSING = "bor_id must be provided";
 	public static final String ERROR_PATRON_SYSTEM_KEY = "Error retrieving Patron System Key";
 	public static final String ERROR_RETRIEVE_PATRON_RECORD = "Error retrieving Patron Record";
 	public static final String ERROR_AUTH_VERIFICATION = "Error in Verification";
 	// Z303 missing
 	public static final String ERROR_GLOBAL_PATRON_RECORD_MISSING = "Error retrieving Global Patron Record";
 	// Z305 missing
 	public static final String ERROR_LOCAL_PATRON_RECORD_MISSING = "Error retrieving Local Patron Record";
 	// Z304 missing
 	public static final String ERROR_ADDRESS_INFORMATION_MISSING = "Error retrieving Patron Address record";
 	public static final String ERROR_AUTHENTICATION_FAILED_SESSION_ID_MISSING = "Authentication failed, no session id returned";
 	public static final String ERROR_AUTHENTICATION_FAILED_UNKNOWN = "Authentication failed for unknown reason";
 	public static final String ERROR_GET_ITEM_DATA_FAILED_UNKNOWN = "Get item data failed for unknown reason";
 	public static final String ERROR_GET_ITEM_DATA_FAILED_SESSION_ID_MISSING = "Get item data failed, no session id returned";
 	public static final String ERROR_ITEM_DATA_MISSING = ERROR_GET_ITEM_DATA_FAILED_UNKNOWN;
 	public static final String ERROR_ITEM_MISSING = "No item exist with the specified criteria";
 	public static final String ERROR_FIND_DOC_FAILED_SESSION_ID_MISSING = "Find document failed, no session id returned";
 	public static final String ERROR_FIND_DOC_MISSING = "Find document or get item data failed for unknown reason";
 	public static final String ERROR_RECORD_MISSING = "No item exists with the specified criteria";
 	public static final String ERROR_DOC_NUM_MISSING = "doc_num must be included in parameters";
 	public static final String ERROR_AVAILABILITY_LIST_TOO_LONG = "Too many ids requested for availability.  Maximum is 10.";
 	public static final String ERROR_FAILED_TO_FIND_REQUEST_FOR_CANCEL = "Failed to find the request to cancel it";
 	public static final String ERROR_UNKNOWN_AGENCY = "Unknown agency";
 	public static final String ERROR_BIB_LIBRARY_NOT_SET = "Bib library not set for agency";
 	public static final String ERROR_ADM_LIBRARY_NOT_SET = "Adm library not set for agency";
 	public static final String ERROR_HOLD_LIBRARY_NOT_SET = "Hold library not set for agency";
 
 	// Parser found empty XML node
 	// Note that NOT FOUND means XML node was found, but with no values in it
 	public static final String ERROR_ITEM_DESCRIPTION_NOT_FOUND = "Item description is not set to this item.";
 	public static final String ERROR_CIRCULATION_STATUS_NOT_FOUND = "Circulation status is not set to this item.";
 	public static final String ERROR_HOLD_QUEUE_NOT_FOUND = "Hold queue is not set to this item.";
 	public static final String ERROR_ITEM_ID_NOT_FOUND = "Item id is not set to this item.";
 	public static final String ERROR_AUTHOR_NOT_FOUND = "Author is not set to this item.";
 	public static final String ERROR_ISBN_NOT_FOUND = "ISBN is not set to this item.";
 	public static final String ERROR_TITLE_NOT_FOUND = "Title is not set to this item.";
 	public static final String ERROR_PUBLISHER_NOT_FOUND = "Publisher is not set to this item.";
 	public static final String ERROR_BIBLIOGRAPHIC_ID_NOT_FOUND = "Biblioghraphic id is not set to this item.";
 	public static final String ERROR_AGENCY_NOT_FOUND = "Agency is not set to this item.";
 	public static final String ERROR_OPENDATE_NOT_FOUND = "Open date is not set to this item.";
 	public static final String ERROR_CALL_NO_NOT_FOUND = "Call Number is not set to this item";
 	public static final String ERROR_COPY_NO_NOT_FOUND = "Copy Number is not set to this item";
 	public static final String ERROR_BARCODE_NOT_FOUND = "Barcode is not set to this item";
 	public static final String ERROR_MATERIAL_NOT_FOUND = "Material (medium type) is not set to this item";
 	public static final String ERROR_LOCATION_NOT_FOUND = "Location is not set to this item";
 	public static final String ERROR_DOCUMENT_NUMBER_NOT_FOUND = "Document number is not set to this item";
 	public static final String ERROR_SEQUENCE_NUMBER_NOT_FOUND = "Sequence number is not set to this item";
 	public static final String ERROR_COLLECTION_NOT_FOUND = "Collection is not set to this item";
 
 	// AlephAPI
 	// TODO: edit those constants into valid format, which Aleph RESTful APIs uses
 	public static final String ALEPHAPI_BOR_AUTH = "bor-auth";
 	public static final String ALEPHAPI_BOR_INFO = "bor-info";
 	public static final String ALEPHAPI_CIRC_STATUS = "circ-status";
 	public static final String ALEPHAPI_ITEM_DATA = "item-data";
 	public static final String ALEPHAPI_READ_ITEM = "read-item";
 	public static final String ALEPHAPI_GET_ITEM_LIST = "get-item-list";
 	public static final String ALEPHAPI_GET_HOLDING = "get-holding";
 	public static final String ALEPHAPI_HOLD_REQUEST = "hold-req";
 	public static final String ALEPHAPI_CANCEL_HOLD_REQUEST = "hold-req-cancel";
 	public static final String ALEPHAPI_RENEW = "renew";
 	public static final String ALEPHAPI_PUBLISH_AVAILABILITY = "publish_avail";
 
 	// AlephAPI params
 	// TODO: edit those constants into valid format, which Aleph RESTful APIs uses
 	public static final String PARAM_ALEPHAPI_NAME = "op";
 	public static final String PARAM_LIBRARY = "library";
 	public static final String PARAM_SUB_LIBRARY = "sub_library";
 	public static final String PARAM_PATRON_ID = "bor_id";
 	public static final String PARAM_PASSWORD = "verification";
 	public static final String PARAM_LOANS = "loans";
 	public static final String PARAM_CASH = "cash";
 	public static final String PARAM_FILTER_CASH = "filter_cash";
 	public static final String PARAM_HOLD = "hold";
 	public static final String PARAM_SYSTEM_NUMBER = "sys_no";
 	public static final String PARAM_BASE = "base";
 	public static final String PARAM_DOC_NUMBER = "doc_number";
 	public static final String PARAM_REC_KEY = "rec_key";
 	public static final String PARAM_BARCODE = "item_barcode";
 	public static final String PARAM_SEQ_NUMBER = "item_sequence";
 	public static final String PARAM_CANCEL_SEQUENCE = "sequence";
 	public static final String PARAM_DUE_DATE = "due_date";
 	public static final String PARAM_DOC_NUM = "doc_num";
 	public static final String PARAM_ITEMS = "items";
 	public static final String PARAM_PATRON_INFO = "patronInformation";
 	public static final String PARAM_ADDRESS = "address";
 	public static final String PARAM_CIRC_ACTIONS = "circulationActions";
 	public static final String PARAM_BOOKINGS = "bookings";
 	public static final String PARAM_REQUESTS = "requests";
 	public static final String PARAM_PATRON_STATUS = "patronStatus";
 	public static final String PARAM_REGISTRATION = "registration";
 
 	// lookup user param values
 	public static final String USER_CASH_BALANCE_ONLY = "B";
 	public static final String USER_CASH_ALL_DATA = "Y";
 	public static final String USER_CASH_NO_DATA = "N";
 	public static final String USER_CASH_OPEN_TRANSACTIONS_ONLY = "O";
 	public static final String USER_CASH_FILTER = "F";
 	public static final String USER_LOANS_ALL_DATA = "Y";
 	public static final String USER_LOANS_NO_DATA = "N";
 	public static final String USER_LOANS_PARTIAL_DATA = "P";
 	public static final String USER_HOLDS_ALL_DATA = "Y";
 	public static final String USER_HOLDS_NO_DATA = "N";
 	public static final String USER_HOLDS_PARTIAL_DATA = "P";
 	public static final String QUEUE_NODE = "queue";
 
 	// Toolkit.properties key values
 	public static final String REST_DLF_SERVER = "RestDlfServer";
 	public static final String REST_DLF_PORT = "RestDlfPort";
 	public static final String REST_DLF_SUFFIX = "RestDlfSuffix";
 
 	// RESTful APIs http requests values
 	public static final String ITEM_PATH_ELEMENT = "record";
 	public static final String USER_PATH_ELEMENT = "patron";
 
 	// TODO: make Xservices & rest dlf switchable
 	public static final String XSERVER_NAME = "AlephXServerName";
 	public static final String XSERVER_PORT = "AlephXServerPort";
 	public static final String XSERVER_SUFFIX = "AlephXServerSuffix";
 
 	public static final String BIBLIOGRAPHIC_LIBRARY = "AlephBibLibrary";
 	public static final String ALEPH_ADMINISTRATIVE_LIBRARY = "AlephAdmLibrary";
 	public static final String INCLUDE_PARTICULAR_PROBLEMS_TO_LUIS = "IncludeParticularProblemsInLookupItemSet";
 	public static final String REQUIRE_AT_LEAST_ONE_SERVICE = "RequiredAtLeastOneService";
 	public static final String DEFAULT_AGENCY = "DefaultAgency";
 	public static final String UNIQUE_ITEM_ID_SEPERATOR = "-";
 	public static final String SEQUENCE_NUMBER_SEPERATOR = "\\."; // Be aware of this regex (dot is reserved character)
 }
