 /*
  * Copyright 2010 Kazuyoshi Aizawa
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.IOException;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.FileStatus;
 import java.io.FileNotFoundException;
 
 /**
  * <p>Write リクエストを表すクラス</p>
  */
 public class WriteRequest extends Request {
 
    public void process() {
        long offset = getOffset();
        long size = getSize();
        long filesize = 0;
        FileStatus fstat = null;       
        
         try {
             FileSystem hdfs = getFileSystem();
 
             // ファイルの属性を得る
             fstat = hdfs.getFileStatus(getFullPath());
             filesize = fstat.getLen();
 
             /*
              * この iumfscntl から受け取る write リクエストのオフセット値
              * は必ず PAGE 境界上。そして受け取るデータは PAGE 境界からの
              * データ。（既存データ含む)
              * 
              *        PAGESIZE              PAGESIZE
              *  |---------------------|---------------------|
              *  |<---------- filesize -------->|
              *  |<---- offset ------->|<-- size --->|
              *
              *  HDFS の append は filesize 直後からの追記しか許さないので
              *  iumfs から渡されたデータから、追記すべき分を算出し、
              *  HDFS に要求する。
              */ 
             if(offset + size < filesize) {
                 // ファイルサイズ未満のデータ書き込み要求。すなわち変更。
                 setResponseHeader(ENOTSUP, 0);
                 return;
             }
             FSDataOutputStream fsdos = hdfs.append(getFullPath());
             /*
              * ファイルの最後に/サイズのデータを書き込み用バッファに読み込む
              * 現在はオフセットの指定はできず Append だけ。
              */
             if(offset > filesize){
                 // オフセットがファイルサイズを超える要求。
                // まず、空白部分を null で埋める。
                fsdos.write(new byte[(int)(offset - filesize)]);
                fsdos.write(getData(0, size));
            } else {
                // オフセットがファイルサイズ未満の要求。
                fsdos.write(getData(filesize - offset, size));
            }
             fsdos.close();
             /*
              * レスポンスヘッダをセット
              */
             setResponseHeader(SUCCESS, 0);
 
         } catch (FileNotFoundException ex){
             logger.finer("FileNotFoundException happend when writing");
             setResponseHeader(ENOENT, 0);            
         } catch (IOException ex) {
             logger.fine("IOException happend when writing");
             ex.printStackTrace();
             setResponseHeader(ENOTSUP, 0);
         } catch (ArrayIndexOutOfBoundsException ex){
             logger.fine("ArrayIndexOutOfBoundsException happend when writing");
             ex.printStackTrace();
             setResponseHeader(EINVAL, 0); 
         }
         
     }
 }
 
