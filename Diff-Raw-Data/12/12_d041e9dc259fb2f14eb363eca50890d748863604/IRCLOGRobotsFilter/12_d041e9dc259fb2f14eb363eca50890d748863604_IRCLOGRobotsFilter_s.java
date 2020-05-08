 /*
  * This file is part of the rvt_irclogs project, a Jahia module to display IRC logs
  *
  * Copyright (C) 2010 R. van Twisk (rvt@dds.nl)
  *
  * This file may be distributed and/or modified under the terms of the
  * GNU General Public License version 2 as published by the Free Software
  * Foundation and appearing in the file gpl-2.0.txt included in the
  * packaging of this file.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  *
  * This copyright notice MUST APPEAR in all copies of the script!
  */
 
 package org.jahia.modules.irclogs.filters;
 
 import org.jahia.services.render.RenderContext;
 import org.jahia.services.render.Resource;
 import org.jahia.services.render.filter.AbstractFilter;
 import org.jahia.services.render.filter.RenderChain;
 
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

 /**
  * Filter that adds a robot's header to the page when year,month or day was selected
  * User: rvt
  * Date: 11/24/12
  * Time: 4:51 PM
  * To change this template use File | Settings | File Templates.
  */
 public class IRCLOGRobotsFilter extends AbstractFilter {
 
 
     @Override
     public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
 
         final String selectedYear = renderContext.getRequest().getParameter("ircyear");
         final String selectedMonth = renderContext.getRequest().getParameter("ircmonth");
         final String selectedDay = renderContext.getRequest().getParameter("ircday");
 
         // Only replace when we have eather a year, month and day but not when we have them all
         if ((selectedYear != null || selectedMonth != null) && selectedDay == null) {
            previousOut=previousOut.replace("</head>", "<META NAME=\"ROBOTS\" CONTENT=\"NOINDEX\">\n</head>");
         }
 
         return previousOut;
     }



 }
