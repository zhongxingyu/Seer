 /**
  * 
  */
 package edu.cmu.hcii.peer.structure;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * A class to represent a procedure.  This will hold
  * all of the necessary information for the app to 
  * display/navigate a full procedure.
  * 
  * @author Chris
  *
  */
 public class Procedure implements Serializable {
 
 	/**
 	 * Auto-Generated serial id
 	 */
 	private static final long serialVersionUID = -5333895722021665909L;
 	private String number;
 	private String title;
 	private String objective;
 	private String duration;
 	private List<ExecNote> execNotes;
 	private Map<String, List<StowageItem>> stowageItems;
 	private List<ProcedureItem> children;
 	private List<String> stepPreviews;
 	
 	/**
 	 * Create a procedure object with the given properties.  The index is then set to
 	 * 0.
 	 * 
 	 * @param title
 	 * @param objective
 	 * @param duration
 	 * @param children
 	 */
 	public Procedure(String number, String title, String objective, String duration, List<ProcedureItem> children) {
 		this.number = number;
 		this.title = title;
 		this.objective = objective;
 		this.duration = duration;
 		this.execNotes = new ArrayList<ExecNote>();
 		this.stowageItems = new HashMap<String, List<StowageItem>>();
 		this.children = children;
 		generateStepPreviews();
 	}
 	
 	/**
 	 * Create a procedure object with the given properties.  The index is then set to
 	 * 0.
 	 * 
 	 * @param number
 	 * @param title
 	 * @param objective
 	 * @param duration
 	 * @param execNotes
 	 * @param stowageItems
 	 * @param children
 	 */
 	public Procedure(String number, String title, String objective, String duration, 
 			List<ExecNote> execNotes, Map<String, List<StowageItem>> stowageItems, List<ProcedureItem> children) {
 		this.number = number;
 		this.title = title;
 		this.objective = objective;
 		this.duration = duration;
 		this.execNotes = execNotes;
 		this.stowageItems = stowageItems;
 		this.children = children;
 		generateStepPreviews();
 	}
 	
 	
 	
 	/**
 	 * @return the number
 	 */
 	public String getNumber() {
 		return number;
 	}
 	
 	
 
 	/**
 	 * @param number the number to set
 	 */
 	public void setNumber(String number) {
 		this.number = number;
 	}
 
 	
 	
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	
 	
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	
 	
 
 	/**
 	 * @return the objective
 	 */
 	public String getObjective() {
 		return objective;
 	}
 	
 	
 
 	/**
 	 * @param objective the objective to set
 	 */
 	public void setObjective(String objective) {
 		this.objective = objective;
 	}
 	
 	
 
 	/**
 	 * @return the duration
 	 */
 	public String getDuration() {
 		return duration;
 	}
 
 	
 	
 	/**
 	 * @param duration the duration to set
 	 */
 	public void setDuration(String duration) {
 		this.duration = duration;
 	}
 
 	
 	
 	/**
 	 * @return the execNotes
 	 */
 	public List<ExecNote> getExecNotes() {
 		return execNotes;
 	}
 
 	
 	
 	/**
 	 * @param execNotes the execNotes to set
 	 */
 	public void setExecNotes(List<ExecNote> execNotes) {
 		this.execNotes = execNotes;
 	}
 
 	
 	
 	/**
 	 * 
 	 * @return the number of execution notes
 	 */
 	public int getNumExecNotes() {
 		return execNotes.size();
 	}
 
 	
 	
 	/**
 	 * @return the stowageItems
 	 */
 	public Map<String, List<StowageItem>> getStowageItems() {
 		return stowageItems;
 	}
 
 	
 	
 	/**
 	 * @param stowageItems the stowageItems to set
 	 */
 	public void setStowageItems(Map<String, List<StowageItem>> stowageItems) {
 		this.stowageItems = stowageItems;
 	}
 
 	
 	
 	/**
 	 * @param index
 	 * @return the step at index
 	 */
 	public ProcedureItem getChildAt(int index) {
 		return children.get(index);
 	}
 
 	
 	
 	/**
 	 * @return the steps
 	 */
 	public List<ProcedureItem> getChildren() {
 		return children;
 	}
 
 	
 	
 	/**
 	 * @param steps the steps to set
 	 */
 	public void setChildren(List<ProcedureItem> children) {
 		this.children = children;
 	}
 
 	
 	
 	/**
 	 * @return the number of steps
 	 */
 	public int getNumChildren() {
 		return children.size();
 	}
 	
 	
 	
 	/**
 	 * @return the steps in a flat, unnested list
 	 */
 	public String getStepPreview(int index){
 		return stepPreviews.get(index);
 	}
 	
 	
 	
 	/**
 	 * @return the steps in a flat, unnested list
 	 */
 	public int getStepPreviewSize(){
 		return stepPreviews.size();
 	}
 	
 	
 	
 	/**
 	 * @return the steps in a flat list
 	 */
 	private void generateStepPreviews(){
 		stepPreviews = new ArrayList<String>();
 		
 		for (int i = 0; i < children.size(); i++){
 			traverseSteps(children.get(i));
 		}
 	}
 	
 	
 	
 	/**
 	 * traverses through all the steps to create an unnested list of steps and substeps
 	 * @param step current step
 	 */
 	private void traverseSteps(ProcedureItem child) {
 		if (child != null){
 			if (child.isCycle()) {
 				Cycle c = (Cycle)child;
 				int reps = c.getReps();
 				
 				for (int i = 0; i < reps; i++) {
 					
 					String start = ((Step)c.getChild(0)).getNumber();
 					String end = ((Step)c.getChild(c.getNumChildren()-1)).getNumber();
					stepPreviews.add("Repeat steps " + start + "-" + end);
 					
 					for (int j = 0; j < child.getNumChildren(); j++){
 						traverseSteps(child.getChild(j));
 					}
 				}
 				
 			} else {
 				if (child.getNumChildren() == 0) {
 					Step s = (Step)child;
					stepPreviews.add(s.getNumber() + ": " + s.getText());
 				}
 				
 				for (int i = 0; i < child.getNumChildren(); i++){
 					traverseSteps(child.getChild(i));
 				}
 			}
 		} else {
 			return;
 		}
 	}
 	
 }
