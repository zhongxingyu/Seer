 package org.geogit.web.api.commands;
 
 import java.util.List;
 
 import org.geogit.api.CommandLocator;
 import org.geogit.api.Remote;
 import org.geogit.api.porcelain.RemoteAddOp;
import org.geogit.api.porcelain.RemoteException;
 import org.geogit.api.porcelain.RemoteListOp;
 import org.geogit.api.porcelain.RemoteRemoveOp;
 import org.geogit.web.api.AbstractWebAPICommand;
 import org.geogit.web.api.CommandContext;
 import org.geogit.web.api.CommandResponse;
 import org.geogit.web.api.CommandSpecException;
 import org.geogit.web.api.ResponseWriter;
 
 /**
  * Interface for the Remote operations in GeoGit. Currently only supports listing of remotes.
  * 
  * Web interface for {@link RemoteListOp}
  */
 
 public class RemoteWebOp extends AbstractWebAPICommand {
 
     private boolean list;
 
     private boolean remove;
 
     private String remoteName;
 
     private String remoteURL;
 
     /**
      * Mutator for the list variable
      * 
      * @param list - true to list the names of your remotes
      */
     public void setList(boolean list) {
         this.list = list;
     }
 
     /**
      * Mutator for the remove variable
      * 
      * @param remove - true to remove the given remote
      */
     public void setRemove(boolean remove) {
         this.remove = remove;
     }
 
     /**
      * Mutator for the remoteName variable
      * 
      * @param remoteName - the name of the remote to add or remove
      */
     public void setRemoteName(String remoteName) {
         this.remoteName = remoteName;
     }
 
     /**
      * Mutator for the remoteURL variable
      * 
      * @param remoteURL - the URL to the repo to make a remote
      */
     public void setRemoteURL(String remoteURL) {
         this.remoteURL = remoteURL;
     }
 
     /**
      * Runs the command and builds the appropriate response.
      * 
      * @param context - the context to use for this command
      */
     @Override
     public void run(CommandContext context) {
         final CommandLocator geogit = this.getCommandLocator(context);
         if (list) {
             final List<Remote> remotes = geogit.command(RemoteListOp.class).call();
 
             context.setResponseContent(new CommandResponse() {
                 @Override
                 public void write(ResponseWriter out) throws Exception {
                     out.start();
                     out.writeRemoteListResponse(remotes);
                     out.finish();
                 }
             });
         } else if (remove) {
             if (remoteName == null || remoteName.trim().isEmpty()) {
                 throw new CommandSpecException("No remote was specified.");
             }
             final Remote remote;
             try {
                 remote = geogit.command(RemoteRemoveOp.class).setName(remoteName).call();
            } catch (RemoteException e) {
                context.setResponseContent(CommandResponse.error(e.statusCode.toString()));
                return;
             } catch (Exception e) {
                context.setResponseContent(CommandResponse.error("Aborting Remote Remove"));
                 return;
             }
             context.setResponseContent(new CommandResponse() {
                 @Override
                 public void write(ResponseWriter out) throws Exception {
                     out.start();
                     out.writeElement("name", remote.getName());
                     out.finish();
                 }
             });
         } else {
             if (remoteName == null || remoteName.trim().isEmpty()) {
                 throw new CommandSpecException("No remote was specified.");
             } else if (remoteURL == null || remoteURL.trim().isEmpty()) {
                 throw new CommandSpecException("No URL was specified.");
             }
             final Remote remote;
             try {
                 remote = geogit.command(RemoteAddOp.class).setName(remoteName).setURL(remoteURL)
                         .call();
            } catch (RemoteException e) {
                context.setResponseContent(CommandResponse.error(e.statusCode.toString()));
                return;
             } catch (Exception e) {
                context.setResponseContent(CommandResponse.error("Aborting Remote Add"));
                 return;
             }
             context.setResponseContent(new CommandResponse() {
                 @Override
                 public void write(ResponseWriter out) throws Exception {
                     out.start();
                     out.writeElement("name", remote.getName());
                     out.finish();
                 }
             });
         }
     }
 
 }
