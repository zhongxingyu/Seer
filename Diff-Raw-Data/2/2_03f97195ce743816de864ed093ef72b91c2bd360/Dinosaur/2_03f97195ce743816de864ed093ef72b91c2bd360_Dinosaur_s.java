 package dinosaur;
 
 import dinosaur.info.*;
 
 /*
  * немного соглашений:
  * инфа о торрентах и файлах есть статическая(не изменяемая, напр. имя, размер и т.д) и динамическая(скрость, сколько загружено/отдано и т.д)
  * для экономии стоит брать стат. информацию один раз чтобы заполнить поля, динамическую не меньше раза в 1 сек. для обновления соответсвующий полей
  * идентификатором торрента выступает хэш в hex-е, именно его надо передавать когда хочешь выполинть какую либо операцию с торрентом, можно взять(и запомнить у себя) в 
  * стат. информации(get_torrent_info_stat)
  * идендификатором файла выступает индекс от 0 до n - 1, где n - их кол-во, можно взять(и запомнить) в стат. информации торрента
  */
 
 /*
  * использование:
  * Dinosaur.GetInstance().OpenMetafile("ola.torrent");
  */
 
 public class Dinosaur {
 	private static final Dinosaur 	instance = new Dinosaur();
 	private torrent_failure[]		fail_torrents_at_start;
 	private boolean					initialized;
 	private Dinosaur()
 	{
 		initialized = false;
 		try
 		{
 			System.loadLibrary("dinosaur");
 			Runtime.getRuntime().addShutdownHook(new Hook());
 			fail_torrents_at_start = InitLibrary();
 			initialized = true;
 		}
 		catch(DinosaurSyscallException e)
 		{
 			
 		}
 		catch(UnsatisfiedLinkError e)
 		{
 			
 		}
 		catch(SecurityException e)
 		{
 			
 		}
 		catch (IllegalArgumentException e) {
 			
 		}
 		catch (IllegalStateException e) {
 			
 		}
 	}
 	/*
 	 * вернет истину если удалось загрузить либу, иначе - ложь
 	 */
	boolean Initialized()
 	{
 		return initialized;
 	}
 	public static Dinosaur GetInstance()
 	{
 		return instance;
 	}
 	/*
 	 * так можно узнать о торрентах, которые не удалось восстановить при загрузке либы
 	 */
 	public torrent_failure[] GetFailTorrents()
 	{
 		return fail_torrents_at_start;
 	}
 	
 	
 	
 	/*
 	 * Инициализации либы, первый вызов, который должен быть - этот
 	 * если не удалось возобновить работу торрентов, вернет описание ошибки(причину) в списке
 	 * для каждого из них
 	 * Искючения:
 	 * DinosaurException::ERR_CODE_CAN_NOT_CREATE_THREAD
 	 * DinosaurSyscallException
 	 */
 	private native torrent_failure[] InitLibrary() throws DinosaurSyscallException;
 	
 	
 	
 	/*
 	 * Последний вызов - этот, осбождает ресурсы, может занять время
 	 */
 	native void ReleaseLibrary();
 	
 	
 	
 	/*
 	 * чтобы добавить новый торрент - этот вызов для тебя, просит путь к торрент-файлу,
 	 * возвращает объект Metafile, содержащий информацию из этого файла,
 	 * которую необходимо отобразить юзеру, перед тем как он решит добавить торрент или нет
 	 * Исключения:
 	 * DinosaurSyscallException
 	 * DinosaurException::ERR_CODE_NULL_REF          - пробросит если либа не инициализирована
 	 * DinosaurException::ERR_CODE_INVALID_METAFILE  - корявый файл
 	 */
 	private native Metafile native_OpenMetafile(String metafile_path) throws DinosaurException, DinosaurSyscallException;
 	public Metafile OpenMetafile(String metafile_path) throws DinosaurException, DinosaurSyscallException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_OpenMetafile(metafile_path);
 	}
 	
 	
 	
 	/*
 	 * если решит не добавлять - вызови, освободит маленько памяти(необязательно)
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF          - пробросит если либа не инициализирована
 	 */
 	private native void native_CloseMetafile() throws DinosaurException;
 	public void CloseMetafile() throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_CloseMetafile();
 	}
 	
 	
 	
 	/*
 	 * если решит добавить, этот вызов добавит торрент, требует путь куда сохранить раздачу
 	 * возвращает infohash в hex-e
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_UNDEF					- случилась какая та херь
 	 * Exception::ERR_CODE_TORRENT_EXISTS			- такой торрент уже есть 
 	 */
 	private native String native_AddTorrent(String save_directory) throws DinosaurException;
 	public String AddTorrent(String save_directory) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_AddTorrent(save_directory);
 	}
 	
 	
 	
 	/*
 	 * Приостанавливает торрент с заданным хэшем
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 * Exception::ERR_CODE_INVALID_OPERATION		- невозможно остановить торрент, может он уже остановлен?
 	 */
 	private native void native_PauseTorrent(String hash) throws DinosaurException;
 	public void PauseTorrent(String hash) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_PauseTorrent(hash);
 	}
 	
 	
 	
 	/*
 	 * Возобновляет торрент с заданным хэшем
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 * Exception::ERR_CODE_INVALID_OPERATION		- невозможно возобновить торрент, может он уже остановлен?
 	 */
 	private native void native_ContinueTorrent(String hash) throws DinosaurException;
 	public void ContinueTorrent(String hash) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_ContinueTorrent(hash);
 	}
 	
 	
 	
 	/*
 	 * Иницирует проверку целостности
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 * Exception::ERR_CODE_INVALID_OPERATION		- нельзя
 	 */
 	private native void native_CheckTorrent(String hash) throws DinosaurException;
 	public void CheckTorrent(String hash) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_CheckTorrent(hash);
 	}
 	
 	
 	
 	/*
 	 * Удаляет торрент, with_data указывает удалять вместе с данными или нет
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 */
 	private native void native_DeleteTorrent(String hash, boolean with_data) throws DinosaurException;
 	public void DeleteTorrent(String hash, boolean with_data) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_DeleteTorrent(hash, with_data);
 	}
 	
 	
 	/*
 	 * Возвращает статическую информацию о торренте с заданным хэшем
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 */
 	private native torrent_stat native_get_torrent_info_stat(String hash) throws DinosaurException;
 	public torrent_stat get_torrent_info_stat(String hash) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_torrent_info_stat(hash);
 	}
 	
 	
 	
 	/*
 	 * Возвращает динамическую информацию о торренте с заданным хэшем
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 */
 	private native torrent_dyn native_get_torrent_info_dyn(String hash) throws DinosaurException;
 	public torrent_dyn get_torrent_info_dyn(String hash) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_torrent_info_dyn(hash);
 	}
 	
 	
 	/*
 	 * Возвращает информацию о трекерах торрента с заданным хэшем
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 */
 	private native tracker[] native_get_torrent_info_trackers(String hash) throws DinosaurException;
 	public tracker[] get_torrent_info_trackers(String hash) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_torrent_info_trackers(hash);
 	}
 	
 	
 	
 	/*
 	 * Возвращает статическую информацию о файлах торрента с заданным хэшем
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 * Exception::ERR_CODE_INVALID_FILE_INDEX		- неверный индекс. >= кол-ву файлов?
 	 */
 	private native file_stat native_get_torrent_info_file_stat(String hash, long index) throws DinosaurException;
 	public file_stat get_torrent_info_file_stat(String hash, long index) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_torrent_info_file_stat(hash, index);
 	}
 	
 	
 	
 	/*
 	 * Возвращает динамическую информацию о файлах торрента с заданным хэшем
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 * Exception::ERR_CODE_INVALID_FILE_INDEX		- неверный индекс. >= кол-ву файлов?
 	 */
 	private native file_dyn native_get_torrent_info_file_dyn(String hash, long index) throws DinosaurException;
 	public file_dyn get_torrent_info_file_dyn(String hash, long index) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_torrent_info_file_dyn(hash, index);
 	}
 	
 	
 	
 	/*
 	 * Возвращает информацию о сидах торрента с заданным хэшем
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 */
 	private native peer[] native_get_torrent_info_seeders(String hash) throws DinosaurException;
 	public peer[] get_torrent_info_seeders(String hash) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_torrent_info_seeders(hash);
 	}
 	
 	
 	
 	/*
 	 * Возвращает информацию о личерах торрента с заданным хэшем
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 */
 	private native peer[] native_get_torrent_info_leechers(String hash) throws DinosaurException;
 	public peer[] get_torrent_info_leechers(String hash) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_torrent_info_leechers(hash);
 	}
 	
 	
 	
 	/*
 	 * Возвращает информацию о загружаемых кусках торрента с заданным хэшем
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 */
 	private native downloadable_piece[] native_get_torrent_info_downloadable_pieces(String hash) throws DinosaurException;
 	public downloadable_piece[] get_torrent_info_downloadable_pieces(String hash) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_torrent_info_downloadable_pieces(hash);
 	}
 	
 	
 	
 	/*
 	 * Возвращает описание ошибки, которая приключилась с торрентом
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 */
 	private native torrent_failure native_get_torrent_failure_desc(String hash) throws DinosaurException;
 	public torrent_failure get_torrent_failure_desc(String hash) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_torrent_failure_desc(hash);
 	}
 	
 	
 	
 	/*
 	 * Выставляет приоритет загрузки файла
 	 * prio == 0 - низкий приоритет
 	 * prio == 1 - нормальный
 	 * prio == 2 - высокий
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 * Exception::ERR_CODE_INVALID_FILE_INDEX		- неверный индекс. >= кол-ву файлов?
 	 * Exception::ERR_CODE_FAIL_SET_FILE_PRIORITY	- не получилось
 	 * Exception::ERR_CODE_INVALID_OPERATION		- нельзя ставить приоритеты во время проверки
 	 */
 	private native void native_set_file_priority(String hash, long index, int prio)  throws DinosaurException;
 	public void set_file_priority(String hash, long index, int prio)  throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_file_priority(hash, index, prio);
 	}
 	
 	
 	
 	/*
 	 * Выставляет приоритет загрузки файла
 	 * Исключения:
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_TORRENT_NOT_EXISTS 		- торрента с таким хэшем нет
 	 * Exception::ERR_CODE_INVALID_FILE_INDEX		- неверный индекс. >= кол-ву файлов?
 	 * Exception::ERR_CODE_FAIL_SET_FILE_PRIORITY	- не получилось
 	 * Exception::ERR_CODE_INVALID_OPERATION		- нельзя ставить приоритеты во время проверки
 	 */
 	public void set_file_priority(String hash, long index, DOWNLOAD_PRIORITY prio)  throws DinosaurException, DinosaurNotInitialized
 	{
 		set_file_priority(hash, index, prio.ordinal());
 	}
 	
 	
 	
 	/*
 	 * Возвращает список хэшей всех торрентов
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 */
 	private native String[] native_get_TorrentList() throws DinosaurException;
 	public String[] get_TorrentList() throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_TorrentList();
 	}
 	
 	
 	
 	/*
 	 * Возвращает список хэшей всех активных торрентов
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 */
 	private native String[] native_get_active_torrents() throws DinosaurException;
 	public String[] get_active_torrents() throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_active_torrents();
 	}
 	
 	
 	
 	/*
 	 * Возвращает список хэшей всех не активных торрентов, они в очереди
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 */
 	private native String[] native_get_torrents_in_queue() throws DinosaurException;
 	public String[] get_torrents_in_queue() throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_torrents_in_queue();
 	}
 	
 	
 	
 	/*
 	 * Возвращает состояние слушающего сокета
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 */
 	private native socket_status native_get_socket_status() throws DinosaurException;
 	public socket_status get_socket_status() throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_socket_status();
 	}
 	
 	
 	
 	/*
 	 * После того как поменял конфиги, стоит вызвать, сохранит конфиги и применит их
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_CAN_NOT_SAVE_CONFIG		- не получилось сохранить конфиги
 	 */
 	private native void native_UpdateConfigs()  throws DinosaurException;
 	public void UpdateConfigs()  throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_UpdateConfigs();
 	}
 	
 	
 	
 	/*
 	 * Возвращает все конфиги в виде класса
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 */
 	private native Configs native_get_configs() throws DinosaurException;
 	public Configs get_configs() throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		return native_get_configs();
 	}
 	
 	
 	
 	/*
 	 * методы ниже выставляют конфиги
 	 */
 	/*
 	 * куда сохранять торренты по дефолту
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_DIR_NOT_EXISTS			- такой папки нет
 	 * SyscallException
 	 */
 	private native void native_set_config_download_directory(String dir) throws DinosaurException, DinosaurSyscallException;
 	public void set_config_download_directory(String dir) throws DinosaurException, DinosaurSyscallException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_download_directory(dir); 
 	}
 	
 	
 	
 	
 	/*
 	 * какой порт слушать
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_INVALID_PORT			- такой папки нет
 	 */
 	private native void native_set_config_port(long v) throws DinosaurException;
 	public void set_config_port(long v) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_port(v); 
 	}
 	
 	
 	
 	
 	/*
 	 * размер кэша записи 
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_INVALID_W_CACHE_SIZE
 	 */
 	private native void native_set_config_write_cache_size(long v) throws DinosaurException;
 	public void set_config_write_cache_size(long v) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_write_cache_size(v); 
 	}
 	
 	
 	
 	
 	/*
 	 * размер кэша чтения
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_INVALID_R_CACHE_SIZE	
 	 */
 	private native void native_set_config_read_cache_size(long v) throws DinosaurException;
 	public void set_config_read_cache_size(long v) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_read_cache_size(v); 
 	}
 	
 	
 	
 	
 	/*
 	 * сколько макс. пиров просить у трекеров
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_INVALID_TRACKER_NUM_WANT		
 	 */
 	private native void native_set_config_tracker_numwant(long v) throws DinosaurException;
 	public void set_config_tracker_numwant(long v) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_tracker_numwant(v); 
 	}
 	
 	
 	
 	
 	/*
 	 * интервал обновления трекеров по дефолту
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_INVALID_TRACKER_DEF_INTERVAL		
 	 */
 	private native void native_set_config_tracker_default_interval(long v) throws DinosaurException;
 	public void set_config_tracker_default_interval(long v) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_tracker_default_interval(v); 
 	}
 	
 	
 	
 	
 	/*
 	 * макс. число сидов в каждом торренте
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_INVALID_MAX_ACTIVE_SEEDS			
 	 */
 	private native void native_set_config_max_active_seeders(long v) throws DinosaurException;
 	public void set_config_max_active_seeders(long v) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_max_active_seeders(v); 
 	}
 	
 	
 	
 	
 	/*
 	 * макс. число личеров в каждом торренте
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_INVALID_MAX_ACTIVE_LEECHS			
 	 */
 	private native void native_set_config_max_active_leechers(long v) throws DinosaurException;
 	public void set_config_max_active_leechers(long v) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_max_active_leechers(v); 
 	}
 	
 	
 	
 	
 	/*
 	 * отправлять  ли have-сообщения пирам
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 */
 	private native void native_set_config_send_have(boolean v) throws DinosaurException;
 	public void set_config_send_have(boolean v) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_send_have(v); 
 	}
 	
 	
 	
 	
 	/*
 	 * какой интерфес слушать
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_INVALID_LISTEN_ON			
 	 */
 	private native void native_set_config_listen_on(String ip) throws DinosaurException;
 	public void set_config_listen_on(String ip) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_listen_on(ip); 
 	}
 	
 	
 	
 	
 	/*
 	 * какой порт слушать
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_INVALID_MAX_ACTIVE_TORRENTS			
 	 */
 	private native void native_set_config_max_active_torrents(long v) throws DinosaurException;
 	public void set_config_max_active_torrents(long v) throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_max_active_torrents(v); 
 	}
 	
 	
 	
 	
 	/*
 	 * после какого ratio считать торрент завершенным
 	 * DinosaurException::ERR_CODE_NULL_REF 		- пробросит если либа не инициализирована
 	 * Exception::ERR_CODE_INVALID_FIN_RATIO			
 	 */
 	private native void native_set_config_fin_ratio(float v)  throws DinosaurException;
 	public void set_config_fin_ratio(float v)  throws DinosaurException, DinosaurNotInitialized
 	{
 		if (!initialized)
 			throw new DinosaurNotInitialized();
 		native_set_config_fin_ratio(v); 
 	}
 }
 
 class Hook extends Thread
 {
 	public void run()
 	{
 		Dinosaur.GetInstance().ReleaseLibrary();
 	}
 }
