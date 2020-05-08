 package ac.soton.fmusim.codegen.translator;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eventb.codegen.il1.Il1Factory;
 import org.eventb.codegen.il1.Program;
 import org.eventb.codegen.il1.Protected;
 import org.eventb.codegen.il1.impl.Il1PackageImpl;
 import org.eventb.codegen.il1.translator.AbstractTranslateEventBToTarget;
 import org.eventb.codegen.il1.translator.ClassHeaderInformation;
 import org.eventb.codegen.il1.translator.IL1TranslationManager;
 import org.eventb.codegen.il1.translator.IL1TranslationUnhandledTypeException;
 import org.eventb.codegen.il1.translator.TargetLanguage;
 import org.eventb.codegen.il1.translator.core.AbstractProgramIL1Translator;
 import org.eventb.codegen.il1.translator.provider.ITranslationRule;
 import org.eventb.codegen.tasking.RMLDataStruct;
 import org.eventb.codegen.tasking.RelevantMachineLoader;
 import org.eventb.codegen.tasking.TaskingTranslationException;
 import org.eventb.codegen.tasking.TaskingTranslationManager;
 import org.eventb.codegen.tasking.TaskingTranslationUnhandledTypeException;
 import org.eventb.core.basis.ContextRoot;
 import org.eventb.core.basis.MachineRoot;
 import org.osgi.service.prefs.BackingStoreException;
 import org.rodinp.core.RodinDBException;
 
 import ac.soton.composition.core.basis.ComposedMachineRoot;
 import ac.soton.compositionmodel.core.compositionmodel.ComposedMachine;
 
 @SuppressWarnings("restriction")
 public class FMUTranslator extends AbstractTranslateEventBToTarget {
 
 	public static String COMMON_HEADER_PARTIAL = "Common";
 	public static String COMMON_HEADER_FULL = COMMON_HEADER_PARTIAL + ".h";
 	private static TaskingTranslationManager taskingTranslationManager = null;
 	private static TargetLanguage targetLanguage = new TargetLanguage("C");
 	private Protected currentProtected;
 
 	public void translateToFMU(IStructuredSelection s)
 			throws TaskingTranslationException, BackingStoreException,
 			CoreException, IOException, URISyntaxException,
 			IL1TranslationUnhandledTypeException {
 		this.setSelection(s);
 		// Generate an IL1 program using existing stage 1 code generator.
 		Program program = translateEventBToIL1(s);
 		// we now have an IL1 program.
 		// We assume we have modelled the FMU's as shared machines. This
 		// means that we can discard the master. The subroutines are then
 		// implemented
 		// as the FMI interface, fmiGetXXX and fmiDoStep etc.
 
 		// Here's an idea. How about a translation from Event-B to an FMI Model.
 		// Synchronised getters between controller and environment will need to
 		// mapped through a master.
 		// Polling-controller/and corresponding environ "tasks" map to fmiGetXXX
 		// ; fmiSetXXX
 		// where both controller and environ are converted to shared machines
 		// and have a master task.
 
 		translateIL1ToFMU(program);
 	}
 
 	private void translateIL1ToFMU(Program program)
 			throws IL1TranslationUnhandledTypeException, RodinDBException,
 			TaskingTranslationUnhandledTypeException {
 		// Now to the code generation
 		IL1TranslationManager il1TranslationManager = new IL1TranslationManager();
 
 		ArrayList<String> code = null;
 
 		// Translation Rules
 		Map<IProject, List<ITranslationRule>> translationRules = loadTranslatorRules();
 		il1TranslationManager.setTranslatorRules(translationRules);
 
 		// Types Rules
 		Map<IProject, List<ITranslationRule>> translationTypeRules = loadTranslatorTypeRules();
 		il1TranslationManager.setTranslatorTypeRules(translationTypeRules);
 
 		String directoryName = getFilePathFromSelected();
 		if (directoryName != null) {
 
 			// put each language and specialisation in a separate directory
 			String directoryNameA = directoryName + "src" + File.separatorChar;
 			String directoryNameB = directoryName + "src" + File.separatorChar
 					+ AbstractProgramIL1Translator.GENERATED_PACKAGE_NAME
 					// + "_" + getTargetLanguage().getCoreLanguage()
 					// + "_" + getTargetLanguage().getSpecificLanguage()
 					+ File.separatorChar;
 
 			// Add the directory information for code, does nothing if it
 			// already exists
 			File fa = new File(directoryNameA);
 			File fb = new File(directoryNameB);
 
 			fa.mkdir();
 			fb.mkdir();
 
			ArrayList<ClassHeaderInformation> headerInfo = il1TranslationManager
					.getClassHeaderInformation();
 			EList<Protected> protectedList = program.getProtected();
 			// for each protected object
 			for (Protected p : protectedList) {
 				code = il1TranslationManager.translateIL1ElementToCode(p,
 						getTargetLanguage());
 				code.add("// EndProtected");
 				currentProtected = p;
 				saveToFile(code, headerInfo, program, directoryNameB,
 						il1TranslationManager);
 			}
 		}
 		System.out.println();
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
 		Il1PackageImpl.init();
 		Il1Factory factory = Il1Factory.eINSTANCE;
 
 		taskingTranslationManager = new TaskingTranslationManager(factory);
 
 		Program program = taskingTranslationManager.translateToIL1Entry(list,
 				composedMachines, composedEvents, composedMachineNames,
 				relevantMachines);
 
 		// Now we have a program we need to save it somewhere
 
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
 
 		// If the target is not null then save it, else throw exception.
 		if (target != null) {
 			saveBaseProgram(program, targetFile(target));
 			storeProject(target.getProject(), taskingTranslationManager);
 		} else {
 			throw new TaskingTranslationException(
 					"TranslateToIL1: Unknown target location");
 		}
 
 		return program;
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
 	// only
 	// protected objects. We also want to save the 'protected object' code from
 	// shared machines
 	// since they map to the FMUs. We store the 'protected object' code in a
 	// field, temporarily,
 	// in TranslateIL1ToFMU, then call the saveToFile, and make use of it there
 	// rather than pass it as a parameter.
 	@Override
 	protected void saveToFile(ArrayList<String> codeToSave,
 			ArrayList<ClassHeaderInformation> headerInformation,
 			Program program, String directoryName,
 			IL1TranslationManager translationManager) {
 
 		ArrayList<String> globalDecls = new ArrayList<String>();
 
 		for (int lineNumber = 0; lineNumber < codeToSave.size(); lineNumber++) {
 			ArrayList<String> protectedCode = new ArrayList<String>();
 			lineNumber = getCodeBlock(codeToSave, lineNumber + 1,
 					"// EndProtected", protectedCode);
 			// Get the protected name
 			String name = currentProtected.getMachineName();
 			saveToFileHelper(protectedCode, name + ".c", directoryName);
 		}
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
				if (i.contains("Common.h")) {
					System.out.println("Debug");
				}
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
 
 }
