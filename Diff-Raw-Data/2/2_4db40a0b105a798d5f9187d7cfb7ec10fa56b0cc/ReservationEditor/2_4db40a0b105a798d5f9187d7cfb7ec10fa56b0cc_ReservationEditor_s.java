 package blue.hotel.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import blue.hotel.logic.CalculateReservation;
 import blue.hotel.model.Customer;
 import blue.hotel.model.Reservation;
 import blue.hotel.model.Room;
 import blue.hotel.model.RoomReservation;
 import blue.hotel.storage.DAO;
 import blue.hotel.storage.DAOException;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.toedter.calendar.JDateChooser;
 
 @SuppressWarnings("serial")
 public class ReservationEditor extends JDialog implements Editor<Reservation>{
 	private JDateChooser arrivalDateField;
 	private JDateChooser departureDateField;
 	private boolean accepted = false;
 	private JButton btnRemoveCustomer;
 	private DefaultListModel customerListModel;
 	private JList customerList;
 	private JSpinner priceSpinner;
 	private JSpinner discountSpinner;
 	private JComboBox customerBox;
 	private JComboBox roomBox;
 	private JSpinner adultSpinner;
 	private JSpinner kidSpinner;
 	private List<Customer> customers;
 	private List<Room> rooms;
 	private DefaultListModel roomReservationListModel;
 	private JList roomReservationList;
 	private JButton btnRemoveRoomReservation;
 	private int reservationId = -1;
 	
 	public ReservationEditor(Reservation r) {
 		this();
 		readFrom(r);
 	}
 
 	public ReservationEditor() {
 		setModalityType(ModalityType.APPLICATION_MODAL);
 		setTitle("new Reservation");
 		setSize(400, 600);
 		setLocationRelativeTo(null);
 		getContentPane().setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel_editor = new JPanel();
 		panel_editor.setBorder(new EmptyBorder(0, 0, 0, 0));
 		getContentPane().add(panel_editor);
 		panel_editor.setLayout(new GridLayout(0, 1, 0, 0));
 		
 		JPanel customerPanel = new JPanel();
 		customerPanel.setBorder(new TitledBorder(null, "Customer", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
 		panel_editor.add(customerPanel);
 		
 		customerListModel = new DefaultListModel();
 		customerPanel.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("15px"),
 				ColumnSpec.decode("344px"),},
 			new RowSpec[] {
 				FormFactory.PARAGRAPH_GAP_ROWSPEC,
 				RowSpec.decode("30px"),
 				RowSpec.decode("33px"),}));
 		customerList = new JList(customerListModel);
 		customerList.setVisibleRowCount(3);
 		customerList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		customerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		customerPanel.add(customerList, "2, 2, left, fill");
 		
 		JPanel customerButtonPanel = new JPanel();
 		customerButtonPanel.setBorder(null);
 		customerPanel.add(customerButtonPanel, "2, 3, center, top");
 		customerButtonPanel.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("218px"),
 				ColumnSpec.decode("51px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("71px"),},
 			new RowSpec[] {
 				FormFactory.LINE_GAP_ROWSPEC,
 				RowSpec.decode("23px"),}));
 		
 		JButton btnAddCustomer = new JButton("Add");
 		customerButtonPanel.add(btnAddCustomer, "2, 2, left, top");
 		
 		btnRemoveCustomer = new JButton("Remove");
 		btnRemoveCustomer.setEnabled(false);
 		customerButtonPanel.add(btnRemoveCustomer, "4, 2, right, top");
 		
 		customerBox = new JComboBox();
 		customerPanel.add(customerBox, "2, 2, fill, top");
 		
 		JPanel roomPanel = new JPanel();
 		roomPanel.setBorder(new TitledBorder(null, "Room", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
 		panel_editor.add(roomPanel);
 		
 		roomReservationListModel = new DefaultListModel();
 		roomPanel.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("15px"),
 				ColumnSpec.decode("344px"),},
 			new RowSpec[] {
 				FormFactory.NARROW_LINE_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.LINE_GAP_ROWSPEC,
 				RowSpec.decode("46px"),
 				RowSpec.decode("30px"),}));
 		
 		roomBox = new JComboBox();
 		roomPanel.add(roomBox, "2, 2, fill, default");
 		roomReservationList = new JList(roomReservationListModel);
 		roomPanel.add(roomReservationList, "2, 4, left, fill");
 		
 		JPanel roomReservationPanel = new JPanel();
 		roomPanel.add(roomReservationPanel, "2, 4, fill, fill");
 		roomReservationPanel.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("150px"),
 				ColumnSpec.decode("left:195px"),
 				FormFactory.RELATED_GAP_COLSPEC,},
 			new RowSpec[] {
 				RowSpec.decode("26px"),
 				RowSpec.decode("26px"),}));
 		
		JLabel lblAduldts = new JLabel("Aduldt(s):");
 		roomReservationPanel.add(lblAduldts, "1, 1, left, top");
 		
 		adultSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
 		roomReservationPanel.add(adultSpinner, "2, 1, fill, top");
 		
 		JLabel lblKids = new JLabel("Kid(s):");
 		roomReservationPanel.add(lblKids, "1, 2, left, top");
 		
 		kidSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
 		roomReservationPanel.add(kidSpinner, "2, 2, fill, top");
 		
 		JPanel roomButtonPanel = new JPanel();
 		roomPanel.add(roomButtonPanel, "2, 5, right, top");
 		roomButtonPanel.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("51px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("71px"),},
 			new RowSpec[] {
 				FormFactory.LINE_GAP_ROWSPEC,
 				RowSpec.decode("23px"),}));
 		
 		JButton btnAddRoomReservation = new JButton("Add");
 		roomButtonPanel.add(btnAddRoomReservation, "2, 2, left, top");
 		
 		btnRemoveRoomReservation = new JButton("Remove");
 		btnRemoveRoomReservation.setEnabled(false);
 		roomButtonPanel.add(btnRemoveRoomReservation, "4, 2, left, top");
 		
 		JPanel stayPanel = new JPanel();
 		stayPanel.setBorder(new TitledBorder(null, "Stay", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
 		panel_editor.add(stayPanel);
 		stayPanel.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("15px"),
 				ColumnSpec.decode("150px"),
 				ColumnSpec.decode("195px"),},
 			new RowSpec[] {
 				RowSpec.decode("12dlu"),
 				RowSpec.decode("30px"),
 				RowSpec.decode("30px"),}));
 		
 		JLabel lblArrival = new JLabel("Arrival:");
 		stayPanel.add(lblArrival, "2, 2, left, top");
 		
 		Calendar cal = Calendar.getInstance();
 		Date now = new Date();
 		cal.setTime(now);
 		arrivalDateField = new JDateChooser(cal.getTime());
 		stayPanel.add(arrivalDateField, "3, 2, fill, top");
 		
 		JLabel lblDeparture = new JLabel("Departure:");
 		stayPanel.add(lblDeparture, "2, 3, left, top");
 		
 		cal.add(Calendar.DAY_OF_YEAR, 1);
 		departureDateField = new JDateChooser(cal.getTime());
 		stayPanel.add(departureDateField, "3, 3, fill, top");
 		
 		JPanel billPanel = new JPanel();
 		billPanel.setBorder(new TitledBorder(null, "Bill", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_editor.add(billPanel);
 		billPanel.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("10dlu"),
 				ColumnSpec.decode("150px"),
 				ColumnSpec.decode("195px"),},
 			new RowSpec[] {
 				RowSpec.decode("12dlu"),
 				RowSpec.decode("30px"),
 				RowSpec.decode("30px"),}));
 		
 		JLabel lblPrice = new JLabel("Price:");
 		billPanel.add(lblPrice, "2, 2, left, top");
 		
 		priceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 0.5));
 		billPanel.add(priceSpinner, "3, 2, fill, top");
 		
 		JLabel lblDiscount = new JLabel("Discount:");
 		billPanel.add(lblDiscount, "2, 3, left, top");
 		
 		discountSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 0.5));
 		billPanel.add(discountSpinner, "3, 3, fill, top");
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
 		getContentPane().add(panel, BorderLayout.SOUTH);
 		
 		JButton btnSave = new JButton("Save");
 		btnSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (ValidationHandler.validate(ReservationEditor.this)) {
 					ReservationEditor.this.accepted = true;
 					ReservationEditor.this.setVisible(false);
 				}
 			}
 		});
 		panel.add(btnSave);
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				ReservationEditor.this.accepted = false;
 				ReservationEditor.this.setVisible(false);
 			}
 		});
 		panel.add(btnCancel);
 		
 		DAO dao = DAO.getInstance();
 		try {
 			customers = dao.getAll(Customer.class);
 			for (Customer c : customers){
 				customerBox.addItem(c);
 			}
 			rooms = dao.getAll(Room.class);
 			for (Room r : rooms){
 				roomBox.addItem(r);
 			}
 		} catch (DAOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		btnAddCustomer.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Customer tmp = (Customer) customerBox.getSelectedItem();
 				boolean notInList = true;
 				for (int i=0; i<customerListModel.getSize(); i++){
 					if (customerListModel.get(i).equals(tmp)){
 						notInList = false;
 					}
 				}
 				if (notInList){
 					customerListModel.addElement(tmp);
 					calculate();
 				}
 			}
 		});
 		
 		btnRemoveCustomer.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(customerList.getSelectedIndex() != -1){
 					customerListModel.remove(customerList.getSelectedIndex());
 					calculate();
 				}
 			}
 		});
 		
 		customerList.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if (customerListModel.getSize() > 0){
 					btnRemoveCustomer.setEnabled(true);
 				} else{
 					btnRemoveCustomer.setEnabled(false);
 				}
 			}
 		});
 		
 		btnAddRoomReservation.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				int amount = (Integer)adultSpinner.getValue() + (Integer)kidSpinner.getValue();
 				Room room = (Room)roomBox.getSelectedItem();
 				if(room.getMaxPersons() >= amount){
 					addRoomReservation();
 					calculate();
 				} else{
 					JOptionPane.showConfirmDialog(null, "Too many persons in one room!", "Error", JOptionPane.CLOSED_OPTION, JOptionPane.WARNING_MESSAGE);
 					adultSpinner.setValue(1.0);
 					kidSpinner.setValue(0.0);
 				}
 			}
 		});
 		
 		btnRemoveRoomReservation.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (roomReservationList.getSelectedIndex() != -1){
 					removeRoomReservation();
 					calculate();
 				}
 			}
 		});
 		
 		roomReservationList.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if (roomReservationListModel.getSize() > 0){
 					btnRemoveRoomReservation.setEnabled(true);
 					if (roomReservationList.getSelectedIndex() != -1){
 						setRoomReservationFields();
 					}
 				} else{
 					btnRemoveRoomReservation.setEnabled(false);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void readFrom(Reservation o) {
 		setTitle("Edit reservation:" + o);
 		reservationId = o.getId();
 		for (Customer c: o.getCustomers()){
 			customerListModel.addElement(c);
 		}
 		priceSpinner.setValue(o.getPrice());
 		discountSpinner.setValue(o.getDiscount());
 		arrivalDateField.setDate(o.getArrival());
 		departureDateField.setDate(o.getDeparture());
 		
 		for(RoomReservation rr : o.getRooms()){
 			roomReservationListModel.addElement(rr);
 		}
 	}
 
 	@Override
 	public void writeTo(Reservation o) {
 		List<Customer> tmpList = new LinkedList<Customer>();
 		for (int i=0; i<customerListModel.getSize(); i++){
 			tmpList.add((Customer) customerListModel.get(i));
 		}
 		o.setCustomers(tmpList);
 		o.setPrice((Double) priceSpinner.getValue());
 		o.setDiscount((Double) discountSpinner.getValue());
 		o.setArrival(arrivalDateField.getDate());
 		o.setDeparture(departureDateField.getDate());
 		
 		try {
 			if (reservationId == -1){
 				DAO dao = DAO.getInstance();
 				o = dao.create(o);
 				System.out.println(o.getId());
 				
 				List<RoomReservation> rr = new ArrayList<RoomReservation>();
 				for (int i=0; i<roomReservationListModel.getSize(); i++){
 					RoomReservation tmp = (RoomReservation)roomReservationListModel.get(i);
 					if(tmp.getReservation() == null){
 						tmp.setReservation(o);
 						tmp = dao.create(tmp);
 					} else{
 						tmp.setReservation(o);
 						tmp = dao.update(tmp);
 					}
 					rr.add(tmp);		
 				}
 				
 				o.setRooms(rr);
 			}
 		} catch (DAOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public boolean run() {
 		accepted = false;
 		setVisible(true);
 		return accepted;
 	}
 	
 	private void addRoomReservation(){
 		
 		RoomReservation rr = new RoomReservation();
 		rr.setKids((Integer)kidSpinner.getValue());
 		rr.setAdults((Integer)adultSpinner.getValue());
 		rr.setRoom((Room) roomBox.getSelectedItem());
 		
 		int id = -1;
 		for (int i=0; i<roomReservationListModel.getSize(); i++){
 			RoomReservation tmp = (RoomReservation)roomReservationListModel.get(i);
 			if(tmp.getRoom().equals(rr.getRoom())){
 				id = i;
 			}
 		}
 		
 		if (id == -1){
 			roomReservationListModel.addElement(rr);
 		} else {
 			roomReservationListModel.remove(id);
 			roomReservationListModel.add(id, rr);
 		}
 		
 		adultSpinner.setValue((Integer)1);
 		kidSpinner.setValue((Integer)0);
 	}
 	
 	private void removeRoomReservation(){
 		roomReservationListModel.remove(roomReservationList.getSelectedIndex());
 	}
 	
 	private void setRoomReservationFields(){
 		RoomReservation rr = (RoomReservation)roomReservationListModel.get(roomReservationList.getSelectedIndex());
 		roomBox.setSelectedItem(rr.getRoom());
 		adultSpinner.setValue(rr.getAdults());
 		kidSpinner.setValue(rr.getKids());
 	}
 	
 	private void calculate(){
 		if((Integer)adultSpinner.getValue() < customerListModel.getSize()){
 			adultSpinner.setValue(customerListModel.getSize());
 		}
 		
 		if (customerListModel.getSize() > 0 &&
 			roomBox.getSelectedIndex() != -1 &&
 			(Integer)adultSpinner.getValue() >0){
 			
 			List<RoomReservation> rr = new ArrayList<RoomReservation>();
 			for (int i=0; i<roomReservationListModel.getSize(); i++){
 				rr.add((RoomReservation)roomReservationListModel.get(i));
 			}
 			
 			priceSpinner.setValue(CalculateReservation.calcualtePrice(rr, arrivalDateField.getDate(), departureDateField.getDate()));
 			
 			List<Customer> c = new ArrayList<Customer>();
 			for (int i=0; i<customerListModel.getSize(); i++){
 				c.add((Customer)customerListModel.get(i));
 			}
 			discountSpinner.setValue(CalculateReservation.calcualteDiscount(c));
 		}
 		else{
 			priceSpinner.setValue(0.0);
 			discountSpinner.setValue(0.0);
 		}
 		
 	}
 
 	@Override
 	public boolean validateInput() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public String inputErrors() {
 		// TODO Auto-generated method stub
 		return "Input validation unimplemented";
 	}
 
 }
