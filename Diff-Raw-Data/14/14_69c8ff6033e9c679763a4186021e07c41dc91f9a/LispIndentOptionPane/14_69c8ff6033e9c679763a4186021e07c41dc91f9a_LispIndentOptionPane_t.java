 
 import org.gjt.sp.jedit.AbstractOptionPane;
 import org.gjt.sp.jedit.jEdit;
 
import javax.swing.ButtonGroup;
 import javax.swing.JCheckBox;
import javax.swing.JLabel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class LispIndentOptionPane extends AbstractOptionPane {
 	// file ending settings
 	JCheckBox check_ending;
 	JTextField file_endings;
 	
 	// vertical indent settings
 	JRadioButton use_defun_indent_by_default;
 	JRadioButton use_align_indent_by_default;
 	JCheckBox check_pattern_for_defun_indent;
 	JCheckBox check_pattern_for_align_indent; // sexy, same line length
 	JTextArea defun_indent_pattern;
 	JTextArea align_indent_pattern;
 	
 	public LispIndentOptionPane() { super("lispindent"); }
 	
 	String getProperty(String name) {
 		return jEdit.getProperty(LispIndentPlugin.OPTIONS_PREFIX + name);
 	}
 	void setProperty(String name, String value) {
 		jEdit.setProperty(LispIndentPlugin.OPTIONS_PREFIX + name, value);
 	}
 	boolean getBooleanProperty(String name) {
 		return jEdit.getBooleanProperty(LispIndentPlugin.OPTIONS_PREFIX + name);
 	}
 	void setBooleanProperty(String name, boolean value) {
 		jEdit.setBooleanProperty(LispIndentPlugin.OPTIONS_PREFIX + name, value);
 	}
 	
 	void insert_separator() {
 		addComponent(new javax.swing.JSeparator());
 	}
 	
 	void init_file_ending_settings() {
 		check_ending = new JCheckBox("Only use lisp indenting if the file name matches:",
 			getBooleanProperty("check_ending"));
 		file_endings = new JTextField(getProperty("file_endings_regex"), 25);
 		
 		file_endings.setEnabled(check_ending.isSelected());
 		check_ending.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				file_endings.setEnabled(check_ending.isSelected());
 			}
 		});
 		
 		addComponent(check_ending);
 		addComponent(file_endings);
 	}
 	
 	void update_indent_pattern_enable() {
 		boolean defun_indent = use_defun_indent_by_default.isSelected();
 		check_pattern_for_defun_indent.setEnabled(!defun_indent);
 		defun_indent_pattern.setEnabled(!defun_indent && check_pattern_for_defun_indent.isSelected());
 		check_pattern_for_align_indent.setEnabled(defun_indent);
 		align_indent_pattern.setEnabled(defun_indent && check_pattern_for_align_indent.isSelected());
 	}
 	
 	void addPatternEnableListener(javax.swing.AbstractButton component) {
 		component.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				update_indent_pattern_enable();
 			}
 		});
 	}
 	
 	void init_vertical_indent_settings() {
 		use_defun_indent_by_default = new JRadioButton("Indent two spaces by default");
 		use_align_indent_by_default = new JRadioButton("Indent to function arguments by default");
 		ButtonGroup bg = new ButtonGroup();
 		bg.add(use_defun_indent_by_default);
 		bg.add(use_align_indent_by_default);
 		
 		if(getBooleanProperty("use_defun_indent_by_default")) {
 			use_defun_indent_by_default.setSelected(true);
 		}
 		else { use_align_indent_by_default.setSelected(true); }
 		
 		check_pattern_for_defun_indent = new JCheckBox("Indent two spaces if operator matches:");
 		check_pattern_for_align_indent = new JCheckBox("Indent to function arguments if operator matches:");
 		check_pattern_for_defun_indent.setSelected(getBooleanProperty("check_pattern_for_defun_indent"));
 		check_pattern_for_align_indent.setSelected(getBooleanProperty("check_pattern_for_align_indent"));
 		
 		defun_indent_pattern = new JTextArea(10, 50);
 		align_indent_pattern = new JTextArea(10, 50);
 		defun_indent_pattern.setText(getProperty("defun_indent_pattern"));
 		align_indent_pattern.setText(getProperty("align_indent_pattern"));
 		
 		addPatternEnableListener(use_defun_indent_by_default);
 		addPatternEnableListener(use_align_indent_by_default);
 		addPatternEnableListener(check_pattern_for_defun_indent);
 		addPatternEnableListener(check_pattern_for_align_indent);
 		update_indent_pattern_enable();
 		
 		addComponent(use_defun_indent_by_default);
 		addComponent(use_align_indent_by_default);
 		addComponent(check_pattern_for_defun_indent);
 		addComponent(new JScrollPane(defun_indent_pattern));
 		addComponent(check_pattern_for_align_indent);
 		addComponent(new JScrollPane(align_indent_pattern));
 	}
 	
 	public void _init() {
 		addSeparator("options.lispindent.file_ending_options.label");
 		init_file_ending_settings();
 		addSeparator("options.lispindent.indent_options.label");
 		init_vertical_indent_settings();
 	}
 	
 	void save_file_ending_settings() {
 		setBooleanProperty("check_ending", check_ending.isSelected());
 		setProperty("file_endings_regex", file_endings.getText());
 	}
 	
 	void save_vertical_indent_settings() {
 		setBooleanProperty("use_defun_indent_by_default", use_defun_indent_by_default.isSelected());
 		setBooleanProperty("check_pattern_for_defun_indent", check_pattern_for_defun_indent.isSelected());
 		setBooleanProperty("check_pattern_for_align_indent", check_pattern_for_align_indent.isSelected());
 		setProperty("defun_indent_pattern", defun_indent_pattern.getText());
 		setProperty("align_indent_pattern", align_indent_pattern.getText());
 	}
 	
 	public void _save() {
 		save_file_ending_settings();
 		save_vertical_indent_settings();
 	}
 }
