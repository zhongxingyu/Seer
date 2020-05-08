 /*
  * Copyright 2007 EDL FOUNDATION
  *
  * Licensed under the EUPL, Version 1.1 or - as soon they
  * will be approved by the European Commission - subsequent
  * versions of the EUPL (the "Licence");
  * you may not use this work except in compliance with the
  * Licence.
  * You may obtain a copy of the Licence at:
  *
  * http://ec.europa.eu/idabc/eupl
  *
  * Unless required by applicable law or agreed to in
  * writing, software distributed under the Licence is
  * distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied.
  * See the Licence for the specific language governing
  * permissions and limitations under the Licence.
  */
 
 package eu.europeana.uim.sugarcrmclient.plugin;
 
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.DataSet;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.sugarcrmclient.internal.helpers.ClientUtils;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntryList;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntryListResponse;
 
 import eu.europeana.uim.sugarcrmclient.jibxbindings.ContactByEmail;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.Login;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.NameValue;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.NameValueList;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.NoteAttachment;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.SelectFields;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.SetEntry;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.SetEntryResponse;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.SetNoteAttachment;
 import eu.europeana.uim.sugarcrmclient.jibxbindings.SetNoteAttachmentResponse;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.ConnectionStatus;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.SugarCrmRecord;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.data.DatasetStates;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.data.RetrievableField;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.data.UpdatableField;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.listeners.PollingListener;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.queries.SimpleSugarCrmQuery;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.queries.SugarCrmQuery;
 import eu.europeana.uim.sugarcrmclient.ws.SugarWsClient;
 import eu.europeana.uim.sugarcrmclient.ws.exceptions.FileAttachmentException;
 import eu.europeana.uim.sugarcrmclient.ws.exceptions.GenericSugarCRMException;
 import eu.europeana.uim.sugarcrmclient.ws.exceptions.LoginFailureException;
 import eu.europeana.uim.sugarcrmclient.ws.exceptions.QueryResultException;
 import eu.europeana.uim.workflow.Workflow;
 import eu.europeana.uim.api.ActiveExecution;
 import eu.europeana.uim.api.Orchestrator;
 import eu.europeana.uim.api.Registry;
 import eu.europeana.uim.api.StorageEngine;
 import eu.europeana.uim.api.StorageEngineException;
 
 
 
 /**
  * This is the implementing class for the OSGI based SugarCrm plugin OSGI service.
  * 
  * @author Georgios Markakis
  */
 public class SugarCRMServiceImpl implements SugarCRMService{
 
 	private SugarWsClient sugarwsClient;
 	private Orchestrator orchestrator;
 	private Registry registry;
 	private List<PollingListener> pollingListeners;
 	
 	private static final String DSMODULENAME = "Opportunities";
 	
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#showConnectionStatus()
 	 */
 	@Override
 	public ConnectionStatus showConnectionStatus() {
 		
 		ConnectionStatus cstatus = new ConnectionStatus();
 		
 		cstatus.setDefaultURI(sugarwsClient.getDefaultUri());
 		cstatus.setSessionID(sugarwsClient.getSessionID());
 
 		return cstatus;
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#updateSession()
 	 */
 	@Override
 	public String updateSession(String username, String password) throws LoginFailureException {
 		StringBuffer connectionInfo = new StringBuffer();
 		Login login = ClientUtils.createStandardLoginObject(username, password);
 		String newsessionID = sugarwsClient.login(login);
 		sugarwsClient.setSessionID(newsessionID);
         connectionInfo.append(sugarwsClient.login(login));
 		return connectionInfo.toString();
 	}
  
 	
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#changeEntryStatus(java.lang.String, eu.europeana.uim.sugarcrmclient.plugin.objects.data.DatasetStates)
 	 */
 	@Override
 	public void changeEntryStatus(String recordId, DatasetStates state)
 			throws QueryResultException {
 		alterSugarCRMItemStatus(recordId, state.getSysId());
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#updateRecordData(java.lang.String, java.util.HashMap)
 	 */
 	@Override
 	public void updateRecordData(String recordID,
 			HashMap<UpdatableField, String> values) throws QueryResultException {
		
 		SetEntry request = new SetEntry();
 		ArrayList <NameValue> nvList = new  ArrayList <NameValue>();
 		Iterator<?> it = values.entrySet().iterator();
 
		//First add the id name\value pair
		NameValue nvid = new NameValue();
		nvid.setName("id");
		nvid.setValue(recordID);
		nvList.add(nvid);
		
		
 	    while (it.hasNext()) {
 	        Map.Entry<UpdatableField, String> pairs = (Map.Entry<UpdatableField, String>)it.next();
 			NameValue nv = new NameValue();
 			nv.setName(pairs.getKey().getFieldId());
 			nv.setValue(pairs.getValue());
 
 			nvList.add(nv);
 	    }
 	    
 		NameValueList valueList = ClientUtils.generatePopulatedNameValueList(nvList);
 		
 		request.setNameValueList(valueList);
 		request.setModuleName("Opportunities");
 		request.setSession(sugarwsClient.getSessionID());	
 		
 		sugarwsClient.set_entry(request);		
 	}
 
 	
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#updateRecordData(eu.europeana.uim.sugarcrmclient.plugin.objects.SugarCrmRecord)
 	 */
 	@Override
 	public void updateRecordData(SugarCrmRecord record) throws QueryResultException {
 		HashMap<UpdatableField, String> values = new HashMap<UpdatableField, String>();
 		
 		String recordId = record.getItemValue(RetrievableField.ID);
 
 		values.put(UpdatableField.AMOUNT, record.getItemValue(UpdatableField.AMOUNT));
 		values.put(UpdatableField.INGESTED_IMAGE, record.getItemValue(UpdatableField.INGESTED_IMAGE));
 		values.put(UpdatableField.INGESTED_SOUND, record.getItemValue(UpdatableField.INGESTED_SOUND));
 		values.put(UpdatableField.INGESTED_TEXT, record.getItemValue(UpdatableField.INGESTED_TEXT));
 		values.put(UpdatableField.INGESTED_VIDEO, record.getItemValue(UpdatableField.INGESTED_VIDEO));
 		values.put(UpdatableField.NEXT_STEP, record.getItemValue(UpdatableField.NEXT_STEP));
 		values.put(UpdatableField.TOTAL_INGESTED, record.getItemValue(UpdatableField.TOTAL_INGESTED));
 
 		updateRecordData(recordId,values);
 	}
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#retrieveRecord(java.lang.String)
 	 */
 	@Override
 	public SugarCrmRecord retrieveRecord(String id) throws QueryResultException {
 		GetEntryList request = new GetEntryList();
 		
 		SelectFields fields = new SelectFields(); //We want to retrieve all fields
 		request.setSelectFields(fields); 
   		request.setModuleName(DSMODULENAME);	
 		request.setSession(sugarwsClient.getSessionID());
 		request.setOrderBy(RetrievableField.DATE_ENTERED.getFieldId());
 		request.setMaxResults(1);
 		request.setOffset(0);
 		
 		request.setQuery("(opportunities.id LIKE '" + id +"')");
 
 		GetEntryListResponse response =  sugarwsClient.get_entry_list(request);
 
 		ArrayList<Element> list = (ArrayList<Element>) response.getReturn().getEntryList().getArray().getAnyList();
 
 		Element record = list.get(0);
 			
 		if (record != null){
 			
 			return SugarCrmRecord.getInstance(record);
 		}
 		else
 		{
 			return null;
 		}
 
 	}
 	
 	
 
 
 	
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#retrieveRecords(eu.europeana.uim.sugarcrmclient.plugin.objects.queries.SugarCrmQuery)
 	 */
 	@Override
 	public List<SugarCrmRecord> retrieveRecords(SugarCrmQuery query)
 			throws QueryResultException {
 		
 		GetEntryList request = new GetEntryList();
 		
 		SelectFields fields = new SelectFields(); //We want to retrieve all fields
 		request.setSelectFields(fields); 
   		request.setModuleName(DSMODULENAME);	
 		request.setSession(sugarwsClient.getSessionID());
 		request.setOrderBy(query.getOrderBy().getFieldId());
 		request.setMaxResults(query.getMaxResults());
 		request.setOffset(query.getOffset());
 		
 		request.setQuery(query.toString());
 
 		GetEntryListResponse response =  sugarwsClient.get_entry_list(request);
 
 		ArrayList<Element> list = null;
 		
 		if(response.getReturn().getEntryList().getArray() != null)
 		{
 		  list = (ArrayList<Element>) response.getReturn().getEntryList().getArray().getAnyList();
 		}
 		else{
 			list = new ArrayList<Element>();
 		}
 		ArrayList<SugarCrmRecord> returnList = wrapDomElements2Objects(list);
 
 		return returnList;
 	}
 	
 	
	
	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#initWorkflowFromRecord(java.lang.String, eu.europeana.uim.sugarcrmclient.plugin.objects.SugarCrmRecord, eu.europeana.uim.sugarcrmclient.plugin.objects.data.DatasetStates)
 	 */
 	@Override
 	public Workflow initWorkflowFromRecord(String worklfowName,SugarCrmRecord record,DatasetStates endstate) throws QueryResultException,StorageEngineException {
 
 			StorageEngine<?> engine = registry.getStorage();
 			Workflow w = registry.getWorkflow(worklfowName);
 			
 			Collection<?> dataset = inferCollection(engine,record);
 			
 			ActiveExecution<?> execution = orchestrator.executeWorkflow(w, dataset);
 			alterSugarCRMItemStatus(dataset.getMnemonic(), endstate.getSysId());	
 		
 			execution.waitUntilFinished();		
 			
 			return w;
 	}
 	
 	
 	
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#initWorkflowsFromRecords(java.lang.String, eu.europeana.uim.sugarcrmclient.plugin.objects.data.DatasetStates, eu.europeana.uim.sugarcrmclient.plugin.objects.data.DatasetStates)
 	 */
 	@Override
 	public List<Workflow>  initWorkflowsFromRecords(String worklfowName,
 			DatasetStates currentstate, DatasetStates endstate)
 			throws QueryResultException, StorageEngineException {
 
 
 		ArrayList<Workflow> wfs = new ArrayList<Workflow>();
 		
 		SimpleSugarCrmQuery query = new SimpleSugarCrmQuery();
		query.setMaxResults(1000);
		query.setOffset(0);
		query.setOrderBy(RetrievableField.DATE_ENTERED);
		query.setStatus(currentstate);
 		
 		List<SugarCrmRecord> relevantRecords = retrieveRecords(query);
 		
 		for(SugarCrmRecord record : relevantRecords){
 			Workflow ws =initWorkflowFromRecord(worklfowName,record,endstate);
 			wfs.add(ws);
 		}
 		
 		return wfs;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#createProviderFromRecord(eu.europeana.uim.sugarcrmclient.plugin.objects.SugarCrmRecord)
 	 */
 	@Override
 	public Provider<?> createProviderFromRecord(SugarCrmRecord record)
 			throws StorageEngineException {
 		
 		StorageEngine<?> engine = registry.getStorage();
 		
 	    String collectionName = record.getItemValue(RetrievableField.NAME);  //"name"
 	    String providerName = record.getItemValue(RetrievableField.ORGANIZATION_NAME); //"account_name"
 	    String providerAcronymName = record.getItemValue(RetrievableField.ACRONYM); //"name_acronym_c"
 	    String mnemonicCode = record.getItemValue(RetrievableField.ID);  //"id"
 	    String countryCode = record.getItemValue(RetrievableField.COUNTRY); //"country_c"
 	    String harvestUrl = record.getItemValue(RetrievableField.HARVEST_URL); //"harvest_url_c"

 	    Provider cuurprovider = engine.findProvider(mnemonicCode);
 	    
         if (cuurprovider == null){
 
         	cuurprovider = engine.createProvider();
         	cuurprovider.setAggregator(false);
         	cuurprovider.setMnemonic(mnemonicCode);
         	cuurprovider.setName(providerName);
         	cuurprovider.setOaiBaseUrl(harvestUrl);
         	cuurprovider.setOaiMetadataPrefix("?");
    
         	
         	engine.updateProvider(cuurprovider);
         	engine.checkpoint();
         }
         
         return cuurprovider;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#createCollectionFromRecord(eu.europeana.uim.sugarcrmclient.plugin.objects.SugarCrmRecord, eu.europeana.uim.store.Provider)
 	 */
 	@Override
 	public Collection<?> createCollectionFromRecord(SugarCrmRecord record,
 			Provider provider) throws StorageEngineException {
 		
 		StorageEngine<?> engine = registry.getStorage();
 		
 	    String collectionName = record.getItemValue(RetrievableField.NAME);  //"name"
 	    String providerName = record.getItemValue(RetrievableField.ORGANIZATION_NAME); //"account_name"
 	    String providerAcronymName = record.getItemValue(RetrievableField.ACRONYM); //"name_acronym_c"
 	    String mnemonicCode = record.getItemValue(RetrievableField.ID);  //"id"
 	    String countryCode = record.getItemValue(RetrievableField.COUNTRY); //"country_c"
 	    String harvestUrl = record.getItemValue(RetrievableField.HARVEST_URL); //"harvest_url_c"
 	    
 	    Collection currcollection = engine.findCollection(mnemonicCode);
 	    
         if(currcollection == null){
         	
         	currcollection = engine.createCollection(provider);
         	currcollection.setLanguage(countryCode);
         	currcollection.setMnemonic(mnemonicCode);
         	currcollection.setName(collectionName);
         	currcollection.setOaiBaseUrl(harvestUrl);
         	currcollection.setOaiMetadataPrefix("?");
         	
 			engine.updateCollection(currcollection);
 			engine.checkpoint();
         }
 		
 	    return currcollection;	
 	}
 
 
 	
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#addNoteAttachmentToRecord(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void addNoteAttachmentToRecord(String recordId, String message)
 			throws FileAttachmentException {
 		SetNoteAttachment request = new SetNoteAttachment();
 	    NoteAttachment note = new NoteAttachment();
 	    
 	    note.setId(recordId);
 	    note.setFile(message);
 	    
 	    Date date = new Date();
 	    
 	    note.setFilename(date.toString() + ".txt");

 		request.setNote(note);
 		request.setSession(sugarwsClient.getSessionID());
 		ClientUtils.logMarshalledObject(request);
 		SetNoteAttachmentResponse resp = sugarwsClient.set_note_attachment(request);
 		
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#addPollingListener(eu.europeana.uim.sugarcrmclient.plugin.objects.listeners.PollingListener)
 	 */
 	@Override
 	public void addPollingListener(PollingListener listener) {
 		if(this.pollingListeners == null){
 			this.pollingListeners = new ArrayList<PollingListener>();
 		}
 		this.pollingListeners.add(listener);
 		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#removePollingListener(eu.europeana.uim.sugarcrmclient.plugin.objects.listeners.PollingListener)
 	 */
 	@Override
 	public void removePollingListener(PollingListener listener) {
 		if(this.pollingListeners == null){
 			this.pollingListeners = new ArrayList<PollingListener>();
 		}
 		this.pollingListeners.remove(listener);
 		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#getPollingListeners()
 	 */
 	@Override
 	public List<PollingListener> getPollingListeners() {
 		
 		return this.pollingListeners;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService#setPollingListeners(java.util.List)
 	 */
 	@Override
 	public void setPollingListeners(List<PollingListener> listeners) {
 		this.pollingListeners = listeners;
 		
 	}
 	
 	
 	/*
 	 * Private methods
 	 */
 	
 	/**
 	 * @param list
 	 * @return
 	 */
 	private ArrayList<SugarCrmRecord>  wrapDomElements2Objects(ArrayList<Element> list){
 		
 		ArrayList<SugarCrmRecord> returnList = new ArrayList<SugarCrmRecord>();
 		
 		for (int i=0; i < list.size(); i++){
 			
 			SugarCrmRecord record = SugarCrmRecord.getInstance(list.get(i));
 			returnList.add(record);
 		}
 		
 		return returnList;
 	}
 	
 	
 
 	/**
 	 * @param engine
 	 * @param trigger
 	 * @return
 	 * @throws StorageEngineException
 	 */
 	private Collection inferCollection(StorageEngine engine, SugarCrmRecord trigger) throws StorageEngineException{
 		    
 		Provider prov =  createProviderFromRecord(trigger);
 		Collection currcollection  = createCollectionFromRecord(trigger, prov);
 		return currcollection;	
 	}
 	
 	
 	
 	/**
 	 * @param value
 	 * @param el
 	 * @return
 	 */
 	private String extractFromElement(String value, Element el){
 		
 		NodeList nl =el.getElementsByTagName(value);
 		
 		if(nl.getLength() != 0){
 			
 			return nl.item(0).getTextContent();
 		}
 		
 		
 		return null;
 	}
 	
 	
 	/**
 	 * @param id
 	 * @param status
 	 * @throws QueryResultException 
 	 */
 	private void alterSugarCRMItemStatus(String id, String status) throws QueryResultException{
 		SetEntry request = new SetEntry();
 		
 		
 		NameValue nv0 = new NameValue();
 		nv0.setName("sales_stage");
 		nv0.setValue(status);
 		
 		NameValue nv1 = new NameValue();
 		nv1.setName("id");
 		nv1.setValue(id);
 		
 		ArrayList <NameValue> nvList = new  ArrayList <NameValue>();
 		
 		nvList.add(nv0);
 		nvList.add(nv1);
 		
 		NameValueList valueList = ClientUtils.generatePopulatedNameValueList(nvList);
 		
 		request.setNameValueList(valueList);
 		request.setModuleName(DSMODULENAME);
 		request.setSession(sugarwsClient.getSessionID());	
 		
 		SetEntryResponse response =  sugarwsClient.set_entry(request);
 
 				
 	}
 	
 	
 
 	
 	
 	/*
 	 * Getters & Setters
 	 */
 	
 	
 	public void setSugarwsClient(SugarWsClient sugarwsClient) {
 		this.sugarwsClient = sugarwsClient;
 	}
 
 	public SugarWsClient getSugarwsClient() {
 		return sugarwsClient;
 	}
 
 
 
 	public void setOrchestrator(Orchestrator orchestrator) {
 		this.orchestrator = orchestrator;
 	}
 
 
 
 	public Orchestrator getOrchestrator() {
 		return orchestrator;
 	}
 
 
 
 	public void setRegistry(Registry registry) {
 		this.registry = registry;
 	}
 
 
 
 	public Registry getRegistry() {
 		return registry;
 	}
 
 
 
 
 }
