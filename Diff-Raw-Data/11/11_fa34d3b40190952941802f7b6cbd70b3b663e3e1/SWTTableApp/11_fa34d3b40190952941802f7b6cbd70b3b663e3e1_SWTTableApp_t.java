 package edu.umd.cs.guitar.ripper.test.aut;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 //Based off of code found at http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/TableExample.htm
 
 public class SWTTableApp {
 	public static void main(String[] argv) {
 		Display display = new Display();
 		Shell shell = new Shell(display);
 
 		shell.setSize(320, 200);
 
 		shell.setText("Table Window");
 		shell.setLayout(new FillLayout());
 
 		Table table = new Table(shell, SWT.BORDER);
 
 		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
 		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
 		TableColumn tc3 = new TableColumn(table, SWT.CENTER);
 		tc1.setText("First Column");
 		tc2.setText("Second Column");
 		tc3.setText("Third Column");
		tc1.setWidth(120);
		tc2.setWidth(120);
		tc3.setWidth(120);
 		table.setHeaderVisible(true);
 
 		TableItem item1 = new TableItem(table, SWT.NONE);
 		item1.setText(new String[] { "Item 1.1", "Item 1.2", "Item 1.3" });
 		TableItem item2 = new TableItem(table, SWT.NONE);
 		item2.setText(new String[] { "Item 2.1", "Item 2.2", "Item 2.3" });
 		TableItem item3 = new TableItem(table, SWT.NONE);
 		item3.setText(new String[] { "Item 3.1", "Item 3.2", "Item 3.3" });
						
 		shell.open();
		
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 		display.dispose();
 	}
 
 }
