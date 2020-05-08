 package test;
 
 import com.github.kenji0717.a3cs.*;
 import javax.vecmath.Vector3d;
 
 public class TestCar01 extends RaceCarBase {
     public void exec() {
        Vector3d loc = getLoc();
         
        Vector3d point = getPoint(13.0);//前方の座標
        Vector3d dir = getDirection(13.0);//前方の方向
        point.sub(loc);
        point.normalize();
        Vector3d left = getUnitVecX();
        Vector3d front = getUnitVecZ();
 
        boolean noborizaka = point.y>0.1;
        boolean kyuukaabu = front.dot(dir)<0.9;
 
         double engineForce = 700.0; //エンジン出力
         if (noborizaka) engineForce += 200; //上り坂では加速
         if (kyuukaabu) engineForce = 300; //急カーブでは減速
         double steering = 0.3*point.dot(left); //左にハンドルを切る量
         double breakingForce = kyuukaabu?10.0:0.0;//急カーブの時ブレーキ
         double drift=0.0; //ドリフトしない。
 
         setForce(engineForce,steering,breakingForce,drift);

//if (engineForce>800.0)System.out.println("加速");
//if (breakingForce>1.0)System.out.println("ブレーキ");
 //System.out.println("Debug: "+getPoint());
 //System.out.println("Debug: "+getDirection(10.0));
 //System.out.println("Debug: "+getVel());
 //System.out.println("Debug: "+getDist());
 //System.out.println("-");
     }
 }
