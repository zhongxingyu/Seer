 package org.javakontor.sherlog.application.internal;
 
 import static org.javakontor.sherlog.application.action.MenuConstants.MENUBAR_ID;
 import static org.javakontor.sherlog.application.action.MenuConstants.TOOLS_MENU_ID;
 import static org.javakontor.sherlog.application.action.MenuConstants.WINDOW_MENU_ID;
 import static org.javakontor.sherlog.application.action.MenuConstants.WINDOW_MENU_TARGET_ID;
 
 import javax.swing.JInternalFrame;
 import javax.swing.SwingUtilities;
 
 import org.javakontor.sherlog.application.action.AbstractAction;
 import org.javakontor.sherlog.application.action.ActionGroupType;
 import org.javakontor.sherlog.application.action.contrib.ActionContribution;
 import org.javakontor.sherlog.application.action.contrib.ActionGroupContribution;
 import org.javakontor.sherlog.application.action.contrib.DefaultActionContribution;
 import org.javakontor.sherlog.application.action.contrib.DefaultActionGroupContribution;
 import org.javakontor.sherlog.application.internal.util.Arranger;
 import org.javakontor.sherlog.application.view.ViewContribution;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 
 public class WindowMenu {
   public static final String        WINDOW_ARRANGE_MENU_ID        = WINDOW_MENU_ID + ".arrange";
 
   public static final String        WINDOW_ARRANGE_MENU_TARGET_ID = MENUBAR_ID + "/" + WINDOW_ARRANGE_MENU_ID;
 
   private final ServiceRegistration _registration;
 
   ApplicationWindow                 _applicationWindow;
 
   public WindowMenu(final BundleContext context, final ApplicationWindow applicationWindow) {
     this._applicationWindow = applicationWindow;
 
     // Create 'Window' menu in menubar
     final WindowMenuActionGroup windowMenuActionGroup = new WindowMenuActionGroup();
     this._registration = context.registerService(ActionGroupContribution.class.getName(), windowMenuActionGroup, null);
   }
 
   /**
    * An ActionGroup containing:
    * <ul>
    * <li>an <b>arrange<b> ActionGroup that allows a user to re-arrange the visible windows</li>
    * <li>a dynamic <b>window list</b> that list all opened window</li>
    * <li>an empty <b>tools</b> submenu</li>
    * <ul>
    * 
    * @author Nils Hartmann (nils@nilshartmann.net)
    */
   class WindowMenuActionGroup extends DefaultActionGroupContribution {
 
     private final ActionGroupContribution[] _staticActionGroups;
 
     public WindowMenuActionGroup() {
       super(WINDOW_MENU_ID, MENUBAR_ID + "(last)", ApplicationMessages.windowMenuTitle, ActionGroupType.simple);
 
      this._staticActionGroups = new ActionGroupContribution[] { new ArrangeActionGroup(),
           new DefaultActionGroupContribution(TOOLS_MENU_ID, WINDOW_MENU_TARGET_ID, ApplicationMessages.toolsMenuTitle) };
     }
 
     @Override
     public ActionGroupContribution[] getStaticActionGroupContributions() {
       return this._staticActionGroups;
     }
 
     @Override
     public boolean isFinal() {
       return false;
     }
   }
 
  public ActionContribution getArrangeActionGroupContirbution(final String label, final int arrangeMode) {
     return new DefaultActionContribution(WINDOW_ARRANGE_MENU_ID + "." + arrangeMode, WINDOW_ARRANGE_MENU_TARGET_ID,
         label, null, new ArrangeAction(arrangeMode));
   }
 
  class ArrangeActionGroup extends DefaultActionGroupContribution {
 
     private final ActionContribution[] _actions;
 
    public ArrangeActionGroup() {
       super(WINDOW_ARRANGE_MENU_ID, WINDOW_MENU_TARGET_ID + "(first)", ApplicationMessages.arrangeMenuTitle,
           ActionGroupType.simple);
 
       this._actions = new ActionContribution[] {
          getArrangeActionGroupContirbution(ApplicationMessages.horizontalMenuTitle, Arranger.HORIZONTAL),
          getArrangeActionGroupContirbution(ApplicationMessages.verticalMenuTitle, Arranger.VERTICAL),
          getArrangeActionGroupContirbution(ApplicationMessages.arrangeWindowsMenuTitle, Arranger.ARRANGE),
          getArrangeActionGroupContirbution(ApplicationMessages.cascadeMenuTitle, Arranger.CASCADE) };
     }
 
     @Override
     public ActionContribution[] getStaticActionContributions() {
       return this._actions;
     }
 
     @Override
     public boolean isFinal() {
       return false;
     }
 
   }
 
   class ArrangeAction extends AbstractAction {
 
     private final int _arrangeMode;
 
     public ArrangeAction(final int arrangeMode) {
 
       this._arrangeMode = arrangeMode;
     }
 
     public void execute() {
       WindowMenu.this._applicationWindow.arrange(this._arrangeMode);
     }
   }
 
   /**
    * An {@link ActionContribution}, that brings the window of a {@link ViewContribution} to front
    * 
    */
   class OpenWindowAction extends AbstractAction {
 
     private final ViewContribution _viewContribution;
 
     public OpenWindowAction(final ViewContribution viewContribution) {
       this._viewContribution = viewContribution;
     }
 
     public void execute() {
       // Find the JInternalFrame that holds the ViewContribution
       final JInternalFrame window = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class,
           this._viewContribution.getPanel());
 
       // bring it to front
       if (window != null) {
         window.toFront();
         window.requestFocusInWindow();
       }
     }
   }
 
   public void dispose() {
     this._registration.unregister();
   }
 
 }
