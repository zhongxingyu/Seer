 /* 
  * Copyright 2009 IT Mill Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.vaadin.chronographer.gwt.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.vaadin.chronographer.gwt.client.connect.ChronoGrapherServerRpc;
 import org.vaadin.chronographer.gwt.client.model.HighlighDecorator;
 import org.vaadin.chronographer.gwt.client.model.TimelineBandInfo;
 import org.vaadin.chronographer.gwt.client.model.TimelineZone;
 import org.vaadin.chronographer.gwt.client.model.theme.TimelineTheme;
 import org.vaadin.chronographer.gwt.client.netthreads.BandInfo;
 import org.vaadin.chronographer.gwt.client.netthreads.BandOptions;
 import org.vaadin.chronographer.gwt.client.netthreads.ClientSizeHelper;
 import org.vaadin.chronographer.gwt.client.netthreads.EventSource;
 import org.vaadin.chronographer.gwt.client.netthreads.HotZoneBandOptions;
 import org.vaadin.chronographer.gwt.client.netthreads.PointHighlightDecorator;
 import org.vaadin.chronographer.gwt.client.netthreads.PointHighlightDecoratorOptions;
 import org.vaadin.chronographer.gwt.client.netthreads.SpanHighlightDecorator;
 import org.vaadin.chronographer.gwt.client.netthreads.SpanHighlightDecoratorOptions;
 import org.vaadin.chronographer.gwt.client.netthreads.Theme;
 import org.vaadin.chronographer.gwt.client.netthreads.TimeLineWidget;
 
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.Window;
 
 public class ChronoGrapherWidget extends TimeLineWidget implements
         ResizeHandler {
     public static final String CLASSNAME = "timeline";
 
     private boolean inited = false;
     private int width;
     private int height;
 
     private ChronoGrapherServerRpc rpc;
 
     public ChronoGrapherWidget() {
         super();
         setStyleName(CLASSNAME);
     }
 
     public void init(String w, String h, boolean horizontal,
             List<TimelineBandInfo> bandInfos,
             List<TimelineTheme> timelineThemes, String eventsJson) {
         if (!inited) {
             extractPixelWidthAndHeights(w, h);
 
             Window.addResizeHandler(this);
             setHorizontalOrientation(horizontal);
 
             Theme theme = Theme.create();
             setStructure(bandInfos, theme);
             setThemes(timelineThemes);
 
             initialise(width, height);
             getTimeLine().addClickHandler(new EventClickHandler());
             inited = true;
         }
         if (eventsJson != null) {
             setEventsJson(eventsJson);
         }
         onResize(true);
     }
 
     private void extractPixelWidthAndHeights(String widthStr, String heightStr) {
         width = ClientSizeHelper.getClientWidth();
         height = ClientSizeHelper.getClientHeight();
 
         if (widthStr.endsWith("px")) {
             width = (int) Double.parseDouble(widthStr.substring(0,
                     widthStr.length() - 2).trim());
         } else if (widthStr.endsWith("%")) {
             int clientWidth = ClientSizeHelper.getClientWidth();
             int percentage = (int) Double.parseDouble(widthStr.substring(0,
                     widthStr.length() - 1).trim());
             width = (int) (clientWidth * (percentage / 100.0));
         }
 
         if (heightStr.endsWith("px")) {
             height = (int) Double.parseDouble(heightStr.substring(0,
                     heightStr.length() - 2).trim());
         } else if (heightStr.endsWith("%")) {
             int clientHeight = ClientSizeHelper.getClientHeight();
             int percentage = (int) Double.parseDouble(heightStr.substring(0,
                     heightStr.length() - 1).trim());
             height = (int) (clientHeight * (percentage / 100.0));
         }
     }
 
     public void setStructure(List<TimelineBandInfo> bandInfos, Theme theme) {
         for (TimelineBandInfo bi : bandInfos) {
             BandOptions options = createBandOptions(bi, theme);
             Integer syncWith = bi.getSyncWith();
             Boolean highligh = bi.getHighligh();
             Boolean overview = bi.getOverview();
 
             ArrayList zones = getBandHotZones();
             for (int j = 0; j < bi.getTimelineZones().size(); j++) {
                 zones.add(createZone(bi.getTimelineZones().get(j)));
             }
             options.setZones(zones);
 
             BandInfo bandInfo = BandInfo.createHotZone(options);
 
             if (syncWith != null) {
                 bandInfo.setSyncWith(syncWith);
             }
             if (highligh != null) {
                 bandInfo.setHighlight(highligh);
             }
             if (overview != null) {
                 bandInfo.setOverview(overview);
             }
 
             List<Object> decorators = createDecorators(theme, bi);
             bandInfo.setDecorators(decorators);
 
             getBandInfos().add(bandInfo);
         }
     }
 
     private List<Object> createDecorators(Theme theme, TimelineBandInfo bi) {
         List<Object> decorators = new ArrayList<Object>();
         for (int i = 0; i < bi.getHighlightDecorators().size(); i++) {
             HighlighDecorator deco = bi.getHighlightDecorators().get(i);
             if (deco.isPointDecorator()) {
                 PointHighlightDecoratorOptions decoOpt = PointHighlightDecoratorOptions
                         .create();
                 decoOpt.setDate(deco.getDate().toString());
                 if (deco.getColor() != null) {
                     decoOpt.setColor(deco.getColor());
                 }
                 if (deco.getOpacity() != null) {
                     decoOpt.setOpacity(deco.getOpacity());
                 }
                 decoOpt.setTheme(theme);
 
                 PointHighlightDecorator pdeco = PointHighlightDecorator
                         .create(decoOpt);
                 decorators.add(pdeco);
             } else {
                 SpanHighlightDecoratorOptions decoOpt = SpanHighlightDecoratorOptions
                         .create();
                 decoOpt.setStartDate(deco.getStartDate().toString());
                 if (deco.getEndDate() != null) {
                     decoOpt.setEndDate(deco.getEndDate().toString());
                 }
                 if (deco.getStartLabel() != null) {
                     decoOpt.setStartLabel(deco.getStartLabel());
                 }
                 if (deco.getEndLabel() != null) {
                     decoOpt.setEndLabel(deco.getEndLabel());
                 }
                 if (deco.getColor() != null) {
                     decoOpt.setColor(deco.getColor());
                 }
                 if (deco.getOpacity() != null) {
                     decoOpt.setOpacity(deco.getOpacity());
                 }
 
                 decoOpt.setTheme(theme);
                 SpanHighlightDecorator decos = SpanHighlightDecorator
                         .create(decoOpt);
                 decorators.add(decos);
             }
         }
         return decorators;
     }
 
     private HotZoneBandOptions createZone(TimelineZone timelineZone) {
         HotZoneBandOptions zone = HotZoneBandOptions.create();
         if (timelineZone.getStart() != null) {
             zone.setStart(timelineZone.getStart().toString());
         }
         if (timelineZone.getEnd() != null) {
             zone.setEnd(timelineZone.getEnd().toString());
         }
         if (timelineZone.getMagnify() != null) {
             zone.setMagnify(timelineZone.getMagnify().intValue());
         }
         if (timelineZone.getMultiple() != null) {
             zone.setMultiple(timelineZone.getMultiple().intValue());
         }
         if (timelineZone.getUnit() != null) {
             zone.setUnit(timelineZone.getUnit().ordinal());
         }
         return zone;
     }
 
     private BandOptions createBandOptions(TimelineBandInfo bi, Theme theme) {
         BandOptions options = BandOptions.create();
         EventSource eventSource = getEventSource();
         if (eventSource == null) {
             eventSource = EventSource.create();
             super.setEventSource(eventSource);
         }
 
         options.setTheme(theme);
         options.setEventSource(getEventSource());
         if (bi.getDate() != null) {
             options.setDate(bi.getDate());
         }
         if (bi.getIntervalPixels() != null) {
             options.setIntervalPixels(bi.getIntervalPixels());
         }
         if (bi.getIntervalUnit() != null) {
             options.setIntervalUnit(bi.getIntervalUnit().ordinal());
         }
         if (bi.getShowEventText() != null) {
             options.setShowEventText(bi.getShowEventText());
         }
         if (bi.getTimeZone() != null) {
             options.setTimeZone(bi.getTimeZone());
         }
         if (bi.getTrackGap() != null) {
             options.setTrackGap(bi.getTrackGap().floatValue());
         }
         if (bi.getTrackHeight() != null) {
             options.setTrackHeight(bi.getTrackHeight().floatValue());
         }
         if (bi.getWidth() != null) {
             options.setWidth(bi.getWidth());
         }
         return options;
     }
 
     public void setEventsJson(String eventsJson) {
         super.clearData();
         getEventSource().loadJSON(eventsJson);
     }
 
     public void setThemes(List<TimelineTheme> timelineThemes) {
         // TODO Auto-generated method stub
 
     }
 
     /**
      * Resize all components
      */
     private void onResize(boolean create) {
         if (width <= 0) {
             width = ClientSizeHelper.getClientWidth();
         }
         if (height <= 0) {
             height = ClientSizeHelper.getClientHeight();
         }
         if (create || getTimeLine() == null) {
             setWidth(Integer.toString(width) + "px");
             setHeight(Integer.toString(height) + "px");
             create();
         }
         layout();
     }
 
     /**
      * Resize all components
      */
     @Override
     public void onResize(ResizeEvent event) {
         onResize(true);
     }
 
     class EventClickHandler implements TimeLineClickHandler {
         @Override
         public void onClick(int idd, int x, int y) {
             rpc.onClick(idd, x, y);
         }
     }
 
     public void setRpc(ChronoGrapherServerRpc rpc) {
         this.rpc = rpc;
     }
 }
