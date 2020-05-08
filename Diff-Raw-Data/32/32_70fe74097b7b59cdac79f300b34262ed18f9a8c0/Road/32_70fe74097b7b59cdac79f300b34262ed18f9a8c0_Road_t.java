 package tk.bnbm.clockdrive4j.model;
 
 import static java.util.Calendar.HOUR_OF_DAY;
 import static java.util.Calendar.MINUTE;
 import static java.util.Calendar.SECOND;
 
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * "道路"を司るクラス。
  *
  * @author kazuhito_m
  */
 public class Road {
 
     /** 道路上の位置の一覧 */
     private List<Point2D.Double> roadPositions;
 
     /**
      * コンストラクタ。
      *
      * @param roadCsvFilePath 読み込むファイルのパス。
      * @throws IOException
      * @throws NumberFormatException
      */
     public Road(String roadCsvFilePath) throws NumberFormatException,
             IOException {
         roadPositions = new ArrayList<Point2D.Double>();
         loadCsvFromFile(roadCsvFilePath);
     }
 
     /**
      * カンマ区切りかタブ区切りの道路上座標ＣＳＶファイルを読み込み、格納しておく。
      *
      * @param roadCsvFilePath 読み込むCSVファイルのパス。
      * @throws IOException
      * @throws NumberFormatException
      */
     protected void loadCsvFromFile(String roadCsvFilePath)
             throws NumberFormatException, IOException {
 
         FileReader fr = null;
         BufferedReader br = null;
 
         try {
 
             fr = new FileReader(roadCsvFilePath);
             br = new BufferedReader(fr);
 
             String line = null;
             while ((line = br.readLine()) != null) {
                 // 1行をデータの要素に分割
                 StringTokenizer st = new StringTokenizer(line, ",\t");
                 if (st.hasMoreTokens()) {
                     String x = st.nextToken();
                     if (st.hasMoreTokens()) {
                         String y = st.nextToken();
                         Point2D.Double p = new Point2D.Double(
                                 Double.parseDouble(x), Double.parseDouble(y));
                         roadPositions.add(p);
                     }
                 }
             }
 
         } finally {
            if (br != null) {
                br.close();
            }
            if (fr != null) {
                fr.close();
            }
         }
     }
 
     /**
      * 時刻に応じた角度を算出するための係数を、0～1.0の範囲で得る。
      *
      * @param time 角度と対応する時刻。
      * @return 指定した時刻に居るべき角度。
      */
     public static double calcPositionRatio(Date time) {
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(time);
 
         double ratio = ((double) cal.get(HOUR_OF_DAY) % 12D / 12D)
                 + ((double) cal.get(MINUTE) % 60D / 60D / 12D)
                 + ((double) cal.get(SECOND) % 60D / 60D / 60D / 12D);
         return ratio;
     }
 
     /**
      * 与えた時間に応じた、道路上の位置を得る。<br>
      * 近傍２点から、微妙な位置を滑らかに補完する。
      *
      * @param time 指定時刻(時分秒を対象)。
      * @return 点オブジェクト。
      */
     public Point2D.Double getRoadPosition(Date time) {
         // 比率計算。
         double ratio = calcPositionRatio(time);
 
         double baseIndex = ratio * roadPositions.size();
         int idxA = (int) baseIndex % roadPositions.size();
         int idxB = (idxA + 1) % roadPositions.size();
         double blendRatio = baseIndex - (int) baseIndex;
 
         Point2D.Double from = roadPositions.get(idxA);
         Point2D.Double to = roadPositions.get(idxB);
 
         return new Point2D.Double(from.getX() * (1D - blendRatio) + to.getX()
                 * blendRatio, from.getY() * (1D - blendRatio) + to.getY()
                 * blendRatio);
     }
 }
