 package tests;
 
 import java.io.IOException;
 
 import org.lwjgl.LWJGLException;
 
 import components.InputComponent;
 import components.PhysicsComponent;
 import components.RenderingComponent;
 
 import entityFramework.IEntity;
 import entityFramework.IEntityManager;
 import entitySystems.AttackResolveSystem;
import entitySystems.RenderingSystem;
 import entitySystems.CharacterControllerSystem;
 import entitySystems.InputSystem;
 import entitySystems.PhysicsSystem;
 import graphics.Color;
 import graphics.TextureLoader;
 import utils.Rectangle;
 
 public class SimpleShootingTest extends AbstractSystemTest{
 	
 	public static void main(String[] args) throws LWJGLException{
 		new SimpleShootingTest(new Rectangle(0,0, 1366, 768), true).start();
 	}
 
 	public SimpleShootingTest(Rectangle screenDim, boolean fullScreen) {
 		super(screenDim, fullScreen);
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
 		IEntity player = manager.createEmptyEntity();
 		
 		PhysicsComponent physComp = new PhysicsComponent();
 		InputComponent inpComp = new InputComponent();
 		RenderingComponent rendComp = new RenderingComponent();
 		
 		rendComp.setColor(new Color(42,200,255, 255));
 		try {
 			rendComp.setTexture(TextureLoader.loadTexture(TextureTest.class.getResourceAsStream("PPieLauncher.png")));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		player.addComponent(rendComp);
 		player.addComponent(inpComp);
 		player.addComponent(physComp);
 		
 		player.refresh();
 	}
 
 }
