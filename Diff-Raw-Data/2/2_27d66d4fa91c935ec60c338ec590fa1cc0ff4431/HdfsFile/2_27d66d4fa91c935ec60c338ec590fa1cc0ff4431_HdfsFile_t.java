 /*
  * Copyright 2011 Kazuyoshi Aizawa
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
 package iumfs.hdfs;
 
 import iumfs.IumfsFile;
 import iumfs.NotSupportedException;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Date;
 import java.util.logging.Logger;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.hdfs.protocol.AlreadyBeingCreatedException;
 import iumfs.FileExistsException;
 import iumfs.Request;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.Path;
 
 /**
  *
  * @author ka78231
  */
 public class HdfsFile extends IumfsFile {
 
     private FileSystem fs;
     protected static final Logger logger = Logger.getLogger(Main.class.getName());
     private String server;
 
     HdfsFile(String server, String pathname) {
         super(pathname);
         this.server = server;
         fs = getFileSystem(server);
         try {
             Configuration conf = new Configuration();
             conf.set("fs.defaultFS", server);
             logger.finer("server=" + server);
             fs = FileSystem.get(conf);
         } catch (IOException ex) {
             ex.printStackTrace();
             System.exit(1);
         }
         Date now = new Date();
         setAtime(now.getTime());
         setCtime(now.getTime());
         setMtime(now.getTime());
     }
 
     @Override
     public long read(ByteBuffer buf, long size, long offset) throws FileNotFoundException, IOException, NotSupportedException {
         int ret;
 
         FSDataInputStream fsdis = fs.open(new Path(getPath()));
         ret = fsdis.read(offset, buf.array(), Request.RESPONSE_HEADER_SIZE, (int) size);
         fsdis.close();
         logger.fine("read offset=" + offset + ",size=" + size);
         return ret;
     }
 
     @Override
     public long write(byte[] buf, long size, long offset) throws FileNotFoundException, IOException, NotSupportedException {
         // ファイルの属性を得る
         FileStatus fstat = fs.getFileStatus(new Path(getPath()));
         long filesize = fstat.getLen();
 
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
         if (offset + size < filesize) {
             // ファイルサイズ未満のデータ書き込み要求。すなわち変更。
             throw new NotSupportedException();
         }
         FSDataOutputStream fsdos = fs.append(new Path(getPath()));
         /*
          * ファイルの最後に/サイズのデータを書き込み用バッファに読み込む
          * 現在はオフセットの指定はできず Append だけ。
          */
         if (offset > filesize) {
             // オフセットがファイルサイズを超える要求。
             // まず、空白部分を null で埋める。
             fsdos.write(new byte[(int) (offset - filesize)]);
             fsdos.write(buf, 0, (int)size);
         } else {
             // オフセットがファイルサイズ未満の要求。
             fsdos.write(buf, (int)(filesize - offset), (int)size);
         }
         fsdos.close();
         /*
          * レスポンスヘッダをセット
          */
         return fsdos.size();
     }
 
     /*
      * <p>FileSystem.mkdir won't return with err even directory already exits.
      * So here we need to check the existance by ourself and return false.
      */
     @Override
     public boolean mkdir() {
         try {
             /*
              * Create new directory on  HDFS
              */
             if (fs.exists(new Path(getPath())) == true) {
                 logger.fine("cannot create directory");
                 return false;
             }
 
             if (fs.mkdirs(new Path(getPath())) == false) {
                 logger.fine("cannot create directory");
                 return false;
             }
            return true; 
         } catch (IOException ex) {
             /*
              * can't throw IOException here.
              * So return false, and iumfs.mkdir would back
              * this 'false' to IOException...
              */
             return false;
         }
     }
 
     @Override
     public boolean delete() {
         try {
             Path path = new Path(getPath());
             if (fs.delete(path, true) == false) {
                 logger.fine("cannot remove " + path.getName());
                 return false;
             }
         } catch (IOException ex) {
             return false;
         }
         return true;
     }
 
     /**
      * List files under directory which expres this object.
      * This corresponds readdir.
      * 
      * @return 
      */
     @Override
     public File[] listFiles() {
         List<File> filelist = new ArrayList<File>();
         FileStatus fstats[];
         try {
             fstats = fs.listStatus(new Path(getPath()));
         } catch (IOException ex) {
             return null;
         }
         for (FileStatus fstat : fstats) {
             filelist.add(new File(fstat.getPath().getName()));
         }
         return filelist.toArray(new File[0]);
     }
 
     @Override
     public long getFileType() {
         try{
             if(fs.getFileStatus(new Path(getPath())).isDir()){
                 return IumfsFile.VDIR;
             } else {
                 return IumfsFile.VREG;
             }
         } catch (IOException ex) {
             return IumfsFile.VREG;
         }
     }
 
     @Override
     public long getPermission() {
         if(isDirectory()){
             return (long) 0040755; // directory
         } else {
             return (long) 0100444; // regular file      
         }
     }
 
     @Override
     public boolean isDirectory() {
         try {
             return fs.getFileStatus(new Path(getPath())).isDir();
         } catch (IOException ex) {
             return false;
         }
     }
 
     /**
      * <p>FileSystem.create を実行する</p>
      * <p>creat(2) が呼ばれたということは既存ファイルがあった場合
      * 既存データを削除(O_TRUNC相当)しなければならないが、 HDFS では データの途中
      * 変更はできないので、既存ファイルがあったらエラーリターンする</p>
      */
     @Override
     public void create() throws IOException {
         /*
          * HDFS 上に新規ファイルを作成し、結果をレスポンスヘッダをセットする
          */
         try {
             Path path = new Path(getPath());
             //ファイルが存在したら EEXIST を返す
             if (fs.exists(path) == true) {
                 logger.fine("cannot create file");
                 throw new FileExistsException();
             }
 
             FSDataOutputStream fsdos = fs.create(path);
             /*
              * レスポンスヘッダをセット
              */
             fsdos.close();
         } catch (AlreadyBeingCreatedException ex) {
             logger.fine("AlreadyBeingCreatedException when writing");
             throw new FileExistsException();
         } catch (IOException ex) {
             logger.fine("IOException happend when writing");
             throw ex;
         }
     }
 
     private FileSystem getFileSystem(String server) {
         if (fs == null) {
             try {
                 Configuration conf = new Configuration();
                 conf.set("fs.defaultFS", server);
                 logger.finer("server=" + server);
                 fs = FileSystem.get(conf);
             } catch (IOException ex) {
                 ex.printStackTrace();
                 System.exit(1);
             }
         }
         return fs;
     }
 
     public static IumfsFile getFile(String server, String pathname) {
         return new HdfsFile(server, pathname);
     }
 
     /**
      * @return the server
      */
     public String getServer() {
         return server;
     }
 
     /**
      * @param server the server to set
      */
     public void setServer(String server) {
         this.server = server;
     }
 }
