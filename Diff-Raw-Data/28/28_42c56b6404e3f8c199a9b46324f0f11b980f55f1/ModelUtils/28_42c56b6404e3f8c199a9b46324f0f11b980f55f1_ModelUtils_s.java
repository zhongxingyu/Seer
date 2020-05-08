 package com.pangratz.oeffinpc.model;
 
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 public class ModelUtils {
 
 	private static final ModelUtils INSTANCE = new ModelUtils();
 
 	public static ModelUtils getInstance() {
 		return INSTANCE;
 	}
 
 	private PersistenceManagerFactory mPMF;
 
 	private ModelUtils() {
 		super();
 		mPMF = PMF.get();
 	}
 
 	public NetworkPlan getNetworkPlan(Long networkId) {
 		if (networkId == null) {
 			return null;
 		}
 
 		PersistenceManager pm = mPMF.getPersistenceManager();
 		try {
 			Query query = pm.newQuery(NetworkPlan.class, "key == networkIdParam");
 			query.declareParameters("Long networkIdParam");
 			List<NetworkPlan> networkPlans = (List<NetworkPlan>) query.execute(networkId);
 			if (networkPlans != null && networkPlans.size() == 1) {
 				return networkPlans.get(0);
 			}
 			return null;
 		} finally {
 			pm.close();
 		}
 	}
 
 	public List<NetworkPlanEntry> getNetworkPlanEntries(Long networkId) {
 		PersistenceManager pm = mPMF.getPersistenceManager();
 		try {
 			Query query = pm.newQuery(NetworkPlanEntry.class, "networkPlanKey == networkIdParam");
 			query.setOrdering("name asc");
 			List<NetworkPlanEntry> networkPlanEntries = (List<NetworkPlanEntry>) query.execute(networkId);
 			return (List<NetworkPlanEntry>) pm.detachCopyAll(networkPlanEntries);
 		} finally {
 			pm.close();
 		}
 	}
 
 	public NetworkPlanEntry getNetworkPlanEntry(Long stationId) {
 		if (stationId == null) {
 			return null;
 		}
 
 		PersistenceManager pm = mPMF.getPersistenceManager();
 		try {
 			Query query = pm.newQuery(NetworkPlanEntry.class, "key == stationId");
 			query.declareParameters("Long stationId");
 			List<NetworkPlanEntry> networkPlanEntries = (List<NetworkPlanEntry>) query.execute(stationId);
 			if (networkPlanEntries != null && networkPlanEntries.size() == 1) {
 				return networkPlanEntries.get(0);
 			}
 			return null;
 		} finally {
 			pm.close();
 		}
 	}
 
 	public List<NetworkPlan> getNetworkPlans() {
 		PersistenceManager pm = mPMF.getPersistenceManager();
 		try {
 			List<NetworkPlan> networkPlans = (List<NetworkPlan>) pm.newQuery(NetworkPlan.class).execute();
 			return (List<NetworkPlan>) pm.detachCopyAll(networkPlans);
 		} finally {
 			pm.close();
 		}
 	}
 
 	public void removeNetworkPlanEntry(Long stationKey) {
 		PersistenceManager pm = mPMF.getPersistenceManager();
 		try {
 			Query query = pm.newQuery(NetworkPlanEntry.class, "key == keyParam");
 			query.deletePersistentAll();
 		} finally {
 			pm.close();
 		}
 	}
 
 	public Long storeNetworkPlan(NetworkPlan networkPlan) {
 		PersistenceManager pm = mPMF.getPersistenceManager();
 		try {
 			NetworkPlan persistent = pm.makePersistent(networkPlan);
 			return persistent.getKey();
 		} finally {
 			pm.close();
 		}
 	}
 
 	public Long storeNetworkPlanEntry(NetworkPlanEntry networkPlanEntry) {
 		PersistenceManager pm = mPMF.getPersistenceManager();
 		try {
 			NetworkPlanEntry persistent = pm.makePersistent(networkPlanEntry);
 			return persistent.getKey();
 		} finally {
 			pm.close();
 		}
 	}
 }
