 import java.util.Map;
 
 public class CreateCommand extends Command { 
 	// cannot have record here. would change global stuf w/o calling execute.
 	private String patientID;
 	private String nurseID;
 	private String departMentID;
 	private String text;
 
 	public CreateCommand(String pid, String nid, String text) {
 		this.patientID = pid;
 		this.nurseID = nid;
 		this.text = text;
 	}
 
 	public String execute(Map<Integer, Record> records) {
 		boolean ok = false;
 		if (super.subject.charAt(0) == 'd') {
			int rID = Record.getNextRecordID();
			Record rec = new Record(super.departmentID, this.patientID, this.nurseID, super.subject, this.text);
			records.put(rID, rec);
 			log.info(String.format(String.format("%s@%s created record %s", super.subject, super.departmentID, rec.recordID)));
 		} else {
			log.info(String.format("Access denied for %s when creating record", super.subject));
 		}
 		return (ok) ? "Record created" : "Access denied";
 	}
 }
