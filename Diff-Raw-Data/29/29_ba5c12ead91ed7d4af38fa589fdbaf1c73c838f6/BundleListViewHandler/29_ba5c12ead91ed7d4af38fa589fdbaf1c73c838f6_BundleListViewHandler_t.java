 package org.javakontor.sherlog.test.ui.handler;
 
 import org.javakontor.sherlog.test.ui.framework.AbstractViewContributionHandler;
 import org.javakontor.sherlog.test.ui.framework.GuiTestContext;
 import org.netbeans.jemmy.operators.JInternalFrameOperator;
 import org.netbeans.jemmy.operators.JTableOperator;
 import org.netbeans.jemmy.operators.WindowOperator;
 
 public class BundleListViewHandler extends AbstractViewContributionHandler {
 
  public static final String           BUNDLE_LIST_VIEWCONTRIBUTION_NAME = "Management Agent";
 
   private final JTableOperator         _bundleListTableOperator;
 
   private final JInternalFrameOperator _bundleListWindowOperator;
 
  public BundleListViewHandler(final GuiTestContext guiTestContext, final WindowOperator parentOperator)
      throws Exception {
     super(guiTestContext);
 
    this._bundleListWindowOperator = new JInternalFrameOperator(parentOperator, BUNDLE_LIST_VIEWCONTRIBUTION_NAME);
    this._bundleListTableOperator = new JTableOperator(this._bundleListWindowOperator);
     getGuiTestSupport().assertViewContributionRegistered(BUNDLE_LIST_VIEWCONTRIBUTION_NAME);
 
   }
 
  public static BundleListViewHandler openFromMenu(final ApplicationWindowHandler handler) throws Exception {
    handler.pushWindowMenuItem("Tools|Management Agent", false);
     // Container source = (Container) handler.getToolsMenuOperator().getSource();
     // System.err.println(" * SOURCE: " + source);
     // JMenuItem waitJMenuItem = JMenuItemOperator.waitJMenuItem(source, "Bundle list", true, true);
     // assertTrue(waitJMenuItem.isSelected());
 
     return new BundleListViewHandler(handler.getGuiTestContext(), handler.getApplicationFrameOperator());
   }
 
   public JInternalFrameOperator getBundleListWindowOperator() {
    return this._bundleListWindowOperator;
   }
 
   public JTableOperator getBundleListTableOperator() {
    return this._bundleListTableOperator;
   }
 
 }
