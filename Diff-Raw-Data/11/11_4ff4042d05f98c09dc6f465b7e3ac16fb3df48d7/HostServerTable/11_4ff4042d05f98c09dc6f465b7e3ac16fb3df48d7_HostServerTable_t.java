 package org.jboss.as.console.client.domain.hosts;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import org.jboss.as.console.client.domain.model.Host;
 import org.jboss.as.console.client.domain.model.ServerInstance;
 import org.jboss.as.console.client.widgets.icons.ConsoleIcons;
 import org.jboss.ballroom.client.widgets.common.DefaultButton;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * A miller column based selection of host/serve combinations
  *
  * @author Heiko Braun
  * @date 12/9/11
  */
 public class HostServerTable {
 
     private static final int ESCAPE = 27;
     public final static double GOLDEN_RATIO = 1.618;
 
     private boolean isRightToLeft = false;
     private HostServerManagement presenter;
 
     private CellList<Host> hostList;
     private CellList<ServerInstance> serverList;
 
     private PopupPanel popup;
 
     private HorizontalPanel header;
     private HTML currentDisplayedValue;
     int popupWidth = -1;
     private String description = null;
     private HTML ratio;
 
     public HostServerTable(HostServerManagement presenter) {
         this.presenter = presenter;
     }
 
     public void setRightToLeft(boolean rightToLeft) {
         isRightToLeft = rightToLeft;
     }
 
     public void setPopupWidth(int popupWidth) {
         this.popupWidth = popupWidth;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Widget asWidget() {
 
         final String panelId = "popup_"+ HTMLPanel.createUniqueId();
         popup = new PopupPanel(true, true) {
 
             @Override
             protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                 if (Event.ONKEYUP == event.getTypeInt()) {
                     if (event.getNativeEvent().getKeyCode() == ESCAPE) {
                         // Dismiss when escape is pressed
                         popup.hide();
                     }
                 }
             }
 
             public void onBrowserEvent(Event event) {
                 super.onBrowserEvent(event);
             }
         };
 
         popup.getElement().setId(panelId);
         popup.setStyleName("default-popup");
 
 
         VerticalPanel layout = new VerticalPanel();
         layout.setStyleName("fill-layout-width");
         layout.addStyleName("tablepicker-popup");
 
         if(description!=null)
             layout.add(new Label(description));
 
         ratio = new HTML("RATIO HERE");
         layout.add(ratio);
         // --------------
 
         hostList = new CellList<Host>(new HostCell());
         hostList.setSelectionModel(new SingleSelectionModel<Host>());
         hostList.addStyleName("fill-layout-width");
 
         serverList = new CellList<ServerInstance>(new ServerCell());
         serverList.setSelectionModel(new SingleSelectionModel<ServerInstance>());
         serverList.addStyleName("fill-layout-width");
 
         hostList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
             @Override
             public void onSelectionChange(SelectionChangeEvent event) {
                 Host selectedHost = getSelectedHost();
                 presenter.loadServer(selectedHost);
             }
         });
 
         serverList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
             @Override
             public void onSelectionChange(SelectionChangeEvent event) {
                 ServerInstance server = getSelectedServer();
                 presenter.onServerSelected(getSelectedHost(), getSelectedServer());
                 updateDisplay();
             }
         });
 
 
         HorizontalPanel millerPanel = new HorizontalPanel();
         millerPanel.setStyleName("fill-layout");
         millerPanel.add(hostList);
         millerPanel.add(serverList);
 
         hostList.getElement().getParentElement().setAttribute("width", "50%");
         serverList.getElement().getParentElement().setAttribute("width", "50%");
 
         layout.add(millerPanel);
 
         DefaultButton doneBtn = new DefaultButton("Done", new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 popup.hide();
             }
         });
         doneBtn.getElement().setAttribute("style","float:right");
         layout.add(doneBtn);
 
 
 
         // --------------
 
 
         popup.setWidget(layout);
 
 
         // --------------
 
         currentDisplayedValue = new HTML("&nbsp;");
         currentDisplayedValue.setStyleName("table-picker-value");
 
         header = new HorizontalPanel();
         header.setStyleName("table-picker");
         header.add(currentDisplayedValue);
 
         Image img = new Image(ConsoleIcons.INSTANCE.tablePicker());
         header.add(img);
 
         currentDisplayedValue.getElement().getParentElement().setAttribute("width", "100%");
 
         img.getParent().getElement().setAttribute("width", "18");
 
         header.getElement().setAttribute("width", "100%");
         header.getElement().setAttribute("cellspacing", "0");
         header.getElement().setAttribute("cellpadding", "0");
         header.getElement().setAttribute("border", "0");
 
 
         ClickHandler clickHandler = new ClickHandler() {
             @Override
             public void onClick(ClickEvent clickEvent) {
                 openPanel();
             }
         };
 
         currentDisplayedValue.addClickHandler(clickHandler);
         img.addClickHandler(clickHandler);
 
         return header;
     }
 
     private void updateDisplay() {
         currentDisplayedValue.setHTML(         // TODO: cope with long names
                 getSelectedHost().getName() + ": "
                         + getSelectedServer().getName()
         );
     }
 
     private Host getSelectedHost() {
         return ((SingleSelectionModel<Host>) hostList.getSelectionModel()).getSelectedObject();
     }
 
     private ServerInstance getSelectedServer() {
         return ((SingleSelectionModel<ServerInstance>) serverList.getSelectionModel()).getSelectedObject();
     }
 
     private void openPanel() {
 
         int winWidth = popupWidth!=-1 ? popupWidth : header.getOffsetWidth() * 2;
         int winHeight = (int) ( winWidth / GOLDEN_RATIO );
 
         popup.setWidth(winWidth +"px");
         popup.setHeight(winHeight + "px");
 
         // right to left
         if(isRightToLeft)
         {
             int popupLeft = header.getAbsoluteLeft() - (winWidth - header.getOffsetWidth());
             popup.setPopupPosition(
                     popupLeft-15,
                     header.getAbsoluteTop()+21
             );
         }
         else
         {
             int popupLeft = header.getAbsoluteLeft();
             popup.setPopupPosition(
                     popupLeft,
                     header.getAbsoluteTop()+21
             );
         }
 
         popup.show();
 
     }
 
     public void clearSelection() {
         currentDisplayedValue.setText("");
     }
 
     /**
      * Display the currently active servers for selection
      * @param servers
      */
     public void setServer(List<ServerInstance> servers) {
 
        /*List<ServerInstance> active = new ArrayList<ServerInstance>();
         for(ServerInstance instance : servers)
             if(instance.isRunning())
                 active.add(instance);
 
        ratio.setHTML("<i>Active Server: "+active.size()+" of "+servers.size()+" instances</i>");*/
 
        serverList.setRowData(0, servers);
         if(!servers.isEmpty())
            serverList.getSelectionModel().setSelected(servers.get(0), true);
     }
 
     public void setHosts(List<Host> hosts) {
 
         ratio.setText("");
 
         hostList.setRowData(0, hosts);
         // clear when hosts are updated
         serverList.setRowData(0, Collections.EMPTY_LIST);
     }
 
     public void doBootstrap() {
         if(hostList.getRowCount()>0)
         {
             hostList.getSelectionModel().setSelected(hostList.getVisibleItem(0), true);
             presenter.loadServer(getSelectedHost());
         }
     }
 
     interface Template extends SafeHtmlTemplates {
         @Template("<div class='server-selection-host'>{0}</div>")
         SafeHtml message(String title);
     }
 
     interface ServerTemplate extends SafeHtmlTemplates {
         @Template("<div class='server-selection-server'>{0}</div>")
         SafeHtml message(String title);
     }
 
     // -----
 
     private static final Template HOST_TEMPLATE = GWT.create(Template.class);
     private static final ServerTemplate SERVER_TEMPLATE = GWT.create(ServerTemplate.class);
 
     public class HostCell extends AbstractCell<Host> {
 
         @Override
         public void render(
                 Context context,
                 Host host,
                 SafeHtmlBuilder safeHtmlBuilder)
         {
             safeHtmlBuilder.append(HOST_TEMPLATE.message(host.getName()));
         }
 
     }
 
     public class ServerCell extends AbstractCell<ServerInstance> {
 
         @Override
         public void render(
                 Context context,
                 ServerInstance server,
                 SafeHtmlBuilder safeHtmlBuilder)
         {
             safeHtmlBuilder.append(SERVER_TEMPLATE.message(server.getName()));
         }
 
     }
 }
 
 
