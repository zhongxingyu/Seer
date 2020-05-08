 /*
  * WaqtSalat, for indicating the muslim prayers times in most cities.
  * Copyright (C) 2011  Papa Issa DIAKHATE (paissad).
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * PLEASE DO NOT REMOVE THIS COPYRIGHT BLOCK.
  * 
  */
 
 package net.waqtsalat.gui.addons;
 
 import java.awt.Graphics;
 import java.awt.Insets;
 
 import javax.swing.Icon;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 
 /**
  * 
  * @author Papa Issa DIAKHATE (<a href="mailto:paissad@gmail.com">paissad</a>)
  */
 public class JIconTextField extends JTextField {
 
 	private static final long serialVersionUID = 1;
 
 	private Icon _icon;
 	private Insets _dummyInsets;
 
 	// =======================================================================
 	// Constructors ...
 
 	/**
 	 * Constructs a new <code>JIconTextField</code>. A default model is created,
 	 * the initial icon is <code>null</code>.
 	 */
 	public JIconTextField() {
 		this(null);
 	}
 
 	/**
 	 * Constructs a new <code>JIconTextField</code> using <i>icon</i>.
 	 * 
 	 * @param icon The <code>Icon</code> to use.
 	 */
 	public JIconTextField(Icon icon) {
 		super();
 		this._icon = icon;
 		Border border = UIManager.getBorder("TextField.border");
 		JTextField dummyJTextField = new JTextField();
		this._dummyInsets = (border != null) ? border.getBorderInsets(dummyJTextField) : new Insets(1, 1, 1, 1);
 		dummyJTextField = null;
 	}
 
 	// =======================================================================
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		int textLeftMargin = 2;
 
 		if (this._icon != null) { // Let's try to get the left margin "size" & nest the icon ...
 			int iconWidth  = _icon.getIconWidth();
 			int iconHeight = _icon.getIconHeight();
 			int x_Icon     = _dummyInsets.left + 5;
 			int y_Icon     = (int) ((this.getHeight() - iconHeight) / 2);
 			textLeftMargin += iconWidth + x_Icon;
 			_icon.paintIcon(this, g, x_Icon, y_Icon);
 		}
 
 		this.setColumns(10);
 		this.setMargin(new Insets(2, textLeftMargin, 2, 2));
 	}
 
 	// =======================================================================
 	// Getters / Setters ...
 
 	/**
 	 * Set the icon to use for this <code>JIconTextField</code>.
 	 * 
 	 * @param icon
 	 *            The icon to use.
 	 */
 	public void setIcon(Icon icon) {
 		this._icon = icon;
 		this.repaint();
 	}
 
 	/**
 	 * Get the icon used for this <code>JIconTextField</code>.
 	 * 
 	 * @return The icon used for this component.
 	 */
 	public Icon getIcon() {
 		return _icon;
 	}
 
 	// =======================================================================
 
 }
