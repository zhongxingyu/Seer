 package org.infuse.fiddle;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.text.DefaultHighlighter;
import javax.swing.tree.TreePath;
 
 import org.derric_lang.validator.interpreter.StructureMatch;
 
 @SuppressWarnings("serial")
 public class MainFrame extends JFrame {
     
     private final Interpreter _interpreter;
 
     private HexViewTable _hexView;
     private JTextPane _codeView;
     private SentenceViewTree _sentenceView;
 
     private File _codeFile;
     private File _dataFile;
     private Sentence _current;
     
     private DefaultHighlighter.DefaultHighlightPainter _hl = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
     
     public MainFrame() throws IOException {
         _interpreter = new Interpreter();
         initGUI();
     }
     
     private void initGUI() throws IOException {
         // Main window
         setTitle("Fiddle");
         setSize(800, 700);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setLocationRelativeTo(null);
         setLayout(new BorderLayout());
         
         // Hex view
         _hexView = new HexViewTable();
         JScrollPane tsp = new JScrollPane(_hexView);
         tsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         tsp.setPreferredSize(new Dimension(_hexView.getPreferredSize().width + 24, _hexView.getPreferredSize().height));
         ListSelectionListener sl = new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 if (!e.getValueIsAdjusting()) {
                     handleHexSelection();
                 }
             }
         };
         _hexView.getSelectionModel().addListSelectionListener(sl);
         _hexView.getColumnModel().getSelectionModel().addListSelectionListener(sl);
         _hexView.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent e) {
                 handleHexSelection();
             }
             @Override
             public void focusLost(FocusEvent e) { }
         });
         
         // Code view
         _codeView = new JTextPane();
         _codeView.setFont(new Font("Courier New", _codeView.getFont().getStyle(), _codeView.getFont().getSize()));
         JPanel noWrapPanel = new JPanel(new BorderLayout());
         noWrapPanel.add(_codeView);
         JScrollPane esp = new JScrollPane(noWrapPanel);
         esp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         _codeView.addCaretListener(new CaretListener() {
             @Override
             public void caretUpdate(CaretEvent e) {
                 handleCodeSelection();
             }
         });
         
         JSplitPane splitContent = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tsp, esp);
         
         // Sentence view
         _sentenceView = new SentenceViewTree();
         _sentenceView.setModel(new SentenceViewTreeModel("None", new ArrayList<StructureMatch>()));
         JScrollPane osp = new JScrollPane(_sentenceView);
         osp.setPreferredSize(new Dimension(160, 700));
         _sentenceView.addTreeSelectionListener(new TreeSelectionListener() {
             @Override
             public void valueChanged(TreeSelectionEvent e) {
                 handleSentenceSelection();
             }
         });
         
         JSplitPane splitNav = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, osp, splitContent);
         add(splitNav);
         
         // Menu
         JMenuBar mb = new JMenuBar();
         JMenu m = new JMenu("File");
         m.setMnemonic(KeyEvent.VK_F);
         JMenuItem mi = new JMenuItem("Open...", KeyEvent.VK_O);
         mi.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 chooseFiles();
             }
         });
         m.add(mi);
         m.addSeparator();
         JMenuItem mx = new JMenuItem("Exit", KeyEvent.VK_X);
         mx.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
         m.add(mx);
         mb.add(m);
         setJMenuBar(mb);
         
         // Finalize window
         setVisible(true);
         splitContent.setDividerLocation(0.50f);
     }
     
     private void chooseFiles() {
         try {
             FileNameExtensionFilter derricFilter = new FileNameExtensionFilter("Derric descriptions", "derric");
             _codeFile = getFile(derricFilter);
             if (_codeFile != null) {
                 List<String> lines = java.nio.file.Files.readAllLines(Paths.get(_codeFile.getPath()), Charset.forName("UTF8"));
                 StringBuilder sb = new StringBuilder();
                 for (String line : lines) {
                     sb.append(line);
                     sb.append("\n");
                 }
                 _codeView.setText(sb.toString());
                 _codeView.setCaretPosition(0);
                 _dataFile = getFile(null);
                 if (_dataFile != null) {
                     _current = _interpreter.run(_codeFile.getPath(), _dataFile.getPath());
                     setTitle(_codeFile.getName() + " on " + _dataFile.getName() + " results in: " + (_current.isValidated ? "Validated!" : "Not validated!"));
                     ((HexViewTableModel) _hexView.getModel()).setFile(_dataFile);
                     _hexView.revalidate();
                     _sentenceView.setModel(new SentenceViewTreeModel(_dataFile.getName(), _current.matches));
                     _sentenceView.revalidate();
                 }
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
     
     private File getFile(FileNameExtensionFilter filter) {
         JFileChooser fc = new JFileChooser();
         if (filter != null) {
             fc.setFileFilter(filter);
         }
         if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(MainFrame.this)) {
             File sf = fc.getSelectedFile();
             if (sf.exists() && !sf.isDirectory()) {
                 return sf;
             }
         }
         return null;
     }
     
     private void handleHexSelection() {
         if (_current != null) {
             StructureMatch match = null;
             if (_hexView.getSelectedColumn() > 0 && _hexView.getSelectedColumn() <= HexViewTableModel.WIDTH) {
                 int offset = (_hexView.getSelectedRow() * HexViewTableModel.WIDTH) + (_hexView.getSelectedColumn() - 1);
                 match = _current.getDataMatch(offset);
             }
             setHighlights(match);
         }
     }
     
     private void handleCodeSelection() {
         if (_current != null) {
             StructureMatch[] matches = _current.getCodeMatches(_codeView.getCaretPosition());
             setHighlights(matches);
         }
     }
     
     private void handleSentenceSelection() {
         if (_current != null) {
            TreePath selection = _sentenceView.getSelectionPath();
            if (selection.getPathCount() == 2) {
                StructureMatch match = ((StructureMatch)selection.getLastPathComponent());
                setHighlights(match);
            } else {
                setHighlights((StructureMatch)null);
            }
         }
     }
     
     private void setHighlights(StructureMatch... matches) {
         clearHighlights();
         for (StructureMatch match : matches) {
             if (match != null) {
                 try {
                     _hexView.addHighlight(match.inputLocation.getOffset(), match.inputLocation.getLength());
                     _codeView.getHighlighter().addHighlight(match.sequenceLocation.getOffset(), match.sequenceLocation.getOffset() + match.sequenceLocation.getLength(), _hl);
                     _codeView.getHighlighter().addHighlight(match.structureLocation.getOffset(), match.structureLocation.getOffset() + match.structureLocation.getLength(), _hl);
                     _sentenceView.addHighlight(match);
                 } catch (Exception ex) {
                     throw new RuntimeException(ex);
                 }
             }
         }
         _hexView.repaint();
         _sentenceView.repaint();
     }
     
     private void clearHighlights() {
         _hexView.clearHighlights();
         _codeView.getHighlighter().removeAllHighlights();
         _sentenceView.clearHighlights();
     }
     
 }
