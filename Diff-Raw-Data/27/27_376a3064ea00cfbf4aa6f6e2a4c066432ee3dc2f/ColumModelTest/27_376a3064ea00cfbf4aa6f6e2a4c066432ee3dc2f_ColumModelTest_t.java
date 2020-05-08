 package com.github.colmodel.test;
 
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import com.github.springrest.base.ColEditor;
 import com.github.springrest.base.ColModel;
 import com.github.springrest.base.ColModelProfile;
 import com.github.springrest.base.EditorRule;
 import com.github.springrest.util.ColModelFactory;
 
 public class ColumModelTest extends TestCase {
 	private ColModelProfile colModelProfile = null;
 	private ColModel testColModel = null;

 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
		colModelProfile = new ColModelFactory("/colmodel/", null, 4).getColModel("StockRecordLine-colmodel.xml", null);
 		Assert.assertNotNull(colModelProfile);
 		Iterator<ColModel> it = colModelProfile.getColModels().iterator();
 		while (it.hasNext()) {
 			ColModel colmodel = it.next();
 			if (colmodel.getName().equals("quantity")) {
 				testColModel = colmodel;
 				break;
 			}
 		}
 		Assert.assertNotNull(testColModel);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		colModelProfile = null;
 		testColModel = null;
 		super.tearDown();
 	}
 
 	public void testEditorType() {
 		Assert.assertEquals("omNumberField", testColModel.getEditor().getType());
 	}
 
 	public void testEditorRules() {
 		ColEditor editor = testColModel.getEditor();
 		Assert.assertNotNull(editor);
 		List<EditorRule> rules = editor.getRules();
		Assert.assertTrue(rules != null && rules.size() == 1);
		EditorRule editorRule = rules.get(0);
 		Assert.assertNotNull(editorRule);
 		Assert.assertEquals("required", editorRule.getMethod());
 		Assert.assertEquals("true", editorRule.getValue());
 		Assert.assertEquals("数量是必填的", editorRule.getMessage());

 	}
 
 }
