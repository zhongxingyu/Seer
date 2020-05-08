 package au.org.scoutmaster.fields;
 
 import au.com.vaadinutils.crud.splitFields.SplitField;
 import au.org.scoutmaster.domain.BaseEntity;
 import au.org.scoutmaster.views.Selected;
 
 import com.vaadin.ui.Label;
 import com.vaadin.ui.VerticalLayout;
 
 @SuppressWarnings("unchecked")
 public class SplitContactTokenField<T extends BaseEntity> extends ContactTokenField<T> implements SplitField
 {
 	private static final long serialVersionUID = 7753660388792217050L;
 	private Label label;
 
 	public SplitContactTokenField(Selected<T> selected, String fieldLabel, VerticalLayout layout)
 	{
		super(selected, null, layout);
 		this.label = new Label(fieldLabel);
 
 	}
 
 	@Override
 	public void setVisible(boolean visible)
 	{
 		label.setVisible(visible);
 		super.setVisible(visible);
 	}
 
 	@Override
 	public Label getLabel()
 	{
 		return label;
 	}
 
 	@Override
 	public String getCaption()
 	{
 		return label.getValue();
 	}
 
 }
