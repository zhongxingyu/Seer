 package fr.imag.adele.apam.test.testcases;
 
 import java.util.Collections;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Link;
 import fr.imag.adele.apam.ResolutionException;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.impl.ComponentImpl;
 import fr.imag.adele.apam.pax.test.implS7.S07CustomException;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter01;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter02;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter03;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter04;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter05;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter06;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter07;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter08;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter09;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter10;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter11;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter12;
 import fr.imag.adele.apam.pax.test.implS7.S07ImplementationImporter13;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
 @RunWith(JUnit4TestRunner.class)
 public class RelationTest extends ExtensionAbstract {
 
 	
 	private static void AssertCorrectSourceTargetTypes(Component component, Class expectedSource,Class expectedTarget){
 		
 		ComponentImpl ci=null;
 		
 		if(component instanceof Implementation && expectedSource==Specification.class){
 			ci=(ComponentImpl)((Implementation)component).getSpec();
 		}if(component instanceof Instance && expectedSource==Instance.class){
 			ci=(ComponentImpl) component;
 		}
 		
 		for(Link link:ci.getLocalLinks()){
 			
 			validateSourceTargetTypes(link.getSource(),link.getDestination(),expectedSource,expectedTarget);
 			
 		}
 		
 	}
 	
 	private static void validateSourceTargetTypes(Component source,Component target,Class expectedSource,Class expectedTarget){
 		
 		//Source
 		Assert.assertTrue(String.format("Source is not of the type %s",expectedSource.getSimpleName()),expectedSource.isInstance(source) );
 		//Target
 		Assert.assertTrue(String.format("Target is not of the type %s",expectedTarget.getSimpleName()),expectedTarget.isInstance(target) );
 		
 	}
 	
 	@Test
 	public void RelationSourceImplementationTargetImplementation_tc097() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-01");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter01 dependency = (S07ImplementationImporter01) instance
 				.getServiceObject();
 		
 		dependency.getInjected();
 		
 		String messageTemplate="Declaring a relation from source %s to target %s, instantiated object of the type %s which is not the targetKind expected.";
 		
 		ComponentImpl ci=(ComponentImpl)implementation;
 		
 		Assert.assertTrue(String.format("Only one link should have been created, but %s links were found",ci.getLocalLinks().size()),ci.getLocalLinks().size()==1);
 		
 		for(Link link:ci.getLocalLinks()){
 			
 			validateSourceTargetTypes(link.getSource(),link.getDestination(),Implementation.class,Implementation.class);
 			
 		}
 		
 		Assert.assertTrue(
 				String.format(messageTemplate,"Implementation","Implementation",instance.getServiceObject()),
 				dependency.getInjected().getProperty("implementation-property") != null
 						&& dependency.getInjected().getProperty(
 								"instance-property") == null);
 	}
 
 	@Test
 	public void RelationSourceImplementationTargetSpecification_tc098() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-02");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter02 dependency = (S07ImplementationImporter02) instance
 				.getServiceObject();
 
 		auxListProperties(dependency.getInjected());
 
 		auxListInstances();
 
 		ComponentImpl ci=(ComponentImpl)implementation;
 		
 		Assert.assertTrue(String.format("Only one link should have been created, but %s links were found",ci.getLocalLinks().size()),ci.getLocalLinks().size()==1);
 		
 		for(Link link:ci.getLocalLinks()){
 			
 			validateSourceTargetTypes(link.getSource(),link.getDestination(),Implementation.class,Specification.class);
 			
 		}
 		
 		String messageTemplate="Declaring a relation from source %s to target %s, instantiated object of the type %s which is not the targetKind expected.";
 				
 		Assert.assertTrue(String.format(messageTemplate,"Implementation","Specification",instance.getServiceObject()),
 						  dependency.getInjected().getProperty("instance-property") == null && 
 						  dependency.getInjected().getProperty("implementation-property") == null && 
 						  dependency.getInjected().getProperty("specification-property") != null);
 
 	}
 
 	@Test
 	public void RelationSourceImplementationTargetInstance_tc099() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-03");
 		
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter03 dependency = (S07ImplementationImporter03) instance
 				.getServiceObject();
 
 		Instance instanceInjectedReference = auxListInstanceReferencedBy(dependency
 				.getInjected());
 
 		auxListProperties(instanceInjectedReference);
 
 		auxListInstances();
 
 		ComponentImpl ci=(ComponentImpl)implementation;
 		
 		Assert.assertTrue(String.format("Only one link should have been created, but %s links were found",ci.getLocalLinks().size()),ci.getLocalLinks().size()==1);
 		
 		for(Link link:ci.getLocalLinks()){
 			
 			validateSourceTargetTypes(link.getSource(),link.getDestination(),Implementation.class,Instance.class);
 			
 		}
 		
 		String messageTemplate="Declaring a relation from source %s to target %s, instantiated object of the type %s which is not the targetKind expected.";
 		
 		Assert.assertTrue(
 				String.format(messageTemplate,"Implementation","Instance",instance.getServiceObject()), 
 				instanceInjectedReference.getProperty("implementation-property") != null &&
 				instanceInjectedReference.getProperty("specification-property") != null);
 
 	}
 
 	@Test
 	public void RelationSourceImplementationTargetImplementationOverride_tc100() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-04");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter04 dependency = (S07ImplementationImporter04) instance
 				.getServiceObject();
 
 		Assert.assertTrue("Declaring a relation information on the apam specification tag, and just using the id in the apam implementation didnt work, so the dependency was not resolved",dependency.getInjected()!=null);
 		
 	}
 
 	@Test
 	public void RelationSourceImplementationTargetInstanceCreationEager_tc101() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-05");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter05 dependency = (S07ImplementationImporter05) instance
 				.getServiceObject();
 
 		auxListInstances();
 		
 //		Instance instanceInjectedReference = auxListInstanceReferencedBy(dependency
 //				.getInjected());
 
 		ComponentImpl ci=(ComponentImpl)implementation;
 		
 		Assert.assertTrue("Using an relation with creation='eager' should instantiate imediatly the dependency, which didnt happened",ci.getLocalLinks().size()>0);
 		
 	}
 	
 	@Test
 	public void RelationSourceImplementationTargetInstanceCreationLazy_tc102() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-06");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter06 dependency = (S07ImplementationImporter06) instance
 				.getServiceObject();
 
 		ComponentImpl ci=(ComponentImpl)implementation;
 		
 		Assert.assertTrue("In relation, testing creation='lazy'. There should not exist a link before the dependency call",ci.getLocalLinks().size()==0);
 		
 		auxListInstances();
 		
 //		Instance instanceInjectedReference = auxListInstanceReferencedBy(dependency
 //				.getInjected());
 		
 		//Force field injection
 		dependency.getInjected();
 		
 		Assert.assertTrue("Using an relation with creation='lazy' should instantiate after the dependency is called, which didnt happened",ci.getLocalLinks().size()==1);
 		
 	}
 
 	@Test
 	public void RelationSourceSpecificationTargetInstance_tc103() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-07");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter07 dependency = (S07ImplementationImporter07) instance
 				.getServiceObject();
 		
 		//Force field injection
 		dependency.getInjected();
 		
 		ComponentImpl ci=(ComponentImpl)implementation.getSpec();
 		
 		Assert.assertTrue(String.format("One link should have been created, but %s links were found",ci.getLocalLinks().size()),ci.getLocalLinks().size()==1);
 		
 		AssertCorrectSourceTargetTypes(implementation,Specification.class,Instance.class);
 		
 	}
 	
 	@Test
 	public void RelationSourceSpecificationTargetImplementation_tc104() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-08");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter08 dependency = (S07ImplementationImporter08) instance
 				.getServiceObject();
 		
 		//Force field injection
 		dependency.getInjected();
 		
 		ComponentImpl ci=(ComponentImpl)implementation.getSpec();
 		
 		Assert.assertTrue(String.format("One link should have been created, but %s links were found",ci.getLocalLinks().size()),ci.getLocalLinks().size()==1);
 		
 		AssertCorrectSourceTargetTypes(implementation,Specification.class,Implementation.class);
 		
 	}
 	
 	@Test
 	public void RelationSourceSpecificationTargetSpecification_tc105() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-09");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter09 dependency = (S07ImplementationImporter09) instance
 				.getServiceObject();
 		
 		//Force field injection
 		dependency.getInjected();
 		
 		ComponentImpl ci=(ComponentImpl)implementation.getSpec();
 		
 		Assert.assertTrue(String.format("One link should have been created, but %s links were found",ci.getLocalLinks().size()),ci.getLocalLinks().size()==1);
 		
 		AssertCorrectSourceTargetTypes(implementation,Specification.class,Specification.class);
 		
 	}
 	
 	@Test
 	public void RelationSourceSpecificationTargetImplementationFailWait_tc106() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-10");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter10 dependency = (S07ImplementationImporter10) instance
 				.getServiceObject();
 		
 		ThreadWrapper wrapper = new ThreadWrapper(dependency);
 		wrapper.setDaemon(true);
 		wrapper.start();
 		
 		auxListInstances();
 		
 		apam.waitForIt(3000);
 		
 		Assert.assertTrue("The dependency is the type of fail='wait', but the component was not put in wait state",wrapper.isAlive());
 		
 	}
 	
 	// Require by the test RelationSourceSpecificationTargetImplementationFailWait_tc106
 	class ThreadWrapper extends Thread  {
 
 			final S07ImplementationImporter10 group;
 
 			public ThreadWrapper(S07ImplementationImporter10 group) {
 				this.group = group;
 			}
 
 			@Override
 			public void run() {
 				System.out.println("Element injected:" + group.getInjected());
 			}
 
 		}
 
 	@Test
 	public void RelationSourceSpecificationTargetImplementationFailExceptionGeneric_tc107() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-11");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter11 dependency = (S07ImplementationImporter11) instance
 				.getServiceObject();
 		
 		Exception raised=null;
 		
 		try{
 			//Force field injection
 			dependency.getInjected();
 		}catch(Exception e){
 			//If arrive at this block, the test has passed
 			raised=e;
 		}
 		
 		Assert.assertTrue("Using tag <relation/> with fail='exception' didnt not raise an exception as expected.", raised!=null);
 		
 		String messageTemplate="Using tag <relation/> with fail='exception' didnt not raise an exception of the type expected. Type raised was %s instead of %s";
 		
 		Assert.assertTrue(String.format(messageTemplate, raised.getClass().getCanonicalName(), ResolutionException.class.getCanonicalName()), ResolutionException.class.isInstance(raised));
 		
 	}
 	
 	@Test
 	public void RelationSourceSpecificationTargetImplementationFailExceptionCustom_tc108() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-12");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter12 dependency = (S07ImplementationImporter12) instance
 				.getServiceObject();
 		
 		Exception raised=null;
 		
 		try{
 			//Force field injection
 			dependency.getInjected();
 		}catch(Exception e){
 			//If arrive at this block, the test has passed
 			raised=e;
 		}
 		
 		Assert.assertTrue("Using tag <relation/> with fail='exception' didnt not raise an exception as expected.", raised!=null);
 		
 		String messageTemplate="Using tag <relation/> with exception='%s' did not raise this specific exception. It raised %s instead";
 		
 		Assert.assertTrue(String.format(messageTemplate, S07CustomException.class.getCanonicalName(),raised.getClass().getCanonicalName()), S07CustomException.class.isInstance(raised));
 		
 	}
 	
 	@Test
 	public void RelationSourceSpecificationTargetInstance_tc109() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"S07-implementation-13");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		S07ImplementationImporter13 dependency = (S07ImplementationImporter13) instance
 				.getServiceObject();
 		
 		//Force field injection
 		dependency.getInjected();
 		
 		ComponentImpl ci=(ComponentImpl) instance;
 		
 		auxListInstances();
 		
 		Assert.assertTrue(String.format("One link should have been created, but %s links were found",ci.getLocalLinks().size()),ci.getLocalLinks().size()==1);
 		
 		AssertCorrectSourceTargetTypes(instance,Instance.class,Instance.class);
 		
 	}
 	
 }
