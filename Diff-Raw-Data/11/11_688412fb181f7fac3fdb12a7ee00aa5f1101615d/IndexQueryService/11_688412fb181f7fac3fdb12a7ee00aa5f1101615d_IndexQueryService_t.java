 package org.nchelp.meteor.provider.access;
 
 import java.math.BigDecimal;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorDataResponse;
 import org.nchelp.meteor.message.MeteorIndexResponse;
 import org.nchelp.meteor.message.response.Award;
import org.nchelp.meteor.message.response.MeteorDataProviderAwardDetails;
 import org.nchelp.meteor.message.response.MeteorDataProviderInfo;
 import org.nchelp.meteor.message.response.MeteorDataProviderMsg;
 import org.nchelp.meteor.message.response.MeteorRsMsg;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.IndexProvider;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.registry.DistributedRegistry;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Cache;
 import org.nchelp.meteor.util.ProviderError;
 import org.nchelp.meteor.util.exception.DataException;
 import org.nchelp.meteor.util.exception.IndexException;
 import org.nchelp.meteor.util.exception.ParameterException;
 
 public class IndexQueryService {
 
 	private final Logger log = Logger.create(this.getClass());
 
 //	private static Cache meteorDataCache = new Cache();
 	private static Cache iProviderCache = new Cache();
 	private static Cache dProviderCache = new Cache();
 /*	static {
 		AggregatedLoanData.setXMLEngines("duplicates.xml", "bestsource.xml");
 	}
 	
 	private AggregatedLoanData aggregatedData = new AggregatedLoanData();
 */
 	private ProviderError errors = new ProviderError();
 //	private Borrower borrower = new Borrower();
 
 	/**
 	 * Main method for Meteor queries.  This will return an XML document
 	 * that represents the summary information
 	 * @param ssn Social Security Number
 	 * @param dob Date Of Birth for this SSN
 	 * @return String
 	 */
 	public String query(MeteorParameters params){
 		// check dataProviderCache
 		// if no providers in cache should 
 		// look for index providers in index provider cache
 		// if not found should look it up and put it in cache 
 
 		List dProviders = null;
 		List iProviders = null;
 		MeteorDataResponse mdr = null;
 		
 		int awardID = 1;
 		
 /*		// Check to see if this is a subsequent request for data that we've 
 		// already looked up.  If the ForceRefresh is true, then no matter
 		// what we won't use cached data.
 		String cachedData = (String)meteorDataCache.cached(params.getSsn());
 		
 		if(cachedData != null){
 			if(params.getForceRefresh()){
 				log.debug("Data is cached but ForceRefresh is true so clearing cache and requerying");
 				// Clear the cache if it is there
 				meteorDataCache.remove(params.getSsn())	;
 			} else {
 				log.debug("Data is cached.");
 				return cachedData;
 			}
 			
 		}
 */
 		DistributedRegistry registry = DistributedRegistry.singleton();
 		SecurityToken token = registry.getAuthentication();
 		try{
 			token.setRole(params.getRole());
 		} catch(ParameterException e){
 			log.error("Invalid Role Defined", e);	
 		}
 
 		dProviders = (List)dProviderCache.cached(params.getSsn());
 
 		if (dProviders == null || dProviders.isEmpty()) {
 
 			iProviders = (List)iProviderCache.cached("");
 
 			if (iProviders == null || iProviders.isEmpty()) {
 
 				iProviders = registry.getIndexProviders();
 				iProviderCache.add("", iProviders);
 			}
 
 			// now request a list of data providers from each index provider.
 			Iterator iterator = iProviders.iterator();
 			IndexProvider iProvider = null;
 
 			while (iterator.hasNext()) {
 				
 				iProvider = (IndexProvider) iterator.next();
 				MeteorIndexResponse ipResp = null;
 				try{
 					log.assert(iProvider != null, "Ack! the IndexProvider object is null");
 					ipResp = iProvider.getDataProviders(token, params);
 				} catch(IndexException e){
 					//TODO!!! THIS IS A NASTY HACK!!!
 					errors.setError((MeteorIndexResponse)null, e);
 				}
 				if(ipResp != null){
 					dProviders = this.aggregateList(dProviders, ipResp.getDataProviderList());
 				}
 			}
 		}
 		
 		// Now interate through each of these and make the real call
 		Iterator iterator = dProviders.iterator();
 		DataProvider dProvider = null;
 		
 		
 		while(iterator.hasNext()){
 			dProvider = (DataProvider) iterator.next();
 			
 			MeteorDataResponse respTemp = null;
 			
 			try{
 				respTemp = dProvider.getData(token, params);
 				
 				/* Loop through each Award type and set an ID
 				 */
				MeteorRsMsg msg = respTemp.getRsMsg();
				
 				int count = respTemp.getRsMsg().getMeteorDataProviderInfoCount();
 				for(int i = 0; i < count; i++){
					MeteorDataProviderInfo mdpi = msg.getMeteorDataProviderInfo(i);
					MeteorDataProviderAwardDetails mdpad = mdpi.getMeteorDataProviderAwardDetails();
					if(mdpad == null) continue;
					
					Award[] awards = mdpad.getAward();
 					
 					for(int j=0; j < awards.length; j++){
 						awards[j].setAPSUniqueAwardID(BigDecimal.valueOf(awardID));
 						awardID++;
 					}
 				}
 			} catch(DataException e){
 				log.error(e);
 				errors.setError(mdr, e);
 			}
 			
 			
 			if(mdr == null){
 				mdr = respTemp;
 			} else {
 				MeteorRsMsg msgTemp = respTemp.getRsMsg();
 				MeteorRsMsg msgFinal = mdr.getRsMsg();
 				
 				for(int i=0; i < msgTemp.getMeteorDataProviderInfoCount(); i++){
 					msgFinal.addMeteorDataProviderInfo(msgTemp.getMeteorDataProviderInfo(i));
 				}
 			}
 			
 /*			// Now add each of the loans to the aggregatedData object
 			Document doc = null;
 			MeteorRsMsg msg = respTemp.getRsMsg();
 			
 			try{
 				Marshaller.marshal(msg, doc);
 			} catch(MarshalException e){
 				// Do something here
 			} catch(ValidationException e){
 				// Do something here too!
 			}
 */			
 		}
 			
 		
 		String xml = null;
 		if(mdr == null){
 			mdr = new MeteorDataResponse();
 			MeteorRsMsg msg = mdr.getRsMsg();
 			MeteorDataProviderInfo mdpi = msg.getMeteorDataProviderInfo(0);
 			MeteorDataProviderMsg mde = new MeteorDataProviderMsg();
 			mde.setRsMsg("No Data Found");
 			mde.setRsMsgLevel("I");
 			mdpi.addMeteorDataProviderMsg(mde);
 		} 
 		
 		
 		xml = mdr.toString();
 		
 		
 		
 		// Massive hack here!!!
 		// According to the XSLT 1.0 spec, a document
 		// cannot have a default namespace other than
 		// the one normally defined for xslt.
 		// So, until version 2.0 of the XSLT spec is 
 		// published and incorporated into Xalan,
 		// We have to strip out the default namespace
 		xml = this.removeDefaultNamespace(xml);
 
 /*		// Save it off for subsequent queries		
 		meteorDataCache.add(params.getSsn(), xml);
 */		
 		return xml;
 	}
 	
 	/**
 	 * View loan detail information
 	 * @param loanID 
 	 * @return String
 	 */
 	public String viewDetail(String loanID){
 		return ""; // aggregatedData.toXML(loanID);
 	}
 	
 	/**
 	 * As each of the calls to AccessProvider.getDataProviders() returns
 	 * call this method to eliminate any of the duplicate Data
 	 * Providers
 	 * @param dataProviders
 	 * @return List
 	 */
 	private List aggregateList(List dataProviders, List newDataProviders){
 		// easiest way to do this is to cast this to a Set and add them then turn it back into a List
 
 		if(newDataProviders == null){
 			return dataProviders;
 		}
 		
 		if(dataProviders == null){
 			dataProviders = new Vector();
 		}
 		dataProviders.addAll(newDataProviders);
 		return dataProviders;
 	}
 	
 	/**
 	 * Massive hack here!!!
 	 * According to the XSLT 1.0 spec, a document
 	 * cannot have a default namespace other than
 	 * the one normally defined for xslt.
 	 * So, until version 2.0 of the XSLT spec is 
 	 * published and incorporated into Xalan,
 	 * We have to strip out the default namespace
 	 * @param xml
 	 * @return String
 	 */
 	private String removeDefaultNamespace(String xml){
 		if(xml == null) return xml;
 		
 		String nameSpace = " xmlns=\"http://schemas.pescxml.org\"";
 		
 		int pos = xml.indexOf(nameSpace);
 		if(pos < 0) return xml;
 		
 		xml = xml.substring(0, pos) + 
 		      xml.substring(pos + nameSpace.length());
 		return xml;
 	}
 
 }
