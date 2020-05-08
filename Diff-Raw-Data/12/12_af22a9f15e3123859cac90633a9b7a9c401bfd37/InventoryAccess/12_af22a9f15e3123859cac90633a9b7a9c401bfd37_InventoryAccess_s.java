 package hwinventory.dao;
 
 import hwinventory.db.HibernateUtil;
 import hwinventory.domain.CategoryHardwareDevice;
 import hwinventory.domain.HardwareDevice;
 import hwinventory.domain.InventoryItem;
 import hwinventory.domain.LocationItemInventory;
 import hwinventory.domain.TypeHardwareDevice;
 import hwinventory.domain.User;
 
 import java.util.Calendar;
 import java.util.List;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 
 public class InventoryAccess extends HardwareInventoryDAO {
 
 	
 	/**
 	 * addCategory
 	 * @param aNameCategory
 	 * @return
 	 */
 	public CategoryHardwareDevice addCategory(String aNameCategory) {
 		CategoryHardwareDevice aCategory = getACategory(aNameCategory);
 		if (aCategory == null) {
 			aCategory = new CategoryHardwareDevice(aNameCategory);
 			saveCategory(aCategory);
 		}
 		return aCategory;
 	}
 	
 	/**
 	 * addType
 	 * @param aNameType
 	 * @param aCategory
 	 * @return
 	 */
 	public TypeHardwareDevice addType(String aNameType,
 			CategoryHardwareDevice aCategory) {
 		TypeHardwareDevice aType = getAType(aNameType);
 		if (aType == null) {
 			aType = new TypeHardwareDevice(aCategory, aNameType);
 			saveType(aType, aCategory);
 		}
 		return aType;
 	}
 
 	/**
 	 * addHardwareDevice
 	 * @param aType
 	 * @param aDiskSize
 	 * @param aMemorySize
 	 * @param anIanNumber
 	 * @param aMacAddress
 	 * @param aSerialNumber
 	 * @param anIpAddress
 	 * @return
 	 */
 	public HardwareDevice addHardwareDevice(TypeHardwareDevice aType,
 			String aDiskSize, String aMemorySize, int anIanNumber,
 			String aMacAddress, String aSerialNumber, String anIpAddress) {
 		HardwareDevice aHardwareDevice = getAHardwareDevice(aType, anIanNumber,
 				anIpAddress, aSerialNumber);
 		if (aHardwareDevice == null) {
 			aHardwareDevice = new HardwareDevice(aType, aDiskSize, aMemorySize,
 					anIanNumber, aMacAddress, aSerialNumber, anIpAddress);
 			saveHardwareDevice(aHardwareDevice, aType);
 		}
 		return aHardwareDevice;
 	}
 
 	/**
 	 * addUser
 	 * @param aNameUser
 	 * @return
 	 */
 	public User addUser(String aNameUser) {
 		User aUser = getAUser(aNameUser);
 		if (aUser == null) {
 			aUser = new User(aNameUser);
 			saveUser(aUser);
 		}
 		return aUser;
 	}
 
 	/**
 	 * addLocation
 	 * @param aNameLocation
 	 * @return
 	 */
 	public LocationItemInventory addAlocation(String aNameLocation) {
 		LocationItemInventory aLocation = getAlocation(aNameLocation);
 		if (aLocation == null) {
 			aLocation = new LocationItemInventory(aNameLocation);
 			saveLocation(aLocation);
 		}
 		return aLocation;
 	}
 
 	/**
 	 * addInventoryItem
 	 * @param aHardwareDevice
 	 * @param aScgNumber
 	 * @param aNameItem
 	 * @param aUser
 	 * @param aLocation
 	 * @param anInventoryDate
 	 * @param aPrice
 	 * @param aBudget
 	 * @param aGuarantee
 	 * @param aGuaranteeEnd
 	 * @param aNote
 	 */
 	public void addInventoryItem(HardwareDevice aHardwareDevice,
 			int aScgNumber, String aNameItem, User aUser,
 			LocationItemInventory aLocation, Calendar anInventoryDate,
 			float aPrice, String aBudget, String aGuarantee,
 			Calendar aGuaranteeEnd, String aNote) {
 		InventoryItem anInventoryItem = getAnInventoryItem(aScgNumber);
 		if (anInventoryItem == null) {
 			anInventoryItem = new InventoryItem(aHardwareDevice, aScgNumber,
 					aNameItem, aUser, aLocation, anInventoryDate, aPrice,
 					aBudget, aGuarantee, aGuaranteeEnd, aNote);
 			saveInventoryItem(anInventoryItem);
 		}
 	}
 
 	/**
 	 * getACategory
 	 * @param aNameCategory
 	 * @return
 	 */
 	private CategoryHardwareDevice getACategory(String aNameCategory) {
 		CategoryHardwareDevice aCategory = (CategoryHardwareDevice) accessAnObjectString(
 				"CategoryHardwareDevice", "nameCategory", aNameCategory);
 		return aCategory;
 	}
 
 	/**
 	 * getAType
 	 * @param aNameType
 	 * @return
 	 */
 	private TypeHardwareDevice getAType(String aNameType) {
 		TypeHardwareDevice aType = (TypeHardwareDevice) accessAnObjectString(
 				"TypeHardwareDevice", "nameType", aNameType);
 		return aType;
 	}
 
 	/**
 	 * getAUser
 	 * @param aNameUser
 	 * @return
 	 */
 	private User getAUser(String aNameUser) {
 		User aUser = (User) accessAnObjectString("User", "nameUser", aNameUser);
 		return aUser;
 	}
 
 	/**
 	 * getALocation
 	 * @param aNameLocation
 	 * @return
 	 */
 	private LocationItemInventory getAlocation(String aNameLocation) {
 		LocationItemInventory aLocation = (LocationItemInventory) accessAnObjectString(
 				"LocationItemInventory", "nameLocation", aNameLocation);
 		return aLocation;
 	}
 
 	/**
 	 * getAHardwareDevice
 	 * @param aType
 	 * @param anIanNumber
 	 * @param anIpAddress
 	 * @param aSerialNumber
 	 * @return
 	 */
 	private HardwareDevice getAHardwareDevice(TypeHardwareDevice aType,
 			int anIanNumber, String anIpAddress, String aSerialNumber) {
 		HardwareDevice aHardwareDevice = (HardwareDevice) accessAHarwareDevice(
 				aType, anIanNumber, anIpAddress, aSerialNumber);
 		return aHardwareDevice;
 	}
 
 	/**
 	 * getAnInventory
 	 * @param aScgNumber
 	 * @return
 	 */
 	private InventoryItem getAnInventoryItem(int aScgNumber) {
 		InventoryItem anInventoryItem = (InventoryItem) accessAnObjectInt(
 				"InventoryItem", "scgNumber", aScgNumber);
 		return anInventoryItem;
 
 	}
 
 	/**
 	 * saveCategory
 	 * @param aCategory
 	 */
 	private void saveCategory(CategoryHardwareDevice aCategory) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		aCategory.setNameCategory(aCategory.getNameCategory());
 		session.save(aCategory);
 		session.getTransaction().commit();
 	}
 
 	/**
 	 * saveType
 	 * @param aType
 	 * @param aCategory
 	 */
 	private void saveType(TypeHardwareDevice aType,
 			CategoryHardwareDevice aCategory) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		CategoryHardwareDevice aCateg = (CategoryHardwareDevice) session.load(CategoryHardwareDevice.class, aCategory.getIdCategory());
 		aCateg.addType(aType);
 		aType.setNameType(aType.getNameType());
 		aType.setCategory(aType.getCategory());
 		session.save(aType);
 		session.getTransaction().commit();
 	}
 
 	/**
 	 * saveUser
 	 * @param aUser
 	 */
 	private void saveUser(User aUser) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		aUser.setNameUser(aUser.getNameUser());
 		session.save(aUser);
 		session.getTransaction().commit();
 	}
 
 	/**
 	 * saveLocation
 	 * @param aLocation
 	 */
 	private void saveLocation(LocationItemInventory aLocation) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		aLocation.setNameLocation(aLocation.getNameLocation());
 		session.save(aLocation);
 		session.getTransaction().commit();
 	}
 	
 	/**
 	 * saveHardwareDevice
 	 * @param aHardwareDevice
 	 * @param aType
 	 */
 	private void saveHardwareDevice(HardwareDevice aHardwareDevice,
 			TypeHardwareDevice aType) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		TypeHardwareDevice aTypeHw = (TypeHardwareDevice) session.load(TypeHardwareDevice.class, aType.getIdType());
 		aTypeHw.addDevices(aHardwareDevice);
 		aHardwareDevice.setDiskSize(aHardwareDevice.getDiskSize());
 		aHardwareDevice.setIanNumber(aHardwareDevice.getIanNumber());
 		aHardwareDevice.setIpAddress(aHardwareDevice.getIpAddress());
 		aHardwareDevice.setMacAddress(aHardwareDevice.getMacAddress());
 		aHardwareDevice.setMemorySize(aHardwareDevice.getMemorySize());
 		aHardwareDevice.setSerialNumber(aHardwareDevice.getSerialNumber());
 		aHardwareDevice.setType(aHardwareDevice.getType());
 		session.save(aHardwareDevice);
 		session.getTransaction().commit();
 	}
 
 	/**
 	 * saveInventoryItem
 	 * @param anInventoryItem
 	 */
 	private void saveInventoryItem(InventoryItem anInventoryItem) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		anInventoryItem.setScgNumber(anInventoryItem.getScgNumber());
 		anInventoryItem.setBudget(anInventoryItem.getBudget());
 		anInventoryItem.setGuarantee(anInventoryItem.getGuarantee());
 		anInventoryItem.setGuaranteeEnd(anInventoryItem.getGuaranteeEnd());
 		anInventoryItem.setHardwareDevice(anInventoryItem.getHardwareDevice());
 		anInventoryItem.setInventoryDate(anInventoryItem.getInventoryDate());
 		anInventoryItem.setLocation(anInventoryItem.getLocation());
 		anInventoryItem.setNameItem(anInventoryItem.getNameItem());
 		anInventoryItem.setNote(anInventoryItem.getNote());
 		anInventoryItem.setPrice(anInventoryItem.getPrice());
 		anInventoryItem.setUser(anInventoryItem.getUser());
 		anInventoryItem.setNote(anInventoryItem.getNote());
 		session.save(anInventoryItem);
 		session.getTransaction().commit();
 	}
 
 	/**
 	 * listObject
 	 * @param className
 	 * @param variableName
 	 * @param variable
 	 * @return
 	 * @throws HibernateException
 	 */
 	public List listObject(String className) throws HibernateException {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List result = null;
 		try {
 			result = session.createQuery("from " + className).list();
 			session.getTransaction().commit();
 		} catch (HibernateException e) {
 			e.printStackTrace();
 		} finally {
 		}
 		return result;
 	}
 	
 	/**
 	 * getAllItems
 	 * @return
 	 */
 	public List getAllItems() {
 		return listObject("InventoryItem");
 	}
 	
 	/**
 	 * getAllUsers
 	 * @return
 	 */
 	public List getAllUsers() {
 		return listObject("User");
 	}
 	
 	/**
 	 * accessAnObjectInt
 	 * @param className
 	 * @param variableName
 	 * @param variable
 	 * @return
 	 * @throws HibernateException
 	 */
 	private Object accessAnObjectInt(String className, String variableName,
 			int variable) throws HibernateException {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Object result = null;
 		try {
 			result = session
 					.createQuery(
 							"from " + className + " c where c." + variableName
 									+ "= :name").setParameter("name", variable)
 					.uniqueResult();
 			session.getTransaction().commit();
 		} catch (HibernateException e) {
 			e.printStackTrace();
 		} finally {
 		}
 		return result;
 	}
 
 	/**
 	 * accessAnObjectString
 	 * @param className
 	 * @param variableName
 	 * @param variable
 	 * @return
 	 * @throws HibernateException
 	 */
 	public Object accessAnObjectString(String className, String variableName,
 			String variable) throws HibernateException {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Object result = null;
 		try {
 			result = session
 					.createQuery(
 							"from " + className + " c where c." + variableName
 									+ "= :name").setParameter("name", variable)
 					.uniqueResult();
 			session.getTransaction().commit();
 		} catch (HibernateException e) {
 			e.printStackTrace();
 		} finally {
 		}
 		return result;
 	}
 	
 	/**
 	 * accessAnObjectLong
 	 * @param className
 	 * @param variableName
 	 * @param variable
 	 * @return
 	 * @throws HibernateException
 	 */
 	public Object accessAnObjectLong(String className, String variableName,
 			Long variable) throws HibernateException {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Object result = null;
 		try {
 			result = session
 					.createQuery(
 							"from " + className + " c where c." + variableName
 									+ "= :name").setParameter("name", variable)
 					.uniqueResult();
 			session.getTransaction().commit();
 		} catch (HibernateException e) {
 			e.printStackTrace();
 		} finally {
 		}
 		return result;
 	}
 	
 	/**
 	 * getAUser
 	 * @param anIdUser
 	 * @return
 	 */
 	public Object getAUser(Long anIdUser) {
 		return accessAnObjectLong("User", "idUser", anIdUser);
 	}
 
 	/**
 	 * accessAHardwareDevice
 	 * @param aType
 	 * @param anIanNumber
 	 * @param anIpAddress
 	 * @param aSerialNumber
 	 * @return
 	 */
 	private HardwareDevice accessAHarwareDevice(TypeHardwareDevice aType,
 			int anIanNumber, String anIpAddress, String aSerialNumber) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		HardwareDevice result = null;
 		if (anIanNumber == 0 && anIpAddress == null && aSerialNumber == null) {
 			try {
 				result = (HardwareDevice) session
 						.createQuery(
 								"from HardwareDevice h where h.type.idType =:type")
 						.setParameter("type", aType.getIdType()).uniqueResult();
 				session.getTransaction().commit();
 			} catch (HibernateException e) {
 				e.printStackTrace();
 			} finally {
 			}
 		} else {
 			try {
 				result = (HardwareDevice) session
 						.createQuery(
 								"from HardwareDevice h where h.ianNumber =:ian and h.ipAddress =:ip and h.serialNumber =:serial")
 						.setParameter("ian", anIanNumber)
 						.setParameter("ip", anIpAddress)
 						.setParameter("serial", aSerialNumber).uniqueResult();
 				session.getTransaction().commit();
 			} catch (HibernateException e) {
 				e.printStackTrace();
 			} finally {
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * modifyCategory	
 	 * @param aNameCategory
 	 * @param aNewNameCategory
 	 */
 	public void modifyCategory(String aNameCategory, String aNewNameCategory) {
 		CategoryHardwareDevice aCategory = getACategory(aNameCategory);
 		if (aCategory != null) {
 			updateCategory(aCategory, aNewNameCategory);
 		}
 	}
 	
 	/**
 	 * uodateCategory
 	 * @param aCategory
 	 * @param aNewNameCategory
 	 */
 	private void updateCategory(CategoryHardwareDevice aCategory, String aNewNameCategory) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		aCategory.setNameCategory(aNewNameCategory);
 		session.update(aCategory);
 		session.getTransaction().commit();	
 	}
 	
 	/**
 	 * modifyUser
 	 * @param aNameUser
 	 * @param aNewNameUser
 	 */
	public void modifyUser(String aNameUser, String aNewNameUser) {
		User aUser = getAUser(aNameUser);
		if (aUser != null) {
			updateUser(aUser, aNewNameUser);
		}
 	}
 	
 	/**
 	 * updateUser
 	 * @param aUser
 	 * @param aNewNameUser
 	 */
	private void updateUser(User aUser, String aNewNameUser) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
		aUser.setNameUser(aNewNameUser);
 		session.update(aUser);
 		session.getTransaction().commit();	
 	}
 	
 	/**
 	 * modifyInventoryItem
 	 * @param aScgNumber
 	 * @param anIanNumber
 	 * @param aNameCategory
 	 * @param aNameItem
 	 * @param anIpAddress
 	 * @param aMacAddress
 	 * @param aNameType
 	 * @param aDiskSize
 	 * @param aMemorySize
 	 * @param aNameLocation
 	 * @param aNameUser
 	 * @param aPrice
 	 * @param anInventoryDate
 	 * @param aBudget
 	 * @param aSerialNumber
 	 * @param aGuarantee
 	 * @param aGuaranteeEnd
 	 * @param aNote
 	 */
 	public void modifyInventoryItem(int aScgNumber, int anIanNumber,
 			String aNameCategory, String aNameItem, String anIpAddress,
 			String aMacAddress, String aNameType, String aDiskSize,
 			String aMemorySize, String aNameLocation, String aNameUser,
 			float aPrice, Calendar anInventoryDate, String aBudget,
 			String aSerialNumber, String aGuarantee, Calendar aGuaranteeEnd,
 			String aNote) {
 		InventoryItem anInventoryItem = getAnInventoryItem(aScgNumber);
 		if (anInventoryItem != null) {
 			User aUser = getAUser(aNameUser);
 			LocationItemInventory aLocation = getAlocation(aNameLocation);
 			TypeHardwareDevice aType = getAType(aNameType);
 			HardwareDevice aHarwareDevice = getAHardwareDevice(aType, anIanNumber, anIpAddress, aSerialNumber);
 			updateInventoryItem(anInventoryItem, aUser, aLocation, aHarwareDevice, aNameItem
 					,aPrice, anInventoryDate, aBudget, aGuarantee, aGuaranteeEnd
 					,aNote);
 		}
 	}
 	
 	/**
 	 * updateInventoryItem
 	 * @param anInventoryItem
 	 * @param aUser
 	 * @param aLocation
 	 * @param aHarwareDevice
 	 * @param aNameItem
 	 * @param aPrice
 	 * @param anInventoryDate
 	 * @param aBudget
 	 * @param aGuarantee
 	 * @param aGuaranteeEnd
 	 * @param aNote
 	 */
 	private void updateInventoryItem(InventoryItem anInventoryItem,
 			User aUser, LocationItemInventory aLocation,
 			HardwareDevice aHarwareDevice, String aNameItem, float aPrice,
 			Calendar anInventoryDate, String aBudget, String aGuarantee,
 			Calendar aGuaranteeEnd, String aNote) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();	
 		anInventoryItem.setBudget(aBudget);
 		anInventoryItem.setGuarantee(aGuarantee);
 		anInventoryItem.setGuaranteeEnd(aGuaranteeEnd);
 		anInventoryItem.setHardwareDevice(aHarwareDevice);
 		anInventoryItem.setInventoryDate(anInventoryDate);
 		anInventoryItem.setLocation(aLocation);
 		anInventoryItem.setNameItem(aNameItem);
 		anInventoryItem.setNote(aNote);
 		anInventoryItem.setPrice(aPrice);
 		anInventoryItem.setUser(aUser);
 		session.update(anInventoryItem);
 		session.getTransaction().commit();	
 	}
 
 	/**
 	 * removeInventoryItem
 	 * @param aScgNumber
 	 */
 	public void removeInventoryItem(int aScgNumber) {
 		InventoryItem anInventoryItem = getAnInventoryItem(aScgNumber);
 		if (anInventoryItem != null) {
 			deleteInventoryItem(anInventoryItem);
 		}
 	}
 	
 	/**
 	 * deleteInventoryItem
 	 * @param anInventoryItem
 	 */
 	public void deleteInventoryItem(InventoryItem anInventoryItem) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.delete(anInventoryItem);
 		session.getTransaction().commit();	
 	}
 	
 	/**
 	 * removeUser
 	 * @param aNameUser
 	 */
 	public void removeUser(String aNameUser) {
 		User aUser = getAUser(aNameUser);
 		if (aUser != null) {
 			deleteUser(aUser);
 		}
 	}
 	
 	/**
 	 * deleteUser
 	 * @param aUser
 	 */
 	private void deleteUser(User aUser) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.delete(aUser);
 		session.getTransaction().commit();	
 	}
 }
