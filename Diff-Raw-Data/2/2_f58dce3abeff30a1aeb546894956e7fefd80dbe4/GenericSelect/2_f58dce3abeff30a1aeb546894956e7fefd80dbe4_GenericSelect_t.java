 package com.idega.presentation.ui;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import com.idega.presentation.IWContext;
 import com.idega.util.text.TextSoap;
 
 /**
  * @author laddi
  *
  * To change this generated comment edit the template variable "typecomment":
  * Window>Preferences>Java>Templates.
  * To enable and disable the creation of type comments go to
  * Window>Preferences>Java>Code Generation.
  */
 public class GenericSelect extends InterfaceObject {
 
 	private boolean _isSetToSubmit;
 	private boolean _isSetToDisable;
 	private boolean _isSetAsNotEmpty;
 	private String _notEmptyErrorMessage;
 	private String _emptyValue;
 
 	private List theElements;
 	private List selectedElements;
 	private boolean _allSelected = false;
 
 	/**
 	 * Creates a new <code>GenericSelect</code> with the name "undefined".
 	 */
 	public GenericSelect() {
 		this("undefined");
 	}
 
 	/**
 	 * Creates a new <code>GenericSelect</code> with the given name.
 	 * @param name	The name of the <code>GenericSelect</code> object.
 	 */
 	public GenericSelect(String name) {
 		setName(name);
 		theElements = new ArrayList();
 		selectedElements = new ArrayList();
 	}
 
 	/**
 	 * Removes all <code>SelectOption</code> objects from the select object.
 	 */
 	public void removeElements() {
 		theElements.clear();
 	}
 
 	/**
 	 * Sets the select to submit automatically.
 	 * Must add to a form before this function is used!!!!
 	 */
 	public void setToSubmit() {
 		setToSubmit(true);
 	}
 
 	/**
 	 * Sets the select to submit automatically.
 	 * Must add to a form before this function is used!!!!
 	 */
 	public void setToSubmit(boolean setToSubmit) {
 		if (setToSubmit)
			setOnChange("this.form.submit()");
 		else
 			removeAttribute("onChange");
 	}
 
 	/**
 	 * Sets the select to submit when the specified option is seleced.
 	 * Must add to a form before this function is used!!!!
 	 */
 	public void setToSubmit(String optionValue) {
 		_isSetToSubmit = true;
 		setOnChange("submitWhenSelected(this,'"+optionValue+"')");
 	}
 
 	/**
 	 * Adds a <code>SelectOption</code> to the select object.
 	 * @param option	The <code>SelectOption</code> to add.
 	 */
 	public void addOption(SelectOption option) {
 		theElements.add(option);
 		if (option.getSelected())
 			setSelectedOption(option.getValue());
 	}
 
 	/**
 	 * Adds a <code>SelectOption</code> to the select object.
 	 * @param option	The <code>SelectOption</code> to add.
 	 */
 	public void setSelectOption(SelectOption option) {
 		addOption(option);
 	}
 
 	/**
 	 * Adds a <code>SelectOption</code> to the select object as the first option.
 	 * @param option	The <code>SelectOption</code> to add.
 	 */
 	public void addFirstOption(SelectOption option) {
 		theElements.add(0, option);
 		if (option.getSelected())
 			setSelectedOption(option.getValue());
 	}
 	
 	/**
 	 * Adds a <code>SelectOption</code> to the select object as the first option.
 	 * @param option	The <code>SelectOption</code> to add.
 	 */
 	public void setFirstSelectOption(SelectOption option) {
 		addFirstOption(option);
 	}
 	
 	/**
 	 * @deprecated Use getOptionCount() instead.
 	 * Returns the number of <code>SelectOption</code> objects added to this <code>GenericSelect</code>.
 	 * @return
 	 */
 	public int getOptionCount() {
 		return theElements.size();
 	}
 
 	/**
 	 * Adds a disabled <code>SelectOption</code> to the select object.
 	 * @param option	The disabled <code>SelectOption</code> to add.
 	 */
 	public void addDisabledOption(SelectOption option) {
 		option.setDisabled(true);
 		theElements.add(option);
 		if (option.getSelected())
 			setSelectedOption(option.getValue());
 	}
 
 	/**
 	 * Adds a disabled <code>SelectOption</code> to the select object.
 	 * @param option	The disabled <code>SelectOption</code> to add.
 	 */
 	public void setDisabledSelectOption(SelectOption option) {
 		addDisabledOption(option);
 	}
 
 	/**
 	 * Adds a separator into the select object.
 	 */
 	public void addSeparator() {
 		SelectOption option = new SelectOption("----------------------------", "separator");
 		option.setDisabled(true);
 		addOption(option);
 	}
 
 	/**
 	 * Sets all <code>SelectOption</code> object in the select object as not selected.
 	 */
 	protected void deselectOptions() {
 		selectedElements.clear();
 	}
 
 	/**
 	 * Gets the value of the selected <code>SelectOption</code>.
 	 * @return String	The value of the <code>SelectOption</code> in the select object.
 	 */
 	public String getSelectedValue() {
 		Iterator iter = selectedElements.iterator();
 		while (iter.hasNext()) {
 			return (String) iter.next();
 		}
 		return "";
 	}
 
 	/**
 	 * Sets the <code>SelectOption</code> with the given value as selected.  If the select object
 	 * allows multiple values this selected value is added to existing selected values.
 	 * @param value	The value of the <code>SelectOption</code> to set as selected.
 	 */
 	public void setSelectedOption(String value) {
 		if (!getMultiple())
 			deselectOptions();
 		selectedElements.add(value);
 	}
 
 	/**
 	 * Gets the <code>SelectOption</code> with the given value.
 	 * @param value	The value of the <code>SelectOption</code>.
 	 * @return SelectOption
 	 */
 	public SelectOption getOption(String value) {
 		SelectOption theReturn = new SelectOption();
 		Iterator iter = theElements.iterator();
 		while (iter.hasNext()) {
 			SelectOption element = (SelectOption) iter.next();
 			if (element.getValue().equals(value))
 				return element;
 		}
 		return theReturn;
 	}
 
 	/**
 	 * Sets the name of the <code>SelectOption</code> with the given value.
 	 * @param value	The value of the <code>SelectOption</code>
 	 * @param name	The new name of the <code>SelectOption</code>.
 	 */
 	public void setOptionName(String value, String name) {
 		SelectOption option = getOption(value);
 		option.setName(name);
 	}
 
 	/**
 	 * Sets this select object to allow multiple selections.
 	 * @param multiple	True to allow multiple, false otherwise.
 	 */
 	protected void setMultiple(boolean multiple) {
 		if (multiple)
 			setAttribute("multiple");
 		else
 			removeAttribute("multiple");
 	}
 
 	/**
 	 * Returns if this select object is set to allow multiple selections.
 	 * @return True if allows multiple, false otherwise.
 	 */
 	protected boolean getMultiple() {
 		if (isAttributeSet("multiple"))
 			return true;
 		return false;
 	}
 
 	public void main(IWContext iwc) throws Exception {
 		if (_isSetAsNotEmpty)
 			setOnSubmitFunction("warnIfDropdownEmpty", "function warnIfDropdownEmpty (inputbox,warnMsg,emptyValue) {\n\n		if ( inputbox.options[inputbox.selectedIndex].value == emptyValue ) { \n		alert ( warnMsg );\n		return false;\n	}\n	else{\n		return true;\n}\n\n}", _notEmptyErrorMessage, _emptyValue);
 		if (_isSetToDisable)
 			getScript().addFunction("disableObjectByDropdown", "function disableObjectByDropdown (dropdown,inputs,value,selectedValue) {\n	if (dropdown.options[dropdown.selectedIndex].value == eval(selectedValue)) {\n \tif (inputs.length > 1) {\n	\t\tfor(var i=0;i<inputs.length;i++)\n	\t\t\tinputs[i].disabled=eval(value);\n	\t\t}\n	\t\tinputs.disabled=eval(value);\n}\nelse {\n\tif (inputs.length > 1) {\n	\t\tfor(var i=0;i<inputs.length;i++)\n	\t\t\tinputs[i].disabled=!eval(value);\n	\t\t}\n	\t\tinputs.disabled=!eval(value);\n}\n}");
 		if (_isSetToSubmit)
 			getScript().addFunction("submitWhenSelected", "function submitWhenSelected (dropdown,selectedValue) {\n\tif (dropdown.options[dropdown.selectedIndex].value == eval(selectedValue))\n\t\tdropdown.form.submit();\n}");
 	}
 
 	/**
 	 * @see com.idega.presentation.PresentationObject#print(IWContext)
 	 */
 	public void print(IWContext iwc) throws Exception {
 		if (getLanguage().equals("HTML")) {
 			println("<select name=\"" + getName() + "\" " + getAttributeString() + " >");
 
 			Iterator iter = theElements.iterator();
 			while (iter.hasNext()) {
 				SelectOption option = (SelectOption) iter.next();
 				boolean setSelected = ((_allSelected) || selectedElements.contains(option.getValue()));
 				option.setSelected(setSelected);
 				option._print(iwc);
 			}
 
 			println("</select>");
 		}
 		else if (getLanguage().equals("WML")) {
 			println("<select name=\"" + getName() + "\" " + getAttributeString() + " >");
 
 			Iterator iter = theElements.iterator();
 			while (iter.hasNext()) {
 				SelectOption option = (SelectOption) iter.next();
 				if (_allSelected)
 					option.setSelected(true);
 				else {
 					if (selectedElements.contains(option.getValue()))
 						option.setSelected(true);
 				}
 				option._print(iwc);
 			}
 
 			println("</select>");
 		}
 	}
 
 	/**
 	 * @see java.lang.Object#clone()
 	 */
 	public Object clone() {
 		GenericSelect obj = null;
 		try {
 			obj = (GenericSelect) super.clone();
 			if (this.theElements != null) {
 				obj.theElements = (List) ((ArrayList) this.theElements).clone();
 				ListIterator iter = obj.theElements.listIterator();
 				while (iter.hasNext()) {
 					int index = iter.nextIndex();
 					Object item = iter.next();
 					if (item instanceof SelectOption) {
 						obj.theElements.set(index, (SelectOption) item);
 					}
 				}
 			}
 			if (this.selectedElements != null) {
 				obj.selectedElements = (List) ((ArrayList) this.selectedElements).clone();
 			}
 		}
 		catch (Exception ex) {
 			ex.printStackTrace(System.err);
 		}
 		return obj;
 	}
 
 	/**
 	 * @see com.idega.presentation.ui.InterfaceObject#handleKeepStatus(IWContext)
 	 */
 	public void handleKeepStatus(IWContext iwc) {
 		if (iwc.isParameterSet(getName())) {
 			String[] values = iwc.getParameterValues(getName());
 			if (values != null) {
 				for (int a = 0; a < values.length; a++) {
 					setSelectedOption(values[a]);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @deprecated Use getOptionCount() instead.
 	 * Returns the number of <code>SelectOption</code> objects added to this <code>GenericSelect</code>.
 	 * @return
 	 */
 	public int getNumberOfElemetent() {
 		if (theElements != null) {
 			return theElements.size();
 		}	else {
 			return 0;	
 		}
 	}
 
 	/**
 	 * Returns a <code>List</code> of <code>SelectOption</code> objects added to this <code>GenericSelect</code>.
 	 * @return
 	 */
 	public List getOptions() {
 		return theElements;
 	}
 
 	protected void setAllSelected(boolean allSelected) {
 		_allSelected = allSelected;
 	}
 
 	/**
 	 * Disables/Enables an <code>InterfaceObject</code> when the selected value is selected in the <code>GenericSelect</code>.
 	 * Uses Javascript.
 	 * @param objectToDisable	The interface object(s) to disable/enable.
 	 * @param selectedValue		The selected value of the <code>GenericSelect</code> to use.
 	 * @param disable					Disables if boolean is true, enables otherwise.
 	 */
 	public void setToDisableWhenSelected(InterfaceObject objectToDisable, String selectedValue, boolean disable) {
 		setToDisableWhenSelected(objectToDisable.getName(), selectedValue, disable);
 	}
 
 	/**
 	 * Disables/Enables an <code>InterfaceObject</code> when the selected value is selected in the <code>GenericSelect</code>.
 	 * Uses Javascript.
 	 * @param objectToDisableName	The name of the interface object(s) to disable/enable.
 	 * @param selectedValue				The selected value of the <code>GenericSelect</code> to use.
 	 * @param disable							Disables if boolean is true, enables otherwise.
 	 */
 	public void setToDisableWhenSelected(String objectToDisableName, String selectedValue, boolean disable) {
 		_isSetToDisable = true;
 		this.setOnChange("disableObjectByDropdown(this,findObj('"+objectToDisableName+"'),'"+String.valueOf(disable)+"','"+selectedValue+"')");
 	}
 
 	/**
 	 * Disables an <code>InterfaceObject</code> when the selected value is selected in the <code>GenericSelect</code>.
 	 * Uses Javascript.
 	 * @param objectToDisable	The interface object(s) to disable.
 	 * @param selectedValue		The selected value of the <code>GenericSelect</code> to use.
 	 */
 	public void setToDisableWhenSelected(InterfaceObject objectToDisable, String selectedValue) {
 		setToDisableWhenSelected(objectToDisable.getName(), selectedValue, true);
 	}
 
 	/**
 	 * Disables an <code>InterfaceObject</code> when the selected value is selected in the <code>GenericSelect</code>.
 	 * Uses Javascript.
 	 * @param objectToDisableName	The name of the interface object(s) to disable.
 	 * @param selectedValue				The selected value of the <code>GenericSelect</code> to use.
 	 */
 	public void setToDisableWhenSelected(String objectToDisableName, String selectedValue) {
 		setToDisableWhenSelected(objectToDisableName, selectedValue, true);
 	}
 
 	/**
 	 * Enables an <code>InterfaceObject</code> when the selected value is selected in the <code>GenericSelect</code>.
 	 * Uses Javascript.
 	 * @param objectToDisable	The interface object(s) to enable.
 	 * @param selectedValue		The selected value of the <code>GenericSelect</code> to use.
 	 */
 	public void setToEnableWhenSelected(InterfaceObject objectToDisable, String selectedValue) {
 		setToDisableWhenSelected(objectToDisable.getName(), selectedValue, false);
 	}
 
 	/**
 	 * Enables an <code>InterfaceObject</code> when the selected value is selected in the <code>GenericSelect</code>.
 	 * Uses Javascript.
 	 * @param objectToDisableName	The name of the interface object(s) to enable.
 	 * @param selectedValue				The selected value of the <code>GenericSelect</code> to use.
 	 */
 	public void setToEnableWhenSelected(String objectToDisableName, String selectedValue) {
 		setToDisableWhenSelected(objectToDisableName, selectedValue, false);
 	}
 
 	/**
 	 * Sets the selection so that it can not be empty, displays an alert with the given 
 	 * error message if the "error" occurs.  Uses Javascript.
 	 * @param errorMessage	The error message to display.
 	 * @param emptyValue		The value representing the "empty" value.
 	 */
 	public void setAsNotEmpty(String errorMessage, String emptyValue) {
 		_isSetAsNotEmpty = true;
 		_notEmptyErrorMessage = TextSoap.removeLineBreaks(errorMessage);
 		_emptyValue = emptyValue;
 	}
 
 	/**
 	 * Sets the selection so that it can not be empty, displays an alert with the given 
 	 * error message if the "error" occurs, uses -1 as the empty value.  Uses Javascript.
 	 * @param errorMessage	The error message to display.
 	 */
 	public void setAsNotEmpty(String errorMessage) {
 		setAsNotEmpty(errorMessage, "-1");
 	}
 }
