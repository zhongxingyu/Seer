 package gui;
 
 import businessLogic.ApplicationFacadeInterface;
 
 import com.toedter.calendar.JCalendar;
 
 import domain.Booking;
 import domain.Offer;
 import domain.RuralHouse;
 
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.beans.*;
 
 import java.sql.Date;
 import java.text.DateFormat;
 import java.util.*;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 
 public class SearchOffersGUI extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	private JLabel lblRuralHouse = new JLabel();
 	private JComboBox jCBRuralHouse;
 	private JButton btnSearch = new JButton();
 	private JButton btnCancel = new JButton();
 	private Calendar myCalendar = null;
 	private JList showOffers = new JList();
 	
 	private JFrame showInfo; 
 	
 	private DefaultListModel<Offer> offerList;
 	private JTextField txtTelephone;
 	private JTextField txtEmail;
 
 	public SearchOffersGUI() {
 		getContentPane().setBackground(new Color(152, 251, 152));
 		try {
 			jbInit();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void jbInit() throws Exception {
 		ApplicationFacadeInterface facade=StartWindow.getBusinessLogic();
 
 		Vector<RuralHouse> rhs=facade.getAllRuralHouses();
 		jCBRuralHouse = new JComboBox<RuralHouse>(rhs);
 		jCBRuralHouse.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				showInfo.setVisible(false);
 				showInfo = new ShowHouseInfoGUI((RuralHouse) jCBRuralHouse.getSelectedItem());
 				showInfo.setVisible(true);
 				btnAccept_actionPerformed(arg0);
 			}
 		});
 
 		this.getContentPane().setLayout(null);
 		this.setSize(new Dimension(460, 360));
 		this.setTitle("Search available rural houses");
 		lblRuralHouse.setText("Rural house:");
 		jCBRuralHouse.setBounds(new Rectangle(109, 20, 115, 20));
 		lblRuralHouse.setBounds(new Rectangle(40, 20, 105, 25));
 		jCBRuralHouse.setBounds(new Rectangle(115, 30, 115, 20));
 		btnSearch.setBackground(new Color(152, 251, 152));
 		btnSearch.setText("Search");
 		btnSearch.setBounds(new Rectangle(15, 281, 130, 30));
 		btnSearch.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				btnAccept_actionPerformed(e);
 			}
 		});
 		btnCancel.setBackground(new Color(152, 251, 152));
 		btnCancel.setText("Cancel");
 		btnCancel.setBounds(new Rectangle(295, 281, 130, 30));
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				btnClose_actionPerformed(e);
 			}
 		});
 		this.getContentPane().add(btnCancel, null);
 		this.getContentPane().add(btnSearch, null);
 		this.getContentPane().add(jCBRuralHouse, null);
 		this.getContentPane().add(lblRuralHouse, null);
 		showOffers.setBounds(55, 126, 305, 125);
 		
 		getContentPane().add(showOffers);
 		
 		JButton btnBook = new JButton();
 		btnBook.setBackground(new Color(152, 251, 152));
 		btnBook.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				btnBook_actionPerformed();
 			}
 		});
 		btnBook.setText("Book");
 		btnBook.setBounds(new Rectangle(284, 281, 130, 30));
 		btnBook.setBounds(155, 281, 130, 30);
 		getContentPane().add(btnBook);
 		
 		txtTelephone = new JTextField();
 		txtTelephone.setText("0");
 		txtTelephone.setBounds(new Rectangle(155, 270, 140, 20));
 		txtTelephone.setBounds(195, 87, 140, 20);
 		getContentPane().add(txtTelephone);
 		
 		JLabel label = new JLabel();
 		label.setText("Telephone contact number:");
 		label.setBounds(new Rectangle(15, 270, 140, 20));
 		label.setBounds(55, 87, 140, 20);
 		getContentPane().add(label);
 		
 		JLabel lblEmail = new JLabel("Email:");
 		lblEmail.setBounds(55, 56, 46, 14);
 		getContentPane().add(lblEmail);
 		
 		txtEmail = new JTextField();
 		txtEmail.setBounds(109, 53, 158, 20);
 		getContentPane().add(txtEmail);
 		txtEmail.setColumns(10);
 		
 		showInfo = new ShowHouseInfoGUI((RuralHouse) jCBRuralHouse.getSelectedItem());
 		showInfo.setVisible(true);
 
 	}
 
 	private void btnClose_actionPerformed(ActionEvent e) {
 		this.setVisible(false);
 		showInfo.setVisible(false);
 	}
 
 	private void btnAccept_actionPerformed(ActionEvent e) {	
 		RuralHouse rh = (RuralHouse) jCBRuralHouse.getSelectedItem();
 		offerList = new DefaultListModel<Offer>();
 		for (Offer offer : rh.offers)
 			if (offer.getBooking() == null)
 				offerList.addElement(offer);
 
 		showOffers.setModel(offerList);
 	}
 
 	private void btnBook_actionPerformed(){
 		String telephone=txtTelephone.getText();
 		String email=txtEmail.getText();
 		Offer auxOffer = (Offer) showOffers.getSelectedValue();
 		Booking auxBooking = auxOffer.createBook(telephone);						
 		BookRuralHouseConfirmationWindow confirmWindow=new BookRuralHouseConfirmationWindow(auxBooking,auxOffer, email, telephone);
 		confirmWindow.setVisible(true);
 	}
 }
