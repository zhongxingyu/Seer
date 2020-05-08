 package gui;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 
 public class EditableTextField extends HorizontalFieldManager {
 
 	private LabelField _label;
 	private BasicEditField _txtField;
 	private LabelField _lblField;
 	private int _color = 0x00757575;
 	private boolean _editable = false;
 
 	public EditableTextField() {
 		this(0);
 	}
 
 	public EditableTextField(long style) {
 		_txtField = new BasicEditField(style);
 		_lblField = new LabelField("", Field.FOCUSABLE) {
 			protected void paint(Graphics g) {
 				g.setColor(_color);
 				super.paint(g);
 			}
 		};
 		_label = new LabelField();
 		this.add(_label);
 		this.add(_lblField);
 	}
 
 	public EditableTextField(String label, String initialValue, long style) {
 		this(style);
 		_label.setText(label);
 		if (initialValue.equals("") || initialValue == null) {
 			setEditable();
 		}
 		setText(initialValue);
 	}
 
 	public EditableTextField(String label) {
 		this(label, "", 0);
 	}
 
 	public EditableTextField(String label, long style) {
 		this(label, "", style);
 	}
 
 	public EditableTextField(String label, String initialValue) {
 		this(label, initialValue, 0);
 	}
 
 	public void setLabel(String label) {
 		_label.setText(label);
 	}
 
 	public void setText(String text) {
 		_txtField.setText(text);
 		_lblField.setText(text);
 		if (text.length() != 0) {
 			setEditable(false);
 		}
 	}
 
 	public void setEditableColor(int color) {
 		if (_color != color) {
 			_color = color;
 			long style = _txtField.getStyle();
 			String text = _txtField.getText();
 			this.delete(_txtField);
 			_txtField = new BasicEditField(style) {
 				protected void paint(Graphics g) {
 					g.setColor(_color);
 					super.paint(g);
 				}
 			};
 			_txtField.setText(text);
 			_txtField.setEditable(false);
 			this.add(_txtField);
 		}
 	}
 
 	public String getText() {
 		if (_editable) {
 			return _txtField.getText();
 		} else {
 			return _lblField.getText();
 		}
 	}
 
 	public int getTextLength() {
 		if (_editable) {
 			return _txtField.getTextLength();
 		} else {
 			return _lblField.getText().length();
 		}
 	}
 
 	public void setEditable() {
		if (!_editable) {
			this.replace(_lblField, _txtField);
			_editable = true;
		}
 	}
 
 	public void setMaxSize(int maxSize) {
 		_txtField.setMaxSize(maxSize);
 	}
 
 	public void setEditable(boolean editable) {
 		if (editable) {
 			setEditable();
 		} else {
 			if (!this.getField(1).equals(_lblField)) {
 				this.replace(_txtField, _lblField);
 				_editable = false;
 			}
 		}
 	}
 }
