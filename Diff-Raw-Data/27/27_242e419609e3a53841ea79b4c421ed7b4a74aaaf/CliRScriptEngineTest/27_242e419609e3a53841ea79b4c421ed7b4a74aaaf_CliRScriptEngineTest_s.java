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
 package edu.dfci.cccb.mev.r.mock.cli;
 
 import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import org.junit.Test;
 
 /**
  * @author levk
  * 
  */
 public class CliRScriptEngineTest {
 
  private ScriptEngine r = new ScriptEngineManager ().getEngineByName ("CliR");
 
   @Test
   public void helloWorld () throws ScriptException {
     r.eval ("cat('hello world\\n')");
   }
 }
