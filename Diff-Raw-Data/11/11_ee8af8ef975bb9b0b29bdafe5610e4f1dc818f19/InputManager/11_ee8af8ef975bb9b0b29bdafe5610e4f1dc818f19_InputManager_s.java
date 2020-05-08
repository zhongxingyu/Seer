 package edu.hsbremen.cb.shaderama.input;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.util.vector.Vector3f;
 
 import edu.hsbremen.cb.shaderama.core.ApplicationSettings;
 import edu.hsbremen.cb.shaderama.core.Core;
 import edu.hsbremen.cb.shaderama.core.Manager;
 
 public class InputManager extends Manager {
 
 	private boolean running = true;
 	private ApplicationSettings settings;
 	
 	private Vector3f moveDelta = new Vector3f(0,0,0);
 	private Vector3f rotationDelta = new Vector3f(0,0,0);
 	
 	private boolean[] keyList = new boolean[Keyboard.KEYBOARD_SIZE];
 	private Object keyLock = new Object();
 	private boolean mouseMove = false;
 	private int tempX;
 	private int tempY;
 	
 	
 	public InputManager(Core core, ApplicationSettings settings) {
 		super(core);
 		this.settings = settings;
 		this.setDaemon(true);
 	}
 	
 	
 
 	@Override
 	public void run() {
 		System.out.println("Starting Input Manager.");
 		
 		int skipTime = 1000 / settings.in_maxticks;
 		while(running) {
 			long before = System.currentTimeMillis();
 			getKeyEvents();
 			handleKeys();
 			handleMouseMove();
 			long after = System.currentTimeMillis();
 			long gone = after - before;
 			long wait = skipTime - gone;
 			try {
 				if (wait > -1) Thread.sleep(skipTime - gone);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				throw new RuntimeException("Thread.sleep error in Inputmanagar", e);
 			}
 		}
 	}
 
 	private void handleMouseMove() {
 		//TODO: ugly code, refactor and make better
 		
 		if(Mouse.isButtonDown(0)) {
 			if(!mouseMove) {
 				tempX = Mouse.getX();
 				tempY = Mouse.getY();
 				mouseMove = true;
 			}
 //			int dx = Mouse.getDX();
 //			if(dx != 0) {
 //				float dx1 = ((float)(settings.r_width / dx));
 //				rotationDelta.y += dx1 * settings.m_yaw;
 //			}
 //			
 //			int dy = Mouse.getDY();
 //			if(dy != 0) {
 //				float dy1 = ((float)(settings.r_height / dy))*settings.r_aspectRatio;
 //				rotationDelta.x += dy1 * settings.m_pitch;
 //			}
 			
 			rotationDelta.x += (float)(tempY - Mouse.getY()) * settings.m_pitch;
 			rotationDelta.y += (float)(tempX - Mouse.getX()) * settings.m_yaw;
 		}
 		if(!Mouse.isButtonDown(0)) {
 			mouseMove = false;
 		}
 	}
 
 	private void getKeyEvents() {
 		while(Keyboard.next()) {
 			if(Keyboard.getEventKeyState()) {
 				keyList[Keyboard.getEventKey()] = true;
 			} else {
 				keyList[Keyboard.getEventKey()] = false;	
 			}
 		}
 	}
 
 
 	private void handleKeys() {
 		synchronized (keyLock) {
 			float moveSpeed = 1f / settings.c_movementSpeed;
 			if(keyList[settings.k_forward]) {
 				moveDelta.z += moveSpeed;
 			} 
 			if(keyList[settings.k_backward]) {
 				moveDelta.z -= moveSpeed;
 			} 
 			if(keyList[settings.k_leftstrafe]) {
 				moveDelta.x += moveSpeed;
 			} 
 			if(keyList[settings.k_rightstrafe]) {
 				moveDelta.x -= moveSpeed;
 			}
 			if(keyList[settings.k_up]) {
 				moveDelta.y -= moveSpeed;
 			}
 			if(keyList[settings.k_down]) {
 				moveDelta.y += moveSpeed;
 			}
 		}
 	}
 	
 	public Vector3f nextMoveDelta() {
 		Vector3f temp = this.moveDelta;
 		synchronized (keyLock) {
 			this.moveDelta = new Vector3f(0,0,0);
 		}
 		return temp;
 	}
 	
 	public Vector3f nextRotationDelta() {
 		Vector3f temp = this.rotationDelta;
 		synchronized (keyLock) {
 			this.rotationDelta = new Vector3f(0,0,0);
 		}
 		return temp;
 	}
 
 }
