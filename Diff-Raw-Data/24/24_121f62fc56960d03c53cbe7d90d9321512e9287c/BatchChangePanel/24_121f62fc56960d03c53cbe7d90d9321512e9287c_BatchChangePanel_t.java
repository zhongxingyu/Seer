 package org.iucn.sis.client.panels.header;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.SchemaCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.filters.AssessmentFilterPanel;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableDraftAssessment;
 import org.iucn.sis.shared.api.acl.feature.AuthorizablePublishedAssessment;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentFilter;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Relationship;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 import org.iucn.sis.shared.api.utils.XMLUtils;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.binder.DataListBinder;
 import com.extjs.gxt.ui.client.data.BaseModel;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.Store;
 import com.extjs.gxt.ui.client.store.StoreFilter;
 import com.extjs.gxt.ui.client.widget.DataList;
 import com.extjs.gxt.ui.client.widget.DataListItem;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.extjs.client.ViewerFilterCheckBox;
 import com.solertium.util.extjs.client.ViewerFilterTextBox;
 import com.solertium.util.extjs.client.WindowUtils;
 
 /**
  * This panel allows the user to instigate a batch change of assessments, based
  * on data in a "template" assessment.
  * 
  * @author adam.schwartz
  */
 @SuppressWarnings("deprecation")
 public class BatchChangePanel extends LayoutContainer {
 	/**
 	 * Represents a field in a MyGWT Model
 	 */
 	public class FieldListElement extends BaseModel {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		String name;
 		boolean hasData;
 
 		public FieldListElement() {
 			super();
 		}
 
 		public FieldListElement(String name, boolean hasData) {
 			this.name = name;
 			this.hasData = hasData;
 			set("name", name);
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public boolean hasData() {
 			return hasData;
 		}
 	}
 
 	private Button submit;
 	private CheckBox overwrite;
 
 	private CheckBox append;
 	private ListBox workingSets;
 
 	//	private ListBox types;
 	private AssessmentFilterPanel filterPanel;
 	
 	private DataList fieldsToUse;
 	private ListStore<FieldListElement> fieldStore;
 	private DataListBinder<FieldListElement> binder;
 	private StoreFilter<FieldListElement> fieldFilter;
 	private ViewerFilterTextBox<FieldListElement> fieldsFilterBox;
 
 	private ViewerFilterCheckBox<FieldListElement> filterShowEmptyFields;
 
 	private Assessment template;
 
 	private boolean built;
 	private LayoutContainer disclaimer;
 	private LayoutContainer containerFieldList;
 	private LayoutContainer containerTypeChooser;
 
 
 	public BatchChangePanel() {
 		built = false;
 
 	}
 
 	private void build() {
 		fieldsToUse = new DataList();
 		fieldsToUse.setCheckable(true);
 		fieldsToUse.setScrollMode(Scroll.AUTO);
 
 		fieldStore = new ListStore<FieldListElement>();
 
 		binder = new DataListBinder<FieldListElement>(fieldsToUse, fieldStore);
 		binder.setDisplayProperty("name");
 
 		fieldsFilterBox = new ViewerFilterTextBox<FieldListElement>();
 		fieldsFilterBox.bind(fieldStore);
 
 		fieldFilter = new StoreFilter<FieldListElement>() {
 			public boolean select(Store<FieldListElement> store, FieldListElement parent, FieldListElement item, String property) {
 				if ((!filterShowEmptyFields.isChecked()) && !(item).hasData())
 					return false;
 
 				String txt = fieldsFilterBox.getText();
 				if (txt != null && !txt.equals("")) {
 					String elementString = (item).getName().toLowerCase();
 					return elementString.indexOf(txt.toLowerCase()) > -1;
 				}
 				return true;
 			}
 		};
 		fieldStore.addFilter(fieldFilter);
 
 		filterShowEmptyFields = new ViewerFilterCheckBox<FieldListElement>();
 		filterShowEmptyFields.bind(fieldStore);
 
 		filterPanel = new AssessmentFilterPanel(new AssessmentFilter(), false, false, true, false);
 		filterPanel.setEnabled(false);
 
 		workingSets = new ListBox(false);
 		workingSets.addChangeListener(new ChangeListener() {
 			public void onChange(Widget sender) {
 				if (workingSets.getSelectedIndex() == 0) {
 					submit.setEnabled(false);
 					filterPanel.setEnabled(false);
 				}
 				else {
 					submit.setEnabled(true);
 					AssessmentFilter filter = WorkingSetCache.impl.getWorkingSet(Integer.valueOf(workingSets.getValue(workingSets.getSelectedIndex()))).getFilter();
 					filterPanel.setFilter(filter);
 					filterPanel.setEnabled(true);
 				}
 			}
 		});
 
 		submit = new Button("Submit Batch Change Request");
 		submit.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				onSubmit();
 			}
 		});
 		submit.setEnabled(false);
 
 		overwrite = new CheckBox();
 		append = new CheckBox();
 
 		disclaimer = new LayoutContainer();
 		disclaimer.add(new HTML("<b>Instructions:</b> The current assessment is used as the "
 				+ "template for mass data updates. Choose the fields to use from the current "
 				+ "assessment from the list on the left. Choose the destination assessments using "
 				+ "the drop downs on the right; they are organized by working set. <b>NOTE:</b> "
 				+ "Some fields are not stored in every assessment, whereas some fields may be "
 				+ "saved as having data, though it is \"empty\" data. Fields that are not stored "
 				+ "in an assessment but are selected to be used in the update will result in "
 				+ "removing the field from the destination assessment, and not storing " + "\"empty\" data."));
 
 		containerFieldList = new LayoutContainer();
 		RowLayout fieldLayout = new RowLayout();
 		fieldLayout.setOrientation(Orientation.VERTICAL);
 		containerFieldList.setLayout(fieldLayout);
 		containerFieldList.add(new HTML("<u>Choose Source Data from Current Assessment</u>"), new RowData(1, 25));
 //		containerFieldList.add(wrapInHorPanel("Show Fields With No Data", filterShowEmptyFields));
 		containerFieldList.add(wrapInHorPanel("Filter By Name", fieldsFilterBox));
 		containerFieldList.add(fieldsToUse, new RowData(1, 1));
 
 		containerTypeChooser = new LayoutContainer();
 		FlowLayout innerLayout = new FlowLayout();
 		containerTypeChooser.setLayout(innerLayout);
 		containerTypeChooser.add(new HTML("<u>Choose Destination Assessments</u>"));
 
 		containerTypeChooser.add(wrapInHorPanel("Select a Working Set", workingSets));
 		containerTypeChooser.add(wrapInHorPanel("Select assessment scope", filterPanel));
 		containerTypeChooser.add(wrapInHorPanel("Overwrite Existing Data", overwrite));
 		containerTypeChooser.add(wrapInHorPanel("Append (for Narrative fields ONLY)", append));
 		containerTypeChooser.add(submit);
 
 		setLayout(new RowLayout());
 		com.extjs.gxt.ui.client.widget.HorizontalPanel innerPanel = new com.extjs.gxt.ui.client.widget.HorizontalPanel();
 		innerPanel.add(containerFieldList);
 		containerFieldList.setWidth(350);
 		containerFieldList.setHeight(440);
 		innerPanel.add(containerTypeChooser);
 		containerTypeChooser.setWidth("100%");
 		containerTypeChooser.setHeight("100%");
 		add(disclaimer, new RowData(1, -1));
 		add(innerPanel, new RowData(1, 1));
 
 		built = true;
 	}
 
 	private void onSubmit() {
 		final Map<String, Field> newData = new HashMap<String, Field>();
 		DataListItem[] fields = fieldsToUse.getChecked().toArray(new DataListItem[0]);
 
 		if (fields.length == 0) {
 			WindowUtils.errorAlert("Error", "Please select at least one data field.");
 		} else {
 			WindowUtils.showLoadingAlert("Processing batch change request...");
 
 			for (int i = 0; i < fields.length; i++)
 				if (template.getField(fields[i].getText()) != null)
 					newData.put(fields[i].getText(), template.getField(fields[i].getText()));
 				else
 					newData.put(fields[i].getText(), null);
 
 			final WorkingSet ws = WorkingSetCache.impl.getWorkingSet(
 					Integer.valueOf(workingSets.getValue(workingSets.getSelectedIndex())));
 
 			startUpdates(newData, ws);
 		}
 	}
 
 	/**
 	 * This will redraw the panel, repopulating the appropriate lists et. al.
 	 */
 	public void refresh() {
 		if (!built)
 			build();
 
 		filterShowEmptyFields.setChecked(true);
 		template = AssessmentCache.impl.getCurrentAssessment();
 		fieldStore.removeAll();
 		for (int i = 0; i < CanonicalNames.batchChangeSubset.length; i++) {
 			String name = CanonicalNames.batchChangeSubset[i];
 			if (!CanonicalNames.batchChangeSubset[i].equals(CanonicalNames.RegionInformation))
 				fieldStore.add(new FieldListElement(name, template.getField(
 						CanonicalNames.batchChangeSubset[i]) != null ));
 		}
 
 		filterShowEmptyFields.setChecked(false);
 		fieldStore.applyFilters("");
 		workingSets.clear();
 		workingSets.addItem("", "");
 		for (Iterator<WorkingSet> iter = WorkingSetCache.impl.getWorkingSets().values().iterator(); iter.hasNext();) {
 			WorkingSet curWS = (WorkingSet) iter.next();
 			workingSets.addItem(curWS.getWorkingSetName(), curWS.getId()+"");
 		}
 	}
 
 	private void startUpdates(final Map<String, Field> newData, final WorkingSet ws) {
 
 		final AssessmentFilter filter = filterPanel.getFilter();
 
 		if (filter.getRegionType().equalsIgnoreCase(Relationship.ALL) || filter.getRegionType().equalsIgnoreCase(Relationship.OR))
 		{
 			WindowUtils.hideLoadingAlert();
 			WindowUtils.errorAlert("Unable to perform operations on working sets that don't have an exact region match.");
 			return;
 		}
 
 		TaxonomyCache.impl.fetchList(ws.getSpeciesIDs(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				WindowUtils.hideLoadingAlert();
 				WindowUtils.errorAlert("Error!", "Error fetching taxa in "
 						+ "selected working set. Please check your connection "
 						+ "or local SIS server status, and try again.");
 			}
 
 			public void onSuccess(String arg0) {
 				String taxaIDs = null;
 				if (filter.isAllPublished() || filter.isRecentPublished())
 				{
 					taxaIDs = "";
 					for (Taxon curTaxa : ws.getSpecies()) {
 						for (Integer region : filter.listRegionIDs()) {
 							if(!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, new AuthorizablePublishedAssessment(curTaxa, region+"")) ) {
 								WindowUtils.hideLoadingAlert();
 								WindowUtils.errorAlert("Unauthorized!", "You are unauthorized to modify " +
 										"published assessments for at least the taxon " + curTaxa.getFullName() +
 								". This operation has been cancelled.");
 								return;
 							} 
 
 						}
 						taxaIDs += curTaxa.getId() + ",";
 					}
 					taxaIDs = taxaIDs.substring(0, taxaIDs.length()-1);
 				}
 
 
 				if (filter.isDraft()) 
 				{
 					taxaIDs = "";
 					for (Taxon curTaxa : ws.getSpecies()) {
 						for (Integer region : filter.listRegionIDs())
							if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, 
									new AuthorizableDraftAssessment(curTaxa, SchemaCache.impl.getDefaultSchema(), region+"")))
 							{	
 								WindowUtils.hideLoadingAlert();
 								WindowUtils.errorAlert("Unauthorized!", "You are unauthorized to modify " +
 										"draft assessments for at least the taxon " + curTaxa.getFullName() +
 								". This operation has been cancelled.");
 								return;
 							}
 						taxaIDs += curTaxa.getId() + ",";
 					}
 					taxaIDs = taxaIDs.substring(0, taxaIDs.length()-1);
 				}
 
 				if (taxaIDs != null) {
 					
 					StringBuffer fieldXML = new StringBuffer("<fields>");
 					for( Entry<String, Field> curEntry : newData.entrySet() )
 						fieldXML.append("<field>" + curEntry.getValue().getName() + "</field>");
 					fieldXML.append("</fields>");
 					
 					StringBuffer xml = new StringBuffer("<batchChange>\n");
 					xml.append("<assessment>" + template.getId() + "</assessment>");
 					xml.append(filter.toXML());
 					xml.append(fieldXML.toString());
 					xml.append("<taxa>" + taxaIDs + "</taxa>");
 					xml.append("<append>" + append.isChecked() + "</append>\n");
 					xml.append("<overwrite>" + overwrite.isChecked() + "</overwrite>\n");
 					xml.append("</batchChange>\n");
 
 					submit.setEnabled(false);
 					final NativeDocument batchAway = SimpleSISClient.getHttpBasicNativeDocument();
 					batchAway.post(UriBase.getInstance().getBatchChangeBase() +"/batchChange", xml.toString(), new GenericCallback<String>() {
 						public void onFailure(Throwable arg0) {
 							WindowUtils.hideLoadingAlert();
 							WindowUtils.errorAlert("Failure!", "Batch change process failed.");
 							submit.setEnabled(true);
 						}
 
 						public void onSuccess(String arg0) {
 							StringBuffer summaryHTML = new StringBuffer();
 							NativeNodeList assessments = batchAway.getDocumentElement().getElementsByTagName("change");
 							if (assessments.getLength() == 0) {
 								summaryHTML.append("No assessments were updated.");
 							} else {
 								for (int i = 0; i < assessments.getLength(); i++) {
 									NativeElement node = assessments.elementAt(i);
 									summaryHTML.append(node.getText() + " assessment, ");
 								}
 							}
 							AssessmentCache.impl.clear();
 							//AssessmentCache.impl.resetCurrentAssessment();
 							StateManager.impl.setAssessment(null);
 
 							WindowUtils.hideLoadingAlert();
 							LayoutContainer innerContainer = new LayoutContainer();
 							innerContainer.setScrollMode(Scroll.AUTO);
 							VerticalPanel innerPanel = new VerticalPanel();
 							innerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
 							innerPanel.add(new HTML("<u>The following assessments were changed:</u><br/>" + XMLUtils.cleanFromXML(summaryHTML.substring(0, summaryHTML.length()))));
 							innerContainer.add(innerPanel);
 
 							final Dialog d = new Dialog();
 							d.setHeading("Success!");
 							RowLayout layout = new RowLayout(Orientation.VERTICAL);
 							d.setLayout(layout);
 							d.add(new HTML("<b>Batch changes were successful.</b>"), new RowData(1, 25));
 							d.add(innerContainer, new RowData(1, 1));
 							d.setHideOnButtonClick(true);
 							d.show();
 							d.setSize(400, 400);
 							d.center();
 							submit.setEnabled(true);
 						}
 					});
 				} else
 					WindowUtils.errorAlert("Error!", "There were no assessments "
 							+ "to be modified in your selected working set "
 							+ "of the selected type. Please try again.");
 			}
 		});
 	}
 
 	private HorizontalPanel wrapInHorPanel(String label, Widget widget) {
 		HorizontalPanel panel = new HorizontalPanel();
 		panel.setSpacing(6);
 		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
 		panel.add(new HTML(label + ": "));
 		panel.add(widget);
 
 		return panel;
 	}
 }
