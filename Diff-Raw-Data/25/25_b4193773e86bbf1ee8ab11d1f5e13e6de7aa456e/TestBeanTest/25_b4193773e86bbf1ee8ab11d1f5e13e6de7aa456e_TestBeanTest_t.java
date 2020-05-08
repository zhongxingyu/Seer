 package org.jboss.qa;
 
 import org.jboss.qa.TestBean;
 
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(Arquillian.class)
 public class TestBeanTest {
   @Inject
   private TestBean testbean;
 
   @Deployment
   public static JavaArchive createDeployment() {
     return ShrinkWrap.create(JavaArchive.class, "test.jar")
         .addClass(TestBean.class)
        .addAsManifestResource("xa-ds.xml")
        // .addAsManifestResource("normal-ds.xml")
         .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }
 
   @Test
   public void testIsDeployed() {
     Assert.assertNotNull(testbean);
     testbean.go();
   }
 }
