 package de.fraunhofer.esk.openetcs.sysml2b;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.papyrus.sysml.blocks.Block;
 import org.eclipse.uml2.uml.Model;
 import org.eclipse.uml2.uml.NamedElement;
 import org.eclipse.uml2.uml.Stereotype;
// import org.eclipse.uml2.uml.util.UMLUtil;
 import de.fraunhofer.esk.openetcs.sysml2b.StructureMapping;
 
 public class FileGenerator {
 	private String pathToBMchs;
 	private Model sysmlModel;
 	
 	public FileGenerator(IFile modelFile) {
 		pathToBMchs = modelFile.getLocation().removeLastSegments(1).toOSString() + "/src/";
 		File file = new File(pathToBMchs);
 		
 		if (!file.exists())	{
 			file.mkdir();
 		}
 		
 		sysmlModel = getModel(modelFile);
 	}
 
 
 	public void generateAndWrite() {
 
 		for (NamedElement element: sysmlModel.getOwnedMembers()) {
 			Stereotype stereotype;
 			System.out.println(element.getName());
 			for (Stereotype st : element.getAppliedStereotypes())
 			{
 				System.out.println(st.getName());
 			}
 			System.out.println("-------------");
 			
 			if ((stereotype = element.getAppliedStereotype("SysML::Blocks::Block")) != null) {
 				createMachineFile((Block) element.getStereotypeApplication(stereotype));
 			}
 		}
 	}
 	
 	private Model getModel(IFile modelFile)
 	{
 		ResourceSet resourceSet = new ResourceSetImpl();
 		URI uri = URI.createURI(modelFile.getFullPath().toString());
 		Resource resource = resourceSet.getResource(uri, true);
 		Model model = (Model) resource.getContents().get(0);
 		
 		return model;
 	}
 	
 	private void createMachineFile(Block block) {
 		StructureMapping mapping = new StructureMapping();
 		
 		writeToFile(block.getBase_Class().getName() + ".mch", mapping.createMachine(block).toString());
 	}
 	
 	public void writeToFile(String filename, String content) {
 		try {
 			FileWriter fileStream = new FileWriter(pathToBMchs + filename);
 			BufferedWriter out = new BufferedWriter(fileStream);
 			out.write(content);
 			out.close();
 		} catch (IOException ioe) {
 			System.err.println("Error: " + ioe.getMessage());
 		}
 	}
 }
