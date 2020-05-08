 package glfx;
 
 import engine.Engine;
 
 public class GLFX
 {
 	
 	
 	
 	
 	public static void main(String[] args)
 	{
 		// Initialize Engine
 		Engine engine=new Engine(1024,768);
		engine.setIcon("/imgs/icon32.png");
 		Engine.setTitle("OpenGL FrameworkX");
 		
 		engine.testLoop();
 	}
 }
