 package burrito.client.crud.widgets;
 
 import com.google.gwt.core.client.JsArrayString;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.ui.TextArea;
 
 public class SelectableTextArea extends TextArea {
 
 	public class SelectedText {
 		protected String originalText = "";
 		public String text = "";
 		public Integer startpos;
 		
 		public SelectedText(JsArrayString tx) {
 			
 			originalText = tx.get(0);
 			text = originalText;
 			try {
 				startpos = Integer.parseInt(tx.get(1));
 			} catch (NumberFormatException e) {
 				startpos = getCursorPos();
 			}
 		}
 	}
 	
 	public void setSelectedText(SelectedText selectedText) {
 		Integer startpos = selectedText.startpos;
 		
 		String text = getText();
 		String beforeText = text.substring(0, startpos);
 		String afterText = text.substring(startpos + selectedText.originalText.length());
 		
 		setText(beforeText + selectedText.text + afterText);
 	}
 	
 	public SelectedText getSelectedTextObj() {
 		Element element = getElement();
 		JsArrayString tx = getSelectedText(element);
 		return new SelectedText(tx);
 	}
 	
 	private static native JsArrayString getSelectedText(Element elem) /*-{
 		if (isNaN(elem.selectionStart)) {
 			return ["", ""];
 		}
 	
 		var start = elem.selectionStart;
 		var end = elem.selectionEnd;
 		var sel = elem.value.substring(start, end);
 	
 		return [""+sel, ""+start];
 	}-*/;
 }
