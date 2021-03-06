 package org.marketcetera.photon.model;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import junit.framework.TestCase;
 
 import org.marketcetera.core.AccessViolator;
 import org.marketcetera.core.AccountID;
 import org.marketcetera.core.InternalID;
 import org.marketcetera.core.MSymbol;
 import org.marketcetera.photon.core.FIXMessageHistory;
 import org.marketcetera.photon.core.IncomingMessageHolder;
 import org.marketcetera.photon.core.MessageHolder;
 import org.marketcetera.photon.core.OutgoingMessageHolder;
 import org.marketcetera.quickfix.FIXMessageUtil;
 
 import quickfix.FieldNotFound;
 import quickfix.Message;
 import quickfix.field.Account;
 import quickfix.field.AvgPx;
 import quickfix.field.ClOrdID;
 import quickfix.field.CumQty;
 import quickfix.field.ExecID;
 import quickfix.field.ExecTransType;
 import quickfix.field.ExecType;
 import quickfix.field.LastPx;
 import quickfix.field.LastQty;
 import quickfix.field.LastShares;
 import quickfix.field.LeavesQty;
 import quickfix.field.MsgType;
 import quickfix.field.OrdStatus;
 import quickfix.field.OrdType;
 import quickfix.field.OrderID;
 import quickfix.field.OrderQty;
 import quickfix.field.Price;
 import quickfix.field.SendingTime;
 import quickfix.field.Side;
 import quickfix.field.Symbol;
 import quickfix.field.TimeInForce;
 import quickfix.field.TransactTime;
 import quickfix.fix42.ExecutionReport;
 import quickfix.fix42.NewOrderSingle;
 import ca.odell.glazedlists.EventList;
 import ca.odell.glazedlists.event.ListEvent;
 import ca.odell.glazedlists.event.ListEventListener;
 
 public class FIXMessageHistoryTest extends TestCase {
 
 	
 	protected FIXMessageHistory getMessageHistory(){
 		return new FIXMessageHistory();
 	}
 
 	/*
 	 * Test method for 'org.marketcetera.photon.model.FIXMessageHistory.addIncomingMessage(Message)'
 	 */
 	public void testAddIncomingMessage() throws FieldNotFound {
 		FIXMessageHistory history = getMessageHistory();
 		InternalID orderID1 = new InternalID("1");
 		InternalID clOrderID1 = new InternalID("2");
 		String execID = "3";
 		char execTransType = ExecTransType.STATUS;
 		char execType = ExecType.PARTIAL_FILL;
 		char ordStatus = OrdStatus.PARTIALLY_FILLED;
 		char side = Side.SELL_SHORT;
 		BigDecimal orderQty = new BigDecimal(1000);
 		BigDecimal orderPrice = new BigDecimal(789);
 		BigDecimal lastQty = new BigDecimal(100);
 		BigDecimal lastPrice = new BigDecimal("12.3");
 		BigDecimal leavesQty = new BigDecimal(900);
 		BigDecimal cumQty = new BigDecimal(100);
 		BigDecimal avgPrice = new BigDecimal("12.3");
 		MSymbol symbol = new MSymbol("ASDF");
 		
 
 		Message message = FIXMessageUtil.newExecutionReport(orderID1, clOrderID1, execID, execTransType, execType, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, leavesQty, cumQty, avgPrice, symbol, null);
 
 		{
 			history.addIncomingMessage(message);
 			EventList<MessageHolder> historyList = history.getAllMessagesList();
 			assertEquals(1, historyList.size());
 			assertEquals(IncomingMessageHolder.class, historyList.get(0).getClass());
 			IncomingMessageHolder holder = (IncomingMessageHolder) historyList.get(0);
 			Message historyMessage = holder.getMessage();
 			assertEquals(orderID1.toString(), historyMessage.getString(OrderID.FIELD));
 			assertEquals(clOrderID1.toString(), historyMessage.getString(ClOrdID.FIELD));
 			assertEquals(execID, historyMessage.getString(ExecID.FIELD));
 			assertEquals(""+execTransType, historyMessage.getString(ExecTransType.FIELD));
 			assertEquals(""+execType, historyMessage.getString(ExecType.FIELD));
 			assertEquals(""+ordStatus, historyMessage.getString(OrdStatus.FIELD));
 			assertEquals(""+side, historyMessage.getString(Side.FIELD));
 			assertEquals(orderQty, new BigDecimal(historyMessage.getString(OrderQty.FIELD)));
 			assertEquals(lastQty, new BigDecimal(historyMessage.getString(LastShares.FIELD)));
 			assertEquals(lastPrice, new BigDecimal(historyMessage.getString(LastPx.FIELD)));
 			assertEquals(cumQty, new BigDecimal(historyMessage.getString(CumQty.FIELD)));
 			assertEquals(avgPrice, new BigDecimal(historyMessage.getString(AvgPx.FIELD)));
 			assertEquals(symbol.getFullSymbol(), historyMessage.getString(Symbol.FIELD));
 		}		
 
 		{
 			InternalID orderID2 = new InternalID("1001");
 			InternalID clOrderID2 = new InternalID("1002");
 			Message message2 = FIXMessageUtil.newExecutionReport(orderID2, clOrderID2, execID, execTransType, execType, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, leavesQty, cumQty, avgPrice, symbol, null);
 			history.addIncomingMessage(message2);
 			EventList<MessageHolder> historyList = history.getAllMessagesList();
 			assertEquals(2, historyList.size());
 			assertEquals(IncomingMessageHolder.class, historyList.get(1).getClass());
 			IncomingMessageHolder holder = (IncomingMessageHolder) historyList.get(1);
 			Message historyMessage = holder.getMessage();
 			assertEquals(orderID2.toString(), historyMessage.getString(OrderID.FIELD));
 			assertEquals(clOrderID2.toString(), historyMessage.getString(ClOrdID.FIELD));
 			assertEquals(execID, historyMessage.getString(ExecID.FIELD));
 			assertEquals(""+execTransType, historyMessage.getString(ExecTransType.FIELD));
 			assertEquals(""+execType, historyMessage.getString(ExecType.FIELD));
 			assertEquals(""+ordStatus, historyMessage.getString(OrdStatus.FIELD));
 			assertEquals(""+side, historyMessage.getString(Side.FIELD));
 			assertEquals(orderQty, new BigDecimal(historyMessage.getString(OrderQty.FIELD)));
 			assertEquals(lastQty, new BigDecimal(historyMessage.getString(LastShares.FIELD)));
 			assertEquals(lastPrice, new BigDecimal(historyMessage.getString(LastPx.FIELD)));
 			assertEquals(cumQty, new BigDecimal(historyMessage.getString(CumQty.FIELD)));
 			assertEquals(avgPrice, new BigDecimal(historyMessage.getString(AvgPx.FIELD)));
 			assertEquals(symbol.getFullSymbol(), historyMessage.getString(Symbol.FIELD));
 		}
 	}
 
 	/*
 	 * Test method for 'org.marketcetera.photon.model.FIXMessageHistory.addOutgoingMessage(Message)'
 	 */
 	public void testAddOutgoingMessage() throws FieldNotFound {
 		FIXMessageHistory history = getMessageHistory();
 		InternalID orderID = new InternalID("1");
 		char side = Side.SELL_SHORT_EXEMPT;
 		BigDecimal quantity = new BigDecimal("2000");
 		MSymbol symbol = new MSymbol("QWER");
 		char timeInForce = TimeInForce.DAY;
 		AccountID account = new AccountID("ACCT");
 		Message message = FIXMessageUtil.newMarketOrder(orderID, side, quantity, symbol, timeInForce, account);
 		history.addOutgoingMessage(message);
 
 		EventList<MessageHolder> historyList = history.getAllMessagesList();
 		assertEquals(1, historyList.size());
 		assertEquals(OutgoingMessageHolder.class, historyList.get(0).getClass());
 		OutgoingMessageHolder holder = (OutgoingMessageHolder) historyList.get(0);
 		Message historyMessage = holder.getMessage();
 		assertEquals(orderID.toString(), historyMessage.getString(ClOrdID.FIELD));
 		assertEquals(""+side, historyMessage.getString(Side.FIELD));
 		assertEquals(quantity, new BigDecimal(historyMessage.getString(OrderQty.FIELD)));
 		assertEquals(symbol.getFullSymbol(), historyMessage.getString(Symbol.FIELD));
 		assertEquals(""+timeInForce, historyMessage.getString(TimeInForce.FIELD));
 		assertEquals(account.toString(), historyMessage.getString(Account.FIELD));
 	}
 
 
 	/*
 	 * Test method for 'org.marketcetera.photon.model.FIXMessageHistory.getLatestExecutionReports()'
 	 */
 	public void testGetLatestExecutionReports() throws FieldNotFound {
 		long currentTime = System.currentTimeMillis();
 		FIXMessageHistory history = getMessageHistory();
 		Message order1 = FIXMessageUtil.newMarketOrder(new InternalID("1"), Side.BUY, new BigDecimal(1000), new MSymbol("ASDF"), TimeInForce.FILL_OR_KILL, new AccountID("ACCT"));
 		Message executionReportForOrder1 = FIXMessageUtil.newExecutionReport(new InternalID("1001"), new InternalID("1"), "2001", ExecTransType.NEW, ExecType.NEW, OrdStatus.NEW, Side.BUY, new BigDecimal(1000), new BigDecimal(789), null, null, new BigDecimal(1000), BigDecimal.ZERO, BigDecimal.ZERO, new MSymbol("ASDF"), null);
 		executionReportForOrder1.getHeader().setField(new SendingTime(new Date(currentTime - 10000)));
 		Message order2 = FIXMessageUtil.newLimitOrder(new InternalID("3"), Side.SELL, new BigDecimal(2000), new MSymbol("QWER"), new BigDecimal("12.3"), TimeInForce.DAY, new AccountID("ACCT"));
 		Message executionReportForOrder2 = FIXMessageUtil.newExecutionReport(new InternalID("1003"), new InternalID("3"), "2003", ExecTransType.NEW, ExecType.NEW, OrdStatus.NEW, Side.SELL, new BigDecimal(2000), new BigDecimal(789), null, null, new BigDecimal(2000), BigDecimal.ZERO, BigDecimal.ZERO, new MSymbol("QWER"), null);
 		executionReportForOrder2.getHeader().setField(new SendingTime(new Date(currentTime - 8000)));
 		Message secondExecutionReportForOrder1 = FIXMessageUtil.newExecutionReport(new InternalID("1001"), new InternalID("1"), "2004", ExecTransType.STATUS, ExecType.PARTIAL_FILL, OrdStatus.PARTIALLY_FILLED, Side.BUY, new BigDecimal(1000), new BigDecimal(789), new BigDecimal(100), new BigDecimal("11.5"), new BigDecimal(900), new BigDecimal(100), new BigDecimal("11.5"), new MSymbol("ASDF"), null);
 		secondExecutionReportForOrder1.getHeader().setField(new SendingTime(new Date(currentTime - 7000)));
 
 		history.addOutgoingMessage(order1);
 		history.addIncomingMessage(executionReportForOrder1);
 		history.addOutgoingMessage(order2);
 		history.addIncomingMessage(executionReportForOrder2);
 		history.addIncomingMessage(secondExecutionReportForOrder1);
 		
 		EventList<MessageHolder> historyList = history.getLatestExecutionReportsList();
 		assertEquals(2, historyList.size());
 		Message historyExecutionReportForOrder1 = historyList.get(0).getMessage();
 		Message historyExecutionReportForOrder2 = historyList.get(1).getMessage();
 
 		assertEquals("1001", historyExecutionReportForOrder1.getString(OrderID.FIELD));
 		assertEquals("2004", historyExecutionReportForOrder1.getString(ExecID.FIELD));
 		assertEquals(order1.getString(ClOrdID.FIELD), historyExecutionReportForOrder1.getString(ClOrdID.FIELD));
 		assertEquals(order1.getString(Side.FIELD), historyExecutionReportForOrder1.getString(Side.FIELD));
 		assertEquals(order1.getString(OrderQty.FIELD), historyExecutionReportForOrder1.getString(OrderQty.FIELD));
 		assertEquals(order1.getString(Symbol.FIELD), historyExecutionReportForOrder1.getString(Symbol.FIELD));
 
 		assertEquals("1003", historyExecutionReportForOrder2.getString(OrderID.FIELD));
 		assertEquals("2003", historyExecutionReportForOrder2.getString(ExecID.FIELD));
 		assertEquals(order2.getString(ClOrdID.FIELD), historyExecutionReportForOrder2.getString(ClOrdID.FIELD));
 		assertEquals(order2.getString(Side.FIELD), historyExecutionReportForOrder2.getString(Side.FIELD));
 		assertEquals(order2.getString(OrderQty.FIELD), historyExecutionReportForOrder2.getString(OrderQty.FIELD));
 		assertEquals(order2.getString(Symbol.FIELD), historyExecutionReportForOrder2.getString(Symbol.FIELD));
 }
 
 	/*
 	 * Test method for 'org.marketcetera.photon.model.FIXMessageHistory.addFIXMessageListener(IFIXMessageListener)'
 	 */
 	public void testAddFIXMessageListener() throws NoSuchFieldException, IllegalAccessException {
 		FIXMessageHistory history = getMessageHistory();
 		
 		Message order1 = FIXMessageUtil.newMarketOrder(new InternalID("1"), Side.BUY, new BigDecimal(1000), new MSymbol("ASDF"), TimeInForce.FILL_OR_KILL, new AccountID("ACCT"));
 		Message executionReportForOrder1 = FIXMessageUtil.newExecutionReport(new InternalID("1001"), new InternalID("1"), "2001", ExecTransType.NEW, ExecType.NEW, OrdStatus.NEW, Side.BUY, new BigDecimal(1000), new BigDecimal(789), null, null, new BigDecimal(1000), BigDecimal.ZERO, BigDecimal.ZERO, new MSymbol("ASDF"), null);
 
 		ListEventListener<MessageHolder> fixMessageListener = new ListEventListener<MessageHolder>() {
 			public int numIncomingMessages = 0;
 			public int numOutgoingMessages = 0;
 
 			@SuppressWarnings("unchecked")
 			public void listChanged(ListEvent<MessageHolder> event) {
 				if (event.hasNext())
 				{
 					event.next();
 					if (event.getType() == ListEvent.INSERT){
 						EventList<MessageHolder> source = (EventList<MessageHolder>) event.getSource();
 						int index = event.getIndex();
 						MessageHolder holder = source.get(index);
 						if (holder instanceof IncomingMessageHolder) {
 							IncomingMessageHolder incoming = (IncomingMessageHolder) holder;
 							try {
 								assertEquals("1001", incoming.getMessage().getString(OrderID.FIELD));
 								numIncomingMessages++;
 							} catch (FieldNotFound e) {
 								fail(e.getMessage());
 							}
 						} else if (holder instanceof OutgoingMessageHolder) {
 							OutgoingMessageHolder outgoing = (OutgoingMessageHolder) holder;
 							try {
 								assertEquals("1", outgoing.getMessage().getString(ClOrdID.FIELD));
 								numOutgoingMessages++;
 							} catch (FieldNotFound e) {
 								fail(e.getMessage());
 							}
 						}
 					}	
 				}
 			}
 			
 		};
 		history.getAllMessagesList().addListEventListener(fixMessageListener);
 		
 		history.addOutgoingMessage(order1);
 		history.addIncomingMessage(executionReportForOrder1);
 		//just use the AccessViolator to get the fields out of the anon inner class
 		AccessViolator violator = new AccessViolator(fixMessageListener.getClass());
 		assertEquals(1,violator.getField("numIncomingMessages", fixMessageListener));
 		assertEquals(1,violator.getField("numOutgoingMessages", fixMessageListener));
 	}
 
 	/*
 	 * Test method for 'org.marketcetera.photon.model.FIXMessageHistory.removePortfolioListener(IFIXMessageListener)'
 	 */
 	public void testRemovePortfolioListener() throws NoSuchFieldException, IllegalAccessException {
 		FIXMessageHistory history = getMessageHistory();
 		
 		Message order1 = FIXMessageUtil.newMarketOrder(new InternalID("1"), Side.BUY, new BigDecimal(1000), new MSymbol("ASDF"), TimeInForce.FILL_OR_KILL, new AccountID("ACCT"));
 		Message executionReportForOrder1 = FIXMessageUtil.newExecutionReport(new InternalID("1001"), new InternalID("1"), "2001", ExecTransType.NEW, ExecType.NEW, OrdStatus.NEW, Side.BUY, new BigDecimal(1000), new BigDecimal(789), null, null, new BigDecimal(1000), BigDecimal.ZERO, BigDecimal.ZERO, new MSymbol("ASDF"), null);
 
 		ListEventListener<MessageHolder> fixMessageListener = new ListEventListener<MessageHolder>() {
 			public int numIncomingMessages = 0;
 			public int numOutgoingMessages = 0;
 
 			public void listChanged(ListEvent<MessageHolder> event) {
 				if (event.getType() == ListEvent.INSERT){
 					Object source = event.getSource();
 					if (source instanceof IncomingMessageHolder) {
 						IncomingMessageHolder incoming = (IncomingMessageHolder) source;
 						try {
 							assertEquals("1001", incoming.getMessage().getString(OrderID.FIELD));
 							numIncomingMessages++;
 						} catch (FieldNotFound e) {
 							fail(e.getMessage());
 						}
 					} else if (source instanceof OutgoingMessageHolder) {
 						OutgoingMessageHolder outgoing = (OutgoingMessageHolder) source;
 						try {
 							assertEquals("1", outgoing.getMessage().getString(ClOrdID.FIELD));
 							numOutgoingMessages++;
 						} catch (FieldNotFound e) {
 							fail(e.getMessage());
 						}
 					}
 
 				}
 			}
 			
 		};
 		
 		history.getAllMessagesList().addListEventListener(fixMessageListener);
 		history.getAllMessagesList().removeListEventListener(fixMessageListener);
 		
 		history.addOutgoingMessage(order1);
 		history.addIncomingMessage(executionReportForOrder1);
 		//just use the AccessViolator to get the fields out of the anon inner class
 		AccessViolator violator = new AccessViolator(fixMessageListener.getClass());
 		assertEquals(0,violator.getField("numIncomingMessages", fixMessageListener));
 		assertEquals(0,violator.getField("numOutgoingMessages", fixMessageListener));
 	}
 	
 	
 	public void testAveragePriceList() throws Exception {
 		FIXMessageHistory messageHistory = getMessageHistory();
 		InternalID orderID1 = new InternalID("1");
 		InternalID clOrderID1 = new InternalID("2");
 		String execID = "300";
 		char execTransType = ExecTransType.STATUS;
 		char execType = ExecType.PARTIAL_FILL;
 		char ordStatus = OrdStatus.PARTIALLY_FILLED;
 		char side = Side.SELL_SHORT;
 		BigDecimal orderQty = new BigDecimal(1000);
 		BigDecimal orderPrice = new BigDecimal(789);
 		BigDecimal lastQty = new BigDecimal(100);
 		BigDecimal lastPrice = new BigDecimal("12.3");
 		BigDecimal leavesQty = new BigDecimal(900);
 		BigDecimal cumQty = new BigDecimal("100");
 		BigDecimal avgPrice = new BigDecimal("12.3");
 		MSymbol symbol = new MSymbol("ASDF");
 
 		Message message = FIXMessageUtil.newExecutionReport(orderID1, clOrderID1, execID, execTransType, execType, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, leavesQty, cumQty, avgPrice, symbol, null);
 		messageHistory.addIncomingMessage(message);
 		
 		orderID1 = new InternalID("3");
 		clOrderID1 = new InternalID("4");
 		execID = "301";
 		lastQty = new BigDecimal(900);
 		lastPrice = new BigDecimal("12.4");
 		cumQty = new BigDecimal(900);
 		avgPrice = new BigDecimal("12.4");
 
 		message = FIXMessageUtil.newExecutionReport(orderID1, clOrderID1, execID, execTransType, execType, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, leavesQty, cumQty, avgPrice, symbol, null);
 		messageHistory.addIncomingMessage(message);
 		
 		EventList<MessageHolder> averagePriceList = messageHistory.getAveragePricesList();
 
 		assertEquals(1, averagePriceList.size());
 		
 		IncomingMessageHolder holder = (IncomingMessageHolder) averagePriceList.get(0);
 		Message returnedMessage = holder.getMessage();
 		assertEquals(MsgType.EXECUTION_REPORT, returnedMessage.getHeader().getString(MsgType.FIELD));
 
 		BigDecimal returnedAvgPrice = new BigDecimal(returnedMessage.getString(AvgPx.FIELD));
 		assertTrue( new BigDecimal("1000").compareTo(new BigDecimal(returnedMessage.getString(CumQty.FIELD))) == 0);
 		assertEquals( ((12.3*100)+(12.4*900))/1000, returnedAvgPrice.doubleValue(), .0001);
 		
 		
 		orderID1 = new InternalID("4");
 		clOrderID1 = new InternalID("5");
 		execID = "302";
 		lastQty = new BigDecimal(900);
 		lastPrice = new BigDecimal("12.4");
 		cumQty = new BigDecimal(900);
 		avgPrice = new BigDecimal("12.4");
 		side = Side.BUY;
 		
 		message = FIXMessageUtil.newExecutionReport(orderID1, clOrderID1, execID, execTransType, execType, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, leavesQty, cumQty, avgPrice, symbol, null);
 		messageHistory.addIncomingMessage(message);
 
 		assertEquals(2, messageHistory.getAveragePricesList().size());
 		holder = (IncomingMessageHolder) averagePriceList.get(0);
 		returnedMessage = holder.getMessage();
 		assertEquals(MsgType.EXECUTION_REPORT, returnedMessage.getHeader().getString(MsgType.FIELD));
 
 		returnedAvgPrice = new BigDecimal(returnedMessage.getString(AvgPx.FIELD));
 		assertEquals(Side.BUY, returnedMessage.getChar(Side.FIELD));
 		assertEquals( 12.4, returnedAvgPrice.doubleValue(), .0001);
 		assertTrue( new BigDecimal("900").compareTo(new BigDecimal(returnedMessage.getString(CumQty.FIELD))) == 0);
 
 
 		
 		orderID1 = new InternalID("6");
 		clOrderID1 = new InternalID("7");
 		execID = "305";
 		lastQty = new BigDecimal(900);
 		lastPrice = new BigDecimal("12.4");
 		cumQty = new BigDecimal(900);
 		avgPrice = new BigDecimal("12.4");
 		side = Side.SELL_SHORT;
 		
 		message = FIXMessageUtil.newExecutionReport(orderID1, clOrderID1, execID, execTransType, execType, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, leavesQty, cumQty, avgPrice, symbol, null);
 		messageHistory.addIncomingMessage(message);
 
 		assertEquals(2, messageHistory.getAveragePricesList().size());
 		holder = (IncomingMessageHolder) averagePriceList.get(1);
 		returnedMessage = holder.getMessage();
 		assertEquals(MsgType.EXECUTION_REPORT, returnedMessage.getHeader().getString(MsgType.FIELD));
 
 		returnedAvgPrice = new BigDecimal(returnedMessage.getString(AvgPx.FIELD));
 		assertEquals(Side.SELL_SHORT, returnedMessage.getChar(Side.FIELD));
 		assertEquals( ((12.3*100)+(12.4*900)+(12.4*(900)))/1900, returnedAvgPrice.doubleValue(), .0001);
 		assertTrue( new BigDecimal("1900").compareTo(new BigDecimal(returnedMessage.getString(CumQty.FIELD))) == 0);
 
 	}
 
 	public void testExecutionReportOrder() throws FieldNotFound
 	{
 		InternalID orderID1 = new InternalID("1");
 		InternalID clOrderID1 = new InternalID("2");
 		String execID = "3";
 		char execTransType = ExecTransType.STATUS;
 		char execType = ExecType.PARTIAL_FILL;
 		char ordStatus = OrdStatus.PARTIALLY_FILLED;
 		char side = Side.SELL_SHORT;
 		BigDecimal orderQty = new BigDecimal(1000);
 		BigDecimal orderPrice = new BigDecimal(789);
 		BigDecimal lastQty = new BigDecimal(100);
 		BigDecimal lastPrice = new BigDecimal("12.3");
 		BigDecimal leavesQty = new BigDecimal(900);
 		BigDecimal cumQty = new BigDecimal(100);
 		BigDecimal avgPrice = new BigDecimal("12.3");
 		MSymbol symbol = new MSymbol("ASDF");
 
 		SendingTime stField = new SendingTime(new Date(10000000));
 		SendingTime stFieldLater = new SendingTime(new Date(10010000));
 		
 		Message message1 = FIXMessageUtil.newExecutionReport(null, clOrderID1, execID, execTransType, execType, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, leavesQty, cumQty, avgPrice, symbol, null);
 		message1.getHeader().setField(stField);
 		
 		lastQty = new BigDecimal(200);
 		Message message2 = FIXMessageUtil.newExecutionReport(orderID1, clOrderID1, execID, execTransType, execType, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, leavesQty, cumQty, avgPrice, symbol, null);
 		message2.getHeader().setField(stField);
 
 		lastQty = new BigDecimal(300);
 		Message message3 = FIXMessageUtil.newExecutionReport(orderID1, clOrderID1, execID, execTransType, execType, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, leavesQty, cumQty, avgPrice, symbol, null);
 		message3.getHeader().setField(stFieldLater);
 		
 		FIXMessageHistory history = getMessageHistory();
 		history.addIncomingMessage(message1);
 		history.addIncomingMessage(message2);
 		assertEquals(new BigDecimal(200), new BigDecimal(history.getLatestExecutionReport(clOrderID1.toString()).getString(LastQty.FIELD)));
 		assertEquals(orderID1.toString(), history.getLatestExecutionReport(clOrderID1.toString()).getString(OrderID.FIELD));
 		
 		// execution reports come in out of order, use the one that has the OrderID in it.
 		history = getMessageHistory();
 		history.addIncomingMessage(message2);
 		history.addIncomingMessage(message1);
 		assertEquals(new BigDecimal(200), new BigDecimal(history.getLatestExecutionReport(clOrderID1.toString()).getString(LastQty.FIELD)));
 		assertTrue(history.getLatestExecutionReport(clOrderID1.toString()).isSetField(OrderID.FIELD));
 
 		history = getMessageHistory();
 		history.addIncomingMessage(message1);
 		history.addIncomingMessage(message2);
 		history.addIncomingMessage(message3);
 		assertEquals(new BigDecimal(300), new BigDecimal(history.getLatestExecutionReport(clOrderID1.toString()).getString(LastQty.FIELD)));
 		assertEquals(orderID1.toString(), history.getLatestExecutionReport(clOrderID1.toString()).getString(OrderID.FIELD));
 		
 		history = getMessageHistory();
 		history.addIncomingMessage(message3);
 		history.addIncomingMessage(message2);
 		history.addIncomingMessage(message1);
 		assertEquals(new BigDecimal(300), new BigDecimal(history.getLatestExecutionReport(clOrderID1.toString()).getString(LastQty.FIELD)));
 		assertEquals(orderID1.toString(), history.getLatestExecutionReport(clOrderID1.toString()).getString(OrderID.FIELD));
 
 		history = getMessageHistory();
 		history.addIncomingMessage(message3);
 		history.addIncomingMessage(message1);
 		history.addIncomingMessage(message2);
 		assertEquals(new BigDecimal(300), new BigDecimal(history.getLatestExecutionReport(clOrderID1.toString()).getString(LastQty.FIELD)));
 		assertEquals(orderID1.toString(), history.getLatestExecutionReport(clOrderID1.toString()).getString(OrderID.FIELD));
 	}
 	
 	public void testStrandedOpenOrder() throws Exception {
 		Message m1 = new NewOrderSingle();
 		m1.setField(new TransactTime(new Date(2007,2,14,18,55,29))); m1.setField(new ClOrdID("1171508063701-server02/127.0.0.1")); m1.setField(new Side(Side.BUY)); m1.setField(new Symbol("R")); m1.setField(new OrderQty(10)); m1.setField(new OrdType(OrdType.LIMIT));  m1.setField(new Price(10)); 
 		Message m2 = new ExecutionReport();
 		m2.setField(new TransactTime(new Date(2007,2,14,18,54,29))); m2.setField(new ClOrdID("1171508063701-server02/127.0.0.1")); m2.setField(new OrdStatus(OrdStatus.NEW)); m2.setField(new Side(Side.BUY)); m2.setField(new Symbol("R")); m2.setField(new OrderQty(10)); m2.setField(new CumQty(0)); m2.setField(new LeavesQty(10));  m2.setField(new Price(10)); m2.setField(new AvgPx(0)); m2.setField(new LastShares(0)); m2.setField(new LastPx(0)); m2.setField(new ExecID("12037")); m2.setField(new OrderID("7324"));
 		Message m3 = new ExecutionReport();
 		m3.setField(new TransactTime(new Date(2007,2,14,18,55,29))); m3.setField(new ClOrdID("1171508063701-server02/127.0.0.1")); m3.setField(new OrdStatus(OrdStatus.FILLED)); m3.setField(new Side(Side.BUY)); m3.setField(new Symbol("R")); m3.setField(new OrderQty(10)); m3.setField(new CumQty(10)); m3.setField(new LeavesQty(0));  m3.setField(new Price(10)); m3.setField(new AvgPx(10)); m3.setField(new LastShares(10)); m3.setField(new LastPx(10)); m3.setField(new ExecID("12041")); m3.setField(new OrderID("7324"));
 		
 		FIXMessageHistory history = new FIXMessageHistory();
 		history.addOutgoingMessage(m1);
 		history.addIncomingMessage(m2);
 		history.addIncomingMessage(m3);
		assertEquals("12041", history.getOpenOrdersList().get(0).getMessage().getString(ExecID.FIELD));
 		assertEquals(0, history.getOpenOrdersList().size());
 	}
 }
