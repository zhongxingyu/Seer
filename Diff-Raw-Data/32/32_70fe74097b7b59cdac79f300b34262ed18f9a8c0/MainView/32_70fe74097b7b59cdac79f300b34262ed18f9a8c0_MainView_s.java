 package tk.bnbm.clockdrive4j.view;
 
 import java.awt.geom.Point2D;
import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javafx.application.Application;
 import javafx.event.EventHandler;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.stage.Screen;
 import javafx.stage.Stage;
 import javafx.stage.Window;
 import javafx.stage.WindowEvent;
 import tk.bnbm.clockdrive4j.model.BackGround;
 import tk.bnbm.clockdrive4j.model.Car;
 import tk.bnbm.clockdrive4j.model.Cloud;
 import tk.bnbm.clockdrive4j.model.Road;
 
 /**
  * 本アプリケーション、メインビューのクラス。<br>
  *
  * the "Clock Drive"である。
  *
  * @author kazuhito_m
  */
 
 public class MainView extends LayoutAndEventView {
 
     // 定数群。
 
     /** 左下時刻表示域の時刻書式 */
     private static final String FMT_TIME = "HH:mm:ss";
 
     // Modelオブジェクト群。
 
     /** 背景モデルオブジェクト。 */
     protected BackGround bg;
 
     /** 道モデルオブジェクト。 */
     protected Road road;
 
     /** 車モデルオブジェクト。 */
     protected Car car;
 
     /** 雲モデルオブジェクト。 */
     protected Cloud cloud;
 
     // 広域変数(プロパティ)
 
     /** 現在表示中の時刻 */
     protected Date current;
 
     /** デバッグ画面のステージ。(立ち上がってない場合はnull) */
     private Stage debugStage;
 
     /**
      * 描画物体の初期化を行うイベント。<br>
      */
     @Override
     public void initDisplayObjects(Scene scene) throws Exception {
         // Modelオブジェクト群初期化。
         bg = new BackGround("./target/classes/images/");
         road = new Road("./target/classes/datas/roadData.csv");
         car = new Car(road);
         cloud = new Cloud(scene.getWidth(), scene.getHeight(), 15);
         // 固定な描画オブジェクトは設定しておく。
         // TODO イメージをリソースから読み込む。
         carImage.setImage(new Image("file:./target/classes/images/car.png"));
 
         // 雲を足す。
         Image cloudImage = new Image("file:./target/classes/images/cloud.png");
         Group root = (Group) scene.getRoot(); // コンテナを取り出し。
         for (Point2D.Double p : cloud.getPositions()) {
             ImageView cloudView = new ImageView(cloudImage); // イメージビューを作り、
             cloudView.relocate(p.getX(), p.getY()); // 座標を移植し
             root.getChildren().add(cloudView); // コンテナに追加し、
             cloudImages.add(cloudView); // 同時にListに入れておく。
         }
         // 雲に埋もれてしまうので、ラベルを一番前へと出し直す。
         root.getChildren().remove(dispTime);
         root.getChildren().add(dispTime);
         // 初回の描画
         draw(new Date());
     }
 
     /**
      * 描画(再描画)イベント。
      */
     @Override
     public void repaint() {
         draw(new Date());
     }
 
     /**
      * 指定された時刻の時計イメージをすべて描く。
      *
      * @param date 表示を行う時刻。
      */
     protected void draw(Date time) {
         // 現在時刻を記憶。
         current = time;
         // Modelの現在内容を描画更新。
         drawBackGround(current);
         drawCar(current);
         drawDigitalTime(current);
         drawClouds(1);
     }
 
     /**
      * 指定された時刻に応じた背景を描く。
      *
      * @param time 表示を行う時刻。
      */
     private void drawBackGround(Date time) {
         // 時刻セット
         bg.setTime(time);
         // その時刻から割り出される背景を割り出す。
         Image srcImage = new Image("file:" + bg.getSrcImagePath());
         Image destImage = new Image("file:" + bg.getDestImagePath());
         // ブレンド元の背景画像を描く（不透明ベタ塗り）
         bgImage.setImage(srcImage);
         // ブレンド先の背景画像を描く（半透明）
         fgImage.setImage(destImage);
         fgImage.setOpacity(bg.getBlendRatio());
     }
 
     /**
      * 指定された時刻に応じて、適切な位置と角度で車を描く。
      *
      * @param time 表示を行う時刻。
      */
     private void drawCar(Date time) {
         // 時刻セット。
         car.setTime(time);
         // 自身のイメージの大きさを考慮し「中心にくる」考慮を入れた座標計算。
         Point2D.Double p = car.getPosition();
         double x = p.getX() - (carImage.getLayoutBounds().getWidth() / 2);
         double y = p.getY() - (carImage.getLayoutBounds().getHeight() / 2);
         // 描画更新
         carImage.relocate(x, y);
         carImage.setRotate(car.getAngle());
     }
 
     /**
      * 指定された時刻に応じて、デジタル時刻を描く。
      *
      * @param time 表示を行う時刻。
      */
     private void drawDigitalTime(Date time) {
         dispTime.setText(new SimpleDateFormat(FMT_TIME).format(time));
     }
 
     /**
      * 雲の影を描く。
      */
     private void drawClouds(double distance) {
         // 指定された距離で雲を移動。
         cloud.move(distance);
         // 座標を移植し移動。
         int i = 0; // Point2Dのhashが変わるばっかりに、原始的な…。
         for (Point2D.Double p : cloud.getPositions()) {
             cloudImages.get(i++).relocate(p.getX(), p.getY());
         }
     }
 
     /**
      * デバッグ開始。
      */
     @Override
     public void startDebug() {
         try {
             // デバッグビルド専用、右クリックにより、時刻を強制指定できる別画面を表示する。（タイマーを停止する）
             this.switchTimer(false);
             // 表示。(あればそれを、無ければ新規作成)
             Stage s;
             if (debugStage == null) {
                 s = new Stage();
                 // コールバックメソッド登録。
                 s.setOnCloseRequest(new EventHandler<WindowEvent>() {
                     public void handle(WindowEvent we) {
                         endDebug();
                     }
                 });
                 DebugForm.setDebugTarget(this);
                 DebugForm.initStage(s); // 形状はコントローラに操作してもらう。
                 s.show();
                 debugStage = s;
             } else {
                 s = debugStage;
             }
 
             // 初期位置計算。(自画面の右横上に隣接する形で)
             Window self = scene.getWindow();
             s.setX(self.getX() + self.getWidth());
             s.setY(self.getY());
             // 画面からはみ出しちゃったら、反対側に表示する
             double allWidth = Screen.getPrimary().getBounds().getWidth();
             if ((s.getX() + s.getWidth()) > allWidth) {
                 s.setX(s.getX() - s.getWidth());
             }
             // 右クリックメニューの「タイマーの…」を殺す。
             this.timerSwitchMenu.setDisable(true);
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * デバッグ終了。
      */
     public void endDebug() {
         if (debugStage != null) {
             debugStage = null;
             this.switchTimer(true); // タイマーを有効に。
             // 右クリックメニューの「タイマーの…」を復活。
             this.timerSwitchMenu.setDisable(false);
         }
     }
 
     /**
      * 閉じられる際に呼ばれるイベント。
      */
     protected void onClose() {
         if (debugStage != null) {
             debugStage.close();
         }
     }
 
     /**
      * アプリケーションのエントリポイント。
      *
      * @param args コマンドライン引数。
      */
     public static void main(String[] args) {
         Application.launch(MainView.class, args);
     }
 
 }
