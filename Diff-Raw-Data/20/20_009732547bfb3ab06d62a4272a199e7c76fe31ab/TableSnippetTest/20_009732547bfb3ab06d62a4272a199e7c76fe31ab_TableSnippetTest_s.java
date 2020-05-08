 package com.emu.swt.snippets;
 
 import static org.junit.Assert.*;
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
 import org.junit.Test;
 
 public class TableSnippetTest {
 
 	@Test
 	public void testMain() {
		TableSnippet snippet = new TableSnippet();
		snippet.main(null);
 	}
 
 }
