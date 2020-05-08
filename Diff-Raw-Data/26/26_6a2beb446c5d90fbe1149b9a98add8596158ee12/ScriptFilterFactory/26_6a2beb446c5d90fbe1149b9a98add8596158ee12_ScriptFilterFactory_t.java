 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package de.unioninvestment.eai.portal.portlet.crud.scripting.model;
 
 import static java.util.Collections.emptyList;
 import groovy.lang.Closure;
 import groovy.lang.GString;
 
 import java.util.List;
 import java.util.Map;
 
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.All;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Any;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Contains;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.EndsWith;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Equal;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Filter;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Greater;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Less;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Nothing;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.RegExpFilter;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.SQLFilter;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.StartsWith;
 import de.unioninvestment.eai.portal.support.scripting.ScriptSQLWhereFactory;
 
 /**
  * Liste unterstützter Filter in
  * {@link ScriptDatabaseContainer#addFilters(groovy.lang.Closure)}.
  * 
  * 
  * @author carsten.mjartan
  */
 public class ScriptFilterFactory {
 
 	private final List<Filter> filters;
 
 	/**
 	 * Konstruktor mit Parameter.
 	 * 
 	 * @param filters
 	 *            Liste für Filter, die bei Aufrufen dieser Instanz gefüllt
 	 *            wird.
 	 */
 	ScriptFilterFactory(List<Filter> filters) {
 		this.filters = filters;
 	}
 
 	/**
 	 * Filtert Zeilen, bei denen die angegebene Spalte nicht dem Wert
 	 * entspricht.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void equal(String columnName, Object value) {
 		filters.add(new Equal(columnName, value));
 	}
 
 	/**
 	 * Filtert Zeilen, bei denen die angegebene Spalte nicht dem Wert
 	 * entspricht.
 	 * 
 	 * @param namedArgs
 	 *            Liste optionaler Named-Parameters (z.B. im Skript
 	 *            'durable:true')
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void equal(Map<String, Object> namedArgs, String columnName,
 			Object value) {
 		filters.add(new Equal(columnName, value, durable(namedArgs)));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte größer als der
 	 * angegebene Wert ist.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void greater(String columnName, Object value) {
 		filters.add(new Greater(columnName, value, false));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte größer als der
 	 * angegebene Wert ist.
 	 * 
 	 * @param namedArgs
 	 *            Liste optionaler Named-Parameters (z.B. im Skript
 	 *            'durable:true')
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void greater(Map<String, Object> namedArgs, String columnName,
 			Object value) {
 		filters.add(new Greater(columnName, value, false, durable(namedArgs)));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte größer oder gleich
 	 * dem angegebenen Wert ist.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void greaterOrEqual(String columnName, Object value) {
 		filters.add(new Greater(columnName, value, true));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte größer oder gleich
 	 * dem angegebenen Wert ist.
 	 * 
 	 * @param namedArgs
 	 *            Liste optionaler Named-Parameters (z.B. im Skript
 	 *            'durable:true')
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void greaterOrEqual(Map<String, Object> namedArgs,
 			String columnName, Object value) {
 		filters.add(new Greater(columnName, value, true, durable(namedArgs)));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte kleiner als der
 	 * angegebene Wert ist.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void less(String columnName, Object value) {
 		filters.add(new Less(columnName, value, false));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte kleiner als der
 	 * angegebene Wert ist.
 	 * 
 	 * @param namedArgs
 	 *            Liste optionaler Named-Parameters (z.B. im Skript
 	 *            'durable:true')
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void less(Map<String, Object> namedArgs, String columnName,
 			Object value) {
 		filters.add(new Less(columnName, value, false, durable(namedArgs)));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte kleiner oder
 	 * gleich dem angegebenen Wert ist.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void lessOrEqual(String columnName, Object value) {
 		filters.add(new Less(columnName, value, true));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte kleiner oder
 	 * gleich dem angegebenen Wert ist.
 	 * 
 	 * @param namedArgs
 	 *            Liste optionaler Named-Parameters (z.B. im Skript
 	 *            'durable:true')
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void lessOrEqual(Map<String, Object> namedArgs, String columnName,
 			Object value) {
 		filters.add(new Less(columnName, value, true, durable(namedArgs)));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte mit dem
 	 * angegebenen Text beginnt. Die Suche erfolgt case-sensitive.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void startsWith(String columnName, String value) {
 		filters.add(new StartsWith(columnName, value, false));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte mit dem
 	 * angegebenen Text beginnt. Die Suche erfolgt case-sensitive.
 	 * 
 	 * @param namedArguments
 	 *            Liste von optionalen Named Arguments
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void startsWith(Map<String, Object> namedArguments,
 			String columnName, String value) {
 		filters.add(new StartsWith(columnName, value, false,
 				durable(namedArguments)));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte mit dem
 	 * angegebenen Text beginnt.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 * @param caseSensitive
 	 *            wenn <code>true</code>, dann wird Groß-/Kleinschreibung
 	 *            unterschieden
 	 */
 	public void startsWith(String columnName, String value,
 			boolean caseSensitive) {
 		filters.add(new StartsWith(columnName, value, caseSensitive));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte mit dem
 	 * angegebenen Text beginnt.
 	 * 
 	 * @param namedArguments
 	 *            Liste von optionalen Named Arguments
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 * @param caseSensitive
 	 *            wenn <code>true</code>, dann wird Groß-/Kleinschreibung
 	 *            unterschieden
 	 */
 	public void startsWith(Map<String, Object> namedArguments,
 			String columnName, String value, boolean caseSensitive) {
 		filters.add(new StartsWith(columnName, value, caseSensitive,
 				durable(namedArguments)));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte mit dem
 	 * angegebenen Text endet.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 * @param caseSensitive
 	 *            wenn <code>true</code>, dann wird Groß-/Kleinschreibung
 	 *            unterschieden
 	 */
 	public void endsWith(String columnName, String value, boolean caseSensitive) {
 		filters.add(new EndsWith(columnName, value, caseSensitive));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte mit dem
 	 * angegebenen Text endet.
 	 * 
 	 * @param namedArguments
 	 *            Liste optionaler Named Arguments
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 * @param caseSensitive
 	 *            wenn <code>true</code>, dann wird Groß-/Kleinschreibung
 	 *            unterschieden
 	 */
 	public void endsWith(Map<String, Object> namedArguments, String columnName,
 			String value, boolean caseSensitive) {
 		filters.add(new EndsWith(columnName, value, caseSensitive));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte mit dem
 	 * angegebenen Text endet. Die Suche erfolgt case-sensitive.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void endsWith(String columnName, String value) {
 		filters.add(new EndsWith(columnName, value, false));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte mit dem
 	 * angegebenen Text endet. Die Suche erfolgt case-sensitive.
 	 * 
 	 * @param namedArguments
 	 *            Liste optionaler Named Arguments
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void endsWith(Map<String, Object> namedArguments, String columnName,
 			String value) {
 		filters.add(new EndsWith(columnName, value, false,
 				durable(namedArguments)));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte den angegebenen
 	 * Text enthält.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 * @param caseSensitive
 	 *            wenn <code>true</code>, dann wird Groß-/Kleinschreibung
 	 *            unterschieden
 	 */
 	public void contains(String columnName, String value, boolean caseSensitive) {
 		filters.add(new Contains(columnName, value, caseSensitive));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte den angegebenen
 	 * Text enthält.
 	 * 
 	 * @param namedArguments
 	 *            Liste optionaler Named Arguments
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 * @param caseSensitive
 	 *            wenn <code>true</code>, dann wird Groß-/Kleinschreibung
 	 *            unterschieden
 	 */
 	public void contains(Map<String, Object> namedArguments, String columnName,
 			String value, boolean caseSensitive) {
 		filters.add(new Contains(columnName, value, caseSensitive));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte den angegebenen
 	 * Text enthält. Die Suche erfolgt case-sensitive.
 	 * 
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void contains(String columnName, String value) {
 		filters.add(new Contains(columnName, value, false));
 	}
 
 	/**
 	 * Akzeptiert nur Zeilen, bei denen die angegebene Spalte den angegebenen
 	 * Text enthält. Die Suche erfolgt case-sensitive.
 	 * 
 	 * @param namedArguments
 	 *            Liste optionaler Named Arguments
 	 * @param columnName
 	 *            die zu filternde Spalte
 	 * @param value
 	 *            Vergleichswert
 	 */
 	public void contains(Map<String, Object> namedArguments, String columnName,
 			String value) {
 		filters.add(new Contains(columnName, value, false,
 				durable(namedArguments)));
 	}
 
 	/**
 	 * Akzeptiert eine Closure mit Filtern, von denen nur einer zutreffen muss.
 	 * 
 	 * @param closure
 	 *            eine Closure die wiederum Filter-Kommandos enthalten kann
 	 */
 	public void any(Closure<?> closure) {
 		filters.add(new Any(new FilterClosureCallable(closure).call()));
 	}
 
 	/**
 	 * Akzeptiert eine Closure mit Filtern, von denen nur einer zutreffen muss.
 	 * 
 	 * @param namedArgs
 	 *            Liste optionaler Named-Parameters (z.B. im Skript
 	 *            'durable:true')
 	 * @param closure
 	 *            eine Closure die wiederum Filter-Kommandos enthalten kann
 	 */
 	public void any(Map<String, Object> namedArgs, Closure<?> closure) {
 		filters.add(new Any(new FilterClosureCallable(closure).call(),
 				durable(namedArgs)));
 	}
 
 	/**
 	 * Akzeptiert eine Closure mit Filtern, von denen alle zutreffen müssen.
 	 * 
 	 * @param closure
 	 *            eine Closure die wiederum Filter-Kommandos enthalten kann
 	 */
 	public void all(Closure<?> closure) {
 		filters.add(new All(new FilterClosureCallable(closure).call()));
 	}
 
 	/**
 	 * Akzeptiert eine Closure mit Filtern, von denen alle zutreffen müssen.
 	 * 
 	 * @param namedArguments
 	 *            Liste optionaler Named Arguments
 	 * @param closure
 	 *            eine Closure die wiederum Filter-Kommandos enthalten kann
 	 */
 	public void all(Map<String, Object> namedArguments, Closure<?> closure) {
 		filters.add(new All(new FilterClosureCallable(closure).call(),
 				durable(namedArguments)));
 	}
 
 	/**
 	 * Akzeptiert nix, zeigt nix an.
 	 */
 	public void nothing() {
 		filters.add(new Nothing());
 	}
 
 	/**
 	 * Akzeptiert nix, zeigt nix an.
 	 */
 	public void nothing(Map<String, Object> args) {
 		filters.add(new Nothing(durable(args)));
 	}
 
 	private boolean durable(Map<String, Object> args) {
 		return arg(args, "durable", Boolean.FALSE).equals(Boolean.TRUE);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> T arg(Map<String, Object> args, String attribute, T defaultValue) {
 		if (args == null) {
 			return defaultValue;
 		} else {
 			Object value = args.get(attribute);
 			if (value == null) {
 				return defaultValue;
 			} else {
 				return (T) value;
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param columnName
 	 *            Spaltenname
 	 * @param where
 	 *            where-Bedingung als GString
 	 */
 	public void where(String columnName, GString where) {
		SQLFilter filter = new ScriptSQLWhereFactory().createFilter(columnName, where,
				false);
		if (filter == null) {
			throw new IllegalArgumentException("NULL value in SQL-where filter criteria '"+ where + "' is not supported");
		}
		filters.add(filter);
 	}
 
 	/**
 	 * 
 	 * @param where
 	 *            where-Bedingung als GString
 	 */
 	public void where(GString where) {
		SQLFilter filter = new ScriptSQLWhereFactory().createFilter(null, where,
				false);
		if (filter == null) {
			throw new IllegalArgumentException("NULL value in SQL-where filter criteria '"+ where + "' is not supported");
		}
		filters.add(filter);
 	}
 	
 	/**
 	 * 
 	 * @param namedArguments
 	 *            Parameter Bsp. "durable":true
 	 * @param columnName
 	 *            Spaltenname
 	 * @param where
 	 *            where-Bedingung als GString
 	 */
 	public void where(Map<String, Object> namedArguments, String columnName,
 			GString where) {
		SQLFilter filter = new ScriptSQLWhereFactory().createFilter(columnName, where,
						durable(namedArguments));
		if (filter == null) {
			throw new IllegalArgumentException("NULL value in SQL-where filter criteria '"+ where + "' is not supported");
		}
		filters.add(filter);
 	}
 
 	/**
 	 * 
 	 * @param columnName
 	 *            Spaltenname
 	 * @param where
 	 *            where-Bedingung als String
 	 */
 	public void where(String columnName, String where) {
 		filters.add(new SQLFilter(columnName, where, emptyList()));
 	}
 
 	/**
 	 * 
 	 * @param where
 	 *            where-Bedingung als String
 	 */
 	public void where(String where) {
 		filters.add(new SQLFilter(null, where, emptyList()));
 	}
 	
 	/**
 	 * 
 	 * @param namedArguments
 	 *            Parameter Bsp. "durable":true
 	 * @param columnName
 	 *            Spaltenname
 	 * @param where
 	 *            where-Bedingung als GString
 	 */
 	public void where(Map<String, Object> namedArguments, String where) {
 		filters.add(new SQLFilter(null, where, emptyList(),
 				durable(namedArguments)));
 	}
 
 	/**
 	 * 
 	 * @param namedArguments
 	 *            Parameter Bsp. "durable":true
 	 * @param columnName
 	 *            Spaltenname
 	 * @param where
 	 *            where-Bedingung als GString
 	 */
 	public void where(Map<String, Object> namedArguments, String columnName,
 			String where) {
 		filters.add(new SQLFilter(columnName, where, emptyList(),
 				durable(namedArguments)));
 	}
 
 	public void regexp(String columnName, String pattern) {
 		filters.add(new RegExpFilter(columnName, pattern, null));
 	}
 
 	public void regexp(Map<String, Object> namedArguments, String columnName,
 			String pattern) {
 		String modifiers = arg(namedArguments, "modifiers", null);
 		boolean durable = durable(namedArguments);
 		filters.add(new RegExpFilter(columnName, pattern, modifiers, durable));
 	}
 }
