 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.analytics.text.summarization;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Set;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypes;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.datatypes.BasicDataTypes.StringsMap;
 import org.seasr.meandre.components.tools.Names;
 
 import cern.colt.matrix.DoubleMatrix1D;
 import cern.colt.matrix.DoubleMatrix2D;
 import cern.colt.matrix.impl.DenseDoubleMatrix1D;
 import cern.colt.matrix.impl.SparseDoubleMatrix2D;
 import cern.colt.matrix.linalg.Algebra;
 import cern.jet.math.Functions;
 
 /**
  * This component ranks and sorts tokenized sentences. Useful for summarization
  *
  * @author Xavier Llor&agrave;
  * @author Boris Capitanu
  *
  */
 
 @Component(
 		name = "HITS Summarizer",
 		creator = "Xavier Llora",
 		baseURL = "meandre://seasr.org/components/tools/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "tools, text, tokenizer, sentences, summarization",
 		description = "This component ranks and sorts the tokenized input sentences " +
 				      "providing a simple summarization by sentence seletion.",
 		dependency = {"colt.jar","protobuf-java-2.0.3.jar"}
 )
 public class HITSSummarizer extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TOKENIZED_SENTENCES,
 			description = "The tokenized sentences"
 	)
 	protected static final String IN_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_SENTENCES,
 			description = "Sorted sentences"
 	)
 	protected static final String OUT_SENTENCES = Names.PORT_SENTENCES;
 
 	@ComponentOutput(
 			name = Names.PORT_TOKENS,
 			description = "Sorted tokens"
 		)
 	private final static String OUT_TOKENS = Names.PORT_TOKENS;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = Names.PROP_N_TOP_TOKENS,
             description = "Number of top tokens to output. -1 outputs all the tokens",
             defaultValue = "20"
     )
     protected static final String PROP_N_TOP_TOKENS = Names.PROP_N_TOP_TOKENS;
 
     @ComponentProperty(
             name = Names.PROP_N_TOP_SENTENCES,
             description = "Number of top sentences to output. -1 outputs all the sentences ",
             defaultValue = "6"
     )
     protected static final String PROP_N_TOP_SENTENCES = Names.PROP_N_TOP_SENTENCES;
 
     @ComponentProperty(
             name = Names.PROP_ITERATIONS,
             description = "Number of iterations to run. ",
             defaultValue = "10"
     )
     protected static final String PROP_ITERATIONS = Names.PROP_ITERATIONS;
 
 	//--------------------------------------------------------------------------------------------
 
 
 	/** Number of tokens to output */
 	private int iNTopTokens;
 
 	/** Number of iterations */
 	private int iIterations;
 
 	/** Number of sentences to output */
 	private int iNTopSentences;
 
 	/** The algebra object */
 	private Algebra alg;
 
 
 	//--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 		this.iNTopSentences = Integer.parseInt(ccp.getProperty(PROP_N_TOP_SENTENCES));
 		this.iNTopTokens = Integer.parseInt(ccp.getProperty(PROP_N_TOP_TOKENS));
 		this.iIterations = Integer.parseInt(ccp.getProperty(PROP_ITERATIONS));
 		this.alg = new Algebra();
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		StringsMap sm = (StringsMap) cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES);
 		Map<String, Integer> mapTokenToPos = new Hashtable<String,Integer>();
 		SparseDoubleMatrix2D sdm = convertTokenizedSentencesToSparseMatrix(sm, mapTokenToPos);
 		DoubleMatrix1D[] scores = hits(sdm);
 		pushSentence(scores[0], sm);
 		pushTokens(scores[1], mapTokenToPos);
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         this.iNTopSentences = this.iNTopTokens = this.iIterations = -1;
         this.alg = null;
     }
 
 	//--------------------------------------------------------------------------------------------
 
 	@Override
 	protected void handleStreamInitiators() throws Exception {
 	    componentContext.pushDataComponentToOutput(OUT_SENTENCES,
 	            componentContext.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));
 	    componentContext.pushDataComponentToOutput(OUT_TOKENS,
 	            componentContext.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));
 	}
 
 	@Override
 	protected void handleStreamTerminators() throws Exception {
 	    componentContext.pushDataComponentToOutput(OUT_SENTENCES,
 	            componentContext.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));
 	    componentContext.pushDataComponentToOutput(OUT_TOKENS,
 	            componentContext.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));
 	}
 
     //--------------------------------------------------------------------------------------------
 
 	/** Internal class for sorting purposes */
 	class Entry {
 		public String sText;
 		public Double dScore;
 
 		public Entry ( String s, Double d ) {
 			sText = s;
 			dScore = d;
 		}
 
 		@Override
         public String toString () {
 			return "<"+sText+", "+dScore+">";
 		}
 	}
 
 
 	/** Push the ranked sentences.
 	 *
 	 * @param score The score
 	 * @param sm The sentences
 	 * @throws ComponentContextException Something went wrong while pushing
 	 */
 	private void pushSentence(DoubleMatrix1D score, StringsMap sm)
 	throws ComponentContextException {
 		// Prepare the list of sentence
 		int iSent = sm.getKeyCount();
 		Entry [] ea = new Entry[iSent];
 		for ( int i=0 ; i<iSent ; i++ )
 			ea[i] = new Entry(sm.getKey(i),score.getQuick(i));
 
 		// Sort them based on the score
 		Arrays.sort(ea, new Comparator<Entry>(){
             public int compare(Entry o1,Entry o2) {
                return (o2.dScore>o1.dScore)?1:(o2.dScore==o1.dScore)?0:-1;
             }} );
 
 		// Push them out
 		org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
 		int iMax = (this.iNTopSentences<0)?ea.length:(this.iNTopSentences>ea.length)?ea.length:this.iNTopSentences;
 		for ( int i=0 ; i<iMax ; i++ )
 			res.addValue(ea[i].sText);
 
 		componentContext.pushDataComponentToOutput(OUT_SENTENCES, res.build());
 	}
 
 	/** Push the ranked tokens.
 	 *
 	 * @param score The score
 	 * @param sm The sentences
	 * @throws ComponentContextException Something went wrong while pusshing
 	 */
 	private void pushTokens(DoubleMatrix1D doubleMatrix1D, Map<String, Integer> mapTokenToPos)
 	throws ComponentContextException {
 
 		// Create the proper list of tokens
 		Set<String> set = mapTokenToPos.keySet();
 		int setSize = set.size();
 		String [] saIndex = new String[setSize];
 		for ( String sKey:set )
 			saIndex[mapTokenToPos.get(sKey)] = sKey;
 
 		// Prepare the list of tokens to sort
 		Entry [] ea = new Entry[setSize];
 		for ( int i=0 ; i<setSize ; i++ )
 			ea[i] = new Entry(saIndex[i],doubleMatrix1D.getQuick(i));
 
 		// Sort
 		Arrays.sort(ea, new Comparator<Entry>(){
             public int compare(Entry o1,Entry o2) {
                return (o2.dScore>o1.dScore)?1:(o2.dScore==o1.dScore)?0:-1;
             }} );
 
 		// Push it out
 		org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
 		int iMax = (this.iNTopTokens<0)?ea.length:(this.iNTopTokens>ea.length)?ea.length:this.iNTopTokens;
 		for ( int i=0 ; i<iMax ; i++ )
 			res.addValue(ea[i].sText);
 
 		componentContext.pushDataComponentToOutput(OUT_TOKENS, res.build());
 	}
 
 
 	/** Computes the HITS score for the given matrix returning the scores for hubs and authorities.
 	 *
 	 * @return The first one is the sentence ranking whereas the second is the token score
 	 */
 	@SuppressWarnings("static-access")
 	private DoubleMatrix1D[] hits(SparseDoubleMatrix2D w) {
 		/* My verified implementation of hits in python using numpy
 
 		   def hits ( W, K=10 ) :
 		     '''Computes the hits score for a weight matrix'''
 		     cont_size, term_size = W.shape
 		     cs, ts = ones(cont_size), ones(term_size)
 		     res = []
 		     cs = cs.reshape((cont_size,1))
 		     for k in xrange(K) :
 		          ncs = sum(w*ts,1)
 		          cs = ncs/(sum(ncs*ncs)**.5)
 		          cs = cs.reshape((cont_size,1))
 		          nts = sum(cs*w,0)
 		          ts = nts/(sum(nts*nts)**.5)
 		          res.append((cs,ts))
 		     return res
 		*/
 
 		Functions F = cern.jet.math.Functions.functions;
 		DoubleMatrix2D wt = alg.transpose(w);
 
 		int cont_size = w.rows();
 		int term_size = w.columns();
 		DoubleMatrix1D cs  = new DenseDoubleMatrix1D(cont_size); cs.assign(1.0);
 		DoubleMatrix1D ncs = new DenseDoubleMatrix1D(cont_size);
 		DoubleMatrix1D ts  = new DenseDoubleMatrix1D(term_size); ts.assign(1.0);
 		DoubleMatrix1D nts = new DenseDoubleMatrix1D(term_size);
 		double tmp;
 		for ( int i=0, iMax=this.iIterations ; i<iMax ; i++ ) {
 			ncs = alg.mult(w, ts); tmp = Math.sqrt(ncs.zDotProduct(ncs));
 			ncs.assign(F.mult(1/tmp));
 			cs.assign(ncs);
 			nts = alg.mult(wt, cs); tmp = Math.sqrt(nts.zDotProduct(nts));
 			nts.assign(F.mult(1/tmp));
 			ts.assign(nts);
 		}
 
 		return new DoubleMatrix1D [] {cs,ts};
 	}
 
 
 	private SparseDoubleMatrix2D convertTokenizedSentencesToSparseMatrix(
 			StringsMap sm, Map<String, Integer> mapTokenToPos) {
 		// Populate the map
 		populateTranslator(sm,mapTokenToPos);
 		// Create the matrix
 		SparseDoubleMatrix2D sdm = new SparseDoubleMatrix2D(sm.getKeyCount(),mapTokenToPos.keySet().size());
 		// Populate the matrix
 		int iSentence = 0;
 		for ( Strings strs:sm.getValueList() ) {
 			for ( String s:strs.getValueList() ) {
 				sdm.setQuick(iSentence, mapTokenToPos.get(s), 1.0f);
 			}
 			iSentence++;
 		}
 
 		// Return the matrix
 		return sdm;
 	}
 
 	/** Populates the given map with a token position translator.
 	 *
 	 * @param sm The string map
 	 * @param mapTokenToPos The mapt to filled
 	 * @return The same map passed
 	 */
 	private Map<String, Integer> populateTranslator(StringsMap sm,
 			Map<String, Integer> mapTokenToPos) {
 		int iCnt = 0;
 		for ( Strings strs:sm.getValueList() )
 			for ( String s:strs.getValueList() )
 				if ( !mapTokenToPos.containsKey(s) ) {
 					mapTokenToPos.put(s, iCnt++);
 				}
 		return mapTokenToPos;
 	}
 
 	@SuppressWarnings("static-access")
 	public static void main ( String [] sa ) {
 		/*
 		  w = array([[1, 0, 0, 0, 1, 0, 0, 1, 0],
                [0, 1, 0, 0, 0, 0, 0, 0, 0],
                [1, 1, 0, 1, 1, 0, 0, 0, 0]])
 
 		   def hits ( W, K=10 ) :
 		     '''Computes the hits score for a weight matrix'''
 		     cont_size, term_size = W.shape
 		     cs, ts = ones(cont_size), ones(term_size)
 		     res = []
 		     cs = cs.reshape((cont_size,1))
 		     for k in xrange(K) :
 		          ncs = sum(w*ts,1)
 		          cs = ncs/(sum(ncs*ncs)**.5)
 		          cs = cs.reshape((cont_size,1))
 		          nts = sum(cs*w,0)
 		          ts = nts/(sum(nts*nts)**.5)
 		          res.append((cs,ts))
 		     return res
 		*/
 
 		SparseDoubleMatrix2D w = new SparseDoubleMatrix2D(3,9);
 		w.assign(0);
 		w.setQuick(0,0,1); w.setQuick(0,4,1); w.setQuick(0,7,1);
 		w.setQuick(1,1,1);
 		w.setQuick(2,0,1); w.setQuick(2,1,1); w.setQuick(2,3,1); w.setQuick(2,4,1);
 
 		Functions F = cern.jet.math.Functions.functions;
 		Algebra alg = new Algebra();
 		DoubleMatrix2D wt = alg.transpose(w);
 
 		System.out.println(w);
 
 		int cont_size = w.rows();
 		int term_size = w.columns();
 		DoubleMatrix1D cs  = new DenseDoubleMatrix1D(cont_size); cs.assign(1.0);
 		DoubleMatrix1D ncs = new DenseDoubleMatrix1D(cont_size);
 		DoubleMatrix1D ts  = new DenseDoubleMatrix1D(term_size); ts.assign(1.0);
 		DoubleMatrix1D nts = new DenseDoubleMatrix1D(term_size);
 		double tmp;
 		for ( int i=0, iMax=10 ; i<iMax ; i++ ) {
 			ncs = alg.mult(w, ts); tmp = Math.sqrt(ncs.zDotProduct(ncs));
 			ncs.assign(F.mult(1/tmp));
 			cs.assign(ncs);
 			System.out.println(cs);
 			nts = alg.mult(wt, cs); tmp = Math.sqrt(nts.zDotProduct(nts));
 			nts.assign(F.mult(1/tmp));
 			ts.assign(nts);
 			System.out.println(ts);
 
 		}
 	}
 }
