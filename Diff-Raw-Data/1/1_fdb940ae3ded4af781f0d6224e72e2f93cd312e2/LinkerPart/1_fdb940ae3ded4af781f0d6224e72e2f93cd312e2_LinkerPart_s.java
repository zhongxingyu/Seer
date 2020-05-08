 /**
  * 
  */
 package hu.modembed.utils.compiler.linker;
 
 import hu.modembed.model.modembed.abstraction.behavior.BehaviorFactory;
 import hu.modembed.model.modembed.abstraction.behavior.CodeSymbolPlacement;
 import hu.modembed.model.modembed.abstraction.behavior.OperationExecution;
 import hu.modembed.model.modembed.abstraction.behavior.SequentialAction;
 import hu.modembed.model.modembed.abstraction.behavior.SequentialBehaviorPart;
 import hu.modembed.model.modembed.abstraction.behavior.SymbolAssignment;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * @author balage
  *
  */
 public class LinkerPart implements ISymbolContext {
 
 	private final ISymbolContext parent;
 	private final SequentialBehaviorPart part;
 	private final String prefix;
 	
 	private final Map<String, String> localSymbols = new HashMap<String, String>();
 	
 	/**
 	 * 
 	 */
 	public LinkerPart(ISymbolContext parent, SequentialBehaviorPart part, List<String> arguments) {
 		this.parent = parent;
 		this.part = part;
 		this.prefix = parent.basePrefix()+part.getName()+countCalls(part.getName())+"_";
 		
 		for(SymbolAssignment sa : part.getLocalSymbols()){
 			SymbolAssignment la = EcoreUtil.copy(sa);
 			la.setSymbol(prefix+sa.getSymbol());
 			localSymbols.put(sa.getSymbol(), la.getSymbol());
 		}
 		
 		for(SequentialAction sa : part.getActions()){
 			if (sa instanceof CodeSymbolPlacement){
 				String symbol = ((CodeSymbolPlacement) sa).getSymbol();
 				localSymbols.put(symbol, prefix+symbol);
 			}
 		}
 		
 		for(int i=0;i<arguments.size();i++){
 			String parameter = part.getParameters().get(i);
 			this.localSymbols.put(parameter, arguments.get(i));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.modembed.utils.compiler.linker.ISymbolContext#getSymbol(java.lang.String)
 	 */
 	@Override
 	public String getSymbol(String symbol) {
 		if (localSymbols.containsKey(symbol)){
 			return localSymbols.get(symbol);
 		}
 		return parent.getSymbol(symbol);
 	}
 
 	public List<SequentialAction> link(){
 		List<SequentialAction> source = part.getActions();
 		List<SequentialAction> result = new ArrayList<SequentialAction>(source.size());
 		
 		for(SequentialAction sa : source){
 			if (sa instanceof CodeSymbolPlacement){
 				CodeSymbolPlacement copy = BehaviorFactory.eINSTANCE.createCodeSymbolPlacement();
 				copy.setSymbol(getSymbol(((CodeSymbolPlacement) sa).getSymbol()));
 				result.add(copy);
 			}
 			if (sa instanceof OperationExecution){
 				List<String> arguments = new ArrayList<String>();
 				for(String sym : ((OperationExecution) sa).getArguments()){
 					arguments.add(getSymbol(sym));
 				}
 				
 				SequentialBehaviorPart part = getCallee(((OperationExecution) sa).getOperation()); 
 				if (part == null){
 					OperationExecution oe = BehaviorFactory.eINSTANCE.createOperationExecution();
 					oe.setOperation(((OperationExecution) sa).getOperation());
 					oe.getArguments().addAll(arguments);
 					result.add(oe);
 				}else{
 					LinkerPart subpart = new LinkerPart(this, part, arguments);
 					result.addAll(subpart.link());
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	@Override
 	public String basePrefix() {
 		return prefix;
 	}
 	
 	/* (non-Javadoc)
 	 * @see hu.modembed.utils.compiler.linker.ISymbolContext#getCallee(java.lang.String)
 	 */
 	@Override
 	public SequentialBehaviorPart getCallee(String name) {
 		return parent.getCallee(name);
 	}
 
 	@Override
 	public void registerSymbol(SymbolAssignment sa) {
 		parent.registerSymbol(sa);
 	}
 
 	@Override
 	public int countCalls(String callee) {
 		return parent.countCalls(callee);
 	}
 
 }
