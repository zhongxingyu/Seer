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
 package com.od.jtimeseries.ui.selector.shared;
 
 import com.od.jtimeseries.timeseries.impl.DefaultTimeSeriesFactory;
 import com.od.jtimeseries.ui.config.UiTimeSeriesConfig;
 import com.od.jtimeseries.ui.timeseries.UIPropertiesTimeSeries;
 import com.od.jtimeseries.util.identifiable.Identifiable;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 
 import java.net.MalformedURLException;
 
 /**
 * Created by IntelliJ IDEA.
 * User: Nick
 * Date: 30/04/11
 * Time: 15:51
 */
 public abstract class AbstractUIContextTimeSeriesFactory extends DefaultTimeSeriesFactory {
 
     private static final LogMethods logMethods = LogUtils.getLogMethods(AbstractUIContextTimeSeriesFactory.class);
 
    public <E extends Identifiable> E createTimeSeries(Identifiable parent, String path, String id, String description, Class<E> clazzType, Object... parameters) {
         UIPropertiesTimeSeries result = null;
         try {
             if (clazzType.isAssignableFrom(UIPropertiesTimeSeries.class) && parameters.length == 1) {
                 if (parameters[0] instanceof UiTimeSeriesConfig) {
                     result = createTimeSeriesForConfig((UiTimeSeriesConfig) parameters[0]);
                 }
             }
         } catch (Exception e) {
             logMethods.logError("Failed to create timeseries for visualizer based on series in source root context", e);
         }
        return (E)result;
     }
 
     protected abstract UIPropertiesTimeSeries createTimeSeriesForConfig(UiTimeSeriesConfig config) throws MalformedURLException;
 }
