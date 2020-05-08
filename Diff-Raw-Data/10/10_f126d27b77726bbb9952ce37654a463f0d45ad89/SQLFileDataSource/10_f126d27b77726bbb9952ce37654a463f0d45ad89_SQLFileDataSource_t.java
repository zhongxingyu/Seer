 package yapto.datasource.sqlfile;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.file.FileAlreadyExistsException;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.search.Query;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import yapto.datasource.AbstractIdBasedPictureBrowser;
 import yapto.datasource.IDataSource;
 import yapto.datasource.IPictureBrowser;
 import yapto.datasource.PictureAddException;
 import yapto.datasource.PictureAddExceptionType;
 import yapto.datasource.PictureInformation;
 import yapto.datasource.index.PictureIndexer;
 import yapto.datasource.process.PictureProcessor;
 import yapto.datasource.sqlfile.config.ISQLFileDataSourceConfiguration;
 import yapto.datasource.tag.ITag;
 import yapto.datasource.tag.IWritableTagRepository;
 import yapto.datasource.tag.TagAddException;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.cache.RemovalListener;
 import com.google.common.eventbus.EventBus;
 
 /**
  * {@link IDataSource} using an SQLite file to stock the meta-informations, and
  * a standard filesystem for the pictures.
  * 
  * @author benobiwan
  * 
  */
 public class SQLFileDataSource implements IDataSource<FsPicture>
 {
 	/**
 	 * Logger object.
 	 */
 	protected static transient final Logger LOGGER = LoggerFactory
 			.getLogger(SQLFileDataSource.class);
 
 	/**
 	 * List of all picture id.
 	 */
 	protected final List<String> _pictureIdList = new Vector<>();
 
 	/**
 	 * Configuration for this {@link SQLFileDataSource}.
 	 */
 	private final ISQLFileDataSourceConfiguration _conf;
 
 	/**
 	 * {@link LoadingCache} used to load the {@link FsPicture}.
 	 */
 	protected final LoadingCache<String, FsPicture> _pictureCache;
 
 	/**
 	 * {@link ImageLoader} used to load the {@link BufferedImage}.
 	 */
 	private final ImageLoader _imageLoader;
 
 	/**
 	 * Object holding the connection to the database and the prepared
 	 * statements.
 	 */
 	private final SQLFileListConnection _fileListConnection;
 
 	/**
 	 * {@link EventBus} used to signal registered objects of changes in the
 	 * {@link IPictureBrowser}.
 	 */
 	protected final EventBus _bus;
 
 	/**
 	 * Object used to index {@link FsPicture}s.
 	 */
 	private final PictureIndexer _indexer;
 
 	/**
 	 * Queue holding all the {@link FsPicture} to update.
 	 */
 	protected final BlockingQueue<FsPicture> _updateQueue = new LinkedBlockingQueue<>();
 
 	/**
 	 * {@link Runnable} used to update modified {@link FsPicture}.
 	 */
 	private final PictureUpdater _updater;
 
 	/**
 	 * {@link PictureProcessor} used to execute external commands on pictures.
 	 */
 	private final PictureProcessor _processor;
 
 	/**
 	 * {@link IWritableTagRepository} used to load and save {@link ITag}s.
 	 */
 	private final IWritableTagRepository _tagRepository;
 
 	/**
 	 * Time to wait before writing picture information to the database.
 	 */
 	private final long _lWaitBeforeWrite;
 
 	/**
 	 * Creates a new SQLFileDataSource.
 	 * 
 	 * @param conf
 	 *            configuration for this {@link SQLFileDataSource}.
 	 * @param bus
 	 *            the {@link EventBus} used to signal registered objects of
 	 *            changes in the {@link IPictureBrowser}.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the connection to the
 	 *             database.
 	 * @throws ClassNotFoundException
 	 *             if the database driver class can't be found.
 	 * @throws IOException
 	 *             if there is an error in creating the required picture
 	 *             directories.
 	 */
 	public SQLFileDataSource(final ISQLFileDataSourceConfiguration conf,
 			final EventBus bus) throws SQLException, ClassNotFoundException,
 			IOException
 	{
 		_bus = bus;
 		_conf = conf;
 		_fileListConnection = new SQLFileListConnection(_conf);
 		_indexer = new PictureIndexer(_conf);
 		_lWaitBeforeWrite = _conf.getWaitBeforeWrite() * 1000;
 		_processor = new PictureProcessor(_conf.getMaxConcurrentIdentifyTask(),
 				_conf.getMaxConcurrentOtherTask());
 
 		// tag repository
 		_tagRepository = new SQLFileTagRepository(_conf, _fileListConnection);
 
 		_imageLoader = new ImageLoader(_conf);
 		// picture cache
 		final CacheLoader<String, FsPicture> pictureLoader = new FsPictureCacheLoader(
 				_fileListConnection, _imageLoader, _tagRepository, this);
 		final RemovalListener<String, FsPicture> pictureListener = new FsPictureRemovalListener(
 				this);
 		_pictureCache = CacheBuilder.newBuilder()
 				.removalListener(pictureListener).build(pictureLoader);
 
 		if (!checkAndCreateDirectories())
 		{
 			throw new IOException(
 					"Error creating the required picture directories : "
 							+ _conf.getMainPictureLoaderConfiguration()
 									.getPictureDirectory());
 		}
 		_fileListConnection.createTables();
 
 		loadPictureIdList();
 		_updater = new PictureUpdater();
 		final Thread t = new Thread(_updater, "picture updater");
 		t.start();
 	}
 
 	@Override
 	public int getPictureCount()
 	{
 		return _pictureIdList.size();
 	}
 
 	@Override
 	public void addPicture(final File pictureFile) throws PictureAddException
 	{
 		if (!pictureFile.canRead())
 		{
 			throw new PictureAddException(PictureAddExceptionType.CAN_T_READ);
 		}
 		if (!pictureFile.isFile())
 		{
 			throw new PictureAddException(PictureAddExceptionType.NOT_A_FILE);
 		}
 		final long lAddedTimestamp = System.currentTimeMillis();
 		// calc id
 		MessageDigest mdSha256;
 		try
 		{
 			mdSha256 = MessageDigest.getInstance("SHA-256");
 		}
 		catch (final NoSuchAlgorithmException e)
 		{
 			throw new PictureAddException(
 					PictureAddExceptionType.NO_SUCH_ALGORITHM, e);
 		}
 		try
 		{
 			FileInputStream stream = null;
 			try
 			{
 				stream = new FileInputStream(pictureFile);
 				final byte[] dataBytes = new byte[4096];
 
 				int byteRead = 0;
 				while ((byteRead = stream.read(dataBytes)) != -1)
 				{
 					mdSha256.update(dataBytes, 0, byteRead);
 				}
 			}
 			catch (final FileNotFoundException e)
 			{
 				throw new PictureAddException(
 						PictureAddExceptionType.FILE_NOT_FOUND, e);
 			}
 			finally
 			{
 				if (stream != null)
 				{
 					stream.close();
 				}
 			}
 		}
 		catch (final IOException e)
 		{
 			throw new PictureAddException(PictureAddExceptionType.IO_ERROR, e);
 		}
 
 		final byte[] mdbytes = mdSha256.digest();
 		final StringBuffer sb = new StringBuffer();
 		for (final byte mdbyte : mdbytes)
 		{
 			sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16)
 					.substring(1).toUpperCase());
 		}
 
 		final String strPictureId = sb.toString();
 
 		// check if already present
 		if (_pictureIdList.contains(strPictureId))
 		{
 			throw new PictureAddException(strPictureId,
 					PictureAddExceptionType.FILE_ALREADY_EXISTS);
 		}
 
 		PictureInformation info;
 		try
 		{
 			info = _processor.identifyPicture(pictureFile);
 		}
 		catch (InterruptedException | ExecutionException e)
 		{
 			throw new PictureAddException(strPictureId,
 					PictureAddExceptionType.IDENTIFY_EXECUTION_ERROR, e);
 		}
 		// copy file
 		final Path destPath = FileSystems.getDefault()
 				.getPath(
 						_conf.getMainPictureLoaderConfiguration()
 								.getPictureDirectory(),
 						strPictureId.substring(0, 2), strPictureId);
 		try
 		{
 			Files.copy(pictureFile.toPath(), destPath);
 		}
 		catch (final FileAlreadyExistsException e)
 		{
 			throw new PictureAddException(strPictureId,
 					PictureAddExceptionType.FILE_ALREADY_EXISTS, e);
 		}
 		catch (final IOException e)
 		{
 			throw new PictureAddException(strPictureId,
 					PictureAddExceptionType.COPY_ERROR, e);
 		}
 		final FsPicture picture = new FsPicture(_imageLoader, this,
 				strPictureId, lAddedTimestamp, lAddedTimestamp, info);
 		try
 		{
 			_fileListConnection.insertPicture(picture);
 			_indexer.indexPicture(picture);
 		}
 		catch (final SQLException e)
 		{
 			throw new PictureAddException(strPictureId,
 					PictureAddExceptionType.SQL_INSERT_ERROR, e);
 		}
 		catch (final CorruptIndexException e)
 		{
 			throw new PictureAddException(strPictureId,
 					PictureAddExceptionType.CORRUPT_INDEX_ERROR, e);
 		}
 		catch (final IOException e)
 		{
 			throw new PictureAddException(strPictureId,
 					PictureAddExceptionType.INDEX_ERROR, e);
 		}
 		_pictureIdList.add(strPictureId);
 	}
 
 	@Override
 	public int getId()
 	{
 		return _conf.getDataSourceId();
 	}
 
 	/**
 	 * Check the existence of all the required directories, and creates them if
 	 * they don't exists. Also check if they are readable and writable.
 	 * 
 	 * @return true if every required directory exists.
 	 */
 	private boolean checkAndCreateDirectories()
 	{
 		boolean bRes = true;
 		bRes &= checkDirectory(new File(_conf.getIndexDirectory()));
 		final File fPictureBaseDirectory = new File(_conf
 				.getMainPictureLoaderConfiguration().getPictureDirectory());
 		bRes &= checkDirectory(fPictureBaseDirectory);
 		if (bRes)
 		{
 			for (int i = 0; i < 256; i++)
 			{
 				String strFileName = Integer.toHexString(i).toUpperCase();
 				if (strFileName.length() == 1)
 				{
 					strFileName = '0' + strFileName;
 				}
 				bRes &= checkDirectory(new File(fPictureBaseDirectory,
 						strFileName));
 			}
 		}
 		final File fThumbnailBaseDirectory = new File(_conf
 				.getThumbnailPictureLoaderConfiguration().getPictureDirectory());
 		bRes &= checkDirectory(fThumbnailBaseDirectory);
 		if (bRes)
 		{
 			for (int i = 0; i < 256; i++)
 			{
 				String strFileName = Integer.toHexString(i).toUpperCase();
 				if (strFileName.length() == 1)
 				{
 					strFileName = '0' + strFileName;
 				}
 				bRes &= checkDirectory(new File(fThumbnailBaseDirectory,
 						strFileName));
 			}
 		}
 
 		return bRes;
 	}
 
 	/**
 	 * Check if the specified directory exists, tries to create it if it
 	 * doesn't. Also check if it's readable and writable.
 	 * 
 	 * @param fDirectory
 	 *            the directory to check.
 	 * @return if the directory exists, and is readable and writable.
 	 */
 	private static boolean checkDirectory(final File fDirectory)
 	{
 		boolean bRes = true;
 		if (!fDirectory.exists())
 		{
 			bRes = fDirectory.mkdir();
 		}
 		else
 		{
 			bRes &= fDirectory.canRead();
 			bRes &= fDirectory.canWrite();
 		}
 		return bRes;
 	}
 
 	@Override
 	public void close()
 	{
 		_updater.stop();
 		_pictureCache.invalidateAll();
 		_processor.shutdown();
 		try
 		{
 			_indexer.close();
 		}
 		catch (final IOException e)
 		{
 			LOGGER.error(e.getMessage(), e);
 		}
 		try
 		{
 			_fileListConnection.close();
 		}
 		catch (final SQLException e)
 		{
 			LOGGER.error(e.getMessage(), e);
 		}
 	}
 
 	@Override
 	public IPictureBrowser<FsPicture> getAllPictures()
 			throws ExecutionException
 	{
 		return new PictureIterator(null, _pictureIdList);
 	}
 
 	@Override
 	public IPictureBrowser<FsPicture> filterPictures(final Query query,
 			final int iLimit) throws IOException, ExecutionException
 	{
 		final List<String> list = _indexer.searchPicture(query, iLimit);
 		return new PictureIterator(query, list);
 	}
 
 	/**
 	 * Load picture id list from the database.
 	 * 
 	 * @throws SQLException
 	 *             if an SQL error occurred during the interrogation of the
 	 *             database.
 	 */
 	private void loadPictureIdList() throws SQLException
 	{
 		final ResultSet resLoad = _fileListConnection.loadPictureList();
 		while (resLoad.next())
 		{
 			final String strId = resLoad
 					.getString(SQLFileListConnection.PICTURE_ID_COLUMN_NAME);
 			_pictureIdList.add(strId);
 		}
 	}
 
 	/**
 	 * Update the picture in the database and the index. A configured amount of
 	 * time between picture update sand writing can be configured.
 	 * 
 	 * @param picture
 	 *            the picture to update.
 	 * @param bImmediat
 	 *            true if the picture has to be updated right now.
 	 */
 	public void updatePicture(final FsPicture picture, final boolean bImmediat)
 	{
 		synchronized (picture)
 		{
 			if (picture.hasBeenModified())
 			{
 				if (!bImmediat)
 				{
 					final long waitTime = picture.getModifiedTimestamp()
 							+ _lWaitBeforeWrite - System.currentTimeMillis();
 					if (waitTime > 0)
 					{
						if (LOGGER.isDebugEnabled())
						{
							LOGGER.debug("Waiting for : " + waitTime
									+ " before updating.");
						}
 						try
 						{
							picture.wait(waitTime);
 						}
 						catch (final InterruptedException e1)
 						{
 							// nothing to do
 						}
 					}
 				}
 				if (LOGGER.isDebugEnabled())
 				{
 					LOGGER.debug("Updating picture, id : " + picture.getId());
 				}
 				try
 				{
 					_fileListConnection.updatePicture(picture);
 					_indexer.indexPicture(picture);
 					picture.unsetModified();
 				}
 				catch (final SQLException | IOException e)
 				{
 					LOGGER.error(e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Add a {@link FsPicture} to the list of pictures needing an update.
 	 * 
 	 * @param picture
 	 *            the {@link FsPicture} needing an update.
 	 */
 	public void setPictureForUpdating(final FsPicture picture)
 	{
 		_updateQueue.add(picture);
 	}
 
 	/**
 	 * Re-index the specified picture.
 	 * 
 	 * @param strPicId
 	 *            id of the picture to re-index.
 	 * @throws CorruptIndexException
 	 *             if the index is corrupted.
 	 * @throws IOException
 	 *             if there is an error while writing the index.
 	 */
 	public void reIndexPicture(final String strPicId)
 			throws CorruptIndexException, IOException
 	{
 		try
 		{
 			final FsPicture picture = _pictureCache.get(strPicId);
 			synchronized (picture)
 			{
 				_indexer.indexPicture(picture);
 			}
 		}
 		catch (final ExecutionException e)
 		{
 			LOGGER.error(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Re-index all the pictures.
 	 * 
 	 * @throws CorruptIndexException
 	 *             if the index is corrupted.
 	 * @throws IOException
 	 *             if there is an error while writing the index.
 	 */
 	public void reIndexAllPictures() throws CorruptIndexException, IOException
 	{
 		for (final String strPicId : _pictureIdList)
 		{
 			reIndexPicture(strPicId);
 		}
 	}
 
 	/**
 	 * {@link IPictureBrowser} on the list of {@ink FsPicture}.
 	 * 
 	 * @author benobiwan
 	 * 
 	 */
 	private final class PictureIterator extends
 			AbstractIdBasedPictureBrowser<FsPicture>
 	{
 		/**
 		 * Creates a new {@link PictureIterator}.
 		 * 
 		 * @param query
 		 *            the {@link Query} used to create the
 		 *            {@link PictureIterator}.
 		 * @param idList
 		 *            the list of ids of the {@link PictureIterator}.
 		 * @throws ExecutionException
 		 *             if an Exception was thrown during the loading of the
 		 *             picture.
 		 */
 		public PictureIterator(final Query query, final List<String> idList)
 				throws ExecutionException
 		{
 			super(SQLFileDataSource.this, query, SQLFileDataSource.this._bus,
 					idList);
 		}
 
 		@Override
 		protected FsPicture getPicture(final String pictureId)
 				throws ExecutionException
 		{
 			return _pictureCache.get(pictureId);
 		}
 	}
 
 	/**
 	 * {@link Runnable} used to update the modified {@link FsPicture}.
 	 * 
 	 * @author benobiwan
 	 * 
 	 */
 	private final class PictureUpdater implements Runnable
 	{
 		/**
 		 * Boolean used to stop the {@link PictureUpdater}.
 		 */
 		private volatile boolean _bStop = false;
 
 		/**
 		 * Thread used to run the {@link PictureUpdater}.
 		 */
 		private volatile Thread _workingThread;
 
 		/**
 		 * Creates a new {@link PictureUpdater}.
 		 */
 		public PictureUpdater()
 		{
 			// nothing to do
 		}
 
 		/**
 		 * Function used to stop the {@link PictureUpdater}.
 		 */
 		public void stop()
 		{
 			_bStop = true;
 			if (_workingThread != null)
 			{
 				synchronized (_workingThread)
 				{
 					_workingThread.interrupt();
 				}
 			}
 		}
 
 		@Override
 		public void run()
 		{
 			_workingThread = Thread.currentThread();
 			while (!_bStop)
 			{
 				try
 				{
 					updatePicture(_updateQueue.take(), false);
 				}
 				catch (final InterruptedException e)
 				{
 					// nothing to do there
 				}
 			}
 		}
 	}
 
 	@Override
 	public Set<ITag> getTagSet()
 	{
 		return _tagRepository.getTagSet();
 	}
 
 	@Override
 	public ITag getRootTag()
 	{
 		return _tagRepository.getRootTag();
 	}
 
 	@Override
 	public ITag get(final int iTagId)
 	{
 		return _tagRepository.get(iTagId);
 	}
 
 	@Override
 	public ITag get(final Integer iTagId)
 	{
 		return _tagRepository.get(iTagId);
 	}
 
 	@Override
 	public ITag get(final String strTagName)
 	{
 		return _tagRepository.get(strTagName);
 	}
 
 	@Override
 	public boolean hasTagNamed(final String strName)
 	{
 		return _tagRepository.hasTagNamed(strName);
 	}
 
 	@Override
 	public void addTag(final ITag parent, final String strName,
 			final String strDescription, final boolean bSelectable)
 			throws TagAddException
 	{
 		_tagRepository.addTag(parent, strName, strDescription, bSelectable);
 	}
 
 	@Override
 	public void addTag(final String strName, final String strDescription,
 			final boolean bSelectable) throws TagAddException
 	{
 		_tagRepository.addTag(strName, strDescription, bSelectable);
 	}
 
 	@Override
 	public void editTag(final int iTagId, final ITag parent,
 			final String strName, final String strDescription,
 			final boolean bSelectable) throws TagAddException
 	{
 		_tagRepository.editTag(iTagId, parent, strName, strDescription,
 				bSelectable);
 	}
 }
