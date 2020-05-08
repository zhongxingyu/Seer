 package test;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import com.akjava.lib.common.form.FormFieldData;
 import com.akjava.lib.common.form.FormFieldDataDto;
 import com.akjava.lib.common.form.FormFieldDataDto.FormFieldToInputEditTagFunction;
 import com.akjava.lib.common.tag.LabelAndValue;
 import com.akjava.lib.common.tag.Tag;
 
 public class FormFieldToInputTemplateTest extends TestCase{
 
 	public void testText1(){
 		FormFieldData data=new FormFieldData();
 		data.setType(FormFieldData.TYPE_TEXT_SHORT);
 		data.setName("Text");
 		data.setKey("text");
 		
 		
 		
 		Tag tag=FormFieldDataDto.getFormFieldToInputTemplateTagFunction().apply(data);
 		
 		assertEquals("<input type=\"text\" name=\"text\" value=\"${value_text}\"/>", tag.toString());
 	}
 	
 	public void testId1(){
 		FormFieldData data=new FormFieldData();
 		data.setType(FormFieldData.TYPE_ID);
 		data.setName("Id");
 		data.setKey("id");
 		
 		Tag tag=FormFieldDataDto.getFormFieldToInputTemplateTagFunction().apply(data);
 		assertEquals("<input type=\"text\" name=\"id\" value=\"${value_id}\"/>", tag.toString());
 	}
 	
 	public void testTextLong1(){
 		FormFieldData data=new FormFieldData();
 		data.setType(FormFieldData.TYPE_TEXT_LONG);
 		data.setName("TextLong");
 		data.setKey("text_long");
 		
 		Tag tag=FormFieldDataDto.getFormFieldToInputTemplateTagFunction().apply(data);
 		assertEquals("<textarea name=\"text_long\">${value_text_long}</textarea>", tag.toString());
 	}
 	
 	public void testCheck1(){
 		FormFieldData data=new FormFieldData();
 		data.setType(FormFieldData.TYPE_CHECK);
 		data.setName("Check");
 		data.setKey("check");
 		
 		Tag tag=FormFieldDataDto.getFormFieldToInputTemplateTagFunction().apply(data);
 		
		assertEquals("<input type=\"checkbox\" name=\"check\" ${checked_check}/>", tag.toString());
 	}
 	
 	public void testSelect1(){
 		FormFieldData data=new FormFieldData();
 		data.setType(FormFieldData.TYPE_SELECT_SINGLE);
 		data.setName("Single");
 		data.setKey("single");
 		
 		data.getOptionValues().add(new LabelAndValue("test1"));
 		data.getOptionValues().add(new LabelAndValue("test2"));
 		
 		Tag tag=FormFieldDataDto.getFormFieldToInputTemplateTagFunction().apply(data);
 		
		assertEquals("<select name=\"single\">\n<option ${selected_single0}>test1</option>\n<option ${selected_single1}>test2</option></select>", tag.toString());
 	}
 	
 	public void testSelect2(){
 		FormFieldData data=new FormFieldData();
 		data.setType(FormFieldData.TYPE_SELECT_SINGLE);
 		data.setName("Single");
 		data.setKey("single");
 		
 		data.getOptionValues().add(new LabelAndValue("test1","0"));
 		data.getOptionValues().add(new LabelAndValue("test2","1"));
 		
 		Tag tag=FormFieldDataDto.getFormFieldToInputTemplateTagFunction().apply(data);
		assertEquals("<select name=\"single\">\n<option value=\"0\" ${selected_single0}>test1</option>\n<option value=\"1\" ${selected_single1}>test2</option></select>", tag.toString());
 	}
 	public void testSelectMulti1(){
 		FormFieldData data=new FormFieldData();
 		data.setType(FormFieldData.TYPE_SELECT_MULTI);
 		data.setName("Multi");
 		data.setKey("multi");
 		
 		data.getOptionValues().add(new LabelAndValue("test1"));
 		data.getOptionValues().add(new LabelAndValue("test2"));
 		
 		Tag tag=FormFieldDataDto.getFormFieldToInputTemplateTagFunction().apply(data);
 		
		assertEquals("<select name=\"multi\" multiple=\"multiple\">\n<option ${selected_multi0}>test1</option>\n<option ${selected_multi1}>test2</option></select>", tag.toString());
 	}
 	
 	public void testSelectMulti2(){
 		FormFieldData data=new FormFieldData();
 		data.setType(FormFieldData.TYPE_SELECT_MULTI);
 		data.setName("Multi");
 		data.setKey("multi");
 		
 		data.getOptionValues().add(new LabelAndValue("test1"));
 		data.getOptionValues().add(new LabelAndValue("test2"));
 		
 		Tag tag=FormFieldDataDto.getFormFieldToInputTemplateTagFunction().apply(data);
 		
		assertEquals("<select name=\"multi\" multiple=\"multiple\">\n<option ${selected_multi0}>test1</option>\n<option ${selected_multi1}>test2</option></select>", tag.toString());
 	}
 }
