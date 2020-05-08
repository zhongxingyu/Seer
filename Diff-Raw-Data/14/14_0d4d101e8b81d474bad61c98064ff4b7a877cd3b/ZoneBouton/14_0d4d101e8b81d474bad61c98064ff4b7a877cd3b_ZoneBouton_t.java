 import javax.swing.JPanel;
 import javax.swing.JButton;
 import java.awt.GridLayout;
 
 public class ZoneBouton extends JPanel {
 
     private Controleur controleur;
 
 
     /**
      *  Constructeur de la zone de bouton
      */
 	ZoneBouton(){
 		this.setLayout(new GridLayout(2,2));
 		
<<<<<<< HEAD
 		this.add(new JButton("test1"));
 		this.add(new JButton("test2"));
 		this.add(new JButton("test3"));
 		this.add(new JButton("test4"));
=======
		
        this.setLayout(new BorderLayout());
	    JPanel grid_button = new JPanel();
        grid_button.setLayout(new GridLayout());
        this.add(grid_button);

        grid_button.add(new JButton("test1"));
		grid_button.add(new JButton("test2"));
		grid_button.add(new JButton("test3"));
		grid_button.add(new JButton("test4"));
>>>>>>> parent of 969fb07... fix du positionnement
 	}
 
     /**
      *  Modifieur du controleur
      *  @param c nouveau controleur
      */
     public void setControleur(Controleur c)
     {
         this.controleur = c;
     }   
     
 
 }
