 package posl.editorkit;
 
 import java.awt.Color;
 
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.text.Highlighter;
 
 import posl.editorkit.util.BoxHighlightPainter;
 import posl.engine.api.Token;
 
 public class PairCaretListener implements CaretListener {
 
 	private Highlighter highlighter;
 	private DocumentImpl doc;
 	private BoxHighlightPainter painter = new BoxHighlightPainter(Color.gray);
 
 	public PairCaretListener(Highlighter highlighter, DocumentImpl document) {
 		super();
 		this.highlighter = highlighter;
 		this.doc = document;
 	}
 
 	private Object pairHighlightTag = null;
 
 	@Override
 	public void caretUpdate(CaretEvent event) {
 		// we need to store and remove the specific highlight that
 		// we created. If we remove all the highlights, that affects
 		// or ability to manually highlight code
 		if (pairHighlightTag != null) {
 			highlighter.removeHighlight(pairHighlightTag);
 			pairHighlightTag = null;
 		}
 		Token[] pairs;
 		try {
 			pairs = doc.getTokenAt(event.getDot());
 			if (pairs.length > 0) {
				Token pair = pairs[0];
 				DocAttributes attr = (DocAttributes) pair.getMeta();
 				if (attr.isPair()) {
 					pair = attr.getPairedToken();
 					pairHighlightTag = highlighter
 							.addHighlight(pair.getStartOffset(),
 									pair.getEndOffset(), painter);
 				}
 			}
 		} catch (Exception e) {
 			// should NOT happen
 			e.printStackTrace();
 		}
 
 	}
 
 }
