 /*
  * utils - Query.java - Copyright © 2009 David Roden
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
  */
 
 package net.pterodactylus.util.database;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.pterodactylus.util.database.OrderField.Order;
 
 /**
  * Container for SQL queries and their parameters.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class Query {
 
 	/**
 	 * The type of the query.
 	 *
 	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 	 */
 	public enum Type {
 
 		/** Query is a SELECT. */
 		SELECT,
 
 		/** Query is an INSERT. */
 		INSERT,
 
 		/** Query is an UPDATE. */
 		UPDATE,
 
 		/** Query is a DELETE. */
 		DELETE
 
 	}
 
 	/** The type of the query. */
 	private final Type type;
 
 	/** The fields for a select query. */
 	private final List<Field> fields = new ArrayList<Field>();
 
 	/** The value fields for an insert or update query. */
 	private final List<ValueField> valueFields = new ArrayList<ValueField>();
 
 	/** The name of the table. */
 	private final String table;
 
 	/** The WHERE clauses. */
 	private final List<WhereClause> whereClauses = new ArrayList<WhereClause>();
 
 	/** The order fields for select queries. */
 	private final List<OrderField> orderFields = new ArrayList<OrderField>();
 
 	/**
 	 * Creates a new query.
 	 *
 	 * @param type
 	 *            The type of the query
 	 * @param table
 	 *            The table that is processed
 	 */
 	public Query(Type type, String table) {
 		this.type = type;
 		this.table = table;
 	}
 
 	/**
 	 * Adds one or more fields to this query.
 	 *
 	 * @param fields
 	 *            The fields to add
 	 */
 	public void addField(Field... fields) {
 		for (Field field : fields) {
 			this.fields.add(field);
 		}
 	}
 
 	/**
 	 * Adds one or more value fields to this query.
 	 *
 	 * @param valueFields
 	 *            The value fields to add
 	 */
 	public void addValueField(ValueField... valueFields) {
 		for (ValueField valueField : valueFields) {
 			this.valueFields.add(valueField);
 		}
 	}
 
 	/**
 	 * Adds one or more WHERE clauses to this query.
 	 *
 	 * @param whereClauses
 	 *            The WHERE clauses to add
 	 */
 	public void addWhereClause(WhereClause... whereClauses) {
 		for (WhereClause whereClause : whereClauses) {
 			this.whereClauses.add(whereClause);
 		}
 	}
 
 	/**
 	 * Adds one or more order fields to this query.
 	 *
 	 * @param orderFields
 	 *            The order fields to add
 	 */
 	public void addOrderField(OrderField... orderFields) {
 		if (type != Type.SELECT) {
 			throw new IllegalStateException("Order fields are only allowed in SELECT queries.");
 		}
 		for (OrderField orderField : orderFields) {
 			this.orderFields.add(orderField);
 		}
 	}
 
 	/**
 	 * Creates an SQL statement from the given connection and prepares it for
 	 * execution.
 	 *
 	 * @param connection
 	 *            The connection to create a statement from
 	 * @return The prepared statement, ready for execution
 	 * @throws SQLException
 	 *             if an SQL error occurs
 	 */
 	public PreparedStatement createStatement(Connection connection) throws SQLException {
 		StringWriter queryWriter = new StringWriter();
 		try {
 			render(queryWriter);
 		} catch (IOException ioe1) {
 			/* ignore. */
 		}
 		String query = queryWriter.toString();
 		System.out.println(query);
 		PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
 		int index = 0;
 		if ((type == Type.UPDATE) || (type == Type.INSERT)) {
 			for (ValueField valueField : valueFields) {
 				valueField.getParameter().set(preparedStatement, ++index);
 			}
 		}
 		if ((type == Type.SELECT) || (type == Type.UPDATE) || (type == Type.DELETE)) {
 			for (WhereClause whereClause : whereClauses) {
 				for (Parameter<?> parameter : whereClause.getParameters()) {
 					parameter.set(preparedStatement, ++index);
 				}
 			}
 		}
 		return preparedStatement;
 	}
 
 	/**
 	 * Renders this query to the given writer.
 	 *
 	 * @param writer
 	 *            The writer to render the query to
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public void render(Writer writer) throws IOException {
 		writer.write(type.name());
 		if (type == Type.SELECT) {
 			writer.write(' ');
 			if (fields.isEmpty()) {
 				writer.write('*');
 			} else {
 				boolean first = true;
 				for (Field field : fields) {
 					if (!first) {
 						writer.write(", ");
 					}
 					writer.write(field.getName());
 					first = false;
 				}
 			}
 			writer.write(" FROM ");
 			writer.write(table);
 			renderWhereClauses(writer);
 			if (!orderFields.isEmpty()) {
 				writer.write(" ORDER BY ");
 				boolean first = true;
 				for (OrderField orderField : orderFields) {
 					if (!first) {
 						writer.write(", ");
 					}
 					writer.write(orderField.getField().getName());
 					writer.write((orderField.getOrder() == Order.ASCENDING) ? " ASC" : " DESC");
 					first = false;
 				}
 			}
 		} else if (type == Type.UPDATE) {
 			writer.write(' ');
 			writer.write(table);
			writer.write(" SET ");
 			boolean first = true;
 			for (ValueField valueField : valueFields) {
 				if (!first) {
 					writer.write(", ");
 				}
 				writer.write(valueField.getName());
				writer.write(" = ?");
 				first = false;
 			}
 			renderWhereClauses(writer);
 		} else if (type == Type.INSERT) {
 			writer.write(" INTO ");
 			writer.write(table);
 			writer.write(" (");
 			boolean first = true;
 			for (ValueField valueField : valueFields) {
 				if (!first) {
 					writer.write(", ");
 				}
 				writer.write(valueField.getName());
 				first = false;
 			}
 			writer.write(") VALUES (");
 			first = true;
 			for (int fieldIndex = 0; fieldIndex < valueFields.size(); ++fieldIndex) {
 				if (!first) {
 					writer.write(", ");
 				}
 				writer.write('?');
 				first = false;
 			}
 			writer.write(')');
 		} else if (type == Type.DELETE) {
 			/* TODO */
 		}
 	}
 
 	/**
 	 * Renders the WHERE clauses, prepending a “WHERE” if there are WHERE
 	 * clauses to render.
 	 *
 	 * @param writer
 	 *            The writer to render the WHERE clauses to
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	private void renderWhereClauses(Writer writer) throws IOException {
 		if (whereClauses.isEmpty()) {
 			return;
 		}
 		writer.write(" WHERE ");
 		if (whereClauses.size() == 1) {
 			whereClauses.get(0).render(writer);
 			return;
 		}
 		AndWhereClause whereClause = new AndWhereClause(whereClauses);
 		whereClause.render(writer);
 	}
 
 }
