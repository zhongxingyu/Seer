 
 package edu.wustl.cab2b.server.path.pathgen;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * Uses the database to cache paths. The cache is pretty slow due to database
  * access, and is to be used when the
  * {@link edu.wustl.cab2b.server.path.pathgen.MemoryCache} fails due to
  * {@link java.lang.OutOfMemoryError}.
  * @author Chandrakant Talele
  */
 public class DatabaseCache extends GraphPathFinderCache
 {
 
 	static String connector = "_";
 
 	int databaseCacheId;
 
 	Connection conn = null;
 
 	PreparedStatement addPath;
 
 	PreparedStatement addIgnoredNodeSet;
 
 	PreparedStatement getPathsOnIgnoringNodes;
 
 	PreparedStatement getAllPaths;
 
 	/**
 	 * @param conn
 	 *            the connection to be used for connecting to database.
 	 * @throws NullPointerException
 	 *             if connection is null.
 	 */
 	public DatabaseCache(Connection conn)
 	{
 		super();
 		if (conn == null)
 		{
 			throw new NullPointerException("Connection cannot be null.");
 		}
 		this.conn = conn;
 		this.databaseCacheId = IdGenerator.getNextId();
 		createPreparedStatments();
 	}
 
 	/**
 	 * @see edu.wustl.cab2b.server.path.pathgen.GraphPathFinderCache#getAllPaths()
 	 */
 	@Override
 	Set<Path> getAllPaths()
 	{
 		checkAlive();
 		HashSet<Path> pathSet = new HashSet<Path>();
 		ResultSet rs = null;
 		try
 		{
 			rs = getAllPaths.executeQuery();
 			while (rs.next())
 			{
 				String intermediatePath = rs.getString(1);
 				int src = rs.getInt(2);
 				int des = rs.getInt(3);
 				List<Node> intermediateNodes = getNodeList(intermediatePath);
 				pathSet.add(new Path(new Node(src), new Node(des), intermediateNodes));
 			}
 
 		}
 		catch (SQLException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 		finally
 		{
 			if (rs != null)
 			{
 				try
 				{
 					rs.close();
 				}
 				catch (SQLException e)
 				{
 					Logger.out.debug(e.getMessage());
 				}
 			}
 		}
 		return pathSet;
 	}
 
 	// insert into IGNORED_NODE_SET
 	// (ignored_node_set_id,nodes_in_set,number_of_ignored_nodes,database_cache_id,src_node_id,des_node_id)
 	// values (2,'_66_78_',2,0,11,22);
 	// insert into TEMP_PATH (IGNORED_NODE_SET_ID,INTERMEDIATE_PATH)
 	// values(1,'_44_99_99_');
 	/**
 	 * @see edu.wustl.cab2b.server.path.pathgen.GraphPathFinderCache#addEntry(edu.wustl.cab2b.server.path.pathgen.SourceDestinationPair,
 	 *      java.util.Set, java.util.Set)
 	 */
 	void addEntry(SourceDestinationPair sdp, Set<Node> ignoredNodes, Set<Path> paths)
 	{
 		checkAlive();
 		int ignoredNodeSetId = IdGenerator.getNextId();
 		String nodesInSet = getString(ignoredNodes);
 		int sizeOfIgnoredNodes = ignoredNodes.size();
 		int src = sdp.getSrcNode().getId();
 		int des = sdp.getDestNode().getId();
 
 		execute(addIgnoredNodeSet, ignoredNodeSetId, nodesInSet, sizeOfIgnoredNodes,
 				databaseCacheId, src, des);
 
 		for (Path p : paths)
 		{
 
 			String path = getPathString(p);
 			execute(addPath, ignoredNodeSetId, path);
 		}
 	}
 
 	private void execute(PreparedStatement ps, Object... params)
 	{
 		int index = 1;
 		try
 		{
 			for (Object param : params)
 			{
 				if (param instanceof String)
 				{
 					ps.setString(index++, (String) param);
 				}
 				else if (param instanceof Integer)
 				{
 					ps.setInt(index++, (Integer) param);
 				}
 			}
 			ps.execute();
 			ps.clearParameters();
 		}
 		catch (SQLException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 	}
 
 	/**
 	 * Returns path on ignoring nodes
 	 * 
 	 * @return collection of Path.
 	 * @see edu.wustl.cab2b.server.path.pathgen.GraphPathFinderCache#getPathsOnIgnoringNodes(edu.wustl.cab2b.server.path.pathgen.SourceDestinationPair,
 	 *      java.util.Set)
 	 */
 	public Set<Path> getPathsOnIgnoringNodes(SourceDestinationPair sdp, Set<Node> ignoredNodes)
 	{
 		checkAlive();
 		Node src = sdp.getSrcNode();
 		Node des = sdp.getDestNode();
 		String nodesInSet = getString(ignoredNodes);
 		Set<Path> set = new HashSet<Path>(0);
 		try
 		{
 			setParams(databaseCacheId, src.getId(), des.getId(), nodesInSet);
 			set = getResults(src, src, ignoredNodes);
 		}
 		catch (SQLException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 
 		return set;
 
 	}
 
 	/**
 	 * Cleans up this cache, and marks it dead.
 	 * @see edu.wustl.cab2b.server.path.pathgen.GraphPathFinderCache#cleanup()
 	 */
 	void cleanup()
 	{
 		checkAlive();
 		super.cleanup();
 		clearCache();
 	}
 
 	/**
 	 * Deletes all rows from table TEMP_PATH and IGNORED_NODE_SET
 	 */
 	private void clearCache()
 	{
 		try
 		{
 			Statement s = conn.createStatement();
 			s.executeUpdate("delete from TEMP_PATH");
 			s.executeUpdate("delete from IGNORED_NODE_SET");
 			addPath.close();
 			addIgnoredNodeSet.close();
 			getPathsOnIgnoringNodes.close();
 		}
 		catch (SQLException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 	}
 
 	/*
 	 * select p.INTERMEDIATE_PATH from IGNORED_NODE_SET ins left outer join
 	 * TEMP_PATH p on ins.IGNORED_NODE_SET_ID = p.IGNORED_NODE_SET_ID where
 	 * ins.DATABASE_CACHE_ID = 0 and ins.SRC_NODE_ID = 14 and ins.DES_NODE_ID =
 	 * 11 and LOCATE(ins.NODES_IN_SET,'_11_12_13_14_15_') <>0 and
 	 * ins.number_of_ignored_nodes = ( select min(ins.number_of_ignored_nodes)
 	 * from IGNORED_NODE_SET ins left outer join TEMP_PATH p on
 	 * ins.IGNORED_NODE_SET_ID = p.IGNORED_NODE_SET_ID where
 	 * ins.DATABASE_CACHE_ID = 0 and ins.SRC_NODE_ID = 14 and ins.DES_NODE_ID =
 	 * 11 and LOCATE(ins.NODES_IN_SET,'_11_12_13_14_15_') <>0 and
 	 * ins.number_of_ignored_nodes > 0 )
 	 */
 
 	private void setParams(int databaseId, int src, int des, String nodesInSet) throws SQLException
 	{
 		getPathsOnIgnoringNodes.setInt(1, databaseCacheId);
 		getPathsOnIgnoringNodes.setInt(2, src);
 		getPathsOnIgnoringNodes.setInt(3, des);
 		getPathsOnIgnoringNodes.setString(4, nodesInSet);
 
 		getPathsOnIgnoringNodes.setInt(5, databaseCacheId);
 		getPathsOnIgnoringNodes.setInt(6, src);
 		getPathsOnIgnoringNodes.setInt(7, des);
 		getPathsOnIgnoringNodes.setString(8, nodesInSet);
 	}
 
 	private Set<Path> getResults(Node src, Node des, Set<Node> nodesToIgnore) throws SQLException
 	{
 		// if intermediate path = null then return empty set
 		// if no records then return null
 		boolean isEmpty = true;
 		HashSet<Path> set = new HashSet<Path>();
 		ResultSet rs = getPathsOnIgnoringNodes.executeQuery();
 		try
 		{
 			while (rs.next())
 			{
 				isEmpty = false;
 				String intermediatePath = rs.getString(1);
 				if (intermediatePath == null)
 				{
 					break;
 				}
 				List<Node> nodes = getNodeList(intermediatePath);
 				Set<Node> nodesInPath = new HashSet<Node>(nodes);
 				nodesInPath.retainAll(nodesToIgnore);
 				if (nodesInPath.isEmpty())
 				{
 					Path p = new Path(src, des, nodes);
 					set.add(p);
 				}
 			}
 		}
 		finally
 		{
 			rs.close();
 		}
 		getPathsOnIgnoringNodes.clearParameters();
 		if (isEmpty)
 		{
 			return null;
 		}
 		return set;
 	}
 
 	List<Node> getNodeList(String nodeString)
 	{
 		ArrayList<Node> nodes = new ArrayList<Node>();
 		String[] arr = nodeString.split(connector);
 		for (int i = 1; i < arr.length; i++)
 		{
 			nodes.add(new Node(Integer.parseInt(arr[i])));
 		}
 		return nodes;
 	}
 
 	private void createPreparedStatments()
 	{
 		try
 		{
 			addIgnoredNodeSet = conn
 					.prepareStatement("insert into IGNORED_NODE_SET (ignored_node_set_id,nodes_in_set,number_of_ignored_nodes,database_cache_id,src_node_id,des_node_id) values (?,?,?,?,?,?)");
 			addPath = conn
 					.prepareStatement("insert into TEMP_PATH (IGNORED_NODE_SET_ID,INTERMEDIATE_PATH) values(?,?)");
 			getPathsOnIgnoringNodes = conn
 					.prepareStatement("select p.INTERMEDIATE_PATH from IGNORED_NODE_SET ins left outer join TEMP_PATH  p on ins.IGNORED_NODE_SET_ID = p.IGNORED_NODE_SET_ID where ins.DATABASE_CACHE_ID = ? and ins.SRC_NODE_ID = ? and ins.DES_NODE_ID = ? and LOCATE(ins.NODES_IN_SET,?) <>0 and ins.number_of_ignored_nodes = (select min(ins.number_of_ignored_nodes) from IGNORED_NODE_SET ins left outer join  TEMP_PATH p on ins.IGNORED_NODE_SET_ID = p.IGNORED_NODE_SET_ID where ins.DATABASE_CACHE_ID = ? and ins.SRC_NODE_ID = ? and ins.DES_NODE_ID = ? and LOCATE(ins.NODES_IN_SET,?) <>0 and ins.number_of_ignored_nodes > 0 ) ");
 			getAllPaths = conn
 					.prepareStatement("select p.intermediate_path,src_node_id,des_node_id from IGNORED_NODE_SET ins join TEMP_PATH  p on ins.IGNORED_NODE_SET_ID = p.IGNORED_NODE_SET_ID where ins.number_of_ignored_nodes = 0");
 
 		}
 		catch (SQLException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 	}
 
 	String getPathString(Path path)
 	{
 		List<Node> intermediateNodes = path.getIntermediateNodes();
 		List<Integer> list = new ArrayList<Integer>(intermediateNodes.size());
 		for (Node node : path.getIntermediateNodes())
 		{
 			list.add(node.getId());
 		}
 		return getString(list);
 	}
 
 	String getString(Set<Node> ignoredNodes)
 	{
 		ArrayList<Integer> list = new ArrayList<Integer>(ignoredNodes.size());
 		for (Node node : ignoredNodes)
 		{
 			list.add(node.getId());
 		}
 		Collections.sort(list);
 		return getString(list);
 	}
 
 	String getString(List<Integer> list)
 	{
 		StringBuffer stringBuffer = new StringBuffer();
 		stringBuffer.append(connector);
 		for (int oneInteger : list)
 		{
 			stringBuffer.append(oneInteger);
 			stringBuffer.append(connector);
 		}
 		return stringBuffer.toString();
 	}
 
 	DatabaseCache()
 	{
		super();
 	}
 }
 
 /**
  * 
  * @author Chandrakant Talele
  *
  */
 class IdGenerator
 {
 
 	static int id;
 
 	static synchronized int getNextId()
 	{
 		return id++;
 	}
 }
 
 /*
  * drop table TEMP_PATH; drop table IGNORED_NODE_SET; create table
  * IGNORED_NODE_SET ( IGNORED_NODE_SET_ID int, NODES_IN_SET varchar(1000),
  * number_of_ignored_nodes int, DATABASE_CACHE_ID int, SRC_NODE_ID int,
  * DES_NODE_ID int, primary key (IGNORED_NODE_SET_ID) ); create table TEMP_PATH (
  * IGNORED_NODE_SET_ID int, INTERMEDIATE_PATH varchar(1000), foreign key
  * (IGNORED_NODE_SET_ID) references IGNORED_NODE_SET (IGNORED_NODE_SET_ID) );
  * insert into IGNORED_NODE_SET
  * (ignored_node_set_id,nodes_in_set,number_of_ignored_nodes,database_cache_id,src_node_id,des_node_id)
  * values (2,'_66_78_',2,0,11,22); insert into TEMP_PATH
  * (IGNORED_NODE_SET_ID,INTERMEDIATE_PATH) values(1,'_44_99_99_');
  */
