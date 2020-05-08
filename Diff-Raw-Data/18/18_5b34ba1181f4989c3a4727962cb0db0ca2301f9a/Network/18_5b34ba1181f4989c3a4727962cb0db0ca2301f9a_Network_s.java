 package common;
 
import math.Vector2;
<<<<<<< HEAD
=======
import utils.Keys;
>>>>>>> Moar GUI
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryonet.EndPoint;
 import components.InputComp;
 import entityFramework.IEntityArchetype;
 import graphics.Color;
 /**
  * 
  * @author Joakim Johansson
  *
  */
 public class Network {
 	
 	public static void registerClasses(EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
	
<<<<<<< HEAD
=======
		kryo.register(Keys.class);
>>>>>>> Moar GUI
 		kryo.register(Vector2.class);
 		kryo.register(IEntityArchetype.class);
 		
 		kryo.register(InputComp.class);
 		kryo.register(components.PhysicsComp.class);
 		kryo.register(components.RenderingComp.class);
 		kryo.register(components.TransformationComp.class);
 		kryo.register(components.AbilityComp.class);
 		kryo.register(components.AnimationComp.class);
 		kryo.register(components.BehaviourComp.class);
 		kryo.register(components.SpecialComp.class);
 		kryo.register(components.SpatialComp.class);
 		
 		kryo.register(NewPlayerMessage.class);
 		kryo.register(EntityNetworkIDsetMessage.class);
 		kryo.register(EntityMovedMessage.class);
 		kryo.register(PlayerCharacteristics.class);
 		kryo.register(Color.class);
 		kryo.register(ChatMessage.class);
		kryo.register(utils.Keys.class);
 		kryo.register(utils.Keyboard.class);
 		kryo.register(utils.ButtonState.class);
 
 		kryo.register(utils.Mouse.class);
 		kryo.register(utils.MouseState.class);
 		kryo.register(java.util.ArrayList.class);
 	}
 }
