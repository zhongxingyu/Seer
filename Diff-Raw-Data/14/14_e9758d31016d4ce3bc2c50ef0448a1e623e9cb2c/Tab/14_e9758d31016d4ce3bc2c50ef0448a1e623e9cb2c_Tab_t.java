 package gov.nih.nci.cabig.ctms.web.tabs;
 
 import org.springframework.validation.Errors;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * A sub-controller class describing the behavior of one page in a {@link Flow}.
  * Tab classes may be shared between flows, but tab instances must not be. 
  *
  * @author Rhett Sutphin
  */
 public class Tab<C> {
     private Integer number;
     private String longTitle;
     private String shortTitle;
     private String viewName;
     private Flow<C> flow;
 
     public Tab() { }
 
     public Tab(String longTitle, String shortTitle, String viewName) {
         this.longTitle = longTitle;
         this.shortTitle = shortTitle;
         this.viewName = viewName;
     }
 
     ////// TEMPLATE METHODS
 
     public Map<String, Object> referenceData() {
         return new HashMap<String, Object>();
     }
 
     public Map<String, Object> referenceData(C command) {
         return referenceData();
     }
 
     public void validate(C command, Errors errors) {
     }
 
     public boolean isAllowDirtyForward() {
         return true;
     }
 
     public boolean isAllowDirtyBack() {
         return true;
     }
 
     /**
      * Invoked before the tab is displayed.
      */
     public void preProcess(HttpServletRequest request, C command) {
     }
 
     /**
      * Invoked after successful binding and validation.
      */
     public void postProcess(HttpServletRequest request, C command, Errors errors) {
     }
 
     /**
      * Retained for backwards compatibility.  Subclasses should override {@link #getTargetTab},
      * if necessary.
      */
     public final int getTargetNumber() {
         return getTargetTab().getNumber();
     }
 
     public Tab<C> getTargetTab() {
        // default is next (+1), unless at the end
        int defaultTarget = Math.min(getNumber() + 1, getFlow().getTabCount() - 1);
         return getFlow().getTab(defaultTarget);
     }
 
     ////// BEAN PROPERTIES
 
     public Integer getNumber() {
         return number;
     }
 
     public void setNumber(Integer number) {
         this.number = number;
     }
 
     public Flow<C> getFlow() {
         return flow;
     }
 
     public void setFlow(Flow<C> flow) {
         this.flow = flow;
     }
 
     public String getLongTitle() {
         return longTitle;
     }
 
     public void setLongTitle(String longTitle) {
         this.longTitle = longTitle;
     }
 
     public String getShortTitle() {
         return shortTitle;
     }
 
     public void setShortTitle(String shortTitle) {
         this.shortTitle = shortTitle;
     }
 
     public String getViewName() {
         return viewName;
     }
 
     public void setViewName(String viewName) {
         this.viewName = viewName;
     }
 }
