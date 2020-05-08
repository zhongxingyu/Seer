 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.uidescription;
 
 import net.sf.okapi.common.ParameterDescriptor;
 
 /**
  * UI part descriptor for a string selection. This UI part supports the following
  * types: String and int.
  * <p>Use {@link #setListType(int)} to specify the type of list the UI should use. By default
  * a simple list box will be used.
  */
 public class ListSelectionPart extends AbstractPart {
 	
 	public static final int LISTTYPE_SIMPLE = 0;
 	public static final int LISTTYPE_DROPDOWN = 1;
 
 	private String[] choicesValues;
 	private int listType = LISTTYPE_SIMPLE;
 	private String[] choicesLabels;
 	
 	/**
 	 * Creates a new ListSelectionPart object with a given parameter descriptor.
 	 * @param paramDescriptor the parameter descriptor for this UI part.
 	 * @param choicesValues the list of the items that can be selected. When the type of the parameter
	 * is an int, the first value return 0, the next 1, and so forth (regardless of their actual text. 
 	 */
 	public ListSelectionPart (ParameterDescriptor paramDescriptor,
 		String[] choicesValues)
 	{
 		super(paramDescriptor);
 		setChoicesValues(choicesValues);
 	}
 
 	@Override
 	protected void checkType () {
 		// Check type support
 		if ( getType().equals(String.class) ) return;
 		if ( getType().equals(int.class) ) return;
 		// Otherwise: call the base method.
 		super.checkType();
 	}
 
 	/**
 	 * Gets the list of items that can be selected.
 	 * @return the list of items that can be selected.
 	 */
 	public String[] getChoicesValues () {
 		return choicesValues;
 	}
 
 	/**
 	 * Sets the list of items that can be selected.
 	 * @param choicesValues the new list of items that can be selected.
 	 */
 	public void setChoicesValues (String[] choicesValues) {
 		this.choicesValues = choicesValues;
 	}
 
 	/**
 	 * Gets the type of list this UI part should use.
 	 * @return the type of list this UI part should use.
 	 */
 	public int getListType () {
 		return listType;
 	}
 
 	/**
 	 * Sets the type of list this UI part should use.
 	 * <p>The possible values are:
 	 * <ul><li>{@link ListSelectionPart#LISTTYPE_SIMPLE} for a a simple list</li>
 	 * <li> {@link ListSelectionPart#LISTTYPE_DROPDOWN} for a drop-down list</li></ul>
 	 * @param listType the new type of list this UI part should use.
 	 */
 	public void setListType (int listType) {
 		this.listType = listType;
 	}
 
 	/**
 	 * Gets the list of the localizable labels to use with the selectable values.
 	 * @return the list of the localizable labels to use with the selectable values.
 	 */
 	public String[] getChoicesLabels () {
 		return choicesLabels;
 	}
 
 	/**
 	 * Sets the list of the localizable labels to use with the selectable values. If this list
 	 * is not set, the values themselves will be used for display.
 	 * @param choicesLabels the list of the localizable labels to use with the selectable values.
 	 */
 	public void setChoicesLabels (String[] choicesLabels) {
 		this.choicesLabels = choicesLabels;
 	}
 
 }
