 package fit;
 
 // Copyright (c) 2002 Cunningham & Cunningham, Inc.
 // Released under the terms of the GNU General Public License version 2 or later.
 
 import java.io.*;
 import java.util.*;
 import java.lang.reflect.*;
 import java.text.DateFormat;
 
 public class Fixture {
 
     public Map summary = new HashMap();
     public Counts counts = new Counts();
 
     public class Counts {
         public int right = 0;
         public int wrong = 0;
         public int ignores = 0;
         public int exceptions = 0;
 
         public String toString() {
             return
                 right + " right, " +
                 wrong + " wrong, " +
                 ignores + " ignored, " +
                 exceptions + " exceptions";
         }
 
         public void tally(Counts source) {
             right += source.right;
             wrong += source.wrong;
             ignores += source.ignores;
             exceptions += source.exceptions;
         }
     }
 
     public class RunTime {
         long start = System.currentTimeMillis();
         long elapsed = 0;
 
         public String toString() {
             elapsed = (System.currentTimeMillis()-start);
             if (elapsed > 600000) {
                 return d(3600000)+":"+d(600000)+d(60000)+":"+d(10000)+d(1000);
             } else {
                 return d(60000)+":"+d(10000)+d(1000)+"."+d(100)+d(10);
             }
         }
 
         String d(long scale) {
             long report = elapsed / scale;
             elapsed -= report * scale;
             return Long.toString(report);
         }
     }
 
 
 
     // Traversal //////////////////////////
 
     public void doTables(Parse tables) {
         summary.put("run date", new Date());
         summary.put("run elapsed time", new RunTime());
         while (tables != null) {
             Parse heading = tables.at(0,0,0);
             if (heading != null) {
                 try {
                     Fixture fixture = loadFixture(heading.text());
                     fixture.counts = counts;
                     fixture.summary = summary;
                     fixture.doTable(tables);
                 } catch (Exception e) {
                     exception (heading, e);
                 }
             }
             tables = tables.more;
         }
     }
 
 	public Fixture loadFixture(String fixtureName)
 		throws InstantiationException, IllegalAccessException, ClassNotFoundException {
 		return (Fixture)(Class.forName(fixtureName).newInstance());
 	}
 
     public void doTable(Parse table) {
         doRows(table.parts.more);
     }
 
     public void doRows(Parse rows) {
         while (rows != null) {
             Parse more = rows.more;
             doRow(rows);
             rows = more;
         }
     }
 
     public void doRow(Parse row) {
         doCells(row.parts);
     }
 
     public void doCells(Parse cells) {
         for (int i=0; cells != null; i++) {
             try {
                 doCell(cells, i);
             } catch (Exception e) {
                 exception(cells, e);
             }
             cells=cells.more;
         }
     }
 
     public void doCell(Parse cell, int columnNumber) {
         ignore(cell);
     }
 
 
     // Annotation ///////////////////////////////
 
     public static String green = "#cfffcf";
     public static String red = "#ffcfcf";
     public static String gray = "#efefef";
     public static String yellow = "#ffffcf";
 
     public  void right (Parse cell) {
         cell.addToTag(" bgcolor=\"" + green + "\"");
         counts.right++;
     }
 
     public void wrong (Parse cell) {
         cell.addToTag(" bgcolor=\"" + red + "\"");
         counts.wrong++;
     }
 
     public void wrong (Parse cell, String actual) {
         wrong(cell);
        
        cell.body = "<table>"
        	+ "<tr><td>" + delimit(escape(cell.text())) + "</td><td>" + label("expected") + "</td></tr>"
        	+ "<tr><td colspan='2'><hr /></td></tr>"
        	+ "<tr><td>" + delimit(escape(actual)) + "</td><td>" + label("actual") + "</td></tr>"
        	+ "</table>";
     }
 
     public void ignore (Parse cell) {
         cell.addToTag(" bgcolor=\"" + gray + "\"");
         counts.ignores++;
     }
 
     public void exception (Parse cell, Throwable exception) {
         while(exception.getClass().equals(InvocationTargetException.class)) {
             exception = ((InvocationTargetException)exception).getTargetException();
         }
         final StringWriter buf = new StringWriter();
         exception.printStackTrace(new PrintWriter(buf));
         cell.addToBody("<hr><pre><font size=-2>" + (buf.toString()) + "</font></pre>");
         cell.addToTag(" bgcolor=\"" + yellow + "\"");
         counts.exceptions++;
     }
 
     // Utility //////////////////////////////////
 
     public String counts() {
         return counts.toString();
     }
 
	public static String delimit (String string) {
		return label("|") + string + label("|"); 
	}

     public static String label (String string) {
        return "<font size=-1 color=#400000><i>" + string + "</i></font>";
     }
 
     public static String gray (String string) {
         return " <font color=#808080>" + string + "</font>";
     }
 
     public static String escape (String string) {
     	string = string.replaceAll("&", "&amp;");
     	string = string.replaceAll("<", "&lt;");
     	string = string.replaceAll("  ", " &nbsp;");
 		string = string.replaceAll("\r\n", "<br />");
 		string = string.replaceAll("\n\r", "<br />");
 		string = string.replaceAll("\r", "<br />");
 		string = string.replaceAll("\n", "<br />");
     	return string;
     }
 
     public static String camel (String name) {
         StringBuffer b = new StringBuffer(name.length());
         StringTokenizer t = new StringTokenizer(name);
         b.append(t.nextToken());
         while (t.hasMoreTokens()) {
             String token = t.nextToken();
             b.append(token.substring(0, 1).toUpperCase());      // replace spaces with camelCase
             b.append(token.substring(1));
         }
         return b.toString();
     }
 
     public Object parse (String s, Class type) throws Exception {
         if (type.equals(String.class))              {return s;}
         if (type.equals(Date.class))                {return DateFormat.getDateInstance().parse(s);}
         if (type.equals(ScientificDouble.class))    {return ScientificDouble.valueOf(s);}
         throw new Exception("can't yet parse "+type);
     }
 
     public void check(Parse cell, TypeAdapter a) {
         String text = cell.text();
         if (text.equals("")) {
             try {
                 cell.addToBody(gray(a.toString(a.get())));
             } catch (Exception e) {
                 cell.addToBody(gray("error"));
             }
         } else if (a == null) {
             ignore(cell);
         } else  if (text.equals("error")) {
             try {
                 Object result = a.invoke();
                 wrong(cell, a.toString(result));
             } catch (IllegalAccessException e) {
                 exception (cell, e);
             } catch (Exception e) {
                 right(cell);
             }
         } else {
             try {
                 Object result = a.get();
                 if (a.equals(a.parse(text), result)) {
                     right(cell);
                 } else {
                     wrong(cell, a.toString(result));
                 }
             } catch (Exception e) {
                 exception(cell, e);
             }
         }
     }
 
 }
