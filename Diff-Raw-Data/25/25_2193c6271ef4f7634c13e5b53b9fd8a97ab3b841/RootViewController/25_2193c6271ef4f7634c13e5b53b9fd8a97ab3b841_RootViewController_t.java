 package my.kinderlieder;
 
 import org.xmlvm.iphone.*;
 
 import java.util.*;
 
class RootViewController extends RotatingViewController implements Observer {
    private Map<SongInfo, UITableViewCell> cells = new HashMap<SongInfo, UITableViewCell>();
     private final UIButton infoButton;
     private UIViewController shopViewController;
     private final UITableView mainView;
     private final UITableViewDataSource dataSource;
     private final UITableViewDelegate delegate;
     private List<SongInfo> songInfos;
     private final UIBarButtonItemDelegate shopAction;
     private final UIControlDelegate infoAction;
     private PdfViewController pdfViewController;
     private final UIBarButtonItem shopButton;
 
     public RootViewController(final UIWindow window, List<SongInfo> songInfos) {
         this.songInfos = songInfos;
        pdfViewController = new PdfViewController(window);
         mainView = new UITableView(window.getFrame(), UITableViewStyle.Plain);
         setTitle("Kinderlieder");
         infoButton = UIButton.buttonWithType(UIButtonType.InfoLight);
         infoAction = new UIControlDelegate() {

            private UIViewController infoController;

             public void raiseEvent(UIControl sender, int uiControlEvent) {
                if (infoController == null) {
                    infoController = new InfoViewController(window);
                }
                 getNavigationController().pushViewController(infoController, true);
             }
         };
         infoButton.addTarget(infoAction, UIControlEvent.TouchUpInside);
         shopAction = new UIBarButtonItemDelegate() {
 
             public void clicked() {
                if (shopViewController == null) {
                     shopViewController = new ShopViewController(window);
                 }
                 getNavigationController().pushViewController(shopViewController, true);
             }
         };
         shopButton = new UIBarButtonItem(UIImage.imageNamed("arrow_down_24.png"), UIBarButtonItemStyle.Plain, shopAction);
         shopButton.setTitle("Download");
 
         setToolbarItems(new ArrayList<UIBarButtonItem>(Arrays.asList(new UIBarButtonItem(infoButton),
                 shopButton)));
         setView(mainView);
         dataSource = new UITableViewDataSource() {
             @Override
             public UITableViewCell cellForRowAtIndexPath(UITableView table, NSIndexPath idx) {
                 final SongInfo songInfo = RootViewController.this.songInfos.get(idx.getRow());
                 UITableViewCell cell = cells.get(songInfo);
                 if (cell == null) {
                     cell = new UITableViewCell(UITableViewCellStyle.Default, null);
                     cell.getTextLabel().setText(songInfo.getName());
                     //cell.getDetailTextLabel().setText(songInfo.getCollectionInfo().getName());
                     cells.put(songInfo, cell);
                 }
                 return cell;
             }
 
             @Override
             public int numberOfRowsInSection(UITableView table, int section) {
                 return RootViewController.this.songInfos.size();
             }
         };
         mainView.setDataSource(dataSource);
 
         delegate = new UITableViewDelegate() {
             @Override
             public void didSelectRowAtIndexPath(UITableView tableview, NSIndexPath indexPath) {
                 final SongInfo songInfo = RootViewController.this.songInfos.get(indexPath.getRow());
                 pdfViewController.show(songInfo);
                 getNavigationController().setToolbarHidden(false, true);
                 getNavigationController().pushViewController(pdfViewController, true);
             }
         };
         mainView.setDelegate(delegate);
 
         Main.library.addObserver(this);
     }

     private void reloadDataOnMainThread() {
         NSObject.performSelectorOnMainThread(new NSSelector<Void>() {
 
             public void invokeWithArgument(Void ignore) {
                 mainView.reloadData();
             }
         }, null, false);
     }
 
     public void reloadSongList() {
         songInfos.clear();
         songInfos.addAll(Main.library.getSongInfos());
         reloadDataOnMainThread();
     }
 
     public void update(Observable o, Object arg) {
        if (o == Main.library) {
             reloadSongList();
         }
     }
 }
