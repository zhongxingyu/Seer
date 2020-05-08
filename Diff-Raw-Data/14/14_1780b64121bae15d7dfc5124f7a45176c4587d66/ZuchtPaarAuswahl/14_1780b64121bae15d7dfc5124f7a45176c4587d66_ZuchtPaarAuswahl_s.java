 package org.peie.avicultura.viewer;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.List;
 
 import javax.swing.JDesktopPane;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.DefaultTableModel;
 
 import org.peie.avicultura.helper.AviInternalFrame;
 import org.peie.avicultura.helper.AviculturaException;
 import org.peie.avicultura.helper.DbHelper;
 import org.peie.avicultura.helper.ZuchtPaarAuswahlObj;
 
 public class ZuchtPaarAuswahl {
 
 	private DbHelper dbhelper;
 	private JTextField field;
 	private JDesktopPane desktopPane;
 	private JPanel taskBar;
 	private JTable table;
 
 	public ZuchtPaarAuswahl(JDesktopPane desktopPane, JPanel taskBar,
 			DbHelper dbhelper, JTextField field) {
 		this.dbhelper = dbhelper;
 		this.field = field;
 		this.desktopPane = desktopPane;
 		this.taskBar = taskBar;
 	}
 
 	public void neueAuswahl(String birdType) throws AviculturaException {
 
 		int heigth = 300;
 		int width = 400;
 
 		Dimension size = new Dimension(width, heigth);
 
 		final AviInternalFrame internalFrame = new AviInternalFrame(taskBar,
 				"Auswahl: "+birdType);
 		// internalFrame.setFrameIcon(new ImageIcon(iconFindBird));
 		internalFrame.setClosable(true);
 		internalFrame.setMaximumSize(size);
 		internalFrame.setSize(width, heigth);
 		internalFrame.setMinimumSize(size);
 		// internalFrame.setBounds(10, 11, 596, 416);
 		internalFrame.setResizable(false);
 		internalFrame.setMaximizable(false);
 		//internalFrame.setClosable(false);
 		desktopPane.add(internalFrame);
 
 		
 		
 		List<ZuchtPaarAuswahlObj> list = dbhelper.getBirdPairAll(birdType);
 		
 		if (list.size() == 0){
 			JOptionPane.showMessageDialog(desktopPane, "Keine Auswahl fr "+birdType,
 					"Fehler im Program", JOptionPane.ERROR_MESSAGE);
			internalFrame.setVisible(false);
 			field.setText("");
 		}else{
 			internalFrame.setVisible(true);
 		}
 		
 		
 		
 		final Object[][] inTable = new Object[list.size()][3];
 		
 		
 		int i = 0;
 		for (ZuchtPaarAuswahlObj zuchtPaarAuswahlObj : list) {
 			inTable[i][0] = zuchtPaarAuswahlObj.getPaarId();
 			inTable[i][1] = zuchtPaarAuswahlObj.getFather();
 			inTable[i][2] = zuchtPaarAuswahlObj.getMother();
 			i++;
 		}
 		
 
 		table = new JTable();
 
 		table.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				
 
 				if (e.getClickCount() == 1
 						&& e.getButton() == MouseEvent.BUTTON1) {
 					int row = table.getSelectedRow();
 					String zuchtPaarId = table.getValueAt(row, 0).toString();
 					
 					field.setText(zuchtPaarId);
					internalFrame.setVisible(false);
 
 				}
 
 				
 			}
 		});
 
 		table.setModel(new DefaultTableModel(inTable, new String[] {
 				"Zuchtpaar", "Vater Ringnr.", "Mutter Ringnr." }) {
 			/**
 					 * 
 					 */
 			private static final long serialVersionUID = 1L;
 			Class[] columnTypes = new Class[] { Integer.class, String.class,
 					String.class };
 
 			public Class getColumnClass(int columnIndex) {
 				return columnTypes[columnIndex];
 			}
 
 			public boolean isCellEditable(int rowIndex, int columnIndex) {
 				return false;
 			}
 		});
 
 		table.setAutoCreateRowSorter(true);
 
 		internalFrame.getContentPane().add(table.getTableHeader(),
 				BorderLayout.NORTH);
 		JScrollPane pane = new JScrollPane(table);
 		internalFrame.getContentPane().add(pane, BorderLayout.CENTER);
 
 	}
 
 }
