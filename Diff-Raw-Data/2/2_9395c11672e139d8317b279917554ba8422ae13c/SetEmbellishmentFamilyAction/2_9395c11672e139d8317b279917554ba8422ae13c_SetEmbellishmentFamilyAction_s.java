 package pipes.editing.actions;
 
 import pipes.model.Note;
 import pipes.model.embellishment.EmbellishmentFamily;
 
 public class SetEmbellishmentFamilyAction implements EditAction {
 	
 	public boolean isLegal() {
		return newEmbellishment.canEmbellish(note, note.getTune().getNoteAfter(note));
 	}
 	
 	public void execute() {
 		note.setEmbellishmentFamily(newEmbellishment);
 	}
 
 	public void undo() {
 		note.setEmbellishmentFamily(oldEmbellishment);
 	}
 	
 	public SetEmbellishmentFamilyAction(Note note, EmbellishmentFamily newEmbellishment) {
 		this.note = note;
 		this.newEmbellishment = newEmbellishment;
 		oldEmbellishment = note.getEmbellishmentFamily();
 	}
 	
 	private Note note;
 	private EmbellishmentFamily oldEmbellishment;
 	private EmbellishmentFamily newEmbellishment;
 }
