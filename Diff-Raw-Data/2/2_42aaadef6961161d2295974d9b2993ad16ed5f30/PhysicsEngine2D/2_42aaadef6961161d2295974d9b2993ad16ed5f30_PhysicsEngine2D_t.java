 package de.engine.math;
 
 import de.engine.environment.Scene;
 import de.engine.objects.Circle;
 import de.engine.objects.ObjectProperties;
 
 
 public class PhysicsEngine2D implements Runnable
 {
     private Scene scene;
     public boolean semaphore = true;
 
     
     public PhysicsEngine2D()
     {
         //
     }
     
     
 	public void Rotation()
 	{
 		//    
 	}
 	
 	
 	public void Translation()
 	{
 		//
 	}
 
 	
 	public void setScene( Scene scene )
 	{
 	    this.scene = scene;
 	}
 
     
     @Override
     public void run()
     {
         // Will be changed soon!
         while( true ) 
         {
             System.out.println( "running..." );
             
             while( semaphore ) 
             {      
                 try
                 {
                     // do a break for 1/30 second
                     Thread.sleep( 33 );
                 }
                 catch (InterruptedException e)
                 {
                     e.printStackTrace();
                 }
                 
                 // Checks collision between circles and ground
                 if (scene.getGround()!=null) 
                 {
 //                    System.out.println( scene.getGround().function( scene.getGround().DOWNHILL, e.getLocation().x ) +" | "+ e.getLocation().y );
                 }
             }
             
             try
             {
                 // do another break for 1/30 second
                 Thread.sleep( 33 );
             }
             
             catch (InterruptedException e)
             {
                 e.printStackTrace();
             }
             
 
             // Repaint 
             // perhaps creating a new thread which updates itself a 1/30 second will be fine
             scene.getCanvas().repaint();
         }
     }
     
     
     // here starts the entry point for all the physical calculation
     public void calculateNextFrame( double deltaTime)
     {
         double oldposition = 0;
         
         for (ObjectProperties obj : scene.getObjects())
         {
             if (obj instanceof Circle) 
             {
                 // Collision detection
                 
                 oldposition = obj.position.getPoint().y;
                obj.position.getPoint().y = -9.81/2d*deltaTime + obj.velocity.getPoint().y * (double)deltaTime + obj.position.getPoint().y;
                 
                 obj.velocity.getPoint().y = (obj.position.getPoint().y-oldposition)/deltaTime;
             }
         }
     }
 }
