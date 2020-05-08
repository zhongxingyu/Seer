 package de.unioninvestment.eai.portal.portlet.crud.scripting.model;
 
 import static java.util.Arrays.asList;
 import groovy.lang.GString;
 import groovy.sql.Sql;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.groovy.runtime.GStringImpl;
 import org.springframework.util.Assert;
 
 import com.vaadin.addon.sqlcontainer.query.generator.StatementHelper;
 
 import de.unioninvestment.eai.portal.portlet.crud.domain.exception.TechnicalCrudPortletException;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.DatabaseQueryContainer;
 
/**
 * Groovy Script Wrapper class for the {@link DatabaseQueryContainer}. Provides
 * special operations that only work with the query backend.
 * 
 * @author carsten.mjartan
 */
 public class ScriptDatabaseQueryContainer extends ScriptDatabaseContainer {
 
	/**
	 * This class acts as dynamic proxy for a {@link PreparedStatement} and
	 * collects calls of
	 * {@link StatementHelper#setParameterValuesToStatement(PreparedStatement)}.
	 * 
	 * It provides a values list for the Query GString.
	 * 
	 * @author carsten.mjartan
	 */
 	private static final class ParameterInvocationHandler implements
 			InvocationHandler {
 
 		LinkedList<Object> values = new LinkedList<Object>();
 
 		@Override
 		public Object invoke(Object proxy, Method method, Object[] args)
 				throws Throwable {
 			if (method.getName().equals("setObject")) {
 				values.add(args[1]);
 			} else if (method.getName().equals("setNull")) {
 				values.add(null);
 			} else if (method.getName().equals("setBigDecimal")) {
 				values.add(args[1]);
 			} else if (method.getName().equals("setDate")) {
 				values.add(args[1]);
 			} else if (method.getName().equals("setString")) {
 				values.add(args[1]);
 			} else if (method.getName().equals("setTime")) {
 				values.add(args[1]);
 			} else if (method.getName().equals("setTimestamp")) {
 				values.add(args[1]);
 			} else {
 				throw new UnsupportedOperationException(
 						"Cannot handle call to " + method + " with args "
 								+ asList(args));
 			}
 			return null;
 		}
 
 		public Object[] getValues() {
 			return values.toArray();
 		}
 	}
 
 	private DatabaseQueryContainer databaseQueryContainer;
 
 	ScriptDatabaseQueryContainer(DataContainer container) {
 		super(container);
 		this.databaseQueryContainer = (DatabaseQueryContainer) container;
 	}
 
 	/**
 	 * Returns the database query that respects the filter criteria and possibly
 	 * a sort order of the current view.
 	 * 
 	 * @param options
 	 *            currently only 'preserveOrder':boolean is supported
 	 * @return a GString that can be used in {@link Sql} GString operations
 	 */
 	public GString getCurrentQuery(Map<String, Object> options) {
 		boolean preserveOrder = options != null
 				&& Boolean.TRUE.equals(options.get("preserveOrder"));
 		StatementHelper helper = databaseQueryContainer
 				.getCurrentQuery(preserveOrder);
 		GString result = gstringify(helper);
 		return result;
 	}
 
 	private GString gstringify(StatementHelper helper) {
 		String queryString = helper.getQueryString();
 		String[] strings = extractGStrings(queryString);
 		int expectedValueLength = queryString.endsWith("?") ? strings.length
 				: strings.length - 1;
 		ParameterInvocationHandler handler = new ParameterInvocationHandler();
 		PreparedStatement stmt = (PreparedStatement) Proxy.newProxyInstance(
 				this.getClass().getClassLoader(),
 				new Class<?>[] { PreparedStatement.class }, handler);
 
 		try {
 			helper.setParameterValuesToStatement(stmt);
 
 		} catch (SQLException e) {
 			throw new TechnicalCrudPortletException(
 					"Error catching parameter values", e);
 		}
 		Object[] values = handler.getValues();
 		Assert.state(values.length == expectedValueLength,
 				"Inconsistent string length, conversion failed");
 
 		return new GStringImpl(values, strings);
 	}
 
 	private String[] extractGStrings(String queryString) {
 		// String[] strings = queryString.split("\\?");
 		List<String> result = new ArrayList<String>();
 		int start = 0;
 		boolean inQuotes = false;
 		int length = queryString.length();
 		int last = length - 1;
 		for (int current = 0; current < length; current++) {
 			if (queryString.charAt(current) == '\'') {
 				inQuotes = !inQuotes; // toggle state
 			} else if (!inQuotes) {
 				if (queryString.charAt(current) == '?') {
 					result.add(queryString.substring(start, current));
 					start = current + 1;
 				}
 			}
 		}
 		if (start < length) {
 			result.add(queryString.substring(start));
 		}
 		return result.toArray(new String[result.size()]);
 	}
 }
