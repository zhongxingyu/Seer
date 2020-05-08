 package org.otherobjects.cms.binding;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.math.RoundingMode;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.test.BaseJcrTestCase;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 
 public class BindServiceImplTest extends BaseJcrTestCase
 {
     private final String dateFormat = "dd/MM/yy";
     private BindServiceImpl bindService;
     protected Date now = new Date();
     private Date testDate;
 
     @Override
     protected void onSetUp() throws Exception
     {
         super.onSetUp();
         this.bindService = new BindServiceImpl();
         this.bindService.setDateFormat(this.dateFormat);
         this.bindService.setDaoService(this.daoService);
         registerType(TestObject.class);
         registerType(TestReferenceObject.class);
         registerType(TestComponentObject.class);
         testDate = new SimpleDateFormat(this.dateFormat).parse("01/01/99");
     }
 
     public void testBindValues() throws Exception
     {
         TestObject o = new TestObject();
         BindingResult errors = this.bindService.bind(o, o.getTypeDef(), getRequest());
 
         assertTrue(errors.getErrorCount() == 0);
 
         // Simple properties
         assertEquals("testString1", PropertyUtils.getNestedProperty(o, "testString"));
         assertEquals("testText1", PropertyUtils.getNestedProperty(o, "testText"));
         assertEquals(this.testDate, PropertyUtils.getNestedProperty(o, "testDate"));
         assertEquals(this.testDate, PropertyUtils.getNestedProperty(o, "testTime"));
         assertEquals(this.testDate, PropertyUtils.getNestedProperty(o, "testTimestamp"));
         assertEquals(new Long(7), PropertyUtils.getNestedProperty(o, "testNumber"));
         assertEquals(new BigDecimal(2.7, new MathContext(2, RoundingMode.HALF_UP)), PropertyUtils.getNestedProperty(o, "testDecimal"));
         assertEquals(Boolean.FALSE, PropertyUtils.getNestedProperty(o, "testBoolean"));
 
         /// Component
         assertEquals("myName", PropertyUtils.getNestedProperty(o, "testComponent.name"));
 
         // Reference
         assertEquals("TR1 Name", PropertyUtils.getNestedProperty(o, "testReference.name"));
     }
 
     @SuppressWarnings("unchecked")
     public void testBindList() throws Exception
     {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("testStringsList[0]", "testString1");
 
         TestObject o = new TestObject();
         this.bindService.bind(o, o.getTypeDef(), request);
 
         assertTrue(List.class.isAssignableFrom(PropertyUtils.getSimpleProperty(o, "testStringsList").getClass()));
         List testStringList = (List) PropertyUtils.getSimpleProperty(o, "testStringsList");
         assertEquals("testString1", testStringList.get(0));
     }
 
     public void testBindComponentList() throws Exception
     {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("testComponentsList[0].name", "testName");
 
         TestObject o = new TestObject();
         this.bindService.bind(o, o.getTypeDef(), request);
 
         assertTrue(List.class.isAssignableFrom(PropertyUtils.getSimpleProperty(o, "testComponentsList").getClass()));
         assertTrue(BaseNode.class.isAssignableFrom(PropertyUtils.getIndexedProperty(o, "testComponentsList", 0).getClass()));
         assertEquals("testName", PropertyUtils.getSimpleProperty(PropertyUtils.getIndexedProperty(o, "testComponentsList", 0), "name"));
     }
 
     public void testBindReferenceList() throws Exception
     {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("testReferencesList[2]", "ae9a48f6-8e4e-4bad-8954-3baea6ed416f");
 
         TestObject o = new TestObject();
         this.bindService.bind(o, o.getTypeDef(), request);
 
         assertTrue(List.class.isAssignableFrom(PropertyUtils.getSimpleProperty(o, "testReferencesList").getClass()));
         // FIXME This needs fixing
         System.err.println("************** SKIPPING " + getClass().getName() + ".testBindReferenceList ********************");
         //        assertTrue(BaseNode.class.isAssignableFrom(PropertyUtils.getIndexedProperty(o, "testReferencesList", 2).getClass()));
     }
 
     public void testDeepNestList() throws Exception
     {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("testComponentsList[0].componentsList[1].stringsList[0]", "testDeepName2");
         request.addParameter("testComponentsList[0].componentsList[0].stringsList[0]", "testDeepName");
         request.addParameter("testComponentsList[0].componentsList[2].stringsList[0]", "testDeepName3");
 
         TestObject o = new TestObject();
         this.bindService.bind(o, o.getTypeDef(), request);
 
         Object deepComponentListEntry = PropertyUtils.getIndexedProperty(o, "testComponentsList", 0);
         Object firstLevelWithComponentsList = PropertyUtils.getIndexedProperty(deepComponentListEntry, "componentsList", 2);
         Object simpleListEntry = PropertyUtils.getIndexedProperty(firstLevelWithComponentsList, "stringsList", 0);
 
         assertEquals("testDeepName3", simpleListEntry);
 
     }
 
     public void testDeepNestComponent() throws Exception
     {
         TestReferenceObject r1 = createAndSaveReference("R1");
        
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("testComponent.component.reference", r1.getId());
         request.addParameter("testComponent.component.name", "testSubName");
         request.addParameter("testComponent.name", "testSuperName");
 
         TestObject o = new TestObject();
         this.bindService.bind(o, o.getTypeDef(), request);
 
         assertEquals("testSuperName", PropertyUtils.getNestedProperty(o, "testComponent.name"));
         assertEquals("testSubName", PropertyUtils.getNestedProperty(o, "testComponent.component.name"));
 
         // FIXME Deep references are broken
         //assertTrue(BaseNode.class.isAssignableFrom(PropertyUtils.getNestedProperty(baseNode, "testDeepComponent.subComponent.reference").getClass()));
     }
 
     public void testBindError()
     {
 
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("testNumber", "abc"); // the wrong one
         request.addParameter("testString", "testString1");
         request.addParameter("testText", "testText1");
         request.addParameter("testDate", "01/01/99");
         request.addParameter("testTime", "01/01/99");
         request.addParameter("testTimestamp", "01/01/99");
         request.addParameter("testDecimal", "2.7");
         request.addParameter("testBoolean", "false");
 
         TestObject o = new TestObject();
         BindingResult errors = this.bindService.bind(o, o.getTypeDef(), request);
 
         assertTrue(errors.getErrorCount() == 1);
 
         FieldError numberFieldError = errors.getFieldError("testNumber");
         assertTrue(numberFieldError.getCode().equals("typeMismatch"));
     }
 
     private MockHttpServletRequest getRequest()
     {
         adminLogin();
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("testString", "testString1");
         request.addParameter("testText", "testText1");
         request.addParameter("testDate", "01/01/99");
         request.addParameter("testTime", "01/01/99");
         request.addParameter("testTimestamp", "01/01/99");
         request.addParameter("testNumber", "7");
         request.addParameter("testDecimal", "2.7");
         request.addParameter("testBoolean", "false");
         request.addParameter("testComponent.name", "myName");
 
         BaseNode tr1 = createAndSaveReference("TR1");
         request.addParameter("testReference", tr1.getId());
         logout();
         return request;
     }
 
     protected TestObject createTestObject()
     {
         TestObject o = new TestObject();
         o.setTestString("testString");
         o.setTestText("testText");
         o.setTestDate(this.now);
         o.setTestTime(this.now);
         o.setTestTimestamp(this.now);
         o.setTestNumber(new Long(1));
         o.setTestDecimal(new BigDecimal(1.0));
         o.setTestBoolean(Boolean.FALSE);
         o.setPath("/test");
         o.setCode("testNode");
         return o;
     }
 
     protected TestReferenceObject createAndSaveReference(String name)
     {
         TestReferenceObject r = new TestReferenceObject();
         r.setJcrPath("/" + name + ".html");
         r.setName(name + " Name");
         return (TestReferenceObject) universalJcrDao.save(r);
     }
 
     protected TestComponentObject createComponent(String name)
     {
         TestComponentObject c = new TestComponentObject();
         c.setJcrPath("/" + name + ".html");
         c.setName(name + " Name");
         return (TestComponentObject) universalJcrDao.save(c);
     }
 }
