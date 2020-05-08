 /**
  * Kuebiko - NoteFrame.java
  * Copyright 2011 Dave Huffman (daveh303 at yahoo dot com).
  * TODO license info.
  */
 
 package dmh.kuebiko.view;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.AbstractAction;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.text.DefaultEditorKit;
 
 import org.apache.commons.lang.SystemUtils;
 import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
 
 import dmh.kuebiko.Main;
 import dmh.kuebiko.controller.NoteManager;
 import dmh.kuebiko.model.Note;
 import dmh.kuebiko.util.ActionManager;
 import dmh.kuebiko.util.ActionObserverUtil;
 import dmh.util.Callback;
 
 /**
  * UI frame for displaying and editing notes.
  * XXX consider changing this class to be a POJO with a JFrame field.
  *
  * @author davehuffman
  */
 public class NoteFrame extends JFrame { // TODO rename to "StackFrame".
     private static final long serialVersionUID = 1L;
     
     private static enum Mode { SEARCH, EDIT }
     
     /** XXX consider moving to a utility class. */
     private static class NoteFrameObservable extends Observable {
         private void setChangedAndNotify() {
             setChangedAndNotify(null);
         }
         
         private void setChangedAndNotify(final Object arg) {
             setChanged();
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     notifyObservers(arg);
                 }});
         }
     }
 
     private final NoteManager noteMngr;
     
     private Mode mode;
     
     private final transient ActionManager actionMngr = new ActionManager();
     
     private final NoteFrameObservable observable = new NoteFrameObservable();
     
     private JSplitPane splitPane;
     JTextField searchText;
     private JScrollPane noteTableScroll;
     NoteTable noteTable;
     NotePanel notePanel;
     private final JLabel stateImageLabel = new JLabel();
     private Component horizontalStrut;
     private JMenuBar menuBar;
     private JMenuItem newNoteMenuItem;
     private JMenu fileMenu;
     private JMenuItem newStackMenuItem;
     private JMenuItem openStackMenuItem;
     private JMenuItem closeMenuItem;
     private JMenuItem closeAllMenuItem;
     private JMenuItem saveMenuItem;
     private JMenuItem saveAllMenuItem;
     private JMenu editMenu;
     private JMenuItem undoMenuItem;
     private JMenuItem redoMenuItem;
     private JMenuItem cutMenuItem;
     private JMenuItem copyMenuItem;
     private JMenuItem pasteMenuItem;
     private JMenuItem deleteNoteMenuItem;
     private JMenuItem renameNoteMenuItem;
 
     /**
      * Create the frame.
      */
     public NoteFrame(NoteManager noteMngr) {
         this.noteMngr = noteMngr;
         
         noteMngr.addObserver(new Observer() {
             @Override
             public void update(Observable o, Object arg) {
                 boolean stackChanged = (Boolean) arg;
                 toggleUnsavedChangeIndicator(stackChanged);
                 if (!stackChanged) {
                     notePanel.getHuxleyUiManager().resetTextChanged();
                 }
             }
         });
         
         // XXX consider moving this to a registration object to move instantiating, registering, and setting on a component to a single line for each action.
         ActionObserverUtil.registerEnMass(actionMngr, observable, 
                 new NewNoteAction(this), 
                 new OpenNoteAction(this), 
                 new DeleteNoteAction(this), 
                 new RenameNoteAction(this),
                 new SaveStackAction(this));
         
         menuBar = new JMenuBar();
         setJMenuBar(menuBar);
         
         fileMenu = new JMenu("File");
         menuBar.add(fileMenu);
         
         newNoteMenuItem = new JMenuItem(actionMngr.getAction(NewNoteAction.class));
         fileMenu.add(newNoteMenuItem);
 
         JMenuItem openNoteMenuItem = new JMenuItem(actionMngr.getAction(OpenNoteAction.class));
         fileMenu.add(openNoteMenuItem);
         
         deleteNoteMenuItem = new JMenuItem(actionMngr.getAction(DeleteNoteAction.class));
         fileMenu.add(deleteNoteMenuItem);
         
         renameNoteMenuItem = new JMenuItem(actionMngr.getAction(RenameNoteAction.class));
         fileMenu.add(renameNoteMenuItem);
         
         fileMenu.addSeparator();
         
         newStackMenuItem = new JMenuItem(actionMngr.getAction(NewStackAction.class));
         fileMenu.add(newStackMenuItem);
         
         openStackMenuItem = new JMenuItem(actionMngr.getAction(OpenStackAction.class));
         fileMenu.add(openStackMenuItem);
         
         fileMenu.addSeparator();
         
         closeMenuItem = new JMenuItem("Close");
         closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, 
                 Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         closeMenuItem.setEnabled(false);
         fileMenu.add(closeMenuItem);
         
         closeAllMenuItem = new JMenuItem("Close All");
         closeAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, 
                 InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         closeAllMenuItem.setEnabled(false);
         fileMenu.add(closeAllMenuItem);
         
         fileMenu.addSeparator();
         
         saveMenuItem = new JMenuItem(actionMngr.getAction(SaveStackAction.class));
         fileMenu.add(saveMenuItem);
         
         saveAllMenuItem = new JMenuItem("Save All");
         saveAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
                 InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         saveAllMenuItem.setEnabled(false);
         fileMenu.add(saveAllMenuItem);
         
         editMenu = new JMenu("Edit");
         menuBar.add(editMenu);
         
         undoMenuItem = new JMenuItem("Undo");
         undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 
                 Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         editMenu.add(undoMenuItem);
         
         redoMenuItem = new JMenuItem("Redo");
         redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 
                 InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         editMenu.add(redoMenuItem);
         
         editMenu.addSeparator();
         
         cutMenuItem = new JMenuItem(actionMngr.getAction(DefaultEditorKit.CutAction.class));
         cutMenuItem.setText("Cut");
         cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, 
                 Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         editMenu.add(cutMenuItem);
         
 //        copyMenuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
         copyMenuItem = new JMenuItem(actionMngr.getAction(DefaultEditorKit.CopyAction.class));
         copyMenuItem.setText("Copy");
         copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 
                 Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         editMenu.add(copyMenuItem);
         
         pasteMenuItem = new JMenuItem(actionMngr.getAction(DefaultEditorKit.PasteAction.class));
         pasteMenuItem.setText("Paste");
         pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 
                 Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         editMenu.add(pasteMenuItem);
         
         JMenu windowMenu = new JMenu("Window");
         menuBar.add(windowMenu);
         
         initialize();
         additionalSetup();
     }
 
     private NoteTable newNoteTable() {
         return new NoteTable(new NoteTableModel(noteMngr));
     }
 
     /**
      * Initialize the contents of the frame. The contents of this method was 
      * generated by Window Builder Pro.
      */
     private void initialize() {
         setTitle(buildTitle());
         setBounds(100, 100, 450, 300);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         GridBagLayout gridBagLayout = new GridBagLayout();
         gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
         gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
         gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
         gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 1.0, 1.0, 1.0,
                 Double.MIN_VALUE };
         getContentPane().setLayout(gridBagLayout);
         
         horizontalStrut = Box.createHorizontalStrut(20);
         horizontalStrut.setMinimumSize(new Dimension(3, 0));
         horizontalStrut.setPreferredSize(new Dimension(3, 0));
         horizontalStrut.setSize(new Dimension(3, 0));
         GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
         gbc_horizontalStrut.insets = new Insets(0, 0, 0, 0);
         gbc_horizontalStrut.gridx = 0;
         gbc_horizontalStrut.gridy = 0;
         getContentPane().add(horizontalStrut, gbc_horizontalStrut);
         
         GridBagConstraints gbc_stateImageLabel = new GridBagConstraints();
         gbc_stateImageLabel.insets = new Insets(0, 0, 0, 0);
         gbc_stateImageLabel.anchor = GridBagConstraints.EAST;
         gbc_stateImageLabel.gridx = 1;
         gbc_stateImageLabel.gridy = 0;
         stateImageLabel.setBorder(null);
         stateImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
         getContentPane().add(stateImageLabel, gbc_stateImageLabel);
 
         searchText = new JTextField();
         GridBagConstraints gbc_searchText = new GridBagConstraints();
         gbc_searchText.insets = new Insets(0, 0, 0, 0);
         gbc_searchText.fill = GridBagConstraints.HORIZONTAL;
         gbc_searchText.gridx = 2;
         gbc_searchText.gridy = 0;
         getContentPane().add(searchText, gbc_searchText);
         searchText.setColumns(10);
         if (SystemUtils.IS_OS_MAC_OSX) {
             // Make the text field look like the standard Mac OS X search
             // box control.
             searchText.putClientProperty("JTextField.variant", "search");
         }
 
         splitPane = new JSplitPane();
         splitPane.setBorder(null);
         splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
         GridBagConstraints gbc_splitPane = new GridBagConstraints();
         gbc_splitPane.gridwidth = 3;
         gbc_splitPane.gridheight = 4;
         gbc_splitPane.insets = new Insets(0, 0, 0, 0);
         gbc_splitPane.fill = GridBagConstraints.BOTH;
         gbc_splitPane.gridx = 0;
         gbc_splitPane.gridy = 1;
         getContentPane().add(splitPane, gbc_splitPane);
 
         notePanel = new NotePanel();
         notePanel.getHuxleyUiManager().setOnTextChangeCallback(
                 new Callback<Boolean>() {
                     @Override
                     public void callback(Boolean input) {
                         toggleUnsavedChangeIndicator(input);
                     }
                 });
         splitPane.setRightComponent(notePanel);
 
         noteTableScroll = new JScrollPane();
         noteTableScroll.setMinimumSize(new Dimension(23, 100));
         splitPane.setLeftComponent(noteTableScroll);
         noteTable = newNoteTable();
         noteTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         noteTableScroll.setViewportView(noteTable);
     }
 
     private String buildTitle() {
         return "Kuebiko";
     }
     
 //    static class FocusVetoableChangeListener implements VetoableChangeListener {
 //        @Override
 //        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
 //            Component oldComp = (Component)evt.getOldValue();
 //            Component newComp = (Component)evt.getNewValue();
 //            
 //            if ("focusOwner".equals(evt.getPropertyName())) {
 //                if (oldComp == null) {
 //                    // the newComp component will gain the focus
 //                } else {
 //                    // the oldComp component will lose the focus
 //                }
 //            } 
 ////            else if ("focusedWindow".equals(evt.getPropertyName())) {
 ////                if (oldComp == null) {
 ////                    // the newComp window will gain the focus
 ////                } else {
 ////                    // the oldComp window will lose the focus
 ////                }
 ////            }
 //
 //            boolean vetoFocusChange = false;
 //            if (vetoFocusChange) {
 //                throw new PropertyVetoException("message", evt);
 //            }
 //        }
 //    }
     
     /**
      * Perform additional setup to the frame. This is separate from the 
      * initialize() method so that the GUI builder doesn't mess with it.
      */
     private void additionalSetup() {
 //        KeyboardFocusManager.getCurrentKeyboardFocusManager()
 //                .addVetoableChangeListener(new FocusVetoableChangeListener());
         
         mode = Mode.SEARCH;
         
         setFocusTraversalPolicy(
                 new CustomFocusTraversalPolicy(searchText, notePanel));
         
         // Search Text Field.
         AutoCompleteDecorator.decorate(searchText, noteMngr.getNoteTitles(), false);
         searchText.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "clear");
         searchText.getDocument().addDocumentListener(
                 new DocumentListener() {
                     @Override
                     public void changedUpdate(DocumentEvent e) {
                         onSearchTextChanged();
                     }
                     @Override
                     public void insertUpdate(DocumentEvent e) {
                         onSearchTextChanged();
                     }
                     @Override
                     public void removeUpdate(DocumentEvent e) {
                         onSearchTextChanged();
                     }
                 });
         searchText.getActionMap().put("clear", new AbstractAction() {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 searchText.setText(null);
             }});
         
         searchText.addFocusListener(new FocusAdapter() {
             @Override
             public void focusGained(FocusEvent e) {
                 setModeToSearch();
                 searchText.selectAll();
             }
             @Override
             public void focusLost(FocusEvent e) {
                 noteTable.selectNote(searchText.getText());
             }
         });
         searchText.addActionListener(actionMngr.getAction(NewNoteAction.class));
         
         // Note Table.
         noteTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent event) {
 //                Main.log("valueChanged; adjusting=[%b].", event.getValueIsAdjusting());
                 if (event.getValueIsAdjusting()) {
                     return;
                 }
                 final Note selectedNote = noteTable.getSelectedNote();
                 final Note previousNote = notePanel.getNote();
 //                Main.log("valueChanged; previousNote=[%s].", previousNote);
 //                Main.log("valueChanged; selectedNote=[%s].", selectedNote);
             
                 if (selectedNote == null) {
                     setModeToSearch();
                 } else {
                     setModeToEdit();
                     notePanel.setNote(selectedNote);
                     searchText.setText(selectedNote.getTitle());
                 }
             }
         });
     }
     
     private void setModeToSearch() {
         mode = Mode.SEARCH;
         stateImageLabel.setIcon(new ImageIcon(
                 ImageManager.get().getImage("search")));
         noteTable.clearSelection();
         observable.setChangedAndNotify();
     }
     
     private void setModeToEdit() {
         mode = Mode.EDIT;
         stateImageLabel.setIcon(new ImageIcon(
                 ImageManager.get().getImage("edit")));
         observable.setChangedAndNotify();
     }
     
     private void toggleUnsavedChangeIndicator(boolean unsavedChanges) {
         if (SystemUtils.IS_OS_MAC_OSX) {
             // Toggle the Mac OS X window-modified indicator.
             getRootPane().putClientProperty("Window.documentModified", unsavedChanges);
         } else {
             if (unsavedChanges) {
                 setTitle("* " + buildTitle());
             } else {
                 setTitle(buildTitle());
             }
         }
     }
     
 //    private void onStackChange() {
 //        toggleUnsavedChangeIndicator(true);
 //    }
 //    
 //    private void onStackSave() {
 //        toggleUnsavedChangeIndicator(false);
 //    }
     
     public boolean isNoteSelected() {
         return (noteTable == null? false : (noteTable.getSelectedRow() != -1));
     }
     
     void deleteSelectedNote() {
         if (!isNoteSelected()) {
             throw new IllegalStateException("No note currently selected.");
         }
         
         noteTable.deleteSelectedNote();
         searchText.setText("");
     }
     
     /**
      * Handler for when the contents of the search text field changes.
      */
     private void onSearchTextChanged() {
         // Only update the UI if the user actively searching.
         if (mode == Mode.SEARCH) {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     noteTable.filter(searchText.getText());
                     notePanel.setNote(null);
                 }
             });
         }
     }
     
     boolean isInSearchMode() {
         return (mode == Mode.SEARCH);
     }
 
     boolean isInEditMode() {
         return (mode == Mode.EDIT);
     }
     
     JTextField getSearchText() {
         return searchText;
     }
     
     NoteTable getNoteTable() {
         return noteTable;
     }
     
     NotePanel getNotePanel() {
         return notePanel;
     }
     
     NoteManager getNoteMngr() {
         return noteMngr;
     }
 }
