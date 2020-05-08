 package de.flower.common.ui.ajax.markup.html.tab;
 
 import de.flower.rmt.ui.common.panel.BasePanel;
 import org.apache.wicket.Page;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
 import org.apache.wicket.extensions.markup.html.tabs.ITab;
 import org.apache.wicket.model.IModel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author flowerrrr
  */
 public abstract class AbstractAjaxTabbedPanel<T> extends BasePanel<T> {
 
     /**
      * Goes into url so keep it small.
      */
     public static final String TAB_INDEX_KEY = "tab";
 
     private AjaxTabbedPanel tabbedPanel;
 
     public AbstractAjaxTabbedPanel() {
         this(null);
     }
 
     public AbstractAjaxTabbedPanel(final IModel<T> model) {
         super(model);
         // create a list of ITab objects used to feed the tabbed panel
         List<ITab> tabs = new ArrayList<ITab>();
         addTabs(tabs);
         tabbedPanel = new AjaxTabbedPanel("tabs", tabs) {
             @Override
             protected void onAjaxUpdate(final AjaxRequestTarget target) {
                 AbstractAjaxTabbedPanel.this.onAjaxUpdate(target, getSelectedTab());
             }
         };
         add(tabbedPanel);
     }
 
     @Override
     protected void onInitialize() {
         super.onInitialize();
         selectActiveTab();
     }
 
     /**
      * Cannot be called in constructor cause page is not available then.
      */
     private void selectActiveTab() {
         Page page = findPage();
         if (page != null) {
             int index = page.getPageParameters().get(TAB_INDEX_KEY).toInt(0);
            tabbedPanel.setSelectedTab(index);
         }
     }
 
     public int getSelectedTab() {
         return tabbedPanel.getSelectedTab();
     }
 
     /**
      * Must be implementing classes to add their panels to the tab-list.
      */
     protected abstract void addTabs(List<ITab> tabs);
 
     /**
      * See AjaxTabbedPanel#onAjaxUpdate().
      * @param target
      */
     protected void onAjaxUpdate(final AjaxRequestTarget target, int selectedTab) {
     }
 }
