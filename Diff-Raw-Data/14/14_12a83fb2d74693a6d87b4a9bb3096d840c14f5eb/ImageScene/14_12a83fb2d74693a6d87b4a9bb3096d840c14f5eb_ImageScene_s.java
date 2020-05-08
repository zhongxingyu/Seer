 package dk.itu.spct;
 
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.mt4j.AbstractMTApplication;
 import org.mt4j.input.IMTInputEventListener;
 import org.mt4j.input.inputData.MTInputEvent;
 import org.mt4j.sceneManagement.AbstractScene;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.math.Vector3D;
 
 import dk.itu.spct.ImageDownloader.ImageListener;
 import dk.itu.spct.SceneUtilities.TapAction;
 
 public class ImageScene extends AbstractScene implements ImageListener {
 
   private ArrayList<TableImage> _images = new ArrayList<TableImage>();
   private TableImage _selectedImage = null;
   private AbstractMTApplication _application;
   private PaymentScene _paymentScene;
   private static Random _random = new Random();
 
   public ImageScene(AbstractMTApplication mtApplication, String name) {
     super(mtApplication, name);
     _application = mtApplication;
 
     this.setClearColor(new MTColor(66, 66, 66));
 
 //    addImageFromFile("images/smiling-cat.jpg");
 //    addImageFromFile("images/photo_cat2.jpg");
     @SuppressWarnings("unused")
     ImageDownloader imageDownloader = new ImageDownloader(this);
     addButtons();
   }
 
   private void addButtons() {
     int bottom = _application.getHeight();
     int right = _application.getWidth();
 
     SceneUtilities.addButton(this, "images/effect1Button.png", new Vector3D(
         right - 60, 35), new TapAction() {
       @Override
       public void onTap() {
         applyEffectToSelectedImage();
       }
     });
     SceneUtilities.addButton(this, "images/uploadButton.png", new Vector3D(110,
         bottom - 35), new TapAction() {
       @Override
       public void onTap() {
         upload();
       }
     });
     SceneUtilities.addButton(this, "images/paymentButton.png", new Vector3D(
         right - 110, bottom - 35), new TapAction() {
       @Override
       public void onTap() {
         showPaymentScene();
       }
     });
   }
 
   protected void showPaymentScene() {
     _application.pushScene();
     if (_paymentScene == null) {
       _paymentScene = new PaymentScene(_application, "Payment", this);
       _application.addScene(_paymentScene);
     }
     _application.changeScene(_paymentScene);
   }
 
   protected void upload() {
     new Thread(new Runnable() {
       @Override
       public void run() {
         ImageUploader uploader = new ImageUploader();
         for (TableImage image : _images)
           uploader.uploadImage(image);
       }
     }).start();
   }
 
   private void applyEffectToSelectedImage() {
     if (_selectedImage == null)
       return;
 
     BufferedImage before = _selectedImage.getBufferedImage();
     BufferedImage after = ImageProcessing.cropImage(before);
     _selectedImage.setBufferedImage(after);
   }
 
   private void addImageFromFile(String path) {
     TableImage image = new TableImage(_application, path, "joe");
     addTableImage(image);
   }
 
   private void addTableImage(TableImage image) {
     image.setWidthXYGlobal(400);
     int x = _random.nextInt(_application.getWidth() - 520) + 260;
     int y = _random.nextInt(_application.getHeight() - 520) + 260;
     image.setPositionGlobal(new Vector3D(x, y));
     _images.add(image);
     image.addInputListener(_imageInputListener);
     getCanvas().addChild(image);
   }
 
   public int getImageCountForNfcId(String id) {
     int count = 0;
     for (TableImage image : _images)
       if (image.getNfcId().equals(id))
         count++;
     return count;
   }
 
   public void removeImagesForNfcId(String id) {
     ArrayList<TableImage> newImageList = new ArrayList<TableImage>();
     for (TableImage image : _images) {
       if (image.getNfcId().equals(id))
         getCanvas().removeChild(image);
       else
         newImageList.add(image);
     }
   }
 
   private IMTInputEventListener _imageInputListener = new IMTInputEventListener() {
     @Override
     public boolean processInputEvent(MTInputEvent inEvt) {
       TableImage selectedImage = (TableImage) inEvt.getTarget();
       if (_selectedImage != selectedImage) {
         _selectedImage = selectedImage;
         highlightSelectedImage();
       }
       return false;
     }
 
     private void highlightSelectedImage() {
       for (TableImage image : _images) {
         MTColor color = null;
         if (image == _selectedImage)
           color = new MTColor(255, 255, 0);
         else
           color = new MTColor(255, 255, 255);
 
         image.setStrokeColor(color);
         image.setFillColor(color);
       }
 
     }
   };
 
   @Override
   public void imageDownloaded(final String filePath, final String nfcId) {
     _application.invokeLater(new Runnable() {
       @Override
       public void run() {
         TableImage tableImage = new TableImage(_application, filePath, nfcId);
         addTableImage(tableImage);
       }
     });
   }
 
 }
