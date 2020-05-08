 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver;
 
 import org.apache.log4j.Logger;
 
 import org.openide.nodes.Node;
 import org.openide.util.HelpCtx;
 import org.openide.util.NbBundle;
 import org.openide.util.RequestProcessor;
 import org.openide.util.actions.NodeAction;
 import org.openide.windows.WindowManager;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.JOptionPane;
 
 import de.cismet.cids.abf.domainserver.project.DomainserverProject;
 import de.cismet.cids.abf.utilities.windows.ErrorUtils;
 
 import de.cismet.diff.db.DatabaseConnection;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public final class DeleteLockAction extends NodeAction {
 
     //~ Static fields/initializers ---------------------------------------------
 
    private static final transient Logger LOG = Logger.getLogger(DeleteLockAction.class);

    private static final RequestProcessor proc = new RequestProcessor("DeleteLockActionRP", 7); // NOI18N
 
     private static final String DELETE_LOCK_STMT = "DELETE FROM cs_locks WHERE id = "; // NOI18N
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     protected void performAction(final Node[] activatedNodes) {
         final DomainserverProject project = activatedNodes[0].getLookup().lookup(DomainserverProject.class);
         final int answer = JOptionPane.showOptionDialog(
                 WindowManager.getDefault().getMainWindow(),
                 org.openide.util.NbBundle.getMessage(
                     DeleteLockAction.class,
                     "DeleteLockAction.performAction(Node[]).JOptionPane.message"),       // NOI18N
                 org.openide.util.NbBundle.getMessage(
                     DeleteLockAction.class,
                     "DeleteLockAction.performAction(Node[]).JOptionPane.title"),         // NOI18N
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.WARNING_MESSAGE,
                 null,
                 new Object[] {
                     org.openide.util.NbBundle.getMessage(
                         DeleteLockAction.class,
                         "DeleteLockAction.performAction(Node[]).JOptionPane.yesOption"), // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         DeleteLockAction.class,
                         "DeleteLockAction.performAction(Node[]).JOptionPane.noOption")   // NOI18N
                 },
                 org.openide.util.NbBundle.getMessage(
                     DeleteLockAction.class,
                     "DeleteLockAction.performAction(Node[]).JOptionPane.noOption"));     // NOI18N
         if (answer != JOptionPane.YES_OPTION) {
             return;
         }
         Connection con = null;
         ResultSet set = null;
         try {
             con = DatabaseConnection.getConnection(project.getRuntimeProps());
             set = con.createStatement().executeQuery(DomainserverProject.STMT_READ_LOCKS);
             while (set.next()) {
                 final Integer id = set.getInt("id");                                     // NOI18N
                 con.createStatement().executeUpdate(DELETE_LOCK_STMT + id);
             }
         } catch (final Exception e) {
             LOG.error("could not remove lock", e);                                       // NOI18N
             ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                     DeleteLockAction.class,
                     "DeleteLockAction.performAction(Node[]).ErrorUtils.atLeastOneLockUndeleteableError.message"), // NOI18N
                 org.openide.util.NbBundle.getMessage(
                     DeleteLockAction.class,
                     "DeleteLockAction.performAction(Node[]).ErrorUtils.atLeastOneLockUndeleteableError.title"),
                 e);                                                                      // NOI18N
         } finally {
             DatabaseConnection.closeResultSet(set);
             DatabaseConnection.closeConnection(con);
         }
     }
 
     @Override
     public String getName() {
        return NbBundle.getMessage(DeleteLockAction.class, "DeleteLockAction.getName().returnvalue"); // NOI18N
     }
 
     @Override
     public HelpCtx getHelpCtx() {
         return HelpCtx.DEFAULT_HELP;
     }
 
     @Override
     protected boolean asynchronous() {
         return false;
     }
 
     @Override
     protected String iconResource() {
         return DomainserverProject.IMAGE_FOLDER + "unlock.png"; // NOI18N
     }
 
     @Override
     protected boolean enable(final Node[] nodes) {
         if (nodes.length != 1) {
             return false;
         }

        final DomainserverProject project = nodes[0].getLookup().lookup(DomainserverProject.class);
        if ((project == null) || project.isConnected() || project.isConnectionInProgress()) {
             return false;
         }

         if (!nodes[0].getName().equals(project.getProjectDirectory().getName())) {
             return false;
         }

         return lockExists(project);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   project  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean lockExists(final DomainserverProject project) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("checking for locks"); // NOI18N
         }
         Connection con = null;
         ResultSet set = null;
         try {
             final FutureTask<Connection> task = new FutureTask(new Callable<Connection>() {
 
                         @Override
                         public Connection call() throws Exception {
                             return DatabaseConnection.getConnection(project.getRuntimeProps(), 2);
                         }
                     });
            proc.post(task);
             con = task.get(300, TimeUnit.MILLISECONDS);
             set = con.createStatement().executeQuery(DomainserverProject.STMT_READ_LOCKS);
 
             return set.next();
         } catch (final Exception e) {
             LOG.warn("could not check for locks", e); // NOI18N
             // possibly notify the user
             return false;
         } finally {
             DatabaseConnection.closeResultSet(set);
             DatabaseConnection.closeConnection(con);
         }
     }
 }
