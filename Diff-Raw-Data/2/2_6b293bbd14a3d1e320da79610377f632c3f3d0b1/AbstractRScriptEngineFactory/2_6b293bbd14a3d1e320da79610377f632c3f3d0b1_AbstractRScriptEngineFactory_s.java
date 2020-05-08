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
 package edu.dfci.cccb.mev.r.prototype;
 
 import static ch.lambdaj.Lambda.join;
 import static java.lang.System.lineSeparator;
 import static java.util.Arrays.asList;
 import static java.util.Collections.unmodifiableList;
 import static javax.script.ScriptEngine.LANGUAGE;
 
 import java.util.List;
 
 import javax.script.ScriptEngineFactory;
 
 import lombok.Getter;
 
 /**
  * @author levk
  * 
  */
 public abstract class AbstractRScriptEngineFactory implements ScriptEngineFactory {
 
   private @Getter final List<String> extensions = unmodifiableList (asList ("R", "r"));
  private @Getter final List<String> mimeTypes = unmodifiableList (asList ("x-application-r"));
   private @Getter final String languageName = "R";
 
   /* (non-Javadoc)
    * @see javax.script.ScriptEngineFactory#getParameter(java.lang.String) */
   @Override
   public Object getParameter (String key) {
     if (LANGUAGE.equals (key))
       return getLanguageName ();
     else
       return null;
   }
 
   /* (non-Javadoc)
    * @see
    * javax.script.ScriptEngineFactory#getMethodCallSyntax(java.lang.String,
    * java.lang.String, java.lang.String[]) */
   @Override
   public String getMethodCallSyntax (String obj, String m, String... args) {
     return obj + "." + m + "(" + join (args, ",") + ")";
   }
 
   /* (non-Javadoc)
    * @see javax.script.ScriptEngineFactory#getOutputStatement(java.lang.String) */
   @Override
   public String getOutputStatement (String toDisplay) {
     return "cat(" + toDisplay + ")";
   }
 
   /* (non-Javadoc)
    * @see javax.script.ScriptEngineFactory#getProgram(java.lang.String[]) */
   @Override
   public String getProgram (String... statements) {
     return join (statements, lineSeparator ());
   }
 }
