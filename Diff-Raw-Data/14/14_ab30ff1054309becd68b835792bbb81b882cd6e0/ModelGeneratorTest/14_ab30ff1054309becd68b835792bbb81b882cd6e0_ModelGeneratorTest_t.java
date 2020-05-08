 package org.eclipse.emf.modelmutator.generator;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.modelmutator.api.ModelMutator;
 import org.eclipse.emf.modelmutator.api.ModelMutatorConfiguration;
 import org.eclipse.emf.modelmutator.mutator.ModelMutatorTest;
 import org.junit.Assert;
 import org.junit.Test;
 
 import testModel.Kategorie;
 import testModel.TestModelFactory;
 
 /**
  * 
  * @author Eugen Neufeld
  * 
  */
 public class ModelGeneratorTest extends ModelMutatorTest {
 
 	@Test
 	public void testNumberRootElements() {
 		ProjectSpace projectSpace = createProjectSpace();
 
 		ModelMutatorConfiguration mmc = createModelMutatorConfigurationSeed(projectSpace);
 
 		ModelMutator.generateModel(mmc);
 		Assert.assertEquals(width, projectSpace.getProject().getModelElements().size());
 	}
 	
 	@Test
 	public void testModelDepth() {
 		int expectedDepth = depth;
 		
 		ProjectSpace projectSpace = createProjectSpace();
 		ModelMutatorConfiguration mmc = createModelMutatorConfigurationSeed(projectSpace);
 		mmc.setDepth(expectedDepth);
 		ModelMutator.generateModel(mmc);
 		
 		// breadth-first search (BFS)
 		List<EObject> parents = new LinkedList<EObject>();
 		parents.add(projectSpace.getProject());
 		
 		List<EObject> children = new LinkedList<EObject>();
 		Set<EClass> classes = new HashSet<EClass>();
 		
		int depth = -1; // Count the Project root-Element as depth=0
 		do {
 			for (EObject parent : parents) {
 				children.addAll(parent.eContents());
 				classes.add(parent.eClass());
 			}
 			
 			depth++;
 			parents.clear();
 			parents.addAll(children);
 			children.clear();
 			classes.clear();
 		} while (!parents.isEmpty());
 		
 		if (depth < expectedDepth) {
 			// The model is not as deep as we would expect - is this a restriction of the meta model?
 			for (EClass clazz : classes) {
 				if (clazz.getEAllContainments() != null && !clazz.getEAllContainments().isEmpty()) {
 					// The metamodel would allow this model to go deeper, so the generator failed
 					Assert.fail("The model is not as deep as the metamodel allows and the generator was expected to generate.");
 				}
 			}
 			
 			// This is a restriction of the metamodel, so the generator could not do anything about it
 		} else if (depth > expectedDepth) {
 			Assert.fail("The model is deeper as we would expect. (calculated: "+depth+", expected: "+expectedDepth+")");
 		}
 		
 		// The depth matches the expected depth
 	}
 	
 	@Test
 	public void testModelWidth() {
 		int expectedWidth = width;
 		
 		ProjectSpace projectSpace = createProjectSpace();
 		ModelMutatorConfiguration mmc = createModelMutatorConfigurationSeed(projectSpace);
 		mmc.setWidth(expectedWidth);
 		ModelMutator.generateModel(mmc);
 		
 		int maxWidth = calculateMaxModelWidth(projectSpace.getProject());
 		
 		Assert.assertEquals("The calculated model width "+maxWidth+" does not match the expected width "+expectedWidth+".", expectedWidth, maxWidth);
 	}
 	
 	private int calculateMaxModelWidth(EObject parent) {
 		if (parent.eContents() == null || parent.eContents().isEmpty()) {
 			return 0;
 		}
 		
 		int maxWidth = parent.eContents().size();
 		for (EObject child : parent.eContents()) {
 			maxWidth = Math.max(maxWidth, calculateMaxModelWidth(child));
 		}
 		
 		return maxWidth;
 	}
 	
 	@Test
 	public void testDoNotGenerateRoot(){
 		ProjectSpace projectSpace = createProjectSpace();
 
 		ModelMutatorConfiguration mmc = createModelMutatorConfigurationSeed(projectSpace);
 		mmc.setDepth(2);
 		mmc.setDoNotGenerateRoot(true);
 		
 		for (int i=0; i<5; i++){
 			Kategorie newModelElement=TestModelFactory.eINSTANCE.createKategorie();
 			newModelElement.setName("Test"+i);
 			projectSpace.getProject().addModelElement(newModelElement);
 		}
 		
 		ModelMutator.generateModel(mmc);
 		Assert.assertEquals(5, projectSpace.getProject().getModelElements().size());
 	}
 	
 	@Test
 	public void testAllElementsOnRoot(){
 		ProjectSpace projectSpace = createProjectSpace();
 		
 		ModelMutatorConfiguration mmc = createModelMutatorConfigurationSeed(projectSpace);
 		mmc.setWidth(2);
 		mmc.setAllElementsOnRoot(true);
 		
 		ModelMutator.generateModel(mmc);
 		Set<EClass> differentClasses=new HashSet<EClass>();
 		for (EObject eObject : projectSpace.getProject().getAllModelElements()){
 			differentClasses.add(eObject.eClass());
 		}
 		Assert.assertEquals(5, differentClasses.size());
 	}
 
 }
