 /**
  * 05.04.2008
  */
 package de.freese.queryengine.performer;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.freese.base.model.row.IValueMeta;
 import de.freese.base.model.row.RowMeta;
 import de.freese.base.model.row.RowMetaAndData;
 import de.freese.base.model.row.ValueMeta;
 import de.freese.base.model.row.ValueMetaType;
 import de.freese.base.persistence.SQLFormatter;
 
 /**
  * Performer fuer SQL Queries der QueryEngine.<br>
  * ueber die {@link JDBCSession} erfolgt der Zugriff auf die {@link Connection}.
  * 
  * @author Thomas Freese
  */
 public class JDBCQueryPerformer implements IQueryPerformer
 {
 	/**
      * 
      */
 	public static final Logger LOGGER = LoggerFactory.getLogger(JDBCQueryPerformer.class);
 
 	/**
 	 * 
 	 */
 	private final SQLFormatter sqlFormatter = new SQLFormatter();
 
 	/**
 	 * Creates a new {@link JDBCQueryPerformer} object.
 	 */
 	public JDBCQueryPerformer()
 	{
 		super();
 
 		if (LOGGER.isDebugEnabled())
 		{
 			this.sqlFormatter.setFormatSQL(true);
 		}
 	}
 
 	/**
 	 * @see de.freese.queryengine.performer.IQueryPerformer#executeQuery(java.lang.Object)
 	 */
 	@Override
 	public List<RowMetaAndData> executeQuery(final Object nativeQuery)
 	{
 		List<RowMetaAndData> result = new ArrayList<>();
 
 		try
 		{
 			JDBCSession.beginTransaction();
 
 			Connection connection = JDBCSession.getCurrentConnection();
 
 			Statement statement = connection.createStatement();
 
 			if (LOGGER.isDebugEnabled())
 			{
 				LOGGER.debug(this.sqlFormatter.format(nativeQuery.toString()));
 			}
 
 			long start = System.currentTimeMillis();
 
 			statement.execute(nativeQuery.toString());
 
 			long ende = System.currentTimeMillis();
 
 			if (LOGGER.isDebugEnabled())
 			{
 				LOGGER.debug("Zeit: " + (ende - start) + " ms");
 			}
 
 			ResultSet resultSet = statement.getResultSet();
 
 			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
 
 			int columnCount = resultSetMetaData.getColumnCount();
 
 			// Spaltennnamen
 			RowMeta rowMeta = new RowMeta();
 
 			for (int col = 1; col <= columnCount; col++)
 			{
 				String columnName = resultSetMetaData.getColumnName(col);
 				int sqlType = resultSetMetaData.getColumnType(col);
 
 				ValueMetaType metaType = ValueMetaType.getFromSQLType(sqlType);
 				IValueMeta valueMeta = new ValueMeta(columnName, metaType);
 				rowMeta.addValueMeta(valueMeta);
 			}
 
 			// Daten
 			while (resultSet.next())
 			{
 				RowMetaAndData metaAndData = new RowMetaAndData();
 				metaAndData.setRowMeta(rowMeta);
 
 				Object[] data = new Object[columnCount];
 
 				for (int column = 0; column < columnCount; column++)
 				{
 					Object obj = resultSet.getObject(column + 1);
 					data[column] = (obj == null) ? "null" : obj;
 				}
 
 				metaAndData.setData(data);
 				result.add(metaAndData);
 			}
 
 			resultSet.close();
 			statement.close();
 			JDBCSession.commitTransaction();
 		}
 		catch (Exception ex1)
 		{
 			LOGGER.error(null, ex1);
 
 			try
 			{
 				JDBCSession.rollbackTransaction();
 			}
 			catch (Exception ex2)
 			{
 				LOGGER.error(null, ex2);
 			}
 		}
 
 		return result;
 	}
 }
