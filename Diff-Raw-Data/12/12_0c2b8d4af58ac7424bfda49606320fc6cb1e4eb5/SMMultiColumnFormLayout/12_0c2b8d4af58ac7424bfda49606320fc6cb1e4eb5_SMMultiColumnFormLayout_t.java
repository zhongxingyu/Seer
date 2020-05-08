 package au.org.scoutmaster.util;
 
 import javax.persistence.metamodel.SetAttribute;
 
 import org.vaadin.tokenfield.TokenField;
 
 import au.com.vaadinutils.crud.FormHelper;
 import au.com.vaadinutils.crud.MultiColumnFormLayout;
 import au.com.vaadinutils.crud.ValidatingFieldGroup;
 import au.org.scoutmaster.domain.BaseEntity;
 import au.org.scoutmaster.views.Selected;
 
 
 public class SMMultiColumnFormLayout<E extends BaseEntity> extends MultiColumnFormLayout<E>
 {
 	private static final long serialVersionUID = 1L;
 
 	public SMMultiColumnFormLayout(int columns, ValidatingFieldGroup<E> fieldGroup)
 	{
 		super(columns, fieldGroup);
 	}
 	
	protected FormHelper<E> getFormHelper(MultiColumnFormLayout<E> layout, ValidatingFieldGroup<E> fieldGroup)
 	{
		return new SMFormHelper<>(layout, fieldGroup);
 	}
 

 	public <L> TokenField bindTokenField(Selected<E> selected, String fieldLabel, SetAttribute<E, L> entityField,
 			Class<L> clazz)
 	{
 		TokenField field = ((SMFormHelper<E>)getFormHelper()).bindTokenField(this, super.getFieldGroup(), selected, fieldLabel, entityField, clazz);
 		this.getFieldList().add(field);
 		return field;
 	}
 
 	public <L> TokenField bindTokenField(Selected<E> selected, String fieldLabel, String fieldName,
 			Class<L> clazz)
 	{
 		TokenField field = ((SMFormHelper<E>)getFormHelper()).bindTokenField(this, super.getFieldGroup(), selected, fieldLabel, fieldName, clazz);
 		this.getFieldList().add(field);
 		return field;
 	}
 
 

 }
