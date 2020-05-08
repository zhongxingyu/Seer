 /**
  * 
  */
 package view;
 
 import java.awt.BorderLayout;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 
 import data.DataFetch;
 
 /**
  * @author jimiford
  *
  */
 @SuppressWarnings("serial")
 public class PokeSearchPanel extends JPanel {
 
 	
 	private static final String DEFAULT = "Search Pokemon...";
 	
 	private DataFetch df;
 	private JTextArea jta;
 	private JScrollPane jsp;
 	private JTable table;
 	
 	public PokeSearchPanel() {
 		super(new BorderLayout());
 		this.df = DataFetch.getInstance();
 		this.table = new JTable();
 		this.jsp = new JScrollPane(table);
 		this.jta = new JTextArea(DEFAULT);
 		this.initializeActions();
 		this.add(jsp, BorderLayout.CENTER);
 		this.add(jta, BorderLayout.NORTH);
 		this.updateTable();
 	}
 	
 	private void updateTable() {
 		if(jta.getText().equals(DEFAULT)) {
 			this.table.setModel(df.getDefaultPokemonModel());
 		} else {
 			this.table.setModel(df.getSearchPokemonModel(jta.getText()));
 		}
 	}
 	
 	private void initializeActions() {
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
 					updateTable();
 				}
 			}
 		});
 		this.jta.addKeyListener(new KeyAdapter() {
 
 			@Override
			public void keyTyped(KeyEvent e) {
 				if(e.getKeyChar() == KeyEvent.VK_ESCAPE) {
 					jta.setText(DEFAULT);
 				}
 				updateTable();
 			}
 			
 		});
 	}
 	
 }
