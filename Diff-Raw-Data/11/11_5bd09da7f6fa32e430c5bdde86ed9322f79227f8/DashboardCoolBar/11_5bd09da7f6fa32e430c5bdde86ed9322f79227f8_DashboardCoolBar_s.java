 package org.ic.tennistrader.ui.toolbars;
 
 import org.apache.log4j.Logger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.CoolBar;
 import org.eclipse.swt.widgets.CoolItem;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.ic.tennistrader.listener.HoverListener;
 
 public class DashboardCoolBar extends Composite {
 
 	static int itemCount;
 
 	private final CoolBar coolBar;
 
 	private CoolItem profileItem;
 
 	private CoolItem dashboardItem;
 
 	private CoolItem searchItem;
 	
 	private static Logger log = Logger.getLogger(DashboardCoolBar.class);
 
 	public DashboardCoolBar(Composite parent) {
 		super(parent, SWT.NONE);
 		setLayout(new FillLayout());
 		coolBar = new CoolBar(this, SWT.NONE);
 
 		makeDashboardItem();
 		makeSearchItem();
 		makeProfileItem();
 
 		coolBar.pack();
 
 		int searchWidth = Display.getCurrent().getClientArea().width
 				- profileItem.getBounds().width
 				- dashboardItem.getBounds().width;
 
 		log.info("shell width: " + parent.getClientArea().width
 				+ "search box width: " + searchWidth + " profile bar width: "
 				+ profileItem.getBounds().width + " dashboard bar width: "
 				+ dashboardItem.getBounds().width);
 
 		searchItem.setPreferredSize(searchWidth, searchItem.getSize().y);
 		searchItem.setSize(searchWidth, searchItem.getSize().y);
 	}
 
 	private void makeProfileItem() {
 		profileItem = new CoolItem(coolBar, SWT.NONE);
 		ProfileToolBar ptb = new ProfileToolBar(coolBar);
 		ToolBar tb = ptb.getToolBar();
 		tb.pack();
 		Point p = tb.getSize();
		Point p2 = profileItem.computeSize(p.x, p.y);
 		profileItem.setControl(tb);
 		profileItem.setPreferredSize(p2);
 
 	}
 
 	private void makeDashboardItem() {
 		dashboardItem = new CoolItem(coolBar, SWT.NONE);
 		DashboardToolBar dtb = new DashboardToolBar(coolBar);
 		ToolBar tb = dtb.getToolBar();
 		tb.pack();
 		Point p = tb.getSize();
 		tb.setSize(p);
 		Point p2 = dashboardItem.computeSize(p.x, p.y);
 		dashboardItem.setControl(tb);
 		dashboardItem.setPreferredSize(p2);
 	}
 
 	private void makeSearchItem() {
 		searchItem = new CoolItem(coolBar, SWT.NONE);
 		Composite c = new Composite(coolBar, SWT.NONE);
 		c.setLayout(new GridLayout(3, false));
 		Button b = new Button(c, SWT.PUSH);
 		b.setImage(new Image(Display.getCurrent(), "images/toolbar/search.png"));
 		b.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, false,
 				true, 1, 1));
 		Text t = new Text(c, SWT.NONE);
 		t.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
 		t.setText("Search...");
 		t.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true,
 				1, 1));
 		t.addMouseListener(new HoverListener(t));
 		Button x = new Button(c, SWT.PUSH);
 		x.setImage(new Image(Display.getCurrent(), "images/toolbar/cross.png"));
 		x.setLayoutData(new GridData(GridData.END, GridData.FILL, false, true,
 				1, 1));
 
 		searchItem.setControl(c);
 		searchItem.setPreferredSize(t.computeSize(SWT.DEFAULT, 36));
 	}
 	
 }
