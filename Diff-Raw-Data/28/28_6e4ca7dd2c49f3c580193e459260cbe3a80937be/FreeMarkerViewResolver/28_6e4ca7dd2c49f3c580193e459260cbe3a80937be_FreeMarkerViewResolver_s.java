 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.web;
 
 /** View resolver that, by default, uses katari's FreemarkerView class.
  */
 public class FreeMarkerViewResolver extends
     org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver {
 
   /** Sets the view name to katari's FreemarkerView.
	 */
   @Override
	protected Class requiredViewClass() {
		return FreeMarkerView.class;
	}
 }
 
