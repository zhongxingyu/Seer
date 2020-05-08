 package edu.brown.cs32.atian.crassus.gui.mainwindow.table.stock;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.regex.Pattern;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableColumn;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.backend.StockList;
 import edu.brown.cs32.atian.crassus.gui.CrassusButton;
 import edu.brown.cs32.atian.crassus.gui.dialogs.TickerDialog;
 import edu.brown.cs32.atian.crassus.gui.dialogs.TickerDialogCloseListener;
 import edu.brown.cs32.atian.crassus.gui.mainwindow.CrassusChangeStockListener;
 import edu.brown.cs32.atian.crassus.gui.mainwindow.table.CrassusTableRowSelector;
 import edu.brown.cs32.atian.crassus.gui.mainwindow.table.SelectUndoable;
 import edu.brown.cs32.atian.crassus.gui.undoable.UndoableStack;
 
 @SuppressWarnings("serial")
 public class CrassusStockTablePane extends JPanel {
 
 	private class ChangeStockListenerForwarder implements ListSelectionListener {
 		private Stock lastStock;
 		int lastIndex = -1;
 		@Override 
 		public void valueChanged(ListSelectionEvent e) {
 			if(listener!=null){
 				int index = table.getSelectedRow();
 				if(index==-1){
 					if(lastStock!=null)
 						listener.changeToStock(null);
 					lastStock = null;
 					lastIndex = index;
 				}
 				else{
 					Stock nextStock = model.getStock(table.getSelectedRow());
 					if(lastStock!=nextStock){
 						listener.changeToStock(nextStock);
 						if(selector.shouldRegisterSelection())
 							undoables.push(new SelectUndoable(lastIndex, index, selector));
 					}
 					lastStock = nextStock;
 					lastIndex = index;
 				}
 			}
 		}
 	}
 
 	private static final Pattern p =  Pattern.compile("[^a-zA-Z0-9]");
 	private class NewTickerListener implements TickerDialogCloseListener {
 		@Override public void tickerDialogClosedWithTicker(String symbol) {
 			addTicker(symbol);
 		}
 	}
 
 	private class PlusButtonListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent arg0) {
 			showNewTickerDialog();
 		}
 	}
 
 	private class MinusButtonListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent arg0) {
 			removeSelectedTicker();
 		}
 	}
 
 	private JFrame frame;
 	private JTable table;
 	private CrassusStockTableModel model;
 	private CrassusStockTableRenderer renderer;
 	
 	private CrassusChangeStockListener listener;
 	private CrassusTableRowSelector selector;
 	
 	private UndoableStack undoables;
 	
 	private StockList stocks;
 	
 	public CrassusStockTablePane(JFrame frame, UndoableStack undoables){
 		this.frame = frame;
 		this.undoables = undoables;
 		
 		this.setBackground(Color.WHITE);
 		this.setLayout(new BorderLayout());
 		
 		table = new JTable();
 		table.setBackground(Color.WHITE);
 		table.getTableHeader().setBackground(Color.WHITE);
 		table.getTableHeader().setReorderingAllowed(false);
 		table.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
 		
 		selector = new CrassusTableRowSelector(table);
 		model = new CrassusStockTableModel(selector);
 		table.setModel(model);
 		
 		table.setShowGrid(false);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//allow only one row to be selected at a time
 		table.setFillsViewportHeight(true);//makes extra space below table entries white
 		table.setRowHeight(22);
 		
 		table.setIntercellSpacing(new Dimension(0,2));
 		
 		renderer =  new CrassusStockTableRenderer();
 		
 		for(int i=0; i<5; i++){
 			TableColumn column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(40);
			column.setMaxWidth(40);
 			column.setResizable(false);
 			
 			column.setCellRenderer(renderer);
 		}
 		
 		table.getSelectionModel().addListSelectionListener(new ChangeStockListenerForwarder());
 		
 		JScrollPane scrollPane = new JScrollPane(table);
 		scrollPane.setBackground(Color.WHITE);
 		scrollPane.setBorder(BorderFactory.createEmptyBorder(10,20,0,0));//right border (0) taken care of by increased table size (to deal with scroll-bar)
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(240,250));
 		
 		JLabel title = new JLabel("Tickers",JLabel.CENTER);
 		title.setFont(new Font("SansSerif",Font.BOLD,18));
 		this.add(title, BorderLayout.NORTH);
 		
 		this.add(scrollPane, BorderLayout.CENTER);
 		
 		this.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createEmptyBorder(15,15,15,15),
 				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
 		
 		JButton addButton = new CrassusButton("+");
 		addButton.addActionListener(new PlusButtonListener());
 		addButton.setToolTipText("<html>add new ticker<br>CTRL +</html>");
 		
 		JButton removeButton = new CrassusButton("-");
 		removeButton.addActionListener(new MinusButtonListener());
 		removeButton.setToolTipText("<html>remove selected ticker<br>CTRL SHIFT -</html>");
 		
 		JPanel buttonHolder = new JPanel();
 		buttonHolder.setBackground(Color.WHITE);
 		buttonHolder.setLayout(new FlowLayout());
 		buttonHolder.add(addButton);
 		buttonHolder.add(removeButton);
 		
 		
 		JPanel buttonAndLine = new JPanel();
 		buttonAndLine.setLayout(new BoxLayout(buttonAndLine,BoxLayout.Y_AXIS));
 		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
 		sep.setBackground(Color.GRAY);
 		buttonAndLine.add(sep);
 		buttonAndLine.add(buttonHolder);
 		
 		this.add(buttonAndLine, BorderLayout.SOUTH);
 	}
 	
 
 	public void showNewTickerDialog() {
 		TickerDialog tickerFrame = new TickerDialog(frame, stocks);
 		tickerFrame.setTickerDialogCloseListener(new NewTickerListener());
 		tickerFrame.setVisible(true);
 	}
 
 	public void addTicker(String symbol) {
 		try{
 			if(p.matcher(symbol).find())
 				throw new IllegalArgumentException();
 			symbol = symbol.toUpperCase();
 			
 			for(Stock other: stocks.getStockList()){
 				if(symbol.equals(other.getTicker())){
 					JOptionPane.showMessageDialog(frame, "\'"+symbol+"\' is already in your ticker-table");
 					int index = stocks.getStockList().indexOf(other);
 					table.setRowSelectionInterval(index,index);
 					return;
 				}
 			}
 			
 			Stock stock = stocks.createStock(symbol);
 			int previousIndex = table.getSelectedRow();
 			model.addLastStock(stock);
 			undoables.push(new AddStockUndoable(model, stock, previousIndex, selector));
 			//table.setRowSelectionInterval(model.getRowCount()-1, model.getRowCount()-1);
 			
 		}catch(IllegalArgumentException e){
 			JOptionPane.showMessageDialog(frame,"\'"+symbol+"\' is not a valid ticker symbol");
 		}
 	}
 
 	public void removeSelectedTicker() {
 		int index = table.getSelectedRow();
 		Stock stock = model.removeStock(index);
 		undoables.push(new RemoveStockUndoable(model, stock, index, selector));
 	}
 
 	public void setChangeStockListener(CrassusChangeStockListener listener){
 		this.listener = listener;
 	}
 
 	public boolean refresh() {
 		return model.refresh();
 	}
 	
 	public void changeStockListTo(StockList stocks) {
 		this.stocks = stocks;
 		renderer.changeStockListTo(stocks);
 		model.changeStockListTo(stocks);
 		selector.select(0);
 	}
 
 
 	public void refreshActiveStock() {
 		Stock stock = model.getStock(table.getSelectedRow());
 		if(stock!=null)
 			stock.refresh();
 	}
 
 
 }
