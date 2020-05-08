 package view.pokevolve;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 
 import data.DataFetch;
 
 @SuppressWarnings("serial")
 public class PokevolvePanel extends JPanel{
 	private static final String DEFAULT = "Search Pokemon...";
 	private DataFetch df;
 	private JTextArea jta;
 	private JTable table;
 	private JScrollPane jsp;
 	private JLabel levelLbl;
 	private JLabel itemLbl;
 	private JLabel moveLbl;
 	private int size;
 	private ArrayList<JLabel> dataLbls;
 	private ArrayList<String> evoData;
 	private JPanel centerPanel;
 	private JTable eggMoves;
 	
 	public PokevolvePanel(){
 		super(new MigLayout());
 		this.df = DataFetch.getInstance();
 		dataLbls = new ArrayList<JLabel>();
 		initComponents();
 	}
 
 	private void initComponents() {
 		jta = new JTextArea(DEFAULT);
 		jta.setPreferredSize(new Dimension(90,30));
 		table = new JTable();
 		jsp = new JScrollPane(table);
 		table.getTableHeader().setReorderingAllowed(false);
 		jsp.setPreferredSize(new Dimension(160,200));
 		initializeActions();
 		JPanel westPanel = new JPanel(new MigLayout());
 		westPanel.add(jta, "wrap");
 		westPanel.add(jsp);
 		updateTable();
 		this.add(westPanel, "west");
 	}
 	
 	private void updateComponents(){
 		this.removeAll();
 		initComponents();
 		int j = 0;
 		System.out.println((dataLbls.size()/2)-1);
		for(int i = 0; i < (dataLbls.size()/2)-1; i++){
 			JPanel panel = new JPanel(new MigLayout());
 			panel.add(dataLbls.get(j), "center, wrap");
 			j++;
 			panel.add(dataLbls.get(j), "center, wrap");
 			j++;
 			panel.add(dataLbls.get(j), "center");
 			j++;
 			panel.setBorder(BorderFactory.createEtchedBorder());
 			this.add(panel);
 		}
 		this.revalidate();
 		this.repaint();
 		if(!dataLbls.isEmpty()){
 			dataLbls.clear();
 		}
 	}
 	public void updatePokevolve(String pokemon){
 		evoData = df.getPokevolveQuery(pokemon);
 		System.out.println(evoData);
 		dataLbls.add(new JLabel(evoData.get(1)));
 		String ID = evoData.get(0);
 		addImage(ID);
 		if(evoData.get(3).endsWith("does not evolve")){
 			dataLbls.add(new JLabel("Does Not Evolve"));
 			updateComponents();
 		}
 		if(evoData.get(3).endsWith("at level")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3) + " " + evoData.get(4)));
 			updatePokevolve(evoData.get(2));
 		}else if(evoData.get(3).equals("when leveled up with high friendship")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3)));
 			updatePokevolve(evoData.get(2));
 		}else if(evoData.get(3).equals("when leveled up with high friendship during the day")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3)));
 			updatePokevolve(evoData.get(2));
 		}else if(evoData.get(3).equals("when leveled up with high friendship during the night")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3)));
 		}else if(evoData.get(3).equals("when traded")){
 			dataLbls.add(new JLabel("Evovles " + evoData.get(3)));
 		}else if(evoData.get(3).endsWith("while holding")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3) + " " + evoData.get(5)));
 		}else if(evoData.get(3).endsWith("when exposed to")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3) + " " + evoData.get(5)));
 		}else if(evoData.get(3).equals("when leveled up with high Beauty")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3)));
 		}else if(evoData.get(3).endsWith("while knowing")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3) + " " + evoData.get(6)));
 		}else if(evoData.get(3).equals("when leveled up with Remoraid in the party")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3)));
 		}else if(evoData.get(3).equals("when leveled up at Mt. Coronet")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3)));
 		}else if(evoData.get(3).equals("when leveled up near a Moss Rock")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3)));
 		}else if(evoData.get(3).equals("when leveled up near an Ice Rock")){
 			dataLbls.add(new JLabel("Evolves " + evoData.get(3)));
 		}
 	}
 	
 	private void addImage(String ID){
 		if(ID.length() < 3){
 			if(ID.length() == 1){
 				ID = "00" + ID;
 			}else{
 				ID = "0" + ID;
 			}
 		}
 		String path = "resources/images/" + ID + ".png";
 		Toolkit tk = Toolkit.getDefaultToolkit();
 		Image img = tk.createImage(path);
 		BufferedImage imgs = null;
 		try {
 			imgs = ImageIO.read(new File(path));
 		} catch (IOException e) {
 		}
 		Image resizeImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
 		dataLbls.add(new JLabel(""));
 		dataLbls.get(dataLbls.size()-1).setIcon(new ImageIcon(resizeImg));
 	}
 	private void updateTable(){
 		if(jta.getText().equals(DEFAULT)){
 			this.table.setModel(df.getSimplifiedDefaultPokemonModel());
 			this.table.getColumnModel().getColumn(0).setHeaderValue("ID");
 			this.table.getColumnModel().getColumn(1).setHeaderValue("Name");
 			this.table.getColumnModel().getColumn(0).setMaxWidth(40);
 			this.table.getColumnModel().getColumn(1).setMaxWidth(100);
 		}else{
 			this.table.setModel(df.getSimplifiedSearchPokemonModel(jta.getText()));
 			this.table.getColumnModel().getColumn(0).setHeaderValue("ID");
 			this.table.getColumnModel().getColumn(1).setHeaderValue("Name");
 			this.table.getColumnModel().getColumn(0).setMaxWidth(40);
 			this.table.getColumnModel().getColumn(1).setMaxWidth(100);
 		}
 	}
 
 	private void initializeActions() {
 		this.jta.addFocusListener(new FocusListener(){
 			public void focusGained(FocusEvent e){
 				if(jta.getText().equals(DEFAULT)){
 					jta.setText("");
 				}
 			}
 			public void focusLost(FocusEvent e){
 				if(jta.getText().equals("")){
 					jta.setText(DEFAULT);
 				}
 				updateTable();
 			}
 		});
 		this.jta.addKeyListener(new KeyAdapter() {
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if(e.getKeyChar() == KeyEvent.VK_ESCAPE) {
 					jta.setText(DEFAULT);
 				}
 				updateTable();
 			}
 			
 		});
 		this.table.addMouseListener(new MouseListener(){
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				int index = table.getSelectedRow();
 				if(index != -1){		
 					String pokemon = (String) table.getValueAt(index, 1);
 					updatePokevolve(pokemon);
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
