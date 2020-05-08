 package com.lemoulinstudio.gfa.nb.screen;
 
 import com.lemoulinstudio.gfa.nb.GfaContext;
 import com.lemoulinstudio.gfa.nb.filetype.rom.RomDataNode;
 import com.lemoulinstudio.gfa.nb.filetype.rom.RomDataObject;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import org.openide.util.ImageUtilities;
 import org.openide.util.Lookup;
 import org.openide.windows.TopComponent;
 
 /**
  *
  * @author Vincent Cantin
  */
 public abstract class ScreenTopComponent extends TopComponent {
 
   private final RomDataObject dataObject;
   private PropertyChangeListener propertyChangeListener;
 
   public ScreenTopComponent(final RomDataObject dataObject) {
     this.dataObject = dataObject;
     
     this.propertyChangeListener = new PropertyChangeListener() {
       public void propertyChange(PropertyChangeEvent evt) {
         setName(dataObject.getName());
       }
     };
 
     dataObject.addPropertyChangeListener(propertyChangeListener);
 
     setName(dataObject.getName());
     setIcon(ImageUtilities.loadImage(RomDataNode.IMAGE_ICON_BASE, true));
   }
 
   public RomDataObject getDataObject() {
     return dataObject;
   }
 
   @Override
   public int getPersistenceType() {
     return TopComponent.PERSISTENCE_NEVER;
   }
 
   private static final String PREFERRED_ID = "ScreenTopComponent";
 
   @Override
   protected String preferredID() {
     return PREFERRED_ID;
   }
 
   @Override
  public Lookup getLookup() {
    return GfaContext.getLookup();
  }

  @Override
   protected void componentActivated() {
     GfaContext.setDelegateLookup(getDataObject().getLookup());
   }
 
   @Override
   protected void componentClosed() {
     if (GfaContext.getDelegateLookup() == dataObject.getLookup())
       GfaContext.setDelegateLookup(Lookup.EMPTY);
 
     dataObject.removePropertyChangeListener(propertyChangeListener);
 
     getDataObject().releaseResources();
   }
 
 }
