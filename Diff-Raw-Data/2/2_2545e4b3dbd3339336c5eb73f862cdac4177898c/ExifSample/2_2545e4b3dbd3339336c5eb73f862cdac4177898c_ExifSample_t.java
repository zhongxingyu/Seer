 package com.pokutuna.lifelog.sample;
 
 import com.pokutuna.lifelog.db.model.*;
import com.pokutuna.lifelog.db.factory.*;
 import com.pokutuna.lifelog.db.model.*;
 import com.pokutuna.lifelog.util.*;
 import java.io.File;
 
 // 画像ファイルからExifを抽出するサンプル
 public class ExifSample {
 
   public static void main(String[] args) {
 
     File tanu = new File("src/test/resources/tanu.jpg"); // Exifのついた画像ファイル
     Exif exif = ExifExtractor.extract(tanu); // ExifExtractor.extractでExifを抽出
 
     // ExifクラスにはJava向けに以下のメソッドを持つ
     // Exif#dateOrNull 写真の撮影日時を表すjava.util.Dateインスタンスを返す、画像に付与されていなければnullが返る
     // Exif#latitudeOrNaN 写真の撮影された緯度(latitude)をDoubleで返す、付与されていなければNaNが返る
     // Exif#longitudeOrNan 写真の撮影された経度(longitude)をDoubleで返す、付与されていなければNaNが返る
 
     //以下動作例
     System.out.println(exif.dateOrNull()); // #=> Wed May 18 23:08:11 JST 2011
     System.out.println(exif.latitudeOrNaN()); // #=> 34.82216666666667
     System.out.println(exif.longitudeOrNaN()); // #=> 135.3195
 
     File icon = new File("src/test/resources/icon.jpg"); // Exifの無い画像ファイル
     Exif noExif = ExifExtractor.extract(icon);
     System.out.println(noExif.dateOrNull()); // #=> null
     System.out.println(noExif.latitudeOrNaN()); // #=> NaN
     System.out.println(noExif.longitudeOrNaN()); // #=> NaN
     // 他、ディレクトリを表すFileや、画像以外を渡すと適当に例外を出す
 
     // com.pokutuna.lifelog.db.model.LifelogModel.PhotoRecord を一気に作成するには
     // PhotoRecordFactoryを使う、DB作成時にはこちらを使うほうがいい
     // PhotoRecordFactory.apply を用いる
     PhotoRecord tanuRecord = PhotoRecordFactory.apply(tanu);
     System.out.println(tanuRecord); // #=> PhotoRecord(src/test/resources,tanu.jpg,2011-05-18 23:08:11,34.82216666666667,135.3195,640,478,52769,2011,5,18,23,8,11)
 
     // ディレクトリを指定したい場合はPhotoRecordFactory.applyの第1引数にディレクトリ名を渡すことで指定できる
     PhotoRecord iconRecord = PhotoRecordFactory.apply("hoge/fuga/piyo", icon);
     System.out.println(iconRecord); // #=> PhotoRecord(hoge/fuga/piyo,icon.jpg,,0.0,0.0,162,162,8130,0,0,0,0,0,0)
     // ExifExtractor以下のメソッドの返り値そのままではなく、NaNは0に、時刻は空文字列に適時変換される
     // これらをLifelogDAO等からInsertすればよい
 
     // Exifの抽出はMetadataExtractorに依存している
     // http://drewnoakes.com/drewnoakes.com/code/exif/
   }
 }
