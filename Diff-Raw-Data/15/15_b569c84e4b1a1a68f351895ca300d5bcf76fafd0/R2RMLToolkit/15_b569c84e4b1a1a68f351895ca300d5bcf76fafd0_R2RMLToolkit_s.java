 /* 
  * Copyright 2011 Antidot opensource@antidot.net
  * https://github.com/antidot/db2triples
  * 
  * DB2Triples is free software; you can redistribute it and/or 
  * modify it under the terms of the GNU General Public License as 
  * published by the Free Software Foundation; either version 2 of 
  * the License, or (at your option) any later version.
  * 
  * DB2Triples is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /***************************************************************************
  *
  * R2RML Toolkit
  *
  * Collection of useful tool-methods used in R2RML 
  *
  ****************************************************************************/
 package net.antidot.semantic.rdf.rdb2rdf.r2rml.tools;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import net.antidot.semantic.rdf.rdb2rdf.commons.SQLToXMLS;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;
 import net.antidot.semantic.xmls.xsd.XSDLexicalTransformation;
 import net.antidot.semantic.xmls.xsd.XSDType;
 import net.antidot.sql.model.db.ColumnIdentifier;
 import net.antidot.sql.model.db.ColumnIdentifierImpl;
 import net.antidot.sql.model.type.SQLType;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public abstract class R2RMLToolkit {
 
 	// Log
 	private static Log log = LogFactory.getLog(R2RMLToolkit.class);
 
 	public static boolean checkCurlyBraces(String value) {
 		if (value == null)
 			// No brace : valid
 			return true;
 		char[] chars = value.toCharArray();
 		boolean openedBrace = false;
 		boolean emptyBraceContent = false;
 		boolean closedBrace = true;
 		for (char c : chars) {
 			switch (c) {
 			case '{':
 				// Already opened
 				if (openedBrace)
 					return false;
 				openedBrace = true;
 				closedBrace = false;
 				emptyBraceContent = true;
 				break;
 
 			case '}':
 				// Already closed or not opened or empty content
 				if (closedBrace || !openedBrace || emptyBraceContent)
 					return false;
 				openedBrace = false;
 				closedBrace = true;
 				emptyBraceContent = true;
 				break;
 
 			default:
 				if (openedBrace)
 					emptyBraceContent = false;
 				break;
 			}
 		}
 		// All curly braces have to be closed
 		return (!openedBrace && closedBrace);
 	}
 
 	public static boolean checkInverseExpression(String inverseExpression) {
 		// TODO
 		return true;
 	}
 
 	public static boolean checkStringTemplate(String stringTemplate) {
 		// TODO
 		return true;
 	}
 
 	/**
 	 * Extracts column names referenced by enclosing them in curly braces (“{”
 	 * and “}”) in a string template.
 	 * 
 	 * @param stringTemplate
 	 * @return
 	 */
 	public static Set<String> extractColumnNamesFromStringTemplate(
 			String stringTemplate) {
 		Set<String> result = new HashSet<String>();
 		// Curly braces that do not enclose column names MUST be
 		// escaped by a backslash character (“\”).
 		stringTemplate = stringTemplate.replaceAll("\\\\\\{", "");
 		stringTemplate = stringTemplate.replaceAll("\\\\\\}", "");
 		if (stringTemplate != null) {
 			StringTokenizer st = new StringTokenizer(stringTemplate, "{}", true);
 			boolean keepNext = false;
 			String next = null;
 			while (st.hasMoreElements()) {
 				String element = st.nextElement().toString();
 				if (keepNext)
 					next = element;
 				keepNext = element.equals("{");
 				if (element.equals("}") && element != null) {
 					log.debug("[R2RMLToolkit:extractColumnNamesFromStringTemplate] Extracted column name "
 							+ next + " from string template " + stringTemplate);
 					result.add(next);
 					next = null;
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Every pair of unescaped curly braces in the inverse expression is a
 	 * column reference in an inverse expression. The string between the braces
 	 * MUST be a valid column name.
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public static Set<ColumnIdentifier> extractColumnNamesFromInverseExpression(
 			String inverseExpression) {
 
 		Set<ColumnIdentifier> result = new HashSet<ColumnIdentifier>();
 		if (inverseExpression != null) {
 			StringTokenizer st = new StringTokenizer(inverseExpression, "{}");
 			while (st.hasMoreElements()) {
 				String element = st.nextElement().toString();
 				int index = inverseExpression.indexOf(element);
 				if (index > 0
 						&& index < inverseExpression.length()
 						&& inverseExpression.charAt(index - 1) == '{'
 						&& (inverseExpression.charAt(index + element.length()) == '}')) {
 					// result.add(deleteBackSlash(element));
 					result.add(ColumnIdentifierImpl.buildFromR2RMLConfigFile(element));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the result of replacing each column reference c in the inverse
 	 * expression with : - the quoted and escaped data value of column c in r,
 	 * if c is a referenced column in the term map - the column name of column
 	 * c, otherwise
 	 * 
 	 * @param stringTemplate
 	 * @param dbValues
 	 * @param columnReferences
 	 * @param dbTypes
 	 * @return
 	 * @throws R2RMLDataError
 	 * @throws SQLException
 	 * @throws UnsupportedEncodingException
 	 * @throws MalformedURLException
 	 */
 	public static String extractColumnValueFromInverseExpression(
 			String inverseExpression,
 			Map<ColumnIdentifier, byte[]> dbValues,
 			Set<ColumnIdentifier> columnReferences) throws R2RMLDataError, SQLException,
 			UnsupportedEncodingException {
 		// Let result be the template string
 		String result = inverseExpression;
 		if (dbValues == null)
 			return null;
 
 		for (ColumnIdentifier column : dbValues.keySet()) {
 			// A quoted and escaped data value is any SQL
 			// string that matches the <literal> or <null specification>
 			// productions of [SQL2].
 			// This string can be used in a SQL query to specify a SQL data
 			// value.
 			String value = null;
 			if (dbValues.get(column) == null)
 				value = "NULL";
 			else {
 				// Extract db value
 				byte[] byteValue = dbValues.get(column);
 				// Apply cast to string to the SQL data value
 				value = new String(byteValue, "UTF-8");
 			}
			result = result.replaceAll("\\{" + column + "\\}", "'" + value
					+ "'");
 			// Test backslashes result =
 			/*
 			 * result.replaceAll("\\{\\\"" + column + "\\\"\\}", "'" + result +
 			 * "'"); result = result.replaceAll("\\{\\'" + column + "\\'\\}",
 			 * "'" + result + "'"); result = result.replaceAll("\\{\\`" + column
 			 * + "\\`\\}", "'" + result + "'");
 			 */
 		}
 		// Replace curly braces of not referenced column references
 		for (ColumnIdentifier column : columnReferences) {
 			if (!dbValues.keySet().contains(column)) {
			    	String columnStr = column.toString();
				result = result.replaceAll("\\{" + columnStr + "\\}", columnStr);
				// Test backslashes result =
				result.replaceAll("\\{\\\"" + columnStr + "\\\"\\}", columnStr);
				result = result
						.replaceAll("\\{\\'" + columnStr + "\\'\\}", columnStr);
				result = result
						.replaceAll("\\{\\`" + columnStr + "\\`\\}", columnStr);
 			}
 		}
 		// Curly braces that do not enclose column names MUST be
 		// escaped by a backslash character (“\”).
 		result = result.replaceAll("\\\\\\{", "{");
 		result = result.replaceAll("\\\\\\}", "}");
 		return result;
 	}
 
 	@Deprecated
 	public static String deleteBackSlash(String value) {
 		String result = value;
 		// Check if column is "backslashed"
 		if (value.startsWith("\"") && value.endsWith("\"")) {
 			result = value.substring(1, value.length() - 1);
 		}
 		if (value.startsWith("'") && value.endsWith("'")) {
 			result = value.substring(1, value.length() - 1);
 		}
 		if (value.startsWith("`") && value.endsWith("`")) {
 			result = value.substring(1, value.length() - 1);
 		}
 		return result;
 	}
 
 	/**
 	 * The template value of the term map for a given logical table row is
 	 * determined as follows.
 	 * 
 	 * @param stringTemplate
 	 * @param dbValues
 	 * @param dbTypes
 	 * @return
 	 * @throws R2RMLDataError
 	 * @throws SQLException
 	 * @throws UnsupportedEncodingException
 	 * @throws MalformedURLException
 	 */
 	public static String extractColumnValueFromStringTemplate(
 			String stringTemplate,
 			Map<ColumnIdentifier, byte[]> dbValues,
 			ResultSetMetaData dbTypes) throws R2RMLDataError, SQLException,
 			UnsupportedEncodingException {
 		// Let result be the template string
 
 		String result = stringTemplate;
 		if (dbValues == null)
 			return null;
 		for (ColumnIdentifier dbValue : dbValues.keySet()) {
 			// For each pair of unescaped curly braces in result: if value is
 			// NULL, then return NULL
 			if (dbValues.get(dbValue) == null)
 				return null;
 		}
 		for (ColumnIdentifier column : dbValues.keySet()) {
 			// Extract db value
 			byte[] byteValue = dbValues.get(column);
 			// Extract RDF Natural form
 			SQLType sqlType = column.getSqlType();
 			// Apply cast to string to the SQL data value
 			String value;
 			if (sqlType != null) {
 				XSDType xsdType = SQLToXMLS.getEquivalentType(sqlType);
 				value = XSDLexicalTransformation.extractNaturalRDFFormFrom(
 						xsdType, byteValue);
 			}
 			else
 			{
 			    value = new String(byteValue, "UTF-8");
 			}
 			result = column.replaceAll(result, getIRISafeVersion(value));
 			// Test backslashes result =
 			/*
 			 * result.replaceAll("\\{\\\"" + column + "\\\"\\}",
 			 * getIRISafeVersion(result)); result = result.replaceAll("\\{\\'" +
 			 * column + "\\'\\}", getIRISafeVersion(result)); result =
 			 * result.replaceAll("\\{\\`" + column + "\\`\\}",
 			 * getIRISafeVersion(result));
 			 */
 
 		}
 		// Curly braces that do not enclose column names MUST be
 		// escaped by a backslash character (“\”).
 		result = result.replaceAll("\\\\\\{", "{");
 		result = result.replaceAll("\\\\\\}", "}");
 
 		return result;
 	}
 
 	/**
 	 * The IRI-safe version of a string is obtained by applying the following
 	 * transformation in [RFC3987].
 	 * 
 	 * @throws R2RMLDataError
 	 * @throws MalformedURLException
 	 */
 	public static String getIRISafeVersion(String value) {
 		// Any character that is not in the iunreserved production
 		// iunreserved = ALPHA / DIGIT / "-" / "." / "_" / "~" / ucschar
 		StringBuffer buff = new StringBuffer(value.length());
 		Set<Character> unreservedChars = new HashSet<Character>();
 
 		unreservedChars.add('-');
 		unreservedChars.add('.');
 		unreservedChars.add('_');
 		unreservedChars.add('~');
 		unreservedChars.add('*');
 		unreservedChars.add('\'');
 		unreservedChars.add('(');
 		unreservedChars.add(')');
 		unreservedChars.add('!');
 		unreservedChars.add('=');
 
 		for (int i = 0; i < value.length(); i++) {
 			char c = value.charAt(i);
 			if (!(Character.isDigit(c) || Character.isLetter(c) || unreservedChars
 					.contains(c))) {
 				// Percent-encode each octet
 				buff.append('%');
 				buff.append(Integer.toHexString(c).toUpperCase());
 			} else {
 				buff.append(c);
 			}
 		}
 		return buff.toString();
 	}
 
 	/**
 	 * The URL-safe version of a string is obtained by an URL encoding to a
 	 * string value.
 	 * 
 	 * @throws R2RMLDataError
 	 * @throws MalformedURLException
 	 */
 	public static String getURLSafeVersion(String value) throws R2RMLDataError {
 		if (value == null)
 			return null;
 		URL url = null;
 		try {
 			url = new URL(value);
 		} catch (MalformedURLException mue) {
 			// This template should be not a url : no encoding
 			return value;
 		}
 		// No exception raised, this template is a valid url : perform
 		// percent-encoding
 		try {
 			java.net.URI uri = new java.net.URI(url.getProtocol(),
 					url.getAuthority(), url.getPath(), url.getQuery(),
 					url.getRef());
 			String result = uri.toURL().toString();
 			// Percent encoding : complete with no supported char in this
 			// treatment
 			result = result.replaceAll("\\,", "%2C");
 			return result;
 		} catch (URISyntaxException e) {
 			throw new R2RMLDataError(
 					"[R2RMLToolkit:getIRISafeVersion] This value " + value
 							+ " can not be percent-encoded because "
 							+ e.getMessage());
 		} catch (MalformedURLException e) {
 			throw new R2RMLDataError(
 					"[R2RMLToolkit:getIRISafeVersion] This value " + value
 							+ " can not be percent-encoded because "
 							+ e.getMessage());
 		}
 	}
 
 }
