 package pl.art.mnp.rogalin.field;
 
 import pl.art.mnp.rogalin.db.FieldInfo;
 import pl.art.mnp.rogalin.ui.field.SelectUiFieldType;
 import pl.art.mnp.rogalin.ui.field.UiFieldType;
 
 @SuppressWarnings("serial")
 public class ComboBoxFieldType extends AbstractFieldType {
 
 	public ComboBoxFieldType(final FieldInfo field) {
 		super(field);
 	}
 
 	@Override
 	public UiFieldType getFormField() {
		return new SelectUiFieldType(field, getOptions(), false);
 	}
 
 	@Override
 	public UiFieldType getSearchField() {
 		return new SelectUiFieldType(field, getOptions(), true);
 	}
 }
