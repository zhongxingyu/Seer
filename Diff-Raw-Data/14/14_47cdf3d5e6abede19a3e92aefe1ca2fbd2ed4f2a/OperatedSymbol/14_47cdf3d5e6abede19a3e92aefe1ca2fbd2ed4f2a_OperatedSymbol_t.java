 /**
  * 
  */
 package hu.e.compiler.internal.model.symbols.impl;
 
 import hu.e.compiler.ECompilerException;
 import hu.e.compiler.internal.model.ISymbolManager;
 import hu.e.compiler.internal.model.symbols.ILiteralSymbol;
 import hu.e.compiler.internal.model.symbols.ISymbol;
 import hu.e.compiler.internal.model.symbols.IVariableSymbol;
 import hu.e.compiler.internal.model.symbols.SymbolContext;
 import hu.e.compiler.list.ProgramStep;
 import hu.e.compiler.list.SequenceStep;
 import hu.e.parser.eSyntax.StructTypeDefMember;
 import hu.e.parser.eSyntax.TypeDef;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 /**
  * @author balazs.grill
  *
  */
 public class OperatedSymbol implements ILiteralSymbol, IVariableSymbol {
 
 	private final List<ProgramStep> steps;
 	private final ISymbol symbol;
 	
 	public OperatedSymbol(List<ProgramStep> steps, ISymbol symbol) {
 		this.steps = steps;
 		this.symbol = symbol;
 	}
 	
 	public ISymbol getSymbol() {
 		return symbol;
 	}
 	
 	/* (non-Javadoc)
 	 * @see hu.e.compiler.internal.model.ISymbol#getValue()
 	 */
 	@Override
 	public BigDecimal getValue() throws ECompilerException {
 		return ((ILiteralSymbol)symbol).getValue();
 	}
 
 	@Override
 	public boolean isAssignableAt(SymbolContext context) {
 		return symbol.isAssignableAt(context);
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.e.compiler.internal.model.ISymbol#getSteps()
 	 */
 	@Override
	public void addSteps(SequenceStep sequence) throws ECompilerException {
 		sequence.getSteps().addAll(steps);
		symbol.addSteps(sequence);
 	}
 
 	@Override
 	public TypeDef getType() throws ECompilerException {
 		return symbol.getType();
 	}
 
 	@Override
 	public ISymbol getAddressSymbol() throws ECompilerException {
 		return ((IVariableSymbol)symbol).getAddressSymbol();
 	}
 
 	@Override
 	public String toString() {
		return "{"+steps.size()+"}"+symbol.toString();
 	}
 
 	@Override
 	public ISymbol getElement(ISymbolManager sm, int index)
 			throws ECompilerException {
 		return symbol.getElement(sm, index);
 	}
 
 	@Override
 	public ISymbol getMember(ISymbolManager sm, StructTypeDefMember member)
 			throws ECompilerException {
 		return symbol.getMember(sm, member);
 	}
 	
 }
