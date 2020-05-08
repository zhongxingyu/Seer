 // Part of "Functional Grammar Language Generator" (http://fgram.sourceforge.net/) (C) 2006 Fabian Steeg
 // This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 // This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 // You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
 package de.uni_koeln.spinfo.fg.parser;
 
 import java.io.StringReader;
 
 import junit.framework.TestCase;
 import antlr.RecognitionException;
 import antlr.TokenStreamException;
 import antlr.TokenStreamHiddenTokenFilter;
 import antlr.collections.AST;
import de.uni_koeln.spinfo.fg.ucs.InputProcessor;
 import de.uni_koeln.spinfo.fg.ucs.PrologGenerator;
 import de.uni_koeln.spinfo.fg.ucs.model.Predicate;
import de.uni_koeln.spinfo.fg.util.Config;
import de.uni_koeln.spinfo.fg.util.Log;
 import de.uni_koeln.spinfo.fg.util.Util;
 
 public class UcsParserTest extends TestCase {
    InputProcessor inputProcessor;
 
    protected void setUp() throws Exception {
        super.setUp();
        Log.init(Config.getString("log_folder"));
        inputProcessor = new InputProcessor(Config.getString("prolog_application"));
        
    }
     public void testParser() {
         String s = Util.getText("ucs/ucs.txt");
         System.out.println(s);
         assertTrue("Error while reading UCS from file", s != null);
         s = s.replaceAll("[^a-zA-Z0-9\\[\\]\\(\\):-]", "");
         UcsLexer lexer = new UcsLexer(new StringReader(s));
 
         lexer.setTokenObjectClass("antlr.CommonHiddenStreamToken");
 
         TokenStreamHiddenTokenFilter filter = new TokenStreamHiddenTokenFilter(
                 lexer);
 
         // filter.discard(UcsParser.WORD);
         filter.hide(UcsParser.WORD);
 
         assertTrue("Error while instantiating Lexer", lexer != null);
         UcsParser parser = new UcsParser(lexer);
         // UcsParser parser = new UcsParser(filter);
         assertTrue("Error while instantiating parser", parser != null);
         Predicate p;
         try {
             p = parser.input();
             System.out.println(p.toString());
             assertTrue("Error while parsing", p != null);
             AST t = parser.getAST();
             System.out.println("AST: " + t.toStringTree());
             PrologGenerator gen = new PrologGenerator(p);
             assertTrue("Error while instantiating PrologGenerator", gen != null);
             String generateProlog = gen.generateProlog();
             assertTrue("Error while generating Prolog", generateProlog != null);
             System.out.println(generateProlog);
         } catch (RecognitionException e) {
             e.printStackTrace();
         } catch (TokenStreamException e) {
             e.printStackTrace();
         }
 
     }
    
    @Override
    protected void tearDown() throws Exception {
        inputProcessor.close();
        super.tearDown();
    }
 
 }
