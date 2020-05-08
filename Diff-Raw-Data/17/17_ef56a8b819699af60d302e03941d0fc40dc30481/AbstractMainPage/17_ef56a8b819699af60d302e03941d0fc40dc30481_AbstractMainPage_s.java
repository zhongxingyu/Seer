 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.loader.ui.wizards;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.amanzi.neo.loader.core.parser.IConfigurationData;
 import org.amanzi.neo.loader.ui.utils.FileSelection;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.neo4j.graphdb.Node;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 /**
  * <p>
  * Abstract class for common main loader page - with selection of root node, crs,select necessary
  * file/dir and select loader
  * </p>
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public abstract class AbstractMainPage<T extends IConfigurationData> extends LoaderPage<T> {
 
     protected final boolean isCRSpresent;
     protected Node rootNode;
     protected HashMap<String, Node> rootList = new HashMap<String, Node>();
     protected Set<String> restrictedNames = new HashSet<String>();
     protected Group main;
     protected Combo root;
     protected FileSelection viewer;
     protected Button selectCRS;
     protected Combo loaders;
     protected String rootName;
 
     /**
      * @param pageName
      */
     protected AbstractMainPage(String pageName, boolean isCRSpresent) {
         super(pageName);
         this.isCRSpresent = isCRSpresent;
     }
 
     @Override
     public void createControl(Composite parent) {
         main = new Group(parent, SWT.NULL);
         main.setLayout(new GridLayout(3, false));
         Label label = new Label(main, SWT.LEFT);
         label.setText(getRootLabel());
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
         root = new Combo(main, SWT.DROP_DOWN);
         GridData rootLayoutData = new GridData(SWT.FILL, SWT.CENTER, false, false, isCRSpresent ? 1 : 2, 1);
         root.setLayoutData(rootLayoutData);
         root.setItems(getRootItems());
         root.addModifyListener(new ModifyListener() {
 
             @Override
             public void modifyText(ModifyEvent e) {
                 changeRootName();
             }
         });
         root.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeRootName();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         if (isCRSpresent) {
             selectCRS = new Button(main, SWT.FILL | SWT.PUSH);
             selectCRS.setAlignment(SWT.LEFT);
             GridData selCrsData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
             selCrsData.widthHint = 150;
             selectCRS.setLayoutData(selCrsData);
             selectCRS.addSelectionListener(new SelectionListener() {
 
                 @Override
                 public void widgetSelected(SelectionEvent e) {
                     selectCRS();
                 }
 
                 @Override
                 public void widgetDefaultSelected(SelectionEvent e) {
                     widgetSelected(e);
                 }
             });
         }
         viewer = new FileSelection();
         viewer.createPartControl(main);
         GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
         viewer.getTreeViewer().getTree().setLayoutData(gridData);
         viewer.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
 
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
                 fileSelectionChanged(event);
             }
         });
         label = new Label(main, SWT.LEFT);
         label.setText(getLoaderLabel());
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
         loaders = new Combo(main, SWT.DROP_DOWN | SWT.READ_ONLY);
         loaders.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
         loaders.setItems(getLoadersDescriptions());
         loaders.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 selectLoader(loaders.getSelectionIndex());
                 update();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         addAdditionalComponents(parent, main);
         new Label(main, SWT.NONE);
        final Button batchMode = new Button(main, SWT.CHECK);
        batchMode.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setAccessType(batchMode.getSelection());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        batchMode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        batchMode.setText("batch mode");

         setControl(main);
         update();
     }
 
     /**
      * File selection changed.
      * 
      * @param event the event
      */
     protected void fileSelectionChanged(SelectionChangedEvent event) {
         update();
     }
 
     /**
      * Adds the additional components.
      * 
      * @param parent the parent
      * @param main the main
      */
     protected void addAdditionalComponents(Composite parent, Group main) {
     }
 
     /**
      * Gets the root label.
      * 
      * @return the root label
      */
     protected abstract String getRootLabel();
 
     /**
      * Gets the loader label.
      * 
      * @return the loader label
      */
     protected abstract String getLoaderLabel();
 
     @Override
     protected void update() {
         if (isCRSpresent) {
             CoordinateReferenceSystem crs = getSelectedCRS();
             selectCRS.setText(String.format("CRS: %s", crs.getName().toString()));
         }
         super.update();
     }
 
     /**
      * Change root name.
      */
     protected void changeRootName() {
         rootName = root.getText();
         rootNode = rootList.get(rootName);
         if (isCRSpresent) {
             if (rootNode!=null){
                 Node gis = NeoServiceFactory.getInstance().getDatasetService().findGisNode(rootNode);
                 if (gis!=null){
                     CoordinateReferenceSystem crs = NeoUtils.getCRS(gis, null);
                     if (crs!=null){
                         selectCRS.setEnabled(false);
                         setSelectedCRS(crs);
                     }else{
                         selectCRS.setEnabled(true);
                     }
                 }else{
                     selectCRS.setEnabled(true);
                 }
             }else{
                 selectCRS.setEnabled(true);
             }
         }
         update();
     }
 
     /**
      * Gets the root items.
      * 
      * @return the root items
      */
     // in this methods should be defined fields rootList and restrictedNames
     protected abstract String[] getRootItems();
 
 }
