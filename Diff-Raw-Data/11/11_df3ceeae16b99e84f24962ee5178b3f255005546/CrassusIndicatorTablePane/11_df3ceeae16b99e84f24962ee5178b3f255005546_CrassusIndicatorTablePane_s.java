 package edu.brown.cs32.atian.crassus.gui.mainwindow.table.indicator;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
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
 import edu.brown.cs32.atian.crassus.gui.CrassusButton;
 import edu.brown.cs32.atian.crassus.gui.WindowCloseListener;
 import edu.brown.cs32.atian.crassus.gui.indicatorwindows.EventWindowFrame;
 import edu.brown.cs32.atian.crassus.gui.mainwindow.CrassusStockWasAlteredListener;
 import edu.brown.cs32.atian.crassus.gui.mainwindow.table.CrassusTableRowSelector;
 import edu.brown.cs32.atian.crassus.gui.mainwindow.table.SelectUndoable;
 import edu.brown.cs32.atian.crassus.gui.undoable.UndoableStack;
 import edu.brown.cs32.atian.crassus.indicators.Indicator;
 
 @SuppressWarnings("serial")
 public class CrassusIndicatorTablePane extends JPanel {
 	
 	public class ChangeIndicatorListener implements ListSelectionListener {
 		
 		private int oldIndex=-1;
 		private Indicator oldIndicator = null;
 		
 		@Override 
 		public void valueChanged(ListSelectionEvent arg0) {
 			int index = table.getSelectedRow();
 			
 			Indicator indicator;
 			if(index==-1)
 				indicator = null;
 			else
 				indicator = stock.getEventList().get(index);
 
 			if(oldIndicator!=indicator && selector.shouldRegisterSelection()){
 				undoables.push(new SelectUndoable(oldIndex, index, selector));
 			}
 			if(stock!=null)
 				stock.setSelectedIndicatorIndex(index);
 			
 			
 			oldIndex = index;
 			oldIndicator=indicator;
 		}
 	}
 
 	public class NewIndicatorListener implements WindowCloseListener {
 		@Override public void windowClosedWithEvent(Indicator ind) {
 			addIndicator(ind);
 		}
 		@Override public void windowClosedWithCancel() {}
 	}
 
 	public class PlusButtonListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent arg0) {
 			showNewIndicatorDialog();
 		}
 	}
 	
 	public class MinusButtonListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent arg0) {
 			removeSelectedIndicator();
 		}
 	}
 
 	private JTable table;
 	private CrassusIndicatorTableModel model;
 	CrassusIndicatorTableRenderer renderer;
 	private CrassusIndicatorTableEditor editor;
 	
 	private CrassusTableRowSelector selector;
 	private UndoableStack undoables;
 	
 	private JFrame _frame;
 	private Stock stock;
 
 	public CrassusIndicatorTablePane(JFrame frame, UndoableStack undoables, CrassusStockWasAlteredListener listener){
 		_frame = frame;
 		this.undoables = undoables;
 		
 		this.setBackground(Color.WHITE);
 		this.setLayout(new BorderLayout());
 		
 		table = new CrassusIndicatorTable();
 		table.setBackground(Color.WHITE);
 		table.setTableHeader(null);//Disable table header
 		
 		selector = new CrassusTableRowSelector(table);
 		model = new CrassusIndicatorTableModel(selector, listener);
 		table.setModel(model);
 		table.setShowGrid(false);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//allow only one row to be selected at a time
 		table.setFillsViewportHeight(true);//makes extra space below table entries white
 		
 		table.setRowHeight(26);
 		table.setIntercellSpacing(new Dimension(0,2));
 		
 		editor = new CrassusIndicatorTableEditor(listener,undoables,model);
 		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
 		renderer = new CrassusIndicatorTableRenderer();
 		
 		for(int i=0; i<3; i++){
 			TableColumn column = table.getColumnModel().getColumn(i);
 			int colWidth = (i==2) ? 110 : 26;
 			column.setPreferredWidth(colWidth);
 			column.setMaxWidth(colWidth);
 			column.setResizable(false);
 			
 			column.setCellRenderer(renderer);
 			if(i<2)
 				column.setCellEditor(editor);
 		}
 		
 		table.getSelectionModel().addListSelectionListener(new ChangeIndicatorListener());
 		
 		table.addMouseListener(
 				new MouseListener(){
 
 					@Override
 					public void mouseClicked(MouseEvent e) {
						if(e.getClickCount()==2){
							System.out.println("double-click");
							showChangeIndicatorDialog();
						}
 					}
 
 					@Override public void mouseEntered(MouseEvent arg0) {}
 					@Override public void mouseExited(MouseEvent arg0) {}
 					@Override public void mousePressed(MouseEvent arg0) {}
 					@Override public void mouseReleased(MouseEvent arg0) {}
 				});
 		
 		JScrollPane scrollPane = new JScrollPane(table);
 		scrollPane.setBackground(Color.WHITE);
 		scrollPane.setBorder(BorderFactory.createEmptyBorder(10,20,0,0));//right border (0) taken care of by increased table size (to deal with scroll-bar)
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scrollPane.setPreferredSize(new Dimension(190,250));
 		
 		JLabel title = new JLabel("Indicators",JLabel.CENTER);
 		title.setFont(new Font("SansSerif",Font.BOLD,18));
 		this.add(title, BorderLayout.NORTH);
 		
 		this.add(scrollPane, BorderLayout.CENTER);
 		
 		this.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createEmptyBorder(10,10,10,10),
 				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
 		
 
 		JButton addButton = new CrassusButton("+");
 		addButton.addActionListener(new PlusButtonListener());
 		addButton.setToolTipText("<html>add new indicator<br>CTRL I</html>");
 		
 		JButton removeButton = new CrassusButton("-");
 		removeButton.addActionListener(new MinusButtonListener());
 		removeButton.setToolTipText("<html>remove selected indicator<br>CTRL SHIFT I</html>");
 		
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
 	
 	public void showNewIndicatorDialog() {
 		if(stock==null){
 			JOptionPane.showMessageDialog(_frame, "You must first have a stock ticker to use indicators.");
 		}
 		else{
 			EventWindowFrame eventWindow = new EventWindowFrame(_frame, new NewIndicatorListener(), stock);
 			eventWindow.display();
 		}
 	}
 	
 	public void showChangeIndicatorDialog() {
		if(stock==null){
 			return;
 		}
 		else{
 			int index = stock.getSelectedIndicatorIndex();
 			Indicator indicator = stock.getEventList().get(index);
 			EventWindowFrame eventWindow = new EventWindowFrame(
 					_frame,
 					new WindowCloseListener(){
 
 						@Override
 						public void windowClosedWithEvent(Indicator indicator) {
 							changeIndicator(indicator);
 						}
 
 						@Override public void windowClosedWithCancel() {}
 					},
 					stock,
 					indicator);
 			eventWindow.display();
 		}
 	}
 
 	protected void changeIndicator(Indicator ind) {
 		ind.setActive(true);
 		ind.setVisible(true);
 		int index = stock.getSelectedIndicatorIndex();
 		Indicator prevInd = model.changeIndicator(index,ind);
 		undoables.push(new ChangeIndicatorUndoable(model, index, prevInd, ind));
 	}
 
 	public void addIndicator(Indicator ind) {
 		
 		ind.setActive(true);
 		ind.setVisible(true);
 		int previousIndex = table.getSelectedRow();
 		model.addLastIndicator(ind);
 		undoables.push(new AddIndicatorUndoable(model, ind, previousIndex, selector));
 	}
 
 	public void removeSelectedIndicator() {
 		if(table.getRowCount()==0)
 			return;
 		
 		int index = table.getSelectedRow();
 		Indicator ind = model.removeIndicator(index);
 		editor.indicatorWasRemoved();
 		undoables.push(new RemoveIndicatorUndoable(model, ind, index, selector));
 	}
 
 	public void changeToStock(Stock stock){
 		
 		this.stock = stock;
 		
 		int indicatorIndex=0;
 		if(stock!=null)
 			indicatorIndex = stock.getSelectedIndicatorIndex();
 		
 		editor.changeToStock(stock);
 		model.changeToStock(stock);
 		renderer.changeToStock(stock);
 		
 		if(stock!=null)
 			selector.select(indicatorIndex);
 		
 	}
 
 	public void refresh() {
 		model.refresh();
 	}
 }
