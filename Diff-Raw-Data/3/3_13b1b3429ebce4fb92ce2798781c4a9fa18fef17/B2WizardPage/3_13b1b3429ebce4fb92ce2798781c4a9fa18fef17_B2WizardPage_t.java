 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.b2eclipse.ui;
 
 import java.io.File;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.layout.PixelConverter;
 import org.eclipse.jface.resource.FontRegistry;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TreeEditor;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.sourcepit.b2eclipse.Activator;
 import org.sourcepit.b2eclipse.dnd.DragListener;
 import org.sourcepit.b2eclipse.dnd.DropListener;
 import org.sourcepit.b2eclipse.input.node.Node;
 import org.sourcepit.b2eclipse.input.node.NodeModule;
 import org.sourcepit.b2eclipse.input.node.NodeModuleProject;
 import org.sourcepit.b2eclipse.input.node.NodeProject;
 import org.sourcepit.b2eclipse.input.node.NodeWorkingSet;
 import org.sourcepit.b2eclipse.input.node.WSNameValidator;
 import org.sourcepit.b2eclipse.provider.LabelProviderForDir;
 import org.sourcepit.b2eclipse.provider.ContentProvider;
 import org.sourcepit.b2eclipse.provider.LabelProviderForPreview;
 
 /**
  * @author WD
  */
 public class B2WizardPage extends WizardPage
 {
    private Button dirRadioBtn;
    private Text dirTxt;
    private Button dirBtn;
 
    private Button workspaceRadioBtn;
    private Text workspaceTxt;
    private Button workspaceBtn;
 
    private CheckboxTreeViewer dirTreeViewer;
    private TreeViewer previewTreeViewer;
 
    private ToolItem refresh;
    // private ToolItem selAll;
    private ToolItem addPrefix;
    private ToolItem add;
    private ToolItem delete;
    private ToolItem expandAll;
 
    private ToolItem toggleMPselection;
    private ToolItem toggleName;
 
    private Backend bckend;
    private Shell dialogShell;
    private IStructuredSelection preSelect;
    private String currentDirectory;
    
    private WSNameValidator wsVal;
 
    // TODO work it! .. i need a glass of water ..
 
    protected B2WizardPage(String pageName, B2Wizard parent, IStructuredSelection selection)
    {
       super(pageName);
       setPageComplete(false);
       setTitle(Messages.msgImportHeader);
       setDescription(Messages.msgImportSuperscription);
       
       wsVal = new WSNameValidator();
       bckend = new Backend(wsVal);
       preSelect = selection;      
    }
 
    public void createControl(Composite parent)
    {
       initializeDialogUnits(parent);
 
       dialogShell = parent.getShell();
 
       Composite widgetContainer = new Composite(parent, SWT.NONE);
       widgetContainer.setLayout(new GridLayout());
 
       createFileChooserArea(widgetContainer);
       createViewArea(widgetContainer);
 
       addListeners();
 
       setControl(widgetContainer);
    }
 
    private void createFileChooserArea(Composite area)
    {
       Composite container = new Composite(area, SWT.NONE);
       container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
       container.setLayout(new GridLayout(3, false));
 
       dirRadioBtn = new Button(container, SWT.RADIO);
       dirRadioBtn.setText(Messages.msgSelectRootRbtn);
 
       dirTxt = new Text(container, SWT.BORDER);
       dirTxt.setToolTipText(Messages.msgSelectRootTt);
       dirTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
       dirBtn = new Button(container, SWT.PUSH);
       dirBtn.setText(Messages.msgBrowseBtn);
       setButtonLayoutData(dirBtn);
 
       workspaceRadioBtn = new Button(container, SWT.RADIO);
       workspaceRadioBtn.setText(Messages.msgSelectWorkspaceRbtn);
 
       workspaceTxt = new Text(container, SWT.BORDER);
       workspaceTxt.setToolTipText(Messages.msgSelectWorkspaceTt);
       workspaceTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
       workspaceBtn = new Button(container, SWT.PUSH);
       workspaceBtn.setText(Messages.msgBrowseBtn);
       setButtonLayoutData(workspaceBtn);
    }
 
    private void createViewArea(Composite area)
    {
       GridLayout layout;
 
       final Composite container = new Composite(area, SWT.NONE);
 
       GridData data = new GridData(GridData.FILL_BOTH);
       data.widthHint = new PixelConverter(new FontRegistry().defaultFont()).convertWidthInCharsToPixels(150);
       data.heightHint = new PixelConverter(new FontRegistry().defaultFont()).convertHeightInCharsToPixels(35);
       container.setLayoutData(data);
 
       layout = new GridLayout(2, true);
       layout.marginWidth = 0;
       container.setLayout(layout);
 
 
       // The CheckboxTreeViever on left side
       final Composite leftContainer = new Composite(container, SWT.BORDER);
       leftContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
 
       layout = new GridLayout(1, false);
       layout.marginHeight = 0;
       layout.marginWidth = 0;
       layout.verticalSpacing = 0;
       leftContainer.setLayout(layout);
 
       new Label(leftContainer, SWT.NONE).setText(Messages.msgLeftHeading);
       new Label(leftContainer, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 
       final ToolBar toolBarLeft = new ToolBar(leftContainer, (SWT.HORIZONTAL | SWT.NONE));
       toolBarLeft.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
       dirTreeViewer = new CheckboxTreeViewer(leftContainer, SWT.NONE);
       dirTreeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
 
       dirTreeViewer.setContentProvider(new ContentProvider());
       dirTreeViewer.setLabelProvider(new LabelProviderForDir(this.getShell()));
 
       refresh = new ToolItem(toolBarLeft, SWT.PUSH);
       refresh.setImage(Activator.getImageFromPath("org.eclipse.jdt.ui", "$nl$/icons/full/elcl16/refresh.gif"));
       refresh.setToolTipText(Messages.msgRestoreTt);
 
       toggleMPselection = new ToolItem(toolBarLeft, SWT.CHECK);
       toggleMPselection.setImage(Activator.getImageFromPath("org.eclipse.ui", "$nl$/icons/full/elcl16/step_done.gif"));
       toggleMPselection.setToolTipText(Messages.msgToggleMPselectionTt);
 
       // selAll = new ToolItem(toolBarLeft, SWT.CHECK);
       // selAll.setImage(Activator.getImageFromPath("org.eclipse.ui", "$nl$/icons/full/elcl16/step_done.gif"));
       // selAll.setToolTipText(Messages.msgSelectDeselectTt);
 
       addPrefix = new ToolItem(toolBarLeft, SWT.PUSH);
       addPrefix.setImage(Activator.getImageFromPath("org.eclipse.jdt.ui", "$nl$/icons/full/obj16/change.gif"));
       addPrefix.setToolTipText(Messages.msgAddPrefixTt);
       addPrefix.setEnabled(false);
 
 
       // The preview TreeViewer on right side
       final Composite rightContainer = new Composite(container, SWT.BORDER);
       rightContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
 
       layout = new GridLayout(1, false);
       layout.marginHeight = 0;
       layout.marginWidth = 0;
       layout.verticalSpacing = 0;
       rightContainer.setLayout(layout);
 
       new Label(rightContainer, SWT.NONE).setText(Messages.msgRightHeading);
       new Label(rightContainer, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
       final ToolBar toolBarRight = new ToolBar(rightContainer, (SWT.HORIZONTAL | SWT.NONE));
       toolBarRight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
       previewTreeViewer = new TreeViewer(rightContainer, SWT.NONE | SWT.MULTI);
       previewTreeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
 
       previewTreeViewer.setContentProvider(new ContentProvider());
       previewTreeViewer.setLabelProvider(new LabelProviderForPreview());
 
       Transfer[] transfer = new Transfer[] { FileTransfer.getInstance() };
       previewTreeViewer.addDragSupport(DND.DROP_MOVE, transfer, new DragListener(previewTreeViewer));
       previewTreeViewer.addDropSupport(DND.DROP_MOVE, transfer, new DropListener(previewTreeViewer));
 
       // init Node for preview
       previewTreeViewer.setInput(new Node());
 
       add = new ToolItem(toolBarRight, SWT.PUSH);
       add.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
       add.setToolTipText(Messages.msgAddNewWSTt);
 
       delete = new ToolItem(toolBarRight, SWT.PUSH);
       delete.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
       delete.setToolTipText(Messages.msgDelWSTt);
       delete.setEnabled(false);
 
       expandAll = new ToolItem(toolBarRight, SWT.PUSH);
       expandAll.setImage(Activator.getImageFromPath("org.eclipse.ui", "$nl$/icons/full/elcl16/expandall.gif"));
       expandAll.setToolTipText(Messages.msgExpandAllTt);
 
       toggleName = new ToolItem(toolBarRight, SWT.CHECK);
       toggleName.setImage(Activator.getImageFromPath("org.eclipse.ui", "$nl$/icons/full/elcl16/min_view.gif"));
       toggleName.setToolTipText(Messages.msgToggleNameTt);
 
       // ------------------------------------------------------------------------------
       // ToggleMode with Listeners
       final Menu menu = new Menu(dialogShell, SWT.POP_UP);
 
       final MenuItem itemOnlyModule = new MenuItem(menu, SWT.PUSH);
       itemOnlyModule.setText(Messages.msgItemOnlyModule);
       itemOnlyModule.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             bckend.setPreviewMode(Backend.Mode.onlyModule);
             bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer);
          }
       });
       final MenuItem itemModuleAndFolder = new MenuItem(menu, SWT.PUSH);
       itemModuleAndFolder.setText(Messages.msgItemModuleAndFolder);
       itemModuleAndFolder.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             bckend.setPreviewMode(Backend.Mode.moduleAndFolder);
             bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer);
          }
       });
       final MenuItem itemOnlyFolder = new MenuItem(menu, SWT.PUSH);
       itemOnlyFolder.setText(Messages.msgItemOnlyFolder);
       itemOnlyFolder.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             bckend.setPreviewMode(Backend.Mode.onlyFolder);
             bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer);
          }
       });
 
       final ToolItem toggleMode = new ToolItem(toolBarRight, SWT.DROP_DOWN);
       toggleMode.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             if (event.detail == SWT.ARROW)
             {
                Rectangle rect = toggleMode.getBounds();
                Point pt = new Point(rect.x, rect.y + rect.height);
                pt = toolBarRight.toDisplay(pt);
                menu.setLocation(pt.x, pt.y);
                menu.setVisible(true);
             }
          }
       });
 
       toggleMode.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_UP));
       toggleMode.setToolTipText(Messages.msgToggleModeTt);
       // ------------------------------------------------------------------------------
 
 
       // Comparator for the Viewers
       ViewerComparator compa = new ViewerComparator()
       {
          @SuppressWarnings("unchecked")
          public int compare(Viewer viewer, Object o1, Object o2)
          {
             Node n1 = ((Node) o1);
             Node n2 = ((Node) o2);
 
             // Module Projects always on top
             if (n1 instanceof NodeModuleProject)
                return -1;
             if (n2 instanceof NodeModuleProject)
                return 1;
 
             if (n1 instanceof NodeProject && n2 instanceof NodeProject)
             {
                return getComparator().compare(n1.getName(), n2.getName());
             }
             if (n1 instanceof NodeProject)
             {
                return o2.hashCode();
             }
             if (n2 instanceof NodeProject)
             {
                return o1.hashCode();
             }
             return getComparator().compare(n1.getName(), n2.getName());
          }
       };
       previewTreeViewer.setComparator(compa);
       dirTreeViewer.setComparator(compa);
 
       preSelect();
    }
 
    /**
     * Will select an Directory and put it in the correct Widget, if any directory was given.
     */
    private void preSelect()
    {
       if (!preSelect.isEmpty())
       {
          currentDirectory = "";
          if (preSelect.getFirstElement() instanceof IResource)
          {
             dirRadioBtn.setSelection(false);
             dirTxt.setEnabled(false);
             dirBtn.setEnabled(false);
             workspaceRadioBtn.setSelection(true);
             workspaceTxt.setEnabled(true);
             workspaceBtn.setEnabled(true);
             currentDirectory = ((IResource) preSelect.getFirstElement()).getLocation().toString();
             workspaceTxt.setText(currentDirectory);
          }
          if (preSelect.getFirstElement() instanceof File)
          {
             dirRadioBtn.setSelection(true);
             dirTxt.setEnabled(true);
             dirBtn.setEnabled(true);
             workspaceRadioBtn.setSelection(false);
             workspaceTxt.setEnabled(false);
             workspaceBtn.setEnabled(false);
             currentDirectory = ((File) preSelect.getFirstElement()).getPath();
             dirTxt.setText(currentDirectory);
          }
 
          if (bckend.testOnLocalDrive(currentDirectory))
          {
             bckend.handleDirTreeViewer(dirTreeViewer, currentDirectory);
             bckend.doCheck(dirTreeViewer, null, true);
             bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer);
 
             // selAll.setSelection(true);
             setPageComplete(true);
          }
          else
          {
             dirRadioBtn.setSelection(true);
             workspaceTxt.setEnabled(false);
             workspaceBtn.setEnabled(false);
          }
       }
       else
       {
          dirRadioBtn.setSelection(true);
          workspaceTxt.setEnabled(false);
          workspaceBtn.setEnabled(false);
       }
    }
 
    private void addListeners()
    {
       dirBtn.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             workspaceTxt.setText("");
             dirTxt.setText(bckend.showDirectorySelectDialog(dirTxt.getText(), dialogShell));
 
          }
       });
 
       workspaceBtn.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             dirTxt.setText("");
             workspaceTxt.setText(bckend.showWorkspaceSelectDialog(dialogShell));
 
          }
       });
 
       dirRadioBtn.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             if (dirRadioBtn.isEnabled())
             {
                dirTxt.setEnabled(true);
                dirBtn.setEnabled(true);
                workspaceTxt.setEnabled(false);
                workspaceBtn.setEnabled(false);
             }
          }
       });
 
       workspaceRadioBtn.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             if (workspaceRadioBtn.isEnabled())
             {
                workspaceTxt.setEnabled(true);
                workspaceBtn.setEnabled(true);
                dirTxt.setEnabled(false);
                dirBtn.setEnabled(false);
             }
          }
       });
 
       // Listener for the Texts
       ModifyListener modLis = new ModifyListener()
       {
          public void modifyText(ModifyEvent e)
          {
             setPageComplete(false);
             currentDirectory = ((Text) e.widget).getText();
 
             if (bckend.testOnLocalDrive(currentDirectory))
             {
               wsVal.clear();
               previewTreeViewer.setInput(new Node());               
                bckend.handleDirTreeViewer(dirTreeViewer, currentDirectory);
                bckend.doCheck(dirTreeViewer, null, true);
                bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer);
 
                // selAll.setSelection(true);
 
                setPageComplete(true);
             }
          }
       };
       dirTxt.addModifyListener(modLis);
       workspaceTxt.addModifyListener(modLis);
 
       refresh.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             setPageComplete(false);
 
             // this is doing the same stuff as modLis
             previewTreeViewer.setInput(new Node());
             bckend.handleDirTreeViewer(dirTreeViewer, currentDirectory);
             bckend.doCheck(dirTreeViewer, null, true);
             bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer);
 
             // selAll.setSelection(true);
 
             setPageComplete(true);
          }
       });
 
       toggleName.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             setPageComplete(false);
             if (toggleName.getSelection())
             {
                // check All
                bckend.toggleNaming(previewTreeViewer, true);
             }
             else
             {
                // un-check All
                bckend.toggleNaming(previewTreeViewer, false);
             }
             // dirTreeViewer.refresh();
             // bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer);
             previewTreeViewer.refresh();
             setPageComplete(true);
          }
       });
 
       toggleMPselection.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             setPageComplete(false);
             if (toggleMPselection.getSelection())
             {
                // check All
                bckend.doCheck(dirTreeViewer, true, false);
             }
             else
             {
                // un-check All
                bckend.doCheck(dirTreeViewer, true, true);
             }
             dirTreeViewer.refresh();
             bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer, true);
 
             setPageComplete(true);
 
          }
       });
 
       // selAll.addListener(SWT.Selection, new Listener()
       // {
       // public void handleEvent(Event event)
       // {
       // setPageComplete(false);
       // if (selAll.getSelection())
       // {
       // // check All
       // bckend.doCheck(dirTreeViewer, true);
       // }
       // else
       // {
       // // un-check All
       // bckend.doCheck(dirTreeViewer, false);
       // }
       // dirTreeViewer.refresh();
       // bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer);
       // setPageComplete(true);
       // }
       // });
 
       addPrefix.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             handleDirPrefixNaming(false);
          }
       });
 
 
       // adds a new working set
       add.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             new NodeWorkingSet((Node) previewTreeViewer.getInput(),
                wsVal.validate(Messages.msgDefaultWSName), null);
             previewTreeViewer.refresh();
          }
       });
 
       // deletes the selected working set(s)
       delete.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             doDeleteNodeInPreview();
          }
       });
 
       expandAll.addListener(SWT.Selection, new Listener()
       {
          public void handleEvent(Event event)
          {
             previewTreeViewer.expandAll();
          }
       });
 
       dirTreeViewer.addSelectionChangedListener(new ISelectionChangedListener()
       {
          public void selectionChanged(SelectionChangedEvent event)
          {
             Node selected = (Node) ((IStructuredSelection) event.getSelection()).getFirstElement();
             if (selected != null && selected instanceof NodeModule)
                addPrefix.setEnabled(true);
             else
                addPrefix.setEnabled(false);
          }
       });
 
       // After doubleClick on a element, user can change the Prefix
       dirTreeViewer.addDoubleClickListener(new IDoubleClickListener()
       {
          public void doubleClick(DoubleClickEvent event)
          {
             handleDirPrefixNaming(false);
          }
       });
 
       previewTreeViewer.addSelectionChangedListener(new ISelectionChangedListener()
       {
          public void selectionChanged(SelectionChangedEvent event)
          {
             @SuppressWarnings("unchecked")
             List<Node> selected = ((IStructuredSelection) event.getSelection()).toList();
             for (Node iter : selected)
             {
                if (iter instanceof Node)
                   delete.setEnabled(true);
                else
                   delete.setEnabled(false);
             }
          }
       });
 
 
       // user can change the name of a WorkingSet after double click on it
       previewTreeViewer.addDoubleClickListener(new IDoubleClickListener()
       {
          public void doubleClick(DoubleClickEvent event)
          {
             handlePrevievRename();
          }
       });
 
       // if a category is checked in the tree, check all its children
       // handles also the appear/disappear of elements in the preview TreeViewer
       dirTreeViewer.addCheckStateListener(new ICheckStateListener()
       {
          public void checkStateChanged(CheckStateChangedEvent event)
          {
             setPageComplete(false);
             Node eventNode = (Node) event.getElement();
 
             if (eventNode.hasConflict())
             {
                // This makes sure the eventNode can't be checked
                dirTreeViewer.setChecked(eventNode, false);
             }
             else
             {
                if (event.getChecked())
                {
                   bckend.doCheck(dirTreeViewer, eventNode, true);
                   bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer);
                }
                else
                {
                   bckend.doCheck(dirTreeViewer, eventNode, false);
 
                   for (Node iter : eventNode.getAllSubNodes())
                   {
                      bckend.deleteFromPrevievTree(previewTreeViewer, iter);
                   }
                   bckend.deleteFromPrevievTree(previewTreeViewer, eventNode);
                   // selAll.setSelection(false);
                }
             }
             setPageComplete(true);
          }
       });
 
       // Global key listener
       this.getShell().getDisplay().addFilter(SWT.KeyDown, new Listener()
       {
          public void handleEvent(Event event)
          {
             if (event.keyCode == SWT.F2)
             {
                // DirTreeviewer was selected
                if (event.widget.equals(dirTreeViewer.getTree()))
                {
                   handleDirPrefixNaming(false);
                }
 
                // PreviewTreeViewer was selected
                if (event.widget.equals(previewTreeViewer.getTree()))
                {
                   handlePrevievRename();
                }
             }
 
             if (event.keyCode == SWT.DEL)
             {
                if (event.widget.equals(dirTreeViewer.getTree()))
                {
                   handleDirPrefixNaming(true);
                }
 
                if (event.widget.equals(previewTreeViewer.getTree()))
                {
                   doDeleteNodeInPreview();
                }
             }
          }
       });
    }
 
    /**
     * @return the root Node of the Preview Viewer
     */
    public Node getPreviewRootNode()
    {
       return (Node) previewTreeViewer.getInput();
    }
 
    /**
     * Brings up a dialog where user can type in a prefix for a module.
     * 
     * @param delete if true the prefix will be deleted
     */
    private void handleDirPrefixNaming(Boolean delete)
    {
       setPageComplete(false);
 
       Node selected = (Node) ((IStructuredSelection) dirTreeViewer.getSelection()).getFirstElement();
 
       if (selected instanceof NodeModule && selected != null)
       {
          if (delete)
          {
             ((NodeModule) selected).setPrefix(null);
          }
          else
          {
             ((NodeModule) selected).setPrefix(bckend.showInputDialog(dialogShell));
          }
          previewTreeViewer.setInput(new Node());
          bckend.refreshPreviewViewer(dirTreeViewer, previewTreeViewer);
       }
       dirTreeViewer.refresh();
       setPageComplete(true);
    }
 
    /**
     * Do the renaming "thing" on PreviewViewer.
     */
    private void handlePrevievRename()
    {
       setPageComplete(false);
       final Node node = (Node) ((IStructuredSelection) previewTreeViewer.getSelection()).getFirstElement();
 
       // Only for Working Sets
       if (node instanceof NodeWorkingSet && node != null)
       {
          final TreeEditor editor = new TreeEditor(previewTreeViewer.getTree());
          editor.horizontalAlignment = SWT.LEFT;
          editor.grabHorizontal = true;
 
          final TreeItem item = previewTreeViewer.getTree().getSelection()[0];
          final Text txt = new Text(previewTreeViewer.getTree(), SWT.NONE);
          wsVal.removeFromlist(((NodeWorkingSet) node).getLongName());
          txt.setText(node.getName());
          txt.selectAll();
          txt.setFocus();
 
          txt.addFocusListener(new FocusListener()
          {
             public void focusLost(FocusEvent e)
             {
                node.setName(wsVal.validate(txt.getText()));
                txt.dispose();
                previewTreeViewer.refresh();
                setPageComplete(true);
             }
 
             public void focusGained(FocusEvent e)
             {
                /* no use */
             }
          });
 
          txt.addKeyListener(new KeyListener()
          {
             public void keyPressed(KeyEvent e)
             {
                switch (e.keyCode)
                {
                   case SWT.CR :
                      node.setName(wsVal.validate(txt.getText()));
                   case SWT.ESC :
                      txt.dispose();
                      previewTreeViewer.refresh();
                      setPageComplete(true);
                      break;
                }
             }
 
             public void keyReleased(KeyEvent e)
             {
                /* no use */
             }
 
          });
          editor.setEditor(txt, item);
       }
    }
 
    private void doDeleteNodeInPreview()
    {
       setPageComplete(false);
       IStructuredSelection selection = (IStructuredSelection) previewTreeViewer.getSelection();
 
       if (!selection.isEmpty())
       {
          // Get the last item in selection
          Node last = (Node) selection.toArray()[selection.size() - 1];
          int index = last.getParent().getChildren().indexOf(last);
          ListIterator<Node> list = last.getParent().getChildren().listIterator(index + 1);
 
          // Select the next item in tree
          if (list.hasNext())
             previewTreeViewer.setSelection(new StructuredSelection(list.next()));
          else
          // Select the parent of the item if there are no more items left
          if (last.getParent().getParent() != null)
             previewTreeViewer.setSelection(new StructuredSelection(last.getParent()));
 
          // Go thought all items of the selection
          for (Object iter : (Object[]) selection.toArray())
          {
             // Removes the projects from Preview
             if (iter instanceof NodeProject || iter instanceof NodeModuleProject)
             {
                Node represent = ((Node) dirTreeViewer.getInput()).getEqualNode(((Node) iter).getFile());
 
                if (represent != null)
                   dirTreeViewer.setChecked(represent, false);
 
                bckend.deleteFromPrevievTree(previewTreeViewer, (Node) iter);
             }
 
             // Removes the WS from Preview
             if (iter instanceof NodeWorkingSet)
             {
                wsVal.removeFromlist(((NodeWorkingSet) iter).getLongName());
                ((Node) iter).deleteNodeAssigningChildrenToParent();
             }
          }
       }
       previewTreeViewer.refresh();
       setPageComplete(true);
    }
 }
