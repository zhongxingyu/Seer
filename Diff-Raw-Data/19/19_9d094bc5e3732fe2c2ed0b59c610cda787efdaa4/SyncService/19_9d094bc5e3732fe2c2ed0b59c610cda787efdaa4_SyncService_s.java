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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.alexlg.bankit.dao.CostDao;
 import org.alexlg.bankit.dao.OperationDao;
 import org.alexlg.bankit.db.Cost;
 import org.alexlg.bankit.db.Operation;
 import org.joda.time.LocalDate;
 import org.joda.time.Months;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Service used for synchronization between costs, operation
  * and imported operations.
  * 
  * @author Alexandre Thomazo
  */
 @Controller
 public class SyncService {
 
 	/** 
 	 * Name of the option which stores the last execution date 
 	 * of materializeCostsIntoOperation.
 	 */
 	public static final String COST_SYNC_OPT = "costSync";
 	
 	/**
 	 * Name of the option which stores the last execution date
 	 * of readQifAndInsertOp.
 	 */
 	public static final String OP_SYNC_OPT = "opSync";
 	
 	@Autowired
 	private CostDao costDao;
 	
 	@Autowired
 	private OperationDao operationDao;
 	
 	@Autowired
 	private OptionsService optionsService;
 	
 	/** Class logger */
 	private static Logger logger = LoggerFactory.getLogger(SyncService.class);
 	
 	/** Date used for testing */
 	private LocalDate endSyncDate = null;
 
 	/**
 	 * This function take all the costs between
 	 * the last execution to create the associated
 	 * operations in the operation list.
 	 * It runs every day at midnight and 5 seconds
 	 */
 	@Transactional
 	@Scheduled(cron="5 0 0 * * *")
 	public void materializeCostsIntoOperation() {
 		logger.info("Starting materializeCostsIntoOperation");
 		
 		//materialize operations 2 days beyond current date
 		LocalDate endDate = getEndSyncDate().plusDays(2);
 		
 		Date startSync = optionsService.getDate(COST_SYNC_OPT);
 		if (startSync == null) {
 			optionsService.set(COST_SYNC_OPT, endDate.toDate());
 			return;
 		}
 		LocalDate startDate = new LocalDate(startSync);
 		
 		//retrieve costs list
 		List<Cost> costs = null;
 		int nbMonth = 1;
 		
 		if (endDate.getYear() == startDate.getYear()
 				&& endDate.getMonthOfYear() == startDate.getMonthOfYear()) {
 			//only retrieve the costs between this 2 "days"
 			costs = costDao.getList(startDate.getDayOfMonth(), endDate.getDayOfMonth());
 		} else {
 			//getting all costs
 			costs = costDao.getList();
 			
 			//we generate a least for the current month (as nbMonth = 1)
 			//then we add whole months between start and end.
 			nbMonth += Months.monthsBetween(startDate, endDate).getMonths();
 			
 			//as monthsBetween calculate for whole month, if start is for example the 24-09
 			//and the end is the 02-10, monthsBetween will return 0 but we need to have
 			//2 loops, one for september and one for november so we add a month.
 			if (endDate.getDayOfMonth() <= startDate.getDayOfMonth()) {
 				nbMonth++;
 			}
 		}
 		
 		//going through each month and each cost to create the operation
 		for (int m = 0 ; m < nbMonth ; m++) {
 			LocalDate curMonth = startDate.plusMonths(m);
 			int lastDayOfMonth = curMonth.dayOfMonth().getMaximumValue();
 			
 			for (Cost cost : costs) {
 				int costDay = cost.getDay();
 				
 				//if the operation is planned after the last day of month
 				//set it to the last day
 				if (costDay > lastDayOfMonth) costDay = lastDayOfMonth;
 				
 				//creating operation date
 				LocalDate opDate = new LocalDate(curMonth.getYear(), curMonth.getMonthOfYear(), costDay);
 				
 				//check if date is in the date interval before creating op
 				if (opDate.isAfter(startDate) && opDate.compareTo(endDate) <= 0) {
 					Operation op = new Operation();
 					op.setOperationDate(opDate.toDate());
 					op.setLabel(cost.getLabel());
 					op.setPlanned(cost.getAmount());
 					operationDao.save(op);
 				}
 			}
 		}
 		
 		optionsService.set(COST_SYNC_OPT, endDate.toDate());
 		
 	}
 
 	/**
 	 * Try to merge all old planned operation with no amount
 	 * with actual operations. It will try to match on the planned
 	 * amount then the label.
 	 */
 	public void mergeOldPlannedOps() {
 		//retrieve all old planned ops
 		LocalDate endDate = getEndSyncDate();
 		Calendar endCal = new GregorianCalendar();
 		endCal.setTime(endDate.toDate());
 		List<Operation> oldOps = operationDao.getOldPlannedOps(endCal);
 		
 		for (Operation oldOp : oldOps) {
 			//checking if a matching operation exists
 			Operation matchedOp = operationDao.matchRealOp(oldOp);
 			
 			if (matchedOp != null) {
 				matchedOp.setPlanned(oldOp.getPlanned());
 				operationDao.delete(oldOp);
 			}
 		}
 	}
 	
 	/**
 	 * Read an inputstream with QIF data and create the
 	 * corresponding operations in the database if after OP_SYNC_OPT date.
 	 * @param qif QIF to read
 	 * @return List of operations id inserted
 	 * @throws IOException If the reading of the file failed
 	 */
 	public List<Integer> readQifAndInsertOp(InputStream qif) throws IOException {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
 		
 		BufferedReader reader = new BufferedReader(new InputStreamReader(qif, "cp1252"));
 		String line = null; //current line in file read
 		Operation curOp = new Operation(); //current operation to fill in
 		int lineNum = 0; //current line number of the file
 		List<Operation> ops = new LinkedList<Operation>();
 		
 		while((line = reader.readLine()) != null) {
 			lineNum++;
 			//ignore empty line
 			if (line.length() == 0) continue;
 			
 			//extract first char
 			String firstChar = line.substring(0, 1);
 			line = line.substring(1, line.length());
 			
 			if (firstChar.equals("D")) {
 				try {
 					Date date = dateFormat.parse(line);
 					curOp.setOperationDate(date);
 				} catch (ParseException e) {
 					throw new IOException("Invalid date [" + line + "] on line " + lineNum, e);
 				}
 				
 			} else if (firstChar.equals("T")) {
 				String number = line.replace(",", "");
 				try {
 					BigDecimal amount = new BigDecimal(number);
 					curOp.setAmount(amount);
 				} catch (NumberFormatException e) {
 					throw new IOException("Invalid number [" + number + "] on line " + lineNum, e);
 				}
 				
 			} else if (firstChar.equals("P") || firstChar.equals("M")) {
				if (curOp.getLabel() == null && line.trim().length() > 0) {
 					//remove all double space
					curOp.setLabel(line.replaceAll("( )+", " ").trim());
 				}
 				
 			} else if (firstChar.equals("^")) {
 				ops.add(curOp);
 				curOp = new Operation();
 			}
 		}
 		
 		//syncing all read operations
 		syncOpList(ops);
 		
 		//getting ids
 		List<Integer> ids = new LinkedList<Integer>();
 		for (Operation op : ops) {
 			int opId = op.getOperationId();
 			if (opId != 0) {
 				ids.add(opId);
 			}
 		}
 
 		return ids;
 	}
 	
 	/**
 	 * Sync a list of operation with current ones.
 	 * @param operations Operations to sync.
 	 */
 	public void syncOpList(List<Operation> operations) {
 		if (operations == null) return;
 		Date startSync = optionsService.getDate(OP_SYNC_OPT);
 		Date maxDate = null; //older operation date
 		
 		for (Operation op : operations) {
 			Date opDate = op.getOperationDate();
 			
 			if (startSync == null || opDate.after(startSync)) {
 				operationDao.insert(op);
 				
 				//checking if operation if after maxDate
 				if (maxDate == null || opDate.after(maxDate)) maxDate = opDate;
 			}
 		}
 		
 		//setting last execution
 		if (maxDate != null) optionsService.set(OP_SYNC_OPT, maxDate);
 	}
 	
 	/**
 	 * Get the date used for the end of the synchronization,
 	 * for now the current date. Mostly for mocking in test.
 	 * @return The date of end sync
 	 */
 	protected LocalDate getEndSyncDate() {
 		if (endSyncDate != null) return endSyncDate;
 		return new LocalDate();
 	}
 
 	protected void setEndSyncDate(LocalDate endSyncDate) {
 		this.endSyncDate = endSyncDate;
 	}
 	
 }
