 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.parser.antlr;
 
 import java.util.List;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.ImmutableList;
 import novelang.common.Problem;
 import novelang.common.SyntacticTree;
 //import novelang.parser.antlr. NovelangParser;
 //import NovelangLexer;
 //import AllTokens;
 
 /**
  * @author Laurent Caillette
  */
 public abstract class AbstractDelegatingParser {
 
   private final NovelangParser parser ;
   private final GrammarDelegate delegate;
 
   public AbstractDelegatingParser( String text, GrammarDelegate delegate ) {
     this.delegate = delegate ;
     CharStream stream = new ANTLRStringStream( text );
     NovelangLexer lexer = new NovelangLexer( stream );
//    lexer.setProblemDelegate( delegate ) ;
     CommonTokenStream tokens = new CommonTokenStream( lexer );
     parser = new NovelangParser( tokens ) ;
     parser.setTreeAdaptor( new CustomTreeAdaptor( delegate.getLocationFactory() ) ) ;
     parser.setGrammarDelegate( delegate ) ;
   }
 
   public boolean hasProblem() {
     return delegate.getProblems().iterator().hasNext() ;
   }
 
   public Iterable< Problem > getProblems() {
     final List< Problem > problems = Lists.newArrayList() ;
     Iterables.addAll( problems, delegate.getProblems() ) ;
     return ImmutableList.copyOf( problems ) ;
   }
 
   public abstract SyntacticTree parse() throws RecognitionException ;
 
   protected NovelangParser getAntlrParser() {
     return parser ;
   }
 
   public GrammarDelegate getDelegate() {
     return delegate ;
   }
 }
