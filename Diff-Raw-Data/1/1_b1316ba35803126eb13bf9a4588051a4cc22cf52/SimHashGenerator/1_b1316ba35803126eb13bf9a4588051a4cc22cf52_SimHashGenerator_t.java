 /*
  * Copyright (c) 2010 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License.  You may
  * obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.  See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package iudex.simhash.filters;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.gravitext.htmap.Key;
 import com.gravitext.htmap.UniMap;
 import com.gravitext.xml.tree.Element;
 import com.gravitext.xml.tree.Node;
 
 import iudex.filter.Described;
 import iudex.filter.Filter;
 import iudex.filter.FilterException;
 import iudex.html.tree.HTMLTreeKeys;
 import iudex.html.tree.TreeFilter;
 import iudex.html.tree.TreeFilterChain;
 import iudex.html.tree.TreeWalker;
 import iudex.html.tree.filters.MetaSkipFilter;
 import iudex.simhash.SimHashKeys;
 import iudex.simhash.gen.StopWordSet;
 import iudex.simhash.gen.TokenCounter;
 
 public class SimHashGenerator implements Filter, Described
 {
     public static final class Input
     {
         public static Input forText( Key<CharSequence> text )
         {
             return new Input( text, null );
         }
 
         public static Input forTree( Key<Element> tree )
         {
             return new Input( null, tree );
         }
 
         Input( Key<CharSequence> text, Key<Element> tree )
         {
             textKey = text;
             treeKey = tree;
         }
 
         public void setWordyRatio( float ratio )
         {
             wordyRatio = ratio;
         }
 
         @Override
         public String toString()
         {
             return textKey.name();
         }
 
         final Key<CharSequence> textKey;
         final Key<Element> treeKey;
         float wordyRatio = 0.0f;
     }
 
     public SimHashGenerator( List<Input> inputs )
     {
         this( inputs, StopWordSet.EMPTY_SET );
     }
 
     public SimHashGenerator( List<Input> inputs, StopWordSet stopWords )
     {
         _inputs = new ArrayList<Input>( inputs );
         _stopWords = stopWords;
     }
 
     @Override
     public List<?> describe()
     {
         return _inputs;
     }
 
     @Override
     public boolean filter( UniMap content ) throws FilterException
     {
         final TokenCounter counter = new TokenCounter( _stopWords );
 
         for( Input in : _inputs ) {
             if( in.textKey != null ) {
                 CharSequence text = content.get( in.textKey );
                 if( text != null ) counter.add( text );
             }
             else {
                 Element root = content.get( in.treeKey );
                 if( root != null ) {
                     float minW = minWordiness( root, in.wordyRatio );
                     TokenWalker walker = new TokenWalker( counter, minW );
                     TreeFilterChain chain =
                         new TreeFilterChain( Arrays.asList( new MetaSkipFilter(),
                                                             walker ) );
                     TreeWalker.walkBreadthFirst( chain, root );
                 }
             }
         }
 
         //Note: Set hash value zero for no input (i.e. the empty doc)
         content.set( SimHashKeys.SIMHASH, counter.simhash() );
 
         return true;
     }
 
     private float minWordiness( Element root, float wordyRatio )
     {
         float overall = root.get( HTMLTreeKeys.WORDINESS );
         return (float) ( Math.floor( overall * wordyRatio / WORDY_STEP )
                          * WORDY_STEP );
     }
 
     private static final class TokenWalker
         implements TreeFilter
     {
         TokenWalker( TokenCounter counter, float minWordiness )
         {
             _counter = counter;
             _minWordiness = minWordiness;
         }
 
         @Override
         public Action filter( Node node )
         {
             Action action = Action.CONTINUE;
 
             if( node.isElement() ) {
                 if( node.get( HTMLTreeKeys.WORDINESS ) < _minWordiness ) {
                     action = Action.SKIP;
                 }
             }
             else {
                 _counter.add( node.characters() );
             }
             return action;
         }
 
         private final TokenCounter _counter;
         private final float _minWordiness;
     }
 
     private static final float WORDY_STEP = 2.0f;
 
     private final List<Input> _inputs;
     private final StopWordSet _stopWords;
 }
