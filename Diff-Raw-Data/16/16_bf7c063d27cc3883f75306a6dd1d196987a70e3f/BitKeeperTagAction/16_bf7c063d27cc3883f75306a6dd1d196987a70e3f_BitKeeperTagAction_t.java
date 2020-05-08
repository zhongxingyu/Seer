 package hudson.plugins.bitkeeper;
 
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractModelObject;
 import hudson.model.Action;
 import hudson.scm.AbstractScmTagAction;
 
 public class BitKeeperTagAction extends AbstractModelObject implements Action {	
 	private String csetkey;
 	
 	public BitKeeperTagAction(AbstractBuild b, String key) {
 		this.csetkey = key;
 	}
 	
 	public String getCsetkey() {
 		return csetkey;
 	}
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8075839917372981396L;
 
 	public String getDisplayName() {
 		return "BitKeeper Changeset";
 	}
 
 	public String getIconFileName() {
 		return null; //return "clipboard.gif";
 	}
 
 	public String getUrlName() {
 		return "recentChangeset";
 	}
 
 	public String getSearchUrl() {
 		return getUrlName();
 	}
 }
