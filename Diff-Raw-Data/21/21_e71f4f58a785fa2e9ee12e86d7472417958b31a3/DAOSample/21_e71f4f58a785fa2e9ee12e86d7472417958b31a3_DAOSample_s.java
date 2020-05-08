 package com.pokutuna.lifelog.sample;
 
 import com.pokutuna.lifelog.db.dao.*;
 import com.pokutuna.lifelog.db.model.*;
import com.pokutuna.lifelog.db.model.LifelogModel.*;
 import java.util.List;
 
 public class DAOSample {
 
   public static void main(String[] args) {
 
     //DAO(データベースアクセスオブジェクト)の作成
     //jdbc:sqlite:[path] で指定する
     //ここではsensing.dbを操作するSensingDAOForJavaをインスタンス化
     SensingDBForJava sdao = new SensingDBForJava("sensing.db");
 
     //SensingDAOが返すデータベースのカラムに対応するオブジェクトは
     //com.pokutuna.Lifelog.db.model.SensingModel以下の、
     //DetectedRecord(検出したデバイスアドレスと時刻を持つ)インタフェースを実装するBtDetected, WifiDetected、
     //DeviceRecord(検出したデバイスごとのアドレスと名前の対応を持つ)インタフェースを実装するBtDevice, WifiDeviceがある
 
     //DeviceRecordはaddress(), name()を持ち、それぞれデバイスアドレスとデバイス名を返す
     //DetectedRecordはaddress(), dateTime()を持ち、デバイスアドレスと検出時刻を返す
 
     //detectedInで指定された期間内に検出したBt,Wifiのレコードを返す
     List<DetectedRecord> detected = sdao.detectedIn("2011-06-01 12:00:00", "2011-06-01 15:00:00");
     for (DetectedRecord de : detected) {
       System.out.println(de);
     }
     /*
      (略)
      WifiDetected(00:17:3F:21:86:52,2011-06-01 13:04:58,-54)
      WifiDetected(00:03:93:ED:92:13,2011-06-01 13:04:58,-87)
      BtDetected(64:B9:E8:D5:E9:D6,2011-06-01 13:05:16)
      BtDetected(58:55:CA:FB:56:D2,2011-06-01 13:05:16)
      WifiDetected(00:17:3F:21:86:52,2011-06-01 13:05:26,-54)
      WifiDetected(00:03:93:ED:92:13,2011-06-01 13:05:26,-87)
      BtDetected(64:B9:E8:D5:E9:D6,2011-06-01 13:05:45)
      (略)
      みたいな出力
      */
 
     //他に, BTについての検出のみを返す List<BtDetected> btDetectedIn(from, to)
     //Wifiについての検出のみを返すList<WifiDetected> wifiDetectedIn(from, to)等がある
     //名前とシグネチャでだいたい分かるようにしてるのでわかんなかったらきいてください。
 
     sdao.countDetection("58:55:CA:FB:56:D2"); //アドレスの検出回数
     sdao.latestDateTime(); //最後にロギングされた時刻
     sdao.addressToName("58:55:CA:FB:56:D2"); //アドレスからデバイス名を取得、未定義なら空文字列""を返す
     sdao.isBluetooth("58:55:CA:FB:56:D2"); //Btデバイスならtrueを返す
     sdao.isWifi("58:55:CA:FB:56:D2"); //Wifiデバイスならtrueを返す
 
     //他にdaoj.insert*等のデータベースへ追加するメソッドもありますが割愛します。
 
     //lifelog.dbを扱うLifelogDAOもある
    LifelogDAOForJava lldao = new LifelogDAOForJava("jdbc:sqlite:Lifelog_110606.db"); //SensingDAOForJavaと同様
 
     List<PhotoRecord> photos = lldao.photoTakenIn("2010-06-01 12:00:00", "2010-06-02 12:00:00");
     //期間内に撮影されたPhotoRecordオブジェクトのリストをphotosに代入
     for(PhotoRecord p : photos) {
       System.out.println(p);
     }
     /*
      PhotoRecord(2010\201006\2010_0601,IMG_2483.JPG,2010-06-01 12:05:48,34.91335,135.163604,2048,1536,1078923,2010,6,1,12,5,48)
      PhotoRecord(2010\201006\2010_0601,IMG_2484.JPG,2010-06-01 12:16:07,34.908167,135.1595,2048,1536,918938,2010,6,1,12,16,7)
      PhotoRecord(2010\201006\2010_0601,IMG_2485.JPG,2010-06-01 12:16:16,34.908167,135.1595,2048,1536,1294904,2010,6,1,12,16,16)
      */
 
     //PhotoRecordオブジェクトはcom.pokutuna.Lifelog.db.model.LifelogModel以下にある。
     //実際のDBはいろいろごちゃごちゃレコードがあるけど、とりあえず以下のみサポート
     /*
     orgDate: String,
     latitude: double,
     longitude: double,
     width: int,
     height: int,
     fileSize: int,
     year: int,
     month: int,
     day: int,
     hour: int,
     minute: int,
     second: int
     */
 
    lldao.photoTakenWhere(35.0, 36.0, 134.0, 135.0); //撮影された緯度経度が範囲内にあるPhotoRecordオブジェクトのリストを返す
     lldao.existsFile("IMG_1325.JPG"); //与えられたfilenameを持つPhotoRecordがDB内にあればtrue
     lldao.existsFile("~/Picture", "IMG_1325.JPG"); //与え要られたdirectory、filenameを持つPhotoRecordがDBにあればtrue
 
     //こちらもinsertがあるけどまあわかるはず
   }
 }
