 package view;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.util.Vector;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 public class Infos extends JFrame {
 	
 	private static final long serialVersionUID = 1L;
	private static Vector<InfosPanel> infosP = new Vector<InfosPanel>(6);
 	
 	public Infos(){
 		super();
		for(int i=1; i <= 6; i++)
 			infosP.add(new InfosPanel(i));
 		
 		createGUI();
 	}
 	
 	private void createGUI(){
 		this.setTitle("Monopoly Joueurs");
 		this.setSize(new Dimension(1170, 200));
 		this.setLocation(5, 670);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JPanel test = new JPanel();
 		this.add(new JScrollPane(test));
		test.setLayout(new GridLayout(0, 6));
 		
		test.add(new JScrollPane(infosP.get(0)));
        test.add(new JScrollPane(infosP.get(1)));
        test.add(new JScrollPane(infosP.get(2)));
        test.add(new JScrollPane(infosP.get(3)));
        test.add(new JScrollPane(infosP.get(4)));
        test.add(new JScrollPane(infosP.get(5)));
 		
 		this.setVisible(true);
 	}
 	
 	/**
 	 * Ajoute une information dans la case du joueur
 	 * @param info L'intitul de l'information
 	 * @param value La valeur de l'information
 	 * @param joueur Le numro du joueur
 	 */
 	public void addInfo(String info, String value, int joueur){
 		infosP.get(joueur-1).addInfo(info, value);
 		this.repaint();
 	}
 }
