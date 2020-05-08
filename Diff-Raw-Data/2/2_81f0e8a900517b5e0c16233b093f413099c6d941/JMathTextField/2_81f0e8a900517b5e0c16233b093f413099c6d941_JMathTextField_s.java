 package applets.Termumformungen$in$der$Technik_03_Logistik;
 
 import javax.swing.JTextField;
 import javax.swing.text.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class JMathTextField extends JTextField {
 
 	private static final long serialVersionUID = 3991754180692357067L;
 
 	{
 		this.addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 				moveCaretPosition(0);
 				setCaretPosition(getText().length());
 			}
 			public void focusLost(FocusEvent e) {}
 		});
 
 		this.addInputMethodListener(new InputMethodListener() {
 			public void inputMethodTextChanged(final InputMethodEvent event) {
 				System.out.println(event);
 				event.consume();
 				JMathTextField.this.getInputContext().endComposition();
 				if(event.getText() == null) return;
 				if(event.getCommittedCharacterCount() > 0) return;
 				final char c = event.getText().last();
 				if(c == '^') {
 					final KeyEvent ke = new KeyEvent(
 							JMathTextField.this, KeyEvent.KEY_TYPED,
 							EventQueue.getMostRecentEventTime(),
 							0, KeyEvent.VK_UNDEFINED, c);
 					JMathTextField.this.processKeyEvent(ke);
 				}
 			}
 			public void caretPositionChanged(InputMethodEvent event) {}
 		});
 	}
 
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
 				String replStr = s.substring(replOffset, replOffset + replLen);
 
 				if(string.matches(" |\\)") && replOffset + 1 <= s.length() && s.substring(replOffset, replOffset + 1).equals(string)) {
 					JMathTextField.this.setCaretPosition(replOffset + 1);
 					return;
 				}
 
 				if(string.isEmpty() && replStr.equals(" ")) {
 					JMathTextField.this.setCaretPosition(replOffset);
 					return;
 				}
 
 				string = string.replace(" ", "");
 				if(string.isEmpty() && replStr.equals("(")) {
 					int count = 1;
 					while(replOffset + replLen < s.length()) {
 						replLen++;
 						replStr = s.substring(replOffset, replOffset + replLen); // just update in case we need it later again
 						if(replStr.charAt(0) == '(') count++;
 						else if(replStr.charAt(0) == ')') count--;
 						if(count == 0) break;
 					}
 				}
 				else if(string.isEmpty() && replStr.equals(")")) {
 					int count = -1;
 					while(replOffset > 0) {
 						replOffset--;
 						replLen++;
 						replStr = s.substring(replOffset, replOffset + replLen);
 						if(replStr.charAt(0) == '(') count++;
 						else if(replStr.charAt(0) == ')') count--;
 						if(count == 0) break;
 					}
 				}
 				
 				if(string.matches("\\+|-|\\*|/|\\^|=")) {
 					if(s.substring(replOffset+replLen).matches("( *(\\+|-|∙|/|\\^|=|\\)).*)|")) {
 						string = string + "_";
 						insertedDummyChar = true;
 					} else if(s.substring(replOffset+replLen).matches(" .*")) {
 						string = "_" + string;
 						insertedDummyChar = true;
 					}
 				}
 				else if(string.matches("\\(")) {
					if(s.isEmpty() || replStr.equals("_")) {
 						string = "(_)";
 						insertedDummyChar = true;
 					}
 					else if(!replStr.isEmpty()) {
 						string = "(" + replStr + ")";
 						insertedBrackets = true;
 					}
 					else
 						return; // ignore this
 				}
 				else if(string.matches("\\)"))
 					return; // ignore that
 				
 				// situation: A = B, press DEL -> make it A = _
 				if(string.isEmpty() && Utils.reveresedString(s.substring(0,replOffset)).matches("( )*(\\+|-|∙|/|\\^|=).*") && !replStr.matches("_|\\+|-|∙|/|\\^|=")) {
 					string = "_";
 					insertedDummyChar = true;
 				}
 				else if(string.isEmpty() && Utils.reveresedString(s.substring(0,replOffset)).matches("( )*(\\+|-|∙|/|\\^|=).*") && replStr.equals("_")) {
 					while(s.substring(replOffset-1,replOffset).equals(" ")) {
 						replOffset--;
 						replLen++;
 					}
 					replOffset--; replLen++; // go just before the op
 					// just update in case we need it later again
 					// noinspection UnusedAssignment
 					replStr = s.substring(replOffset, replOffset + replLen);
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
