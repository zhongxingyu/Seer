 package com.google.code.qualitas.engines.ode.spring;
 
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.google.code.qualitas.engines.api.deployment.Deployer;
 import com.google.code.qualitas.engines.api.deployment.Undeployer;
 import com.google.code.qualitas.engines.api.factory.BundleFactory;
 import com.google.code.qualitas.engines.api.instrumentation.Instrumentor;
 import com.google.code.qualitas.engines.api.invocation.Invoker;
import com.google.code.qualitas.engines.api.resolution.PropertiesResolver;
 import com.google.code.qualitas.engines.api.validation.Validator;
 import com.google.code.qualitas.engines.ode.deployment.OdeDeployer;
 import com.google.code.qualitas.engines.ode.deployment.OdeUndeployer;
 import com.google.code.qualitas.engines.ode.factory.OdeBundleFactory;
 import com.google.code.qualitas.engines.ode.instrumentation.OdeInstrumentor;
 import com.google.code.qualitas.engines.ode.invocation.OdeInvoker;
 import com.google.code.qualitas.engines.ode.resolution.OdePropertiesResolver;
 import com.google.code.qualitas.engines.ode.validation.OdeValidator;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/META-INF/spring/qualitas-engines-ode-context.xml")
 public class IoCTest {
 
     @Autowired
     private ApplicationContext context;
 
     @Test
     public void factoryTest() {
         Map<String, BundleFactory> factoriesMap = context.getBeansOfType(BundleFactory.class);
 
         Assert.assertNotNull(factoriesMap);
         Assert.assertEquals(1, factoriesMap.size());
 
         BundleFactory factory = factoriesMap.entrySet().iterator().next().getValue();
 
         Assert.assertTrue((factory instanceof OdeBundleFactory));
     }
 
     @Test
     public void deployerTest() {
         Map<String, Deployer> deployersMap = context.getBeansOfType(Deployer.class);
 
         Assert.assertNotNull(deployersMap);
         Assert.assertEquals(1, deployersMap.size());
 
         Deployer deployer = deployersMap.entrySet().iterator().next().getValue();
 
         Assert.assertTrue((deployer instanceof OdeDeployer));
     }
 
     @Test
     public void undeployerTest() {
         Map<String, Undeployer> undeployersMap = context.getBeansOfType(Undeployer.class);
 
         Assert.assertNotNull(undeployersMap);
         Assert.assertEquals(1, undeployersMap.size());
 
         Undeployer undeployer = undeployersMap.entrySet().iterator().next().getValue();
 
         Assert.assertTrue((undeployer instanceof OdeUndeployer));
     }
 
     @Test
     public void instrumentorTest() {
         Map<String, Instrumentor> instrumentorsMap = context.getBeansOfType(Instrumentor.class);
 
         Assert.assertNotNull(instrumentorsMap);
         Assert.assertEquals(1, instrumentorsMap.size());
 
         Instrumentor instrumentor = instrumentorsMap.entrySet().iterator().next().getValue();
 
         Assert.assertTrue((instrumentor instanceof OdeInstrumentor));
     }
 
     @Test
     public void validatorTest() {
         Map<String, Validator> validatorsMap = context.getBeansOfType(Validator.class);
 
         Assert.assertNotNull(validatorsMap);
         Assert.assertEquals(1, validatorsMap.size());
 
         Validator validator = validatorsMap.entrySet().iterator().next().getValue();
 
         Assert.assertTrue((validator instanceof OdeValidator));
     }
 
     @Test
     public void invokerTest() {
         Map<String, Invoker> invokersMap = context.getBeansOfType(Invoker.class);
 
         Assert.assertNotNull(invokersMap);
         Assert.assertEquals(1, invokersMap.size());
 
         Invoker invoker = invokersMap.entrySet().iterator().next().getValue();
 
         Assert.assertTrue((invoker instanceof OdeInvoker));
     }
 
     @Test
     public void resolverTest() {
         Map<String, PropertiesResolver> propertiesResolverMap = context
                 .getBeansOfType(PropertiesResolver.class);
 
         Assert.assertNotNull(propertiesResolverMap);
         Assert.assertEquals(1, propertiesResolverMap.size());
 
         PropertiesResolver propertiesResolver = propertiesResolverMap.entrySet().iterator().next()
                 .getValue();
 
         Assert.assertTrue((propertiesResolver instanceof OdePropertiesResolver));
     }
 }
