 package org.sakaiproject.hierarchy.impl.model.dao;
 
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.db.api.SqlReader;
 import org.sakaiproject.hierarchy.api.model.Hierarchy;
 import org.sakaiproject.hierarchy.impl.HierarchyImpl;
 import org.sakaiproject.id.api.IdManager;
 
 public class HierarchySqlReader implements SqlReader
 {
 	private static final Log log = LogFactory.getLog(HierarchySqlReader.class);
 
 	private static final int HIERARCHY_ID_POS = 1;
 
 	private static final int HIERARCHY_PATH_HASH_POS = 2;
 
 	private static final int HIERARCHY_PATH_POS = 3;
 
 	private static final int HIERARCHY_PARENT_ID = 4;
 
 	private static final int HIERARCHY_REALM_POS = 5;
 
 	private static final int HIERARCHY_VERSION_POS = 6;
 
 	public static final String FIND_ROOTS = "select id, pathhash, path, parent_id, realm, version from hierarchy_nodes where parent_id is null " ;
 
 	public static final String FIND_BY_PATHHASH_SQL = "select id, pathhash, path, parent_id, realm, version from hierarchy_nodes where pathhash = ? ";
 
 	public static final String FIND_BY_PARENT_ID_SQL = "select id, pathhash, path, parent_id, realm, version from hierarchy_nodes where parent_id = ? ";
 
 	public static final String INSERT_SQL = "insert into hierarchy_nodes ( id, pathhash, path, parent_id, realm, version ) values ( ?, ?, ?, ?, ?, ?) ";
 
	public static final String UPDATE_SQL = "update hierarchy_nodes set pathhash = ?, path = ?, parent_id = ?, realm = ?, version = ? where id = ? and version = ?";
 
 	public static final String DELETE_SQL = "delete from hierarchy_nodes where id = ? and version = ?";
 
 	public static final String FIND_BY_ID_SQL = "select id, pathhash, path, parent_id, realm, version from hierarchy_nodes where id = ? ";
 
 	public static final String FIND_CHILD_ID_BY_PARENT_ID = "select id from hierarchy_nodes where parent_id = ? ";
 
 	public static final String DELETE_NODE_GROUPS_SQL_1 = "delete from hierarchy_nodes where id in ( ";
 
 	public static final Object DELETE_NODE_GROUPS_SQL_2 = " ) ";
 
 	public static final Object FIND_CHILD_ID_BY_PARENT_GROUPS_SQL_1 = "select id from hierarchy_nodes where parent_id in (  ";
 
 	public static final Object FIND_CHILD_ID_BY_PARENT_GROUPS_SQL_2 = " ) ";
 	
 
 	private Hierarchy owner = null;
 
 	private HierarchyDAO dao = null;
 
 	public HierarchySqlReader(HierarchyDAO dao, Hierarchy owner)
 	{
 		this.owner = owner;
 		this.dao = dao;
 	}
 
 	public HierarchySqlReader(HierarchyDAO dao)
 	{
 		this.dao = dao;
 	}
 
 	public Object readSqlResultRecord(ResultSet result)
 	{
 		try
 		{
 			HierarchyImpl h = new HierarchyImpl();
 			h.setId(result.getString(HIERARCHY_ID_POS));
 			h.setPath(result.getString(HIERARCHY_PATH_POS));
 			if (owner == null)
 			{
 				h.setInternalParent(new LazyHierarchyParent(dao, result
 						.getString(HIERARCHY_PARENT_ID)));
 			}
 			else
 			{
 				h.setInternalParent(owner);
 			}
 			h.setRealm(result.getString(HIERARCHY_REALM_POS));
 			h.setVersion(result.getTimestamp(HIERARCHY_VERSION_POS));
 			
 			h.setInternalChildren(new LazyHierarchyChildren(dao, h));
 			h.setInternalProperties(new LazyHierarchyProperties(dao, h));
 			h.setModified(false);
 			return h;
 		}
 		catch (Throwable ex)
 		{
 			log.error("Failed to convert Row to Hierarchy Object ",ex);
 			throw new RuntimeException("Failed to load record ", ex);
 
 		}
 	}
 
 	public static Object[] getUpdateObjects(Hierarchy hierarchy)
 	{
 		Object[] o = new Object[7];
 //		pathhash = ?, path = ? parent_id = ?, realm = ?, version = ? where id = ? and version = ?";
 
 	    o[0] = hierarchy.getPathHash();
 		o[1] = hierarchy.getPath();
 		if ( hierarchy.getParent() != null ) {
 			o[2] = hierarchy.getParent().getId();
 		} else {
 			o[2] = null;
 		}
 		o[3] = hierarchy.getRealm();
 		o[4] = new Timestamp(System.currentTimeMillis());
 		o[5] = hierarchy.getId();
 		o[6] = hierarchy.getVersion();
 		hierarchy.setVersion((Date)o[4]);
 		return o;
 	}
 
 	public static Object[] getInsertObjects(Hierarchy hierarchy, IdManager idManager)
 	{
 //	"insert into hierarchy_nodes ( id, pathhash, path, parent_id, realm, version ) values ( ?, ?, ?, ?, ?, ?) ";
 		Object[] o = new Object[6];
 		if ( hierarchy.getId() == null ) {
 			hierarchy.setId(idManager.createUuid());
 		}
 	    o[0] = hierarchy.getId();
 		o[1] = hierarchy.getPathHash();
 		o[2] = hierarchy.getPath();
 		if ( hierarchy.getParent() != null ) {
 			o[3] = hierarchy.getParent().getId();
 		} else {
 			o[3] = null;
 		}
 		o[4] = hierarchy.getRealm();
 		o[5] = new Timestamp(System.currentTimeMillis());
 		return o;
 	}
 
 	public static Object[] getDeleteObjects(Hierarchy hierarchy)
 	{
 // "delete from hierarchy_nodes where id = ? and version = ?";
 		Object[] o = new Object[2];
 	    o[0] = hierarchy.getId();
 		o[1] = hierarchy.getVersion();
 		return o;
 	}
 
 }
