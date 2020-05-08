 package Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.LinkedList;
 
 import javax.swing.JFrame;
 import javax.swing.JTable;
 
 import gui.ChooseStrategy;
import gui.CopyOfOrderbookTable;
 import gui.OrderbookTableNew;
 
 import Selecting_Algothrim.*;
 
 import org.junit.Test;
 public class TestMain {
 	@Test
 	public void testAddTrade () {
 		newMomentum testStrategy = new newMomentum();
 		testStrategy.addTrade(2.1);
 		testStrategy.addTrade(2.1);
 		testStrategy.addTrade(2.2);
 		testStrategy.addTrade(2.3);
 		testStrategy.addTrade(2.3);
 		testStrategy.addTrade(2.32);
 		testStrategy.addTrade(2.34);
 		testStrategy.addTrade(2.52);
 		testStrategy.addTrade(2.55);
 		testStrategy.addTrade(2.7);
 		double epsilon = 0.01;
 		assertEquals("Testing for adding 10 trades and correctly calculating the average",2.133  , testStrategy.getAverage(), epsilon);	
 		testStrategy.addTrade(2.8);
 		assertEquals("Testing for 11th trade added, and correctly ignoring the 1st trade",2.413  , testStrategy.getAverage(), epsilon);	
 	}
 	
 	@Test
 	public void testSignal () {
 		newMomentum testStrategy = new newMomentum();
 		signalObject tempSignal;
 		orderObject lastSale = new orderObject( -1, -1);
 		orderObject lastBuy = new orderObject( -1, -1);
 		assertEquals("Default signal is nothing", "nothing", testStrategy.generateOrderSignal(lastSale, lastBuy).getType());
 		testStrategy.addTrade(2.1);
 		testStrategy.addTrade(2.1);
 		testStrategy.addTrade(2.2);
 		testStrategy.addTrade(2.3);
 		testStrategy.addTrade(2.3);
 		testStrategy.addTrade(2.32);
 		testStrategy.addTrade(2.34);
 		testStrategy.addTrade(2.52);
 		testStrategy.addTrade(2.55);
 		testStrategy.addTrade(2.7);
 		
 		testStrategy.addTrade(1.1);
 		testStrategy.addTrade(1.1);
 		testStrategy.addTrade(1.2);
 		testStrategy.addTrade(1.3);
 		testStrategy.addTrade(1.3);
 		testStrategy.addTrade(1.32);
 		testStrategy.addTrade(1.34);
 		testStrategy.addTrade(1.52);
 		testStrategy.addTrade(1.55);
 		testStrategy.addTrade(1.7);
 		tempSignal = testStrategy.generateOrderSignal(lastSale, lastBuy);
 		//generate a new average < lastaverage
 		
 		//testStrategy.generateOrderSignal(lastSale, lastBuy);
 		//System.out.println(tempSignal.getType());
 		assertEquals("Computing buy signal", "buy", tempSignal.getType());
 		//System.out.println("Generating a sell signal with Trades..");
 		lastSale = new orderObject(10, 1.7);
 		testStrategy.addTrade(2.1);
 		testStrategy.addTrade(2.1);
 		testStrategy.addTrade(2.2);
 		testStrategy.addTrade(2.3);
 		testStrategy.addTrade(2.3);
 		testStrategy.addTrade(2.32);
 		testStrategy.addTrade(2.34);
 		testStrategy.addTrade(2.52);
 		testStrategy.addTrade(2.55);
 		testStrategy.addTrade(2.7);
 	
 		tempSignal = testStrategy.generateOrderSignal(lastSale, lastBuy);
 		//generate a new average > lastaverage
 		//System.out.println(tempSignal.getType());
 		assertEquals("Computing sell signal", "nothing", testStrategy.generateOrderSignal(lastSale, lastBuy).getType());
 	}
 	@Test
 	public void testShareQuantity() {
 		newMomentum testStrategy = new newMomentum();
 		assertTrue("Default share qty is zero", testStrategy.isShareQuantityZero());
 		// not sure how finishShare works
 	}
 	@Test
 	public void testReceipt () {
 		newMomentum testStrategy = new newMomentum();
 		assertEquals("Default number of Buy Receipts", 0, testStrategy.getBuyReceipt().size());
 		assertEquals("Default number of Sell Receipts", 0, testStrategy.getSellReceipt().size());
 		testStrategy.getreceiptNumber(1);
 		//Default signal is in buy mode
 		//First transaction should be a sale
 		assertEquals("Added 1 sell receipt", 1, testStrategy.getSellReceipt().size());
 	}
 	@Test
 	public void reTestAddTrade () {
 		reverseMomentum testStrategy = new reverseMomentum();
 		testStrategy.addTrade(2.1);
 		testStrategy.addTrade(2.1);
 		testStrategy.addTrade(2.2);
 		testStrategy.addTrade(2.3);
 		testStrategy.addTrade(2.3);
 		testStrategy.addTrade(2.32);
 		testStrategy.addTrade(2.34);
 		testStrategy.addTrade(2.52);
 		testStrategy.addTrade(2.55);
 		testStrategy.addTrade(2.7);
 		double epsilon = 0.01;
 		assertEquals("Testing for adding 10 trades and correctly calculating the average",2.133  , testStrategy.getAverage(), epsilon);	
 		testStrategy.addTrade(2.8);
 		assertEquals("Testing for 11th trade added, and correctly ignoring the 1st trade",2.413  , testStrategy.getAverage(), epsilon);	
 	}
 	
 	@Test
 	public void reTestShareQuantity() {
 		reverseMomentum testStrategy = new reverseMomentum();
 		assertTrue("Default share qty is zero", testStrategy.isShareQuantityZero());
 	}
 	@Test
 	
 	public void reTestReceipt () {
 		reverseMomentum testStrategy = new reverseMomentum();
 		assertEquals("Default number of Buy Receipts", 0, testStrategy.getBuyReceipt().size());
 		assertEquals("Default number of Sell Receipts", 0, testStrategy.getSellReceipt().size());
 		testStrategy.getreceiptNumber(1);
 		//Default signal is in buy mode
 		//First transaction should be a sale
 		assertEquals("Added 1 sell receipt", 1, testStrategy.getSellReceipt().size());
 	}
 	//GUI TESTS
 	@Test
 	//fix
 	public void testOrderbookTable(){
		CopyOfOrderbookTable testTableModel = new CopyOfOrderbookTable();
 		JTable testTable = new JTable();
 		testTable.setModel(testTableModel);
 		 //{"BidID", "AskId", "Price", "Volume", "Timestamp"};
 		Object[] testInput= {new Long(100), new Long(100), new Double(34), new Integer(13), "time"};
 		testTableModel.addElement(testInput);
 		assertEquals("Column 1 is BidID","BidID", testTableModel.getColumnName(0));
 		assertEquals("Column 2 is BidID","AskId", testTableModel.getColumnName(1));
 		assertEquals("Column 3 is BidID","Price", testTableModel.getColumnName(2));
 		assertEquals("Column 4 is BidID","Volume", testTableModel.getColumnName(3));
 		assertEquals("Column 5 is BidID","Timestamp", testTableModel.getColumnName(4));
 		
 		assertEquals("Insert 1 row of data",1, testTableModel.getRowCount());	
 		assertEquals("Column Count is 5", 5, testTableModel.getColumnCount());
 		
 		testTableModel.setValueAt(new Long(99), 0, 0);
 		assertEquals("Testing setValueAt", new Long(99), testTableModel.getValueAt(0,0));
 		testTableModel.addElement(testInput);
 		assertEquals("Testing addElement for second line",2, testTableModel.getRowCount());
 		testTableModel.setValueAt(100, 0, 3);
 		assertEquals("Testing getValueAt for changed value in column 4", 100, testTableModel.getValueAt(0,3));
 		testTableModel.addElement(testInput);
 		assertEquals("Testing for Row count", 3, testTableModel.getRowCount());
 		
 		LinkedList<Object[]> testDataSet = new LinkedList<Object[]>();
 		testDataSet.add(testInput);
 		testDataSet.add(testInput);
 		testDataSet.add(testInput);
 		testDataSet.add(testInput);
 		testDataSet.add(testInput);
 		
 		testTableModel.setData(testDataSet);
 		assertEquals("Updated new dataset", 5, testTableModel.getRowCount());		
 	}
 	//@Test
 	//to test, uncomment @Test, and enter 5 in threshold when dialog appears
 	public void testChooseStrategyDialog () {
 		JFrame testFrame = new JFrame();
 		ChooseStrategy testDialog = new ChooseStrategy(testFrame);
 		testDialog.setVisible(false);
 		testDialog.dispose();
 		assertEquals("Testing default values of output values", 5,testDialog.getThreshold() );
 		assertEquals("Testing default values of output values", "Momentum",testDialog.getStrategy() );
 	}
 	
 	
 }
