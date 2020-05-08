 /*
  * @(#)$Id$
  * Copyright 2003 Leonid Dubinsky
  */
 
 package org.podval.album;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.util.Date;
 
 import org.podval.imageio.Orientation;
 
 import java.text.SimpleDateFormat;
 
 
 /**
  * Represents a photograph, its attributes, and its scalings.
  */
 public abstract class Picture implements Comparable {
 
   /**
    * Looks up picture by the path.
    *
    * @param path
    *   string containing the path of the picture.
    *   Can not be <code>null</code>.
    *   Must contain ':', since it is a picture path.
    *
    * @return picture
    *   the picture at the specified path or <code>null</code>
    *   if there is no such picture
    */
   public static Picture getByPath(String path) {
     if (path == null)
       throw new NullPointerException("Path is null!");
 
    int colon = path.lastIndexOf(':');
 
     if (colon == -1)
       throw new IllegalArgumentException("No ':' in the picture path " + path);
 
     String albumPath = path.substring(0, colon);
     String pictureName = path.substring(colon+1, path.length());
 
     Album album = Album.getByPath(albumPath);
     return (album != null) ? album.getPicture(pictureName) : null;
   }
 
 
   /**
    * Sole constructor - for use by the subclasses.
    *
    * @param name
    *   string representing the name of this picture.
    *   Can not be <code>null</code> or empty.
    */
   protected Picture(String name) {
     if (name == null)
       throw new NullPointerException("Name is null!");
 
     if (name.equals(""))
       throw new IllegalArgumentException("Name is empty!");
 
     this.name = name;
   }
 
 
   /**
    * Returns this picture's name.
    *
    * @return
    *   string representing this picture's name
    */
   public final String getName() {
     return name;
   }
 
   /** @todo short name! */
 
   /**
    * Sets this picture's album to a specified value.
    *
    * @param value
    *   the album this picture belongs to.
    *   Can not be <code>null</code>.
    *   Type of this parameter is <code>AlbumLocal</code>,
    *   since only <code>AlbumLocal</code> can contain pictures.
    */
   public final void setAlbum(AlbumLocal value) {
     if (value == null)
       throw new NullPointerException("Album is null!");
 
     album = value;
   }
 
 
   /**
    * Gets this picture's album.
    *
    * @return
    *   the album this picture belongs to.
    *   Return type is <code>AlbumLocal</code>,
    *   since only <code>AlbumLocal</code> can contain pictures.
    *
    * @throws NullPointerException
    *   if this picture's album was not set
    */
   public final AlbumLocal getAlbum() {
     if (album == null)
       throw new NullPointerException("Album is not set!");
 
     return album;
   }
 
 
   /**
    * Gets this picture's path.
    *
    * @return
    *   string containing the path of this picture
    */
   public final String getPath() {
     return getAlbum().getPath() + ":" + getName();
   }
 
 
   /**
    * Sets this picture's title.
    *
    * @param value
    *   string specifying the title of this picture.
    *   Can be <code>null</code>.
    */
   public final synchronized void setTitle(String value) {
     load();
     if (((title == null) && (value != null)) || !title.equals(value)) {
       title = value;
       changed();
     }
   }
 
 
   /**
    * Gets this picture's title.
    *
    * @return
    *   string with this picture's title
    *   If no title was set for this picture, returns the result of
    *   {@link getDefaultTitle()}.
    */
   public final String getTitle() {
     load();
     return (title != null) ? title : getDefaultTitle();
   }
 
 
   /**
    * Compares this picture with the specified picture by timestamp.
    * Returns a negative integer, zero, or a positive integer when this picture
    * was taken earlier than, at the same time as, or later than the specified picture.
    * <p>
    */
   public final int compareTo(Object o) {
     Picture other = (Picture) o;
     return getDateTime().compareTo(other.getDateTime());
   }
 
 
   private static final SimpleDateFormat dateFormat =
     new SimpleDateFormat("M/d/y HH:mm:ss");
 
 
   /**
    * Returns this picture's timestamp as a string.
    *
    * @return
    *   string representation of this picture's timestamp, formatted
    *   as "M/d/y HH:mm:ss"
    */
   public final String getDateTimeString() {
     return dateFormat.format(getDateTime());
   }
 
 
   /**
    * Marks this picture - and it's album - as changed.
    */
   protected final void changed() {
     changed = true;
     getAlbum().changed(this);
   }
 
 
   /**
    * Returns this picture's default title.
    * Serves as the title if the title was not set.
    *
    * @return
    *   this picture's default title
    */
   protected abstract String getDefaultTitle();
 
 
   /**
    * Returns file containing this picture's thumbnail
    * or <code>null</code> if it is not available.<br>
    * Image size is 120 by 160 pixels.<br>
    * Image is oriented correctly in accordance with this
    * picture's orientation.
    *
    * @return
    *   file containing this picture's thumbnail
    *   or <code>null</code> if it is not available
    */
   public abstract File getThumbnailFile();
 
 
   /**
    * Returns file containing this picture's screen-sized image
    * or <code>null</code> if it is not available.<br>
    * Image size is 480 by 640 pixels.<br>
    * Image is oriented correctly in accordance with this
    * picture's orientation.
    *
    * @return
    *   file containing this picture's screen-sized image
    *   or <code>null</code> if it is not available
    */
   public abstract File getScreensizedFile();
 
 
   /**
    * Returns file containing this picture's full-sized image
    * or <code>null</code> if it is not available.<br>
    * Image is oriented correctly in accordance with this
    * picture's orientation.
    *
    * @return
    *   file containing this picture's full-sized image
    *   or <code>null</code> if it is not available
    */
   public abstract File getFullsizedFile();
 
 
   /**
    * Returns date and time when this picture was taken.
    *
    * @return
    *   date this picture was taken
    */
   public abstract Date getDateTime();
 
 
   /**
    * Returns this picture's orientation.
    *
    * @return
    *   this picture's orientation
    */
   public abstract Orientation getOrientation();
 
 
   /**
    * Rotates this picture to the right.
    * All scalings of the picture will be rotated appropriately on next request.
    */
   public abstract void rotateLeft();
 
 
   /**
    * Rotates this picture to the left.
    * All scalings of the picture will be rotated appropriately on next request.
    */
   public abstract void rotateRight();
 
 
   /**
    * Makes sure that this picture's attributes are loaded from the
    * persistent store.
    */
   protected abstract void load();
 
 
   /**
    * Saves changed attributes of this picture in a persistent store.
    */
   public abstract void save();
 
 
   private final String name;
 
 
   private AlbumLocal album;
 
 
   /**
    * Contains this picture's title - or <code>null</code> if it was not set.
    */
   protected String title;
 
 
   /**
    * Marks this picture as changed when <code>true</code>.<br>
    * INVARIANT: Before it changes to <code>true</true>,
    * <code>load()</code> invoked.
    */
   protected boolean changed;
 }
