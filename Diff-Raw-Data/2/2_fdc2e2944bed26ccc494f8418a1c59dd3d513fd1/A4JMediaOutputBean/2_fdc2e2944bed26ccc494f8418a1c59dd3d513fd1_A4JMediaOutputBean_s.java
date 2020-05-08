 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010-2011, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.bean.a4j;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.imageio.ImageIO;
 
 import org.richfaces.component.UIMediaOutput;
 import org.richfaces.tests.metamer.Attributes;
 import org.richfaces.tests.metamer.bean.MediaData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Managed bean for a4j:mediaOutput.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 @ManagedBean(name = "a4jMediaOutputBean")
 @RequestScoped
 public class A4JMediaOutputBean implements Serializable {
 
     private static final long serialVersionUID = -1L;
     private static Logger logger;
     private Attributes attributes;
 
     /**
      * Initializes the managed bean.
      */
     @PostConstruct
     public void init() {
         logger = LoggerFactory.getLogger(getClass());
         logger.debug("initializing bean " + getClass().getName());
 
         attributes = Attributes.getComponentAttributesFromFacesConfig(UIMediaOutput.class, getClass());
 
         attributes.setAttribute("session", true);
         attributes.setAttribute("rendered", true);
         attributes.remove("createContent");
         attributes.remove("element");
         attributes.remove("value");
         attributes.remove("mimeType");
         attributes.remove("uriAttribute");
     }
 
     /**
      * Getter for attributes.
      * 
      * @return A map containing all attributes of tested component. Name of the component is key in the map.
      */
     public Attributes getAttributes() {
         return attributes;
     }
 
     /**
      * Setter for attributes.
      * 
      * @param attributes
      *            map containing all attributes of tested component. Name of the component is key in the map.
      */
     public void setAttributes(Attributes attributes) {
         this.attributes = attributes;
     }
 
     public void paint(OutputStream out, Object data) throws IOException {
         if (data instanceof MediaData) {
             MediaData paintData = (MediaData) data;
             BufferedImage img = new BufferedImage(paintData.getWidth(), paintData.getHeight(), BufferedImage.TYPE_INT_RGB);
             Graphics2D graphics2D = img.createGraphics();
             graphics2D.clearRect(0, 0, paintData.getWidth(), paintData.getHeight());
 
             graphics2D.setColor(Color.YELLOW);
             graphics2D.fillRect(0, 0, paintData.getWidth() / 2, paintData.getHeight() / 2);
 
             graphics2D.setColor(Color.RED);
             graphics2D.fillRect(paintData.getWidth() / 2, 0, paintData.getWidth() / 2, paintData.getHeight() / 2);
 
             graphics2D.setColor(Color.BLUE);
            graphics2D.fillRect(0, paintData.getWidth() / 2, paintData.getWidth() / 2, paintData.getHeight() / 2);
 
             graphics2D.setColor(Color.GREEN);
             graphics2D.fillRect(paintData.getWidth() / 2, paintData.getHeight() / 2, paintData.getWidth() / 2, paintData.getHeight() / 2);
 
             ImageIO.write(img, "png", out);
         }
     }
 
     private void copy(InputStream in, OutputStream out) throws IOException {
         byte[] buffer = new byte[2048];
         int read;
 
         while ((read = in.read(buffer)) != -1) {
             out.write(buffer, 0, read);
         }
     }
 
     public void paintFlash(OutputStream out, Object data) throws IOException {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
 
         if (loader == null) {
             loader = getClass().getClassLoader();
         }
 
         InputStream stream = loader.getResourceAsStream("resources/flash/text.swf");
 
         if (stream != null) {
             try {
                 copy(stream, out);
             } finally {
                 stream.close();
             }
         }
     }
 }
