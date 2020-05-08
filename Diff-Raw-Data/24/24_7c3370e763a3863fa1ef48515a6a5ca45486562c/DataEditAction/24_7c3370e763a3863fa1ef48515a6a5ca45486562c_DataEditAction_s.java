 /*
  Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package browser;
 
 
 import static browser.DataObjectType.NETWORK;
 import browser.util.AttrUtil;
 
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 
 import org.cytoscape.equations.BooleanList;
 import org.cytoscape.equations.EqnCompiler;
 import org.cytoscape.equations.DoubleList;
 import org.cytoscape.equations.Equation;
 import org.cytoscape.equations.FunctionUtil;
 import org.cytoscape.equations.LongList;
 import org.cytoscape.equations.StringList;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.List;
 import java.util.TreeMap;
 import javax.swing.JOptionPane;
 import javax.swing.undo.AbstractUndoableEdit;
 
 
 /**
  * Validate and set new value to the CyAttributes.
  */
 public class DataEditAction extends AbstractUndoableEdit {
 	private final String attrKey;
 	private final String attrName;
 	private final Object old_value;
 	private final Object new_value;
 	private final DataObjectType objectType;
 	private final DataTableModel tableModel;
 
 	private boolean valid = false;
 	private ValidatedObjectAndEditString objectAndEditString = null;
 
 	/**
 	 * Creates a new DataEditAction object.
 	 *
 	 * @param table  DOCUMENT ME!
 	 * @param attrKey  DOCUMENT ME!
 	 * @param attrName  DOCUMENT ME!
 	 * @param keys  DOCUMENT ME!
 	 * @param old_value  DOCUMENT ME!
 	 * @param new_value  DOCUMENT ME!
 	 * @param graphObjectType  DOCUMENT ME!
 	 */
 	public DataEditAction(DataTableModel table, String attrKey, String attrName,
 	                      Object old_value, Object new_value, DataObjectType graphObjectType)
 	{
 		this.tableModel = table;
 		this.attrKey = attrKey;
 		this.attrName = attrName;
 		this.old_value = old_value;
 		this.new_value = new_value;
 		this.objectType = graphObjectType;
 
 		redo();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String getPresentationName() {
 		return attrKey + " attribute " + attrName + " changed.";
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String getRedoPresentationName() {
 		return "Redo: " + attrKey + ":" + attrName + " to:" + new_value + " from " + old_value;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String getUndoPresentationName() {
 		return "Undo: " + attrKey + ":" + attrName + " back to:" + old_value + " from " + new_value;
 	}
 
 	public ValidatedObjectAndEditString getValidatedObjectAndEditString() { return objectAndEditString; }
 
 	/**
 	 * Set attribute value.  Input validater is added.
 	 *
 	 * @param id
 	 * @param att
 	 * @param newValue
 	 */
 	private void setAttributeValue(final String id, final String attrName, final Object newValue) {
 		valid = false;
 		if (newValue == null)
 			return;
 
 		final CyAttributes attrs = objectType.getAssociatedAttributes();
 
 		// Error message for the popup dialog.
 		String errMessage = null;
 
 		// Change object to String
 		final String newValueStr = newValue.toString().trim();
 
 		final byte targetType = attrs.getType(attrName);
 		if (targetType == CyAttributes.TYPE_INTEGER)
 			handleInteger(newValueStr, attrs, id);
 		else if (targetType == CyAttributes.TYPE_FLOATING)
 			handleDouble(newValueStr, attrs, id);
 		else if (targetType == CyAttributes.TYPE_BOOLEAN)
 			handleBoolean(newValueStr, attrs, id);
 		else if (targetType == CyAttributes.TYPE_STRING)
 			handleString(newValueStr, attrs, id);
 		else if (targetType == CyAttributes.TYPE_SIMPLE_LIST)
 			handleList(newValueStr, attrs, id);
 		else if (targetType == CyAttributes.TYPE_SIMPLE_MAP)
 			handleMap(newValueStr, attrs, id);
 	}
 
 	private void handleInteger(final String newValueStr, final CyAttributes attrs, final String id) {
 		// Deal with equations first:
 		if (newValueStr != null && newValueStr.length() >= 2 && newValueStr.charAt(0) == '=') {
 			final Equation equation = parseEquation(newValueStr, attrs, id, attrName);
 			if (equation == null) {
 				objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#PARSE");
 				attrs.deleteAttribute(id, attrName);
 				return;
 			}
 
 			final Class returnType = equation.getType();
 			if (returnType != Long.class && returnType != Double.class && returnType != Boolean.class && returnType != Object.class) {
 				showErrorWindow("Error in attribute \"" + attrName
 						+ "\": equation is of type " + getLastDotComponent(returnType.toString())
 						+ " but should be of type Integer!");
 				objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#TYPE");
 				attrs.deleteAttribute(id, attrName);
 				return;
 			}
 
 			attrs.setAttribute(id, attrName, equation);
 			final Object attrValue = attrs.getAttribute(id, attrName);
 			String errorMessage = attrs.getLastEquationError();
 			if (errorMessage != null)
 				errorMessage = "#ERROR(" + errorMessage + ")";
 			objectAndEditString = new ValidatedObjectAndEditString(attrValue, newValueStr, errorMessage);
 			valid = true;
 			return;
 		}
 
 		Integer newIntVal;
 		try {
 			newIntVal = Integer.valueOf(newValueStr);
 			attrs.setAttribute(id, attrName, newIntVal);
 			objectAndEditString = new ValidatedObjectAndEditString(newIntVal);
 			valid = true;
 		} catch (final Exception e) {
 			objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#ERROR");
 			attrs.deleteAttribute(id, attrName);
 			showErrorWindow("Attribute " + attrName
 					+ " should be an Integer (or the number is too big/small).");
 		}
 	}
 
 	private void handleDouble(final String newValueStr, final CyAttributes attrs, final String id) {
 		// Deal with equations first:
 		if (newValueStr != null && newValueStr.length() >= 2 && newValueStr.charAt(0) == '=') {
 			final Equation equation = parseEquation(newValueStr, attrs, id, attrName);
 			if (equation == null) {
 				objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#PARSE");
 				attrs.deleteAttribute(id, attrName);
 				return;
 			}
 
 			final Class returnType = equation.getType();
 			if (returnType != Double.class && returnType != Long.class && returnType != Boolean.class && returnType != Object.class) {
 				showErrorWindow("Error in attribute \"" + attrName
 						+ "\": equation is of type " + getLastDotComponent(returnType.toString())
 						+ " but should be of type Floating Point!");
 				objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#TYPE");
 				attrs.deleteAttribute(id, attrName);
 				return;
 			}
 
 			attrs.setAttribute(id, attrName, equation);
 			final Object attrValue = attrs.getAttribute(id, attrName);
 			String errorMessage = attrs.getLastEquationError();
 			if (errorMessage != null)
 				errorMessage = "#ERROR(" + errorMessage + ")";
 			objectAndEditString = new ValidatedObjectAndEditString(attrValue, newValueStr, errorMessage);
 			valid = true;
 			return;
 		}
 
 		Double newDblVal;
 		try {
 			newDblVal = Double.valueOf(newValueStr);
 			attrs.setAttribute(id, attrName, newDblVal);
 			objectAndEditString = new ValidatedObjectAndEditString(newDblVal);
 			valid = true;
 		} catch (final Exception e) {
 			objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#ERROR");
 			attrs.deleteAttribute(id, attrName);
 			showErrorWindow("Attribute " + attrName
 					+ " should be a floating point number (or the number is too big/small).");
 		}
 	}
 
 	private void handleBoolean(final String newValueStr, final CyAttributes attrs, final String id) {
 		// Deal with equations first:
 		if (newValueStr != null && newValueStr.length() >= 2 && newValueStr.charAt(0) == '=') {
 			final Equation equation = parseEquation(newValueStr, attrs, id, attrName);
 			if (equation == null) {
 				objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#PARSE");
 				attrs.deleteAttribute(id, attrName);
 				return;
 			}
 
 			final Class returnType = equation.getType();
 			if (returnType != Boolean.class && returnType != Long.class && returnType != Double.class && returnType != Object.class) {
 				objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#TYPE");
 				attrs.deleteAttribute(id, attrName);
 				showErrorWindow("Error in attribute \"" + attrName
 						+ "\": equation is of type " + getLastDotComponent(returnType.toString())
 						+ " but should be of type Boolean!");
 				return;
 			}
 
 			attrs.setAttribute(id, attrName, equation);
 			final Object attrValue = attrs.getAttribute(id, attrName);
 			String errorMessage = attrs.getLastEquationError();
 			if (errorMessage != null)
 				errorMessage = "#ERROR(" + errorMessage + ")";
 			objectAndEditString = new ValidatedObjectAndEditString(attrValue, newValueStr, errorMessage);
 			valid = true;
 			return;
 		}
 
 		Boolean newBoolVal = false;
 		try {
 			newBoolVal = Boolean.valueOf(newValueStr);
 			attrs.setAttribute(id, attrName, newBoolVal);
 			objectAndEditString = new ValidatedObjectAndEditString(newBoolVal);
 			valid = true;
 		} catch (final Exception e) {
 			objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#ERROR");
 			attrs.deleteAttribute(id, attrName);
 			showErrorWindow("Attribute " + attrName + " should be a boolean value (true/false).");
 		}
 	}
 
 	private void handleString(final String newValueStr, final CyAttributes attrs, final String id) {
 		final String newStrVal = replaceCStyleEscapes(newValueStr);
 
 		// Deal with equations first:
 		if (newValueStr != null && newValueStr.length() >= 2 && newValueStr.charAt(0) == '=') {
 			final Equation equation = parseEquation(newStrVal, attrs, id, attrName);
 			if (equation == null) {
 				objectAndEditString = new ValidatedObjectAndEditString(null, newStrVal, "#PARSE");
 				attrs.deleteAttribute(id, attrName);
 				return;
 			}
 
 			attrs.setAttribute(id, attrName, equation);
 			objectAndEditString = new ValidatedObjectAndEditString(attrs.getAttribute(id, attrName), equation.toString());
 			final Object attrValue = attrs.getAttribute(id, attrName);
 			String errorMessage = attrs.getLastEquationError();
 			if (errorMessage != null)
 				errorMessage = "#ERROR(" + errorMessage + ")";
 			objectAndEditString = new ValidatedObjectAndEditString(attrValue, newValueStr, errorMessage);
 			valid = true;
 			return;
 		}
 
 		attrs.setAttribute(id, attrName, newStrVal);
 		objectAndEditString = new ValidatedObjectAndEditString(newStrVal);
 		valid = true;
 	}
 
 	private void handleList(final String newValueStr, final CyAttributes attrs, final String id) {
 		// Deal with equations first:
 		if (newValueStr != null && newValueStr.length() >= 2 && newValueStr.charAt(0) == '=') {
 			final Equation equation = parseEquation(newValueStr, attrs, id, attrName);
 			if (equation == null) {
 				objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#PARSE");
 				attrs.deleteAttribute(id, attrName);
 				return;
 			}
 
 			final Class returnType = equation.getType();
 			if (!FunctionUtil.isSomeKindOfList(returnType) && returnType != Object.class) {
 				objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#TYPE");
 				attrs.deleteAttribute(id, attrName);
 				showErrorWindow("Error in attribute \"" + attrName
 						+ "\": equation is of type " + getLastDotComponent(returnType.toString())
 						+ " but should be of type List!");
 				return;
 			}
 
 			final byte listElementType = attrs.getListElementType(attrName);
 			if (returnType == DoubleList.class && listElementType != CyAttributes.TYPE_FLOATING
 			    || returnType == LongList.class && listElementType != CyAttributes.TYPE_INTEGER
 			    || returnType == StringList.class && listElementType != CyAttributes.TYPE_STRING
 			    || returnType == BooleanList.class && listElementType != CyAttributes.TYPE_BOOLEAN)
 			{
 				objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#TYPE");
 				attrs.deleteAttribute(id, attrName);
 				showErrorWindow("Error in attribute \"" + attrName
 						+ "\": equation is of type " + getLastDotComponent(returnType.toString())
 						+ " which is the wrong type of list!");
 				return;
 			}
 
 			attrs.setListAttribute(id, attrName, equation);
 			final Object attrValue = attrs.getAttribute(id, attrName);
 			String errorMessage = attrs.getLastEquationError();
 			if (errorMessage != null)
 				errorMessage = "#ERROR(" + errorMessage + ")";
 			objectAndEditString = new ValidatedObjectAndEditString(attrValue, newValueStr, errorMessage);
 			valid = true;
 			return;
 		}
 
 		final String escapedString = replaceCStyleEscapes(newValueStr);
		final List origList = attrs.getListAttribute(id, attrName);
 
 		List newList = null;
		if (origList.isEmpty() || origList.get(0).getClass() == String.class)
 			newList = parseStringListValue(escapedString);
		else if (origList.get(0).getClass() == Double.class)
 			newList = parseDoubleListValue(escapedString);
		else if (origList.get(0).getClass() == Integer.class)
 			newList = parseIntegerListValue(escapedString);
		else if (origList.get(0).getClass() == Boolean.class)
 			newList = parseBooleanListValue(escapedString);
		else
 			throw new ClassCastException("can't determined List type!");
 
 		if (newList == null) {
 			objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#ERROR");
 			attrs.deleteAttribute(id, attrName);
 			showErrorWindow("Invalid list!");
 			return;
 		}
 		else {
 			attrs.setListAttribute(id, attrName, newList);
 			objectAndEditString = new ValidatedObjectAndEditString(escapedString);
 			valid = true;
 		}
 	}
 
 	private void handleMap(final String newValueStr, final CyAttributes attrs, final String id) {
 		// Deal with equations first:
 		if (newValueStr != null && newValueStr.length() >= 2 && newValueStr.charAt(0) == '=') {
 			objectAndEditString = new ValidatedObjectAndEditString(null, newValueStr, "#ERROR");
 			attrs.deleteAttribute(id, attrName);
 			showErrorWindow("Error in attribute \"" + attrName
 					+ "\": no equations are supported for maps!");
 			return;
 		}
 
 		showErrorWindow("Map editing is not supported in this version.");
 	}
 
 	/**
 	 *  Assumes that "s" consists of components separated by dots.
 	 *  @returns the last component of "s" or all of "s" if there are no dots
 	 */
 	private static String getLastDotComponent(final String s) {
 		final int lastDotPos = s.lastIndexOf('.');
 		if (lastDotPos == -1)
 			return s;
 
 		return s.substring(lastDotPos + 1);
 	}
 
 	/** Does some rudimentary list syntax checking and returns the number of items in "listCandidate."
 	 * @param listCandidate a string that will be analysed as to list-syntax conformance.
 	 * @returns -1 if "listCandidate" does not conform to a list syntax, otherwise the number of items in the simple list.
 	 */
 	private int countListItems(final String listCandidate) {
 		if (listCandidate.length() < 2 || listCandidate.charAt(0) != '[' || listCandidate.charAt(listCandidate.length() - 1) != ']')
 			return -1;
 
 		int commaCount = 0;
 		for (int charIndex = 1; charIndex < listCandidate.length() - 1; ++charIndex) {
 			if (listCandidate.charAt(charIndex) == ',')
 				++commaCount;
 		}
 
 		return commaCount;
 	}
 
 	/** Attemps to convert "listCandidate" to a List of String.
 	 * @param listCandidate hopefully a list of strings.
 	 * @returns the List if "listCandidate" has been successfully parsed, else null.
 	 */
 	private List parseStringListValue(final String listCandidate) {
 		final int itemCount = countListItems(listCandidate);
 		if (itemCount == -1)
 			return null;
 
 		final String bracketlessList = listCandidate.substring(1, listCandidate.length() - 1);
 		final String[] items = bracketlessList.split("\\s*,\\s*");
 
 		return Arrays.asList(items);
 	}
 
 	/** Attemps to convert "listCandidate" to a List of Double.
 	 * @param listCandidate hopefully a list of doubles.
 	 * @returns the List if "listCandidate" has been successfully parsed, else null.
 	 */
 	private List parseDoubleListValue(final String listCandidate) {
 		final int itemCount = countListItems(listCandidate);
 		if (itemCount == -1)
 			return null;
 
 		final String bracketlessList = listCandidate.substring(1, listCandidate.length() - 1);
 		final String[] items = bracketlessList.split("\\s*,\\s*");
 
 		final List<Double> doubleList = new ArrayList<Double>(itemCount);
 		try {
 			for (final String item : items) {
 				final Double newDouble = Double.valueOf(item);
 				doubleList.add(newDouble);
 			}
 		} catch (final NumberFormatException e) {
 			return null; // At least one of the list items was not a double.
 		}
 
 		return doubleList;
 	}
 
 	/** Attemps to convert "listCandidate" to a List of Integer.
 	 * @param listCandidate hopefully a list of ints.
 	 * @returns the List if "listCandidate" has been successfully parsed, else null.
 	 */
 	private List parseIntegerListValue(final String listCandidate) {
 		final int itemCount = countListItems(listCandidate);
 		if (itemCount == -1)
 			return null;
 
 		final String bracketlessList = listCandidate.substring(1, listCandidate.length() - 1);
 		final String[] items = bracketlessList.split("\\s*,\\s*");
 
 		final List<Integer> intList = new ArrayList<Integer>(itemCount);
 		try {
 			for (final String item : items) {
 				final Integer newInteger = Integer.valueOf(item);
 				intList.add(newInteger);
 			}
 		} catch (final NumberFormatException e) {
 			return null; // At least one of the list items was not a int.
 		}
 
 		return intList;
 	}
 
 	/** Attemps to convert "listCandidate" to a List of Boolean.
 	 * @param listCandidate hopefully a list of booleans.
 	 * @returns the List if "listCandidate" has been successfully parsed, else null.
 	 */
 	private List parseBooleanListValue(final String listCandidate) {
 		final int itemCount = countListItems(listCandidate);
 		if (itemCount == -1)
 			return null;
 
 		final String bracketlessList = listCandidate.substring(1, listCandidate.length() - 1);
 		final String[] items = bracketlessList.split("\\s*,\\s*");
 
 		final List<Boolean> booleanList = new ArrayList<Boolean>(itemCount);
 		try {
 			for (final String item : items) {
 				final Boolean newBoolean = Boolean.valueOf(item);
 				booleanList.add(newBoolean);
 			}
 		} catch (final NumberFormatException e) {
 			return null; // At least one of the list items was not a boolean.
 		}
 
 		return booleanList;
 	}
 
 	private String replaceCStyleEscapes(String s) {
 		StringBuffer sb = new StringBuffer( s );
 		int index = 0;
 		while ( index < sb.length() ) {
 			if ( sb.charAt(index) == '\\' ) {
 				if ( sb.charAt(index+1) == 'n') {
 					sb.setCharAt(index,'\n');
 					sb.deleteCharAt(index+1);
 					index++;
 				} else if ( sb.charAt(index+1) == 'b') {
 					sb.setCharAt(index,'\b');
 					sb.deleteCharAt(index+1);
 					index++;
 				} else if ( sb.charAt(index+1) == 'r') {
 					sb.setCharAt(index,'\r');
 					sb.deleteCharAt(index+1);
 					index++;
 				} else if ( sb.charAt(index+1) == 'f') {
 					sb.setCharAt(index,'\f');
 					sb.deleteCharAt(index+1);
 					index++;
 				} else if ( sb.charAt(index+1) == 't') {
 					sb.setCharAt(index,'\t');
 					sb.deleteCharAt(index+1);
 					index++;
 				}
 			}
 			index++;
 		}
 		return sb.toString();
 	}
 
 	// Pop-up window for error message
 	private static void showErrorWindow(final String errMessage) {
 		JOptionPane.showMessageDialog(Cytoscape.getDesktop(), errMessage, "Invalid Value!",
 		                              JOptionPane.ERROR_MESSAGE);
 	}
 
 	/**
 	 * For redo function.
 	 */
 	public void redo() {
 		setAttributeValue(attrKey, attrName, new_value);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 */
 	public void undo() {
 		setAttributeValue(attrKey, attrName, old_value);
 
 		if (objectType != NETWORK) {
 			tableModel.setTableData();
 		} else {
 			tableModel.setNetworkTable();
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean isValid() {
 		return valid;
 	}
 
 	/**
 	 *  @returns the successfully compiled equation or null if an error occurred
 	 */
 	private Equation parseEquation(final String equation, final CyAttributes attrs, final String id,
 	                               final String currentAttrName)
 	{
 		final Map<String, Class> attribNameToTypeMap = AttrUtil.getAttrNamesAndTypes(attrs);
 		attribNameToTypeMap.put("ID", String.class);
 
 		final EqnCompiler compiler = new EqnCompiler();
 		if (!compiler.compile(equation, attribNameToTypeMap)) {
 			showErrorWindow("Error in equation for attribute\"" + currentAttrName + "\": "
 			                + compiler.getLastErrorMsg());
 			return null;
 		}
 
 		return compiler.getEquation();
 	}
 }
