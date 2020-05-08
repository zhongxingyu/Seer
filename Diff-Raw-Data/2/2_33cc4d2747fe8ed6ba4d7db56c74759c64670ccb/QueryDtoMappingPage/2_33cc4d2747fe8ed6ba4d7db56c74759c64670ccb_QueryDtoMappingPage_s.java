 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dolteng.eclipse.wizard;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.seasar.dolteng.core.dao.impl.BasicDatabaseMetadataDao;
 import org.seasar.dolteng.core.entity.ColumnMetaData;
 import org.seasar.dolteng.core.entity.FieldMetaData;
 import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
 import org.seasar.dolteng.core.types.TypeMapping;
 import org.seasar.dolteng.core.types.TypeMappingRegistry;
 import org.seasar.dolteng.eclipse.Constants;
 import org.seasar.dolteng.eclipse.DoltengCore;
 import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
 import org.seasar.dolteng.eclipse.model.EntityMappingRow;
 import org.seasar.dolteng.eclipse.model.impl.BasicEntityMappingRow;
 import org.seasar.dolteng.eclipse.model.impl.ColumnNameColumn;
 import org.seasar.dolteng.eclipse.model.impl.FieldNameColumn;
 import org.seasar.dolteng.eclipse.model.impl.IsGenerateColumn;
 import org.seasar.dolteng.eclipse.model.impl.JavaClassColumn;
 import org.seasar.dolteng.eclipse.model.impl.ModifierColumn;
 import org.seasar.dolteng.eclipse.model.impl.SqlTypeColumn;
 import org.seasar.dolteng.eclipse.nls.Labels;
 import org.seasar.dolteng.eclipse.nls.Messages;
 import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
 import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
 import org.seasar.dolteng.eclipse.util.NameConverter;
 import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
 import org.seasar.dolteng.eclipse.util.ProjectUtil;
 import org.seasar.dolteng.eclipse.util.ResourcesUtil;
 import org.seasar.dolteng.eclipse.viewer.ComparableViewerSorter;
 import org.seasar.dolteng.eclipse.viewer.TableProvider;
 import org.seasar.dolteng.eclipse.wigets.ModifierGroup;
 import org.seasar.dolteng.eclipse.wigets.ResourceTreeSelectionDialog;
 import org.seasar.framework.util.InputStreamUtil;
 import org.seasar.framework.util.ReaderUtil;
 import org.seasar.framework.util.StringUtil;
 
 /**
  * @author taichi
  * 
  */
 public class QueryDtoMappingPage extends WizardPage implements
         ModifierGroup.ModifierSelectionListener {
 
     public static final String NAME = QueryDtoMappingPage.class.getName();
 
     private IResource selected;
 
     private Text twoWaySqlPath;
 
     private TableViewer viewer;
 
     private List<EntityMappingRow> mappingRows;
 
     private ConnectionConfig config;
 
     private boolean canSelectPublicField = false;
 
     private boolean usePublicField = false;
 
     private DoltengPreferences pref;
 
     public QueryDtoMappingPage() {
         super(Labels.WIZARD_PAGE_DTO_FIELD_SELECTION);
         setTitle(Labels.WIZARD_PAGE_DTO_FIELD_SELECTION);
         setDescription(Labels.WIZARD_ENTITY_CREATION_DESCRIPTION);
         this.mappingRows = new ArrayList<EntityMappingRow>();
     }
 
     public void init(IStructuredSelection selection) {
         Object o = selection.getFirstElement();
         selected = ResourcesUtil.toResource(o);
 
         pref = DoltengCore.getPreferences(selected.getProject());
         this.canSelectPublicField = Constants.DAO_TYPE_KUINADAO.equals(pref
                 .getDaoType()) == false;
         if (pref != null && this.canSelectPublicField) {
             this.usePublicField = pref.isUsePublicField();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
      */
     public void createControl(Composite parent) {
         initializeDialogUnits(parent);
 
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.numColumns = 4;
         composite.setLayout(layout);
 
         Label label = new Label(composite, SWT.LEFT);
         label.setText(Labels.WIZARD_SQL_FILE);
         twoWaySqlPath = new Text(composite, SWT.BORDER | SWT.SINGLE);
         GridData gd = new GridData(GridData.FILL_HORIZONTAL);
         twoWaySqlPath.setLayoutData(gd);
         twoWaySqlPath.setEnabled(false);
         Button browse = new Button(composite, SWT.PUSH);
         browse.setText(Labels.BROWSE);
         browse.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 chooseSQL();
                 refreshRows();
             }
         });
         if (selected instanceof IFile) {
             IFile f = (IFile) selected;
             if ("sql".equalsIgnoreCase(f.getFileExtension())) {
                 twoWaySqlPath.setText(f.getFullPath().toString());
             }
         }
         Button refresh = new Button(composite, SWT.PUSH);
         refresh.setText(Labels.REFRESH);
         refresh.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent event) {
                 refreshRows();
             }
         });
 
         if (this.canSelectPublicField) {
             createPartOfPublicField(composite);
         }
 
         this.viewer = new TableViewer(composite, SWT.BORDER
                 | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
         Table table = viewer.getTable();
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
 
         viewer.setContentProvider(new ArrayContentProvider());
         viewer.setLabelProvider(new TableProvider(viewer,
                 createColumnDescs(table)));
         viewer.setSorter(new ComparableViewerSorter());
         viewer.setInput(this.mappingRows);
         viewer.addSelectionChangedListener(new ISelectionChangedListener() {
             public void selectionChanged(SelectionChangedEvent event) {
                 validateDuplicateJavaFieldNames();
             }
         });
         table.addFocusListener(new FocusListener() {
             public void focusGained(FocusEvent e) {
                 validateDuplicateJavaFieldNames();
             }
             public void focusLost(FocusEvent e) {
                 validateDuplicateJavaFieldNames();
             }
         });
 
         gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                 | GridData.GRAB_VERTICAL);
         gd.heightHint = 180;
         gd.horizontalSpan = 4;
         table.setLayoutData(gd);
 
         Label spacer = new Label(composite, SWT.NONE);
         gd = new GridData();
         gd.horizontalAlignment = GridData.FILL;
         gd.verticalAlignment = GridData.BEGINNING;
         gd.heightHint = 4;
         gd.horizontalSpan = 4;
         spacer.setLayoutData(gd);
 
         setControl(composite);
     }
 
     private void validateDuplicateJavaFieldNames() {
         for (EntityMappingRow currentRow : mappingRows) {
             if (!currentRow.isGenerate()) {
                 continue;
             }
             for (EntityMappingRow otherRow : mappingRows) {
                 if (!otherRow.isGenerate()) {
                     continue;
                 }
                 if (otherRow == currentRow) {
                     continue;
                 }
                 String s1 = currentRow.getJavaFieldName();
                 String s2 = otherRow.getJavaFieldName();
                 if (StringUtil.equals(s1, s2)) {
                    setErrorMessage("フィールド名が重複しています。");
                     setPageComplete(false);
                     return;
                 }
             }
         }
         setErrorMessage(null);
         setPageComplete(true);
     }
 
     private void createPartOfPublicField(Composite composite) {
         ModifierGroup mc = new ModifierGroup(composite, SWT.NONE,
                 usePublicField);
         GridData gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.horizontalSpan = 4;
         mc.setLayoutData(gd);
         mc.add(this);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.wigets.ModifierComposite.ModifierSelectionListener#privateSelected()
      */
     public void privateSelected() {
         usePublicField = false;
         setConfigUsePublicField(usePublicField);
         // TODO : Accessor Modifierの列をenable若しくはvisible
     }
 
     public boolean getUsePublicField() {
         return usePublicField;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.wigets.ModifierComposite.ModifierSelectionListener#publicSelected()
      */
     public void publicSelected() {
         usePublicField = true;
         setConfigUsePublicField(usePublicField);
         // TODO : Accessor Modifierの列をdisable若しくはinvisible
     }
 
     protected void setConfigUsePublicField(boolean use) {
         if (pref != null) {
             pref.setUsePublicField(use);
         }
     }
 
     private void chooseSQL() {
         ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                 getShell(), ProjectUtil.getWorkspaceRoot(), IResource.FOLDER
                         | IResource.PROJECT | IResource.FILE);
         dialog.addFilter(new ViewerFilter() {
             @Override
             public boolean select(Viewer viewer, Object parentElement,
                     Object element) {
                 if (element instanceof IFile) {
                     IFile f = (IFile) element;
                     return "sql".equalsIgnoreCase(f.getFileExtension());
                 }
                 return true;
             }
         });
         dialog.setInitialSelection(selected);
         dialog.setAllowMultiple(false);
 
         if (dialog.open() == Dialog.OK) {
             Object[] results = dialog.getResult();
             if (results != null && 0 < results.length
                     && results[0] instanceof IFile) {
                 IFile f = (IFile) results[0];
                 this.twoWaySqlPath.setText(f.getFullPath().toString());
             }
         }
     }
 
     private void refreshRows() {
         try {
             getWizard().getContainer().run(false, false,
                     new IRunnableWithProgress() {
                         public void run(IProgressMonitor monitor)
                                 throws InvocationTargetException,
                                 InterruptedException {
                             monitor = ProgressMonitorUtil.care(monitor);
                             try {
                                 if (selected != null && selected.exists()) {
                                     monitor.beginTask(Messages.bind(
                                             Messages.PROCESS_MAPPING, selected
                                                     .getName()), 2);
                                     mappingRows.clear();
                                     createRows();
                                     ProgressMonitorUtil.isCanceled(monitor, 1);
                                     viewer.refresh();
                                     ProgressMonitorUtil.isCanceled(monitor, 1);
                                 }
                             } finally {
                                 monitor.done();
                             }
                         }
                     });
         } catch (Exception e) {
             DoltengCore.log(e);
         }
     }
 
     @SuppressWarnings("unchecked")
     private ColumnDescriptor[] createColumnDescs(Table table) {
         List descs = new ArrayList();
         descs.add(new IsGenerateColumn(table));
         descs.add(new SqlTypeColumn(table));
         descs.add(new ColumnNameColumn(table));
         descs.add(new ModifierColumn(table));
         descs.add(new JavaClassColumn(table, toItems()));
         descs.add(new FieldNameColumn(table));
         return (ColumnDescriptor[]) descs.toArray(new ColumnDescriptor[descs
                 .size()]);
     }
 
     private String[] toItems() {
         List<String> l = new ArrayList<String>();
         IProject project = ProjectUtil.getProject(this.selected);
         TypeMappingRegistry registry = DoltengCore
                 .getTypeMappingRegistry(project);
         TypeMapping[] types = registry.findAllTypes();
         for (TypeMapping type : types) {
             l.add(type.getJavaClassName());
         }
         return l.toArray(new String[l.size()]);
     }
 
     /**
      * @return
      */
     public void createRows() {
         if (config == null) {
             return;
         }
         IProject project = ProjectUtil.getProject(this.selected);
         TypeMappingRegistry registry = DoltengCore
                 .getTypeMappingRegistry(project);
         BasicDatabaseMetadataDao dao = new BasicDatabaseMetadataDao();
         dao.setDataSource(config);
         IFile file = getSqlFile();
         if (file == null) {
             return;
         }
         String sql = getSql(file);
         if (StringUtil.isEmpty(sql) == false) {
             ColumnMetaData[] metas = dao.getColumns(sql);
             for (ColumnMetaData meta : metas) {
                 FieldMetaData field = new BasicFieldMetaData();
                 setUpFieldMetaData(registry, meta, field);
                 EntityMappingRow row = new BasicEntityMappingRow(meta, field,
                         registry);
                 row.setGenerate(true);
                 this.mappingRows.add(row);
             }
             Collections.sort(this.mappingRows);
         }
     }
 
     private IFile getSqlFile() {
         String s = this.twoWaySqlPath.getText();
         if (StringUtil.isEmpty(s) == false) {
             IPath p = new Path(s);
             IWorkspaceRoot root = ProjectUtil.getWorkspaceRoot();
             IFile file = root.getFile(p);
             if (file != null && file.exists()
                     && "sql".equalsIgnoreCase(file.getFileExtension())) {
                 return file;
             }
         }
         return null;
     }
 
     private String getSql(IFile twoWaySql) {
         String result = "";
         InputStream in = null;
         try {
             in = twoWaySql.getContents();
             result = ReaderUtil.readText(new InputStreamReader(in));
         } catch (Exception e) {
             DoltengCore.log(e);
         } finally {
             InputStreamUtil.close(in);
         }
         return result;
     }
 
     private void setUpFieldMetaData(TypeMappingRegistry registry,
             ColumnMetaData meta, FieldMetaData field) {
         TypeMapping mapping = registry.toJavaClass(meta);
         field.setModifiers(Modifier.PUBLIC);
         field.setDeclaringClassName(mapping.getJavaClassName());
         field.setName(StringUtil.decapitalize(NameConverter.toCamelCase(meta
                 .getName())));
     }
 
     public List<EntityMappingRow> getMappingRows() {
         return this.mappingRows;
     }
 
     public void setConfig(ConnectionConfig config) {
         this.config = config;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
      */
     @Override
     public void setVisible(boolean visible) {
         if (visible) {
             refreshRows();
         }
 
         /*
          * ここで初期表示時のためのフィールド名重複チェックを行う。
          * （createControl内で呼んでもうまくいかなかったため）
          * もっと良い方法があれば変える。
          */
         validateDuplicateJavaFieldNames();
 
         super.setVisible(visible);
     }
 }
