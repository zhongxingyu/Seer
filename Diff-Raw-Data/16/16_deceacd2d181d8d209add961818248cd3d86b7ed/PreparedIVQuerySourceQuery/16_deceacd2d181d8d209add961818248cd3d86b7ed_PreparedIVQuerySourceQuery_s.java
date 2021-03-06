 /*******************************************************************************
  * Copyright (c) 2004, 2007 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.data.engine.impl;
 
 /**
  * 
  */
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.birt.core.data.DataType;
 import org.eclipse.birt.core.data.ExpressionUtil;
 import org.eclipse.birt.core.exception.BirtException;
 import org.eclipse.birt.data.engine.api.IBaseQueryDefinition;
 import org.eclipse.birt.data.engine.api.IBaseQueryResults;
 import org.eclipse.birt.data.engine.api.IBinding;
 import org.eclipse.birt.data.engine.api.IComputedColumn;
 import org.eclipse.birt.data.engine.api.IQueryDefinition;
 import org.eclipse.birt.data.engine.api.IQueryResults;
 import org.eclipse.birt.data.engine.api.IResultMetaData;
 import org.eclipse.birt.data.engine.api.querydefn.Binding;
 import org.eclipse.birt.data.engine.api.querydefn.GroupDefinition;
 import org.eclipse.birt.data.engine.api.querydefn.QueryDefinition;
 import org.eclipse.birt.data.engine.api.querydefn.ScriptExpression;
 import org.eclipse.birt.data.engine.api.querydefn.SubqueryDefinition;
 import org.eclipse.birt.data.engine.api.querydefn.SubqueryLocator;
 import org.eclipse.birt.data.engine.core.DataException;
 import org.eclipse.birt.data.engine.executor.BaseQuery;
 import org.eclipse.birt.data.engine.executor.ResultClass;
 import org.eclipse.birt.data.engine.executor.ResultFieldMetadata;
 import org.eclipse.birt.data.engine.executor.ResultObject;
 import org.eclipse.birt.data.engine.executor.transform.CachedResultSet;
 import org.eclipse.birt.data.engine.impl.document.viewing.NewInstanceHelper;
 import org.eclipse.birt.data.engine.odi.IDataSetPopulator;
 import org.eclipse.birt.data.engine.odi.IDataSource;
 import org.eclipse.birt.data.engine.odi.IEventHandler;
 import org.eclipse.birt.data.engine.odi.IQuery;
 import org.eclipse.birt.data.engine.odi.IResultClass;
 import org.eclipse.birt.data.engine.odi.IResultIterator;
 import org.eclipse.birt.data.engine.odi.IResultObject;
 import org.mozilla.javascript.Scriptable;
 
 /**
  * When query is applied with a different group from the original group, this
  * instance will be used to regenerate the query result.
  */
 class PreparedIVQuerySourceQuery extends PreparedDataSourceQuery
 {
 
 	private DataEngineImpl engine;
 	private IQueryDefinition queryDefn;
 	private IQueryResults queryResults;
 
 	/**
 	 * @param dataEngine
 	 * @param queryDefn
 	 * @throws DataException
 	 */
 	PreparedIVQuerySourceQuery( DataEngineImpl dataEngine,
 			IQueryDefinition queryDefn ) throws DataException
 	{
 		super( dataEngine, queryDefn, null, null );
 		Object[] params = {
 				dataEngine, queryDefn
 		};
 		logger.entering( PreparedIVDataSourceQuery.class.getName( ),
 				"PreparedIVNestedQuery",
 				params );
 
 		this.queryDefn = queryDefn;
 		this.engine = dataEngine;
 		IBinding[] bindings = null;
 		if ( this.queryDefn.getSourceQuery( ) instanceof SubqueryLocator )
 		{
 			this.queryResults = engine.getQueryResults( getParentQueryResultsID( (SubqueryLocator) ( queryDefn.getSourceQuery( ) ) ) );
 			IQueryDefinition queryDefinition = queryResults.getPreparedQuery( )
 					.getReportQueryDefn( );
 			bindings = getSubQueryBindings( queryDefinition,
 					( (SubqueryLocator) queryDefn.getSourceQuery( ) ).getName( ) );
 		}
 		else
 		{
 			this.queryResults = engine.getQueryResults( ( (IQueryDefinition) queryDefn.getSourceQuery( ) ).getQueryResultsID( ) );
			IQueryDefinition queryDefinition = queryResults.getPreparedQuery( )
					.getReportQueryDefn( );
			bindings = (IBinding[]) queryDefinition.getBindings( )
					.values( ).toArray( new IBinding[0] );
 		}
 		for ( int i = 0; i < bindings.length; i++ )
 		{
 			IBinding binding = bindings[i];
 			this.queryDefn.addBinding( new Binding( binding.getBindingName( ),
 					new ScriptExpression( ExpressionUtil.createJSDataSetRowExpression( binding.getBindingName( ) ),
 							binding.getDataType( ) ) ) );
 		}
 		preparedQuery = new PreparedQuery( dataEngine.getSession( ),
 				dataEngine.getContext( ),
 				this.queryDefn,
 				this,
 				appContext );
 		logger.exiting( PreparedIVDataSourceQuery.class.getName( ),
 				"PreparedIVNestedQuery" );
 	}
 
 	/**
 	 * 
 	 * @param queryDefinition
 	 * @param subQueryName
 	 * @return
 	 */
 	private static IBinding[] getSubQueryBindings(
 			IBaseQueryDefinition queryDefinition, String subQueryName )
 	{
 		List groups = queryDefinition.getGroups( );
 		if ( groups != null )
 		{
 			for ( int i = 0; i < groups.size( ); i++ )
 			{
 				GroupDefinition groupDefinition = (GroupDefinition) groups.get( i );
 				if ( groupDefinition.getSubqueries( ) != null )
 				{
 					SubqueryDefinition[] subqueryDefinitions = (SubqueryDefinition[]) groupDefinition.getSubqueries( )
 							.toArray( new SubqueryDefinition[0] );
 					IBinding[] bindings = getSubQueryBindings( subqueryDefinitions,
 							subQueryName );
 					if ( bindings != null )
 					{
 						return bindings;
 					}
 				}
 			}
 		}
 		if( queryDefinition.getSubqueries( ) != null )
 		{
 			SubqueryDefinition[] subqueryDefinitions = (SubqueryDefinition[]) queryDefinition.getSubqueries( )
 					.toArray( new SubqueryDefinition[0] );
 			return getSubQueryBindings( subqueryDefinitions, subQueryName );
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param subqueryDefinitions
 	 * @param subQueryName
 	 * @return
 	 */
 	private static IBinding[] getSubQueryBindings(
 			SubqueryDefinition[] subqueryDefinitions, String subQueryName )
 	{
 		for ( int j = 0; j < subqueryDefinitions.length; j++ )
 		{
 			if ( subqueryDefinitions[j].getName( ) != null
 					&& subqueryDefinitions[j].getName( )
 							.equals( subQueryName ) )
 			{
 				return (IBinding[]) subqueryDefinitions[j].getBindings( )
 						.values( )
 						.toArray( new IBinding[0] );
 			}
 			IBinding[] bindings = getSubQueryBindings( subqueryDefinitions[j], subQueryName );
 			if ( bindings != null )
 			{
 				return bindings;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param subqueryLocator
 	 * @return
 	 */
 	private String getParentQueryResultsID( SubqueryLocator subqueryLocator )
 	{
 		IBaseQueryDefinition baseQueryDefinition = subqueryLocator.getParentQuery( );
 		while ( !( baseQueryDefinition instanceof QueryDefinition ) )
 		{
 			baseQueryDefinition = baseQueryDefinition.getParentQuery( );
 		}
 		return ( (QueryDefinition) baseQueryDefinition ).getQueryResultsID( );
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.birt.data.engine.impl.PreparedDataSourceQuery#newExecutor()
 	 */
 	protected QueryExecutor newExecutor( )
 	{
 		return new IVQuerySourceExecutor( engine.getSession( ).getSharedScope( ) );
 	}
 
 	/**
 	 * Dummy implementation.
 	 */
 	public Collection getParameterMetaData( ) throws BirtException
 	{
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.birt.data.engine.impl.PreparedDataSourceQuery#produceQueryResults
 	 * (org.eclipse.birt.data.engine.api.IBaseQueryResults,
 	 * org.mozilla.javascript.Scriptable)
 	 */
 	protected IQueryResults produceQueryResults(
 			IBaseQueryResults outerResults, Scriptable scope )
 			throws DataException
 	{
 		QueryResults queryResults = preparedQuery.doPrepare( outerResults,
 				scope,
 				newExecutor( ),
 				this );
 		return queryResults;
 	}
 
 	/**
 	 * 
 	 *
 	 */
 	private class IVQuerySourceExecutor extends QueryExecutor
 	{
 
 		private Scriptable queryScope;
 		private BaseQuery query;
 		private DataSetRuntime dsRuntime;
 		private IResultClass resultClass;
 
 		/**
 		 * @param sharedScope
 		 * @param baseQueryDefn
 		 * @param aggrTable
 		 */
 		IVQuerySourceExecutor( Scriptable sharedScope )
 		{
 			super( preparedQuery.getSharedScope( ),
 					preparedQuery.getBaseQueryDefn( ),
 					preparedQuery.getAggrTable( ),
 					dataEngine.getSession( ) );
 		}
 
 		/*
 		 * @seeorg.eclipse.birt.data.engine.impl.PreparedQuery.Executor#
 		 * createOdiDataSource()
 		 */
 		protected IDataSource createOdiDataSource( ) throws DataException
 		{
 			return NewInstanceHelper.newDataSource( );
 		}
 
 		/*
 		 * @see
 		 * org.eclipse.birt.data.engine.impl.PreparedQuery.Executor#findDataSource
 		 * ()
 		 */
 		protected DataSourceRuntime findDataSource( ) throws DataException
 		{
 			return NewInstanceHelper.newDataSourceRuntime( queryScope );
 		}
 
 		/*
 		 * @seeorg.eclipse.birt.data.engine.impl.PreparedQuery.Executor#
 		 * newDataSetRuntime()
 		 */
 		protected DataSetRuntime newDataSetRuntime( ) throws DataException
 		{
 			dsRuntime = new DataSetRuntime( NewInstanceHelper.newIVDataSetDesign( ),
 					this );
 
 			return dsRuntime;
 		}
 
 		/*
 		 * @see
 		 * org.eclipse.birt.data.engine.impl.QueryExecutor#getResultMetaData()
 		 */
 		public IResultMetaData getResultMetaData( ) throws DataException
 		{
 			return new ResultMetaData( getResultClass( ) );
 		}
 
 		/*
 		 * @see
 		 * org.eclipse.birt.data.engine.impl.PreparedQuery.Executor#createOdiQuery
 		 * ()
 		 */
 		protected IQuery createOdiQuery( ) throws DataException
 		{
 			query = new IVQuerySourceQuery( getResultClass( ) );
 			return query;
 		}
 
 		/**
 		 * 
 		 * @return
 		 * @throws DataException
 		 */
 		private IResultClass getResultClass( ) throws DataException
 		{
 			IBinding[] bindings = null;
 			IQueryDefinition queryDefinition = queryResults.getPreparedQuery( )
 					.getReportQueryDefn( );
 
 			if ( queryDefn.getSourceQuery( ) instanceof SubqueryLocator )
 			{
 				bindings = getSubQueryBindings( queryDefinition,
 						( (SubqueryLocator) queryDefn.getSourceQuery( ) ).getName( ) );
 			}
 			else
 			{
 				bindings = (IBinding[]) ( queryDefinition.getBindings( )
 						.values( ).toArray( new IBinding[0] ) );
 			}
 			resultClass = createResultClass( bindings, temporaryComputedColumns );
 
 			return resultClass;
 		}
 
 		/**
 		 * 
 		 * @param exprMetaInfo
 		 * @param temporaryComputedColumns
 		 * @return
 		 * @throws DataException
 		 */
 		private IResultClass createResultClass( IBinding[] bindings,
 				List temporaryComputedColumns ) throws DataException
 		{
 			ResultFieldMetadata rfm = null;
 			ArrayList<ResultFieldMetadata> projectedColumns = new ArrayList<ResultFieldMetadata>( );
 			if ( bindings != null )
 			{
 				for ( int i = 0; i < bindings.length; i++ )
 				{
 					Class result = null;
 					result = DataType.getClass( bindings[i].getDataType( ) );
 					if ( result == null )
 					{
 						result = String.class;
 					}
 					rfm = new ResultFieldMetadata( i,
 							bindings[i].getBindingName( ),
 							"",
 							result,
 							null,
 							false );
 					projectedColumns.add( rfm );
 				}
 			}
 			for ( int i = 0; i < temporaryComputedColumns.size( ); i++ )
 			{
 				IComputedColumn computedColumn = (IComputedColumn) temporaryComputedColumns.get( i );
 				Class result = DataType.getClass( computedColumn.getDataType( ) );
 				if ( result == null )
 				{
 					result = String.class;
 				}
 				rfm = new ResultFieldMetadata( i,
 						computedColumn.getName( ),
 						"",
 						result,
 						null,
 						true );
 				projectedColumns.add( rfm );
 			}
 			return new ResultClass( projectedColumns );
 		}
 
 		/*
 		 * @see
 		 * org.eclipse.birt.data.engine.impl.PreparedQuery.Executor#executeOdiQuery
 		 * ()
 		 */
 		protected IResultIterator executeOdiQuery( IEventHandler eventHandler,
 				StopSign stopSign ) throws DataException
 		{
 
 			IResultIterator resultIterator;
 			try
 			{
 				org.eclipse.birt.data.engine.api.IResultIterator sourceResultIterator = queryResults.getResultIterator( );
 				if ( queryDefn.getSourceQuery( ) instanceof SubqueryLocator )
 				{
 					sourceResultIterator = getSubQueryIterator( (SubqueryLocator) queryDefn.getSourceQuery( ),
 							sourceResultIterator );
 				}
 				IDataSetPopulator querySourcePopulator = new IVQuerySourcePopulator( sourceResultIterator,
 						getResultClass( ),
 						query );
 				resultIterator = new CachedResultSet( query,
 						resultClass,
 						querySourcePopulator,
 						eventHandler,
 						engine.getSession( ),
 						stopSign );
 				return resultIterator;
 			}
 			catch ( BirtException e )
 			{
 				throw DataException.wrap( e );
 			}
 
 		}
 		
 		/**
 		 * 
 		 * @param subqueryLocator
 		 * @param sourceResultIterator
 		 * @return
 		 * @throws BirtException 
 		 */
 		private org.eclipse.birt.data.engine.api.IResultIterator getSubQueryIterator(
 				SubqueryLocator subqueryLocator,
 				org.eclipse.birt.data.engine.api.IResultIterator sourceResultIterator )
 				throws BirtException
 		{
 			org.eclipse.birt.data.engine.api.IResultIterator resultIterator = sourceResultIterator;
 			if ( subqueryLocator.getParentQuery( ) != null && subqueryLocator.getParentQuery( ) instanceof SubqueryLocator )
 			{
 				resultIterator = getSubQueryIterator( (SubqueryLocator) subqueryLocator.getParentQuery( ),
 						sourceResultIterator );
 			}
 			resultIterator.moveTo( subqueryLocator.getRowId( ) );
 			return resultIterator.getSecondaryIterator( subqueryLocator.getName( ), null );
 		}
 
 	}
 
 	/**
 	 * 
 	 * @author Administrator
 	 * 
 	 */
 	private class IVQuerySourcePopulator implements IDataSetPopulator
 	{
 
 		private org.eclipse.birt.data.engine.api.IResultIterator apiResultIterator = null;
 		private IResultClass resultClass = null;
 		private String[] fieldNames = null;
 
 		IVQuerySourcePopulator(
 				org.eclipse.birt.data.engine.api.IResultIterator apiResultIterator,
 				IResultClass resultClass, BaseQuery query )
 				throws DataException
 		{
 			this.apiResultIterator = apiResultIterator;
 			this.resultClass = resultClass;
 			fieldNames = resultClass.getFieldNames( );
 
 		}
 
 		/**
 		 * 
 		 */
 		public IResultObject next( ) throws DataException
 		{
 			try
 			{
 				if ( !apiResultIterator.next( ) )
 				{
 					return null;
 				}
 			}
 			catch ( BirtException e )
 			{
 				throw DataException.wrap( e );
 			}
 			Object[] fields = new Object[fieldNames.length];
 			for ( int i = 0; i < fields.length; i++ )
 			{
 				try
 				{
 					if ( !resultClass.isCustomField( i + 1 ) )
 					{
 						fields[i] = apiResultIterator.getValue( fieldNames[i] );
 					}
 				}
 				catch ( BirtException e )
 				{
 					throw DataException.wrap( e );
 				}
 			}
 			IResultObject result = new ResultObject( resultClass, fields );
 
 			return result;
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 	public class IVQuerySourceQuery extends BaseQuery
 	{
 
 		//
 		private IResultClass meta;
 
 		/**
 		 * Constructor
 		 * 
 		 * @param resultClass
 		 */
 		public IVQuerySourceQuery( IResultClass resultClass )
 		{
 			meta = resultClass;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.birt.data.engine.odi.IQuery#close()
 		 */
 		public void close( )
 		{
 
 		}
 
 		/**
 		 * Return the result class of this joint data set.
 		 * 
 		 * @return
 		 */
 		public IResultClass getResultClass( )
 		{
 			return meta;
 		}
 
 	}
 
 }
