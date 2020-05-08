 package com.pokutuna.lifelog.sample;
 
 import com.pokutuna.lifelog.parsing.LogParser;
 import com.pokutuna.lifelog.parsing.LogToken.*;
 import java.io.File;
 import java.io.FileReader;
 import java.util.List;
 
 // ログパーサのサンプル
 public class LogParserSample {
 
   // ログをパース(構文解析)し、インスタンスに格納するLogParserの使い方
   // いわゆる再帰下降パーサ
   // BNFを書いたり日付の値をチェックしたり、適当に正規表現でパースするのに比べてだいぶマトモに動作するはず
 
   // ログデータの中身は com.pokutuna.lifelog.parsing.LogToken.* 以下のクラス・インターフェースに関連付けられる
   // LogLine: ログデータの各行を表す基底インターフェース
   // DetectLog: デバイスの検出を表すインタフェース
   //   BtDetected: Bluetoothデバイスの検出ログを表すクラス
   //   WifiDetected: Wifiデバイスの検出ログを表すクラス
   //     それぞれdateTime(), deviceName(), address(), signal() 等のメソッドで値を取得することができる
   // Annotation: アノテーション([LOGGER_BDA]や[BT_SCAN]で始まる補足情報ログ)をインタフェース
   //   LoggerVersion, LoggerBDA, BtScan, WifiScan 等(まだあまり使わない気がするので詳細は割愛)
   // OtherLog: 空行やパースできなかったログ行を表すインタフェース
   //   BlankLine, ErrorLog
 
   // これらのログデータに対応するクラスは、HasAddress, HasDate, HasDeviceName, HasSignalなどの
   // インタフェースの組み合わせで実装されているため、適時インタフェース単位でフィルタが可能
 
   // 動作例
   public static void main(String[] args) {
     try {
       File file = new File("src/test/resources/test_btlogdata.tsv");
       FileReader reader = new FileReader(file);
       // LogParser.runForJava に java.io.Reader のサブクラスを渡す、ここではFileReader
       List<LogLine> logs = LogParser.runForJava(reader);
 
       for(LogLine l : logs) {
         System.out.println(l);
       } // #=>
       /* LoggerBDA(70:71:BC:21:11:1E)
        * BtScan(Sun Oct 10 00:00:41 JST 2010)
        * BtDetectLog(Sun Oct 10 00:00:41 JST 2010,Kono_Pocket_PC,00:22:64:CD:9E:94)
        * BtDetectLog(Sun Oct 10 00:00:41 JST 2010,TARAKEMP,00:22:43:E2:D8:94)
        * BtDetectLog(Sun Oct 10 00:00:41 JST 2010,LOOX,00:1B:DC:0F:B9:F1)
        * BtDetectLog(Sun Oct 10 00:00:41 JST 2010,Stinger5.2,00:1D:FD:EC:4F:38)
        * BtScan(Sun Oct 10 00:01:12 JST 2010)
        * BtDetectLog(Sun Oct 10 00:01:12 JST 2010,BlackBerry 8900,00:25:57:71:9C:0B)
        * BtDetectLog(Sun Oct 10 00:01:12 JST 2010,TARAKEMP,00:22:43:E2:D8:94)
        * (略)
        * */
 
       // 主にデータベース(sensing.db)に格納するために使われるため、
       // DetectLogインタフェースを実装するクラスには、
       // com.pokutuna.lifelog.db.model.SensingModel.* 以下のモデルへの変換も定義されてある
       // BtDetected#toDeviceRecord, WifiDetected#toDeviceDetected,
       // BtDetected#toDetectedRecord, WifiDetected#toDetectedRecord
       for(LogLine l : logs) {
         if(l instanceof DetectLog) {
           DetectLog dl = (DetectLog)l;
          System.out.println(dl + " -> " + dl.toDeviceRecord() + " : " + dl.toDetectRecord());
         }
       } // #=>
       /* BtDetectLog(Sun Oct 10 00:00:41 JST 2010,Kono_Pocket_PC,00:22:64:CD:9E:94) -> BtDevice(00:22:64:CD:9E:94,Kono_Pocket_PC) : BtDetected(00:22:64:CD:9E:94,2010-10-10 00:00:41)
        * BtDetectLog(Sun Oct 10 00:00:41 JST 2010,TARAKEMP,00:22:43:E2:D8:94) -> BtDevice(00:22:43:E2:D8:94,TARAKEMP) : BtDetected(00:22:43:E2:D8:94,2010-10-10 00:00:41)
        * BtDetectLog(Sun Oct 10 00:00:41 JST 2010,LOOX,00:1B:DC:0F:B9:F1) -> BtDevice(00:1B:DC:0F:B9:F1,LOOX) : BtDetected(00:1B:DC:0F:B9:F1,2010-10-10 00:00:41)
        * BtDetectLog(Sun Oct 10 00:00:41 JST 2010,Stinger5.2,00:1D:FD:EC:4F:38) -> BtDevice(00:1D:FD:EC:4F:38,Stinger5.2) : BtDetected(00:1D:FD:EC:4F:38,2010-10-10 00:00:41)
        * BtDetectLog(Sun Oct 10 00:01:12 JST 2010,BlackBerry 8900,00:25:57:71:9C:0B) -> BtDevice(00:25:57:71:9C:0B,BlackBerry 8900) : BtDetected(00:25:57:71:9C:0B,2010-10-10 00:01:12)
        * BtDetectLog(Sun Oct 10 00:01:12 JST 2010,TARAKEMP,00:22:43:E2:D8:94) -> BtDevice(00:22:43:E2:D8:94,TARAKEMP) : BtDetected(00:22:43:E2:D8:94,2010-10-10 00:01:12)
        */
 
       // 得られたDetectLogからDeviceRecord, DetectRecordを生成しDBに格納していく感じになるはずです
 
     } catch (Exception e) {
       // 通常、文法的にパースできなかったものはErrorLog(lawLine: String)に対応付けられるため例外は発生しないが、
       // 文字コードの問題や稀にファイルが途中から壊れていたりするためファイル単位で例外のハンドリングは必要
       e.printStackTrace();
     }
   }
 }
