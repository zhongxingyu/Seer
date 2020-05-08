 /**
  * Copyright (C) 2011 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.ui.timeserious.frame;
 
 import com.od.jtimeseries.net.udp.TimeSeriesServerDictionary;
 import com.od.jtimeseries.ui.config.ConfigAware;
 import com.od.jtimeseries.ui.config.ConfigAwareTreeManager;
 import com.od.jtimeseries.ui.config.ConfigInitializer;
 import com.od.jtimeseries.ui.config.TimeSeriousConfig;
 import com.od.jtimeseries.ui.displaypattern.DisplayNameCalculator;
 import com.od.jtimeseries.ui.identifiable.DesktopContext;
 import com.od.jtimeseries.ui.timeserious.action.ApplicationActionModels;
 import com.od.jtimeseries.ui.timeserious.mainselector.MainSeriesSelector;
 import com.od.jtimeseries.ui.timeserious.rootcontext.TimeSeriousRootContext;
 import com.od.jtimeseries.util.identifiable.Identifiable;
 import com.od.jtimeseries.util.identifiable.IdentifiableTreeEvent;
 import com.od.jtimeseries.util.identifiable.IdentifiableTreeListener;
 import com.od.jtimeseries.util.identifiable.IdentifiableTreeListenerAdapter;
 import com.od.swing.util.AwtSafeListener;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 08/04/11
  * Time: 06:43
  */
 public class FrameManager implements ConfigAware {
 
     private TimeSeriousMainFrame mainFrame;
     private TimeSeriesServerDictionary udpPingHttpServerDictionary;
     private ApplicationActionModels applicationActionModels;
     private DisplayNameCalculator displayNameCalculator;
     private TimeSeriousRootContext rootContext;
     private ConfigAwareTreeManager configTreeManager;
     private ConfigInitializer configInitializer;
     private MainSeriesSelector mainSeriesSelector;
     private Map<DesktopContext, AbstractDesktopFrame> desktopContextToFrameMap = new HashMap<DesktopContext, AbstractDesktopFrame>();
 
     public FrameManager(TimeSeriesServerDictionary udpPingHttpServerDictionary,
                         ApplicationActionModels applicationActionModels,
                         DisplayNameCalculator displayNameCalculator,
                         TimeSeriousRootContext rootContext,
                         ConfigAwareTreeManager configTreeManager,
                         ConfigInitializer configInitializer) {
         this.udpPingHttpServerDictionary = udpPingHttpServerDictionary;
         this.applicationActionModels = applicationActionModels;
         this.displayNameCalculator = displayNameCalculator;
         this.rootContext = rootContext;
         this.configTreeManager = configTreeManager;
         this.configInitializer = configInitializer;
         this.mainSeriesSelector = new MainSeriesSelector(
             rootContext,
             applicationActionModels,
             udpPingHttpServerDictionary,
             displayNameCalculator
         );
        addShowFrameTreeListener();
     }
 
     public List<ConfigAware> getConfigAwareChildren() {
         return Arrays.asList((ConfigAware)mainSeriesSelector, mainFrame);
     }
 
     public void clearConfig() {
        disposeAllFrames();
    }

    private void disposeAllFrames() {
         mainFrame = null;
        for ( AbstractDesktopFrame f : desktopContextToFrameMap.values()) {
            f.setVisible(false);
            f.dispose();
        }
        desktopContextToFrameMap.clear();
     }
 
     public void prepareConfigForSave(TimeSeriousConfig config) {
     }
 
     public void restoreConfig(TimeSeriousConfig config) {
         DesktopContext c = rootContext.getMainDesktopContext();
         this.mainFrame = createMainFrame(c);
         addToFrameMap(c, mainFrame);
     }
 
     private void showFrames() {
         List<DesktopContext> contexts = rootContext.findAll(DesktopContext.class).getAllMatches();
         for ( DesktopContext context : contexts) {
             if ( context.isShown()) {
                 showFrame(context);
             }
         }
     }
 
     private void addShowFrameTreeListener() {
         rootContext.addTreeListener(AwtSafeListener.getAwtSafeListener(new ShowFrameTreeListener(), IdentifiableTreeListener.class));
     }
 
     private class ShowFrameTreeListener extends IdentifiableTreeListenerAdapter {
 
         public void descendantAdded(IdentifiableTreeEvent contextTreeEvent) {
             for ( Identifiable c : contextTreeEvent.getNodes()) {
                 if ( c instanceof DesktopContext) {
                     DesktopContext n = (DesktopContext)c;
                     if ( n.isShown()) {
                         showFrame(n);
                     }
                 }
             }
         }
 
         public void descendantChanged(IdentifiableTreeEvent contextTreeEvent) {
             for ( Identifiable c : contextTreeEvent.getNodes()) {
                 if ( c instanceof DesktopContext) {
                     DesktopContext n = (DesktopContext)c;
                     if ( n.isShown()) {
                         showFrame(n);
                     }
                 }
             }
         }
 
         public void descendantRemoved(IdentifiableTreeEvent contextTreeEvent) {
              for ( Identifiable c : contextTreeEvent.getNodes()) {
                 if ( c instanceof DesktopContext) {
                     DesktopContext n = (DesktopContext)c;
                     AbstractDesktopFrame f = desktopContextToFrameMap.get(n);
                     if ( f != null) { //could already be hidden
                         f.dispose();
                     }
                 }
             }
         }
     }
 
     private void showFrame(DesktopContext desktopContext) {
         AbstractDesktopFrame frame;
         if ( desktopContext.isMainDesktopContext()) {
             frame = mainFrame;
         } else {
             frame = new DefaultDesktopFrame(
                 udpPingHttpServerDictionary,
                 displayNameCalculator,
                 desktopContext,
                 mainSeriesSelector.getSelectionPanel(),
                 rootContext,
                 applicationActionModels
             );
         }
         frame.setVisible(true);
         addToFrameMap(desktopContext, frame);
     }
 
     private void addToFrameMap(final DesktopContext desktopContext, AbstractDesktopFrame frame) {
         desktopContextToFrameMap.put(desktopContext, frame);
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 desktopContextToFrameMap.remove(desktopContext);
             }
         });
     }
 
     private TimeSeriousMainFrame createMainFrame(DesktopContext desktopContext) {
         return new TimeSeriousMainFrame(udpPingHttpServerDictionary,applicationActionModels, configTreeManager, configInitializer, displayNameCalculator, rootContext, mainSeriesSelector, desktopContext);
     }
 }
