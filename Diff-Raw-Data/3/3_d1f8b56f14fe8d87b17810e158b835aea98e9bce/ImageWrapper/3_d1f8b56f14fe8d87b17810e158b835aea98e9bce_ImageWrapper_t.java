 /*
  * Copyright (C) 2012 Interactive Media Management
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package dk.i2m.drupal.fields.wrappers;
 
 import dk.i2m.drupal.core.FormAPIField;
 import dk.i2m.drupal.fields.Image;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 /**
  *
  * @author Raymond Wanyoike <rwa at i2m.dk>
  */
 public class ImageWrapper implements FormAPIField<Image> {
 
     private List<Image> images = new ArrayList<Image>();
 
     private String name;
 
     public ImageWrapper() {
     }
 
     public ImageWrapper(String name) {
         this.name = name;
     }
 
     public ImageWrapper(String name, String fid, String alt, String title) {
         this(name);
         images.add(new Image(fid, alt, title));
     }
 
     @Override
     public void add(Image field) {
         images.add(field);
     }
 
     @Override
     public Set<NameValuePair> setup(String language,
             Set<NameValuePair> nvps) {
         for (int i = 0; i < images.size(); i++) {
             Image image = images.get(i);
 
             if (image.getFid() == null) {
                 throw new IllegalArgumentException("fid cannot be null");
             }
 
             nvps.add(new BasicNameValuePair(name + "[" + language + "]" + "["
                     + i + "][fid]", image.getFid()));
 
            nvps.add(new BasicNameValuePair(name + "[" + language + "]" + "["
                    + i + "][display]", "1"));
            
             if (image.getAlt() != null) {
                 nvps.add(new BasicNameValuePair(name + "[" + language + "]"
                         + "[" + i + "][alt]", image.getAlt()));
             }
             
             if (image.getTitle() != null) {
                 nvps.add(new BasicNameValuePair(name + "[" + language + "]"
                         + "[" + i + "][title]", image.getTitle()));
             }
         }
 
         return nvps;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 }
