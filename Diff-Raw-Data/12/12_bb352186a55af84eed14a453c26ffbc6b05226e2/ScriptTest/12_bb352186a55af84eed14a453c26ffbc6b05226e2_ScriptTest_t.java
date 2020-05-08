 package cat.atridas.antagonista.test;
 
 import java.io.FileNotFoundException;
 import java.util.logging.Level;
 
 import javax.vecmath.Color3f;
 import javax.vecmath.Vector3f;
 
 import cat.atridas.antagonista.HashedString;
 import cat.atridas.antagonista.Transformation;
 import cat.atridas.antagonista.Utils;
 import cat.atridas.antagonista.core.Core;
 import cat.atridas.antagonista.defensa.EntityFactory;
 import cat.atridas.antagonista.entities.EntityManager;
 import cat.atridas.antagonista.entities.SystemManager;
 import cat.atridas.antagonista.graphics.DebugRender;
 import cat.atridas.antagonista.graphics.MeshManager;
 import cat.atridas.antagonista.graphics.RenderManager;
 import cat.atridas.antagonista.graphics.SceneData;
 import cat.atridas.antagonista.input.InputManager;
 
 public class ScriptTest {
   /**
    * Arguments de la VM interessants:
    * 
    * -ea -Djava.library.path="./native/windows" 
    * -Xmx1024m -XX:+UseG1GC
    * -XX:MaxGCPauseMillis=5
    * -XX:GCPauseIntervalMillis=83
    * -verbose:gc 
    * -Dcom.sun.management.jmxremote
    *  
    * @param args
    * @throws FileNotFoundException 
    */
   public static void main(String[] args) throws FileNotFoundException {
     //comprovem que els asserts estiguin actius
     boolean assertsActives = false;
     assert (assertsActives = true) == true;
     if(!assertsActives)
       throw new RuntimeException("Falta activar els asserts");
     
     Utils.setConsoleLogLevel(Level.FINEST);
     
 
     Core core = Core.getCore();
     core.init(800, 600, TestEntities.class.getName(), true, null);
 
     
     //ScriptManager scriptManager = new ScriptManager("data/xml/scriptManager.xml");
     
     //cat.atridas.antagonista.entities.System pyRTSCameraSystem = scriptManager.createNewInstance("RTSCameraSystem", cat.atridas.antagonista.entities.System.class);
     
     ///////////////////////////////////////////////////////////////////////////////////////
     
     SystemManager sm = core.getSystemManager();
     /*
     sm.registerSystem(new RTSCameraSystem());
     //sm.registerSystem(pyRTSCameraSystem);
     
     sm.registerSystem(new PhysicsCharacterControllerSystem());
     sm.registerSystem(new RigidBodySystem());
     
     sm.registerSystem(new RenderingCameraSystem());
     sm.registerSystem(new RenderingSystem());
     */
     sm.registerSystem("RTSCameraSystem");
     
     sm.registerSystem("PhysicsCharacterControllerSystem");
     sm.registerSystem("RigidBodySystem");
     
     sm.registerSystem("RenderingCameraSystem");
     sm.registerSystem("RenderingSystem");
     
     
     
     ///////////////////////////////////////////////////////////////////////////////////////
     
     InputManager im = core.getInputManager();
     RenderManager rm = core.getRenderManager();
     
     EntityManager em = core.getEntityManager();
     MeshManager mm = core.getMeshManager();
 
     im.loadActions("data/xml/inputManager.xml");
     im.activateMode(Utils.MAIN_GAME);
     
     DebugRender dr = core.getDebugRender();
     dr.activate();
     core.setPhysicsDebugRender(true);
     
     ///////////////////////////////////////////////////////////////////////////////////////////
     
 
     SceneData sceneData = rm.getSceneData();
     sceneData.setAmbientLight(new Color3f(0.3f, 0.3f, 0.3f));
     sceneData.setDirectionalLight(new Vector3f(0.5f,1,-1), new Color3f(0.3f, 0.3f, 0.3f));
     
     
     
     Transformation position = new Transformation();
     
     EntityFactory.createRajola(em, mm, position, new HashedString("TerraBasic"));
     
     EntityFactory.createCamera(em, new HashedString("Camera"));
     
     
     
     //scriptManager.load("data/scripts/test.py");
     
     //String script = "from data.scripts.test import catacrocker\n";
     
     //scriptManager.execute(script);
     
     while(!im.isCloseRequested() && !im.isActionActive(Utils.CLOSE)) {
 
       core.performSimpleTick();
       
     }
     
     core.cleanUnusedResources(false);
     core.cleanUnusedResources(true);
     
     core.close();
   }
 }
