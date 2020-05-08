 package edu.brown.cs32.atian.crassus.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.border.EtchedBorder;
 import javax.swing.table.TableColumn;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.gui.indicatorwindows.EventWindow;
 import edu.brown.cs32.atian.crassus.gui.indicatorwindows.EventWindowFrame;
 import edu.brown.cs32.atian.crassus.indicators.Indicator;
 
 @SuppressWarnings("serial")
 public class CrassusIndicatorTablePane extends JPanel {
 
 	public class NewIndicatorListener implements WindowCloseListener {
 		@Override public void windowClosedWithEvent(Indicator ind) {
 			model.addIndicator(ind);
 		}
 		@Override public void windowClosedWithCancel() {}
 	}
 
 	public class PlusButtonListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent arg0) {
 			@SuppressWarnings("unused")
 			EventWindow eventWindow = new EventWindowFrame(_frame, new NewIndicatorListener(), _stock);
 		}
 	}
 	
 	public class CtrlIAction extends AbstractAction {
 		@Override public void actionPerformed(ActionEvent e) {
 			@SuppressWarnings("unused")
 			EventWindow eventWindow = new EventWindowFrame(_frame, new NewIndicatorListener(), _stock);
 		}
 	}
 
 	public class MinusButtonListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent arg0) {
 			removeSelectedIndicator();
 		}
 	}
 
 	public class CtrlShiftIAction extends AbstractAction {
 		@Override public void actionPerformed(ActionEvent e) {
 			removeSelectedIndicator();
 		}
 	}
 
 	private JTable table;
 	private CrassusIndicatorTableModel model;
 	CrassusIndicatorTableRenderer renderer;
 	private CrassusIndicatorTableEditor editor;
 	private JFrame _frame;
 	private Stock _stock;
 
 	public CrassusIndicatorTablePane(JFrame frame){
 		_frame = frame;
 		
 		this.setBackground(Color.WHITE);
 		this.setLayout(new BorderLayout());
 		
 		table = new JTable();
 		table.setBackground(Color.WHITE);
 		table.setTableHeader(null);//Disable table header
 		
 		model = new CrassusIndicatorTableModel();
 		table.setModel(model);
 		table.setShowGrid(false);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//allow only one row to be selected at a time
 		table.setFillsViewportHeight(true);//makes extra space below table entries white
 		
 		table.setRowHeight(22);
 		table.setIntercellSpacing(new Dimension(0,2));
 		
 		editor = new CrassusIndicatorTableEditor();
 		renderer = new CrassusIndicatorTableRenderer();
 		
 		for(int i=0; i<3; i++){
 			TableColumn column = table.getColumnModel().getColumn(i);
 			int colWidth = (i==2) ? 120 : 20;
 			column.setPreferredWidth(colWidth);
 			column.setMaxWidth(colWidth);
 			column.setResizable(false);
 			
 			column.setCellRenderer(renderer);
 			if(i<2)
 				column.setCellEditor(editor);
 		}
 		
 		JScrollPane scrollPane = new JScrollPane(table);
 		scrollPane.setBackground(Color.WHITE);
 		scrollPane.setBorder(BorderFactory.createEmptyBorder(10,20,0,0));//right border (0) taken care of by increased table size (to deal with scroll-bar)
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scrollPane.setPreferredSize(new Dimension(200,250));
 		
 		JLabel title = new JLabel("Indicators",JLabel.CENTER);
 		title.setFont(new Font("SansSerif",Font.BOLD,18));
 		this.add(title, BorderLayout.NORTH);
 		
 		this.add(scrollPane, BorderLayout.CENTER);
 		
 		this.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createEmptyBorder(20,20,20,20),
 				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
 		
 
 		JButton addButton = new JButton("+");
 		addButton.addActionListener(new PlusButtonListener());
 		addButton.setToolTipText("add new indicator");
 		addButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_I,InputEvent.CTRL_DOWN_MASK),
 				"CTRL I");
 		addButton.getActionMap().put("CTRL I", new CtrlIAction());
 		
 		JButton removeButton = new JButton("-");
 		removeButton.addActionListener(new MinusButtonListener());
 		removeButton.setToolTipText("remove selected indicator");
 		removeButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_I,InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK), 
 				"CTRL SHIFT I");
 		removeButton.getActionMap().put("CTRL SHIFT I", new CtrlShiftIAction());
 		
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
 	
 	public void removeSelectedIndicator() {
 		if(table.getRowCount()==0)
 			return;
 		
 		int index = table.getSelectedRow();
 		if(table.getRowCount()>1){
 			if(index == table.getRowCount()-1)
 				table.setRowSelectionInterval(index-1,index-1);
 			else
 				table.setRowSelectionInterval(index+1,index+1);
 		}
 		model.removeIndicator(index);
 	}
 
 	public void changeToStock(Stock stock){
 		_stock = stock;
		table.clearSelection();
 		model.changeToStock(stock);
 		renderer.changeToStock(stock);
 		editor.changeToStock(stock);
 	}
 }
