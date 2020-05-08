 package org.extensiblecatalog.ncip.v2.aleph.restdlf.item;
 
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephConstants;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephConstants.Availability;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.agency.AlephAgency;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.user.AlephUser;
 import org.extensiblecatalog.ncip.v2.service.CirculationStatus;
 import org.extensiblecatalog.ncip.v2.service.Version1CirculationStatus;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * An item returned from Aleph
  * 
  * @author Rick Johnson (NDU)
  *
  */
 public class AlephItem implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 65004L;
 	private String bibId;
 	private String itemId;
 	private String holdingsId;
 	private String description;
 	private String location;
 	private String link;
 	private String callNumber;
 	private String author;
 	private String isbn;
 	private String mediumType;
 	private String subLibrary;
 	private String collection;
 
 	private String docNumber;
 	private String seqNumber;
 	
 	private String publisher;
 	private String series;
 	private String title;
 	
 	private CirculationStatus circulationStatus;
 	private String electronicResource;
 	private String sessionId;
 	private String barcode;
 	
 	private Date dateHoldRequested;
 	private Date dateAvailablePickup;
 	
 	private Date dueDate;
 	
 	private int holdQueueLength = 0;
 	
 	private BigDecimal fineAmount;
 	private BigDecimal numberOfPieces;
 	private Date fineAccrualDate;
 	
 	private List<AlephUser> borrowingUsers;
 	private List<AlephUser> requestingUsers;
 	
 	private Availability availability;
 	
 	private String fineStatus;
 	private String creditDebit;
 	
 	private String holdRequestId;
 	private AlephAgency agency;
 	private String holdQueue;
 	private String publicationDate;
 	private String copyNumber;
 	private boolean exists = true;
 	
 	public AlephItem(){
 		borrowingUsers = new ArrayList<AlephUser>();
 		requestingUsers = new ArrayList<AlephUser>();
 	}
 	
 	public AlephItem doesntExists() {
 		this.exists = false;
 		return this;
 	}
 	
 	/**
 	 * @return the bibId
 	 */
 	public String getBibId() {
 		return bibId;
 	}
 	
 	/**
 	 * @param bibId the bibId to set
 	 */
 	public void setBibId(String bibId) {
 		this.bibId = bibId;
 		if (this.bibId!=null&&this.bibId.length()<AlephConstants.BIB_ID_LENGTH){
 			//ensure at least 9 characters, if not pad zeros on front
 			while (this.bibId.length()<AlephConstants.BIB_ID_LENGTH){
 				this.bibId = "0"+this.bibId;
 			}
 		}
 	}
 	
 	/**
 	 * @return the admId
 	 */
 	public String getItemId() {
 		return itemId;
 	}
 	
 	/**
 	 * @param itemId the admId to set
 	 */
 	public void setItemId(String itemId) {
 		this.itemId = itemId;
 	}
 	
 	private void updateItemId(){
 		if (getDocNumber()!=null&&getSeqNumber()!=null){
 			setItemId(getDocNumber()+getSeqNumber());
 		}
 	}
 	
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 	
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	/**
 	 * @return the location
 	 */
 	public String getLocation() {
 		return location;
 	}
 	
 	/**
 	 * @param location the location to set
 	 */
 	public void setLocation(String location) {
 		this.location = location;
 	}
 	
 	/**
 	 * @return the link
 	 */
 	public String getLink() {
 		return link;
 	}
 	
 	/**
 	 * @param link the link to set
 	 */
 	public void setLink(String link) {
 		this.link = link;
 	}
 	
 	/**
 	 * @return the callNumber
 	 */
 	public String getCallNumber() {
 		return callNumber;
 	}
 	
 	/**
 	 * @param callNumber the callNumber to set
 	 */
 	public void setCallNumber(String callNumber) {
 		this.callNumber = callNumber;
 		//check for any special characters that should be removed
 		if (callNumber!=null){
 			this.callNumber = this.callNumber.replaceAll("&nbsp;", " ");
 		}
 	}
 	
 	/**
 	 * @return the author
 	 */
 	public String getAuthor() {
 		return author;
 	}
 	
 	/**
 	 * @param author the author to set
 	 */
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 	
 	/**
 	 * @return the isbn
 	 */
 	public String getIsbn() {
 		return isbn;
 	}
 	
 	/**
 	 * @param isbn the isbn to set
 	 */
 	public void setIsbn(String isbn) {
 		this.isbn = isbn;
 	}
 	
 	/**
 	 * @return the mediumType
 	 */
 	public String getMediumType() {
 		return mediumType;
 	}
 	
 	/**
 	 * @param mediumType the mediumType to set
 	 */
 	public void setMediumType(String mediumType) {
 		this.mediumType = mediumType;
 	}
 
 	public String getSubLibrary() {
 		return subLibrary;
 	}
 	public void setSubLibrary(String subLibrary) {
 		this.subLibrary = subLibrary;
 	}
 
 	public String getCollection() {
 		return collection;
 	}
 	public void setCollection(String collection) {
 		this.collection = collection;
 	}
 	
 	/**
 	 * @return the publisher
 	 */
 	public String getPublisher() {
 		return publisher;
 	}
 	
 	/**
 	 * @param publisher the publisher to set
 	 */
 	public void setPublisher(String publisher) {
 		this.publisher = publisher;
 	}
 	
 	/**
 	 * @return the series
 	 */
 	public String getSeries() {
 		return series;
 	}
 	
 	/**
 	 * @param series the series to set
 	 */
 	public void setSeries(String series) {
 		this.series = series;
 	}
 	
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 	
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	
 	/**
 	 * @return the circulationStatus
 	 */
 	public CirculationStatus getCirculationStatus() {
 		return circulationStatus;
 	}
 	
 	/**
 	 * @param circulationStatus the circulationStatus to set
 	 */
 	public void setCirculationStatus(String circulationStatus) {
 		this.circulationStatus = new CirculationStatus(Version1CirculationStatus.VERSION_1_CIRCULATION_STATUS, circulationStatus);
		if(! circulationStatus.equalsIgnoreCase("On Shelf"))
 			try {
 				this.setDateAvailablePickup(circulationStatus);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 	}
 	
 	/**
 	 * @param circulationStatus the circulationStatus to set
 	 */
 	public void setCirculationStatus(CirculationStatus circulationStatus) {
 		this.circulationStatus = circulationStatus;
 	}
 	
 	/**
 	 * @return the electronicResource
 	 */
 	public String getElectronicResource() {
 		return electronicResource;
 	}
 	
 	/**
 	 * @param electronicResource the electronicResource to set
 	 */
 	public void setElectronicResource(String electronicResource) {
 		this.electronicResource = electronicResource;
 	}
 	
 	/**
 	 * @param sessionId the sessionId to set
 	 */
 	public void setSessionId(String sessionId) {
 		this.sessionId = sessionId;
 	}
 
 	/**
 	 * @return the sessionId
 	 */
 	public String getSessionId() {
 		return sessionId;
 	}
 
 	/**
 	 * @param holdingsId the holdingsId to set
 	 */
 	public void setHoldingsId(String holdingsId) {
 		this.holdingsId = holdingsId;
 		if (this.holdingsId!=null&&this.holdingsId.length()<AlephConstants.HOLDINGS_ID_LENGTH){
 			//ensure at least 9 characters, if not pad zeros on front
 			while (this.holdingsId.length()<AlephConstants.HOLDINGS_ID_LENGTH){
 				this.holdingsId = "0"+this.holdingsId;
 			}
 		}
 	}
 
 	/**
 	 * @return the holdingsId
 	 */
 	public String getHoldingsId() {
 		return holdingsId;
 	}
 
 	/**
 	 * @param holdQueueLength the holdQueueLength to set
 	 */
 	public void setHoldQueueLength(int holdQueueLength) {
 		this.holdQueueLength = holdQueueLength;
 	}
 
 	/**
 	 * @return the holdQueueLength
 	 */
 	public int getHoldQueueLength() {
 		return holdQueueLength;
 	}
 	
 	/**
 	 * @return the holdQueueErrorMessage
 	 */
 	public String getHoldQueue() {
 		return holdQueue;
 	}
 	
 	/**
 	 * Add a borrowing user to this aleph item.
 	 * It will not add it to the internal list if it already exists
 	 * 
 	 * @param user
 	 */
 	public void addBorrowingUser(AlephUser user){
 		if (!borrowingUsers.contains(user)){
 			borrowingUsers.add(user);
 		}
 	}
 	
 	/**
 	 * @return the borrowingUsers
 	 */
 	public List<AlephUser> getBorrowingUsers() {
 		return borrowingUsers;
 	}
 	
 	/**
 	 * Add a requesting user to this aleph item.
 	 * It will not add it to the internal list if it already exists
 	 * 
 	 * @param user
 	 */
 	public void addRequestingUser(AlephUser user){
 		if (!requestingUsers.contains(user)){
 			requestingUsers.add(user);
 		}
 	}
 	
 	/**
 	 * @return the requestingUsers
 	 */
 	public List<AlephUser> getRequestingUsers() {
 		return requestingUsers;
 	}
 	/**
 	 * @param barcode the barcode to set
 	 */
 	public void setBarcode(String barcode) {
 		this.barcode = barcode;
 	}
 	/**
 	 * @return the barcode
 	 */
 	public String getBarcode() {
 		return barcode;
 	}
 	/**
 	 * @param docNumber the docNumber to set
 	 */
 	public void setDocNumber(String docNumber) {
 		this.docNumber = docNumber;
 		//make sure docnumber is 9 digits, if not add leading zeroes
 		while (this.docNumber!=null&&this.docNumber.length()<AlephConstants.DOC_NUMBER_LENGTH){
 			this.docNumber = "0"+this.docNumber;	
 		}
 		//update the item id if necessary
 		updateItemId();
 	}
 	
 	/**
 	 * @return the docNumber
 	 */
 	public String getDocNumber() {
 		return docNumber;
 	}
 	
 	/**
 	 * @param seqNumber the seqNumber to set
 	 */
 	public void setSeqNumber(String seqNumber) {
 		this.seqNumber = seqNumber;
 		//make sure seqnumber is 6 digits, if not add leading zeroes
 		while (this.seqNumber!=null&&this.seqNumber.length()<AlephConstants.SEQ_NUMBER_LENGTH){
 			this.seqNumber = "0"+this.seqNumber;	
 		}
 		//update the item id if necessary
 		updateItemId();
 	}
 	/**
 	 * @return the seqNumber
 	 */
 	public String getSeqNumber() {
 		return seqNumber;
 	}
 	/**
 	 * @param dateHoldRequested the dateHoldRequested to set
 	 */
 	public void setDateHoldRequested(Date dateHoldRequested) {
 		this.dateHoldRequested = dateHoldRequested;
 	}
 	
 	public void setDateHoldRequested(String date) throws ParseException{
 		//assume date in right format and parse, ignore empty values
 		if (date!=null&&date.length()>0){
 			SimpleDateFormat dateFormatter = new SimpleDateFormat(AlephConstants.HOLD_DATE_FORMAT);
 			setDateHoldRequested(dateFormatter.parse(date));
 		}
 	}
 	
 	/**
 	 * @return the dateHoldRequested
 	 */
 	public Date getDateHoldRequested() {
 		return dateHoldRequested;
 	}
 	
 	/**
 	 * @param dateAvailablePickup the dateAvailablePickup to set
 	 */
 	public void setDateAvailablePickup(Date dateAvailablePickup) {
 		this.dateAvailablePickup = dateAvailablePickup;
 	}
 	
 	public void setDateAvailablePickup(String date) throws ParseException{
 		//assume date in right format and parse, ignore empty values
 		if (date!=null&&date.length()>0){
 			SimpleDateFormat dateFormatter = new SimpleDateFormat(AlephConstants.HOLD_DATE_FORMAT);
 			setDateAvailablePickup(dateFormatter.parse(date));
 		}
 	}
 	
 	/**
 	 * @return the dateAvailablePickup
 	 */
 	public Date getDateAvailablePickup() {
 		return dateAvailablePickup;
 	}
 	/**
 	 * @param available the available to set
 	 */
 	public void setAvailability(Availability available) {
 		this.availability = available;
 	}
 	/**
 	 * @return the available
 	 */
 	public Availability getAvailability() {
 		return availability;
 	}
 	/**
 	 * @param dueDate the dueDate to set
 	 */
 	public void setDueDate(Date dueDate) {
 		this.dueDate = dueDate;
 	}
 	
 	/**
 	 * Parse date from string and set to value if parsing successful
 	 * @param date
 	 * @throws ParseException
 	 */
 	public void setDueDateRenewal(String date) throws ParseException{
 		setDueDate(date,AlephConstants.RENEW_DUE_DATE_FORMAT);
 	}
 	
 	/**
 	 * Parse date from string and set to value if parsing successful
 	 * @param date
 	 * @throws ParseException
 	 */
 	public void setDueDateLoan(String date) throws ParseException{
 		setDueDate(date,AlephConstants.LOAN_DUE_DATE_FORMAT);
 	}
 	
 	/**
 	 * Parse date from string and set to value if parsing successful
 	 * @param date
 	 * @throws ParseException
 	 */
 	public void setDueDate(String date, String format) throws ParseException{
 		//assume date in right format and parse, ignore empty values
 		if (date!=null&&date.length()>0){
 			SimpleDateFormat dateFormatter = new SimpleDateFormat(format);
 			setDueDate(dateFormatter.parse(date));
 		}
 	}
 	
 	/**
 	 * @return the dueDate
 	 */
 	public Date getDueDate() {
 		return dueDate;
 	}
 	
 	/**
 	 * @param fineAmount the fineAmount to set
 	 */
 	public void setFineAmount(double fineAmount) {
 		this.fineAmount = new BigDecimal(fineAmount);
 		this.fineAmount.setScale(2, BigDecimal.ROUND_HALF_UP); 
 	}
 	
 	/**
 	 * @return the fineAmount
 	 */
 	public BigDecimal getFineAmount() {
 		return fineAmount;
 	}
 	
 	/**
 	 * Try to set the fine amount from the string passed in
 	 * 
 	 * @param fineAmount
 	 */
 	public void setFineAmount(String fineAmount){
 		try {
 			setFineAmount(Double.parseDouble(fineAmount));
 		} catch (NumberFormatException nfe){
 			//do nothing...make
 		}
 	}
 	/**
 	 * @param fineAccrualDate the fineAccrualDate to set
 	 */
 	public void setFineAccrualDate(Date fineAccrualDate) {
 		this.fineAccrualDate = fineAccrualDate;
 	}
 	/**
 	 * @return the fineAccrualDate
 	 */
 	public Date getFineAccrualDate() {
 		return fineAccrualDate;
 	}
 	
 	/**
 	 * Parse date from string and set to value if parsing successful
 	 * @param date
 	 * @throws ParseException
 	 */
 	public void setFineAccrualDate(String date) throws ParseException{
 		//assume date in right format and parse, ignore empty values
 		if (date!=null&&date.length()>0){
 			SimpleDateFormat dateFormatter = new SimpleDateFormat(AlephConstants.FINE_ACCRUAL_DATE_FORMAT);
 			setFineAccrualDate(dateFormatter.parse(date));
 		}
 	}
 	/**
 	 * @param fineStatus the fineStatus to set
 	 */
 	public void setFineStatus(String fineStatus) {
 		this.fineStatus = fineStatus;
 	}
 	/**
 	 * @return the fineStatus
 	 */
 	public String getFineStatus() {
 		return fineStatus;
 	}
 	/**
 	 * @param creditDebit the creditDebit to set
 	 */
 	public void setCreditDebit(String creditDebit) {
 		this.creditDebit = creditDebit;
 	}
 	/**
 	 * @return the creditDebit
 	 */
 	public String getCreditDebit() {
 		return creditDebit;
 	}
 	/**
 	 * @param holdRequestId the holdRequestId to set
 	 */
 	public void setHoldRequestId(String holdRequestId) {
 		this.holdRequestId = holdRequestId;
 	}
 	/**
 	 * @return the holdRequestId
 	 */
 	public String getHoldRequestId() {
 		return holdRequestId;
 	}
 	/**
 	 * @param agency the agency to set
 	 */
 	public void setAgency(AlephAgency agency) {
 		this.agency = agency;
 	}
 	/**
 	 * @return the agency
 	 */
 	public AlephAgency getAgency() {
 		return agency;
 	}
 	
 	/**
 	 * This method will update this overwrite anything in this alephItem
 	 * with the alephItem's values passed in if they are not null.
 	 * 
 	 * @param item
 	 */
 	public void updateFromAlephItem(AlephItem item){
 		if (item != null) {
 			if (item.getAgency() != null) {
 				this.setAgency(item.getAgency());
 			}
 			if (item.getAuthor() != null) {
 				this.setAuthor(item.getAuthor());
 			}
 			if (item.getAvailability() != null) {
 				this.setAvailability(item.getAvailability());
 			}
 			if (item.getBarcode() != null) {
 				this.setBarcode(item.getBarcode());
 			}
 			if (item.getBibId() != null) {
 				this.setBibId(item.getBibId());
 			}
 			if (item.getBorrowingUsers() != null) {
 				for (AlephUser user : item.getBorrowingUsers()) {
 					this.addBorrowingUser(user);
 				}
 			}
 			if (item.getCallNumber() != null) {
 				this.setCallNumber(item.getCallNumber());
 			}
 			if (item.getCirculationStatus() != null) {
 				this.setCirculationStatus(item.getCirculationStatus());
 			}
 			if (item.getCreditDebit() != null) {
 				this.setCreditDebit(item.getCreditDebit());
 			}
 			if (item.getDateAvailablePickup() != null) {
 				this.setDateAvailablePickup(item.getDateAvailablePickup());
 			}
 			if (item.getDateHoldRequested() != null) {
 				this.setDateHoldRequested(item.getDateHoldRequested());
 			}
 			if (item.getDescription() != null) {
 				this.setDescription(item.getDescription());
 			}
 			if (item.getDocNumber() != null) {
 				this.setDocNumber(item.getDocNumber());
 			}
 			if (item.getDueDate() != null) {
 				this.setDueDate(item.getDueDate());
 			}
 			if (item.getElectronicResource() != null) {
 				this.setElectronicResource(item.getElectronicResource());
 			}
 			if (item.getFineAccrualDate() != null) {
 				this.setFineAccrualDate(item.getFineAccrualDate());
 			}
 			if (item.getFineAmount() != null) {
 				this.setFineAmount(item.getFineAmount().doubleValue());
 			}
 			if (item.getFineStatus() != null) {
 				this.setFineStatus(item.getFineStatus());
 			}
 			if (item.getHoldingsId() != null) {
 				this.setHoldingsId(item.getHoldingsId());
 			}
 			if (item.getHoldQueueLength() != this.getHoldQueueLength()) {
 				this.setHoldQueueLength(item.getHoldQueueLength());
 			}
 			if (item.getHoldRequestId() != null) {
 				this.setHoldRequestId(item.getHoldRequestId());
 			}
 			if (item.getIsbn() != null) {
 				this.setIsbn(item.getIsbn());
 			}
 			if (item.getItemId() != null) {
 				this.setItemId(item.getItemId());
 			}
 			if (item.getLocation() != null) {
 				this.setLocation(item.getLocation());
 			}
 			if (item.getMediumType() != null) {
 				this.setMediumType(item.getMediumType());
 			}
 			if (item.getPublisher() != null) {
 				this.setPublisher(item.getPublisher());
 			}
 			if (item.getRequestingUsers() != null) {
 				for (AlephUser user : item.getRequestingUsers()) {
 					this.addRequestingUser(user);
 				}
 			}
 			if (item.getSeqNumber() != null) {
 				this.setSeqNumber(item.getSeqNumber());
 			}
 			if (item.getSeries() != null) {
 				this.setSeries(item.getSeries());
 			}
 			if (item.getSessionId() != null) {
 				this.setSessionId(item.getSessionId());
 			}
 			if (item.getTitle() != null) {
 				this.setTitle(item.getTitle());
 			}
 		}
 	}
 	
 	public String toString(){
 		StringBuffer sb = new StringBuffer();
 		sb.append("[BibID:"+(getBibId()!=null?getBibId():"null")+"],");
 		sb.append("[ItemID:"+(getItemId()!=null?getItemId():"null")+"],");
 		sb.append("[HoldingsID:"+(getHoldingsId()!=null?getHoldingsId():"null")+"],");
 		sb.append("[DocNumber:"+(getDocNumber()!=null?getDocNumber():"null")+"],");
 		sb.append("[SeqNumber:"+(getSeqNumber()!=null?getSeqNumber():"null")+"],");
 		sb.append("[Barcode:"+(getBarcode()!=null?getBarcode():"null")+"],");
 		sb.append("[Agency:"+(getAgency()!=null&&getAgency().getAgencyId()!=null?getAgency().getAgencyId():"null")+"],");
 		sb.append("[Description:"+(getDescription()!=null?getDescription():"null")+"],");
 		sb.append("[Location:"+(getLocation()!=null?getLocation():"null")+"],");
 		sb.append("[CallNumber:"+(getCallNumber()!=null?getCallNumber():"null")+"],");
 		sb.append("[Author:"+(getAuthor()!=null?getAuthor():"null")+"],");
 		sb.append("[ISBN:"+(getIsbn()!=null?getIsbn():"null")+"],");
 		sb.append("[Medium Type:"+(getMediumType()!=null?getMediumType():"null")+"],");
 		sb.append("[Publisher:"+(getPublisher()!=null?getPublisher():"null")+"],");
 		sb.append("[Series:"+(getSeries()!=null?getSeries():"null")+"],");
 		sb.append("[Title:"+(getTitle()!=null?getTitle():"null")+"],");
 		sb.append("[CirculationStatus:"+(getCirculationStatus()!=null?getCirculationStatus():"null")+"],");
 		sb.append("[Availability:"+(getAvailability()!=null?getAvailability():"null")+"],");
 		sb.append("[ElectronicResource:"+(getElectronicResource()!=null?getElectronicResource():"null")+"],");
 		sb.append("[HoldRequestID:"+(getHoldRequestId()!=null?getHoldRequestId():"null")+"],");
 		sb.append("[DateHoldRequested:"+(getDateHoldRequested()!=null?getDateHoldRequested():"null")+"],");
 		sb.append("[DateAvailablePickup:"+(getDateAvailablePickup()!=null?getDateAvailablePickup():"null")+"],");
 		sb.append("[DueDate:"+(getDueDate()!=null?getDueDate():"null")+"],");
 		sb.append("[HoldQueueLength:"+(getHoldQueueLength()!=-1?getHoldQueueLength():getHoldQueue())+"],");
 		sb.append("[FineAmount:"+(getFineAmount()!=null?getFineAmount().doubleValue():"null")+"],");
 		sb.append("[FineAccrualDate:"+(getFineAccrualDate()!=null?getFineAccrualDate():"null")+"],");
 		sb.append("[FineStatus:"+(getFineStatus()!=null?getFineStatus():"null")+"],");
 		sb.append("[CreditDebit:"+(getCreditDebit()!=null?getCreditDebit():"null")+"],");
 		sb.append("[BorrowingUsers:"+(getBorrowingUsers()!=null?getBorrowingUsers():"null")+"],");
 		sb.append("[RequestingUsers:"+(getRequestingUsers()!=null?getRequestingUsers():"null")+"],");
 		sb.append("[SessionId:"+(getSessionId()!=null?getSessionId():"null")+"],");
 		return sb.toString();
 	}
 	public void setholdQueue(String errorHoldQueueNotFound) {
 		this.holdQueueLength = -1;
 		this.holdQueue = errorHoldQueueNotFound;
 		
 	}
 	public BigDecimal getNumberOfPieces() {
 		return numberOfPieces;
 	}
 	public void setNumberOfPieces(BigDecimal numberOfPieces) {
 		this.numberOfPieces = numberOfPieces;
 	}
 	
 	public String getPublicationDate() {
 		return publicationDate;
 	}
 
 	public void setPublicationDate(String publicationDate) {
 		this.publicationDate = publicationDate;		
 	}
 
 	public String getCopyNumber() {
 		return copyNumber;
 	}
 	
 	public void setCopyNumber(String copyNumber) {
 		this.copyNumber = copyNumber;
 	}
 
 	public boolean exists() {
 		return exists ;
 	}
 
 	public void setAgency(String agencyId) {
 		AlephAgency agency = new AlephAgency();
 		agency.setAgencyId(agencyId);
 		this.agency = agency;
 	}
 }
