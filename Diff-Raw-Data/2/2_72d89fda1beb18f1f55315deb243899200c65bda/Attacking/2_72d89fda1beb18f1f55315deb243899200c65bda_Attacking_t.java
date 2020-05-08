 package player;
 
 import gameCharacter.GameCharacter;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import utils.JsonUtil;
 import utils.JsonUtil.JSONAttack;
 import utils.KeyHandle;
 import actions.ActionDecorator;
 import actions.Attack;
 
 import com.golden.gamedev.object.Timer;
 
 public class Attacking extends ActionDecorator {
 
 	private static final long serialVersionUID = 1L;
 
 	private KeyHandle keys;
 	private Timer timer = new Timer(250);
 	private List<ActionDecorator> attacks = new ArrayList<ActionDecorator>();
 
 	public Attacking(Attack attack) {
 		super(attack);
 		initResources();
 	}
 
 	public void initResources() {
 		keys = new KeyHandle(getWrapper().getCharacter().getGame());
 
 		JsonUtil.JSONPlayerAttacking attacking = (JsonUtil.JSONPlayerAttacking) getJsonable();
 		if (attacking.keys == null)
 			new RuntimeException("Attack keys undefined");
 
 		keys.add(Attack.ATTACK_BASIC, attacking.keys);
 
 		for (JSONAttack attack : attacking.attacks) {
			attacks.add(new StdAttack(new Attack(getWrapper(), attack)));
 		}
 	}
 
 	public void setActiveDirection(int direction) {
 		for (ActionDecorator attack : attacks)
 			if (attack.isEnabled()) {
 				getWrapper().getCharacter().setCurrentDirection(direction);
 				
 				Attack attk = (Attack) action;
 				attk.directions.get(direction).changeCharacter(true);
 			}
 	}
 
 	public boolean isEnabled() {
 		for (ActionDecorator attack : attacks)
 			if (attack.isEnabled())
 				return true;
 		return false;
 	}
 
 	public void update(long elapsed) {
 		if (isEnabled()) {
 			super.update(elapsed);
 
 			int status = keys.checkKeys();
 			GameCharacter character = getWrapper().getCharacter();
 
 			if (status != -1) {
 				if (!isActive()) {
 					setActive(true);
 					timer.setActive(true);
 					character.stop();
 					setActiveDirection(character.getCurrentDirection());
 					getWrapper().get("walking").setEnabled(false, true);
 				}
 			} else if (isActive()) {
 				if (timer.action(elapsed)) {
 					getWrapper().get("walking").setEnabled(true, false);
 					setActive(false);
 					timer.refresh();
 					timer.setActive(false);
 				}
 
 			}
 		}
 	}
 
 	public void render(Graphics2D g) {
 	}
 
 }
