 /*
     Copyright (C) 2010 LearningWell AB (www.learningwell.com), Kärnkraftsäkerhet och Utbildning AB (www.ksu.se)
 
     This file is part of GIL (Generic Integration Layer).
 
     GIL is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     GIL is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with GIL.  If not, see <http://www.gnu.org/licenses/>.
 */
 package gil.web.html.page;
 
 import gil.GIL;
 import gil.core.SoftwareInfo;
 import gil.web.html.Div;
 import gil.web.html.Heading;
 import gil.web.html.PlainText;
 import gil.web.html.Tag;
 
 /**
  * @author Göran Larsson @ LearningWell AB
  */
 public class AboutPage extends MasterPage {
 
     public AboutPage() {
         GIL.VersionInfo info = GIL.instance().getVersionInfo();
 
         Div div = new Div("about");
         div.addContent(new Heading("About", Heading.H2));
 
        div.addContent(new Heading("Current GIL", Heading.H3));
         addVersionInfo(div, info.gilInfo);
         
         div.addContent(new Heading("External System", Heading.H3));
         addVersionInfoSection(info.externalSystemInfo, div);
 
         div.addContent(new Heading("Process Model", Heading.H3));
         addVersionInfoSection(info.processModelInfo, div);
 
         this.setSectionContent("content", div);
     }
 
     private void addVersionInfoSection(SoftwareInfo[] info, Div div) {
         for (SoftwareInfo i : info) {
             addVersionInfo(div, i);
             if (i != info[info.length - 1]) {
                 div.addContent(new Tag("hr"));
             }
         }
     }
 
     private void addVersionInfo(Div addTo, SoftwareInfo info) {
         Div div = new Div("aboutVersion");
 
         div.addContent(new Heading("Name", Heading.H4)).addContent(new PlainText(info.getName()));
         div.addContent(new Heading("Version", Heading.H4)).addContent(new PlainText(info.getVersion()));
         div.addContent(new Heading("Company", Heading.H4)).addContent(new PlainText(info.getCompany()));
         div.addContent(new Heading("Description", Heading.H4)).addContent(new PlainText(info.getDescription()));
 
         addTo.addContent(div);
     }
 }
