 package com.aavu.client.gui.gadgets;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import com.aavu.client.async.StdAsyncCallback;
 import com.aavu.client.domain.HippoLocation;
 import com.aavu.client.domain.Meta;
 import com.aavu.client.domain.MetaLocation;
 import com.aavu.client.domain.Topic;
 import com.aavu.client.domain.commands.SaveMetaLocationCommand;
 import com.aavu.client.domain.dto.LocationDTO;
 import com.aavu.client.gui.ext.TooltipListener;
 import com.aavu.client.gui.maps.HippoMapWidget;
 import com.aavu.client.gui.maps.MapController;
 import com.aavu.client.service.Manager;
 import com.aavu.client.strings.ConstHolder;
 import com.aavu.client.widget.DeleteButton;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.mapitz.gwt.googleMaps.client.GLatLng;
 import com.mapitz.gwt.googleMaps.client.GMarker;
 
 /**
  * Google Map Gadget.
  * 
  * @author Jeff Dwyer
  *
  */
 public class MapGadget extends MetaGadget implements TopicLoader, MapController {
 	
 	/**
 	 * store this because we need to center AFTER load() & setVisible() or we'll
 	 * end up just 'top-lefting' bc we center when size == (0,0)
 	 */
 	private HippoLocation centerLoc;		
 	
 	/**
 	 * 
 	 * @author Jeff Dwyer
 	 *
 	 */
 	private class LocationLabel extends Label implements ClickListener{
 
 		public LocationLabel(HippoLocation mv) {
 			
 			setText(mv.toShortString());	
 			
 			addClickListener(this);
 		}
 
 		public void onClick(Widget sender) {
 			
 			//mapWidget.putInChooseMode();
 		}		
 	}
 
 	private class MetaW extends VerticalPanel implements ClickListener {
 
 		private MetaLocation meta;
 		private Label title;
 		
 
 		public MetaW(MetaLocation _meta, boolean showDelete) {
 			this.meta = _meta;
 			
 			Set locations = myTopic.getMetaValuesFor(meta);
 			
 			
 			HorizontalPanel titleP = new HorizontalPanel();
 			
 			title = new Label(meta.getTitle());
 			title.addClickListener(this);
 			titleP.add(title);
 
 			if(showDelete){
 				DeleteButton deleteButton = new DeleteButton();
 				deleteButton.addClickListener(new ClickListener(){
 					public void onClick(Widget sender) {
 						removeMeta(meta,myTopic,MetaW.this);
 					}
 				});
 				titleP.add(deleteButton);
 			}
 						
 			add(titleP);
 			
 			System.out.println("LOAD "+locations.size());
 						
 			for (Iterator iter = locations.iterator(); iter.hasNext();) {
 				HippoLocation location = (HippoLocation) iter.next();
 				
 				System.out.println("LOAD");
 				
 				add(new LocationLabel(location));
 				
 				LocationDTO locObj = new LocationDTO(myTopic.getIdentifier(),location,meta);
 				
 				mapWidget.add(locObj,false);
 				
 				centerLoc = location;
 
 			}
 		}
 
 		public void onClick(Widget sender) {
 			selectedMeta = meta;
 			//title.add
 		}
 		
 	}
 	
 	private static final int DEFAULT_ZOOM = 1;	
 	private MetaLocation selectedMeta;
 
 	private Topic myTopic;
 	
 	
 	private Manager manager;
 	
 	
 	
 	private HippoMapWidget mapWidget;
 
 
 	public MapGadget(Manager _manager) {		
 		super(_manager, ConstHolder.myConstants.gadget_map_title(), new MetaLocation());
 		
 		this.manager = _manager;
 		    
 		
 		mapWidget = new HippoMapWidget(this,200,200,DEFAULT_ZOOM);		
 		
 		
 		extraPanel.add(mapWidget);
 	}
 
 
 
 
 	//@Override
 	protected void addMWidg(final Meta meta, boolean showDelete) {
 		
 		makeVisible();
 		
 		
 		MetaW metaWidge = new MetaW((MetaLocation)meta,showDelete);		
 		
 		metasPanel.add(metaWidge);
 		
 		selectedMeta = (MetaLocation) meta;
 		
 		//HippoLocation mv = (HippoLocation) myTopic.getSingleMetaValueFor(meta);
 		
 	}
 	
 
 	public LocationDTO getNewLocationForPoint(GLatLng point) {
 		
 		if(selectedMeta != null){
 			
 			if(myTopic.getMetaValuesFor(selectedMeta).size() > 0){
 				manager.displayInfo(ConstHolder.myConstants.gadget_map_onlyoneper());
 				return null;
 			}
 			
 			HippoLocation newLoc = new HippoLocation();
 			newLoc.setLocation(point);
 			
 			saveLocation(selectedMeta,newLoc);			
 			
 			LocationDTO locObj = new LocationDTO(myTopic.getIdentifier(),newLoc,selectedMeta);
 			
 			
 			return locObj;
 		}else{
 			manager.displayInfo(ConstHolder.myConstants.gadget_map_clickToAdd());			
 			return null;
 		}
 	}
 
 	//@Override
 	public Image getPickerButton() {		
 		Image b = ConstHolder.images.gadgetMap().createImage();
 		b.addMouseListener(new TooltipListener(0,40,ConstHolder.myConstants.gadget_map_title()));
 		return b;
 	}
 	
 	public int load(Topic topic){
 		myTopic = topic;
 		mapWidget.clear();
 		
 		int size = super.load(topic);		
 		
 		System.out.println("SIZE "+size);
 		
 		if(size > 0){			
 			makeVisible();
 		}else{
 			//extraPanel.setVisible(false);
 		}
 		
 		return size;
 	}
 
 	
 	//@Override
	public void nowVisible() {		
 		center();
 	}
 
 
 
 
 	/**
 	 * separate this because we need to center AFTER load() & setVisible() or we'll
 	 * end up just 'top-lefting' bc we center when size == (0,0)
 	 */
 	private void center() {
 		
 //		System.out.println("about to center "+mapWidget.getOffsetWidth());
 //		System.out.println("about to center "+mapWidget.isVisible());
 		
 		mapWidget.centerOn(centerLoc);
 	}
 
 
 
 
 	private void makeVisible() {
 		mapWidget.setSize(200);
 		mapWidget.setVisible(true);
 		extraPanel.setVisible(true);
 	}
 
 	private void saveLocation(Meta theSelectedMeta, HippoLocation newLoc) {
 
 		System.out.println("\n\n--------------------\n\nSave "+newLoc);
 		
 		Set locations = myTopic.getMetaValuesFor(theSelectedMeta);		
 		
 		System.out.println("bf Saving locations size "+locations.size());
 		
 		locations.add(newLoc);
 		
 		System.out.println("af Saving locations size "+locations.size());
 		for (Iterator iter = locations.iterator(); iter.hasNext();) {			
 			HippoLocation hl = (HippoLocation) iter.next();
 			System.out.println("hl "+hl);
 		}
 		
 		manager.getTopicCache().executeCommand(myTopic,new SaveMetaLocationCommand(myTopic,theSelectedMeta,locations),
 				new StdAsyncCallback(ConstHolder.myConstants.save()){});
 
 	
 	}
 
 
 
 	public void update(LocationDTO dragged) {
 		System.out.println("Update "+dragged+" selectedMeta "+selectedMeta);
 		Set locations = myTopic.getMetaValuesFor(selectedMeta);		
 		locations.add(dragged.getLocation());		
 		
 		System.out.println("Updating locations size "+locations.size());
 		for (Iterator iter = locations.iterator(); iter.hasNext();) {
 			HippoLocation hl = (HippoLocation) iter.next();
 			System.out.println("hl "+hl);
 		}
 		
 		manager.getTopicCache().executeCommand(myTopic,new SaveMetaLocationCommand(myTopic,selectedMeta,locations),
 				new StdAsyncCallback(ConstHolder.myConstants.save()){});
 
 	}
 
 
 
 	public void userDoubleClicked(LocationDTO selected) {
 		System.out.println("selected "+selected);
 		
 	}
 
 
 
 	public void userSelected(LocationDTO selected, GMarker marker) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 	/**
 	 * NOTE: Only necessary for IE. Without this, the map will load and display, but it 
 	 * won't play nicely with the other gadgets, in particular any EntryPreview with more than a few
 	 * lines of text in it. FF worked fine.
 	 */
 	//@Override
 	public void showForFirstTime() {
 		super.showForFirstTime();
 		makeVisible();
 	}
 
 
 }
