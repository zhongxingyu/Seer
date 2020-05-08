 package com.example.fsexplorer.fs;
 
 import com.example.fsexplorer.api.Node;
 import com.example.fsexplorer.api.NodeList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.nio.file.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Andrey Tarashevsky
  *         Date: 05.02.13
  */
 public class FileSystemResolverImpl implements FileSystemResolver {
 
     private static final Logger log = LoggerFactory.getLogger(FileSystemResolverImpl.class);
 
     private final Path rootPath;
 
     private final FileTypeFactory fileTypeFactory;
 
     public FileSystemResolverImpl(String rootPath, FileTypeFactory fileTypeFactory) {
         this.rootPath = Paths.get(rootPath);
         this.fileTypeFactory = fileTypeFactory;
     }
 
     @Override
     public NodeList getAllChildren(final String p) {
         Path path = rootPath.resolve(p);
 
         if (!Files.exists(path)) {
             return new NodeList(null);
         }
 
         List<Node> nodes = new ArrayList<>();
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
 
             for (Path entry : stream) {
                Node node = Node.createNode(entry.getFileName().toString(), entry.toFile().getPath(),
                         Files.size(entry), fileTypeFactory.fromPath(entry));
                 nodes.add(node);
             }
         } catch (DirectoryIteratorException ex) {
             log.error("Something wrong with directory iteration", ex);
         } catch (IOException e) {
             log.error("Something happened on retrieving list of children of " + p, e);
             return new NodeList(null);
         }
 
         return new NodeList(nodes);
     }
 }
