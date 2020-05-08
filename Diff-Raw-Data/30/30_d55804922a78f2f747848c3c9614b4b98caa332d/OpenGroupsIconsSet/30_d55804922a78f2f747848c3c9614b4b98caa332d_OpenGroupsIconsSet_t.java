 package ro.zg.open_groups.gui.constants;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import ro.zg.netcell.vaadin.action.constants.ActionConstants;
 
 import com.vaadin.terminal.ThemeResource;
 
 public class OpenGroupsIconsSet {
     private String dirPath;
     private Map<String, String> iconsMap=new HashMap<String, String>();
     private Map<String,String> sizesMap=new HashMap<String, String>();
     
     public static final String COMMENT="comment";
     public static final String ISSUE="issue";
     public static final String SOLUTION="solution";
     
     public static final String LEFT_ARROW="left_arrow";
     public static final String RIGHT_ARROW="right_arrow";
     
     public static final String NEW_POST="CREATE";
     public static final String UPDATED_POST="UPDATE";
     
     public static final String PARENT="parent";
     
     public static final String VOTE_UP="vote_up";
     public static final String VOTE_DOWN="vote_down";
     public static final String CANCEL="cancel";
     
     public static final String REFRESH="refresh";
     public static final String SEARCH="search";
     
     public static final String UPSTREAM_HIERARCHY_ON="entity.upstream.hierarchy.on";
     public static final String UPSTREAM_HIERARCHY_OFF="entity.upstream.hierarchy.off";
     
     public static final String SHOW_CAUSE_ON= ActionConstants.SHOW_CAUSE+".on";
     public static final String SHOW_CAUSE_OFF=ActionConstants.SHOW_CAUSE+".off";
     
     public static final String SMALL="small";
     public static final String LARGE="large";
     public static final String MEDIUM="medium";
     
     public OpenGroupsIconsSet(String dirPath) {
 	this.dirPath=dirPath;
     }
     
     protected void addIcon(String name, String path) {
 	iconsMap.put(name, path);
     }
     
     protected void addSize(String name, String path) {
 	sizesMap.put(name, path);
     }
     
     public ThemeResource getIcon(String name,String size) {
	String s = "/";
 	return new ThemeResource(dirPath+s+sizesMap.get(size)+s+iconsMap.get(name));
     }
 
     /**
      * @return the dirPath
      */
     public String getDirPath() {
         return dirPath;
     }
 
     /**
      * @param dirPath the dirPath to set
      */
     public void setDirPath(String dirPath) {
         this.dirPath = dirPath;
     }
     
 }
