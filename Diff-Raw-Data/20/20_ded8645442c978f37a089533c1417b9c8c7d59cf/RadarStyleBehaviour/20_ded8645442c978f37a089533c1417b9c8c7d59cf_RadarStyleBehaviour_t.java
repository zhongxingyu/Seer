 package com.solidstategroup.radar.web.behaviours;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.protocol.http.WebApplication;
 
/**
 * This class is no longer used. Static imports are being used instead on the base page as context path is resolved by
 * Wicket
 */
 public class RadarStyleBehaviour extends Behavior {
 
     private static final String CSS_PATH = "/css/";
 
     @Override
     public void renderHead(Component component, IHeaderResponse response) {
         super.renderHead(component, response);
     }
 }
