 /**
  * 
  */
 package org.savara.sam.web.client.view;
 
 import org.savara.sam.web.client.presenter.SituationLayoutPresenter;
 import org.savara.sam.web.client.presenter.SituationLayoutPresenter.SituationLayoutView;
 
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.ViewImpl;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
 import com.smartgwt.client.data.Criteria;
 import com.smartgwt.client.data.DataSource;
 import com.smartgwt.client.data.Record;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 import com.smartgwt.client.widgets.toolbar.ToolStrip;
 import com.smartgwt.client.widgets.toolbar.ToolStripButton;
 import com.smartgwt.client.widgets.viewer.DetailViewer;
 
 /**
  * @author Jeff Yu
  * @date Nov 17, 2011
  */
 public class SituationLayoutViewImpl extends ViewImpl implements SituationLayoutView {
 
 	private SituationLayoutPresenter presenter;
 		
 	private VLayout panel;
 	
 	private ListGrid notificationList;
 		
 	@Inject
 	public SituationLayoutViewImpl() {
         
 		panel  = LayoutUtil.getPagePanel();
 		panel.addMember(LayoutUtil.getHeaderLayout());
 		
 		HLayout body = new HLayout();
 		body.setWidth100();
 		body.setPadding(3);
 		body.setHeight(850);
 		panel.addMember(body);
 				
 		body.addMember(LayoutUtil.getMenuStack());
 		
 		VLayout main = new VLayout();
 		main.setMargin(5);
 		body.addMember(main);
 		
 		main.addMember(getNotificationList());
         panel.addMember(LayoutUtil.getFooterLayout());
         
 	}
 
 
 	private VLayout getNotificationList() {
 		
 		VLayout situationList = new VLayout();
 		situationList.setWidth100();
 		situationList.setHeight100();
 		
 		ToolStrip situationTS = new ToolStrip();
 		situationTS.setWidth100();
 		
 		final DataSource situationDS = new SituationDataSource();
 		
 		ToolStripButton refresh = new ToolStripButton("Refresh", "[SKIN]/headerIcons/refresh.png");
 		refresh.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event) {
				notificationList.invalidateCache();
 				notificationList.redraw();			
 			}			
 		});
 		situationTS.addButton(refresh);
 		
         notificationList = new ListGrid(){
                 @Override  
                 protected Canvas getCellHoverComponent(Record record, Integer rowNum, Integer colNum) {
                     DetailViewer detailViewer = new DetailViewer();  
                     detailViewer.setWidth(400);  
                     detailViewer.setDataSource(situationDS);  
                     Criteria criteria = new Criteria();  
                     criteria.addCriteria("rowNum", rowNum);  
                     detailViewer.fetchData(criteria);  
       
                     return detailViewer;  
                 }
                 
                 @Override
                 protected String getBaseStyle(ListGridRecord record, int rowNum, int colNum) {
                     String severity = record.getAttributeAsString("severity");
                 	if ("Major".equalsIgnoreCase(severity)) { 
                         return "majorSituation";  
                     } else if ("Critical".equalsIgnoreCase(severity)) {
                     	return "criticalSituation";
                     } else {
                         return super.getBaseStyle(record, rowNum, colNum);  
                     }  
                 }
       
         };  
         notificationList.setWidth100();
         notificationList.setHeight100();
         notificationList.setShowAllRecords(true);  
         notificationList.setCellHeight(22);  
         
         notificationList.setDataSource(situationDS);
         
         notificationList.setCanEdit(true);        
         notificationList.setShowFilterEditor(true);
         notificationList.setAutoFetchData(true);
         notificationList.setFilterOnKeypress(true);
         
         notificationList.setCanHover(true);
         notificationList.setShowHover(true);
         notificationList.setShowHoverComponents(true);
         notificationList.setBackgroundColor("red");
         
         
         situationList.addMember(situationTS);
         situationList.addMember(notificationList);
         
         notificationList.draw();
         return situationList;
 	}
 	
 	
 	public void refreshData(ListGridRecord[] data) {
 		
 	}
 	
 	public void setPresenter(SituationLayoutPresenter presenter) {
 		this.presenter = presenter;
 	}
 	
 	
 	public Widget asWidget() {
 		return panel;
 	}
 
 }
