 /*
  * Copyright 2011 Eike Kettner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.eknet.swing.uploadfield;
 
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
 import java.io.IOException;
import java.io.UnsupportedEncodingException;
 import java.net.URL;
import java.net.URLDecoder;
 
 import javax.swing.Icon;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /**
  * The value of the {@link UploadField} and {@link MultiUploadField} components.
  * <p/>
  * Only two fields are really set from user interaction, while the others are filled when
  * processing the input in the {@link UploadField}. The user can set the {@link #resource}
  * and {@link #name} fields. Then {@link PreviewHandler}s are processing the URL to create
  * a preview image, which is added to this value. Second, the input is stored to a file,
  * which may be temporary or not, depending on the selection. If a file from the local
  * file system is selected, this file is set. If the data is not coming from the local
  * file system (like from a http://... url), the data is stored to a temprary file. Then
  * to create a small preview image, a {@link Icon} is generated which is also stored in
  * this object.
  * <p/>
  * If no preview image could be generated, the image is set with a "missing image"
  * placeholder. Whether this is the case can be queried by {@link #isMissingImage()}.
  * 
  * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
  * @since 01.10.11 15:39
  */
 public final class UploadValue implements Cloneable, Comparable<UploadValue> {
 
   private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
 
   private URL resource;
   private String name;
 
   private BufferedImage image;
   private Icon icon;
   private File file;
 
   public UploadValue(URL resource, String name) {
     this.resource = resource;
     this.name = name;
   }
 
   public UploadValue(URL imageResource) {
     this.resource = imageResource;
   }
 
   public UploadValue(UploadValue value) {
     if (value != null) {
       this.name = value.getName();
       this.resource = value.getResource();
       this.image = value.getImage();
       this.icon = value.getIcon();
       this.file = value.getFile();
     }
   }
 
   public UploadValue() {
   }
 
   public URL getResource() {
     return resource;
   }
 
   public void setResource(URL resource) {
     URL old = getResource();
     this.resource = resource;
     changeSupport.firePropertyChange("imageResource", old, resource);
    if (resource != null) {
      try {
        setName(URLDecoder.decode(Utils.lastUrlPart(resource), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        //oops
        setName(Utils.lastUrlPart(resource));
      }
     }
     setImage(null);
     setFile(null);
     setIcon(null);
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     String old = getName();
     this.name = name;
     changeSupport.firePropertyChange("imageName", old, name);
   }
 
   public BufferedImage getImage() {
     return image;
   }
 
   public void setImage(@Nullable BufferedImage image) {
     BufferedImage old = getImage();
     this.image = image;
     changeSupport.firePropertyChange("image", old, image);
   }
 
   @NotNull
   public BufferedImage getScaledImage(int maxWidth, int maxHeight) throws IOException {
     final BufferedImage img = getImage();
     if (img == null) {
       throw new IllegalStateException("Cannot read image from url: " + resource);
     }
     return Scales.scaleIfNecessary(img, maxWidth, maxHeight);
   }
 
   @NotNull
   public BufferedImage getScaledImage(Dimension maxWidthAndHeight) throws IOException {
     return getScaledImage(maxWidthAndHeight.width, maxWidthAndHeight.height);
   }
 
   public boolean isMissingImage() {
     return getImage() == Utils.getMissingImage();
   }
 
   public File getFile() {
     return file;
   }
 
   public void setFile(@Nullable File file) {
     File old = getFile();
     this.file = file;
     changeSupport.firePropertyChange("imageFile", old, file);
   }
 
   public Icon getIcon() {
     return icon;
   }
 
   public void setIcon(@Nullable Icon icon) {
     Icon old = getIcon();
     this.icon = icon;
     changeSupport.firePropertyChange("icon", old, icon);
   }
 
   public void addPropertyChangeListener(PropertyChangeListener listener) {
     if (listener != null) {
       changeSupport.addPropertyChangeListener(listener);
     }
   }
 
   public void removePropertyChangeListener(PropertyChangeListener listener) {
     if (listener != null) {
       changeSupport.removePropertyChangeListener(listener);
     }
   }
 
   @Override
   public boolean equals(Object o) {
     if (this == o) return true;
     if (o == null || getClass() != o.getClass()) return false;
 
     UploadValue that = (UploadValue) o;
 
     if (name != null ? !name.equals(that.name) : that.name != null) return false;
     if (resource != null ? !resource.equals(that.resource) : that.resource != null) return false;
 
     return true;
   }
 
   @Override
   public int hashCode() {
     int result = resource != null ? resource.hashCode() : 0;
     result = 31 * result + (name != null ? name.hashCode() : 0);
     return result;
   }
 
   @Override
   public String toString() {
     return "ImageValue{" +
             "imageResource=" + resource +
             ", imageName='" + name + '\'' +
             ", image='" + (image != null) + '\'' +
             ", icon='" + (icon != null) + "'" +
             ", imageFile='" + (file != null) + "'" +
             '}';
   }
 
   @SuppressWarnings({"CloneDoesntCallSuperClone"})
   @Override
   public UploadValue clone() throws CloneNotSupportedException {
     return new UploadValue(this);
   }
 
   @Override
   public int compareTo(UploadValue o) {
     if (isNullOrEmpty(o)) {
       return -1;
     }
     if (isNullOrEmpty(this)) {
       return +1;
     }
     return resource.toString().compareTo(o.resource.toString());
   }
 
   public static boolean isNullOrEmpty(UploadValue value) {
     return value == null
             || value.getResource() == null
             && (value.getName() == null || "".equals(value.getName().trim()));
   }
 }
