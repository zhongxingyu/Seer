 /**
  * 
  */
 package view;
 
 import java.awt.BorderLayout;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 import view.pokedex.PokedexScreen;
 
 import data.DataFetch;
 
 /**
  * @author jimiford
  *
  */
 @SuppressWarnings("serial")
 public class PokeSearchPanel extends JPanel {
 
 	
 	private static final String DEFAULT = "Search Pokemon...";
 	private boolean override;
 	private DataFetch df;
 	private JTextArea jta;
 	private JScrollPane jsp;
 	private JTable table;
 	private PokeListener listen;
 	private PokedexScreen PSpanel;
 	
 	
 	public PokeSearchPanel(PokeListener listen) {
 		super(new BorderLayout());
 		this.listen = listen;
 		this.df = DataFetch.getInstance();
 		this.table = new JTable();
 		this.table.getTableHeader().setReorderingAllowed(false);
 		this.jsp = new JScrollPane(table);
 		this.jta = new JTextArea(DEFAULT);
 		this.initActions();
 		this.add(jsp, BorderLayout.CENTER);
 		this.add(jta, BorderLayout.NORTH);
 		PSpanel = new PokedexScreen();
 		this.updateModel();
 	}
 	
 	public JTable getTable() {
 		return this.table;
 	}
 	
 	public void updateModel() {
 		if(jta.getText().equals(DEFAULT)) {
 //			TableModel atm = df.getDefaultPokemonModel();
 			
 			this.table.setModel(df.getDefaultPokemonModel());
 		} else {
 			this.table.setModel(df.getSearchPokemonModel(jta.getText()));
 		}
 	}
 	
 	private void initActions() {
 		this.jta.addFocusListener(new FocusListener() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				if(jta.getText().equals(DEFAULT)) {
 					jta.setText("");
 				}
 			}
 			@Override
 			public void focusLost(FocusEvent e) {
 				if(jta.getText().equals("")) {
 					jta.setText(DEFAULT);
 					updateModel();
 				}
 			}
 		});
 		this.jta.addKeyListener(new KeyAdapter() {
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if(e.getKeyChar() == KeyEvent.VK_ESCAPE) {
 					jta.setText(DEFAULT);
 				}
 				updateModel();
 			}
 			
 		});
 		this.table.addMouseListener(new MouseListener(){
 
 			@Override
			public void mouseClicked(MouseEvent e) {
 				int index = table.getSelectedRow();
 				if(index != -1){		
 					String pokemon = (String) table.getValueAt(index, 1);
 					// TODO call the setPokedexEntry(pokemon) method in PokedexScreen and switch views
 				}				
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 	}
 	
 }
