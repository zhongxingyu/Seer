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
 //	private JLabel levelLbl;
 //	private JLabel itemLbl;
 //	private JLabel moveLbl;
 	private int size;
 	private ArrayList<JLabel> dataLbls;
 	private ArrayList<String> evoData;
 	private JScrollPane pictureScroller;
 	private JPanel picturePanel;
 	private JPanel westPanel;
 //	private JPanel centerPanel;
 //	private JTable eggMoves;
 	//private int records;
 	private boolean branching = false;
 
 	public PokevolvePanel(){
 		super(new MigLayout());
 		this.df = DataFetch.getInstance();
 		dataLbls = new ArrayList<JLabel>();
 		initComponents();
 		fillComponents();
 		updateTable();
 	}
 
 	private void initComponents() {
 		jta = new JTextArea(DEFAULT);
 		jta.setPreferredSize(new Dimension(160,25));
 //		jta.setMinimumSize(new Dimension(160,25));
 		table = new JTable();
 		picturePanel = new JPanel(new MigLayout());
 		pictureScroller = new JScrollPane(picturePanel);
 		jsp = new JScrollPane(table);
 		table.getTableHeader().setReorderingAllowed(false);
 		jsp.setPreferredSize(new Dimension(160,200));
 		jsp.setMinimumSize(new Dimension(160,200));
 		initializeActions();
 		westPanel = new JPanel(new MigLayout());
 	}
 	
 	private void fillComponents() {
 		westPanel.add(jta, "wrap");
 		westPanel.add(jsp);
 		this.add(westPanel, "west");
 		this.add(pictureScroller);
 	}
 
 	private void updateComponents(){
 		this.picturePanel.removeAll();
 //		this.remove(picturePanel);
 //		initComponents();
 		int j = 0;
 		//System.out.println(dataLbls.size());
 		for(int i = 0; i < (dataLbls.size()/3); i++){
 			JPanel panel = new JPanel(new MigLayout());
 			panel.add(dataLbls.get(j++), "center, wrap");
 			panel.add(dataLbls.get(j++), "center, wrap");
 			panel.add(dataLbls.get(j++), "center");
 			if(dataLbls.size()/3 == 5){
 				if((i+1)%3 == 0){
 					this.picturePanel.add(panel, "wrap");
 				}else{
 					this.picturePanel.add(panel);
 				}
 			}else if(dataLbls.size()/3 == 4){
 				if((i+1)%2 == 0){
 					this.picturePanel.add(panel, "wrap");
 				}else{
 					this.picturePanel.add(panel);
 				}
 			}else if(dataLbls.size()/3 == 8){
 				if((i+1)%4 == 0){
 					this.picturePanel.add(panel, "wrap");
 				}else{
 					this.picturePanel.add(panel);
 				}
 			}else if(dataLbls.size()/3 == 6){
 				if((i+1)%3 == 0){
 					this.picturePanel.add(panel, "wrap");
 				}else{
 					this.picturePanel.add(panel);
 				}
 			}else{
 				this.picturePanel.add(panel);
 			}
 		}
 //		this.add(picturePanel);
 		this.revalidate();
 		this.repaint();
 		if(!dataLbls.isEmpty()){
 			dataLbls.clear();
 		}
 	}
 	public void updatePokevolve(String pokemon, int branchNo){
 		evoData = df.getPokevolveQuery(pokemon);
 		if(evoData.size() == 0) {
 			return;
 		}
 		if(evoData.get(0).equals("133")){
 			updateEevee();
 			return;
 		}
 		if(evoData.get(0).equals("236")){
 			updateTyrogue();
 			return;
 		}
 		int records = evoData.size()/7;
 		if(records > 1){
 			branching = true;
 		}
 		for(int i = 0; i < records; i++){
 			if(i > 0){
 				//branchNo = branchNo + (i*7);
 				branchNo += 7;
 			}
 			//System.out.println(evoData);
 			dataLbls.add(new JLabel(evoData.get(branchNo+1)));
 			String ID = evoData.get(branchNo);
 			addImage(ID);
 			if(evoData.get(branchNo+3).endsWith("does not evolve")){
 				dataLbls.add(new JLabel("Does Not Evolve"));
 				if(branching == false){
 					updateComponents();
 				}
 				branching = false;
 			}
 			if(evoData.get(branchNo+3).endsWith("at level")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3) + " " + evoData.get(branchNo+4)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).equals("when leveled up with high friendship")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).equals("when leveled up with high friendship during the day")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).equals("when leveled up with high friendship during the night")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).equals("when traded")){
 				dataLbls.add(new JLabel("Evovles " + evoData.get(branchNo+3)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).endsWith("while holding")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3) + " " + evoData.get(branchNo+5)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).endsWith("when exposed to")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3) + " " + evoData.get(branchNo+5)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).equals("when leveled up with high Beauty")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).endsWith("while knowing")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3) + " " + evoData.get(branchNo+6)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).equals("when leveled up with Remoraid in the party")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).equals("when leveled up at Mt. Coronet")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).equals("when leveled up near a Moss Rock")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).equals("when leveled up near an Ice Rock")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}else if(evoData.get(branchNo+3).equals("when traded while holding ")){
 				dataLbls.add(new JLabel("Evolves " + evoData.get(branchNo+3) + " " + evoData.get(branchNo+5)));
 				updatePokevolve(evoData.get(branchNo+2), 0);
 				evoData = df.getPokevolveQuery(pokemon);
 				records = evoData.size()/7;
 			}
 		}
 	}
 
 	private void updateTyrogue() {
 		evoData = df.getPokevolveQuery("236");
 		dataLbls.add(new JLabel(evoData.get(1)));
 		addImage("236");
 		dataLbls.add(new JLabel(""));
 		for(int i = 0; i < 3; i++){
 			dataLbls.add(new JLabel(evoData.get(i*7+2)));
 			ArrayList<String> temp = df.getPokedexQuery(evoData.get(i*7+2));
 			String ID = temp.get(0);
 			addImage(ID);
 			if(evoData.get(i*7+3).endsWith("at level")){
				dataLbls.add(new JLabel("Evolves " + evoData.get(i*7+3)));
 			}
 		}
 		updateComponents();
 
 	}
 
 	private void updateEevee() {
 		evoData = df.getPokevolveQuery("133");
 		dataLbls.add(new JLabel(evoData.get(1)));
 		addImage("133");
 		dataLbls.add(new JLabel(""));
 		for(int i = 0; i < 7; i++){
 			dataLbls.add(new JLabel(evoData.get(i*7+2)));
 			ArrayList<String> temp = df.getPokedexQuery(evoData.get(i*7+2));
 			String ID = temp.get(0);
 			addImage(ID);
 			if(evoData.get(i*7+3).equals("when exposed to")){
				dataLbls.add(new JLabel("Evolves " + evoData.get(i*7+3) + " " + evoData.get(i*7+5)));
 			}else if(evoData.get(i*7+3).startsWith("when leveled up")){
				dataLbls.add(new JLabel("Evolves " + evoData.get(i*7+3)));
 			}
 		}
 		updateComponents();
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
 		} catch (IOException e) { }
 		Image resizeImg = img.getScaledInstance(
				imgs.getWidth()/2, imgs.getHeight()/2, Image.SCALE_SMOOTH);
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
 					updatePokevolve(pokemon, 0);
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
