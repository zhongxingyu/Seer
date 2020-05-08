 package nitrogene.weapon;
 
 import org.newdawn.slick.Sound;
 
 public enum EnumWeapon{
	BASIC ("res/Laser1.png", 3, 100F, 10, 0.7F, "res/LaserV2ro.png", "res/sound/laser1final.ogg", 0, 1000, 1),
     OP ("res/Laser1.png", 0, 100F, 1000, 3F, "res/LaserV2ro.png", "res/sound/laser1final.ogg", 0, 100, 1),
 	PULSAR ("res/klaar_pulsar_2.png", 0, 70f, 5, 0.7f, "res/pulsar_bolt.png", "res/sound/laser1final.ogg", 200, 1500, 3);
 	
 	public int accuracy; //Accuracy
 	public float speed; //Laser Speed
 	public int damage; //DUH
 	public float size;
 	public String image;
 	public String laserimage;
 	public String firesound;
 	public int interburst, outerburst, burstnumber;
 	//im is the launcehrs image
 	//laserimage is the projectiles image
 	//interburst is time inbetween shots in a burst --> for non-burst, make this 0
 	//outerburst is the time between bursts --> for non-burst, use this as the time varaible between single shots
 	//burstnumber is the amt of shots in a burst --> for a non-burst, make this 1 (single shot per burst)
 	EnumWeapon(String im, int accuracy1, float speed1, int damage1, float size1, String laserimage, String firesound, int interburst, int outerburst, int burstnumber) {
 		this.image = im;
 		this.laserimage = laserimage;
 		this.accuracy = accuracy1;
 		this.speed = speed1;
 		this.damage = damage1;
 		this.size = size1;
 		this.firesound = firesound;
 		this.interburst = interburst;
 		this.outerburst = outerburst;
 		this.burstnumber = burstnumber;
 	}
     
 }
