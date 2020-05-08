 package pl.art.mnp.rogalin.ui.field;
 
 import org.apache.commons.lang.StringUtils;
 
 import pl.art.mnp.rogalin.db.FieldInfo;
 import pl.art.mnp.rogalin.db.predicate.ContainsPredicate;
 import pl.art.mnp.rogalin.db.predicate.Predicate;
 
 import com.mongodb.DBObject;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.ui.AbstractField;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 @SuppressWarnings("serial")
 public class TextWithDefaultUiFieldType extends AbstractUiFieldType {
 
 	private final TextField textField;
 
 	private final VerticalLayout layout;
 
 	private final String defaultValue;
 
 	private final CheckBox checkBox;
 
 	private String savedValue = "";
 
 	public TextWithDefaultUiFieldType(FieldInfo field, boolean search, final String defaultValue) {
 		super(field);
 		this.defaultValue = defaultValue;
 		this.layout = new VerticalLayout();
 		layout.setCaption(field.toString());
 
 		textField = new TextField();
 		textField.setValidationVisible(true);
 		if (!search) {
 			textField.setRequired(field.isRequired());
 		}
 		textField.setRequiredError(EMPTY_FIELD_ERROR);
 
 		checkBox = new CheckBox(defaultValue);
 		checkBox.addValueChangeListener(new ValueChangeListener() {
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 				if (checkBox.getValue()) {
 					if (!defaultValue.equals(textField.getValue())) {
 						savedValue = textField.getValue();
 					}
 					textField.setValue(defaultValue);
 					textField.setEnabled(false);
 				} else {
 					textField.setValue(savedValue);
 					textField.setEnabled(true);
 				}
 			}
 		});
 
 		layout.addComponents(textField, checkBox);
 	}
 
 	@Override
 	public VerticalLayout getComponent() {
 		return layout;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	protected AbstractField getAbstractField() {
 		return textField;
 	}
 
 	@Override
 	public void setFromDbObject(DBObject object) {
 		super.setFromDbObject(object);
 		if (defaultValue.equals(textField.getValue())) {
 			checkBox.setValue(true);
 		}
 	}
 
 	@Override
 	public Predicate getPredicate() {
 		if (StringUtils.isEmpty(textField.getValue())) {
 			return DUMMY_PREDICATE;
 		}
 		return new ContainsPredicate(textField.getValue(), fieldInfo.name());
 	}
 
 	@Override
 	public void clear() {
 		textField.setValue("");
 	}
 }
