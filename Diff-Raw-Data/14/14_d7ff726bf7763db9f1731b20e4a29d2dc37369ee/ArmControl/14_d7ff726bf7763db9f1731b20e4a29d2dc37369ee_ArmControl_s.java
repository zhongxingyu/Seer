 package actuation;
 
 import java.awt.Dimension;
 
 import communication.DynamixelSerial;
 
 public class ArmControl implements Runnable{
 	
 	DynamixelArm da = new DynamixelArm();
 	
 	
 	
 	private int Home = 190;
 	
 	int GLOBALOFFSET = -12;
 	
<<<<<<< HEAD
<<<<<<< HEAD
	int tableHeight = 70;
=======
 	int tableHeight = 67;
>>>>>>> Can drop coins in buckets
 	int botHeight = 100;
=======
	int tableHeight = 55;
	int botHeight = 150;
>>>>>>> tracking working (Ben)
 	int xoffset = 180;
 	
 	int speed = 150;
 	
 	int BoxS1 = 85;
 	int BoxS2 = -83;
 	
 	int Box1 = 130+GLOBALOFFSET;
 	int Box2 = 70+GLOBALOFFSET;
 	int Box3 = -120-GLOBALOFFSET;
 	int Box4 = -70-GLOBALOFFSET;
 	boolean atHome = false;
 
 	public ArmControl(){
 		//this.goHome();
 		da.port = 3;
 	}
 	public static void main(String[] args) {
 		ArmControl ac;
 		(new Thread(ac = new ArmControl())).start();
 		
 		
 		//ac.goTo(ac.Box1, 150, 0);
 		//ac.da.setAng(0, 90, 200);
 		//while(ac.da.ds.readMoving()){DynamixelSerial.halt(10);}
 		ac.goHome();
 		ac.get(200);
 		ac.put(1);
 		ac.get(170);
 		ac.put(1);
 		ac.get(170);
 		ac.put(2);
 		ac.get(170);
 		ac.put(3);
 		ac.get(170);
 		ac.put(4);
 
 		
 		
 		/**for(int i =160; i<170;i++){
 			ac.get(i);
 
 			if(i%2 == 0){ac.put(2);}else{ac.put(1);}
 		}**/
 		
 		System.out.println("Done");
 		
 		
 		//ac.goTo(ac.Box2-5, ac.botHeight-20, 0);
 		//ac.goTo(ac.Box2-5, ac.botHeight+20, 0);
 		
 		//ac.goTo((int)(ac.da.L2), (int)(ac.da.L1), 0);
 		//ac.goTo(ac.Box2, ac.botHeight-20, -40);
 
 		
 		
 		
 		
 		//ac.goTo(ac.BoxS1, ac.botHeight, 0);
 		//ac.goTo(ac.BoxS1, ac.botHeight-25, -20);
 		//ac.goTo(ac.BoxS1, ac.botHeight, 0);
 
 		//ac.goTo(ac.BoxS1, ac.botHeight-25, 20);
 		//ac.put(1);
 		//ac.goTo(130, 150, 0);
 		//ac.goTo(110, 150, 0);
 		//ac.goTo(90, 150, 0);
 		//ac.goTo(90, 130, 0);
 		//ac.goTo(90, 110, 0);
 		//ac.goTo(90, 90, 0);
 		//ac.goTo(90, 70, 0);
 	}
 	
 	public void goHome(){
 		da.flip(false);
 		da.setXY(Home, botHeight, speed);
 		da.setXY(Home, botHeight, speed);
 		while(da.ds.readMoving()){DynamixelSerial.halt(10);}
 		atHome = true;
 	}
 	
 	public void goTo(int x, int y){
 		if(x < 0){da.flip(true);}
 		else{da.flip(false);}
 		da.setXY(Math.abs(x), y, speed);
 		da.setXY(Math.abs(x), y, speed);
 		atHome = false;
 		while(da.ds.readMoving()){DynamixelSerial.halt(10);}
 	}
 	
 	public void goTo(int x, int y, int ang){
 		if(x < 0){da.flip(true);}
 		else{da.flip(false);}
 		da.setXY(Math.abs(x), y, speed, ang);
 		da.setXY(Math.abs(x), y, speed, ang);
 		atHome = false;
 		while(da.ds.readMoving()){DynamixelSerial.halt(10);}
 	}
 	
 	public void get(int x){
 		if(x>180){
 			goTo(x-10, tableHeight+15, 0);
 			while(da.ds.readMoving()){DynamixelSerial.halt(10);}
 			goTo(x-5, tableHeight+15, 0);
 			while(da.ds.readMoving()){DynamixelSerial.halt(10);}
 			da.setXY(x, tableHeight, speed/2);
 			da.setXY(x, tableHeight, speed/2);
 			while(da.ds.readMoving()){DynamixelSerial.halt(10);}
 		} else {
 			da.setXY(x, tableHeight+15, speed/2);
 			da.setXY(x, tableHeight+15, speed/2);
 			while(da.ds.readMoving()){DynamixelSerial.halt(10);}
 			da.setXY(x, tableHeight+5, speed/2);
 			da.setXY(x, tableHeight+5, speed/2);
 			while(da.ds.readMoving()){DynamixelSerial.halt(10);}
 		}
 
 		da.setXY(Home-10, botHeight+35, speed/3);
 		da.setXY(Home-10, botHeight+35, speed/3);
 		while(da.ds.readMoving()){DynamixelSerial.halt(10);}
 
 	}
 	
 	public void getRad(int r){
 		int x = xoffset - r;
 		get(x);
 	}
 	
 	public void put(int num){
 		if(num == 1){
 			goTo(Box1, botHeight+10, 40);
 			goTo(Box1, botHeight-20, 0);
 			goTo(Box1+20, botHeight+20, 0);
 		}else if(num == 2){
 			goTo(Box2+60, botHeight+20, 0);
 			goTo(Box2+60, botHeight-15, 0);
 			goTo(Box2, botHeight-10, 0);
 			goTo(Box2, botHeight+40, 0);
 		}else if(num == 3){
 			goTo(Box3, botHeight+10, 40);
 			goTo(Box3, botHeight-23, 0);
 			goTo(Box3-20, botHeight+20, 0);
 		}else if(num == 4){
 			goTo(Box4-60, botHeight+20, 0);
 			goTo(Box4-60, botHeight-15, 0);
 			goTo(Box4, botHeight-10, 0);
 			goTo(Box4, botHeight+40, 0);
 		}
 			
 		
 		atHome = false;
 		//goTo(num,tableHeight-20);
 		//DynamixelSerial.halt(2000);
 		
 		//while(da.ds.readMoving()){}
 		//da.ds.motor(3, 0);
 		//goTo(num+50,tableHeight);
 		//while(da.ds.readMoving()){}
 		goHome();
 	}
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
