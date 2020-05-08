 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 public class CombatGUI implements ActionListener {
 
 	protected CombatEngine engine;
 	protected JFrame combat;
 	protected JPanel order;
 	protected JTextArea combatOrder;
 
 	protected JPanel playerParty;
 	protected ArrayList<JTextArea> playerRoster;
 
 	protected JPanel enemyParty;
 	protected ArrayList<JTextArea> enemyRoster;
 
 	protected JPanel infoBox;
 	protected JTextArea info;
 	protected JComboBox<String> attacks;
 	protected JComboBox<String> targets;
 
 	protected JPanel pictureWindow;
 	protected JPanel combatTextInfo;
 	protected JPanel combatActions;
 
 	protected JButton action;
 	
 
 	protected Entity currentTurn;
 
 	// private CombatEngine engine;
 
 	public CombatGUI(CombatEngine engine) {
 		this.engine = engine;
 		String info1 = "";
 		String info2 = "";
 		String info3 = "";
 
 		combat = new JFrame("JFrame");
 		order = new JPanel();
 		 for (int k = engine.turnStack.size() - 1; k >= 0; k--){
 		 info1 = info1 + engine.turnStack.get(k).getName() + " ";
 		}
 		combatOrder = new JTextArea(info1);
 		combatOrder.setEditable(false);
 		order.add(combatOrder);
 		combat.setLayout(new BorderLayout());
 		combat.add(order, BorderLayout.NORTH);
 
 		playerParty = new JPanel(new GridLayout(10, 1));
 		playerRoster = new ArrayList<JTextArea>();
 		for (int i = 0; i < engine.characters.size(); i++) {
 			info1 = engine.characters.get(i).getName();
 			info2 = "Health: " + engine.characters.get(i).getCurrentHealth() + "/"
 					+ engine.characters.get(i).getMaxHealth();
 			JTextArea e = new JTextArea(info1 + "\n" + info2);
 			e.setEditable(false);
 			playerRoster.add(e);
 		}
 		for (int j = 0; j < playerRoster.size(); j++) {
 			playerParty.add(playerRoster.get(j));
 		}
 		combat.add(playerParty, BorderLayout.EAST);
 
 		infoBox = new JPanel();
 		info = new JTextArea();
 		infoBox.add(info);
 		combat.add(infoBox, BorderLayout.SOUTH);
 
 		enemyParty = new JPanel(new GridLayout(10, 1));
 		enemyRoster = new ArrayList<JTextArea>();
 		for (int h = 0; h < engine.enemies.size(); h++) {
 			info1 = engine.enemies.get(h).getName();
 			info2 = "Health: " + engine.enemies.get(h).getCurrentHealth() + "/"
 					+ engine.enemies.get(h).getMaxHealth();
 			JTextArea e = new JTextArea(info1 + "\n" + info2);
 			enemyRoster.add(e);
 			e.setEditable(false);
 		}
 		for (int l = 0; l < enemyRoster.size(); l++) {
 			enemyParty.add(enemyRoster.get(l));
 		}
 		combat.add(enemyParty, BorderLayout.WEST);
 
 		pictureWindow = new JPanel();
 		combat.add(pictureWindow, BorderLayout.CENTER);
 
 		attacks = new JComboBox<String>();
 		updateActions(engine.turnStack.peek());
 		String[] target = new String[engine.enemies.size() + engine.characters.size()];
 		int b = 0;
 		for ( b = 0; b < engine.enemies.size(); b++) {
 			target[b] = engine.enemies.get(b).getName();
 		}
 		int a = 0;
 		for (b=b; b < engine.characters.size() + engine.enemies.size(); b++){
 			target[b] = engine.characters.get(a).getName();
 			a++;
 		}
 		targets = new JComboBox<String>(target);
 		action = new JButton("Charge!");
 		action.addActionListener(this);
 		info1 = "";
 		for (int u = 0; u < engine.enemies.size(); u++) {
 			info1 = info1 + engine.enemies.get(u).getName() + ", ";
 		}
 		info1 = info1 + "appeared!";
 		info = new JTextArea("Combat initialized!" + '\n' + info1);
 		info.setEditable(false);
 		combatTextInfo = new JPanel();
 		combatActions = new JPanel(new GridLayout(1, 3));
 		infoBox = new JPanel(new GridLayout(2, 1));
 		combatTextInfo.add(info);
 		combatActions.add(attacks);
 		combatActions.add(targets);
 		combatActions.add(action);
 		infoBox.add(combatTextInfo);
 		infoBox.add(combatActions);
 		combat.add(infoBox, BorderLayout.SOUTH);
 
 		combat.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		combat.setSize(new Dimension(600, 600));
 		combat.pack();
 		combat.setVisible(true);
 
 	}
 
 	public void update() {
 		//update gui components
 		
 	}
 
 	private void updateActions(Entity current) {
 		attacks.removeAllItems();
 		attacks.addItem("Attack");
 		attacks.addItem("Flee");
 		for (int i = 0; i < current.abilities.size(); i++) {
 			attacks.addItem(current.abilities.get(i).getName());
 		}
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e){
 		if (engine.turnStack.peek() instanceof Character){
 			engine.playerTurn();
 		}
 	}
 }
