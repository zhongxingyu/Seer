 package nl.spelberg.viewbean;
 
 import java.util.Date;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class ViewBeanModelTest {
 
     @Test
     public void testNoAttributes() {
        Object bean = new Object();
        ViewBeanModel beanModel = new ViewBeanModel<Object>(bean);
 
        assertEquals(bean, beanModel.bean());
         assertNotNull(beanModel.fields());
         assertEquals(0, beanModel.fields().size());
 
         try {
             new ViewBeanModel<Object>(null);
             fail();
         } catch (IllegalArgumentException e) {
             assertEquals("bean is null", e.getMessage());
         }
     }
 
     @Test
     public void testSimpleAttributes() {
         SimpleBean simpleBean = new SimpleBean("simple value");
         ViewBeanModel<SimpleBean> beanModel = new ViewBeanModel<SimpleBean>(simpleBean);
 
         assertNotNull(beanModel.fields());
         assertEquals(1, beanModel.fields().size());
 
         ViewBeanField<String> valueAttributeView = beanModel.fieldWithName("value", String.class);
 
         assertEquals(new ViewBeanField<String>(simpleBean, ViewBeanUtils.fieldForName(simpleBean, "value")),
                 beanModel.fields().iterator().next());
 
         assertEquals("value", valueAttributeView.id());
         assertEquals("simple value", valueAttributeView.value());
         assertFalse(valueAttributeView.readOnly());
         assertFalse(valueAttributeView.nullable());
     }
 
     @Test
     public void testComplexAttributes() {
         ComplexBean complexBean = new ComplexBean("ID", "complex value", 42, "Flinke omschrijving.");
         ViewBeanModel<ComplexBean> beanModel = new ViewBeanModel<ComplexBean>(complexBean);
 
         assertNotNull(beanModel.fields());
         assertEquals(4, beanModel.fields().size());
 
         ViewBeanField<String> nameBeanField = beanModel.fieldWithName("name", String.class);
 
         assertEquals(new ViewBeanField<String>(complexBean, ViewBeanUtils.fieldForName(complexBean, "name")),
                 beanModel.fields().iterator().next());
 
         assertEquals("name", nameBeanField.id());
         assertEquals("complex value", nameBeanField.value());
         assertFalse(nameBeanField.readOnly());
         assertFalse(nameBeanField.nullable());
 
         ViewBeanField<Integer> leeftijdAttributeView = beanModel.fieldWithName("leeftijd", Integer.class);
         assertTrue(leeftijdAttributeView.readOnly());
         assertFalse(leeftijdAttributeView.nullable());
     }
 
     private static class SimpleBean {
 
         @ViewField
         private String value;
 
         public SimpleBean(String value) {
             this.value = value;
         }
 
         public String getValue() {
             return value;
         }
     }
 
     private static class ComplexBean {
 
         private String id;
 
         @ViewField
         private String name;
 
         @ViewField(nullable = true)
         private Date birtDate;
 
         @ViewField(readOnly = true)
         private int leeftijd;
 
         @ViewField(id = "extra-info")
         private String description;
 
         private ComplexBean(String id, String name, int leeftijd, String description) {
             this.id = id;
             this.name = name;
             this.leeftijd = leeftijd;
             this.description = description;
         }
 
         public String getId() {
             return id;
         }
 
         public String getName() {
             return name;
         }
 
         public int getLeeftijd() {
             return leeftijd;
         }
 
         public String getDescription() {
             return description;
         }
     }
 
 }
