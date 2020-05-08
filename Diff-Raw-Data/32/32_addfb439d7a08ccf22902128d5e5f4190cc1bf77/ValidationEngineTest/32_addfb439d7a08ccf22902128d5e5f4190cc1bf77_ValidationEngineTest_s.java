 package com.google.codes.dryvalidator;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ScriptableObject;
 
 import com.google.codes.dryvalidator.dto.FormItem;
 import com.google.codes.dryvalidator.dto.Validation;
 
 
 public class ValidationEngineTest {
 	static ValidationEngine validationEngine;
 
 	@BeforeClass
 	public static void validationEngineを初期化する() {
 		validationEngine = new ValidationEngine();
 		validationEngine.setup();
 	}
 
 	@AfterClass
 	public static void validationEngineを破棄する() {
 		validationEngine.dispose();
 	}
 
 	private FormItem getFormItem() {
 		FormItem formItem = new FormItem();
 		formItem.setId("NAME");
 		formItem.setLabel("氏名");
     	formItem.getValidations().addValidation(new Validation("required", "true"));
     	formItem.getValidations().addValidation(new Validation("maxLength", "10"));
     	formItem.getValidations().addValidation(new Validation("letterType", "Alnum+Punct"));
     	return formItem;
 	}
 
 	@Test
 	public void exec() {
 		validationEngine.register(getFormItem());
 		List<String> messages = validationEngine.exec("NAME", "A1- ");
 		System.out.println(messages);
 		validationEngine.unregisterAll();
 	}
 
 	@Test
 	public void requiredのテスト() {
 		FormItem formItem = new FormItem();
 		formItem.setId("NAME");
 		formItem.setLabel("氏名");
     	formItem.getValidations().addValidation(new Validation("required", "false"));
 		validationEngine.register(formItem);
 		Assert.assertTrue(validationEngine.exec("NAME", "").isEmpty());
 		Assert.assertTrue(validationEngine.exec("NAME", null).isEmpty());
 		Assert.assertTrue(validationEngine.exec("NAME", "abc").isEmpty());
 		validationEngine.unregisterAll();
 
 		formItem.getValidations().clear();
     	formItem.getValidations().addValidation(new Validation("required", "true"));
 		validationEngine.register(formItem);
 		Assert.assertTrue(!validationEngine.exec("NAME", "").isEmpty());
 		Assert.assertTrue(!validationEngine.exec("NAME", null).isEmpty());
 		Assert.assertTrue(validationEngine.exec("NAME", "abc").isEmpty());
 		validationEngine.unregisterAll();
 
 
 	}
 
 	@Test
 	public void maxLengthのテスト() {
 		FormItem formItem = new FormItem();
 		formItem.setId("NAME");
 		formItem.setLabel("氏名");
     	formItem.getValidations().addValidation(new Validation("maxLength", "10"));
 		validationEngine.register(formItem);
 		// 未入力の場合はチェックしない
 		Assert.assertTrue(validationEngine.exec("NAME", "").isEmpty());
 		Assert.assertTrue(validationEngine.exec("NAME", null).isEmpty());
 
 		//10文字以内はOK
 		Assert.assertTrue(validationEngine.exec("NAME", "abc").isEmpty());
 
 		// 10文字超えると駄目
 		Assert.assertTrue(validationEngine.exec("NAME",  "0123456789").isEmpty());
 		Assert.assertTrue(!validationEngine.exec("NAME", "01234567891").isEmpty());
 
 		// 全角でも文字で数える
 		Assert.assertTrue(validationEngine.exec("NAME",  "０１２３４５６７８９").isEmpty());
 		Assert.assertTrue(!validationEngine.exec("NAME", "０１２３４５６７８９１").isEmpty());
 
 		validationEngine.unregisterAll();
 	}
 
 
 	public void characterClassを動かす() {
 		Context ctx = validationEngine.getContext();
 		ScriptableObject scope = validationEngine.getGlobalScope();
 
 		ctx.evaluateString(scope,
 			"DRYValidator.CharacterClass.enable(['Hiragana', 'Katakana', 'Unknown']);"
 			+ "console.log('enable='+DRYValidator.CharacterClass.enable());"
 				, "<cmd>", 1, null);
 	}
 
 	@Test
 	public void selectionの動き() {
 		FormItem formItem = new FormItem();
 		formItem.setId("NAME");
 		formItem.setLabel("氏名");
 
     	formItem.getValidations().addValidation(new Validation("Selection", "{min:1}"));
 		validationEngine.register(formItem);
 		// 未入力の場合はチェックしない
 		Assert.assertFalse(validationEngine.exec("NAME", new String[]{""}).isEmpty());
 		Assert.assertTrue(validationEngine.exec("NAME", new String[]{"asdf"}).isEmpty());
 		Assert.assertFalse(validationEngine.exec("NAME", "").isEmpty());
 		Assert.assertTrue(validationEngine.exec("NAME", "hoge").isEmpty());
 		validationEngine.unregisterAll();
 
     	formItem.getValidations().addValidation(new Validation("Selection", "{max:2}"));
 		validationEngine.register(formItem);
 		Assert.assertTrue(validationEngine.exec("NAME", "hoge").isEmpty());
 		Assert.assertFalse(validationEngine.exec("NAME", new String[]{"a","b","c"}).isEmpty());
 		validationEngine.unregisterAll();
 	}
 
 	@Test
 	public void 数値の範囲チェック() {
 		FormItem formItem = new FormItem();
 		formItem.setId("FEE");
 		formItem.setLabel("金額");
 
     	formItem.getValidations().addValidation(new Validation("Range", "{min:5}"));
 		validationEngine.register(formItem);
 
 		Assert.assertTrue("未入力の場合はチェックしない", validationEngine.exec("FEE", "").isEmpty());
 		Assert.assertFalse("最小5の場合4はNG", validationEngine.exec("FEE", "4").isEmpty());
 		Assert.assertTrue("最小5の場合5はOK", validationEngine.exec("FEE", "5").isEmpty());
 		validationEngine.unregisterAll();
 
     	formItem.getValidations().addValidation(new Validation("Range", "{max:5}"));
 		validationEngine.register(formItem);
 		Assert.assertTrue("最大5の場合5はOK", validationEngine.exec("FEE", "5").isEmpty());
 		Assert.assertFalse("最大5の場合6はNG", validationEngine.exec("FEE", "6").isEmpty());
 		validationEngine.unregisterAll();
 	}
 
 	@Test
	public void ネストしたフォーム() throws Exception {
 		// 入力チェックのを定義する
 		FormItem formItem = new FormItem();
 		formItem.setId("familyList[].name");
 		formItem.setLabel("氏名");
     	formItem.getValidations().addValidation(new Validation("maxLength", "2"));
 		validationEngine.register(formItem);
 		Map<String, Object> formValues = new HashMap<String, Object>();
 		List<FamilyDto> familyList = new ArrayList<FamilyDto>();
 		FamilyDto family1 = new FamilyDto();
 		family1.name = "太郎";
 		familyList.add(family1);
 		FamilyDto family2 = new FamilyDto();
 		family2.name = "次郎長";
 		familyList.add(family2);
 		formValues.put("familyList", familyList);
 		Map<String, List<String>> messages = validationEngine.exec(formValues);
		Assert.assertEquals(1, messages.size());
 		System.out.println(messages);
 		validationEngine.unregisterAll();
 	}
 
 
 	public static class FamilyDto implements Serializable {
 		private static final long serialVersionUID = 1059538278850982523L;
 		public String name;
 	}
 }
 
