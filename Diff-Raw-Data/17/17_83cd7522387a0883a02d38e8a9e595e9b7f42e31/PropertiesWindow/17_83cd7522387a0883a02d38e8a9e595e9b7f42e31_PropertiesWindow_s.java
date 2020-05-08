 package ru.korgov.intellij.lspr.properties.ui;
 
 import com.intellij.codeInsight.template.JavaCodeContextType;
 import com.intellij.codeInsight.template.impl.TemplateContext;
 import com.intellij.codeInsight.template.impl.TemplateEditorUtil;
 import com.intellij.ide.highlighter.XmlFileType;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.EditorFactory;
 import com.intellij.openapi.fileTypes.FileType;
 import com.intellij.uiDesigner.core.GridConstraints;
 import org.jetbrains.annotations.Nullable;
 import ru.korgov.intellij.lspr.properties.api.ConflictsPolicity;
 import ru.korgov.intellij.lspr.properties.api.Constants;
 import ru.korgov.intellij.lspr.properties.api.SearchScopeEnum;
 import ru.korgov.intellij.lspr.properties.api.XProperties;
 import ru.korgov.util.alias.Cf;
 import ru.korgov.util.alias.Su;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Set;
 
 /**
  * Author: Kirill Korgov (kirill@korgov.ru)
  * Date: 02.12.12
  */
 public class PropertiesWindow {
 
     private static final GridConstraints DEFAULT_CONSTRAINTS = new GridConstraints(0, 0, 1, 1,
             GridConstraints.ANCHOR_CENTER,
             GridConstraints.FILL_BOTH,
             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
             null, null, null, 0, true);
 
     private JButton setDefaultHeadersButton;
     private JCheckBox productionScope;
     private JCheckBox testScope;
     private JCheckBox librariesScope;
     private JCheckBox excludeBeansCheckbox;
     private JRadioButton conflictsAutoOneRadBut;
     private JRadioButton conflictsAutoAllRadBut;
     private JRadioButton conflictsManualSelectRadBut;
     private JPanel mainPanel;
     private JCheckBox customBeansMappingCheckbox;
     private JCheckBox onlyVCSFilesCheckbox;
 
     private JPanel priorityFilePathsEditorPanel;
     private JPanel beansHeaderEditorPanel;
     private JPanel beansFooterEditorPanel;
     private JPanel customBeansMappingEditorPanel;
     private JPanel excludeBeansEditorPanel;
 
     private final Editor priorityFilePathsEditor = createEditor(false, false, XmlFileType.INSTANCE);
     private final Editor beansHeaderEditor = createEditor(false, false, XmlFileType.INSTANCE);
     private final Editor beansFooterEditor = createEditor(false, false, XmlFileType.INSTANCE);
     private final Editor customBeansMappingEditor = createEditor(false, false, XmlFileType.INSTANCE);
     private final Editor excludeBeansEditor = createEditor(false, false, XmlFileType.INSTANCE);
 
     public JPanel getMainPanel() {
         return mainPanel;
     }
 
     public PropertiesWindow() {
 
         initEditors();
 
         setDefaultHeadersButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 ApplicationManager.getApplication().runWriteAction(new Runnable() {
                     @Override
                     public void run() {
                         setTextFafety(beansHeaderEditor, Constants.DEFAULT_HEADER);
                         setTextFafety(beansFooterEditor, Constants.DEFAULT_FOOTER);
                     }
                 });
             }
         });
     }
 
     private void initEditors() {
         priorityFilePathsEditorPanel.add(priorityFilePathsEditor.getComponent(), DEFAULT_CONSTRAINTS);
         beansHeaderEditorPanel.add(beansHeaderEditor.getComponent(), DEFAULT_CONSTRAINTS);
         beansFooterEditorPanel.add(beansFooterEditor.getComponent(), DEFAULT_CONSTRAINTS);
         customBeansMappingEditorPanel.add(customBeansMappingEditor.getComponent(), DEFAULT_CONSTRAINTS);
         excludeBeansEditorPanel.add(excludeBeansEditor.getComponent(), DEFAULT_CONSTRAINTS);
     }
 
     private Editor createEditor(final boolean isViewer, final boolean isTemplate, final FileType fileType) {
         final EditorFactory editorFactory = EditorFactory.getInstance();
         final Document document = editorFactory.createDocument("");
 
         final Editor editor = editorFactory.createEditor(document, null, fileType, isViewer);
         editor.getSettings().setLineNumbersShown(false);
         editor.getSettings().setVirtualSpace(false);
         editor.getSettings().setWhitespacesShown(true);
         editor.getSettings().setAdditionalLinesCount(0);
 
         if (isTemplate) {
             final TemplateContext contextByType = new TemplateContext();
             contextByType.setEnabled(new JavaCodeContextType.Statement(), true);
             TemplateEditorUtil.setHighlighter(editor, contextByType);
         }
 
         return editor;
     }
 
     private void setTextFafety(final Editor editor, final String text) {
        final JComponent component = editor.getComponent();
        final Dimension oldPrefSize = component.getPreferredSize();
        final int caretOffset = editor.getCaretModel().getOffset();
        editor.getDocument().setText(text);
        editor.getCaretModel().moveToOffset(Math.min(caretOffset, text.length()));
        component.setPreferredSize(oldPrefSize);
     }
 
     public void loadCurrentProperties(final XProperties properties) {
         if (properties != null) {
             setTextFafety(beansHeaderEditor, properties.getBeansHeader());
             setTextFafety(beansFooterEditor, properties.getBeansFooter());
 
             selectScopes(properties);
             setTextFafety(excludeBeansEditor, Su.join(properties.getExcludeBeans(), "\n"));
             setTextFafety(priorityFilePathsEditor, Su.join(properties.getPriorityPaths(), "\n"));
 
             selectConflictPolicity(properties);
             customBeansMappingCheckbox.setSelected(properties.isCustomBeansMappingUsed());
             excludeBeansCheckbox.setSelected(properties.isExcludeBeansUsed());
             onlyVCSFilesCheckbox.setSelected(properties.isOnlyVcsFiles());
             setTextFafety(customBeansMappingEditor, properties.getCustomBeansMapping());
 
         }
     }
 
     private void selectConflictPolicity(final XProperties properties) {
         final ConflictsPolicity policity = properties.getConflictsPolicity();
         switch (policity) {
             case AUTO_ALL:
                 conflictsAutoAllRadBut.setSelected(true);
                 break;
             case AUTO_ONE:
                 conflictsAutoOneRadBut.setSelected(true);
                 break;
             case MANUAL_SELECT:
                 conflictsManualSelectRadBut.setSelected(true);
                 break;
         }
     }
 
     private void selectScopes(final XProperties properties) {
         final Set<SearchScopeEnum> scopes = properties.getSearchScope();
         productionScope.setSelected(scopes.contains(SearchScopeEnum.PRODUCTION));
         librariesScope.setSelected(scopes.contains(SearchScopeEnum.LIBRARIES));
         testScope.setSelected(scopes.contains(SearchScopeEnum.TEST));
     }
 
     public void saveCurrentSettings(final XProperties service) {
         service.setBeansHeader(beansHeaderEditor.getDocument().getText());
         service.setBeansFooter(beansFooterEditor.getDocument().getText());
         service.setExcludeBeans(Cf.list(excludeBeansEditor.getDocument().getText().split("\\s")));
         service.setPriorityPaths(Cf.list(priorityFilePathsEditor.getDocument().getText().split("\\s")));
         service.setSearchScope(getSelectedScopes());
         setConflictsPolicityIfExists(service);
         service.setCustomBeansMappingUsed(customBeansMappingCheckbox.isSelected());
         service.setExcludeBeansUsed(excludeBeansCheckbox.isSelected());
         service.setOnlyVcsFiles(onlyVCSFilesCheckbox.isSelected());
         service.setCustomBeansMapping(customBeansMappingEditor.getDocument().getText());
     }
 
     private void setConflictsPolicityIfExists(final XProperties service) {
         final ConflictsPolicity selectedPolicity = getSelectedPolicity();
         if (selectedPolicity != null) {
             service.setConflictsPolicity(selectedPolicity);
         }
     }
 
     @Nullable
     private ConflictsPolicity getSelectedPolicity() {
         if (conflictsAutoAllRadBut.isSelected()) {
             return ConflictsPolicity.AUTO_ALL;
         }
 
         if (conflictsAutoOneRadBut.isSelected()) {
             return ConflictsPolicity.AUTO_ONE;
         }
 
         if (conflictsManualSelectRadBut.isSelected()) {
             return ConflictsPolicity.MANUAL_SELECT;
         }
         return null;
     }
 
     private Set<SearchScopeEnum> getSelectedScopes() {
         final Set<SearchScopeEnum> out = Cf.newSet();
         if (productionScope.isSelected()) {
             out.add(SearchScopeEnum.PRODUCTION);
         }
 
         if (testScope.isSelected()) {
             out.add(SearchScopeEnum.TEST);
         }
 
         if (librariesScope.isSelected()) {
             out.add(SearchScopeEnum.LIBRARIES);
         }
 
         return out;
     }
 
 
     public boolean isModified(final XProperties state) {
         final XProperties currentState = state.getDefaultInstance();
         saveCurrentSettings(currentState);
         return !state.equals(currentState);
     }
 }
