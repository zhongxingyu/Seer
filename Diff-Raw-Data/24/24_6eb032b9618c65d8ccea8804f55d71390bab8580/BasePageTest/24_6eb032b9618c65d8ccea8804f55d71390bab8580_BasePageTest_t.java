 package com.solidstategroup.radar.test.web;
 
 import com.solidstategroup.radar.web.RadarApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
 import org.apache.wicket.util.tester.WicketTester;
 import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
 
@RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:context.xml"})
 public abstract class BasePageTest {
 
     protected WicketTester tester;
 
    @Autowired
    private ApplicationContext applicationContext;

     @Before
     public void setUp() {
        RadarApplication application = new RadarApplication() {
            @Override
            public void init() {
                // We have to set the application context manually outside of a web context
                getComponentInstantiationListeners().add(new SpringComponentInjector(this, applicationContext));
            }
        };

        tester = new WicketTester(application);
     }
 
 }
