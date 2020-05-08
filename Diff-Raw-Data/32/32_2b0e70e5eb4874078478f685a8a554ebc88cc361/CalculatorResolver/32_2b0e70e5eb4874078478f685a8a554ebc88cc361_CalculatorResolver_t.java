 package net.ggtools.codestory;
 
 import groovy.lang.GroovyShell;
 
 import javax.servlet.http.HttpServletRequest;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.Locale;
 
 /**
  * User: Christophe Labouisse
  * Date: 17/01/13
  * Time: 11:43
  */
 public class CalculatorResolver implements Resolver {
 
     @Override
     public String solve(HttpServletRequest request) throws ResolverException {
         String q = request.getParameter("q");
 
         if (q == null) return null;
 
         String expr = q.replace(',', '.').replaceAll(" ", "+");
         try {
             DecimalFormat format = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.FRENCH));
             GroovyShell shell = new GroovyShell();
             return format.format(shell.evaluate(expr));
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 }
