 package objects;
 
 import java.util.Vector;
 import util.annotations.*;
 
 @StructurePattern("Bean Pattern")
 public class EditableEvent extends Event{ 
 		EditableReport report;
 	public EditableEvent(){
 		type = "event";
 		id = "";
 		execs = new Vector<Exec>();
 		this.report = new EditableReport();
 		on = new EditableOn();
 	}
	@Row(4) @Column(0)
	public Report getReport(){
		return this.report;
	}
 	
 	@Row(0) @Column(1)
 	public void setId(String id) {
 		this.id = id;
 	}
 	
 	@Row(2) @Column(0) @Explanation("")
 	public Vector<Exec> getExecs() {
 		return execs;
 	}
 	
 	@Row(3) @Column(0)
 	public void addExec(){
 		Exec ex = new Exec();
 		//Check to make sure scene does not already contain the item
 		if(!execs.contains(ex)){
 			execs.add(ex);
 		}
 		else
 			System.out.println(this.id+" already contains "+ex);
 	}
 	
 	@Row(3) @Column(1) @Label("Remove Exec")
 	public void removeExec(int i){
 		if((i>execs.size())||(i<=0)){
 			System.out.println("Please enter a number within the range.");
 			return;
 		}
 		i=i-1;
 		execs.remove(execs.get(i));
 	}
 	
 	@Visible(false)
 	public void editOn(EventType e, String a1, String a2){
 		on.setType(e);
 		on.setArg1(a1);
 		on.setArg2(a2);
 	}
 	
 	@Visible(false)
 	public void editReport(String title, String description, String backdrop, String narration, String sound, String ambience){
 		report.setTitle(title);
 		report.setDescription(description);
 		report.setBackdrop(backdrop);
 		report.setNarration(narration);
 		report.setSound(sound);
 		report.setAmbience(ambience);
 	}
 	
 	@Visible(false)
 	public void copyExecs(Vector<Exec> exs){
 		execs = (Vector<Exec>) util.misc.Common.deepCopy(exs);
 	}
 }
