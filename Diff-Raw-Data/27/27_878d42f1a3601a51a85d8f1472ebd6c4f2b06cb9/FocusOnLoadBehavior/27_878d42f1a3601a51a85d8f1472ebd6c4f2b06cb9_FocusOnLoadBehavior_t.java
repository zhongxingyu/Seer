 package com.madalla.wicket.form;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.IComponentBorder;
 import org.apache.wicket.Response;
 import org.apache.wicket.behavior.AbstractBehavior;
 
 public class FocusOnLoadBehavior extends AbstractBehavior{
 	private static final long serialVersionUID = 1L;
 
 		@Override
 		public void bind(Component component) {
 			super.bind(component);
 			component.setOutputMarkupId(true);
 			component.setComponentBorder(new IComponentBorder() {
 				private static final long serialVersionUID = 1L;
 
 				public void renderBefore(Component component) {
 				}
 
 				public void renderAfter(Component component) {
 					final Response response = component.getResponse();
 					response.write(
							"<script type=\"text/javascript\" language=\"javascript\">var elem = document.getElementById(\"" +
 							component.getMarkupId() +
							"\"); if (elem.offsetWidth) elem.focus();</script>");
 				}
 			});
 		}
 
 		@Override
 		public boolean isTemporary() {
 			return true;
 		}
 }
