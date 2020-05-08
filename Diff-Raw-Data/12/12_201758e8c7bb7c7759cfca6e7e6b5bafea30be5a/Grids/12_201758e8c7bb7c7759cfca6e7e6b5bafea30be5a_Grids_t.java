 package it.qubixic.showcase;
 
 import it.qubixic.component.grid.Grid;
 import it.qubixic.component.grid.ListType;
 import it.qubixic.component.grid.layout.ComponentLayoutType;
 import it.qubixic.component.grid.thumbnail.Thumbnail;
 import it.qubixic.component.grid.thumbnail.ThumbnailCaption;
 import it.qubixic.showcase.utils.ImageUtils;
 import javax.microedition.lcdui.Image;
 import java.util.Vector;
 import javax.microedition.midlet.*;
 
 public class Grids extends BaseForm {
 
     private final String GRID_HORI_SAME_DIMENSIONS_COMPONENTS
             = "Horizontal Grid, Same component dimensions" ;
     
     public Grids(MIDlet midlet) {
         super ("Grid", midlet) ;
         appendGrid(createHorizontalGridSameComponentDimensions()) ;
     }
     
     public void appendGrid(Grid grid) {
         append(grid);
     }
     
     private Grid createHorizontalGridSameComponentDimensions() {
         Grid horizontalGridSameComponentDimensions = new Grid(GRID_HORI_SAME_DIMENSIONS_COMPONENTS, 
                 ListType.HORIZONTAL, ComponentLayoutType.SAME_DIMENSIONS) ;
         horizontalGridSameComponentDimensions.append(createThumbnailsSameDimensions());
         return horizontalGridSameComponentDimensions ;     
     }    
     
     private Vector createThumbnailsSameDimensions() {
         Vector thumbnails = new Vector() ;
         final int noOfThumbnails = 4 ;
         ThumbnailCaption thumbnailCaption ;
         Image thumbnailImage ;
             
        for (int thumbnailsCounter = 1 ;thumbnailsCounter < noOfThumbnails ;
                 thumbnailsCounter++) {
             thumbnailCaption = new ThumbnailCaption("Thumbnail " + thumbnailsCounter,
                     "Description here...") ;
             thumbnailImage = ImageUtils.loadJarImage("/" 
                     + thumbnailsCounter 
                     + "-100x100.png");
             thumbnails.addElement(new Thumbnail(thumbnailCaption, thumbnailImage));
         }
         return thumbnails ;
     }    
 }
