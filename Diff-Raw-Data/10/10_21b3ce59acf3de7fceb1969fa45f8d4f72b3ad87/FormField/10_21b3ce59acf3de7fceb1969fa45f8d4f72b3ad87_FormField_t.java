 package monbulk.shared.Form;
 
 import java.util.Date;
 
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent.Type;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import monbulk.shared.Form.DateValidation;
 import monbulk.shared.Form.IntegerValidation;
 import monbulk.shared.Form.StringValidation;
 import monbulk.shared.Form.TextFieldValidation;
 import monbulk.shared.util.GWTLogger;
 /**
  * This is the basic Form Field class
  * TODO: It needs an enum for FieldTypes
  * 
  * @author Andrew Glenn
  *
  */
 	public class FormField implements iFormField
 	{
 		protected String FieldName;
 		protected String FieldType;
 		protected Object FieldValue;
 		protected String FieldWidget;
 		protected Widget _FieldVaLueWidget;
 		protected Widget _ValidationWidget;
 		protected iFormFieldValidation fieldValidator;
 		protected Boolean isSummaryField;
 		protected Boolean isTitleField;
 		protected Boolean isStatic;
 		protected Boolean hasValue;
 		protected FormWidget returnWidget;
 		
 		
 		public FormField(String FormName,String objectType)
 		{
 			//
 			this.FieldValue="";
 			this.FieldName = FormName;
 			this.FieldType = objectType;
 			setFieldWidget();
 			isSummaryField = false;
 			isTitleField = false;
 			hasValue = false;
 			isStatic = false;
 		}
 		public FormField(String FormName,String objectType,Boolean shouldLoad)
 		{
 			//
 			if(!shouldLoad)
 			{
 				this.FieldName = FormName;
 				this.FieldType = objectType;
 				this.FieldValue="";
 				isSummaryField = false;
 				isTitleField = false;
 				hasValue = false;
 				isStatic = false;
 			}
 		}
 		public FormField(String FormName,String objectType, Object Value)
 		{
 			this.FieldValue="";
 			this.FieldName = FormName;
 			this.FieldType = objectType;
 			this.FieldValue = Value;
 			setFieldWidget();
 			this.FieldValue = Value;
 			this.FieldWidget="Label";
 			this.fieldValidator = null;
 			isSummaryField = false;
 			isTitleField = false;
 			isStatic = false;
 			hasValue = true;
 		}
 		
 		@Override
 		public Boolean hasValue()
 		{
 			return this.hasValue;
 		}
 		@Override
 		public void setAsSummaryField()
 		{
 			this.isSummaryField = true;
 		}
 		@Override
 		public void setAsTitleField()
 		{
 			this.isTitleField = true;
 		}
 		@Override
 		public Boolean isStatic()
 		{
 			return this.isStatic;
 		}
 		public void setStatic(Boolean isStatic)
 		{
 			this.isStatic = isStatic;
 			this.setFieldWidget();
 		}
 		@Override
 		public Boolean isSummary()
 		{
 			return this.isSummaryField;
 		}
 		@Override
 		public Boolean isTitle()
 		{
 			return this.isTitleField;
 		}
 		private void setFieldWidget()
 		{		
 			Label FieldNameLabel = new Label();
 			FieldNameLabel.setText(this.FieldName);
 			this._ValidationWidget = new Label();
 			this.returnWidget = new FormWidget(this.FieldName);
 			GWTLogger.Log("Adding Field:" + FieldType + ":" + FieldName, "FormField", "setFieldWidget", "126");
 			if(FieldType=="String")
 			{
 				this.FieldWidget="TextBox";
 				
 				TextBox tmpBox = new TextBox();
 				
 				//this._FieldVaLueWidget.add
 				
 				this._ValidationWidget = new Label();
 				
 				if(this.isStatic!=null)
 				{
 					if(this.isStatic)
 					{
 						tmpBox.setEnabled(false);
 					}
 				}
 				this._FieldVaLueWidget = tmpBox;
 				this.fieldValidator = new StringValidation(this.FieldName);
 			}
 			else if(FieldType=="Date")
 			{
 				this.FieldWidget="Calendar";
 				Date tmpDate = new Date();
 				this.FieldValue= tmpDate.toString();
 				this.fieldValidator = new DateValidation(this.FieldName);
 				this._FieldVaLueWidget = new TextBox();
 			}
 			else if(FieldType=="Description")
 			{
 				this.FieldWidget="TextArea";
 			
 				this.fieldValidator = new TextFieldValidation(this.FieldName);
 				this._FieldVaLueWidget = new TextArea();
 				this._FieldVaLueWidget.setHeight("100px");
 				//Window.alert("A TextArea is added");
 				
 				
 			}
 			else if(FieldType=="Int")
 			{
 				this.FieldWidget="TextBox";
 				this.FieldValue="0";
 				this.fieldValidator = new IntegerValidation(this.FieldName);
 				this._FieldVaLueWidget = new TextBox();
 			}
 			else if(FieldType.contains("Boolean"))
 			{
 				this.FieldWidget="CheckBox";
 				this.FieldValue=false;
 				this.fieldValidator = null;
 				this._FieldVaLueWidget = new CheckBox();
 				
 			}
 			else if(FieldType=="List")
 			{
 				this.FieldWidget="ListBox";
 				this.FieldValue=false;
 				this.fieldValidator = null;
 				this._FieldVaLueWidget = new Widget();
 			}
 			else
 			{
 				this.FieldWidget="TextBox";
 				this.FieldValue="";
 				this.fieldValidator = new IntegerValidation(this.FieldName);
 				this._FieldVaLueWidget = new TextBox();
 			}
 			//FieldNameLabel.setHorizontalAlignment(HorizontalAlignmentConstant.endOf(Direction.LTR));
 			//FieldNameLabel.setWidth("80px");
 			//_FieldVaLueWidget.setPixelSize(200, 30);
 			//this._ValidationWidget.setSize("30px", "30px");
 			this.returnWidget.setLabelWidget(FieldNameLabel);
 			
 			this.returnWidget.setFormWidget(_FieldVaLueWidget);
 			this.returnWidget.setValidWidget(_ValidationWidget);
 			
 		}
 		@Override
 		public String GetFieldName() {
 			// TODO Auto-generated method stub
 			return FieldName;
 		}
 
 		@Override
 		public Object GetFieldValue() {
 			// TODO Auto-generated method stub
 			return FieldValue;
 		}
 
 		@Override
 		public String getFieldTypeName() {
 			// TODO Auto-generated method stub
 			return FieldType;
 		}
 
 		@Override
 		public String GetWidgetName() {
 			// TODO Auto-generated method stub
 			return FieldWidget;
 		}
 		@Override
 		public void SetFieldValue(String FieldValue) {
 			// TODO Auto-generated method stub
 			if(this.Validate(FieldValue)=="")
 			{
 				this.FieldValue = FieldValue;
 				hasValue = true;
 			}
 		}
 		@Override
 		public iFormFieldValidation GetFieldValidation() {
 			// TODO Auto-generated method stub
 			return fieldValidator;
 		}
 
 		@Override
 		public void setFieldName(String newName) {
 			this.FieldName=newName;
 			
 		}
 
 		@Override
 		public FormWidget getWidgetReference() {
 			
 			return returnWidget;
 		}
 
 		@Override
 		public String Validate(String FieldValue) {
 			if(this.fieldValidator!=null)
			{
 				if(this.fieldValidator.isValueValid(FieldValue))
 				{
 					this._ValidationWidget.setStyleName("Valid");
 					Label tmpLabel = (Label)this._ValidationWidget;
 					tmpLabel.setVisible(true);
 					tmpLabel.setText("");
 					return "";
 				}
 				else
 				{
 					this._ValidationWidget.setStyleName("InValid");
 					return fieldValidator.getInvalidReason();
 				}
 			}
			else
			{
				this._ValidationWidget.setStyleName("Valid");
				Label tmpLabel = (Label)this._ValidationWidget;
				tmpLabel.setVisible(true);
				tmpLabel.setText("");
			}
 			return "";
 		}
 
 		@Override
 		public <H extends EventHandler> HandlerRegistration addHandler(
 				H handler, Type<H> type) {
 			return this._FieldVaLueWidget.addHandler(handler, type);
 		}
 		
 }
