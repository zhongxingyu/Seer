 package com.beckproduct.quilt.domain;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: jbeck
  * Date: Mar 16, 2009
  * Time: 2:54:14 PM
  *
  * The Quilt - The object that contains the all the tiles
  * <p>
  *
  * @hibernate.class table="quilt" lazy="false"
  */
 public class Quilt extends JPanel
 {
     private String id;
 
    public LinkedHashSet tiles;
 
     public Quilt(LayoutManager layoutManager)
     {
         super(layoutManager);
        this.tiles = new LinkedHashSet();
     }
 
     public Component add(Component component)
     {
         tiles.add(component);
         super.add(component);
         return component;
     }
 
     /**
      * @return Returns the id.
      * @hibernate.id generator-class="uuid.hex"
      */
     public String getId()
     {
         return id;
     }
 
     /**
      * @param id
      *            The Id to set.
      */
     public void setId(String id)
     {
         this.id = id;
     }
 
     /**
     * @hibernate.set name="tiles" lazy="false" cascade="all" order-by="time"
     * @hibernate.collection-key column="quilt_id"
     * @hibernate.collection-one-to-many class="com.beckproduct.quilt.domain.Quilt"
     *
     * @return Returns the tiles.
      */
    public LinkedHashSet getTiles()
     {
         return tiles;
     }
 
    public void setTiles(LinkedHashSet tiles)
     {
         this.tiles = tiles;
     }
 }
