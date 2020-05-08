 package maro.wrapper;
 
 // my only
 import maro.core.EmotionKnowledge;
 import maro.core.BBKeeper;
 
 //java only
 import java.util.Iterator;
 import java.util.Set;
 import java.util.NoSuchElementException;
 
 // jason only
 import jason.bb.BeliefBase;
 import jason.asSyntax.Term;
 import jason.asSyntax.Literal;
 import jason.bb.ChainBBAdapter;
 import jason.asSemantics.Agent;
 import jason.asSyntax.NumberTerm;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.PredicateIndicator;
 
 // others only
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 // Nao sei, me parece q ta dando problema de acesso com corrente.
 // mas nao temos acesso concorrente, temos?
 public class BBAffective extends ChainBBAdapter
 {
 	protected EmotionKnowledge ek = null;
 	private Agent myAgent = null;
 	protected String myName = null;
 
 	/**
 	 * Called before the MAS execution with the agent that uses this
 	 * BB and the args informed in .mas2j project.<br>
 	 * Example in .mas2j:<br>
 	 *     <code>agent BeliefBaseClass(1,bla);</code><br>
 	 * the init args will be ["1", "bla"].
 	 */
 	@Override
 	public void
 	init(Agent ag, String[] args) {
 		myAgent = ag;
 		myName = myAgent.getTS().getUserAgArch().getAgName();
 		BBKeeper.getInstance().put(myName, this);
 
 		if (args.length == 1) {
 			try {
 				ek = new EmotionKnowledge(myName, args[0]);
 
 				ek.loadPreferences(nextBB);
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.exit(22);
 			}
 		}
 	}
 
 	/** Called just before the end of MAS execution */
 	@Override
 	public void
 	stop() {
 		ek.dumpData();
 	}
 
 	/** Returns the number of beliefs in BB */
 	@Override
 	public int
 	size() {
 		// nao pareceu ser chamado em nenhum ponto...
 		System.err.println("lucca size()");
 		return super.size();
 	}
 
 	/** Adds a belief in the begin/end of the BB, returns true if succeed.
 	 *  The annots of l may be changed to reflect what was changed in the BB,
 	 *  for example, if l is p[a,b] in a BB with p[a], l will be changed to
 	 *  p[b] to produce the event +p[b], since only the annotation b is changed
 	 *  in the BB. */
 	protected boolean
 	add(Literal l, boolean addInEnd) {
 		Dumper d = null;
 		boolean ret = false;
 		int arity = l.getArity();
 
 		// Se eh uma percepcao nao temos interesse...
 		if (l.hasAnnot(BeliefBase.TPercept)) {
 			// ... mas temos interesse no caso da crenca step para atualizar o resumo
 			if (l.getFunctor().equals("step") == false)
 				return false;
 
 			NumberTerm nt = (NumberTerm) l.getTerm(0);
 			int step = (int) nt.solve();
 			Set<String> feels; // temos que botar a anotacao de percepcao ainda!
 
 			ek.summarize(step);
 			feels = ek.feelings(step);
 			for (String f: feels) {
 				Literal lit = Dumper.fromString(f, 20);
 				lit.addAnnot(BeliefBase.TPercept);
 				// sim, sentimentos são conclusoes da ontologia que ficam
 				// na base de dados mais interna...
 				super.add(lit);
 			}
 
 			return false;
 		}
 		// Se for o functor 'sameAs' aceitamos, independente da aridade
 		// Caso contrario, tem que ser relevante
 		if (!l.getFunctor().equals("sameAs") && !ek.isRelevant(arity, l.getFunctor()))
 			return false;
 
 		d = Dumper.dumpLiteral(l);
 		ret = ek.add(d);
 		//System.err.println("lucca add " + d + " = " + ret);
 		return ret;
 	}
 
 	public Set<String> getEmotionType() {
 		if (ek == null) return null;
 		return ek.getEmotionType();
 	}
 	public Integer getEmotionValence(String emotion) {
 		if (ek == null) return null;
 		return ek.getEmotionValence(emotion, myName);
 	}
 	public Integer getEmotionPotence(String emotion) {
 		if (ek == null) return null;
 		return ek.getEmotionPotence(emotion, myName);
 	}
 
 	@Override
 	public boolean
 	add(Literal l) {
 		boolean ret = add(l, false);
 		if (ret) return true;
         Literal inbb = super.contains(l);
         if (inbb != null) inbb.clearAnnots();
 		return super.add(l);
 	}
 
 	@Override
 	public boolean
 	add(int index, Literal l) {
 		boolean ret = add(l, index != 0);
 		if (ret) return true;
         Literal inbb = super.contains(l);
         if (inbb != null) inbb.clearAnnots();
 		return super.add(index, l);
 	}
 
 	/** Returns an iterator for all beliefs. */
 	@Override
 	public Iterator<Literal>
 	iterator() {
 		final Iterator<Literal> ret = checkIteratorNull(super.iterator());
 		final Iterator<Dumper> id = checkIteratorNull(ek.iterator());
 
 		return new Iterator<Literal> () {
 			@Override
 			public boolean hasNext() {
 				return id.hasNext() || ret.hasNext();
 			}
 			@Override
 			public Literal next() {
                 if (id.hasNext() == false) return ret.next();
                 return Dumper.fromDumper(id.next(), 43);
 			}
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 
 	protected Iterator<Literal>
 	getCandidateBeliefs2(PredicateIndicator pi)
 	{
         final String functor = pi.getFunctor();
         final int arity = pi.getArity();
 		final Iterator<Dumper> id = ek.getCandidatesByFunctorAndArityIter(functor, arity);
 		//System.err.println("lucca iter: " + id + " for " + functor + "/" + arity);
 		if (id == null)  return null;
         final Iterator<Literal> lit = checkIteratorNull(super.getCandidateBeliefs(pi));
 		return new Iterator<Literal> () {
 			@Override
 			public boolean hasNext() {
 				return id.hasNext() || lit.hasNext();
 			}
 			@Override
 			public Literal next() {
                 if (id.hasNext() == false) return lit.next();
                 return Dumper.fromDumper(id.next(), 39);
 			}
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 
 	/**
 	 * Returns an iterator for all literals in BB that match the functor/arity 
 	 * of the parameter.<br>
 	 */
 	@Override
 	public Iterator<Literal>
 	getCandidateBeliefs(PredicateIndicator pi)
 	{
 		Iterator<Literal> it = getCandidateBeliefs2(pi);
 		if (it != null) return it;
 		return super.getCandidateBeliefs(pi);
 	}
 
 	/**
 	 * Returns an iterator for all literals relevant for l's predicate
 	 * indicator, if l is a var, returns all beliefs.<br>
 	 *
 	 * The unifier <i>u</i> may contain values for variables in <i>l</i>.
 	 *
 	 * Example, if BB={a(10),a(20),a(2,1),b(f)}, then
 	 * <code>getCandidateBeliefs(a(5), {})</code> = {a(10),a(20)}.<br>
 	 * if BB={a(10),a(20)}, then <code>getCandidateBeliefs(X)</code> =
 	 * {a(10),a(20)}. The <code>getCandidateBeliefs(a(X), {X -> 5})</code> 
 	 * should also return {a(10),a(20)}.<br>
 	 */
 	@Override
 	public Iterator<Literal>
 	getCandidateBeliefs(Literal l, Unifier u)
 	{
 		if (l.isVar()) {
 			// all bels are relevant
 			return iterator();
 		}
 
 		return getCandidateBeliefs(l.getPredicateIndicator());
 	}
 
 	/**
 	 * Returns the literal l as it is in BB, this method does not
 	 * consider annotations in the search. <br> Example, if
 	 * BB={a(10)[a,b]}, <code>contains(a(10)[d])</code> returns
 	 * a(10)[a,b].
 	 */
 	@Override
 	public Literal
 	contains(Literal l)
 	{
 		Iterator<Literal> il = getCandidateBeliefs(l.getPredicateIndicator());
 
 		if (il != null) {
 			while (il.hasNext()) {
 				Literal lit = il.next();
 				Term[] litS = lit.getTermsArray();
 				Term[] lS = l.getTermsArray();
 				boolean ok = true;
 
 				for (int idx = 0; ok && idx < lS.length; idx++) {
 					if (litS[idx].toString().equals(lS[idx].toString()) == false)
 						ok = false;
 				}
 
 				if (ok == true) return lit;
 			}
 		}
 
 		return super.contains(l);
 	}
 
 	/** Returns all beliefs that have "percept" as source */
 	//	public Iterator<Literal> getPercepts() // LATER: por enquanto nao teremos percepcoes aqui
 	//	{
 	//		// passar pela ontologia primeiro e depois pelo default...
 	//		// ...os dados da ontologia seriam por enquanto so crencas...
 	//		// ...todavia uma outra poderia ser feita para mapear o
 	//		// eu ''externo'' com o eu ''interno'' ?!
 	//		Iterator<Literal> perceived = super.getPercepts();
 	//
 	//		while (perceived != null && perceived.hasNext()) {
 	//			Literal lit = perceived.next();
 	//			System.err.println("lucca "+ myName
 	//						+".getPercepts(): "+lit);
 	//		}
 	//
 	//		return super.getPercepts();
 	//	}
 
 	/** Removes a literal from BB, returns true if succeed */
 	@Override
 	public boolean
 	remove(Literal l)
 	{
 		Dumper d = null;
 		boolean ret = false;
 		int arity = l.getArity();
 		Literal s;
 
 		// Se eh uma percepcao nao temos interesse...
 		if (l.hasAnnot(BeliefBase.TPercept)) {
 			return super.remove(l);
 		}
 		// Se for o functor 'sameAs' aceitamos, independente da aridade...
 		// Caso contrario, tem que ser relevante
 		if (!l.getFunctor().equals("sameAs") && !ek.isRelevant(arity, l.getFunctor()))
 			return super.remove(l);
 
 		s = contains(l);
 		//l.delAnnot( BeliefBase.TSelf );
 		if (s == null) return super.remove(l);
 
 		s.delAnnots(l.getAnnots());
 
 		if (s.hasAnnot() == true) {
 			if (s.hasSource() == false) s.addAnnot( BeliefBase.TSelf );
 			d = Dumper.dumpLiteral(s);
 			ret = ek.add(d); // remove and replace
 		} else {
 			d = Dumper.dumpLiteral(l);
 			ret = ek.remove(d);
 		}
 
 		//System.err.println("lucca del " + d + " = " + ret + " based on " + l);
 		if (ret == false) return super.remove(l);
 		return ret;
 	}
 
 	/** Removes all believes with some functor/arity */
 	@Override
 	public boolean
 	abolish(PredicateIndicator pi)
 	{
 		// Visto no codigo e parece que nao ocorre mais,
 		// o agente chama direto cada um a ser removido
 		// baseado no literal...
 		System.err.println("abolish(PredicateIndicator) not implemented");
 		System.exit(18);
 		return super.abolish(pi);
 	}
 
 	// So foi necessario sobrecarregar isso porque o iterator
 	// da nossa classe por algum motivo nao era chamado... :/
 	@Override
 	public Element
 	getAsDOM(Document document) {
 		// codigo copiado do DefaultBeliefBase
 		int tries = 0;
 		Element ebels = null;
 		while (tries < 10) {
 			ebels = (Element) document.createElement("beliefs");
 			try {
 				for (Literal l: this)
 					ebels.appendChild(l.getAsDOM(document));
 				break;
 			} catch (Exception e) {
 				tries++;
 			}
 		}
 		return ebels;
 	}
 
     private <T> Iterator<T>
     checkIteratorNull(Iterator<T> it) {
         if (it != null) return it;
 		return new Iterator<T> () {
 			@Override
 			public boolean hasNext() {
 				return false;
 			}
 			@Override
 			public T next() {
                 throw new NoSuchElementException();
 			}
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
     }
 }
