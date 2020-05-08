 package atl.space.components.render;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL11.glColor4f;
 
 import java.awt.Font;
 import java.nio.FloatBuffer;
 import java.text.DecimalFormat;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 
 import utility.Camera;
 import atl.space.components.Component;
 
 public class DistanceDisplayComponent extends Overlay2DRenderComponent {
 	
 	private static final boolean DEBUG = true;
 	
 	private static DecimalFormat formatter = new DecimalFormat("0.00");
 	private Camera view;
 	private UnicodeFont font;
 	
 	private static float WIDTH = 640;
 	private static float HEIGHT = 480;
 	
 	public DistanceDisplayComponent(){
 		this((Camera)null);
 	}
 	
 	public DistanceDisplayComponent(Camera cam){
 		this(cam, new UnicodeFont(new Font("Arial", java.awt.Font.PLAIN,
 				18)));
 	}
 	
 	public DistanceDisplayComponent(Camera cam, UnicodeFont font){
 		super("distancedisplay");
		setOrthoProjMatrix(createOrthoMatrix(0, WIDTH,0, HEIGHT, 1, -1));
		//jasgsfkjaghkjfhagkj height is backwards!?!?!?
 		this.view = cam;
 		this.font = font;
 		setUpFonts();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void setUpFonts() {
 		font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
 		font.addAsciiGlyphs();
 		try {
 			font.loadGlyphs();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	public DistanceDisplayComponent(DistanceDisplayComponent ddc){
 		super(ddc);
 		this.view = ddc.view;
 	}
 	
 	@Override
 	public void render() {
 		FloatBuffer winpos = getWinPos();
 		float windowX = winpos.get(0);
 		float windowY = winpos.get(1);
		setUp2D();
		if (winpos.get(2) > 0 && winpos.get(2) < 1) {			
			renderDistance(windowX, windowY);	
 		}
		backTo3D();
 	}
 	
 	public void renderDistance(float windowX, float windowY) {
 		//glColor4f(1, 1, 1, 1);
 		//TODO make this work
 		/*if(DEBUG){
 			System.out.println(font.getFont());
 		}*/
 		font.drawString(
 				windowX,
 				windowY,
 				"What");
 		glDisable(GL_TEXTURE_2D);
 		//glEnable(GL_TEXTURE_2D);
 		
 	}
 	
 	
 	@Override
 	public Component clone() {
 		return new DistanceDisplayComponent(this);
 	}
 
 
 }
