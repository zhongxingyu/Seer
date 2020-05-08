 package org.snaplogic.snap.gooddata;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.snaplogic.cc.Capabilities;
 import org.snaplogic.cc.Capability;
 import org.snaplogic.cc.ComponentContainer;
 import org.snaplogic.cc.InputView;
 import org.snaplogic.cc.OutputView;
 import org.snaplogic.cc.prop.DictProp;
 import org.snaplogic.cc.prop.ListProp;
 import org.snaplogic.cc.prop.ListPropErr;
 import org.snaplogic.cc.prop.SimpleProp;
 import org.snaplogic.cc.prop.SimpleProp.SimplePropType;
 import org.snaplogic.common.ComponentResourceErr;
 import org.snaplogic.common.Field;
 import org.snaplogic.common.SnapHttpLib;
 import org.snaplogic.common.Field.SnapFieldType;
 import org.snaplogic.common.exceptions.SnapComponentException;
 import org.snaplogic.snapi.ExtendedSnapi;
 import org.snaplogic.snapi.Keys;
 import org.snaplogic.snapi.PropertyConstraint;
 import org.snaplogic.snapi.ResDef;
 import org.snaplogic.snapi.Snapi;
 import org.snaplogic.snapi.PropertyConstraint.Type;
 
 import com.gooddata.exception.GdcLoginException;
 import com.gooddata.integration.model.Project;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 
 /**
  * For information on the usage of this see {@link GoodDataWizard#checkDateFormats} method
  * 
  * @author grisha@snaplogic.com
  */
 class NumericFieldsCannotBeSpecifiedAsDatesException extends Exception {
 	private ComponentResourceErr err;
 
 	NumericFieldsCannotBeSpecifiedAsDatesException(ComponentResourceErr err) {
 		super();
 		this.err = err;
 	}
 
 	ComponentResourceErr getErr() {
 		return err;
 	}
 
 }
 
 /**
  * Wizard component for creating GoodData data flows. This depends on functionality available as of SnapLogic 2.3.0. Be
  * sure to make directives file of the Snap that includes this functionality to start with <tt>MIN_VERSION=2.3</tt>.
  * 
  * @author grisha@snaplogic.com
  * 
  * @since SnapLogic 2.3.0
  */
 public class GoodDataWizard extends AbstractGoodDataComponent {
 
 	public static final String[] LDM_TYPES = new String[] { SourceColumn.LDM_TYPE_ATTRIBUTE,
 			SourceColumn.LDM_TYPE_CONNECTION_POINT, SourceColumn.LDM_TYPE_DATE, SourceColumn.LDM_TYPE_FACT,
 			SourceColumn.LDM_TYPE_IGNORE, SourceColumn.LDM_TYPE_LABEL, SourceColumn.LDM_TYPE_REFERENCE };
 
 	public static final String DEFAULT_LDM_DATE_FORMAT = "yyyy-MM-dd";
 
 	public static final String PROP_OVERWRITE = "overwrite";
 
 	public static final String PROP_GD_URI_PREFIX = "uri_prefix";
 
 	public static String PROP_DLI_DEFINITION = "dli_definition";
 
 	public static String PROP_DATE_FORMAT_DEFINITION = "date_format_definition";
 
 	public static final String DEFAULT_GD_URI_PREFIX = "/gd";
 
 	private static final String CREATE_NEW_CONNECTION = "Create new...";
 
 	private static final String PROP_WIZARD_STEP = "wizard_step";
 
 	private static final String LABEL_KEY_FIELD_NAME = "field_name";
 	private static final String LABEL_KEY_PARENT = "parent";
 	private static final String CREATE_NEW_PROJECT = "Create new...";
 	private static final String PROP_DLI_NAME = "dli_name";
 	public static String PROP_LABEL_DEFINITION = "labels_definition";
 
 	@Override
 	public String getDescription() {
 		return "GoodData Project Setup";
 	}
 
 	@Override
 	public Capabilities getCapabilities() {
 		return new Capabilities() {
 			private static final long serialVersionUID = 3848994857590856035L;
 
 			{
 				put(Capability.INPUT_VIEW_LOWER_LIMIT, 0);
 				put(Capability.INPUT_VIEW_UPPER_LIMIT, 0);
 				put(Capability.OUTPUT_VIEW_LOWER_LIMIT, 0);
 				put(Capability.OUTPUT_VIEW_UPPER_LIMIT, 0);
 				put(Capability.WIZARD, true);
 			}
 		};
 	}
 
 	@Override
 	public String getLabel() {
 		return "GoodData Wizard (Java)";
 	}
 
 	@Override
 	public String getAPIVersion() {
 		return "1.0";
 	}
 
 	@Override
 	public String getComponentVersion() {
 		return "1.0";
 	}
 
 	// TODO this becomes an actual method in ComponentAPI
 	// but not yet
 	private void hidePropertiesTmp(String... propNames) {
 		for (String propName : propNames) {
 			setPropertyHidden(propName, true);
 		}
 	}
 
 	// TODO this should become part of Snaplogic's ComponentAPI
 	// but after 2.3.0 (where it will be done in a much more efficient way)
 	protected List<String> getResourcesMatchingCategory(List<String> cats) {
 		info("Looking for resources matching %s", GoodDataConnection.CONNECTION_CATEGORIES);
 		Snapi snapi = Snapi.getSnapi();
 		String serverUri = ComponentContainer.getServerUri();
 		List resList = snapi.listResources(serverUri, null, null);
 		Map<String, Object> resMap = (Map) resList.get(0);
 		List uriList = new ArrayList();
 		List candidates = new ArrayList();
 		for (String relUri : resMap.keySet()) {
 			String absUri = SnapHttpLib.concatPaths(serverUri, relUri);
 			Map resource = snapi.readResource(absUri, null);
 			resource = (Map) resource.get(absUri);
 			Map resdefMap = (Map) resource.get(Keys.RESDEF);
 			ResDef resdef = new ResDef(resdefMap);
 			// TODO resdef should have getResourceCategories method!
 			Map catMap = (Map) resdefMap.get(Keys.RESOURCE_CATEGORY);
 			if (catMap == null) {
 				continue;
 			}
 			List<String> catList = (List) catMap.get(Keys.VALUE);
 			if (catList == null) {
 				continue;
 			}
 			for (String cat : catList) {
 				if (cat.equals(GoodDataConnection.GOODDATA_CONNECTION_CATEGORY)) {
 					candidates.add(absUri);
 				}
 			}
 		}
 		return candidates;
 	}
 
 	@Override
 	public void createResourceTemplate() {
 		// TODO until more data is gathered about this usage, we cannot
 		// provide SDK functions that are both usable and not redundant.
 		// So this part is manual - here we're getting a list of properties
 		// used in this step that will be hidden in the next step of the Wizard
 		// This will become clearer once documentation for Wizard is up.
 
 		// First step: do we use existing connection or create a new one?
 		// TODO wizard currently does not support Resource References
 		// when it does, this should be changed for better user experience.
 		// This is not a connection reference here, but we'll reuse the same
 		// property name
 		List<String> availableGDConnections = getResourcesMatchingCategory(GoodDataConnection.CONNECTION_CATEGORIES);
 		availableGDConnections.add(0, CREATE_NEW_CONNECTION);
 		// TODO PropertyConstraint constructor should be lenient enough to take
 		// a Collection
 		PropertyConstraint availableGDConnectionsConstraint = new PropertyConstraint(Type.LOV, availableGDConnections
 				.toArray());
 		SimpleProp connProp = new SimpleProp("GoodData Connection", SimplePropType.SnapString, "GoodData Connection",
 				availableGDConnectionsConstraint, true);
 		setPropertyDef(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF, connProp);
 		String curConValue = getStringPropertyValue(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF);
 		if (curConValue == null) {
 			int idx = 0;
 			if (availableGDConnections.size() > 1) {
 				idx = 1;
 			}
 			setPropertyValue(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF, availableGDConnections.get(idx));
 		}
 
 		setPropertyDef(PROP_GD_URI_PREFIX, new SimpleProp("URI prefix", SimplePropType.SnapString, "URI prefix", true));
 		setPropertyValue(PROP_GD_URI_PREFIX, DEFAULT_GD_URI_PREFIX);
 
 		// TODO we should add another property, "overwrite" to control whether
 		// to overwrite
 		// resources that may already exist.
 		setPropertyDef(PROP_WIZARD_STEP, new SimpleProp(PROP_WIZARD_STEP, SimplePropType.SnapNumber, "",
 				new PropertyConstraint(Type.HIDDEN, true), true));
 		setPropertyValue(PROP_WIZARD_STEP, new BigDecimal(1));
 	}
 
 	/**
 	 * Step 2 of wizard - gather properties to create a {@link GoodDataConnection} if existing one hasn't been picked
 	 */
 	private void gatherConnectionProps(ComponentResourceErr err) {
 		nextStep();
 		GoodDataConnection.createGDConnectionResourceTemplate(this);
 	}
 
 	/**
 	 * Simple utility method to increment wizard step and hide currently shown properties
 	 */
 	private void nextStep() {
 		for (String propName : getResdef().listPropertyNames()) {
 			setPropertyHidden(propName, true);
 		}
 		int curStep = ((BigDecimal) getPropertyValue(PROP_WIZARD_STEP)).intValue();
 		curStep++;
 		setPropertyValue(PROP_WIZARD_STEP, new BigDecimal(curStep));
 	}
 
 	private static final String PROP_GD_INPUT = "gd_input";
 
 	/**
 	 * Step 3 of wizard -- get output views of existing resources that can serve as input to the {@link GoodDataPut} to
 	 * be created.
 	 */
 	private void getViewCandidates(ComponentResourceErr err) {
 		nextStep();
 		info("Looking for resources matching %s", GoodDataConnection.CONNECTION_CATEGORIES);
 		Snapi snapi = Snapi.getSnapi();
 		String serverUri = ComponentContainer.getServerUri();
 		List resList = snapi.listResources(serverUri, null, null);
 		Map<String, Object> resMap = (Map) resList.get(0);
 		List uriList = new ArrayList();
 		List<String> candidates = new ArrayList<String>();
 		for (String relUri : resMap.keySet()) {
 			String absUri = SnapHttpLib.concatPaths(serverUri, relUri);
 			Map resource = snapi.readResource(absUri, null);
 			resource = (Map) resource.get(absUri);
 			Map resdefMap = (Map) resource.get(Keys.RESDEF);
 			ResDef resdef = new ResDef(resdefMap);
 			Collection<String> outViews = resdef.listOutputViewNames();
 			if (outViews.isEmpty()) {
 				continue;
 			}
 			for (String outViewName : outViews) {
 				Map<String, Object> outView = resdef.getOutputView(outViewName);
 				if (outView.get(0) == null) {
 					candidates.add(absUri + "::" + outViewName);
 				} else {
 					continue;
 				}
 			}
 		}
 		String[] candArr = (String[]) candidates.toArray(new String[] {});
 		PropertyConstraint gdInputConstraint = new PropertyConstraint(Type.LOV, candArr);
 		setPropertyDef(PROP_GD_INPUT, new SimpleProp("Input to GoodData", SimplePropType.SnapString,
 				"Output view of a resource that serves as input to GoodData", gdInputConstraint, true));
 		if (candidates.isEmpty()) {
 			err.setMessage("No resources found that can serve as input to GoodData.");
 			return;
 		}
 		setPropertyValue(PROP_GD_INPUT, candidates.get(0));
 	}
 
 	/**
 	 * Get the {@link ResDef} and fields from the view picked as the good data source in {@link #PROP_GD_INPUT} property
 	 * 
 	 * @return A {@link Map} such that key <tt>fields</tt> corresponds to <tt>List<List<String>></tt> of fields and
 	 *         {@link Keys#RESDEF} is the {@link ResDef}
 	 */
 	// TODO "fields" should be part of Keys
 	private Map<String, Object> getResDefandFieldsFromSourceView() {
 		Snapi snapi = Snapi.getSnapi();
 		String gdInput = getStringPropertyValue(PROP_GD_INPUT);
 		String[] uriAndView = gdInput.split("::");
 		String uri = uriAndView[0];
 		String view = uriAndView[1];
 		ResDef gdInputResdef = getSrcResDef(uri);
 		Map<String, Object> outView = gdInputResdef.getOutputView(view);
 		List<List<String>> fields = (List) outView.get("fields");
 		Map<String, Object> retval = new HashMap<String, Object>();
 		retval.put("fields", fields);
 		retval.put(Keys.RESDEF, gdInputResdef);
 		return retval;
 	}
 
 	/**
 	 * Get the {@link ResDef} specified in {@link #PROP_GD_INPUT} property
 	 */
 	// TODO this should be a part of Java SnAPI...
 	private ResDef getSrcResDef(String uri) {
 		Snapi snapi = Snapi.getSnapi();
 		Map resdefMap = snapi.readResource(uri, null);
 		resdefMap = (Map) resdefMap.get(uri);
 		resdefMap = (Map) resdefMap.get(Keys.RESDEF);
 		ResDef resdef = new ResDef(resdefMap);
 		return resdef;
 	}
 
 	private static final String DLI_SPEC_KEY_FIELD_NAME = "field_name";
 
 	private static final String DLI_SPEC_KEY_LDM_TYPE = "ldm_type";
 
 	private static final String DATE_FORMAT_KEY_FIELD_NAME = "field_name";
 
 	private static final String DATE_FORMAT_KEY_FORMAT = "format";
 
 	/**
 	 * Step 4 of wizard - show a DLI based on the view picked in previous step. User can edit this of course. This will
 	 * be created as a complex property, and then, in the {@link #execute(Map, Map)} method XML will be written out and
 	 * loaded into GoodData
 	 * 
 	 * @param err
 	 */
 	private void getDLIConfig(ComponentResourceErr err) {
 		nextStep();
 		Map<String, Object> resdefAndFields = getResDefandFieldsFromSourceView();
 		List<List<String>> fields = (List<List<String>>) resdefAndFields.get("fields");
 		SimpleProp stringProp = new SimpleProp("Field", SimplePropType.SnapString, "");
 		DictProp fieldDefProp = new DictProp("Field definition", stringProp, "", 2, 2, true, true);
 		fieldDefProp.put(DLI_SPEC_KEY_FIELD_NAME, new SimpleProp("Field name", SimplePropType.SnapString, "string1"));
 		PropertyConstraint ldmTypeConstraint = new PropertyConstraint(Type.LOV, LDM_TYPES);
 		fieldDefProp.put(DLI_SPEC_KEY_LDM_TYPE, new SimpleProp("LDM Type", SimplePropType.SnapString, "",
 				ldmTypeConstraint, true));
 		ListProp dliSpec = new ListProp("DLI definition", fieldDefProp, "", fields.size(), fields.size(), true);
 		setPropertyDef(PROP_DLI_DEFINITION, dliSpec);
 		List dliSpecValue = new ArrayList();
 		for (List<String> field : fields) {
 			Map fieldSpec = new HashMap();
 			fieldSpec.put(DLI_SPEC_KEY_FIELD_NAME, field.get(0));
 			String snapTypeStr = field.get(1);
 			if (snapTypeStr.equals(SnapFieldType.SnapDateTime.toString())) {
 				fieldSpec.put(DLI_SPEC_KEY_LDM_TYPE, SourceColumn.LDM_TYPE_DATE);
 			} else {
 				fieldSpec.put(DLI_SPEC_KEY_LDM_TYPE, SourceColumn.LDM_TYPE_ATTRIBUTE);
 			}
 			dliSpecValue.add(fieldSpec);
 		}
 		setPropertyValue(PROP_DLI_DEFINITION, dliSpecValue);
 	}
 
 	private void getProjects(ComponentResourceErr err) {
 		nextStep();
 
 		PropertyConstraint projNameLovConstraint = new PropertyConstraint();
 		SimpleProp projNameProp = new SimpleProp("Project name", SimpleProp.SimplePropType.SnapString, "Project name",
 				projNameLovConstraint, true);
 		String gdConnUri = getStringPropertyValue(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF);
 		ArrayList<String> projectArrayList = new ArrayList<String>();
 		projectArrayList.add(0, CREATE_NEW_PROJECT);
 
		ResDef conResDef;
		if (CREATE_NEW_CONNECTION.equalsIgnoreCase(gdConnUri)) {
 			ExtendedSnapi snapi = ExtendedSnapi.getExtendedSnapi();
 			String serverUri = ComponentContainer.getServerUri();
 			conResDef = createConnectionObject(snapi, serverUri);
		} else {
			conResDef = getResourceObject(gdConnUri, null);
 		}

 		GdcRESTApiWrapper restApi;
 		try {
 			restApi = GoodDataConnection.login(conResDef, this);
 		} catch (GdcLoginException gdcle) {
 			elog(gdcle);
 			if (err == null) {
 				throw new SnapComponentException(gdcle);
 			}
 			err.getResourceRefErr(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF).setMessage(
 					"Could not validate connection reference: %s", new Object[] { gdcle });
 			return;
 		}
 
 		HashMap<String, Project> projectList = GoodDataApiHelper.getProjectList(restApi);
 		Set<String> projectSet = projectList.keySet();
 		projectArrayList.addAll(projectSet);
 
 		projNameLovConstraint.put(PropertyConstraint.Type.LOV, projectArrayList.toArray(new String[0]));
 		setPropertyDef(GoodDataPut.PROP_PROJECT_NAME, projNameProp);
 	}
 
 	/*
 	 * * To create a wizard screen for field formats that are
 	 * 
 	 * @param fieldsToFormat list of fields that need formats specified as returned by {@link
 	 * #checkDateFormats(ComponentResourceErr)}
 	 */
 	private void getDateFormats(ComponentResourceErr err, List<String> fieldsToFormat) {
 		nextStep();
 		SimpleProp stringProp = new SimpleProp("Field", SimplePropType.SnapString, "");
 		DictProp fieldDefProp = new DictProp("Field definition", stringProp, "", 2, 2, true, true);
 		fieldDefProp
 				.put(DATE_FORMAT_KEY_FIELD_NAME, new SimpleProp("Field name", SimplePropType.SnapString, "string1"));
 		PropertyConstraint ldmTypeConstraint = new PropertyConstraint(Type.LOV, LDM_TYPES);
 		fieldDefProp.put(DATE_FORMAT_KEY_FORMAT, new SimpleProp("Format", SimplePropType.SnapString, "", null, true));
 		ListProp dateFormatSpec = new ListProp("Date formats", fieldDefProp, "", fieldsToFormat.size(), fieldsToFormat
 				.size(), true);
 		setPropertyDef(PROP_DATE_FORMAT_DEFINITION, dateFormatSpec);
 		List formatSpecValue = new ArrayList();
 		for (String field : fieldsToFormat) {
 			Map fieldSpec = new HashMap();
 			fieldSpec.put(DATE_FORMAT_KEY_FIELD_NAME, field);
 			fieldSpec.put(DATE_FORMAT_KEY_FORMAT, DEFAULT_LDM_DATE_FORMAT);
 			formatSpecValue.add(fieldSpec);
 		}
 		setPropertyValue(PROP_DATE_FORMAT_DEFINITION, formatSpecValue);
 	}
 
 	/**
 	 * This is to be run in step 4. The logic is this: If there are fields that have type
 	 * {@link SnapFieldType#SnapString} and are specified as {@link #LDM_TYPE_DATE} by the user in the previous step,
 	 * this method returns a {@link List} of them to be used in {@link #getDateFormats()} in the same step. If fields
 	 * specified as {@link #LDM_TYPE_DATE} are {@link SnapFieldType#SnapDateTime}, we don't need a user to enter a
 	 * format, we can implicitly specify it as {@link #DEFAULT_LDM_DATE_FORMAT} since we control it. If a field
 	 * specified as <tt>Date</tt> is a {@link SnapFieldType#SnapNumber} then we throw
 	 * {@link NumericFieldsCannotBeSpecifiedAsDatesException} to an error and go no further.
 	 * 
 	 * @throws NumericFieldsCannotBeSpecifiedAsDatesException
 	 *             if any fields of type {@link SnapNumber} are
 	 */
 	private List<String> checkDateFormats(ComponentResourceErr err)
 			throws NumericFieldsCannotBeSpecifiedAsDatesException {
 		List<String> result = new ArrayList<String>();
 		List<Map> dliSpecValue = (List<Map>) getListPropertyValue(PROP_DLI_DEFINITION);
 		Map<String, Object> resdefAndFields = getResDefandFieldsFromSourceView();
 		List<List<String>> fields = (List<List<String>>) resdefAndFields.get("fields");
 		List<String> fieldsSpecedAsDate = new ArrayList<String>();
 		// Look for fields speced as LDM_TYPE_DATE
 		for (Map<String, String> fieldSpecs : dliSpecValue) {
 			String specedFieldName = fieldSpecs.get(DLI_SPEC_KEY_FIELD_NAME);
 			String specedLdmType = fieldSpecs.get(DLI_SPEC_KEY_LDM_TYPE);
 			if (!specedLdmType.equals(SourceColumn.LDM_TYPE_DATE)) {
 				continue;
 			}
 			fieldsSpecedAsDate.add(specedFieldName);
 		}
 		// Look for fields that are strings that were speced as Date
 		// If there are fields that speced as date but were numbers,
 		// we will error out
 		String notStringOrDatetimeFieldSpecedAsDates = "";
 		int fieldIdx = -1;
 		for (List<String> field : fields) {
 			fieldIdx++;
 			String fieldName = field.get(0);
 			if (fieldsSpecedAsDate.indexOf(fieldName) < 0) {
 				continue;
 			}
 			String snapTypeStr = field.get(1);
 			if (snapTypeStr.equals(SnapFieldType.SnapDateTime.toString())) {
 				continue;
 			} else if (snapTypeStr.equals(SnapFieldType.SnapString.toString())) {
 				result.add(fieldName);
 			} else {
 				if (notStringOrDatetimeFieldSpecedAsDates.length() > 0) {
 					notStringOrDatetimeFieldSpecedAsDates += ",";
 				}
 				notStringOrDatetimeFieldSpecedAsDates += "'" + fieldName + "'";
 			}
 
 		}
 		if (notStringOrDatetimeFieldSpecedAsDates.length() > 0) {
 			ListPropErr listPropErr = (ListPropErr) err.getPropertyErr(PROP_DLI_DEFINITION);
 			// TODO It appears Java SnAPI implementation is missing
 			// functionality to
 			// target the errors to a particular list item...
 			// So we resort to concatenating things
 			String errMsg = "The following field(s) are not of type '" + SnapFieldType.SnapString + "' or '"
 					+ SnapFieldType.SnapDateTime + "', but are specified as " + SourceColumn.LDM_TYPE_DATE + ": "
 					+ notStringOrDatetimeFieldSpecedAsDates;
 			listPropErr.setMessage(errMsg);
 			throw new NumericFieldsCannotBeSpecifiedAsDatesException(err);
 		}
 		return result;
 	}
 
 	private void confirmationScreen() {
 		nextStep();
 		PropertyConstraint unmodifiable = new PropertyConstraint(Type.UNMODIFIABLE, true);
 		String propNameHack = "You are about to create DLI and model for your GoodData project. Click 'Next' to continue, or 'Back' to review your choices";
 		setPropertyDef(propNameHack, new SimpleProp(propNameHack, SimplePropType.SnapString, "", unmodifiable, false));
 		setPropertyValue(propNameHack, "OK");
 	}
 
 	private void getLabels(ComponentResourceErr err, List<String> fieldsToFormat) {
 		nextStep();
 		SimpleProp stringProp = new SimpleProp("Field", SimpleProp.SimplePropType.SnapString, "");
 		DictProp fieldDefProp = new DictProp("Field definition", stringProp, "", 2, 2, true, true);
 		fieldDefProp.put(DATE_FORMAT_KEY_FIELD_NAME, new SimpleProp("Label", SimpleProp.SimplePropType.SnapString,
 				"string1"));
 		List<String> possibleLabelTargets = getPossibleLabelTargets(err);
 		PropertyConstraint labelTargetConstraint = new PropertyConstraint(PropertyConstraint.Type.LOV,
 				possibleLabelTargets);
 		fieldDefProp.put(LABEL_KEY_PARENT, new SimpleProp("Reference", SimpleProp.SimplePropType.SnapString, "",
 				labelTargetConstraint, true));
 		ListProp labelSpec = new ListProp("Labels", fieldDefProp, "", fieldsToFormat.size(), fieldsToFormat.size(),
 				true);
 		setPropertyDef(PROP_LABEL_DEFINITION, labelSpec);
 		List<Map<String, String>> formatSpecValue = new ArrayList<Map<String, String>>();
 		for (String field : fieldsToFormat) {
 			Map<String, String> fieldSpec = new HashMap<String, String>();
 			fieldSpec.put(LABEL_KEY_FIELD_NAME, field);
 			fieldSpec.put(LABEL_KEY_PARENT, possibleLabelTargets.get(0));
 			formatSpecValue.add(fieldSpec);
 		}
 		setPropertyValue(PROP_LABEL_DEFINITION, formatSpecValue);
 	}
 
 	private List<String> getPossibleLabelTargets(ComponentResourceErr err) {
 		List<String> result = new ArrayList<String>();
 		List<Map> dliSpecValue = getListPropertyValue(PROP_DLI_DEFINITION);
 
 		for (Map fieldSpecs : dliSpecValue) {
 			String specedFieldName = (String) fieldSpecs.get(DATE_FORMAT_KEY_FIELD_NAME);
 			String specedLdmType = (String) fieldSpecs.get(DLI_SPEC_KEY_LDM_TYPE);
 			if (specedLdmType.equals(SourceColumn.LDM_TYPE_FACT)
 					|| specedLdmType.equals(SourceColumn.LDM_TYPE_REFERENCE)
 					|| specedLdmType.equals(SourceColumn.LDM_TYPE_LABEL)
 					|| specedLdmType.equals(SourceColumn.LDM_TYPE_DATE)
 					|| specedLdmType.equals(SourceColumn.LDM_TYPE_IGNORE))
 				continue;
 			result.add(specedFieldName);
 		}
 		return result;
 	}
 
 	private List<String> checkLabels(ComponentResourceErr err) throws NumericFieldsCannotBeSpecifiedAsDatesException {
 		List<String> result = new ArrayList<String>();
 		List<Map> dliSpecValue = getListPropertyValue(PROP_DLI_DEFINITION);
 
 		for (Map<String, String> fieldSpecs : dliSpecValue) {
 			String specedFieldName = (String) fieldSpecs.get("field_name");
 			String specedLdmType = (String) fieldSpecs.get("ldm_type");
 			if (!specedLdmType.equals("LABEL")) {
 				continue;
 			}
 			result.add(specedFieldName);
 		}
 		return result;
 	}
 
 	@Override
 	public void suggestResourceValues(ComponentResourceErr err) {
 		try {
 			int step = ((BigDecimal) getPropertyValue(PROP_WIZARD_STEP)).intValue();
 			switch (step) {
 			case 1:
 				String gdConnUri = getStringPropertyValue(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF);
 				if (gdConnUri.equals(CREATE_NEW_CONNECTION)) {
 					// Proceed to step 2: Ask for connection properties
 					gatherConnectionProps(err);
 					return;
 				}
 				// Else, fall through
 				nextStep();
 			case 2:
 				getProjects(err);
 				break;
 			case 3:
 				getViewCandidates(err);
 				break;
 			case 4:
 				getDLIConfig(err);
 				break;
 			case 5:
 				try {
					List<String> stringDateFields = checkDateFormats(err);
 					if (stringDateFields.isEmpty()) {
						// do nothing
 						nextStep();
					} else {
						getDateFormats(err, stringDateFields);
 						break;
 					}
 				} catch (NumericFieldsCannotBeSpecifiedAsDatesException nfcbsade) {
 					return;
 				}
 			case 6:
 				try {
 					List stringLabelFields = checkLabels(err);
 					if (stringLabelFields.isEmpty()) {
 						nextStep();
					} else {
						getLabels(err, stringLabelFields);
 						break;
 					}
 				} catch (NumericFieldsCannotBeSpecifiedAsDatesException nfcbsade) {
 					return;
 				}
 			case 7:
 				confirmationScreen();
 				return;
 			}
 		} catch (Exception e) {
 			err.setMessage("Error occurred: %s", e);
 			elog(e);
 		}
 	}
 
 	@Override
 	public void execute(Map<String, InputView> inputViews, Map<String, OutputView> outputViews) {
 		ExtendedSnapi snapi = ExtendedSnapi.getExtendedSnapi();
 		String gdUriPrefix = getStringPropertyValue(PROP_GD_URI_PREFIX);
 		String serverUri = ComponentContainer.getServerUri();
 		String gdConnUri = getStringPropertyValue(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF);
 		if (gdConnUri.equals(CREATE_NEW_CONNECTION)) {
 			gdConnUri = gdUriPrefix + "/GoodDataConnection";
 			ResDef connResDef = createConnectionObject(snapi, serverUri);
 			connResDef.save(gdConnUri);
 		}
 
 		String projectName = getStringPropertyValue(GoodDataPut.PROP_PROJECT_NAME);
 		if (CREATE_NEW_PROJECT.equals(projectName)) {
 			// Create a new project
 
 		}
 
 		String dliName = getStringPropertyValue(PROP_DLI_NAME);
 
 		List dliColumnTypes = getListPropertyValue(PROP_DLI_DEFINITION);
 		List dateFormatsList = getListPropertyValue(PROP_DATE_FORMAT_DEFINITION);
 		HashMap<String, String> dateFormats = propertySheetToHash(dateFormatsList, DATE_FORMAT_KEY_FIELD_NAME,
 				DATE_FORMAT_KEY_FORMAT);
 		List labelFields = getListPropertyValue(PROP_LABEL_DEFINITION);
 		HashMap<String, String> labelParents = propertySheetToHash(labelFields, LABEL_KEY_FIELD_NAME, LABEL_KEY_PARENT);
 
 		SourceSchema ss = SourceSchema.createSchema(dliName);
 
 		for (Iterator localIterator1 = dliColumnTypes.iterator(); localIterator1.hasNext();) {
 			Object columnType = localIterator1.next();
 
 			HashMap entry = (HashMap) columnType;
 			String fieldName = (String) entry.get(DLI_SPEC_KEY_FIELD_NAME);
 			String ldmType = (String) entry.get(DLI_SPEC_KEY_LDM_TYPE);
 			SourceColumn column = new SourceColumn(fieldName, ldmType, fieldName);
 
 			if (dateFormats.containsKey(fieldName)) {
 				column.setFormat((String) dateFormats.get(fieldName));
 			}
 
 			if (labelParents.containsKey(fieldName)) {
 				column.setReference((String) labelParents.get(fieldName));
 			}
 			ss.addColumn(column);
 		}
 
 		System.out.println(ss.toString());
 
 		// Create the model XML
 		String xml = "";
 		// TODO add code to actually create the model here.
 
 		String gdInput = getStringPropertyValue(PROP_GD_INPUT);
 		String[] uriAndView = gdInput.split("::");
 		String uri = uriAndView[0];
 		String outViewName = uriAndView[1];
 
 		Map<String, Object> resdefAndFields = getResDefandFieldsFromSourceView();
 		ResDef srcResDef = (ResDef) resdefAndFields.get(Keys.RESDEF);
 
 		String gdPutUri = gdUriPrefix + "/GoodDataUpload";
 		ResDef putResDef = snapi.createResourceObject(serverUri, GoodDataPut.class.getName(), null);
 		putResDef.setResourceRef(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF, gdConnUri);
 		// Ideally do suggest and then set the DLI and project values
 		// But to do that, the model has to already have been created
 		// it will have to correspond to the view we will link down the
 		// road here.
 		putResDef = putResDef.suggestValues(null);
 		// We remove the suggested view for now because we haven't created the
 		// model
 		// and haven't set the proper values after suggest so therefore
 		// suggested view is probably wrong - but in reality it should be
 		// the same as srcResDef's output view
 		Collection<String> inViews = putResDef.listInputViewNames();
 		String inViewName = "Input";
 		if (inViews.size() > 0) {
 			inViewName = inViews.iterator().next();
 			putResDef.removeInputView(inViewName);
 		}
 		List<Field> fields = new ArrayList<Field>();
 		List<List<String>> srcFields = (List<List<String>>) resdefAndFields.get("fields");
 		String[][] fieldLinks = new String[srcFields.size()][2];
 		int i = 0;
 		for (List<String> srcField : srcFields) {
 			String fieldName = srcField.get(0);
 			Field f = new Field(fieldName, SnapFieldType.map.get(srcField.get(1)), srcField.get(2));
 			fields.add(f);
 			List<String> link = new ArrayList<String>();
 			fieldLinks[i][0] = fieldName;
 			fieldLinks[i][1] = fieldName;
 			i++;
 		}
 		putResDef.addRecordInputView(inViewName, fields, "", true);
 		putResDef.save(gdPutUri);
 
 		// This part is pending the fix of
 		// https://www.snaplogic.org/trac/ticket/2571
 
 		// PipelineResDef pipe =
 		// (PipelineResDef)snapi.createResourceObject(serverUri,
 		// Keys.PIPELINE_COMPONENT_NAME, null);
 		// pipe.add(putResDef, "Target");
 		// pipe.add(srcResDef, "Source");
 		//        
 		// pipe.linkViews("Source", outViewName, "Target", inViewName,
 		// fieldLinks);
 		// pipe.save(gdUriPrefix + "/Pipeline");
 	}
 
 	private ResDef createConnectionObject(ExtendedSnapi snapi, String serverUri) {
 		ResDef connResDef = snapi.createResourceObject(serverUri, GoodDataConnection.class.getName(), null);
 		String[] connProps = { GoodDataConnection.PROP_HOSTNAME, GoodDataConnection.PROP_HOSTNAME_FTP,
 				GoodDataConnection.PROP_PASSWORD, GoodDataConnection.PROP_USERNAME, GoodDataConnection.PROP_PROTOCOL };
 		for (String connProp : connProps) {
 			String val = getStringPropertyValue(connProp);
 			connResDef.setPropertyValue(connProp, val);
 		}
 		return connResDef;
 	}
 
 	private HashMap<String, String> propertySheetToHash(List<Object> list, String key_key, String value_key) {
 		HashMap<String, String> result = new HashMap<String, String>();
 		for (Iterator localIterator = list.iterator(); localIterator.hasNext();) {
 			Object item = localIterator.next();
 			HashMap entry = (HashMap) item;
 			String key = (String) entry.get(key_key);
 			String value = (String) entry.get(value_key);
 			result.put(key, value);
 		}
 
 		return result;
 	}
 }
