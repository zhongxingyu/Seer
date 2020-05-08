 package org.motechproject.carereporting.web.chart.builder;
 
 public class MouseBuilder extends ParamsBuilder {
 
     private static final String PARAM_TRACK = "track";
     private static final String PARAM_RELATIVE = "relative";
    private static final String PARAM_POSITION = "position";
 
     public MouseBuilder track(boolean track) {
         return (MouseBuilder) param(PARAM_TRACK, track);
     }
 
     public MouseBuilder relative(boolean relative) {
         return (MouseBuilder) param(PARAM_RELATIVE, relative);
     }

    public MouseBuilder position(String position) {
        return (MouseBuilder) param(PARAM_POSITION, position);
    }

 }
