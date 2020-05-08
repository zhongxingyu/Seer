 package com.googlecode.wicketelements.components.module;
 
 import com.googlecode.jbp.common.requirements.ParamRequirements;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.markup.html.CSSPackageResource;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.border.Border;
 import org.apache.wicket.markup.html.link.AbstractLink;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 
 public class Module extends Border {
     private boolean expanded = true;
     private WebMarkupContainer content;
 
     public Module(final String id, final IModel<?> titleModelParam) {
         super(id, titleModelParam);
         ParamRequirements.INSTANCE.requireNotNull(titleModelParam, "A module must have a title model.  Parameter 'titleModelParam' must not be null.");
         init(titleModelParam);
     }
 
     private void init(final IModel<?> titleModelParam) {
         add(CSSPackageResource.getHeaderContribution(Module.class,
                 "Module.css", "screen, projection"));
         setOutputMarkupId(true);
         content = new WebMarkupContainer("content");
         content.add(getBodyContainer());
         add(content);
         final AbstractLink headerLink = new AjaxFallbackLink("header") {
             @Override
             public void onClick(final AjaxRequestTarget target) {
                 expanded = !expanded;
                 target.addComponent(Module.this);
             }
         };
         //do not use setVisible to hide or show the body, as hidden components in forms would not use validation anymore.
         content.add(new AttributeAppender("class", true, new AbstractReadOnlyModel<Object>() {
             @Override
             public Object getObject() {
                 return expanded ? "displayBlock" : "displayNone";
             }
        }, " "));
         headerLink.add(new Label("title", titleModelParam));
         add(headerLink);
     }
 
     public boolean isExpanded() {
         return expanded;
     }
 
     public void setExpanded(final boolean expandedParam) {
         expanded = expandedParam;
     }
 }
