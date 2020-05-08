 package music;
 
 import java.util.LinkedList;
 
 /* Voice represents a single musical voice within a staff.  It is comprised of a linked list of
  * MultiNotes.
  */
 public class Voice {
 	LinkedList<MultiNote> _notes;
 	
	public Voice(){
		_notes = new LinkedList<MultiNote>();
 	}
 	
 	public LinkedList<MultiNote> getMultiNotes() {
 		return _notes;
 	}
 }
