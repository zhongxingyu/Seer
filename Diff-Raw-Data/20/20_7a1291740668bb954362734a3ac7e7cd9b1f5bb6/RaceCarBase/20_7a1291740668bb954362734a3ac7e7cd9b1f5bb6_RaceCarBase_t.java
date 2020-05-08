 package test_cr;
 
 import java.util.*;
 import java.net.URL;
 import java.io.*;
 import javax.vecmath.*;
 
 import com.github.hiuprocon.pve.car.CarBase;
 
 /**
  * このクラスを拡張してレースするための車を作成します。
  * これを拡張して作成する車クラスは必ず引数無しの
  * コンストラクタを持つ必要があります。
  */
 //public abstract class RaceCarBase extends CarBase {
 public abstract class RaceCarBase {
 	public CarBase car;
 
     //センターライン上の点の集合
     ArrayList<Vector3d> points = new ArrayList<Vector3d>();
     //センターライン上の点におけるスタート位置からの距離
     ArrayList<Double> dists = new ArrayList<Double>();
     //センターライン上の点における方向ベクトル
     ArrayList<Vector3d> dirs = new ArrayList<Vector3d>();
     final double courseLength = 693.7857818243989;
     /**
      * RaceCarBaseのコンストラクタです。
      */
     protected RaceCarBase() {
    	car = new CarBase() {
    		public void exec() {
    			RaceCarBase.this.exec();
    		}
    	};
         try {
            URL url = new URL("x-res:///res/lines.txt");
             InputStream is = url.openStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);
             String s = "";
             while (true) {
                 String line = br.readLine();
                 if (line==null) break;
                 s = s + line;
             }
             String ss[] = s.split(",");
             for (int i=0;i<ss.length;i++) {
                 String sss[] = ss[i].trim().split("\\s+");
                 double x = Double.parseDouble(sss[0]);
                 double y = Double.parseDouble(sss[1]);
                 double z = Double.parseDouble(sss[2]);
                 points.add(new Vector3d(x,y,z));
             }
         } catch(Exception e) {
             e.printStackTrace();
         }
         dists.add(0.0);
         dirs.add(new Vector3d(0,0,-1));
         double milage = 0.0;
         Vector3d v1,v2,v3;
         for (int i=1;i<points.size()-1;i++) {
             Vector3d tmp1 = new Vector3d();
             Vector3d tmp2 = new Vector3d();
             v1 = points.get(i-1);
             v2 = points.get(i);
             v3 = points.get(i+1);
             tmp1.sub(v2,v1);
             milage += tmp1.length();
             dists.add(milage);
             tmp1.normalize();
             tmp2.sub(v3,v2);
             tmp2.normalize();
             tmp2.add(tmp1);
             tmp2.normalize();
             dirs.add(tmp2);
         }
         dists.add(courseLength);
         dirs.add(new Vector3d(0,0,-1));
     }
 
     /**
      * この車のID(整数値)を取得します。
      */
     protected int getCarID() {
         return car.getCarID();
     }
 
     /**
      * 前進のための力(engine)、ハンドルの方向(steering:正->左,負->右)、
      * ブレーキの力(breaking)、そしてドリフト(drift)を指定して車をコントロールします。
      * ドリフトの値は0.9より大きな値を指定した場合にドリフト、それ以下の場合は通常走行となります。
      */
     protected void setForce(double engine,double steering,double breaking,double drift) {
         car.setForce(engine,steering,breaking,drift);
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
 
     //ここより下は2012年度から追加されたAPI。以下の計算はコースのセンターラインの
     //座標情報が十分緻密でなめらかであるので、これを前提にしてプログラムしてある。
 
     /**
      * 現在の車の速度を返します。
      */
     protected Vector3d getVel() {
         return car.getVel();
     }
 
     /**
      * インデックスを0から689に正規化する。
      */
     int normalizeIndex(int i) {
         i = i % 690;
         if (i<0) i = i + 690;
         return i;
     }
     /**
      * 保存されているコースのセンターライン上のポイント情報を取り出します。
      * 実際のポイント数は690個であり引数iがは0以上689以下の数字であれば
      * その実際のデータを返します。この範囲外の数字が引数として与えられた場合は、
      * 690で割った時のあまりを計算し、負の数である場合はさらに690を加えた数字を
      * 用いてポイントを特定します。
      */
     protected Vector3d getRawPointData(int i) {
         i = normalizeIndex(i);
         return new Vector3d(points.get(i));
     }
     double getDist(int i) {
         i = normalizeIndex(i);
         return dists.get(i);
     }
     Vector3d getDir(int i) {
         i = normalizeIndex(i);
         return new Vector3d(dirs.get(i));
     }
 
     /**
      * 現在地点にもっとも近い点(v1)の添字と2番めに近い点(v2)の添字、そして
      * 現在地点からv1,v2を結ぶ直線に垂線を下ろした時の交点を算出する
      * ための定数(k)を返す。具体的には「交点=k*v1+(1-k)*v2」となる定数。
      */
     Tuple3<Integer,Integer,Double> getNearest2PointsAndRatio() {
         Vector3d loc = getLoc();
         Vector3d tmp = new Vector3d();
         double min1 = Double.MAX_VALUE;
         double min2 = Double.MAX_VALUE;
         int index1 = 0;
         int index2 = 0;
         for (int i=0;i<points.size();i++) {
             tmp.sub(points.get(i),loc);
             double dis = tmp.length();
             if (dis<min1) {
                 min2 = min1;
                 index2 = index1;
                 min1 = dis;
                 index1 = i;
             } else if (dis<min2) {
                 min2 = dis;
                 index2 = i;
             }
         }
         tmp.sub(points.get(index1),points.get(index2));
         double k = tmp.length();
         tmp.normalize();
         Vector3d tmp2 = new Vector3d();
         tmp2.sub(loc,points.get(index2));
         k = tmp.dot(tmp2)/k;
         return new Tuple3<Integer,Integer,Double>(index1,index2,k);
     }
 
     /**
      * コースのセンターライン上の点のうち現在位置に最も近い点を計算し、その点がスタート位置から
      * センターラインに沿って走行した場合の走行距離を返します。
      * ただし車が十分センターラインに近い場所にない場合は正確な値は計算されない可能性があります。
      * 詳細はソースコード参照のこと。
      */
     protected double getDist() {
         Tuple3<Integer,Integer,Double> t = getNearest2PointsAndRatio();
         double d1 = dists.get(t.t1);
         double d2 = dists.get(t.t2);
         if (t.t3>1.0) return d1;
         if (t.t3<0.0) return d2;
         return t.t3*d1 + (1.0-t.t3)*d2;
     }
 
     /**
      * コースのセンターライン上の点のうち、現在位置に最も近い座標を返します。
      * ただし車が十分センターラインに近い場所にない場合は正確な値は計算されない可能性があります。
      * 詳細はソースコード参照のこと。
      */
     protected Vector3d getPoint() {
         Tuple3<Integer,Integer,Double> t = getNearest2PointsAndRatio();
         Vector3d v1 = new Vector3d(points.get(t.t1));
         Vector3d v2 = new Vector3d(points.get(t.t2));
         if (t.t3>1.0) return v1;
         if (t.t3<0.0) return v2;
         v1.scale(t.t3);
         v2.scale(1.0-t.t3);
         v1.add(v2);
         return v1;
     }
 
     /**
      * コースのセンターライン上の点のうち、現在位置に最も近い点における
      * センターラインの接線、つまり進行方向を単位ベクトルで返します。
      * ただし車が十分センターラインに近い場所にない場合は正確な値は計算されない可能性があります。
      * 詳細はソースコード参照のこと。
      */
     protected Vector3d getDirection() {
         Tuple3<Integer,Integer,Double> t = getNearest2PointsAndRatio();
         Vector3d v1 = new Vector3d(dirs.get(t.t1));
         Vector3d v2 = new Vector3d(dirs.get(t.t2));
         if (t.t3>1.0) return v1;
         if (t.t3<0.0) return v2;
         v1.scale(t.t3);
         v2.scale(1.0-t.t3);
         v1.add(v2);
         v1.normalize();
         return v1;
     }
 
     /**
      * まず、getNearest2PointsAndRatio()を使って現在位置にもっとも近い場所を
      * 割り出し、その場所から引数のplusメートル進んだ場所の情報を算出する。
      * 返り値はgetNearest2PointsAndRatio()と同様の形式。
      */
     Tuple3<Integer,Integer,Double> getNearest2PointsAndRatioPlus(double plus) {
         Tuple3<Integer,Integer,Double> t = getNearest2PointsAndRatio();
         double d1 = dists.get(t.t1);
         double d2 = dists.get(t.t2);
         double mokuhyou;
         if (t.t3<0.0) mokuhyou = d1 + plus;
         else if (t.t3>1.0) mokuhyou = d2 + plus;
         else mokuhyou = t.t3*d1 + (1.0-t.t3)*d2 + plus;
 
         int index=t.t1<t.t2?t.t1:t.t2;
         while (true) {
             double d = getDist(index);
             if (index>=690) d = d + courseLength;
             if (d>mokuhyou)
                 break;
             index++;
         }
         int i1 = normalizeIndex(index-1);
         int i2 = normalizeIndex(index);
         d1 = dists.get(i1);
         d2 = dists.get(i2);
         double k = d2 - d1;
         k = (d2-mokuhyou)/k;
         return new Tuple3<Integer,Integer,Double>(i1,i2,k);
     }
     /**
      * コースのセンターライン上最も近い点からplusメートル前方のセンターライン上の座標を返します。
      * ただし車が十分センターラインに近い場所にない場合は正確な値は計算されない可能性があります。
      * 詳細はソースコード参照のこと。
      */
     protected Vector3d getPoint(double plus) {
         Tuple3<Integer,Integer,Double> t = getNearest2PointsAndRatioPlus(plus);
         Vector3d v1 = new Vector3d(points.get(t.t1));
         Vector3d v2 = new Vector3d(points.get(t.t2));
         if (t.t3>1.0) return v1;
         if (t.t3<0.0) return v2;
         v1.scale(t.t3);
         v2.scale(1.0-t.t3);
         v1.add(v2);
         return v1;
     }
 
     /**
      * コースのセンターライン上最も近い点からplusメートル前方の
      * センターラインの接線、つまり進行方向を単位ベクトルで返します。
      * ただし車が十分センターラインに近い場所にない場合は正確な値は計算されない可能性があります。
      * 詳細はソースコード参照のこと。
      */
     protected Vector3d getDirection(double plus) {
         Tuple3<Integer,Integer,Double> t = getNearest2PointsAndRatioPlus(plus);
         Vector3d v1 = new Vector3d(dirs.get(t.t1));
         Vector3d v2 = new Vector3d(dirs.get(t.t2));
         if (t.t3>1.0) return v1;
         if (t.t3<0.0) return v2;
         v1.scale(t.t3);
         v2.scale(1.0-t.t3);
         v1.add(v2);
         v1.normalize();
         return v1;
     }
     class Tuple2<T1,T2> {
         public final T1 t1;
         public final T2 t2;
         public Tuple2(T1 t1,T2 t2) {
             this.t1 = t1;
             this.t2 = t2;
         }
     }
     class Tuple3<T1,T2,T3> {
         public final T1 t1;
         public final T2 t2;
         public final T3 t3;
         public Tuple3(T1 t1,T2 t2,T3 t3) {
             this.t1 = t1;
             this.t2 = t2;
             this.t3 = t3;
         }
     }
 }
