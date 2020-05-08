 package org.makumba.parade.view.managers;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 
 import org.makumba.parade.init.ParadeProperties;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.FileCVS;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.RowCVS;
 import org.makumba.parade.view.interfaces.FileView;
 import org.makumba.parade.view.interfaces.HeaderView;
 import org.makumba.parade.view.interfaces.ParadeView;
 
 public class CVSViewManager implements ParadeView, FileView, HeaderView {
 
     public String getParadeViewHeader() {
        String header = "<th>CVS module, user, branch</th>";
         return header;
     }
 
     public String getParadeView(Row r) {
         RowCVS cvsdata = (RowCVS) r.getRowdata().get("cvs");
 
         String view = cvsdata.getUser() + ",<b>" + cvsdata.getModule() + "</b>," + cvsdata.getBranch();
         return view;
     }
 
     public String getFileView(Row r, String path, File f) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         path = r.getRowpath() + path;
         
         FileCVS cvsdata = (FileCVS) f.getFiledata().get("cvs");
         RowCVS rowcvsdata = (RowCVS) r.getRowdata().get("cvs");
 
         out.print("<td>");
 
         String cvscommand = "";
         String cvscommit = "";
         try {
             String encodedPath = java.net.URLEncoder.encode(path,"UTF-8");
             String encodedFile = java.net.URLEncoder.encode(f.getPath(), "UTF-8");
             cvscommand = "<a target='command' href='/Cvs.do?context="+r.getRowname()+"&path="+encodedPath+"&file="
                     + encodedFile + "&op=";
             
             cvscommit = "<a target='command' href='/servlet/browse?context="+r.getRowname()+"&path="+encodedPath+"&file="
                     + encodedFile + "&display=command&view=commit'>";
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         String cvsweb = ParadeProperties.getProperty("cvs.site");
         String webPath = (f.getPath().substring(f.getRow().getRowpath().length())).replace(java.io.File.separatorChar,
                 '/');
         String cvswebLink = cvsweb + rowcvsdata.getModule() + webPath;
 
         // if there's no CVS data
         if (cvsdata == null) {
             if (f.getName().startsWith(".#")) {
                 out
                         .print("<a title='Backup of your working file, can be deleted once you resolved its conflicts with CVS'>Conflict Backup</a>");
 
             } else { // show options to add to cvs
                 out.print(cvscommand + "add'><img src='/images/cvs-add.gif' alt='add'></a>" + cvscommand
                         + "addbin'><img src='/images/cvs-add-binary.gif' alt='add binary'></a>");
             }
             return result.toString();
         }
         switch (cvsdata.getStatus().intValue()) {
 
         case 101: { // IGNORED
             out.print("<div class='cvs-ignored'>ignored</div>");
         }
             break;
 
         case -1: { // UNKNOWN
             out.print("???");
         }
             break;
 
         case 100: { // UP_TO_DATE
             if (f.getIsDir()) {
                 out.print("<a href='" + cvswebLink + "' title='CVS log'>(dir)</a>");
             } else {
                 out.print("<a href='" + cvswebLink + "' title='CVS log'>" + cvsdata.getRevision() + "</a>");
             }
 
         }
             break;
 
         case 1: { // LOCALLY_MODIFIED
             if (f.getIsDir()) {
                 out.print("<a href='" + cvswebLink + "' title='CVS log'>(dir)</a>");
             } else {
                 out.println("<a href='" + cvswebLink + "' title='CVS log'>" + cvsdata.getRevision() + "</a>"
                         + cvscommit + "<img src='/images/cvs-committ.gif' alt='CVS commit'></a>" + cvscommand
                         + "diff'><img src='/images/cvs-diff.gif' alt='CVS diff'></a>");
             }
 
         }
             break;
 
         case 2: { // NEEDS_CHECKOUT
             if (f.getIsDir()) {
                 out.print("<a href='" + cvswebLink + "' title='CVS log'>(dir)</a>");
             } else {
                 out.println("<a href='" + cvswebLink + "' title='CVS log'>" + cvsdata.getRevision() + "</a>"
                         + cvscommand + "updatefile'><img src='/images/cvs-update.gif' alt='CVS checkout'></a>"
                         + cvscommand + "deletefile'><img src='/images/cvs-remove.gif' alt='CVS remove'></a>");
             }
 
         }
             break;
 
         case 3: { // NEEDS_UPDATE
             if (f.getIsDir()) {
                 out.print("<a href='" + cvswebLink + "' title='CVS log'>(dir)</a>");
             } else {
                 out.println("<a href='" + cvswebLink + "' title='CVS log'>" + cvsdata.getRevision() + "</a>"
                         + cvscommand + "updatefile'><img src='/images/cvs-update.gif' alt='CVS update'></a>");
             }
 
         }
             break;
 
         case 4: { // ADDED
             out.println("<a href='" + cvswebLink + "' title='CVS log'>"+cvsdata.getRevision() + "</a>" +
                     cvscommit + "<img src='/images/cvs-committ.gif' alt='CVS commit'></a>");
         }
             break;
 
         case 5: { // DELETED
             out.println("<a href='" + cvswebLink + "' title='CVS log'>" + cvsdata.getRevision() + "</a>" + cvscommit
                     + "<img src='/images/cvs-committ.gif' alt='CVS commit'></a>");
         }
             break;
 
         case 6: { // CONFLICT
             out.println("<a href='" + cvswebLink + "' title='CVS log'><b><font color='red'>Conflict</font></b> "
                     + cvsdata.getRevision() + "</a>" + cvscommit
                     + "<img src='/images/cvs-committ.gif' alt='CVS commit'></a>" + cvscommand
                     + "diff'><img src='/images/cvs-diff.gif' alt='CVS diff'></a>");
         }
             break;
         }
 
         out.print("</td>");
 
         return result.toString();
     }
 
     public String getFileViewHeader(Row r, String path) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         out.println("<a href='/Cvs.do?op=check&context=" + r.getRowname() +
                 "&params="+path+"' target='command'><img src='/images/cvs-query.gif'" +
                 " alt='CVS check status' border='0'></a>");
         
         out.println("<a href='/Cvs.do?op=update&context=" + r.getRowname() +
                 "&params="+path+"' target='command'><img src='/images/cvs-update.gif'" +
                 " alt='CVS local update' border='0'></a>");
         
         out.println("<a href='/Cvs.do?op=rupdate&context=" + r.getRowname() +
                 "&params="+path+"' target='command'><img src='/images/cvs-update.gif'" +
                  " alt='CVS recursive update' border='0'></a>");
         
 
         String header = "<th>CVS "+result.toString()+"</th>";
         return header;
     }
 
     public String getHeaderView(Row r, String path) {
 
         /*
          * <%-- $Header:
          * /cvsroot/parade/parade2/webapp/WEB-INF/classes/org/makumba/parade/view/managers/CVSViewManager.java,v 1.8
          * 2006/01/09 20:27:29 stefanba Exp $ --%> <br>CVS: <% String cvscommand="<a target='command'
          * href=command.jsp?"+pageContext.findAttribute("parade.sameDir")+"&cvs.perDir=&op=cvs&cvs.op="; %>
          * <%=cvscommand%>update&cvs.-l=&cvs.-n=&cvs.op.-d=&cvs.op.-P=>check status</a>
          * <%=cvscommand%>update&cvs.op.-d=&cvs.op.-P=&cvs.op.-l=&reload=>local update</a>
          * <%=cvscommand%>update&cvs.op.-d=&cvs.op.-P=&reload=>recursive update</a>
          * 
          * 
          */
         return null;
     }
 
 }
