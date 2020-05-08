 package week3.bke;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 
 import javax.swing.*;
 
 public class BKEView extends JFrame implements Observer {
 	JPanel p; // panel for buttons
 	List<JButton> lb;
 	JLabel l;
 	JButton b;
 	
 	public BKEView(Spel s) {
 		super("Boter, kaas en eieren");
 		s.addObserver(this);
 		
 		Container cp = this.getContentPane();
 		
 		p = new JPanel();
 		lb = new ArrayList<JButton>();
 		for(int i = 0; i < Bord.DIM; i++) {
 			for(int j = 0; j < Bord.DIM; j++) {
 				JButton b = new JButton();
 				b.addActionListener(new BKEController(s));
 				b.setActionCommand("" + (i * Bord.DIM + j));
 
 				p.add(b);
 				lb.add(b);
 			}
 		}
 		
 		p.setLayout(new GridLayout(3,3));
 		cp.add(p);
 
 		JPanel controlPanel = new JPanel();
 
 		// spacer
 		controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		l = new JLabel();
 		l.setAlignmentX(Component.CENTER_ALIGNMENT);
 		controlPanel.add(l);
 
 		// spacer
 		controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 		
 		b = new JButton("Nog een keer?");
 		b.setAlignmentX(Component.CENTER_ALIGNMENT);
 		b.setEnabled(false);
 		b.addActionListener(new BKEController(s));
 		b.setActionCommand("" + (Bord.DIM * Bord.DIM));
 		controlPanel.add(b);
 
 		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
 		cp.add(controlPanel);
 
 		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
 		this.setResizable(false);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
 		this.setSize(300,300);
 		this.setVisible(true);
 
 		this.update(null, s); // set-up initial state
 	}
 	
 	@Override
 	public void update(Observable o, Object obj) {
 		Spel s = ((Spel) obj);
 		Bord b = s.getBord();
 		for (int i = 0; b.isVakje(i); i++) {
 			lb.get(i).setText(b.getVakje(i).toString());
 			if (!b.isLeegVakje(i)) {
 				lb.get(i).setEnabled(false);
 			} else {
 				lb.get(i).setEnabled(true);
 			}
 		}
 		
 		if (b.gameOver()) {
 			if (b.heeftWinnaar()) {
 				String winnaar = Mark.LEEG.toString();
 				if (b.isWinnaar(Mark.OO)) {
 					winnaar = Mark.OO.toString();	
 				} else {
 					winnaar = Mark.XX.toString();
 				}
 				l.setText(winnaar + " heeft gewonnen");
 			} else {
 				l.setText("Remise");
 			}
 			for (int i = 0; b.isVakje(i); i++) {
 				 lb.get(i).setEnabled(false);
 			}
 			
 			this.b.setEnabled(true); // enable 'again' button
 		} else {
		  this.b.setEnabled(false);
 			l.setText(s.getHuidig() + " is aan de beurt");
 		}	
 	}
 
 	public static void main(String[] args) {
 		Spel s = new Spel();
 		new BKEView(s);
 	}
 	
 	// Controller als inner class
 
   public class BKEController implements ActionListener {
   	Spel spel;
 
   	public BKEController(Spel s) {
   		spel = s;
   	}
 
   	@Override
   	public void actionPerformed(ActionEvent e) {
   		int button = Integer.parseInt(e.getActionCommand());
 
   		if (button < (Bord.DIM * Bord.DIM)) {
   			spel.doeZet(button);
   		} else {
   			spel.reset();
   		}
   	}
   }
 }
