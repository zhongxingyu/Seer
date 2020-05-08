 package hmm;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class UnsupervisedTrainingAlgorithm {
 	private static Random random=new Random(3);
 	public static OptimizedStateCollection train(ArrayList<ArrayList<String>> trainingSentenceStrings, int stateCount){
 		OptimizedStateCollection hmm=new OptimizedStateCollection();
 		hmm.states.remove(hmm.unknownState().name);
 
 		// initialize states
 		for (int i=0; i<stateCount; i++){
 			hmm.getStateTraining("s"+i);
 		}
 
 		// setup random state transistion probabilities
 		for (OptimizedState a: hmm.getStates()){
 			for (OptimizedState b: hmm.getStates()){
 				a.setNextStateProbability(b, BigDouble.valueOf(random.nextDouble()));
 			}
 		}
 
 		ArrayList<ArrayList<Word>> trainingSentences=new ArrayList<ArrayList<Word>>();
 
 		// read all words
 		for (List<String> list: trainingSentenceStrings){
 			ArrayList<Word> sentence=new ArrayList<Word>();
 			for (String word: list){
 				String[] parts=word.split("/");
 				sentence.add(hmm.getWordTraining(parts[0]));
 			}
 			trainingSentences.add(sentence);
 		}
 
 		// setup random word emission probabilities
 		for (OptimizedState a: hmm.getStates())
 			for (Word w: hmm.words.values())
 				a.setWordEmissionProbability(w, BigDouble.valueOf(random.nextDouble()));
 
 		// clear transitions to the start
 		for (OptimizedState a: hmm.getStates()){
 			a.setNextStateProbability(hmm.startState(), BigDouble.ZERO);
 		}
 		// normalize all probabilities
 		for (OptimizedState a: hmm.getStates())
 			a.normalize();
 
 		BigDouble oldProbability=BigDouble.valueOf(-1);
 		BigDouble probability=BigDouble.ZERO;
 
 		// optimization loop
 		do{
 			// create forward and backward algorithms
 			ArrayList<ForwardAlgorithm<OptimizedStateCollection, OptimizedState>>
 			alphas=new ArrayList<ForwardAlgorithm<OptimizedStateCollection,OptimizedState>>();
 
 			ArrayList<BackwardAlgorithm<OptimizedStateCollection, OptimizedState>>
 			betas=new ArrayList<BackwardAlgorithm<OptimizedStateCollection,OptimizedState>>();
 
 			//System.out.print(hmm);
 			for (List<Word> list: trainingSentences){
 				ForwardAlgorithm<OptimizedStateCollection, OptimizedState> alpha = new ForwardAlgorithm<OptimizedStateCollection, OptimizedState>(hmm,list);
 				alphas.add(alpha);
 				BackwardAlgorithm<OptimizedStateCollection, OptimizedState> beta = new BackwardAlgorithm<OptimizedStateCollection, OptimizedState>(hmm, list);
 				betas.add(beta);
 				BigDouble a=alpha.getFinalProbability();
 				BigDouble b=beta.getFinalProbability();
 				if (a.add(b).compareTo(BigDouble.ZERO)!=0){
 					if ((a.subtract(b).abs().divide(a.add(b).abs()).compareTo(BigDouble.valueOf(1e-5))>0))
 						throw new Error("Alpha and Beta do not match");
 				}
 			}
 
 			// calculate the probability of the input under the current HMM
 			probability=BigDouble.ONE;
 			{
 				int i=0;
 				for (List<Word> list: trainingSentences){
 					probability=probability.multiply(alphas.get(i).getFinalProbability());
 					i++;
 				}
 			}
 
 			System.out.println(probability);
 			//System.out.println(hmm);
 
 			// optimize while the probability of the output increases by at least 10 percent
 			if (oldProbability.multiply(BigDouble.valueOf(1.1)).compareTo(
					probability) > 0 || true)
 				return hmm;
 
 
 			oldProbability=probability;
 
 			// update probabilities of a new HMM (M-step)
 			OptimizedStateCollection newHmm=copyStateCollection(hmm);
 
 			// set transition probabilities
 			for (OptimizedState a: hmm.getStates()){
 				if (a==hmm.endState()) continue;
 				OptimizedState newA=newHmm.getState(a.name);
 				BigDouble denominator=BigDouble.ZERO;
 				for (OptimizedState b: hmm.getStates()){
 					if (b==hmm.startState()) continue;
 					OptimizedState newB=newHmm.getState(b.name);
 
 					// calculate the numerator
 					BigDouble numerator=BigDouble.ZERO;
 					int i=0;
 					for (List<Word> sentence: trainingSentences){
 						for( int t=-1; t<sentence.size(); t++){
 							BigDouble d=
 								xi(t,sentence,a,b,alphas.get(i),betas.get(i),hmm)
 								.multiply(BigDouble.valueOf(sentence.size())); //weight by sentence length
 							numerator=numerator.add(d);
 							denominator=denominator.add(d);
 						}
 						i++;
 					}
 					newA.setNextStateProbability(newB, numerator);
 				}
 
 				// normalize with the denominator
 				// this can only be done after iteration over all b's above
 				if (denominator.compareTo(BigDouble.ZERO)==0) denominator=BigDouble.ONE;
 				for (OptimizedState b: hmm.getStates()){
 					if (b==hmm.startState()) continue;
 					OptimizedState newB=newHmm.getState(b.name);
 					newA.setNextStateProbability(newB,
 							newA.nextStateProbability(newB).divide(denominator));
 				}
 			}
 
 			// set emission probabilities
 			for (OptimizedState a: hmm.getStates()){
 				if (a==hmm.endState()) continue;
 				if (a==hmm.startState()) continue;
 				OptimizedState newA=newHmm.getState(a.name);
 
 				// calculate the denominator
 				BigDouble denominator=BigDouble.ZERO;
 				{
 					int i=0;
 					for (List<Word> sentence: trainingSentences){
 						int t=0;
 						for( Word w: sentence){
 							denominator=denominator.add(
 									gamma(t,a,alphas.get(i),betas.get(i))
 									.multiply(BigDouble.valueOf(sentence.size()))); //weight by sentence length
 							t++;
 						}
 						i++;
 					}
 				}
 				if (denominator.compareTo(BigDouble.ZERO)==0) denominator=BigDouble.ONE;
 
 				// set the probabilities for all words
 				for (Word word: hmm.words.values()){
 					BigDouble numerator=BigDouble.ZERO;
 					int i=0;
 					for (List<Word> sentence: trainingSentences){
 						int t=0;
 						for( Word w: sentence){
 							if (w==word)
 								numerator=numerator.add(
 										gamma(t,a,alphas.get(i),betas.get(i))
 										.multiply(BigDouble.valueOf(sentence.size()))); //weighten by sentence length
 							t++;
 						}
 						i++;
 					}
 					newA.setWordEmissionProbability(word, numerator.divide(denominator));
 				}
 
 			}
 			hmm=newHmm;
 			//return hmm;
 		} while (true); // return statement is located above
 
 		// never reached
 	}
 
 	/**
 	 * Formulae according to "Speech and language processing" by Daniel Jurafsky and James H. Martin
 	 * Xi=Probability of traversing from state i to state j at time t
 	 * Xi_t(i,j)=P(q_t=i,q_{t+1}=j | O,\lambda)
 	 * =\frac{\alpha_t(i)a_{ij}b_j(o_{t+1})\beta_{t+1}(j)}{\alpha_T(N)}
 	 * 
 	 * @param t	input position
 	 * @param sentence input sentence
 	 * @param a state i
 	 * @param b state j
 	 * @param alpha forward probability
 	 * @param beta backward probability
 	 * @param hmm
 	 * @return
 	 */
 	private static BigDouble xi(int t, List<Word> sentence, OptimizedState a, OptimizedState b, ForwardAlgorithm<OptimizedStateCollection,OptimizedState> alpha, BackwardAlgorithm<OptimizedStateCollection,OptimizedState> beta, OptimizedStateCollection hmm){
 		BigDouble
 		result=alpha.get(t, a);
 		result=result.multiply(a.nextStateProbability(b));
 		if ((t+1)<sentence.size())
 			result=result.multiply(b.wordEmittingProbability(sentence.get(t+1)));
 		result=result.multiply(beta.get(t+1,b));
 		if (alpha.getFinalProbability().compareTo(BigDouble.ZERO)!=0)
 			result=result.divide(alpha.getFinalProbability());
 		return result;
 	}
 
 	/**
 	 * Formulae according to "Speech and language processing" by Daniel Jurafsky and James H. Martin
 	 * Gamma==probability of beeing in state j at time t
 	 * @param t time
 	 * @param w word at time t
 	 * @param a state j
 	 * @param alpha
 	 * @param beta
 	 * @return
 	 */
 	private static BigDouble gamma(int t, OptimizedState a, ForwardAlgorithm<OptimizedStateCollection,OptimizedState> alpha, BackwardAlgorithm<OptimizedStateCollection,OptimizedState> beta){
 		return
 		alpha.get(t,a)
 		.multiply(beta.get(t,a))
 		.divide(alpha.getFinalProbability());
 
 	}
 	private static OptimizedStateCollection copyStateCollection(OptimizedStateCollection other){
 		OptimizedStateCollection hmm=new OptimizedStateCollection();
 
 		// clear data
 		hmm.words.clear();
 		hmm.states.clear();
 
 		// add states
 		for (OptimizedState s: other.states.values()){
 			hmm.getStateTraining(s.name);
 		}
 
 		// add words
 		for (Word w: other.words.values()){
 			hmm.words.put(w.name,w);
 		}
 
 		return hmm;
 	}
 }
