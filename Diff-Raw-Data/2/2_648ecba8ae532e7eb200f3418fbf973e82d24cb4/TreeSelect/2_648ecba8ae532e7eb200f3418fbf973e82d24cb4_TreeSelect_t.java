 // Copyright 2008 Sílex Sistemas Ltda. e ONP Informática Ltda.
 package br.com.arsmachina.tapestrycrud.components;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.tapestry5.Binding;
 import org.apache.tapestry5.BindingConstants;
 import org.apache.tapestry5.ComponentResources;
 import org.apache.tapestry5.FieldValidationSupport;
 import org.apache.tapestry5.FieldValidator;
 import org.apache.tapestry5.MarkupWriter;
 import org.apache.tapestry5.RenderSupport;
 import org.apache.tapestry5.SelectModel;
 import org.apache.tapestry5.ValidationException;
 import org.apache.tapestry5.ValidationTracker;
 import org.apache.tapestry5.ValueEncoder;
 import org.apache.tapestry5.annotations.Environmental;
 import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
 import org.apache.tapestry5.annotations.IncludeStylesheet;
 import org.apache.tapestry5.annotations.Mixin;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.corelib.base.AbstractField;
 import org.apache.tapestry5.corelib.components.Select;
 import org.apache.tapestry5.corelib.mixins.RenderDisabled;
 import org.apache.tapestry5.internal.TapestryInternalUtils;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.ioc.internal.util.InternalUtils;
 import org.apache.tapestry5.services.ComponentDefaultProvider;
 import org.apache.tapestry5.services.FieldValidatorDefaultSource;
 import org.apache.tapestry5.services.Request;
 import org.apache.tapestry5.services.ValueEncoderSource;
 import org.apache.tapestry5.util.EnumSelectModel;
 
 import br.com.arsmachina.tapestrycrud.Constants;
 import br.com.arsmachina.tapestrycrud.tree.SimpleTreeSelectNode;
 import br.com.arsmachina.tapestrycrud.tree.TreeSelectNode;
 
 /**
  * Most of this code was copied from the {@link Select} component.
  * 
  * @author Thiago H. de Paula Figueiredo
  */
 @IncludeJavaScriptLibrary("classpath:/br/com/arsmachina/tapestrycrud/javascript/treeselect.js")
 @IncludeStylesheet(Constants.TAPESTRY_CRUD_CSS_ASSET)
 public class TreeSelect extends AbstractField {
 
 	private static final ArrayList<TreeSelectNode> EMPTY_LIST =
 		new ArrayList<TreeSelectNode>(0);
 
 	/**
 	 * Allows a specific implementation of {@link ValueEncoder} to be supplied.
 	 * This is used to create client-side string values for the different
 	 * options.
 	 * 
 	 * @see ValueEncoderSource
 	 */
 	@SuppressWarnings("unchecked")
 	@Parameter
 	private ValueEncoder encoder;
 
 	@Inject
 	private ComponentDefaultProvider defaultProvider;
 
 	/**
 	 * The list of root nodes to be used as options.
 	 */
 	@Parameter(required = true, allowNull = false)
 	private List<TreeSelectNode> rootNodes;
 
 	@Inject
 	private Request request;
 
 	@Inject
 	private ComponentResources resources;
 
 	@Environmental
 	private ValidationTracker tracker;
 
 	/**
 	 * Performs input validation on the value supplied by the user in the form
 	 * submission.
 	 */
 	@Parameter(defaultPrefix = BindingConstants.VALIDATE)
 	private FieldValidator<Object> validate;
 
 	/**
 	 * The value to read or update.
 	 */
 	@Parameter(required = true, principal = true, autoconnect = true)
 	private Object value;
 
 	/**
 	 * "No parent object" option label;
 	 */
 	@Parameter(value = "message:treeselect.noparent.option", defaultPrefix = BindingConstants.MESSAGE)
 	private String noParentOptionLabel;
 
 	@Inject
 	private FieldValidationSupport fieldValidationSupport;
 
 	@SuppressWarnings("unused")
 	@Mixin
 	private RenderDisabled renderDisabled;
 	
 	@Inject
 	private RenderSupport renderSupport;
 
 	private String selectedClientValue;
 
 	@SuppressWarnings("unused")
 	private boolean isSelected(String clientValue) {
 		return TapestryInternalUtils.isEqual(clientValue, selectedClientValue);
 	}
 
 	@Override
 	protected void processSubmission(String elementName) {
 		
 		String submittedValue = request.getParameter(elementName);
 
 		// When the null option is selected, the "on" value is submitted.
		if (submittedValue != null && submittedValue.equals("on")) {
 			submittedValue = null;
 		}
 		
 		tracker.recordInput(this, submittedValue);
 
 		Object selectedValue =
 			InternalUtils.isBlank(submittedValue) ? null
 					: encoder.toValue(submittedValue);
 
 		try {
 			fieldValidationSupport.validate(selectedValue, resources, validate);
 			value = selectedValue;
 		}
 		catch (ValidationException ex) {
 			tracker.recordError(this, ex.getMessage());
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	void beginRender(MarkupWriter writer) {
 
 		writer.element("ul", "class", "t-crud-tree-select");
 
 		renderNoParentOption(writer);
 
 		for (TreeSelectNode node : rootNodes) {
 			render(node, writer);
 		}
 
 		writer.end(); // outer ul tag
 		
 		final String clientId = getClientId();
 		final String client = encoder.toClient(value);
 		
 		renderSupport.addScript("TreeSelect.disableDescendentInputs('%s-%s')",
 				clientId, client);
 
 	}
 
 	private void renderNoParentOption(MarkupWriter writer) {
 
 		Map<String, String> attributes = new HashMap<String, String>(1);
 		attributes.put("class", "noParentOption");
 
 		SimpleTreeSelectNode node =
 			new SimpleTreeSelectNode(null, EMPTY_LIST, noParentOptionLabel,
 					false, attributes);
 
 		render(node, writer);
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void render(TreeSelectNode node, MarkupWriter writer) {
 
 		String selectedClientValue = encoder.toClient(value);
 
 		// Use the value passed up in the form submission, if available.
 		// Failing that, see if there is a current value (via the value
 		// parameter), and
 		// convert that to a client value for later comparison.
 
 		String thisClientValue = encoder.toClient(node.getValue());
 		boolean checked = isEqual(selectedClientValue, thisClientValue);
 
 		String clientId = getClientId();
 		String radioId = clientId + "-" + thisClientValue;
 
 		writer.element("li");
 		writeAttributes(node.getAttributes(), writer);
 
 		if (checked) {
 			writer.getElement().addClassName("checked");
 		}
 
 		renderLabel(node, writer, radioId);
 		renderRadioButton(writer, thisClientValue, checked, radioId);
 
 		writer.end(); // input
 
 		List<TreeSelectNode> children = node.getChildren();
 
 		if (children.isEmpty() == false) {
 
 			writer.element("ul");
 
 			for (TreeSelectNode child : children) {
 				render(child, writer);
 			}
 
 			writer.end(); // ul
 
 		}
 
 		writer.end(); // li
 
 	}
 
 	/**
 	 * @param selectedClientValue
 	 * @param thisClientValue
 	 * @return
 	 */
 	private boolean isEqual(String selectedClientValue, String thisClientValue) {
 
 		if (selectedClientValue == thisClientValue) {
 			return true;
 		} else if (selectedClientValue == null && thisClientValue != null
 				|| selectedClientValue != null && thisClientValue == null) {
 
 			return false;
 
 		}
 
 		return thisClientValue.equals(selectedClientValue);
 
 	}
 
 	/**
 	 * @param writer
 	 * @param thisClientValue
 	 * @param checked
 	 * @param radioId
 	 */
 	private void renderRadioButton(MarkupWriter writer, String thisClientValue,
 			boolean checked, String radioId) {
 		writer.element("input", "type", "radio", "name", getControlName(),
 				"id", radioId, "value", thisClientValue);
 
 		if (checked) {
 			writer.attributes("checked", "checked");
 		}
 	}
 
 	/**
 	 * @param node
 	 * @param writer
 	 * @param radioId
 	 */
 	private void renderLabel(TreeSelectNode node, MarkupWriter writer,
 			String radioId) {
 		writer.element("label", "for", radioId);
 		writer.write(node.getLabel());
 		writer.end(); // label
 	}
 
 	@SuppressWarnings("unchecked")
 	ValueEncoder defaultEncoder() {
 		return defaultProvider.defaultValueEncoder("value", resources);
 	}
 
 	@SuppressWarnings("unchecked")
 	SelectModel defaultModel() {
 		Class valueType = resources.getBoundType("value");
 
 		if (valueType == null)
 			return null;
 
 		if (Enum.class.isAssignableFrom(valueType))
 			return new EnumSelectModel(valueType,
 					resources.getContainerMessages());
 
 		return null;
 	}
 
 	/**
 	 * Computes a default value for the "validate" parameter using
 	 * {@link FieldValidatorDefaultSource}.
 	 */
 	Binding defaultValidate() {
 		return defaultProvider.defaultValidatorBinding("value", resources);
 	}
 
 	@Override
 	public boolean isRequired() {
 		return validate.isRequired();
 	}
 
 	private void writeAttributes(Map<String, String> attributes,
 			MarkupWriter writer) {
 		if (attributes == null)
 			return;
 
 		for (Map.Entry<String, String> e : attributes.entrySet())
 			writer.attributes(e.getKey(), e.getValue());
 	}
 
 }
