 package witchdoctor.detect;
 
 import com.google.common.collect.Iterables;
 
 import witchdoctor.detect.changes.ChangeDecorator;
 import witchdoctor.detect.changes.IChange;
 
 
 public class CodeDocumentChange extends ChangeDecorator {
 	
 	public static CodeDocumentChange create(IChange change, 
 			CodeDocument original, CodeDocument revised) {
 		CodeDocument container = change.isDeletion() ? original : revised;
 		CodeDocument other = change.isDeletion() ? revised : original;
 		int charPosition = 0;
 		for (int i = 0; i < change.getPosition(); i++) {
 			charPosition += Iterables.get(container.getLines(), i).length();
 		}
 		return new CodeDocumentChange(change, container, other, charPosition);
 	}
 	
 	private final CodeDocument container;
 	private final CodeDocument other;
 	private final int charPosition;
 
	private CodeDocumentChange(IChange decorated, 
 			CodeDocument container, CodeDocument other, int charPosition) {
 		super(decorated);
 		this.container = container;
 		this.other = other;
 		this.charPosition = charPosition;
 	}
 	
 	public CodeDocument getContainerDocument() {
 		return container;
 	}
 	
 	public CodeDocument getOtherDocument() {
 		return other;
 	}
 	
 	@Override
 	public int getPosition() {
 		return charPosition;
 	}
 
 }
