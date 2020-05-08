 package my.kinderlieder;
 
 import org.xmlvm.iphone.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 class PdfViewVontroller extends RotatingViewController {
     //these should be local, but can't since we need them inside the delegate
     private UIBarButtonItem rightBarButtonItem;
     public UIBarButtonItem repeatButton;
     private List<UIBarButtonItem> buttonsPlay;
     private List<UIBarButtonItem> buttonsPauseStop;
     private List<UIBarButtonItem> buttonsPlayStop;
     boolean repeat;
 
     public PdfViewVontroller(SongInfo songInfo, UIWindow window) {
         setTitle(songInfo.name);
         UIWebView pdfView = new UIWebView(window.getFrame());
         setView(pdfView);
         pdfView.setScalesPageToFit(true);
         final NSURL pdfURL = NSURL.fileURLWithPath(songInfo.pdfPath);
         pdfView.loadRequest(NSURLRequest.requestWithURL(pdfURL));
         if (UIPrintInteractionController.isPrintingAvailable()) {
             rightBarButtonItem = new UIBarButtonItem(UIBarButtonSystemItem.Action, new UIBarButtonItemDelegate() {
                 public void clicked() {
                     UIPrintInteractionController print = UIPrintInteractionController.sharedPrintController();
                     print.setPrintingItem(pdfURL);
                     print.presentFromBarButtonItem(rightBarButtonItem, true, new UIPrintInteractionController.UIPrintInteractionCompletionHandler() {
 
                         public void completed(UIPrintInteractionController controller, boolean b, NSError nsError) {
                             System.out.println("finished: " + b + ", error: " + nsError);
                         }
                     });
                 }
             });
             setHidesBottomBarWhenPushed(false);
             getNavigationItem().setRightBarButtonItem(rightBarButtonItem);
         }
 
         if (Main.getAudioPlayer() != null) {
             Main.getAudioPlayer().stop();
         }
         Main.setAudioPlayer(AVAudioPlayer.audioPlayerWithContentsOfURL(NSURL.fileURLWithPath(songInfo.m4aPath), null));
         Main.getAudioPlayer().prepareToPlay();
         UIBarButtonItem playButton = new UIBarButtonItem(UIBarButtonSystemItem.Play, new UIBarButtonItemDelegate() {
             public void clicked() {
                 Main.getAudioPlayer().play();
                 Main.getAudioPlayer().setDelegate(new AVAudioPlayerDelegate() {
                     public void audioPlayerDidFinishPlaying(AVAudioPlayer player, boolean successfully) {
                         player.prepareToPlay();
                         setToolbarItems(new ArrayList<UIBarButtonItem>(buttonsPlay));
                     }
 
                     public void audioPlayerDecodeErrorDidOccur(AVAudioPlayer player, NSError error) {
                     }
 
                     public void audioPlayerBeginInterruption(AVAudioPlayer player) {
                     }
 
                     public void audioPlayerEndInterruption(AVAudioPlayer player) {
                     }
                 });
                 setToolbarItems(new ArrayList<UIBarButtonItem>(buttonsPauseStop));
 
             }
         });
         UIBarButtonItem pauseButton = new UIBarButtonItem(UIBarButtonSystemItem.Pause, new UIBarButtonItemDelegate() {
             public void clicked() {
                 Main.getAudioPlayer().pause();
                 setToolbarItems(new ArrayList<UIBarButtonItem>(buttonsPlayStop));
             }
         });
         UIBarButtonItem stopButton = new UIBarButtonItem(UIBarButtonSystemItem.Stop, new UIBarButtonItemDelegate() {
             public void clicked() {
                 Main.getAudioPlayer().stop();
                 Main.getAudioPlayer().setCurrentTime(0d);
                 Main.getAudioPlayer().prepareToPlay();
                 setToolbarItems(new ArrayList<UIBarButtonItem>(buttonsPlay));
             }
         });
        repeat=false;
        Main.getAudioPlayer().setNumberOfLoops(0);
        repeatButton = new UIBarButtonItem(UIImage.imageNamed("repeat.png"), UIBarButtonItemStyle.Plain, new UIBarButtonItemDelegate() {
             public void clicked() {
                if (!repeat) {
                     Main.getAudioPlayer().setNumberOfLoops(-1);
                     repeatButton.setImage(UIImage.imageNamed("no-repeat.png"));
                 } else {
                    Main.getAudioPlayer().setNumberOfLoops(0);
                     repeatButton.setImage(UIImage.imageNamed("repeat.png"));
                 }
                 repeat = !repeat;
             }
         });
         buttonsPlay = Arrays.asList(playButton, repeatButton);
         buttonsPauseStop = Arrays.asList(pauseButton, stopButton, repeatButton);
         buttonsPlayStop = Arrays.asList(playButton, stopButton, repeatButton);
         setToolbarItems(new ArrayList<UIBarButtonItem>(buttonsPlay));
     }
 }
