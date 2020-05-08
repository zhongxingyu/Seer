 /*
  * Copyright (C) 2012 Alexandre Thomazo
  *
  * This file is part of BankIt.
  *
  * BankIt is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * BankIt is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with BankIt. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.alexlg.bankit.services;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.io.FileInputStream;
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.alexlg.bankit.dao.AbstractDaoTest;
 import org.alexlg.bankit.dao.OperationDao;
 import org.alexlg.bankit.db.Operation;
 import org.joda.time.LocalDate;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  * Test class for {@link SyncService}
  * 
  * @author Alexandre Thomazo
  */
 public class SyncServiceTest extends AbstractDaoTest {
 
 	@Autowired
 	private OptionsService optionsService;
 	
 	@Autowired
 	private SyncService syncService;
 	
 	@Autowired
 	private OperationDao operationDao;
 	
 	/**
 	 * Test the materialization of operations from cost
 	 * within the same month.
 	 */
 	@Test
 	public void testMaterializeCostsIntoOperationSameMonth() throws Exception {
 		LocalDate previousSyncDate = new LocalDate(2012, 9, 2);
 		LocalDate endTestDate = new LocalDate(2012, 9, 22);
 		optionsService.set(SyncService.COST_SYNC_OPT, previousSyncDate.toDate());
 		
 		//return a fixed date
 		syncService.setEndSyncDate(endTestDate);
 		
 		//calling method
 		syncService.materializeCostsIntoOperation();
 		
 		//checking operation created
 		GregorianCalendar calPrevSyncDate = new GregorianCalendar();
 		calPrevSyncDate.setTime(previousSyncDate.toDate());
 		List<Operation> ops = operationDao.getFuture(calPrevSyncDate);
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		assertEquals("ops size", 2, ops.size());
 		assertEquals("ops date", "2012-09-03", sdf.format(ops.get(0).getOperationDate()));
 		assertEquals("ops label", "PRLV Assurance Auto", ops.get(0).getLabel());
 		
 		assertEquals("ops date", "2012-09-24", sdf.format(ops.get(1).getOperationDate()));
 		assertEquals("ops label", "PRLV Free Mobile", ops.get(1).getLabel());
 	}
 	
 	/**
 	 * Test the materialization of operations from cost
 	 * between two month.
 	 */
 	@Test
 	public void testMaterializeCostsIntoOperationDifferentMonth() throws Exception {
 		LocalDate previousSyncDate = new LocalDate(2012, 9, 3);
 		LocalDate endTestDate = new LocalDate(2012, 10, 7);
 		optionsService.set(SyncService.COST_SYNC_OPT, previousSyncDate.toDate());
 		
 		//return a fixed date
 		syncService.setEndSyncDate(endTestDate);
 		
 		//calling method
 		syncService.materializeCostsIntoOperation();
 		
 		//checking operation created
 		GregorianCalendar calPrevSyncDate = new GregorianCalendar();
 		calPrevSyncDate.setTime(previousSyncDate.toDate());
 		List<Operation> ops = operationDao.getFuture(calPrevSyncDate);
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		assertEquals("ops size", 4, ops.size());
 		assertEquals("ops date", "2012-09-24", sdf.format(ops.get(0).getOperationDate()));
 		assertEquals("ops label", "PRLV Free Mobile", ops.get(0).getLabel());
 		
 		assertEquals("ops date", "2012-09-27", sdf.format(ops.get(1).getOperationDate()));
 		assertEquals("ops label", "VIR SALAIRE", ops.get(1).getLabel());
 		
 		assertEquals("ops date", "2012-10-01", sdf.format(ops.get(2).getOperationDate()));
 		assertEquals("ops label", "VIR LOYER", ops.get(2).getLabel());
 		
 		assertEquals("ops date", "2012-10-03", sdf.format(ops.get(3).getOperationDate()));
 		assertEquals("ops label", "PRLV Assurance Auto", ops.get(3).getLabel());
 	}
 	
 	@Test
 	public void testMergeOldPlannedOps() throws Exception {
 		LocalDate testDate = new LocalDate(2012, 8, 29);
 		
 		//return a fixed date
 		syncService.setEndSyncDate(testDate);
 		
 		//create 2 operation to match with the planned one already present in the database
 		Calendar day = Calendar.getInstance();
 		day.clear();
 		
 		//matches on planned amount (14)
 		Operation realOp1 = new Operation();
 		realOp1.setAmount(new BigDecimal("-200"));
 		day.set(2012, Calendar.JUNE, 12);
 		realOp1.setOperationDate(day.getTime());
 		realOp1.setLabel("Test Planned Op");
 		operationDao.insert(realOp1);
 		
 		//matches on label (12)
 		Operation realOp2 = new Operation();
 		realOp2.setAmount(new BigDecimal("-72.24"));
 		day.set(2012, Calendar.AUGUST, 29);
 		realOp2.setOperationDate(day.getTime());
 		realOp2.setLabel("Impots Revenu XYZ-AAA");
 		operationDao.insert(realOp2);
 		
 		//start matching
 		syncService.mergeOldPlannedOps();
 		
 		//check database state
 		assertNull("planned 1 matched not deleted", operationDao.get(14));
 		assertNull("planned 2 matched not deleted", operationDao.get(12));
 		assertEquals("real 1 planned amount not updated", new BigDecimal("-200.00"), 
 				operationDao.get(realOp1.getOperationId()).getPlanned());
 		assertEquals("real 2 planned amount not updated", new BigDecimal("-845.00"),
 				operationDao.get(realOp2.getOperationId()).getPlanned());
 	}
 	
 	@Test
 	public void testReadQifAndInsertOp() throws Exception {
 		//setting start option sync
 		LocalDate previousSyncDate = new LocalDate(2012, 10, 4);
 		optionsService.set(SyncService.OP_SYNC_OPT, previousSyncDate.toDate());
 		
 		String qifFile = SyncServiceTest.class.getResource("/releve.qif").getFile();
 		List<Integer> opList = syncService.readQifAndInsertOp(new FileInputStream(qifFile));
 		
 		//checking operation creation
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		
 		assertEquals("operation size list", 5, opList.size());
 		Operation op = operationDao.get(opList.get(0));
 		assertEquals("op label", "PRLV ASSUR AUTO", op.getLabel());
 		assertEquals("op amount", new BigDecimal("-65.20"), op.getAmount());
 		assertEquals("op date", "2012-10-06", sdf.format(op.getOperationDate()));
 		
 		//check if sync date correctly updated
 		Date startSync = optionsService.getDate(SyncService.OP_SYNC_OPT);
 		assertEquals("op date", "2012-10-23", sdf.format(startSync));
 	}
 	
 }
