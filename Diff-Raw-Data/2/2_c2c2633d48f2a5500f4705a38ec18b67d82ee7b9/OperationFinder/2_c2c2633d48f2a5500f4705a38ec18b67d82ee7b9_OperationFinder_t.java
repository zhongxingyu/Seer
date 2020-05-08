 /**
  * 
  */
 package hu.e.compiler.internal;
 
 import hu.e.compiler.ECompilerException;
 import hu.e.compiler.internal.model.symbols.ILiteralSymbol;
 import hu.e.compiler.internal.model.symbols.ISymbol;
 import hu.e.compiler.internal.model.symbols.IVariableSymbol;
 import hu.e.parser.eSyntax.DataTypeDef;
 import hu.e.parser.eSyntax.Operation;
 import hu.e.parser.eSyntax.OperationRole;
 import hu.e.parser.eSyntax.OperatorDefinition;
 import hu.e.parser.eSyntax.Package;
 import hu.e.parser.eSyntax.ParameterKind;
 import hu.e.parser.eSyntax.ParameterVariable;
 import hu.e.parser.eSyntax.TopLevelItem;
 import hu.e.parser.eSyntax.TypeDef;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.EcoreUtil2;
 
 /**
  * @author balazs.grill
  *
  */
 public class OperationFinder {
 
 	private final Package pack; 
 	private final EObject element;
 	
 	public EObject getElement() {
 		return element;
 	}
 	
 	public OperationFinder(EObject element) {
 		this.element = element;
 		EObject eo = element.eResource().getContents().get(0);
 		if (eo instanceof Package){
 			this.pack = (Package)eo;
 		}else{
 			throw new IllegalArgumentException(element+" is not contained by a namespace!");
 		}
 	}
 	
 	public OperationCompiler getOperationCompiler(OperationRole role, ISymbol...symbols) throws ECompilerException{
 		Operation op = getOperation(role, symbols);
 		if (op == null) return null;
 		OperationCompiler opcomp = new OperationCompiler(op);
 		
 		for(int i=0;i<symbols.length;i++){
 			opcomp.addParameter(op.getParams().get(i).getVar(), symbols[i]);
 		}
 		
 		return opcomp;
 	}
 	
 	public Operation getOperation(OperationRole role, ISymbol...symbols) throws ECompilerException{
 		OperatorDefinition opdef = findDef(role);
 		if (opdef == null) return null;
 		for(Operation op : opdef.getCandidate()){
 			if (checkOperation(op, symbols)) return op;
 		}
 		return null;
 	}
 	
 	private OperatorDefinition findDef(OperationRole role){
 		for(TopLevelItem tli : pack.getItems()){
 			if (tli instanceof OperatorDefinition){
 				if (role == ((OperatorDefinition) tli).getRole())
 					return (OperatorDefinition)tli;
 			}
 		}
 		for(Package ipack : pack.getUses()){
 			for(TopLevelItem tli : ipack.getItems()){
 				if (tli instanceof OperatorDefinition){
 					if (role == ((OperatorDefinition) tli).getRole())
 						return (OperatorDefinition)tli;
 				}
 			}
 		}
 		return null;
 	}
 	
 	private boolean checkOperation(Operation op, ISymbol...symbols) throws ECompilerException{
 		if (op.eIsProxy()) op = (Operation) EcoreUtil2.resolve(op, pack);
 		if (op.getParams().size() != symbols.length) return false;
 		for(int i=0;i<symbols.length;i++){
 			ParameterVariable pv = op.getParams().get(i);
 			ISymbol s = symbols[i];
 			
			if (s instanceof ILiteralSymbol && s.isLiteral()){
 				if (pv.getKind() == ParameterKind.VAR) return false;
 				
 				TypeDef td = pv.getVar().getType().getDef();
 				
 				if (td instanceof DataTypeDef){
 					int bits = ((DataTypeDef) td).getBits();
 					if (1<<bits <= ((ILiteralSymbol) s).getValue()) return false;
 				}else{
 					return false;
 				}
 				
 			}else if (s instanceof IVariableSymbol){
 				if (pv.getKind() == ParameterKind.CONST) return false;
 				
 				if (s.getType() != pv.getVar().getType()) return false;
 				
 			}else{
 				throw new IllegalArgumentException("Unsupported operator symbol: "+s);
 			}
 		}
 		return true;
 	}
 	
 }
