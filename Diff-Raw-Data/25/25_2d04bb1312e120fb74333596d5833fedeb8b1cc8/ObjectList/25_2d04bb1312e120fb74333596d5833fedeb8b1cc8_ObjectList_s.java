 package blue.hotel.gui;
 
 
 import javax.swing.DefaultListModel;
 import javax.swing.JOptionPane;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 
 import blue.hotel.logic.SaveReservation;
 import blue.hotel.model.Customer;
 import blue.hotel.model.Reservation;
 import blue.hotel.model.Room;
 import blue.hotel.storage.DAO;
 import blue.hotel.storage.DAOException;
 
 @SuppressWarnings("serial")
 public class ObjectList<T> extends JPanel {
 	private final String PLACE_HOLDER_STRING = "No entry available...";
 	
 	Class<T> klass;
 	JList list;
 	List<T> objects;
 	
 	private void reloadObjects() {
 		try {
 			objects = DAO.getInstance().getAll(klass);
 		} catch (DAOException e) {
 			objects = new LinkedList<T>();
 			e.printStackTrace();
 		}
 		
 		if (list != null) {
 			DefaultListModel dlm = new DefaultListModel();
 			for (T o: objects) {
 				boolean add = true;
 				
 				//do not display canceled reservations
 				if(o instanceof Reservation) {
 					Reservation res = (Reservation)o;
 					if(res.isStorno()) add = false;
 				}
 				
 				if(add) dlm.addElement(o);
 			}
 			list.setModel(dlm);
 			
 			if(dlm.size() == 0) {
 				dlm.addElement(PLACE_HOLDER_STRING);
 			}
 		}
 	}
 
 	public String getSimpleName() {
 		return klass.getSimpleName();
 	}
 	
 	public ObjectList(Class<T> klass) {
 		//setModalityType(ModalityType.APPLICATION_MODAL);
 		this.klass = klass;
 		//setTitle("Data Editor: " + klass.getSimpleName());
 		
 		this.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		this.add(panel, BorderLayout.SOUTH);
 		
 		JButton btnEdit = new JButton("Edit");
 		btnEdit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (list.getSelectedIndex() == -1) {
 					JOptionPane.showMessageDialog(ObjectList.this, "Nothing selected.");
 					return;					
 				}
 				
 				T o = (T)list.getSelectedValue();
 				
 				if(o.equals(PLACE_HOLDER_STRING)) {
 					JOptionPane.showMessageDialog(ObjectList.this, "Please select a valid entry.");
 					return;
 				}
 				
 				Editor<T> editor = (Editor<T>) EditorManager.openEditor(ObjectList.this.klass);
 				editor.readFrom(o);
 				
 				if (editor.run()) {
 					editor.writeTo(o);
 					try {
 						DAO.getInstance().update(o);
 					} catch (DAOException e1) {
 						JOptionPane.showMessageDialog(ObjectList.this, "Cannot update: " + e1);
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
 		
 		JButton btnNew = new JButton("New");
 		btnNew.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Editor<T> editor = EditorManager.openEditor(ObjectList.this.klass);
 					if (editor.run()) {
 						T o = ObjectList.this.klass.newInstance();
 						editor.writeTo(o);
 						if( ObjectList.this.klass == Reservation.class){
 							DAO.getInstance().update(o);
 						} else{
 							DAO.getInstance().create(o);
 						}
 						ObjectList.this.reloadObjects();
 					}
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		panel.setLayout(new GridLayout(0, 3, 10, 0));
 		panel.add(btnNew);
 		panel.add(btnEdit);
 		
 		JButton btnDelete;
 		if (klass.getName().equals(Reservation.class.getName())) {
 			btnDelete = new JButton("Cancellation");
 		} else {
 			btnDelete = new JButton("Delete");
 		}
 		
 		btnDelete.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (list.getSelectedIndex() == -1) {
 					JOptionPane.showMessageDialog(ObjectList.this, "Nothing selected.");
 					return;					
 				}
 				
 				T o = (T)list.getSelectedValue();
 				
 				if (o instanceof Customer) {
					/* TODO Issue #6: If the customer has any
					 * reservations, show error and return --thp */
 				}
 				
 				if (o instanceof Room) {
					/* TODO Issue #6: If the room is part of any
					 * reservations, show error and return --thp */
 				}
 				
 				if(o.equals(PLACE_HOLDER_STRING)) {
 					JOptionPane.showMessageDialog(ObjectList.this, "Please select a valid entry.");
 					return;
 				}
 				
 				try {
 					if(o instanceof Reservation) {
 						//do storno if o is a reservation
 						int option = JOptionPane.showConfirmDialog(ObjectList.this,
 							    		"Do you really want do cancel this reservation?", "Cancellation",
 							    		JOptionPane.YES_NO_OPTION,
 							    		JOptionPane.QUESTION_MESSAGE);
 						
 				
 						if(option == JOptionPane.YES_OPTION) {
 							Reservation res = (Reservation)o;
 							res.setStorno(true);
 							res = SaveReservation.save(res, res.getRooms());
 						}
 					} else {
 						//delete selected entry
 						DAO.getInstance().delete(o);
 					}
 					
 					ObjectList.this.reloadObjects();
 				} catch (DAOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 		panel.add(btnDelete);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		this.add(scrollPane, BorderLayout.CENTER);
 		
 		list = new JList();
 		scrollPane.getViewport().setView(list);
 		
 		setSize(500, 500);
 		reloadObjects();
 	}
 }
