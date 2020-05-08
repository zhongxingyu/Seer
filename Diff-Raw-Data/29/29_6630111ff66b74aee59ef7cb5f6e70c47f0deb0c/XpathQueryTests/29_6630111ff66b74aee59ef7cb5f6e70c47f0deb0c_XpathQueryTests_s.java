 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.gburgett.xflat.query;
 
 import org.gburgett.xflat.convert.ConversionService;
 import org.gburgett.xflat.convert.DefaultConversionService;
 import org.gburgett.xflat.convert.converters.JDomConverters;
 import org.gburgett.xflat.convert.converters.StringConverters;
 import org.gburgett.xflat.query.XpathQuery.QueryType;
 import org.jdom2.Element;
 import org.jdom2.xpath.XPathExpression;
 import org.jdom2.xpath.XPathFactory;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Matchers;
 import test.Foo;
 
 /**
  *
  * @author gordon
  */
 public class XpathQueryTests {
 
     private ConversionService conversionService;
     
     private XPathFactory xpath;
     
     @Before
     public void setup(){
         this.conversionService = new DefaultConversionService();
         StringConverters.RegisterTo(conversionService);
         JDomConverters.RegisterTo(conversionService);
         
         this.xpath = XPathFactory.instance();
     }
     
     //<editor-fold desc="equals">
     @Test
     public void testEq_String_RowHasText_Matches() throws Exception {
         System.out.println("testEq_String_RowHasText_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("value");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data");
         XpathQuery query = XpathQuery.eq(path, "value");
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query.toString());
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
         assertEquals("Should be correct query type", QueryType.EQ, query.getQueryType());
         assertEquals("Should be correct value", "value", query.getValue());
         assertEquals("Should be correct value ", String.class, query.getValueType());
     }//end testEq_String_RowHasText_Matches
     
     @Test
     public void testEq_String_RowHasText_DoesntMatch() throws Exception {
         System.out.println("testEq_String_RowHasText_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("wrongVal");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data");
         XpathQuery query = XpathQuery.eq(path, "value");
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testEq_String_RowHasText_DoesntMatch
     
     @Test
     public void testEq_String_RowHasElement_DoesntMatch() throws Exception {
         System.out.println("testEq_String_RowHasElement_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setContent(new Element("child"));
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data");
         XpathQuery query = XpathQuery.eq(path, "value");
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testEq_String_RowHasElement_DoesntMatch
     
     @Test
     public void testEq_String_RowHasDeepText_Matches() throws Exception {
         System.out.println("testEq_String_RowHasDeepText_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("value");
         row.addContent(new Element("deep"));
         row.getChild("deep").addContent(data);
         
         XPathExpression<Object> path = xpath.compile("deep/data");
         XpathQuery query = XpathQuery.eq(path, "value");
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testEq_String_RowHasDeepText_Equals
     
     @Test
     public void testEq_String_RowHasAttribute_Matches() throws Exception {
         System.out.println("testEq_String_RowHasAttribute_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setAttribute("val", "value");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
         XpathQuery query = XpathQuery.eq(path, "value");
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testEq_String_RowHasAttribute_Matches
     
     @Test
     public void testEq_Null_SelectedValueExists_DoesntMatch() throws Exception {
         System.out.println("testEq_Null_SelectedValueExists_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("value");
         row.addContent(new Element("deep"));
         row.getChild("deep").addContent(data);
         
         XPathExpression<Object> path = xpath.compile("deep/data");
         XpathQuery query = XpathQuery.eq(path, null);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testEq_Null_SelectedValueExists_DoesntMatch
     
     @Test
     public void testEq_Null_SelectedValueDoesNotExist_Matches() throws Exception {
         System.out.println("testEq_Null_SelectedValueDoesNotExist_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("value");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("deep/data");
         XpathQuery query = XpathQuery.eq(path, null);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testEq_Null_SelectedValueDoesNotExist_Matches
     
     @Test
     public void testEq_Int_DeepElementEquals_Matches() throws Exception {
         System.out.println("testEq_Int_DeepElementEquals_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("17");
         row.addContent(new Element("deep"));
         row.getChild("deep").addContent(data);
         
         XPathExpression<Object> path = xpath.compile("deep/data");
         XpathQuery query = XpathQuery.eq(path, 17);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testEq_Int_DeepElementEquals_Matches
     
     @Test
     public void testEq_Int_DeepElementNotEqual_DoesntMatch() throws Exception {
         System.out.println("testEq_Int_DeepElementNotEqual_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("23.0");
         row.addContent(new Element("deep"));
         row.getChild("deep").addContent(data);
         
         XPathExpression<Object> path = xpath.compile("deep/data");
         XpathQuery query = XpathQuery.eq(path, 17);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testEq_Int_DeepElementNotEqual_DoesntMatch
     
     @Test
     public void testEq_Foo_ElementEqualByValue_Matches() throws Exception {
         System.out.println("testEq_Foo_ElementEqualByValue_Matches");
         
         //setup conversion service
         conversionService.addConverter(Foo.class, Element.class, new Foo.ToElementConverter());
         conversionService.addConverter(Element.class, Foo.class, new Foo.FromElementConverter());
         
         //setup data
         Element row = new Element("row");
         
         Foo data = new Foo();
         data.fooInt = 34;
         
         row.addContent(conversionService.convert(data, Element.class));
         
         Foo queryFoo = new Foo();
         queryFoo.fooInt = 34;
         
         //setup query
         XPathExpression<Object> path = xpath.compile("foo");
         XpathQuery query = XpathQuery.eq(path, queryFoo);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testEq_Foo_ElementEqualByValue_Matches
     
     @Test
     public void testEq_Foo_ElementNotEqualByValue_DoesntMatch() throws Exception {
         System.out.println("testEq_Foo_ElementNotEqualByValue_DoesntMatch");
         
         //setup conversion service
         conversionService.addConverter(Foo.class, Element.class, new Foo.ToElementConverter());
         conversionService.addConverter(Element.class, Foo.class, new Foo.FromElementConverter());
         
         //setup data
         Element row = new Element("row");
         
         Foo data = new Foo();
         data.fooInt = 34;
         
         row.addContent(conversionService.convert(data, Element.class));
         
         Foo queryFoo = new Foo();
         queryFoo.fooInt = 12;
         
         //setup query
         XPathExpression<Object> path = xpath.compile("foo");
         XpathQuery query = XpathQuery.eq(path, queryFoo);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testEq_Foo_ElementNotEqualByValue_DoesntMatch
     
     //</editor-fold>
     
     //<editor-fold desc="not equals" >
     
     @Test
     public void testNe_String_RowHasAttributeValue_DoesntMatch() throws Exception {
         System.out.println("testNe_String_RowHasAttributeValue_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setAttribute("val", "value");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
         XpathQuery query = XpathQuery.ne(path, "value");
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
         assertEquals("Should be correct query type", QueryType.NE, query.getQueryType());
         assertEquals("Should be correct value", "value", query.getValue());
         assertEquals("Should be correct value ", String.class, query.getValueType());
     }//end testNe_String_RowHasAttributeValue_DoesntMatch
     
     @Test
     public void testNe_String_DeepValueDiffers_Matches() throws Exception {
         System.out.println("testNe_String_DeepValueDiffers_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("wrongValue");
         row.addContent(new Element("deep"));
         row.getChild("deep").addContent(data);
         
         XPathExpression<Object> path = xpath.compile("deep/data");
         XpathQuery query = XpathQuery.ne(path, "value");
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testNe_String_DeepValueDiffers_Matches
     
     @Test
     public void testNe_Null_RowExists_Matches() throws Exception {
         System.out.println("testNe_Null_RowExists_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("value");
         row.addContent(new Element("deep"));
         row.getChild("deep").addContent(data);
         
         XPathExpression<Object> path = xpath.compile("deep/data");
         XpathQuery query = XpathQuery.ne(path, null);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testNe_Null_RowExists_Matches
     
     @Test
     public void testNe_Null_RowDoesntExist_DoesntMatch() throws Exception {
         System.out.println("testNe_Null_RowDoesntExist_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("value");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("deep/data");
         XpathQuery query = XpathQuery.ne(path, null);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testNe_Null_RowDoesntExist_Matches
     
     //</editor-fold>
 
     //<editor-fold desc="less than">
     
     @Test
     public void testLessThan_Int_AttributeIsLessThan_Matches() throws Exception {
         System.out.println("testLessThan_Int_AttributeIsLessThan_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setAttribute("val", "20");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
         XpathQuery query = XpathQuery.lt(path, 21);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
         assertEquals("Should be correct query type", QueryType.LT, query.getQueryType());
         assertEquals("Should be correct value", 21, query.getValue());
         assertEquals("Should be correct value ", Integer.class, query.getValueType());
     }//end testLessThan_Int_AttributeIsLessThan_Matches
     
     @Test
     public void testLessThan_Int_ElementValueIsGreaterThan_DoesntMatch() throws Exception {
         System.out.println("testLessThan_Int_ElementValueIsGreaterThan_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("30");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data");
         XpathQuery query = XpathQuery.lt(path, 12);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testLessThan_Int_ElementValueIsGreaterThan_DoesntMatch
     
     @Test
     public void testLessThan_Int_AttributeDoesNotExist_DoesntMatch() throws Exception {
         System.out.println("testLessThan_Int_AttributeDoesNotExist_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("this element has no @val attribute");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
         XpathQuery query = XpathQuery.lt(path, 21);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testLessThan_Int_AttributeDoesNotExist_DoesntMatch
     
     //</editor-fold>
 
     //<editor-fold desc="greater than">
     
     @Test
    public void testGreaterThan_Float_AttributeIsLessThan_Matches() throws Exception {
        System.out.println("testGreaterThan_Float_AttributeIsLessThan_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setAttribute("val", "20.9");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
        XpathQuery query = XpathQuery.lt(path, (float)21.7);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
        assertTrue(matches);
        assertEquals("Should be correct query type", QueryType.LT, query.getQueryType());
         assertEquals("Should be correct value", (float)21.7, query.getValue());
         assertEquals("Should be correct value ", Float.class, query.getValueType());
    }//end testGreaterThan_Float_AttributeIsLessThan_Matches
     
     @Test
    public void testGreaterThan_Float_ElementValueIsGreaterThan_DoesntMatch() throws Exception {
        System.out.println("testGreaterThan_Float_ElementValueIsGreaterThan_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("30.1");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data");
        XpathQuery query = XpathQuery.lt(path, 30.09);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
        assertFalse(matches);
     }//end testGreaterThan_Float_ElementValueIsGreaterThan_DoesntMatch
     
     @Test
     public void testGreaterThan_Float_AttributeDoesNotExist_DoesntMatch() throws Exception {
         System.out.println("testGreaterThan_Float_AttributeDoesNotExist_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         
         data.setText("this element has no @val attribute");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
        XpathQuery query = XpathQuery.lt(path, 21.84);
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testGreaterThan_Float_AttributeDoesNotExist_DoesntMatch
     //</editor-fold>
     
     //<editor-fold desc="and">
     
     @Test
     public void testAnd_IntAndString_HasAttributeAndElement_Matches() throws Exception {
         System.out.println("testAnd_IntAndString_HasAttributeAndElement_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         data.addContent(new Element("sub"));
         
         data.getChild("sub").setText("Some Text");
         data.setAttribute("val", "12.4");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
         XpathQuery query = XpathQuery.and(
                     XpathQuery.lt(xpath.compile("data/@val"), 13.0),
                     XpathQuery.eq(xpath.compile("data/sub"), "Some Text")
                 );
         
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testAnd_IntAndString_HasAttributeAndElement_Matches
     
     @Test
     public void testAnd_IntAndString_HasAttributeButNotElement_DoesntMatch() throws Exception {
         System.out.println("testAnd_IntAndString_HasAttributeButNotElement_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         data.addContent(new Element("sub"));
         
         data.getChild("sub").setText("Wrong Text");
         data.setAttribute("val", "12.4");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
         XpathQuery query = XpathQuery.and(
                     XpathQuery.lt(xpath.compile("data/@val"), 13.0),
                     XpathQuery.eq(xpath.compile("data/sub"), "Some Text")
                 );
         
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testAnd_IntAndString_HasAttributeButNotElement_DoesntMatch
     
     //</editor-fold>
     
     //<editor-fold desc="Or">
     @Test
     public void testOr_DoubleAndString_HasElementButAttributeDoesntExist_Matches() throws Exception {
         System.out.println("testOr_DoubleAndString_HasElementButAttributeDoesntExist_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         data.addContent(new Element("sub"));
         
         data.getChild("sub").setText("Some Text");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
         XpathQuery query = XpathQuery.or(
                     XpathQuery.lt(xpath.compile("data/@val"), 13.0),
                     XpathQuery.eq(xpath.compile("data/sub"), "Some Text")
                 );
         
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testOr_DoubleAndString_HasElementButAttributeDoesntExist_Matches
     
     
     @Test
     public void testOr_DoubleAndString_HasNeither_DoesntMatch() throws Exception {
         System.out.println("testOr_DoubleAndString_HasNeither_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         data.addContent(new Element("sub"));
         
         data.getChild("sub").setText("Other Text");
         data.setAttribute("val", "35");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
         XpathQuery query = XpathQuery.or(
                     XpathQuery.lt(xpath.compile("data/@val"), 13.0),
                     XpathQuery.eq(xpath.compile("data/sub"), "Some Text")
                 );
         
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testOr_DoubleAndString_HasNeither_DoesntMatch
     //</editor-fold>
     
     //<editor-fold desc="matches">
     @Test
     public void testMatches_DoesMatchValue_Matches() throws Exception {
         System.out.println("testMatches_DoesMatchValue_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         data.addContent(new Element("sub"));
         
         data.getChild("sub").setText("Other Text");
         data.setAttribute("val", "35");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
         XpathQuery query = XpathQuery.matches(path, org.hamcrest.Matchers.equalTo(35.0), Double.class);
         
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testMatches_DoesMatchValue_Matches
     
     @Test
     public void testMatches_DoesntMatchValue_DoesntMatch() throws Exception {
         System.out.println("testMatches_DoesntMatchValue_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         data.addContent(new Element("sub"));
         
         data.getChild("sub").setText("Other Text");
         data.setAttribute("val", "42");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data/@val");
         XpathQuery query = XpathQuery.matches(path, org.hamcrest.Matchers.equalTo(35.0), Double.class);
         
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testMatches_DoesntMatchValue_DoesntMatch
     //</editor-fold>
     
     @Test
     public void testExists_Exists_Matches() throws Exception {
         System.out.println("testExists_Exists_Matches");
         
         Element row = new Element("row");
         Element data = new Element("data");
         data.addContent(new Element("sub"));
         
         data.getChild("sub").setText("Other Text");
         data.setAttribute("val", "42");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data[@val=42]");
         XpathQuery query = XpathQuery.exists(path);
         
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertTrue(matches);
     }//end testExists_Exists_Matches
     
     @Test
     public void testExists_DoesntExist_DoesntMatch() throws Exception {
         System.out.println("testExists_DoesntExist_DoesntMatch");
         
         Element row = new Element("row");
         Element data = new Element("data");
         data.addContent(new Element("sub"));
         
         data.getChild("sub").setText("Other Text");
         data.setAttribute("val", "42");
         row.addContent(data);
         
         XPathExpression<Object> path = xpath.compile("data[sub = \"Wrong Text\"]");
         XpathQuery query = XpathQuery.exists(path);
         
         query.setConversionService(conversionService);
         
         System.out.println(path.getExpression() + ": " + path.evaluateFirst(row));
         System.out.println(query);
         
         //act
         boolean matches = query.getRowMatcher().matches(row);
         
         //assert
         assertFalse(matches);
     }//end testExists_DoesntExist_DoesntMatch
 }
