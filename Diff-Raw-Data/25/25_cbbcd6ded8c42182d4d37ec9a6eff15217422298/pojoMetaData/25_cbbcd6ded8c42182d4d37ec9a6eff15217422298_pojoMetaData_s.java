 package monbulk.shared.Model.pojo;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Element;
 
 import monbulk.shared.Form.FormBuilder;
 import monbulk.shared.Form.FormField;
 import monbulk.shared.Form.iFormField;
 
 import monbulk.shared.Model.IPojo;
 
 public class pojoMetaData implements IPojo{
 
 	private String MetaDataName;
 	private FormBuilder MetaDataConstants;
 	private String MetaDataNameSpace;
 	private Boolean isPublic;
 	private Boolean isMandatory;
 	 
 	public static final String FormName = "MetaDataDetail";
 	public static final String MetaDataNameField = "MetaDataName";
 	public static final String MetaDataNameSpaceField = "MetaDataNameSpace";
 	public static final String IsPublicField = "isPublic";
 	public static final String IsMandatoryField = "isMandatory";
 	
 	
 	public pojoMetaData(String MetaDataName)
 	{
 		this.MetaDataName = MetaDataName;
 	}
 	public pojoMetaData(String MetaDataName,String NameSpace)
 	{
 		this.MetaDataName = MetaDataName;
 		this.MetaDataNameSpace = NameSpace;
 	}
 	@Override
 	public void saveForm(FormBuilder input) {
 		Iterator<iFormField> i = input.getFormDetails().iterator();		
 		while(i.hasNext())
 		{
 			iFormField tmpItem = i.next();
 			if(tmpItem.hasValue())
 			{
 				setFieldVale(tmpItem.GetFieldName(),tmpItem.GetFieldValue());
 			}
 		}
 		
 	}
 	public void addMetaDataField(iFormField Field)
 	{
 		this.MetaDataConstants.addField(Field); 
 	}
 	public void removeMetaDataField(iFormField Field)
 	{
 		this.MetaDataConstants.RemoveFormItem(Field.GetFieldName(),true); 
 	}
 	@Override
 	public FormBuilder getFormStructure() {
 		// TODO Auto-generated method stub
 		FormBuilder MetaDataForm = new FormBuilder();
 		MetaDataForm.SetFormName(FormName);
 		/*
 		 * Set IsPublic field
 		 */
 		FormField tmpField = new FormField(IsPublicField,"Boolean");
 		tmpField.setAsSummaryField();
 		if(this.isPublic.equals(null))
 		{
 			tmpField.SetFieldValue(this.isPublic.toString());
 		}
 		MetaDataForm.addField(tmpField);
 		
 		/*
 		 * Set isMandatory Field
 		 */
 		FormField tmpField2 = new FormField(IsMandatoryField,"Boolean");
 		tmpField.setAsSummaryField();
 		if(this.isMandatory.equals(null))
 		{
 			tmpField.SetFieldValue(this.isMandatory.toString());
 		}
 		MetaDataForm.addField(tmpField2);
 		
 		/*
 		 * Set Name field
 		 */
 		FormField tmpField3 = new FormField(MetaDataNameField,"String");
 		tmpField3.setAsSummaryField();
 		if(this.MetaDataName.equals(null))
 		{
 			tmpField3.SetFieldValue(this.MetaDataName);
 		}
 		MetaDataForm.addField(tmpField3);
 		
 		/*
 		 * Set Namespace Field
 		 */
 		FormField tmpField4 = new FormField(MetaDataNameSpaceField,"String");
 		tmpField4.setAsSummaryField();
 		if(this.MetaDataNameSpace.equals(null))
 		{
 			tmpField4.SetFieldValue(this.MetaDataNameSpace);
 		}
 		MetaDataForm.addField(tmpField4);
 		
 		if(MetaDataConstants.getFormDetails().size() > 0)
 		{
 			MetaDataForm.MergeForm(MetaDataConstants);
 		}
 		
 		return MetaDataForm;
 		
 	}
 	@Override
 	public void deserialise() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void deserialiseFromList(String XML) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public String writeOutput(String Format) {
 		// TODO Auto-generated method stub
 		if(Format=="TCL")
 		{
 			return WriteTCL();
 		}
 		else
 		{
 			return "";
 		
 		}
 	}
 	public void AppendXML(Element xml,Document doc)
 	{
 		Element node1 = doc.createElement("metadata");
 		Element node2 = doc.createElement("definition");
 		if(isMandatory!=null)
 		{
 			if(isMandatory)
 			{
 				node2.setAttribute("requirement", "mandatory");
 			}
 			else
 			{
 				node2.setAttribute("requirement", "optional");
 			}
 		}
 		else
 		{
 			node2.setAttribute("requirement", "mandatory");
 		}
 		node2.appendChild(doc.createTextNode(this.MetaDataName));
 		//.setNodeValue(this.MetaDataName);
 		node1.appendChild(node2);
 		xml.appendChild(node1);
 	}
 	private String WriteTCL()
 	{
 		if(isMandatory)
 		{
 			return ":metadata < :definition -requirement mandatory " + MetaDataName +" > \\";
 		}
 		else
 		{
 			return ":metadata < :definition " + MetaDataName +" > \\";
 		}
 	}
 	
 	@Override
 	public void setFieldVale(String FieldName, Object FieldValue) {
 		//We may need to split up FieldName from former merges
 		if(FieldName == MetaDataNameField)
 		{
 			this.MetaDataName = FieldValue.toString();
 		}
 		else if(FieldName == MetaDataNameSpaceField)
 		{
 			this.MetaDataNameSpace = FieldValue.toString();
 		}
 		else if(FieldName == IsPublicField)
 		{
			this.isPublic = (Boolean) FieldValue;
 		}
 		else if(FieldName == IsMandatoryField)
 		{
 			this.isMandatory= (Boolean) FieldValue;
 		}
 		else if(!this.MetaDataConstants.getFieldItemForName(FieldName).equals(null))
 		{
 			this.MetaDataConstants.getFieldItemForName(FieldName).SetFieldValue((String) FieldValue);
 		}
 		
 	}
 	
 	@Override
 	public String getFieldVale(String FieldName) {
 		// TODO Auto-generated method stub
 		if(FieldName == MetaDataNameField)
 		{
 			return this.MetaDataName;
 		}
 		else if(FieldName == MetaDataNameSpaceField)
 		{
 			return this.MetaDataNameSpace;
 		}
 		else if(FieldName == IsPublicField)
 		{
			return this.isPublic.toString();
 		}
 		else if(FieldName == IsMandatoryField)
 		{
			return this.isMandatory.toString();
 		}
 		else if(!this.MetaDataConstants.getFieldItemForName(FieldName).equals(null))
 		{
 			return this.MetaDataConstants.getFieldItemForName(FieldName).GetFieldValue().toString();
 		}
 		else
 		{
 			return "";
 		}
 	}
 	@Override
 	public void readInput(String Format, Object Input) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 
 }
