 package kkckkc.jsourcepad.ui;
 
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.model.settings.WindowSettings;
 import kkckkc.jsourcepad.ui.statusbar.CurrentPosition;
 import kkckkc.jsourcepad.ui.statusbar.LanguageView;
 import kkckkc.jsourcepad.ui.statusbar.SymbolView;
 import kkckkc.jsourcepad.ui.statusbar.TabView;
import net.miginfocom.swing.MigLayout;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.annotation.PostConstruct;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import java.awt.*;
 
 public class WindowViewImpl implements WindowView {
 
 	private Window window;
 	private JMenuBar menubar;
 	private JFrame frame;
     private ProjectPresenter tree;
     private DocListPresenter blv;
 
     private boolean showProjectDrawer;
     private JSplitPane splitpane;
 
     @Autowired
 	public void setWindow(Window window) {
 	    this.window = window;
     }
 	
 	@PostConstruct
 	public void init() {
 		frame.setSize(900, 650);
 		
 		splitpane = createSplitPane();
 
 		tree = window.getPresenter(ProjectPresenter.class);
         blv = window.getPresenter(DocListPresenter.class);
 
         WindowSettings ws = Application.get().getSettingsManager().get(WindowSettings.class);
         showProjectDrawer = ws.isShowProjectDrawer() && window.getProject() != null;
 
         if (showProjectDrawer) {
             frame.add(splitpane, BorderLayout.CENTER);
 
 		    JScrollPane st = createTreeScrollPane(tree.getJComponent());
 		    st.setPreferredSize(new Dimension(200, 0));
 		    splitpane.add(st);
     		splitpane.add(blv.getJComponent());
         } else {
             frame.add(blv.getJComponent(), BorderLayout.CENTER);
         }
 		
 		menubar = new JMenuBar();
 		frame.setJMenuBar(menubar);
 		
 		JPanel statusBarPanel = createStatusBar();
 
        statusBarPanel.add(new JButton("File Filter"));
		statusBarPanel.add(processStatusBarView(new LanguageView(window)), "split 8,gapx 10");
		statusBarPanel.add(delimiter(), "gapx 10");
		statusBarPanel.add(processStatusBarView(new CurrentPosition(window)), "gapx 10");
		statusBarPanel.add(delimiter(), "gapx 10");
		statusBarPanel.add(processStatusBarView(new TabView(window)), "gapx 10");
		statusBarPanel.add(delimiter(), "gapx 10");
		statusBarPanel.add(processStatusBarView(new SymbolView(window)), "gapx 10");
 		
 		frame.add(statusBarPanel, BorderLayout.SOUTH);
 	}
 
 	protected Component processStatusBarView(JComponent view) {
 		return view;
 	}
 
 	protected JPanel createStatusBar() {
		//JPanel statusBarPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 15, 0));
        JPanel statusBarPanel = new JPanel(new MigLayout("insets 0 0 0 5", "[left][right,grow]"));
 		statusBarPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
 		return statusBarPanel;
 	}
 
 	protected JScrollPane createTreeScrollPane(JComponent tree) {
 	    JScrollPane st = new JScrollPane(tree);
 	    return st;
     }
 	
 	public JMenuBar getMenubar() {
 	    return menubar;
     }
 
     @Override
     @Autowired
     public void setJFrame(JFrame frame) {
         this.frame = frame;
     }
 
     @Override
     public void setShowProjectDrawer(boolean showProjectDrawer) {
         if (this.showProjectDrawer == showProjectDrawer) return;
         
         this.showProjectDrawer = showProjectDrawer;
                 
         if (showProjectDrawer) {
             frame.remove(blv.getJComponent());
 
             splitpane = createSplitPane();
             frame.add(splitpane, BorderLayout.CENTER);
 
 		    JScrollPane st = createTreeScrollPane(tree.getJComponent());
 		    st.setPreferredSize(new Dimension(200, 0));
             splitpane.add(st);
     		splitpane.add(blv.getJComponent());
 
             frame.validate();
             frame.repaint();
         } else {
             frame.remove(splitpane);
             splitpane.removeAll();
 
             frame.add(blv.getJComponent(), BorderLayout.CENTER);
 
             frame.validate();
             frame.repaint();
         }
     }
 
     protected JLabel delimiter() {
 	    JLabel jl = new JLabel("");
 		jl.setBorder(new EtchedBorder(EtchedBorder.RAISED));
 		jl.setPreferredSize(new Dimension(1, 20));
 		jl.setMaximumSize(new Dimension(1, 200));
 	    return jl;
     }
 
 	protected JSplitPane createSplitPane() {
 		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		return sp;
 	}	
 }
