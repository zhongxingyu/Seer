 package org.mvnsearch.snippet.plugin.actions;
 
 import com.intellij.openapi.actionSystem.ActionManager;
 import com.intellij.openapi.actionSystem.ActionToolbar;
 import com.intellij.openapi.actionSystem.DefaultActionGroup;
 import com.intellij.openapi.util.IconLoader;
 import org.mvnsearch.snippet.Snippet;
 import org.mvnsearch.snippet.SnippetSearchAgent;
 import org.mvnsearch.snippet.SnippetSearchAgentsFactory;
 import org.mvnsearch.snippet.plugin.ui.tree.ResultNode;
 import org.mvnsearch.snippet.plugin.ui.tree.RootNode;
 import org.mvnsearch.snippet.plugin.ui.tree.SearchAgentNode;
 
 import javax.swing.*;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 /**
  * snippet search panel form
  *
  * @author Anki R Nelaturu
  */
 public class SearchPanelForm {
     public JPanel mainPanel;
     private JTextField searchForTextField;  //search field
     private JButton goButton;   //search button
     private JTabbedPane tabbedPane1;
     private JEditorPane htmlPane;  //html description pane
     private JEditorPane codeEditorPane;  //code editor pane
     private JPanel codePanel;  //code panel with actions included
     private JTree resultsTree; //result tree
     private JProgressBar progressBar; //progress bar
     private JComboBox repositoryCombo;  //repository combo
     private RootNode rootNode = new RootNode();  //tree root node
     private java.util.List<Snippet> currentCodeSnippets = new ArrayList<Snippet>();
     private int currentCodeSnippetIndex = 0;
 
     /**
      * construct search panel form
      */
     public SearchPanelForm() {
         goButton.setIcon(IconLoader.findIcon("/org/mvnsearch/snippet/plugin/icons/search.png"));
         resultsTree.setModel(new DefaultTreeModel(rootNode));
         final ActionManager actionManager = ActionManager.getInstance();
         final DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction("eSnippet.ToolGroup");
         final ActionToolbar actionToolbar = actionManager.createActionToolbar("eSnippet.ToolGroup", actionGroup, false);
         codePanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
         for (SnippetSearchAgent agent : SnippetSearchAgentsFactory.getInstance().getSnippetManagers()) {
             repositoryCombo.addItem(agent);
         }
         searchForTextField.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                 search();
             }
         });
         goButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                 search();
             }
         });
         resultsTree.setLargeModel(true);
         resultsTree.setRowHeight(18);
         resultsTree.addTreeSelectionListener(new TreeSelectionListener() {
             public void valueChanged(final TreeSelectionEvent treeSelectionEvent) {
                 new Thread(new Runnable() {
                     public void run() {
                         startProgress();
                         Object pe = treeSelectionEvent.getPath().getLastPathComponent();
                         if (pe instanceof ResultNode) {
                             displaySnippetNode((ResultNode) pe);
                         } else if (pe instanceof SearchAgentNode) {
                             displayAgentNode((SearchAgentNode) pe);
                         }
                         endProgress();
                     }
                 }).start();
             }
         });
     }
 
     /**
      * start logic execution progress
      */
     private void startProgress() {
         searchForTextField.setEnabled(false);
         goButton.setEnabled(false);
         resultsTree.setEnabled(false);
         progressBar.setIndeterminate(true);
     }
 
     /**
      * stop logic execution progress
      */
     private void endProgress() {
         searchForTextField.setEnabled(true);
         goButton.setEnabled(true);
         resultsTree.setEnabled(true);
         progressBar.setIndeterminate(false);
     }
 
     /**
      * display agent info
      *
      * @param searchAgentNode search agent node
      */
     private void displayAgentNode(SearchAgentNode searchAgentNode) {
         SnippetSearchAgent agent = searchAgentNode.getAgent();
         codeEditorPane.setText(agent.getDescription());
         htmlPane.setText(agent.getDescription());
         currentCodeSnippetIndex = -1;
     }
 
     /**
      * display snippet info mation
      *
      * @param snippetNode snippet node
      */
     private void displaySnippetNode(ResultNode snippetNode) {
         Snippet snippet = snippetNode.getSnippet();
         currentCodeSnippetIndex = currentCodeSnippets.indexOf(snippet);
         refreshSnippetInfo();
     }
 
     /**
      * get crrent snippet's code
      *
      * @return code
      */
     public String getCurrentSnippetCode() {
         try {
             return codeEditorPane.getDocument().getText(0, codeEditorPane.getDocument().getLength());
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
         return "";
     }
 
     /**
      * step into next snippet
      */
     public void nextCodeSnippet() {
         if (currentCodeSnippetIndex < currentCodeSnippets.size() - 1) {
             currentCodeSnippetIndex++;
             refreshSnippetInfo();
             hightSnippetNode();
         }
     }
 
     /**
      * step into previous snippet
      */
     public void previousCodeSnippet() {
         if (currentCodeSnippetIndex > 0) {
             currentCodeSnippetIndex--;
             refreshSnippetInfo();
             hightSnippetNode();
         }
     }
 
     /**
      * hight snippet node
      */
     private void hightSnippetNode() {
         DefaultMutableTreeNode snippetNode = (DefaultMutableTreeNode) rootNode.getChildAt(0).getChildAt(currentCodeSnippetIndex);
         TreePath path = new TreePath(snippetNode.getPath());
         resultsTree.getSelectionModel().setSelectionPath(path);
     }
 
     /**
      * has next snippet
      *
      * @return result
      */
     public boolean hasNext() {
         return currentCodeSnippetIndex < currentCodeSnippets.size() - 1;
     }
 
     /**
      * has previous snippet
      *
      * @return result
      */
     public boolean hasPrevious() {
         return currentCodeSnippetIndex > 0;
     }
 
     /**
      * refresh code  editor pane
      */
     public void refreshSnippetInfo() {
         if (currentCodeSnippets.size() <= 0) {
             codeEditorPane.setText("<html><body><h1>No code snippets found. Sorry</ht></body></html>");
             return;
         }
         Snippet snippet = currentCodeSnippets.get(currentCodeSnippetIndex);
         htmlPane.setText(snippet.getDescription());
         htmlPane.setCaretPosition(0);
         codeEditorPane.setContentType("text/xml");
         codeEditorPane.setText(snippet.getCode());
         codeEditorPane.setCaretPosition(0);
     }
 
     /**
      * execute search logic
      */
     private void search() {
         new Thread(new Runnable() {
             public void run() {
                 startProgress();
                 searchSnippets();
                 endProgress();
             }
         }).start();
     }
 
     /**
      * search snippets
      */
     private void searchSnippets() {
         String keywords = searchForTextField.getText().trim();
         if (keywords.length() > 0) {
             rootNode.removeAllChildren();
             SnippetSearchAgent agent = (SnippetSearchAgent) repositoryCombo.getSelectedItem(); //SnippetSearchAgentsFactory.getInstance().findAgent(agentId);
             currentCodeSnippets = agent.query(keywords.split("\\s+"));
             SearchAgentNode agentNode = new SearchAgentNode(agent, currentCodeSnippets.size());
             for (Snippet snippet : currentCodeSnippets) {
                 agentNode.add(new ResultNode(snippet));
             }
             rootNode.add(agentNode);
             resultsTree.invalidate();
             resultsTree.updateUI();
             resultsTree.validate();
             int rc = resultsTree.getRowCount();
             for (int i = 0; i < rc; i++) {
                 resultsTree.expandRow(i);
             }
         }
     }
 
     /**
      * get selected agent
      *
      * @return selected agent
      */
     public SnippetSearchAgent getSelectedAgent() {
         return (SnippetSearchAgent) repositoryCombo.getSelectedItem();
     }
 
     /**
      * get current snippet
      *
      * @return snippet object
      */
     public Snippet getCurrentSnippet() {
        if (currentCodeSnippetIndex < 0 || currentCodeSnippets.isEmpty()) return null;
         return currentCodeSnippets.get(currentCodeSnippetIndex);
     }
 }
