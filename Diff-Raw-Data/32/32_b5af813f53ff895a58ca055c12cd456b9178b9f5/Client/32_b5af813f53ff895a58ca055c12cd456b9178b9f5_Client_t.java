 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.q1cc.cfs.mmm.client;
 
 import net.q1cc.cfs.mmm.common.world.WorldLoader;
 import net.q1cc.cfs.mmm.common.world.World;
 import java.io.File;
 import net.q1cc.cfs.mmm.client.render.MainGLRender;
 import net.q1cc.cfs.mmm.common.Info;
 import net.q1cc.cfs.mmm.common.Player;
 
 /**
  *
  * @author claus
  */
 public class Client {
     
     public static Client instance;
     public MainGLRender renderer;
     
     public World world;
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         System.setProperty("org.lwjgl.path","lib\\native\\windows");
         //System.getProperties().list(System.out);
         
         //process commandline
         //init something else
         Info.info = new ClientInfo();
         instance=new Client();
        instance.init();
     }
     
    public void init(){
         //display menu
         //connect to server or load world
         System.out.println(Runtime.getRuntime().maxMemory());
         loadWorld(new File("testworld/"));
         //System.out.println(world.generateOctree.toString(0)); //this takes some time.
         renderer=new MainGLRender(world);
         renderer.start();
         try {
             renderer.join();
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
         System.out.println("exiting");
     }
     
     private void loadWorld(File folder){
         world=WorldLoader.load(folder);
     }
     
 }
