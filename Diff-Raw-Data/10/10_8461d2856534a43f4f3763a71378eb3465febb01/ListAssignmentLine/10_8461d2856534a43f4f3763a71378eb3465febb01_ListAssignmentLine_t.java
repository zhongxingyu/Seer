 package line;
 
 import java.util.Vector;
 
 import model.ErrorHandler;
 
 import view.CPlusPlusWindow;
 
 public class ListAssignmentLine extends Token implements AbstractLine {
 	String leftOperand;
 	public ListAssignmentLine(String text, int indent) {
 		tokens = new Vector<String>();
 		StringBuffer stringBuffer = new StringBuffer(text);
 		isOk = true;
 		this.indent = indent;
 		leftOperand = getLeftOperand(text);
 		getTokens(stringBuffer);
 	}
 
 	public boolean isOk() {
 		return isOk;
 	}
 
 	public void writeLine(CPlusPlusWindow cPlusPlusWindow) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("string tmp[]= {");
 		for (int i = 0; i < tokens.size() - 1; ++i)
			builder.append("\"" + tokens.elementAt(i) + "\"" + ",");
		builder.append("\"" + tokens.lastElement() + "\"");
 		builder.append("};");
 		cPlusPlusWindow.setText(builder.toString(), indent);
 
 		builder.setLength(0);
 		builder.append(leftOperand + ".setValue" + "(tmp,sizeof(tmp)/sizeof(tmp[0]));");
 
 		cPlusPlusWindow.setText(builder.toString(), indent);
 	}
 
 	public String getLeftOperand(String text) {
 		for (int i = 0; i < text.length(); ++i) {
 			char c = text.charAt(i);
 			if (c == '=') {
 				String operand = text.substring(0, i);
 				operand = operand.replaceAll("\\s", "");
 				return operand;
 			}
 		}
 		return "";
 	}
 
 	public void getTokens(StringBuffer text) {
 		int i = 0;
 		for (; i < text.length(); ++i) {
 			char c = text.charAt(i);
 			if (c == '=')
 				break;
 		}
 
 		String newText = text.substring(i + 1).trim();
 		if (!newText.startsWith(("[")))
 			isOk = false;
 		if (!((newText.endsWith("];") || (newText.endsWith("]")))))
 			isOk = false;
 
 		if (newText.endsWith("]"))
 			newText = newText.substring(1, newText.length() - 1);
 		else
 			newText = newText.substring(1, newText.length() - 2);
 
 		StringBuilder builder = new StringBuilder();
 		for (int j = 0; j < newText.length(); ++j) {
 			char c = newText.charAt(j);
 			if (c == ' ' || c == '\'' || c == '\"')
 				continue;
 			else if (c == ',') {
 				if (builder.length() != 0)
 					tokens.add(builder.toString());
 				else
 					isOk = false;
 				builder.setLength(0);
 			} else
 				builder.append(c);
 		}
 		if (builder.length() !=0)
 			tokens.add(builder.toString());
 	}
 
 	public void error() {
 		ErrorHandler.getInstance().setError("Error in ListAssignmentLine");
 		isOk = false;
 
 	}
 
 }
