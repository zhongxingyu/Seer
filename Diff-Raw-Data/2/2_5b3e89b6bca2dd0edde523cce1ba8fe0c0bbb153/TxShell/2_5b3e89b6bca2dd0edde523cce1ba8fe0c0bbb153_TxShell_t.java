 /*
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package taxonomy.webui.client.widget;
 
 import taxonomy.resources.client.model.VNaturalObject;
 
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.sencha.gxt.core.client.util.Margins;
 import com.sencha.gxt.widget.core.client.TabItemConfig;
 import com.sencha.gxt.widget.core.client.TabPanel;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.MarginData;
 
 /**
  * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
  * @version $Id$
  * 
  */
 public class TxShell extends BorderLayoutContainer {
 
   /** . */
   static TabPanel center;
 
   @Inject
   public TxShell() {
     monitorWindowResize = true;
     Window.enableScrolling(false);
     setPixelSize(Window.getClientWidth(), Window.getClientHeight());
 
     setStateful(false);
     setStateId("explorerLayout");
 
     // BorderLayoutStateHandler state = new BorderLayoutStateHandler(this);
     // state.loadState();
 
     HTML north = new HTML();
     north.setHTML("<div id='demo-theme'></div><div id=demo-title>Taxonomy Web Base Application</div>");
     north.getElement().setId("demo-header");
 
     BorderLayoutData northData = new BorderLayoutData(35);
     setNorthWidget(north, northData);
 
     setSouthWidget(new OperatorToolbar(), new BorderLayoutData(40));
     
     MarginData centerData = new MarginData();
     centerData.setMargins(new Margins(5));
 
     center = new TabPanel();
     center.setTabScroll(true);
     center.setCloseContextMenu(true);
     center.addSelectionHandler(new SelectionHandler<Widget>() {
       @Override
       public void onSelection(SelectionEvent<Widget> event) {
         if(event.getSelectedItem() instanceof ModelGridPanel) {
           OperatorToolbar.rootPanel = (ModelGridPanel)event.getSelectedItem();
           if(OperatorToolbar.rootPanel.getCheckBoxSelectionModel().getSelectedItems().size() == 0) {
             OperatorToolbar.disableModifyButton();
          } else {
            OperatorToolbar.enableModifyButton();
           }
         }
       }
     });
 
     TabItemConfig tabConfig = new TabItemConfig(Tables.NATURALOBJECT.getName(), true);
     ModelGridPanel<VNaturalObject> panel = ModelGridFactory.createNObject();
     center.add(panel, tabConfig);
     center.setActiveWidget(panel);
     setCenterWidget(center, centerData);
   }
 
   @Override
   protected void onWindowResize(int width, int height) {
     setPixelSize(width, height);
   }
 }
