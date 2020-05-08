 package kkckkc.jsourcepad.ui;
 
 import com.google.common.io.Resources;
 import kkckkc.jsourcepad.Presenter;
 import kkckkc.jsourcepad.action.bundle.BundleAction;
 import kkckkc.jsourcepad.action.bundle.BundleJMenuItem;
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.Doc;
 import kkckkc.jsourcepad.model.DocList;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.model.bundle.BundleMenuProvider;
 import kkckkc.jsourcepad.model.settings.SettingsManager;
 import kkckkc.jsourcepad.model.settings.WindowSettings;
 import kkckkc.jsourcepad.util.action.ActionGroup;
 import kkckkc.jsourcepad.util.action.CompoundActionGroup;
 import kkckkc.jsourcepad.util.action.MenuFactory;
 import kkckkc.jsourcepad.util.messagebus.DispatchStrategy;
 import kkckkc.jsourcepad.util.messagebus.Subscription;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 import java.util.Arrays;
 
 
 public class WindowPresenter implements Presenter<WindowView>, DocList.Listener {
 
 	private Window window;
 	private WindowView windowView;
 	private JFrame frame;
     private Subscription subscription;
 
     @Autowired
 	public void setWindow(Window window) {
 	    this.window = window;
     }
 
 	@Autowired
     public void setView(WindowView view) {
 	    this.windowView = view;
     }
 
 
 	@PostConstruct
     public void init() throws Exception {
 		frame = window.getContainer();
 
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         
         frame.setIconImages(Arrays.asList(
                 toolkit.createImage(Resources.toByteArray(getClass().getResource("/icons/icon_16x16x32.png"))),
                 toolkit.createImage(Resources.toByteArray(getClass().getResource("/icons/icon_32x32x32.png"))),
                 toolkit.createImage(Resources.toByteArray(getClass().getResource("/icons/icon_48x48x32.png"))),
                 toolkit.createImage(Resources.toByteArray(getClass().getResource("/icons/icon_128x128x32.png"))),
                 toolkit.createImage(Resources.toByteArray(getClass().getResource("/icons/icon_256x256x32.png")))
                 ));
         windowView.setJFrame(frame);
         
 		frame.setTitle("JSourcePad");
 		
 		frame.setLocationRelativeTo(null);
         frame.setLocationByPlatform(true);
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				Application.get().getWindowManager().closeWindow(window);
 			}
 		});
 
 		frame.addWindowFocusListener(new WindowFocusListener() {
 			public void windowLostFocus(WindowEvent e) {
 				window.topic(Window.FocusListener.class).post().focusLost(window);
 			}
 
 			public void windowGainedFocus(WindowEvent e) {
 				window.topic(Window.FocusListener.class).post().focusGained(window);
 			}
 		});
 		
 		final JMenuBar mb = windowView.getMenubar();
 
         final JMenu fileMenu = new JMenu("File");
         final JMenu editMenu = new JMenu("Edit");
         final JMenu viewMenu = new JMenu("View");
         final JMenu textMenu = new JMenu("Text");
         final JMenu navigationMenu = new JMenu("Navigation");
         final JMenu bundleMenu = new JMenu("Bundles");
         final JMenu windowMenu = new JMenu("Window");
 
         mb.add(fileMenu);
         mb.add(editMenu);
         mb.add(viewMenu);
         mb.add(textMenu);
         mb.add(navigationMenu);
         mb.add(bundleMenu);
         mb.add(windowMenu);
 
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 MenuFactory menuFactory = new MenuFactory();
 
                 menuFactory.buildMenu(fileMenu, window.getActionManager().getActionGroup("file-menu"), null, false);
                 menuFactory.buildMenu(editMenu, window.getActionManager().getActionGroup("edit-menu"), null, false);
                 menuFactory.buildMenu(viewMenu, window.getActionManager().getActionGroup("view-menu"), null, false);
                 menuFactory.buildMenu(textMenu, window.getActionManager().getActionGroup("text-menu"), null, false);
                 menuFactory.buildMenu(navigationMenu, window.getActionManager().getActionGroup("navigation-menu"), null, false);
 
                 ActionGroup bundlesAg = window.getActionManager().getActionGroup("bundles-menu");
                 MenuFactory.ItemBuilder itemBuilder = new MenuFactory.ItemBuilder() {
                     public JMenuItem build(Action action) {
                         if (action instanceof BundleAction) {
                             return new BundleJMenuItem((BundleAction) action);
                         } else {
                             return new JMenuItem(action);
                         }
                     }
                 };
                 bundlesAg.putValue("itemBuilder", itemBuilder);
                 menuFactory.buildMenu(bundleMenu, new CompoundActionGroup(bundlesAg, BundleMenuProvider.getBundleActionGroup()), itemBuilder, true);
                 menuFactory.buildMenu(windowMenu, window.getActionManager().getActionGroup("window-menu"), null, false);
             }
         });
 
        window.restoreState();
         window.topic(DocList.Listener.class).subscribe(DispatchStrategy.ASYNC_EVENT, WindowPresenter.this);
         frame.setVisible(true);
 
         subscription = Application.get().getSettingsManager().subscribe(WindowSettings.class, new SettingsManager.Listener<WindowSettings>() {
             public void settingUpdated(WindowSettings settings) {
                 windowView.setShowProjectDrawer(settings.isShowProjectDrawer());
             }
         }, false);
     }
 	
 
     @PreDestroy
     public void destroy() {
         subscription.unsubscribe();
     }
 	
 	public void dispose() {
 		this.frame.dispose();
 	}
 
 	public void closed(int index, Doc doc) {
 		this.frame.setTitle("JSourcePad");
 	}
 
 	public void created(Doc doc) {
 	}
 
 	public void selected(int index, Doc doc) {
 		this.frame.setTitle("JSourcePad - " + doc.getTitle());
 	}
 
 }
