 package my.triviagame.xmcd;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.io.*;
 import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
 import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
 import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
 import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
 import org.apache.commons.io.IOUtils;
 
 /**
  * Decompressed an xmcd archive and streams records as {@link XmcdDisc}s.
  */
 public class XmcdDiscStream implements Iterator<XmcdDisc>, Closeable {
 
     /**
      * Extracts an xmcd archive and streams the records within.
      */
     public XmcdDiscStream(File archive) throws XmcdException {
         try {
             FileInputStream fin = new FileInputStream(archive);
             BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream(fin);
             tar = new TarArchiveInputStream(bzip2);
             currentEntry = tar.getNextTarEntry();
         } catch (IOException e) {
             throw new XmcdExtractionException(String.format("Could not open file %s", archive.getName()), e);
         }
     }
    
     /**
      * Extracts several xmcd archives and streams the records within.
      */
     public static Iterator<XmcdDisc> extractAll(Iterable<File> archives) throws IOException {
        Iterator<XmcdDiscStream> xmcdStreams = Iterators.transform(archives.iterator(), new Function<File, XmcdDiscStream>() {
            @Override
            public XmcdDiscStream apply(File file) throws XmcdException {
                return new XmcdDiscStream(file);
            }
        });
         return Iterators.concat(xmcdStreams);
     }
 
     @Override
     public boolean hasNext() {
         return currentEntry != null;
     }
 
     @Override
     public XmcdDisc next() throws XmcdException {
         XmcdDisc xmcdDisc;
         try {
             // Get to the next file, skipping any directory entries along the way
             while (currentEntry != null && currentEntry.getFile().isDirectory()) {
                 currentEntry = tar.getNextTarEntry();
             }
             // Read the next file
             String nextFileContents;
             nextFileContents = IOUtils.toString(tar);
             // Parse the next file
             FreedbGenre freedbGenre = FreedbGenre.fromDirectoryName(currentEntry.getFile().getParentFile().getName());
             xmcdDisc = XmcdDisc.fromXmcdFile(nextFileContents, freedbGenre);
             // Advance to the next file for the next call
             currentEntry = tar.getNextTarEntry();
         } catch (IOException e) {
             throw new XmcdExtractionException("Error while unarchiving", e);
         } catch (XmcdMissingInformationException e) {
             throw e;
         }
         return xmcdDisc;
     }
 
     @Override
     public void remove() {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void close() throws IOException {
         tar.close();
     }
 
     private TarArchiveInputStream tar;
     private TarArchiveEntry currentEntry;
 }
