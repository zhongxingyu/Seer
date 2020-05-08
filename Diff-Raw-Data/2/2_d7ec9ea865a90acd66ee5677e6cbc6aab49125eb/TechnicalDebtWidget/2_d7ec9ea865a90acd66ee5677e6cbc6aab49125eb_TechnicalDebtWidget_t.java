 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource
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
 
 package org.sonar.plugins.technicaldebt;
 
 import org.sonar.api.web.AbstractRubyTemplate;
 import org.sonar.api.web.Description;
 import org.sonar.api.web.RubyRailsWidget;
 
 @Description("Reports on the technical debt carried by a project")
 public final class TechnicalDebtWidget extends AbstractRubyTemplate implements RubyRailsWidget {
   protected String getTemplatePath() {
     return "/org/sonar/plugins/technicaldebt/dashboard_widget.erb";
   }
 
   public String getId() {
     return getClass().toString();
   }
 
   public String getTitle() {
    return "Technical Debt";
   }
 }
