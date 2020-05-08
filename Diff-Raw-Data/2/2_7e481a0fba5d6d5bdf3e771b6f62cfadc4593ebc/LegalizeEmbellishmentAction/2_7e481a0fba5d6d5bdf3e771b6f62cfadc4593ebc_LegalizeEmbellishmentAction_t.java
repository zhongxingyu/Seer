 package pipes.editing.actions;
 
 import pipes.model.Note;
 import pipes.model.embellishment.EmbellishmentFamily;
 
 public class LegalizeEmbellishmentAction implements EditAction {
 
 	public void execute() {
		if (oldFamily != null && !oldFamily.canEmbellish(target.getTune().getNoteBefore(target), target))
 			target.setEmbellishmentFamily(null);
 		else
 			target.setEmbellishmentFamily(oldFamily);
 	}
 
 	public void undo() {
 		target.setEmbellishmentFamily(oldFamily);
 	}
 
 	public LegalizeEmbellishmentAction(Note n) {
 		target = n;
 		oldFamily = n.getEmbellishmentFamily();
 	}
 	
 	private Note target;
 	private EmbellishmentFamily oldFamily;
 }
