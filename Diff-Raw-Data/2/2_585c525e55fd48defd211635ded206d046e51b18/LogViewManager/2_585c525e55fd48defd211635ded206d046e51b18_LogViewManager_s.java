 package org.makumba.parade.view.managers;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.Log;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.tools.HtmlUtils;
 
 import freemarker.template.SimpleHash;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class LogViewManager {
 
     public String getLogView(Session s, String context, Integer years, Integer months, Integer days) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         Template temp = null;
         try {
             temp = InitServlet.getFreemarkerCfg().getTemplate("logs.ftl");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         // Creating the data model
         SimpleHash root = new SimpleHash();
         root.put("context", context);
         root.put("year", years.toString());
         root.put("month", ""+(months.intValue()+1));
         root.put("day", days.toString());
         
         Calendar cal = GregorianCalendar.getInstance();
         
         cal.clear();
         cal.set(years.intValue(), months.intValue(), days.intValue());
         
         String query = "from Log l where l.context = :context and l.date > :date";
         if(context.equals("all"))
             query = "from Log l where l.date > :date";
         if(context.equals("(root)"))
            query = "from Log l where l.context is null and l.date > :date";
         
         Query q = s.createQuery(query);
         q.setCacheable(false);
         
         if(!context.equals("all") && !context.equals("(root)"))
           q.setString("context", context);
         
         q.setDate("date", cal.getTime());
         
         List<Log> entries = q.list();
         List viewEntries = new LinkedList();
         for(int i=0; i<entries.size(); i++) {
             SimpleHash entry = new SimpleHash();
             if(entries.get(i).getMessage().trim().length() == 0) //skip blank lines
                 continue;
             if(entries.get(i).getMessage().equals("Server restart"))
                 entry.put("serverRestart", true);
             else
                 entry.put("serverRestart", false);
             entry.put("message", HtmlUtils.string2html(entries.get(i).getMessage()));
             entry.put("level", entries.get(i).getLevel());
             entry.put("user", (entries.get(i).getUser() == null)?"system":entries.get(i).getUser());
             entry.put("context", (entries.get(i).getContext() == null)?"parade2":entries.get(i).getContext());
             
             viewEntries.add(entry);
         }
         
         root.put("entries", viewEntries);
         
         /* Merge data model with template */
         try {
             temp.process(root, out);
         } catch (TemplateException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         return result.toString();
         
     }
     
     public String getLogMenuView(Session s, String context, Integer years, Integer months, Integer days) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         Template temp = null;
         try {
             temp = InitServlet.getFreemarkerCfg().getTemplate("logsHeader.ftl");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         // Creating the data model
         SimpleHash root = new SimpleHash();
         root.put("context", context);
         root.put("year", years.toString());
         root.put("month", ""+(months.intValue()+1));
         root.put("day", days.toString());
         
         List rows = new LinkedList();
         
         Parade p = (Parade) s.get(Parade.class, new Long(1));
         
         for (Iterator i = p.getRows().keySet().iterator(); i.hasNext();) {
             Row currentRow = (Row) p.getRows().get(i.next());
             String displayName = currentRow.getRowname();
             if (currentRow.getRowname() == "")
                 displayName = "(root)";
             
             rows.add(displayName);
         }
         
         root.put("rows", rows);
         
         /* Merge data model with template */
         try {
             temp.process(root, out);
         } catch (TemplateException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         return result.toString();
         
     }
 }
