 /**
  *
  */
 package org.mule.galaxy.web.client.ui.renderer;
 
 import org.mule.galaxy.web.client.ui.panel.WidgetHelper;
 
 import com.extjs.gxt.ui.client.data.BaseModel;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.HtmlContainer;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 
 public class FauxLinkRenderer implements GridCellRenderer<BaseModel> {
 
     private final boolean hover;
     private final String toolTip;
 
     public FauxLinkRenderer() {
         this(true);
     }
 
     public FauxLinkRenderer(boolean hover) {
         this(null, hover);
     }
 
     /**
      * Support to add a tooltip to the link
      * @param toolTip - the model property where the String is set (ie, model.get("property") )
      */
     public FauxLinkRenderer(String toolTip) {
         this(toolTip, true);
     }
 
     public FauxLinkRenderer(String toolTip, boolean hover) {
         this.toolTip = toolTip;
         this.hover = hover;
     }
 
     public Object render(BaseModel model, String property, ColumnData config, int rowIndex,
                          int colIndex, ListStore<BaseModel> store, Grid<BaseModel> grid) {
 
         String value = getText(model, property);
         if (value == null) {
             return null;
         }
 
         HtmlContainer htmlWrapper = new HtmlContainer(WidgetHelper.createFauxLink(value, hover));
         if(toolTip != null) {
             htmlWrapper.setToolTip(model.<String>get(toolTip));
         }
 
         return htmlWrapper;
     }
 
     protected String getText(BaseModel model, String property) {
        return model.<Object>get(property).toString();
     }
 
 }
