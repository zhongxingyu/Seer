 package com.managehelper.ui;
 
 
 import com.managehelper.model.ApplicationContext;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Label;
 
 import static com.managehelper.ui.UiUtils.addEditableListeners;
 
 
 public class FinalFrame  {
 
 
     protected Shell shell;
     private Table coefRate;
 
     /**
      * Create contents of the shell.
      */
     protected void createContents(final ApplicationContext context) {
 
         ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
         scrolledComposite.setBounds(10, 10, 564, 289);
         scrolledComposite.setExpandHorizontal(true);
         scrolledComposite.setExpandVertical(true);
 
         Table table = new Table(scrolledComposite, SWT.BORDER | SWT.FULL_SELECTION);
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
 
         createColumns(table);
         createGroupsLine(table, context.getNumOfGroups(), context);
 
 
         scrolledComposite.setContent(table);
         scrolledComposite.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 
         createCoefficientRateTable();
 
         Label lblNewLabel = new Label(shell, SWT.NONE);
         lblNewLabel.setBounds(10, 317, 105, 15);
         lblNewLabel.setText("\u0412\u0435\u0441\u0430");
 
         Button btnEvalute = new Button(shell, SWT.NONE);
         btnEvalute.setBounds(499, 370, 75, 25);
         btnEvalute.setText("Вычислить");
 
         btnEvalute.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent selectionEvent) {
                 final TableItem item = coefRate.getItem(0);
                 for(int i = 0; i < coefRate.getColumnCount(); i ++){
                     context.getWeights()[i] = Double.valueOf(item.getText(i).trim());
                 }
 
             }
         });
 
         createBestGroupTable();
 
     }
 
     private void createBestGroupTable() {
         Table bestGroupRatesTbl = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
         bestGroupRatesTbl.setLinesVisible(true);
         bestGroupRatesTbl.setHeaderVisible(true);
         bestGroupRatesTbl.setBounds(121, 360, 253, 49);
 
         TableColumn tableColumn = new TableColumn(bestGroupRatesTbl, SWT.NONE);
         tableColumn.setWidth(50);
         tableColumn.setText("А");
 
         TableColumn tableColumn_1 = new TableColumn(bestGroupRatesTbl, SWT.NONE);
         tableColumn_1.setWidth(50);
         tableColumn_1.setText("Ms+");
 
         TableColumn tableColumn_2 = new TableColumn(bestGroupRatesTbl, SWT.NONE);
         tableColumn_2.setWidth(50);
         tableColumn_2.setText("Ms-");
 
         TableColumn tableColumn_3 = new TableColumn(bestGroupRatesTbl, SWT.NONE);
         tableColumn_3.setWidth(52);
         tableColumn_3.setText("Q");
 
         TableColumn tableColumn_4 = new TableColumn(bestGroupRatesTbl, SWT.NONE);
         tableColumn_4.setWidth(46);
         tableColumn_4.setText("P");
 
         Label label = new Label(shell, SWT.NONE);
         label.setText("\u041B\u0443\u0447\u0448\u0430\u044F \u0433\u0440\u0443\u043F\u043F\u0430");
         label.setBounds(10, 380, 105, 15);
     }
 
     private void createCoefficientRateTable() {
 
         coefRate = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
         coefRate.setBounds(121, 305, 253, 50);
         coefRate.setHeaderVisible(true);
         coefRate.setLinesVisible(true);
 
         TableColumn tableColumn = new TableColumn(coefRate, SWT.NONE);
         tableColumn.setWidth(50);
         tableColumn.setText("А");
 
         TableColumn tableColumn_1 = new TableColumn(coefRate, SWT.NONE);
         tableColumn_1.setWidth(50);
         tableColumn_1.setText("Ms+");
 
         TableColumn tableColumn_2 = new TableColumn(coefRate, SWT.NONE);
         tableColumn_2.setWidth(50);
         tableColumn_2.setText("Ms-");
 
         TableColumn tableColumn_3 = new TableColumn(coefRate, SWT.NONE);
         tableColumn_3.setWidth(52);
         tableColumn_3.setText("Q");
 
         TableColumn tableColumn_4 = new TableColumn(coefRate, SWT.NONE);
         tableColumn_4.setWidth(46);
         tableColumn_4.setText("P");
 
         TableItem tableItem = new TableItem(coefRate, SWT.NONE);
         tableItem.setText("");
 
         addEditableListeners(coefRate);
     }
 
     private void createGroupsLine(Table table, int count, ApplicationContext context) {
 
         for(int i = 0; i < count; i++) {
             TableItem tableItem = new TableItem(table, SWT.NONE);
             tableItem.setText("" + (i + 1));
             tableItem.setText(1, "" + context.getTeams().get(i).getIndex());
             tableItem.setText(2, "" + context.getTeams().get(i).getMedianaPlus());
            tableItem.setText(4, "" + context.getTeams().get(i).getMedianaMinus());
             tableItem.setText(4, "" + context.getTeams().get(i).getGroupUnity());
         }
     }
 
     private void createColumns(Table table) {
         TableColumn columnDefault = new TableColumn(table, SWT.NONE);
         columnDefault.setWidth(100);
         columnDefault.setText("");
 
         TableColumn columnA = new TableColumn(table, SWT.NONE);
         columnA.setWidth(100);
         columnA.setText("А");
 
         TableColumn msPlusColumn = new TableColumn(table, SWT.NONE);
         msPlusColumn.setWidth(100);
         msPlusColumn.setText("Ms+");
 
         TableColumn msMinusColumn = new TableColumn(table, SWT.NONE);
         msMinusColumn.setWidth(100);
         msMinusColumn.setText("Ms-");
 
         TableColumn qColumn = new TableColumn(table, SWT.NONE);
         qColumn.setWidth(100);
         qColumn.setText("Q");
 
         TableColumn pColumn = new TableColumn(table, SWT.NONE);
         pColumn.setWidth(100);
         pColumn.setText("P");
     }
 
 
     public void open(ApplicationContext context) {
         Display display = Display.getDefault();
         shell = new Shell();
         shell.setSize(600, 447);
         shell.setText("");
 
         createContents(context);
 
         shell.setLayout(null);
         shell.open();
         shell.layout();
         while (!shell.isDisposed()) {
             if (!display.readAndDispatch()) {
                 display.sleep();
             }
         }
 
     }
 }
