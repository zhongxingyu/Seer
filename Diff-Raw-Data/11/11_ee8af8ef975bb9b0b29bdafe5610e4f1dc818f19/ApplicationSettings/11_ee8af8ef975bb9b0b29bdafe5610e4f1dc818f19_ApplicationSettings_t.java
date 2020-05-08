 package edu.hsbremen.cb.shaderama.core;
 
 import org.lwjgl.input.Keyboard;
 
 public class ApplicationSettings {
 	public int r_width = 800;
 	public int r_height = 600;
 	public float r_aspectRatio = r_width / r_height;
 	
 	public int r_maxfps = 60;
 	
 	public int in_maxticks = 30;
 	
 	//movement
 	public float c_movementSpeed = 10;
 	
 	//mouse
 	public float m_turnSpeed = 10;
	public float m_pitch = 0.05f;
	public float m_yaw = 0.08f;
 	
 	
 	//keys
 	public int k_forward = Keyboard.KEY_W;
 	public int k_leftstrafe = Keyboard.KEY_A;
 	public int k_rightstrafe = Keyboard.KEY_D;
 	public int k_backward = Keyboard.KEY_S;
 	public int k_up = Keyboard.KEY_SPACE;
 	public int k_down = Keyboard.KEY_X;
 	
 }
