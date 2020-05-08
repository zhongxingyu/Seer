 package com.rsx.impl;
 
 import com.rsx.Element;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 public class ReallySimpleXmlTest {
 
     @Test
     public void shouldParseXml() {
         InputStream xmlStream = getClass().getResourceAsStream("simple.xml");
 
         Element root = new ReallySimpleXmlImpl().parse(xmlStream);
 
         String name = root.element("person").value().toString();
         Assert.assertEquals("Should load person name", "Peter", name);
 
         Integer age = root.element("person").attribute("age").toInteger();
         Assert.assertEquals("Should load persons age", 22, age.intValue());
 
         Long longAge = root.element("person").attribute("age").toLong();
         Assert.assertEquals("Should load persons long age", 22L, longAge.longValue());
 
         String job = root.element("jobs").elements("job").get(1).value().toString();
         Assert.assertEquals("Should load persons second job", "Sweeper", job);
 
         Double wage = root.element("jobs").elements("job").get(0).attribute("wage").toDouble();
         Assert.assertEquals("Should load persons first jobs wage", 12.3, wage, 0.0);
 
         SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
         Date startDate = root.element("jobs").elements("job").get(0).attribute("startDate").toDate(format);
 
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(startDate);
 
         Assert.assertEquals("Should have correct day", 30, calendar.get(Calendar.DAY_OF_MONTH));
         Assert.assertEquals("Should have correct month", 0, calendar.get(Calendar.MONTH));
         Assert.assertEquals("Should have correct year", 2009, calendar.get(Calendar.YEAR));
     }
 
     @Test
     public void shouldReturnNullIfYouGetTheNamesWrong() {
         InputStream xmlStream = getClass().getResourceAsStream("simple.xml");
 
         Element root = new ReallySimpleXmlImpl().parse(xmlStream);
 
         Assert.assertNull(root.element("wrong_name"));
         Assert.assertNull(root.element("person").attribute("wrong_name"));
     }
 
     @Test
     public void shouldHandleNamespacedElements() {
         InputStream xmlStream = getClass().getResourceAsStream("simple.xml");
         Element root = new ReallySimpleXmlImpl().parse(xmlStream);
         Assert.assertTrue(root.element("foo:alive").attribute("value").toBoolean());
         Assert.assertEquals("Should have 2 family", 2, root.elements("foo:family").size());        
     }
 
     @Test
     public void shouldLoadChildElements() {
         InputStream xmlStream = getClass().getResourceAsStream("simple.xml");
 
         Element root = new ReallySimpleXmlImpl().parse(xmlStream);
         List<Element> elements = root.element("jobs").elements();
 
        Assert.assertEquals(3, elements.size());

         Element job1 = elements.get(0);
         Assert.assertEquals("job", job1.name());
         Assert.assertEquals("Washer", job1.value().toString());
         Element job2 = elements.get(1);
         Assert.assertEquals("job", job2.name());
         Assert.assertEquals("Sweeper", job2.value().toString());

        Assert.assertEquals("crummy-jobs", elements.get(2).name());
    }

    @Test
    public void shouldNotRecurseIntoChildElements() {
        InputStream xmlStream = getClass().getResourceAsStream("simple.xml");
        Element root = new ReallySimpleXmlImpl().parse(xmlStream);

        List<Element> ages = root.elements("age");

        Assert.assertEquals(1, ages.size());
        Assert.assertEquals(ages.get(0).value().toInteger().intValue(), 30);
     }
 }
