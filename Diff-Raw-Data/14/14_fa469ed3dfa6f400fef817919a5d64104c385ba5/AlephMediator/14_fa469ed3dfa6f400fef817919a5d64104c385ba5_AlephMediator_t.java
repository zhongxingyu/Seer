 package org.extensiblecatalog.ncip.v2.aleph.AlephXServices;
 
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.agency.AlephAgency;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.agency.AlephAgencyFactory;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.item.AlephItem;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.item.AlephItemFactory;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.user.AlephUser;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.user.AlephUserFactory;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.xservice.XService;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.xservice.XServiceFactory;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephConfiguration;
 import org.extensiblecatalog.ncip.v2.service.UpdateUserInitiationData;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 //import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 
 
 /**
  * This class implements the Mediator design pattern
  * in order to abstract interaction between Aleph
  * and external services.
  * 
  * @author Rick Johnson (NDU)
  *
  */
 public class AlephMediator implements Serializable{
 	
 	//private final static Logger LOGGER = Logger.getLogger(AlephMediator.class);
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6088723904212979983L;
 	private String xServerName;
 	private String xServerPort;
 	private Map<String,AlephAgency> agencies;
 	private Map<String,AlephConstants.Availability> availabilityCircStatusMap;
 	private String defaultAgencyId;
 	
 	/**
 	 * Protected constructor to limit construction to the factory
 	 */
 	protected AlephMediator() {}
 
 	/**
 	 * Set the X Server Name called with requests
 	 * 
 	 * @param xServerName
 	 */
 	public void setXServerName(String xServerName) {
 		this.xServerName = xServerName;
 	}
 
 	/**
 	 * Set the X Server Name called with requests
 	 * 
 	 * @return Aleph X Server name
 	 */
 	public String getXServerName() {
 		return xServerName;
 	}
 	
 	/**
 	 * Set the X Server Port used with requests
 	 * 
 	 * @param xServerPort
 	 */
 	public void setXServerPort(String xServerPort) {
 		this.xServerPort = xServerPort;
 	}
 
 	/**
 	 * Get the X Server Port used with requests
 	 * 
 	 * @return Aleph X Server port
 	 */
 	public String getXServerPort() {
 		return xServerPort;
 	}
 	
 	/**
 	 * @param agencies the agencies to set
 	 */
 	public void setAgencies(Map<String,AlephAgency> agencies) {
 		this.agencies = agencies;
 	}
 
 	/**
 	 * @return the agencies
 	 */
 	public Map<String,AlephAgency> getAgencies() {
 		return agencies;
 	}
 	
 	/**
 	 * Get the Aleph Agency from the map if it exists using agency id as a key
 	 * 
 	 * @param agencyId
 	 * @return
 	 */
 	public AlephAgency getAlephAgency(String agencyId){
 		if (agencies==null) agencies = new HashMap<String,AlephAgency>();
 		return agencies.get(agencyId);
 	}
 	
 	public void addAlephAgency(String agencyId, String admLibrary, String bibLibrary, String holdLibrary){
 		if (agencies==null) agencies = new HashMap<String,AlephAgency>();
 		AlephAgency agency = AlephAgencyFactory.createAlephAgency(agencyId, admLibrary, bibLibrary, holdLibrary);
 		agencies.put(agencyId, agency);
 	}
 	
 	public void addAlephAgency(AlephAgency agency){
 		if (agencies==null) agencies = new HashMap<String,AlephAgency>();
 		if (agency!=null&&agency.getAgencyId()!=null){
 			agencies.put(agency.getAgencyId(), agency);
 		}
 	}
 	
 	public String getBibLibrary(String agencyId){
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency!=null){
 			return agency.getBibLibrary();
 		} else {
 			return null;
 		}
 	}
 	
 	public String getAdmLibrary(String agencyId){
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency!=null){
 			return agency.getAdmLibrary();
 		} else {
 			return null;
 		}
 	}
 	
 	public String getHoldingsLibrary(String agencyId){
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency!=null){
 			return agency.getHoldingsLibrary();
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * 
 	 * Call the Bor Auth X-Service in order to authenticate a patron against the default agency,
 	 * and credentials passed in.  It will throw an exception if authentication failed.
 	 * 
 	 * @param patron_id
 	 * @param password
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public AlephUser authenticateUser(String patron_id, String password) 
 		throws IOException, ParserConfigurationException, SAXException, AlephException{
 		return authenticateUser(getDefaultAgencyId(),patron_id,password);
 	}
 	
 	/**
 	 * 
 	 * Call the Bor Auth X-Service in order to authenticate a patron against the agency
 	 * and credentials passed in.  It will throw an exception if authentication failed, or
 	 * if the agency id passed in is an unknown agency.
 	 * 
 	 * @param agency id
 	 * @param patron_id
 	 * @param password
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public AlephUser authenticateUser(String agencyId, 
 			String patron_id, String password) throws IOException, ParserConfigurationException, SAXException, AlephException{
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		XService xService = XServiceFactory.createBorAuthXService(agency.getAdmLibrary(), null, patron_id, password);
 		Document doc = xService.execute(getXServerName(), getXServerPort(), false);
 		//generate new aleph user
 		AlephUser user =  AlephUserFactory.createAlephUser(agency,doc);
 		if (user!=null) user.setAuthenticatedUsername(patron_id);
 		return user;
 	}
 	
 	/**
 	 * Lookup user information in the specified library in Aleph
 	 *  
 	 * @param library			the library for the patron
 	 * @param patron_id			the patron id
 	 * @param password			the password for the patron (optional)
 	 * @param getFines 			if true retrieve all fines, if false do not return any fines
 	 * @param getHolds			if true retrieve all holds, if false do not return any hold data
 	 * @param getLoans			if true retrieve all loan data, if false do not return any loan data
 	 * 
 	 * @return AlephUser with specified information if available
 	 */
 	public AlephUser lookupUser(String agencyId, String patron_id, String password, boolean getFines,
 			boolean getHolds, boolean getLoans) throws IOException, ParserConfigurationException, SAXException, AlephException {
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		String cash = AlephConstants.USER_CASH_ALL_DATA;
 		if (!getFines){
 			cash = AlephConstants.USER_CASH_NO_DATA;
 		}
 		
 		String holds = AlephConstants.USER_HOLDS_ALL_DATA;
 		if (!getHolds){
 			holds = AlephConstants.USER_HOLDS_NO_DATA;
 		}
 			
 		String loans = AlephConstants.USER_LOANS_ALL_DATA;
 		if (!getLoans){
 			loans = AlephConstants.USER_LOANS_NO_DATA;
 		}
 		
 		XService xService = XServiceFactory.createBorInfoXService(agency.getAdmLibrary(), patron_id, password, cash, null, holds, loans);
 		Document doc = xService.execute(getXServerName(), getXServerPort(), false);
 		//generate new aleph user
 		return AlephUserFactory.createAlephUser(agency,doc);
 	}
 	
 	/**
 	 * Lookup an item using the bib id and bib library base
 	 * 
 	 * @param bibId
 	 * @param library
 	 * @param getBibInformation
 	 * @param getHoldQueueLength
 	 * @param getCurrentBorrowers
 	 * @param getCurrentRequesters
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXExceptionimport java.util.Iterator;
 	 * @throws AlephException
 	 */
 	public AlephItem lookupItemByBibId(String bibId, String agencyId, boolean getBibInformation, 
 		boolean getHoldQueueLength, boolean getCurrentBorrowers, boolean getCurrentRequesters) 
 		throws IOException,ParserConfigurationException,SAXException,AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		AlephItem item = AlephItemFactory.createAlephItem(agency);
 		item.setBibId(bibId);
 		//get circ status and item id
 		XService xService = XServiceFactory.createItemDataXService(agency.getBibLibrary(), bibId);
 		Document doc = xService.execute(getXServerName(), getXServerPort(), false);
 		item = AlephItemFactory.updateAlephItemParseItemDataResponse(item, doc);
 		
 		if (getBibInformation){
 			xService = XServiceFactory.createFindDocXService(agency.getBibLibrary(), bibId);
 			doc = xService.execute(getXServerName(), getXServerPort(), false);
 			item = AlephItemFactory.updateAlephItemParseFindDocResponse(item, doc);
 		}
 		
 		return item;
 	}
 	
 	/**
 	 * Lookup an item using the bib id and bib library base
 	 * 
 	 * @param bibId
 	 * @param itemId 
 	 * @param library
 	 * @param getBibInformation
 	 * @param getHoldQueueLength
 	 * @param getCurrentBorrowers
 	 * @param getCurrentRequesters
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXExceptionimport java.util.Iterator;
 	 * @throws AlephException
 	 */
 	public AlephItem lookupItemByBibIdItemId(String bibId, String itemId, String agencyId,  boolean getBibInformation, 
 		boolean getHoldQueueLength, boolean getCurrentBorrowers, boolean getCurrentRequesters) 
 		throws IOException,ParserConfigurationException,SAXException,AlephException{
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		List<AlephItem> items = lookupItemsByBibId(bibId, agencyId, getBibInformation, 
 				getHoldQueueLength,getCurrentBorrowers,getCurrentRequesters);
 		if (items!=null&&items.size()>0){
 			if (itemId==null) return items.get(0);
 			for (AlephItem item : items){
 				if (item.getItemId().equals(itemId)){
 					return item;
 				}
 			}
 			//if we get this far, just return the first item in the list
 			return items.get(0);
 		}
 		return AlephItemFactory.createAlephItem(agency);
 	}
 	
 	public List<AlephItem> lookupHoldingsItemsByBibId(String bibId, String agencyId, boolean getBibInformation, 
 				boolean getHoldQueueLength, boolean getCurrentBorrowers, boolean getCurrentRequesters)
 				throws IOException,ParserConfigurationException,SAXException,AlephException{
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		List<AlephItem> items = lookupItemsByBibId(bibId, agencyId, getBibInformation, 
 				getHoldQueueLength, getCurrentBorrowers, getCurrentRequesters);
 		List<AlephItem> holdingsItems = new ArrayList<AlephItem>();
 		for (AlephItem bibItem : items){
 			//call read item and only keep items with correct hold id
 			if (bibItem.getBarcode()!=null){
 				XService xService = XServiceFactory.createReadItemXService(agency.getAdmLibrary(), bibItem.getBarcode());
 				Document doc = xService.execute(getXServerName(), getXServerPort(),false);
 				XMLParserUtil.outputNode(doc);
 				AlephItem readItem = AlephItemFactory.getReadAlephItem(agency, doc);
 				//if ((readItem.getHoldingsId()!=null&&readItem.getHoldingsId().endsWith(holdingsId))||(holdingsId!=null&&holdingsId.endsWith(readItem.getHoldingsId()))){
 			    bibItem.updateFromAlephItem(readItem);
 				holdingsItems.add(bibItem);
 				//}
 			} else if (bibItem.getDocNumber()!=null&&bibItem.getSeqNumber()!=null){
 				XService xService = XServiceFactory.createReadItemXService(agency.getAdmLibrary(), bibItem.getDocNumber(),bibItem.getSeqNumber());
 				Document doc = xService.execute(getXServerName(), getXServerPort(),false);
 				AlephItem readItem = AlephItemFactory.getReadAlephItem(agency, doc);
 				//if ((readItem.getHoldingsId()!=null&&readItem.getHoldingsId().endsWith(holdingsId))||(holdingsId!=null&&holdingsId.endsWith(readItem.getHoldingsId()))){
 				bibItem.updateFromAlephItem(readItem);
 				holdingsItems.add(bibItem);
 				//}
 			}
 		}
 		return holdingsItems;
 	}
 	
 	public List<AlephItem> lookupItemsByBibId(String bibId, String agencyId, boolean getBibInformation, 
 				boolean getHoldQueueLength, boolean getCurrentBorrowers, boolean getCurrentRequesters) 
 				throws IOException,ParserConfigurationException,SAXException,AlephException{
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		//get circ status and item id
 		XService xService = XServiceFactory.createItemDataXService(agency.getBibLibrary(), bibId);
 		Document doc = xService.execute(getXServerName(), getXServerPort(), false);
 		XMLParserUtil.outputNode(doc);
 		List<AlephItem> items = AlephItemFactory.parseItemDataResponse(agency,doc);
 		
 		if (getBibInformation){
 			xService = XServiceFactory.createFindDocXService(agency.getBibLibrary(), bibId);
 			doc = xService.execute(getXServerName(), getXServerPort(), false);
 			//add bib information to each item
 			for (AlephItem item : items){
 				item.setBibId(bibId);
 				item = AlephItemFactory.updateAlephItemParseFindDocResponse(item, doc);
 			}
 		}
 		
 		return items;
 	}
 	
 	/**
 	 * Lookup an item using the item id which equals item id plus seq number, i.e. 001616262000010
 	 *  
 	 * @param itemId
 	 * @param adm_library
 	 * @param bib_library
 	 * @param getBibInformation
 	 * @param getHoldQueueLength
 	 * @param getCurrentBorrowers
 	 * @param getCurrentRequesters
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public AlephItem lookupItemByItemId(String itemId, String agencyId, boolean getBibInformation, 
 			boolean getHoldQueueLength, boolean getCurrentBorrowers, boolean getCurrentRequesters,
 			boolean getCirculationStatus)
 			throws IOException,ParserConfigurationException,SAXException,AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		AlephItem item = AlephItemFactory.createAlephItem(agency);
 		item.setItemId(itemId);
 		//get bib id
 		XService xService = XServiceFactory.createFindDocXService(agency.getAdmLibrary(), itemId);
 		Document doc = xService.execute(getXServerName(), getXServerPort(), false);
 
 		item = AlephItemFactory.updateAlephItemParseFindDocResponse(item, doc);
 		
 		if (getBibInformation&&item.getBibId()!=null){
 			item = lookupItemByBibIdItemId(item.getBibId(),item.getItemId(),agencyId, getBibInformation, 
 					getHoldQueueLength, getCurrentBorrowers, getCurrentRequesters);
 		}
 		
 		if (getCirculationStatus&&item.getCirculationStatus()==null&&item.getBibId()!=null){
 		    Map <String,AlephItem> barcodeStatusMap = new HashMap<String,AlephItem>();
 		    //populate barcode status map by calling circ status for each bib id
 		    barcodeStatusMap.putAll(getCircStatusByBibId(agency,item.getBibId()));
 		    
 		    boolean found = false;
 		    if (item.getBarcode()!=null){
 		    	AlephItem foundItem = barcodeStatusMap.get(item.getBarcode());
 		    	if (foundItem!=null){
 		    		item.setCirculationStatus(foundItem.getCirculationStatus());
 		    		item.setAvailability(this.getAlephItemAvailability(foundItem));
 		    		found = true;
 		    	}
 		    }
 			
 		    if (!found){
 		      item.setAvailability(AlephConstants.Availability.UNKNOWN);
 		    }
 		}
 		
 		return item;
 	}
 	
 	/**
 	 * Lookup an item using the holdings id and item id
 	 * 
 	 * @param holdingId
 	 * @param bib_library
 	 * @param hold_library
 	 * @param getBibInformation
 	 * @param getHoldQueueLength
 	 * @param getCurrentBorrowers
 	 * @param getCurrentRequesters
 	 * @return
 	 * @throws AlephException
 	 */
 	public AlephItem lookupItemByHoldingsIdItemId(String holdingsId, String itemId, String agencyId, boolean getBibInformation, 
 			boolean getHoldQueueLength, boolean getCurrentBorrowers, boolean getCurrentRequesters) 
 	    throws IOException,ParserConfigurationException,SAXException,AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getHoldingsLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_HOLD_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		List<AlephItem> items = lookupItemsByHoldingsId(holdingsId, agencyId, getBibInformation, 
 				getHoldQueueLength,getCurrentBorrowers,getCurrentRequesters);
 		if (items!=null&&items.size()>0){
 			if (itemId==null) return items.get(0);
 			for (AlephItem item : items){
 				if (item.getItemId().equals(itemId)){
 					return item;
 				}
 			}
 			//if we get this far, just return the first item in the list
 			return items.get(0);
 		}
 		return AlephItemFactory.createAlephItem(agency);
 	}
 	
 	/**
 	 * Lookup items using the holdings id.  It really just retrieves the bib id for the holding id
 	 * and then call lookupItemsByBibId
 	 * 
 	 * @param holdingsId
 	 * @param agencyId
 	 * @param getBibInformation
 	 * @param getHoldQueueLength
 	 * @param getCurrentBorrowers
 	 * @param getCurrentRequesters
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public List<AlephItem> lookupItemsByHoldingsId(String holdingsId, String agencyId, boolean getBibInformation, 
 			boolean getHoldQueueLength, boolean getCurrentBorrowers, boolean getCurrentRequesters) 
 			throws IOException, ParserConfigurationException, SAXException, AlephException {
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency == null) {
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY + " "
 					+ agencyId);
 		} else if (agency.getBibLibrary() == null) {
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET
 					+ agencyId);
 		}
 
 		// get bibid
 		AlephItem item = AlephItemFactory.createAlephItem(agency);
 		item.setHoldingsId(holdingsId);
 		XService xService = XServiceFactory.createFindDocXService(agency
 				.getHoldingsLibrary(), holdingsId);
 		Document doc = xService.execute(getXServerName(), getXServerPort(),
 				false);
 		item = AlephItemFactory.updateAlephItemParseFindDocResponse(item, doc);
 
 		List<AlephItem> items = lookupItemsByBibId(item.getBibId(), agencyId, getBibInformation, 
 				getHoldQueueLength, getCurrentBorrowers, getCurrentRequesters);
 		List<AlephItem> keepItems = new ArrayList<AlephItem>();
 		for (AlephItem bibItem : items){
 			//call read item and only keep items with correct hold id
 			if (bibItem.getBarcode()!=null){
 				xService = XServiceFactory.createReadItemXService(agency.getAdmLibrary(), bibItem.getBarcode());
 				doc = xService.execute(getXServerName(), getXServerPort(),false);
 				XMLParserUtil.outputNode(doc);
 				AlephItem readItem = AlephItemFactory.getReadAlephItem(agency, doc);
 				if ((readItem.getHoldingsId()!=null&&readItem.getHoldingsId().endsWith(holdingsId))||(holdingsId!=null&&holdingsId.endsWith(readItem.getHoldingsId()))){
 					bibItem.updateFromAlephItem(readItem);
 					keepItems.add(bibItem);
 				}
 			} else if (bibItem.getDocNumber()!=null&&bibItem.getSeqNumber()!=null){
 				xService = XServiceFactory.createReadItemXService(agency.getAdmLibrary(), bibItem.getDocNumber(),item.getSeqNumber());
 				doc = xService.execute(getXServerName(), getXServerPort(),false);
 				AlephItem readItem = AlephItemFactory.getReadAlephItem(agency, doc);
 				if ((readItem.getHoldingsId()!=null&&readItem.getHoldingsId().endsWith(holdingsId))||(holdingsId!=null&&holdingsId.endsWith(readItem.getHoldingsId()))){
 					bibItem.updateFromAlephItem(readItem);
 					keepItems.add(bibItem);
 				}
 			}
 		}
 		return keepItems;
 	}
 	
 	/**
 	 * Cancel a requested item by hold id.  It will first lookup the item to the get bib id and item id
 	 * and then call the cancel by item id method.
 	 * 
 	 * @param adm_library
 	 * @param bib_library
 	 * @param hold_libary
 	 * @param patron_id
 	 * @param holdings_id
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public AlephItem cancelRequestItemByHoldingsId(String agencyId, String patron_id, String holdings_id) 
 		throws IOException,ParserConfigurationException,SAXException,AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getHoldingsLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_HOLD_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		List<AlephItem> items = this.lookupItemsByHoldingsId(holdings_id, agencyId, false, false, false, false);
 		if (items==null||items.size()<=0) throw new AlephException("Unable to find item to cancel");
 		AlephItem returnItem = null;
 		AlephException failEx = null;
 		boolean cancelled = false;
 		for (AlephItem item : items){
 			try {
 				returnItem = cancelRequestItemByItemId(agencyId, patron_id, item.getItemId());
 			} catch (AlephException ex){
 				failEx = ex;
 			}
 			cancelled = true;
 		}
 		if (!cancelled&&failEx!=null){
 			throw failEx;
 		} else if (cancelled&&returnItem!=null){
 			return returnItem;
 		} else {
 			throw new AlephException(AlephConstants.ERROR_FAILED_TO_FIND_REQUEST_FOR_CANCEL);
 		}
 	}
 	
 	/**
 	 * Cancel a requested item by bib id.  It will first lookup the item to the get item id
 	 * and then call the cancel by item id method.
 	 * 
 	 * @param adm_library
 	 * @param bib_library
 	 * @param patron_id
 	 * @param bib_id
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public AlephItem cancelRequestItemByBibId(String agencyId, String patron_id, String bib_id) 
 		throws IOException,ParserConfigurationException,SAXException,AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		List<AlephItem> items = this.lookupItemsByBibId(bib_id, agencyId, false, false, false, false);
 		if (items==null||items.size()<=0) throw new AlephException("Unable to find item to cancel");
 		AlephItem returnItem = null;
 		AlephException failEx = null;
 		boolean cancelled = false;
 		for (AlephItem item : items){
 			try {
 				returnItem = cancelRequestItemByItemId(agencyId, patron_id, item.getItemId());
 			} catch (AlephException ex){
 				failEx = ex;
 			}
 			cancelled = true;
 		}
 		if (!cancelled&&failEx!=null){
 			throw failEx;
 		} else if (cancelled&&returnItem!=null){
 			return returnItem;
 		} else {
 			throw new AlephException(AlephConstants.ERROR_FAILED_TO_FIND_REQUEST_FOR_CANCEL);
 		}
 	}
 	
 	/**
 	 * Cancel a requested item by item id
 	 * 
 	 * @param adm_library
 	 * @param patron_id
 	 * @param item_id
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public AlephItem cancelRequestItemByItemId(String agencyId, String patron_id, String item_id)
 		throws IOException,ParserConfigurationException,SAXException,AlephException{
 
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		AlephItem alephItem = null;
 		if (patron_id==null||item_id==null){
 			throw new AlephException("Patron id and item id must be set.");
 		}
 		//first get item hold ids for the item from item id....using lookupUser call
 		AlephUser user = this.lookupUser(agencyId, patron_id, null, false, true, false);
 		//then cancel all holds returned (maybe more than one)
 		boolean cancelled = false;
 		if (user!=null&&user.getRequestedItems()!=null&&user.getRequestedItems().size()>0){
 			for (AlephItem eachItem : user.getRequestedItems()){
 				if (eachItem!=null&&item_id.equals(eachItem.getItemId())&&eachItem.getHoldRequestId()!=null){
 					XService xService = XServiceFactory.createCancelHoldRequestXService(agency.getAdmLibrary(), eachItem.getItemId(), eachItem.getHoldRequestId());
 					Document doc = xService.execute(getXServerName(), getXServerPort(), false);
 					String error = XMLParserUtil.getError(doc);
 					if (error!=null){
 						throw new AlephException("Failed to cancel hold request for Item: "+item_id+" and user: "+patron_id+" reason: "+error);
 					}
 					String reply = XMLParserUtil.getReply(doc);
 					if (reply!=null&&reply.equals(AlephConstants.STATUS_OK)){
 						cancelled = true;
 						alephItem = eachItem;
 					}
 				}
 			}
 		}
 		if (!cancelled){
 			throw new AlephException(AlephConstants.ERROR_FAILED_TO_FIND_REQUEST_FOR_CANCEL);
 		}
 		return alephItem;
 	}
 	
 	/**
 	 * Request an item based on holdings id.  It will submit the request for the borrower id passed in.
 	 * The requestItemByItemId method should be used instead whenever possible because this method can be
 	 * inefficient.
 	 * 
 	 * @param request_user_bor_id
 	 * @param adm_library
 	 * @param bib_library
 	 * @param hold_library
 	 * @param holdingsId
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public Date requestItemByHoldingsId(String request_user_bor_id, String agencyId, String holdingsId) 
 		throws IOException, ParserConfigurationException, SAXException, AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getHoldingsLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_HOLD_LIBRARY_NOT_SET+agencyId);
 		}
 		//get item data to get barcode, just call lookupItemByItemId
 		AlephItem item = lookupItemByHoldingsIdItemId(holdingsId, null, agencyId, false, false, false, false);
 		return requestItem(request_user_bor_id, agencyId, item);
 	}
 	
 	/**
 	 * Request an item based on bib id.  It will submit the request for the borrower id passed in.
 	 * This method may be unreliable if there are items with duplicate bib id's.  Therefore, the requestItemByItemId
 	 * method should be used instead whenever possible.
 	 * 
 	 * @param request_user_bor_id
 	 * @param adm_library
 	 * @param bib_library
 	 * @param bibId
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public Date requestItemByBibId(String request_user_bor_id, String agencyId, String bibId) 
 		throws IOException, ParserConfigurationException, SAXException, AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		//get item data to get barcode, just call lookupItemByItemId
 		AlephItem item = lookupItemByBibId(bibId, agencyId, false, false, false, false);
 		return requestItem(request_user_bor_id, agencyId, item);
 	}
 	
 	/**
 	 * Request an item based on item id.  It will submit the request for the borrower id passed in.
 	 * 
 	 * @param request_user_bor_id
 	 * @param adm_library
 	 * @param bib_library
 	 * @param itemId
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public Date requestItemByItemId(String request_user_bor_id, String agencyId, String itemId) 
 		throws IOException, ParserConfigurationException, SAXException, AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		//get item data to get barcode, just call lookupItemByItemId
 		AlephItem item = lookupItemByItemId(itemId, agencyId, true, false, false, false, false);
 		return requestItem(request_user_bor_id, agencyId, item);
 	}
 	
 	/**
 	 * Request the item for the given borrower.
 	 * Checks to see if a request has already been made, if so it will not re-request but will throw an error.
 	 * 
 	 * @param request_user_bor_id
 	 * @param adm_library
 	 * @param item
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	private Date requestItem(String request_user_bor_id, String agencyId, AlephItem item) throws IOException, ParserConfigurationException, SAXException, AlephException{
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		//check if user can request item
 		if (item!=null&&item.getCirculationStatus()!=null&&item.getCirculationStatus().equals(AlephConstants.CIRC_STATUS_CHECKED_OUT)){
 			throw new AlephException("Item is checked out.  Please recall item instead.");
 		}
 		AlephUser user = this.lookupUser(agencyId, request_user_bor_id, null, false, true, false);
 		Date available = null;
 		if (user==null){
 			throw new AlephException("Requesting user not found in the system");
 		} else if (user.hasRequestedItem(item)){
 			throw new AlephException("Item is already requested");
 		} else if (item!=null&&item.getBarcode()!=null){
 			XService xService = XServiceFactory.createHoldRequestXService(agency.getAdmLibrary(), item.getBarcode(), request_user_bor_id);
 			Document doc = xService.execute(getXServerName(), getXServerPort(), false);
 			String error = XMLParserUtil.getError(doc);
 			if (error!=null){
 				if (item.getItemId()!=null){
 					throw new AlephException("Failed to request Item ID: "+item.getItemId()+" and user: "+request_user_bor_id+" reason: "+error);
 				} else if (item.getBibId()!=null){
 					throw new AlephException("Failed to request Item Bib ID: "+item.getBibId()+" and user: "+request_user_bor_id+" reason: "+error);
 				} else {
 					throw new AlephException("Failed to request Item ID: null and user: "+request_user_bor_id+" reason: "+error);
 				}
 			}
 			String reply = XMLParserUtil.getReply(doc);
 			if (reply!=null&&reply.equals(AlephConstants.STATUS_OK)){
 				//try to get requested item
 				List<AlephItem> reqItems = AlephItemFactory.getHoldAlephItems(agency,doc);
 				if (reqItems!=null&&reqItems.size()>0){
 					AlephItem alephItem = reqItems.get(0);
 					available = alephItem.getDateAvailablePickup();
 				}
 			}
 		} else {
 			throw new AlephException("Failed to request Item that is null or has no barcode set");
 		}
 		return available;
 	}
 	
 	/**
 	 * Renew an item based on hold id.  It will submit the renewal for the borrower id passed in.
 	 * This method may be unreliable if there are items with duplicate bib id's.  Therefore, the renewItemByItemId
 	 * method should be used instead whenever possible.
 	 * 
 	 * @param request_user_bor_id
 	 * @param adm_library
 	 * @param bib_library
 	 * @param hold_library
 	 * @param holdingsId
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public Date renewItemByHoldingsId(String request_user_bor_id, String agencyId, String holdingsId) 
 		throws IOException, ParserConfigurationException, SAXException, AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getHoldingsLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_HOLD_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		//get item by hold id
 		AlephItem item = lookupItemByHoldingsIdItemId(holdingsId, null, agencyId, false, false, false, false);
 		return renewItem(request_user_bor_id, agency.getAdmLibrary(), item);
 	}
 	
 	/**
 	 * Renew an item based on bib id.  It will submit the renewal for the borrower id passed in.
 	 * This method may be unreliable if there are items with duplicate bib id's.  Therefore, the renewItemByItemId
 	 * method should be used instead whenever possible.
 	 * 
 	 * @param request_user_bor_id
 	 * @param adm_library
 	 * @param bib_library
 	 * @param bibId
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public Date renewItemByBibId(String request_user_bor_id, String agencyId, String bibId) 
 		throws IOException, ParserConfigurationException, SAXException, AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		//get item data to get barcode, just call lookupItemByItemId
 		AlephItem item = lookupItemByBibId(bibId, agencyId, false, false, false, false);
 		return renewItem(request_user_bor_id, agency.getAdmLibrary(), item);
 	}
 	
 	/**
 	 * Renew an item based on item id.  It will submit the renewal for the borrower id passed in.
 	 * 
 	 * @param request_user_bor_id
 	 * @param adm_library
 	 * @param bib_library
 	 * @param itemId
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public Date renewItemByItemId(String request_user_bor_id, String agencyId, String itemId) 
 		throws IOException, ParserConfigurationException, SAXException, AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		//get item data to get barcode, just call lookupItemByItemId
 		AlephItem item = lookupItemByItemId(itemId, agencyId, true, false, false, false, false);
 		return renewItem(request_user_bor_id, agency.getAdmLibrary(), item);
 	}
 	
 	/**
 	 * Renew the item for the given borrower.
 	 * Checks to see if can renew the item, if not it will throw an error.
 	 * 
 	 * @param request_user_bor_id
 	 * @param adm_library
 	 * @param item
 	 * @return
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	private Date renewItem(String request_user_bor_id, String adm_library, AlephItem item) 
 		throws IOException, ParserConfigurationException, SAXException, AlephException{
 		if (item!=null&&item.getCirculationStatus()!=null&&!item.getCirculationStatus().equals(AlephConstants.CIRC_STATUS_CHECKED_OUT)){
 			throw new AlephException(AlephConstants.ERROR_ITEM_NOT_CHECKED_OUT);
 		}
 		if (item!=null&&item.getBarcode()!=null){
 			XService xService = XServiceFactory.createRenewXService(adm_library, item.getBarcode(), request_user_bor_id);
 			Document doc = xService.execute(getXServerName(), getXServerPort(), false);
 			String error = XMLParserUtil.getError(doc);
 			if (error!=null){
 				if (item.getItemId()!=null){
 					throw new AlephException("Failed to renew Item ID: "+item.getItemId()+" and user: "+request_user_bor_id+" reason: "+error);
 				} else if (item.getBibId()!=null){
 					throw new AlephException("Failed to renew Item Bib ID: "+item.getBibId()+" and user: "+request_user_bor_id+" reason: "+error);
 				} else if (item.getHoldingsId()!=null){
 					throw new AlephException("Failed to renew Item Holdings ID: "+item.getHoldingsId()+" and user: "+request_user_bor_id+" reason: "+error);
 				} else {
 					throw new AlephException("Failed to renew Item ID: null and user: "+request_user_bor_id+" reason: "+error);
 				}
 			}
 			String reply = XMLParserUtil.getReply(doc);
 			if (reply!=null&&reply.equals(AlephConstants.STATUS_OK)){
 				//try to get due date
 				try {
 					item.setDueDateRenewal(XMLParserUtil.getDueDate(doc));
 				} catch (ParseException pe){
 					throw new AlephException("Renewal status unknown. Unrecognized due date format returned.");
 				}
 			}
 		} else {
 			throw new AlephException("Failed to request Item that is null or has no barcode set");
 		}
 		
 		if (item==null) return null;		
 		return item.getDueDate();
 	}
 	
 	public List<AlephItem> getAvailabilityByHoldingsId(String agencyId, List<String> holdingsIds) 
 		throws IOException,
 			ParserConfigurationException, SAXException, AlephException {
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency == null) {
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY + " "
 					+ agencyId);
 		} else if (agency.getAdmLibrary() == null) {
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET
 					+ agencyId);
 		} else if (agency.getBibLibrary() == null) {
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET
 					+ agencyId);
 		}
 
 		List<AlephItem> checkItems = new ArrayList<AlephItem>();
 		List<AlephItem> availability = new ArrayList<AlephItem>();
 		Map<String, AlephItem> barcodeStatusMap = new HashMap<String, AlephItem>();
 
 		for (String holdingsId : holdingsIds) {
 			boolean threwException = false;
 		    
 		    List<AlephItem> items = null;
 			// just need to get the bib id for a holdings id
 			try {
 				items = this.lookupItemsByHoldingsId(holdingsId, agencyId, true, false, false, false);
 			} catch (Exception ex) {
 				// if this is true than bib id will get set to does not exist
 				threwException = true;
 			}
 			if (holdingsId!=null){
 				if (holdingsId.length()<=0 || items==null || items.size()<=0 || threwException){
 					AlephItem item = AlephItemFactory.createAlephItem(agency);
 					item.setHoldingsId(holdingsId);
 					item.setAvailability(AlephConstants.Availability.DOESNT_EXIST);
 					availability.add(item);
 				} else if (items.get(0)==null||items.get(0).getBibId()==null){
 					AlephItem item = AlephItemFactory.createAlephItem(agency);
 					item.setAvailability(AlephConstants.Availability.UNKNOWN);
 				} else {
 					checkItems.addAll(items);
 					barcodeStatusMap.putAll(getCircStatusByBibId(agency, items.get(0).getBibId()));
 				}
 			}
 		}
 
 		// now match barcode to items returned and transfer circ status and
 		// availability found
 		for (AlephItem checkItem : checkItems) {
 			boolean found = false;
 			if (checkItem.getBarcode() != null) {
 				AlephItem foundItem = barcodeStatusMap.get(checkItem
 						.getBarcode());
 				if (foundItem != null) {
 					checkItem.updateFromAlephItem(foundItem);
 					found = true;
 				}
 			}
 			if (!found) {
 				checkItem.setAvailability(AlephConstants.Availability.UNKNOWN);
 			}
 			availability.add(checkItem);
 		}
 		return availability;
 	}
 	
 	public List<AlephItem> getAvailabilityByBibId(String agencyId, List<String> bibIds) 
 		throws IOException, ParserConfigurationException, SAXException, AlephException{
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		List<AlephItem> checkItems = new ArrayList<AlephItem>();
 		List<AlephItem> availability = new ArrayList<AlephItem>();
 		Map<String, AlephItem> barcodeStatusMap = new HashMap<String, AlephItem>();
 		
 		for (String bibId : bibIds){
 			boolean threwException = false;
 			List<AlephItem> items = null;
 			//this should return items with barcode, item id, and bib id defined
 			try {
 				items = this.lookupItemsByBibId(bibId, agencyId, true, false, false, false);
 			} catch (Exception ex){
 				//if this is true than bib id will get set to does not exist
 				threwException = true;
 			}
 			if (bibId!=null && (bibId.length()<=0 || items==null || items.size()<=0 || threwException)){
 				AlephItem item = AlephItemFactory.createAlephItem(agency);
 				item.setBibId(bibId);
 				item
 						.setAvailability(AlephConstants.Availability.DOESNT_EXIST);
 				availability.add(item);
 			} else if (bibId!=null){
 				checkItems.addAll(items);
 				barcodeStatusMap.putAll(getCircStatusByBibId(agency, bibId));
 			}
 		}
 		
 		// now match barcode to items returned and transfer circ status and
 		// availability found
 		for (AlephItem checkItem : checkItems) {
 			boolean found = false;
 			if (checkItem.getBarcode() != null) {
 				AlephItem foundItem = barcodeStatusMap.get(checkItem
 						.getBarcode());
 				if (foundItem != null) {
 					checkItem.updateFromAlephItem(foundItem);
 					found = true;
 				}
 			}
 			if (!found) {
 				checkItem
 						.setAvailability(AlephConstants.Availability.UNKNOWN);
 			}
 			availability.add(checkItem);
 		}
 		return availability;
 	}
 	
 	public List<AlephItem> getAvailabilityByItemId(String agencyId, List<String> itemIds) 
 	    throws IOException, ParserConfigurationException, SAXException, AlephException{
 		
 		AlephAgency agency = getAlephAgency(agencyId);
 		if (agency==null){
 			throw new AlephException(AlephConstants.ERROR_UNKNOWN_AGENCY+" "+agencyId);
 		} else if (agency.getAdmLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_ADM_LIBRARY_NOT_SET+agencyId);
 		} else if (agency.getBibLibrary()==null){
 			throw new AlephException(AlephConstants.ERROR_BIB_LIBRARY_NOT_SET+agencyId);
 		}
 		
 		List<AlephItem> availability = new ArrayList<AlephItem>();
 		List<AlephItem> checkItems = new ArrayList<AlephItem>();
 		List<String> checkBibs = new ArrayList<String>();
 		Map <String,AlephItem> barcodeStatusMap = new HashMap<String,AlephItem>();
 		
 		if (itemIds!=null&&itemIds.size()>0){
 			for (String itemId : itemIds){
 				AlephItem alephItem = null;
 				boolean threwException = false;
 				try {
 					//get bib id, item id
 					alephItem = this.lookupItemByItemId(itemId, agencyId, false, false, false, false, false);
 				} catch (Exception ex){
 					//set to unknown if there is an exception on lookup
 					//probably unnecessary since alephItem will probably be null if an exception is thrown, but we will use anyway
 					threwException = true;
 				}
 				if (alephItem==null||alephItem.getItemId()==null||alephItem.getBibId()==null||threwException){
 					AlephItem item = AlephItemFactory.createAlephItem(agency);
 					item.setItemId(itemId);
 					item.setAvailability(AlephConstants.Availability.DOESNT_EXIST);
 					availability.add(item);
 				} else {
 					if (!checkBibs.contains(alephItem.getBibId())){
 						checkBibs.add(alephItem.getBibId());
 					}
 					//get barcode, itemid
 					AlephItem alephItem2 = lookupItemByBibIdItemId(alephItem.getBibId(),itemId,agency.getAgencyId(),false,false,false,false);
 					if (alephItem2!=null){
 						//now barcode, bib id, and item id should all be set
 						alephItem.updateFromAlephItem(alephItem2);
 						checkItems.add(alephItem);
 					}
 				}
 			}
 			
 			//populate barcode status map by calling circ status for each bib id
 			for (String bibId : checkBibs){
 				barcodeStatusMap.putAll(getCircStatusByBibId(agency,bibId));
 			}
 			//now match barcode to items returned and transfer circ status and availability found
 			for (AlephItem checkItem : checkItems){
 				boolean found = false;
 				if (checkItem.getBarcode()!=null){
 					AlephItem foundItem = barcodeStatusMap.get(checkItem.getBarcode());
 					if (foundItem!=null){
 						checkItem.updateFromAlephItem(foundItem);
 						found = true;
 					}
 				}
 				if (!found){
 					checkItem.setAvailability(AlephConstants.Availability.UNKNOWN);
 				}
 				availability.add(checkItem);
 			}
 		}
 		return availability;
 	}
 	
 	/**
 	 * Return a Map of barcode to AlephItem with availability and circ status set
 	 * for the bib id passed in.  A map is used so can return 
 	 * status for all items at once that have same bib requested
 	 *   
 	 * @param agency
 	 * @param bibId
 	 * @return
 	 * @throws AlephException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 */
 	private Map<String,AlephItem> getCircStatusByBibId(AlephAgency agency, String bibId) 
 	    throws AlephException, IOException, ParserConfigurationException, SAXException{
         XService xService = XServiceFactory.createCircStatusXService(agency.getBibLibrary(), bibId);
 		
 		Document doc = xService.execute(getXServerName(), getXServerPort(), false);
 		XMLParserUtil.outputNode(doc);
 		List<AlephItem> foundItems = AlephItemFactory.getAlephItemsCircStatus(agency, doc);
 		Map<String,AlephItem> barcodeStatusMap = new HashMap<String,AlephItem>();
 		for (AlephItem item : foundItems){
 			if (item.getBarcode()!=null){
 				item.setAvailability(this.getAlephItemAvailability(item));
 				barcodeStatusMap.put(item.getBarcode(), item);
 			}
 		}
 		
 		return barcodeStatusMap;
 	}
 	
 	/**
 	 * @param defaultAgencyId the defaultAgencyId to set
 	 */
 	public void setDefaultAgencyId(String defaultAgencyId) {
 		this.defaultAgencyId = defaultAgencyId;
 	}
 
 	/**
 	 * @return the defaultAgencyId
 	 */
 	public String getDefaultAgencyId() {
 		if (defaultAgencyId!=null){
 			return defaultAgencyId;
 		} else if (defaultAgencyId==null&&getAgencies().size()>0){
 			return getAgencies().keySet().iterator().next();
 		} else {
 			return null;
 		}
 	}
 	
 	public void addAvailableCircStatus(String status) {
 		if (availabilityCircStatusMap==null){
 			availabilityCircStatusMap = new HashMap<String,AlephConstants.Availability>();
 		}
 		//add as lowercase so all checks can be case-insensitive
 		if (status!=null){
 			availabilityCircStatusMap.put(status.toLowerCase(), AlephConstants.Availability.AVAILABLE);
 		}
 	}
 	
 	public void addPossiblyAvailableCircStatus(String status) {
 		if (availabilityCircStatusMap==null){
 			availabilityCircStatusMap = new HashMap<String,AlephConstants.Availability>();
 		}
 		//add as lowercase so all checks can be case-insensitive
 		if (status!=null){
 			availabilityCircStatusMap.put(status.toLowerCase(), AlephConstants.Availability.POSSIBLY_AVAILABLE);
 		}
 	}
 	
 	public void addNotAvailableCircStatus(String status) {
 		if (availabilityCircStatusMap==null){
 			availabilityCircStatusMap = new HashMap<String,AlephConstants.Availability>();
 		}
 		//add as lowercase so all checks can be case-insensitive
 		if (status!=null){
 			availabilityCircStatusMap.put(status.toLowerCase(), AlephConstants.Availability.NOT_AVAILABLE);
 		}
 	}
 
 	/**
 	 * @param availabilityCircStatusMap the availabilityCircStatusMap to set
 	 */
 	public void setAvailabilityCircStatusMap(
 			Map<String,AlephConstants.Availability> availabilityCircStatusMap) {
 		this.availabilityCircStatusMap = availabilityCircStatusMap;
 	}
 
 	/**
 	 * @return the availabilityCircStatusMap
 	 */
 	public Map<String,AlephConstants.Availability> getAvailabilityCircStatusMap() {
 		return availabilityCircStatusMap;
 	}
 	
 	public AlephConstants.Availability getAlephItemAvailability(AlephItem item){
 		if (availabilityCircStatusMap==null){
 			availabilityCircStatusMap = new HashMap<String,AlephConstants.Availability>();
 		}
 		AlephConstants.Availability available = AlephConstants.Availability.UNKNOWN;
 		if (item!=null){
 			if (item.getDueDate()!=null){
 				available = AlephConstants.Availability.NOT_AVAILABLE;
 			} else if (item.getCirculationStatus()!=null){
 				available = this.availabilityCircStatusMap.get(item.getCirculationStatus().toLowerCase());
 				if (available==null){
 					//default to possibly available if a status is set but unrecognized
 					available = AlephConstants.Availability.POSSIBLY_AVAILABLE;
 				}
 			} else {
 				available = AlephConstants.Availability.AVAILABLE;
 			}
 		}
 		return available;
 	}
 
 	public AlephUser updateUser(String patronId, String password, UpdateUserInitiationData initData) throws AlephException, IOException, SAXException, ParserConfigurationException {
 		
 		AlephUser user = authenticateUser(patronId, password);

 		return user;
 	}
 }
