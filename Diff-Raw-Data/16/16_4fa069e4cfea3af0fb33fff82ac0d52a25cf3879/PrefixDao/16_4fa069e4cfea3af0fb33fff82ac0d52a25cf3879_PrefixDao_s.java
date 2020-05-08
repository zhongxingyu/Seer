 package cz.cuni.mff.odcleanstore.webfrontend.dao.prefixes;
 
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 import cz.cuni.mff.odcleanstore.util.CodeSnippet;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.prefixes.Prefix;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoTemplate;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.EnumDatabaseInstance;
 
 public class PrefixDao extends DaoTemplate<Prefix>
 {
 	public static final String TABLE_NAME = "DB.DBA.SYS_XML_PERSISTENT_NS_DECL";
 
 	private static final long serialVersionUID = 1L;
 	
 	private ParameterizedRowMapper<Prefix> rowMapper;
 	
 	public PrefixDao()
 	{
 		this.rowMapper = new PrefixRowMapper();
 	}
 	
 	@Override
 	public String getTableName()
 	{
 		return TABLE_NAME;
 	}
 
 	@Override
 	protected ParameterizedRowMapper<Prefix> getRowMapper()
 	{
 		return rowMapper;
 	}
 	
 	public void delete(Prefix item) throws Exception
 	{
 		String query = "DB.DBA.XML_REMOVE_NS_BY_PREFIX (?, 2)";
 		Object[] params = { item.getPrefix() };
 		
 		// the delete in the clean DB must preceed the delete in the dirty DB in order
 		// for the transactional behavior to work correctly
 		// (the operation is surrounded by a transaction on the clean JDBC template)
 		//
 		jdbcUpdate(query, params, EnumDatabaseInstance.CLEAN);
 		jdbcUpdate(query, params, EnumDatabaseInstance.DIRTY);
 	}
 	
 	public void save(final Prefix item) throws Exception
 	{
 		final PrefixDao dao = this;
 
 		executeInTransaction(new CodeSnippet() {
 				@Override
 				public void execute() throws Exception {
					String query = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE NS_PREFIX = ?";
					
					Object[] params = new Object[] { item.getPrefix() };
					
					boolean present = false;
					
					present |= dao.jdbcQueryForInt(query, params, EnumDatabaseInstance.CLEAN) > 0;
					present |= dao.jdbcQueryForInt(query, params, EnumDatabaseInstance.DIRTY) > 0;
 					
					if (present) {
						throw new Exception("Prefix `" + item.getPrefix() + "` already exists.");
					}
 
 					query = "DB.DBA.XML_SET_NS_DECL (?, ?, 2)";
 					params = new Object[] { item.getPrefix(), item.getUrl() };
 
 					// the save in the clean DB must preceed the save in the dirty DB in order
 					// for the transactional behavior to work correctly
 					// (the operation is surrounded by a transaction on the clean JDBC template)
 					//
 					dao.jdbcUpdate(query, params, EnumDatabaseInstance.CLEAN);
 					dao.jdbcUpdate(query, params, EnumDatabaseInstance.DIRTY);
 				}
 		});
 	}
 	
 	@Override
 	public Prefix loadBy(String columnName, Object value)
 	{
 		return super.loadBy(columnName, value);
 	}
 }
