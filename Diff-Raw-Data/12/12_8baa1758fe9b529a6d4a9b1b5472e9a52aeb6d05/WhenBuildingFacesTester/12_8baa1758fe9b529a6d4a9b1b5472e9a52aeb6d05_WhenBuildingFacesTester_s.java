 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.steeplesoft.jsf.facestester.sample;
 
 import com.steeplesoft.jsf.facestester.FacesTester;
 import com.steeplesoft.jsf.facestester.injection.InjectionManager;
 import com.steeplesoft.jsf.sample.ManagedBeanWithJpa;
 import com.steeplesoft.jsf.sample.ZipCodeMapperPage;
 
 import javax.faces.component.html.HtmlOutputText;
 import javax.faces.context.FacesContext;
 import javax.faces.application.FacesMessage;
 
 import org.junit.Test;
 import org.junit.Before;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.hamcrest.core.IsNot.not;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * @author jasonlee
  */
 public class WhenBuildingFacesTester {
 
     private FacesTester tester;
 
     @Before
     public void setUp() {
         tester = new FacesTester();
     }
 
     @Test
     public void shouldBeAbleToCreateStandardComponents() {
         assertThat(tester.createComponent(HtmlOutputText.COMPONENT_TYPE).getWrappedComponent() instanceof HtmlOutputText,
                 is(true));
     }
 
     @Test
     public void shouldBeAbleToAddFacesMessage() {
        FacesContext context = FacesContext.getCurrentInstance();
         context.addMessage(null, new FacesMessage("add entry failed - see log for details"));
     }
 
     @Test
     public void shouldBeAbleToAccessManagedBeans() {
         ZipCodeMapperPage backingBean = tester.getManagedBean(ZipCodeMapperPage.class, "ZipCodeMapperPage");
 
         assertThat(backingBean, is(not(nullValue())));
     }
 
     /*
      * Maybe I should break this into multiple tests.
      */
     @Test
     public void shouldHaveInjectionPerformed() {
         InjectionManager.registerObject("em", new MockEntityManager());
         InjectionManager.registerObject("emf", new MockEntityManagerFactory());
 
         ManagedBeanWithJpa mb = tester.getManagedBean(ManagedBeanWithJpa.class, "jpaBean");
 
         assertNotNull(mb.getEntityManager1());
         assertNotNull(mb.getEntityManager2());
 
         assertNotNull(mb.getEntityManagerFactory1());
         assertNotNull(mb.getEntityManagerFactory2());
     }
}
