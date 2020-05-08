 /**
  * Copyright (c) 2005, 2011, Werner Keil, JScience and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil, Eric Russell - initial API and implementation
  */
 package org.eclipse.uomo.units.impl.format;
 
 import java.lang.reflect.Field;
 import java.math.BigInteger;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.eclipse.uomo.units.AbstractUnit;
 import org.eclipse.uomo.units.SymbolMap;
 import org.eclipse.uomo.units.impl.MultiplyConverter;
 import org.eclipse.uomo.units.impl.RationalConverter;
 import org.unitsofmeasurement.unit.Unit;
 import org.unitsofmeasurement.unit.UnitConverter;
 
 
 /**
  * <p> This class holds the default implementation of the SymbolMap
  *     interface.</p>
  *
  * <p> No attempt is made to verify the uniqueness of the mappings.</p>
  *
  * <p> Mappings are read from a <code>ResourceBundle</code>, the keys
  *     of which should consist of a fully-qualified class name, followed
  *     by a dot ('.'), and then the name of a static field belonging
  *     to that class, followed optionally by another dot and a number.
  *     If the trailing dot and number are not present, the value
  *     associated with the key is treated as a
  *     {@linkplain SymbolMap#label(org.unitsofmeasure.Unit, String) label},
  *     otherwise if the trailing dot and number are present, the value
  *     is treated as an {@linkplain SymbolMap#alias(org.unitsofmeasure.Unit,String) alias}.
  *     Aliases map from String to Unit only, whereas labels map in both
  *     directions. A given unit may have any number of aliases, but may
  *     have only one label.</p>
  *
  * @author <a href="mailto:eric-r@northwestern.edu">Eric Russell</a>
  * @author  <a href="mailto:uomo@catmedia.us">Werner Keil</a>
 * @version 1.7 ($Revision: 212 $), $Date: 2010-09-13 23:50:44 +0200 (Mo, 13 Sep 2010) $
  */
 class SymbolMapImpl implements SymbolMap {

     private final Map<String, Unit<?>> symbolToUnit;
     private final Map<Unit<?>, String> unitToSymbol;
     private final Map<String, ParsePrefix> symbolToPrefix;
    private final Map<Object, String> prefixToSymbol;
     private final Map<UnitConverter, ParsePrefix> converterToPrefix;
 
     /**
      * Creates an empty mapping.
      */
     public SymbolMapImpl () {
         symbolToUnit = new HashMap<String, Unit<?>>();
         unitToSymbol = new HashMap<Unit<?>, String>();
         symbolToPrefix = new HashMap<String, ParsePrefix>();
        prefixToSymbol = new HashMap<Object, String>();
         converterToPrefix = new HashMap<UnitConverter, ParsePrefix>();
     }
 
     /**
      * Creates a symbol map from the specified resource bundle,
      *
      * @param rb the resource bundle.
      */
     public SymbolMapImpl (ResourceBundle rb) {
         this();
         for (Enumeration<String> i = rb.getKeys(); i.hasMoreElements();) {
             String fqn = i.nextElement();
             String symbol = rb.getString(fqn);
             boolean isAlias = false;
             int lastDot = fqn.lastIndexOf('.');
             String className = fqn.substring(0, lastDot);
             String fieldName = fqn.substring(lastDot+1, fqn.length());
             if (Character.isDigit(fieldName.charAt(0))) {
                 isAlias = true;
                 fqn = className;
                 lastDot = fqn.lastIndexOf('.');
                 className = fqn.substring(0, lastDot);
                 fieldName = fqn.substring(lastDot+1, fqn.length());
             }
             try {
                 Class<?> c = Class.forName(className);
                 Field field = c.getField(fieldName);
                 Object value = field.get(null);
                 if (value instanceof AbstractUnit<?>) {
                     if (isAlias) {
                         alias((AbstractUnit<?>)value, symbol);
                     } else {
                         label((AbstractUnit<?>)value, symbol);
                     }
                 } else if (value instanceof ParsePrefix) {
                     label((ParsePrefix)value, symbol);
                 } else {
                     throw new ClassCastException("" + value + " to Unit or Prefix");  //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$
                 }
             } catch (Exception e) {
                 System.err.println("Error reading Unit names: " + e.toString()); //$NON-NLS-1$
             }
         }
     }
 
 //    public void label (Unit<?> unit, String symbol) {
 //        symbolToUnit.put(symbol, unit);
 //        unitToSymbol.put(unit, symbol);
 //    }
 //
 //    public void alias (Unit<?> unit, String symbol) {
 //        symbolToUnit.put(symbol, unit);
 //    }
 
 
     public Unit<?> getUnit (String symbol) {
         return symbolToUnit.get(symbol);
     }
 
 //    public String getSymbol (Unit<?> unit) {
 //        return unitToSymbol.get(unit);
 //    }
 
    public UnitConverter getConverter(String prefix) {
         ParsePrefix prefixObject = symbolToPrefix.get(prefix);
         if (prefixObject == null) return null;
         return prefixObject.getConverter();
     }
 
     /**
      * Attaches a label to the specified prefix. For example:[code]
      *    symbolMap.label(Prefix.GIGA, "G");
      *    symbolMap.label(Prefix.MICRO, "µ");
      * [/code]
      */
     void label(ParsePrefix prefix, String symbol) {
         symbolToPrefix.put(symbol, prefix);
         prefixToSymbol.put(prefix, symbol);
         converterToPrefix.put(prefix.getConverter(), prefix);
         // adding MultiplyConverters (ensuring KILO(METRE) = METRE.multiply(1000)
         if (prefix.getConverter() instanceof RationalConverter) {
         	RationalConverter rc = (RationalConverter)prefix.getConverter();
         	if (rc.getDividend() != null && BigInteger.ONE.equals(rc.getDivisor())) {
         		converterToPrefix.put(new MultiplyConverter(rc.getDividend().doubleValue()), prefix);
         	} else if (rc.getDivisor() != null && BigInteger.ONE.equals(rc.getDividend())) {
         		converterToPrefix.put(new MultiplyConverter(1d / rc.getDivisor().doubleValue()), prefix);
         	}
         }
     }
 
     /**
      * Returns the prefix (if any) for the specified symbol.
      *
      * @param symbol the unit symbol.
      * @return the corresponding prefix or <code>null</code> if none.
      */
     ParsePrefix getPrefix (String symbol) {
         for (Iterator<String> i = symbolToPrefix.keySet().iterator(); i.hasNext(); ) {
             String pfSymbol = i.next();
             if (symbol.startsWith(pfSymbol)) {
                 return symbolToPrefix.get(pfSymbol);
             }
         }
         return null;
     }
 
     /**
      * Returns the prefix for the specified converter.
      *
      * @param converter the unit converter.
      * @return the corresponding prefix or <code>null</code> if none.
      */
     ParsePrefix getPrefixObject (UnitConverter converter) {
         return converterToPrefix.get(converter);
     }
 
     /**
      * Returns the symbol for the specified prefix.
      *
      * @param prefix the prefix.
      * @return the corresponding symbol or <code>null</code> if none.
      */
     String getSymbol (ParsePrefix prefix) {
         return prefixToSymbol.get(prefix);
     }
 
 	public void alias(Unit<?> unit, String symbol) {
 		symbolToUnit.put(symbol, unit);
 	}
 
 	public String getPrefix(UnitConverter converter) {
         ParsePrefix prefix = getPrefixObject(converter);
         if (prefix == null) return null;
         return prefixToSymbol.get(prefix);
 	}
 
 	public String getSymbol(Unit<?> unit) {
 		return unitToSymbol.get(unit);
 	}
 
 	public void label(Unit<?> unit, String symbol) {
         symbolToUnit.put(symbol, unit);
         unitToSymbol.put(unit, symbol);
 	}
 
 	public void prefix(UnitConverter cvtr, String prefix) {
 		throw new UnsupportedOperationException("Prefixes are not modifiable"); //$NON-NLS-1$
 	}
 }
