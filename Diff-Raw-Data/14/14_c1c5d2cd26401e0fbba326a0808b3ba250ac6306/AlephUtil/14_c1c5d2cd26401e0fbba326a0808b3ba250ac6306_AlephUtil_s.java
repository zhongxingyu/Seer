 package org.extensiblecatalog.ncip.v2.aleph.util;
 
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.extensiblecatalog.ncip.v2.aleph.item.AlephItem;
 import org.extensiblecatalog.ncip.v2.aleph.user.AlephXServicesUser;
 import org.extensiblecatalog.ncip.v2.service.AgencyId;
 import org.extensiblecatalog.ncip.v2.service.BibliographicDescription;
 import org.extensiblecatalog.ncip.v2.service.BibliographicItemId;
 import org.extensiblecatalog.ncip.v2.service.BlockOrTrap;
 import org.extensiblecatalog.ncip.v2.service.BlockOrTrapType;
 import org.extensiblecatalog.ncip.v2.service.CirculationStatus;
 import org.extensiblecatalog.ncip.v2.service.ComponentId;
 import org.extensiblecatalog.ncip.v2.service.ComponentIdentifierType;
 import org.extensiblecatalog.ncip.v2.service.CurrentBorrower;
 import org.extensiblecatalog.ncip.v2.service.CurrentRequester;
 import org.extensiblecatalog.ncip.v2.service.FromAgencyId;
 import org.extensiblecatalog.ncip.v2.service.HoldingsInformation;
 import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
 import org.extensiblecatalog.ncip.v2.service.ItemDescription;
 import org.extensiblecatalog.ncip.v2.service.ItemOptionalFields;
 import org.extensiblecatalog.ncip.v2.service.ItemTransaction;
 import org.extensiblecatalog.ncip.v2.service.ItemUseRestrictionType;
 import org.extensiblecatalog.ncip.v2.service.Location;
 import org.extensiblecatalog.ncip.v2.service.LocationName;
 import org.extensiblecatalog.ncip.v2.service.LocationNameInstance;
 import org.extensiblecatalog.ncip.v2.service.MediumType;
 import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
 import org.extensiblecatalog.ncip.v2.service.PhysicalAddress;
 import org.extensiblecatalog.ncip.v2.service.RequestStatusType;
 import org.extensiblecatalog.ncip.v2.service.ResponseHeader;
 import org.extensiblecatalog.ncip.v2.service.StructuredAddress;
 import org.extensiblecatalog.ncip.v2.service.ToAgencyId;
 import org.extensiblecatalog.ncip.v2.service.UserId;
 import org.extensiblecatalog.ncip.v2.service.UserIdentifierType;
 import org.extensiblecatalog.ncip.v2.service.Version1AgencyElementType;
 import org.extensiblecatalog.ncip.v2.service.Version1BibliographicItemIdentifierCode;
 import org.extensiblecatalog.ncip.v2.service.Version1BibliographicRecordIdentifierCode;
 import org.extensiblecatalog.ncip.v2.service.Version1CirculationStatus;
 import org.extensiblecatalog.ncip.v2.service.Version1ItemUseRestrictionType;
 import org.extensiblecatalog.ncip.v2.service.Version1LocationType;
 import org.extensiblecatalog.ncip.v2.service.Version1MediumType;
 import org.extensiblecatalog.ncip.v2.service.Version1PhysicalAddressType;
 import org.extensiblecatalog.ncip.v2.service.Version1RequestStatusType;
 import org.xml.sax.SAXException;
 
 public class AlephUtil {
 
 	private static Map<String, List<String>> itemRestrictionClasses;
 	private static boolean itemRestrictionClassesInitialized = false;
 
 	private static void initializeItemRestrictionClasses() {
 		itemRestrictionClasses = new HashMap<String, List<String>>();
 
 		// Define & add list of item statuses defining restriction 'In Library Use Only'
 		List<String> inLibraryUseOnly = new ArrayList<String>();
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_TO_THE_MUSIC_CORNER_ONLY_4F);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_IN_HOUSE_LOAN);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_REFERENCE_ONLY);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_REFERENCE_ONLY_SPN_2F);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_REFERENCE_ONLY_ST_4F);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_REFERENCE_SHELF);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_STUDY_ROOM);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_IN_HOUSE_ILL);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_RETRO);
 		itemRestrictionClasses.put(AlephConstants.ITEM_RESTRICTION_IN_LIBRARY_USE_ONLY, inLibraryUseOnly);
 
 		// Define & add list of item statuses defining restrictions of type 'Loan Period'
 		List<String> loanPeriods = new ArrayList<String>();
 		loanPeriods.add(AlephConstants.ITEM_STATUS_OPEN_STOCK_MONTH);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_LONG_TERM_LOAN);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_MONTH);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_WEEK);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_14_DAYS);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_7_DAYS);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_2_HOURS);
 		itemRestrictionClasses.put(AlephConstants.ITEM_RESTRICTION_LOAN_PERIOD, loanPeriods);
 
 		// Note that other item statuses returned by Aleph RESTful APIs ILS are non-restrictive
 		itemRestrictionClassesInitialized = true;
 	}
 
 	public static AgencyId createAgencyId(String agencyId) {
 		return new AgencyId(Version1AgencyElementType.VERSION_1_AGENCY_ELEMENT_TYPE, agencyId);
 	}
 
 	public static BibliographicDescription parseBibliographicDescription(AlephItem alephItem) {
 		return parseBibliographicDescription(alephItem, false);
 	}
 
 	public static BibliographicDescription parseBibliographicDescription(AlephItem alephItem, boolean includeComponentIdWithBarcode) {
 
 		BibliographicDescription bibliographicDescription = new BibliographicDescription();
 
 		if (alephItem.getAuthor() != null)
 			bibliographicDescription.setAuthor(alephItem.getAuthor());
 
 		if (alephItem.getMediumType() != null)
 			bibliographicDescription.setMediumType(alephItem.getMediumType());
 
 		if (alephItem.getPublisher() != null)
 			bibliographicDescription.setPublisher(alephItem.getPublisher());
 
 		if (alephItem.getSeries() != null)
 			bibliographicDescription.setSeriesTitleNumber(alephItem.getSeries());
 
 		if (alephItem.getTitle() != null)
 			bibliographicDescription.setTitle(alephItem.getTitle());
 
 		if (includeComponentIdWithBarcode && alephItem.getBarcodeValue() != null) {
 			ComponentId componentId = createComponentIdAsAccessionNumber(alephItem.getBarcodeValue());
 			bibliographicDescription.setComponentId(componentId);
 		}
 
 		if (alephItem.getIsbn() != null)
 			bibliographicDescription.setBibliographicItemIds(Arrays.asList(alephItem.getIsbn()));
 
 		// FIXME: NCIP JAXB translator cuts off all BibliographicRecordIds - that's why we're using componentId to transfer barcode
 		return bibliographicDescription;
 	}
 
 	public static ComponentId createComponentIdAsAccessionNumber(String barcodeValue) {
 		ComponentId componentId = new ComponentId();
 		componentId.setComponentIdentifierType(new ComponentIdentifierType(Version1BibliographicRecordIdentifierCode.VERSION_1_BIBLIOGRAPHIC_RECORD_IDENTIFIER_CODE,
 				Version1BibliographicRecordIdentifierCode.ACCESSION_NUMBER.getValue()));
 		componentId.setComponentIdentifier(barcodeValue);
 		return componentId;
 	}
 
 	public static Location parseLocation(AlephItem alephItem) {
 		Location location = new Location();
 		alephItem.getLocation();
 		LocationNameInstance locationNameInstance = new LocationNameInstance();
 
 		locationNameInstance.setLocationNameValue(alephItem.getLocation());
 		// TODO: more to come from requirement for level
 		locationNameInstance.setLocationNameLevel(new BigDecimal("1"));// temporarily set to 1.
 
 		List<LocationNameInstance> locationNameInstances = new ArrayList<LocationNameInstance>();
 		locationNameInstances.add(locationNameInstance);
 
 		LocationName locationName = new LocationName();
 		locationName.setLocationNameInstances(locationNameInstances);
 
 		location = new Location();
 		location.setLocationName(locationName);
 		location.setLocationType(Version1LocationType.PERMANENT_LOCATION);
 		return location;
 	}
 
 	public static ItemOptionalFields parseItemOptionalFields(AlephItem alephItem) {
 		ItemOptionalFields iof = new ItemOptionalFields();
 
 		if (alephItem.getCirculationStatus() != null)
 			iof.setCirculationStatus(alephItem.getCirculationStatus());
 
 		if (alephItem.getHoldQueueLength() >= 0)
 			iof.setHoldQueueLength(new BigDecimal(alephItem.getHoldQueueLength()));
 
 		if (alephItem.getElectronicResource() != null)
 			iof.setElectronicResource(alephItem.getElectronicResource());
 
 		if (alephItem.getCallNumber() != null || alephItem.getCopyNumber() != null || alephItem.getDescription() != null) {
 			ItemDescription description = new ItemDescription();
 
 			if (alephItem.getCallNumber() != null)
 				description.setCallNumber(alephItem.getCallNumber());
 
 			if (alephItem.getCopyNumber() != null)
 				description.setCopyNumber(alephItem.getCopyNumber());
 
 			if (alephItem.getDescription() != null)
 				description.setHoldingsInformation(alephItem.getDescription());
 
 			if (alephItem.getNumberOfPieces() != null)
 				description.setNumberOfPieces(alephItem.getNumberOfPieces());
 
 			iof.setItemDescription(description);
 		}
 
 		if (alephItem.getItemUseRestrictionTypes().size() > 0)
 			iof.setItemUseRestrictionTypes(alephItem.getItemUseRestrictionTypes());
 
 		if (alephItem.getLocation() != null || alephItem.getCollection() != null) {
 			Location location = new Location();
 
 			LocationName locationName = new LocationName();
 
 			List<LocationNameInstance> locationNameInstances = new ArrayList<LocationNameInstance>();
 
 			if (alephItem.getLocation() != null)
 				locationNameInstances.add(createLocationNameInstance(alephItem.getLocation(), new BigDecimal(1)));
 
 			if (alephItem.getCollection() != null)
 				locationNameInstances.add(createLocationNameInstance(alephItem.getCollection(), new BigDecimal(2)));
 
 			locationName.setLocationNameInstances(locationNameInstances);
 
 			location.setLocationName(locationName);
 			location.setLocationType(Version1LocationType.PERMANENT_LOCATION);
 
 			iof.setLocations(Arrays.asList(location));
 		}
 
 		if (alephItem.getBarcode() != null)
 			iof.setBibliographicDescription(alephItem.getBarcode());
 
 		return iof;
 	}
 
 	public static LocationNameInstance createLocationNameInstance(String locationNameValue, BigDecimal locationNameLevel) {
 		LocationNameInstance locationNameInstance = new LocationNameInstance();
 		locationNameInstance.setLocationNameValue(locationNameValue);
 		locationNameInstance.setLocationNameLevel(locationNameLevel);
 		return locationNameInstance;
 	}
 
 	public static ItemUseRestrictionType parseItemUseRestrictionType(String itemRestriction) {
 		ItemUseRestrictionType itemUseRestrictionType = null;
 
 		if (!itemRestrictionClassesInitialized)
 			initializeItemRestrictionClasses();
 
 		boolean isLibraryOnlyRestrictionType = itemRestrictionClasses.get(AlephConstants.ITEM_RESTRICTION_IN_LIBRARY_USE_ONLY).contains(itemRestriction);
 		boolean isLoanPeriodRestrictionType = itemRestrictionClasses.get(AlephConstants.ITEM_RESTRICTION_LOAN_PERIOD).contains(itemRestriction);
 
 		if (isLibraryOnlyRestrictionType) {
 
 			if (itemRestriction.equals(AlephConstants.ITEM_STATUS_TO_THE_MUSIC_CORNER_ONLY_4F))
 				itemUseRestrictionType = Version1ItemUseRestrictionType.SUPERVISION_REQUIRED;
 
 			else if (itemRestriction.equals(AlephConstants.ITEM_STATUS_REFERENCE_ONLY_SPN_2F))
 				itemUseRestrictionType = Version1ItemUseRestrictionType.USE_ONLY_IN_CONTROLLED_ACCESS;
 
 			else if (itemRestriction.equals(AlephConstants.ITEM_STATUS_RETRO))
 				itemUseRestrictionType = Version1ItemUseRestrictionType.NOT_FOR_LOAN;
 			else
 				itemUseRestrictionType = Version1ItemUseRestrictionType.IN_LIBRARY_USE_ONLY;
 
 		} else if (isLoanPeriodRestrictionType) {
 
 			if (itemRestriction.equals(AlephConstants.ITEM_STATUS_LONG_TERM_LOAN))
 				itemUseRestrictionType = Version1ItemUseRestrictionType.LIMITED_CIRCULATION_LONG_LOAN_PERIOD;
 
 			else if (itemRestriction.equals(AlephConstants.ITEM_STATUS_MONTH) || itemRestriction.equals(AlephConstants.ITEM_STATUS_OPEN_STOCK_MONTH))
 				itemUseRestrictionType = Version1ItemUseRestrictionType.LIMITED_CIRCULATION_NORMAL_LOAN_PERIOD;
 
 			else
 				itemUseRestrictionType = Version1ItemUseRestrictionType.LIMITED_CIRCULATION_SHORT_LOAN_PERIOD;
 		} else
 			itemUseRestrictionType = new ItemUseRestrictionType("localized", itemRestriction);
 
 		return itemUseRestrictionType;
 	}
 
 	public static ItemTransaction parseItemTransaction(AlephItem alephItem) {
 
 		ItemTransaction itemTransaction = null;
 
 		boolean borrowingUsersSet = alephItem.getBorrowingUsers() != null && alephItem.getBorrowingUsers().size() > 0;
 		boolean requestingUsersSet = alephItem.getRequestingUsers() != null && alephItem.getRequestingUsers().size() > 0;
 
 		if (borrowingUsersSet || requestingUsersSet) {
 
 			itemTransaction = new ItemTransaction();
 
 			if (borrowingUsersSet) {
 				CurrentBorrower borrower = new CurrentBorrower();
 				UserId userId = new UserId();
 				AlephXServicesUser alephUser = alephItem.getBorrowingUsers().get(0);
 				userId.setUserIdentifierValue(alephUser.getAuthenticatedUsername());
 				userId.setUserIdentifierType(new UserIdentifierType("Username"));
 				userId.setAgencyId(new AgencyId(alephUser.getAgency().getAgencyId()));
 				borrower.setUserId(userId);
 				itemTransaction.setCurrentBorrower(borrower);
 			}
 
 			if (requestingUsersSet) {
 				Iterator<AlephXServicesUser> i = alephItem.getRequestingUsers().iterator();
 				while (i.hasNext()) {
 					AlephXServicesUser alephUser = i.next();
 					CurrentRequester requester = new CurrentRequester();
 					UserId userId = new UserId();
 					userId.setUserIdentifierValue(alephUser.getAuthenticatedUsername());
 					userId.setUserIdentifierType(new UserIdentifierType("Username"));
 					userId.setAgencyId(new AgencyId(alephUser.getAgency().getAgencyId()));
 					requester.setUserId(userId);
 					List<CurrentRequester> requesters = itemTransaction.getCurrentRequesters();
 					if (requesters == null)
 						requesters = new ArrayList<CurrentRequester>();
 					requesters.add(requester);
 					itemTransaction.setCurrentRequesters(requesters);
 				}
 
 			}
 		}
 		return itemTransaction;
 	}
 
 	public static boolean inDaylightTime() {
 		return TimeZone.getDefault().inDaylightTime(new Date());
 	}
 
 	public static String convertToAlephDate(GregorianCalendar gregorianCalendar) {
 		if (gregorianCalendar == null)
 			return null;
 		// We need: 20141231
 
 		String month = Integer.toString(gregorianCalendar.get(Calendar.MONTH) + 1);
 		if (month.length() < 2)
 			month = "0" + month;
 		String day = Integer.toString(gregorianCalendar.get(Calendar.DAY_OF_MONTH));
 		if (day.length() < 2)
 			day = "0" + day;
 		return Integer.toString(gregorianCalendar.get(Calendar.YEAR)) + month + day;
 	}
 
 	public static String parseRecordIdFromAlephItemId(String itemId) {
 		return itemId.split(AlephConstants.UNIQUE_ITEM_ID_SEPARATOR)[0];
 	}
 
 	public static String parseItemIdFromAlephItemId(String itemId) {
		return itemId.split(AlephConstants.UNIQUE_ITEM_ID_SEPARATOR)[1];
 	}
 
 	public static MediumType detectMediumType(String mediumTypeParsed) {
 		return detectMediumType(mediumTypeParsed, false);
 	}
 
 	public static MediumType detectMediumType(String mediumTypeParsed, boolean localizationDesired) {
 		MediumType mediumType = null;
 
 		// TODO: Find out Aleph non-localized values & change these AlephLocalization.SOMETHING to AlephConstants.SOMETHING ...
 
 		if (localizationDesired) {
 			mediumType = new MediumType("localized", mediumTypeParsed);
 		} else {
 			if (mediumTypeParsed.matches(AlephLocalization.BOOK + "|" + AlephLocalization.GRAPHICS + "|" + AlephLocalization.MAP))
 				mediumType = Version1MediumType.BOOK;
 			else if (mediumTypeParsed.matches(AlephLocalization.MAGAZINE1 + "|" + AlephLocalization.MAGAZINE2))
 				mediumType = Version1MediumType.MAGAZINE;
 			else if (mediumTypeParsed.equalsIgnoreCase(AlephLocalization.COMPACT_DISC))
 				mediumType = Version1MediumType.CD_ROM;
 			else if (mediumTypeParsed.equalsIgnoreCase(AlephLocalization.AUDIO_TAPE))
 				mediumType = Version1MediumType.AUDIO_TAPE;
 			else {
 				mediumType = new MediumType("localized", mediumTypeParsed);
 			}
 		}
 		return mediumType;
 	}
 
 	public static PhysicalAddress parsePhysicalAddress(String address) {
 
 		PhysicalAddress physicalAddress = new PhysicalAddress();
 		StructuredAddress structuredAddress = new StructuredAddress();
 
 		String[] addressParts = address.split(AlephConstants.UNSTRUCTURED_ADDRESS_SEPARATOR);
 
 		int addressPartsLength = addressParts.length;
 
 		if (addressPartsLength > 2) {
 			// In this case address = "Vysokoškolské koleje VUT - Kolejní 2, A05/237, 612 00 Brno";
 			structuredAddress.setStreet(addressParts[0]);
 			structuredAddress.setPostOfficeBox(addressParts[1]);
 
 			String postalCode = addressParts[2].substring(0, AlephConstants.POSTAL_CODE_LENGTH);
 			structuredAddress.setPostalCode(postalCode);
 
 			String city = addressParts[2].substring(AlephConstants.POSTAL_CODE_LENGTH);
 			structuredAddress.setLocality(city.trim());
 		} else if (addressPartsLength > 1) {
 			// F.e. Trvalá ulice 123, 12345 Trvalé
 			structuredAddress.setStreet(addressParts[0]);
 
 			String postalCode = addressParts[1].substring(0, AlephConstants.POSTAL_CODE_LENGTH);
 			structuredAddress.setPostalCode(postalCode);
 
 			String city = addressParts[1].substring(AlephConstants.POSTAL_CODE_LENGTH);
 			structuredAddress.setLocality(city.trim());
 		} else
 			structuredAddress.setStreet(addressParts[0]);
 
 		physicalAddress.setStructuredAddress(structuredAddress);
 		physicalAddress.setPhysicalAddressType(Version1PhysicalAddressType.POSTAL_ADDRESS);
 
 		return physicalAddress;
 	}
 
 	/**
 	 * Pattern for building unique item Id from document number & item sequence item is:<br/>
 	 * bibLibrary + docRecordNumber + "-" + admLibrary + docItemNumber + itemSequenceNumber<br/>
 	 * <br/>
	 * 
 	 * Output should look like this example:<br />
 	 * MZK01000000421-MZK50000062021000010 <br />
 	 * 
 	 * @param localConfig
 	 * @param docRecordNumber
 	 * @param docItemNumber
 	 * @param itemSequenceNumber
 	 * @return
 	 */
 	public static String buildAlephItemId(String documentRecordNumber, String documentItemNumber, String itemSequenceNumber) {
 		String itemId = LocalConfig.getBibLibrary() + documentRecordNumber + AlephConstants.UNIQUE_ITEM_ID_SEPARATOR + LocalConfig.getAdmLibrary() + documentItemNumber
 				+ itemSequenceNumber;
 		return itemId;
 	}
 
 	public static BlockOrTrap parseBlockOrTrap(String parsedBlock) {
 		BlockOrTrap blockOrTrap = new BlockOrTrap();
 
 		blockOrTrap.setBlockOrTrapType(new BlockOrTrapType("http://www.niso.org/ncip/v1_0/imp1/schemes/blockortraptype/blockortraptype.scm", parsedBlock));
 
 		return blockOrTrap;
 	}
 
 	/**
 	 * Tries to convert date string parsed from aleph response to GregorianCalendar format.<br />
 	 * Throws SAXException if not successful.
 	 * 
 	 * @param alephDateParsed
 	 * @return gregorianCalendarDate
 	 * @throws SAXException
 	 */
 	public static GregorianCalendar parseGregorianCalendarFromAlephDate(String alephDateParsed) throws SAXException {
 		if (!alephDateParsed.equalsIgnoreCase("00000000")) {
 			GregorianCalendar gregorianCalendarDate = new GregorianCalendar(TimeZone.getDefault());
 
 			try {
 				gregorianCalendarDate.setTime(AlephConstants.ALEPH_DATE_FORMATTER.parse(alephDateParsed));
 				if (inDaylightTime())
 					gregorianCalendarDate.add(Calendar.HOUR_OF_DAY, 3);
 				else
 					gregorianCalendarDate.add(Calendar.HOUR_OF_DAY, 1);
 			} catch (ParseException e) {
 				throw new SAXException(e);
 			}
 
 			return gregorianCalendarDate;
 		} else
 			return null;
 	}
 
 	public static CirculationStatus parseCirculationStatus(String circulationStatusVal) {
 		CirculationStatus circulationStatus;
 		if (circulationStatusVal.matches(AlephConstants.CIRC_STATUS_ON_SHELF + "|" + AlephConstants.CIRC_STATUS_REQUESTED + "|" + AlephConstants.CIRC_STATUS_PROCESSING)) {
 			if (circulationStatusVal.equalsIgnoreCase(AlephConstants.CIRC_STATUS_ON_SHELF)) {
 				circulationStatus = Version1CirculationStatus.AVAILABLE_ON_SHELF;
 			} else if (circulationStatusVal.equalsIgnoreCase(AlephConstants.CIRC_STATUS_REQUESTED)) {
 				circulationStatus = Version1CirculationStatus.ON_LOAN;
 			} else
 				// Status = processing
 				circulationStatus = Version1CirculationStatus.IN_PROCESS;
 
 		} else
 			circulationStatus = new CirculationStatus("localized", circulationStatusVal);
 		return circulationStatus;
 	}
 
 	public static ResponseHeader reverseInitiationHeader(NCIPInitiationData initData) {
 
 		InitiationHeader initiationHeader = initData.getInitiationHeader();
 		ResponseHeader responseHeader = null;
 
 		if (initiationHeader != null) {
 			responseHeader = new ResponseHeader();
 
 			if (initiationHeader.getFromAgencyId() != null && initiationHeader.getToAgencyId() != null) {
 				// Reverse From/To AgencyId because of the request was processed (return to initiator)
 				ToAgencyId toAgencyId = new ToAgencyId();
 				toAgencyId.setAgencyIds(initiationHeader.getFromAgencyId().getAgencyIds());
 
 				FromAgencyId fromAgencyId = new FromAgencyId();
 				fromAgencyId.setAgencyIds(initiationHeader.getToAgencyId().getAgencyIds());
 
 				responseHeader.setFromAgencyId(fromAgencyId);
 				responseHeader.setToAgencyId(toAgencyId);
 			}
 			if (initiationHeader.getFromSystemId() != null && initiationHeader.getToSystemId() != null) {
 				responseHeader.setFromSystemId(initiationHeader.getFromSystemId());
 				responseHeader.setToSystemId(initiationHeader.getToSystemId());
 				if (initiationHeader.getFromAgencyAuthentication() != null && !initiationHeader.getFromAgencyAuthentication().isEmpty())
 					responseHeader.setFromSystemAuthentication(initiationHeader.getFromAgencyAuthentication());
 			}
 		}
 		return responseHeader;
 	}
 
 	public static boolean isCorrectRecordId(String recordId, int bibLibLength) {
		if (recordId.length() == AlephConstants.BIB_ID_LENGTH + bibLibLength) {
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isCorrectItemId(String sequenceNumber, int bibLibLength) {
		if (sequenceNumber.length() == AlephConstants.BIB_ID_LENGTH + bibLibLength + AlephConstants.ITEM_ID_UNIQUE_PART_LENGTH) {
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isCorrectLoanId(String alephLoanId, int bibLibLength) {
 		// Loan Id has the same length specifications as Record Id
 		return isCorrectRecordId(alephLoanId, bibLibLength);
 	}
 
 	/**
 	 * Marks expired tokens as expired.<br>
 	 * Note that these expired tokens are removed immediately after new token is created.<br>
 	 * The purpose of not removing those earlier is to let user know of the token expiration.
 	 * 
 	 * @param tokens
 	 * @param tokenExpirationTimeInSeconds
 	 */
 	public static void markExpiredTokens(HashMap<String, ItemToken> tokens, int tokenExpirationTimeInSeconds) {
 		long theTime = new Date().getTime();
 
 		for (ItemToken token : tokens.values()) {
 			if (token.getTimeCreated() + tokenExpirationTimeInSeconds * 1000 < theTime) {
 				token.setExpired();
 			}
 		}
 	}
 
 	/**
 	 * Removes expired tokens from memory hashmap.
 	 * 
 	 * @param tokens
 	 */
 	public static void purgeExpiredTokens(HashMap<String, ItemToken> tokens) {
 		for (Map.Entry<String, ItemToken> tokenEntry : tokens.entrySet()) {
 			if (tokenEntry.getValue().isExpired())
 				tokens.remove(tokenEntry.getKey());
 		}
 	}
 
 	public static BibliographicItemId createBibliographicItemIdAsISBN(String bibliographicItemIdentifier) {
 		BibliographicItemId bibliographicItemId = new BibliographicItemId();
 		bibliographicItemId.setBibliographicItemIdentifier(bibliographicItemIdentifier);
 		bibliographicItemId.setBibliographicItemIdentifierCode((Version1BibliographicItemIdentifierCode.ISBN));
 		return bibliographicItemId;
 	}
 
 	public static BibliographicItemId createBibliographicItemIdAsLegalDepositNumber(String bibliographicItemIdentifier) {
 		BibliographicItemId bibliographicItemId = new BibliographicItemId();
 		bibliographicItemId.setBibliographicItemIdentifier(bibliographicItemIdentifier);
 		bibliographicItemId.setBibliographicItemIdentifierCode(Version1BibliographicItemIdentifierCode.LEGAL_DEPOSIT_NUMBER);
 		return bibliographicItemId;
 	}
 
 	public static BibliographicItemId createBibliographicItemIdAsURI(String bibliographicItemIdentifier) {
 		BibliographicItemId bibliographicItemId = new BibliographicItemId();
 		bibliographicItemId.setBibliographicItemIdentifier(bibliographicItemIdentifier);
 		bibliographicItemId.setBibliographicItemIdentifierCode(Version1BibliographicItemIdentifierCode.URI);
 		return bibliographicItemId;
 	}
 
 	/**
 	 * Builds an XML POST in order to send it as a Http request to renew an item.
	 * 
 	 * You can set desired due date - but your Aleph settings may not be compatible with
	 * 
 	 * setting custom due date & will add by default let's say 5 days to old due date.<br>
 	 * <br>
 	 * That's why you can set it's argument to null & nothing extra will happen
 	 * 
 	 * @param desiredDueDate
 	 * @return renewXMLtoPOST
 	 */
 	public static String buildRenewPOSTXml(GregorianCalendar desiredDueDate) {
 		if (desiredDueDate != null)
 			return buildRenewPOSTXml(AlephUtil.convertToAlephDate(desiredDueDate));
 		else
 			return buildRenewPOSTXml("20991231");
 	}
 
 	private static String buildRenewPOSTXml(String desiredDueDate) {
 		return "<?xml version = \"1.0\" encoding = \"UTF-8\"?>" + "<get-pat-loan><loan renew=\"Y\"><z36><z36-due-date>" + desiredDueDate
 				+ "</z36-due-date></z36></loan></get-pat-loan>";
 	}
 
 	public static RequestStatusType parseRequestStatusTypeFromZ37StatusNode(String value) {
 		RequestStatusType requestStatusType;
 		if (value == "S")
 			requestStatusType = Version1RequestStatusType.AVAILABLE_FOR_PICKUP;
 		else
 			requestStatusType = Version1RequestStatusType.IN_PROCESS;
 		return requestStatusType;
 	}
 
 	public static HoldingsInformation createHoldingsInformationUnscructured(String description) {
 		HoldingsInformation holdingsInformation = new HoldingsInformation();
 		holdingsInformation.setUnstructuredHoldingsData(description);
 		return holdingsInformation;
 	}
 
 	public static int parseHoldQueueLengthFromStatusNode(String parsedStatus) {
 		// Parsing this : Waiting in position 1 in queue; current due date 05/Jan/2015
 		// Or this: In process - in this case return -1
 		String[] words = parsedStatus.split("\\s+");
 		if (words.length < 6)
 			return -1;
 		else
 			return Integer.parseInt(words[3]);
 	}
 }
