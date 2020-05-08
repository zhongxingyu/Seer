 package vms.gui;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.table.*;
 
 import vms.Alert;
 import vms.Alert.AlertType;
 import vms.gui.MainGUI.UserIdentity;
 
 import common.Vessel;
 
 import java.awt.Component;
 import java.awt.Color;
 import java.util.*;
 
 public class TablePanel extends JPanel implements ActionListener {
 	
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = -7376989668658607570L;
 	final private String[] columnNames = {
     		"Vessel ID",
             "Type",
             "X Position",
             "Y Position",
             "Speed",
             "Course",
             "Distance",
             "Update Time",
             "Risk"
     };
 	
 	private class VesselTableModel extends AbstractTableModel {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -2611186118709966440L;
 		private List<Vessel> _Vessels = new ArrayList<Vessel>();
 		private List<Alert> _Alerts = new ArrayList<Alert>();
 		
 		public void setVessels(List<Vessel> vlist) {
 			_Vessels = vlist;
 		}
 		public void setAlerts(List<Alert> alist) {
 			_Alerts = alist;
 		}
 	    
 	    @Override
 	    public String getColumnName(int columnIndex) {
 	    	return columnNames[columnIndex];
 	    }
 
 		@Override
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 
 		@Override
 		public int getRowCount() {
 			return _Vessels.size();
 		}
 
 		@Override
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			Vessel v = _Vessels.get(rowIndex);
 			switch(columnIndex) {
 			case 0: return v.getId();
 			case 1: return v.getType();
 			case 2: return v.getCoord(Calendar.getInstance()).x();
 			case 3: return v.getCoord(Calendar.getInstance()).y();
 			case 4: return v.getSpeed(Calendar.getInstance());
			case 5: return "Long: " +v.getCourse(Calendar.getInstance()).xVel() + ", Lat: " + 
 			v.getCourse(Calendar.getInstance()).yVel();
 			case 6: return v.getDistance(Calendar.getInstance());
 			case 7: return v.getLastTimestamp().getTimeInMillis();
 			case 8: 
 				for(Alert a : _Alerts) {
 					if (a.contains(v)) return a.getType().toString();
 				}
 				return ""; //No alert for this ship
 			}
 			return "Unknown";
 		}
 		
 	};
 	private VesselTableModel _TableModel = new VesselTableModel();
 	private JComboBox _NameList;
 	private JComboBox _OrderList;
 	private JTable _Table;
 	private JScrollPane _ScrollPane;
 	private JPanel _OperatorPanel;
 
 	private int _OrderName = 0;
 	private int _OrderType = 0;
 	
     final private String[] orderTypeNames = {
     		"Ascending",
     		"Descending"
     };
     final private String[] orderListNames = {
     		"Vessel ID",
             "Type",
             "Speed",
             "Distance"
     };
 	JLabel[] label = {
 			new JLabel("Order by"),
 			new JLabel("Type order")
 	};
     
 	public TablePanel() {
 		RiskColor renderer = new RiskColor();
 		_Table = new JTable(_TableModel);
 		_Table.setPreferredScrollableViewportSize(new Dimension(1000, 440));
 		_Table.setOpaque(true);
 		_Table.setAutoCreateRowSorter(true);
 		for (int i=0; i<columnNames.length; i++) {
 			_Table.getColumn(columnNames[i]).setCellRenderer(renderer);
 		}
 		
 		//Create the scroll pane and add the table to it.
 		_ScrollPane = new JScrollPane(_Table);
 		add(_ScrollPane, BorderLayout.NORTH);
 		
 		_OperatorPanel = new JPanel();
 		_OperatorPanel.setLayout(new GridLayout(2, 4));
 		
 /*
 		JPanel empty1 = new JPanel();
 		JPanel empty2 = new JPanel();
 		
 		//Create Button to add
 		_NameList = new JComboBox(orderListNames);
 		_OrderList = new JComboBox(orderTypeNames);
 		_NameList.setSelectedIndex(0);
 		_NameList.addActionListener(this);
 		_OrderList.setSelectedIndex(0);
 		_OrderList.addActionListener(this);
 		_OperatorPanel.add(empty1);
 		_OperatorPanel.add(empty2);
 		_OperatorPanel.add(label[0]);
 		_OperatorPanel.add(label[1]);
 		_OperatorPanel.add(_NameList);
 		_OperatorPanel.add(_OrderList);
 		add(_OperatorPanel, BorderLayout.SOUTH);
 */		
 	}
 	
 	public void changeIdentity(UserIdentity identity) {
 		_OperatorPanel.setVisible(identity == UserIdentity.OPERATOR);
 	}
 	
 	public void update(final List<Alert> alerts, final List<Vessel> vessels) {
 		_TableModel.setAlerts(alerts);
 		_TableModel.setVessels(vessels);
 		_Table.updateUI();
 	}
 	
 	public void actionPerformed(ActionEvent arg0) {
 		if (arg0.getSource().equals(_NameList)) {
 			JComboBox cb = (JComboBox)arg0.getSource();
 			_OrderName = cb.getSelectedIndex();
 		}
 		else if (arg0.getSource().equals(_OrderList)) {
 			JComboBox cb = (JComboBox)arg0.getSource();
 			_OrderType = cb.getSelectedIndex();
 		}
 	}
 	
 	public List<Vessel> sort(List<Vessel> list) {
 		Collections.sort(list, new VesselComparator(_OrderName, _OrderType));
 		return list;
 	}
 }
 
 class RiskColor extends JLabel implements TableCellRenderer {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4637707447984335303L;
 
 	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
 			boolean hasFocus, int row, int column) {
 		setOpaque(true);
 		setText(value.toString());
 		
 		if (table.getValueAt(row, 8).toString().equals(AlertType.HIGHRISK.toString()))
 			setBackground(Color.RED);
 		else if (table.getValueAt(row, 8).toString().equals(AlertType.LOWRISK.toString()))
 			setBackground(Color.YELLOW);
 		else
 			setBackground(null);
 		
 		return this;
 	}
 }
 
 class VesselComparator implements Comparator<Vessel> {
 	private int _OrderName, _OrderType;
 	public VesselComparator(int oname, int otype) {
 		_OrderName = oname;
 		_OrderType = otype;
 	}
 
 	@Override
 	public int compare(Vessel arg0, Vessel arg1) {
 		int res = 0;
 		Calendar now = Calendar.getInstance();
 		switch (_OrderName) {
 		case 0: 
 			res = arg0.getId().compareTo(arg1.getId());
 			break;
 		case 1:
 			res = arg0.getType().toString().compareTo(arg1.getType().toString());
 			break;
 		case 2:
 			res = ((Double)arg0.getSpeed(now)).compareTo(arg1.getSpeed(now));
 			break;
 		case 3:
 			res = ((Double)arg0.getDistance(now)).compareTo(arg1.getDistance(now));
 			break;
 		
 		}
 		if (_OrderType == 1) {
 			//Reverse order
 			res = 0 - res;
 		}
 		return res;
 	}
 	
 }
 
