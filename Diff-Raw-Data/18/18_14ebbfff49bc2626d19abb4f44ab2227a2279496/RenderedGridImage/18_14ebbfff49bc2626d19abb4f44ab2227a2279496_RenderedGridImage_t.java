 package de.hswt.hrm.plant.ui;
 
 import java.awt.Image;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.List;
 
 import org.ghost4j.document.DocumentException;
 import org.ghost4j.document.PSDocument;
 import org.ghost4j.renderer.RendererException;
 import org.ghost4j.renderer.SimpleRenderer;
 
 import de.hswt.hrm.plant.model.PSGridImage;
 
 /**
  * This is a rendered version of PSGridImage. The PostScript is converted to an image object by
  * ghost4j.
  * 
  * @author Michael Sieger
  * 
  */
 public class RenderedGridImage {
 
     private static final int DPI = 300;
 
    private PSGridImage gridImage;
     private Image renderedImage;
 
    public RenderedGridImage(PSGridImage gridImage) throws IOException, RendererException,
             DocumentException, NotSinglePageException {
        this.gridImage = gridImage;
         PSDocument doc = new PSDocument();
        doc.load(new ByteArrayInputStream(gridImage.getPostScript().getBytes("UTF-8")));
         SimpleRenderer ren = new SimpleRenderer();
         ren.setResolution(DPI);
         List<Image> renderResult = ren.render(doc);
         if (renderResult.size() != 1) {
             throw new NotSinglePageException();
         }
         renderedImage = renderResult.get(0);
     }
 
    public PSGridImage getSchemeGrid() {
        return gridImage;
     }
 
     public Image getRenderedImage() {
         return renderedImage;
     }
 
 }
