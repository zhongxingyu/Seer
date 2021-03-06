 /*
  * Copyright 2011 DELVING BV
  *
  * Licensed under the EUPL, Version 1.0 or as soon they
  * will be approved by the European Commission - subsequent
  * versions of the EUPL (the "Licence");
  * you may not use this work except in compliance with the
  * Licence.
  * You may obtain a copy of the Licence at:
  *
  * http://ec.europa.eu/idabc/eupl
  *
  * Unless required by applicable law or agreed to in
  * writing, software distributed under the Licence is
  * distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied.
  * See the Licence for the specific language governing
  * permissions and limitations under the Licence.
  */
 
 package eu.delving.sip.frames;
 
 import eu.delving.metadata.MappingFunction;
 import eu.delving.metadata.NodeMapping;
 import eu.delving.metadata.Operator;
 import eu.delving.metadata.RecDefNode;
 import eu.delving.sip.base.Exec;
 import eu.delving.sip.base.FrameBase;
 import eu.delving.sip.base.FunctionPanel;
 import eu.delving.sip.base.Utility;
 import eu.delving.sip.model.CreateModel;
 import eu.delving.sip.model.MappingCompileModel;
 import eu.delving.sip.model.MappingModel;
 import eu.delving.sip.model.SipModel;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.undo.UndoManager;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Refining the mapping interactively
  *
  * @author Gerald de Jong <gerald@delving.eu>
  */
 
 public class FieldMappingFrame extends FrameBase {
     private final Action REVERT_ACTION = new RevertAction();
     private final Action UNDO_ACTION = new UndoAction();
     private final Action REDO_ACTION = new RedoAction();
     private JTextArea groovyCodeArea;
     private JTextArea outputArea;
     private JEditorPane helpView;
     private JComboBox operatorBox = new JComboBox(Operator.values());
     private ContextVarListModel contextVarModel = new ContextVarListModel();
     private JList contextVarList = new JList(contextVarModel);
     private DictionaryPanel dictionaryPanel;
     private FunctionPanel functionPanel;
     private UndoManager undoManager = new UndoManager();
 
     public FieldMappingFrame(JDesktopPane desktop, SipModel sipModel) {
         super(Which.FIELD_MAPPING, desktop, sipModel, "Field Mapping", false);
         try {
             helpView = new JEditorPane(getClass().getResource("/groovy-help.html"));
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }
         contextVarList.setPrototypeCellValue("somelongvariablename");
         dictionaryPanel = new DictionaryPanel(sipModel.getCreateModel());
         functionPanel = new FunctionPanel(sipModel);
         groovyCodeArea = new JTextArea(sipModel.getFieldCompileModel().getCodeDocument());
         groovyCodeArea.setFont(new Font("Monospaced", Font.BOLD, 12));
         groovyCodeArea.setTabSize(3);
         groovyCodeArea.getDocument().addUndoableEditListener(undoManager);
         groovyCodeArea.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent documentEvent) {
                 handleEnablement();
             }
 
             @Override
             public void removeUpdate(DocumentEvent documentEvent) {
                 handleEnablement();
             }
 
             @Override
             public void changedUpdate(DocumentEvent documentEvent) {
                 handleEnablement();
             }
         });
         outputArea = new JTextArea(sipModel.getFieldCompileModel().getOutputDocument());
         attachAction(UNDO_ACTION);
         attachAction(REDO_ACTION);
         Utility.attachUrlLauncher(outputArea);
         wireUp();
     }
 
     private void attachAction(Action action) {
         groovyCodeArea.getInputMap().put((KeyStroke) action.getValue(Action.ACCELERATOR_KEY), action.getValue(Action.NAME));
         groovyCodeArea.getActionMap().put(action.getValue(Action.NAME), action);
     }
 
     private void handleEnablement() {
         UNDO_ACTION.setEnabled(undoManager.canUndo());
         REDO_ACTION.setEnabled(undoManager.canRedo());
     }
 
     @Override
     protected void onOpen(boolean opened) {
         sipModel.getFieldCompileModel().setEnabled(opened);
     }
 
     @Override
     protected void buildContent(Container content) {
         add(createTabs(), BorderLayout.CENTER);
     }
 
     private JComponent createTabs() {
         JTabbedPane tabs = new JTabbedPane();
         tabs.addTab("Field", createCodeOutputPanel());
         tabs.addTab("Functions", functionPanel);
         tabs.addTab("Dictionary", dictionaryPanel);
         tabs.addTab("Help", Utility.scrollV(helpView));
         return tabs;
     }
 
     private JPanel createCodeOutputPanel() {
         JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
         p.add(createCodePanel());
         p.add(createOutputPanel());
         return p;
     }
 
     private JPanel createCodePanel() {
         JPanel p = new JPanel(new BorderLayout());
         p.add(Utility.scrollVH("Groovy Code", groovyCodeArea), BorderLayout.CENTER);
         p.add(createBesideCode(), BorderLayout.EAST);
         return p;
     }
 
     private JPanel createBesideCode() {
         JPanel pp = new JPanel(new GridLayout(0, 1));
         pp.add(operatorBox);
         pp.add(createActionButton(UNDO_ACTION));
         pp.add(createActionButton(REDO_ACTION));
         pp.add(new JButton(REVERT_ACTION));
         JPanel p = new JPanel(new BorderLayout());
         p.add(pp, BorderLayout.NORTH);
         p.add(Utility.scrollV("Context", contextVarList), BorderLayout.CENTER);
         return p;
     }
 
     private JButton createActionButton(Action action) {
         KeyStroke stroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
         JButton button = new JButton(action);
         button.setText(button.getText()+ " " + KeyEvent.getKeyModifiersText(stroke.getModifiers()) + "-" + KeyEvent.getKeyText(stroke.getKeyCode()));
         return button;
     }
 
     private JPanel createOutputPanel() {
         JPanel p = new JPanel(new BorderLayout());
         p.setBorder(BorderFactory.createTitledBorder("Output Record"));
         p.add(Utility.scrollVH(outputArea), BorderLayout.CENTER);
         p.add(new JLabel("Note: URLs can be launched by double-clicking them.", JLabel.CENTER), BorderLayout.SOUTH);
         return p;
     }
 
     private void wireUp() {
         operatorBox.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent itemEvent) {
                 final NodeMapping nodeMapping = sipModel.getCreateModel().getNodeMapping();
                 if (nodeMapping != null) {
                     nodeMapping.operator = (Operator) operatorBox.getSelectedItem();
                     nodeMapping.notifyChanged();
                 }
             }
         });
         sipModel.getMappingModel().addChangeListener(new MappingModel.ChangeListener() {
             @Override
             public void functionChanged(MappingModel mappingModel, MappingFunction function) {
             }
 
             @Override
             public void nodeMappingChanged(MappingModel mappingModel, RecDefNode node, final NodeMapping nodeMapping) {
                 Exec.work(new Runnable() {
                     @Override
                     public void run() {
                         sipModel.getFieldCompileModel().setNodeMapping(nodeMapping);
                     }
                 });
             }
 
             @Override
             public void nodeMappingAdded(MappingModel mappingModel, RecDefNode node, NodeMapping nodeMapping) {
             }
 
             @Override
             public void nodeMappingRemoved(MappingModel mappingModel, RecDefNode node, NodeMapping nodeMapping) {
             }
         });
         sipModel.getCreateModel().addListener(new CreateModel.Listener() {
             @Override
             public void statsTreeNodeSet(CreateModel createModel) {
             }
 
             @Override
             public void recDefTreeNodeSet(CreateModel createModel) {
             }
 
             @Override
             public void nodeMappingSet(CreateModel createModel) {
                NodeMapping nodeMapping = createModel.getNodeMapping();
                 contextVarModel.setList(nodeMapping);
                 sipModel.getFieldCompileModel().setNodeMapping(nodeMapping);
                groovyCodeArea.setEditable(nodeMapping != null && nodeMapping.isUserCodeEditable());
                boolean all = nodeMapping == null || nodeMapping.getOperator() == Operator.ALL;
                operatorBox.setSelectedIndex(all ? 0 : 1);
             }
 
             @Override
             public void nodeMappingChanged(CreateModel createModel) {
             }
         });
         sipModel.getFieldCompileModel().addListener(new ModelStateListener());
         outputArea.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent documentEvent) {
                 outputArea.setCaretPosition(0);
             }
 
             @Override
             public void removeUpdate(DocumentEvent documentEvent) {
             }
 
             @Override
             public void changedUpdate(DocumentEvent documentEvent) {
             }
         });
     }
 
     private class ContextVarListModel extends AbstractListModel {
         private List<String> vars = new ArrayList<String>();
 
         public void setList(NodeMapping nodeMapping) {
             int size = getSize();
             vars.clear();
             if (size > 0) {
                 fireIntervalRemoved(this, 0, size);
             }
             if (nodeMapping != null) vars.addAll(nodeMapping.getContextVariables());
             size = getSize();
             if (size > 0) {
                 fireIntervalAdded(this, 0, size);
             }
         }
 
         @Override
         public int getSize() {
             return vars.size();
         }
 
         @Override
         public Object getElementAt(int i) {
             return vars.get(i);
         }
     }
 
     private class ModelStateListener implements MappingCompileModel.Listener {
 
         @Override
         public void stateChanged(final MappingCompileModel.State state) {
             Exec.swing(new Runnable() {
 
                 @Override
                 public void run() {
                     switch (state) {
                         case ORIGINAL:
                             undoManager.discardAllEdits();
                             // fall through
                         case SAVED:
                             groovyCodeArea.setBackground(new Color(1.0f, 1.0f, 1.0f));
                             break;
                         case EDITED:
                             groovyCodeArea.setBackground(new Color(1.0f, 1.0f, 0.9f));
                             break;
                         case ERROR:
                             groovyCodeArea.setBackground(new Color(1.0f, 0.9f, 0.9f));
                             break;
                     }
                 }
             });
         }
     }
 
     private class RevertAction extends AbstractAction {
         private RevertAction() {
             super("Revert to Original");
         }
 
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
             int answer = JOptionPane.showConfirmDialog(FieldMappingFrame.this, "Discard edited code and revert to the original?", "", JOptionPane.OK_CANCEL_OPTION);
             if (answer == JOptionPane.OK_OPTION) {
                 Exec.work(new Runnable() {
                     @Override
                     public void run() {
                         sipModel.getCreateModel().revertToOriginal();
                         sipModel.getFieldCompileModel().setNodeMapping(sipModel.getCreateModel().getNodeMapping());
                     }
                 });
             }
         }
     }
 
     private class UndoAction extends AbstractAction {
         private UndoAction() {
             super("Undo");
             putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         }
 
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
             undoManager.undo();
         }
     }
 
     private class RedoAction extends AbstractAction {
         private RedoAction() {
             super("Redo");
             putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_DOWN_MASK));
         }
 
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
             undoManager.redo();
         }
     }
 
 }
