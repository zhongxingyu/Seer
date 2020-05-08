 package tk.bnbm.clockdrive4j.model;
 
 import static java.util.Calendar.HOUR_OF_DAY;
 import static java.util.Calendar.MINUTE;
 import static java.util.Calendar.SECOND;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public class BackGround {
 
     /** 与えられた時刻 */
     private Calendar time;
 
     /** すべての背景画像のファイルパス一覧 */
     protected File[] imageFileNames;
 
     /**
      * コンストラクタ。<br>
      * コスト削減のため、頻繁に使う「時刻」をカレンダー化。そのカレンダーを初期化する。
      */
     public BackGround() {
         this.time = Calendar.getInstance();
     }
 
     /**
      * コンストラクタ。<br>
      * 背景画像を読み込むフォルダを受け取って、背景画像のファイル名一覧を取得する。
      *
      * @param imagePath 画像ファイルのローカルパス。
      */
     public BackGround(String imagePath) {
         this();
         imageFileNames = new File(imagePath).listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.matches("bg.*\\.png");
             }
         });
         // 順番が狂うため、ファイル名でソート
         List<File> files = Arrays.asList(imageFileNames);
         Collections.sort(files);
         imageFileNames = files.toArray(new File[0]); // PMDで怒られた…型確定には引数に「型だけ教える空の配列」が要るようだ。
     }
 
     /**
      * 基準時刻を与える。
      *
      * @param t 与える基準時刻。
      */
     public void setTime(Date t) {
         time.setTime(t);
     }
 
     /**
      * 背景画像Ａ（２枚ブレンドするうち、先にベタ塗りする方）のファイルパスを取得する。
      *
      * @return ファイルパス。
      */
     public String getSrcImagePath() {
         return imageFileNames[calcPartOfDay(imageFileNames.length, 0)]
                 .getPath();
     }
 
     /**
      * 背景画像Ｂ（２枚ブレンドするうち、後で半透明に描く方）のファイルパスを取得する。<br>
      * たとえば全部で４枚なら、２４Ｈを４等分し、６時間ごとに切り替わっていく。
      *
      * @return ファイルパス。
      */
     public String getDestImagePath() {
         return imageFileNames[calcPartOfDay(imageFileNames.length, +1)]
                 .getPath();
     }
 
     /**
      * 一日の内「どの部分に居るか」を数値にて返す。<br>
      *
      * 日をいくつのパートに分けるかは、引数 totalPart にて指定し、戻り値は0～totalPartとなる。
      * また、オフセットが指定でき、1を指定すると次の、-1を指定すると前のパートを返す。
      *
      * @param totalPartCount 総パート数(日を幾つのパートに分けるか)。
      * @param offset オフセット。
      * @return パート番号。
      */
     protected int calcPartOfDay(int totalPartCount, int offset) {
         int hour = this.time.get(HOUR_OF_DAY);
         return (offset + hour / (24 / totalPartCount)) % totalPartCount;
     }
 
     /**
      * 背景画像ＡとＢのブレンド率を取得する。<br>
      * たとえば全部で４枚なら、２４Ｈを４等分し、６時間ごとに0～1.0を繰り返す。
      *
      * @return ブレンド率数値(百分率)
      */
     public double getBlendRatio() {
 
        double partHours = (double) (24 / imageFileNames.length); // 背景パーツ、一枚がカバーする時間。
         double hour = (double) time.get(HOUR_OF_DAY);
         double min = (double) time.get(MINUTE);
         double sec = (double) time.get(SECOND);
 
         double ratio = (hour % partHours + min / 60D + sec / 3600D) / partHours;
 
         return ratio;
     }
 }
