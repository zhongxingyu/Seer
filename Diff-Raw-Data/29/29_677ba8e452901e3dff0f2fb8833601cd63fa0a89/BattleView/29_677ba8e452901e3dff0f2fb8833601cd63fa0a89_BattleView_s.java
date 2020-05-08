 package warborn.view;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import warborn.model.Battle;
 import warborn.model.ButtonFactory;
 import warborn.model.LabelFactory;
 import warborn.model.Warborn;
 import warborn.model.Territory;
 
 public class BattleView extends Observable implements Observer, ActionListener {
 
 	public JButton btOneAttack, btAutoAttack, btRetreat;
 	public JFrame battleFrame;
 	private Warborn model;
 	private Territory t1, t2;
 	private JLabel lbAttacker, lbDefender, lbAtkTroops, lbDefTroops;
 	/**
 	 * Create the panel.
 	 */
 	public BattleView(Warborn model) {
 		this.model = model;
 		this.model.addObserver(this);
 		
 		battleFrame = new JFrame();
 		JPanel battleView = new JPanel();
 		battleView.setSize(451, 300);
 		battleView.setLayout(null);
 		battleFrame.add(battleView);
 		battleFrame.setVisible(false);
 		
 		JLabel lbBattle = new LabelFactory("Battle!");
 		lbBattle.setFont(new Font("Rod", Font.BOLD | Font.ITALIC, 40));
 		lbBattle.setBounds(140, 11, 195, 70);
 		battleView.add(lbBattle);
 		
 		btOneAttack = new ButtonFactory("One Attack");
 		btOneAttack.setBounds(10, 219, 142, 70);
 		btOneAttack.addActionListener(this);
 		battleView.add(btOneAttack);
 		
 		btAutoAttack = new ButtonFactory("Auto Attack");
 		btAutoAttack.setBounds(154, 219, 142, 70);
 		btAutoAttack.addActionListener(this);
 		battleView.add(btAutoAttack);
 		
 		btRetreat = new ButtonFactory("Retreat");
 		btRetreat.setBounds(298, 219, 142, 70);
 		btRetreat.addActionListener(this);
 		battleView.add(btRetreat);
 		
 		lbAttacker = new LabelFactory("Attacker");
 		lbAttacker.setBounds(10, 57, 95, 24);
 		battleView.add(lbAttacker);
 		
 		lbDefender = new LabelFactory("Defender");
 		lbDefender.setBounds(345, 57, 95, 24);
 		battleView.add(lbDefender);
 		
 		JLabel lbAtkTroops = new LabelFactory("atkTroops");
 		lbAtkTroops.setBounds(10, 92, 124, 24);
 		battleView.add(lbAtkTroops);
 		
 		JLabel lbDefTroops = new LabelFactory("defTroops");
 		lbDefTroops.setBounds(316, 92, 124, 24);
 		battleView.add(lbDefTroops);
 		
 		JTextArea textArea = new JTextArea();
 		textArea.setEditable(false);
 		textArea.setBounds(10, 127, 430, 81);
 		battleView.add(textArea);
 
 	}
 	
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if(model.getState() == 2 && model.getPhase() == 1){
 			battleFrame.setVisible(true);
 			Battle battle = model.getBattle();
 			t1 = battle.getFirstTerritory();
 			t2 = battle.getSecondTerritory();
 			lbAttacker.setText(t1.getName());
 			lbDefender.setText(t2.getName());
 			lbAtkTroops.setText(t1.getNbrOfUnits() + "");
 			lbDefTroops.setText(t2.getNbrOfUnits() + "");
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		notifyObservers(e);
		
 	}
 }
