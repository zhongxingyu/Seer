 package com.retroduction.carma.resolvers;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.retroduction.carma.core.api.eventlisteners.IEventListener;
 import com.retroduction.carma.core.api.eventlisteners.om.ClassesUnderTestResolved;
 import com.retroduction.carma.core.api.eventlisteners.om.TestSetDetermined;
 import com.retroduction.carma.core.api.resolvers.IResolver;
 import com.retroduction.carma.core.api.testrunners.ITestCaseInstantiationVerifier;
 import com.retroduction.carma.core.api.testrunners.om.ClassDescription;
 import com.retroduction.carma.resolvers.util.FilterVerifier;
 
 public class Resolver implements IResolver {
 
 	private FilterVerifier classFilterVerifier;
 
 	private FilterVerifier testClassFilterVerifier;
 
 	private ITestCaseInstantiationVerifier instantiationVerifier;
 
 	private IResolver nestedResolver;
 
 	private IEventListener eventListener;
 
 	public void setEventListener(IEventListener eventListener) {
 		this.eventListener = eventListener;
 	}
 
 	public void setClassFilterVerifier(FilterVerifier filterVerifier) {
 		this.classFilterVerifier = filterVerifier;
 	}
 
 	public void setInstantiationVerifier(ITestCaseInstantiationVerifier instantiationVerifier) {
 		this.instantiationVerifier = instantiationVerifier;
 	}
 
 	public void setNestedResolver(IResolver nestedResolver) {
 		this.nestedResolver = nestedResolver;
 	}
 
 	public Set<ClassDescription> resolve() {
 
 		// TODO: remove uneccessary conversions between list -> set -> list !
 		Set<ClassDescription> classDescriptions = new HashSet<ClassDescription>(nestedResolver.resolve());
 
 		Set<ClassDescription> remainingClassUnderTest = removeSuperfluousClassNames(classDescriptions);
 
 		eventListener
 				.notifyEvent(new ClassesUnderTestResolved(new ArrayList<ClassDescription>(remainingClassUnderTest)));
 
 		Set<ClassDescription> remainingClassesUnderTestWithWorkingTestClasses = removeSuperfluousTestClasses(remainingClassUnderTest);
 
 		return remainingClassesUnderTestWithWorkingTestClasses;
 
 	}
 
 	Set<ClassDescription> removeSuperfluousClassNames(Set<ClassDescription> classesUnderTest) {
 
 		HashSet<String> resolvedClassNames = new HashSet<String>();
 
 		for (ClassDescription classDescription : classesUnderTest) {
 			resolvedClassNames.add(classDescription.getQualifiedClassName());
 		}
 
 		Set<String> remainingClassesNames = classFilterVerifier.removeExcludedClasses(resolvedClassNames);
 
		remainingClassesNames = instantiationVerifier.removeNonInstantiatableClasses(remainingClassesNames);

 		Set<ClassDescription> remainingClassDescriptions = new HashSet<ClassDescription>();
 
 		for (ClassDescription classDescription : classesUnderTest) {
 			if (remainingClassesNames.contains(classDescription.getQualifiedClassName()))
 				remainingClassDescriptions.add(classDescription);
 		}
 		return remainingClassDescriptions;
 	}
 
 	Set<ClassDescription> removeSuperfluousTestClasses(Set<ClassDescription> remainingClassDescriptions) {
 
 		for (ClassDescription classUnderTestDescription : remainingClassDescriptions) {
 
 			Set<String> associatedTestNames = classUnderTestDescription.getAssociatedTestNames();
 
 			Set<String> remainingTestNames = testClassFilterVerifier.removeExcludedClasses(associatedTestNames);
 
 			remainingTestNames = instantiationVerifier.removeNonInstantiatableClasses(remainingTestNames);
 
 			classUnderTestDescription.setAssociatedTestNames(remainingTestNames);
 
 			eventListener.notifyEvent(new TestSetDetermined(classUnderTestDescription.getQualifiedClassName(),
 					remainingTestNames));
 
 		}
 
 		return remainingClassDescriptions;
 	}
 
 	public void setTestClassFilterVerifier(FilterVerifier testClassFilterVerifier) {
 		this.testClassFilterVerifier = testClassFilterVerifier;
 	}
 
 }
