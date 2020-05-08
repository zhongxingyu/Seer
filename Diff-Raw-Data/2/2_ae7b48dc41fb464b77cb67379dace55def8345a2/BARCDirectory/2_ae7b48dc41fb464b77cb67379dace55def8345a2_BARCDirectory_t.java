 /*
  * Copyright (c) 2008-2010 David Kellum
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
 package iudex.barc;
 
 import iudex.barc.BARCFile.Record;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * Manages concurrent reads and exclusive write sessions to a directory of
  * BARC files.
  */
 public final class BARCDirectory implements Closeable
 {
     /**
      * Scan's path for existing BARCFiles ("012345.barc") or creates a new
      * directory at this path.
      */
     public BARCDirectory( File path ) throws IOException
     {
         _path = path;
         if( path.exists() ) {
             if( path.isDirectory() ) scanBARCFiles();
             else throw new IOException( "Not a (BARC) Directory: " + path );
         }
         else if( ! path.mkdirs() ) {
             throw new IOException( "Unable to create directory: " + path );
         }
     }
 
     public BARCFile.Record read( int fileNumber, long offset )
         throws IOException
     {
         BARCFile barc = openBarc( fileNumber, false );
         return barc.read( offset );
     }
 
     /**
      *  Set target length after which new BARCFile will be opened for write.
      *  Default: 1GB
      */
     public void setTargetLength( long length )
     {
         _targetBARCLength = length;
     }
 
     public synchronized WriteSession startWriteSession()
         throws InterruptedException, IOException
     {
         // Wait for no current session.
         // FIXME: Optimize by pooling a controlled number of simultaneous write
         // files instead of just one.
         while( _currentSession != null ) wait();
 
         // If no _writeFile yet then try to use the last file in the directory
         if( ( _writeFile == null ) && ( _maxFnum >= 0 ) ) {
             _writeFile = openBarc( _maxFnum, false );
         }
 
         // If _writeFile is too large then stop using it.
         if( ( _writeFile != null ) &&
             ( _writeFile.size() >= _targetBARCLength ) ) {
             _writeFile = null;
         }
 
         // If no _writeFile, then create a new one.
         if( _writeFile == null ) _writeFile = openBarc( _maxFnum + 1, true );
 
         _currentSession = new WriteSession( _writeFile, _maxFnum );
         return _currentSession;
     }
 
     @Override
     public synchronized void close() throws IOException
     {
         for( BARCFile bfile : _barcs ) {
             if( bfile != null ) bfile.close();
         }
         _barcs.clear();
         _writeFile = null;
         _currentSession = null;
     }
 
     /**
      * An exclusive write session to a single BARC file.  Multiple append()'s
      * may be called in a single session. Insure session.close() is called to
      * allow the next thread to write.
      */
     public final class WriteSession implements Closeable
     {
         /**
          * File number that this write session is writing to.
          */
         public int fileNumber()
         {
             return _fnum;
         }
 
         public Record append() throws IOException
         {
             if( _currentRecord != null ) _currentRecord.close();
             _currentRecord = _barc.append();
             return _currentRecord;
         }
 
         /**
          * End session, allowing another thread to write to this same file.
          */
         public void close() throws IOException
         {
             if( _currentRecord != null ) {
                 _currentRecord.close();
                 _currentRecord = null;
             }
 
             _barc = null;
 
             closeSession();
         }
 
         WriteSession( BARCFile barc, int fnum )
         {
             _barc = barc;
             _fnum = fnum;
         }
 
         private BARCFile _barc;
         private final int _fnum;
         private Record _currentRecord = null;
     }
 
     private synchronized BARCFile openBarc( int fnum, boolean create )
         throws IOException
     {
         BARCFile barc = null;
         if( fnum < _barcs.size() ) barc = _barcs.get( fnum );
         if( barc == null ) {
             File bfile = new File( _path, String.format(  "%06d.barc", fnum ) );
             if( create || bfile.exists() ) {
                 barc = new BARCFile( bfile );
                 while( _barcs.size() <= fnum ) _barcs.add( null );
                 _barcs.set( fnum, barc );
                 _maxFnum = Math.max( _maxFnum, fnum );
             }
             else {
                 throw new FileNotFoundException( bfile.toString() );
             }
         }
         return barc;
     }
 
     private void scanBARCFiles()
     {
         for( String fname : _path.list() ) {
             try {
                 if( fname.length() == "012345.barc".length() ) {
                     int fnum = Integer.parseInt( fname.substring( 0, 6 ) );
                     //  FIXME: Save map?
                     _maxFnum = Math.max( _maxFnum, fnum );
                 }
             }
             catch( NumberFormatException x ) {}  // skip to next file
         }
     }
 
     private synchronized void closeSession()
     {
         _currentSession = null;
         notify();  //Next thread can startWriteSession()
     }
 
     private final File _path;
 
    private long _targetBARCLength = 0x40000000L; //1GB
     private BARCFile _writeFile = null;
     private WriteSession _currentSession = null;
 
     private int _maxFnum = -1;
     private ArrayList<BARCFile> _barcs = new ArrayList<BARCFile>( 64 );
 }
