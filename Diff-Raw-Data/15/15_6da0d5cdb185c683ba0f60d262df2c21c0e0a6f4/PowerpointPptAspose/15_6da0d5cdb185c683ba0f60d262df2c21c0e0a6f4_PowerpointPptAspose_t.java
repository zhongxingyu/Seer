 package com.itcarg.office.aspose.powerpoint;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Map;
 
 import com.aspose.slides.FillType;
 import com.aspose.slides.Paragraph;
 import com.aspose.slides.Paragraphs;
 import com.aspose.slides.Picture;
import com.aspose.slides.PictureFrame;
 import com.aspose.slides.Placeholder;
 import com.aspose.slides.Placeholders;
 import com.aspose.slides.PptImageException;
 import com.aspose.slides.Presentation;
 import com.aspose.slides.Shape;
 import com.aspose.slides.Shapes;
 import com.aspose.slides.Slide;
 import com.aspose.slides.Slides;
 import com.aspose.slides.TextHolder;
 import com.itcarg.office.aspose.BaseAspose;
 
 public class PowerpointPptAspose extends BaseAspose implements PowerpointHandler {
     private Presentation presentation;
 
     public PowerpointPptAspose(InputStream slidesStream) {
         presentation = new Presentation(slidesStream);
     }
 
     private Presentation getPresentation() {
         return presentation;
     }
 
     @Override
     public void replaceMap(Map<String, String> properties) throws Exception {
         Slides slides = getPresentation().getSlides();
         for (int i = 0; i < slides.size(); i++) {
             Slide slide = slides.get(i);
             Placeholders placeholders = slide.getPlaceholders();
             for (int j = 0; j < placeholders.size(); j++) {
                 Placeholder placeholder = placeholders.get(j);
                 if (placeholder instanceof TextHolder) {
                     TextHolder textHolder = (TextHolder) placeholder;
                     Paragraphs paragraphs = textHolder.getParagraphs();
                     for (int k = 0; k < paragraphs.size(); k++) {
                         Paragraph paragraph = paragraphs.get(k);
                         for (Map.Entry<String, String> entry : properties.entrySet()) {
                             paragraph.setText(paragraph.getText().replaceAll(
                                     "\\$" + entry.getKey(), entry.getValue()));
                         }
                     }
                 }
             }
         }
     }
 
     @Override
     public void saveAs(File filename) throws IOException {
         OutputStream out = null;
         try {
             out = new BufferedOutputStream(new FileOutputStream(filename));
             getPresentation().write(out);
         } finally {
             if (out != null) {
                 out.close();
             }
         }
     }
 
     public void replaceImages(Map<String, URL> images) throws PptImageException, IOException {
         Slides slides = getPresentation().getSlides();
         for (int i = 0; i < slides.size(); i++) {
             Slide slide = slides.get(i);
             for (Map.Entry<String, URL> entry : images.entrySet()) {
                 Shape shape = FindShape(slide, entry.getKey());
                 if (shape == null) {
                     continue;
                 }
                 Picture pic = new Picture(getPresentation(), new BufferedInputStream(entry
                         .getValue().openStream()));
                
                 int picId = getPresentation().getPictures().add(pic);
                if (shape instanceof PictureFrame) {
                    ((PictureFrame) shape).setPictureId(picId);
                } else {
                    shape.getFillFormat().setType(FillType.PICTURE);
                    shape.getFillFormat().setPictureId(picId);
                }
             }
         }
     }
 
     private Shape FindShape(Slide slide, String alttext) {
         // Iterating through all shapes inside the slide
         Shapes shapes = slide.getShapes();
         for (int i = 0; i < shapes.size(); i++) {
             // If the alternative text of the slide matches with the required
             // one then
             // return the shape
             Shape shape = shapes.get(i);
             log.info("shape alt text: {}, alttext: {}", shape.getAlternativeText(), alttext);
             if (shape.getAlternativeText().equals(alttext)) {
                 return shape;
             }
         }
         return null;
     }
 }
