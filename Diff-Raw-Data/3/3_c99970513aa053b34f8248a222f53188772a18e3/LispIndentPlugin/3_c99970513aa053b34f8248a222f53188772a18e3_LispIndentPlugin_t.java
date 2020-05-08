 
 import org.gjt.sp.jedit.buffer.BufferAdapter;
 import org.gjt.sp.jedit.buffer.BufferListener;
 import org.gjt.sp.jedit.jEdit;
 import org.gjt.sp.jedit.buffer.JEditBuffer;
 import org.gjt.sp.jedit.Buffer;
 import org.gjt.sp.jedit.textarea.TextArea;
 
 public class LispIndentPlugin extends org.gjt.sp.jedit.EditPlugin {
 	public static final String OPTIONS_PREFIX = "LISPINDENT_OPTION_";
 	
 	static String getProperty(String name) {
 		return jEdit.getProperty(OPTIONS_PREFIX + name);
 	}
 	static boolean getBooleanProperty(String name) {
 		return jEdit.getBooleanProperty(OPTIONS_PREFIX + name);
 	}
 	
 	static boolean is_lisp_indent_ending(Buffer buffer) {
 		String p = buffer.getPath();
 		String regex = getProperty("file_endings_regex");
 		return p.matches(regex);
 	}
 	
 	static boolean should_use_lisp_indent(Buffer buffer) {
 		if(getBooleanProperty("check_ending")) {
 			return is_lisp_indent_ending(buffer);
 		}
 		else { return true; }
 	}
 	
 	static int get_line_offset(JEditBuffer buffer, int index) {
 		int line = buffer.getLineOfOffset(index);
 		return index - buffer.getLineStartOffset(line);
 	}
 	
 	static String get_operator(JEditBuffer buffer, int index) {
 		String c;
 		int start_index = index + 1;
 		int end_index = index;
 		for(int i = index; i < buffer.getLength(); i++) {
 			end_index = i;
 			c = buffer.getText(i, 1);
 			if(c.equals(" ") || c.equals("\n")) { break; }
 		}
 		return buffer.getText(index + 1, end_index - (index + 1));
 	}
 	
 	static int get_parenthesis_indent(JEditBuffer buffer, int index) {
 		int line_offset = get_line_offset(buffer, index);
 		String op = get_operator(buffer, index);
 		if(getBooleanProperty("use_defun_indent_by_default")) {
 			if(getBooleanProperty("check_pattern_for_align_indent") &&
 				op.matches(getProperty("align_indent_pattern"))) {
 				return line_offset + op.length() + 2;
 			}
 			else { return line_offset + 2; }
 		}
 		else {
 			if(getBooleanProperty("check_pattern_for_defun_indent") &&
 				op.matches(getProperty("defun_indent_pattern"))) {
 				return line_offset + 2;
 			}
 			else { return line_offset + op.length() + 2; }
 		}
 	}
 	
 	static int get_bracket_indent(JEditBuffer buffer, int index) { return get_line_offset(buffer, index) + 1; }
 	
 	static int get_indent_of_line(JEditBuffer buffer, int line) {
 		int i = buffer.getLineStartOffset(line);
 		int sum = 0;
 		while(buffer.getText(i, 1).equals(" ")) {
 			sum += 1;
 			i += 1;
 		}
 		return sum;
 	}
 	
 	static int get_indent(int start, JEditBuffer buffer) {
 		if(start < 0) { return 0; }
 		int start_line = buffer.getLineOfOffset(start);
 		int current_line_start = buffer.getLineStartOffset(start_line);
 		int br = 0;       // bracket count
 		int cbr = 0;      // curly bracket count
 		int pa = 0;       // parenthesis count
 		String c;
 		for(int i = start; i >= 0; i--) {
 			c = buffer.getText(i, 1);
 			// update counts
 			if(c.equals("("))      { pa += 1; }
 			else if(c.equals(")")) { pa -= 1; }
 			else if(c.equals("[")) { br += 1; }
 			else if(c.equals("]")) { br -= 1; }
 			else if(c.equals("{")) { cbr += 1; }
 			else if(c.equals("}")) { cbr -= 1; }
 			// check counts for possible indenting
 			if(br > 0 || cbr > 0) { return get_bracket_indent(buffer, i); }
 			else if(pa > 0) {
 				if(i != 0 && buffer.getText(i - 1, 1).equals("'")) { // indent as list
 					return get_bracket_indent(buffer, i);
 				}
 				else { return get_parenthesis_indent(buffer, i); }   // indent as function call
 			}
 			else if(c.equals("\n") && br == 0 && cbr == 0 && pa == 0) {
 				return get_indent_of_line(buffer, buffer.getLineOfOffset(i + 1));
 			}
 		}
 		return 0;
 	}
 	
 	static String build_indent_string(int offset) {
 		String str = "";
 		for(int i = 0; i < offset; i++) { str = str.concat(" "); }
 		return str;
 	}
 	
 	static void indent_line(JEditBuffer buffer, int line) {
 		int line_start = buffer.getLineStartOffset(line);
 		int current_indent = get_indent_of_line(buffer, line);
 		buffer.remove(line_start, current_indent);
 		// we need "- 2" here to "skip" the newline
 		buffer.insert(line_start, build_indent_string(get_indent(line_start - 2, buffer)));
 	}
 	
 	public static void indent(Buffer buffer, TextArea textArea) {
 		int[] lines = textArea.getSelectedLines();
 		if(should_use_lisp_indent(buffer)) {
			buffer.beginCompoundEdit();
 			for(int i = 0; i < lines.length; i++) {
 				int line = lines[i];
 				indent_line(buffer, line);
 			}
			buffer.endCompoundEdit();
 		}
 		else { buffer.indentLines(lines); }
 	}
 	
 	public static void insert_enter_and_indent(Buffer buffer, TextArea textArea) {
 		if(should_use_lisp_indent(buffer)) {
 			// we need "- 1" here to skip the newline (if the caret is at the end of a line)
 			int indent = get_indent(textArea.getCaretPosition() - 1, buffer);
 			buffer.insert(textArea.getCaretPosition(), "\n");
 			buffer.insert(textArea.getCaretPosition(), build_indent_string(indent));
 		}
 		else { textArea.insertEnterAndIndent(); }
 	}
 }
