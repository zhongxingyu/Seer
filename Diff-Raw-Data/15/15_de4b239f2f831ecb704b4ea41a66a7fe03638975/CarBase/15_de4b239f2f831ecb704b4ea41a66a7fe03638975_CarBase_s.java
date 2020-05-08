 package com.github.hiuprocon.pve.car;
 
 import java.util.ArrayList;
 import javax.vecmath.*;
 import jp.sourceforge.acerola3d.a3.Util;
 import com.github.hiuprocon.pve.core.*;
 import com.github.hiuprocon.pve.obj.*;
 
 /**
  * このクラスを拡張してレースorバトルするための車を作成します。 これを拡張して作成する車クラスは必ず引数無しの
  * コンストラクタを持つ必要があります。レースでは関係ありませんが、 この車は初期値100のエネルギーを持ち、被弾すると3づつエネルギーが
  * 減少します。また弾丸を発射した時も1づつエネルギーを消費します。
  */
 public class CarBase implements ActiveObject {
     static int carIDCount = 0;
     public SimpleCarObj car;
     PVEWorld world;
     CarSim carSim;
     int carID;
     int energy = 100;
 
     /**
      * CarBaseのコンストラクタです。
      */
     public CarBase() {
         carID = carIDCount++;
     }
 
     public final void init(String a3url, PVEWorld world, CarSim cs) {
         this.world = world;
         carSim = cs;
         car = new SimpleCarObj(world, a3url);
         car.setUserData(this);
     }
 
     /**
      * この車のID(整数値)を取得します。
      */
     public int getCarID() {
         return carID;
     }
 
     /**
      * 前進のための力(gEngineForce)、ハンドルの方向(gVehicleSteering:正->右,負->左)、
      * そしてブレーキの力(gBreakingForce)を指定して車をコントロールします。
      */
     public void setForce(double gEngineForce, double gVehicleSteering,
             double gBreakingForce, double drift) {
         car.setForce(gEngineForce, gVehicleSteering, gBreakingForce, drift);
     }
 
     /**
      * この車の現在の座標を取得します。
      */
     public Vector3d getLoc() {
         return car.getLoc();
     }
 
     /**
      * この車の現在の回転を四元数で取得します。
      */
     public Quat4d getQuat() {
         return car.getQuat();
     }
 
     /**
      * この車の現在の回転をオイラー角で取得します。
      */
     public Vector3d getRot() {
         return car.getRot();
     }
 
     /**
      * この車の現在の進行方向左向きの単位ベクトルを取得します。
      */
     public Vector3d getUnitVecX() {
         Quat4d q = getQuat();
         q.normalize();
         return Util.trans(q, new Vector3d(1, 0, 0));
     }
 
     /**
      * この車の現在の進行方向上向きの単位ベクトルを取得します。
      */
     public Vector3d getUnitVecY() {
         Quat4d q = getQuat();
         q.normalize();
         return Util.trans(q, new Vector3d(0, 1, 0));
     }
 
     /**
      * この車の現在の進行方向の単位ベクトルを取得します。
      */
     public Vector3d getUnitVecZ() {
         Quat4d q = getQuat();
         q.normalize();
         return Util.trans(q, new Vector3d(0, 0, 1));
     }
 
     /**
      * 弾丸を発射します。引数のベクトルは弾丸を発射したい方向を世界座標で 指定します。ただしこの車の進行方向と弾丸の発射方向が45度以上開いて
      * いる場合は弾丸は発射されません。このメソッドを呼び出すと無条件で エネルギーを1消費します。
      */
     public void shoot(Vector3d d) {
         energy = energy - 1;
         if (d.length() < 0.0001)
             return;
         d.normalize();
         Vector3d front = getUnitVecZ();
         if (d.dot(front) < 0.707)// 0.707=1/1.41421356
             return;
         Vector3d v = new Vector3d(d);// vは弾丸の速度ベクトル
         v.scale(10.0);
         Vector3d l = getLoc();
         l.add(new Vector3d(0.0, 0.6, 0.0));
         d.scale(1.5);// 計算上0.85より少し上なら良いはずだけど???
         l.add(d);
         Bullet b = new Bullet(l, v);
         //b.setGravity(new Vector3d());
         world.add(b);
         // carSim.addActiveObject(b);
     }
 
     /**
      * 現在のフィールドに存在している全ての車の情報を取得します。 データの詳細についてはCarDataクラスを参照して下さい。
      */
     public ArrayList<CarData> getAllCarData() {
         ArrayList<CarData> ret = new ArrayList<CarData>();
         for (CarBase c : carSim.getAllCar()) {
             CarData cd = new CarData();
             cd.carID = c.getCarID();
             cd.loc = c.getLoc();
             cd.unitVecX = c.getUnitVecX();
             cd.unitVecY = c.getUnitVecY();
             cd.unitVecZ = c.getUnitVecZ();
             cd.energy = c.energy;
             ret.add(cd);
         }
         return ret;
     }
 
     /**
      * この車の現在のエネルギー値を取得します。
      */
     public int getEnergy() {
         return energy;
     }
 
     /**
      * この車の現在のエネルギー値を設定します。
      */
     public void setEnergy(int e) {
         energy = e;
     }
 
     public void hit() {
         energy = energy - 3;
         System.out.println("車:" + carID + ":いて!!!");
     }
 
     /**
      * シミュレーション開始からの時間(秒)を返します。
      */
     public double getTime() {
         return world.getTime();
     }
 
     /**
      * この車をコントロールするプログラムを記述するメソッドです。 このCarBaseを継承するクラスでは必ず、このメソッドを実装して 下さい。
      */
     public void exec() {
         ;
     };
 
     // 以下2012,08/18追加
     Vector3d oldLoc = new Vector3d();
     Vector3d vel = new Vector3d();
 
     public void beforeExec() {
         Vector3d loc = getLoc();
         vel.sub(loc, oldLoc);
         vel.scale(1.0 / world.stepTime);
         oldLoc = loc;
     }
 
     /**
      * 車の現在の速度を返します。
      */
     public Vector3d getVel() {
         return new Vector3d(vel);
     }

    /**
     * この車に任意のデータを持たせます．(主に逆引き用)
     */
    public void setUserData(Object userData) {
        this.userData = userData;
    }

    /**
     * この車にsetUserDataでセットしたデータを取り出します．
     * (主に逆引き用)
     */
    public Object getUserData() {
        return userData;
    }
 }
