 package zinara.symtable;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 
 import zinara.ast.Declaration;
 import zinara.ast.SingleDeclaration;
 import zinara.ast.MultipleDeclaration;
 import zinara.ast.expression.Expression;
 import zinara.ast.instructions.Assignation;
 import zinara.ast.instructions.SingleAssignation;
 import zinara.ast.instructions.MultipleAssignation;
 import zinara.exceptions.IdentifierAlreadyDeclaredException;
 import zinara.exceptions.IdentifierNotDeclaredException;
 import zinara.exceptions.InvalidAssignationException;
 import zinara.exceptions.TypeClashException;
 
 public class SymTable{
     private HashMap table;
     private SymTable father;
     private ArrayList sons;    // ArrayList of symtables
     public String name = "";
     
     public SymTable() {
 	this.sons = new ArrayList();
 	this.father = null;
 	this.table = new HashMap();
     }
 
     public SymTable(SymTable f) {
 	this.sons = new ArrayList();
 	this.father = f;
 	this.table = new HashMap();
     }
 
     public SymTable(SymTable f, String name) {
 	this.sons = new ArrayList();
 	this.father = f;
 	this.table = new HashMap();
 	this.name = name;
     }
 
     public boolean checkDoubleDeclaration(String id) throws IdentifierAlreadyDeclaredException {
 	if (containsId(id))
 	    throw new IdentifierAlreadyDeclaredException(id);
 	else return false;
     }
 
     public void addDeclaration(Declaration decl) throws IdentifierAlreadyDeclaredException {
 	SingleDeclaration current_decl;
 	if (decl.isSingle()) {
 	    current_decl = (SingleDeclaration)decl;
 	    if (!containsId(current_decl.getId())) {
 		addSymbol(current_decl.getId(),
 			  new SymValue(current_decl.getType(), current_decl.isVariable()));
 	    } else
 		throw new IdentifierAlreadyDeclaredException(((SingleDeclaration)decl).getId());
 	} else {
 	    for (int i = 0; i < ((MultipleDeclaration)decl).size(); i++) {
 		current_decl = ((MultipleDeclaration)decl).get(i);
 		if (!containsId(current_decl.getId())) {
 		    addSymbol(current_decl.getId(),
 			      new SymValue(current_decl.getType(), current_decl.isVariable()));
 		} else
 		    throw new IdentifierAlreadyDeclaredException(current_decl.getId());
 	    }
 	}
     }
 
     public void addSymbol (String id, SymValue v){
 	this.table.put(id,v);
     }
 
     public void deleteSymbol (String id){
 	this.table.remove(id);
     }
 
     public SymValue getSymbol (String id){
 	return (SymValue)this.table.get(id);
     }
 
    public SymValue getSymbolRecursively(String id) {
	if (containsId(id)) return this.getSymbol(id);
	else if (father != null) return father.getSymbol(id);
	else return null;
    }

     public SymValue getSymbolOrDie(String id) throws IdentifierNotDeclaredException {
 	if (table.containsKey(id)) return (SymValue)table.get(id);
 	else if (father != null) return father.getSymbolOrDie(id);
 	else throw new IdentifierNotDeclaredException(id);
     }
 
     public SymTable getFather (){
 	return this.father;
     }
 
     public boolean containsId (String id){
 	return this.table.containsKey(id);
     }
 
     public SymTable containsIdOrDie(String id) throws IdentifierNotDeclaredException {
 	if (containsId(id)) return this;
 	else if (father != null) return father.containsIdOrDie(id);
 	else throw new IdentifierNotDeclaredException(id);
     }
 
     public boolean containsValue (SymValue v){
 	return this.table.containsValue(v);
     }
 
     public boolean isEmpty (){
 	return this.table.isEmpty();
     }
 
     public String toString() {
 	String ret = "";
 	for (int i = 0; i < sons.size(); i++)
 	    ret += (SymTable)sons.get(i) + ", ";
 	if (ret.length() != 0) ret = ret.substring(0, ret.length()-2);
 	return "<" + table.toString() + "[" + ret + "]>";
     }
 
     /*
       Checks two things, if the id of the assignation is declared and
       if the types match.
      */
     public SingleAssignation checkAssignation(String id, Expression expr) 
 	throws IdentifierNotDeclaredException, TypeClashException {
 	SymValue idSymValue = getSymbolOrDie(id);
 
 	if (idSymValue.getType().equals(expr.getType()))
 	    return new SingleAssignation(id, expr);
 	else
 	    throw new TypeClashException("Conflicto de tipos entre el identificador " + id + idSymValue.getType() +" y la expresion " + expr + expr.getType());
     }
 
     /*
       Checks if the list of ids and expressions match in number, then
       checks the validity of every assignation issuing
       `checkAssignation`.
      */
     public Assignation checkMultipleAssignations(ArrayList ids, ArrayList exprs)
 	throws IdentifierNotDeclaredException, TypeClashException, InvalidAssignationException {
 	if (ids.size() != exprs.size())
 	    throw new InvalidAssignationException(); // FIX THIS: same as in MultipleAssignation.java
 	if (ids.size() == 1)
 	    return checkAssignation((String)ids.get(0), (Expression)exprs.get(0));
 	else {
 	    ArrayList assignations = new ArrayList();
 	    for (int i = 0; i < ids.size(); i++)
 		assignations.add(checkAssignation((String)ids.get(i), (Expression)exprs.get(i)));
 	    return new MultipleAssignation(assignations);
 	}
     }
 
     public SymTable newTable() {
 	SymTable son = new SymTable(this);
 	sons.add(son);
 	return son;
     }
 
     public void addSon(SymTable son){
 	this.sons.add(son);
     }
 }
