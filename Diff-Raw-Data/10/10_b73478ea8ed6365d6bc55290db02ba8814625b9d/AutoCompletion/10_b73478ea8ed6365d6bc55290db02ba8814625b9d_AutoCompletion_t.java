 import javax.swing.*;
 import javax.swing.plaf.basic.BasicComboPopup;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.PlainDocument;
 import java.awt.event.*;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 /*
  * Barak Harizi: Auto completion for combo box that searches not only by prefix,
  * but by all the sub strings possible matching the pattern.
  */
 public class AutoCompletion extends PlainDocument
 {
 	private static final String EDITOR_PROP_NAME = "editor";
 	private static final String MODEL_PROP_NAME = "model";
 
 	private BasicComboPopup popup;
 	private JComboBox comboBox;
 	private ComboBoxModel model;
 	private JTextComponent editor;
 	// flag to indicate if setSelectedItem has been called
 	// subsequent calls to remove/insertString should be ignored
 	private boolean selecting=false;
 	private boolean hitBackspace=false;
 
 	private KeyListener editorKeyListener;
 	private FocusListener editorFocusListener;
 	private MouseListener popupListMouseListener;
 
 	private String pattern = "";
 
 	public AutoCompletion(final JComboBox comboBox)
 	{
 		this.comboBox = comboBox;
 		popup = (BasicComboPopup)comboBox.getAccessibleContext().getAccessibleChild(0);
 		model = comboBox.getModel();
 
 		comboBox.addPropertyChangeListener(new PropertyChangeListener()
 		{
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				if (e.getPropertyName().equals(EDITOR_PROP_NAME)) configureEditor((ComboBoxEditor) e.getNewValue());
 				if (e.getPropertyName().equals(MODEL_PROP_NAME)) model = (ComboBoxModel) e.getNewValue();
 			}
 		});
 
 		editorKeyListener = new KeyAdapter()
 		{
 			public void keyPressed(KeyEvent e)
 			{
 				if (comboBox.isDisplayable()) comboBox.setPopupVisible(true);
 				hitBackspace=false;
 				switch (e.getKeyCode())
 				{
 					// determine if the pressed key is backspace (needed by the remove method)
 					case KeyEvent.VK_BACK_SPACE :
 						hitBackspace=true;
 						break;
 					case KeyEvent.VK_DOWN:
 					case KeyEvent.VK_UP:
 						if (comboBox.isPopupVisible())
 						{
 							setPopupListSelectedItemByKeyEvent(e);
 						}
 						e.consume();
 						break;
 					// ignore keys
 					case KeyEvent.VK_A:
 						if (!e.isControlDown())
 						{
 							pattern += e.getKeyChar();
 							break;
 						}
 					case KeyEvent.VK_RIGHT :
 					case KeyEvent.VK_LEFT :
 					case KeyEvent.VK_DELETE :
 						e.consume();
 						comboBox.getToolkit().beep();
 						break;
 					default:
 						if (isValidChar(e.getKeyChar()))
 						{
 							pattern += e.getKeyChar();
 						}
 				}
 			}
 		};
 
 		editorFocusListener = new FocusAdapter()
 		{
 			public void focusGained(FocusEvent e)
 			{
 				editor.setCaretPosition(0);
 				pattern = "";
 			}
 			public void focusLost(FocusEvent e)
 			{
 				pattern = "";
 			}
 		};
 
 		configureEditor(comboBox.getEditor());
 
 		// Handle initially selected object
 		if (comboBox.getSelectedItem() != null) setText(comboBox.getSelectedItem().toString());
 	}
 
 	private void setPopupListSelectedItemByKeyEvent(KeyEvent keyEvent)
 	{
 		int selectedIndex = popup.getList().getSelectedIndex();
 		if (!pattern.isEmpty())
 		{
 			// If the key pressed is DOWN
 			if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN)
 			{
 				for (int i = selectedIndex + 1; i < model.getSize(); i++)
 				{
 					if (isCurrentItemMatchesPattern(i)) return;
 
 					if (i == model.getSize() - 1)
 					{
 						i = -1;
 					}
 				}
 			}
 			// The key pressed is UP
 			else
 			{
 				selectedIndex = (selectedIndex  == -1) ? model.getSize() : selectedIndex;
 				for (int i = selectedIndex - 1; i >= 0; i--)
 				{
 					if (isCurrentItemMatchesPattern(i)) return;
 
 					if (i == 0)
 					{
 						i = model.getSize();
 					}
 				}
 			}
 		}
 		else
 		{
 			if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN)
 			{
 				comboBox.setSelectedIndex((selectedIndex == model.getSize() - 1) ? 0 : selectedIndex + 1);
 			}
 			else
 			{
 				comboBox.setSelectedIndex((selectedIndex == 0) ? model.getSize() - 1 : selectedIndex - 1);
 			}
 		}
 	}
 
 	private boolean isCurrentItemMatchesPattern(int i)
 	{
 		Object currentItem = model.getElementAt(i);
 
 		// current item contains the pattern
 		if (currentItem != null && currentItem.toString().contains(pattern))
 		{
 			setSelectedItem(currentItem);
 			setText(currentItem.toString());
 			highlightByPattern(currentItem);
 			return true;
 		}
 		return false;
 	}
 
 	void configureEditor(ComboBoxEditor newEditor)
 	{
 		if (editor != null)
 		{
 			editor.removeKeyListener(editorKeyListener);
 			editor.removeFocusListener(editorFocusListener);
 		}
 
 		if (newEditor != null)
 		{
 			editor = (JTextComponent) newEditor.getEditorComponent();
 			editor.addKeyListener(editorKeyListener);
 			editor.addFocusListener(editorFocusListener);
 			editor.setDocument(this);
 			handleMouseEvents();
 		}
 	}
 
 	private void handleMouseEvents()
 	{
 		int length = editor.getMouseListeners().length;
 		for (int i = 0; i < length; i++)
 		{
 			editor.removeMouseListener(editor.getMouseListeners()[0]);
 		}
 
 		editor.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mousePressed(MouseEvent e)
 			{
 				comboBox.grabFocus();
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e)
 			{
 				if (editor.getSelectionStart() != editor.getSelectionEnd())
 				{
 					editor.setCaretPosition(0);
 				}
 			}
 		});
 
 		if (popupListMouseListener != null)
 		{
 			popup.getList().removeMouseListener(popupListMouseListener);
 		}
 		popupListMouseListener = new MouseAdapter()
 		{
 			@Override
 			public void mousePressed(MouseEvent e)
 			{
 				Object selectedValue = popup.getList().getSelectedValue();
 				if (selectedValue != null)
 				{
 					pattern = "";
 					setSelectedItem(selectedValue);
 					setText(selectedValue.toString());
 					editor.setCaretPosition(0);
 					e.consume();
 				}
 			}
 		};
 		popup.getList().addMouseListener(popupListMouseListener);
 	}
 
 	public void remove(int offs, int len) throws BadLocationException
 	{
 		// return immediately when selecting an item
 		if (selecting) return;
 
 		if (hitBackspace)
 		{
			if (editor.getSelectionStart() == editor.getSelectionEnd())
			{
				comboBox.getToolkit().beep();
				return;
			}
 			if (editor.getCaretPosition() > 0)
 			{
 				if (!pattern.isEmpty()) pattern = pattern.substring(0, pattern.length() - 1);
 				editor.moveCaretPosition(editor.getCaretPosition() - 1);
 			}
 			else
 			{
 				comboBox.getToolkit().beep();
 			}
 		}
 	}
 
 	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
 	{
 		// return immediately when selecting an item
 		if (selecting) return;
 
 		// lookup and select a matching item
 		Object item = lookupItem(pattern);
 
 		if (item != null)
 		{
 			setSelectedItem(item);
 			setText(item.toString());
 			highlightByPattern(item);
 		}
 		else
 		{
 			pattern = pattern.substring(0, pattern.length() - 1);
 			comboBox.getToolkit().beep();
 		}
 	}
 
 	private void highlightByPattern(Object item)
 	{
 		int indexPattern = item.toString().indexOf(pattern);
 		editor.select(indexPattern, indexPattern+pattern.length());
 	}
 
 	private void setText(String text)
 	{
 		try
 		{
 			// remove all text and insert the completed string
 			super.remove(0, getLength());
 			super.insertString(0, text, null);
 		}
 		catch (BadLocationException e)
 		{
 			throw new RuntimeException(e.toString());
 		}
 	}
 
 	private void setSelectedItem(Object item)
 	{
 		selecting = true;
 		model.setSelectedItem(item);
 		selecting = false;
 	}
 
 	private Object lookupItem(String pattern)
 	{
 		Object selectedItem = model.getSelectedItem();
 		// only search for a different item if the currently selected does not match
 		if (selectedItem != null && selectedItem.toString().contains(pattern))
 		{
 			return selectedItem;
 		}
 		else
 		{
 			// iterate over all items
 			for (int i = 0, n = model.getSize(); i < n; i++)
 			{
 				Object currentItem = model.getElementAt(i);
 				// current item contains the pattern?
 				if (currentItem != null && currentItem.toString().contains(pattern))
 				{
 					return currentItem;
 				}
 			}
 		}
 		// no item contains the pattern => return null
 		return null;
 	}
 
 	public boolean isValidChar(char c)
 	{
		return (Character.isAlphabetic(c) || Character.isDigit(c) || Character.isSpaceChar(c) || c == '"');
 	}
 
 	public static void enable(JComboBox comboBox)
 	{
 		// has to be editable
 		comboBox.setEditable(true);
 		// change the editor's document
 		new AutoCompletion(comboBox);
 	}
 }
