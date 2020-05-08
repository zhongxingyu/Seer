 package org.nauxiancc.gui;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.Arrays;
 
 import javax.swing.*;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 
 import org.nauxiancc.configuration.Global.Paths;
 import org.nauxiancc.executor.Executor;
 import org.nauxiancc.projects.Project;
 
 /**
  * The main panel for editing the runner's code, displaying instructions and
  * displaying results.
  *
  * @author Naux
  * @since 1.0
  */
 
 public class JavaEditor extends JPanel implements KeyListener, ActionListener, FocusListener {
 
     private static final long serialVersionUID = 4203077483497169333L;
     private final JTextPane textPane;
     private final ResultsTable resultsTable;
     private final JTextArea instructions;
     private final Project project;
     private final JButton clear;
     private static final SimpleAttributeSet KEYWORD_SET = new SimpleAttributeSet();
     private static final SimpleAttributeSet NORMAL_SET = new SimpleAttributeSet();
 
     static {
         StyleConstants.setForeground(KEYWORD_SET, new Color(0x7036BE));
         StyleConstants.setForeground(NORMAL_SET, new Color(0x000000));
     }
 
     private static final String[] KEYWORDS = {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
             "continue", "default", "do", "double", "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto", "if",
             "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public",
             "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try",
             "void", "volatile", "while"};
 
     /**
      * Constructs a new Java editor based off of the project. Will load code and
      * skeleton for the runner if necessary.
      *
      * @param project The project to base this runner off of.
      */
 
     public JavaEditor(Project project) {
         super(new BorderLayout());
 
         this.project = project;
 
         instructions = new JTextArea();
         textPane = new JTextPane();
         final JButton button = new JButton("Run");
         button.setHorizontalTextPosition(SwingConstants.CENTER);
 
         button.setPreferredSize(new Dimension(200, button.getPreferredSize().height));
         button.setToolTipText("Runs the project. (Ctrl+R)");
 
         clear = new JButton("Clear Project");
         resultsTable = new ResultsTable(project);
 
         final JPanel leftSide = new JPanel(new BorderLayout());
         final JPanel buttons = new JPanel();
         final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsTable, leftSide);
         final JSplitPane textSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(instructions), split);
 
         textPane.setContentType("java");
         textPane.setFont(new Font("Consolas", Font.PLAIN, 12));
         textPane.addKeyListener(this);
         textPane.addFocusListener(this);
 
         button.addActionListener(this);
 
         clear.addActionListener(this);
 
         textSplit.setDividerLocation(100);
         split.setDividerLocation(300);
         buttons.add(clear);
         buttons.add(button);
 
         leftSide.add(new JScrollPane(textPane), BorderLayout.CENTER);
         leftSide.add(buttons, BorderLayout.SOUTH);
 
         instructions.setEditable(false);
         instructions.setLineWrap(false);
         instructions.setFont(new Font("Consolas", Font.PLAIN, 12));
         instructions.setToolTipText("Instructions and any errors will appear here.");
 
         textPane.requestFocus();
         textPane.setText(project.getCurrentCode());
         add(textSplit, BorderLayout.CENTER);
         highlightKeywords();
         setName(project.getName());
     }
 
     /**
      * Used to change the instruction panel's text. Most commonly, this is done
      * for error representation.
      *
      * @param string The <tt>String</tt> you would like to set the text to.
      * @see {@link JavaEditor#append(String)}
      * @since 1.0
      */
 
     public void setInstructionsText(String string) {
         instructions.setText(string);
     }
 
     /**
      * Appends a String to the instructions pane. This will be mostly used for
      * direct error sourcing.
      *
      * @param string The <tt>String</tt> you would like to append to the current
      *               text.
      * @since 1.0
      */
 
     public void append(String string) {
         instructions.append(string);
     }
 
     /**
      * This will highlight the Java keywords, from a list of reserved ones. It
      * will change the color of them, given by specific character attribute.
      *
      * @since 1.0
      */
 
     private void highlightKeywords() {
        final String line = textPane.getText().replaceAll("[([)];\n\t]", " ");
         int i = 0;
         for (final String word : line.split(" ")) {
             final boolean keyword = Arrays.binarySearch(KEYWORDS, word) >= 0;
             textPane.getStyledDocument().setCharacterAttributes(i, word.length(), keyword ? KEYWORD_SET : NORMAL_SET, true);
             i += word.length() + 1;
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         highlightKeywords();
     }
 
     public void keyTyped(KeyEvent e) {
     }
 
     public void keyPressed(KeyEvent e) {
         if (e.getKeyChar() == KeyEvent.VK_ALT) {
             actionPerformed(null);
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e != null) {
             if ((e.getSource() instanceof JButton)) {
                 if (e.getSource().equals(clear)) {
                     final JFrame confirm = new JFrame();
                     confirm.setLocationRelativeTo(getParent());
                     final int n = JOptionPane.showConfirmDialog(confirm, "This will delete all progress on this project.\nDo you wish to continue?",
                             "Continue?", JOptionPane.YES_NO_OPTION);
                     if (n == JOptionPane.YES_OPTION) {
                         if (!project.getFile().exists()) {
                             JOptionPane.showMessageDialog(null, "Error deleting current code!");
                         }
                         final File classF = new File(project.getFile().getAbsolutePath().replace(".java", ".class"));
                         project.getFile().delete();
                         classF.delete();
                         final File f = new File(Paths.SETTINGS + File.separator + "data.dat");
                         try {
                             final BufferedReader br = new BufferedReader(new FileReader(f));
                             final String temp = br.readLine();
                             if (temp != null) {
                                 final PrintWriter os = new PrintWriter(new FileWriter(f));
                                 os.print(temp.replace("|" + project.getName() + "|", ""));
                                 os.flush();
                                 os.close();
                             }
                             br.close();
                             project.setComplete(false);
                             textPane.setText(project.getCurrentCode());
                             highlightKeywords();
                         } catch (Exception e1) {
                             e1.printStackTrace();
                         }
                     }
                     return;
                 }
             }
         }
         if (textPane.getText().length() == 0 || project == null) {
             return;
         }
         if (!project.save(textPane.getText())) {
             JOptionPane.showMessageDialog(null, "Error saving current code!");
             return;
         }
         resultsTable.setResults(Executor.runAndGetResults(project));
     }
 
     @Override
     public void focusGained(FocusEvent e) {
         setInstructionsText(project.getProperties().getDescription());
     }
 
     @Override
     public void focusLost(FocusEvent e) {
 
     }
 }
