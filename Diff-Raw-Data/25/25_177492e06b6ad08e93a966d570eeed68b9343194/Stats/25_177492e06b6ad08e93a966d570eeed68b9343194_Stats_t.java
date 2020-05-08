 package pudgewars.components;
 
 import java.awt.Image;
 
 import pudgewars.Game;
 import pudgewars.entities.PudgeEntity;
 import pudgewars.render.GUI;
 import pudgewars.util.ImageHandler;
 
 public class Stats {
 	public PudgeEntity pudge;
 
 	// Inventory Data
 	private int experience = 25;
 	public boolean isOpen = false;
 	public Image[] statImages;
 	public Image[] expImages;
 	public Image[] lifeImages;
 	public Image[] hookCooldownImages;
 	public Image[] grappleCooldownImages;
 
 	// Movement Data
 	public BaseStat moveSpeed = new BaseStat(this, "MoveSpeed", 0, 3.8, 0.4, 1);
 
 	// Hook Data
 	public BaseStat hookSize = new BaseStat(this, "Hook Size", 5, 0.75, 0.1, 1);
 	public BaseStat hookSpeed = new BaseStat(this, "Hook Speed", 3, 8, 1, 1);
 	public BaseStat hookRange = new BaseStat(this, "Hook Range", 4, 14, 2, 1);
 	public BaseStat hookDamage = new BaseStat(this, "Hook Damage", 2, 4, 1, 1);
 
 	// Player Data
 	public BaseStat life = new BaseStat(this, "Life", 1, 20, 2, 1);
 	public BaseStat[] ref;
 	private int _life;
 
 	public Stats(PudgeEntity p) {
 		pudge = p;
 
 		ref = new BaseStat[CharStat.length];
 		ref[0] = life;
 		ref[1] = moveSpeed;
 		ref[2] = hookSize;
 		ref[3] = hookSpeed;
 		ref[4] = hookRange;
 		ref[5] = hookDamage;
 
 		_life = (int) life.getValue();
 
 		// Load the Images
 		expImages = new Image[4];
 		for (int i = 0; i < 4; i++)
 			expImages[i] = ImageHandler.get().getImage("exp_" + i);
 
 		// Life Images
 		lifeImages = new Image[2];
 		lifeImages[0] = ImageHandler.get().getImage("HUD_life_empty");
 		lifeImages[1] = ImageHandler.get().getImage("HUD_life_full");
 
 		hookCooldownImages = new Image[2];
 		hookCooldownImages[0] = ImageHandler.get().getImage("hook_empty");
 		hookCooldownImages[1] = ImageHandler.get().getImage("hook_full");
 		grappleCooldownImages = new Image[2];
 		grappleCooldownImages[0] = ImageHandler.get().getImage("grapple_empty");
 		grappleCooldownImages[1] = ImageHandler.get().getImage("grapple_full");
 	}
 
 	public void restoreDefaults() {
 		pudge.rigidbody.speed = moveSpeed.getValue();
 	}
 
 	/*
 	 * Network
 	 */
 	public String getNetString() {
 		String s = experience + "]" + _life;
 		for (int i = 0; i < ref.length; i++)
 			s += "]" + ref[i].getNetString();
 		return s;
 	}
 
 	public void setNetString(String s) {
 		String[] t = s.split("]");
 		experience = Integer.parseInt(t[0]);
 		_life = Integer.parseInt(t[1]);
 		for (int i = 2; i < ref.length + 2; i++)
 			ref[i - 2].setNetString(t[i]);
 	}
 
 	/*
 	 * Life
 	 */
 	public void set_life(int life) {
 		this._life = life;
 	}
 
 	public int life() {
 		return _life;
 	}
 
 	public double lifePercentage() {
 		return (double) _life / life.getValue();
 	}
 
 	public void addLife(int a) {
 		_life += a;
 		if (_life > life.getValue()) _life = (int) life.getValue();
 	}
 
 	public boolean subLife(int a) {
 		_life -= a;
 		if (_life <= 0) {
 			pudge.kill();
 			return true;
 		}
 		return false;
 	}
 
 	public void addExp(int amt) {
 		experience += amt;
		if (experience > (expImages.length - 1) * 10) experience = ((expImages.length - 1) * 10);
 		if (experience < 0) experience = 0;
 	}
 
 	public void onGUI() {
 		// Draw Life
 		GUI.partialHorizontalBar(lifeImages, 10, Game.s.height - 16, lifePercentage());
 
 		// Hook Cooldown
 		GUI.partialHorizontalBar(hookCooldownImages, 10, Game.s.height - 32, (1 - (pudge.hookCooldown / PudgeEntity.HOOK_COOLDOWN)));
 
 		// Grapple Cooldown
 		GUI.partialHorizontalBar(grappleCooldownImages, 10, Game.s.height - 48, (1 - (pudge.grappleCooldown / PudgeEntity.GRAPPLEHOOK_COOLDOWN)));
 
 		// Draw Experience
		if(experience < 30){
			Image tImg[] = { expImages[(int) (experience / 10)], expImages[(int) (experience / 10) + 1] };
			GUI.partialHorizontalBar(tImg, (Game.s.width - expImages[0].getWidth(null)) / 2, Game.s.height - expImages[0].getHeight(null), ((((int) experience) % 10) / 10.0));
		}else{
			Image tImg[] = { expImages[(int) (experience / 10)], expImages[(int) (experience / 10)] };
			GUI.partialHorizontalBar(tImg, (Game.s.width - expImages[0].getWidth(null)) / 2, Game.s.height - expImages[0].getHeight(null), ((((int) experience)) / 10.0));
		}
		
 		if (isOpen) {
 			for (int i = 0; i < CharStat.length; i++) {
 				if (ref[i].drawButtons(10, 10 + i * (16 + 4))) {
 					if (ref[i].getCost() <= experience) {
 						experience -= ref[i].getCost();
 						ref[i].setLevel(ref[i].level + 1);
 						pudge.shouldSendNetworkData = true;
 					}
 				}
 			}
 		}
 	}
 }
