 /**
  * 
  */
 package hu.e.parser.convert;
 
 
 import hu.e.parser.convert.impl.ExpressionConverter;
 import hu.e.parser.convert.impl.FunctionConverter;
 import hu.e.parser.convert.impl.RootReferenceScope;
 import hu.e.parser.convert.impl.TypeConverter;
 import hu.e.parser.eSyntax.ConstantVariable;
 import hu.e.parser.eSyntax.InsctructionSectionNotation;
 import hu.e.parser.eSyntax.InstructionNotation;
 import hu.e.parser.eSyntax.InstructionParameterNotation;
 import hu.e.parser.eSyntax.InstructionSetNotation;
 import hu.e.parser.eSyntax.InstructionWordNotation;
 import hu.e.parser.eSyntax.Library;
 import hu.e.parser.eSyntax.LibraryItem;
 import hu.e.parser.eSyntax.Operation;
 import hu.e.parser.eSyntax.RegisterVariable;
 import hu.e.parser.eSyntax.Type;
 import hu.e.parser.eSyntax.Variable;
 import hu.modembed.model.core.assembler.AssemblerFactory;
 import hu.modembed.model.core.assembler.AssemblerPackage;
 import hu.modembed.model.core.assembler.ConstantSection;
 import hu.modembed.model.core.assembler.Instruction;
 import hu.modembed.model.core.assembler.InstructionParameter;
 import hu.modembed.model.core.assembler.InstructionSection;
 import hu.modembed.model.core.assembler.InstructionSet;
 import hu.modembed.model.core.assembler.InstructionWord;
 import hu.modembed.model.core.assembler.ParameterSection;
 import hu.modembed.model.emodel.EmodelFactory;
 import hu.modembed.model.emodel.HeapVariable;
 import hu.modembed.model.emodel.LibraryElement;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 
 /**
  * @author balazs.grill
  *
  */
 public class LibraryConverter {
 
 	public static final String MODELS_FOLDER = ".models";
 	
 	private final ResourceSet resourceSet;
 	
 	private final IProject project;
 	
 	public LibraryConverter(IProject project) {
 		this.resourceSet = new ResourceSetImpl();
 		this.project = project;
 	}
 	
 	private URI getLibURI(String name){
 		IFolder folder = project.getFolder(MODELS_FOLDER);
 		if (!folder.exists()){
 			try {
 				folder.create(true, true, new NullProgressMonitor());
 			} catch (CoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		IFile file = folder.getFile(name+".xmi");
 		return URI.createPlatformResourceURI(file.getFullPath().toString(), true);
 	}
 	
 	private LibraryElement convertElement(LibraryItem item, ICrossReferenceScope scope){
 		if (item instanceof Type){
 			Type from = (Type)item;
 			hu.modembed.model.emodel.Type to = EmodelFactory.eINSTANCE.createType();
 			
 			to.setName(from.getName());
 			to.setDefinition(TypeConverter.convertTypeDef(from.getDef(), scope));
 			
 			return to;
 		}
 		
 		if (item instanceof Operation){
 			return FunctionConverter.convert((Operation)item, scope);
 		}
 		
 		if (item instanceof Variable){
 			
 			if (item instanceof ConstantVariable){
 				hu.modembed.model.emodel.ConstantVariable cv = EmodelFactory.eINSTANCE.createConstantVariable();
 				cv.setName(item.getName());
 				cv.setType(TypeConverter.convertTypeDef(((Variable) item).getType(), scope));
 				cv.setValue(ExpressionConverter.convert(((ConstantVariable) item).getValue(), scope));
 				return cv;
 			}else if (item instanceof RegisterVariable){
 				hu.modembed.model.emodel.RegisterVariable rv = EmodelFactory.eINSTANCE.createRegisterVariable();
 				rv.setName(item.getName());
 				rv.setType(TypeConverter.convertTypeDef(((Variable) item).getType(), scope));
 				rv.setAddress(ExpressionConverter.convert(((RegisterVariable) item).getAddr(), scope));
 				return rv;
 			}else{
 				//Heap variable
 				HeapVariable hv = EmodelFactory.eINSTANCE.createHeapVariable();
 				hv.setName(item.getName());
 				hv.setType(TypeConverter.convertTypeDef(((Variable) item).getType(), scope));
 				return hv;
 			}
 			
 		}
 		
 		return null;
 	}
 	
 	public static int convertLiteral(String lit){
 		if (lit == null) return 0;
 		if (lit.length() == 0) return 0;
 		if (lit.toLowerCase().startsWith("b")){
 			return Integer.parseInt(lit.substring(1), 2);
 		}
 		if (lit.toLowerCase().startsWith("0x")){
 			return Integer.parseInt(lit.substring(2), 16);
 		}
 		return Integer.parseInt(lit);
 	}
 	
 	public Instruction convertInstruction(InstructionNotation in){
 		Instruction result = AssemblerFactory.eINSTANCE.createInstruction();
 		result.setName(in.getName());
 		
 		Map<Character, InstructionParameter> params = new HashMap<Character, InstructionParameter>();
 		for(InstructionParameterNotation pn : in.getParams()) {
 			InstructionParameter p = AssemblerFactory.eINSTANCE.createInstructionParameter();
 			p.setId(pn.getName().charAt(0));
 			p.setDefaultValue(convertLiteral(pn.getDefault()));
 			params.put(p.getId(), p);
 			result.getParameters().add(p);
 		}
 		
 		int start = 0;
 		for(InstructionWordNotation wn : in.getWords()){
 			InstructionWord w = AssemblerFactory.eINSTANCE.createInstructionWord();
 			for(InsctructionSectionNotation sn : wn.getSections()){
 				InstructionSection s = null;
 				if (null != sn.getParam()){
 					//Parameters
 					ParameterSection ps = AssemblerFactory.eINSTANCE.createParameterSection();
 					ps.setParameter(params.get(sn.getParam().charAt(0)));
 					s = ps;
 				}else{
 					//Constant
 					ConstantSection cs = AssemblerFactory.eINSTANCE.createConstantSection();
 					cs.setValue(convertLiteral(sn.getValue()));
 					s = cs;
 				}
 
 				if (s != null){
 					s.setStart(start);
 					s.setSize(convertLiteral(sn.getSize()));
 					s.setShift(convertLiteral(sn.getShift()));
 					w.getSections().add(s);
 					start += s.getSize();
 				}
 			}
 			result.getWords().add(w);
 		}
 		
 		return result;
 	}
 	
 	public void convert(InstructionSetNotation isetn){
 		InstructionSet result = AssemblerFactory.eINSTANCE.createInstructionSet();
 		result.setName(isetn.getName());
 		
 		ICrossReferenceScope scope = new RootReferenceScope(project, resourceSet, result, Collections.<String>emptyList());
 		if (isetn.getExtends() != null){
			scope.addCrossReference(result, AssemblerPackage.eINSTANCE.getInstructionSet_Extend(), isetn.getExtends());
 		}
 		
 		for(InstructionNotation in : isetn.getInstructions()){
 			result.getInstructions().add(convertInstruction(in));
 		}
 		
 		scope.resolveReferences();
 		
 		URI uri = getLibURI(result.getName());
 		Resource r = resourceSet.createResource(uri);
 		r.getContents().add(result);
 		try {
 			r.save(null);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void convert(Library library){
 		hu.modembed.model.emodel.Library result = EmodelFactory.eINSTANCE.createLibrary();
 		result.setName(library.getName());
 		
 		ICrossReferenceScope scope = new RootReferenceScope(project, resourceSet, result, library.getUse());
 		
 		for(LibraryItem item : library.getItems()){
 			LibraryElement le = convertElement(item, scope);
 			if (le != null){
 				result.getContent().add(le);
 			}
 		}
 		
 		scope.resolveReferences();
 		
 		URI uri = getLibURI(result.getName());
 		Resource r = resourceSet.createResource(uri);
 		r.getContents().add(result);
 		try {
 			r.save(null);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 }
