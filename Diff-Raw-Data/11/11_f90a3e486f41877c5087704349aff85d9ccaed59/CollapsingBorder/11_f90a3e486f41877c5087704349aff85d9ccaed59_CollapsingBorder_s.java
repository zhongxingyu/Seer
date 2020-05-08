 package com.nitorcreations.wicket.components;
 
 import com.nitorcreations.wicket.util.AjaxUtil;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.border.Border;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.StringResourceModel;
 
 /**
  * A border that adds a ajax fallback toggling link for the content
  */
 public class CollapsingBorder extends Border {

    private  WebMarkupContainer contents;
 
     private final IModel<Boolean> contentVisible;
 
     public CollapsingBorder(String id) {
         this(id, false);
     }
 
     public CollapsingBorder(String id, boolean visibleByDefault) {
         super(id);
         this.contentVisible = Model.of(visibleByDefault);
         setOutputMarkupId(true);
 
        contents = new WebMarkupContainer("container") {
             @Override
             protected void onConfigure() {
                 super.onConfigure();
                 setVisibilityAllowed(contentVisible.getObject());
             }
         };
         addToBorder(contents.add(getBodyContainer()));
 
         addToBorder(new ContentToggleLink("toggleContent"));
     }
 
     public CollapsingBorder setContentVisible(boolean visible) {
         this.contentVisible.setObject(visible);
         return this;
     }
 
     private class ContentToggleLink extends AjaxFallbackLink<Void> {
         public ContentToggleLink(String id) {
             super(id);
 
             add(new Label("toggleText", new StringResourceModel("toggleText.${}", contentVisible)));
         }
 
         @Override
         public void onClick(AjaxRequestTarget target) {
             contentVisible.setObject(!contentVisible.getObject());
             AjaxUtil.add(target, CollapsingBorder.this);
         }
     }
 
 
 }
