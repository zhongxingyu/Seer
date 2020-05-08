 package org.extensiblecatalog.ncip.v2.aleph.restdlf.handlers;
 
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.TimeZone;
 import java.util.spi.TimeZoneNameProvider;
 
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephConstants;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephException;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.AlephItem;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephUtil;
 import org.extensiblecatalog.ncip.v2.service.*;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * This class is constructed to parse XML outputs of Aleph RESTful APIs items (regular lookup, patron loans/holdRequests)
  * 
  * 
  * @author Jiří Kozlovský (MZK)
  * 
  */
 public class AlephItemHandler extends DefaultHandler {
 	private List<AlephItem> listOfItems;
 	private AlephItem currentAlephItem;
 
 	// Required to build unique item id
 	private String docNumber;
 	private String itemSequence;
 	private boolean itemIdNotFound;
 
 	// Required to decide whether is set second call number the one we need
 	private String secondCallNoType;
 
 	// Desired services
 	private boolean bibDescriptionDesired;
 	private boolean circulationStatusDesired;
 	private boolean holdQueueLengthDesired;
 	private boolean itemDesrciptionDesired;
 
 	// Regular item achievements
 	private boolean circulationStatusReached = false;
 	private boolean holdQueueLengthReached = false;
 	private boolean itemDesrciptionReached = false;
 	private boolean authorReached = false;
 	private boolean isbnReached = false;
 	private boolean titleReached = false;
 	private boolean publisherReached = false;
 	private boolean bibIdReached = false;
 	private boolean docNoReached = false;
 	private boolean locationReached = false;
 	private boolean openDateReached = false;
 	private boolean callNoReached = false;
 	private boolean copyNoReached = false;
 	private boolean materialReached = false;
 	private boolean barcodeReached = false;
 	private boolean itemSequenceReached = false;
 	private boolean agencyReached = false;
 	private boolean collectionReached = false;
 	private boolean secondCallNoTypeReached = false;
 	private boolean secondCallNoReached = false;
 
 	// Variables for parsing loans & requests
 	private BibliographicDescription bibliographicDescription;
 	private TimeZone localTimeZone;
 	private boolean parsingLoansOrRequests = false;
 	private boolean loansHandling = false;
 	private boolean holdRequestsHandling = false;
 	private boolean hourPlacedReached = false;
 	private boolean earliestDateNeededReached = false;
 	private boolean needBeforeDateReached = false;
 	private boolean datePlacedReached = false;
 	private boolean pickupLocationReached = false;
 	private boolean pickupExpiryDateReached = false;
 	private boolean reminderLevelReached = false;
 	private boolean requestIdReached = false;
 	private boolean requestTypeReached = false;
 	private boolean pickupDateReached = false;
 	private boolean statusReached = false;
 	private boolean dueDateReached = false;
 	private boolean loanDateReached = false;
 
 	private List<RequestedItem> requestedItems;
 	private RequestedItem currentRequestedItem;
 
 	private List<LoanedItem> loanedItems;
 	private LoanedItem currentLoanedItem;
 
 	public AlephItemHandler parseLoansOrRequests() {
 		bibliographicDescription = new BibliographicDescription();
 		parsingLoansOrRequests = true;
 		return this;
 	}
 
 	public void setLoansHandlingNow() {
 		loansHandling = true;
 		holdRequestsHandling = false;
 	}
 
 	public void setRequestsHandlingNow() {
 		loansHandling = false;
 		holdRequestsHandling = true;
 	}
 
 	/**
 	 * @return the listOfItems
 	 */
 	public List<AlephItem> getListOfItems() {
 		return listOfItems;
 	}
 
 	public AlephItem getAlephItem() {
 		return currentAlephItem;
 	}
 
 	/**
 	 * Constructor sets desired services.
 	 * 
 	 * @param requireAtLeastOneService
 	 *            - usually parsed from toolkit.properties
 	 * @param bibDescriptionDesired
 	 *            - parsed from initData
 	 * @param circulationStatusDesired
 	 *            - parsed from initData
 	 * @param holdQueueLengthDesired
 	 *            - parsed from initData
 	 * @param itemDesrciptionDesired
 	 *            - parsed from initData
 	 * @throws AlephException
 	 */
 	public AlephItemHandler(boolean requireAtLeastOneService, boolean bibDescriptionDesired, boolean circulationStatusDesired, boolean holdQueueLnegthDesired,
 			boolean itemDesrciptionDesired) throws AlephException {
 		if (bibDescriptionDesired || circulationStatusDesired || holdQueueLnegthDesired || itemDesrciptionDesired) {
 			this.bibDescriptionDesired = bibDescriptionDesired;
 			this.circulationStatusDesired = circulationStatusDesired;
 			this.holdQueueLengthDesired = holdQueueLnegthDesired;
 			this.itemDesrciptionDesired = itemDesrciptionDesired;
 		} else if (requireAtLeastOneService) {
 			throw new AlephException("No service desired. Please supply at least one service you wish to use.");
 		}
 		localTimeZone = TimeZone.getTimeZone("ECT");
 	}
 
 	@Override
 	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
 		if (!parsingLoansOrRequests) {
 			if (qName.equalsIgnoreCase(AlephConstants.ITEM_NODE)) {
 				secondCallNoType = null;
 				currentAlephItem = new AlephItem();
 				currentAlephItem.setLink(attributes.getValue(AlephConstants.HREF_NODE_ATTR));
 				if (listOfItems == null)
 					listOfItems = new ArrayList<AlephItem>();
 			} else if (qName.equalsIgnoreCase(AlephConstants.STATUS_NODE) && circulationStatusDesired) {
 				circulationStatusReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_HOLD_DOC_NUMBER_NODE) && holdQueueLengthDesired) {
 				holdQueueLengthReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_DESCRIPTION_NODE) && itemDesrciptionDesired) {
 				itemDesrciptionReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_DOC_NUMBER_NODE)) {
 				docNoReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_ITEM_SEQUENCE_NODE)) {
 				itemSequenceReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_SUB_LIBRARY_NODE)) {
 				locationReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_OPEN_DATE_NODE)) {
 				openDateReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_CALL_NUMBER_NODE)) {
 				callNoReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_CALL_NUMBER_2_TYPE_NODE)) {
 				secondCallNoTypeReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_CALL_NUMBER_2_NODE)) {
 				secondCallNoReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_COPY_ID_NODE)) {
 				copyNoReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_BARCODE)) {
 				barcodeReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_MATERIAL_NODE)) {
 				materialReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.TRANSLATE_CHANGE_ACTIVE_LIBRARY_NODE)) {
 				agencyReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_COLLECTION_NODE)) {
 				collectionReached = true;
 			} else if (bibDescriptionDesired) {
 				if (qName.equalsIgnoreCase(AlephConstants.Z13_AUTHOR_NODE)) {
 					authorReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z13_ISBN_NODE)) {
 					isbnReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z13_TITLE_NODE)) {
 					titleReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z13_PUBLISHER_NODE)) {
 					publisherReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z13_BIB_ID_NODE)) {
 					bibIdReached = true;
 				}
 			}
 		} else {
 			if (qName.equalsIgnoreCase(AlephConstants.Z13_AUTHOR_NODE)) {
 				authorReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z13_ISBN_NODE)) {
 				isbnReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z13_TITLE_NODE)) {
 				titleReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z13_PUBLISHER_NODE)) {
 				publisherReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z13_BIB_ID_NODE)) {
 				bibIdReached = true;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_MATERIAL_NODE)) {
 				materialReached = true;
 			} else if (loansHandling) { // Handling loans XML output
 				if (qName.equalsIgnoreCase(AlephConstants.LOAN_ITEM_NODE)) {
 					currentLoanedItem = new LoanedItem();
 
 					if (loanedItems == null)
 						loanedItems = new ArrayList<LoanedItem>();
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z36_DUE_DATE_NODE)) {
 					dueDateReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z36_LOAN_DATE_NODE)) {
 					loanDateReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z36_ITEM_SEQUENCE_NODE)) {
 					itemSequenceReached = true;
 				}
 			} else if (holdRequestsHandling) { // Handling requests XML output
 				if (qName.equalsIgnoreCase(AlephConstants.HOLD_REQUEST_NODE)) {
 					currentRequestedItem = new RequestedItem();
 					if (requestedItems == null)
 						requestedItems = new ArrayList<RequestedItem>();
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_OPEN_DATE_NODE)) {
 					datePlacedReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_OPEN_HOUR_NODE)) {
 					hourPlacedReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_REQUEST_DATE_NODE)) {
 					earliestDateNeededReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_END_REQUEST_DATE_NODE)) {
 					needBeforeDateReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_ITEM_SEQUENCE_NODE)) {
 					itemSequenceReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_HOLD_SEQUENCE_NODE)) {
 					holdQueueLengthReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_PICKUP_LOCATION_NODE)) {
 					pickupLocationReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_END_HOLD_DATE_NODE)) {
 					pickupExpiryDateReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_RECALL_TYPE_NODE)) {
 					reminderLevelReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_REQUEST_NUMBER_NODE)) {
 					requestIdReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_PRIORITY_NODE)) {
 					requestTypeReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_HOLD_DATE_NODE)) {
 					pickupDateReached = true;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_STATUS_NODE)) {
 					statusReached = true;
 				}
 			}
 		}
 	}
 
 	// END OF PARSING START ELEMENT
 
 	@Override
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 		if (!parsingLoansOrRequests) {
 			if (qName.equalsIgnoreCase(AlephConstants.ITEM_NODE)) {
 				listOfItems.add(currentAlephItem);
 			} else if (qName.equalsIgnoreCase(AlephConstants.STATUS_NODE) && circulationStatusReached) {
 				// currentAlephItem.setCirculationStatus(AlephConstants.ERROR_CIRCULATION_STATUS_NOT_FOUND);
 				circulationStatusReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_HOLD_DOC_NUMBER_NODE) && holdQueueLengthReached) {
 				currentAlephItem.setHoldQueueLength(-1);
 				// currentAlephItem.setholdQueue(AlephConstants.ERROR_HOLD_QUEUE_NOT_FOUND);
 				holdQueueLengthReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_DESCRIPTION_NODE) && itemDesrciptionReached) {
 				// currentAlephItem.setDescription(AlephConstants.ERROR_ITEM_DESCRIPTION_NOT_FOUND);
 				itemDesrciptionReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_DOC_NUMBER_NODE) && docNoReached) {
 				// currentAlephItem.setBibId(AlephConstants.ERROR_BIBLIOGRAPHIC_ID_NOT_FOUND);
 				// currentAlephItem.setDocNumber(AlephConstants.ERROR_DOCUMENT_NUMBER_NOT_FOUND);
 				docNoReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_ITEM_SEQUENCE_NODE) && itemSequenceReached) {
 				// currentAlephItem.setSeqNumber(AlephConstants.ERROR_SEQUENCE_NUMBER_NOT_FOUND);
 				itemSequenceReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_SUB_LIBRARY_NODE) && locationReached) {
 				// currentAlephItem.setLocation(AlephConstants.ERROR_LOCATION_NOT_FOUND);
 				locationReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_OPEN_DATE_NODE) && openDateReached) {
 				// currentAlephItem.setPublicationDate(AlephConstants.ERROR_OPENDATE_NOT_FOUND);
 				openDateReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_CALL_NUMBER_NODE) && callNoReached) {
 				// currentAlephItem.setCallNumber(AlephConstants.ERROR_CALL_NO_NOT_FOUND);
 				callNoReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_CALL_NUMBER_2_TYPE_NODE) && secondCallNoTypeReached) {
 				secondCallNoTypeReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_CALL_NUMBER_2_NODE) && secondCallNoReached) {
 				secondCallNoReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_COPY_ID_NODE) && copyNoReached) {
 				// currentAlephItem.setCopyNumber(AlephConstants.ERROR_COPY_NO_NOT_FOUND);
 				copyNoReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_BARCODE) && barcodeReached) {
 				// currentAlephItem.setBarcode(AlephConstants.ERROR_BARCODE_NOT_FOUND);
 				barcodeReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_MATERIAL_NODE) && materialReached) {
 				// currentAlephItem.setMediumType(AlephConstants.ERROR_MATERIAL_NOT_FOUND);
 				materialReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.TRANSLATE_CHANGE_ACTIVE_LIBRARY_NODE) && agencyReached) {
 				// currentAlephItem.setAgency(AlephConstants.ERROR_AGENCY_NOT_FOUND);
 				agencyReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_COLLECTION_NODE) && collectionReached) {
 				// currentAlephItem.setCollection(AlephConstants.ERROR_COLLECTION_NOT_FOUND);
 				collectionReached = false;
 			} else if (bibDescriptionDesired) {
 				if (qName.equalsIgnoreCase(AlephConstants.Z13_AUTHOR_NODE) && authorReached) {
 					// currentAlephItem.setAuthor(AlephConstants.ERROR_AUTHOR_NOT_FOUND);
 					authorReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z13_ISBN_NODE) && isbnReached) {
 					// currentAlephItem.setIsbn(AlephConstants.ERROR_ISBN_NOT_FOUND);
 					isbnReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z13_TITLE_NODE) && titleReached) {
 					// currentAlephItem.setTitle(AlephConstants.ERROR_TITLE_NOT_FOUND);
 					titleReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z13_PUBLISHER_NODE) && publisherReached) {
 					// currentAlephItem.setPublisher(AlephConstants.ERROR_PUBLISHER_NOT_FOUND);
 					publisherReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z13_BIB_ID_NODE) && bibIdReached) {
 					// currentAlephItem.setBibId(AlephConstants.ERROR_BIBLIOGRAPHIC_ID_NOT_FOUND);
 					bibIdReached = false;
 				}
 			}
 		} else {
 			if (qName.equalsIgnoreCase(AlephConstants.Z13_AUTHOR_NODE) && authorReached) {
 				authorReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z13_ISBN_NODE) && isbnReached) {
 				isbnReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z13_TITLE_NODE) && titleReached) {
 				titleReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z13_PUBLISHER_NODE) && publisherReached) {
 				publisherReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z13_BIB_ID_NODE) && bibIdReached) {
 				itemIdNotFound = true;
 				bibIdReached = false;
 			} else if (qName.equalsIgnoreCase(AlephConstants.Z30_MATERIAL_NODE) && materialReached) {
 				materialReached = false;
 			} else if (loansHandling) {
 				if (qName.equalsIgnoreCase(AlephConstants.LOAN_ITEM_NODE)) {
 					if (!itemIdNotFound) {
 						ItemId itemId = new ItemId();
 						itemId.setItemIdentifierValue(docNumber.trim() + "-" + itemSequence.trim());
 						itemId.setItemIdentifierType(Version1ItemIdentifierType.ACCESSION_NUMBER);
 						currentLoanedItem.setItemId(itemId);
 					}
 					currentLoanedItem.setBibliographicDescription(bibliographicDescription);
 					loanedItems.add(currentLoanedItem);
 					bibliographicDescription = new BibliographicDescription();
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z36_DUE_DATE_NODE) && dueDateReached) {
 					dueDateReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z36_LOAN_DATE_NODE) && loanDateReached) {
 					loanDateReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z36_ITEM_SEQUENCE_NODE) && itemSequenceReached) {
 					itemIdNotFound = true;
 					itemSequenceReached = false;
 				}
 			} else if (holdRequestsHandling) {
 				if (qName.equalsIgnoreCase(AlephConstants.HOLD_REQUEST_NODE)) {
 					if (!itemIdNotFound) {
 						ItemId itemId = new ItemId();
 						itemId.setItemIdentifierValue(docNumber.trim() + "-" + itemSequence.trim());
 						itemId.setItemIdentifierType(Version1ItemIdentifierType.ACCESSION_NUMBER);
 						currentRequestedItem.setItemId(itemId);
 					}
 					currentRequestedItem.setBibliographicDescription(bibliographicDescription);
 					requestedItems.add(currentRequestedItem);
 					bibliographicDescription = new BibliographicDescription();
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_OPEN_DATE_NODE) && datePlacedReached) {
 					datePlacedReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_OPEN_HOUR_NODE) && hourPlacedReached) {
 					hourPlacedReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_REQUEST_DATE_NODE) && earliestDateNeededReached) {
 					earliestDateNeededReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_END_REQUEST_DATE_NODE) && needBeforeDateReached) {
 					needBeforeDateReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_ITEM_SEQUENCE_NODE) && itemSequenceReached) {
 					itemSequenceReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_HOLD_SEQUENCE_NODE) && holdQueueLengthReached) {
 					holdQueueLengthReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_PICKUP_LOCATION_NODE) && pickupLocationReached) {
 					pickupLocationReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_END_HOLD_DATE_NODE) && pickupExpiryDateReached) {
 					pickupExpiryDateReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_RECALL_TYPE_NODE) && reminderLevelReached) {
 					reminderLevelReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_REQUEST_NUMBER_NODE) && requestIdReached) {
 					requestIdReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_PRIORITY_NODE) && requestTypeReached) {
 					requestTypeReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_HOLD_DATE_NODE) && pickupDateReached) {
 					pickupDateReached = false;
 				} else if (qName.equalsIgnoreCase(AlephConstants.Z37_STATUS_NODE) && statusReached) {
 					statusReached = false;
 				}
 			}
 		}
 	}
 
 	// END OF PARSING END ELEMENT
 
 	@Override
 	public void characters(char ch[], int start, int length) throws SAXException {
 		if (!parsingLoansOrRequests) {
 			if (circulationStatusReached) {
 				currentAlephItem.setCirculationStatus(new String(ch, start, length));
 				circulationStatusReached = false;
 			} else if (holdQueueLengthReached) {
 				currentAlephItem.setHoldQueueLength(Integer.parseInt(new String(ch, start, length)));
 				holdQueueLengthReached = false;
 			} else if (itemDesrciptionReached) {
 				currentAlephItem.setDescription(new String(ch, start, length));
 				itemDesrciptionReached = false;
 			} else if (docNoReached) {
 				currentAlephItem.setDocNumber(new String(ch, start, length));
 				docNoReached = false;
 			} else if (itemSequenceReached) {
 				currentAlephItem.setItemSeqNumber(new String(ch, start, length));
 				itemSequenceReached = false;
 			} else if (locationReached) {
 				currentAlephItem.setLocation(new String(ch, start, length));
 				locationReached = false;
 			} else if (openDateReached) {
 				currentAlephItem.setPublicationDate(new String(ch, start, length));
 				openDateReached = false;
 			} else if (callNoReached) {
 				currentAlephItem.setCallNumber(new String(ch, start, length));
 				callNoReached = false;
 			} else if (secondCallNoTypeReached) {
 				secondCallNoType = new String(ch, start, length);
 				secondCallNoTypeReached = false;
 			} else if (secondCallNoReached) {
 				if (secondCallNoType != null && !secondCallNoType.equalsIgnoreCase("9"))
 					currentAlephItem.setCallNumber(new String(ch, start, length));
 				else if (secondCallNoType == null)
 					currentAlephItem.setCallNumber(new String(ch, start, length));
 				secondCallNoReached = false;
 			} else if (copyNoReached) {
 				currentAlephItem.setCopyNumber(new String(ch, start, length));
 				copyNoReached = false;
 			} else if (barcodeReached) {
 				currentAlephItem.setBarcode(new String(ch, start, length));
 				barcodeReached = false;
 			} else if (materialReached) {
 				currentAlephItem.setMediumType(new String(ch, start, length));
 				materialReached = false;
 			} else if (agencyReached) {
 				currentAlephItem.setAgency(new String(ch, start, length));
 				agencyReached = false;
 			} else if (collectionReached) {
 				currentAlephItem.setCollection(new String(ch, start, length));
 				collectionReached = false;
 			} else if (bibDescriptionDesired) {
 				if (authorReached) {
 					currentAlephItem.setAuthor(new String(ch, start, length));
 					authorReached = false;
 				} else if (isbnReached) {
 					currentAlephItem.setIsbn(new String(ch, start, length));
 					isbnReached = false;
 				} else if (titleReached) {
 					currentAlephItem.setTitle(new String(ch, start, length));
 					titleReached = false;
 				} else if (publisherReached) {
 					currentAlephItem.setPublisher(new String(ch, start, length));
 					publisherReached = false;
 				} else if (bibIdReached) {
 					currentAlephItem.setBibId(new String(ch, start, length));
 					bibIdReached = false;
 				}
 			}
 		} else {
 			if (materialReached) {
 				String mediumTypeParsed = new String(ch, start, length);
 				MediumType mediumType = AlephUtil.detectMediumType(mediumTypeParsed);
 				bibliographicDescription.setMediumType(mediumType);
 				materialReached = false;
 			} else if (authorReached) {
 				bibliographicDescription.setAuthor(new String(ch, start, length));
 				authorReached = false;
 			} else if (isbnReached) {
 				bibliographicDescription.setBibliographicLevel(null);
 				isbnReached = false;
 			} else if (titleReached) {
 				bibliographicDescription.setTitle(new String(ch, start, length));
 				titleReached = false;
 			} else if (publisherReached) {
 				bibliographicDescription.setPublisher(new String(ch, start, length));
 				publisherReached = false;
 			} else if (bibIdReached) {
 				String parsedBibId = new String(ch, start, length);
 				docNumber = parsedBibId;
 				List<BibliographicItemId> bibliographicItemIds = new ArrayList<BibliographicItemId>();
 				BibliographicItemId bibId = new BibliographicItemId();
 
 				bibId.setBibliographicItemIdentifier(parsedBibId);
 				bibId.setBibliographicItemIdentifierCode(Version1BibliographicItemIdentifierCode.URI);
 
 				bibliographicItemIds.add(bibId);
 
 				bibliographicDescription.setBibliographicItemIds(bibliographicItemIds);
 				bibIdReached = false;
 			} else if (itemSequenceReached) {
 				itemSequence = new String(ch, start, length);
 				itemSequenceReached = false;
 			} else if (loansHandling) {
 				if (dueDateReached) {
 					String dateDueParsed = new String(ch, start, length);
 					GregorianCalendar dateDue = AlephUtil.parseGregorianCalendarFromAlephDate(dateDueParsed);
 
 					currentLoanedItem.setDateDue(dateDue);
 
 					dueDateReached = false;
 				} else if (loanDateReached) {
 					String loanDateParsed = new String(ch, start, length);
 					GregorianCalendar loanDate = AlephUtil.parseGregorianCalendarFromAlephDate(loanDateParsed);
 
 					currentLoanedItem.setDateCheckedOut(loanDate);
 
 					loanDateReached = false;
 				}
 			} else if (holdRequestsHandling) {
 				if (datePlacedReached) {
 					String datePlacedParsed = new String(ch, start, length);
 					GregorianCalendar datePlaced = AlephUtil.parseGregorianCalendarFromAlephDate(datePlacedParsed);
 
					currentRequestedItem.setDatePlaced(datePlaced);
 
 					datePlacedReached = false;
 				} else if (hourPlacedReached) {
 					String hourPlacedParsed = new String(ch, start, length);
 					if (!hourPlacedParsed.equalsIgnoreCase("00000000")) {
 						GregorianCalendar datePlaced = currentRequestedItem.getDatePlaced();
 						GregorianCalendar hourPlaced = new GregorianCalendar(localTimeZone);
 
 						try {
 							hourPlaced.setTime(AlephConstants.ALEPH_HOUR_FORMATTER.parse(hourPlacedParsed));
 						} catch (ParseException e) {
 							e.printStackTrace();
 						}
 
 						datePlaced.add(Calendar.HOUR_OF_DAY, hourPlaced.get(Calendar.HOUR_OF_DAY) - 1);
 						datePlaced.add(Calendar.MINUTE, hourPlaced.get(Calendar.MINUTE));
 
 						currentRequestedItem.setDatePlaced(datePlaced);
 					}
 					hourPlacedReached = false;
 				} else if (earliestDateNeededReached) {
 					String earliestDateNeededParsed = new String(ch, start, length);
 					GregorianCalendar earliestDateNeeded = AlephUtil.parseGregorianCalendarFromAlephDate(earliestDateNeededParsed);
 
					currentRequestedItem.setEarliestDateNeeded(earliestDateNeeded);
 
 					earliestDateNeededReached = false;
 				} else if (needBeforeDateReached) {
 					String needBeforeDateParsed = new String(ch, start, length);
 					GregorianCalendar needBeforeDate = AlephUtil.parseGregorianCalendarFromAlephDate(needBeforeDateParsed);
 
					currentRequestedItem.setNeedBeforeDate(needBeforeDate);
 
 					needBeforeDateReached = false;
 				} else if (holdQueueLengthReached) {
 					currentRequestedItem.setHoldQueueLength(new BigDecimal(new String(ch, start, length)));
 					holdQueueLengthReached = false;
 				} else if (pickupLocationReached) {
 					PickupLocation pickupLocation = new PickupLocation(new String(ch, start, length));
 					currentRequestedItem.setPickupLocation(pickupLocation);
 					pickupLocationReached = false;
 				} else if (pickupExpiryDateReached) {
 					String pickupExpiryDateParsed = new String(ch, start, length);
 					GregorianCalendar pickupExpiryDate = AlephUtil.parseGregorianCalendarFromAlephDate(pickupExpiryDateParsed);
 
					currentRequestedItem.setPickupExpiryDate(pickupExpiryDate);
 
 					pickupExpiryDateReached = false;
 				} else if (reminderLevelReached) {
 					currentRequestedItem.setReminderLevel(new BigDecimal(new String(ch, start, length)));
 					reminderLevelReached = false;
 				} else if (requestIdReached) {
 					RequestId requestId = new RequestId();
 					requestId.setRequestIdentifierValue(new String(ch, start, length));
 					currentRequestedItem.setRequestId(requestId);
 					requestIdReached = false;
 				} else if (requestTypeReached) {
 					RequestType requestType = null;
 					String parsedValue = new String(ch, start, length);
 					if (parsedValue == "30") // TODO: Add remaining request types
 						requestType = Version1RequestType.LOAN;
 					else
 						requestType = Version1RequestType.ESTIMATE; // FIXME: Put here better default value
 					currentRequestedItem.setRequestType(requestType);
 					requestTypeReached = false;
 				} else if (pickupDateReached) {
 					String pickupDateParsed = new String(ch, start, length);
 					GregorianCalendar pickupDate = AlephUtil.parseGregorianCalendarFromAlephDate(pickupDateParsed);
 
					currentRequestedItem.setPickupDate(pickupDate);
 
 					pickupDateReached = false;
 				} else if (statusReached) {
 					String parsedStatus = new String(ch, start, length);
 					RequestStatusType requestStatusType;
 					if (parsedStatus == "S")
 						requestStatusType = Version1RequestStatusType.AVAILABLE_FOR_PICKUP;
 					else
 						requestStatusType = Version1RequestStatusType.IN_PROCESS;
 					currentRequestedItem.setRequestStatusType(requestStatusType);
 					statusReached = false;
 				}
 			}
 		}
 	}
 
 	// END OF PARSING CHARACTERS INSIDE ELEMENTS
 
 	public List<RequestedItem> getListOfRequestedItems() {
 		return requestedItems;
 	}
 
 	public List<LoanedItem> getListOfLoanedItems() {
 		return loanedItems;
 	}
 }
