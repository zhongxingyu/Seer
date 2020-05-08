 /**
  * 
  */
 package hu.e.compiler.internal.model;
 
 import hu.e.compiler.ECompiler;
 import hu.e.compiler.ECompilerException;
 import hu.e.compiler.internal.model.symbols.ILinkTimeSymbol;
 import hu.e.compiler.internal.model.symbols.ILiteralSymbol;
 import hu.e.compiler.internal.model.symbols.ISymbol;
 import hu.e.compiler.internal.model.symbols.SymbolContext;
 import hu.e.compiler.list.InstructionArgument;
 import hu.e.compiler.list.InstructionStep;
 import hu.e.compiler.list.ListFactory;
 import hu.e.parser.eSyntax.InstructionWord;
 import hu.e.parser.eSyntax.LiteralValue;
 import hu.e.parser.eSyntax.Variable;
 import hu.e.parser.eSyntax.VariableReference;
 import hu.e.parser.eSyntax.WordSection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author balazs.grill
  *
  */
 public class InstructionWordInstance{
 	
 	public static long getItemValue(long value, int shift, int start, int size){
 		long v = value;
 		v = v>>shift;
 		v = v%(1<<size);
 		v = v<<start;
 		return v;
 	}
 	
 	private final List<InstructionArgument> arguments = new ArrayList<InstructionArgument>();
 	
 	private long value;
 	private int size;
 	
 	public InstructionWordInstance(InstructionWord word, ISymbolManager sm) throws ECompilerException {
 		List<WordSection> sections = word.getSections();
 		
 		int s = 0;
 		value = 0;
 		this.size = 0;
 		for(int i=sections.size()-1;i>=0;i--){
 			WordSection ws = sections.get(i);
 			
 			int size = -1;
 			int shift = -1;
 			long v = 0;
 			
 			if (ws instanceof LiteralValue){
 				size = ((LiteralValue) ws).getSize();
 				shift = ((LiteralValue) ws).getShift();
 				v = ECompiler.convertLiteral(((LiteralValue) ws).getValue());
 				value += getItemValue(v, shift, s, size);
 			}
 			if (ws instanceof VariableReference){
 				size = ((VariableReference) ws).getSize();
 				shift = ((VariableReference) ws).getShift();
 				Variable var = ((VariableReference)ws).getVar();
 
 
 				ISymbol vs = sm.getSymbol(var);
 				if (vs == null)
 					throw new ECompilerException(ws, "Cannot resolve symbol: "+((VariableReference)ws).getVar());
 				if (!vs.isAssignableAt(SymbolContext.LINKTIME))
 					throw new ECompilerException(ws, "Instruction word can only contain compile-time variables: "+vs);
 
 				if (vs instanceof ILinkTimeSymbol){
 					InstructionArgument arg = ListFactory.eINSTANCE.createInstructionArgument();
 					arg.setShift(shift);
 					arg.setSize(size);
 					arg.setStart(s);
 					arg.setOffset(((ILinkTimeSymbol) vs).getOffset());
 					arg.setValue(((ILinkTimeSymbol) vs).getLinkTimeValue());
 					arguments.add(arg);
 				} else{	
 					v = TypeDefinitionResolver.getRawValue(vs.getType(), ((ILiteralSymbol)vs).getValue());
 					value += getItemValue(v, shift, s, size);
 				}
 
 			}
 			
 			s += size;
 		}
 		
 		this.size = s;
 	}
 	
 	public int getWidth(){
 		return size;
 	}
 	
 	public long getValue(){
 		return value;
 	}
 	
 	public InstructionStep create() throws ECompilerException{
 		InstructionStep is = ListFactory.eINSTANCE.createInstructionStep();
 		is.setCode(getValue());
 		is.getArgs().addAll(arguments);
		is.setWidth(getWidth());
 		return is;
 	}
 	
 }
