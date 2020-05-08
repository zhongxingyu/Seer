 package descent.ui.text.rules;
 
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;
 
 /**
  * Extends a SingleLineRule in order to match D strings.
  * D strings can end with 'c', 'w' or 'd'. Examples:
  * "hello"
  * "hello"c
  * "hello"w
  * "hello"d
  */
 public class DStringRule extends MultiLineRule {
 
 	public DStringRule(String startSequence, String endSequence, IToken token, char escapeCharacter) {
 		super(startSequence, endSequence, token, escapeCharacter, true);
 	}
 	
 	@Override
 	protected boolean endSequenceDetected(ICharacterScanner scanner) {
 		if (super.endSequenceDetected(scanner)) {
 			int c = scanner.read();
 			if (c == 'c' || c == 'w' || c == 'd') {
 				return true;
 			} else {
 				scanner.unread();
 				return true;
 			}
 		} else {
 			return false;
 		}
 	}
 
 }
