  /*
   * Copyright 2000-2005 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
   *
   Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
   *
   * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
   *
   * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
   *
   * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
   *
   * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
   *
   * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
   *
   * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
   *
   * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
   */
  package gov.nih.nci.ncicb.cadsr.loader.ui;
 
  import gov.nih.nci.ncicb.cadsr.loader.*;
  import gov.nih.nci.ncicb.cadsr.loader.parser.ElementWriter;
  import gov.nih.nci.ncicb.cadsr.loader.ui.tree.*;
  import gov.nih.nci.ncicb.cadsr.loader.ui.event.*;
  import gov.nih.nci.ncicb.cadsr.loader.util.*;
  import gov.nih.nci.ncicb.cadsr.loader.ui.util.*;
  import gov.nih.nci.ncicb.cadsr.loader.validator.*;
 
 
  import java.awt.Component;
  import java.awt.BorderLayout;
  import java.awt.Dimension;
  import java.awt.Toolkit;
  import java.awt.event.ActionEvent;
  import java.awt.event.ActionListener;
  import java.awt.event.KeyEvent;
 
  import java.beans.PropertyChangeListener;
  import java.beans.PropertyChangeEvent;
 
  import javax.swing.*;
 
  import java.util.*;
 
  import gov.nih.nci.ncicb.cadsr.domain.*;
 
 import java.awt.FlowLayout;
 import java.awt.event.WindowEvent;
 
  import org.apache.log4j.Logger;
  import java.lang.reflect.Method;
  import javax.swing.JOptionPane;
 
  /**
   * The main Frame containing other frames
   *
   * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
   */
  public class MainFrame extends JFrame 
    implements ViewChangeListener, CloseableTabbedPaneListener,
               PropertyChangeListener
  {
 
    private JMenuBar mainMenuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("File");
    private JMenuItem saveMenuItem = new JMenuItem("Save");
    private JMenuItem saveAsMenuItem = new JMenuItem("Save As");
    private JMenuItem exitMenuItem = new JMenuItem("Exit");
 
    private JMenu editMenu = new JMenu("Edit");
    private JMenuItem findMenuItem = new JMenuItem("Find");
    private JMenuItem prefMenuItem = new JMenuItem("Preferences");
 
    private JMenu elementMenu = new JMenu("Element");
    private JMenuItem applyMenuItem = new JMenuItem("Apply");
    private JMenuItem applyToAllMenuItem = new JMenuItem("Apply to All");
    private JMenuItem previewReuseMenuItem = new JMenuItem("Preview DE Reuse");
 
 
    private JMenu runMenu = new JMenu("Run");
    private JMenuItem validateMenuItem = new JMenuItem("Validate");
    private JMenuItem defaultsMenuItem = new JMenuItem("Defaults");
    private JMenuItem validateConceptsMenuItem = new JMenuItem("Validate Concepts");
 
    private JMenu helpMenu = new JMenu("Help");
    private JMenuItem aboutMenuItem = new JMenuItem("About");
    private JMenuItem indexMenuItem = new JMenuItem("SIW on GForge");
 
    private JSplitPane jSplitPane1 = new JSplitPane();
    private JSplitPane jSplitPane2 = new JSplitPane();
    private JTabbedPane jTabbedPane1 = new JTabbedPane();
    private CloseableTabbedPane viewTabbedPane = new CloseableTabbedPane();
 
    private DEReuseDialog reuseDialog;
    private NavigationPanel navigationPanel;
    private ErrorPanel errorPanel = null;
    private JPanel logPanel;
 
    private MainFrame _this = this;
 
    private JLabel infoLabel = new JLabel(" ");
    private JLabel filePathLabel = new JLabel(" ");
 
  //   private Map<String, UMLElementViewPanel> viewPanels = new HashMap();
    private UMLElementViewPanelFactory umlVPFactory = null;
    private NodeViewPanel viewPanel = null;
    private Map<String, NodeViewPanel> viewPanels = new HashMap();
    private AssociationViewPanel associationViewPanel = null;
  //   private ValueDomainViewPanel vdViewPanel = null;
 
    private LVDPanel lvdPanel = null;
 
    private PackageViewPanel packageViewPanel = null;
 
    private ReviewTracker ownerTracker, curatorTracker;
 
    private RunMode runMode = null;
 
    private String saveFilename = "";
 
    private ElementWriter xmiWriter = null;
 
    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private UserPreferences prefs = UserPreferences.getInstance();
    private boolean isFrameMaximed;
    private int oldState;
    private int mainFrameWidth = 0;
    private int mainFrameHeight = 0;
    private int verticleSplit = 0;
    private int horizontalSplit = 0;
    private List<String> mainFramePrefs = null;
 
    private static Logger logger = Logger.getLogger(MainFrame.class);
 
    public MainFrame()
    {
    }
 
    public void init() {
      UserSelections selections = UserSelections.getInstance();
      
      runMode = (RunMode)(selections.getProperty("MODE"));
 
  //     if(runMode.equals(RunMode.Curator))
      curatorTracker = ReviewTracker.getInstance(ReviewTrackerType.Curator);
      ownerTracker = ReviewTracker.getInstance(ReviewTrackerType.Owner);
 
      saveFilename = (String)selections.getProperty("FILENAME");
 
      this.reuseDialog = BeansAccessor.getDEReuseDialog();
      
      jbInit();
    }
 
    public void exit() {
      int verticleSplit = jSplitPane1.getDividerLocation();
      int horizontalSplit = jSplitPane2.getDividerLocation();
      
      if(isFrameMaximed){
         prefs.setMainFramePreferences(true, verticleSplit, horizontalSplit);
      }
      else{         
          Dimension dim = this.getSize();
          int mainFrameWidth = (int) dim.getWidth();
          int mainFrameHeight = (int) dim.getHeight();
          prefs.setMainFramePreferences(false, verticleSplit, horizontalSplit);
          prefs.setMainFramePreferences(mainFrameWidth, mainFrameHeight, verticleSplit, horizontalSplit);
      }
 
      if(!ChangeTracker.getInstance().isEmpty()) {
      int result = JOptionPane.showConfirmDialog((JFrame) null, "Would you like to save your file before quitting?");
      switch(result) { 
      case JOptionPane.YES_OPTION: 
        saveMenuItem.doClick();
        break;
      case JOptionPane.NO_OPTION:
        break;
      
      case JOptionPane.CANCEL_OPTION:
        return;    
      }
      System.exit(0);
      }
      else 
        System.exit(0);
    }
 
    public void propertyChange(PropertyChangeEvent evt) {
      if(evt.getPropertyName().equals("APPLY")) {
        applyMenuItem.setEnabled((Boolean)evt.getNewValue());
        applyToAllMenuItem.setEnabled((Boolean)evt.getNewValue());
        
        if((Boolean)evt.getNewValue() == true)
          infoLabel.setText("Unsaved Changes");
        else
          infoLabel.setText("Changes Applied");
      } else if(evt.getPropertyName().equals("EXPORT_ERRORS")) {
        infoLabel.setText("Export Errors Complete");
      } else if(evt.getPropertyName().equals("EXPORT_ERRORS_FAILED")) {
        infoLabel.setText("Export Errors Failed !");
      } 
    }
 
    /**
     * Set the working title on the frame window.
     * 
     * @param file_ null to use the current file name or !null to change the current file name as with
     *    a "Save As"
     */
    public void setWorkingTitle(String file_)
    {
        if (file_ != null && file_.length() > 0)
            saveFilename = file_;
 //       this.setTitle(PropertyAccessor.getProperty("siw.title") + " - " + saveFilename + " - " + runMode.getTitleName());
        filePathLabel.setText(saveFilename);
        this.setTitle(PropertyAccessor.getProperty("siw.title") + " - " + runMode.getTitleName() + " - " + 
             saveFilename.substring(saveFilename.lastIndexOf("/")+1, saveFilename.length()));
    }
 
    private void jbInit() {
      
      this.getContentPane().setLayout(new BorderLayout());
      setInitialMainFrameSize();
      this.setJMenuBar(mainMenuBar);
      this.setIconImage(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("siw-logo3_2.gif")).getImage());
      setWorkingTitle(null);
 
      jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
      setSplitPanes();
      jSplitPane1.setOneTouchExpandable(true);
      jSplitPane2.setOneTouchExpandable(true);
 
      fileMenu.add(saveMenuItem);
      fileMenu.add(saveAsMenuItem);
      fileMenu.addSeparator();
      fileMenu.add(findMenuItem);
      fileMenu.addSeparator();
      fileMenu.add(exitMenuItem);
      mainMenuBar.add(fileMenu);
 
      editMenu.add(findMenuItem);
      editMenu.add(prefMenuItem);
      mainMenuBar.add(editMenu);
      
 
      applyMenuItem.setEnabled(false);
      applyToAllMenuItem.setEnabled(false);
      previewReuseMenuItem.setEnabled(false);
      elementMenu.add(applyMenuItem);
      elementMenu.add(applyToAllMenuItem);
 
      // not in this release. re-add to get feature
      mainMenuBar.add(elementMenu);
 
      if(runMode.equals(RunMode.Reviewer)) {
        runMenu.add(validateConceptsMenuItem);
        mainMenuBar.add(runMenu);
      }
 
      if(runMode.equals(RunMode.Curator)) {
        runMenu.add(validateConceptsMenuItem);
        mainMenuBar.add(runMenu);
      }
 
      helpMenu.add(indexMenuItem);
      helpMenu.addSeparator();
      helpMenu.add(aboutMenuItem);
      mainMenuBar.add(helpMenu);
 
      navigationPanel = new NavigationPanel();
      errorPanel = new ErrorPanel(TreeBuilder.getInstance().getRootNode());
 
      errorPanel.addPropertyChangeListener(this);
      errorPanel.addNavigationListener(navigationPanel);
      navigationPanel.addNavigationListener(errorPanel);
 
      jTabbedPane1.addTab("Errors", errorPanel);
 
      Icon closeIcon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("close-tab.gif"));
      viewTabbedPane.setCloseIcons(closeIcon, closeIcon, closeIcon);
      viewTabbedPane.addCloseableTabbedPaneListener(this);
 
      jTabbedPane1.addTab("Log", logPanel);
      jSplitPane2.add(jTabbedPane1, JSplitPane.BOTTOM);
      jSplitPane2.add(viewTabbedPane, JSplitPane.TOP);
      jSplitPane1.add(jSplitPane2, JSplitPane.RIGHT);
      
      jSplitPane1.add(navigationPanel, JSplitPane.LEFT);
      
      navigationPanel.addViewChangeListener(this);
 
 
 
      JPanel infoLabelPanel = new JPanel();
      infoLabelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
      infoLabelPanel.add(infoLabel);
 
      JPanel filePathLabelPanel = new JPanel();
      filePathLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      filePathLabelPanel.add(filePathLabel);
     
      JPanel southPanel = new JPanel();
      southPanel.setLayout(new BorderLayout());
      southPanel.add(infoLabelPanel, BorderLayout.WEST);
      southPanel.add(filePathLabelPanel, BorderLayout.EAST);
      
      this.getContentPane().add(jSplitPane1, BorderLayout.CENTER);
      this.getContentPane().add(southPanel, BorderLayout.SOUTH);
 
      exitMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
              _this.exit();
          }
      });  
 
      defaultsMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            UmlDefaultsPanel dp =  new UmlDefaultsPanel(_this);
            dp.show();
            UIUtil.putToCenter(dp);
          }
      });
      
      findMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          SearchDialog sd = new SearchDialog(_this);
          UIUtil.putToCenter(sd);
          sd.addSearchListener(navigationPanel);
          sd.setVisible(true);
 
        }
      });
      
      findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,    
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      
      final PreferenceDialog pd = new PreferenceDialog(_this);
      prefMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          pd.updatePreferences();
          UIUtil.putToCenter(pd);
          pd.setVisible(true);
        }
      });
      
      saveMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          xmiWriter.setOutput(saveFilename);
          try {
            xmiWriter.write(ElementsLists.getInstance());
            
            infoLabel.setText("File Saved");
          } catch (Throwable e){
            JOptionPane.showMessageDialog(_this, "There was an error saving your File. Please contact support.", "Error Saving File", JOptionPane.ERROR_MESSAGE);
            infoLabel.setText("Save Failed!!");
            logger.error(e);
            e.printStackTrace();
          } // end of try-catch
        }
      });
      
      saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,    
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      
      saveAsMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          String saveDir = UserPreferences.getInstance().getRecentDir();
          JFileChooser chooser = new JFileChooser(saveDir);
 
          String extension = null;
          String fileType = (String)UserSelections.getInstance().getProperty("FILE_TYPE");
          if(fileType.equals("ARGO"))
            extension = "uml";
          else if(fileType.equals("EA"))
            extension = "xmi";
 
          chooser.setFileFilter(new InputFileFilter(extension));
          int returnVal = chooser.showSaveDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
              String filePath = chooser.getSelectedFile().getAbsolutePath();
              
              if(!filePath.endsWith(extension))
                filePath = filePath + "." + extension;
              
              UserPreferences.getInstance().setRecentDir(filePath);
              xmiWriter.setOutput(filePath);
              setWorkingTitle(filePath);
              try {
                xmiWriter.write(ElementsLists.getInstance());
                infoLabel.setText("File Saved");
              } catch (Throwable e){
                JOptionPane.showMessageDialog(_this, "There was an error saving your File. Please contact support.", "Error Saving File", JOptionPane.ERROR_MESSAGE);
                infoLabel.setText("Save Failed!!");
              } // end of try-catch
            }
        }
      });
 
      validateMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            ValidationItems.getInstance().clear();
            Validator validator = new UMLValidator();
            validator.validate();
 
            ElementsLists elements = ElementsLists.getInstance();
 
            TreeBuilder tb = TreeBuilder.getInstance();
            tb.init();
            tb.buildTree(elements);
 
            errorPanel.update(tb.getRootNode());
          } 
        });
        
      validateConceptsMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          int n = JOptionPane.showConfirmDialog(_this, 
            "This process may take some time. Would you like to continue? ",
            "Validate Concepts", JOptionPane.YES_NO_OPTION);
          if(n == JOptionPane.YES_OPTION) {
            ValidateConceptsDialog vcd = new ValidateConceptsDialog(_this);
            vcd.addSearchListener(navigationPanel);
            vcd.setVisible(true);
          }
        }
      });
 
      applyMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            UMLElementViewPanel viewPanel =
              (UMLElementViewPanel)viewTabbedPane
              .getSelectedComponent();
            
            try {
              viewPanel.applyPressed();
            } catch (ApplyException e){
              infoLabel.setText("Changes were not applied!");
            } // end of try-catch
            
          }
        });
      applyToAllMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            UMLElementViewPanel viewPanel =
              (UMLElementViewPanel)viewTabbedPane
              .getSelectedComponent();
            
            try {
              viewPanel.apply(true);
            } catch (ApplyException e){
              infoLabel.setText("Changes were not applied!");
            } // end of try-catch
            
          }
        });
 
      previewReuseMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
              UMLElementViewPanel viewPanel =
                  (UMLElementViewPanel)viewTabbedPane
                  .getSelectedComponent();
 
              // update dialog with current node
              reuseDialog.init(viewPanel.getConceptEditorPanel().getNode());
              UIUtil.putToCenter(reuseDialog);
              reuseDialog.setVisible(true);
          }
        });
 
      previewReuseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,    
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      
      aboutMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            new AboutPanel();
          }
        });
      
      indexMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            String errMsg = "Error attempting to launch web browser";
            String osName = System.getProperty("os.name");
            String url = "http://gforge.nci.nih.gov/projects/siw/";
            try {
              if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                                                           new Class[] {String.class});
                openURL.invoke(null, new Object[] {url});
              }
              else if (osName.startsWith("Windows"))
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
              else { //assume Unix or Linux
                String[] browsers = {
                  "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++)
                  if (Runtime.getRuntime().exec(
                        new String[] {"which", browsers[count]}).waitFor() == 0)
                    browser = browsers[count];
                if (browser == null)
                  throw new Exception("Could not find web browser");
                else
                  Runtime.getRuntime().exec(new String[] {browser, url});
              }
            } catch (Exception e) {
              JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
            }
          }
        });      
    }
 
    public void viewChanged(ViewChangeEvent event) {
        UMLNode node = (UMLNode)event.getViewObject();
      
      previewReuseMenuItem.setEnabled(false);
 
      if(event.getType() == ViewChangeEvent.VIEW_CONCEPTS
         || event.getType() == ViewChangeEvent.VIEW_VALUE_MEANING
         || event.getType() == ViewChangeEvent.VIEW_INHERITED) {
        
        // If concept is already showing, just bring it up front
        if(viewPanels.containsKey(node.getFullPath())) {
          NodeViewPanel pa = viewPanels.get(node.getFullPath());
          viewTabbedPane.setSelectedComponent((JPanel)pa);
          return;
        }
        
        if (node instanceof AttributeNode) {
          DataElement de = (DataElement)node.getUserObject();
          if (StringUtil.isEmpty(de.getPublicId())) {
            previewReuseMenuItem.setEnabled(true);
          }
        }
        
        // if we ask for new tab, or no tab yet, or it's an assoc or a VD. 
        // then open a new tab.
        if((event.getInNewTab() == true) || (viewPanels.size() == 0) 
            || viewTabbedPane.getSelectedComponent() instanceof AssociationViewPanel
            || viewTabbedPane.getSelectedComponent() instanceof ValueDomainViewPanel) {
 
 
          newTab(event, node);
 
        } else { // if not, update current tab.
          NodeViewPanel viewPanel = (NodeViewPanel)viewTabbedPane.getSelectedComponent();
 
          viewTabbedPane.remove(viewTabbedPane.getSelectedIndex());
          viewPanels.remove(viewPanel.getName());
          newTab(event, node);
 
        }
 
      } else if(event.getType() == ViewChangeEvent.VIEW_ASSOCIATION) {
 
        if(associationViewPanel == null) {
          associationViewPanel = new AssociationViewPanel(node);
 
          associationViewPanel.addPropertyChangeListener(this);
          associationViewPanel.addReviewListener(navigationPanel);
          associationViewPanel.addReviewListener(ownerTracker);
          associationViewPanel.addReviewListener(curatorTracker);
          associationViewPanel.addElementChangeListener(ChangeTracker.getInstance());
          associationViewPanel.addNavigationListener(navigationPanel);
          navigationPanel.addNavigationListener(associationViewPanel);
          
          viewTabbedPane.addTab("Association", associationViewPanel);
          associationViewPanel.setName("Association");
          infoLabel.setText("Association");
          associationViewPanel.addCustomPropertyChangeListener(this);
        } else
          associationViewPanel.update(node);
 
        viewTabbedPane.setSelectedComponent(associationViewPanel);
 
      } else if(event.getType() == ViewChangeEvent.VIEW_VALUE_DOMAIN) {
        
  //      if(vdViewPanel == null) {
  //        vdViewPanel = new ValueDomainViewPanel((ValueDomain)node.getUserObject(), node);
  //        viewTabbedPane.addTab("ValueDomain", vdViewPanel);
  //        vdViewPanel.setName("ValueDomain");
  //        infoLabel.setText("ValueDomain");
  //      }
  //      else
        
  //       viewTabbedPane.remove(vdViewPanel);
  //       viewTabbedPane.addTab("ValueDomain", vdViewPanel);
  //       vdViewPanel.update((ValueDomain)node.getUserObject(), node);
  //       infoLabel.setText("ValueDomain");      
 
  //       viewTabbedPane.setSelectedComponent(vdViewPanel);
 
           viewTabbedPane.remove(lvdPanel);
           viewTabbedPane.addTab("ValueDomain."+LookupUtil.lookupFullName((ValueDomain)node.getUserObject()), lvdPanel);
 //          viewTabbedPane.addTab("ValueDomain", lvdPanel);
           lvdPanel.update((ValueDomain)node.getUserObject(), node);
           lvdPanel.addNavigationListener(navigationPanel);
           navigationPanel.addNavigationListener(lvdPanel);
           infoLabel.setText("ValueDomain");      
           viewTabbedPane.setSelectedComponent(lvdPanel);
 
  //    
 
      } else if(event.getType() == ViewChangeEvent.VIEW_PACKAGE) {
        
        if(!viewPanels.containsValue(packageViewPanel))
          packageViewPanel = null;
 
        if(node.getUserObject() == null)
          return;
 
        if(packageViewPanel == null) {
          packageViewPanel = new PackageViewPanel(node);
          // show only the last 15 chars of the package in tab title
          String disp  = node.getFullPath();
          if(disp.length() > 15)
            disp = disp.substring(disp.length() - 15);
 
          viewTabbedPane.addTab(disp, packageViewPanel);
          packageViewPanel.setName("Package");
          infoLabel.setText(node.getFullPath());
        }
        else
          packageViewPanel.updateNode(node);
        
        viewTabbedPane.setSelectedComponent(packageViewPanel);
      }
    }
 
    private void newTab(ViewChangeEvent event, UMLNode node) {
      String tabTitle = node.getDisplay();
      if(node instanceof AttributeNode) {
          if(event.getType() == ViewChangeEvent.VIEW_INHERITED) 
              tabTitle = node.getParent().getParent().getDisplay() 
              + "." + tabTitle;
          else
              tabTitle = node.getParent().getDisplay() 
              + "." + tabTitle;
      }
      
      if(event.getType() == ViewChangeEvent.VIEW_INHERITED) {
        viewPanel = new InheritedAttributeViewPanel(node);
      } else {
 //       viewPanel = new UMLElementViewPanel(node);
         viewPanel = umlVPFactory.createUMLElementViewPanel(node);
         viewPanel.updateNode(node);
      }          
      
      viewPanel.addPropertyChangeListener(this);
      viewPanel.addReviewListener(navigationPanel);
      viewPanel.addReviewListener(ownerTracker);
      viewPanel.addReviewListener(curatorTracker);
      viewPanel.addElementChangeListener(ChangeTracker.getInstance());
      viewPanel.addNavigationListener(navigationPanel);
      navigationPanel.addNavigationListener(viewPanel);
      
      
      viewTabbedPane.addTab(tabTitle, (JPanel)viewPanel);
      viewTabbedPane.setSelectedComponent((JPanel)viewPanel);
      
      viewPanel.setName(node.getFullPath());
      viewPanels.put(viewPanel.getName(), viewPanel);
      
      infoLabel.setText(tabTitle);
    }
    
    public boolean closeTab(int index) {
 
      Component c = viewTabbedPane.getComponentAt(index);
      if(c.equals(associationViewPanel))
        associationViewPanel = null;
  //     if(c.equals(vdViewPanel))
  //       vdViewPanel = null;
  //     if(c.equals(lvdPanel))
  //       lvdPanel = null;
      
      if(c.equals(packageViewPanel))
        packageViewPanel = null;
      viewPanels.remove(c.getName());
 
      if (viewPanel instanceof UMLElementViewPanel)
          umlVPFactory.removeFromList((UMLElementViewPanel)viewPanel);
    
      return true;
    }
 
    public void setXmiWriter(ElementWriter writer) {
      this.xmiWriter = writer;
    }
   
    public void setLogTab(JPanel panel) {
      this.logPanel = panel;
    }
 
    public void setLvdPanel(LVDPanel lvdPanel) {
      this.lvdPanel = lvdPanel;
 
      lvdPanel.addElementChangeListener(ChangeTracker.getInstance());
      lvdPanel.addPropertyChangeListener(this);
 
    }
   
      public void stateChanged(WindowEvent e){
          int currentState = e.getNewState();
          oldState = e.getOldState();
          isFrameMaximed = (currentState & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
          if(isFrameMaximed){
              this.setExtendedState(MAXIMIZED_BOTH);
              setSplitPanesFields();
          } else{
              setMainFrameFields();
              setSplitPanesFields();
              this.setSize(new Dimension(mainFrameWidth, mainFrameHeight));  //added
 //             putToCenter();
          }
          setSplitPanes();
      }
 
      private void putToCenter() {
        this.setLocation((screenSize.width - this.getSize().width) / 2, (screenSize.height - this.getSize().height) / 2);
      }
 
     private void setInitialMainFrameSize(){
      this.isFrameMaximed = prefs.isMainFrameMaximized();
      if(isFrameMaximed){
          this.setExtendedState(MAXIMIZED_BOTH);
        } else {
          setMainFrameFields();
          this.setSize(new Dimension(mainFrameWidth, mainFrameHeight));  //added
        }
        setSplitPanesFields();
      }
      
      private void setMainFrameFields(){
          mainFramePrefs = prefs.getMainFramePreferences();
          mainFrameWidth = ((mainFramePrefs == null || mainFramePrefs.get(0) == null) ? 930 : 
              Integer.parseInt(mainFramePrefs.get(0)));
          mainFrameHeight = ((mainFramePrefs == null || mainFramePrefs.get(1) == null) ? 700 : 
              Integer.parseInt(mainFramePrefs.get(1)));
      }
      
      private void setSplitPanesFields(){
          verticleSplit = ((mainFramePrefs == null || mainFramePrefs.get(2) == null) ? 160 : 
              Integer.parseInt(mainFramePrefs.get(2)));
          horizontalSplit = ((mainFramePrefs == null || mainFramePrefs.get(3) == null) ? 450 : 
              Integer.parseInt(mainFramePrefs.get(3)));
      }
 
      private void setSplitPanes(){
          jSplitPane1.setDividerLocation(verticleSplit); // added
          jSplitPane2.setDividerLocation(horizontalSplit); // added
      }
      
      public void setUmlVPFactory(UMLElementViewPanelFactory umlVPFactory) {
         this.umlVPFactory = umlVPFactory;
      }
 
  //   public void setValueDomainViewPanel(ValueDomainViewPanel vdViewPanel) {
  //     this.vdViewPanel = vdViewPanel;
  //     vdViewPanel.addElementChangeListener(ChangeTracker.getInstance());
  //     vdViewPanel.addPropertyChangeListener(this);
  //   }
   
  }
