 package org.glite.authz.pap.distribution;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.glite.authz.pap.common.PAP;
 import org.glite.authz.pap.common.utils.xacml.PolicySetHelper;
 import org.glite.authz.pap.repository.PAPContainer;
 import org.glite.authz.pap.repository.RepositoryManager;
 import org.glite.authz.pap.repository.dao.PAPDAO;
 import org.glite.authz.pap.repository.exceptions.AlreadyExistsException;
 import org.glite.authz.pap.repository.exceptions.NotFoundException;
 import org.glite.authz.pap.ui.wizard.BlacklistPolicySet;
 import org.glite.authz.pap.ui.wizard.ServiceClassPolicySet;
 import org.opensaml.xacml.policy.PolicySetType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PAPManager {
     
 	private static PAPManager instance = null;
     private static final Logger log = LoggerFactory.getLogger(PAPManager.class);
     protected static PAP localPAP;
     
     public static PAPManager getInstance() {
         if (instance == null)
             instance = new PAPManager();
         return instance;
     }
     
     protected DistributionConfiguration distributionConfiguration;
     protected PAPDAO papDAO;
     protected List<PAP> papList;
     
     protected PAPManager() {
         distributionConfiguration = DistributionConfiguration.getInstance();
         papDAO = RepositoryManager.getDAOFactory().getPAPDAO();
         localPAP = PAP.makeLocalPAP();
         initPAPList();
     }
     
     public PAPContainer addTrustedPAP(PAP pap) {
     	
     	String papAlias = pap.getAlias();
         
         if (exists(papAlias))
             throw new AlreadyExistsException("PAP \"" + papAlias + "\" already exists");
         
         distributionConfiguration.savePAP(pap);
         papList.add(pap);
         papDAO.store(pap);
         
         return new PAPContainer(pap);
     }
     
     public void createLocalPAPIfNotExists() {
         
         if (localPAPExists())
             return;
         
         papDAO.store(localPAP);
         
         PAPContainer localPAPContainer = getLocalPAPContainer();
         
         PolicySetType localPolicySet = PolicySetHelper.buildWithAnyTarget(localPAP.getPapId(),
                 PolicySetHelper.COMB_ALG_ORDERED_DENY_OVERRIDS);
         
         localPAPContainer.storePolicySet((new BlacklistPolicySet()).getPolicySetType());
         localPAPContainer.storePolicySet((new ServiceClassPolicySet()).getPolicySetType());
         
         PolicySetHelper.addPolicySetReference(localPolicySet, BlacklistPolicySet.POLICY_SET_ID);
         PolicySetHelper.addPolicySetReference(localPolicySet, ServiceClassPolicySet.POLICY_SET_ID);
         
         localPAPContainer.storePolicySet(localPolicySet);
     }
     
     public PAP deleteTrustedPAP(String papAlias) throws NotFoundException {
     	PAP pap = getPAP(papAlias);
    	papList.remove(pap);
     	distributionConfiguration.removePAP(papAlias);
         papDAO.delete(papAlias);
         return pap;
     }
     
     public boolean exists(String papAlias) {
         for (PAP pap:papList) {
             if (pap.getAlias().equals(papAlias))
                 return true;
         }
         return false;
     }
     
     public List<PAP> getAllTrustedPAPs() {
         return new ArrayList<PAP>(papList);
     }
     
     public PAP getLocalPAP() {
         return localPAP;
     }
     
     public PAPContainer getLocalPAPContainer() {
         if (!localPAPExists())
             throw new NotFoundException("Critical error (probably a BUG): local PAP not found.");
         return new PAPContainer(localPAP);
     }
     
     public PAP getPAP(String papAlias) throws NotFoundException {
         for (PAP pap : papList) {
             if (pap.getAlias().equals(papAlias))
                 return pap;
         }
         
         log.debug("Requested PAP not found:" + papAlias);
         throw new NotFoundException("PAP not found: " + papAlias);
     }
     
     public List<PAP> getPublicTrustedPAPs() {
         
         List<PAP> resultList = new LinkedList<PAP>();
         
         for (PAP pap:papList) {
             if (pap.isVisibilityPublic())
                 resultList.add(pap);
         }
         
         return resultList;
     }
     
     public PAPContainer getTrustedPAPContainer(String papAlias) {
         return new PAPContainer(getPAP(papAlias));
     }
     
     public List<PAPContainer> getTrustedPAPContainerAll() {
         List<PAPContainer> papContainerList = new ArrayList<PAPContainer>(papList.size());
         for (PAP pap:papList) {
             papContainerList.add(new PAPContainer(pap));
         }
         return papContainerList;
     }
     
     public List<PAPContainer> getTrustedPAPContainerPublic() {
         List<PAPContainer> papContainerList = new LinkedList<PAPContainer>();
         for (PAP pap:papList) {
             if (pap.isVisibilityPublic())
                 papContainerList.add(new PAPContainer(pap));
         }
         return papContainerList;
     }
     
     public String[] getTrustedPAPOrder() {
     	return distributionConfiguration.getPAPOrderArray();
     }
     
     public void setTrustedPAPOrder(String[] aliasArray) {
     	distributionConfiguration.savePAPOrder(aliasArray);
     	papList = distributionConfiguration.getRemotePAPList();
     }
     
     public void updateTrustedPAP(String papAlias, PAP newpap) {
         
         boolean found = false;
         
         for (int i=0; i<papList.size(); i++) {
             PAP pap = papList.get(i);
             if (pap.getAlias().equals(papAlias)) {
                 papList.set(i, newpap);
                 found = true;
                 break;
             }
         }
         
         if (!found)
             throw new NotFoundException("PAP not found (id=" + papAlias + ")");
     }
     
     private void initPAPList() {
     	
     	// synchronize the PAPs stored in the repository with the
     	// ones defined in the distribution configuration
     	
     	papList = new LinkedList<PAP>();
         List<PAP> configPAPList = DistributionConfiguration.getInstance().getRemotePAPList();
         
         // follow the order of the distribution config PAP list
         for (PAP cpap : configPAPList) {
         	String papAlias = cpap.getAlias();
         	try {
         		PAP rpap = papDAO.get(papAlias);
         		if (cpap.equals(rpap)) {
         			papList.add(rpap);
         			continue;
         		} else {
         			log.info("Settings for PAP \"" + papAlias
 							+ "\" has been updated. Invalidating cache");
 					papDAO.delete(papAlias);
         		}
         	} catch (NotFoundException e) {
         		// nothing to do
         	}
         	papDAO.store(cpap);
         }
         
         // remove from the repository PAPs that are not in the distribution configuration
         for (String alias:papDAO.getAllAliases()) {
         	
         	if (alias.equals(PAP.localPAPAlias))
         		continue;
         	
             if (exists(alias))
                 continue;
             
             log.info("Removing PAP \"" + alias + "\"");
             papDAO.delete(alias);
         }
     }
     
     private boolean localPAPExists() {
         return papDAO.exists(localPAP.getAlias());
     }
     
 }
