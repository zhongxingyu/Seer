 package my.kinderlieder;
 
 import org.xmlvm.iphone.*;
 
import java.util.*;
 
 class RootViewController extends RotatingViewController {
    private Map<SongInfo, UITableViewCell> cells=new HashMap<SongInfo, UITableViewCell>();
    private final UIButton infoButton;

     public RootViewController(final UIWindow window, final List<SongInfo> songInfos) {
         UITableView mainView = new UITableView(window.getFrame(), UITableViewStyle.Plain);
         setTitle("Kinderlieder");
        infoButton = UIButton.buttonWithType(UIButtonType.InfoLight);
         infoButton.addTarget(new UIControlDelegate() {
             public void raiseEvent(UIControl sender, int uiControlEvent) {
                 UIViewController infoController = new InfoViewController(window);
                 getNavigationController().pushViewController(infoController, true);
             }
         }, UIControlEvent.TouchUpInside);
         final UIBarButtonItem shopButton = new UIBarButtonItem(UIImage.imageNamed("arrow_down_24.png"), UIBarButtonItemStyle.Plain, new UIBarButtonItemDelegate() {
 
             public void clicked() {
                 UIViewController shopController = new ShopViewController(window);
                 getNavigationController().pushViewController(shopController, true);
             }
         });
         shopButton.setTitle("Download");
 
         setToolbarItems(new ArrayList<UIBarButtonItem>(Arrays.asList(new UIBarButtonItem(infoButton),
                 shopButton)));
         setView(mainView);
         mainView.setDataSource(new UITableViewDataSource() {
             @Override
             public UITableViewCell cellForRowAtIndexPath(UITableView table, NSIndexPath idx) {
                 final SongInfo songInfo = songInfos.get(idx.getRow());
                UITableViewCell cell = cells.get(songInfo);
                if(cell==null){
                    cell=new UITableViewCell(UITableViewCellStyle.Default, null);
                    cell.getTextLabel().setText(songInfo.getName()+" "+ songInfo.getCollectionInfo().getName());
                    cells.put(songInfo, cell);
                }
                 return cell;
             }
 
             @Override
             public int numberOfRowsInSection(UITableView table, int section) {
                 return songInfos.size();
             }
         });
 
         mainView.setDelegate(new UITableViewDelegate() {
             @Override
             public void didSelectRowAtIndexPath(UITableView tableview, NSIndexPath indexPath) {
                 final SongInfo songInfo = songInfos.get(indexPath.getRow());
                 final UIViewController pdfViewController = new PdfViewVontroller(songInfo, window);
                 getNavigationController().setToolbarHidden(false, true);
                 getNavigationController().pushViewController(pdfViewController, true);
             }
         });
 
     }
 }
