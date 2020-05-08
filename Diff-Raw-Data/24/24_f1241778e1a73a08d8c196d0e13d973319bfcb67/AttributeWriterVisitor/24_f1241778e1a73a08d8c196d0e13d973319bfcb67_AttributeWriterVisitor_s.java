 package serialization;
 
 
 import org.dom4j.Document;
 import org.dom4j.Element;
 
 import asttweaks.ParList;
 import edu.tum.lua.ast.Binop;
 import edu.tum.lua.ast.BooleanExp;
 import edu.tum.lua.ast.Dots;
 import edu.tum.lua.ast.FieldNameExp;
 import edu.tum.lua.ast.FuncName;
 import edu.tum.lua.ast.FuncNameVar;
 import edu.tum.lua.ast.FunctionExp;
 import edu.tum.lua.ast.LocalFuncDef;
 import edu.tum.lua.ast.Name;
 import edu.tum.lua.ast.NumberExp;
 import edu.tum.lua.ast.Op;
 import edu.tum.lua.ast.TextExp;
 import edu.tum.lua.ast.Unop;
 import edu.tum.lua.ast.Variable;
 import edu.tum.lua.ast.VisitorAdaptor;
 import edu.tum.lua.ast.VisitorNode;
 
 public class AttributeWriterVisitor extends VisitorAdaptor {
 
 	private Element currElement;
 	
 	private Document document;
 	
 	private void addAttr(String name, String value) {
 		currElement.addAttribute(name, value);
 	}
 	
 	public AttributeWriterVisitor(Element curr, Document doc) {
 		currElement = curr;
 		document = doc;
 	}
 	
 	private String oplookup(int op) {
 		switch(op) {
 			case Op.ADD: return "ADD";
 			case Op.AND: return "AND";
 			case Op.CONCAT: return "CONCAT";
 			case Op.DIV: return "DIV";
 			case Op.EQ: return "EQ";
 			case Op.GE: return "GE";
 			case Op.GT: return "GT";
 			case Op.LE: return "LE";
 			case Op.LEN: return "LEN";
 			case Op.LT: return "LT";
 			case Op.MOD: return "MOD";
 			case Op.MUL: return "MUL";
 			case Op.NEQ: return "NEQ";
 			case Op.NOT: return "NOT";
 			case Op.OR: return "OR";
 			case Op.POW: return "POW";
 			case Op.SUB: return "SUB";
 			case Op.UNM: return "UNM";
 			default: return "UNKNOWN";
 		}
 	}
 	
 	@Override
 	public void visit(Binop binop) {
 		addAttr("op", oplookup(binop.op));
 	}
 	
 	@Override
 	public void visit(Unop unop) {
 		addAttr("op", oplookup(unop.op));
 	}
 	
 	public void visit(NumberExp numberExp) {
 		addAttr("number", String.valueOf(numberExp.number));
 	}
 	
 	public void visit(Variable var) {
 		addAttr("var", var.var);
 	}
 	
 	@Override
 	public void visit(Dots dots) {
 	}
 	
 	@Override
 	public void visit(LocalFuncDef localFuncDef) {
 		addAttr("name", localFuncDef.name);
 		addAttr("varargs", String.valueOf(localFuncDef.varargs));
 	}
 	
 	@Override
 	public void visit(FieldNameExp fieldNameExp) {
 		addAttr("ident", fieldNameExp.ident);
 	}
 	
 	@Override
 	public void visit(FunctionExp functionExp) {
 		addAttr("varargs", String.valueOf(functionExp.varargs));
 
 	}
 	
 	
 	@Override
 	public void visit(Name name) {
 		addAttr("name", name.name);
 	}
 	
 	@Override
 	public void visit(TextExp textExp) {
 		addAttr("text", textExp.text);
 	}
 	
 	@Override
 	public void visit(BooleanExp booleanExp) {
 		addAttr("value", String.valueOf(booleanExp.value));
 	}
 
 	@Override
 	public void visit() {
 		// TODO Auto-generated method stub
 		super.visit();
 	}
 }
