 package org.iucn.sis.client.panels.filters;
 
 import java.util.Map.Entry;
 
 import org.iucn.sis.client.api.ui.models.region.RegionModel;
 import org.iucn.sis.client.panels.region.AddRegionPanel;
 import org.iucn.sis.shared.api.models.AssessmentFilter;
 import org.iucn.sis.shared.api.models.Relationship;
 
 import com.extjs.gxt.ui.client.Style.VerticalAlignment;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.user.client.ui.HTML;
 
 public class AssessmentFilterRegionalPanel extends LayoutContainer {
 
 	protected boolean addDescriptions;
 	protected CheckBox allCheck; 
 	protected AssessmentFilter filter;
 	protected AddRegionPanel regionPanel;
 	protected SimpleComboBox<String> box;	
 	protected boolean allowAllRegions;
 	protected boolean allowMatchesAnyRegion;
 	
 	public final static String all = "all selected locales";
 	public final static String or = "any selected locale";
 
 	public AssessmentFilterRegionalPanel(boolean allowAllRegions, AssessmentFilter filter, boolean allowMatchesAnyRegion) {
 		this.allowAllRegions = allowAllRegions;
 		this.filter = filter;
 		this.allowMatchesAnyRegion = allowMatchesAnyRegion;
		System.out.println("allowed to match any region " + allowMatchesAnyRegion);
 
 	}
 
 	public void setFilter(AssessmentFilter filter) {
 		regionPanel.clearData();
 		regionPanel.setRegionsSelected(filter.getRegionIDsCSV());
 		regionPanel.refreshUI();
		if (allowMatchesAnyRegion && filter.getRegionType().equalsIgnoreCase(Relationship.OR))
 			box.setSimpleValue(or);
		else {
 			box.setSimpleValue(all);
 		}
 		box.setEnabled(allowMatchesAnyRegion);
 		
 			
 		if (allowAllRegions) {
 			allCheck.setValue(filter.isAllRegions());
 		}
 		layout();
 	}
 
 	public void draw() {
 
 		regionPanel = new AddRegionPanel();			
 		regionPanel.draw();
 		box = new SimpleComboBox<String>();
 		box.add(all);
 		box.add(or);
 		box.setVisible(allowMatchesAnyRegion);
 
 		HorizontalPanel boxPanel = new HorizontalPanel();
 		boxPanel.add(new HTML("Locale must match: "));
 		boxPanel.add(box);
 		boxPanel.setVerticalAlign(VerticalAlignment.MIDDLE);
 
 		RowLayout layout = new RowLayout();
 		setLayout(layout);
 		RowData data = new RowData();
 		data.setMargins(new Margins(4,0,4,0));
 
 		if (allowAllRegions) {
 			allCheck = new CheckBox();
 			allCheck.setBoxLabel("All locales");
 			allCheck.addListener(Events.Change, new Listener<FieldEvent>() {
 
 				public void handleEvent(FieldEvent be) {
 					if ((Boolean) be.getValue()) {
 						regionPanel.refreshUI();
 					}
 
 					regionPanel.setEnabled(!(Boolean)be.getValue());	
 					box.setEnabled(!(Boolean)be.getValue());
 				}
 			});
 			HorizontalPanel allRegionPanel = new HorizontalPanel();
 			allRegionPanel.setVerticalAlign(VerticalAlignment.BOTTOM);
 			allRegionPanel.add(allCheck);
 			allRegionPanel.add(new HTML("<b>or select specific locales</b>"));			
 			add(allRegionPanel, data);
 		}
 
 
 		add(boxPanel, data);
 		RowData data2 = new RowData();
 		data2.setMargins(new Margins(4,10, 4, 93));
 		add(regionPanel, data2);			
 		setFilter(this.filter);
 
 
 	}
 
 
 	public void addDescriptions(boolean addDescriptions) {
 		this.addDescriptions = addDescriptions;
 	}
 
 	public void putIntoAssessmentFilter(AssessmentFilter filter) {
 		if (allCheck!=null && allCheck.getValue())
 		{
 			filter.setAllRegions();
 		}
 		else
 		{
 			filter.getRegionIds().clear();
 
 			for (Entry<ComboBox<RegionModel>, RegionModel> entry : regionPanel.getBoxesToSelected().entrySet())
 			{ 
 				if (entry != null)
 				{
 					entry.getValue().sinkModelDataIntoRegion();
 					filter.getRegionIds().add(entry.getValue().getRegion().getId());
 				}	
 			}
 			
 			if (box.getSimpleValue().equalsIgnoreCase(all)) {
 				filter.setRegionType(Relationship.AND);
 				System.out.println("setting to and in filter " + filter );
 			}
 			else {
 				filter.setRegionType(Relationship.OR);
 				System.out.println("setting to or in filter " + filter );
 			}
 		}
 
 	}
 
 
 	public boolean anyRegionSelected() {
 
 		
 		if (allCheck != null) {
 			return !regionPanel.getSelectedRegions().isEmpty() || allCheck.getValue();
 		} else {
 			return !regionPanel.getSelectedRegions().isEmpty();
 		}
 		
 	}
 
 
 	@Override
 		public void setEnabled(boolean enabled) {
 			super.setEnabled(enabled);
 			if (enabled && !allowMatchesAnyRegion) {
 				box.setEnabled(false);
 			}
 		}
 
 }
