 package com.github.kenji0717.a3cs;
 
 import jp.sourceforge.acerola3d.a3.*;
 import java.awt.event.*;
 
 import javax.vecmath.*;
 
 import com.bulletphysics.collision.dispatch.CollisionWorld;
 import com.bulletphysics.collision.dispatch.CollisionWorld.RayResultCallback;
 
 //JBulletを使った物理計算の実験
 public class SimplePC extends KeyAdapter implements CollisionListener {
     PhysicalWorld pw;//物理計算をしてくれるオブジェクト
 
     boolean keyUp = false;
     boolean keyDown = false;
     boolean keyRight = false;
     boolean keyLeft = false;
     boolean keySpace = false;
     
     private static float gEngineForce = 0.0f;
     private static float gBreakingForce = 0.0f;
     private static float maxEngineForce = 500.f;
     private static float maxBreakingForce = 100.f;
     private static float gVehicleSteering = 0.0f;
     private static float steeringClamp = 0.1f;
 
     public SimplePC() throws Exception {
         pw = new PhysicalWorld();//物理計算をしてくれるオブジェクトを生成
         pw.addCollisionListener(this);
 
         /* */
         pw.window.addKeyListener(this);
         pw.window.setCameraLocImmediately(0.0,2.5,7.5);
         pw.window.setCameraLookAtPointImmediately(0.0,1.0,0.0);
         pw.window.setNavigationMode(A3CanvasInterface.NaviMode.SIMPLE);
         /* */
 
         //MyGround g = new MyGround(pw);//地面
         MyGround2 g = new MyGround2(pw);//地面
         pw.add(g);
 
         //初期状態としてMyBoxを積み上げておく
         /*
         for (double x=-3.0;x<=3.0;x+=2.0) {
             for (double y=1.0;y<=7.0;y+=2.0) {
                 for (double z=-3.0;z<=3.0;z+=2.0) {
                     MyBox b = new MyBox(x,y,z+20,pw);//立方体
                     pw.add(b);
                 }
             }
         }
         */
         //MyBox bb = new MyBox(0.0,1.0,20.0,pw);//立方体
         //pw.add(bb);
 
         MyCar c = new MyCar(0.0,2.5,0.0,pw);
         pw.add(c);
         //c.setLoc2(0.0,2.5,0.0);
 
         MyCheckPoint cp = new MyCheckPoint(0.0,0.0,10.0,pw);
         pw.add(cp);
         //cp.setLoc2(10.0,0.0,0.0);
 
         pw.window.setAvatar(c.a3);
         Vector3d lookAt = new Vector3d(0.0,0.0,30.0);
         Vector3d camera = new Vector3d(0.0,3.0,-10.0);
         Vector3d up = new Vector3d(0.0,1.0,0.0);
         pw.window.setNavigationMode(A3CanvasInterface.NaviMode.CHASE,lookAt,camera,up,1.0);
 
         while (true) {
             Thread.sleep(33);
             gEngineForce=0.0f;
             if (keyUp) gEngineForce+=maxEngineForce;
             if (keyDown) gEngineForce-=maxEngineForce;
             gVehicleSteering = 0.0f;
             if (keyRight) gVehicleSteering -= steeringClamp;
             if (keyLeft) gVehicleSteering += steeringClamp;
             /*
             if (keySpace)
                 gBreakingForce=maxBreakingForce;
             else
                 gBreakingForce=0.0f;
             */
             if (keySpace)
                 this.shoot();
             c.setForce(gEngineForce,gVehicleSteering,gBreakingForce);
 
            /*
            //光線テストの実験しようとしたら、このスレッドで実行するのはNGみたい
             RayResultCallback rayRC = new CollisionWorld.ClosestRayResultCallback(new Vector3f(0,0.5f,0),new Vector3f(0,0.5f,5));
             pw.dynamicsWorld.rayTest(new Vector3f(0,0.5f,0), new Vector3f(0,0.5f,5), rayRC);
             System.out.println("gaha:"+rayRC.hasHit());
            */
         }
     }
 
     public void keyPressed(KeyEvent ke) {
         switch(ke.getKeyCode()) {
         case KeyEvent.VK_UP:    keyUp = true;    break;
         case KeyEvent.VK_DOWN:  keyDown = true;  break;
         case KeyEvent.VK_RIGHT: keyRight = true; break;
         case KeyEvent.VK_LEFT:  keyLeft = true;  break;
         case KeyEvent.VK_SPACE: keySpace = true; break;
         }
     }
     public void keyReleased(KeyEvent ke) {
         switch(ke.getKeyCode()) {
         case KeyEvent.VK_UP:    keyUp = false;    break;
         case KeyEvent.VK_DOWN:  keyDown = false;  break;
         case KeyEvent.VK_RIGHT: keyRight = false; break;
         case KeyEvent.VK_LEFT:  keyLeft = false;  break;
         case KeyEvent.VK_SPACE: keySpace = false; break;
         }
     }
 
     //弾丸発射の処理
     //現在のカメラの位置と方向を使って弾丸を発射
     long lastShootTime = System.currentTimeMillis();
     void shoot() {
         if ((System.currentTimeMillis()-lastShootTime)<500)
             return; //あまり連続して打つと重なってしまうから
         Vector3d v = pw.window.getCameraLoc();
         Vector3d vv = pw.window.getCameraUnitVecZ();
         vv.normalize();
         vv.scale(-50.0);//カメラの方向を使って弾丸の初速度を計算
         MySphere s = null;
         try {
             s = new MySphere(v.x,v.y,v.z,pw);//弾丸(球)
         } catch(Exception ee) {
             ee.printStackTrace();
         }
         s.setVel(vv.x,vv.y,vv.z);//弾丸の初速度をセット
         pw.add(s);
         lastShootTime = System.currentTimeMillis();
     }
 
     public void collided(A3CollisionObject a,A3CollisionObject b) {
         System.out.print("a:"+a.a3.getUserData().toString());
         System.out.print(" b:"+b.a3.getUserData().toString());
         System.out.println("  gaha");
     }
     public static void main(String[] args) throws Exception {
         new SimplePC();
     }
 }
