 package fr.utbm.vi51.gui;
 
 import org.janusproject.kernel.Kernel;
 import org.janusproject.kernel.agent.Kernels;
 
 import fr.utbm.vi51.agent.Queen;
 import fr.utbm.vi51.agent.Warrior;
 import fr.utbm.vi51.agent.Worker;
 import fr.utbm.vi51.environment.Environment;
 import fr.utbm.vi51.environment.Food;
 import fr.utbm.vi51.environment.LandType;
 import fr.utbm.vi51.environment.Side;
 import fr.utbm.vi51.environment.Square;
 import fr.utbm.vi51.util.AgentScheduler;
 import fr.utbm.vi51.util.Point3D;
 
 /**
  * @author Top-K
  * 
  */
 public final class Main {
 
     private Main() {
     }
 
     public static void main(String[] args) throws InterruptedException {
         // Generate environment map
         generateMap1();
         Environment env = Environment.getInstance();
         Square[][][] map = Environment.getInstance().getMap();
         // SenderAgent b = new SenderAgent();
         //KernelTimeManager tm = new VMKernelTimeManager();
         AgentScheduler as = new AgentScheduler();
         Kernel k = Kernels.create(as);
         k.launchLightAgent(env);
         // k.launchLightAgent(b);
         Side a = new Side(1,"img/Ants/worker1.png","img/Ants/warrior1.png","img/Ants/queen1.png");
         Side b = new Side(2,"img/Ants/worker2.png","img/Ants/warrior2.png","img/Ants/queen2.png");
 
         Queen q1 = new Queen(new Point3D(5, 6, 0), 1, a, k);
         Queen q2 = new Queen(new Point3D(35, 35, 0), 1, b, k);
         k.launchLightAgent(q1);
         k.launchLightAgent(q2);
         
         //k.launchLightAgent(new Warrior(new Point3D(35, 34, 0), 10, b));
         //k.launchLightAgent(new Worker(new Point3D(7, 8, 0), 15,a));
        // k.launchLightAgent(new Worker(new Point3D(7, 8, 0), 15,a));
         for (int i = 0; i < 50; ++i) {
             k.launchLightAgent(new Warrior(new Point3D(20, 20, 0), 15,b));
            //k.launchLightAgent(new Warrior(new Point3D(25,25, 0), 15,a));
         }
 
 
        for (int i = 0; i < 200; ++i) {
             k.launchLightAgent(new Worker(new Point3D(5, 6, 0), 15,a));
            //k.launchLightAgent(new Worker(new Point3D(35, 35, 0), 15,b));
        }
 
         k.launchHeavyAgent(new WindowsContainer());
 
         for (int i = 0; i < 20; ++i) {
             new Food(new Point3D(12, 12, 0));
             new Food(new Point3D(19, 19, 0));
             new Food(new Point3D(3, 17, 0));
             new Food(new Point3D(17, 6, 0));
             new Food(new Point3D(23, 23, 0));
             new Food(new Point3D(5, 35, 0));
             new Food(new Point3D(30, 33, 0));
             new Food(new Point3D(16, 30, 0));
 
         }
     }
 
     // Functions for map g�n�ration
     private static boolean generateMap1() throws InterruptedException {
         // Getting the environment from the singleton
         Environment env = Environment.getInstance();
         // Getting environment map
         Square[][][] map = env.getMap();
 
         // Generation of top level full of grass
         for (int i = 0; i < map.length; i++) {
             for (int j = 0; j < map[0].length; j++) {
                 map[i][j][0] = new Square(LandType.GRASS);
             }
         }
 
         for (int i = 3; i < 7; i++) {
             for (int j = 3; j < 10; j++) {
                 map[i][j][0] = new Square(LandType.WATER);
             }
         }
 
         for (int i = 3; i < 19; i++) {
             map[i][10][0] = new Square(LandType.WALL);
             map[10][i][0] = new Square(LandType.WALL);
         }
 
         for (int i = 9; i < 12; i++) {
             for (int j = 9; j < 12; j++) {
                 map[i][j][0] = new Square(LandType.WATER);
             }
         }
 
         // Some walls around the map
         for (int i = 0; i < map.length; ++i) {
             map[i][0][0] = new Square(LandType.WALL);
             map[i][map[0].length - 1][0] = new Square(LandType.WALL);
         }
         for (int i = 0; i < map.length; ++i) {
             map[0][i][0] = new Square(LandType.WALL);
             map[map.length - 1][i][0] = new Square(LandType.WALL);
         }
 
         return true;
     }
 }
