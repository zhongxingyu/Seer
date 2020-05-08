 package com.yohostudios.theater.play;
 
 import java.util.Collections;
 import java.util.List;
 
 import android.graphics.Canvas;
 
 import com.yohostudios.theater.AbstractXMLObject;
 import com.yohostudios.theater.exception.XMLParsingException;
 import com.yohostudios.theater.util.FileUtils;
 
 /**
  * A container for Actors represented on screen.
  * 
  * @author simon
  * 
  */
 public class Scene extends AbstractXMLObject {
 
     /** List of Actor for the Scene */
     private List<Actor> actors;
     /** is the scene visible or not */
     private boolean visible;
     /** should the scene open with an effect */
     private boolean openingWithEffect;
     /** should the scene close with an effect */
     private boolean closingWithEffect;
 
     /**
      * Default constructor.
      */
     public Scene() {
     }
 
     /**
      * Constructs a Scene using XML data from a resource file.
      * 
      * @param resourceFileName the filename of the resource file.
      */
     public Scene(String resourceFileName) {
         setXmlData(FileUtils.getFileContentAsString(resourceFileName));
         setId(1); // TODO implement
         setActors(getObjectListFromXML(Actor.class, getXmlData()));
     }
 
     /*
      * Loads and parses an XML file to create a Scene.
      * 
      * private void loadScene() {
      * 
      * initFromXML(getXmlData()); // TODO implement this }
      */
 
     /**
      * Draws actors in the room.
      * @param canvas the new canvas
      */
     public void draw(Canvas canvas) {
         // TODO implement this
     }
 
     /**
      * Sort actors by their Z coordinate (ascending or descending).
      * 
      * @param ascending is true, descending is false.
      */
     public void orderActorsFromZ(boolean ascending) {
 
         if (ascending) {
             Collections.sort(actors, Actor.getZComparator());
         } else {
             Collections.sort(actors,
                     Collections.reverseOrder(Actor.getZComparator()));
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.yohostudios.theater.AbstractXMLObject#initFromXML(java.lang.String)
      */
     @Override
     public void initFromXML(String xmlString) throws XMLParsingException {
 
         setId(Long.parseLong(getValueFromParam(xmlString, "id")));
         setVisible(Boolean
                 .parseBoolean(getValueFromParam(xmlString, "visible")));
         setOpeningWithEffect(Boolean.parseBoolean(getValueFromParam(xmlString,
                 "openingWithEffect")));
         setClosingWithEffect(Boolean.parseBoolean(getValueFromParam(xmlString,
                 "closingWithEffect")));
         setActors(getObjectListFromXML(Actor.class, xmlString));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.yohostudios.theater.AbstractXMLObject#modifyAttribute(java.lang.String
      * , java.lang.String)
      */
     @Override
     public void modifyAttribute(String attributeName, String attributeValue) {
         // TODO Auto-generated method stub
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.yohostudios.theater.AbstractXMLObject#getAttribute(java.lang.String)
      */
     @Override
     public String getAttribute(String attributeName) {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.yohostudios.theater.AbstractXMLObject#callMethod(java.lang.String,
      * java.lang.String)
      */
     @Override
     public void callMethod(String attributeName, String attributeValue) {
         // TODO Auto-generated method stub
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.yohostudios.theater.AbstractXMLObject#freeResources()
      */
     @Override
     public void freeResources() {
         // TODO Auto-generated method stub
 
     }
 
     // ////////////////////////////////////////////////////////////////////////
     // Getters and setters.
     // ////////////////////////////////////////////////////////////////////////
 
     /**
      * @return access values List Actors
      */
     public List<Actor> getActors() {
         return actors;
     }
 
     /**
      * @param actors taint or change List Actors
      */
     public void setActors(List<Actor> actors) {
         this.actors = actors;
     }
 
     /**
      * @return access values If the Actors is Visible
      */
     public boolean isVisible() {
         return visible;
     }
 
     /**
      * @param visible taint or change If the Actors is Visible
      */
     public void setVisible(boolean visible) {
         this.visible = visible;
     }
 
    /**
     * @return true if the scene is opening with an effect, otherwise false.
     */
     public boolean isOpeningWithEffect() {
         return openingWithEffect;
     }
 
    /**
     * @param openingWithEffect true if the scene is opening with an effect,
     *            otherwise false.
     */
     public void setOpeningWithEffect(boolean openingWithEffect) {
         this.openingWithEffect = openingWithEffect;
     }
 
    /**
     * 
     * @return true if the scene is closing with an effect, otherwise false.
     */
     public boolean isClosingWithEffect() {
         return closingWithEffect;
     }
 
    /***
     * @param closingWithEffect true if the scene is closing with an effect,
     *            otherwise false.
     */
     public void setClosingWithEffect(boolean closingWithEffect) {
         this.closingWithEffect = closingWithEffect;
     }
 
 }
