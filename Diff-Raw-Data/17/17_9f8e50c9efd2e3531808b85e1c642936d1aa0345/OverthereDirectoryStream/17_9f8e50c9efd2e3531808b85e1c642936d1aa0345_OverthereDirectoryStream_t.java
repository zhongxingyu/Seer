 package com.xebialabs.overthere.nio.file;
 
 import java.io.IOException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Path;
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 
 import com.xebialabs.overthere.OverthereFile;
 
 import static com.google.common.collect.Lists.transform;
 
 public class OverthereDirectoryStream implements DirectoryStream<Path> {
 
     private Iterable<Path> paths;
 
     public OverthereDirectoryStream(final OvertherePath dir, final Filter<? super Path> filter) {
         initEntries(dir, filter);
     }
 
     private void initEntries(final OvertherePath dir, final Filter<? super Path> filter) {
         List<OverthereFile> overthereFiles = dir.getOverthereFile().listFiles();
         paths = Iterables.filter(transform(overthereFiles, new Function<OverthereFile, Path>() {
             @Override
             public Path apply(final OverthereFile input) {
                 return new OvertherePath(dir.getOverthereFileSystem(), input.getPath());
             }
         }), new Predicate<Path>() {
             public boolean apply(final Path input) {
                 try {
                     return filter.accept(input);
                 } catch (IOException e) {
                     return false;
                 }
             }
         });
     }
 
     @Override
     public Iterator<Path> iterator() {
         return paths.iterator();
     }
 
     @Override
     public void close() throws IOException {
         // no-op
     }
 }
