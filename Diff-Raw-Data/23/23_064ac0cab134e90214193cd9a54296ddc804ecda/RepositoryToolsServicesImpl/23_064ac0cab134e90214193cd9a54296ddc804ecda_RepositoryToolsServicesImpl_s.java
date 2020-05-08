 package org.kevoree.library.javase.webserver.collaborationToolsBasics.server;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import org.kevoree.library.javase.fileSystem.client.AbstractItem;
 import org.kevoree.library.javase.fileSystem.client.FolderItem;
 import org.kevoree.library.javase.fileSystem.client.LockFilesService;
 import org.kevoree.library.javase.fileSystemGitRepository.GitRepositoryActions;
 import org.kevoree.library.javase.webserver.collaborationToolsBasics.client.RepositoryToolsServices;
 import org.kevoree.library.javase.webserver.servlet.FakeServletContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 public class RepositoryToolsServicesImpl extends RemoteServiceServlet implements RepositoryToolsServices {
 
     private Logger logger = LoggerFactory.getLogger(RepositoryToolsServicesImpl.class);
     AbstractItem baseFolder;
     private String directoryPath;
     private RepositoryToolsComponent repositoryToolsComponent;
 
     public RepositoryToolsServicesImpl(RepositoryToolsComponent repoToolsComponent, String directoryPath ){
         baseFolder = new FolderItem(directoryPath);
         this.directoryPath = directoryPath;
         this.repositoryToolsComponent = repoToolsComponent;
     }
 
     public javax.servlet.ServletContext getServletContext() {
         return new FakeServletContext();
     }
 
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         super.doGet(req, resp);
     }
 
     public AbstractItem importRepository(String login, String password,String url) {
         String nameRepository = url.substring(url.lastIndexOf("/")+1,url.lastIndexOf("."));
        return repositoryToolsComponent.getPortByName("createRepo", GitRepositoryActions.class).importRepository(login, password, url ,nameRepository, directoryPath);
     }
 
     public boolean move(AbstractItem oldItem, AbstractItem newItem){
         return repositoryToolsComponent.getPortByName("files", LockFilesService.class).move(oldItem.getPath(), newItem.getPath());
     }
 
     public boolean newFileIntoRepository(AbstractItem item){
         logger.debug(" saveFile {}" ,item.getPath());
         return repositoryToolsComponent.getPortByName("files", LockFilesService.class).saveFile(item.getPath(), new byte[0], true);
     }
 
     public boolean saveFileAfterUpload(AbstractItem item){
         logger.debug(" saveFile after upload {}", item.getPath());
         return repositoryToolsComponent.getPortByName("files", LockFilesService.class).saveFile(item.getPath(), new byte[0], true);
     }
 
     public boolean createFolderIntoLocalRepository(AbstractItem item){
         return  repositoryToolsComponent.getPortByName("files", LockFilesService.class).mkdirs(item.getPath());
     }
 
     public AbstractItem initRepository(String login, String password, String nameRepository){
         AbstractItem itemReturned = repositoryToolsComponent.getPortByName("createRepo", GitRepositoryActions.class).initRepository(login, password, nameRepository, directoryPath);
         if(itemReturned==null){
             throw new Error(" error during initRepository");
         }
         else{
             return itemReturned;
         }
     }
 
     public boolean updateContentFileAndCommit(String file, byte[] editorText, String login) {
         logger.debug(" in updateContentFileAndCommit String file = {}", file );
         return  repositoryToolsComponent.getPortByName("files", LockFilesService.class).saveFile(file, editorText, true);
     }
 
     public boolean pushRepository(String login, String password) {
         return repositoryToolsComponent.getPortByName("createRepo", GitRepositoryActions.class).pushRepository(login, password);
     }
 
     public String getFileContent(String filePath)  throws IOException {
             return new String(repositoryToolsComponent.getPortByName("files", LockFilesService.class).getFileContent(filePath, false));
     }
 }
