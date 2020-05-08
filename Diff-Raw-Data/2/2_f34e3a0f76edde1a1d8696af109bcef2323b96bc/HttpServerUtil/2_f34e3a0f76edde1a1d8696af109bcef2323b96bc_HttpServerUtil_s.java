 import java.io.*;
 import java.text.*;
 import java.util.*;
 
 /* ユーティリティ的なものを集めたクラス */
 class HttpServerUtil {
     /* 拡張子、MIMEタイプ、テキストファイルかどうか("text"ならテキスト) */
     private static final String[][] FILETYPE_LIST = {
         {"txt", "text/plain", "text"},
         {"csv", "text/csv", "text"},
         {"css", "text/css", "text"},
         {"htm", "text/html", "text"},
         {"html", "text/html", "text"},
         {"xml", "application/xml", "text"},
         {"xhtml", "application/xhtml+xml", "text"},
         {"Atom", "application/atom-xml", "text"},
         {"js", "application/javascript", "text"},
         {"json", "application/json", "text"},
         {"pdf", "application/pdf", "binary"},
         {"zip", "application/zip", "binary"},
         {"gz", "application/x-gzip", "binary"},
         {"gzip", "application/x-gzip", "binary"},
         {"jpeg", "image/jpeg", "binary"},
         {"jpg", "image/jpeg", "binary"},
         {"gif", "image/gif", "binary"},
         {"png", "image/png", "binary"},
         {"bmp", "image/x-ms-bmp", "binary"}
     };
     /* fnameの拡張子を取得する。
      * .から始まる場合空文字列が帰る
      * fnameが本当にファイル名を指しているか（ディレクトリでないか）は
      * このメソッドではチェックしない */
     private static String getSuffix(String fname) {
         if (fname == null) {
             return null;
         }
 
         int dotPos = fname.lastIndexOf('.');
 
         // 拡張子を持たない場合空文字列
         // .無し、もしくは.から始まる場合拡張子無し
         if (dotPos == -1 || dotPos == 0) {
             return "";
         } else {
             return fname.substring(dotPos + 1);
         }
     }
 
 
     /* 実際にはこのメソッドは今回は使っていない */
     public static boolean isTextFile(File f) {
         String suffix = getSuffix(f.getName());
         if (suffix == null) {
             return false; // テキストではない
        } else if(suffix == "") {
             return true; // 取り敢えずテキストにしておく
             /* 理由は、ここに来るのは
              * 1. .bashrcのように先頭にのみ.が付く
              * →テキストファイル
              * 2. そもそも拡張子がない
              * →READMEなど？ということでひとまずテキストに
              * 3. f.getName()が空文字列
              * →そのようなファイル名のものは無い？
              */
         } else {
             for ( String[] elem : FILETYPE_LIST) {
                 if (elem[0].equalsIgnoreCase(suffix)) {
                     if (elem[2].equals("text")) {
                         return true;
                     } else {
                         return false;
                     }
                 }
             }
             // リストに無い場合バイナリ扱い
             return false;
         }
     }
 
     /* ファイルfのMIME Typeを拡張子から推測する */
     public static String getMIMEType(File f) {
         String suffix = getSuffix(f.getName());
         for ( String[] elem : FILETYPE_LIST) {
             if (elem[0].equalsIgnoreCase(suffix)) {
                 return elem[1];
             }
         }
         // 一致なしなら任意のバイナリデータを表すoctet-stream扱い
         return "application/octet-stream";
     }
 
     /* elemがArrayの要素かどうかの判定 */
     public static boolean isMember(String[] array, String elem) {
         return Arrays.asList(array).contains(elem);
     }
 
     /* 現在時刻(GMT)をRFC1123形式で返す
      * レスポンスヘッダのDate:フィールドで必要
      * 例:"Tue, 19 Mar 2013 11:55:32 GMT"
      */
     public static String rfc1123CurrentDate() {
         SimpleDateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
         rfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));
         return rfc1123.format(new Date());
     }
 }
 
