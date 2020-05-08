 /*
   File: CyAttributesImpl.java
 
   Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)
 
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
 package cytoscape.data;
 
 
 import cytoscape.Cytoscape;
 import cytoscape.data.attr.CountedIterator;
 import cytoscape.data.attr.MultiHashMap;
 import cytoscape.data.attr.MultiHashMapDefinition;
 import cytoscape.data.attr.util.MultiHashMapFactory;
 import cytoscape.logger.CyLogger;
 import cytoscape.util.TopoGraphNode;
 import cytoscape.util.TopologicalSort;
 
 import org.cytoscape.equations.Equation;
 import org.cytoscape.equations.DoubleList;
 import org.cytoscape.equations.LongList;
 import org.cytoscape.equations.StringList;
 import org.cytoscape.equations.BooleanList;
 import org.cytoscape.equations.interpreter.IdentDescriptor;
 import org.cytoscape.equations.interpreter.Interpreter;
 
 import java.util.*;
 
 
 public class CyAttributesImpl implements CyAttributes {
 	private final MultiHashMap mmap;
 	private final MultiHashMapDefinition mmapDef;
 
 	//  used to store human readable descriptions of attributes.
 	private Map descriptionMap;
 
 	//  used to store only those attributes, which should be hidden from
 	//  the end user.
 	private Set userInvisibleSet;
 
 	//  used to store only those attributes, which should not be editable
 	//  by the end user.
 	private Set userNonEditableSet;
 
 	private String lastEquationError = null;
 
 	protected static final CyLogger logger = CyLogger.getLogger(Cytoscape.class);
 
 	/**
 	 * Creates a new CyAttributesImpl object.
 	 */
 	public CyAttributesImpl() {
 		Object model = MultiHashMapFactory.instantiateDataModel();
 		mmap = (MultiHashMap) model;
 		mmapDef = (MultiHashMapDefinition) model;
 		descriptionMap = new HashMap();
 		userInvisibleSet = new HashSet();
 		userNonEditableSet = new HashSet();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param attributeName DOCUMENT ME!
 	 * @param description DOCUMENT ME!
 	 */
 	public void setAttributeDescription(String attributeName, String description) {
 		descriptionMap.put(attributeName, description);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String getAttributeDescription(String attributeName) {
 		return (String) descriptionMap.get(attributeName);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param attributeName DOCUMENT ME!
 	 * @param value DOCUMENT ME!
 	 */
 	public void setUserVisible(String attributeName, boolean value) {
 		if (value) {
 			if (userInvisibleSet.contains(attributeName)) {
 				userInvisibleSet.remove(attributeName);
 			}
 		} else {
 			if (!userInvisibleSet.contains(attributeName)) {
 				userInvisibleSet.add(attributeName);
 			}
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean getUserVisible(String attributeName) {
 		//  by default, all attributes are visible, return value = true
 		if (userInvisibleSet.contains(attributeName)) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param attributeName DOCUMENT ME!
 	 * @param value DOCUMENT ME!
 	 */
 	public void setUserEditable(String attributeName, boolean value) {
 		if (value) {
 			if (userNonEditableSet.contains(attributeName)) {
 				userNonEditableSet.remove(attributeName);
 			}
 		} else {
 			if (!userNonEditableSet.contains(attributeName)) {
 				userNonEditableSet.add(attributeName);
 			}
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean getUserEditable(String attributeName) {
 		//  by default, all attributes are editable, return value = true
 		if (userNonEditableSet.contains(attributeName)) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String[] getAttributeNames() {
 		final CountedIterator citer = mmapDef.getDefinedAttributes();
 		final String[] names = new String[citer.numRemaining()];
 		int inx = 0;
 
 		while (citer.hasNext()) {
 			names[inx++] = (String) citer.next();
 		}
 
 		return names;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean hasAttribute(String id, String attributeName) {
 		final byte valType = mmapDef.getAttributeValueType(attributeName);
 
 		if (valType < 0)
 			return false;
 
 		final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 		if (dimTypes.length == 0) {
 			return mmap.getAttributeValue(id, attributeName, null) != null;
 		} else {
 			return mmap.getAttributeKeyspan(id, attributeName, null).numRemaining() > 0;
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 * @param value DOCUMENT ME!
 	 */
 	public void setAttribute(final String id, final String attributeName, final Boolean value) {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		if (attributeName == null)
 			throw new IllegalArgumentException("attributeName is null");
 
 		final byte type = mmapDef.getAttributeValueType(attributeName);
 
 		if (type < 0) {
 			mmapDef.defineAttribute(attributeName, MultiHashMapDefinition.TYPE_BOOLEAN, null);
 		} else {
 			if (type != MultiHashMapDefinition.TYPE_BOOLEAN) {
 				throw new IllegalArgumentException("definition for attributeName '" + attributeName
 				                                   + "' already exists and it is not of TYPE_BOOLEAN");
 			}
 
 			final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 			if (dimTypes.length != 0) {
 				throw new IllegalArgumentException("definition for attributeName '" + attributeName
 				                                   + "' already exists and it is not of TYPE_BOOLEAN");
 			}
 		}
 
 		mmap.setAttributeValue(id, attributeName, value, null);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 * @param value DOCUMENT ME!
 	 */
 	public void setAttribute(String id, String attributeName, Integer value) {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		if (attributeName == null)
 			throw new IllegalArgumentException("attributeName is null");
 
 		final byte type = mmapDef.getAttributeValueType(attributeName);
 
 		if (type < 0) {
 			mmapDef.defineAttribute(attributeName, MultiHashMapDefinition.TYPE_INTEGER, null);
 		} else {
 			if (type != MultiHashMapDefinition.TYPE_INTEGER) {
 				throw new IllegalArgumentException("definition for attributeName '" + attributeName
 				                                   + "' already exists and it is not of TYPE_INTEGER");
 			}
 
 			final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 			if (dimTypes.length != 0) {
 				throw new IllegalArgumentException("definition for attributeName '" + attributeName
 				                                   + "' already exists and it is not of TYPE_INTEGER");
 			}
 		}
 
 		mmap.setAttributeValue(id, attributeName, value, null);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 * @param value DOCUMENT ME!
 	 */
 	public void setAttribute(String id, String attributeName, Double value) {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		if (attributeName == null)
 			throw new IllegalArgumentException("attributeName is null");
 
 		final byte type = mmapDef.getAttributeValueType(attributeName);
 
 		if (type < 0) {
 			mmapDef.defineAttribute(attributeName, MultiHashMapDefinition.TYPE_FLOATING_POINT, null);
 		} else {
 			if (type != MultiHashMapDefinition.TYPE_FLOATING_POINT) {
 				throw new IllegalArgumentException("definition for attributeName '" + attributeName
 				                                   + "' already exists and it is not of TYPE_FLOATING");
 			}
 
 			final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 			if (dimTypes.length != 0) {
 				throw new IllegalArgumentException("definition for attributeName '" + attributeName
 				                                   + "' already exists and it is not of TYPE_FLOATING");
 			}
 		}
 
 		mmap.setAttributeValue(id, attributeName, value, null);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 * @param value DOCUMENT ME!
 	 */
 	public void setAttribute(String id, String attributeName, String value) {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		if (attributeName == null)
 			throw new IllegalArgumentException("attributeName is null");
 
 		final byte type = mmapDef.getAttributeValueType(attributeName);
 
 		if (type < 0) {
 			mmapDef.defineAttribute(attributeName, MultiHashMapDefinition.TYPE_STRING, null);
 		} else {
 			if (type != MultiHashMapDefinition.TYPE_STRING) {
 				throw new IllegalArgumentException("definition for attributeName '" + attributeName
 				                                   + "' already exists and it is not of TYPE_STRING");
 			}
 
 			final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 			if (dimTypes.length != 0) {
 				throw new IllegalArgumentException("definition for attributeName '" + attributeName
 				                                   + "' already exists and it is not of TYPE_STRING");
 			}
 		}
 
 		mmap.setAttributeValue(id, attributeName, value, null);
 	}
 
 	/**
 	 *  @param id            unique identifier.
 	 *  @param attributeName attribute name.
 	 *  @param equation      an attribute equation
 	 */
 	public void setAttribute(final String id, final String attributeName, final Equation equation) {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		if (attributeName == null)
 			throw new IllegalArgumentException("attributeName is null");
 
 		final byte type = mmapDef.getAttributeValueType(attributeName);
 		final Class returnType = equation.getType();
 		if (type < 0) { // Attribute does not yet exist!
 			final byte mappedType;
 			if (returnType == Double.class)
 				mappedType = MultiHashMapDefinition.TYPE_FLOATING_POINT;
 			else if (returnType == String.class)
 				mappedType = MultiHashMapDefinition.TYPE_STRING;
 			else if (returnType == Boolean.class)
 				mappedType = MultiHashMapDefinition.TYPE_BOOLEAN;
 			else
 				throw new IllegalStateException("unknown equation return type: " + returnType + "!");
 			mmapDef.defineAttribute(attributeName, mappedType, null);
 		} else {
 			final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 			if (dimTypes.length != 0)
 				throw new IllegalArgumentException("definition for \"" + attributeName 
 			                                           + "\" already exists and it is not of a scalar type!");
 
 			if (type == MultiHashMapDefinition.TYPE_STRING)
 				/* Everything is compatible w/ this! */;
 			else if (type == MultiHashMapDefinition.TYPE_INTEGER) {
 				if (returnType != Long.class && returnType != Double.class && returnType != Boolean.class)
 					throw new IllegalArgumentException("an equation of type " + returnType
 					                                   + " is not compatible with TYPE_INTEGER for attribute \""
 					                                   + attributeName + "\"!");
 			}
 			else if (type == MultiHashMapDefinition.TYPE_FLOATING_POINT) {
 				if (returnType != Double.class && returnType != Long.class && returnType != Boolean.class)
 					throw new IllegalArgumentException("an equation of type " + returnType
 					                                   + " is not compatible with TYPE_FLOATING_POINT for attribute \""
 					                                   + attributeName + "\"!");
 			}
 			else if (type == MultiHashMapDefinition.TYPE_BOOLEAN) {
 				if (returnType != Boolean.class && returnType != Long.class && returnType != Double.class)
 					throw new IllegalArgumentException("an equation of type " + returnType
 					                                   + " is not compatible with TYPE_BOOLEAN for attribute \""
 					                                   + attributeName + "\"!");
 			}
 			else
 				throw new IllegalArgumentException("an equation of type " + returnType
 					                           + " is not compatible with attribute \""
 					                           + attributeName + "\"!");
 		}
 
 		mmap.setAttributeValue(id, attributeName, equation, null);
 	}
 
 	/**
 	 *  @param id            unique identifier.
 	 *  @param attributeName attribute name.
 	 *  @param equation      an attribute equation
 	 *  @param dataType      must be one of MultiHashMapDefinition.TYPE_{BOOLEAN,STRING,INTEGER, or FLOATING_POINT}
 	 */
 	public void setAttribute(final String id, final String attributeName, final Equation equation,
 	                         final byte dataType)
 	{
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		if (attributeName == null)
 			throw new IllegalArgumentException("attributeName is null");
 
 		final byte type = mmapDef.getAttributeValueType(attributeName);
 		final Class returnType = equation.getType();
 		if (type < 0) { // Attribute does not yet exist.
 			if (dataType == MultiHashMapDefinition.TYPE_STRING)
 				/* Everything is compatible w/ this! */;
 			else if (type == MultiHashMapDefinition.TYPE_INTEGER) {
 				if (returnType != Double.class && returnType != Boolean.class)
 					throw new IllegalArgumentException("an equation of type " + returnType
 					                                   + " is not compatible with TYPE_INTEGER for attribute \""
 					                                   + attributeName + "\"!");
 			}
 			else if (dataType == MultiHashMapDefinition.TYPE_FLOATING_POINT) {
 				if (returnType != Double.class && returnType != Boolean.class)
 					throw new IllegalArgumentException("an equation of type " + returnType
 					                                   + " is not compatible with TYPE_FLOATING_POINT for attribute \""
 					                                   + attributeName + "\"!");
 			}
 			mmapDef.defineAttribute(attributeName, dataType, null);
 		} else {
 			final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 			if (dimTypes.length != 0)
 				throw new IllegalArgumentException("definition for \"" + attributeName 
 			                                           + "\" already exists and it is not of a scalar type!");
 			if (type != dataType)
 				throw new IllegalArgumentException("incompatible data type!");
 
 			if (type == MultiHashMapDefinition.TYPE_STRING)
 				/* Everything is compatible w/ this! */;
 			else if (type == MultiHashMapDefinition.TYPE_INTEGER) {
 				if (returnType != Double.class && returnType != Boolean.class && returnType != Long.class)
 					throw new IllegalArgumentException("an equation of type " + returnType
 					                                   + " is not compatible with TYPE_INTEGER for attribute \""
 					                                   + attributeName + "\"!");
 			}
 			else if (type == MultiHashMapDefinition.TYPE_FLOATING_POINT) {
 				if (returnType != Double.class && returnType != Boolean.class)
 					throw new IllegalArgumentException("an equation of type " + returnType
 					                                   + " is not compatible with TYPE_FLOATING_POINT for attribute \""
 					                                   + attributeName + "\"!");
 			}
 		}
 
 		mmap.setAttributeValue(id, attributeName, equation, null);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Boolean getBooleanAttribute(final String id, final String attributeName) {
 		lastEquationError = null;
 
 		final byte type = mmapDef.getAttributeValueType(attributeName);
 		if (type < 0)
 			return null;
 
 		if (type != MultiHashMapDefinition.TYPE_BOOLEAN)
 			throw new ClassCastException("definition for attributeName '" + attributeName
 			                             + "' is not of TYPE_BOOLEAN");
 
 		final Object attribValue = mmap.getAttributeValue(id, attributeName, null);
 		if (attribValue == null)
 			return null;
 		if (attribValue instanceof Boolean)
 			return (Boolean)attribValue;
 
 		// Now we assume that we are dealing with an equation:
 		final StringBuilder errorMessage = new StringBuilder();
 		final Object equationValue = evalEquation(id, attributeName, (Equation)attribValue,
 		                                          errorMessage);
 		if (equationValue == null) {
 			lastEquationError = errorMessage.toString();
 			return null;
 		}
 		return convertEqnRetValToBoolean(id, attributeName, equationValue);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Integer getIntegerAttribute(final String id, final String attributeName) {
 		lastEquationError = null;
 
 		final byte type = mmapDef.getAttributeValueType(attributeName);
 		if (type < 0)
 			return null;
 
 		if (type != MultiHashMapDefinition.TYPE_INTEGER)
 			throw new ClassCastException("definition for attributeName '" + attributeName
 			                             + "' is not of TYPE_INTEGER");
 
 		final Object attribValue = mmap.getAttributeValue(id, attributeName, null);
 		if (attribValue == null)
 			return null;
 		if (attribValue instanceof Integer)
 			return (Integer)attribValue;
 
 		// Now we assume that we are dealing with an equation:
 		final StringBuilder errorMessage = new StringBuilder();
 		final Object equationValue = evalEquation(id, attributeName, (Equation)attribValue,
 		                                          errorMessage);
 		if (equationValue == null) {
 			lastEquationError = errorMessage.toString();
 			return null;
 		}
 		if (equationValue.getClass() == Long.class) {
 			final long valueAsLong = (Long)equationValue;
 			return (int)valueAsLong;
 		}
 		if (equationValue.getClass() == Boolean.class) {
 			final Boolean valueAsBoolean = (Boolean)equationValue;
 			return valueAsBoolean ? 1 : 0;
 		}
 		if (equationValue.getClass() == String.class) {
 			final String valueAsString = (String)equationValue;
 			try {
 				final double valueAsDouble = Double.parseDouble(valueAsString);
 				return (int)excelTrunc(valueAsDouble);
 			} catch (final NumberFormatException e) {
 				throw new IllegalStateException("\"" + valueAsString
 				                                + "\" cannot be interpreted as an integer value!");
 			}
 		}
 		if (equationValue.getClass() == Double.class) {
 			final double valueAsDouble = (Double)equationValue;
 			return (int)excelTrunc(valueAsDouble);
 		}
 
 		throw new IllegalStateException("an equation returned an unknown class type: "
 		                                + equationValue.getClass() + "!");
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Double getDoubleAttribute(String id, String attributeName) {
 		lastEquationError = null;
 
 		final byte type = mmapDef.getAttributeValueType(attributeName);
 		if (type < 0)
 			return null;
 
 		if (type != MultiHashMapDefinition.TYPE_FLOATING_POINT)
 			throw new ClassCastException("definition for attributeName '" + attributeName
 			                             + "' is not of TYPE_FLOATING");
 
 		final Object attribValue = mmap.getAttributeValue(id, attributeName, null);
 		if (attribValue == null)
 			return null;
 		if (attribValue instanceof Double)
 			return (Double)attribValue;
 
 		// Now we assume that we are dealing with an equation:
 		final StringBuilder errorMessage = new StringBuilder();
 		final Object equationValue = evalEquation(id, attributeName, (Equation)attribValue,
 		                                          errorMessage);
 		if (equationValue == null) {
 			lastEquationError = errorMessage.toString();
 			return null;
 		}
 		return convertEqnRetValToDouble(id, attributeName, equationValue);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String getStringAttribute(final String id, final String attributeName) {
 		lastEquationError = null;
 
 		final byte type = mmapDef.getAttributeValueType(attributeName);
 		if (type < 0)
 			return null;
 
 		if (type != MultiHashMapDefinition.TYPE_STRING)
 			throw new ClassCastException("definition for attributeName '" + attributeName
 			                             + "' is not of TYPE_STRING");
 
 		final Object attribValue = mmap.getAttributeValue(id, attributeName, null);
 		if (attribValue == null)
 			return null;
 		if (attribValue instanceof String)
 			return (String)attribValue;
 
 		// Now we assume that we are dealing with an equation:
 		final StringBuilder errorMessage = new StringBuilder();
 		final Object equationValue = evalEquation(id, attributeName, (Equation)attribValue,
 		                                          errorMessage);
 		if (equationValue == null) {
 			lastEquationError = errorMessage.toString();
 			return null;
 		}
 		return equationValue.toString();
 	}
 
 	/**
 	 * Gets an Object value at the specified id/attributeName. This is a convenience
 	 * method for those situations when attribute type isn't relevant.  You should
 	 * NOT use this method and cast the result to the type of attribute.  Instead,
 	 * just call the appropriate get*Attribute method.
 	 *
 	 * @param id            unique identifier.
 	 * @param attributeName attribute name.
 	 * @return Object, or null if no id/attributeName pair is found.
 	 */
 	public Object getAttribute(final String id, final String attributeName) {
 		lastEquationError = null;
 
 		final byte type = getType(attributeName); 
 
 		if ( type == TYPE_SIMPLE_LIST )
 			return getListAttribute(id, attributeName);
 		else if ( type == TYPE_SIMPLE_MAP )
 			return getMapAttribute(id, attributeName);
 		else if ( type == TYPE_UNDEFINED || type == TYPE_COMPLEX )
 			return null;
 
 		final Object attribValue = mmap.getAttributeValue(id, attributeName, null);
 		if (attribValue == null)
 			return null;
 
 		if (attribValue instanceof Equation) {
 			final StringBuilder errorMessage = new StringBuilder();
 			final Object equationValue = evalEquation(id, attributeName, (Equation)attribValue,
 			                                          errorMessage);
 			if (equationValue == null) {
 				lastEquationError = errorMessage.toString();
 				return null;
 			}
 
 			if (type == MultiHashMapDefinition.TYPE_INTEGER)
 				return convertEqnRetValToInteger(id, attributeName, equationValue);
 			if (type == MultiHashMapDefinition.TYPE_FLOATING_POINT)
 				return convertEqnRetValToDouble(id, attributeName, equationValue);
 			if (type == MultiHashMapDefinition.TYPE_BOOLEAN)
 				return convertEqnRetValToBoolean(id, attributeName, equationValue);
 			if (type == MultiHashMapDefinition.TYPE_STRING)
 				return equationValue.toString();
 			return equationValue;
 		} 
 
 		return attribValue;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public byte getType(final String attributeName) {
 		final byte valType = mmapDef.getAttributeValueType(attributeName);
 
 		if (valType < 0)
 			return TYPE_UNDEFINED;
 
 		final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 		if (dimTypes.length == 0) {
 			return valType;
 		}
 
 		if (dimTypes.length > 1) {
 			return TYPE_COMPLEX;
 		}
 
 		if (dimTypes[0] == MultiHashMapDefinition.TYPE_INTEGER) {
 			return TYPE_SIMPLE_LIST;
 		}
 
 		if (dimTypes[0] == MultiHashMapDefinition.TYPE_STRING) {
 			return TYPE_SIMPLE_MAP;
 		}
 
 		return TYPE_COMPLEX;
 	}
 
 	/**
 	 * Gets the member data type of the specified simple list attribute.
 	 *
 	 * @param attributeName the name of a simple list attribute
 	 * @return one of: TYPE_BOOLEAN, TYPE_INTEGER, TYPE_FLOATING, TYPE_STRING
 	 */
 	public byte getListElementType(final String attributeName) {
 		final byte valType = mmapDef.getAttributeValueType(attributeName);
 		if (valType < 0)
 			throw new IllegalArgumentException("'" + attributeName + "' is not a simple list attribute!");
 
 		final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 		if (dimTypes.length != 1 || dimTypes[0] != MultiHashMapDefinition.TYPE_INTEGER)
 			throw new IllegalArgumentException("'" + attributeName + "' is not a simple list attribute!");
 
 		return valType;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean deleteAttribute(String id, String attributeName) {
 		boolean b = mmap.removeAllAttributeValues(id, attributeName);
 
 		return b;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean deleteAttribute(String attributeName) {
 		boolean b = mmapDef.undefineAttribute(attributeName);
 
 		return b;
 	}
 
 	public void setListAttribute(String id, String attributeName, List list) {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		if (attributeName == null)
 			throw new IllegalArgumentException("attributeName is null");
 
 		if (list == null)
 			throw new IllegalArgumentException("list is null");
 
 		Iterator itor = list.iterator();
 
 		if (!itor.hasNext()) {
 			return;
 		}
 
 		final byte type;
 		Object obj = itor.next();
 
 		if (obj instanceof Double) {
 			type = TYPE_FLOATING;
 		} else if (obj instanceof Integer) {
 			type = TYPE_INTEGER;
 		} else if (obj instanceof Boolean) {
 			type = TYPE_BOOLEAN;
 		} else if (obj instanceof String) {
 			type = TYPE_STRING;
 		} else
 			throw new IllegalArgumentException("objects in list are of unrecognized type");
 
 		while (itor.hasNext()) {
 			obj = itor.next();
 
 			if (((type == TYPE_FLOATING) && (!(obj instanceof Double)))
 			    || ((type == TYPE_INTEGER) && (!(obj instanceof Integer)))
 			    || ((type == TYPE_BOOLEAN) && (!(obj instanceof Boolean)))
 			    || ((type == TYPE_STRING) && (!(obj instanceof String))))
 				throw new IllegalArgumentException("items in list are not all of the same type");
 		}
 
 		final byte valType = mmapDef.getAttributeValueType(attributeName);
 
 		if (valType < 0) {
 			mmapDef.defineAttribute(attributeName, type,
 			                        new byte[] { MultiHashMapDefinition.TYPE_INTEGER });
 		} else {
 			if (valType != type) {
 				throw new IllegalArgumentException("existing definition for attributeName '"
 				                                   + attributeName
 				                                   + "' is a TYPE_SIMPLE_LIST that stores other value types");
 			}
 
 			final byte[] keyTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 			if ((keyTypes.length != 1) || (keyTypes[0] != MultiHashMapDefinition.TYPE_INTEGER)) {
 				throw new IllegalArgumentException("existing definition for attributeName '"
 				                                   + attributeName + "' is not of TYPE_SIMPLE_LIST");
 			}
 		}
 
 		mmap.removeAllAttributeValues(id, attributeName);
 		itor = list.iterator();
 
 		int inx = 0;
 		final Object[] key = new Object[1];
 
 		while (itor.hasNext()) {
 			key[0] = new Integer(inx++);
 			mmap.setAttributeValue(id, attributeName, itor.next(), key);
 		}
 	}
 
 	public void setListAttribute(final String id, final String attributeName, final Equation equation) {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		if (attributeName == null)
 			throw new IllegalArgumentException("attributeName is null");
 
 		if (equation == null)
 			throw new IllegalArgumentException("equation is null");
 
 		final byte type;
 		final Class returnType = equation.getType();
 		if (returnType == DoubleList.class)
 			type = TYPE_FLOATING;
 		else if (returnType == LongList.class)
 			type = TYPE_INTEGER;
 		else if (returnType == BooleanList.class)
 			type = TYPE_BOOLEAN;
 		else if (returnType == StringList.class)
 			type = TYPE_STRING;
 		else
 			throw new IllegalArgumentException("objects in list are of unrecognized type");
 
 		final byte valType = mmapDef.getAttributeValueType(attributeName);
 		if (valType < 0) {
 			mmapDef.defineAttribute(attributeName, type,
 			                        new byte[] { MultiHashMapDefinition.TYPE_INTEGER });
 		} else {
 			if (valType != type) {
 				throw new IllegalArgumentException("existing definition for attributeName '"
 				                                   + attributeName
 				                                   + "' is a TYPE_SIMPLE_LIST that stores other value types");
 			}
 
 			final byte[] keyTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 			if ((keyTypes.length != 1) || (keyTypes[0] != MultiHashMapDefinition.TYPE_INTEGER)) {
 				throw new IllegalArgumentException("existing definition for attributeName '"
 				                                   + attributeName + "' is not of TYPE_SIMPLE_LIST");
 			}
 		}
 
 		mmap.removeAllAttributeValues(id, attributeName);
 		final Object[] key = new Object[] { new Integer(-1) };
 		mmap.setAttributeValue(id, attributeName, equation, key);
 	}
 
 	// deprecated
 	public List getAttributeList(String id, String attributeName) {
 		return getListAttribute(id, attributeName);
 	}
 
 	public List getListAttribute(final String id, final String attributeName) {
 		lastEquationError = null;
 
 		if (mmapDef.getAttributeValueType(attributeName) < 0)
 			return null;
 
 		final byte[] keyTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 		if ((keyTypes.length != 1) || (keyTypes[0] != MultiHashMapDefinition.TYPE_INTEGER))
 			throw new ClassCastException("attributeName '" + attributeName
 			                             + "' is not of TYPE_SIMPLE_LIST");
 
 		final Object equation = mmap.getAttributeValue(id, attributeName, new Object[] { new Integer(-1) });
 		if (equation != null) {
 			final StringBuilder errorMessage = new StringBuilder();
 			final Object equationValue = evalEquation(id, attributeName, (Equation)equation,
 			                                          errorMessage);
 			if (equationValue == null) {
 				lastEquationError = errorMessage.toString();
 				return null;
 			}
 
 			// Map back to Integer from Long, if necessary:
 			if (equationValue instanceof LongList) {
 				final List<Integer> intList = new ArrayList<Integer>();
 				for (final Long l : (LongList)equationValue)
 					intList.add((int)(long)l);
 
 				return intList;
 			}
 
 			return (List)equationValue;
 		}
 
 		final ArrayList returnThis = new ArrayList();
 		final Object[] key = new Object[1];
 
 		for (int i = 0;; i++) {
 			key[0] = new Integer(i);
 
 			final Object val = mmap.getAttributeValue(id, attributeName, key);
 
 			if (val == null)
 				break;
 
 			returnThis.add(i, val);
 		}
 
 		return returnThis;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 * @param map DOCUMENT ME!
 	 */
 	public void setMapAttribute(String id, String attributeName, Map map) {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		if (attributeName == null)
 			throw new IllegalArgumentException("attributeName is null");
 
 		final Set entrySet = map.entrySet();
 		Iterator itor = entrySet.iterator();
 
 		if (!itor.hasNext()) {
 			return;
 		}
 
 		final byte type;
 		Map.Entry entry = (Map.Entry) itor.next();
 
 		if (!(entry.getKey() instanceof String)) {
 			throw new IllegalArgumentException("keys in map are not all String");
 		}
 
 		Object val = entry.getValue();
 
 		if (val instanceof Double) {
 			type = TYPE_FLOATING;
 		} else if (val instanceof Integer) {
 			type = TYPE_INTEGER;
 		} else if (val instanceof Boolean) {
 			type = TYPE_BOOLEAN;
 		} else if (val instanceof String) {
 			type = TYPE_STRING;
 		} else
 			throw new IllegalArgumentException("values in map are of unrecognized type");
 
 		while (itor.hasNext()) {
 			entry = (Map.Entry) itor.next();
 
 			if (!(entry.getKey() instanceof String)) {
 				throw new IllegalArgumentException("keys in map are not all String");
 			}
 
 			val = entry.getValue();
 
 			if (((type == TYPE_FLOATING) && (!(val instanceof Double)))
 			    || ((type == TYPE_INTEGER) && (!(val instanceof Integer)))
 			    || ((type == TYPE_BOOLEAN) && (!(val instanceof Boolean)))
 			    || ((type == TYPE_STRING) && (!(val instanceof String))))
 				throw new IllegalArgumentException("values in map are not all of the same type");
 		}
 
 		final byte valType = mmapDef.getAttributeValueType(attributeName);
 
 		if (valType < 0) {
 			mmapDef.defineAttribute(attributeName, type,
 			                        new byte[] { MultiHashMapDefinition.TYPE_STRING });
 		} else {
 			if (valType != type) {
 				throw new IllegalArgumentException("existing definition for attributeName '"
 				                                   + attributeName
 				                                   + "' is a TYPE_SIMPLE_MAP that stores other value types");
 			}
 
 			final byte[] keyTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 			if ((keyTypes.length != 1) || (keyTypes[0] != MultiHashMapDefinition.TYPE_STRING)) {
 				throw new IllegalArgumentException("existing definition for attributeName '"
 				                                   + attributeName + "' is not of TYPE_SIMPLE_MAP");
 			}
 		}
 
 		mmap.removeAllAttributeValues(id, attributeName);
 		itor = entrySet.iterator();
 
 		final Object[] key = new Object[1];
 
 		while (itor.hasNext()) {
 			entry = (Map.Entry) itor.next();
 			key[0] = entry.getKey();
 			mmap.setAttributeValue(id, attributeName, entry.getValue(), key);
 		}
 	}
 
 	// deprecated
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Map getAttributeMap(String id, String attributeName) {
 		return getMapAttribute(id, attributeName);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param id DOCUMENT ME!
 	 * @param attributeName DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Map getMapAttribute(String id, String attributeName) {
 		lastEquationError = null;
 
 		if (mmapDef.getAttributeValueType(attributeName) < 0)
 			return null;
 
 		final byte[] keyTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 		if ((keyTypes.length != 1) || (keyTypes[0] != MultiHashMapDefinition.TYPE_STRING)) {
 			throw new ClassCastException("attributeName '" + attributeName
 			                             + "' is not of TYPE_SIMPLE_MAP");
 		}
 
 		final Map returnThis = new HashMap();
 		final Iterator keyspan = mmap.getAttributeKeyspan(id, attributeName, null);
 		final Object[] key = new Object[1];
 
 		while (keyspan.hasNext()) {
 			key[0] = keyspan.next();
 			returnThis.put(key[0], mmap.getAttributeValue(id, attributeName, key));
 		}
 
 		return returnThis;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public MultiHashMap getMultiHashMap() {
 		return mmap;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public MultiHashMapDefinition getMultiHashMapDefinition() {
 		return mmapDef;
 	}
 
 	/**
 	 *  @return the equation associated with an attribute or null if there is no equation associated with it
 	 */
 	public Equation getEquation(final String id, final String attributeName) {
 		// This check is necessary for when "attributeName" does not actually refer to an attribute!
 		final byte type = getType(attributeName);
 		if (type == TYPE_UNDEFINED)
 			return null;
 
 		if (type == TYPE_SIMPLE_LIST) {
 			final Object equation = mmap.getAttributeValue(id, attributeName, new Object[] { new Integer(-1) });
 			return (equation == null) ? null : (Equation)equation;
 		}
 
 		final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 		if (dimTypes.length > 0)
 			return null;
 
 		final Object attribValue = mmap.getAttributeValue(id, attributeName, null);
 		if (attribValue == null || !(attribValue instanceof Equation))
 			return null;
 
 		return (Equation)attribValue;
 	}
 
 	/**
 	 *  Returns any attribute-equation related error message after a call to getAttribute() or
 	 *  getXXXAttribute().  N.B., the last error message will be cached!
 	 *
 	 *  @return an error message or null if the last call to getAttribute() did not result in an
 	 *           equation related error
 	 */
 	public String getLastEquationError() { return lastEquationError; }
 
 	private Object evalEquation(final String id, final String attribName, final Equation equation,
 	                            final StringBuilder errorMessage)
 	{
 		final Collection<String> attribReferences = equation.getAttribReferences();
 
 		final Map<String, IdentDescriptor> nameToDescriptorMap = new TreeMap<String, IdentDescriptor>();
 		for (final String attribRef : attribReferences) {
 			if (attribRef.equals("ID")) {
 				nameToDescriptorMap.put("ID", new IdentDescriptor(id));
 				continue;
 			}
 
 			final Object attribValue = getAttribute(id, attribRef);
 			if (attribValue == null) {
 				errorMessage.append("Missing value for referenced attribute \"" + attribRef + "\"!");
 				logger.warn("Missing value for \"" + attribRef
 				            + "\" while evaluating an equation (ID:" + id
 				            + ", attribute name:" + attribName + ")");
 				return null;
 			}
 
 			try {
 				nameToDescriptorMap.put(attribRef, new IdentDescriptor(attribValue));
 			} catch (final Exception e) {
 				errorMessage.append("Bad attribute reference to \"" + attribRef + "\"!");
 				logger.warn("Bad attribute reference to \"" + attribRef
 				            + "\" while evaluating an equation (ID:" + id
 				            + ", attribute name:" + attribName + ")");
 				return null;
 			}
 		}
 
 		final Interpreter interpreter = new Interpreter(equation, nameToDescriptorMap);
 		try {
			return interpreter.run();
 		} catch (final Exception e) {
 			errorMessage.append(e.getMessage());
 			logger.warn("Error while evaluating an equation: " + e.getMessage() + " (ID:"
 			            + id + ", attribute name:" + attribName + ")");
 			return null;
 		}
 	}
 
 	/**
 	 *  @return an in-order list of attribute names that will have to be evaluated before "attribName" can be evaluated
 	 */
 	private List<String> topoSortAttribReferences(final String id, final String attribName) {
 		final Object equationCandidate = mmap.getAttributeValue(id, attribName, null);
 		if (!(equationCandidate instanceof Equation))
 			return new ArrayList<String>();
 
 		final Equation equation = (Equation)equationCandidate;
 		final Set<String> attribReferences = equation.getAttribReferences();
 		if (attribReferences.size() == 0)
 			return new ArrayList<String>();
 
 		final Set<String> alreadyProcessed = new TreeSet<String>();
 		alreadyProcessed.add(attribName);
 		final List<TopoGraphNode> dependencies = new ArrayList<TopoGraphNode>();
 		for (final String attribReference : attribReferences)
                         followReferences(id, attribReference, alreadyProcessed, dependencies);
 
 
 		final List<TopoGraphNode> topoOrder = TopologicalSort.sort(dependencies);
 		final List<String> retVal = new ArrayList<String>();
 		for (final TopoGraphNode node : topoOrder) {
 			final AttribTopoGraphNode attribTopoGraphNode = (AttribTopoGraphNode)node;
 			final String nodeName = attribTopoGraphNode.getNodeName();
 			if (nodeName.equals(attribName))
 				return retVal;
 			else
 				retVal.add(nodeName);
 		}
 
 		// We should never get here because "attribName" should have been found in the for-loop above!
 		throw new IllegalStateException("\"" + attribName
 		                                + "\" was not found in the toplogical order, which should be impossible!");
 	}
 
 	/**
 	 *  Helper function for topoSortAttribReferences() performing a depth-first search of equation evaluation dependencies.
 	 */
 	private void followReferences(final String id, final String attribName, final Collection<String> alreadyProcessed,
 	                              final Collection<TopoGraphNode> dependencies)
 	{
 		// Already visited this attribute?
 		if (alreadyProcessed.contains(attribName))
 			return;
 
 		alreadyProcessed.add(attribName);
 		final Object equationCandidate = mmap.getAttributeValue(id, attribName, null);
 		if (!(equationCandidate instanceof Equation))
 			return;
 
 		final Equation equation = (Equation)equationCandidate;
 		final Set<String> attribReferences = equation.getAttribReferences();
 		for (final String attribReference : attribReferences)
 			followReferences(id, attribReference, alreadyProcessed, dependencies);
 	}
 
 	/**
 	 *  @return "x" truncated using Excel's notion of truncation.
 	 */
 	private static double excelTrunc(final double x) {
 		final boolean isNegative = x < 0.0;
 		return Math.round(x + (isNegative ? +0.5 : -0.5));
 	}
 
 	/**
 	 *  @return "d" converted to an Integer using Excel rules, should the number be outside the range of an int, null will be returned
 	 */
 	private static Integer doubleToInteger(final double d) {
 		if (d > Integer.MAX_VALUE || d < Integer.MIN_VALUE)
 			return null;
 
 		double x = ((Double)d).intValue();
 		if (x != d && x < 0.0)
 			--x;
 
 		return (Integer)(int)x;
 	}
 
 	/**
 	 *  @return "l" converted to an Integer using Excel rules, should the number be outside the range of an int, null will be returned
 	 */
 	private static Integer longToInteger(final double l) {
 		if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE)
 			return (Integer)(int)l;
 
 		return null;
 	}
 
 	/**
 	 *  @return "equationValue" interpreted according to Excel rules as an integer or null if that is not possible
 	 */
 	private Integer convertEqnRetValToInteger(final String id, final String attribName, final Object equationValue) {
 		if (equationValue.getClass() == Double.class) {
 			final Integer retVal = doubleToInteger((Double)equationValue);
 			if (retVal == null)
 				logger.warn("Cannot convert a floating point value ("
 					    + equationValue + ") to an integer!  (ID:" + id
 					    + ", attribute name:" + attribName + ")");
 			return retVal;
 		}
 		else if (equationValue.getClass() == Long.class) {
 			final Integer retVal = longToInteger((Long)equationValue);
 			if (retVal == null)
 				logger.warn("Cannot convert a large integer (long) value ("
 					    + equationValue + ") to an integer! (ID:" + id
 					    + ", attribute name:" + attribName + ")");
 			return retVal;
 		}
 		else if (equationValue.getClass() == Boolean.class) {
 			final Boolean boolValue = (Boolean)equationValue;
 			return (Integer)(boolValue ? 1 : 0);
 		}
 		else
 			throw new IllegalStateException("we should never get here!");
 	}
 
 	/**
 	 *  @return "equationValue" interpreted according to Excel rules as a double or null if that is not possible
 	 */
 	private Double convertEqnRetValToDouble(final String id, final String attribName, final Object equationValue) {
 		if (equationValue.getClass() == Double.class)
 			return (Double)equationValue;
 		else if (equationValue.getClass() == Long.class)
 			return (double)(Long)(equationValue);
 		else if (equationValue.getClass() == Boolean.class) {
 			final Boolean boolValue = (Boolean)equationValue;
 			return boolValue ? 1.0 : 0.0;
 		}
 		else if (equationValue.getClass() == String.class) {
 			final String valueAsString = (String)equationValue;
 			try {
 				return Double.parseDouble(valueAsString);
 			} catch (final NumberFormatException e) {
 				logger.warn("Cannot convert a string (\"" + valueAsString
 				            + "\") to a floating point value! (ID:" + id
                                             + ", attribute name:" + attribName + ")");
 				return null;
 			}
 		}
 		else
 			throw new IllegalStateException("we should never get here!");
 	}
 
 	/**
 	 *  @return "equationValue" interpreted according to Excel rules as a boolean
 	 */
 	private Boolean convertEqnRetValToBoolean(final String id, final String attribName, final Object equationValue) {
 		if (equationValue.getClass() == Double.class)
 			return (Double)equationValue != 0.0;
 		else if (equationValue.getClass() == Long.class)
 			return (Long)(equationValue) != 0L;
 		else if (equationValue.getClass() == Boolean.class) {
 			return (Boolean)equationValue;
 		}
 		else if (equationValue.getClass() == String.class) {
 			final String stringValue = (String)equationValue;
 			if (stringValue.compareToIgnoreCase("true") == 0)
 				return true;
 			else if (stringValue.compareToIgnoreCase("false") == 0)
 				return false;
 			else {
 				logger.warn("Cannot convert a string (\"" + stringValue
 				            + "\") to a boolean value! (ID:" + id
                                             + ", attribute name:" + attribName + ")");
 				return null;
 			}
 		}
 		else
 			throw new IllegalStateException("we should never get here!");
 	}
 }
