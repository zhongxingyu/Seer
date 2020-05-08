 package android.topdown.game;
 
 import android.gameengine.icadroids.sound.GameSound;
 
 public class SoundLib implements Runnable{
 	private static boolean loaded = false;
 	public static final int FERDI_BANAAN = 0;
 	public static final int FERDI_BANAAN2 = 1;
 	public static final int FERDI_BANAAN3 = 2;
 	public static final int FERDI_BANAAN4 = 3;
 	public static final int FERDI_BITCHPLEASE = 4;
 	public static final int FERDI_BITCHPLEASE2 = 5;
 	public static final int FERDI_COMEGETSOME = 6;
 	public static final int FERDI_FULLOFCRAP = 7;
 	public static final int FERDI_GOTMOTHERFUCKINGPILLS = 8;
 	public static final int SFX_GUNCLICK = 9;
 	public static final int FERDI_HA = 10;
 	public static final int FERDI_HMM = 11;
 	public static final int FERDI_HURT1 = 12;
 	public static final int FERDI_HURT2 = 13;
 	public static final int FERDI_HURT3 = 14;
 	public static final int FERDI_HURTSCREAM = 15;
 	public static final int FERDI_JIJGAATDOOD = 16;
 	public static final int FERDI_MOAN = 17;
 	public static final int FERDI_MOTHERFUCKINGFOOD = 18;
 	public static final int FERDI_MOTHERFUCKINGSHOTGUN = 19;
 	public static final int FERDI_NEEDMOREFOOD = 20;
 	public static final int FERDI_PILLS = 21;
 	public static final int SFX_PISTOL = 22;
 	public static final int FERDI_PUNCHINBALLS = 23;
 	public static final int FERDI_RAPESCREAM = 24;
 	public static final int FERDI_RELOADINGTHEMBITCHES = 25;
 	public static final int FERDI_SCREAM = 26;
 	public static final int SFX_SHOTGUN = 27;
 	public static final int FERDI_SHOTGUNFERDI = 29;
 	public static final int FERDI_TAKEITLIKEABOSS = 30;
 	public static final int FERDI_TASTYBACONSTRIPS = 31;
 	public static final int FERDI_THATSUREWASEASY = 32;
 	public static final int ZOMBIE_ZOMGRUNT = 33;
 	public static final int ZOMBIE_ZOMGRUNT2 = 34;
 	public static final int ZOMBIE_ZOMGRUNT3 = 35;
 	public static final int ZOMBIE_ZOMMOAN1 = 36;
 	public static final int ZOMBIE_ZOMMOAN2 = 37;
 	public static final int ZOMBIE_ZOMMOAN3 = 38;
 	public static final int ZOMBIE_ZOMMOAN4 = 39;
 	public static final int ZOMBIE_ZOMMUMBLE = 40;
 	public static final int ZOMBIE_ZOMMUMBLE2 = 41;
 	public static final int ZOMBIE_ZOMMUMBLE3 = 42;
 	public static final int ZOMBIE_ZOMSCREAM = 43;
 	public static final int ZOMBIE_ZOMSCREAM2 = 44;
 	public static final int ZOMBIE_ZOMSCREAM3 = 45;
 	public static final int ZOMBIE_ZOMSCREAM4 = 46;
 	public static final int ZOMBIE_ZOMSHORT = 47;
 	public static final int ZOMBIE_ZOMSHORT2 = 48;
 	public static final int ZOMBIE_ZOMSHORT3 = 49;
 	public static final int FERDI_ZONDERSOUND = 50;
 
	private static Thread t = new Thread(new SoundLib());
 	public void load() {
 		loaded = false;
 		loadSound(FERDI_BANAAN, "banaan");
 		loadSound(FERDI_BANAAN2, "banaan2");
 		loadSound(FERDI_BANAAN3, "banaan3");
 		loadSound(FERDI_BANAAN4, "banaan4");
 		loadSound(FERDI_BITCHPLEASE, "bitchplease");
 		loadSound(FERDI_BITCHPLEASE2, "bitchplease2");
 		loadSound(FERDI_COMEGETSOME, "comegetsome");
 		loadSound(FERDI_FULLOFCRAP, "fullofcrap");
 		loadSound(FERDI_GOTMOTHERFUCKINGPILLS, "gotmotherfuckingpills");
 		loadSound(SFX_GUNCLICK, "gunclick");
 		loadSound(FERDI_HA, "ha");
 		loadSound(FERDI_HMM, "hmm");
 		loadSound(FERDI_HURT1, "hurt1");
 		loadSound(FERDI_HURT2, "hurt2");
 		loadSound(FERDI_HURT3, "hurt3");
 		loadSound(FERDI_HURTSCREAM, "hurtscream");
 		loadSound(FERDI_JIJGAATDOOD, "jijgaatdood");
 		loadSound(FERDI_MOAN, "moan");
 		loadSound(FERDI_MOTHERFUCKINGFOOD, "motherfuckingfood");
 		loadSound(FERDI_MOTHERFUCKINGSHOTGUN, "motherfuckingshotgun");
 		loadSound(FERDI_NEEDMOREFOOD, "needmorefood");
 		loadSound(FERDI_PILLS, "pills");
 		loadSound(SFX_PISTOL, "pistol");
 		loadSound(FERDI_PUNCHINBALLS, "punchinballs");
 		loadSound(FERDI_RAPESCREAM, "rapescream");
 		loadSound(FERDI_RELOADINGTHEMBITCHES, "reloadingthembitches");
 		loadSound(FERDI_SCREAM, "scream");
 		loadSound(SFX_SHOTGUN, "shotgun");
 		loadSound(FERDI_SHOTGUNFERDI, "shotgunferdi");
 		loadSound(FERDI_TAKEITLIKEABOSS, "takeitlikeaboss");
 		loadSound(FERDI_TASTYBACONSTRIPS, "tastybaconstrips");
 		loadSound(FERDI_THATSUREWASEASY, "thatsurewaseasy");
 		loadSound(ZOMBIE_ZOMGRUNT, "zomgrunt");
 		loadSound(ZOMBIE_ZOMGRUNT2, "zomgrunt2");
 		loadSound(ZOMBIE_ZOMGRUNT3, "zomgrunt3");
 		loadSound(ZOMBIE_ZOMMOAN1, "zommoan1");
 		loadSound(ZOMBIE_ZOMMOAN2, "zommoan2");
 		loadSound(ZOMBIE_ZOMMOAN3, "zommoan3");
 		loadSound(ZOMBIE_ZOMMOAN4, "zommoan4");
 		loadSound(ZOMBIE_ZOMMUMBLE, "zommumble");
 		loadSound(ZOMBIE_ZOMMUMBLE2, "zommumble2");
 		loadSound(ZOMBIE_ZOMMUMBLE3, "zommumble3");
 		loadSound(ZOMBIE_ZOMSCREAM, "zomscream");
 		loadSound(ZOMBIE_ZOMSCREAM2, "zomscream2");
 		loadSound(ZOMBIE_ZOMSCREAM3, "zomscream3");
 		loadSound(ZOMBIE_ZOMSCREAM4, "zomscream4");
 		loadSound(ZOMBIE_ZOMSHORT, "zomshort");
 		loadSound(ZOMBIE_ZOMSHORT2, "zomshort2");
 		loadSound(ZOMBIE_ZOMSHORT3, "zomshort3");
 		loadSound(FERDI_ZONDERSOUND, "zondersound");
 		loaded = true;
 	}
 	
 	private void loadSound(int id, String name){
 		try{
 			GameSound.addSound(id,name);
 		} catch(Exception e){
 			System.err.println("Failed to load sound: '"+name+"'\n"+e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	public static void play(int id) {
 			try{
 			GameSound.playSound(id, 0);
 			} catch (Exception e) {
 				System.err.println("ERR: Sound not loaded");
				loaded = false;
				if(loading())
					startLoad();
 			}
 	}
 
 	public void run() {
 		if(!loaded){// weet niet of dit nodig is maar zet het er toch in
 			load();
 		}
 	}
	public static boolean loading(){
		return !t.isAlive()&&loaded==false;
	}
	public static void startLoad(){
		t = new Thread(new SoundLib());
		t.setPriority(7);
		t.start();
	}
 }
