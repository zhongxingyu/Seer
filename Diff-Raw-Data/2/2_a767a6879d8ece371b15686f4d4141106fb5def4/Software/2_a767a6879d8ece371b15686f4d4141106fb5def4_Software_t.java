 package openske.model.software;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import openske.model.InfrastructureItem;
 import openske.model.assets.Asset;
 import openske.model.assets.AssetAccess;
 import openske.model.assets.AssetAccessType;
 import openske.model.assets.AssetAccessor;
 import openske.model.assets.AssetType;
 import openske.model.hardware.Host;
 import openske.model.measurablesecurity.CpeEntry;
 import openske.model.security.SecurityState;
 import openske.model.security.UserAccount;
 import openske.model.security.UserGroup;
 
 
 public class Software extends InfrastructureItem implements AssetAccessor, CpeEntry, Comparable<Software> {
 
     protected Host host;
     protected String cpeId;
     protected boolean anAsset;
     protected SecurityState securityState;
     protected List<UserAccount> accounts;
     protected List<AssetAccess> assetAccesses;
     protected List<Software> dependencies;
     protected List<UserGroup> groups;
     protected List<Vulnerability> vulnerabilities;
     protected List<Weakness> weaknesses;
 
     public Software(String cpeId, Host host) {
         this.cpeId = cpeId;
         this.host = host;
         this.vulnerabilities = new ArrayList<Vulnerability>();
         this.weaknesses = new ArrayList<Weakness>();
         this.dependencies = new ArrayList<Software>();
         this.accounts = new ArrayList<UserAccount>();
         this.groups = new ArrayList<UserGroup>();
         this.assetAccesses = new ArrayList<AssetAccess>();
         this.securityState = SecurityState.UNKNOWN;
     }
 
     public Software addAccount(String username, String password) {
         UserAccount account = new UserAccount(username, password, this);
         return this.addAccount(account);
     }
 
     public Software addAccount(UserAccount account) {
         if (!this.accounts.contains(account)) {
             this.accounts.add(account);
         }
         return this;
     }
 
     public Software addVulnerabilities(String... identifiers) {
         if (identifiers != null && identifiers.length > 0) {
             for (String id : identifiers) {
                 this.addVulnerability(id);
             }
         }
         return this;
 
     }
 
     public Software addVulnerability(String identifier) {
         Vulnerability vuln = new Vulnerability(identifier, this);
         if (vuln != null && !this.hasVulnerability(vuln)) {
             this.vulnerabilities.add(vuln);
             vuln.setSoftware(this);
             this.addWeaknesses(Weakness.forVulnerability(vuln));
         }
         return this;
     }
 
     public Software addWeakness(String identifier) {
         return this.addWeakness(new Weakness(identifier, this));
 
     }
 
     public Software addWeakness(Weakness weakness) {
        if (weakness != null && !this.hasWeakness(weakness)) {
             this.weaknesses.add(weakness);
             weakness.setSoftware(this);
         }
         return this;
     }
 
     public Software addWeaknesses(List<Weakness> weaknesses) {
         if (weaknesses != null) {
             for (Weakness weakness : weaknesses) {
                 if (!this.weaknesses.contains(weakness)) {
                     this.weaknesses.add(weakness);
                 }
             }
         }
         return this;
     }
 
     public List<UserAccount> getAccounts() {
         return accounts;
     }
 
     public List<AssetAccess> getAssetAccesses() {
         return assetAccesses;
     }
 
     public List<Software> getDependencies() {
         return dependencies;
     }
 
     public List<UserGroup> getGroups() {
         return groups;
     }
 
     public Host getHost() {
         return host;
     }
 
     public UserAccount getRandomAccount() {
         if (this.accounts.isEmpty()) {
             return null;
         } else {
             int randomIndex = new Random().nextInt(this.accounts.size());
             return this.accounts.get(randomIndex);
         }
     }
 
     public Software addAssetAccess(Asset asset, AssetAccessType type) {
         AssetAccess access = new AssetAccess(asset, this, type);
         if (!this.assetAccesses.contains(access)) {
             this.assetAccesses.add(access);
         }
         return this;
     }
 
     public Asset getRandomAsset(AssetType type) {
         if (this.assetAccesses.isEmpty()) {
             return null;
         } else {
             List<Asset> matchedAssets = new ArrayList<Asset>();
             for (AssetAccess access : this.assetAccesses) {
                 if (access.getAsset().getType() == type) {
                     matchedAssets.add(access.getAsset());
                 }
             }
             if (matchedAssets.isEmpty()) {
                 return null;
             } else {
                 int randomIndex = new Random().nextInt(matchedAssets.size());
                 return matchedAssets.get(randomIndex);
             }
         }
     }
 
     public SecurityState getSecurityState() {
         return securityState;
     }
 
     public List<Vulnerability> getVulnerabilities() {
         return vulnerabilities;
     }
 
     public List<Weakness> getWeaknesses() {
         return weaknesses;
     }
 
     public boolean hasVulnerability(Vulnerability vuln) {
         return vuln != null && this.vulnerabilities.contains(vuln);
     }
 
     public boolean hasWeakness(Weakness weakness) {
         return weakness != null && this.weaknesses.contains(weakness);
     }
 
     public boolean isAnAsset() {
         return anAsset;
     }
 
     public void setAccounts(List<UserAccount> accounts) {
         this.accounts = accounts;
     }
 
     public void setAnAsset(boolean anAsset) {
         this.anAsset = anAsset;
     }
 
     public void setAssetAccesses(List<AssetAccess> assetAccesses) {
         this.assetAccesses = assetAccesses;
     }
 
     public void setDependencies(List<Software> dependencies) {
         this.dependencies = dependencies;
     }
 
     public void setGroups(List<UserGroup> groups) {
         this.groups = groups;
     }
 
     public void setHost(Host host) {
         this.host = host;
     }
 
     public void setSecurityState(SecurityState securityState) {
         this.securityState = securityState;
     }
 
     public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
         this.vulnerabilities = vulnerabilities;
     }
 
     public void setWeaknesses(List<Weakness> weaknesses) {
         this.weaknesses = weaknesses;
     }
 
     public String toString() {
         return this.cpeId();
     }
 
     @Override
     public String cpeId() {
         return this.cpeId;
     }
 
     @Override
     public int compareTo(Software o) {
         return cpeId.compareToIgnoreCase(o.cpeId());
     }
 
     @Override
     public String statistics() {
         return String.format("%s : %s (vulnerabilities = %d, weaknesses = %d)", this.getClass().getSimpleName(), cpeId, vulnerabilities.size(), weaknesses.size());
     }
     
     @Override
     public boolean equals(Object obj) {
         return obj instanceof Software && ((Software)obj).cpeId().equals(cpeId);
     }
     
     @Override
     public String inspect() {
         StringBuffer sb = new StringBuffer();
         sb.append(statistics());
         sb.append("\n");
         sb.append(" - Weaknesses:\n");
         for(Weakness w : weaknesses) {
             sb.append("\t - " + w.toString() + " : " + w.getName() + "\n");
         }
 //        for(Vulnerability v : vulnerabilities) {
 //            sb.append("\t - " + v.toString() + "\n");
 //        }
         return sb.toString();
     }
 }
