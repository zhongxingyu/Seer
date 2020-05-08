 package cmg.org.monitor.memcache;
 
 import java.util.ArrayList;
 
 import cmg.org.monitor.dao.SystemMonitorDAO;
 import cmg.org.monitor.dao.impl.SystemMonitorDaoJDOImpl;
 import cmg.org.monitor.entity.shared.SystemMonitor;
 import cmg.org.monitor.memcache.shared.AlertMonitorDto;
 import cmg.org.monitor.memcache.shared.CpuDTO;
 import cmg.org.monitor.memcache.shared.FileSystemCacheDto;
 import cmg.org.monitor.memcache.shared.JvmDto;
 import cmg.org.monitor.memcache.shared.MemoryDto;
 import cmg.org.monitor.memcache.shared.ServiceMonitorDto;
 import cmg.org.monitor.memcache.shared.SystemMonitorDto;
 import cmg.org.monitor.util.shared.MonitorConstant;
 import cmg.org.monitor.util.shared.Ultility;
 
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 
 public class MonitorMemcache {
 	private static MemcacheService syncCache = MemcacheServiceFactory
 			.getMemcacheService("monitor_system_store");
 
 	// START List of methods access alert monitor store
 	public static void putAlertMonitor(String sid, AlertMonitorDto alert) {
 		ArrayList<AlertMonitorDto> alertList = listAlerts(sid, false);
 		boolean check = false;
 		if (alertList != null) {
 			// check if exists alert type
 			for (AlertMonitorDto alertOn : alertList) {
 				if (alert.getType() == alertOn.getType()) {
 					check = true;
 					break;
 				}
 			}
 		} else {
 			alertList = new ArrayList<AlertMonitorDto>();
 		}
 		if (!check) {
 			alertList.add(alert);
 			put(Key.create(Key.ALERT_STORE, sid), alertList);
 		}
 	}
 
 	/**
 	 * @param sid
 	 *            The ID of system monitor
 	 * @return List of alert monitor store in memcache
 	 */
 	public static ArrayList<AlertMonitorDto> listAlerts(String sid,
 			boolean allowDelete) {
 		ArrayList<AlertMonitorDto> list = null;
 		Object obj = get(Key.create(Key.ALERT_STORE, sid));
 		if (obj != null) {
 			if (obj instanceof ArrayList<?>) {
 				list = (ArrayList<AlertMonitorDto>) obj;
 			}
 		}
 		if (allowDelete) {
 			delete(Key.create(Key.ALERT_STORE, sid));
 		}
 		return list;
 	}
 
 	// END List of methods access alert monitor store
 
 	// START List of methods access data via Server
 	/**
 	 * @return Store of System Monitor return null if no system found in
 	 *         memcache and JDO
 	 */
 	public static ArrayList<SystemMonitorStore> getSystemMonitorStore() {
 		ArrayList<SystemMonitorStore> smsList = null;
 		SystemMonitorStore sms = null;
 
 		ArrayList<SystemMonitorDto> sysList = listSystemMonitor();
 		if (sysList != null) {
 			smsList = new ArrayList<SystemMonitorStore>();
 			for (SystemMonitorDto sys : sysList) {
 				sms = SystemMonitorStore.create();
 				sms.setSysMonitor(sys);
 				smsList.add(sms);
 			}
 		}
 		return smsList;
 	}
 
 	/**
 	 * @param sms
 	 *            Store all information of monitor system put the store into
 	 *            memcache
 	 */
 	public static void putSystemMonitorStore(
 			ArrayList<SystemMonitorStore> smsList) {
 		if (smsList != null) {
 			// check system monitor status
 			try {
 				ArrayList<SystemMonitorStore> temp = getSystemMonitorStore();
 				if (temp != null) {
 					if (smsList.size() == temp.size()) {
 						SystemMonitorDAO sysDao = new SystemMonitorDaoJDOImpl();
 						SystemMonitor sysJdo = null;
 						SystemMonitorDto sysIn = null;
 						SystemMonitorDto sysOn = null;
 						for (int i = 0; i < smsList.size(); i++) {
 							sysIn = smsList.get(i).getSysMonitor();
 							sysOn = temp.get(i).getSysMonitor();
 							// check Id
 							if (sysIn.getId().equals(sysOn.getId())) {
 								// if status changed. update JDO
 								if (sysIn.isStatus() != sysOn.isStatus()) {
 									sysJdo = sysDao
 											.getSystembyID(sysIn.getId());
 									sysJdo.setStatus(sysIn.isStatus());
 									sysDao.updateSystem(sysJdo);
 									changeFlag(true);
 								}
 							}
 						}
 					}
 				}
 			} catch (Exception ex) {
 				// do nothing
 			}
 
 			int count = getCount() + 1;
 			// Store count
 			setCount(count);
 			ArrayList<SystemMonitorDto> sysList = new ArrayList<SystemMonitorDto>();
 			SystemMonitorDto sys = null;
 			for (SystemMonitorStore sms : smsList) {
 				sys = sms.getSysMonitor();
 				sysList.add(sys);
 				String sid = sms.getSysMonitor().getId();
 				// Store alert monitor information
 				ArrayList<AlertMonitorDto> alertList = sms.getAlert();
 				if (alertList != null) {
 					for (AlertMonitorDto alert : alertList) {
 						putAlertMonitor(sid, alert);
 					}
 				}
 				// Store CPU & memory information
 				put(Key.create(Key.CPU_MEMORY_STORE, count, sid),
 						sms.getMemory());
 				put(Key.create(Key.CPU_STORE, count, sid), sms.getCpu());
 				// Store File system list information
 				put(Key.create(Key.FILE_SYSTEM_STORE, count, sid),
 						sms.getFileSysList());
 				// Store JVM information
 				put(Key.create(Key.JVM_STORE, count, sid), sms.getJvm());
 				// Store service list information
 				put(Key.create(Key.SERVICE_STORE, count, sid),
 						sms.getServiceMonitorList());
 			}
 			// Store system monitor list
 			put(Key.create(Key.SYSTEM_MONITOR_STORE, count), sysList);
 		}
 	}
 
 	// END List of methods access data via Server
 
 	// START List of methods access data via RPC
 	/**
 	 * @param sid
 	 *            The ID of system monitor
 	 * @return
 	 */
 	public static SystemMonitorDto getLastestDataMonitor(String sid) {
 		SystemMonitorDto sys = getSystemById(sid);
 		if (sys != null) {
 			sys.setHealthStatus(getCurrentHealthStatus(sid));
 			sys.setCpuHistory(listCpu(sid,
 					MonitorConstant.HISTORY_CPU_MEMORY_LENGTH));
 			sys.setMemHistory(listMemory(sid,
 					MonitorConstant.HISTORY_CPU_MEMORY_LENGTH));
 			sys.setLastestJvm(getJvm(sid));
 		}
 		return sys;
 	}
 
 	public static SystemMonitorDto getSystemMonitorById(String sid) {
 		SystemMonitorDto sys = getSystemById(sid);
 		try {
 			sys.setGroups(Ultility.listGroup());
 		} catch (Exception ex) {
 			// do nothing
 		}
 		return sys;
 	}
 
 	/**
 	 * @param sid
 	 *            The ID of system monitor
 	 * @return true if system id is valid
 	 */
 	public static boolean checkSystemId(String sid) {
 		return (getSystemById(sid) != null);
 	}
 
 	/**
 	 * @param url
 	 *            of remote system.
 	 * @return true if this url is exists
 	 */
 	public static boolean checkRemoteUrl(String url) {
 		boolean check = false;
 		ArrayList<SystemMonitorDto> list = listSystemMonitor();
		if (list != null) {
			if (list.size() > 0) {
				for (SystemMonitorDto sys : list) {
					if (sys.getRemoteUrl().toLowerCase().trim()
							.equals(url.toLowerCase().trim())) {
						check = true;
						break;
					}
				}
 			}
 		}
 		return check;
 	}
 
 	/**
 	 * @param system
 	 *            The system monitor
 	 * @return true if system added successfully. Create new system monitor.
 	 *         Insert into JDO and change flag
 	 */
 	public static boolean createSystem(SystemMonitorDto system) {
 		boolean b = false;
 		SystemMonitorDAO sysDao = new SystemMonitorDaoJDOImpl();
 		SystemMonitor sysJdo = null;
 		try {
 			sysJdo = new SystemMonitor();
 			sysJdo.setCode(sysDao.createCode());
 			sysJdo.setName(system.getName());
 			sysJdo.setUrl(system.getUrl());
 			sysJdo.setActive(system.isActive());
 			sysJdo.setProtocol(system.getProtocol());
 			sysJdo.setGroupEmail(system.getGroupEmail());
 			sysJdo.setIp(system.getIp());
 			sysJdo.setRemoteUrl(system.getRemoteUrl());
 			b = sysDao.addnewSystem(sysJdo);
 			changeFlag(b);
 		} catch (Exception ex) {
 			// do nothing
 		}
 		return b;
 	}
 
 	/**
 	 * @param system
 	 *            The system monitor
 	 * @return true if system updated successfully. Update system monitor
 	 *         information.
 	 */
 	public static boolean updateSystem(SystemMonitorDto system) {
 		boolean b = false;
 		SystemMonitorDAO sysDao = new SystemMonitorDaoJDOImpl();
 		SystemMonitor sysJdo = null;
 		try {
 			sysJdo = sysDao.getSystembyID(system.getId());
 			sysJdo.setName(system.getName());
 			sysJdo.setUrl(system.getUrl());
 			sysJdo.setActive(system.isActive());
 			sysJdo.setProtocol(system.getProtocol());
 			sysJdo.setGroupEmail(system.getGroupEmail());
 			sysJdo.setIp(system.getIp());
 			sysJdo.setRemoteUrl(system.getRemoteUrl());
 			sysDao.updateSystem(sysJdo);
 			b = true;
 			changeFlag(b);
 		} catch (Exception ex) {
 			// do nothing
 		}
 		return b;
 	}
 
 	/**
 	 * @return List of all system monitor. Throw to UI via RPC
 	 */
 	public static ArrayList<SystemMonitorDto> listSystemMonitorToUi() {
 		ArrayList<SystemMonitorDto> list = listSystemMonitor();
 		SystemMonitorDto sys = null;
 		for (int i = 0; i < list.size(); i++) {
 			sys = list.get(i);
 			String sid = sys.getId();
 			ArrayList<MemoryDto> listMem = getMemories(sid);
 			sys.setHealthStatus(getCurrentHealthStatus(sid));
 			sys.setLastestCpu(getCpu(sid));
 			if (listMem != null) {
 				if (listMem.size() > 0) {
 					sys.setLastestMemory(listMem.get(0));
 				}
 			}
 			list.set(i, sys);
 		}
 		return list;
 	}
 
 	// END List of methods access data via RPC
 
 	// START List of methods access data from memcache
 
 	/**
 	 * @param sid
 	 *            The ID of system monitor
 	 * @return List of services
 	 */
 	protected static ArrayList<ServiceMonitorDto> listService(String sid) {
 		ArrayList<ServiceMonitorDto> list = null;
 		Object obj = get(Key.create(Key.SERVICE_STORE, getCount(), sid));
 		if (obj != null) {
 			if (obj instanceof ArrayList<?>) {
 				list = (ArrayList<ServiceMonitorDto>) obj;
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * @param numberOfResult
 	 *            number of result which want to get
 	 * @return List of Cpu information. return null if no Memory info found
 	 */
 	protected static ArrayList<ArrayList<MemoryDto>> listMemory(String sid,
 			int numberOfResult) {
 		ArrayList<ArrayList<MemoryDto>> list = null;
 		int count = getCount();
 		Object obj = get(Key.create(Key.CPU_MEMORY_STORE, count, sid));
 		ArrayList<MemoryDto> mem = getMemories(sid);
 		if (mem != null) {
 			list = new ArrayList<ArrayList<MemoryDto>>();
 			list.add(mem);
 			for (int i = 1; i < numberOfResult - 1; i++) {
 				mem = getMemories(sid, count - i);
 				if (mem != null) {
 					list.add(mem);
 				} else {
 					break;
 				}
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * @return Memory Information. return null if no Memory information found
 	 */
 	protected static ArrayList<MemoryDto> getMemories(String sid) {
 		return getMemories(sid, getCount());
 	}
 
 	/**
 	 * @param count
 	 *            The count number of key store.
 	 * @return Memory Information. return null if no Memory information found
 	 */
 	protected static ArrayList<MemoryDto> getMemories(String sid, int count) {
 		ArrayList<MemoryDto> list = null;
 		Object obj = get(Key.create(Key.CPU_MEMORY_STORE, count, sid));
 		if (obj != null) {
 			if (obj instanceof ArrayList<?>) {
 				list = (ArrayList<MemoryDto>) obj;
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * @param numberOfResult
 	 *            number of result which want to get
 	 * @return List of Cpu information. return null if no Cpu info found
 	 */
 	protected static ArrayList<CpuDTO> listCpu(String sid, int numberOfResult) {
 		ArrayList<CpuDTO> list = null;
 		int count = getCount();
 		Object obj = get(Key.create(Key.CPU_STORE, count, sid));
 		CpuDTO cpu = getCpu(sid);
 		if (cpu != null) {
 			list = new ArrayList<CpuDTO>();
 			list.add(cpu);
 			for (int i = 1; i < numberOfResult - 1; i++) {
 				cpu = getCpu(sid, count - i);
 				if (cpu != null) {
 					list.add(cpu);
 				} else {
 					break;
 				}
 
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * @return CPU Information. return null if no CPU information found
 	 */
 	protected static CpuDTO getCpu(String sid) {
 		return getCpu(sid, getCount());
 	}
 
 	/**
 	 * @param count
 	 *            The count number of key store.
 	 * @return CPU Information. return null if no CPU information found
 	 */
 	protected static CpuDTO getCpu(String sid, int count) {
 		CpuDTO cpu = null;
 		Object obj = get(Key.create(Key.CPU_STORE, count, sid));
 		if (obj != null) {
 			if (obj instanceof CpuDTO) {
 				cpu = (CpuDTO) obj;
 			}
 		}
 		return cpu;
 	}
 
 	/**
 	 * @param sid
 	 *            The ID of system monitor
 	 * @return All system monitor information. Return null if no system found
 	 *         Get SystemMonitor by this Id.
 	 */
 	protected static SystemMonitorDto getSystemById(String sid) {
 		SystemMonitorDto sys = null;
 		ArrayList<SystemMonitorDto> list = listSystemMonitor();
 		if (list != null) {
 			for (SystemMonitorDto sysDto : list) {
 				if (sysDto.getId().equals(sid)) {
 					sys = sysDto;
 					break;
 				}
 			}
 		}
 		return sys;
 	}
 
 	/**
 	 * @return list of file system. Store all informations of file system
 	 */
 	protected static ArrayList<FileSystemCacheDto> listFileSystem(String sid) {
 		ArrayList<FileSystemCacheDto> list = null;
 		Object obj = get(Key.create(Key.FILE_SYSTEM_STORE, getCount(), sid));
 		if (obj != null) {
 			if (obj instanceof ArrayList<?>) {
 				list = (ArrayList<FileSystemCacheDto>) obj;
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * @return Java virtual memory information. from memcahe
 	 */
 	protected static JvmDto getJvm(String sid) {
 		JvmDto jvm = null;
 		Object obj = get(Key.create(Key.JVM_STORE, getCount(), sid));
 		if (obj != null) {
 			if (obj instanceof JvmDto) {
 				jvm = (JvmDto) obj;
 			}
 		}
 		return jvm;
 	}
 
 	/**
 	 * @return system monitor list. Read from JDO if not found in memcache or
 	 *         JDO add new one or JDO update system information Return null if
 	 *         no system found.
 	 */
 	protected static ArrayList<SystemMonitorDto> listSystemMonitor() {
 		ArrayList<SystemMonitorDto> sysList = null;
 		if (isFlagChange()) {
 			sysList = readSystemFromJdo();
 		} else {
 			Object obj = get(Key.create(Key.SYSTEM_MONITOR_STORE, getCount()));
 			if (obj == null) {
 				sysList = readSystemFromJdo();
 			} else if (obj instanceof ArrayList<?>) {
 				sysList = (ArrayList<SystemMonitorDto>) obj;
 			}
 		}
 		return sysList;
 	}
 
 	/**
 	 * @param sid
 	 *            The ID of system monitor
 	 * @return return "dead" if cannot read data from system monitor. return
 	 *         "bored" if: - Cpu usage is higher than 90 percent - One of
 	 *         Memories usage is higher than 90 percent - One of filesystems
 	 *         memory usage is higher than 90 percent - One of services status
 	 *         is false or ping is higer than 500ms return "smile" if all status
 	 *         are good
 	 */
 	protected static String getCurrentHealthStatus(String sid) {
 		String status = "dead";
 		boolean checkService = true;
 		boolean checkCpu = true;
 		boolean checkMem = true;
 		boolean checkFileSystem = true;
 		boolean checkJvm = true;
 		SystemMonitorDto sys = getSystemById(sid);
 		CpuDTO cpu = getCpu(sid);
 		JvmDto jvm = getJvm(sid);
 
 		ArrayList<MemoryDto> memories = getMemories(sid);
 		ArrayList<FileSystemCacheDto> fileSystems = listFileSystem(sid);
 		ArrayList<ServiceMonitorDto> services = listService(sid);
 		if (sys != null) {
 			// Cpu usage
 			if (cpu != null) {
 				checkCpu = cpu.getCpuUsage() < 90;
 			}
 			if (memories != null) {
 				for (MemoryDto mem : memories) {
 					if (mem.getPercentUsage() > 90) {
 						checkMem = false;
 						break;
 					}
 				}
 			}
 			if (jvm != null) {
 				checkJvm = jvm.getPercentMemoryUsage() < 90;
 			}
 			if (services != null) {
 				for (ServiceMonitorDto service : services) {
 					if (!service.isStatus() || service.getPing() >= 500) {
 						checkService = false;
 						break;
 					}
 				}
 			}
 			if (fileSystems != null) {
 				for (FileSystemCacheDto fs : fileSystems) {
 					if (fs.getPercentUsed() > 90) {
 						checkFileSystem = false;
 						break;
 					}
 				}
 			}
 			status = sys.isStatus() && sys.isActive() ? (checkCpu && checkMem
 					&& checkJvm && checkFileSystem && checkService ? "smile"
 					: "bored") : "dead";
 		}
 		return status;
 	}
 
 	/**
 	 * @return the list of system monitor read from JDO. Null if no system found
 	 */
 	private static ArrayList<SystemMonitorDto> readSystemFromJdo() {
 		ArrayList<SystemMonitorDto> list = null;
 		try {
 			SystemMonitorDAO sysDao = new SystemMonitorDaoJDOImpl();
 			SystemMonitor[] sysList = sysDao.listSystems(false);
 			SystemMonitorDto sysDto = null;
 			if (sysList != null) {
 				list = new ArrayList<SystemMonitorDto>();
 				for (SystemMonitor sys : sysList) {
 					sysDto = new SystemMonitorDto();
 					sysDto.setId(sys.getId());
 					sysDto.setCode(sys.getCode());
 					sysDto.setActive(sys.isActive());
 					sysDto.setDeleted(sys.isDeleted());
 					sysDto.setEmail(sys.getEmail());
 					sysDto.setGroupEmail(sys.getGroupEmail());
 					sysDto.setIp(sys.getIp());
 					sysDto.setName(sys.getName());
 					sysDto.setProtocol(sys.getProtocol());
 					sysDto.setRemoteUrl(sys.getRemoteUrl());
 					sysDto.setStatus(sys.getStatus());
 					sysDto.setUrl(sys.getUrl());
 					list.add(sysDto);
 				}
 			}
 		} catch (Exception ex) {
 			// do nothing
 		}
 		//
 		changeFlag(false);
 		return list;
 	}
 
 	/**
 	 * @return count number.
 	 */
 	protected static int getCount() {
 		int count = 0;
 		try {
 			count = Integer.parseInt(get(Key.create(Key.COUNT_STORE))
 					.toString());
 		} catch (Exception e) {
 			// do nothing
 		}
 		return count;
 	}
 
 	/**
 	 * @param count
 	 *            store the counting value.
 	 */
 	protected static void setCount(int count) {
 		put(Key.create(Key.COUNT_STORE), count);
 	}
 
 	/**
 	 * @param key
 	 *            The key of store
 	 * @param obj
 	 *            Object which want to store
 	 */
 	protected static void put(Key key, Object obj) {
 		syncCache.put(key, obj, null, SetPolicy.SET_ALWAYS);
 	}
 
 	/**
 	 * @param key
 	 *            The key of store
 	 * @return Object in store. Return null if not found
 	 */
 	protected static Object get(Key key) {
 		return syncCache.get(key);
 	}
 	
 	protected static boolean delete(Key key) {
 		return syncCache.delete(key);
 	}
 
 	/**
 	 * @param b
 	 *            true when an System Monitor JDO insert or update.
 	 */
 	public static void changeFlag(boolean b) {
 		put(Key.create(Key.FLAG_STORE), b);
 	}
 
 	/**
 	 * @return return true if flag not found in mecache or one of system monitor
 	 *         is update or memche add new one
 	 */
 	protected static boolean isFlagChange() {
 		boolean b = true;
 		if (get(Key.create(Key.FLAG_STORE)) == null) {
 			b = true;
 		} else {
 			try {
 				b = Boolean.parseBoolean(get(Key.create(Key.FLAG_STORE))
 						.toString());
 			} catch (Exception ex) {
 			}
 		}
 		return b;
 	}
 
 	// END List of methods access data from memcache
 }
