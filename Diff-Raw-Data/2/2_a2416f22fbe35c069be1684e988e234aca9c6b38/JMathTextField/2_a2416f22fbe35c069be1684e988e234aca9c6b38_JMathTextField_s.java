 package applets.Termumformungen$in$der$Technik_03_Logistik;
 
 import javax.swing.JTextField;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.DocumentFilter;
 import javax.swing.text.PlainDocument;
 
 public class JMathTextField extends JTextField {
 
 	private static final long serialVersionUID = 3991754180692357067L;
 
 	private OperatorTree operatorTree = new OperatorTree();
 	public OperatorTree getOperatorTree() { return operatorTree; }
 
 	protected void updateByOpTree(javax.swing.text.DocumentFilter.FilterBypass fb) {
 		String oldStr = JMathTextField.this.getText();
 		String newStr = operatorTree.toString();
 		int commonStart = Utils.equalStartLen(oldStr, newStr);
 		int commonEnd = Utils.equalEndLen(oldStr, newStr);
 		if(commonEnd + commonStart >= Math.min(oldStr.length(), newStr.length()))
 			commonEnd = Math.min(oldStr.length(), newStr.length()) - commonStart;
 
 		try {
 			fb.replace(commonStart, oldStr.length() - commonEnd - commonStart, newStr.substring(commonStart, newStr.length() - commonEnd), null);
 		} catch (BadLocationException e) {
 			// this should not happen. we should have checked this. this whole function should be safe
 			throw new AssertionError(e);
 		}	
 	}
 	
 	@Override
 	protected Document createDefaultModel() {
 		PlainDocument d = (PlainDocument) super.createDefaultModel();
 
 		d.setDocumentFilter(new DocumentFilter() {
 			@Override
 			public void insertString(FilterBypass fb, int offset,
 					String string, AttributeSet attr)
 					throws BadLocationException {					
 				replace(fb, offset, 0, string, attr);
 			}
 			@Override
 			public void remove(FilterBypass fb, int offset, int length)
 					throws BadLocationException {
 				replace(fb, offset, length, "", null);
 			}
 			@Override
 			public void replace(FilterBypass fb, int replOffset, int replLen,
 					String string, AttributeSet attrs)
 					throws BadLocationException {
 				String s = JMathTextField.this.getText();
 				boolean insertedDummyChar = false;
 				boolean insertedBrackets = false;
 				String marked = s.substring(replOffset, replOffset + replLen);
 
				if(string.matches(" |\\)") && replOffset + 1 < s.length() && s.substring(replOffset, replOffset + 1).equals(string)) {
 					JMathTextField.this.setCaretPosition(replOffset + 1);
 					return;
 				}
 				
 				string = string.replace(" ", "");
 				if(string.isEmpty() && marked.equals("(")) {
 					int count = 1;
 					while(replOffset + replLen < s.length()) {
 						replLen++;
 						marked = s.substring(replOffset, replOffset + replLen);
 						if(marked.charAt(0) == '(') count++;
 						else if(marked.charAt(0) == ')') count--;
 						if(count == 0) break;
 					}
 				}
 				else if(string.isEmpty() && marked.equals(")")) {
 					int count = -1;
 					while(replOffset > 0) {
 						replOffset--;
 						replLen++;
 						marked = s.substring(replOffset, replOffset + replLen);
 						if(marked.charAt(0) == '(') count++;
 						else if(marked.charAt(0) == ')') count--;
 						if(count == 0) break;
 					}
 				}
 				
 				if(replLen == 0 && string.matches("\\+|-|\\*|/|\\^|=")) {
 					if(s.substring(replOffset).matches(" *(((\\+|-|∙|/|\\^|=|\\)).*)|)")) {
 						string = string + "_";
 						insertedDummyChar = true;
 					} else if(s.substring(replOffset).matches(" .*")) {
 						string = "_" + string;
 						insertedDummyChar = true;
 					}
 				}
 				else if(string.matches("\\(")) {
 					if(replLen == 0 || marked.equals("_")) {
 						string = "(_)";
 						insertedDummyChar = true;
 					}
 					else {
 						string = "(" + marked + ")";
 						insertedBrackets = true;
 					}
 				}
 				else if(string.matches("\\)"))
 					return; // ignore that
 				
 				// situation: A = B, press DEL -> make it A = _
 				if(string.isEmpty() && replOffset > 0 && replLen == 1 && s.substring(replOffset - 1, replOffset).equals(" ") && !marked.equals("_")) {
 					string = "_";
 					insertedDummyChar = true;
 				}
 				
 				setNewString(fb, s.substring(0, replOffset) + string + s.substring(replOffset + replLen));
 				
 				if(insertedDummyChar) {
 					int p = JMathTextField.this.getText().indexOf('_');
 					if(p >= 0) {
 						JMathTextField.this.setCaretPosition(p);
 						JMathTextField.this.moveCaretPosition(p + 1);						
 					}
 				}
 				else if(insertedBrackets) {
 					// move just before the ')'
 					if(JMathTextField.this.getCaretPosition() > 0)
 						JMathTextField.this.setCaretPosition(JMathTextField.this.getCaretPosition() - 1);
 				}
 			}
 
 			synchronized void setNewString(FilterBypass fb, String tempStr) throws BadLocationException {
 				operatorTree = OTParser.parse(tempStr, null);
 				JMathTextField.this.updateByOpTree(fb);
 			}
 		});
 		
 		return d;
 	}
 	
 }
