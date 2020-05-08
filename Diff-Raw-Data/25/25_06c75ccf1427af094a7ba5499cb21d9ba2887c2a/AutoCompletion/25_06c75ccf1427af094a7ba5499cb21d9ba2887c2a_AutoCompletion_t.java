 import javax.swing.*;
 import javax.swing.plaf.basic.BasicComboPopup;
 import javax.swing.text.JTextComponent;
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 /*
  * Barak Harizi: Auto completion for combo box that searches not only by prefix,
  * but by all the sub strings possible matching the pattern.
  */
 public class AutoCompletion
 {
 	private static final String EDITOR_PROP_NAME = "editor";
 	private static final String MODEL_PROP_NAME = "model";
 	private static final String SPECIAL_CHARS = "`~!@#$%^&*()_-+=[]{}|\\\"':;?/.>,<";
	private static final Color FOCUS_COLOR = new Color(202, 255, 201);
 
 	private BasicComboPopup popup;
 	private JComboBox comboBox;
 	private ComboBoxModel model;
 	private JTextComponent editor;
 
 	private KeyListener editorKeyListener;
 	private FocusListener editorFocusListener;
 	private MouseListener popupListMouseListener;
 
 	private String pattern = "";
 	private Color initialColor;
 
 	public AutoCompletion(final JComboBox comboBox)
 	{
 		this.comboBox = comboBox;
 		popup = (BasicComboPopup)comboBox.getAccessibleContext().getAccessibleChild(0);
 		model = comboBox.getModel();
 
 		comboBox.addPropertyChangeListener(getComboBoxPropListener());
 
 		editorKeyListener = getEditorKeyAdapter(comboBox);
 		editorFocusListener = getEditorFocusAdapter();
 		configureEditor(comboBox.getEditor());
 	}
 
 	private FocusAdapter getEditorFocusAdapter()
 	{
 		return new FocusAdapter()
 		{
 			public void focusGained(FocusEvent e)
 			{
 				initialColor = editor.getBackground();
 				if (editor.isEnabled())
 				{
 					editor.setCaretPosition(0);
 					hideCaret();
					changeEditorBackground(FOCUS_COLOR);
 					pattern = "";
 				}
 			}
 			public void focusLost(FocusEvent e)
 			{
				changeEditorBackground(initialColor);
 				pattern = "";
 			}
 		};
 	}
 
 	private KeyAdapter getEditorKeyAdapter(final JComboBox comboBox)
 	{
 		return new KeyAdapter()
 		{
 			public void keyPressed(KeyEvent e)
 			{
 				if (comboBox.isDisplayable()) comboBox.setPopupVisible(true);
 				switch (e.getKeyCode())
 				{
 					case KeyEvent.VK_BACK_SPACE :
 						deleteByBackspace();
 						e.consume();
 						return;
 					case KeyEvent.VK_DOWN:
 					case KeyEvent.VK_UP:
 						if (comboBox.isPopupVisible())
 						{
 							setPopupListSelectedItemByKeyEvent(e);
 						}
 						e.consume();
 						return;
 					// ignore keys
 					case KeyEvent.VK_V:
 					case KeyEvent.VK_A:
 						if (!e.isControlDown())
 						{
 							addCharToPattern(e.getKeyChar());
 							return;
 						}
 					default:
 						if (isValidChar(e.getKeyChar()))
 						{
 							addCharToPattern(e.getKeyChar());
 						}
 				}
 				if (e.getKeyCode() != KeyEvent.VK_ESCAPE && e.getKeyCode() != KeyEvent.VK_ENTER)
 				{
 					e.consume();
 				}
 			}
 
 			@Override
 			public void keyTyped(KeyEvent e)
 			{
 				e.consume();
 			}
 		};
 	}
 
 	private PropertyChangeListener getComboBoxPropListener()
 	{
 		return new PropertyChangeListener()
 		{
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				if (e.getPropertyName().equals(EDITOR_PROP_NAME)) configureEditor((ComboBoxEditor) e.getNewValue());
 				if (e.getPropertyName().equals(MODEL_PROP_NAME)) model = (ComboBoxModel) e.getNewValue();
 			}
 		};
 	}
 
 	private void setPopupListSelectedItemByKeyEvent(KeyEvent keyEvent)
 	{
 		int selectedIndex = popup.getList().getSelectedIndex();
 		if (!pattern.isEmpty())
 		{
 			// If the key pressed is DOWN
 			if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN)
 			{
 				selectedIndex = (selectedIndex  == model.getSize() - 1) ? 0 : selectedIndex;
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
 				selectedIndex = (selectedIndex  == 0) ? model.getSize() : selectedIndex;
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
 			if (selectedIndex == -1)
 			{
 				comboBox.setSelectedIndex(keyEvent.getKeyCode() == KeyEvent.VK_DOWN ? 0 : model.getSize() - 1);
 				return;
 			}
 
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
 			model.setSelectedItem(currentItem);
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
 			handleMouseEvents();
 		}
 	}
 
	private void changeEditorBackground(Color c)
	{
		PropertyChangeListener[] savedPropertyChangeListeners = editor.getPropertyChangeListeners();
		for (PropertyChangeListener listener : savedPropertyChangeListeners)
		{
			editor.removePropertyChangeListener(listener);
		}

		editor.setBackground(c);

		for (PropertyChangeListener listener : savedPropertyChangeListeners)
		{
			editor.addPropertyChangeListener(listener);
		}
	}

 	private void handleMouseEvents()
 	{
 		for (MouseMotionListener mouseMotionListener : editor.getMouseMotionListeners())
 		{
 			editor.removeMouseMotionListener(mouseMotionListener);
 		}
 
 		for (MouseListener mouseListener : editor.getMouseListeners())
 		{
 			editor.removeMouseListener(mouseListener);
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
 				hideCaret();
 			}
 		});
 
 		if (popupListMouseListener != null)
 		{
 			popup.getList().removeMouseListener(popupListMouseListener);
 		}
 		popupListMouseListener = new MouseAdapter()
 		{
 			@Override
 			public void mouseReleased(MouseEvent e)
 			{
 				Object selectedValue = popup.getList().getSelectedValue();
 				if (selectedValue != null)
 				{
 					pattern = "";
 					model.setSelectedItem(selectedValue);
 					editor.setCaretPosition(0);
 					e.consume();
 				}
 			}
 		};
 		popup.getList().addMouseListener(popupListMouseListener);
 	}
 
 	private void hideCaret()
 	{
 		editor.getCaret().setVisible(false);
 	}
 
 	public void deleteByBackspace()
 	{
 		if (!pattern.isEmpty())
 		{
 		 	pattern = pattern.substring(0, pattern.length() - 1);
 			editor.moveCaretPosition(editor.getCaretPosition() - 1);
 		}
 		else
 		{
 			comboBox.getToolkit().beep();
 		}
 	}
 
 	public void addCharToPattern(char c)
 	{
 		// lookup and select a matching item
 		Object item = lookupItem(pattern + c);
 
 		if (item != null)
 		{
 			pattern += c;
 			model.setSelectedItem(item);
 			highlightByPattern(item);
 		}
 		else
 		{
 			comboBox.getToolkit().beep();
 		}
 	}
 
 	private void highlightByPattern(Object item)
 	{
 		int indexPattern = item.toString().indexOf(pattern);
 		editor.select(indexPattern, indexPattern+pattern.length());
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
 		return (Character.isLetterOrDigit(c) || Character.isSpaceChar(c) || SPECIAL_CHARS.indexOf(c) != -1);
 	}
 
 	public static void enable(JComboBox comboBox)
 	{
 		// has to be editable
 		comboBox.setEditable(true);
 		// change the editor's document
 		new AutoCompletion(comboBox);
 	}
 }
