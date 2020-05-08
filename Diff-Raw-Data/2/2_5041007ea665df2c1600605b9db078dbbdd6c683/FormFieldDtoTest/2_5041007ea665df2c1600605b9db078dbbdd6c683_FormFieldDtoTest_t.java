 package test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.akjava.lib.common.form.FormFieldData;
 import com.akjava.lib.common.form.FormFieldDataDto;
 import com.akjava.lib.common.form.Modifier;
 import com.akjava.lib.common.form.Modifiers;
 import com.akjava.lib.common.form.StaticValidators;
 import com.akjava.lib.common.form.Validator;
 import com.akjava.lib.common.tag.LabelAndValue;
 
 public class FormFieldDtoTest extends TestCase{
 
 	public void testSimpleConvertCsv1(){
 		String collect="name\t\ttext\t\t\t\t\t\t\t";
 		FormFieldData field=new FormFieldData();
 		field.setName("name");
 		
 		assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 	}
 	
 	
 	
 	//without validator and options
 	public void testSimpleConvertCsv2(){
 		String collect="name\t" +
 				"key\t" +
 				"text\t" +
 				"\t" +
 				"default\t" +
 				"yes\t" +
 				"\t"+
 				"\t"+
 				"placeholder\t" +
 				"comment";
 		FormFieldData field=new FormFieldData();
 		field.setName("name");
 		field.setKey("key");
 		field.setCreateAuto(true);
 		field.setPlaceHolder("placeholder");
 		field.setDefaultValue("default");
 		field.setComment("comment");
 		
 		assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 	}
 	//validator
 	public void testSimpleConvertCsv3(){
 		String collect="name\t" +
 				"key\t" +
 				"text\t" +
 				"\t" +
 				"default\t" +
 				"yes\t" +
 				"notempty\t"+
 				"\t"+
 				"placeholder\t" +
 				"comment";
 		FormFieldData field=new FormFieldData();
 		field.setName("name");
 		field.setKey("key");
 		field.setCreateAuto(true);
 		field.setPlaceHolder("placeholder");
 		field.setDefaultValue("default");
 		field.setComment("comment");
 		
 		List<Validator> validators=new ArrayList<Validator>();
 		validators.add(StaticValidators.notEmptyValidator());
 		
 		field.setValidators(validators);
 		
 		assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 	}
 	
 	//option values
 	public void testSimpleConvertCsv4(){
 		String collect="name\t" +
 				"key\t" +
 				"text\t" +
 				"test1,test2\t" +
 				"default\t" +
 				"yes\t" +
 				"\t"+
 				"\t"+
 				"placeholder\t" +
 				"comment";
 		FormFieldData field=new FormFieldData();
 		field.setName("name");
 		field.setKey("key");
 		field.setCreateAuto(true);
 		field.setPlaceHolder("placeholder");
 		field.setDefaultValue("default");
 		field.setComment("comment");
 		
 		List<LabelAndValue> optionValues=new ArrayList<LabelAndValue>();
 		optionValues.add(new LabelAndValue("test1"));
 		optionValues.add(new LabelAndValue("test2"));
 		field.setOptionValues(optionValues);
 		
 		assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 	}
 	
 	//full
 		public void testSimpleConvertCsv5(){
 			String collect="name\t" +
 					"key\t" +
 					"text\t" +
 					"test1,test2\t" +
 					"default\t" +
 					"yes\t" +
 					"notempty,asciichar\t"+
 					"\t"+
 					"placeholder\t" +
 					"comment";
 			FormFieldData field=new FormFieldData();
 			field.setName("name");
 			field.setKey("key");
 			field.setCreateAuto(true);
 			field.setPlaceHolder("placeholder");
 			field.setDefaultValue("default");
 			field.setComment("comment");
 			
 			List<LabelAndValue> optionValues=new ArrayList<LabelAndValue>();
 			optionValues.add(new LabelAndValue("test1"));
 			optionValues.add(new LabelAndValue("test2"));
 			field.setOptionValues(optionValues);
 			
 			List<Validator> validators=new ArrayList<Validator>();
 			validators.add(StaticValidators.notEmptyValidator());
 			validators.add(StaticValidators.asciiCharOnly());
 			
 			field.setValidators(validators);
 			
 			assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 		}
 		
 		public void testSimpleConvert1(){
 			String collect="name\t\ttext\t\t\t\t\t\t\t";
 			
 			FormFieldData field=FormFieldDataDto.csvToFormField(collect);
 			
 			
 			assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 		}
 		
 
 		
 		//without validator and options
 		public void testSimpleConvert2(){
 			String collect="name\t" +
 					"key\t" +
 					"text\t" +
 					"\t" +
 					"default\t" +
 					"yes\t" +
 					"\t"+
 					"\t"+
 					"placeholder\t" +
 					"comment";
 			FormFieldData field=FormFieldDataDto.csvToFormField(collect);
 			
 			assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 		}
 		//validator
 		public void testSimpleConvert3(){
 			String collect="name\t" +
 					"key\t" +
 					"text\t" +
 					"\t" +
 					"default\t" +
 					"yes\t" +
 					"notempty\t"+
 					"linetobr\t"+
 					"placeholder\t" +
 					"comment";
 			FormFieldData field=FormFieldDataDto.csvToFormField(collect);
 			
 			List<Validator> validators=new ArrayList<Validator>();
 			validators.add(StaticValidators.notEmptyValidator());
 			
 			field.setValidators(validators);
 			
 			List<Modifier> modifiers=new ArrayList<Modifier>();
			modifiers.add(Modifiers.getLineToBrModifier());
 			
 			field.setModifiers(modifiers);
 			
 			assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 		}
 		
 		//option values
 		public void testSimpleConvert4(){
 			String collect="name\t" +
 					"key\t" +
 					"text\t" +
 					"test1:0,test2:1\t" +
 					"default\t" +
 					"yes\t" +
 					"\t"+
 					"\t"+
 					"placeholder\t" +
 					"comment";
 			FormFieldData field=FormFieldDataDto.csvToFormField(collect);
 			
 			assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 		}
 		
 		//full
 			public void testSimpleConvert5(){
 				String collect="name\t" +
 						"key\t" +
 						"text\t" +
 						"test1:0,test2:1\t" +
 						"default\t" +
 						"yes\t" +
 						"notempty,asciichar\t"+
 						"linetobr,sanitize\t"+
 						"placeholder\t" +
 						"comment";
 				
 				FormFieldData field=FormFieldDataDto.csvToFormField(collect);
 				
 				assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 			}
 			
 			//how check work
 			public void testSimpleConvert6(){
 				String collect="name\t" +
 						"key\t" +
 						"check\t" +
 						"YES:YES,NO:NO\t" +
 						"default\t" +
 						"yes\t" +
 						"notempty,asciichar\t"+
 						"linetobr,sanitize\t"+
 						"placeholder\t" +
 						"comment";
 				
 				FormFieldData field=FormFieldDataDto.csvToFormField(collect);
 				
 				assertEquals(collect, FormFieldDataDto.formFieldToCsv(field));
 			}
 }
