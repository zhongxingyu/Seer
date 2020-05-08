 /**
  * 
  */
 package org.eclipse.dltk.javascript.internal.ui.text;
 
 import java.io.CharArrayReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.ui.editor.semantic.highlighting.Highlighting;
 import org.eclipse.dltk.internal.ui.editor.semantic.highlighting.PositionUpdater;
 import org.eclipse.dltk.internal.ui.editor.semantic.highlighting.SemanticHighlightingPresenter;
 
 import com.xored.org.mozilla.javascript.CompilerEnvirons;
 import com.xored.org.mozilla.javascript.ErrorReporter;
 import com.xored.org.mozilla.javascript.EvaluatorException;
 import com.xored.org.mozilla.javascript.IXMLCallback;
 import com.xored.org.mozilla.javascript.Parser;
 
 final class JavaScriptPositionUpdater extends PositionUpdater {
 	HashSet currentPositions = new HashSet();
 
 	List calculateNewPositions(ISourceModule ast,
 			final SemanticHighlightingPresenter presenter,
 			final Highlighting[] highlightings) {
 		final ArrayList result = new ArrayList();
 		try {
 			char[] sourceAsCharArray = ast.getSourceAsCharArray();
 
 			Parser p = new Parser(new CompilerEnvirons(), new ErrorReporter() {
 
 				public void error(String message, String sourceName, int line,
 						String lineSource, int offset) {
 					// TODO Auto-generated method stub
 
 				}
 
 				public EvaluatorException runtimeError(String message,
 						String sourceName, int line, String lineSource,
 						int lineOffset) {
 					// TODO Auto-generated method stub
 					return null;
 				}
 
 				public void warning(String message, String sourceName,
 						int line, String lineSource, int lineOffset) {
 					// TODO Auto-generated method stub
 
 				}
 
 			});
 
 			try {
 				p.setXMLCallback(new IXMLCallback() {
 
 					public void xmlTokenStart(int offset, String tagName,
 							int cursor) {
 						int i = cursor - offset + 1;
						if (tagName.length() != i) {
 							StringBuffer copy = new StringBuffer();
 							for (int a = 0; a < tagName.length(); a++) {
 								char c = tagName.charAt(a);
 								if (c == '\n')
 									copy.append("  ");
 								else
 									copy.append(c);
 							}
 							tagName = copy.toString();
 						}
 
 						XMLTokenizer r = new XMLTokenizer(new StringReader(
 								tagName));
 						List l = r.getRegions();
 						for (int a = 0; a < l.size(); a++) {
 							Token object = (Token) l.get(a);
 							if (object.context == XMLTokenizer.XML_TAG_NAME) {
 								result.add(presenter.createHighlightedPosition(
 										offset - 1 + object.start,
 										object.textLength, highlightings[0]));
 
 							} else if (object.context == XMLTokenizer.XML_TAG_ATTRIBUTE_NAME) {
 								result.add(presenter.createHighlightedPosition(
 										offset - 1 + object.start,
 										object.textLength, highlightings[1]));
 							} else if (object.context == XMLTokenizer.XML_COMMENT_OPEN
 									|| object.context == XMLTokenizer.XML_COMMENT_TEXT
 									|| object.context == XMLTokenizer.XML_COMMENT_CLOSE) {
 								result.add(presenter.createHighlightedPosition(
 										offset - 1 + object.start,
 										object.textLength, highlightings[2]));
 							} else if (object.context == XMLTokenizer.XML_TAG_ATTRIBUTE_VALUE) {
 
 							} else {
 // result.add(presenter
 // .createHighlightedPosition(
 // offset - 1
 // + object.start,
 // object.textLength,
 // highlightings[3]));
 							}
 						}
 // result.add(presenter.createHighlightedPosition(
 // offset - 1, i,
 // highlightings[0]));
 					}
 
 				});
 				p.parse(new CharArrayReader(sourceAsCharArray),
 						this.toString(), 0);
 			} catch (IOException e) {
 
 			}
 // result.add(presenter.createHighlightedPosition(offset , length,
 // highlightings[0]));
 
 		} catch (ModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	public UpdateResult reconcile(ISourceModule ast,
 			SemanticHighlightingPresenter presenter,
 			Highlighting[] highlightings, List currentPositions) {
 		this.currentPositions = new HashSet(currentPositions);
 		// TODO Auto-generated method stub
 		List calculateNewPositions = calculateNewPositions(ast, presenter,
 				highlightings);
 		Iterator it = calculateNewPositions.iterator();
 		ArrayList addedPositions = new ArrayList();
 
 		HashSet removed = new HashSet(currentPositions);
 		while (it.hasNext()) {
 			Object o = it.next();
 			if (currentPositions.contains(o)) {
 				removed.remove(o);
 			} else {
 				addedPositions.add(o);
 			}
 		}
 		ArrayList removedPositions = new ArrayList(removed);
 		this.currentPositions = new HashSet(calculateNewPositions);
 		return new UpdateResult(addedPositions, removedPositions);
 	}
 }
