 package edu.brown.cs32.atian.crassus.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.backend.StockImpl;
 import edu.brown.cs32.atian.crassus.backend.StockList;
 
 public class CrassusStockTablePane extends JPanel {
 
 	public class ChangeStockListenerForwarder implements ListSelectionListener {
 		@Override public void valueChanged(ListSelectionEvent e) {
 			if(_listener!=null)
				_listener.changeToStock(model.getStock(table.getSelectedRow()));
 		}
 	}
 
 	public class NewTickerListener implements TickerDialogCloseListener {
 		@Override public void tickerDialogClosedWithTicker(String symbol) {
 			Stock stock = new StockImpl(symbol);
 			stock.refresh();
 			model.addStock(stock);
 		}
 	}
 
 	public class NewStockListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent arg0) {
 			TickerDialog tickerFrame = new TickerDialog(_frame);
 			tickerFrame.setTickerDialogCloseListener(new NewTickerListener());
 			tickerFrame.setVisible(true);
 		}
 	}
 	
 	public class RemoveStockListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent arg0) {
 			model.removeStock(table.getSelectedRow());
 		}
 	}
 
 	private JFrame _frame;
 	private JTable table;
 	private CrassusStockTableModel model;
 	private CrassusChangeStockListener _listener;
 	
 	public CrassusStockTablePane(JFrame frame, StockList stocks){
 		_frame = frame;
 		
 		this.setBackground(Color.WHITE);
 		this.setLayout(new BorderLayout());
 		
 		table = new JTable();
 		table.setBackground(Color.WHITE);
 		table.getTableHeader().setBackground(Color.WHITE);
 		table.getTableHeader().setReorderingAllowed(false);
 		table.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
 		
 		model = new CrassusStockTableModel(stocks);
 		table.setModel(model);
 		table.setShowGrid(false);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//allow only one row to be selected at a time
 		table.setFillsViewportHeight(true);//makes extra space below table entries white
 		
 		for(int i=0; i<5; i++){
 			TableColumn column = table.getColumnModel().getColumn(i);
 			
 			column.setPreferredWidth(50);
 			column.setMaxWidth(50);
 			column.setMinWidth(50);
 			
 			if(i==0){
 				DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
 				renderer.setHorizontalAlignment(SwingConstants.CENTER);
 				column.setCellRenderer(renderer);
 			}
 			else{
 				DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
 				renderer.setHorizontalAlignment(SwingConstants.RIGHT);
 				column.setCellRenderer(renderer);
 			}
 		}
 		
 		table.getSelectionModel().addListSelectionListener(new ChangeStockListenerForwarder());
 		
 		JScrollPane scrollPane = new JScrollPane(table);
 		scrollPane.setBackground(Color.WHITE);
 		scrollPane.setBorder(BorderFactory.createEmptyBorder(10,20,0,0));//right border (0) taken care of by increased table size (to deal with scroll-bar)
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scrollPane.setPreferredSize(new Dimension(290,250));
 		
 		JLabel title = new JLabel("Tickers",JLabel.CENTER);
 		title.setFont(new Font("SansSerif",Font.BOLD,18));
 		this.add(title, BorderLayout.NORTH);
 		
 		this.add(scrollPane, BorderLayout.CENTER);
 		
 		this.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createEmptyBorder(20,20,20,20),
 				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
 		
 		JButton addButton = new JButton("+");
 		JButton removeButton = new JButton("-");
 		
 		JPanel buttonHolder = new JPanel();
 		buttonHolder.setBackground(Color.WHITE);
 		buttonHolder.setLayout(new FlowLayout());
 		buttonHolder.add(addButton);
 		buttonHolder.add(removeButton);
 		
 		addButton.addActionListener(new NewStockListener());
 		removeButton.addActionListener(new RemoveStockListener());
 		
 		JPanel buttonAndLine = new JPanel();
 		buttonAndLine.setLayout(new BoxLayout(buttonAndLine,BoxLayout.Y_AXIS));
 		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
 		sep.setBackground(Color.GRAY);
 		buttonAndLine.add(sep);
 		buttonAndLine.add(buttonHolder);
 		
 		this.add(buttonAndLine, BorderLayout.SOUTH);
 	}
 	
 	public void setChangeStockListener(CrassusChangeStockListener listener){
 		_listener = listener;
 	}
 
 	public void refresh() {
 		model.refresh();
 	}
 	
 }
