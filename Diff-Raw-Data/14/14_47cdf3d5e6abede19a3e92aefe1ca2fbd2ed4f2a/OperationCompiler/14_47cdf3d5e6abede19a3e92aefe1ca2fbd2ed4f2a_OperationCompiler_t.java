 /**
  * 
  */
 package hu.e.compiler.internal;
 
 import hu.e.compiler.ECompilerException;
 import hu.e.compiler.internal.linking.CodePlatform;
 import hu.e.compiler.internal.linking.OperationFinder;
 import hu.e.compiler.internal.model.CompilationErrorEntry;
 import hu.e.compiler.internal.model.ISymbolManager;
 import hu.e.compiler.internal.model.IVariableManager;
 import hu.e.compiler.internal.model.symbols.ISymbol;
 import hu.e.compiler.internal.model.symbols.impl.NullSymbol;
 import hu.e.compiler.internal.symbols.AbstractSymbolManager;
 import hu.e.compiler.list.ListFactory;
 import hu.e.compiler.list.MemoryAssignment;
 import hu.e.compiler.list.ProgramStep;
 import hu.e.compiler.list.SequenceStep;
 import hu.e.parser.eSyntax.CompilationUnit;
 import hu.e.parser.eSyntax.Operation;
 import hu.e.parser.eSyntax.OperationBlock;
 import hu.e.parser.eSyntax.Variable;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author balazs.grill
  *
  */
 public class OperationCompiler {
 
 	private final class WrappedSymbolManager extends AbstractSymbolManager {
 		private final ISymbolManager sm;
 
 		private WrappedSymbolManager(ISymbolManager sm) {
 			super(platform);
 			this.sm = sm;
 		}
 
 		@Override
 		public IVariableManager getVariableManager() {
 			return sm.getVariableManager();
 		}
 
 		@Override
 		public ISymbol getSymbol(Variable ref) throws ECompilerException {
 			if (parameters.containsKey(ref)){
 				return parameters.get(ref);
 			}
 			return sm.getSymbol(ref);
 		}
 
 		@Override
 		public OperationFinder getOpFinder() {
 			return platform.getOperationFinder();
 		}
 	}
 
 	private final Operation operation;
 	
 	private final CodePlatform platform;
 	
 	private final Map<Variable, ISymbol> parameters = new HashMap<Variable, ISymbol>();
 	
 	public void addParameter(Variable v, ISymbol symbol){
 		parameters.put(v, symbol);
 	}
 	
 	public OperationCompiler(CodePlatform platform, Operation op) {
 		this.operation = op;
 		this.platform = platform;
 	}
 	
 	public MemoryAssignment createResultBuffer(SequenceStep step){
 		if(operation.getReturnvar() != null){
 			//needs a result buffer
 			MemoryAssignment ma = ListFactory.eINSTANCE.createMemoryAssignment();
 			ma.setName("result");
 			step.getVariables().add(ma);
 			return ma;
 		}
 		return null;
 	}
 	
 	public ProgramStep compile(final ISymbolManager sm, MemoryAssignment resultbuffer){
 		SequenceStep result = ListFactory.eINSTANCE.createSequenceStep();
 		result.setName(((CompilationUnit)operation.eContainer()).getName() +"."+ operation.getName());
 		List<ProgramStep> steps = result.getSteps();
 		
 		for(Variable v : parameters.keySet()){
 			ISymbol symbol = parameters.get(v);
 			steps.add(CompilationErrorEntry.info(v, v.getName()+" = "+symbol.toString()));
 		}
 		
 		if (resultbuffer != null){
 			try {
 				sm.getVariableManager().define(sm, operation.getReturnvar(),resultbuffer);
 			} catch (ECompilerException e) {
 				steps.add(CompilationErrorEntry.create(e));
 			}
 		}
 
 		OperationBlock block = operation.getBlock();
 		if (block == null){
 			steps.add(CompilationErrorEntry.error(block,  "Null block!"));
 		}
 		BlockCompiler bc = new BlockCompiler(block);
 		steps.add(bc.compile(new WrappedSymbolManager(sm)));
		
 		return result;
 	}
 	
 	public ISymbol getReturns(SequenceStep steps, ISymbolManager sm) throws ECompilerException{
 		if (operation.getReturn() != null){
 			return new WrappedSymbolManager(sm).resolve(steps, operation.getReturn());
 		}
 		if (operation.getReturnvar() != null){
 			return sm.getSymbol(operation.getReturnvar());
 		}
 		return new NullSymbol();
 	}
 	
 }
