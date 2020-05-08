 
 package scalr.variable;
 
 import java.util.ArrayList;
 
 import scalr.expression.Expression;
 import scalr.expression.ExpressionType;
 
 public class Sequence implements Variable
 {
 	ArrayList<Note>	theNotes;
 	
 	public Sequence()
 	{
 		theNotes = new ArrayList<Note>();
 	}
 	
 	public Sequence(Note... notes)
 	{
 		theNotes = new ArrayList<Note>(notes.length + 10);
 		for (Note n : notes)
 			theNotes.add(n);
 	}
 	
 	public Note getNote(String index)
 	{
 		return theNotes.get(Integer.parseInt(index));
 	}
 	
 	public void extend(int ext)
 	{
 		ArrayList<Note> extendedNotes = new ArrayList<Note>(theNotes.size() * ext + 10);
		for (int i = 0; i < ext; i++) {
			
		}
 	}
 	
 	public void addSequence(Sequence seq)
 	{
		theNotes.addAll(seq.theNotes);
 	}
 	
 	public void addNoteToEnd(Note e)
 	{
		theNotes.add(e);
 	}
 	
 	public void addNoteToStart(Note e)
 	{
 		if (theNotes.size() > 0)
			theNotes.add(0, e);
 		else
			theNotes.add(e);
 	}
 	
 	public void deleteLeftmost()
 	{
 		theNotes.remove(0);
 	}
 	
 	public void deleteRightmost()
 	{
 		int t = theNotes.size() - 1;
 		theNotes.remove(t);
 	}
 	
 	@Override
 	public Variable getCopy()
 	{
 		ArrayList<Note> copy = new ArrayList<Note>(theNotes.size());
 		for (Note n : theNotes)
 			copy.add(n.getCopy());
 		Sequence seq = new Sequence();
 		seq.theNotes = copy;
 		return seq;
 	}
 	
 	@Override
 	public Expression getValue(Expression... expressions)
 	{
 		return this;
 	}
 	
 	@Override
 	public ExpressionType getType()
 	{
 		return ExpressionType.SEQUENCE;
 	}
 	
 	@Override
 	public String toString()
 	{
 		// A track opens with a bracket
 		String result = "[";
 		// Append each note to the string
 		for (Note n : theNotes)
 			result += n.toString() + "|";
 		// remove the last bar (if there is a note)
 		if (theNotes.size() > 0)
 			result = result.substring(0, result.length() - 1);
 		// And it closes with a bracket
 		return result + "]";
 	}
 }
