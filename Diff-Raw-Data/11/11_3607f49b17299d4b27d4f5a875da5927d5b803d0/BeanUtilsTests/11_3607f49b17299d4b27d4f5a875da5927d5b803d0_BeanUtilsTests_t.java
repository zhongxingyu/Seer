 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.common;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.common.BeanUtils;
 import org.xins.common.types.standard.Date;
 
 import com.mycompany.allinone.capi.SimpleTypesRequest;
 import com.mycompany.allinone.capi.DefinedTypesRequest;
 import com.mycompany.allinone.types.Salutation;
 import com.mycompany.allinone.types.TextList;
 import org.xins.common.xml.Element;
 
 /**
  * Tests for class <code>BeanUtils</code>
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public class BeanUtilsTests extends TestCase {
 
    /**
     * Constructs a new <code>BeanUtilsTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public BeanUtilsTests(String name) {
       super(name);
    }
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(BeanUtilsTests.class);
    }
 
    public void testPopulate() throws Exception {
 
       // Populate from request to pojo
       SimpleTypesRequest request = new SimpleTypesRequest();
       request.setInputText("Test123");
       SimplePojo pojo = new SimplePojo();
       Object pojo2 = BeanUtils.populate(request, pojo);
       assertEquals("Test123", pojo.getInputText());
       assertEquals("Test123", ((SimplePojo) pojo2).getInputText());
       assertEquals(pojo, pojo2);
 
       // Populate from pojo to request
       pojo.setInputText("Another test");
       Object request2 = BeanUtils.populate(pojo, request);
       assertEquals("Another test", request.getInputText());
       assertEquals("Another test", ((SimpleTypesRequest) request2).getInputText());
       assertEquals(request, request2);
    }
 
    public void testPopulateWithMapping() throws Exception {
 
       // Populate from request to pojo
       Properties mapping1 = new Properties();
       mapping1.setProperty("InputBoolean", "AlmostBoolean");
       SimpleTypesRequest request = new SimpleTypesRequest();
       request.setInputText("Test123");
       request.setInputBoolean(Boolean.TRUE);
       SimplePojo pojo = new SimplePojo();
 
       // First without the mapping
       BeanUtils.populate(request, pojo);
       assertFalse(pojo.getAlmostBoolean().booleanValue());
 
       // Now with the mapping
       BeanUtils.populate(request, pojo, mapping1);
       assertTrue(pojo.getAlmostBoolean().booleanValue());
    }
 
    public void testPopulateWithConvertion() throws Exception {
 
       // Boolean to String
       Properties mapping1 = new Properties();
       mapping1.setProperty("InputBoolean", "InputText");
       SimpleTypesRequest request = new SimpleTypesRequest();
       request.setInputBoolean(Boolean.TRUE);
       SimplePojo pojo = new SimplePojo();
       BeanUtils.populate(request, pojo, mapping1);
       assertEquals("true", pojo.getInputText());
 
       // String to Boolean
       Properties mapping2 = new Properties();
       mapping2.setProperty("InputText", "InputBoolean");
       pojo.setInputText("true");
       BeanUtils.populate(pojo, request, mapping2);
       assertTrue(request.getInputBoolean().booleanValue());
 
       // String to Boolean with invalid value -> unchanged value
       pojo.setInputText("almost true");
       BeanUtils.populate(pojo, request, mapping2);
       assertTrue(request.getInputBoolean().booleanValue());
    }
 
    public void testTypeConvertion() throws Exception {
 
       // Boolean to boolean
       Properties mapping1 = new Properties();
       mapping1.setProperty("InputBoolean", "RealBoolean");
       SimpleTypesRequest request = new SimpleTypesRequest();
       request.setInputBoolean(Boolean.TRUE);
       SimplePojo pojo = new SimplePojo();
       BeanUtils.populate(request, pojo, mapping1);
       assertTrue(pojo.getRealBoolean());
 
       // String to EnumItem
       SimplePojo pojo2 = new SimplePojo();
       pojo2.setInputText("Mister");
       DefinedTypesRequest request2 = new DefinedTypesRequest();
       Properties mapping2 = new Properties();
       mapping2.setProperty("InputText", "InputSalutation");
       BeanUtils.populate(pojo2, request2, mapping2);
       assertEquals(Salutation.MISTER, request2.getInputSalutation());
    }
 
    public void testConvert() throws Exception {
       Integer convert1 = (Integer) BeanUtils.convert(new Integer(123), Integer.class);
       assertEquals(new Integer(123), convert1);
 
       String enumToString = (String) BeanUtils.convert(Salutation.LADY, String.class);
       assertEquals("Miss", enumToString);
 
       Salutation.Item stringToEnum = (Salutation.Item) BeanUtils.convert("Miss", Salutation.Item.class);
       assertEquals(Salutation.LADY, stringToEnum);
 
       String dateToString = (String) BeanUtils.convert(Date.fromStringForRequired("20061211"), String.class);
       assertEquals("20061211", dateToString);
 
       Date.Value stringToDate = (Date.Value) BeanUtils.convert("20061211", Date.Value.class);
       assertEquals("20061211", stringToDate.toString());
 
       java.util.Date dateToDate = (java.util.Date) BeanUtils.convert(stringToDate, java.util.Date.class);
       assertEquals(106, dateToDate.getYear());
 
       TextList.Value list1 = TextList.fromStringForOptional("15&16&bla");
       java.util.List listToList1 = (java.util.List) BeanUtils.convert(list1, java.util.List.class);
       assertEquals(3, listToList1.size());
       assertEquals("15", listToList1.get(0));
       assertEquals("16", listToList1.get(1));
       assertEquals("bla", listToList1.get(2));
       TextList.Value listToList2 = (TextList.Value) BeanUtils.convert(listToList1, TextList.Value.class);
       assertEquals(3, listToList2.getSize());
       assertEquals("15", listToList2.get(0));
       assertEquals("16", listToList2.get(1));
       assertEquals("bla", listToList2.get(2));
 
       Integer integer1 = (Integer) BeanUtils.convert(new Integer(42), Integer.class);
       assertEquals(new Integer(42), integer1);
       Integer integer2 = (Integer) BeanUtils.convert(new Integer(43), Integer.TYPE);
       assertEquals(new Integer(43), integer2);
    }
 
    public void testGetParameters() throws Exception {
       SimplePojo pojo = createPOJO();
       Map mapPojo = BeanUtils.getParameters(pojo);
       assertEquals(4, mapPojo.size());
       assertEquals(Boolean.TRUE, mapPojo.get("almostBoolean"));
       assertEquals(Boolean.TRUE, mapPojo.get("realBoolean"));
       assertEquals("input", mapPojo.get("inputText"));
       assertEquals(new Integer(123), mapPojo.get("simpleInt"));
 
       SimplePojo pojo2 = createPOJO();
       pojo2.setAlmostBoolean(null);
       Map mapPojo2 = BeanUtils.getParameters(pojo2);
       assertEquals(3, mapPojo2.size());
       assertNull(mapPojo2.get("almostBoolean"));
    }
 
    public void testGetParametersAsString() throws Exception {
       SimplePojo pojo = createPOJO();
       Map mapPojo = BeanUtils.getParametersAsString(pojo);
       assertEquals(4, mapPojo.size());
       assertEquals("true", mapPojo.get("almostBoolean"));
       assertEquals("true", mapPojo.get("realBoolean"));
       assertEquals("input", mapPojo.get("inputText"));
       assertEquals("123", mapPojo.get("simpleInt"));
 
       SimplePojo pojo2 = createPOJO();
       pojo2.setAlmostBoolean(null);
       Map mapPojo2 = BeanUtils.getParametersAsString(pojo2);
       assertEquals(3, mapPojo2.size());
       assertNull(mapPojo2.get("almostBoolean"));
    }
 
    public void testGetParametersAsObject() throws Exception {
       SimplePojo pojo = createPOJO();
       Map mapPojo = BeanUtils.getParametersAsObject(pojo);
       assertEquals(4, mapPojo.size());
       Object almostBoolean = mapPojo.get("almostBoolean");
       assertEquals(Boolean.TRUE, almostBoolean);
       assertEquals(Boolean.TRUE, mapPojo.get("realBoolean"));
       assertEquals("input", mapPojo.get("inputText"));
       assertEquals(new Integer(123), mapPojo.get("simpleInt"));
 
       SimplePojo pojo2 = createPOJO();
       pojo2.setAlmostBoolean(null);
       Map mapPojo2 = BeanUtils.getParametersAsObject(pojo2);
       assertEquals(3, mapPojo2.size());
       assertNull(mapPojo2.get("almostBoolean"));
 
       DefinedTypesRequest request = new DefinedTypesRequest();
       request.setInputSalutation(Salutation.LADY);
       request.setInputAge((byte) 18);
       TextList.Value list1 = TextList.fromStringForOptional("15&16&bla");
       request.setInputList(list1);
       Map mapRequest = BeanUtils.getParametersAsObject(request);
      assertEquals(3, mapRequest.size());
       assertEquals(new Byte((byte) 18), mapRequest.get("inputAge"));
       assertEquals("Miss", mapRequest.get("inputSalutation"));
       java.util.List list2 = (java.util.List) mapRequest.get("inputList");
       assertEquals(3, list2.size());
       assertEquals("15", list2.get(0));
       assertEquals("16", list2.get(1));
       assertEquals("bla", list2.get(2));
       assertNull(mapRequest.get("inputShared"));
    }
 
    public void testSetParameters() throws Exception {
       SimplePojo pojo = new SimplePojo();
       Map map = new HashMap();
       map.put("almostBoolean", "true");
       map.put("realBoolean", Boolean.TRUE);
       map.put("simpleInt", new Integer("33"));
       BeanUtils.setParameters(map, pojo);
       assertEquals(Boolean.TRUE, pojo.getAlmostBoolean());
       assertEquals(true, pojo.getRealBoolean());
       assertEquals(33, pojo.getSimpleInt());
       assertNull(pojo.getInputText());
 
       // Update the POJO
       Map map2 = new HashMap();
       map2.put("simpleInt", new Integer("44"));
       BeanUtils.setParameters(map2, pojo);
       assertEquals(true, pojo.getRealBoolean());
       assertEquals(44, pojo.getSimpleInt());
    }
 
    public void testXmlToObject1() throws Exception {
       Element element1 = new Element("simplePojo");
       element1.setAttribute("simpleInt", "33");
       element1.setAttribute("almostBoolean", "true");
       PojoContainer pojoContainer = new PojoContainer();
       assertEquals(0, pojoContainer.listSimplePojo().size());
       BeanUtils.xmlToObject(element1, pojoContainer);
       assertEquals(1, pojoContainer.listSimplePojo().size());
       SimplePojo pojo1 = (SimplePojo) pojoContainer.listSimplePojo().get(0);
       assertEquals(33, pojo1.getSimpleInt());
       assertEquals(Boolean.TRUE, pojo1.getAlmostBoolean());
       assertEquals(false, pojo1.getRealBoolean());
       assertNull(pojo1.getInputText());
    }
 
    public void testXmlToObject2() throws Exception {
       Element element1 = new Element("pojo");
       element1.setAttribute("simpleInt", "33");
       element1.setAttribute("almostBoolean", "true");
       Properties mappingElement = new Properties();
       mappingElement.setProperty("pojo", "SimplePojo");
       Properties mappingAttributes = new Properties();
       mappingAttributes.setProperty("almostBoolean", "realBoolean");
       mappingAttributes.setProperty("simpleInt", "inputText");
       PojoContainer pojoContainer = new PojoContainer();
       assertEquals(0, pojoContainer.listSimplePojo().size());
       BeanUtils.xmlToObject(element1, pojoContainer, mappingElement, mappingAttributes);
       assertEquals(1, pojoContainer.listSimplePojo().size());
       SimplePojo pojo1 = (SimplePojo) pojoContainer.listSimplePojo().get(0);
       assertEquals(0, pojo1.getSimpleInt());
       assertEquals(Boolean.FALSE, pojo1.getAlmostBoolean());
       assertEquals(true, pojo1.getRealBoolean());
       assertEquals("33", pojo1.getInputText());
    }
 
    private SimplePojo createPOJO() {
       SimplePojo pojo = new SimplePojo();
       pojo.setAlmostBoolean(Boolean.TRUE);
       pojo.setRealBoolean(true);
       pojo.setInputText("input");
       pojo.setSimpleInt(123);
       return pojo;
    }
 
    public static class SimplePojo {
 
       private String _inputText;
       private Boolean _almostBoolean = Boolean.FALSE;
       private boolean _realBoolean = false;
       private int _simpleInt;
 
       public void setInputText(String inputText) {
          _inputText = inputText;
       }
 
       public String getInputText() {
          return _inputText;
       }
 
       public void setAlmostBoolean(Boolean almostBoolean) {
          _almostBoolean = almostBoolean;
       }
 
       public Boolean getAlmostBoolean() {
          return _almostBoolean;
       }
 
       public void setRealBoolean(boolean realBoolean) {
          _realBoolean = realBoolean;
       }
 
       public boolean getRealBoolean() {
          return _realBoolean;
       }
 
       public void setSimpleInt(int anInt) {
          _simpleInt = anInt;
       }
 
       public int getSimpleInt() {
          return _simpleInt;
       }
    }
 
    public class PojoContainer {
       
       private List pojoList = new ArrayList();
       
       public void addSimplePojo(SimplePojo pojo) {
          pojoList.add(pojo);
       }
       public List listSimplePojo() {
          return pojoList;
       }
    }
 }
