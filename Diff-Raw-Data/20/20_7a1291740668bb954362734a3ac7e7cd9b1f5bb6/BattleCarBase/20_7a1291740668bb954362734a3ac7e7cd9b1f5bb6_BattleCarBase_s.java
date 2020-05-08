 package test_cb;
 
 import java.util.ArrayList;
 import javax.vecmath.*;
 
 import com.github.hiuprocon.pve.car.CarBase;
 import com.github.hiuprocon.pve.car.CarData;
 
 /**
  * このクラスを拡張してバトルするための車を作成します。
  * これを拡張して作成する車クラスは必ず引数無しの
  * コンストラクタを持つ必要があります。
  * この車は初期値100のエネルギーを持ち、被弾すると5づつエネルギーが
  * 減少します。また弾丸を発射した時も1づつエネルギーを消費します。
  */
 public abstract class BattleCarBase {
 	CarBase car;
 
     /**
      * BattleCarBaseのコンストラクタです。
      */
     protected BattleCarBase() {
    	car = new CarBase();
     }
 
     /**
      * この車のID(整数値)を取得します。
      */
     protected int getCarID() {
         return car.getCarID();
     }
 
     /**
      * 前進のための力(engine)、ハンドルの方向(steering:正->左,負->右)、
      * そしてブレーキの力(breaking)を指定して車をコントロールします。
      */
     protected void setForce(double engine,double steering,double breaking) {
         car.setForce(engine,steering,breaking,0.0);
     }
 
     /**
      * 前進のための力(engine)、ハンドルの方向(steering:正->左,負->右)、
      * ブレーキの力(breaking)、そしてドリフト(drift)を指定して車をコントロールします。
      */
     protected void setForce(double engine,double steering,double breaking,double drift) {
         car.setForce(engine,steering,breaking,0.0);
     }
 
     /**
      * この車の現在の座標を取得します。
      */
     protected Vector3d getLoc() {
         return car.getLoc();
     }
 
     /**
      * この車の現在の回転を四元数で取得します。
      */
     protected Quat4d getQuat() {
         return car.getQuat();
     }
 
     /**
      * この車の現在の回転をオイラー角で取得します。
      */
     protected Vector3d getRot() {
         return car.getRot();
     }
 
     /**
      * この車の現在の進行方向左向きの単位ベクトルを取得します。
      */
     protected Vector3d getUnitVecX() {
         return car.getUnitVecX();
     }
 
     /**
      * この車の現在の進行方向上向きの単位ベクトルを取得します。
      */
     protected Vector3d getUnitVecY() {
         return car.getUnitVecY();
     }
 
     /**
      * この車の現在の進行方向の単位ベクトルを取得します。
      */
     protected Vector3d getUnitVecZ() {
         return car.getUnitVecZ();
     }
 
     /**
      * 弾丸を発射します。引数のベクトルは弾丸を発射したい方向を世界座標で
      * 指定します。ただしこの車の進行方向と弾丸の発射方向が45度以上開いて
      * いる場合は弾丸は発射されません。このメソッドを呼び出すと無条件で
      * エネルギーを1消費します。
      */
     protected void shoot(Vector3d d) {
         car.shoot(d);
     }
 
     /**
      * 現在のフィールドに存在している全ての車の情報を取得します。
      * データの詳細についてはCarDataクラスを参照して下さい。
      */
     protected ArrayList<CarData> getAllCarData() {
         return car.getAllCarData();
     }
 
     /**
      * この車の現在のエネルギー値を取得します。
      */
     protected int getEnergy() {
         return car.getEnergy();
     }
 
 
     /**
      * シミュレーション開始からの時間(秒)を返します。
      */
     protected double getTime() {
         return car.getTime();
     }
 
     /**
      * この車をコントロールするプログラムを記述するメソッドです。
      * このCarBaseを継承するクラスでは必ず、このメソッドを実装して
      * 下さい。
      */
     public abstract void exec();
 }
