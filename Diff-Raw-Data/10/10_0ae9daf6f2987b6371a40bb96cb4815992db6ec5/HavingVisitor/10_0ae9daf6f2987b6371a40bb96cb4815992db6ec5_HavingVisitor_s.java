 package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime;
 
 import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
 import com.hp.hpl.jena.sparql.expr.ExprAggregator;
 import com.hp.hpl.jena.sparql.expr.ExprFunction2;
 import com.hp.hpl.jena.sparql.expr.NodeValue;
 
 import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueFloat;
 import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueVisitor;
 // Simple implementation only one avg.
 public class HavingVisitor extends GenericVisitor{
 	private StringBuffer code;
 	private VarNameManager vm;
 	
 	public HavingVisitor(){
 		code = new StringBuffer();
 		vm = VarNameManager.getVarNameManager();
 	}
 	
 	public StringBuffer getCode() {
 		return code;
 	}
 
 
 	public void setCode(StringBuffer code) {
 		this.code = code;
 	}
 
 
 	@Override
 	public void visit(ExprFunction2 arg0) {
 		arg0.getArg1().visit(this);
		System.out.println("1");
 		vm.getNextResultVar2();
 		arg0.getArg2().visit(this);
		System.out.println("3");
 
 		if (arg0 instanceof E_GreaterThanOrEqual) {
 			code.append(", greaterOrEqual(" + vm.getResultVar1() + ", " + vm.getResultVar2() + ")");
 		}
 	}
 
 	@Override
 	public void visit(ExprAggregator arg0) {
 		//For not nested expressions. E.g. AVG(?value)
 		code.append("calcAverage(" + vm.getAggrDbId() + ", " + vm.getWindowTime() + ", " + vm.getResultVar1() + ")");
 	}
 	
 	@Override
 	public void visit(NodeValue arg0) {
 		vm.setResultVar2("'" + arg0.toString() + "'");
 	}
 	
 }
