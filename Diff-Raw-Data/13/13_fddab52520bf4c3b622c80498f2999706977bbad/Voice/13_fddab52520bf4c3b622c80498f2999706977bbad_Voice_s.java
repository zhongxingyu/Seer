 package music;
 
 import java.util.LinkedList;
 
 /* Voice represents a single musical voice within a staff.  It is comprised of a linked list of
  * MultiNotes.
  */
 public class Voice {
 	LinkedList<MultiNote> _notes;
 	
	public Voice(LinkedList<MultiNote> notes){
		_notes = notes;
 	}
 	
 	public LinkedList<MultiNote> getMultiNotes() {
 		return _notes;
 	}
 }
