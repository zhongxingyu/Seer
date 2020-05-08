 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource SA
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.metricshelp.client;
 
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import org.sonar.gwt.ui.Page;
 import org.sonar.wsclient.gwt.AbstractListCallback;
 import org.sonar.wsclient.gwt.Sonar;
 import org.sonar.wsclient.services.Metric;
 import org.sonar.wsclient.services.MetricQuery;
 
 import java.util.List;
 
 public class MetricsPage extends Page {
 
   @Override
   protected Widget doOnModuleLoad() {
     final VerticalPanel panel = new VerticalPanel();
 

    Sonar.getInstance().findAll(MetricQuery.all(), new AbstractListCallback<Metric>(){
       @Override
       protected void doOnResponse(List<Metric> metrics) {
         Grid grid = new Grid(metrics.size(), 3);
         grid.setStyleName("data");
 
        for (int i=0 ; i<metrics.size() ; i++) {
          if (i%2==0) {
             grid.getRowFormatter().setStyleName(i, "even");
           } else {
             grid.getRowFormatter().setStyleName(i, "odd");
           }
           grid.setWidget(i, 0, new Label(metrics.get(i).getKey()));
           grid.setWidget(i, 1, new Label(metrics.get(i).getName()));
           grid.setWidget(i, 2, new Label(metrics.get(i).getDescription()));
         }
 
         panel.add(grid);
       }
     });
 



     return panel;
   }
 

 }
