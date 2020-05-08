 package br.com.adaptworks.scraper;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.Assert;
 import org.junit.Test;
 
 import br.com.adaptworks.scraper.converter.Converter;
 import br.com.adaptworks.scraper.converter.NoOpConverter;
 import br.com.adaptworks.scraper.exception.ScraperException;
 
 /**
  * @author jonasabreu
  * 
  */
 final public class TemplateTest {
 
     @Test(expected = IllegalArgumentException.class)
     public void testThatThrowsExceptionIfInputStreamIsNull() {
         new Template<Item>((InputStream) null, Item.class);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testThatThrowsExceptionIfTemplateIsNull() {
         new Template<Item>((String) null, Item.class);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testThatThrowsExceptionIfTypeIsNull() {
         new Template<Item>("", null);
     }
 
     @Test(expected = ScraperException.class)
     public void testThatThrowsExceptionFieldDoenstExists() {
         new Template<Item>("<td>${fieldThatDoesntExist}</td>", Item.class).match(new Html("<td>123</td>"));
     }
 
     @Test
     public void testThatRecoversData() {
         List<Item> match = new Template<Item>("<td>${test}</td>", Item.class).match(new Html("<td>123</td>"));
         Assert.assertEquals(1, match.size());
         Assert.assertEquals("123", match.get(0).test());
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testThatAddsNoOpConverterToConverterList() {
         Mockery mockery = new Mockery();
         final List converters = mockery.mock(List.class);
         mockery.checking(new Expectations() {
             {
                 oneOf(converters).add(with(any(NoOpConverter.class)));
             }
         });
         new Template<Item>("<td>", Item.class, converters);
 
         mockery.assertIsSatisfied();
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testThatUsesConvertersWhenSetting() {
         Mockery mockery = new Mockery();
         final Converter converter = mockery.mock(Converter.class);
         mockery.checking(new Expectations() {
             {
                 oneOf(converter).accept(with(any(Class.class)));
                 will(returnValue(true));
 
                 oneOf(converter).convert(with("123"));
                 will(returnValue("123"));
             }
         });
 
         List<Converter> list = new ArrayList<Converter>();
         list.add(converter);
 
        List<Item> match = new Template<Item>("<td>${test}</td>", Item.class, list).match(new Html("<td>123</td>"));
 
         mockery.assertIsSatisfied();
     }
 
 }
