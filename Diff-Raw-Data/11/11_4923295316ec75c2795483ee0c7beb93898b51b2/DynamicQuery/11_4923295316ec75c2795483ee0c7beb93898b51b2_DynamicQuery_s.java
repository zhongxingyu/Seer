 /**
  * 
  */
 package com.sql.dynamicquery;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import com.sun.org.apache.bcel.internal.generic.NEW;
 
 /**
  * @author DirectXMan12
  *
  */
 public class DynamicQuery implements SQLConvertable, Collection<ITable>
 {
 	
 	private LinkedHashSet<ITable> _referencedTables;
 	private LinkedHashSet<TableColumn> _referencedCols;
 	private LinkedList<ISelectionPredicate> _whereFilters;
 	private LinkedList<OrderByFilter> _orderFilters;
 	private LinkedList<ISelectionPredicate> _havingFilters;
 	private LinkedList<IFilter> _filters;
 	private LinkedHashSet<ITable> _refTablesIgnoreFrom;
 	
 	private Class<? extends ITable> _mainClass;
 	
 	public DynamicQuery()
 	{
 		_referencedTables = new LinkedHashSet<ITable>();
 		_referencedCols = new LinkedHashSet<TableColumn>();
 		_filters = new LinkedList<IFilter>();
 		_whereFilters = new LinkedList<ISelectionPredicate>();
 		_refTablesIgnoreFrom = new LinkedHashSet<ITable>();
 		_orderFilters = new LinkedList<OrderByFilter>();
 		_havingFilters = new LinkedList<ISelectionPredicate>();
 		
 		_mainClass = null;
 	}
 	
 	public DynamicQuery(DynamicQuery q)
 	{
 		_referencedTables = (LinkedHashSet<ITable>) q.getReferencedTables().clone();
 		_referencedCols = (LinkedHashSet<TableColumn>) q.getReferencedColumns().clone();
 		_filters = (LinkedList<IFilter>) q.getFilters().clone();
 		_whereFilters = (LinkedList<ISelectionPredicate>) q.getWhereFilters().clone();
 		_refTablesIgnoreFrom = (LinkedHashSet<ITable>) q.getIgnoredReferencedTables().clone();
 		_orderFilters = (LinkedList<OrderByFilter>) q.getOrderFilters();
 		_havingFilters = (LinkedList<ISelectionPredicate>) q.getHavingFilters();
 		
 		_mainClass = q._mainClass;
 	}
 	
 	public DynamicQuery(Class<? extends ITable> mainClass)
 	{
 		_referencedTables = new LinkedHashSet<ITable>();
 		_referencedCols = new LinkedHashSet<TableColumn>();
 		_filters = new LinkedList<IFilter>();
 		_whereFilters = new LinkedList<ISelectionPredicate>();
 		_refTablesIgnoreFrom = new LinkedHashSet<ITable>();
 		_orderFilters = new LinkedList<OrderByFilter>();
 		_havingFilters = new LinkedList<ISelectionPredicate>();
 		
 		_mainClass = mainClass;
 		ITable tbl = proxyInstanceOf(_mainClass, new TableProxy(_mainClass));
 		_referencedTables.add(tbl);
 	}
 	
 	public DynamicQuery project()
 	{
 		DynamicQuery q = new DynamicQuery(this);
 		for (ITable t : _referencedTables)
 		{
 			for(Method m : t.getActualClass().getMethods())
 			{
				if((m.isAnnotationPresent(Column.class) || (m.isAnnotationPresent(BelongsTo.class) && m.getName().endsWith("Id"))) && !m.getName().startsWith("get")) // TODO: fix so includes id columns from BelongsTo
 				{
 					q.addColumn(new TableColumn(t,m));
 				}
 			}
 		}
 		
 		return q;
 	}
 	
 	protected DynamicQuery project(ITable tbl)
 	{
 		_referencedTables.add(tbl);
 		return project();
 	}
 	
 	public DynamicQuery project(TableColumn... cols)
 	{
 		DynamicQuery q = new DynamicQuery(this);
 		
 		for (TableColumn t : cols)
 		{
 			q.addColumn(t);
 		}
 		
 		return q;
 	}
 	
 	public DynamicQuery join(ITable it)
 	{
 		DynamicQuery q = new DynamicQuery(this);
 		
 		q.addReferencedTables(Arrays.asList(it));
 		q.addIgnoredReferencedTable(Arrays.asList(it));
 		q.addFilter(new JoinFilter(it));
 		return q;
 	}
 	
 	public DynamicQuery on(ISelectionPredicate p)
 	{
 		IFilter lastFilter = _filters.getLast();
 		if (!(lastFilter instanceof JoinFilter)) throw new RuntimeException("Error: previous operation wasn't a join");
 		
 		DynamicQuery q = new DynamicQuery(this);
 		JoinFilter lastJoin = (JoinFilter) q._filters.getLast();
 		lastJoin.on(p);
 		return q;
 	}
 	
 	public DynamicQuery where(ISelectionPredicate p)
 	{	
 		DynamicQuery q = new DynamicQuery(this);
 		q.addReferencedTables(p.referencedTables());
 		
 		q.addWhereFilter(p);
 		
 		return q;
 	}
 	
 	public DynamicQuery group(TableColumn col)
 	{
 		DynamicQuery q = new DynamicQuery(this);
 		q.addFilter(new GroupByFilter(col));
 		
 		return q;
 	}
 	
 	public DynamicQuery order(TableColumn col, OrderByFilter.DIRECTION dir)
 	{
 		DynamicQuery q = new DynamicQuery(this);
 		q.addOrderFilter(col, dir);
 		
 		return q;
 	}
 	
 	public DynamicQuery having(ISelectionPredicate p)
 	{
 		DynamicQuery q = new DynamicQuery(this);
 		q.addHavingFilter(p);
 		
 		return q;
 	}
 	
 	public DynamicQuery count()
 	{
 		DynamicQuery q = new DynamicQuery(this);
 		q.addColumn(new CountColumn());
 		
 		return q;
 	}
 	
 	private void addReferencedTables(List<ITable> tl)
 	{
 		/*Iterator<ITable> iter = tl.iterator();
 		ITable i;	
 		while(iter.hasNext())
 		{
 			i = iter.next();
 			if (!_referencedTables.contains(i)) _referencedTables.add(i);
 		}*/
 		_referencedTables.addAll(tl);
 	}
 	
 	private void addIgnoredReferencedTable(List<ITable> tl)
 	{
 		/*Iterator<ITable> iter = tl.iterator();
 		ITable i;	
 		while(iter.hasNext())
 		{
 			i = iter.next();
 			if (!_refTablesIgnoreFrom.contains(i)) _refTablesIgnoreFrom.add(i);
 		}*/
 		_refTablesIgnoreFrom.addAll(tl);
 	}
 	
 	public boolean isGroupingOrAggregateColumn(TableColumn c)
 	{
 		if (c instanceof IAggregateColumn) return true;
 		
 		for (IFilter f : _filters)
 		{
 			if (!(f instanceof GroupByFilter)) continue;
 			Boolean b = ((GroupByFilter)f).getReferencedColumns().contains(c);
 			if ( b ) return true;
 		}
 		
 		return false;
 	}
 	
 	public String toSql()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append("select ");
 		for (TableColumn c : _referencedCols)
 		{
 			if (_havingFilters.size() > 0 && !isGroupingOrAggregateColumn(c)) continue;
 			sb.append(c.toDefinitionSql());
 			sb.append(", ");
 		}
 		sb.delete(sb.length()-2, sb.length()-1);
 		
 		sb.append("from ");
 		
 		for (ITable t : _referencedTables)
 		{
 			if (_refTablesIgnoreFrom.contains(t)) continue;
 			sb.append(t.toSql());
 			sb.append(", ");
 		}
 		
 		sb.delete(sb.length()-2, sb.length()-1);
 		
 		if (_filters.size() > 0)
 		{
 			for (IFilter f : _filters)
 			{
 				sb.append(f.toSql());
 				sb.append(" ");
 			}
 		}
 		
 		if(_whereFilters.size() > 0)
 		{
 			//sb.append("where ");
 			sb.append(new WhereFilter(null).getKeyword());
 			sb.append(" ");
 			
 			for (ISelectionPredicate p : _whereFilters)
 			{
 				sb.append(p.toSql());
 				sb.append(" and ");
 			}
 			sb.delete(sb.length()-5, sb.length());
 		}
 		
 		if (_havingFilters.size() > 0)
 		{
 			//sb.append("having ");
 			sb.append(new HavingFilter(null).getKeyword());
 			sb.append(" ");
 			
 			for(ISelectionPredicate p : _havingFilters)
 			{
 				sb.append(p.toSql());
 				sb.append(" and ");
 			}
 			sb.delete(sb.length()-5, sb.length());
 		}
 		
 		if (_orderFilters.size() > 0)
 		{
 			//sb.append("order by ");
 			sb.append(_orderFilters.get(0).getKeyword());
 			sb.append(" ");
 			
 			for(OrderByFilter f : _orderFilters)
 			{
 				sb.append(f.subSql());
 				sb.append(", ");
 			}
 			sb.delete(sb.length()-2, sb.length());
 		}
 		
 		//sb.append(";");
 		return sb.toString();
 	}
 	
 	private LinkedHashSet<ITable> getReferencedTables()
 	{
 		return _referencedTables;
 	}
 	
 	private LinkedHashSet<TableColumn> getReferencedColumns()
 	{
 		return _referencedCols;
 	}
 	
 	private LinkedList<IFilter> getFilters()
 	{
 		return _filters;
 	}
 	
 	private LinkedList<ISelectionPredicate> getWhereFilters()
 	{
 		return _whereFilters;
 	}
 	
 	private LinkedHashSet<ITable> getIgnoredReferencedTables()
 	{
 		return _refTablesIgnoreFrom;
 	}
 	
 	private LinkedList<OrderByFilter> getOrderFilters()
 	{
 		return _orderFilters;
 	}
 	
 	private LinkedList<ISelectionPredicate> getHavingFilters()
 	{
 		return _havingFilters;
 	}
 	
 	private void addColumn(TableColumn col)
 	{
 		if(!_referencedCols.contains(col)) _referencedCols.add(col);
 	}
 	
 	private void addFilter(IFilter f)
 	{
 		if (f instanceof WhereFilter) _whereFilters.add(((WhereFilter)f).getPredicate());
 		_filters.add(f);
 	}
 	
 	private void addWhereFilter(ISelectionPredicate p)
 	{
 		_whereFilters.add(p);
 	}
 	
 	private void addOrderFilter(TableColumn col, OrderByFilter.DIRECTION dir)
 	{
 		_orderFilters.add(new OrderByFilter(col, dir));
 	}
 	
 	private void addHavingFilter(ISelectionPredicate p)
 	{
 		_havingFilters.add(p);
 	}
  
 	private LinkedBlockingDeque<ResultCluster> _results = null;
 	private LinkedBlockingDeque<ITable> _rawResults = null;
 	
 	public static <T extends ITable> T proxyInstanceOf(Class<T> tblClass, InvocationHandler proxyClassInstance)
 	{
 		return tblClass.cast( Proxy.newProxyInstance(proxyClassInstance.getClass().getClassLoader(), new Class[] {tblClass}, proxyClassInstance));
 	}
 	
 	private String getActualFullColumnName(String lcTableName, String lcColumnName)
 	{
 		// Aggregate Column
 		if (lcTableName.equals("") || lcTableName == null)
 		{
 			for (TableColumn c : _referencedCols)
 			{
 				if (c.isAliased() && c.getAlias().toLowerCase().equals(lcColumnName))
 				{
 					return c.toSql();
 				}
 			}
 		}
 			
 		// Normal Column
 		for (TableColumn c : _referencedCols)
 		{
 			if (!c.getTable().toSql().toLowerCase().equals(lcTableName)) continue;
 			Boolean aliased = c.isAliased();
 			if ((!c.isAliased() && c.getName().toLowerCase().equals(lcColumnName)) || (c.isAliased() && c.getAlias().toLowerCase().equals(lcTableName)))
 			{
 				return c.toSql();
 			}
 		}
 		
 		return null;
 	}
 	
 	protected void executeQuery()
 	{
		_results = new LinkedBlockingDeque<ResultCluster>(); 
 		
 		Connection conn = null;
 		Properties connectionProps = DynamicQueryDatabaseConfigurator.getProperties();
 	    
 	    ResultSet rs = null;
 	    
 	    try
 	    {
 			conn = DriverManager.getConnection(DynamicQueryDatabaseConfigurator.getDatabaseString(), connectionProps);
 			Statement stmt = conn.createStatement();
 		    
 			 rs = stmt.executeQuery(this.toSql());
 		}
 	    catch (SQLException e1)
 	    {
 	    	e1.printStackTrace();
 			throw new RuntimeException("[stacktrace above] Issue executing the query '"+this.toSql()+"' -- "+e1.toString());
 		}
 	    
 		try
 		{
 			HashMap<Integer, ResultCluster> groupedRes = new HashMap<Integer, ResultCluster>();
 			while(rs.next())
 			{	
 				HashMap<String, Object> cols = new HashMap<String, Object>();
 				
 				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
 				{
 					String tblName = rs.getMetaData().getTableName(i).toLowerCase();
 					String colname = rs.getMetaData().getColumnName(i).toLowerCase();
 					cols.put(getActualFullColumnName(tblName, colname), rs.getObject(i));
 				}
 				
				//_rawResults.put(proxyInstanceOf(_mainClass, new ResultRowProxy(getReferencedColumns(), cols, _mainClass))); // TODO: fix this -- currently throws NPE
 				
 				String mainClassName = proxyInstanceOf(_mainClass, new TableProxy(_mainClass)).toSql();
 				int mainKey = -1843243124;
 				try
 				{
 					mainKey = (Integer) cols.get(mainClassName+"."+"id");
 				}
 				catch (NullPointerException ex)
 				{
 					// this is a group by query, so use the hash of the combined value of the group by columns to act as the key
 					StringBuilder sb = new StringBuilder();
 					
 					for (IFilter f : _filters)
 					{
 						if (!(f instanceof GroupByFilter)) continue;
 						for (TableColumn c : ((GroupByFilter)f).getReferencedColumns())
 						{
 							sb.append(cols.get(c.toSql()));
 							sb.append(",");
 						}
 						sb.delete(sb.length()-1, sb.length());
 						
 						mainKey = sb.toString().hashCode();
 					}
 				}
 				if (!groupedRes.containsKey(mainKey))
 				{
 					// create new entry
 					ResultCluster rc = new ResultCluster(getReferencedTables(), proxyInstanceOf(_mainClass, new ResultRowProxy(getReferencedColumns(), cols, _mainClass)));
 					groupedRes.put(mainKey, rc);
 				}
 				ResultCluster cl = groupedRes.get(mainKey);
 				
 				for(Method m : _mainClass.getMethods())
 				{
 					if (m.isAnnotationPresent(HasMany.class) && !m.getName().startsWith("get")) // || m.isAnnotationPresent(HasOne.class) || m.isAnnotationPresent(BelongsTo.class) -- TODO: figure out how to work BelongsTo into this
 					{
 						Class<?> retType = _mainClass.getMethod("get"+TableProxy.ucFirstLetter(Inflector.pluralize(m.getName()))).getReturnType();
 						Class<? extends ITable> retClass;
 						if (retType.isArray()) retClass = (Class<? extends ITable>) Class.forName(retType.getName().replaceFirst("\\[.", "").replaceFirst(";", ""));
 						else retClass = (Class<? extends ITable>) retType;
 						ITable inst = proxyInstanceOf(retClass, new TableProxy(retClass));
 						
 						if (retType.isArray()) cl.putInList(inst, proxyInstanceOf(retClass, new ResultRowProxy(getReferencedColumns(), cols, retClass))); // TODO: implement for deeper nesting(i.e. lists of resultclusters)
 						else cl.putAsSingleton(inst, proxyInstanceOf(retClass, new ResultRowProxy(getReferencedColumns(), cols, retClass)));
 					}
 				}
 			}
 			
 			for (ResultCluster rc : groupedRes.values())
 			{
 				_results.put(rc);
 			}
 		}
 		catch (Exception ex)
 		{
 			System.err.println("Unable to extract query results: "+ex.toString()+" (stack trace below) --");
 			ex.printStackTrace();
 		}
 		try
 		{
 			conn.close();
 		}
 		catch (SQLException ex)
 		{
 			System.err.println("Unable to close database connection: "+ex.toString()+" (stack trace below) --");
 			ex.printStackTrace();
 		}
 	}
 	
 	protected LinkedBlockingDeque<ITable> getResults()
 	{
 		if (_results.size() == 0) return new LinkedBlockingDeque<ITable>();
 		LinkedBlockingDeque<ITable> res = new LinkedBlockingDeque<ITable>(_results.size());
 		try
 		{
 			for (ResultCluster<?> rc : _results)
 			{
 				res.put(proxyInstanceOf(rc.getMainEntryClass(), rc));
 			}
 		}
 		catch (InterruptedException e)
 		{
 			System.err.println("Unable to retrieve results: results still being processed (stack trace below) -- ");
 			e.printStackTrace();
 		}
 		
 		return res;
 	}
 	
 	protected LinkedBlockingDeque<ITable> getRawResults()
 	{
 		return _rawResults;
 	}
 	
 	public int getCount()
 	{
 		DynamicQuery countedthis = this.count();
 		countedthis.executeQuery();
 		return (Integer) ((ResultRowProxy)countedthis.getResults().getFirst()).getColumnValue("null.count(*)");
 	}
 	
 	// start of Collection<ITable> methods
 	
 	@Override
 	public int size()
 	{
 		return getCount();
 	}
 
 	@Override
 	public boolean isEmpty()
 	{
 		return getCount() == 0;
 	}
 
 	@Override
 	public boolean contains(Object o)
 	{
 		if (_results == null) executeQuery();
 		return getResults().contains(o);
 	}
 
 	@Override
 	public Iterator<ITable> iterator()
 	{
 		executeQuery();
 		return getResults().iterator(); // TODO: make this better - specifically tailored to given top-level class?
 	}
 
 	@Override
 	public Object[] toArray()
 	{
 		executeQuery();
 		return getResults().toArray();
 	}
 
 	@Override
 	public <T> T[] toArray(T[] a)
 	{
 		return _results.toArray(a);
 	}
 
 	@Override
 	public boolean add(ITable e)
 	{
 		throw new UnsupportedOperationException("Can't add to a result set");
 	}
 
 	@Override
 	public boolean remove(Object o)
 	{
 		throw new UnsupportedOperationException("Can't remove from a result set");
 	}
 
 	@Override
 	public boolean containsAll(Collection<?> c)
 	{
 		executeQuery();
 		return _results.containsAll(c);
 	}
 
 	@Override
 	public boolean addAll(Collection<? extends ITable> c)
 	{
 		throw new UnsupportedOperationException("Can't add to a result set");
 	}
 
 	@Override
 	public boolean removeAll(Collection<?> c)
 	{
 		throw new UnsupportedOperationException("Can't remove from a result set");
 	}
 
 	@Override
 	public boolean retainAll(Collection<?> c)
 	{
 		throw new UnsupportedOperationException("Can't add to or remove from a result set");
 	}
 
 	@Override
 	public void clear()
 	{
 		throw new UnsupportedOperationException("Can't remove from a result set");
 	}
 	
 }
