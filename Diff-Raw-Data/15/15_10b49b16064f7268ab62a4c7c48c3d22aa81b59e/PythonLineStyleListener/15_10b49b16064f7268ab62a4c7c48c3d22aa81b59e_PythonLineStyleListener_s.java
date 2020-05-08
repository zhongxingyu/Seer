 /**
  * 
  */
 package net.sf.pyconsole.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.LineStyleEvent;
 import org.eclipse.swt.custom.LineStyleListener;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * @author Nirav Thaker
  * 
  */
 public class PythonLineStyleListener implements LineStyleListener {
 
 	private final PythonConsolePage pythonConsolePage;
 
 	public PythonLineStyleListener(PythonConsolePage pythonConsolePage) {
 		this.pythonConsolePage = pythonConsolePage;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.swt.custom.LineStyleListener#lineGetStyle(org.eclipse.swt
 	 * .custom.LineStyleEvent)
 	 */
 	@Override
 	public void lineGetStyle(LineStyleEvent event) {
 		if (event.lineText.startsWith(">>>")) {
 			List<String> keywords = net.sf.pyconsole.Activator.getDefault()
 					.getKeywords();
 			List<StyleRange> styles = new ArrayList<StyleRange>();
 			Color clrKwText = Display.getCurrent().getSystemColor(
 					SWT.COLOR_BLUE);
 			for (String keyWord : keywords) {
 				int indexOfKeyWord = event.lineText.indexOf(keyWord);
 				if (indexOfKeyWord != -1 && isWord(event.lineText, keyWord)) {
 					styles.add(new StyleRange(
 							event.lineOffset + indexOfKeyWord,
 							keyWord.length(), clrKwText, null, SWT.BOLD));
 
 				}
 			}
 			event.styles = styles.toArray(new StyleRange[0]);
 		} else {
 			Color errorText = Display.getCurrent().getSystemColor(
 					SWT.COLOR_DARK_RED);
 			StyleRange range = new StyleRange();
 			range.start = event.lineOffset;
 			range.length = event.lineText.length();
 			range.foreground = errorText;
 			event.styles = new StyleRange[] { range };
 		}
 	}
 
 	private boolean isWord(String lineText, String keyWord) {
		// TODO: Identify if it's a word
		return true;
 	}
 
 	public PythonConsolePage getPythonConsolePage() {
 		return pythonConsolePage;
 	}
 }
