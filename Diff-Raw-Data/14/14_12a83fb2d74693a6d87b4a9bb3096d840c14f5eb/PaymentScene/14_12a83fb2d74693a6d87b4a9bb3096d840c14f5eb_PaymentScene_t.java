 package dk.itu.spct;
 
 import org.mt4j.AbstractMTApplication;
 import org.mt4j.components.visibleComponents.widgets.MTTextArea;
 import org.mt4j.components.visibleComponents.widgets.buttons.MTImageButton;
 import org.mt4j.sceneManagement.AbstractScene;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.font.FontManager;
 import org.mt4j.util.font.IFont;
 import org.mt4j.util.math.Vector3D;
 
 import dk.itu.spct.SceneUtilities.TapAction;
 
 public class PaymentScene extends AbstractScene {
 
   private AbstractMTApplication _application;
   private MTTextArea _textArea;
   private NfcCoinWrapper _wrapper;
   private ImageScene _imageScene;
   private MTImageButton _okButton;
 
   private String _nfcId;
   private int _imageCount;
 
   public PaymentScene(AbstractMTApplication application, String name,
       ImageScene imageScene) {
     super(application, name);
     _application = application;
     _imageScene = imageScene;
     this.setClearColor(new MTColor(66, 66, 66));
     addControls();
 
     showText("Waiting for NFC card");
     setupCoinListener();
   }
 
   private void setupCoinListener() {
     _wrapper = new NfcCoinWrapper(new NfcCoinWrapper.INfcCardListener() {
       @Override
       public void cardConnected(String id, int availableAmount) {
         _nfcId = id;
         _imageCount = _imageScene.getImageCountForNfcId(_nfcId);
 
         showText("Hello " + id + ", you have " + availableAmount
            + " coin(s).\n\n" + "Picture(s): " + _imageCount + "\n" + "Price: "
             + _imageCount);
         enablePaymentButton(availableAmount >= _imageCount);
       }
 
       @Override
       public void cardDisconnected() {
         showText("Waiting for NFC card");
       }
     });
   }
 
   private void addControls() {
     int right = _application.getWidth();
     int bottom = _application.getHeight();
 
     addTextArea();
 
     SceneUtilities.addButton(this, "images/backButton.png", new Vector3D(60,
         bottom - 35), new TapAction() {
       @Override
       public void onTap() {
         _application.popScene();
       }
     });
     _okButton = SceneUtilities.addButton(this, "images/okButton.png",
         new Vector3D(right / 2, bottom - bottom / 3), new TapAction() {
           @Override
           public void onTap() {
             acceptPayment();
           }
         });
     _okButton.setEnabled(false);
   }
 
   protected void acceptPayment() {
     _wrapper.charge(_imageCount);
     _imageScene.removeImagesForNfcId(_nfcId);
    showText("Thank you!\nYou may remove your card.");
     enablePaymentButton(false);
     popSceneInASecond();
   }
 
   private void popSceneInASecond() {
     new Thread(new Runnable() {
       @Override
       public void run() {
         try {
          Thread.sleep(1500);
         } catch (InterruptedException e) {
         }
         _application.invokeLater(new Runnable() {
 
           @Override
           public void run() {
             _application.popScene();
           }
         });
       }
     }).start();
   }
 
   private void addTextArea() {
     IFont textFont = FontManager.getInstance().createFont(_application,
         "arial.ttf", 30, new MTColor(255, 255, 255));
     _textArea = new MTTextArea(_application, textFont);
     _textArea.setNoStroke(true);
     _textArea.setNoFill(true);
     _textArea.unregisterAllInputProcessors();
     _textArea.removeAllGestureEventListeners();
     getCanvas().addChild(_textArea);
   }
 
   private void showText(final String text) {
     _application.invokeLater(new Runnable() {
       @Override
       public void run() {
         _textArea.setText(text);
         _textArea.setPositionGlobal(new Vector3D(_application.getWidth() / 2,
             _application.getHeight() / 3));
       }
     });
   }
 
   private void enablePaymentButton(final boolean enable) {
     _application.invokeLater(new Runnable() {
       @Override
       public void run() {
         _okButton.setEnabled(enable);
       }
     });
   }
 }
