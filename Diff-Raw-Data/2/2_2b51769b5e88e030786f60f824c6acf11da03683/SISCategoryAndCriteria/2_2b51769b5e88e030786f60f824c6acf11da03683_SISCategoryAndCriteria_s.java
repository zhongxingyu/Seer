 package org.iucn.sis.shared.api.structures;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.utils.FormattedDate;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 @SuppressWarnings({"deprecation", "unchecked"})
 public class SISCategoryAndCriteria extends Structure<Field> {
 
 	// Piece 0: Is manual
 	// Piece 1: Version
 	// Piece 2: Manual Category
 	// Piece 3: Manual Criteria
 	// Piece 4: Auto Category
 	// Piece 5: Auto Criteria
 	// Piece 6: RLHistory Text
 	// Piece 7: PossiblyExtinct
 	// Piece 8: PossiblyExtinctCandidate
 	// Piece 9: DateLastSeen
 	// Piece 10: Category Text
 	// Piece 11: Data Deficient Reason
 
 	public static final int IS_MANUAL_INDEX = 0;
 	public static final int CRIT_VERSION_INDEX = 1;
 	public static final int MANUAL_CATEGORY_INDEX = 2;
 	public static final int MANUAL_CRITERIA_INDEX = 3;
 	public static final int GENERATED_CATEGORY_INDEX = 4;
 	public static final int GENERATED_CRITERIA_INDEX = 5;
 	public static final int RLHISTORY_TEXT_INDEX = 6;
 	public static final int POSSIBLY_EXTINCT_INDEX = 7;
 	public static final int POSSIBLY_EXTINCT_CANDIDATE_INDEX = 8;
 	public static final int DATE_LAST_SEEN_INDEX = 9;
 	public static final int CATEGORY_TEXT_INDEX = 10;
 	public static final int DATA_DEFICIENT_INDEX = 11;
 
 	public static List<String> generateDefaultDataList() {
 		List<String> rlCritData = new ArrayList<String>();
 		rlCritData.add("false");
 		rlCritData.add("0"); // Corresponds to v3.1
 		rlCritData.add("");
 		rlCritData.add("");
 		rlCritData.add("");
 		rlCritData.add("");
 		rlCritData.add("");
 		rlCritData.add("false");
 		rlCritData.add("false");
 		rlCritData.add("");
 		rlCritData.add("");
 		rlCritData.add(""); //FOR DD STRUCTURE
 		
 		return rlCritData;
 	}
 
 	private MessageBox box = null;
 	public class CustomCategoryChangeListener implements ChangeListener {
 		public void onChange(Widget sender) {
 			if( categoryListBox.getValue( categoryListBox.getSelectedIndex() ).equals("CUSTOM") ) {
 				box = MessageBox.prompt("Enter Category", "Please enter the " +
 						"category.", new Listener<MessageBoxEvent>() {
 					public void handleEvent(MessageBoxEvent be) {
 						if( !be.getButtonClicked().getText().equalsIgnoreCase("cancel")) {
 							String cat = box.getTextBox().getValue().toString();
 							categoryListBox.insertItem(cat, cat, 1);
 							categoryListBox.setSelectedIndex(1);
 						}
 						
 						box.close();
 					}
 				});
 			}
 		}
 	}
 	private CustomCategoryChangeListener customCatListener;
 	
 	// WIDGETS
 	private Label criteriaStringBox;
 	private HTML invalidCriteriaString;
 
 	//private HorizontalPanel categoryPanel;
 	private ListBox categoryListBox;
 	private VerticalPanel critAndGridPanel;
 	private CriteriaGrid v3_1Grid;
 
 	private CriteriaGrid v2_3Grid;
 	private ListBox critVersion;
 	private ArrayList<String> critVersionDisclaimerChoices;
 
 	private HTML critVersionDisclaimer;
 	private HorizontalPanel manualPanel;
 
 	private Button manualOverride;
 	private HorizontalPanel dateLastSeenPanel;
 
 	private TextBox dateLastSeen;
 	private HorizontalPanel possiblyExtinctPanel;
 	private RadioButton possiblyExtinctBox;
 	
 	private ListBox dataDeficientListBox;
 	private HorizontalPanel dataDeficientPanel;
 	
 	private RadioButton possiblyExtinctCandidateBox;
 
 	private TextArea rlText;
 
 	private TextBox categoryTextBox;
 
 	// DATA
 	private boolean isManual = false;
 	private Integer version;
 	private String manualCriteria;
 	private String manualCategory;
 	private String generatedCriteria;
 	private String generatedCategory;
 
 	//private String categoryText;
 	private ChangeListener critVersionListener;
 
 	private ChangeListener categoryListBoxListener;
 
 	private static final String[] criteriaDescriptions = new String[] { "EX - Extinct", "EW - Extinct in the Wild",
 			"CR Critically Endangered", "EN - Endangered", "VU - Vulnerable",
 			"LR/cd - Lower Risk Conservation Dependent", "LR/nt - Lower Risk Near Threatened",
 			"LR/lc - Lower Risk Least Concern", "DD - Data Deficient", "NE - Not Evaluated", "NT - Near Threatened",
 			"LC - Least Concern", "NR - Not Recognised " };
 
 	public SISCategoryAndCriteria(String struct, String descript, String structID, Object data) {
 		super(struct, descript, null, data);
 		customCatListener = new CustomCategoryChangeListener();
 		buildContentPanel(Orientation.VERTICAL);
 	}
 	
 	@Override
 	public boolean hasChanged(Field field) {
 		Field fauxParent = new Field(), fauxChild = new Field(CanonicalNames.RedListCriteria, null);
 		
 		save(fauxParent, fauxChild);
 		
 		if (field == null) {
 			boolean childHasData = fauxChild.hasData();
 			if (childHasData)
 				Debug.println("HasChanged in RLCat&Crit: DB has null value, but child hasData, there are {0} primitive fields: \n{1}", fauxChild.getPrimitiveField().size(), fauxChild.getKeyToPrimitiveFields().keySet());
 			else
 				Debug.println("HasChanged in RLCat&Crit: DB has null value, child has no data, no changes.");
 			return childHasData;
 		}
 		
 		if (field.getPrimitiveField().size() != fauxChild.getPrimitiveField().size()) {
 			return true;
 		}
 		
 		Map<String, PrimitiveField> savedFields = fauxChild.getKeyToPrimitiveFields();
 		for (Map.Entry<String, PrimitiveField> entry : savedFields.entrySet()) {
 			PrimitiveField oldPrimField = field.getPrimitiveField(entry.getKey());
 			if (oldPrimField == null) {
 				Debug.println("HasChanged in RLCat&Crit: DB missing new value for {0} of {1}", entry.getKey(), entry.getValue().getRawValue());
 				return true;
 			}
 			
 			String oldValue = oldPrimField.getRawValue();
 			if ("".equals(oldValue))
 				oldValue = null;
 			
 			String newValue = entry.getValue().getRawValue();
 			if ("".equals(newValue))
 				newValue = null;
 						
 			boolean hasChanged = false;
 			if (newValue == null) {
 				if (oldValue != null)
 					hasChanged = true;
 			} else {
 				if (oldValue == null)
 					hasChanged = true;
 				else if (!newValue.equals(oldValue))
 					hasChanged = true;
 			}
 			
 			Debug.println("HasChanged in RLCat&Crit: Interrogating {0} with DB value {1} and child value {2}, result is {3}", entry.getKey(), oldValue, newValue, hasChanged);
 			
 			if (hasChanged)
 				return hasChanged;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void save(Field parent, Field field) {
 		//TODO: IMPLEMENT ME! Woo hoo.
 		if (field == null) {
 			Debug.println("Creating new red list field to save");
 			field = new Field(CanonicalNames.RedListCriteria, null);
 			field.setParent(null);
 			//parent.addField(field);
 		}
 		else
 			Debug.println("Using field {0} with id {1} to save", field.getName(), field.getId());
 		
 		RedListCriteriaField proxy = new RedListCriteriaField(field);
 		proxy.setManual(isManual);
 		proxy.setCriteriaVersion(version);
 		proxy.setManualCategory(manualCategory);
 		proxy.setManualCriteria(manualCriteria);
 		proxy.setGeneratedCategory(generatedCategory);
 		proxy.setGeneratedCriteria(generatedCriteria);
 		proxy.setRLHistoryText(rlText.getText());
 		proxy.setPossiblyExtinct(possiblyExtinctBox.getValue());
 		proxy.setPossiblyExtinctCandidate(possiblyExtinctCandidateBox.getValue());
 		
 		Date dateLastSeenValue = null;
 		try {
 			dateLastSeenValue = FormattedDate.impl.getDate(dateLastSeen.getValue()); 
 		} catch (IllegalArgumentException e) {
 			Debug.println("RedListCriteria failed to save date last seen due to formatting error on the string {0}", dateLastSeen.getValue());
 		} catch (IndexOutOfBoundsException e) {
 			Debug.println("RedListCriteria failed to save date last seen due to formatting error on the string {0}", dateLastSeen.getValue());
 		}
 		proxy.setDateLastSeen(dateLastSeenValue);
 		proxy.setCategoryText(categoryTextBox.getText());
 		proxy.setDataDeficient(dataDeficientListBox.getValue(dataDeficientListBox.getSelectedIndex()));
 		
 		Debug.println("Saved Cat&Crit as {0}", field.toXML());
 	}
 	
 	private void buildPreCategoriesListBox() {
 		if (categoryListBox.getItemCount() == 30)
 			return;
 
 		categoryListBox.setSelectedIndex(0);
 		categoryListBox.clear();
 		categoryListBox.addChangeListener(customCatListener);
 		
 		categoryListBox.addItem("--- No Category Selected ---", "");
 		categoryListBox.addItem("Extinct (EX)", "EX");
 		categoryListBox.addItem("Extinct in the Wild (EW)", "EW");
 		categoryListBox.addItem("Critically Endangered (CR)", "CR");
 		categoryListBox.addItem("Endangered (EN)", "EN");
 		categoryListBox.addItem("Vulnerable (VU)", "VU");
 		categoryListBox.addItem("Lower Risk Conservation Dependent (LR/cd)", "LR/cd");
 		categoryListBox.addItem("Lower Risk Near Threatened (LR/nt)", "LR/nt");
 		categoryListBox.addItem("Lower Risk Least Concern (LR/lc)", "LR/lc");
 		categoryListBox.addItem("Data Deficient (DD)", "DD");
 		categoryListBox.addItem("Not Evaluated (NE)", "NE");
 		categoryListBox.addItem("Near Threatened (NT)", "NT");
 		categoryListBox.addItem("Least Concern (LC)", "LC");
 		categoryListBox.addItem("Not Recognised (NR)", "NR");
 		categoryListBox.addItem("Endangered (E)", "E");
 		categoryListBox.addItem("Vulnerable (V)", "V");
 		categoryListBox.addItem("Threatened (T)", "T");
 		categoryListBox.addItem("Rare (R)", "R");
 		categoryListBox.addItem("Indeterminate (I)", "I");
 		categoryListBox.addItem("Insufficiently Known (K)", "K");
 		categoryListBox.addItem("Insufficiently Known* (K*)", "K*");
 		categoryListBox.addItem("No Information (?)", "?");
 		categoryListBox.addItem("Not Threatened (nt)", "nt");
 		categoryListBox.addItem("Out of Danger (O)", "O");
 		categoryListBox.addItem("Abundant (A)", "A");
 		categoryListBox.addItem("Commercially Threatened (CT)", "CT");
 		categoryListBox.addItem("Threatened Community (TC)", "TC");
 		categoryListBox.addItem("Threatened Phenomenon (TP)", "TP");
 		categoryListBox.addItem("Extinct (Ex)", "Ex");
 		categoryListBox.addItem("Extinct? (Ex?)", "Ex?");
 		categoryListBox.addItem("Extinct/Endangered (Ex/E)", "Ex/E");
 		categoryListBox.addItem("Custom Category...", "CUSTOM");
 	}
 
 	private void build2_3CategoriesListBox() {
 		if (categoryListBox.getItemCount() == 14)
 			return;
 
 		categoryListBox.setSelectedIndex(0);
 		categoryListBox.clear();
 		categoryListBox.removeChangeListener(customCatListener);
 		
 		categoryListBox.addItem("--- No Category Selected ---", "");
 		categoryListBox.addItem("Extinct (EX)", "EX");
 		categoryListBox.addItem("Extinct in the Wild (EW)", "EW");
 		categoryListBox.addItem("Critically Endangered (CR)", "CR");
 		categoryListBox.addItem("Endangered (EN)", "EN");
 		categoryListBox.addItem("Vulnerable (VU)", "VU");
 		categoryListBox.addItem("Lower Risk Conservation Dependent (LR/cd)", "LR/cd");
 		categoryListBox.addItem("Lower Risk Near Threatened (LR/nt)", "LR/nt");
 		categoryListBox.addItem("Lower Risk Least Concern (LR/lc)", "LR/lc");
 		categoryListBox.addItem("Data Deficient (DD)", "DD");
 		categoryListBox.addItem("Not Evaluated (NE)", "NE");
 		categoryListBox.addItem("Near Threatened (NT)", "NT");
 		categoryListBox.addItem("Least Concern (LC)", "LC");
 		categoryListBox.addItem("Not Recognised (NR)", "NR");
 	}
 
 	private void build3_1ListBox() {
 		boolean regional = AssessmentCache.impl.getCurrentAssessment().isRegional();
 
 		if (categoryListBox.getItemCount() == (regional ? 12 : 10))
 			return;
 
 		categoryListBox.setSelectedIndex(0);
 		categoryListBox.clear();
 		categoryListBox.removeChangeListener(customCatListener);
 		
 		categoryListBox.addItem("--- No Category Selected ---", "");
 		categoryListBox.addItem("Extinct (EX)", "EX");
 		categoryListBox.addItem("Extinct in the Wild (EW)", "EW");
 		categoryListBox.addItem("Critically Endangered (CR)", "CR");
 		categoryListBox.addItem("Endangered (EN)", "EN");
 		categoryListBox.addItem("Vulnerable (VU)", "VU");
 		categoryListBox.addItem("Near Threatened (NT)", "NT");
 		categoryListBox.addItem("Least Concern (LC)", "LC");
 		categoryListBox.addItem("Data Deficient (DD)", "DD");
 		categoryListBox.addItem("Not Evaluated (NE)", "NE");
 
 		if (regional) {
 			categoryListBox.addItem("Regionally Extinct (RE)", "RE");
 			categoryListBox.addItem("Not Applicable (NA)", "NA");
 		}
 	}
 
 	private void buildCritAndGridPanel() {
 		HorizontalPanel versionPanel = new HorizontalPanel();
 		versionPanel.setSpacing(4);
 		versionPanel.add(new HTML("Criteria version: "));
 		versionPanel.add(critVersion);
 		versionPanel.add(critVersionDisclaimer);
 
 		v3_1Grid.setVisible(false);
 		v2_3Grid.setVisible(false);
 
 		critAndGridPanel = new VerticalPanel();
 		critAndGridPanel.setSpacing(4);
 		critAndGridPanel.add(versionPanel);
 		critAndGridPanel.add(v3_1Grid.getWidget());
 		critAndGridPanel.add(v2_3Grid.getWidget());
 	}
 
 	public void clearData() {
 		v3_1Grid.clearWidgets();
 		v2_3Grid.clearWidgets();
 
 		isManual = false;
 		version = Integer.valueOf(0);
 		manualCategory = "";
 		manualCriteria = "";
 		generatedCategory = "";
 		generatedCriteria = "";
 		categoryTextBox.setText("");
 		critVersion.setSelectedIndex(0);
 		categoryListBox.setSelectedIndex(0);
 		criteriaStringBox.setText("");
 		invalidCriteriaString.setHTML("");
 		rlText.setText("");
 		possiblyExtinctBox.setChecked(false);
 		possiblyExtinctCandidateBox.setChecked(false);
 		dateLastSeen.setText("");
 
 		refreshStructures();
 	}
 
 	protected Widget createLabel() {
 		clearDisplayPanel();
 
 		HorizontalPanel topPanel = new HorizontalPanel();
 		topPanel.setSpacing(8);
 		topPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 
 		topPanel.add(new HTML("Category: "));
 		topPanel.add(categoryListBox);
 		topPanel.add(categoryTextBox);
 		topPanel.add(new HTML("Criteria String: "));
 		topPanel.add(criteriaStringBox);
 		topPanel.add(invalidCriteriaString);
 
 		displayPanel.add(topPanel);
 		displayPanel.add(dateLastSeenPanel);
 		displayPanel.add(possiblyExtinctPanel);
 		displayPanel.add(dataDeficientPanel);
 		displayPanel.add(manualPanel);
 
 		//NEXT 3 CALLS MUST BE IN THIS ORDER
 		categoryListBoxListener.onChange(categoryListBox);
 		critVersionListener.onChange(critVersion);
 		refreshStructures();
 
 		return displayPanel;
 	}
 
 	protected Widget createViewOnlyLabel() {
 		refreshStructures();
 
 		return displayPanel;
 	}
 
 	public void createWidget() {
 		dateLastSeen = new TextBox();
 		dateLastSeenPanel = new HorizontalPanel();
 		dateLastSeenPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 		dateLastSeenPanel.setSpacing(6);
 		dateLastSeenPanel.add(new HTML("&nbsp&nbsp&nbsp&nbsp&nbsp"));
 		dateLastSeenPanel.add(new HTML("Date Last Seen: "));
 		dateLastSeenPanel.add(dateLastSeen);
 		dateLastSeenPanel.setVisible(false);
 
 		possiblyExtinctBox = new RadioButton("extinctGroup");
 		possiblyExtinctCandidateBox = new RadioButton("extinctGroup");
 		possiblyExtinctPanel = new HorizontalPanel();
 		possiblyExtinctPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 		possiblyExtinctPanel.setSpacing(6);
 		possiblyExtinctPanel.add(new HTML("&nbsp&nbsp&nbsp&nbsp&nbsp"));
 		possiblyExtinctPanel.add(new HTML("Possibly Extinct"));
 		possiblyExtinctPanel.add(possiblyExtinctBox);
 		possiblyExtinctPanel.add(new HTML("Possibly Extinct Candidate"));
 		possiblyExtinctPanel.add(possiblyExtinctCandidateBox);
 		possiblyExtinctPanel.setVisible(false);
 		
 		dataDeficientListBox = new ListBox(false);
 		dataDeficientListBox.addItem("", "");
 		dataDeficientListBox.addItem("None", "none");
 		dataDeficientListBox.addItem("Provenance", "provenenance");
 		dataDeficientListBox.addItem("Taxonomic", "taxonomic");
 		dataDeficientPanel = new HorizontalPanel();
 		dataDeficientPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 		dataDeficientPanel.setSpacing(6);
 		dataDeficientPanel.add(new HTML("Data Deficient reason: "));
 		dataDeficientPanel.add(dataDeficientListBox);
 //		dataDeficientPanel.setVisible(false);
 
 		critVersion = new ListBox();
 		critVersion.addItem("3.1");
 		critVersion.addItem("2.3");
 		critVersion.addItem("Earlier Version...");
 		critVersionListener = new ChangeListener() {
 			public void onChange(Widget sender) {
 				critVersionDisclaimer
 						.setHTML((String) critVersionDisclaimerChoices.get(critVersion.getSelectedIndex()));
 
 				if (critVersion.getSelectedIndex() == 2) {
 					v2_3Grid.setVisible(false);
 					v3_1Grid.setVisible(false);
 					criteriaStringBox.setText("N/A");
 					categoryTextBox.setVisible(true);
 //					build2_3CategoriesListBox();
 					buildPreCategoriesListBox();
 				} else if (critVersion.getSelectedIndex() == 1) {
 					v3_1Grid.setVisible(false);
 					v2_3Grid.setVisible(true);
 					v2_3Grid.updateCriteriaString(v2_3Grid.createCriteriaString());
 					categoryTextBox.setVisible(false);
 					build2_3CategoriesListBox();
 				} else {
 					v2_3Grid.setVisible(false);
 					v3_1Grid.setVisible(true);
 					v3_1Grid.updateCriteriaString(v3_1Grid.createCriteriaString());
 					categoryTextBox.setVisible(false);
 					build3_1ListBox();
 				}
 
 				version = critVersion.getSelectedIndex();
 				refreshStructures();
 				updateValidityOfCriteriaString();
 			}
 		};
 		critVersion.addChangeListener(critVersionListener);
 
 		critVersionDisclaimerChoices = new ArrayList();
 		critVersionDisclaimerChoices
 				.add("<span class=\"navyFont\">Current standard. Use for all new assessments!</span>");
 		critVersionDisclaimerChoices
 				.add("<span class=\"redFont\">Deprecated standard. DO NOT USE unless entering historic assessment.</span>");
 		critVersionDisclaimerChoices
 				.add("<span class=\"redFont\">Prior to standards. DO NOT USE unless entering historic assessment.</span>");
 
 		critVersionDisclaimer = new HTML("");
 
 		categoryListBox = new ListBox(false);
 		categoryListBoxListener = new ChangeListener() {
 			public void onChange(Widget sender) {
 				int index = categoryListBox.getSelectedIndex();
 				
 				String value = "";
 				if (index > -1)
 					value = categoryListBox.getValue(index);
 				
 				if (isManual)
 					manualCategory = value;
 				else
 					generatedCategory = value;
 
 				refreshStructures();
 				updateValidityOfCriteriaString();
 			}
 		};
 		categoryListBox.addChangeListener(categoryListBoxListener);
 
 		categoryTextBox = new TextBox();
 		categoryTextBox.setText("");
 		categoryTextBox.setVisible(false);
 
 		criteriaStringBox = new Label();
 		invalidCriteriaString = new HTML();
 		invalidCriteriaString.addStyleName("expert-criteria-error");
 
 		v3_1Grid = new CriteriaGrid3_1() {
 			protected void updateCriteriaString(String result) {
 				boolean isValid = isCriteriaValid(result, categoryListBox.getValue(categoryListBox.getSelectedIndex()));
 				updateResult(result, isValid);
 			}
 		};
 		v2_3Grid = new CriteriaGrid2_3() {
 			protected void updateCriteriaString(String result) {
 				boolean isValid = isCriteriaValid(result, categoryListBox.getItemText(categoryListBox
 						.getSelectedIndex()));
 				updateResult(result, isValid);
 			}
 		};
 
 		buildCritAndGridPanel();
 
 		isManual = false;
 		if (isManual)
 			manualOverride = new Button("Revert to Calculated");
 		else
 			manualOverride = new Button("Enter Manual Data");
 
 		manualOverride.addListener(Events.Select, new Listener() {
 			public void handleEvent(BaseEvent be) {
 				isManual = !isManual;
 				refreshStructures();
 			}
 		});
 
 		rlText = new TextArea();
 		rlText.setWidth("100%");
 		rlText.setHeight("200px");
 
 		VerticalPanel buttonAndHistoryText = new VerticalPanel();
 		buttonAndHistoryText.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		buttonAndHistoryText.setSpacing(10);
 		buttonAndHistoryText.add(manualOverride);
 		buttonAndHistoryText.add(new HTML("Red List History Text"));
 		buttonAndHistoryText.add(rlText);
 
 		manualPanel = new HorizontalPanel();
 		manualPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
 		manualPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		manualPanel.setSpacing(4);
 		manualPanel.add(buttonAndHistoryText);
 		manualPanel.add(critAndGridPanel);
 
 		build2_3CategoriesListBox();
 	}
 
 	/**
 	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
 	 * if it contains multiples structures, all of those, in order.
 	 */
 	public ArrayList<String> extractDescriptions() {
 		ArrayList<String> ret = new ArrayList<String>();
 		ret.add("Is manual");
 		ret.add("Version");
 		ret.add("Manual Category");
 		ret.add("Manual Criteria");
 		ret.add("Auto Category");
 		ret.add("Auto Criteria");
 		ret.add("Red List History Text");
 		ret.add("Possibly Extinct");
 		ret.add("Possibly Extinct Candidate");
 		ret.add("Date Last Seen");
 		ret.add("Category Text");
 		return ret;
 	}
 	
 	@Override
 	public List<ClassificationInfo> getClassificationInfo() {
 		return new ArrayList<ClassificationInfo>();
 	}
 
 	public String getData() {
 		return null;
 	}
 
 	/**
 	 * Pass in the raw data from an Assessment object, and this will return
 	 * it in happy, displayable String form
 	 * 
 	 * @return ArrayList of Strings, having converted the rawData to nicely
 	 *         displayable String data. Happy days!
 	 */
 	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
 		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
 		offset++;
 
 		// Version values start at 0, 0 is a meaningful value here
 		// so need to adjust by one for use with
 		// DisplayableDataHelper.toDisplayableSingleSelect
 		try {
 			Integer versionIndex = Integer.parseInt((String) rawData.get(offset)) + 1;
 			prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect(versionIndex.toString(), new Object[] {
 				"3.1", "2.3", "Unspecified Version" }));
 		} catch (NumberFormatException e) {
 			prettyData.add(offset, rawData.get(offset));
 		}
 		offset++;
 
 		// Return the full category description
 		String cat = (String) rawData.get(offset);
 		boolean found = false;
 		if( !cat.equals("") ) {
 			for (int i = 0; i < criteriaDescriptions.length; i++) {
 				if (criteriaDescriptions[i].startsWith(cat)) {
 					prettyData.add(offset, criteriaDescriptions[i]); // manual
 					// category
 					found = true;
 					break;
 				}
 			}
 		}
 		if (!found) {
 			prettyData.add(offset, cat);
 		}
 		offset++;
 
 		prettyData.add(offset, rawData.get(offset));
 		offset++;
 
 		// Return the full category description
 		cat = (String) rawData.get(offset);
 		found = false;
 		if( !cat.equals("") ) {
 			for (int i = 0; i < criteriaDescriptions.length; i++) {
 				if (criteriaDescriptions[i].startsWith(cat)) {
 					prettyData.add(offset, criteriaDescriptions[i]); // auto
 					// category
 					found = true;
 					break;
 				}
 			}
 		}
 		if (!found) {
 			prettyData.add(offset, cat);
 		}
 		offset++;
 
 		prettyData.add(offset, rawData.get(offset));
 		offset++;
 		prettyData.add(offset, rawData.get(offset));
 		offset++;
 		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
 		offset++;
 		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
 		offset++;
 		prettyData.add(offset, rawData.get(offset));
 		offset++;
 		prettyData.add(offset, rawData.get(offset));
 		offset++;
 		
 		if( rawData.size() > offset ) {
 			prettyData.add(offset, rawData.get(offset));
 			offset++;
 		}
 
 		return offset;
 	}
 
 	private void refreshStructures() {
 		// Not built yet!
 		if (critAndGridPanel == null)
 			return;
 
 		critAndGridPanel.setVisible(isManual);
 		categoryListBox.setEnabled(isManual);
 
 		if (isManual)
 			manualOverride.setText("Revert to Calculated");
 		else
 			manualOverride.setText("Enter Manual Data");
 		
 		critVersion.setSelectedIndex(version);
 
 		categoryListBox.setSelectedIndex(0);
 		String cat = isManual ? manualCategory : generatedCategory;
 		boolean found = false;
 
 		if (cat != null && !cat.equals("")) {
 			for (int i = 0; i < categoryListBox.getItemCount(); i++) {
				if (cat.trim().equalsIgnoreCase(categoryListBox.getValue(i).trim())) {
 					categoryListBox.setSelectedIndex(i);
 					found = true;
 					break;
 				}
 			}
 			if (!found) {
 				categoryListBox.addItem(cat);
 				categoryListBox.setSelectedIndex(categoryListBox.getItemCount() - 1);
 			}
 		} else {
 			possiblyExtinctPanel.setVisible(false);
 			dataDeficientPanel.setVisible(false);
 			dataDeficientListBox.setSelectedIndex(0);
 		}
 
 		criteriaStringBox.setText(isManual ? manualCriteria : generatedCriteria);
 		invalidCriteriaString.setText("");
 
 		if (critVersion.getSelectedIndex() == 0)
 			if (isManual)
 				v3_1Grid.parseCriteriaString(manualCriteria);
 			else
 				v3_1Grid.parseCriteriaString(generatedCriteria);
 
 		else if (critVersion.getSelectedIndex() == 1)
 			if (isManual)
 				v2_3Grid.parseCriteriaString(manualCriteria);
 			else
 				v2_3Grid.parseCriteriaString(generatedCriteria);
 		
 		if (categoryListBox.getItemText(categoryListBox.getSelectedIndex()).startsWith("Extinct"))
 			dateLastSeenPanel.setVisible(true);
 		else
 			dateLastSeenPanel.setVisible(false);
 
 		if (categoryListBox.getValue(categoryListBox.getSelectedIndex()).equals("CR"))
 			possiblyExtinctPanel.setVisible(true);
 		else
 			possiblyExtinctPanel.setVisible(false);
 		
 		if (categoryListBox.getValue(categoryListBox.getSelectedIndex()).equals("DD")) {
 			dataDeficientPanel.setVisible(true);
 		} else {
 			dataDeficientPanel.setVisible(false);
 			dataDeficientListBox.setSelectedIndex(0);
 		}
 	}
 	
 	@Override
 	public void setData(Field field) {
 		RedListCriteriaField proxy;
 		if (field == null)
 			proxy = new RedListCriteriaField(new Field());
 		else
 			proxy = new RedListCriteriaField(field);
 		
 		v3_1Grid.clearWidgets();
 		v2_3Grid.clearWidgets();
 
 		isManual = proxy.isManual();
 		version = proxy.getCriteriaVersion();
 		manualCategory = proxy.getManualCategory();
 		manualCriteria = proxy.getManualCriteria();
 		generatedCategory = proxy.getGeneratedCategory();
 		generatedCriteria = proxy.getGeneratedCriteria();
 		rlText.setText(proxy.getRLHistoryText());
 		possiblyExtinctBox.setValue(proxy.isPossiblyExtinct());
 		possiblyExtinctCandidateBox.setValue(proxy.isPossiblyExtinctCandidate());
 		Date date = proxy.getDateLastSeen();
 		String dateValue = "";
 		if (date != null)
 			dateValue = FormattedDate.impl.getDate(date);
 		dateLastSeen.setText(dateValue);
 		categoryTextBox.setText(proxy.getCategoryText());
 		
 		String dataDeficientText = proxy.getDataDeficient();
 		if (!"".equals(dataDeficientText))
 			for (int i = 0; i < dataDeficientListBox.getItemCount(); i++) {
 				if (dataDeficientListBox.getValue(i).equalsIgnoreCase(dataDeficientText)) {
 					dataDeficientListBox.setSelectedIndex(i);
 					break;
 				}
 			}
 
 		refreshStructures();
 	}
 
 	public void setEnabled(boolean isEnabled) {
 	}
 
 	private void updateResult(String criteriaString, boolean isValidString) {
 		criteriaStringBox.setText(criteriaString);
 		manualCriteria = criteriaString;
 		updateValidityOfCriteriaString();
 
 	}
 
 	private void updateValidityOfCriteriaString() {
 		// String criteriaString = criteriaStringBox.getText();
 		// boolean isValid;
 		// if (critVersion.getSelectedIndex() == 2) {
 		// isValid = true;
 		// } else if (critVersion.getSelectedIndex() == 1) {
 		// // isValid = v2_3Grid.isCriteriaValid(criteriaString,
 		// categoryListBox.getValue(categoryListBox
 		// // .getSelectedIndex()));
 		// isValid = true;
 		// } else {
 		// isValid = v3_1Grid.isCriteriaValid(criteriaString,
 		// categoryListBox.getValue(categoryListBox
 		// .getSelectedIndex()));
 		// }
 		//
 		// if (!isValid) {
 		// invalidCriteriaString.setHTML("Criteria String is Invalid for Category");
 		// } else
 		// invalidCriteriaString.setText("");
 	}
 }
