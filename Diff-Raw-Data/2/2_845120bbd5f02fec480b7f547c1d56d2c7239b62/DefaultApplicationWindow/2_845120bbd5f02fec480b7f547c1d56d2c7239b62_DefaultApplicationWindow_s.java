 package org.javakontor.sherlog.application.internal;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.beans.PropertyVetoException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDesktopPane;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.SwingUtilities;
 
 import org.javakontor.sherlog.application.internal.util.Arranger;
 import org.javakontor.sherlog.application.internal.util.WallpaperDesktopPane;
 import org.javakontor.sherlog.application.internal.window.DialogFrame;
 import org.javakontor.sherlog.application.internal.window.ModalDialogFrame;
 import org.javakontor.sherlog.application.request.Request;
 import org.javakontor.sherlog.application.request.RequestHandler;
 import org.javakontor.sherlog.application.request.RequestHandlerImpl;
 import org.javakontor.sherlog.application.request.SetStatusMessageRequest;
 import org.javakontor.sherlog.application.view.ViewContribution;
 import org.javakontor.sherlog.util.Assert;
 import org.javakontor.sherlog.util.ui.GuiExecutor;
 
 /**
  * Implements a default application window.
  */
 public class DefaultApplicationWindow extends JFrame implements ApplicationWindow, RequestHandler {
 
   /**
    * 
    */
   private static final long             serialVersionUID = 1L;
 
   /** the internal used desktop pane * */
   private WallpaperDesktopPane          _desktopPane;
 
   private ApplicationStatusBar          statusPanel;
 
   private RequestHandler                _requestHandler;
 
   /**
    * the map of registered dialogs
    * 
    * <p>
    * Value can either be an DialogFrame (in cases ViewContribution is a non-modal represents dialog) or a ModalDialog in
    * cases ViewContribution is modal
    */
   private Map<ViewContribution, Object> _dialogMap;
 
   /**
    * Creates an instance of type MainWindow.
    * 
    * @param title -
    * 
    * @throws HeadlessException -
    */
   public DefaultApplicationWindow(String title) throws HeadlessException {
     super(title);
 
     setUp();
   }
 
   @Override
   public void dispose() {
     if (this.statusPanel != null) {
       this.statusPanel.dispose();
     }
     super.dispose();
   }
 
   public void handleRequest(Request request) {
     this._requestHandler.handleRequest(request);
   }
 
   public void setSuccessor(RequestHandler successor) {
     this._requestHandler.setSuccessor(successor);
   }
 
   /**
    * Adds the specified dialog to the application window.
    * 
    * @param viewContribution
    *          the dialog to add to the application window.
    */
   public void add(ViewContribution viewContribution) {
     Assert.notNull("Parameter viewContribution has to be set!", viewContribution);
 
     if (!this._dialogMap.containsKey(viewContribution)) {
       Object view;
       if (viewContribution.getDescriptor().isModal()) {
         view = openModalFrame(viewContribution);
       } else {
         view = openNonModalFrame(viewContribution);
       }
       this._dialogMap.put(viewContribution, view);
       viewContribution.setSuccessor(this);
     }
   }
 
   /**
    * Removes the specified dialog form the application window.
    * 
    * @param viewContribution
    *          the dialog to remove from the application window.
    */
   public void remove(ViewContribution viewContribution) {
     Assert.notNull("Parameter dialog has to be set!", viewContribution);
 
     if (this._dialogMap.containsKey(viewContribution)) {
       Object view = this._dialogMap.get(viewContribution);
       if (viewContribution.getDescriptor().isModal()) {
         ModalDialogFrame modalDialogFrame = (ModalDialogFrame) view;
         modalDialogFrame.dispose();
       } else {
         DialogFrame dialogFrame = (DialogFrame) view;
         this._desktopPane.remove(dialogFrame);
         dialogFrame.dispose();
       }
 
       this._dialogMap.remove(viewContribution);
 
       validate();
       repaint();
     }
   }
 
   /**
    * Sets the specified image as the wallpaper.
    * 
    * @param wallpaper
    *          the image to set as the wallpaper.
    */
   public void setWallpaper(Image wallpaper) {
     this._desktopPane.setWallpaper(wallpaper);
   }
 
   /**
    * Sets the layout style.
    * 
    * @param wallpaperLayoutStyle
    *          the layout style.
    */
   public void setWallpaperLayoutStyle(int wallpaperLayoutStyle) {
     this._desktopPane.setWallpaperLayoutStyle(wallpaperLayoutStyle);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.javakontor.sherlog.application.internal.window.ApplicationWindow#arrange(int)
    */
   public void arrange(int style) {
     Arranger.tileFrames(style, this._desktopPane);
   }
 
   /**
    * Set up the main window.
    */
   private void setUp() {
 
     this._requestHandler = new RequestHandlerImpl() {
 
       @Override
       public boolean canHandleRequest(Request request) {
         return request instanceof SetStatusMessageRequest;
       }
 
       @Override
       public void doHandleRequest(final Request request) {
         if (DefaultApplicationWindow.this._dialogMap.containsKey(request.sender())) {
           GuiExecutor.execute(new Runnable() {
             public void run() {
               Object view = DefaultApplicationWindow.this._dialogMap.get(request.sender());
               // SetStatusMessageRequest is only allowed for DialogFrames (i.e. non-modal dialogs)
               if (view instanceof DialogFrame) {
                 DialogFrame dialogFrame = (DialogFrame) view;
                 dialogFrame.setStatusMessage(((SetStatusMessageRequest) request).getStatusMessage());
               }
             }
           });
         }
       }
     };
 
     this._dialogMap = new HashMap<ViewContribution, Object>();
 
     this._desktopPane = new WallpaperDesktopPane();
    this._desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
 
     getContentPane().setLayout(new BorderLayout());
     getContentPane().add(this._desktopPane, BorderLayout.CENTER);
     this.statusPanel = new ApplicationStatusBar();
     getContentPane().add(this.statusPanel, java.awt.BorderLayout.SOUTH);
 
     this.getGlassPane().setVisible(true);
     Image image = new ImageIcon(getClass().getResource("background.jpg")).getImage();
 
     setWallpaperLayoutStyle(WallpaperDesktopPane.STRETCH);
     setWallpaper(image);
 
     setJMenuBar(new JMenuBar());
 
     setSize(new Dimension(800, 600));
     setLocationRelativeTo(null);
 
   }
 
   /**
    * Creates and opens a modal frame for the specified viewContribution.
    * 
    * @param viewContribution
    *          the view contribution
    * @return the {@link ModalDialogFrame} that has been opened for the specified ViewContribution
    */
   protected ModalDialogFrame openModalFrame(ViewContribution viewContribution) {
 
     // Create a new ModalDialogFrame instance for the ViewContribution
     final ModalDialogFrame modalDialogFrame = new ModalDialogFrame(this, viewContribution);
 
     // Position the frame and make it visible on swing EDT
     SwingUtilities.invokeLater(new Runnable() {
       public void run() {
         modalDialogFrame.setLocationRelativeTo(DefaultApplicationWindow.this);
         modalDialogFrame.setVisible(true);
       }
     });
 
     // return the instance
     return modalDialogFrame;
   }
 
   /**
    * Creates and opens a non-modal frame for the specified viewContribution
    * 
    * @param viewContribution
    *          the view contribution
    * @return the DialogFrame that has been opened for the specified ViewContribution
    */
   protected DialogFrame openNonModalFrame(ViewContribution viewContribution) {
     // Create new DialogFrame instance
     final DialogFrame dialogFrame = new DialogFrame(this, viewContribution);
 
     // add Frame to desktop pane
     this._desktopPane.add(dialogFrame);
 
     // maximize if requested
     if (viewContribution.getDescriptor().openMaximized()) {
       try {
         dialogFrame.setMaximum(true);
       } catch (PropertyVetoException e) {
         //
       }
     }
 
     // make the frame visible on swing EDT
     SwingUtilities.invokeLater(new Runnable() {
       public void run() {
         dialogFrame.toFront();
         dialogFrame.setVisible(true);
       }
     });
 
     return dialogFrame;
   }
 
 }
