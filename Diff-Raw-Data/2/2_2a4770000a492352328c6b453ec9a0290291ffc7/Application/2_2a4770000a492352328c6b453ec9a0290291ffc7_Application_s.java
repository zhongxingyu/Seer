 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JLabel;
 import java.awt.Font;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.JPasswordField;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseEvent;
 import java.awt.Color;
 import java.awt.Dimension;
 
 public class Application extends JPanel implements MouseListener
 {
     int x = Client.fenetre_x;
     int y = Client.fenetre_y;
     
     JScrollPane sp;
     
     JPanel conteneur_ouest = new JPanel();
     JPanel conteneur_centre = new JPanel();
     JPanel commentaires = new JPanel();
     JPanel header = new JPanel();
     JPanel infos = new JPanel();
     JPanel nom = new JPanel();
     JPanel avis = new JPanel();
 
     Application(String id, String nom_application)
     {
 	req_avis(id);
 	Client.getInstance().getFen().setTitle(nom_application);
 	setSize(x, y);
 	
 	setLayout(new BorderLayout());
 	conteneur_ouest.setLayout(new BorderLayout());
 	conteneur_centre.setLayout(new BorderLayout());
 	header.setLayout(new BorderLayout());
 	infos.setLayout(new GridLayout(5,1));
 	nom.setLayout(new BorderLayout());
 	
 	//avis.setPreferredSize(new Dimension(300, 400));
 
 	nom.add("Center", new JLabel(nom_application));
 	nom.add("East", new JLabel("v47545"));
 	
 	header.add("West", new JButton("deco"));
 	header.add("Center", new JTextField(10));
 	header.add("East",new JButton("Accueil"));
 	header.add("South", nom);
 	
 	infos.add(new JLabel("Catégorie:"));
 	infos.add(new JLabel("Os:"));
 	
 	infos.add(new JLabel("prix:"));
 	infos.add(new JLabel("mensuel:"));
 
 	sp = new JScrollPane(avis)
 	    {
 		public Dimension getPreferredSize()
 		{
 		    return new Dimension(200, 200);
 		}
 	    };
 
 	add("North", header);
 	add("West", infos);
 	add("Center", sp);
     }
 
     private void read_sqldata(SqlData r)
     {
         for(int i = 0; i < r.getNbCol(); i++)
             {
                 System.out.print(r.nomCol[i]);
             }
         System.out.println("");
         for(int i = 0; i < r.getNbLigne(); i++)
             {
                 for(int j = 0; j < r.getNbCol(); j++)
                     {
                         System.out.print(r.data[i][j] + " \t");
                     }
                 System.out.println("");
             }
     }
     
     public void req_avis(String id)
     {
 	SqlData r = Client.getInstance().getConnect().request("get_avisApp",id);
 	read_sqldata(r);
 	System.out.println(r.getNbLigne() +  " " + r.getNbCol());
 	avis.setLayout(new GridLayout(r.getNbLigne(), 1));
 	for(int i = 0; i < r.getNbLigne(); i++)
 	    {
 		System.out.println("nom: "+r.data[i][0]);
		SqlData nom = Client.getInstance().getConnect().request("get_infoMe", r.data[i][0]);
 		
 		JPanel avis_aff = new JPanel();
 		avis_aff.setLayout(new BorderLayout());
 		
 		avis_aff.add("North", new JLabel(nom.data[0][1]));
 		avis_aff.add("Center", new JLabel(r.data[i][3]));
 		avis_aff.add("South", new JLabel(r.data[i][4]));		    
 		avis.add(avis_aff);
 	    }
     }
 
     public void mouseClicked(MouseEvent e)
     {
 	
     }
     
     public void mouseEntered(MouseEvent e){}
     public void mouseExited(MouseEvent e){}
     public void mousePressed(MouseEvent e){}
     public void mouseReleased(MouseEvent e){}
 }
