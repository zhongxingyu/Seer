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
             Parse fixtureName = fixtureName(tables);
             if (fixtureName != null) {
                 try {
                     Fixture fixture = loadFixture(fixtureName.text());
                     fixture.counts = counts;
                     fixture.summary = summary;
                     fixture.doTable(tables);
                 } catch (Exception e) {
                     exception (fixtureName, e);
                 }
             }
             tables = tables.more;
         }
     }
     
     public Parse fixtureName(Parse tables) {
 		return tables.at(0,0,0);
     }
 
 	public Fixture loadFixture(String fixtureName)
 		throws InstantiationException, IllegalAccessException, ClassNotFoundException {
 		try {
 			return (Fixture)(Class.forName(fixtureName).newInstance());
 		}
 		catch (ClassNotFoundException e) {
 			throw new RuntimeException("The fixture \"" + fixtureName + "\" was not found.", e);
 		}
 		catch (ClassCastException e) {
 			throw new RuntimeException("\"" + fixtureName + "\" was found, but it's not a fixture.", e);
 		}
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
 		cell.body = escape(cell.text());
         counts.wrong++;
     }
 
     public void wrong (Parse cell, String actual) {
         wrong(cell);
         cell.addToBody(label("expected") + "<hr>" + escape(actual) + label("actual"));
     }
 
 	public void info (Parse cell, String message) {
 		cell.addToBody(info(message));
 	}
 
 	public String info (String message) {
 		return " <font color=\"#808080\">" + escape(message) + "</font>";
 	}
 
     public void ignore (Parse cell) {
         cell.addToTag(" bgcolor=\"" + gray + "\"");
         counts.ignores++;
     }
 
 	public void error (Parse cell, String message) {
 		cell.body = escape(cell.text());
		cell.addToBody("<hr><pre><font size=-2>" + escape(message) + "</font></pre>");
 		cell.addToTag(" bgcolor=\"" + yellow + "\"");
 		counts.exceptions++;
 	}
 
     public void exception (Parse cell, Throwable exception) {
         while(exception.getClass().equals(InvocationTargetException.class)) {
             exception = ((InvocationTargetException)exception).getTargetException();
         }
         final StringWriter buf = new StringWriter();
         exception.printStackTrace(new PrintWriter(buf));
         error(cell, buf.toString());
     }
 
     // Utility //////////////////////////////////
 
     public String counts() {
         return counts.toString();
     }
 
     public static String label (String string) {
         return " <font size=-1 color=\"#c08080\"><i>" + string + "</i></font>";
     }
 
     public static String escape (String string) {
     	string = string.replaceAll("&", "&amp;");
     	string = string.replaceAll("<", "&lt;");
     	string = string.replaceAll("  ", " &nbsp;");
 		string = string.replaceAll("\r\n", "<br />");
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
                 info(cell, a.toString(a.get()));
             } catch (Exception e) {
                 info(cell, "error");
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
