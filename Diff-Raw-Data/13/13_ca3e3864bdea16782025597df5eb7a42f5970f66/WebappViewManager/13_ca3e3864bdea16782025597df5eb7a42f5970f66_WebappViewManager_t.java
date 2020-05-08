 package org.makumba.parade.view.managers;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.RowWebapp;
 import org.makumba.parade.model.managers.ServletContainer;
 import org.makumba.parade.view.interfaces.HeaderView;
 import org.makumba.parade.view.interfaces.ParadeView;
 
 public class WebappViewManager implements HeaderView, ParadeView {
 
     public String getHeaderView(Row r, String path) {
         RowWebapp data = (RowWebapp) r.getRowdata().get("webapp");
 
         return "webapp: " + getCommands(r, data, path);
     }
 
     public String getParadeViewHeader() {
         String header = "<th>Webapp path, status</th>";
         return header;
     }
 
     public String getParadeView(Row r) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         RowWebapp data = (RowWebapp) r.getRowdata().get("webapp");
 
         int status = data.getStatus().intValue();
        
        if( r.getRowpath().equals(r.getParade().getBaseDir())) {
            data.setStatus(new Integer(ServletContainer.RUNNING));
            status = ServletContainer.RUNNING;
        }
 
         out.println(data.getWebappPath() + ", " + ServletContainer.status[status] + "<br>");
 
         out.println(getCommands(r, data, ""));
 
         return result.toString();
     }
 
     private String getCommands(Row r, RowWebapp data, String path) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         int status = data.getStatus().intValue();
 
        if( r.getRowpath().equals(r.getParade().getBaseDir())) {
             data.setStatus(new Integer(ServletContainer.RUNNING));
            status = ServletContainer.RUNNING;
        }
            
         // TODO - consider the case WEBINF isn't found, ie get server name, port from some request
         if (data.getWebappPath().equals("NO WEBINF")) {
             /*
              * if(pageContext.findAttribute('servletContext.noPrintStatus')==null){ if(stt==ServletContainer.RUNNING) { %>
              * <a href= <%='http://'+request.getServerName()+':'+request.getServerPort()+contextName %> > <%}%>
              * <%=ServletContainer.status[stt]%> <% if(stt==ServletContainer.RUNNING) { %> </a> <%} %> <br> <% }
              * 
              */
         } else {
             if (status == ServletContainer.RUNNING) {
                 out.println("<a href='/Webapp.do?context=" + r.getRowname() 
                         + "&path="+path+"&op=servletContextReload'>reload</a> "
                         + "<a href='/Webapp.do?context=" + r.getRowname()
                         + "&path="+path+"&op=servletContextStop'>stop</a> ");
             }
             if (status == ServletContainer.STOPPED) {
                 out.println("<a href='/Webapp.do?context=" + r.getRowname()
                         + "&path="+path+"&op=servletContextStart'>start</a> ");
             }
             if (status != ServletContainer.NOT_INSTALLED) {
                 out.println("<a href='/Webapp.do?context=" + r.getRowname()
                         + "&path="+path+"&op=servletContextRemove'>uninstall</a>");
             }
             if (status == ServletContainer.NOT_INSTALLED) {
                 out.println("<a href='/Webapp.do?context=" + r.getRowname()
                         + "&path="+path+"&op=servletContextInstall'>install</a>");
             }
         }
 
         return result.toString();
     }
 
 }
