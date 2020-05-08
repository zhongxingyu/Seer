 package engenhoka.balloons;
 
 import javafx.scene.image.Image;
 import javafx.scene.media.AudioClip;
 
 public class Resources {
 	public final static Image iwatinha = new Image(Resources.class.getResourceAsStream("logos/iwatinha.png"));
 	public final static Image iwatinha_happy = new Image(Resources.class.getResourceAsStream("iwatinha-happy.png"));
 	public final static Image iwatinha_sad = new Image(Resources.class.getResourceAsStream("iwatinha-sad.png"));
 	public final static Image logo = new Image(Resources.class.getResourceAsStream("logo.png"));
 	
 //	public final static Image pow = new Image(Resources.class.getResourceAsStream("pow.png"));
 	public final static AudioClip pop = new AudioClip(Resources.class.getResource("pop.mp3").toString());
 	public final static AudioClip applause = new AudioClip(Resources.class.getResource("applause.mp3").toString());
 	public final static AudioClip trumpet = new AudioClip(Resources.class.getResource("trumpet.mp3").toString());
 	
 	private static final String colors[] = { "blue", "green", "pink", "red", "yellow", "orange" };
 	
 	public final static Image[] balloons = new Image[colors.length];
 	public final static Image[] pows = new Image[colors.length];
 
	public static final int LOGO_COUNT = 8;
 	public final static Image[] logos = new Image[LOGO_COUNT+1];
 	
 	static {
 		for(int i = 0; i < colors.length; i++) {
 			System.out.println("Resources initing color " + colors[i]);
 			balloons[i] = new Image(Resources.class.getResourceAsStream("balloon-" + colors[i] + ".png"));
 			pows[i] = new Image(Resources.class.getResourceAsStream("splashes/splash-" + colors[i] + ".png"));
 		}
 
 		for(int i = 0; i < LOGO_COUNT; i++)
 			logos[i] = new Image(Resources.class.getResourceAsStream(String.format("logos/logo-%02d.jpg", i+1)));
 		
 		logos[LOGO_COUNT] = iwatinha;
 	}
 }
 
