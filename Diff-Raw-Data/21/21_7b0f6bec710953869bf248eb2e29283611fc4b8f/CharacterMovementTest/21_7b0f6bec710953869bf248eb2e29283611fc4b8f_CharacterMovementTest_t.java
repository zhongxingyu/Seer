 package tests;
 
 import java.io.IOException;
 
 import math.Vector2;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import graphics.Color;
 
 import components.InputComponent;
 import components.PhysicsComponent;
 import components.PositionComponent;
 import components.RenderingComponent;
 
 import utils.Rectangle;
 import entityFramework.IEntity;
 import entityFramework.IEntityManager;
import entitySystems.RenderingSystem;
 import entitySystems.CharacterControllerSystem;
 import entitySystems.InputSystem;
 import entitySystems.PhysicsSystem;
 import graphics.TextureLoader;
 /**
  * 
  * @author Lukas
  *
  */
 public class CharacterMovementTest extends AbstractSystemTest{
 
 	public CharacterMovementTest(Rectangle screenDim, boolean fullScreen) {
 		super(screenDim, fullScreen);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void initializeSystems() {
 		tester.addLogicubSystem(new CharacterControllerSystem(this.tester.getWorld()));
 		tester.addLogicubSystem(new InputSystem(this.tester.getWorld()));
 		tester.addLogicubSystem(new PhysicsSystem(this.tester.getWorld()));
		tester.addRenderSubSystem(new RenderingSystem(this.tester.getWorld(), this.graphics));
 	}
 
 	@Override
 	public void initializeEntities(IEntityManager manager) {
 		IEntity entity = manager.createEmptyEntity();
 		
 		PositionComponent posC = new PositionComponent();
 		posC.setPosition(new Vector2(0,0));
 		
 		PhysicsComponent physC = new PhysicsComponent();
 		physC.setVelocity(new Vector2(0,0));
 		
 		InputComponent inpC = new InputComponent();
 
 		inpC.setBackButton(Keyboard.KEY_S);
 		inpC.setLeftButton(Keyboard.KEY_A);
 		inpC.setForwardButton(Keyboard.KEY_W);
 		inpC.setRightButton(Keyboard.KEY_D);
 		inpC.setGallopButton(Keyboard.KEY_LSHIFT);
 		inpC.setPipBuckButton(Keyboard.KEY_TAB);
 		
 		RenderingComponent rendC = new RenderingComponent();
 		rendC.setColor(new Color(255,255,255,255));
 		try {
 			rendC.setTexture(TextureLoader.loadTexture(TextureTest.class.getResourceAsStream("HEJHEJ.png")));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		entity.addComponent(rendC);
 		entity.addComponent(inpC);
 		entity.addComponent(physC);
 		entity.addComponent(posC);
 		
 		entity.refresh();
 		System.out.println(	this.tester.getWorld().getDatabase().getEntityCount());
 	}
 	
 	public static void main(String[] args) throws LWJGLException {
 		new CharacterMovementTest(new Rectangle(0,0, 800, 600), false).start();
 	}
 
 }
