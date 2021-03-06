 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.dfci.cccb.mev.analysis;
 
 import static us.levk.util.io.support.Provisionals.file;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 
 import us.levk.util.io.implementation.Provisional;
 import edu.dfci.cccb.mev.domain.AnnotationNotFoundException;
 import edu.dfci.cccb.mev.domain.Heatmap;
 import edu.dfci.cccb.mev.domain.MatrixAnnotation;
 import edu.dfci.cccb.mev.domain.MatrixSelection;
 
 /**
  * @author levk
  * 
  */
 public class Limma {
 
   private static final ScriptEngine r = new ScriptEngineManager ().getEngineByName ("R");
   private static final VelocityEngine velocity = new VelocityEngine ();
 
   public static void execute (Heatmap heatmap,
                               String selection1,
                               String selection2,
                               final File output,
                               final File significant,
                               final File rnk) throws IOException, ScriptException, AnnotationNotFoundException {
     try (final Provisional input = file ();
          final Provisional configuration = file ();
          ByteArrayOutputStream script = new ByteArrayOutputStream ()) {
       configure (new FileOutputStream (configuration), heatmap, selection1, selection2);
       dump (new FileOutputStream (input), heatmap);
       velocity.getTemplate ("limma.R.vm").merge (new VelocityContext (new HashMap<String, String> () {
         private static final long serialVersionUID = 1L;
 
         {
           put ("input", input.getAbsolutePath ());
           put ("configuration", configuration.getAbsolutePath ());
           put ("output", output.getAbsolutePath ());
           put ("significant", significant.getAbsolutePath ());
           put ("rnk", rnk.getAbsolutePath ());
         }
       }), new OutputStreamWriter (script));
       r.eval (new InputStreamReader (new ByteArrayInputStream (script.toByteArray ())));
     }
   }
 
   private static void configure (OutputStream configuration, Heatmap heatmap, String s1, String s2) throws IOException {
     int rows = heatmap.getSummary ().rows ();
     MatrixSelection first = heatmap.getRowSelection (s1, 0, rows);
     MatrixSelection second = heatmap.getRowSelection (s2, 0, rows);
     PrintStream out = new PrintStream (configuration);
     for (int index = 0; index < rows; index++)
       out.println (index
                   + "\t" + (first.getIndices ().contains (index) ? 1 : (second.getIndices ().contains (index) ? 0 : -1)));
     out.flush ();
   }
 
   private static void dump (final OutputStream data, final Heatmap heatmap) throws IOException,
                                                                            AnnotationNotFoundException {
     final String newline = System.getProperty ("line.separator", "\n"), tab = "\t";
     StringBuffer header = new StringBuffer ();
     for (MatrixAnnotation<?> column : heatmap.getColumnAnnotation (0, heatmap.getSummary ().columns () - 1, "COLUMN"))
       header.append (tab).append (column.attribute ());
     data.write (header.append (newline).toString ().getBytes ());
     heatmap.toStream (newline,
                       new Object () {
                         private Iterator<MatrixAnnotation<?>> rows = heatmap.getRowAnnotation (0,
                                                                                                heatmap.getSummary ()
                                                                                                       .rows (),
                                                                                                "annotation-0")
                                                                             .iterator ();
 
                         /* (non-Javadoc)
                          * @see java.lang.Object#toString() */
                         @Override
                         public String toString () {
                           return rows.next ().toString () + tab;
                         }
                       },
                       new ObjectOutputStream () {
                         /* (non-Javadoc)
                          * @see
                          * java.io.ObjectOutputStream#writeObjectOverride(java
                          * .lang.Object) */
                         @Override
                         protected void writeObjectOverride (Object obj) throws IOException {
                           data.write (obj.toString ().getBytes ());
                         }
                       });
   }
 }
