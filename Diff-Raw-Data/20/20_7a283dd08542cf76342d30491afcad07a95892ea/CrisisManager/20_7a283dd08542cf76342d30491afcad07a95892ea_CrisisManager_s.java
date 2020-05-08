 package crisis.application;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import crisis.api.model.Crisis;
 import crisis.api.model.CrisisGroup;
 import crisis.api.model.CrisisGroupId;
 import crisis.api.model.CrisisMoney;
 import crisis.api.model.CrisisTx;
 import crisis.api.model.CrisisTxId;
 import crisis.api.model.CrisisTxType;
 import crisis.api.persistence.CrisisPersistenceException;
 import crisis.api.persistence.ICrisisPersistenceManager;
 import crisis.application.exceptions.LoadException;
 import crisis.application.exceptions.SaveException;
 import crisis.application.listeners.ICrisisDataChangeListener;
 import crisis.persistence.xml.jaxb.CrisisPersistenceJaxb;
 
 public class CrisisManager {
 
 	private static final ICrisisPersistenceManager persistenceManager = new CrisisPersistenceJaxb();
 
 	private static final int BUFFER = 2048;
 
 	private static final List<ICrisisDataChangeListener> modelChangeListeners = new LinkedList<>();
 
 	private static CrisisManager manager;
 
 	private static volatile boolean isDirty = false;
 
 	private static CrisisGroup groupsRoot;
 
 	private final AtomicLong groupMaxId = new AtomicLong(0);;
 	
 	private final AtomicLong txMaxId = new AtomicLong(0);
 	
 	private final Map<CrisisGroupId, CrisisGroup> groupByIdMap = new HashMap<>();
 
 	private final Map<CrisisTxId, CrisisTx> txByIdMap = new HashMap<>();
 
 	private Crisis crisis;
 	
 	public static CrisisManager getManager() {
 
 		if (manager == null) {
 			manager = new CrisisManager();
 		}
 		return manager;
 	}
 
 	
 	private CrisisManager() {
 		
 		if(CrisisPreferences.getAutoLoadEnabled()
 				&& !CrisisPreferences.getLastLoadedFile().isEmpty()){
 			try {
 				load(CrisisPreferences.getLastLoadedFile());
 			} catch(LoadException e) {
 				// TODO trace
 				System.out.println(e);
 			} catch (CrisisPersistenceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} 
 
 		if(crisis == null) {
 			crisis = new Crisis(new ArrayList<CrisisGroup>(), new ArrayList<CrisisTx>());
			
			crisis.getGroups().add(
					new CrisisGroup(
							new CrisisGroupId(1),
							"asdda",
							null,
							"",
							new ArrayList<CrisisGroupId>(),
							new ArrayList<CrisisTxId>()));
			
 		}
 		
 		dataChanged();
 	}
 	
 	private Long getNextGroupId() {
 		
 		return groupMaxId.incrementAndGet();
 	}
 	
 	private Long getNextTxId() {
 		
 		return txMaxId.incrementAndGet();
 	}
 
 	
 
 
 
 
 	private void setIsDirty(boolean isDirty) {
 		CrisisManager.isDirty = isDirty;
 	}
 
 	public boolean isDirty() {
 
 		return CrisisManager.isDirty;
 	}
 
 	
 	public void save(String fileName) throws SaveException, CrisisPersistenceException {
 
 		// save model
 		persistenceManager.saveCrisisData(crisis, "crisis.xml");
 		
 		// zip
 		try {
 			BufferedInputStream origin = null;
 			FileOutputStream dest = new FileOutputStream(fileName);
 			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
 					dest));
 			byte data[] = new byte[BUFFER];
 
 			String files[] = new String[] { "crisis.xml" };
 
 			for (int i = 0; i < files.length; i++) {
 				System.out.println("Adding: " + files[i]);
 				FileInputStream fi = new FileInputStream(files[i]);
 				origin = new BufferedInputStream(fi, BUFFER);
 				ZipEntry entry = new ZipEntry(files[i]);
 				out.putNextEntry(entry);
 				int count;
 				while ((count = origin.read(data, 0, BUFFER)) != -1) {
 					out.write(data, 0, count);
 				}
 				origin.close();
 			}
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new SaveException(
 					"Could not save! File cannot be written...", e);
 		}
 
 		CrisisPreferences.setLastLoadedFile(fileName);
 		setIsDirty(false);
 	}
 	
 	
 	public void load(String fileName) throws LoadException, CrisisPersistenceException {
 
 		// unzip
 		try {
 			BufferedOutputStream dest = null;
 			FileInputStream fis = new FileInputStream(fileName);
 			ZipInputStream zis = new ZipInputStream(
 					new BufferedInputStream(fis));
 			ZipEntry entry;
 			while ((entry = zis.getNextEntry()) != null) {
 				int count;
 				byte data[] = new byte[BUFFER];
 				// write the files to the disk
 				FileOutputStream fos = new FileOutputStream(entry.getName());
 				dest = new BufferedOutputStream(fos, BUFFER);
 				while ((count = zis.read(data, 0, BUFFER)) != -1) {
 					dest.write(data, 0, count);
 				}
 				dest.flush();
 				dest.close();
 			}
 			zis.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new LoadException("Could not load! File '" + fileName
 					+ "' doesn't exist...", e);
 		}
 
 		// load data model
 		crisis = persistenceManager.loadCrisisData("crisis.xml");
 
 		groupByIdMap.clear();
 		
 		txByIdMap.clear();
 		
 		for(CrisisGroup group : crisis.getGroups()) {
 			// load group map
 			groupByIdMap.put(group.getGroupId(), group);
 			// find max group id
 			if(	groupMaxId.get() < group.getGroupId().getId()) {
 				groupMaxId.set(group.getGroupId().getId());
 			}
 		}
 
 		for(CrisisTx tx : crisis.getTransactions()) {
 			// load map
 			txByIdMap.put(tx.getTxId(), tx);
 			// find max group id
 			if(	txMaxId.get() < tx.getTxId().getId()) {
 				txMaxId.set(tx.getTxId().getId());
 			}
 		}
 
 		CrisisPreferences.setLastLoadedFile(fileName);
 		
 		setIsDirty(false);
 
 		dataChanged();
 	}
 	
 	
 	public CrisisGroup getGroupsRoot() {
 		
 		return groupsRoot;
 	}
 	
 	
 	public List<CrisisGroup> getAllGroups() {
 
 		return crisis.getGroups();
 	}
 	
 	
 	public CrisisGroup getGroupById(CrisisGroupId groupId) {
 		
 		return groupByIdMap.get(groupId);
 	}
 	
 	
 	public List<CrisisGroup> getGroupsById(List<CrisisGroupId> groupIdList) {
 		
 		List<CrisisGroup> groupList = new ArrayList<>();
 		
 		for(CrisisGroupId groupId : groupIdList) {
 			
 			groupList.add(getGroupById(groupId));
 		}
 		
 		return groupList;
 	}
 
 
 	public CrisisGroup createGroup(
 			String name,
 			CrisisGroup parentGroup,
 			String description) {
 		
 		CrisisGroupId newGroupId = new CrisisGroupId(getNextGroupId()); 
 		
 		CrisisGroup newGroup = new CrisisGroup(
 				newGroupId,
 				name,
				parentGroup == null ? null : parentGroup.getParentGroupId(),
 				description,
 				new ArrayList<CrisisGroupId>(),
 				new ArrayList<CrisisTxId>());
 		
 		if (parentGroup != null) {
 			parentGroup.getChildGroupIdList().add(newGroup.getGroupId());
 		}
 		crisis.getGroups().add(newGroup);
 
 		groupByIdMap.put(newGroupId, newGroup);
 
 		setIsDirty(true);
 
 		dataChanged();
 
 		return newGroup;
 	}
 
 
 	public CrisisGroup modifyGroup(
 			CrisisGroupId groupId,
 			String newName,
 			CrisisGroup newParentGroup,
 			String newDescription) {
 
 		CrisisGroup group = groupByIdMap.get(groupId);
 		CrisisGroup parentGroup =  groupByIdMap.get(group.getParentGroupId());
 		
 		parentGroup.getChildGroupIdList().remove(groupId);
 		newParentGroup.getChildGroupIdList().add(groupId);
 		group.setParentGroupId(newParentGroup.getGroupId());
 
 		group.setGroupName(newName);
 		group.setGroupDescription(newDescription);
 
 
 		setIsDirty(true);
 
 		dataChanged();
 		
 		return group;
 	}
 
 
 	public CrisisGroup deleteGroup(CrisisGroup group) {
 
 		CrisisGroup parentGroup = null;
 		if(group.getParentGroupId() != null) {
 			parentGroup = groupByIdMap.get(group.getParentGroupId());
 			parentGroup.getChildGroupIdList().remove(group.getGroupId());
 		}
 		
 		crisis.getGroups().remove(group);
 
 		groupByIdMap.remove(group.getGroupId());
 
 		setIsDirty(true);
 
 		dataChanged();
 		
 		return group;
 	}
 
 
 	public List<CrisisTx> getAllTransactions() {
 		
 		return crisis.getTransactions();
 	}
 	
 
 	public CrisisTx getTransactionById(CrisisTxId txId) {
 		
 		return txByIdMap.get(txId);
 	}
 	
 	
 	public List<CrisisTx> getTransactionsById(List<CrisisTxId> txIdList) {
 		
 		List<CrisisTx> txList = new ArrayList<>();
 		
 		for(CrisisTxId txId : txIdList) {
 			txList.add(getTransactionById(txId));
 		}
 		
 		return txList;
 	}
 	
 	
 	public CrisisTx createTx(
 			CrisisGroup parentGroup,
 			Calendar date,
 			String description,
 			CrisisMoney value,
 			String comment,
 			CrisisTxType txType) {
 
 		CrisisTxId newTxId = new CrisisTxId(getNextTxId());
 		CrisisTx newTx = new CrisisTx(
 				newTxId,
 				parentGroup.getGroupId(),
 				date,
 				description,
 				value,
 				comment, 
 				txType);
 		
 		parentGroup.getTransactionIdList().add(newTxId);
 
 		txByIdMap.put(newTxId, newTx);
 
 		setIsDirty(true);
 		
 		dataChanged();
 		
 		return newTx;
 	}
 
 	
 	public CrisisTx modifyTx(
 			CrisisTxId txId,
 			CrisisGroup newParentGroup,
 			Calendar newDate,
 			String newDescription,
 			CrisisMoney newValue,
 			String newComment,
 			CrisisTxType newTxType) {
 
 		CrisisTx tx = txByIdMap.get(txId);
 		CrisisGroupId parentGroupId = tx.getParentGroupId();
 		CrisisGroup parentGroup = groupByIdMap.get(parentGroupId);
 		
 		parentGroup.getTransactionIdList().remove(tx);
 		newParentGroup.getTransactionIdList().add(txId);
 		tx.setParentGroupId(newParentGroup.getGroupId());
 		
 		tx.setDate(newDate);
 		tx.setDescription(newDescription);
 		tx.setValue(newValue);
 		tx.setComment(newComment);
 		tx.setTxType(newTxType);
 
 		setIsDirty(true);
 
 		dataChanged();
 		
 		return tx;
 	}
 
 
 	public CrisisTx deleteTx(CrisisTx tx) {
 
 		CrisisTxId txId = tx.getTxId();
 		CrisisGroupId parentGroupId = tx.getParentGroupId();
 		CrisisGroup parentGroup = groupByIdMap.get(parentGroupId);
 		parentGroup.getTransactionIdList().remove(txId);
 		
 		crisis.getTransactions().remove(tx);
 
 		txByIdMap.remove(txId);
 		
 		dataChanged();
 
 		setIsDirty(true);
 
 		return tx;
 	}
 
 	
 	private void dataChanged() {
 
 		for (ICrisisDataChangeListener listener : modelChangeListeners) {
 		
 			listener.dataChanged();
 		}
 	}
 
 	
 	public void registerListener(ICrisisDataChangeListener listener) {
 
 		modelChangeListeners.add(listener);
 	}
 
 	
 	public void unregisterListener(ICrisisDataChangeListener listener) {
 
 		modelChangeListeners.remove(listener);
 	}
 
 
 	public List<CrisisGroup> getTopLevelGroups() {
 		List<CrisisGroup> topLevelGroups = new LinkedList<>();
 		for(CrisisGroup group : getAllGroups()) {
 			if(group.getParentGroupId() == null) {
 				topLevelGroups.add(group);
 			}
 		}
 		return topLevelGroups;
 	}
 }
