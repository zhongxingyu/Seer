 package chum.gl;
 
 import chum.gl.render.Sprite;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import android.graphics.Bitmap;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 
 /**
  * A SpriteSheet is a special form of {@link Texture} containing {@link Sprite}
  * images, as well as info defining the locations of each sprite image.
  * 
  * SpriteSheet is used both to create the sprites (using a certain index into
  * the sheet), as well drawing the sprites (by having the sprites reference into
  * the sheet)
  */
 public class SpriteSheet extends Texture {
 
     /** The location of each Sprite on the sheet */
     public ImageData[] data;
 
     /** The width of the texture image, in pixels */
     public int width;
 
     /** The height of the texture image, in pixels */
     public int height;
 
 
     /**
      * Create a new SpriteSheet
      */
     public SpriteSheet(RenderContext renderContext) {
         this(renderContext, 1);
     }
 
 
     /**
      * Create a new SpriteSheet sized for a specific number of sprites
      * 
      * @param count
      *            the number of sprites / ImageData to be defined on the sheet
      */
     public SpriteSheet(RenderContext renderContext, int count) {
         super(renderContext, 1);
         this.data = new ImageData[count];
     }
 
     
     /**
      * Create a new SpriteSheet sized for a specific number of sprites
      * 
      * @param count
      *            the number of sprites / ImageData to be defined on the sheet
      */
     public SpriteSheet(RenderContext renderContext, int count, ImageProvider provider) {
         this(renderContext, count);
         setProvider(provider);
         load(renderContext.gl10);
     }
 
 
     @Override
     public Bitmap getBitmap(int num) {
         Bitmap bmp = super.getBitmap(num);
         this.width = bmp.getWidth();
         this.height = bmp.getHeight();
         return bmp;
     }
 
     
     /**
      * Define the location of a sprite on the sheet
      */
     public SpriteSheet.ImageData define(int index, int left, int top, int right,
             int bottom) {
 
         // Increase the storage for the ImageData if needed
         if (data.length < index + 1) {
             ImageData[] old = data;
             data = new ImageData[old.length * 2];
             for (int i = 0; i < old.length; ++i)
                 data[i] = old[i];
         }
 
         data[index] = new ImageData(this, left, top, right, bottom, this.width,
                                     this.height);
         return data[index];
     }
 
 
     /**
      * Location of each sprite image on a SpriteSheet
      */
     public static class ImageData {
 
         /** The SpriteSheet this corresponds to */
         public SpriteSheet sheet;
 
         /** left edge location, in pixels */
         public int left;
 
         /** top edge location, in pixels */
         public int top;
 
         /** right edge location, in pixels */
         public int right;
 
         /** bottom edge location, in pixels */
         public int bottom;
 
         /** left edge location, in texels */
         public float u1;
 
         /** top edge location, in texels */
         public float v1;
 
         /** right edge location, in texels */
         public float u2;
 
         /** bottom edge location, in texels */
         public float v2;
 
 
         public ImageData(SpriteSheet sheet, int left, int top, int right, int bottom,
                 int sheetWidth, int sheetHeight) {
             this.sheet = sheet;
             this.left = left;
             this.top = top;
             this.right = right;
             this.bottom = bottom;
             updateTexels(sheetWidth, sheetHeight);
         }
 
 
         protected void updateTexels(int width, int height) {
             if (width == 0 || height == 0) {
                 u1 = v1 = u2 = v2 = 0;
                 return;
             }
 
             float w = width;
             float h = height;
 
             u1 = left / w;
             v1 = bottom / h;
             u2 = right / w;
             v2 = top / h;
         }
 
     }
 
     
     /**
        Helper to load sprite sheet data from file(s).
     
        @author jeremy
      */
     public static class Loader {
         
         RenderContext renderContext;
         
         public Loader(RenderContext renderContext) {
             this.renderContext = renderContext;
         }
         
         
         /**
            Create a SpriteSheet using the specified atlas image, and the image location
            data read from the specified xml file.
            
            The XML file has this format:
         
              <sheet>
                <sprite name="circle" x="0" y="0" width="128" height="128" />
                ...
              </sheet>
          */
         public SpriteSheet loadFromAssets(String imgPath, String xmlPath) {
             Texture.AssetProvider provider = new Texture.AssetProvider(imgPath);
             
             Document xml = openXMLAsset(xmlPath);
             NodeList spriteList = xml.getElementsByTagName("sprite");
 
             SpriteSheet sheet = new SpriteSheet(renderContext,spriteList.getLength(),provider);
             
             for(int i=0; i < spriteList.getLength(); ++i) {
                 Element elem = (Element)spriteList.item(i);
                 int x = Integer.parseInt(elem.getAttribute("x"));
                 int y = Integer.parseInt(elem.getAttribute("y"));
                 int w = Integer.parseInt(elem.getAttribute("width"));
                 int h = Integer.parseInt(elem.getAttribute("height"));
                sheet.define(i,x,y,x+w,y+h);
             }
             
             return sheet;
         }
         
         
         
         protected Document openXMLAsset(String xmlPath) {
             // TODO: better error handling here -- raise meaningful errors instead of return null
             
             // Open the stream
             // -------------------------------------------------------------
             InputStream stream;
             try { stream = renderContext.appContext.getAssets().open(xmlPath); }
             catch (IOException e) {
                 chum.util.Log.e("Error loading "+xmlPath+": "+e);
                 return null;
             } 
 
             Document document = null;
             try {
                 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                 DocumentBuilder builder = factory.newDocumentBuilder();
                 document = builder.parse(stream);
             } catch(ParserConfigurationException e) {
                 chum.util.Log.e("Error loading "+xmlPath+": "+e);
                 return null;
             } catch(SAXException e) {
                 chum.util.Log.e("Error loading "+xmlPath+": "+e);
                 return null;
             } catch(IOException e) {
                 chum.util.Log.e("Error loading "+xmlPath+": "+e);
                 return null;
             }
 
             return document;
         }
     }
     
     
 }
