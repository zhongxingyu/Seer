 package edu.brown.cs32.atian.crassus.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.border.EtchedBorder;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import edu.brown.cs32.atian.crassus.backend.Indicator;
 
 public class CrassusEventTablePane extends JPanel {
 
 	public class NewIndicatorListener implements WindowCloseListener {
 		@Override public void windowClosedWithEvent(Indicator ind) {
 			model.addIndicator(ind);
 		}
 		@Override public void windowClosedWithCancel() {}
 	}
 
 	public class NewEventListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent arg0) {
			EventWindow eventWindow = new EventWindowFrame();
 			eventWindow.setWindowCloseListener(new NewIndicatorListener());
 		}
 	}
 	
 	CrassusEventTableModel model;
 
	public CrassusEventTablePane(){
 		this.setBackground(Color.WHITE);
 		this.setLayout(new BorderLayout());
 		
 		JTable table = new JTable();
 		table.setBackground(Color.WHITE);
 		table.setTableHeader(null);//Disable table header
 		
 		model = new CrassusEventTableModel();
 		table.setModel(model);
 		table.setShowGrid(false);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//allow only one row to be selected at a time
 		table.setFillsViewportHeight(true);//makes extra space below table entries white
 		
 		for(int i=0; i<3; i++){
 			TableColumn column = table.getColumnModel().getColumn(i);
 			int colWidth = (i==2) ? 120 : 20;
 			column.setPreferredWidth(colWidth);
 			column.setMaxWidth(colWidth);
 			column.setMinWidth(colWidth);
 			
 			/*
 			if(i==0){
 				column.setCellRenderer(new CrassusEventTableCellEyeRenderer());
 				column.setCellEditor(new CrassusEventTableCellEyeEditor());
 			}
 			else*/ if(i==2){
 				DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
 				renderer.setHorizontalAlignment(SwingConstants.CENTER);
 				column.setCellRenderer(renderer);
 			}
 		}
 		
 		JScrollPane scrollPane = new JScrollPane(table);
 		scrollPane.setBackground(Color.WHITE);
 		scrollPane.setBorder(BorderFactory.createEmptyBorder(10,20,0,0));//right border (0) taken care of by increased table size (to deal with scroll-bar)
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scrollPane.setPreferredSize(new Dimension(200,250));
 		
 		JLabel title = new JLabel("Events",JLabel.CENTER);
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
 		
 		addButton.addActionListener(new NewEventListener());
 		
 		JPanel buttonAndLine = new JPanel();
 		buttonAndLine.setLayout(new BoxLayout(buttonAndLine,BoxLayout.Y_AXIS));
 		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
 		sep.setBackground(Color.GRAY);
 		buttonAndLine.add(sep);
 		buttonAndLine.add(buttonHolder);
 		
 		this.add(buttonAndLine, BorderLayout.SOUTH);
 	}
 }
