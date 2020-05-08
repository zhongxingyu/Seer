 package de.objectcode.time4u.server.web.ui.user;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Install;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.annotations.Startup;
 import org.jboss.seam.annotations.intercept.BypassInterceptors;
 import org.jboss.seam.contexts.Contexts;
 import org.jboss.seam.contexts.ServletLifecycle;
 import org.jboss.seam.core.ConversationPropagation;
 import org.jboss.seam.core.Manager;
 import org.jboss.seam.web.AbstractResource;
 import org.jboss.seam.web.ServletContexts;
 
 import de.objectcode.time4u.server.ejb.seam.api.report.ColumnDefinition;
 import de.objectcode.time4u.server.ejb.seam.api.report.ColumnType;
 import de.objectcode.time4u.server.ejb.seam.api.report.ReportResult;
 import de.objectcode.time4u.server.ejb.seam.api.report.ReportResultGroup;
 import de.objectcode.time4u.server.ejb.seam.api.report.ReportRow;
 import de.objectcode.time4u.server.web.ui.converter.StringArrayConverter;
 import de.objectcode.time4u.server.web.ui.converter.TimeConverter;
 
 @Scope(ScopeType.APPLICATION)
 @Name("user.csvReportResource")
 @BypassInterceptors
 @Install(precedence = Install.APPLICATION)
 @Startup
 public class CSVReportResource extends AbstractResource
 {
   private final static String DELIM = ";";
   private final static String FIELD = "\"";
 
   private final static DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
 
   @Override
   public void getResource(final HttpServletRequest request, final HttpServletResponse response)
       throws ServletException, IOException
   {
     ServletLifecycle.beginRequest(request);
     try {
       ServletContexts.instance().setRequest(request);
       ConversationPropagation.instance().restoreConversationId(request.getParameterMap());
       Manager.instance().restoreConversation();
       ServletLifecycle.resumeConversation(request);
 
       final ReportResult reportResult = (ReportResult) Contexts.getConversationContext().get("user.reportResult");
 
       if (reportResult != null) {
        response.setContentType("text/csv;charset=UTF-8");
         response.setCharacterEncoding("UTF-8");
 
         final ServletOutputStream out = response.getOutputStream();
 
         boolean first = true;
 
         for (final ColumnDefinition column : reportResult.getGroupByColumns()) {
           if (!first) {
             out.print(DELIM);
           }
           out.print(toCSVString(column.getHeader()));
           first = false;
         }
         for (final ColumnDefinition column : reportResult.getColumns()) {
           if (!first) {
             out.print(DELIM);
           }
           out.print(toCSVString(column.getHeader()));
           first = false;
         }
         out.println();
 
         for (final ReportRow row : reportResult.getRows()) {
           writeReportRow(row, reportResult.getColumns(), out);
         }
         out.close();
       }
     } finally {
       ServletLifecycle.endRequest(request);
     }
   }
 
   @Override
   public String getResourcePath()
   {
     return "/csvreport";
   }
 
   private void writeReportRow(final ReportRow row, final List<ColumnDefinition> columns, final ServletOutputStream out)
       throws IOException
   {
     boolean first = true;
     for (int i = 0; i < columns.size(); i++) {
       if (!first) {
         out.print(DELIM);
       }
       writeValue(row.getData()[i], columns.get(i).getColumnType(), out);
       first = false;
     }
     out.println();
   }
 
   private void writeReportGroup(final int depth, final ReportResultGroup group, final ServletOutputStream out)
       throws IOException
   {
     for (int i = 0; i < depth; i++) {
       out.print(DELIM);
     }
    out.print(String.valueOf(group.getValue()));
     for (final ReportResultGroup subGroup : group.getGroups()) {
       writeReportGroup(depth + 1, subGroup, out);
     }
   }
 
   private void writeValue(final Object value, final ColumnType columnType, final ServletOutputStream out)
       throws IOException
   {
     switch (columnType) {
       case NAME:
       case DESCRIPTION:
         if (value != null && value instanceof String) {
           out.print(toCSVString((String) value));
         }
         break;
       case NAME_ARRAY:
         out.print(toCSVString(StringArrayConverter.format(value)));
         break;
       case DATE:
         if (value != null && value instanceof Date) {
           out.print(DATE_FORMAT.format((Date) value));
         }
         break;
       case TIME:
         if (value != null && value instanceof Integer) {
           out.print(TimeConverter.format((Integer) value));
         }
         break;
     }
   }
 
   private String toCSVString(final String str)
   {
     return FIELD + str.replace(FIELD, FIELD + FIELD) + FIELD;
   }
 }
