 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.faces.components;
 
 import com.flexive.faces.FxJsfComponentUtils;
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.shared.XPathElement;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.war.servlet.ThumbnailServlet;
 
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIComponent;
 import javax.faces.component.html.HtmlGraphicImage;
 import javax.faces.component.html.HtmlOutputText;
 import javax.faces.context.FacesContext;
 import java.io.IOException;
 import java.util.Arrays;
 
 /**
  * A basic component for displaying thumbnails of content instances. Renders URLs
  * to the {@link com.flexive.war.servlet.ThumbnailServlet}.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class Thumbnail extends UIOutput {
     private FxPK pk;
     private FxBinary binary;
     private UIComponent body;
     private boolean urlOnly;
     private String previewSize = BinaryDescriptor.PreviewSizes.PREVIEW2.name();
 
     public Thumbnail() {
         setRendererType(null);
     }
 
     @Override
     public void encodeBegin(FacesContext facesContext) throws IOException {
         final String link;
         final BinaryDescriptor.PreviewSizes previewSize;
         try {
             previewSize = BinaryDescriptor.PreviewSizes.valueOf(getPreviewSize().toUpperCase());
         } catch (IllegalArgumentException e) {
             throw new IllegalArgumentException("Invalid preview size: " + getPreviewSize()
                     + ", possible values are: " + Arrays.toString(BinaryDescriptor.PreviewSizes.values()));
         }
         if (getPk() != null) {
             link = ThumbnailServlet.getLink(getPk(), previewSize);
         } else if (getBinary() != null) {
             link = ThumbnailServlet.getLink(XPathElement.getPK(getBinary().getXPath()),
                     previewSize,
                     getBinary().getXPath());
         } else {
             throw new FxInvalidParameterException("pk", "ex.jsf.thumbnail.empty").asRuntimeException();
         }
         if (isUrlOnly()) {
             body = FxJsfUtils.addChildComponent(this, HtmlOutputText.COMPONENT_TYPE);
             ((HtmlOutputText) body).setEscape(false);
             ((HtmlOutputText) body).setValue(link.substring(1));    // don't return absolute URLs to the client
         } else {
             body = FxJsfUtils.addChildComponent(this, HtmlGraphicImage.COMPONENT_TYPE);
             ((HtmlGraphicImage) body).setUrl(link);
         }
     }
 
     @Override
     public void encodeEnd(FacesContext facesContext) throws IOException {
         getChildren().remove(body);
     }
 
     public FxPK getPk() {
         if (pk == null) {
             return (FxPK) FxJsfComponentUtils.getValue(this, "pk");
         }
         return pk;
     }
 
     public void setPk(FxPK pk) {
         this.pk = pk;
     }
 
     public FxBinary getBinary() {
         if (binary == null) {
             return (FxBinary) FxJsfComponentUtils.getValue(this, "binary");
         }
         return binary;
     }
 
     public void setBinary(FxBinary binary) {
         this.binary = binary;
     }
 
     public boolean isUrlOnly() {
         return FxJsfComponentUtils.getBooleanValue(this, "urlOnly", urlOnly);
     }
 
     public void setUrlOnly(boolean urlOnly) {
         this.urlOnly = urlOnly;
     }
 
     public String getPreviewSize() {
         return FxJsfComponentUtils.getStringValue(this, "previewSize", previewSize);
     }
 
     public void setPreviewSize(String previewSize) {
         this.previewSize = previewSize;
     }

    @Override
    public Object saveState(FacesContext context) {
        final Object[] state = new Object[5];
        state[0] = super.saveState(context);
        state[1] = pk;
        state[2] = binary;
        state[3] = urlOnly;
        state[4] = previewSize;
        return state;
    }

    @Override
    public void restoreState(FacesContext context, Object oState) {
        final Object[] state = (Object[]) oState;
        super.restoreState(context, state[0]);
        pk = (FxPK) state[1];
        binary = (FxBinary) state[2];
        urlOnly = state[3] != null && (Boolean) state[3];
        previewSize = (String) state[4];
    }
 }
