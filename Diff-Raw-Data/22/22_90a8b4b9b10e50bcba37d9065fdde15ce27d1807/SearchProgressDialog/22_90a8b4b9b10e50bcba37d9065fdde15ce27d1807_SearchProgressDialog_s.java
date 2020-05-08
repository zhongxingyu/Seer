 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * SearchProgressDialog.java
  *
  * Created on 18. November 2003, 15:00
  */
 package Sirius.navigator.search.dynamic;
 
 import Sirius.navigator.connection.*;
 import Sirius.navigator.exception.*;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.ui.status.*;
import Sirius.navigator.ui.tree.*;
 import Sirius.navigator.ui.widget.*;
 
import Sirius.server.middleware.types.Node;
 import Sirius.server.search.SearchResult;
 
 import org.apache.log4j.Logger;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.*;
 
 import javax.swing.*;
 
 import de.cismet.tools.CismetThreadPool;
 
 /**
  * DOCUMENT ME!
  *
  * @author   pascal
  * @version  $Revision$, $Date$
  */
 public class SearchProgressDialog extends javax.swing.JDialog {
 
     //~ Instance fields --------------------------------------------------------
 
     private final Logger logger;
     private final ResourceManager resources;
     private final DefaultStatusChangeSupport statusChangeSupport;
     private final MutableImageLabel animationLabel;
     private SearchThread searchThread;
     /** Holds value of property canceld. */
     private boolean canceld;
     /** Holds value of property resultNodes. */
     // private Node[] resultNodes;
     /** Holds value of property searchResult. */
     private SearchResult searchResult;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton cancelButton;
     private javax.swing.JPanel iconPanel;
     // End of variables declaration//GEN-END:variables
     // ##########################################################################
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form SearchProgressDialog.
      *
      * @param  parent               DOCUMENT ME!
      * @param  statusChangeSupport  DOCUMENT ME!
      */
     public SearchProgressDialog(final Frame parent, final DefaultStatusChangeSupport statusChangeSupport) {
         super(
             parent,
             org.openide.util.NbBundle.getMessage(SearchProgressDialog.class, "SearchProgressDialog.title"), // NOI18N
             true);
         this.logger = Logger.getLogger(this.getClass());
 
         this.resources = ResourceManager.getManager();
         this.statusChangeSupport = statusChangeSupport;
 
         initComponents();
 
         this.animationLabel = new MutableImageLabel(
                 resources.getIcon("SearchIcon01.gif"), // NOI18N
                 resources.getIcon("SearchIcon02.gif")); // NOI18N
         this.iconPanel.add(this.animationLabel, BorderLayout.CENTER);
         this.cancelButton.addActionListener(new ButtonListener());
 
         this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
     }
 
     /**
      * Creates a new SearchProgressDialog object.
      *
      * @param  parent               DOCUMENT ME!
      * @param  statusChangeSupport  DOCUMENT ME!
      */
     public SearchProgressDialog(final JDialog parent, final DefaultStatusChangeSupport statusChangeSupport) {
         super(
             parent,
             org.openide.util.NbBundle.getMessage(SearchProgressDialog.class, "SearchProgressDialog.title"), // NOI18N
             true);
         this.logger = Logger.getLogger(this.getClass());
 
         this.resources = ResourceManager.getManager();
         this.statusChangeSupport = statusChangeSupport;
 
         initComponents();
 
         this.animationLabel = new MutableImageLabel(
                 resources.getIcon("SearchIcon01.gif"), // NOI18N
                 resources.getIcon("SearchIcon02.gif")); // NOI18N
         this.iconPanel.add(this.animationLabel, BorderLayout.CENTER);
         this.cancelButton.addActionListener(new ButtonListener());
 
         this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  b  DOCUMENT ME!
      */
     public void setLabelAnimation(final boolean b) {
         this.animationLabel.switchOn(
             b);
     }
     /**
      * .........................................................................
      *
      * @param  classNodeKeys  DOCUMENT ME!
      * @param  searchOptions  DOCUMENT ME!
      */
     public void show(final Collection classNodeKeys, final Collection searchOptions) {
         if ((this.searchThread != null) && this.searchThread.isAlive()) {
             logger.warn("search thread is still running"); // NOI18N
 
             try {
                 // TODO display warning message
                 this.searchThread.join();
                 if (logger.isDebugEnabled()) {
                     logger.debug("searchThread.join() successfull"); // NOI18N
                 }
             } catch (InterruptedException iexp) {
                 if (logger.isDebugEnabled()) {
                     logger.warn(iexp.getMessage(), iexp);
                 }
             }
         }
 
         this.statusChangeSupport.fireStatusChange(
             org.openide.util.NbBundle.getMessage(
                 SearchProgressDialog.class,
                 "SearchProgressDialog.show(Collection,Collection).status.running"), // NOI18N
             Status.MESSAGE_POSITION_2,
             Status.ICON_IGNORE,
             Status.ICON_BLINKING);
 
         this.animationLabel.switchOn(
             true);
         // this.setResultNodes(null);
 
         this.setSearchResult(
             null);
 
         this.setCanceld(
             false);
 
         searchThread = new SearchThread(classNodeKeys, searchOptions);
 //        searchThread.start();
         CismetThreadPool.execute(searchThread);
 
         if (logger.isDebugEnabled()) {
             logger.debug("waiting for search thread to finish"); // NOI18N
         }                                                        // this.pack();
 
         super.show();
     }
 
     // .........................................................................
     /**
      * Getter for property canceld.
      *
      * @return  Value of property canceld.
      */
     public synchronized boolean isCanceld() {
         return this.canceld;
     }
 
     /**
      * Setter for property canceld.
      *
      * @param  canceld  New value of property canceld.
      */
     private synchronized void setCanceld(final boolean canceld) {
         this.canceld = canceld;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         final javax.swing.JPanel contentPanel = new javax.swing.JPanel();
         iconPanel = new javax.swing.JPanel();
         final javax.swing.JLabel infoLabel = new javax.swing.JLabel();
         final javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
         cancelButton = new javax.swing.JButton();
 
         addWindowListener(new java.awt.event.WindowAdapter() {
 
                 @Override
                 public void windowClosing(final java.awt.event.WindowEvent evt) {
                     closeDialog(evt);
                 }
             });
 
         contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 0, 5));
         contentPanel.setLayout(new java.awt.GridBagLayout());
 
         iconPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                 javax.swing.BorderFactory.createEtchedBorder(),
                 javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
         iconPanel.setLayout(new java.awt.BorderLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 1.0;
         contentPanel.add(iconPanel, gridBagConstraints);
 
         infoLabel.setText(org.openide.util.NbBundle.getMessage(
                 SearchProgressDialog.class,
                 "SearchProgressDialog.infoLabel.text")); // NOI18N
         infoLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                 javax.swing.BorderFactory.createEtchedBorder(),
                 javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
         contentPanel.add(infoLabel, gridBagConstraints);
 
         getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);
 
         buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         buttonPanel.setLayout(new java.awt.BorderLayout());
 
         cancelButton.setMnemonic(org.openide.util.NbBundle.getMessage(
                 SearchProgressDialog.class,
                 "SearchProgressDialog.cancelButton.mnemonic").charAt(0));
         cancelButton.setText(org.openide.util.NbBundle.getMessage(
                 SearchProgressDialog.class,
                 "SearchProgressDialog.cancelButton.text"));    // NOI18N
         cancelButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 SearchProgressDialog.class,
                 "SearchProgressDialog.cancelButton.tooltip")); // NOI18N
         buttonPanel.add(cancelButton, java.awt.BorderLayout.CENTER);
 
         getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * Closes the dialog.
      *
      * @param  evt  DOCUMENT ME!
      */
     private void closeDialog(final java.awt.event.WindowEvent evt) //GEN-FIRST:event_closeDialog
     {
         setVisible(false);
         dispose();
     }                                                              //GEN-LAST:event_closeDialog
 
     /**
      * Getter for property searchResult.
      *
      * @return  Value of property searchResult.
      */
     public SearchResult getSearchResult() {
         return this.searchResult;
     }
 
     /**
      * Setter for property searchResult.
      *
      * @param  searchResult  New value of property searchResult.
      */
     public void setSearchResult(final SearchResult searchResult) {
         this.searchResult = searchResult;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * Getter for property resultNodes.
      *
      * @return   Value of property resultNodes.
      *
      * @version  $Revision$, $Date$
      */
     /*public synchronized Node[] getResultNodes()
      * { return this.resultNodes;}*/
     /**
      * Setter for property resultNodes.
      *
      * @param    resultNodes  New value of property resultNodes.
      *
      * @version  $Revision$, $Date$
      */
     /*private synchronized void setResultNodes(Node[] resultNodes)
      * { this.resultNodes = resultNodes;}*/
     // -------------------------------------------------------------------------
     private class ButtonListener implements ActionListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void actionPerformed(final ActionEvent e) {
             SearchProgressDialog.this.statusChangeSupport.fireStatusChange(
                 org.openide.util.NbBundle.getMessage(
                     SearchProgressDialog.class,
                     "SearchProgressDialog.ButtonListener.actionPerformed(ActionEvent).status.canceled"), // NOI18N
                 Status.MESSAGE_POSITION_2,
                 Status.ICON_DEACTIVATED,
                 Status.ICON_DEACTIVATED);
             SearchProgressDialog.this.animationLabel.switchOff(true);
             SearchProgressDialog.this.setCanceld(true);
             // SearchProgressDialog.this.setResultNodes(null);
             SearchProgressDialog.this.setSearchResult(null);
 
             SearchProgressDialog.this.dispose();
         }
     }
 
 // -------------------------------------------------------------------------
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class SearchThread extends Thread {
 
         //~ Instance fields ----------------------------------------------------
 
         private final Logger logger;
         private final Collection classNodeKeys;
         private final Collection searchOptions;
         // private Node[] resultNodes = null;
         private SearchResult searchResult = null;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new SearchThread object.
          *
          * @param  classNodeKeys  DOCUMENT ME!
          * @param  searchOptions  DOCUMENT ME!
          */
         private SearchThread(final Collection classNodeKeys, final Collection searchOptions) {
             super("SearchThread"); // NOI18N
 
             this.logger = Logger.getLogger(this.getClass());
 
             this.classNodeKeys = classNodeKeys;
             this.searchOptions = searchOptions;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void run() {
             logger.info("starting new search with ");                            // NOI18N
             if (logger.isDebugEnabled()) {
                 logger.debug("# classNodeKeys: " + classNodeKeys.size() + ", #  searchOptions: "
                             + searchOptions.size());                             // NOI18N
             }
             if (!SearchProgressDialog.this.isCanceld()) {
                 try {
                     if ((this.classNodeKeys != null) && (this.classNodeKeys.size() > 0)) {
                         if (logger.isDebugEnabled()) {
                             logger.debug("performing search with class ids");    // NOI18N
                         }
                         this.searchResult = SessionManager.getProxy().search(this.classNodeKeys, this.searchOptions);
                     } else {
                         if (logger.isDebugEnabled()) {
                             logger.debug("performing search without class ids"); // NOI18N
                         }
                         this.searchResult = SessionManager.getProxy().search(this.searchOptions);
                     }
 
                     if (!SearchProgressDialog.this.isCanceld()) {
                         SearchProgressDialog.this.setSearchResult(this.searchResult);
 
                         if (this.searchResult.isNode() && (this.searchResult.getNodes() != null)
                                     && (this.searchResult.getNodes().length > 0)) {
                             if (logger.isInfoEnabled()) {
                                 logger.info(this.searchResult.getNodes().length + " nodes found");      // NOI18N
                             }
                             SearchProgressDialog.this.statusChangeSupport.fireStatusChange(
                                 this.searchResult.getNodes().length
                                         + org.openide.util.NbBundle.getMessage(
                                             SearchProgressDialog.class,
                                             "SearchProgressDialog.SearchThread.run().status.results"),  // NOI18N
                                 Status.MESSAGE_POSITION_2,
                                 Status.ICON_ACTIVATED,
                                 Status.ICON_DEACTIVATED);
                         } else if (this.searchResult.isObject()) {
                             logger.info(this.searchResult.getObjects().length + " meta objects found"); // NOI18N
                         } else if (this.searchResult.isSearchParameter()) {
                             if (logger.isDebugEnabled()) {
                                 logger.debug("searchParameter found");                                  // NOI18N
                             }
                             SearchProgressDialog.this.statusChangeSupport.fireStatusChange(
                                 "",
                                 Status.MESSAGE_POSITION_2,
                                 Status.ICON_ACTIVATED,
                                 Status.ICON_DEACTIVATED);                                               // NOI18N
                         } else if (logger.isDebugEnabled()) {
                             logger.warn("no search results found: " + this.searchResult.getResult() + "("
                                         + this.searchResult.getResult().getClass() + ")");              // NOI18N
                             SearchProgressDialog.this.statusChangeSupport.fireStatusChange(
                                 org.openide.util.NbBundle.getMessage(
                                     SearchProgressDialog.class,
                                     "SearchProgressDialog.SearchThread.run().status.noresults"),        // NOI18N
                                 Status.MESSAGE_POSITION_2,
                                 Status.ICON_ACTIVATED,
                                 Status.ICON_DEACTIVATED);
                         }
 
                         SearchProgressDialog.this.dispose();
                     }
                 } catch (Throwable t) {
                     logger.error("could not perform search", t); // NOI18N
                     SearchProgressDialog.this.setSearchResult(null);
                     SearchProgressDialog.this.animationLabel.switchOff(true);
 
                     if (!SearchProgressDialog.this.isCanceld()) {
                         SearchProgressDialog.this.statusChangeSupport.fireStatusChange(
                             org.openide.util.NbBundle.getMessage(
                                 SearchProgressDialog.class,
                                 "SearchProgressDialog.SearchThread.run().status.error"), // NOI18N
                             Status.MESSAGE_POSITION_2,
                             Status.ICON_DEACTIVATED,
                             Status.ICON_ACTIVATED);
                         ExceptionManager.getManager()
                                 .showExceptionDialog(
                                     ExceptionManager.ERROR,
                                     org.openide.util.NbBundle.getMessage(
                                         SearchProgressDialog.class,
                                         "SearchProgressDialog.SearchThread.run().name"), // NOI18N
                                     org.openide.util.NbBundle.getMessage(
                                         SearchProgressDialog.class,
                                         "SearchProgressDialog.SearchThread.run().message"), // NOI18N
                                     t);
                         SearchProgressDialog.this.dispose();
                     }
                 }
             }
         }
     }
     /**
      * @param args the command line arguments
      */
     /*public static void main(String args[])
      * { //new SearchProgressDialog(new javax.swing.JFrame(), true).show(); System.exit(0);}*/
 }
