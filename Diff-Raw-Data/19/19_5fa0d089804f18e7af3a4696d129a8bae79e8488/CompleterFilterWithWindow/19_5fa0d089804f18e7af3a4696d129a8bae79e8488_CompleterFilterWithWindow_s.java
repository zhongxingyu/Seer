 package de.cismet.tools.gui.autocomplete;
 
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JWindow;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 
 public class CompleterFilterWithWindow extends CompleterFilter {
 
     public CompleterFilterWithWindow(Object[] completerObjs, JTextField textField) {
         super(completerObjs, textField);
         _init();
     }
 
     @Override
     public void insertString(FilterBypass filterBypass, int offset, String string, AttributeSet attributeSet) throws BadLocationException {
         setFilterWindowVisible(false);
         super.insertString(filterBypass, offset, string, attributeSet);
     }
 
     @Override
     public void remove(FilterBypass filterBypass, int offset, int length) throws BadLocationException {
         setFilterWindowVisible(false);
         super.remove(filterBypass, offset, length);
     }
 
     @Override
     public void replace(FilterBypass filterBypass, int offset, int length, String string, AttributeSet attributeSet) throws BadLocationException {
         if (isAdjusting) {
             filterBypass.replace(offset, length, string, attributeSet);
             return;
         }
 
         super.replace(filterBypass, offset, length, string, attributeSet);
 
         if (getLeadingSelectedIndex() == -1) {
             if (isFilterWindowVisible()) {
                 setFilterWindowVisible(false);
             }
 
             return;
         }
 
         lm.setFilter(preText);
 
         if (!isFilterWindowVisible()) {
             setFilterWindowVisible(true);
         } else {
             _setWindowHeight();
         }
 
         list.setSelectedValue(textField.getText(), true);
     }
 
     private void _init() {
         fwl = new FilterWindowListener();
         lm = new FilterListModel(objectList);
         tfkl = new TextFieldKeyListener();
         textField.addKeyListener(tfkl);
 
         EscapeAction escape = new EscapeAction();
         textField.registerKeyboardAction(escape, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                 JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
     }
 
     public boolean isFilterWindowVisible() {
         return ((win != null) && (win.isVisible()));
     }
 
     @Override
     public void setCaseSensitive(boolean caseSensitive) {
         super.setCaseSensitive(caseSensitive);
         lm.setCaseSensitive(caseSensitive);
     }
 
     public void setFilterWindowVisible(boolean visible) {
         if (visible) {
             _initWindow();
             list.setModel(lm);
             win.setVisible(true);
             textField.requestFocus();
             textField.addFocusListener(fwl);
         } else {
             if (win == null) {
                 return;
             }
 
             win.setVisible(false);
             win.removeFocusListener(fwl);
             Window ancestor = SwingUtilities.getWindowAncestor(textField);
             ancestor.removeMouseListener(fwl);
             textField.removeFocusListener(fwl);
             textField.removeAncestorListener(fwl);
             list.removeMouseListener(lml);
             list.removeListSelectionListener(lsl);
             lsl = null;
             lml = null;
             win.dispose();
             win = null;
             list = null;
         }
     }
 
     private void _initWindow() {
         Window ancestor = SwingUtilities.getWindowAncestor(textField);
         win = new JWindow(ancestor);
         win.addWindowFocusListener(fwl);
         textField.addAncestorListener(fwl);
         ancestor.addMouseListener(fwl);
         lsl = new ListSelListener();
         lml = new ListMouseListener();
 
         list = new JList(lm);
         list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         list.setFocusable(false);
        list.setPrototypeCellValue("Prototype");
         list.addListSelectionListener(lsl);
         list.addMouseListener(lml);
 
         sp = new JScrollPane(list,
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setFocusable(false);
         sp.getVerticalScrollBar().setFocusable(false);
 
         _setWindowHeight();
         win.setLocation(textField.getLocationOnScreen().x, textField.getLocationOnScreen().y + textField.getHeight());
         win.getContentPane().add(sp);
     }
 
     private void _setWindowHeight() {
         int height = list.getFixedCellHeight() * Math.min(MAX_VISIBLE_ROWS, lm.getSize());
         height += list.getInsets().top + list.getInsets().bottom;
         height += sp.getInsets().top + sp.getInsets().bottom;
 
         win.setSize(textField.getWidth(), height);
         sp.setSize(textField.getWidth(), height); // bottom border fails to draw without this
     }
 
     @Override
     public void setCompleterMatches(Object[] objectsToMatch) {
         if (isFilterWindowVisible()) {
             setFilterWindowVisible(false);
         }
 
         super.setCompleterMatches(objectsToMatch);
         lm.setCompleterMatches(objectsToMatch);
     }
 
     class EscapeAction extends AbstractAction {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             if (isFilterWindowVisible()) {
                 setFilterWindowVisible(false);
             }
         }
     }
 
     private class FilterWindowListener extends MouseAdapter
             implements AncestorListener, FocusListener, WindowFocusListener {
 
         @Override
         public void ancestorMoved(AncestorEvent event) {
             setFilterWindowVisible(false);
         }
 
         @Override
         public void ancestorAdded(AncestorEvent event) {
             setFilterWindowVisible(false);
         }
 
         @Override
         public void ancestorRemoved(AncestorEvent event) {
             setFilterWindowVisible(false);
         }
 
         @Override
         public void focusLost(FocusEvent e) {
             if (e.getOppositeComponent() != win) {
                 setFilterWindowVisible(false);
             }
         }
 
         @Override
         public void focusGained(FocusEvent e) {
         }
 
         @Override
         public void windowLostFocus(WindowEvent e) {
             Window w = e.getOppositeWindow();
 
             if (w.getFocusOwner() != textField) {
                 setFilterWindowVisible(false);
             }
         }
 
         @Override
         public void windowGainedFocus(WindowEvent e) {
         }
 
         @Override
         public void mousePressed(MouseEvent e) {
             setFilterWindowVisible(false);
         }
     }
 
     private class TextFieldKeyListener extends KeyAdapter {
 
         @Override
         public void keyPressed(KeyEvent e) {
             if (!((e.getKeyCode() == KeyEvent.VK_DOWN) ||
                     (e.getKeyCode() == KeyEvent.VK_UP) ||
                     ((e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) && (isFilterWindowVisible())) ||
                     ((e.getKeyCode() == KeyEvent.VK_PAGE_UP) && (isFilterWindowVisible())) ||
                     (e.getKeyCode() == KeyEvent.VK_ENTER))) {
                 return;
             }
 
             if ((e.getKeyCode() == KeyEvent.VK_DOWN) && !isFilterWindowVisible()) {
                 preText = textField.getText();
                 lm.setFilter(preText);
 
                 if (lm.getSize() > 0) {
                     setFilterWindowVisible(true);
                 } else {
                     return;
                 }
             }
 
             if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                 if (isFilterWindowVisible()) {
                     setFilterWindowVisible(false);
                 }
 
                 textField.setCaretPosition(textField.getText().length());
                 return;
             }
 
             int index = -1;
 
             if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                 index = Math.min(list.getSelectedIndex() + 1, list.getModel().getSize() - 1);
             } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                 index = Math.max(list.getSelectedIndex() - 1, 0);
             } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                 index = Math.max(list.getSelectedIndex() - MAX_VISIBLE_ROWS, 0);
             } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                 index = Math.min(list.getSelectedIndex() + MAX_VISIBLE_ROWS, list.getModel().getSize() - 1);
             }
 
             if (index == -1) {
                 return;
             }
 
             list.setSelectedIndex(index);
             list.scrollRectToVisible(list.getCellBounds(index, index));
         }
     }
 
     private class ListSelListener implements ListSelectionListener {
 
         @Override
         public void valueChanged(ListSelectionEvent e) {
             isAdjusting = true;
             textField.setText(list.getSelectedValue().toString());
             isAdjusting = false;
             textField.select(preText.length(), textField.getText().length());
         }
     }
 
     private class ListMouseListener extends MouseAdapter {
 
         @Override
         public void mouseClicked(MouseEvent e) {
             if (e.getClickCount() == 2) {
                 setFilterWindowVisible(false);
             }
         }
     }
     private FilterWindowListener fwl;
     private JWindow win;
     private TextFieldKeyListener tfkl;
     private ListSelListener lsl;
     private ListMouseListener lml;
     private JList list;
     private JScrollPane sp;
     private FilterListModel lm;
     private boolean isAdjusting = false;
     public static int MAX_VISIBLE_ROWS = 8;
 }
