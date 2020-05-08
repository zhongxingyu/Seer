 package com.solidstategroup.radar.web.behaviours;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.protocol.http.WebApplication;
 
 public class RadarStyleBehaviour extends Behavior {
 
     private static final String CSS_PATH = "/css/";
 
     @Override
     public void renderHead(Component component, IHeaderResponse response) {
         super.renderHead(component, response);

        // Render these like this so any paths we have still renderTemplate CSS URLs correctly
        response.renderCSSReference(CSS_PATH + "normal.css");
        response.renderCSSReference(CSS_PATH + "layout.css");
        response.renderCSSReference(CSS_PATH + "ajxmenu.css");
        response.renderCSSReference(CSS_PATH + "admin.css");
        response.renderCSSReference(CSS_PATH + "tabs_lab.css");
     }
 }
