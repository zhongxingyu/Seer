 /**
  * Generated with Acceleo
  */
 package org.eclipse.emf.samples.conference.tests.junit;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.samples.conference.ConferencePackage;
 import org.eclipse.emf.samples.conference.providers.ConferenceMessages;
 import org.eclipse.emf.eef.runtime.EMFPropertiesRuntime;
 import org.eclipse.emf.eef.runtime.tests.SWTBotEEFTestCase;
 import org.eclipse.emf.eef.runtime.tests.exceptions.InputModelInvalidException;
 import org.eclipse.emf.eef.runtime.tests.utils.EEFTestsModelsUtils;
 import org.eclipse.emf.eef.runtime.tests.utils.EEFTestsResourceUtils;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 
 
 /**
  * TestCase for Conference
  * 
  */
 public class ConferenceTestCase extends SWTBotEEFTestCase {
 	
 	/**
 	 *  The project that contains models for tests 
 	 */
	protected static final String TESTS_PROJECT_NAME = "org.eclipse.emf.sample.eef.tests.junit";
 	
 	/**
 	 *  The folder that contains the input models for tests 
 	 */
 	protected static final String INPUT_MODEL_FOLDER = "input";
 	
 	/**
 	 *  The folder that contains the expected models for tests 
 	 */	 
 	protected static final String EXPECTED_MODEL_FOLDER = "expected";
 	
 	/**
 	 * The input model
 	 */
 	protected static final String INPUT_MODEL_NAME = "input.conference";
 	
 	/**
 	 * The expected model
 	 */	 
 	protected static final String EXPECTED_MODEL_NAME = "expected.conference";
 
 	/**
 	 * The test project
 	 */
 	private IProject testProject;
 	
 	/**
 	 * The workspace folder containing the input model 
 	 */
 	private IFolder modelFolder;
 	
 	/**
 	 * The expectedModel 
 	 */
 	protected Resource expectedModel;
 	
 	/**
 	 * The ResourceSet where to operate
 	 */
 	protected AdapterFactoryEditingDomain editingDomain = new AdapterFactoryEditingDomain(EMFPropertiesRuntime.getDefault().getAdapterFactory(), new BasicCommandStack());
 	
 	/**
 	 * The EClass of the type to edit
 	 */
 	private EClass conferenceMetaClass = ConferencePackage.eINSTANCE.getConference();
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.tests.SWTBotEEFTestCase#initWorkspaceForTests()
 	 */
 	protected void initWorkspaceForTests() throws CoreException, IOException {
 		List<String> names = new ArrayList<String>();
 		names.add(INPUT_MODEL_FOLDER);
 		testProject = EEFTestsResourceUtils.createTestProject(TESTS_PROJECT_NAME, names);
 		modelFolder = testProject.getFolder(INPUT_MODEL_FOLDER);
 	}
 
 	/**
 	 * Import the input model
 	 * @throws CoreException error during model import
 	 * @throws IOException error during model import
 	 */
 	protected void initializeInputModel() throws CoreException, IOException  {
 		EEFTestsResourceUtils.importModel(TESTS_PROJECT_NAME, "models/" + INPUT_MODEL_FOLDER + "/" + INPUT_MODEL_NAME, modelFolder);
 		URI fileURI = URI.createPlatformResourceURI(TESTS_PROJECT_NAME + "/" + INPUT_MODEL_FOLDER + "/" + INPUT_MODEL_NAME, true);
 		Resource activeResource = editingDomain.getResourceSet().getResource(fileURI, true);
 		bot.defineActiveModel(activeResource);
 	}
 	
 	/**
 	 * Delete the test models
 	 * @throws CoreException error during model deleting
 	 */
 	protected void deleteModels() throws CoreException {
 		IFile inputFile = EEFTestsResourceUtils.workspaceFile(bot.getActiveResource());
 		bot.unloadActiveModel();
 		NullProgressMonitor monitor = new NullProgressMonitor();
 		inputFile.delete(true, true, monitor);
 		IFile expectedFile = EEFTestsResourceUtils.workspaceFile(expectedModel);
 		expectedModel.unload();
 		expectedFile.delete(true, true, monitor);
 		testProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
 	}
 	
 	/**
 	 * Create the expected model from the input model
 	 * @throws InputModelInvalidException error during expected model initialization
 	 * @throws IOException error during expected model serialization
 	 */
 	protected void initializeExpectedModelForConferencePlace() throws InputModelInvalidException, IOException {
 		// Create the resource for the expected model
 		URI fileURI = URI.createPlatformResourceURI(TESTS_PROJECT_NAME + "/" + EXPECTED_MODEL_FOLDER + "/" + EXPECTED_MODEL_NAME, true);
 		expectedModel = editingDomain.getResourceSet().createResource(fileURI);
 		
 		// Create the expected model content by applying the attempted command on a copy of the input model content
 		expectedModel.getContents().addAll(EcoreUtil.copyAll(bot.getActiveResource().getContents()));
 		EObject conference = EEFTestsModelsUtils.getFirstInstanceOf(expectedModel, conferenceMetaClass);
 		if (conference == null)
 			throw new InputModelInvalidException("The input model doesn't contain enough instance of " + conferenceMetaClass.getName() + " EClass");
 		CompoundCommand cc = new CompoundCommand();
 		
 		cc.append(SetCommand.create(editingDomain, conference, ConferencePackage.eINSTANCE.getConference_Place(), "value2"));
 		editingDomain.getCommandStack().execute(cc);
 		expectedModel.save(Collections.EMPTY_MAP);
 	}
 	
     /**
 	 * Test the wizard properties :
 	 * - init the input model
 	 * - calculate the expected model
 	 * - initialize the model editor
 	 * - change the properties in the wizard properties
 	 * - compare the expected and the real model : if they are equals the test pass
 	 * - delete the models
 	 */
 	public void testEditConferencePlace() throws Exception {
 		
 		// Import the input model
 		initializeInputModel();
 		
 		// Create the expected model
 		initializeExpectedModelForConferencePlace();
 		
 		// Open the input model with the treeview editor
 		SWTBotEditor modelEditor = bot.openActiveModel();
 				
 		// Open the EEF wizard (by double click) to edit the Conference element
 		EObject firstInstanceOf = EEFTestsModelsUtils.getFirstInstanceOf(bot.getActiveResource(), conferenceMetaClass);
 		if (firstInstanceOf == null)
 			throw new InputModelInvalidException("The input model doesn't contain enough instance of " + conferenceMetaClass.getName() + " EClass");
 		
 		SWTBotShell wizardShell = bot.prepareBatchEditing(modelEditor, conferenceMetaClass, firstInstanceOf);
 		
 		// Change value of the place feature of the Conference element 
 		bot.editTextFeature(wizardShell, ConferenceMessages.ConferencePropertiesEditionPart_PlaceLabel, "value2");
 				
 		// Save the changement
 		bot.finalizeEdition(modelEditor);
 		
 		// Compare real model with expected model
 		assertExpectedModelReached(expectedModel);
 		
 		// Delete the input model
 		deleteModels();
 
 	}
 	
 
 
 }
