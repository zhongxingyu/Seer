 package usp.ime.line.ivprog.model.domainaction;
 
 import usp.ime.line.ivprog.Services;
 import usp.ime.line.ivprog.model.IVPProgram;
 import usp.ime.line.ivprog.model.components.datafactory.dataobjetcs.Variable;
 import ilm.framework.assignment.model.DomainAction;
 import ilm.framework.domain.DomainModel;
 
 public class NewVariable extends DomainAction{
 	
 	private IVPProgram model;
 	private String scopeID;
 	private String varID;
 
 	public NewVariable(String name, String description) {
 		super(name, description);
 	}
 
 	public void setDomainModel(DomainModel m) {
 		model = (IVPProgram)m;
 	}
 
 	protected void executeAction() {
		if(varID == null)
 			varID = model.createVariable(scopeID);
 		else
			model.restoreVariable(scopeID, varID);
 	}
 
 	protected void undoAction() {
 		System.out.println("undoAction: "+varID);
 		model.removeVariable(scopeID, varID);
 	}
 
 	public boolean equals(DomainAction a) {
 		return false;
 	}
 	
 	public String getScopeID() {
 		return scopeID;
 	}
 
 	public void setScopeID(String scopeID) {
 		this.scopeID = scopeID;
 	}
 
 	public String getVarID() {
 		return varID;
 	}
 
 	public void setVarID(String varID) {
 		this.varID = varID;
 	}
 
 
 }
