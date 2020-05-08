 package org.generationcp.middleware.manager.test;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.generationcp.middleware.manager.DatabaseConnectionParameters;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.manager.api.InventoryDataManager;
 import org.generationcp.middleware.pojos.Lot;
 import org.generationcp.middleware.pojos.Transaction;
 import org.generationcp.middleware.pojos.report.LotReportRow;
 import org.generationcp.middleware.pojos.report.TransactionReportRow;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class TestInventoryDataManagerImpl
 {
 	private static ManagerFactory factory;
 	private static InventoryDataManager manager;
 
 	@BeforeClass
 	public static void setUp() throws Exception
 	{
 		DatabaseConnectionParameters local = new DatabaseConnectionParameters("testDatabaseConfig.properties", "local");
 		DatabaseConnectionParameters central = new DatabaseConnectionParameters("testDatabaseConfig.properties", "central");
 		factory = new ManagerFactory(local, central);
 		manager = factory.getInventoryDataManager();
 	}
 
 	@Test
 	public void testFindLotsByEntityType() throws Exception
 	{
 		List<Lot> results = manager.findLotsByEntityType("GERMPLSM", 0, 5);
 		Assert.assertTrue(results != null);
 		Assert.assertTrue(!results.isEmpty());
 		System.out.println("RESULTS:");
 		for(Lot result : results)
 			System.out.println(result);
 	}
 	
 	@Test
 	public void testCountLotsByEntityType() throws Exception
 	{
 		System.out.println(manager.countLotsByEntityType("GERMPLSM"));
 	}
 	
 	@Test
 	public void testFindLotsByEntityTypeAndEntityId() throws Exception
 	{
 		List<Lot> results = manager.findLotsByEntityTypeAndEntityId("GERMPLSM", new Integer(50533), 0, 5);
 		Assert.assertTrue(results != null);
 		Assert.assertTrue(!results.isEmpty());
 		System.out.println("RESULTS:");
 		for(Lot result : results)
 			System.out.println(result);
 	}
 	
 	@Test
 	public void testCountLotsByEntityTypeAndEntityId() throws Exception
 	{
 		System.out.println(manager.countLotsByEntityTypeAndEntityId("GERMPLSM", new Integer(50533)));
 	}
 	
 	@Test
 	public void testFindLotsByEntityTypeAndLocationId() throws Exception
 	{
 		List<Lot> results = manager.findLotsByEntityTypeAndLocationId("GERMPLSM", new Integer(9000), 0, 5);
 		Assert.assertTrue(results != null);
 		Assert.assertTrue(!results.isEmpty());
 		System.out.println("RESULTS:");
 		for(Lot result : results)
 			System.out.println(result);
 	}
 	
 	@Test
 	public void testCountLotsByEntityTypeAndLocationId() throws Exception
 	{
 		System.out.println(manager.countLotsByEntityTypeAndLocationId("GERMPLSM", new Integer(9000)));
 	}
 	
 	@Test
 	public void testFindLotsByEntityTypeAndEntityIdAndLocationId() throws Exception
 	{
 		List<Lot> results = manager.findLotsByEntityTypeAndEntityIdAndLocationId("GERMPLSM", new Integer(50533), new Integer(9000), 0, 5);
 		Assert.assertTrue(results != null);
 		Assert.assertTrue(!results.isEmpty());
 		System.out.println("RESULTS:");
 		for(Lot result : results)
 			System.out.println(result);
 	}
 	
 	@Test
 	public void testCountLotsByEntityTypeAndEntityIdAndLocationId() throws Exception
 	{
 		System.out.println(manager.countLotsByEntityTypeAndEntityIdAndLocationId("GERMPLSM", new Integer(50533), new Integer(9000)));
 	}
 	
 	@Test
 	public void testGetActualLotBalance() throws Exception
 	{
 		try {
 			System.out.println("getActualLotBalance(): " + manager.getActualLotBalance(-1));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testGetAvailableLotBalance() throws Exception
 	{
 		try {
 			System.out.println("getAvailableLotBalance(): " + manager.getAvailableLotBalance(-1));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testAddLot() throws Exception
 	{
 		Lot lot = new Lot();
 		lot.setComments("sample added lot");
 		lot.setEntityId(new Integer(50533));
 		lot.setEntityType("GERMPLSM");
 		lot.setLocationId(new Integer(9001));
 		lot.setScaleId(new Integer(1538));
 		lot.setSource(null);
 		lot.setStatus(new Integer(0));
 		lot.setUserId(new Integer(1));
 		
 		int added = manager.addLot(lot);
 		Assert.assertTrue(added == 1);
 	}
 	
 	@Test
 	public void testUpdateLot() throws Exception
 	{
 		//this test assumes there are existing lot records with entity type = GERMPLSM
 		Lot lot = manager.findLotsByEntityType("GERMPLSM", 0, 5).get(0);
 		lot.setComments("update comment");
 		int update = manager.updateLot(lot);
 		Assert.assertTrue(update == 1);
 	}
 	
 	@Test 
 	public void testAddTransaction() throws Exception
 	{
 		//this test assumes there are existing lot records with entity type = GERMPLSM
 		Transaction transaction = new Transaction();
 		transaction.setComments("sample added transaction");
 		transaction.setDate(new Integer(20120413));
 		Lot lot = manager.findLotsByEntityType("GERMPLSM", 0, 5).get(0);
 		transaction.setLot(lot);
 		transaction.setPersonId(new Integer(1));
 		transaction.setPreviousAmount(null);
 		transaction.setQuantity(new Integer(100));
 		transaction.setSourceId(null);
 		transaction.setSourceRecordId(null);
 		transaction.setSourceType(null);
 		transaction.setStatus(new Integer(1));
 		transaction.setUserId(new Integer(1));
 		
 		int added = manager.addTransaction(transaction);
 		Assert.assertTrue(added == 1);
 	}
 	
 	
 	@Test
 	public void testUpdateTransaction() throws Exception
 	{
 		//this test assumes that there are existing records in the transaction table
 		Transaction t = manager.getTransactionById(new Integer(-1));
 		t.setComments("updated comment again");
 		t.setStatus(new Integer(0));
 		
 		int updated = manager.updateTransaction(t);
 		Assert.assertTrue(updated == 1);
 	}
 	
 	@Test
 	public void testFindTransactionsByLotId() throws Exception
 	{
 		Set<Transaction> transactions = manager.findTransactionsByLotId(new Integer(-1));
 		Assert.assertTrue(transactions != null);
 		Assert.assertTrue(!transactions.isEmpty());
 		for(Transaction t : transactions)
 			System.out.println(t);
 	}
 	
 	@Test
 	public void testGetAllReserveTransactions() throws Exception
 	{
 		List<Transaction> transactions = manager.getAllReserveTransactions(0, 5);
 		Assert.assertTrue(transactions != null);
 		Assert.assertTrue(!transactions.isEmpty());
 		for(Transaction t : transactions)
 			System.out.println(t);
 	}
 	
 	@Test
 	public void countAllReserveTransactions() throws Exception
 	{
 		System.out.println(manager.countAllReserveTransactions());
 	}
 	
 	@Test
 	public void testGetAllDepositTransactions() throws Exception
 	{
 		List<Transaction> transactions = manager.getAllDepositTransactions(0, 5);
 		Assert.assertTrue(transactions != null);
 		Assert.assertTrue(!transactions.isEmpty());
 		for(Transaction t : transactions)
 			System.out.println(t);
 	}
 	
 	@Test
 	public void countAllDepositTransactions() throws Exception
 	{
 		System.out.println(manager.countAllDepositTransactions());
 	}
 	
 	@Test
 	public void testGetAllReserveTransactionsByRequestor() throws Exception
 	{
 		List<Transaction> transactions = manager.getAllReserveTransactionsByRequestor(new Integer(253), 0, 5);
 		Assert.assertTrue(transactions != null);
 		Assert.assertTrue(!transactions.isEmpty());
 		for(Transaction t : transactions)
 			System.out.println(t);
 	}
 	
 	@Test
 	public void countAllReserveTransactionsByRequestor() throws Exception
 	{
 		System.out.println(manager.countAllReserveTransactionsByRequestor(new Integer(253)));
 	}
 	
 	@Test
 	public void testGetAllDepositTransactionsByDonor() throws Exception
 	{
 		List<Transaction> transactions = manager.getAllDepositTransactionsByDonor(new Integer(253), 0, 5);
 		Assert.assertTrue(transactions != null);
 		Assert.assertTrue(!transactions.isEmpty());
 		for(Transaction t : transactions)
 			System.out.println(t);
 	}
 	
 	@Test
 	public void countAllDepositTransactionsByDonor() throws Exception
 	{
 		System.out.println(manager.countAllDepositTransactionsByDonor(new Integer(253)));
 	}
 	
 	@Test
 	public void testGenerateReportOnAllUncommittedTransactions() throws Exception
 	{
 		System.out.println("Number of uncommitted transactions: " + manager.countAllUncommittedTransactions());
 		List<TransactionReportRow> report = manager.generateReportOnAllUncommittedTransactions(0, 5);
 		Assert.assertTrue(report != null);
 		Assert.assertTrue(!report.isEmpty());
 		System.out.println("REPORT:");
 		for(TransactionReportRow row: report)
 			System.out.println(row);
 	}
 	
 	@Test
 	public void testGenerateReportOnAllReserveTransactions() throws Exception
 	{
 		System.out.println("Number of reserve transactions: " + manager.countAllReserveTransactions());
 		List<TransactionReportRow> report = manager.generateReportOnAllReserveTransactions(0, 5);
 		Assert.assertTrue(report != null);
 		Assert.assertTrue(!report.isEmpty());
 		System.out.println("REPORT:");
 		for(TransactionReportRow row: report)
 			System.out.println(row);
 	}
 	
 	@Test
 	public void testGenerateReportOnAllWithdrawalTransactions() throws Exception
 	{
 		System.out.println("Number of reserve transactions: " + manager.countAllWithdrawalTransactions());
 		List<TransactionReportRow> report = manager.generateReportOnAllWithdrawalTransactions(0, 5);
 		Assert.assertTrue(report != null);
 		Assert.assertTrue(!report.isEmpty());
 		System.out.println("REPORT:");
 		for(TransactionReportRow row: report)
 			System.out.println(row);
 	}
 	
 	@Test
 	public void testGenerateReportOnAllLots() throws Exception
 	{
 		System.out.println("Balance Report on All Lots");
 		System.out.println("Number of lots: " + manager.countAllLots());
 		List<LotReportRow> report = manager.generateReportOnAllLots(0, 10);
 		Assert.assertTrue(report != null);
 		Assert.assertTrue(!report.isEmpty());
 		System.out.println("REPORT:");
 		for(LotReportRow row: report)
 			System.out.println(row);
 	}
 	
 	@Test
 	public void testGenerateReportsOnDormantLots() throws Exception
 	{
 		System.out.println("Balance Report on DORMANT Lots");
 		List<LotReportRow> report = manager.generateReportOnDormantLots(2012, 0, 10);
 		Assert.assertTrue(report != null);
 		Assert.assertTrue(!report.isEmpty());
 		System.out.println("REPORT:");
 		for(LotReportRow row: report)
 			System.out.println(row);
 	}
 	
 	@Test
 	public void testGenerateReportOnLotsByEntityType() throws Exception
 	{
 		System.out.println("Balance Report on Lots by Entity Type: GERMPLSM");
 		List<LotReportRow> report = manager.generateReportOnLotsByEntityType("GERMPLSM", 0, 10);
 		Assert.assertTrue(report != null);
 		Assert.assertTrue(!report.isEmpty());
 		System.out.println("REPORT:");
 		for(LotReportRow row: report)
 			System.out.println(row);
 	}
 	
 	@Test
 	public void testGenerateReportOnLotsByEntityTypeAndEntityId() throws Exception
 	{
 		System.out.println("Balance Report on Lots by Entity Type and Entity ID:");
 		List<Integer> entityIdList = new ArrayList<Integer>();
 		entityIdList.add(50533);
 		entityIdList.add(3);
 		List<LotReportRow> report = manager.generateReportOnLotsByEntityTypeAndEntityId("GERMPLSM", entityIdList, 0, 10);
 		Assert.assertTrue(report != null);
 		Assert.assertTrue(!report.isEmpty());
 		System.out.println("REPORT:");
 		for(LotReportRow row: report)
 			System.out.println(row);
 	}
 
 	
 	@Test
 	public void testGenerateReportOnEmptyLot()  throws Exception
 	{
 		System.out.println("Report on empty lot");
 		List<LotReportRow> report=manager.generateReportOnEmptyLot(0, 2);
 		Assert.assertTrue(report != null);
		Assert.assertTrue(!report.isEmpty());
 		System.out.println("REPORT:");
 		for(LotReportRow row: report)
 			System.out.println(row);
 	}
 	
 	
 	@Test
 	public void testGenerateReportOnLotWithMinimumAmount()  throws Exception
 	{
 		System.out.println("Report on lot with minimum balance");
 		List<LotReportRow> report=manager.generateReportOnLotWithMinimumAmount(200, 0, 1);
 		Assert.assertTrue(report != null);
 		Assert.assertTrue(!report.isEmpty());
 		System.out.println("REPORT:");
 		for(LotReportRow row: report)
 			System.out.println(row);
 	}
 	
 	
 	@AfterClass
 	public static void tearDown() throws Exception
 	{
 		factory.close();
 	}
 }
