 /*******************************************************************************
  * Copyright (c) 2004 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.data.engine.api;
 
import java.io.File;
 import java.io.IOException;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 import org.eclipse.birt.core.archive.IDocArchiveReader;
 import org.eclipse.birt.core.archive.IDocArchiveWriter;
 import org.eclipse.birt.core.archive.RAInputStream;
 import org.eclipse.birt.core.archive.RAOutputStream;
 import org.eclipse.birt.core.exception.BirtException;
 import org.eclipse.birt.data.engine.core.DataException;
 import org.eclipse.birt.data.engine.i18n.ResourceConstants;
 import org.mozilla.javascript.Scriptable;
 
 import com.ibm.icu.util.ULocale;
 
 /**
  * Define in which context Data Engine is running. The context can be divided
  * into three types: generation, presentation and direct presentation. 
  */
 public class DataEngineContext
 {
 	/** three defined mode */
 	public final static int MODE_GENERATION = 1;
 	public final static int MODE_PRESENTATION = 2;
 	public final static int DIRECT_PRESENTATION  = 3;
 	
 	/**
 	 * this is a special mode, that a query which is running on a report
 	 * document also needs to be stored into the same report document. This is a
 	 * update operation, and it is a combination mode of presentation and
 	 * generation. One note is in running in this mode, the report document must
 	 * be same for reading and writing.
 	 */
 	public final static int MODE_UPDATE = 4;
 	
 	/**
 	 * AppContext and Data Set cache count setting decide whether cache is used,
 	 * which is default value for data engine context.
 	 */
 	public final static int CACHE_USE_DEFAULT = 1;
 	
 	/**
 	 * Do not use cache, regardless of data set cache setting
 	 */
 	public final static int CACHE_USE_DISABLE = 2;
 	
 	/**
 	 * Always use cached data if available, disregard data set cache setting and
 	 * AppContext. cachRowCount parameter decides cache count.
 	 */
 	public final static int CACHE_USE_ALWAYS = 3;
 	
 	public final static int CACHE_MODE_IN_MEMORY = 1;
 	
 	public final static int CACHE_MODE_IN_DISK = 2;
 	
 	/** some fields */
 	private int mode;
 	private Scriptable scope;	
 	private IDocArchiveReader reader;
 	private IDocArchiveWriter writer;
 	private ULocale currentLocale;
 	
 	/** cacheCount field */
 	private int cacheOption;
 	private int cacheCount;
 
 	private String tmpDir = System.getProperty( "java.io.tmpdir" );
 	
 	/** stream id for internal use, don't use it externally */
 	public final static int VERSION_INFO_STREAM = 11;
 	
 	public final static int DATASET_DATA_STREAM = 21;
 	
 	public final static int DATASET_META_STREAM = 22;
 	
 	public final static int EXPR_VALUE_STREAM = 31;
 	
 	/**
 	 *@deprecated
 	 */
 	public final static int EXPR_META_STREAM = 32;
 	
 	/**
 	 */
 	public final static int EXPR_ROWLEN_STREAM = 33;
 	
 	/**
 	 *@deprecated
 	 */
 	public final static int GROUP_INFO_STREAM = 41;
 	
 	public final static int SUBQUERY_INFO_STREAM = 42;
 	
 	// current query definition
 	/**
 	 *@deprecated
 	 */
 	public final static int QUERY_DEFN_STREAM = 43;
 	
 	// original query defintion
 	public final static int ORIGINAL_QUERY_DEFN_STREAM = 44;
 	
 	// row index to the base rd
 	/**
 	 *@deprecated
 	 */
 	public final static int ROW_INDEX_STREAM = 51;
 	
 	// manage query running on based rd
 	/**
 	 *@deprecated
 	 */
 	public final static int QUERYID_INFO_STREAM = 61;
 	
 	// parent index to the base subquery rd
 	/**
 	 *@deprecated
 	 */
 	public final static int SUBQUERY_PARENTINDEX_STREAM = 71;
 	
 	public final static int META_STREAM = 99;
 	
 	public final static int META_INDEX_STREAM = 100;
 	
 	private static Logger logger = Logger.getLogger( DataEngineContext.class.getName( ) );
 	
 	/**
 	 * When mode is MODE_GENERATION, the writer stream of archive will be used.
 	 * When mode is MODE_PRESENTATION, the reader stream of archive will be used.
 	 * When mode is DIRECT_PRESENTATION, the archive will not be used.
 	 * When mode is PRESENTATION_AND_GENERATION, both the write stream and the read 
 	 * steram of archive will be used. 
 	 * 
 	 * @param mode
 	 * @param scope
 	 * @param reader
 	 * @param writer
 	 * @return an instance of DataEngineContext
 	 */
 	public static DataEngineContext newInstance( int mode, Scriptable scope,
 			IDocArchiveReader reader, IDocArchiveWriter writer )
 			throws BirtException
 	{
 		return new DataEngineContext( mode, scope, reader, writer );
 	}
 	
 	/**
 	 * @param mode
 	 * @param scope
 	 * @param reader
 	 * @param writer
 	 * @throws BirtException
 	 */
 	private DataEngineContext( int mode, Scriptable scope,
 			IDocArchiveReader reader, IDocArchiveWriter writer )
 			throws BirtException
 	{
 		Object[] params = {
 				new Integer( mode ), scope, reader, writer
 		};
 		logger.entering( DataEngineContext.class.getName( ),
 				"DataEngineContext",
 				params );
 		
 		if ( !( mode == MODE_GENERATION
 				|| mode == MODE_PRESENTATION || mode == DIRECT_PRESENTATION || mode == MODE_UPDATE ) )
 			throw new DataException( ResourceConstants.RD_INVALID_MODE );
 
 		if ( writer == null && mode == MODE_GENERATION )
 			throw new DataException( ResourceConstants.RD_INVALID_ARCHIVE );
 
 		if ( reader == null && mode == MODE_PRESENTATION )
 			throw new DataException( ResourceConstants.RD_INVALID_ARCHIVE );
 
 		if ( ( writer == null || reader == null )
 				&& mode == MODE_UPDATE )
 			throw new DataException( ResourceConstants.RD_INVALID_ARCHIVE );
 
 		this.mode = mode;
 		this.scope = scope;
 		this.reader = reader;
 		this.writer = writer;
 		this.cacheOption = CACHE_USE_DEFAULT;
 		logger.exiting( DataEngineContext.class.getName( ), "DataEngineContext" );
 	}
 
 	/** 
 	 * @return current context mode
 	 */
 	public int getMode( )
 	{
 		return mode;
 	}
 
 	/**
 	 * @return current top scope
 	 */
 	public Scriptable getJavaScriptScope( )
 	{
 		return scope;
 	}
 
 	/**
 	 * @return cacheCount
 	 */
 	public int getCacheOption( )
 	{
 		return this.cacheOption;
 	}
 	
 	/**
 	 * @return cacheCount
 	 */
 	public int getCacheCount( )
 	{
 		return this.cacheCount;
 	}
 
 	/**
 	 * This method is used to set the cache option for current data engine
 	 * instance. These option values will override the values defined in
 	 * individual data set and its application context. The option value has
 	 * three posible values, CACHE_USE_DEFAULT, CACHE_USE_DISABLE,
 	 * CACHE_USE_ALWAYS. The cacheCount values can be larger than 0, which
 	 * indicates the count of how many rows will be ccached, equal to 0, which
 	 * indicates cache will not be used, less than 0, which indicates the entire
 	 * data set will be cached.
 	 * 
 	 * Please notice, this cache function only available for
 	 * DIRECT_PRESENTATION. In other cases, exception will be thrown.
 	 * 
 	 * @param option
 	 * @param cacheCount
 	 */
 	public void setCacheOption( int option, int cacheCount )
 			throws BirtException
 	{
 		if ( this.mode != DIRECT_PRESENTATION )
 			throw new DataException( ResourceConstants.CACHE_FUNCTION_WRONG_MODE );
 
 		this.cacheOption = option;
 		this.cacheCount = cacheCount;
 	}
 	
 	/**
 	 * According to the paramters of streamID, subStreamID and streamType, an
 	 * output stream will be created for it. To make stream close simply, the
 	 * stream needs to be closed by caller, and then caller requires to add
 	 * buffer stream layer when needed.
 	 * 
 	 * @param streamID
 	 * @param subStreamID
 	 * @param streamType
 	 * @return output stream for specified streamID, subStreamID and streamType
 	 */
 	public RAOutputStream getOutputStream( String streamID, String subStreamID,
 			int streamType ) throws DataException
 	{
 		assert writer != null;
 		
 		String relativePath = getPath( streamID, subStreamID, streamType );
 		
 		try
 		{
 			RAOutputStream outputStream = writer.openRandomAccessStream( relativePath );
 
 			if ( outputStream == null )
 				throw new DataException( ResourceConstants.RD_SAVE_STREAM_ERROR );
 
 			return outputStream;
 		}
 		catch (IOException e)
 		{
 			throw new DataException( ResourceConstants.RD_SAVE_STREAM_ERROR, e );
 		}
 	}
 
 	/**
 	 * Determins whether one particular stream exists
 	 * 
 	 * @param streamID
 	 * @param subStreamID
 	 * @param streamType
 	 * @return boolean value
 	 */
 	public boolean hasOutStream( String streamID, String subStreamID,
 			int streamType )
 	{
 		String relativePath = getPath( streamID, subStreamID, streamType );
 
 		if ( writer != null )
 			return writer.exists( relativePath );
 		else
 			return false;
 	}
 	
 	/**
 	 * @param streamID
 	 * @param subStreamID
 	 * @param streamType
 	 * @return
 	 */
 	public boolean hasInStream( String streamID, String subStreamID,
 			int streamType )
 	{
 		String relativePath = getPath( streamID, subStreamID, streamType );
 
 		if ( reader != null )
 			return reader.exists( relativePath );
 		else
 			return false;
 	}
 	
 	/**
 	 * @param streamID
 	 * @param subStreamID
 	 * @param streamType
 	 */
 	public void dropStream( String streamID, String subStreamID, int streamType )
 	{
 		String relativePath = getPath( streamID, subStreamID, streamType );
 		
 		if ( writer != null )
 			writer.dropStream( relativePath );
 	}
 	
 	/**
 	 * Directly drop stream
 	 * 
 	 * @param streamPath
 	 */
 	public void dropStream( String streamPath )
 	{
 		if ( writer != null )
 			writer.dropStream( streamPath );
 	}
 
 	/**
 	 * According to the paramters of streamID, subStreamID and streamType, an
 	 * input stream will be created for it. To make stream close simply, the
 	 * stream needs to be closed by caller, and then caller requires to add
 	 * buffer stream layer when needed.
 	 * 
 	 * @param streamID
 	 * @param subStreamID
 	 * @param streamType
 	 * @return input stream for specified streamID, subStreamID and streamType
 	 */
 	public RAInputStream getInputStream( String streamID, String subStreamID,
 			int streamType ) throws DataException
 	{
 		assert reader != null;
 
 		String relativePath = getPath( streamID, subStreamID, streamType );
 		
 		try
 		{
 			RAInputStream inputStream = reader.getStream( relativePath );
 			
 			if ( inputStream == null )
 			{
 				throw new DataException( ResourceConstants.RD_LOAD_STREAM_ERROR );
 			}
 			
 			return inputStream;
 		}
 		catch (IOException e)
 		{
 			throw new DataException( ResourceConstants.RD_LOAD_STREAM_ERROR, e );
 		}
 	}
 	
 	/**
 	 * @param locale
 	 *            The current task's locale
 	 */
 	public void setLocale( Locale locale )
 	{
 		currentLocale = ULocale.forLocale( locale );
 		DataException.setLocale( currentLocale );
 
 	}
 
 	/**
 	 * @return The current locale
 	 */
 	public ULocale getLocale( )
 	{
 		return currentLocale;
 	}
 	
 	/**
 	 * get Dte temporary dir.
 	 * @return
 	 */
 	public String getTmpdir( )
 	{
		if( !tmpDir.endsWith( File.separator ))
		{
			return tmpDir + File.separator;
		}
 		return tmpDir;
 	}
 
 	/**
 	 * set Dte temporary dir.
 	 * @param tmpdir
 	 */
 	public void setTmpdir( String tmpdir )
 	{
 		this.tmpDir = tmpdir;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public IDocArchiveReader getDocReader()
 	{
 		return this.reader;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public IDocArchiveWriter getDocWriter()
 	{
 		return this.writer;
 	}
 	
 	/**
 	 * @param streamType
 	 * @return relative path, notice in reading data from file, directory can
 	 *         not be created.
 	 */
 	public static String getPath( String streamID, String subStreamID, int streamType )
 	{
 		if ( streamType == VERSION_INFO_STREAM )
 			return "/DataEngine/VesionInfo";
 		
 		String relativePath = null;
 		switch ( streamType )
 		{
 			case DATASET_DATA_STREAM :
 				relativePath = "DataSetData";
 				break;
 			case DATASET_META_STREAM :
 				relativePath = "ResultClass";
 				break;
 			case EXPR_VALUE_STREAM :
 				relativePath = "ExprValue";
 				break;
 			case EXPR_ROWLEN_STREAM :
 				relativePath = "ExprRowLen";
 				break;
 			case EXPR_META_STREAM :
 				relativePath = "ExprMetaInfo";
 				break;
 			case GROUP_INFO_STREAM :
 				relativePath = "GroupInfo";
 				break;
 			case SUBQUERY_INFO_STREAM :
 				relativePath = "SubQueryInfo";
 				break;
 			case QUERY_DEFN_STREAM :
 				relativePath = "QueryDefn";
 				break;
 			case ORIGINAL_QUERY_DEFN_STREAM:
 				relativePath = "OriginalQueryDefn";
 				break;
 			case ROW_INDEX_STREAM:
 				relativePath = "RowIndex";
 				break;
 			case QUERYID_INFO_STREAM :
 				relativePath = "QueryIDInfo";
 				break;
 			case SUBQUERY_PARENTINDEX_STREAM :
 				relativePath = "ParentIndex";
 				break;
 			case META_STREAM :
 				relativePath = "Meta";
 				break;
 			case META_INDEX_STREAM :
 				relativePath = "MetaIndex";
 				break;
 			default :
 				assert false; // impossible
 		}
 		
 		String streamRoot = "/" + streamID + "/";
 		if ( subStreamID != null )
 			streamRoot += subStreamID + "/";
 		return streamRoot + relativePath;
 	}	
 
 }
