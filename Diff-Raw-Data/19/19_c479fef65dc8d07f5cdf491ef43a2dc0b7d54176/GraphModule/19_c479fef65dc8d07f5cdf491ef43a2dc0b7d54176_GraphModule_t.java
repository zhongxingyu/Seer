 package com.pagesociety.web.module.graph;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 
 import com.pagesociety.persistence.Entity;
 import com.pagesociety.persistence.EntityDefinition;
 import com.pagesociety.persistence.EntityIndex;
 import com.pagesociety.persistence.FieldDefinition;
 import com.pagesociety.persistence.PersistenceException;
 import com.pagesociety.persistence.Query;
 import com.pagesociety.persistence.QueryResult;
 import com.pagesociety.persistence.Types;
 import com.pagesociety.web.UserApplicationContext;
 import com.pagesociety.web.WebApplication;
 import com.pagesociety.web.exception.InitializationException;
 import com.pagesociety.web.exception.SyncException;
 import com.pagesociety.web.exception.WebApplicationException;
 import com.pagesociety.web.module.Export;
 import com.pagesociety.web.module.PagingQueryResult;
 import com.pagesociety.web.module.WebStoreModule;
 import com.pagesociety.web.module.user.UserModule;
 
 
 public class GraphModule extends WebStoreModule 
 {
 	private static final String SLOT_GUARD = "graph-guard";
 	private IGraphGuard guard;
 
 
 	
 
 	public void init(WebApplication app, Map<String,Object> config) throws InitializationException
 	{
 		super.init(app,config);
 		guard = (IGraphGuard)getSlot(SLOT_GUARD);
 	}
 	
 	protected void defineSlots()
 	{
 		super.defineSlots();
 		DEFINE_SLOT(SLOT_GUARD,IGraphGuard.class,false,DefaultGraphGuard.class);
 	}	
 	
 
 	protected int GET_GRAPH_TYPE(Entity graph)
 	{
		//TODO TOPH! 
		try
		{
			graph = GET(graph.getType(),graph.getId());
		}
		catch (PersistenceException e)
		{
			e.printStackTrace();
		}
		if (graph==null)
		{
			System.out.println("X");
		}
 		return (Integer)graph.getAttribute(GRAPH_FIELD_TYPE);
 	}
 	
 	protected boolean IS_DIRECTED(Entity g)
 	{
 		return GET_GRAPH_TYPE(g) == GRAPH_TYPE_DIRECTED;
 	}
 	
 	protected boolean IS_UNDIRECTED(Entity g)
 	{
 		return GET_GRAPH_TYPE(g) == GRAPH_TYPE_UNDIRECTED;
 	}
 	
	protected Entity GET_GRAPH(/*GraphVertex*/Entity e)
 	{
		return (Entity)e.getAttribute(GRAPH_VERTEX_FIELD_GRAPH);
 	}
 
 	/////////////////BEGIN  M O D U L E   F U N C T I O N S/////////////////////////////////////////
 
 
 	@Export
 	public Entity CreateGraph(UserApplicationContext uctx,String name,int type) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		Entity t = getGraphForUserByName(user,name);
 		if(t != null)
 			throw new WebApplicationException("GRAPH NAMED "+name+" ALREADY EXISTS FOR USER "+user.getId());
 		GUARD(guard.canCreateGraph(user));
 		return createGraph(user,name,type);
 	}
 	
 	public Entity createGraph(Entity user,String name,int type) throws PersistenceException
 	{
 
 		Entity graph =  NEW(GRAPH_ENTITY,
 						user,
 						GRAPH_FIELD_NAME,name,
 						GRAPH_FIELD_TYPE,type);
 		return graph;
 	}
 	
 
 	@Export 
 	//TODO: if we would ever want to change type we would probably want to have some
 	//conversion policy that talks about how to transform
 	public Entity UpdateGraph(UserApplicationContext uctx,Long graph_id,String name) throws WebApplicationException,PersistenceException
 	{
 		Entity user  = (Entity)uctx.getUser();
 		Entity g 	 = getGraphForUserByName(user,name);
 		if(g != null)
 			throw new WebApplicationException("GRAPH NAMED "+name+" ALREADY EXISTS FOR USER "+user.getId());
 		
 		Entity graph = GET(GRAPH_ENTITY,graph_id);
 		GUARD(guard.canUpdateGraph(user,graph,name));
 		return updateGraph(graph,name);
 	}
 	
 	Entity updateGraph(Entity graph,String name) throws WebApplicationException,PersistenceException
 	{
 		return UPDATE(graph, 
 					  GRAPH_FIELD_NAME,name);
 	}
 	
 
 	@Export
 	public List<List<Entity>> DeleteGraph(UserApplicationContext uctx,long graph_id) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		Entity graph = GET(GRAPH_ENTITY,graph_id);
 		GUARD(guard.canDeleteGraph(user,graph));
 		
 		
 		return deleteGraph(graph);
 	}
 	
 	public List<List<Entity>> deleteGraph(Entity graph) throws PersistenceException
 	{
 		//TODO: make this method..delete all edges delete all vertices//
 		return null;
 	}
 	
 
 	
 	@Export
 	public Entity CreateVertex(UserApplicationContext uctx,long graph_id,String vertice_class,String vertice_id,Entity data) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		Entity graph = GET(GRAPH_ENTITY,graph_id);
 		GUARD(guard.canCreateVertex(user,graph,vertice_class,vertice_id,data));
 		return createVertex(user,graph,vertice_class,vertice_id,data);
 	}
 	
 	//TODO: should probably check to make sure id is unique if it is not null.should do same in tree //
 	public Entity createVertex(Entity creator, Entity graph,String vertice_class,String vertice_id,Entity data) throws PersistenceException 
 	{
 		Entity new_vertice = NEW(GRAPH_VERTEX_ENTITY,
 								creator,
 								GRAPH_VERTEX_FIELD_GRAPH,graph,
 								GRAPH_VERTEX_FIELD_CLASS,vertice_class,
 								GRAPH_VERTEX_FIELD_ID,vertice_id,
 								GRAPH_VERTEX_FIELD_DATA,data);
 	
 		return new_vertice;
 	}
 
 	@Export
 	public Entity UpdateVertex(UserApplicationContext uctx,long graph_vertice_id,String vertice_class,String vertice_id,Entity data) throws WebApplicationException,PersistenceException
 	{
 		Entity user      = (Entity)uctx.getUser();
 		Entity vertex   = GET(GRAPH_VERTEX_ENTITY,graph_vertice_id);
 	
 		GUARD(guard.canUpdateVertex(user,vertex,vertice_class,vertice_id,data));
 		return updateVertex(vertex,vertice_class,vertice_id,data);
 	}
 
 	public Entity updateVertex(Entity vertex,String vertice_class,String vertice_id,Entity data) throws PersistenceException
 	{
 		return UPDATE(vertex,
 					  GRAPH_VERTEX_FIELD_ID,vertice_id,
 					  GRAPH_VERTEX_FIELD_CLASS,vertice_class,
 					  GRAPH_VERTEX_FIELD_DATA, data);
 	}
 
 	@Export //returns the vertex as the first element and all edges as the second element//
 	public List<Entity> DeleteVertex(UserApplicationContext uctx,long vertice_id) throws WebApplicationException,PersistenceException
 	{
 		Entity user 	 = (Entity)uctx.getUser();
 		Entity vertex   = GET(GRAPH_VERTEX_ENTITY,vertice_id);
 		GUARD(guard.canDeleteVertex(user,vertex));
 		return deleteVertex(vertex);
 	}
 	public List<Entity> deleteVertex(Entity vertex)
 	{
 		//TODO: implement //
 		return null;
 	}
 	
 	public static final int ERROR_BAD_GRAPH_TYPE = 0x01;
 	@Export
 	public Entity CreateEdge(UserApplicationContext uctx,long graph_id,Entity vertice1,Entity vertice2,Entity data) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		Entity graph = GET(GRAPH_ENTITY,graph_id);
 		GUARD(guard.canCreateEdge(user,graph));
 		return createEdge(user,graph,vertice1,vertice2,data);
 	}
 	
 	public Entity createEdge(Entity creator, Entity graph,Entity vertice1,Entity vertice2,Entity data) throws PersistenceException,WebApplicationException 
 	{
 		Entity new_edge = null;
 		int type = GET_GRAPH_TYPE(graph);
 		switch(type)
 		{
 		
 			case GRAPH_TYPE_DIRECTED:
 				return createDirectedEdge(creator, graph, vertice1, vertice2, data);
 			case GRAPH_TYPE_UNDIRECTED:	
 				return createUndirectedEdge(creator, graph, vertice1, vertice2, data);
 			default:
 				throw new WebApplicationException("UNKNOWN GRAPH TYPE "+GET_GRAPH_TYPE(graph),ERROR_BAD_GRAPH_TYPE);
 		}
 
 	}
 	
 	public Entity createDirectedEdge(Entity creator, Entity graph,Entity vertice1,Entity vertice2,Entity data) throws PersistenceException,WebApplicationException 
 	{
 		Entity new_edge = NEW(GRAPH_DIRECTED_EDGE_ENTITY,
 				   creator,
 				   GRAPH_EDGE_FIELD_GRAPH,graph,
 				   GRAPH_DIRECTED_EDGE_FIELD_ORIGIN,vertice1,
 				   GRAPH_DIRECTED_EDGE_FIELD_DESTINATION,vertice2,
 				   GRAPH_EDGE_FIELD_DATA,data);
 		
 		return new_edge;
 	}
 	
 	
 	public Entity createUndirectedEdge(Entity creator, Entity graph,Entity vertice1,Entity vertice2,Entity data) throws PersistenceException,WebApplicationException 
 	{
 		List<Entity> v_list = new ArrayList<Entity>();
 		v_list.add(vertice1);
 		v_list.add(vertice2);
 		
 		Entity new_edge = NEW(GRAPH_UNDIRECTED_EDGE_ENTITY,
 				   creator,
 				   GRAPH_EDGE_FIELD_GRAPH,graph,
 				   GRAPH_UNDIRECTED_EDGE_FIELD_VERTICES,v_list,
 				   GRAPH_EDGE_FIELD_DATA,data);			
 		return new_edge;
 	}
 	
 	@Export 
 	public Entity DeleteEdge(UserApplicationContext uctx,long graph_id,long edge_id) throws WebApplicationException,PersistenceException
 	{
 		Entity user 	 = (Entity)uctx.getUser();
 		Entity graph	 = GET(GRAPH_ENTITY,graph_id);
 		Entity edge = null;
 		int type = GET_GRAPH_TYPE(graph);
 		switch(type)
 		{
 			case GRAPH_TYPE_DIRECTED:
 				edge = GET(GRAPH_DIRECTED_EDGE_ENTITY,edge_id);
 				break;
 			case GRAPH_TYPE_UNDIRECTED:
 				edge = GET(GRAPH_UNDIRECTED_EDGE_ENTITY,edge_id);
 			default:
 				throw new WebApplicationException("UNKNOWN GRAPH TYPE "+GET_GRAPH_TYPE(graph),ERROR_BAD_GRAPH_TYPE);
 		}
 		GUARD(guard.canDeleteEdge(user,graph,edge));
 		return deleteEdge(edge);
 	}
 	
 	public Entity deleteEdge(Entity edge) throws PersistenceException
 	{
 		return DELETE(edge);
 	}
 	
 	@Export //TODO: make data...data type and data and do GET just to be consistent and ensure data correctness//
 	public Entity UpdateEdge(UserApplicationContext uctx,long graph_id,long graph_edge_id,long vertice1_id,long vertice2_id,String data_type,long data_id) throws WebApplicationException,PersistenceException
 	{
 		Entity user      			  = (Entity)uctx.getUser();
 		Entity graph				  = GET(GRAPH_ENTITY,graph_id);
 		Entity data = null;
 		if(data_type != null)
 			data = GET(data_type,data_id);
 		Entity edge = null;
 		
 		switch(GET_GRAPH_TYPE(graph))
 		{
 			case GRAPH_TYPE_DIRECTED:
 				
 				GUARD(guard.canUpdateEdge(user,graph,edge));
 				return updateDirectedEdge(edge, GET(GRAPH_VERTEX_ENTITY,vertice1_id),GET(GRAPH_VERTEX_ENTITY,vertice2_id),data);
 			case GRAPH_TYPE_UNDIRECTED:
 				edge = GET(GRAPH_UNDIRECTED_EDGE_ENTITY,graph_edge_id);
 				GUARD(guard.canUpdateEdge(user,graph,edge));
 				return updateUndirectedEdge(GET(GRAPH_UNDIRECTED_EDGE_ENTITY,graph_edge_id), GET(GRAPH_VERTEX_ENTITY,vertice1_id),GET(GRAPH_VERTEX_ENTITY,vertice2_id),data);
 			default:
 				throw new WebApplicationException("UNKNOWN GRAPH TYPE "+GET_GRAPH_TYPE(graph),ERROR_BAD_GRAPH_TYPE);
 		}
 	}
 
 	public Entity updateEdge(Entity graph,Entity edge,Entity vertice1,Entity vertice2,Entity data) throws PersistenceException,WebApplicationException
 	{
 		switch(GET_GRAPH_TYPE(graph))
 		{
 			case GRAPH_TYPE_DIRECTED:
 				return updateDirectedEdge(edge, vertice1,vertice2,data);
 			case GRAPH_TYPE_UNDIRECTED:
 				return updateUndirectedEdge(edge, vertice1,vertice2,data);
 			default:
 				throw new WebApplicationException("UNKNOWN GRAPH TYPE "+GET_GRAPH_TYPE(graph),ERROR_BAD_GRAPH_TYPE);
 		}	
 	}
 	
 	public Entity updateDirectedEdge(Entity edge,Entity origin_vertice,Entity destination_vertice,Entity data) throws PersistenceException
 	{
 		return UPDATE(edge,
 					  GRAPH_DIRECTED_EDGE_FIELD_ORIGIN,origin_vertice,
 					  GRAPH_DIRECTED_EDGE_FIELD_DESTINATION,destination_vertice,
 					  GRAPH_VERTEX_FIELD_DATA, data);
 	}
 	
 	public Entity updateUndirectedEdge(Entity edge,Entity vertice1,Entity vertice2,Entity data) throws PersistenceException
 	{
 		List<Entity> v_list = new ArrayList<Entity>(2);
 		v_list.add(vertice1);
 		v_list.add(vertice2);
 		return UPDATE(edge,
 					  GRAPH_UNDIRECTED_EDGE_FIELD_VERTICES,v_list,
 					  GRAPH_VERTEX_FIELD_DATA, data);
 	}
 	
 	
 	@Export
 	public Entity CloneGraph(UserApplicationContext uctx,long graph_id,String new_graph_name) throws WebApplicationException,PersistenceException
 	{
 		throw new WebApplicationException("UNIMPLEMENTED");
 		//TODO: clone it babi!
 	}
 
 	
 	@Export
 	public PagingQueryResult GetUserGraphs(UserApplicationContext uctx,int offset,int page_size,String order_by) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		GUARD(guard.canGetGraphsForUser(user,user));
 		Query q = getGraphsByUserByNameQ(user, Query.VAL_GLOB);
 		q.offset(offset);
 		q.pageSize(page_size);
 		if(order_by != null)
 			q.orderBy(order_by);
 		return PAGING_QUERY(q);
 	}
 	
 	@Export
 	public PagingQueryResult GetGraphsForUser(UserApplicationContext uctx,long user_id,int offset,int page_size,String order_by) throws WebApplicationException,PersistenceException
 	{
 		Entity user   = (Entity)uctx.getUser();
 		Entity target = GET(UserModule.USER_ENTITY,user_id);//is this bad??..should user module be a slot?
 		GUARD(guard.canGetGraphsForUser(user,target));
 		Query q = getGraphsByUserByNameQ(target,Query.VAL_GLOB);
 		q.offset(offset);
 		q.pageSize(page_size);
 		if(order_by != null)
 			q.orderBy(order_by);
 		return PAGING_QUERY(q);
 	}
 	
 	@Export
 	public Entity GetGraphForUserByName(UserApplicationContext uctx,long user_id,String name) throws WebApplicationException,PersistenceException
 	{
 		Entity user   = (Entity)uctx.getUser();
 		Entity target = GET(UserModule.USER_ENTITY,user_id);//is this bad??..should user module be a slot?
 		Entity graph  = getGraphForUserByName(target,name);
 		GUARD(guard.canGetGraph(user,graph));		
 		
 		if(graph == null)
 			return null;
 		return graph;
 	}
 	
 	public Entity getGraphForUserByName(Entity user,String name) throws PersistenceException
 	{
 		Query q = getGraphsByUserByNameQ(user, name);
 		q.idx(IDX_GRAPH_BY_CREATOR_BY_GRAPH_NAME);
 		q.eq(q.list(user,name));
 		QueryResult result = QUERY(q);			
 		
 		if(result.size() == 0)
 			return null;
 		else
 			return result.getEntities().get(0);
 	}
 	
 	
 	public Query getGraphsByUserByNameQ(Entity user,Object name) throws PersistenceException
 	{
 		Query q = new Query(GRAPH_ENTITY);
 		q.idx(IDX_GRAPH_BY_CREATOR_BY_GRAPH_NAME);
 		q.eq(q.list(user,name));
 		return q;
 	}
 	
 	@Export
 	public Entity GetUserGraphByName(UserApplicationContext uctx,String name) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		Entity graph = getGraphForUserByName(user,name);
 		if(graph == null)
 			return null;
 		GUARD(guard.canGetGraph(user,graph));
 		return graph;
 	}
 	
 	public Entity getGraphById(long graph_id) throws PersistenceException
 	{
 		return GET(GRAPH_ENTITY,graph_id);
 	}
 	
 	@Export
 	public PagingQueryResult GetGraphVertices(UserApplicationContext uctx, long graph_id,int offset, int page_size, String order_by) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		Entity graph = GET(GRAPH_ENTITY,graph_id);
 		GUARD(guard.canGetGraphVertices(user,graph));
 		Query q = getGraphVerticesByClassQ(graph, Query.VAL_GLOB);
 		q.offset(offset);
 		q.pageSize(page_size);
 		if(order_by != null)
 			q.orderBy(order_by);
 		return PAGING_QUERY(q);
 	}
 	
 	@Export
 	public PagingQueryResult GetGraphVerticesByClass(UserApplicationContext uctx, long graph_id,String vertice_classname,int offset, int page_size, String order_by) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		Entity graph = GET(GRAPH_ENTITY,graph_id);
 		GUARD(guard.canGetGraphVerticesByClass(user,graph,vertice_classname));
 		Query q = getGraphVerticesByClassQ(graph, vertice_classname);
 		q.offset(offset);
 		q.pageSize(page_size);
 		if(order_by != null)
 			q.orderBy(order_by);
 		return PAGING_QUERY(q);
 	}
 	
 	public Query getGraphVerticesByClassQ(Entity graph,Object vertex_classname) throws PersistenceException
 	{
 		Query q = new Query(GRAPH_VERTEX_ENTITY);
 		q.idx(IDX_GRAPH_VERTEX_BY_GRAPH_BY_VERTICE_CLASS);
 		q.eq(q.list(graph,vertex_classname));
 		return q;
 	}
 	
 	@Export
 	public Entity GetGraphVerticeById(UserApplicationContext uctx, long graph_id,String vertex_id) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		Entity graph = GET(GRAPH_ENTITY,graph_id);
 		GUARD(guard.canGetGraphVertexById(user,vertex_id));
 		return getGraphVertexById(graph,vertex_id);
 		
 	}
 	//TODO: ids should probably be enforced as unique unless it is null.
 	//same thing goes for treemodule as well!
 	public Entity getGraphVertexById(Entity graph,String vertex_id) throws PersistenceException
 	{
 		Query q = new Query(GRAPH_VERTEX_ENTITY);
 		q.idx(IDX_GRAPH_VERTEX_BY_GRAPH_BY_VERTICE_ID);
 		q.eq(q.list(graph,vertex_id));
 		QueryResult result = QUERY(q);			
 		if(result.size() == 0)
 			return null;
 		return result.getEntities().get(0);
 	}
 	
 	
 	@Export
 	public Entity FillVertexData(UserApplicationContext uctx,long vertex_id,int data_ref_fill_depth) throws WebApplicationException,PersistenceException
 	{
 		Entity user    = (Entity)uctx.getUser();
 		Entity vertex = GET(GRAPH_VERTEX_ENTITY,vertex_id);
 		GUARD(guard.canFillVerticeData(user,vertex,data_ref_fill_depth));
 		do_fill_data((Entity)vertex.getAttribute(GRAPH_VERTEX_FIELD_DATA),0,data_ref_fill_depth);
 		return vertex;
 	}
 	
 	public void do_fill_data(Entity data,int c,int d) throws PersistenceException
 	{
 		if(data == null || c == d)
 			return;
 		
 		FILL_REFS(data);
 		EntityDefinition def = store.getEntityDefinition(data.getType());
 		List<FieldDefinition> r_fields = def.getReferenceFields();
 		int s = r_fields.size();
 		for(int i = 0;i < s;i++)
 		{
 			FieldDefinition fd = r_fields.get(i);
 			String ref_field_name = fd.getName();
 			if(fd.isArray())
 			{
 				c++;
 				List<Entity> l = (List<Entity>)data.getAttribute(ref_field_name);
 				if(l == null)
 					return;
 				for(int ii = 0;ii < l.size();ii++)
 					do_fill_data(l.get(ii),c,d);
 
 			}
 			else
 			{
 				Entity val = (Entity)data.getAttribute(ref_field_name);
 				do_fill_data(val, c++, d);
 			}
 		}
 	}
 	
 	//////GRAPH FUNCTIONS////
 	
 	//Returns a directed outgoing edge from this vertex to v, or an undirected edge that connects this vertex to v.
 	 public Entity findEdge(Entity v1,Entity v2) throws PersistenceException,WebApplicationException
 	 {
 		 List<Entity> edges = findEdgeSet(v1,v2);
 		 if(edges.size() == 0)
 			 return null;
 		 return edges.get(0);
 	 }
 	 
 	 //Returns the set ofall edges that connect this vertex with the specified vertex v.
 	 public List<Entity> findEdgeSet(Entity v1,Entity v2) throws PersistenceException,WebApplicationException
 	 {
 		 Query q = findEdgeSetQ(v1, v2);
 		 return QUERY(q).getEntities();
 	 }
 	 
 	 public PagingQueryResult findEdgeSet(Entity v1,Entity v2,int offset,int page_size,String edge_order_by,int dir) throws PersistenceException,WebApplicationException
 	 {
 		 Query q = findEdgeSetQ(v1, v2);
 		 if(edge_order_by != null)
 			 q.orderBy(edge_order_by, dir);
 		 q.offset(offset);
 		 q.pageSize(page_size);
 		 return PAGING_QUERY(q);
 	 }
 	 
 	 public Query findEdgeSetQ(Entity v1,Entity v2) throws PersistenceException,WebApplicationException
 	 {
 		 Entity graph = GET_GRAPH(v1);
 		 Query q = null;
 		 switch(GET_GRAPH_TYPE(graph))
 		 {
 		 	case GRAPH_TYPE_DIRECTED:
 		 		q = new Query(GRAPH_DIRECTED_EDGE_ENTITY);
 		 		q.idx(IDX_GRAPH_DIRECTED_EDGE_BY_ORIGIN_BY_DESTINATION);
 		 		q.eq(q.list(v1,v2));
 		 		break;
 		 	case GRAPH_TYPE_UNDIRECTED:
 		 		q = new Query(GRAPH_DIRECTED_EDGE_ENTITY);
 		 		q.idx(IDX_GRAPH_UNDIRECTED_EDGE_BY_VERTICES);
 		 		q.setContainsAll(q.list(v1,v2));
 		 		break;
 		 	default:
 		 		throw new WebApplicationException("UNKNOWN GRAPH TYPE "+GET_GRAPH_TYPE(graph),ERROR_BAD_GRAPH_TYPE);	 		
 		 }
 	 	return q;
 	 }
 	 //TODO: make sure v is EXPANDed,probably want to pass in graph so we re not looking it up all the time//
 	 //or denormalize type into graph vertex and edge//
 	 //Returns the set of incoming edges of this vertex.
 	 public List<Entity> getInEdges(Entity v) throws PersistenceException,WebApplicationException
 	 {
 		 Query q = getInEdgesQ(v);
 		 return QUERY(q).getEntities();
 	 }
 	 
 	 public PagingQueryResult getInEdges(Entity v,int offset,int page_size,String edge_order_by,int dir) throws PersistenceException,WebApplicationException
 	 {
 		 Query q = getInEdgesQ(v);
 		 if(edge_order_by != null)
 			 q.orderBy(edge_order_by,dir);
 		 q.offset(offset);
 		 q.pageSize(page_size);
 		 return PAGING_QUERY(q);		 
 	 }
 	 
 	 public Query getInEdgesQ(Entity v) throws PersistenceException,WebApplicationException
 	 {
 		 Entity graph = GET_GRAPH(v);
 		 Query q = null;
 		 switch(GET_GRAPH_TYPE(graph))
 		 {
 		 	case GRAPH_TYPE_DIRECTED:
 				 q = new Query(GRAPH_DIRECTED_EDGE_ENTITY);
 				 q.idx(IDX_GRAPH_DIRECTED_EDGE_BY_DESTINATION_BY_ORIGIN);
 				 q.eq(q.list(v,Query.VAL_GLOB));
 		 		 break;
 		 	case GRAPH_TYPE_UNDIRECTED:
 				 q = new Query(GRAPH_UNDIRECTED_EDGE_ENTITY);
 				 q.idx(IDX_GRAPH_UNDIRECTED_EDGE_BY_VERTICES);
 				 q.setContainsAny(q.list(v));
 		 		 break;
 		 	default:
 		 		throw new WebApplicationException("UNKNOWN GRAPH TYPE "+GET_GRAPH_TYPE(graph),ERROR_BAD_GRAPH_TYPE);	 		
 		 }
 
 		 
 		return q;
 	  }
 	 
 	 //Returns the set of outgoing edges of this vertex.
 	 public List<Entity> getOutEdges(Entity v) throws PersistenceException,WebApplicationException
 	 {
 		 return QUERY(getOutEdgesQ(v)).getEntities();
 	 }
 	 
 	 public PagingQueryResult getOutEdges(Entity v,int offset,int page_size,String edge_order_by,int dir) throws PersistenceException,WebApplicationException
 	 {
 		 Query q = getOutEdgesQ(v);
 		 if(edge_order_by != null)
 			 q.orderBy(edge_order_by,dir);
 		 q.offset(offset);
 		 q.pageSize(page_size);
 		 return PAGING_QUERY(q);
 	 }
 	 
 	 public Query getOutEdgesQ(Entity v) throws PersistenceException,WebApplicationException
 	 {
 		 Entity graph = GET_GRAPH(v);
 		 Query q = null;
 		 switch(GET_GRAPH_TYPE(graph))
 		 {
 		 	case GRAPH_TYPE_DIRECTED:
 				 q = new Query(GRAPH_DIRECTED_EDGE_ENTITY);
 				 q.idx(IDX_GRAPH_DIRECTED_EDGE_BY_ORIGIN_BY_DESTINATION);
 				 q.eq(q.list(v,Query.VAL_GLOB));
 		 		 break;
 		 	case GRAPH_TYPE_UNDIRECTED:
 				 q = new Query(GRAPH_UNDIRECTED_EDGE_ENTITY);
 				 q.idx(IDX_GRAPH_UNDIRECTED_EDGE_BY_VERTICES);
 				 q.setContainsAny(q.list(v));
 		 		 break;
 		 	default:
 		 		throw new WebApplicationException("UNKNOWN GRAPH TYPE "+GET_GRAPH_TYPE(graph),ERROR_BAD_GRAPH_TYPE);	 		
 		 }
 		return q;
 	 }
 	 
 	 //	Returns the set of predecessors of this vertex.
 	 List<Entity> getPredecessors(Entity v) throws PersistenceException,WebApplicationException
 	 {
 		 List<Entity> edges 		 = getInEdges(v);
 		 List<Entity> pl 			 = new ArrayList<Entity>();
 
 		 int s = edges.size();
 		 for(int i = 0;i < s;i++)
 		 {
 			 Entity edge = edges.get(i);
 			 Entity p = null;
 			 if(is_directed_edge(edge))
 				 pl.add(getSource(edge));
 			 else 
 				 pl.add(getOpposite(v,edge));
 		 }
 		 return pl;
 	 }
 	 
 	 public PagingQueryResult getPredecessors(Entity v,int offset,int page_size,String edge_order_by,int dir) throws PersistenceException,WebApplicationException
 	 {
 		 PagingQueryResult edges_pqr = getInEdges(v,offset,page_size,edge_order_by,dir);
 		 List<Entity> pl 			 = new ArrayList<Entity>();
 
 		 List<Entity> edges = edges_pqr.getEntities();
 		 int s = edges.size();
 		 for(int i = 0;i < s;i++)
 		 {
 			 Entity edge = edges.get(i);
 			 Entity p = null;
 			 if(is_directed_edge(edge))
 				 pl.add(getSource(edge));
 			 else 
 				 pl.add(getOpposite(v,edge));
 		 }
 		 return new PagingQueryResult(pl,edges_pqr.size(),offset,page_size);
 	 }
 	 
 	 
      //Returns the set of successors of this vertex.     
 	 public List<Entity> 	getSuccessors(Entity v) throws PersistenceException,WebApplicationException
 	 {
 	 	 List<Entity> edges = getOutEdges(v);
 		 List<Entity> successors 	= new ArrayList<Entity>();
 		 int s = edges.size();
 		 for(int i = 0;i < s;i++)
 		 {
 			 Entity edge = edges.get(i);
 			 if(is_directed_edge(edge))
 				 successors.add(getDestination(edge));
 			 else 
 				 successors.add(getOpposite(v,edge));
 		 }
 		 return successors;
 	 }
 	 
 	 public PagingQueryResult 	getSuccessors(Entity v,int offset,int page_size,String edge_order_by,int dir) throws PersistenceException,WebApplicationException
 	 {
 	 	 PagingQueryResult edges_pqr = getOutEdges(v,offset,page_size,edge_order_by,dir);
 		 List<Entity> successors 	= new ArrayList<Entity>();
 		 List<Entity> edges = edges_pqr.getEntities();
 		 int s = edges.size();
 		 for(int i = 0;i < s;i++)
 		 {
 			 Entity edge = edges.get(i);
 			 if(is_directed_edge(edge))
 				 successors.add(getDestination(edge));
 			 else 
 				 successors.add(getOpposite(v,edge));
 		 }
 		 return new PagingQueryResult(successors,edges_pqr.size(),offset,page_size);
 	 }
 	
 	 //Returns the number of incoming edges that are incident to this vertex.
 	 public int inDegree(Entity v) throws PersistenceException,WebApplicationException
 	 {	
 		 return getInEdges(v).size();
 	 }
 	 
 	 //Returns true if this vertex is a destination of the specified edge e, and false otherwise.
 	 public boolean isDest(Entity v, Entity edge)
 	 {
 		 return(((Entity)edge.getAttribute(GRAPH_DIRECTED_EDGE_FIELD_DESTINATION)).equals(v));
 
 	}
      //Returns true if this vertex is a predecessor of the specified vertex v, and false otherwise.
 	 public boolean isPredecessorOf(Entity v1,Entity v2) throws PersistenceException,WebApplicationException
 	 {
 		 Entity graph = GET_GRAPH(v1);
 		 Query q = null;
 		 switch(GET_GRAPH_TYPE(graph))
 		 {
 		 	case GRAPH_TYPE_DIRECTED:
 				 q = new Query(GRAPH_DIRECTED_EDGE_ENTITY);
 				 q.idx(IDX_GRAPH_DIRECTED_EDGE_BY_ORIGIN_BY_DESTINATION);
 				 q.eq(q.list(v1,v2));
 		 		 break;
 		 	case GRAPH_TYPE_UNDIRECTED:
 				 q = new Query(GRAPH_UNDIRECTED_EDGE_ENTITY);
 				 q.idx(IDX_GRAPH_UNDIRECTED_EDGE_BY_VERTICES);
 				 q.setContainsAll(q.list(v1,v2));
 		 		 break;
 		 	default:
 		 		throw new WebApplicationException("UNKNOWN GRAPH TYPE "+GET_GRAPH_TYPE(graph),ERROR_BAD_GRAPH_TYPE);	 		
 		 }
 
 		 return (QUERY(q).getEntities().size() != 0);
 		 
 	 }
      //Returns true if this vertex is a source of the specified edge e, and false otherwise.
 	 public boolean isSource(Entity v,Entity edge)
 	 {	
 		 return(((Entity)edge.getAttribute(GRAPH_DIRECTED_EDGE_FIELD_ORIGIN)).equals(v));
 	 }
      //Returns true if this vertex is a successor of the specified vertex v, and false otherwise.
 	 public boolean isSuccessorOf(Entity v1,Entity v2) throws PersistenceException,WebApplicationException
 	 {
 		 Entity graph = GET_GRAPH(v1);
 		 Query q = null;
 		 switch(GET_GRAPH_TYPE(graph))
 		 {
 		 	case GRAPH_TYPE_DIRECTED:
 				 q = new Query(GRAPH_DIRECTED_EDGE_ENTITY);
 				 q.idx(IDX_GRAPH_DIRECTED_EDGE_BY_DESTINATION_BY_ORIGIN);
 				 q.eq(q.list(v1,v2));
 		 		 break;
 		 	case GRAPH_TYPE_UNDIRECTED:
 				 q = new Query(GRAPH_UNDIRECTED_EDGE_ENTITY);
 				 q.idx(IDX_GRAPH_UNDIRECTED_EDGE_BY_VERTICES);
 				 q.setContainsAll(q.list(v1,v2));
 		 		 break;
 		 	default:
 		 		throw new WebApplicationException("UNKNOWN GRAPH TYPE "+GET_GRAPH_TYPE(graph),ERROR_BAD_GRAPH_TYPE);	 		
 		 }
 
 		 return (QUERY(q).getEntities().size() != 0);
 
 	 }
 	 
      // Returns the number of predecessors of this vertex.
 	 public int numPredecessors(Entity v) throws PersistenceException,WebApplicationException
 	 {
 		 return getPredecessors(v).size();
 	 }
      //Returns the number of successors of this vertex.
 	 public int numSuccessors(Entity v) throws PersistenceException,WebApplicationException
 	 {
 		 return getSuccessors(v).size();
 	 }
 	 //Returns the number of outgoing edges that are incident to this vertex.
 	 
 	 public int outDegree(Entity v) throws PersistenceException,WebApplicationException
 	 {
 		 return getOutEdges(v).size();
 	 } 
 	
 	 public Entity getSource(Entity edge) throws PersistenceException
 	 {
 		 return FILL_REFS((Entity)edge.getAttribute(GRAPH_DIRECTED_EDGE_FIELD_ORIGIN));
 	 }
 	 
 	 public Entity getDestination(Entity edge) throws PersistenceException
 	 {
 		 return FILL_REFS((Entity)edge.getAttribute(GRAPH_DIRECTED_EDGE_FIELD_DESTINATION));
 	 }
 	 
 	 public Entity getOpposite(Entity v,Entity edge) throws PersistenceException
 	 {
 		 if(is_undirected_edge(edge))
 		 {
 			 List<Entity> v_list = (List<Entity>)edge.getAttribute(GRAPH_UNDIRECTED_EDGE_FIELD_VERTICES);
 			 Entity candidate = v_list.get(0);
 			 if(candidate.getId() == v.getId())
 				 return FILL_REFS(v_list.get(1));
 			 else
 				 return FILL_REFS(candidate);
 		 }
 		 else
 		 //
 		 {
 			 if(isSource(v, edge))
 				 return getDestination(edge);
 			 else
 				 return getSource(edge);
 		 }
 	 }
 	 
 	 private boolean is_directed_edge(Entity edge)
 	 {
 		 return (Integer)edge.getAttribute(GRAPH_EDGE_FIELD_TYPE) == EDGE_TYPE_DIRECTED;
 	 }
 	 
 	 private boolean is_undirected_edge(Entity edge)
 	 {
 		 return (Integer)edge.getAttribute(GRAPH_EDGE_FIELD_TYPE) == EDGE_TYPE_UNDIRECTED;
 	 }
 	 
 	/////////////////E N D  M O D U L E   F U N C T I O N S/////////////////////////////////////////
 		
 	public static final String GRAPH_ENTITY 					= "Graph";
 	public static final String GRAPH_FIELD_NAME 				= "name";
 	public static final String GRAPH_FIELD_TYPE 				= "type";
 
 	public static final String GRAPH_VERTEX_ENTITY			= "GraphVertex";
 	public static final String GRAPH_VERTEX_FIELD_GRAPH		= "graph";
 	public static final String GRAPH_VERTEX_FIELD_ID		= "vertex_id";
 	public static final String GRAPH_VERTEX_FIELD_CLASS		= "vertex_class";
 	public static final String GRAPH_VERTEX_FIELD_DATA 		= "data";
 	public static final String GRAPH_VERTEX_FIELD_METADATA 	= "metadata";
 	
 	public static final String GRAPH_EDGE_FIELD_GRAPH			= "graph";
 	public static final String GRAPH_EDGE_FIELD_TYPE			= "type";
 	public static final String GRAPH_EDGE_FIELD_WEIGHT			= "weight";
 	public static final String GRAPH_EDGE_FIELD_DATA			= "data";
 	public static final String GRAPH_EDGE_FIELD_METADATA		= "metadata";
 	
 	public static final String GRAPH_DIRECTED_EDGE_ENTITY				= "GraphDirectedEdge";
 	public static final String GRAPH_DIRECTED_EDGE_FIELD_ORIGIN			= "origin";
 	public static final String GRAPH_DIRECTED_EDGE_FIELD_DESTINATION	= "destination";
 
 	
 	public static final String GRAPH_UNDIRECTED_EDGE_ENTITY	    		= "GraphUndirectedEdge";
 	public static final String GRAPH_UNDIRECTED_EDGE_FIELD_VERTICES		= "vertices";
 
 	
 	public static final int GRAPH_TYPE_UNDEFINED  = 0x00;
 	public static final int GRAPH_TYPE_UNDIRECTED = 0x01;
 	public static final int GRAPH_TYPE_DIRECTED   = 0x02;
 	public static final int GRAPH_TYPE_MIXED      = 0x03;
 	
 	public static final int EDGE_TYPE_UNDEFINED     = 0x00;
 	public static final int EDGE_TYPE_DIRECTED      = 0x01;
 	public static final int EDGE_TYPE_UNDIRECTED    = 0x02;
 
 	protected void defineEntities(Map<String,Object> config) throws PersistenceException,SyncException
 	{
 		DEFINE_ENTITY(GRAPH_ENTITY,
 					  GRAPH_FIELD_NAME,Types.TYPE_STRING,null,
 					  GRAPH_FIELD_TYPE,Types.TYPE_INT,GRAPH_TYPE_UNDEFINED);
 
 		DEFINE_ENTITY(GRAPH_VERTEX_ENTITY,
 				GRAPH_VERTEX_FIELD_GRAPH,Types.TYPE_REFERENCE, GRAPH_ENTITY,null,
 				GRAPH_VERTEX_FIELD_ID,Types.TYPE_STRING,"",
 				GRAPH_VERTEX_FIELD_CLASS,Types.TYPE_STRING,"",
 				GRAPH_VERTEX_FIELD_DATA,Types.TYPE_REFERENCE, FieldDefinition.REF_TYPE_UNTYPED_ENTITY,null,
 				GRAPH_VERTEX_FIELD_METADATA,Types.TYPE_STRING,"");
 		
 		DEFINE_ENTITY(GRAPH_DIRECTED_EDGE_ENTITY,
 				  GRAPH_EDGE_FIELD_GRAPH,Types.TYPE_REFERENCE, GRAPH_ENTITY,null,
 				  GRAPH_EDGE_FIELD_TYPE,Types.TYPE_INT,EDGE_TYPE_DIRECTED,
 				  GRAPH_DIRECTED_EDGE_FIELD_ORIGIN,Types.TYPE_REFERENCE, GRAPH_VERTEX_ENTITY,null,
 				  GRAPH_DIRECTED_EDGE_FIELD_DESTINATION,Types.TYPE_REFERENCE, GRAPH_VERTEX_ENTITY,null,
 				  GRAPH_EDGE_FIELD_WEIGHT,Types.TYPE_FLOAT,0f,
 				  GRAPH_EDGE_FIELD_DATA,Types.TYPE_REFERENCE, FieldDefinition.REF_TYPE_UNTYPED_ENTITY,null,
 				  GRAPH_EDGE_FIELD_METADATA,Types.TYPE_STRING,"");
 		
 		DEFINE_ENTITY(GRAPH_UNDIRECTED_EDGE_ENTITY,
 				  GRAPH_EDGE_FIELD_GRAPH,Types.TYPE_REFERENCE, GRAPH_ENTITY,null,
 				  GRAPH_EDGE_FIELD_TYPE,Types.TYPE_INT,EDGE_TYPE_UNDIRECTED,
 				  GRAPH_UNDIRECTED_EDGE_FIELD_VERTICES,Types.TYPE_REFERENCE | Types.TYPE_ARRAY, GRAPH_VERTEX_ENTITY,null,
 				  GRAPH_EDGE_FIELD_WEIGHT,Types.TYPE_FLOAT,0f,
 				  GRAPH_EDGE_FIELD_DATA,Types.TYPE_REFERENCE, FieldDefinition.REF_TYPE_UNTYPED_ENTITY,null,
 				  GRAPH_EDGE_FIELD_METADATA,Types.TYPE_STRING,"");
 		
 	}
 
 	public static final String IDX_GRAPH_BY_CREATOR_BY_GRAPH_NAME 				= "byCreatorByGraphName";
 	public static final String IDX_GRAPH_VERTEX_BY_GRAPH_BY_VERTICE_CLASS		= "byGraphByVerticeClass";
 	public static final String IDX_GRAPH_VERTEX_BY_GRAPH_BY_VERTICE_ID			= "byGraphByVerticeId";
 	public static final String IDX_GRAPH_DIRECTED_EDGE_BY_ORIGIN_BY_DESTINATION = "byOriginByDestination";
 	public static final String IDX_GRAPH_DIRECTED_EDGE_BY_DESTINATION_BY_ORIGIN = "byDestinationOrigin";
 	public static final String IDX_GRAPH_UNDIRECTED_EDGE_BY_VERTICES            = "byVertices";
 	public static final String IDX_GRAPH_EDGE_BY_GRAPH			   			    = "byGraph";
 	
 	protected void defineIndexes(Map<String,Object> config) throws PersistenceException,SyncException
 	{
 		DEFINE_ENTITY_INDEX(GRAPH_ENTITY,IDX_GRAPH_BY_CREATOR_BY_GRAPH_NAME , EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX, FIELD_CREATOR,GRAPH_FIELD_NAME);
 		DEFINE_ENTITY_INDEX(GRAPH_VERTEX_ENTITY,IDX_GRAPH_VERTEX_BY_GRAPH_BY_VERTICE_CLASS , EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX,GRAPH_VERTEX_FIELD_GRAPH,GRAPH_VERTEX_FIELD_CLASS);
 		DEFINE_ENTITY_INDEX(GRAPH_VERTEX_ENTITY,IDX_GRAPH_VERTEX_BY_GRAPH_BY_VERTICE_ID , EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX, GRAPH_VERTEX_FIELD_GRAPH,GRAPH_VERTEX_FIELD_ID);
 		DEFINE_ENTITY_INDEX(GRAPH_DIRECTED_EDGE_ENTITY,IDX_GRAPH_DIRECTED_EDGE_BY_ORIGIN_BY_DESTINATION, EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX,GRAPH_DIRECTED_EDGE_FIELD_ORIGIN,GRAPH_DIRECTED_EDGE_FIELD_DESTINATION);
 		DEFINE_ENTITY_INDEX(GRAPH_DIRECTED_EDGE_ENTITY,IDX_GRAPH_DIRECTED_EDGE_BY_DESTINATION_BY_ORIGIN, EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX,GRAPH_DIRECTED_EDGE_FIELD_DESTINATION,GRAPH_DIRECTED_EDGE_FIELD_ORIGIN);
 		DEFINE_ENTITY_INDEX(GRAPH_DIRECTED_EDGE_ENTITY,IDX_GRAPH_EDGE_BY_GRAPH, EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, GRAPH_EDGE_FIELD_GRAPH);
 		DEFINE_ENTITY_INDEX(GRAPH_UNDIRECTED_EDGE_ENTITY,IDX_GRAPH_EDGE_BY_GRAPH, EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, GRAPH_EDGE_FIELD_GRAPH);
 		DEFINE_ENTITY_INDEX(GRAPH_UNDIRECTED_EDGE_ENTITY,IDX_GRAPH_UNDIRECTED_EDGE_BY_VERTICES, EntityIndex.TYPE_ARRAY_MEMBERSHIP_INDEX, GRAPH_UNDIRECTED_EDGE_FIELD_VERTICES);
 	}
 	
 	protected void defineRelationships(Map<String,Object> config) throws PersistenceException,SyncException
 	{
 	//	DEFINE_ENTITY_RELATIONSHIP(TREE_NODE_ENTITY, TREE_NODE_FIELD_PARENT_NODE, EntityRelationshipDefinition.TYPE_ONE_TO_MANY, TREE_NODE_ENTITY, TREE_NODE_FIELD_CHILDREN);
 	}
 
 }
