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
 package com.flexive.faces.components.content;
 
 import com.flexive.faces.FxJsfComponentUtils;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxValue;
 import com.flexive.shared.value.mapper.InputMapper;
 import com.flexive.shared.value.renderer.FxValueFormatter;
 import com.sun.faces.facelets.el.VariableMapperWrapper;
 import java.io.IOException;
 import javax.el.ExpressionFactory;
 import javax.el.ValueExpression;
 import javax.el.VariableMapper;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.view.facelets.FaceletContext;
 import javax.faces.view.facelets.TagAttribute;
 import javax.faces.view.facelets.TagConfig;
 import javax.faces.view.facelets.TagHandler;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * A facelets handler that renders a complete form element for editing or displaying
  * a property of a content instance provided by {@link FxContentView}. The output template
  * is provided by fxValueInputRow.xhtml (the component template of fx:fxValueInputRow).
  * A custom template can be specified with the <code>template</code> parameter.
  * 
  */
 public class Jsf2FxValueHandler extends TagHandler {
 
     private static final String TEMPLATE_ROOT = "META-INF/templates/";
     private String template = "fxValueInputRow.xhtml";
 
     public Jsf2FxValueHandler(TagConfig config) {
         super(config);
     }
 
     public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException {
         final String var;
         try {
             if (this.getAttribute("var") == null) {
                 // no variable name specified, use enclosing content view
                 if (!(parent instanceof FxContentView)) {
                     throw new FacesException("Facelet parent is no FxContentView instance and \"var\" attribute not specified.");
                 }
                 var = ((FxContentView) parent).getVar();
             } else {
                 // use a custom variable name
                 var = this.getAttribute("var").getValue(ctx);
             }
         } catch (FxRuntimeException e) {
             throw new FacesException("The fx:value component must be embedded in a fx:content instance.");
         }
         final boolean isNewValue = this.getAttribute("new") != null && Boolean.valueOf(this.getAttribute("new").getValue(ctx));
         final VariableMapper origMapper = ctx.getVariableMapper();
         final VariableMapperWrapper mapper = new VariableMapperWrapper(origMapper);
         try {
             ctx.setVariableMapper(mapper);
 
             // get property attribute
             final String property;
             final TagAttribute propertyAttribute = this.getAttribute("property");
             if (propertyAttribute != null) {
                 final ValueExpression propertyExpression = propertyAttribute.getValueExpression(ctx, String.class);
                 property = (String) propertyExpression.getValue(ctx);
             } else {
                 property = null;
             }
             FxJsfComponentUtils.requireAttribute("fx:value", "property", property);
 
             // assign id, label/labelKey and value based on the enclosing FxContentView instance
             final ExpressionFactory expressionFactory = ctx.getExpressionFactory();
             mapper.setVariable("id", expressionFactory.createValueExpression(
                     StringUtils.replace(property, "/", "_"), String.class));
             if (this.getAttribute("labelKey") == null) {
                 // use property label
                 mapper.setVariable("label", expressionFactory.createValueExpression(ctx, FxContentView.getExpression(var, property, "label"), FxString.class));
             } else {
                 // use provided message key
                 assignAttribute(ctx, mapper, "labelKey", String.class);
             }
             // retrieve content from content view
             mapper.setVariable("value", expressionFactory.createValueExpression(ctx,
                     FxContentView.getExpression(var, property, isNewValue ? "new" : null), FxValue.class));
 
             // passthrough other template attributes
             assignAttribute(ctx, mapper, "inputMapper", InputMapper.class);
             assignAttribute(ctx, mapper, "onchange", String.class);
             assignAttribute(ctx, mapper, "readOnly", Boolean.class);
             assignAttribute(ctx, mapper, "decorate", Boolean.class);
             assignAttribute(ctx, mapper, "filter", Boolean.class);
             assignAttribute(ctx, mapper, "forceLineInput", Boolean.class);
             assignAttribute(ctx, mapper, "valueFormatter", FxValueFormatter.class);
             assignAttribute(ctx, mapper, "containerDivClass", String.class);
             assignAttribute(ctx, mapper, "autocompleteHandler", String.class);
             assignAttribute(ctx, mapper, "disableMultiLanguage", Boolean.class);
             assignAttribute(ctx, mapper, "disableLytebox", Boolean.class);
             assignAttribute(ctx, mapper, "tooltip", String.class);
             assignAttribute(ctx, mapper, "tooltipKey", String.class);
 
             // TODO: cache templates/use a facelet ResourceResolver to encapsulate this
             ctx.includeFacelet(parent, Thread.currentThread().getContextClassLoader().getResource(TEMPLATE_ROOT + template));
         } finally {
             ctx.setVariableMapper(origMapper);
         }
     }
 
     private void assignAttribute(FaceletContext ctx, VariableMapper mapper, String name, Class<?> expectedClass) {
         final TagAttribute attribute = getAttribute(name);
         if (attribute != null) {
             mapper.setVariable(name, attribute.getValueExpression(ctx, expectedClass));
         }
     }
 
     /**
      * Sets the template name (relative to /WEB-INF/templates).
      *
      * @param template the template name (relative to /WEB-INF/templates).
      */
     public void setTemplate(String template) {
         this.template = template;
     }
 }
 
