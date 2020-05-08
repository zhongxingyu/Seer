 package gutenberg.collect;
 import java.awt.image.BufferedImage;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Hashtable;
 
 import javax.imageio.ImageIO;
 
 import com.google.zxing.Binarizer;
 import com.google.zxing.BinaryBitmap;
 import com.google.zxing.DecodeHintType;
 import com.google.zxing.LuminanceSource;
 import com.google.zxing.Reader;
 import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
 import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
 import com.google.zxing.common.GlobalHistogramBinarizer;
 import com.google.zxing.common.HybridBinarizer;
 import com.google.zxing.qrcode.QRCodeReader;
 
 public class Detect extends Task {
 
     public Detect(Path bankroot, String filter) {
         super(bankroot, filter);
         this.workingDir = bankroot.resolve(SCANTRAY);
         this.staging = bankroot.resolve(STAGING);
     }
 
     @Override
     protected void execute(Path file) throws Exception {
         
        String orientation;
         Result result; 
         Path target;
         
         result = decode(file);            
         if (result != null) {
            orientation = (String)result.getResultMetadata().
                get(ResultMetadataType.ORIENTATION);
             target = staging.resolve(result.getText().replace(" ",""));
            if (orientation.equals("1")) flip(file, file);
         } else {
             target = staging.resolve(file.getFileName());
         }
         
         if (!Files.exists(target))
             resize(file, target, 600, 800);
         
         Files.delete(file);
     }
 
     private Result decode(Path scan) throws Exception {
 
         BufferedImage img = null, subImg = null;        
         Result result = null;
         Path subImgPath, workingImgPath;
         
         boolean flipped = false;
         while (true) {
             
             img = ImageIO.read(scan.toFile());
             int third = (int)(img.getWidth()/3.0);            
             subImg = img.getSubimage(2*third, 0, third, (int)img.getHeight());
             
             subImgPath = scan.resolveSibling(scan.getFileName() + CROPPED);
             workingImgPath = scan.resolveSibling(scan.getFileName() + WORKING);            
             ImageIO.write(subImg, IMG_FORMAT, Files.newOutputStream(subImgPath));
             
             //{original, 150dpi, 200dpi, 125dpi, 100dpi}
             int[] widths = {third, 425, 566, 333, 283};
             for (int width : widths) {
                 int height;
                 if (width != third) {
                     height = (int)(subImg.getHeight()*1.0/subImg.getWidth()*width);
                     resize(subImgPath, workingImgPath, width, height);
                 } else {
                     ImageIO.write(subImg, IMG_FORMAT, Files.newOutputStream(workingImgPath));
                 }
                 subImg = ImageIO.read(Files.newInputStream(workingImgPath));
                 result = decode(subImg);
                 
                 if (result != null) {
                    result.putMetadata(ResultMetadataType.ORIENTATION, 
                        flipped ? "1" : "0");
                     break;
                 }            
             }
             
             if (result == null) {
                 if (flipped) {
                     break;//give up
                 } else {
                     flip(scan, scan);
                     flipped = true;
                 }
             } else {
                 break;
             }
         }
         
         Files.delete(subImgPath);
         Files.delete(workingImgPath);
         return result;
     }
     
     private Result decode(BufferedImage subImg) throws Exception {
         
         ZXingConfig[] configs = new ZXingConfig[2];
         LuminanceSource source = null;
         BinaryBitmap bitmap = null;
         Binarizer binarizer = null;
         bitmap = null;
         Hashtable<DecodeHintType, Boolean> hints = null;
         Reader reader = null;
         Result result = null;
             
         hints = new Hashtable<DecodeHintType, Boolean>();
         hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                     
         source = new BufferedImageLuminanceSource(subImg);
         
         configs[0] = new ZXingConfig();
         configs[0].binarizer = new GlobalHistogramBinarizer(source);
         configs[0].reader = new QRCodeReader();
 
         configs[1] = new ZXingConfig();
         configs[1].binarizer = new HybridBinarizer(source);
         configs[1].reader = new QRCodeReader();
         
         for (int i = 0; i < configs.length; i++) {
             
             binarizer = configs[i].binarizer;
             bitmap = new BinaryBitmap(binarizer);
             reader = configs[i].reader;
             try {
                 result = reader.decode(bitmap, hints);
                 break;
             } catch (Exception e) { }
         }
         return result;
     }    
     
     private void resize(Path src, Path target, int width, int height) 
         throws Exception { 
         String resize = String.format(CMD_RESIZE, 
             bankroot.relativize(src), width, height, 
             bankroot.relativize(target));
         exec(bankroot, resize);
     }
     
     private void flip(Path src, Path target) throws Exception {
         String rotate = String.format(CMD_ROTATE,
             bankroot.relativize(src), 
             bankroot.relativize(target));
         exec(bankroot, rotate);
     }    
 
     private Path staging;
     private final String 
         CMD_RESIZE = "convert %s -type TrueColor -resize %sx%s %s",
         CMD_ROTATE = "convert %s -type TrueColor -rotate 180 %s",
         IMG_FORMAT = "JPG";

 }
 
 class ZXingConfig {
 
     public Binarizer binarizer;
     public Reader    reader;
 
 }
