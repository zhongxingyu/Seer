 package com.parq.server.dao;
 
 import java.util.Date;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.parq.server.dao.model.object.ParkingInstance;
 import com.parq.server.dao.model.object.ParkingLocation;
 import com.parq.server.dao.model.object.Payment;
 import com.parq.server.dao.model.object.User;
 import com.parq.server.dao.model.object.Payment.PaymentType;
 import com.parq.server.dao.support.SupportScriptForDaoTesting;
 
 public class TestParkingStatusDao extends TestCase {
 
 	private ParkingStatusDao statusDao;
 	private UserDao userDao;
 	private ParkingInstance pi;
 	private ParkingInstance piRefil;
 	private User user;
 	private List<ParkingLocation> buildingList;
 
 	@Override
 	protected void setUp() throws Exception {
 		SupportScriptForDaoTesting.insertFakeData();
 		
 		statusDao = new ParkingStatusDao();
 		User newUser = new User();
 		newUser.setPassword("password");
 		newUser.setEmail("eMail");
 		userDao = new UserDao();
 		boolean userCreationSuccessful = userDao.createNewUser(newUser);
 		assertTrue(userCreationSuccessful);
 		user = userDao.getUserByEmail("eMail");
 		
 		ClientDao clientDao = new ClientDao();
 		buildingList = clientDao.getParkingLocationsAndSpacesByClientId(
 				clientDao.getClientByName(SupportScriptForDaoTesting.clientNameMain).getId());
 		
 		pi = new ParkingInstance();
 		pi.setPaidParking(true);
 		pi.setParkingBeganTime(new Date(System.currentTimeMillis()));
 		pi.setParkingEndTime(new Date(System.currentTimeMillis() + 3600000));
 		pi.setSpaceId(buildingList.get(0).getSpaces().get(0).getSpaceId());
 		pi.setUserId(user.getUserID());
 		
 		Payment paymentInfo = new Payment();
 		paymentInfo.setAmountPaidCents(1005);
 		paymentInfo.setPaymentDateTime(new Date(System.currentTimeMillis()));
 		paymentInfo.setPaymentRefNumber("Test_Payment_Ref_Num_1");
 		paymentInfo.setPaymentType(PaymentType.CreditCard);
 		pi.setPaymentInfo(paymentInfo);
 		
 		piRefil = new ParkingInstance();
 		piRefil.setPaidParking(true);
 		piRefil.setParkingBeganTime(new Date(System.currentTimeMillis()));
 		piRefil.setParkingEndTime(new Date(System.currentTimeMillis() + 7200000));
 		piRefil.setSpaceId(buildingList.get(0).getSpaces().get(0).getSpaceId());
 		piRefil.setUserId(user.getUserID());
 		
 		Payment paymentInfo2 = new Payment();
 		paymentInfo2.setAmountPaidCents(1250);
 		paymentInfo2.setPaymentDateTime(new Date(System.currentTimeMillis()));
 		paymentInfo2.setPaymentRefNumber("Test_Payment_Ref_Num_1");
 		paymentInfo2.setPaymentType(PaymentType.CreditCard);
 		piRefil.setPaymentInfo(paymentInfo2);
 	}
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		// user object cleanup
 		userDao.deleteUserById(user.getUserID());
 	}
 
 
 
 	public void testGetParkingStatusBySpaceIds() {
 		statusDao.getParkingStatusBySpaceIds(new int[]{1,2,3});
 	}
 	
 	public void testAddNewParkingAndPayment() {
 		assertTrue(statusDao.addNewParkingAndPayment(pi));
 		
 		List<ParkingInstance> result = statusDao.getParkingStatusBySpaceIds(new int[]{buildingList.get(0).getSpaces().get(0).getSpaceId()});
 		assertNotNull(result);
 		assertFalse(result.isEmpty());
 		ParkingInstance resultInstance = result.get(0);
 		assertEquals(resultInstance.getSpaceId(), pi.getSpaceId());
 		assertEquals(resultInstance.getUserId(), pi.getUserId());
 		// time comparison is truncated to the nearest seconds
 		assertEquals(resultInstance.getParkingBeganTime().getTime() / 1000, pi.getParkingBeganTime().getTime() / 1000);
 		// time comparison is truncated to the nearest seconds
 		assertEquals(resultInstance.getParkingEndTime().getTime() / 1000, pi.getParkingEndTime().getTime() / 1000);
 		assertEquals(resultInstance.getPaymentInfo().getAmountPaidCents(), pi.getPaymentInfo().getAmountPaidCents());
 		assertEquals(resultInstance.getPaymentInfo().getParkingInstId(), resultInstance.getParkingInstId());
 		assertEquals(resultInstance.getPaymentInfo().getPaymentRefNumber(), pi.getPaymentInfo().getPaymentRefNumber());
 		// time comparison is truncated to the nearest seconds
 		assertEquals(resultInstance.getPaymentInfo().getPaymentDateTime().getTime() / 1000, 
 					pi.getPaymentInfo().getPaymentDateTime().getTime() / 1000);
 		assertEquals(resultInstance.getPaymentInfo().getPaymentType(), pi.getPaymentInfo().getPaymentType());
 		assertEquals(resultInstance.getPaymentInfo().getAmountPaidCents(), pi.getPaymentInfo().getAmountPaidCents());
 	}
 	
 	public void testRefillParkingForParkingSpace() {
 		assertTrue(statusDao.addNewParkingAndPayment(pi));
 		
 		// add the new parking instance
 		List<ParkingInstance> resultInsts = statusDao.getParkingStatusBySpaceIds(new int[]{buildingList.get(0).getSpaces().get(0).getSpaceId()});
 		ParkingInstance initialParkingInst = resultInsts.get(0);
 		
 		// refill the parking
 		assertTrue(statusDao.refillParkingForParkingSpace(piRefil.getSpaceId(), piRefil.getParkingEndTime(), piRefil.getPaymentInfo()));
 
 		// get the newly refilled parking info
 		List<ParkingInstance> resultInsts2 = statusDao.getParkingStatusBySpaceIds(new int[]{buildingList.get(0).getSpaces().get(0).getSpaceId()});
 		assertFalse(resultInsts2.isEmpty());
 		ParkingInstance refilledParkingInst = resultInsts2.get(0);
 		
 		
 		assertEquals(refilledParkingInst.getSpaceId(), initialParkingInst.getSpaceId());
 		assertEquals(refilledParkingInst.getUserId(), initialParkingInst.getUserId());
 		// time comparison is truncated to the nearest minute
 		assertEquals(refilledParkingInst.getParkingBeganTime().getTime() / 60000, System.currentTimeMillis() / 60000);
 		// time comparison is truncated to the nearest seconds
 		assertEquals(refilledParkingInst.getParkingEndTime().getTime() / 1000, piRefil.getParkingEndTime().getTime() / 1000);
 		assertEquals(refilledParkingInst.getParkingRefNumber(), initialParkingInst.getParkingRefNumber());
 		assertEquals(refilledParkingInst.getPaymentInfo().getAmountPaidCents(), piRefil.getPaymentInfo().getAmountPaidCents());
 		assertFalse(refilledParkingInst.getPaymentInfo().getParkingInstId() == initialParkingInst.getPaymentInfo().getParkingInstId());
 		assertEquals(refilledParkingInst.getPaymentInfo().getPaymentRefNumber(), piRefil.getPaymentInfo().getPaymentRefNumber());
 		// time comparison is truncated to the nearest seconds
 		assertEquals(refilledParkingInst.getPaymentInfo().getPaymentDateTime().getTime() / 1000, 
 				piRefil.getPaymentInfo().getPaymentDateTime().getTime() / 1000);
 		assertEquals(refilledParkingInst.getPaymentInfo().getPaymentType(), piRefil.getPaymentInfo().getPaymentType());
 	}
 	
 	public void testGetUserParkingStatus() {
 		assertTrue(statusDao.addNewParkingAndPayment(pi));
 		
 		ParkingInstance userParkingStatus = statusDao.getUserParkingStatus(user.getUserID());
 		assertNotNull(userParkingStatus);
 		assertEquals(user.getUserID(), userParkingStatus.getUserId());
 		assertEquals(userParkingStatus.getSpaceId(), pi.getSpaceId());
 		assertEquals(userParkingStatus.getUserId(), pi.getUserId());
 		// time comparison is truncated to the nearest seconds
 		assertEquals(userParkingStatus.getParkingBeganTime().getTime() / 1000, pi.getParkingBeganTime().getTime() / 1000);
 		// time comparison is truncated to the nearest seconds
 		assertEquals(userParkingStatus.getParkingEndTime().getTime() / 1000, pi.getParkingEndTime().getTime() / 1000);
 		assertEquals(userParkingStatus.getPaymentInfo().getAmountPaidCents(), pi.getPaymentInfo().getAmountPaidCents());
 		assertEquals(userParkingStatus.getPaymentInfo().getParkingInstId(), userParkingStatus.getParkingInstId());
 		assertEquals(userParkingStatus.getPaymentInfo().getPaymentRefNumber(), pi.getPaymentInfo().getPaymentRefNumber());
 		// time comparison is truncated to the nearest seconds
 		assertEquals(userParkingStatus.getPaymentInfo().getPaymentDateTime().getTime() / 1000, 
 					pi.getPaymentInfo().getPaymentDateTime().getTime() / 1000);
 		assertEquals(userParkingStatus.getPaymentInfo().getPaymentType(), pi.getPaymentInfo().getPaymentType());
 		assertEquals(userParkingStatus.getPaymentInfo().getAmountPaidCents(), pi.getPaymentInfo().getAmountPaidCents());
 		
 		// user object cleanup
 		 boolean deleteSuccessful = userDao.deleteUserById(user.getUserID());
 		 assertTrue(deleteSuccessful);
 	}
 
 	public void testCacheRunTime() {
 		assertTrue(statusDao.addNewParkingAndPayment(pi));
 		
 		ParkingInstance gups = statusDao.getUserParkingStatus(user.getUserID());
 		
 		long curSysTimeBeforeCacheCall = System.currentTimeMillis();
 		for (int i = 0; i < 1000; i++) {
 			ParkingInstance ups = statusDao.getUserParkingStatus(user.getUserID());
 			assertNotNull(ups);
 			assertEquals(user.getUserID(), ups.getUserId());
 			assertSame(gups, ups);
 		}
 		
 		ParkingInstance gpsbsi = statusDao.getParkingStatusBySpaceIds(new int[]{gups.getSpaceId()}).get(0);
 		for (int i = 0; i < 1000; i++) {
 			ParkingInstance ps = statusDao.getParkingStatusBySpaceIds(new int[]{gups.getSpaceId()}).get(0);
 			assertNotNull(ps);
 			assertEquals(user.getUserID(), ps.getUserId());
 			assertSame(gpsbsi, ps);
 		}
 		
 		long curSysTimeAfterCacheCall = System.currentTimeMillis();
 		// check to see that the a call to the get method 2000 time result in runtime of less then 1 second
 		assertTrue(curSysTimeAfterCacheCall - curSysTimeBeforeCacheCall < 1000);
 		// System.out.println("20000 cache hit time in milliseconds: " 
 		//	+ (curSysTimeAfterCacheCall - curSysTimeBeforeCacheCall));
 		
 		// user object cleanup
 		boolean deleteSuccessful = userDao.deleteUserById(user.getUserID());
 		assertTrue(deleteSuccessful);
 	}
 	
 	public void testCacheUpdate() {
 		assertTrue(statusDao.addNewParkingAndPayment(pi));
 		
 		ParkingInstance oldGUPS = statusDao.getUserParkingStatus(user.getUserID());
 		
 		// make multiple cache hits to ensure current result are cached.
 		for (int i = 0; i < 10; i++) {
 			ParkingInstance ups = statusDao.getUserParkingStatus(user.getUserID());
 			assertNotNull(ups);
 			assertEquals(user.getUserID(), ups.getUserId());
 			assertSame(oldGUPS, ups);
 		}
 		
 		ParkingInstance oldGPSBSI = statusDao.getParkingStatusBySpaceIds(new int[]{oldGUPS.getSpaceId()}).get(0);
 		for (int i = 0; i < 10; i++) {
 			ParkingInstance ps = statusDao.getParkingStatusBySpaceIds(new int[]{oldGUPS.getSpaceId()}).get(0);
 			assertNotNull(ps);
 			assertEquals(user.getUserID(), ps.getUserId());
 			assertSame(oldGPSBSI, ps);
 		}
 		
 		// update the payment information
 		ParkingInstance newPi = new ParkingInstance();
 		newPi.setPaidParking(true);
 		newPi.setParkingBeganTime(new Date(System.currentTimeMillis() + 5000));
 		newPi.setParkingEndTime(new Date(System.currentTimeMillis() + 3600000));
 		newPi.setSpaceId(buildingList.get(0).getSpaces().get(0).getSpaceId());
 		newPi.setUserId(user.getUserID());
 		
 		Payment newPaymentInfo = new Payment();
 		newPaymentInfo.setAmountPaidCents(550);
 		newPaymentInfo.setPaymentDateTime(new Date(System.currentTimeMillis() + 5000));
 		newPaymentInfo.setPaymentRefNumber("Test_Payment_Ref_Num_2");
 		newPaymentInfo.setPaymentType(PaymentType.CreditCard);
 		newPi.setPaymentInfo(newPaymentInfo);
 		
 		assertTrue(statusDao.addNewParkingAndPayment(newPi));
 		
 		ParkingInstance newGUPS = statusDao.getUserParkingStatus(user.getUserID());
 		ParkingInstance newGPSBSI = statusDao.getParkingStatusBySpaceIds(new int[]{oldGUPS.getSpaceId()}).get(0);
 		
 		assertNotSame(oldGUPS, newGUPS);
 		assertNotSame(oldGPSBSI, newGPSBSI);
 		assertTrue(newGUPS.getParkingInstId() > oldGUPS.getParkingInstId());
 		assertTrue(newGUPS.getPaymentInfo().getPaymentId() > oldGUPS.getPaymentInfo().getPaymentId());
 		assertEquals("Test_Payment_Ref_Num_2" ,newGUPS.getPaymentInfo().getPaymentRefNumber());
 		assertEquals(550 ,newGUPS.getPaymentInfo().getAmountPaidCents());
 		
 		// test cache
 		for (int i = 0; i < 10; i++) {
 			ParkingInstance ups = statusDao.getUserParkingStatus(user.getUserID());
 			assertNotNull(ups);
 			assertEquals(user.getUserID(), ups.getUserId());
 			assertSame(newGUPS, ups);
 		}
 		
 		// test cache
 		for (int i = 0; i < 10; i++) {
 			ParkingInstance ps = statusDao.getParkingStatusBySpaceIds(new int[]{oldGUPS.getSpaceId()}).get(0);
 			assertNotNull(ps);
 			assertEquals(user.getUserID(), ps.getUserId());
 			assertSame(newGPSBSI, ps);
 		}
 		
 		// user object cleanup
 		boolean deleteSuccessful = userDao.deleteUserById(user.getUserID());
 		assertTrue(deleteSuccessful);
 	}
 	
 	public void testUnparkBySpaceIdAndParkingInstId() {
 		assertTrue(statusDao.addNewParkingAndPayment(pi));
 
 		// update the parking endtime to current time minus 60 sec
 		ParkingInstance oldPiStatus = statusDao.getUserParkingStatus(user
 				.getUserID());
 		long curTime = System.currentTimeMillis() - (60 * 1000);
 
 		statusDao.unparkBySpaceIdAndParkingInstId(oldPiStatus.getSpaceId(),
 				oldPiStatus.getParkingInstId(), new Date(curTime));
 		
 		ParkingInstance newPiStatus = statusDao.getUserParkingStatus(user
 				.getUserID());
 		// timestamp stored in the DB is only accurate to the second interval.
 		assertEquals(newPiStatus.getParkingEndTime().getTime() / 1000,  curTime / 1000);
 		
 		// user object cleanup
 		 boolean deleteSuccessful = userDao.deleteUserById(user.getUserID());
 		 assertTrue(deleteSuccessful);
 	}
 	
 }
