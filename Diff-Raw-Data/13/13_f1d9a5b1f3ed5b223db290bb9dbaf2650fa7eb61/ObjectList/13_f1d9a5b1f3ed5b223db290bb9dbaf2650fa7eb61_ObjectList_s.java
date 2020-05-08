 package blue.hotel.gui;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 import java.awt.BorderLayout;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JButton;
 
 import blue.hotel.model.Reservation;
 import blue.hotel.storage.DAO;
 import blue.hotel.storage.DAOException;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.GridLayout;
 import javax.swing.JScrollPane;
 
 @SuppressWarnings("serial")
 public class ObjectList<T> extends JPanel {
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
 				dlm.addElement(o);
 			}
 			list.setModel(dlm);
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
 		
 		JButton btnDelete = new JButton("Delete");
 		btnDelete.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (list.getSelectedIndex() == -1) {
 					JOptionPane.showMessageDialog(ObjectList.this, "Nothing selected.");
 					return;					
 				}
 				
 				T o = (T)list.getSelectedValue();
 				
 				try {
 					DAO.getInstance().delete(o);
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
