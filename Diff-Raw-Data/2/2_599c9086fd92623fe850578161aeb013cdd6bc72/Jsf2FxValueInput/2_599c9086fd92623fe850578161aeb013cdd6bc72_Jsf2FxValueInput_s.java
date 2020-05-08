 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
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
 package com.flexive.faces.components.input;
 
 import com.flexive.faces.FxJsf2Const;
 import com.flexive.faces.FxJsf2Utils;
 import com.flexive.shared.value.FxValue;
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 import javax.faces.component.html.HtmlGraphicImage;
 import javax.faces.context.FacesContext;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * JSF2 implementation of fx:fxValueInput. Optional resource dependencies like TinyMCE are currently not
  * loaded automatically, until this issue is resolved use fx:includes to include them manually.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 @ResourceDependencies({
     @ResourceDependency(library=FxJsf2Const.RESOURCE_LIBRARY, name=FxJsf2Const.RESOURCE_CSS_COMPONENTS, target="head"),
    @ResourceDependency(library=FxJsf2Const.RESOURCE_LIBRARY, name=FxJsf2Const.RESOURCE_JS_COMPONENTS, target="head"),
 })
 public class Jsf2FxValueInput extends AbstractFxValueInput {
     private static final Log LOG = LogFactory.getLog(Jsf2FxValueInput.class);
 
     @Override
     public RenderHelper getRenderHelper(FacesContext context, FxValue value, boolean editMode) {
         return editMode
                 ? new Jsf2EditModeHelper(this, getClientId(context), value)
                 : new Jsf2ReadOnlyModeHelper(this, getClientId(context), value);
     }
 
     @Override
     protected void setPackagedImageUrl(HtmlGraphicImage imageComponent, String imagePath) {
         imageComponent.setUrl(
                 FxJsf2Utils.getResourceRequestPath(imagePath, FxJsf2Const.RESOURCE_LIBRARY)
         );
     }
 
 }
