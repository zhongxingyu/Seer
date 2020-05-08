 package org.motechproject.carereporting.web.chart.builder;
 
 public class MouseBuilder extends ParamsBuilder {
 
     private static final String PARAM_TRACK = "track";
     private static final String PARAM_RELATIVE = "relative";
 
     public MouseBuilder track(boolean track) {
         return (MouseBuilder) param(PARAM_TRACK, track);
     }
 
     public MouseBuilder relative(boolean relative) {
         return (MouseBuilder) param(PARAM_RELATIVE, relative);
     }
 }
