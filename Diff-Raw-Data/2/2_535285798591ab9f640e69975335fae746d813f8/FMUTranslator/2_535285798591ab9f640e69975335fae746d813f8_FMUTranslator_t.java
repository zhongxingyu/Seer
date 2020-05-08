 package ac.soton.fmusim.codegen;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.eclipse.cdt.core.model.CModelException;
 import org.eclipse.cdt.core.model.CoreModel;
 import org.eclipse.cdt.core.model.ICProject;
 import org.eclipse.cdt.core.model.IPathEntry;
 import org.eclipse.cdt.core.model.ISourceEntry;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eventb.codegen.il1.Declaration;
 import org.eventb.codegen.il1.Il1Factory;
 import org.eventb.codegen.il1.Program;
 import org.eventb.codegen.il1.Protected;
 import org.eventb.codegen.il1.Subroutine;
 import org.eventb.codegen.il1.impl.Il1PackageImpl;
 import org.eventb.codegen.il1.translator.AbstractTranslateEventBToTarget;
 import org.eventb.codegen.il1.translator.ClassHeaderInformation;
 import org.eventb.codegen.il1.translator.IL1TranslationManager;
 import org.eventb.codegen.il1.translator.IL1TranslationUnhandledTypeException;
 import org.eventb.codegen.il1.translator.TargetLanguage;
 import org.eventb.codegen.il1.translator.provider.ITranslationRule;
 import org.eventb.codegen.tasking.RMLDataStruct;
 import org.eventb.codegen.tasking.RelevantMachineLoader;
 import org.eventb.codegen.tasking.TaskingTranslationException;
 import org.eventb.codegen.tasking.TaskingTranslationManager;
 import org.eventb.codegen.tasking.TaskingTranslationUnhandledTypeException;
 import org.eventb.codegen.tasking.utils.CodeGenTaskingUtils;
 import org.eventb.core.ast.ITypeEnvironment;
 import org.eventb.core.ast.Type;
 import org.eventb.core.basis.ContextRoot;
 import org.eventb.core.basis.MachineRoot;
 import org.eventb.emf.core.machine.Machine;
 import org.eventb.emf.core.machine.Variable;
 import org.osgi.service.prefs.BackingStoreException;
 import org.rodinp.core.IRodinDB;
 import org.rodinp.core.IRodinFile;
 import org.rodinp.core.IRodinProject;
 import org.rodinp.core.RodinCore;
 import org.rodinp.core.RodinDBException;
 
 import FmiModel.BooleanType;
 import FmiModel.CoSimulationType;
 import FmiModel.DocumentRoot;
 import FmiModel.FmiModelDescriptionType;
 import FmiModel.FmiModelFactory;
 import FmiModel.FmiScalarVariable;
 import FmiModel.IntegerType;
 import FmiModel.ModelVariablesType;
 import FmiModel.RealType1;
 import FmiModel.StringType;
 import ac.soton.composition.core.basis.ComposedMachineRoot;
 import ac.soton.compositionmodel.core.compositionmodel.ComposedMachine;
 
 // This class is the entry point for the translation proper. 
 // UNLIKE the existing C code generator, it does not extend AbstractProgramIL1Translator.
 // It is not related to the extensibility mechanism implemented by Chris, i.e. does not
 // use an extension point. However, protected objects, and those nested within, do use it.
 
 @SuppressWarnings("restriction")
 public class FMUTranslator extends AbstractTranslateEventBToTarget {
 
 	public static final String REAL = "Real";
 	public static final String STRING = "String";
 	public static final String BOOLEAN = "Boolean";
 	public static final String INTEGER = "Integer";
 	public static String COMMON_HEADER_PARTIAL = "Common";
 	public static String COMMON_HEADER_FULL = COMMON_HEADER_PARTIAL + ".h";
 	// The target source folder for the translation - static to enable the IL1
 	// C translator to reference it
 	public static IFolder targetSourceFolder = null;
 	private static TaskingTranslationManager taskingTranslationManager = null;
 	private static TargetLanguage targetLanguage = new TargetLanguage("FMI_C");
 	private static ArrayList<DocumentRoot> docRootList = new ArrayList<DocumentRoot>();
 
 	private Protected currentProtected;
 	// Keep a local count here value references of variable arrays.
 	// This is reset to zero for each machine.
 	private int realVariableCount = 0;
 	private int stringVariableCount = 0;
 	private int integerVariableCount = 0;
 	private int boolVariableCount = 0;
 
 	private IProject targetProject = null;
 
 
 	// Translate the selected Composed Machine/Event-B Machine to FMU(s)
 	public void translateToFMU(IStructuredSelection s)
 			throws TaskingTranslationException, BackingStoreException,
 			CoreException, IOException, URISyntaxException,
 			IL1TranslationUnhandledTypeException {
 		this.setSelection(s);
 		// Initialise the tasking translation manager
 		Il1PackageImpl.init();
 		Il1Factory factory = Il1Factory.eINSTANCE;
 		taskingTranslationManager = new TaskingTranslationManager(factory);
 		// Generate an IL1 program using existing stage 1 code generator.
 		Program program = translateEventBToIL1(s);
 		// Create a target Directory
 		createTargetProject(taskingTranslationManager);
 		// From the program, we can create the modelDescription file
 		createModelDescriptionFile(program);
 		// we can generate the FMU from the IL1program.
 		translateIL1ToFMU(program);
 		// reflect the changes in the model, back to the workspace.
 		updateResources();
 	}
 
 	private void createTargetProject(TaskingTranslationManager taskingTranslationManager) throws CoreException,
 			TaskingTranslationException {
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 
 		targetProject = root.getProject(TaskingTranslationManager.getProject()
 				.getName() + "Targetx");
 		if (!targetProject.exists()) {
 			targetProject.create(null);
 			targetProject.open(null);
 			if (!targetProject.hasNature("org.eclipse.cdt.core.cnature")) {
 				IProjectDescription description = targetProject.getDescription();
 				String[] natures = description.getNatureIds();
 				String[] newNatures = new String[natures.length + 1];
 				System.arraycopy(natures, 0, newNatures, 0, natures.length);
 				newNatures[natures.length] = "org.eclipse.cdt.core.cnature";
 				// We can put these back in if we decide to do managed builds etc.
 //				newNatures[natures.length + 1] = "org.eclipse.cdt.managedbuilder.core.managedBuildNature";
 //				newNatures[natures.length + 2] = "org.eclipse.cdt.managedbuilder.core.ScannerConfigNature";
 				description.setNatureIds(newNatures);
 				targetProject.setDescription(description, null);
 			}
 		}
 		// create C source folder in the project called "src" for generated source
 		// and external for inherited code
 		createCSourceFolder(targetProject, "src");
 		createCSourceFolder(targetProject, "external");
 		
 	}
 
 
 
 	private void createCSourceFolder(IProject targetProject, String newDirectoryName)
 			throws CoreException, CModelException {
 		// obtain a model,project and folder
 		CoreModel cModel = CoreModel.getDefault();
 		ICProject cProject = cModel.create(targetProject);
 		// This is where this 'important' target source directory is created!!!
 		targetSourceFolder  = targetProject.getFolder(newDirectoryName);
 		targetSourceFolder.create(true, true, null);
 		// create a new sourceEntry (i.e. identifies this folder as a C source folder)
 		ISourceEntry newSourceEntry = CoreModel
 				.newSourceEntry(targetSourceFolder.getFullPath());
 		// now add it to the cProject info, by creating a new list of entries.
 		IPathEntry[] existingPathEntries = cProject.getRawPathEntries();
 		List<IPathEntry> newEntries = new ArrayList<IPathEntry>(
 				existingPathEntries.length);
 		// add existingPathEntries to newEntries
 		newEntries.addAll(Arrays.asList(existingPathEntries));
 		// add the new sourceEntry
 		newEntries.add(newSourceEntry);
 		// set the pathEntry values in the project
 		cProject.setRawPathEntries(
 				newEntries.toArray(new IPathEntry[newEntries.size()]), null);
 	}
 
 	private void createModelDescriptionFile(Program program)
 			throws IOException, TaskingTranslationException, CModelException,
 			CoreException {
 
 		ArrayList<Machine> fmuMachineList = taskingTranslationManager
 				.getFMUMachineList();
 		for (Machine fmuMachine : fmuMachineList) {
 			// Reset the value reference array indices for each machine.
 			realVariableCount = 0;
 			stringVariableCount = 0;
 			integerVariableCount = 0;
 			boolVariableCount = 0;
 			// Each fmuMachine will have its own DocumentRoot
 			DocumentRoot docRoot = FmiModelFactory.eINSTANCE
 					.createDocumentRoot();
 			// add this machine documentroot to the list
 			docRootList.add(docRoot);
 			// set various values
 			FmiModelDescriptionType descriptionType = FmiModelFactory.eINSTANCE
 					.createFmiModelDescriptionType();
 			docRoot.setFmiModelDescription(descriptionType);
 			descriptionType.setFmiVersion("2.0");
 			descriptionType.setGenerationTool("EB2FMU");
 			descriptionType.setAuthor("University of Southampton");
 			XMLGregorianCalendar xmlGC = makeDate();
 			descriptionType.setGenerationDateAndTime(xmlGC);
 			descriptionType.setGuid("GUID_" + fmuMachine.getName() + "_"
 					+ xmlGC.toXMLFormat());
 			descriptionType.setModelName(fmuMachine.getName());
 			// This is a co-simulation
 			CoSimulationType coSimType = FmiModelFactory.eINSTANCE
 					.createCoSimulationType();
 			descriptionType.getCoSimulation().add(coSimType);
 			// This is where we store the FMI scalar variables
 			ModelVariablesType modelVarsType = FmiModelFactory.eINSTANCE
 					.createModelVariablesType();
 			descriptionType.setModelVariables(modelVarsType);
 			// Get the root so we can obtain the type environment
 			IRodinDB rodinDB = RodinCore.getRodinDB();
 			IRodinProject rodinProject = rodinDB.getRodinProject(program
 					.getProjectName());
			IRodinFile mchFile = rodinProject.getRodinFile(fmuMachine.getName()
 					+ ".bum");
 			MachineRoot root = (MachineRoot) mchFile.getRoot();
 			EList<Variable> variableList = fmuMachine.getVariables();
 			// get the FMI type from the type environment
 			ITypeEnvironment typeEnv = taskingTranslationManager
 					.getTypeEnvironment(root);
 			// Iterate through the machine's variables and generate FMIScalar
 			// values
 			for (Variable var : variableList) {
 				variableToFMIScalar(modelVarsType, typeEnv, var);
 			}
 			// create a descriptions folder.
 			String fileName = fmuMachine.getName() + "."
 					+ FmiModelFactory.eINSTANCE.getEPackage().getName().toLowerCase();
 			File newFile = createNewFile(fileName, "descriptions");
 			String netUri = newFile.toURI().toString();
 			URI emfURI = URI.createURI(netUri);
 			ResourceSet resSet = new ResourceSetImpl();
 			Resource resource = resSet.createResource(emfURI);
 			resource.getContents().add(docRoot);
 			resource.save(Collections.EMPTY_MAP);
 		}// end of foreach machine
 	}// end of createModelDescriptionFile(...);
 
 	// create a new file, with fileName, in the named subFolder of 'the' targetProject.
 	private File createNewFile(String fileName, String subFolderName) throws CoreException,
 			IOException {
 		IFolder newFolder = targetProject.getFolder(subFolderName);
 		if (!newFolder.exists()) {
 			newFolder.create(true, true, null);
 		}
 
 		// construct the new fileName for the model description
 		String directoryPath = 
 				newFolder.getRawLocation().toString() + File.separatorChar;
 		// construct the new fileName path for the model description
 		String fPathName = directoryPath
 				+ fileName;
 		File newFile = new File(fPathName);
 		boolean success = newFile.createNewFile();
 		// force creation of a new file
 		if (!success) {
 			newFile.delete();
 			newFile.createNewFile();
 		}
 		return newFile;
 	}
 
 	// The method populates the ModelVariables segment with scalar
 	// variables, generated from the variable's type etc.
 	private void variableToFMIScalar(ModelVariablesType modelVarsType,
 			ITypeEnvironment typeEnv, Variable var) {
 		Type type = typeEnv.getType(var.getName());
 		// Create and set an fmiScalar value for each variable
 		FmiScalarVariable scalar = FmiModelFactory.eINSTANCE
 				.createFmiScalarVariable();
 		modelVarsType.getScalarVariable().add(scalar);
 		scalar.setName(var.getName());
 		String typeString = getFMITypeString(type);
 		// Add a type if it is an integer
 		if (typeString.equals(INTEGER)) {
 			scalar.setValueReference(integerVariableCount);
 			integerVariableCount++;
 			IntegerType integerType = FmiModelFactory.eINSTANCE
 					.createIntegerType();
 			scalar.setInteger(integerType);
 		}
 		// else if it is a real
 		else if (typeString.equals(REAL)) {
 			scalar.setValueReference(realVariableCount);
 			realVariableCount++;
 			RealType1 realType = FmiModelFactory.eINSTANCE.createRealType1();
 			scalar.setReal(realType);
 		}
 		// elseif it is a string
 		else if (typeString.equals(STRING)) {
 			scalar.setValueReference(stringVariableCount);
 			stringVariableCount++;
 			StringType stringType = FmiModelFactory.eINSTANCE
 					.createStringType();
 			scalar.setString(stringType);
 		}
 		// elsif it is a boolean
 		else if (typeString.equals(BOOLEAN)) {
 			scalar.setValueReference(boolVariableCount);
 			boolVariableCount++;
 			BooleanType boolType = FmiModelFactory.eINSTANCE
 					.createBooleanType();
 			scalar.setBoolean(boolType);
 		}
 	}
 
 	public static String getFMIType(Type type)
 			throws TaskingTranslationException {
 		String fmiTypeName = null;
 		String typeAsString = type.toString();
 		if (typeAsString.equalsIgnoreCase(CodeGenTaskingUtils.INT_SYMBOL)) {
 			fmiTypeName = INTEGER;
 		} else if (typeAsString
 				.equalsIgnoreCase(CodeGenTaskingUtils.BOOL_SYMBOL)) {
 			fmiTypeName = BOOLEAN;
 		} else if (typeAsString.equalsIgnoreCase(STRING)) {
 			fmiTypeName = STRING;
 		} else if (typeAsString.equalsIgnoreCase(REAL)) {
 			fmiTypeName = REAL;
 		}
 		if (fmiTypeName == null) {
 			throw new TaskingTranslationException("FMI Type not found for: "
 					+ type.toString());
 		} else {
 			return fmiTypeName;
 		}
 	}
 
 	private XMLGregorianCalendar makeDate() {
 		DatatypeFactory df = null;
 		Date date = new Date();
 		try {
 			df = DatatypeFactory.newInstance();
 		} catch (DatatypeConfigurationException dce) {
 			throw new IllegalStateException(
 					"Exception while obtaining DatatypeFactory instance", dce);
 		}
 		GregorianCalendar gc = new GregorianCalendar();
 		gc.setTimeInMillis(date.getTime());
 		XMLGregorianCalendar xmlGC = df.newXMLGregorianCalendar(gc);
 		return xmlGC;
 	}
 
 	// This method translates Event-B models into an IL1 program
 	private static Program translateEventBToIL1(IStructuredSelection s)
 			throws TaskingTranslationException, BackingStoreException,
 			CoreException, IOException, URISyntaxException {
 		Object[] list = s.toArray();
 		// load all the machines into a pre-prepared structure.
 		RMLDataStruct relevantMachines = RelevantMachineLoader
 				.getAllRelevantMachines(list);
 
 		list = relevantMachines.machines;
 		ArrayList<ComposedMachine> composedMachines = relevantMachines.composedMachines;
 		Map<String, String> composedEvents = relevantMachines.composedEvents;
 		ArrayList<String> composedMachineNames = relevantMachines.composedMachineNames;
 
 		IFile target = null;
 		// Get target's location from the list which is derived from the
 		// structured selection.
 		for (Object obj : list) {
 			if (obj instanceof MachineRoot) {
 				target = ((MachineRoot) obj).getResource();
 				break;
 			} else if (obj instanceof ContextRoot) {
 				target = ((ContextRoot) obj).getResource();
 				break;
 			} else if (obj instanceof ComposedMachineRoot) {
 				target = ((ComposedMachineRoot) obj).getResource();
 				break;
 			}
 		}
 		storeProject(target.getProject(), taskingTranslationManager);
 
 		Program program = taskingTranslationManager.translateToIL1Entry(list,
 				composedMachines, composedEvents, composedMachineNames,
 				relevantMachines);
 
 		// We delete the temporary subroutines
 		EList<Protected> protectedList = program.getProtected();
 		for (Protected prot : protectedList) {
 			EList<Subroutine> subroutineList = prot.getSubroutines();
 			List<Subroutine> tmpSubroutine = new ArrayList<Subroutine>();
 			for (Subroutine subroutine : subroutineList) {
 				if (!subroutine.isTemporary()) {
 					tmpSubroutine.add(subroutine);
 				}
 			}
 			if (tmpSubroutine.size() != subroutineList.size()) {
 				subroutineList.clear();
 				subroutineList.addAll(tmpSubroutine);
 			}
 		}
 		saveBaseProgram(program, targetFile(target));
 		return program;
 	}
 
 	// This method is equivalent to CProgramIL1Translator, tailored for
 	// use with FMI translation.
 	private void translateIL1ToFMU(Program program)
 			throws IL1TranslationUnhandledTypeException, RodinDBException,
 			TaskingTranslationUnhandledTypeException {
 		// Now to the code generation
 		IL1TranslationManager il1TranslationManager = new IL1TranslationManager();
 
 		boolean hasBool = false;
 		TreeIterator<EObject> programContentList = program.eAllContents();
 		while (programContentList.hasNext()) {
 			EObject obj = programContentList.next();
 			if (obj instanceof Declaration) {
 				Declaration decl = (Declaration) obj;
 				if (decl.getType().equals(CodeGenTaskingUtils.BOOL_SYMBOL)) {
 					hasBool = true;
 					break;
 				}
 			}
 		}
 
 		// If we have any boolean variable then add the BOOL definitions
 		if (hasBool) {
 			// Output OpenMP blocking
 			il1TranslationManager.addIncludeStatement("typedef int BOOL;");
 			il1TranslationManager.addIncludeStatement("#define TRUE 1");
 			il1TranslationManager.addIncludeStatement("#define FALSE 0");
 		}
 
 		ArrayList<String> code = null;
 
 		// Translation Rules
 		Map<IProject, List<ITranslationRule>> translationRules = loadTranslatorRules();
 		il1TranslationManager.setTranslatorRules(translationRules);
 
 		// Types Rules
 		Map<IProject, List<ITranslationRule>> translationTypeRules = loadTranslatorTypeRules();
 		il1TranslationManager.setTranslatorTypeRules(translationTypeRules);
 
 		String parentDirectoryPath = getFilePathFromSelected();
 		if (parentDirectoryPath != null) {
 			// make the file system ready.
 			String newDirectoryPath = 
 					FMUTranslator.targetSourceFolder.getRawLocation().toString()
 					+ File.separatorChar;
 			
 			ArrayList<ClassHeaderInformation> headerInfo = il1TranslationManager
 					.getClassHeaderInformation();
 			EList<Protected> protectedList = program.getProtected();
 			// for each protected object
 			for (Protected p : protectedList) {
 				code = il1TranslationManager.translateIL1ElementToCode(p,
 						getTargetLanguage());
 				code.add(0, "#include \"" + COMMON_HEADER_FULL + "\"");
 				code.add("// EndProtected");
 				currentProtected = p;
 				saveToFile(code, headerInfo, program, newDirectoryPath,
 						il1TranslationManager);
 			}
 		}
 		System.out.println();
 	}
 
 	// Create the file associated with the output
 	// The sourceRes is the container of the MainClass
 	// element that we want to transform
 	protected static String targetFile(IFile source) throws URISyntaxException {
 
 		java.net.URI location = source.getLocationURI();
 		IPath p = new Path(location.getPath());
 
 		IPath newPath = p.removeFileExtension();
 		String path = newPath + ".il1";
 
 		return path;
 	}
 
 	protected static void saveBaseProgram(Program program, String filename)
 			throws IOException {
 		URI uri = URI.createFileURI(filename);
 		Resource outResource = new XMLResourceFactoryImpl().createResource(uri);
 		outResource.getContents().add(program);
 		outResource.save(null);
 	}
 
 	private static void storeProject(IProject project,
 			TaskingTranslationManager translationManager) {
 		translationManager.setProject(project);
 	}
 
 	@Override
 	protected TargetLanguage getTargetLanguage() {
 		return targetLanguage;
 	}
 
 	@Override
 	protected ArrayList<String> formatCode(ArrayList<String> code,
 			IL1TranslationManager translationManager) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	// We override the saveToFile since we do not need to write tasks for FMU's
 	// only protected objects. We also want to save the 'protected object' 
 	// code from shared machines since they map to the FMUs. We store the
 	// 'protected object' code in a field, temporarily, in TranslateIL1ToFMU,
 	// then call the saveToFile, and make use of it there rather than pass it
 	// as a parameter.
 	@Override
 	protected void saveToFile(ArrayList<String> codeToSave,
 			ArrayList<ClassHeaderInformation> headerInformation,
 			Program program, String directoryName,
 			IL1TranslationManager translationManager) {
 
 		ArrayList<String> globalDecls = new ArrayList<String>();
 
 		for (int lineNumber = 0; lineNumber < codeToSave.size(); lineNumber++) {
 			ArrayList<String> protectedCode = new ArrayList<String>();
 			// In the original code (the default C code) we used lineNumber + 1.
 			// For FMUs we use lineNumber, to pick up the 'include "common.h"'
 			// statement.
 			lineNumber = getCodeBlock(codeToSave, lineNumber,
 					"// EndProtected", protectedCode);
 			// Get the protected name
 			String name = currentProtected.getMachineName();
 			saveToFileHelper(protectedCode, name + ".c", directoryName);
 		}
 		// Generate the header files.
 		// Each protected file just includes "common.h" which includes the other
 		// files.
 		generateHeaders(headerInformation, directoryName, translationManager,
 				globalDecls);
 	}
 
 	private void generateHeaders(
 			ArrayList<ClassHeaderInformation> headerInformation,
 			String directoryName, IL1TranslationManager translationManager,
 			ArrayList<String> globalDecls) {
 		// Now sort out header files
 		// For common header
 		ClassHeaderInformation common = new ClassHeaderInformation();
 		common.className = COMMON_HEADER_PARTIAL;
 
 		// wont use initial now, will add headers manually, then add common
 		// class for compiler specific code
 		common.functionDeclarations.addAll(translationManager
 				.getIncludeStatements());
 
 		// Add any global declarations
 		common.functionDeclarations.addAll(globalDecls);
 
 		// Add the header files to include in the initial data
 		for (ClassHeaderInformation c : headerInformation) {
 			String headerName = c.className + ".h";
 			if (!headerName.equalsIgnoreCase("common.h")) {
 				common.functionDeclarations.add("#include \"" + headerName
 						+ "\"");
 			}
 		}
 		headerInformation.add(common);
 
 		if (translationManager.getCompilerDependentExecutableCodeBlock().size() > 0) {
 			ArrayList<String> commonCode = new ArrayList<String>();
 			// commonCode.add(codeGenerateTimestamp);
 			commonCode.add("#include \"" + COMMON_HEADER_FULL + "\"");
 			commonCode.addAll(formatCode(translationManager
 					.getCompilerDependentExecutableCodeBlock(),
 					translationManager));
 			this.saveToFileHelper(commonCode, "Common.c", directoryName);
 		}
 
 		// Save the header files
 		for (ClassHeaderInformation c : headerInformation) {
 			String headerName = c.className;
 			String headerPreBlock = c.className.toUpperCase() + "_H";
 
 			ArrayList<String> headerCode = new ArrayList<String>();
 			// headerCode.add(codeGenerateTimestamp);
 			headerCode.add("#ifndef " + headerPreBlock);
 			headerCode.add("#define " + headerPreBlock);
 
 			for (String i : c.functionDeclarations) {
 				headerCode.add(i);
 			}
 
 			headerCode.add("#endif");
 			headerCode.add(""); // blank line
 
 			this.saveToFileHelper(headerCode, headerName + ".h", directoryName);
 		}
 	}
 
 	protected int getCodeBlock(ArrayList<String> codeIn, int startIdx,
 			String endStatement, ArrayList<String> codeOut) {
 		int endIdx = startIdx;
 		for (int i = startIdx; i < codeIn.size()
 				&& !codeIn.get(i).equals(endStatement); i++, endIdx++) {
 			codeOut.add(codeIn.get(i));
 		}
 		return endIdx;
 	}
 
 	protected String getName(ArrayList<String> codeIn, String lhs) {
 		// Find first occurence of the lhs string
 		// As the first lines may be include / import statements
 		for (String s : codeIn) {
 			if (s.startsWith(lhs)) {
 				return s.split(lhs)[1].trim();
 			}
 		}
 		return ""; // something went wrong
 	}
 
 	public static String getFMITypeString(Type type) {
 		String fmiTypeName = null;
 		String typeAsString = type.toString();
 		if (typeAsString.equalsIgnoreCase(CodeGenTaskingUtils.INT_SYMBOL)) {
 			fmiTypeName = INTEGER;
 		} else if (typeAsString
 				.equalsIgnoreCase(CodeGenTaskingUtils.BOOL_SYMBOL)) {
 			fmiTypeName = FMUTranslator.BOOLEAN;
 		} else if (typeAsString.equalsIgnoreCase(STRING)) {
 			fmiTypeName = STRING;
 		} else if (typeAsString.equalsIgnoreCase(REAL)) {
 			fmiTypeName = REAL;
 		}
 		return fmiTypeName;
 	}
 
 }
