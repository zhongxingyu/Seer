 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Classe che rappresenta una rete di Bayes.
  * Contiente metodi per la manipolazione e l'inferenza.
  * <br><i>(alcune idee e parti del codice appartengono a Ravi Mohan - MIT)</i>
  */
 public class BayesNet {
 
 	/** Identifica la modalità operativa che lavora tramite la descrizione delle variabili casuali. */
 	public static final int MODE_DESCRIPTION = 0;
 
 	/** Identifica la modalità operativa che lavora tramite gli ID delle variabili casuali (SCONSIGLIATO). */
 	public static final int MODE_ID = 1;
 
 	/** lista dei nodi radice della net */
 	private List<BayesNetNode> roots = new ArrayList<BayesNetNode>();
 
 	/** lista di tutti i nodi */
 	private List<BayesNetNode> variableNodes;
 
 	/** Costruttore della rete (1 radice) */
 	public BayesNet(BayesNetNode root) {
 		roots.add(root);
 	}
 
 	/** Costruttore della rete (2 radici) */
 	public BayesNet(BayesNetNode root1, BayesNetNode root2) {
 		this(root1);
 		roots.add(root2);
 	}
 
 	/** Costruttore della rete (3 radici) */
 	public BayesNet(BayesNetNode root1, BayesNetNode root2, BayesNetNode root3) {
 		this(root1, root2);
 		roots.add(root3);
 	}
 
 	/** Costruttore della rete data una lista di radici*/
 	public BayesNet(List<BayesNetNode> rootNodes) {
 		roots = rootNodes;
 	}
 
 
 	/**
 	 * Ritorna una lista delle variabili di cui è composta la rete.
 	 * @return lista di variabili (stringhe)
 	 */
 	public List<String> getVariables() {
 		variableNodes = getVariableNodes();
 		List<String> variables = new ArrayList<String>();
 		for (BayesNetNode variableNode : variableNodes) {
 			variables.add(variableNode.getVariable());
 		}
 		return variables;
 	}
 
 	/**
 	 * Ritorna l'ID di una variabile a partire dalla sua descrizione.
 	 * @param desc descrizione della variabili
 	 * @return l'ID della variabile
 	 * @throws BayesBoxIOExc solleva un'eccezione se non viene trovata alcuna variabile con quella descrizione.
 	 */
 	public String getIdFromDescription(String desc) throws BayesBoxIOExc{
 		for(BayesNetNode n : variableNodes){
 			if(n.getDescription().equals(desc))return n.getVariable();
 		}
 		throw new BayesBoxIOExc("La descrizione ["+desc+"] non corrisponde a nessuna variabile.");
 	}
 
 	/**
 	 * Fornisce una tabella di ID di variabili a partire dalle loro descrizioni.
 	 * @param evidence tabella sorgente contenente le descrizioni delle variabili e i relativi valori.
 	 * @return una tabella indicizzata per ID delle variabili, con i rispettivi valori.
 	 * @throws BayesBoxIOExc solleva un'eccezione se non viene trovata alcuna variabile con una data descrizione.
 	 */
 	public Hashtable<String, Boolean> getIdFromDescription(Hashtable<String, Boolean> evidence) throws BayesBoxIOExc{
 		Hashtable<String, Boolean> ris = new Hashtable<String, Boolean>();
 		for(BayesNetNode n : variableNodes){
 			if(evidence.get(n.getDescription()) != null){
 				ris.put(n.getVariable(),evidence.get(n.getDescription()));
 				//System.out.println(n.getVariable()+evidence.get(n.getDescription())+"");
 			}
 		}
 		if(ris.size()!=evidence.size())throw new BayesBoxIOExc("La descrizione di una delle variabili non corrisponde a nessuna variabile.");
 		return ris;
 	}
 
 	/**
 	 * Ottiene la probabilità di un dato evento, data una certa evidenza.
 	 * @param Y la variabile che rappresenta un nodo/evento.
 	 * @param value valore interessato.
 	 * @param evidence evidenza
 	 * @return la probabilità dell'evento, data l'evidenza)
 	 */
 	public double probabilityOf(String Y, Boolean value, Hashtable<String, Boolean> evidence) {
 		BayesNetNode y = getNodeOf(Y);
 		if (y == null) {
 			throw new RuntimeException("Impossibile trovare il nodo di variabile "+ Y);
 		} else {
 
 			List<BayesNetNode> parentNodes = y.getParents();
 
 			// root nodes
 			if (parentNodes.size() == 0) {
 				Hashtable<String, Boolean> YTable = new Hashtable<String, Boolean>();
 				YTable.put(Y, value); //piccola forzatura dovuta alla rappresentazione "particolare" del nodo radice
 
 				double prob = y.probabilityOf(YTable);
 				return prob;
 
 			// non rootnodes
 			} else {
 				Hashtable<String, Boolean> parentValues = new Hashtable<String, Boolean>();
 				for (BayesNetNode parent : parentNodes) {
 					//if(evidence.get(parent.getVariable())!=null)
 					parentValues.put(parent.getVariable(), evidence.get(parent.getVariable()));
 				}
 				double prob = y.probabilityOf(parentValues);
 				
 				if (value.equals(Boolean.TRUE)) {
 					return prob;
 				} else {
 					return (1.0 - prob);
 				}
 
 			}
 		}
 	}
 
 
 	/**
 	 * Effettua inferenza esatta tramite metodo di enumerazione.
 	 * @param X variabile query.
 	 * @param evidence evidenza 
 	 * @param queryMode modalità della query, può essere<br> -<b> BayesNet.MODE_DESCRIPTION</b> (consigliata) <br>-<b> BayesNet.MODE_ID</b>
 	 * @return la distribuzione di probabilità per l'evento. return[0] = % true, return[1] = % false.
 	 * @throws BayesBoxIOExc
 	 */
 	public double[] enumerationAsk(String X, Hashtable<String, Boolean> evidence, int queryMode) throws BayesBoxIOExc{
 		if(queryMode == MODE_ID){
 			System.out.println("MODE_ID");
 			return enumerationAsk(X, evidence);
 		}
 		else // (queryMode == MODE_DESCRIPTION)
 			return enumerationAsk(getIdFromDescription(X), getIdFromDescription(evidence));
 	}
 
 	/**
 	 * Funzione interna che si occupa di eseguire effettivamente l'enumerazione.
 	 */
 	private double[] enumerationAsk(String X, Hashtable<String, Boolean> evidence){
 		System.out.println("Calling enumerationAsk");
 		Hashtable<String, Boolean> evidence2 =  cloneEvidenceVariables(evidence);
 
 		BayesNetNode q = getNodeOf(X);
 		double[] ris = new double[q.getStateNames().length]; 
 
 		evidence.put(X, true);
 		ris[0] = enumerateAll(getVariables(),evidence);
 
 		evidence2.put(X, false);
 		ris[1] = enumerateAll(getVariables(),evidence2);
 
 		System.out.println("senza normalizzare: "+ris[0]+ ", "+ris[1]);
 		return Util.normalize(ris);
 	}
 
 	/**
 	 * Metodo interno per l'enumerazione ricorsiva.
 	 */
 	private double enumerateAll(List<String> vars,Hashtable<String, Boolean> evidence){
 		//System.out.println("call enumerateALL with " + vars.size() + "vars"); 
 		if(vars.size()==0)return 1.0;
 
 		String Y = Util.first(vars);
 		System.out.println("Calcolo "+Y + " su -> "+vars.toString());
 		double probOf;
 		if(evidence.get(Y) == null){ //non compariva nell'evidenza
 			//System.out.println("NON compariva nell'evidenza");
 			double tmpRis=0.0;
 
 
 			boolean value=true;
 			for(int i=0;i<2;i++){
 				Hashtable<String, Boolean> newEvidence=cloneEvidenceVariables(evidence);
 				newEvidence.put(Y, value);
 
 				probOf = probabilityOf(Y, value, newEvidence);// getNodeOf(Y).getDistribution().probabilityOf(Y, value);
 				//System.out.println("prob posteriori:" +probOf);
 				tmpRis +=  probOf * enumerateAll(Util.rest(vars), newEvidence);
 				value=!value;  
 			} 
 			//System.out.println("tmpRis: +"+tmpRis);
 			return tmpRis;
 		}else{//compariva nell'evidenza 
 			//System.out.println("compariva nell'evidenza");
 			probOf = probabilityOf(Y, (Boolean)evidence.get(Y), evidence);//getNodeOf(Y).getDistribution().probabilityOf(Y, (Boolean)evidence.get(Y));
 			//System.out.println("prob posteriori:" +probOf);
 			return  probOf * enumerateAll(Util.rest(vars), evidence);
 		}
 
 	}
 
 
 	/**
 	 * Metodo di supporto che restituisce in forma comprensibile la tabella, associando alla var la sua descrizione
 	 * @param source tabella sorgente
 	 * @return tabella con descrizioni
 	 */
 	public Hashtable<String, Boolean> getComprensiveResult(Hashtable<?, ?> source){
 
 		Hashtable<String, Boolean> ris=new Hashtable<String, Boolean>();
 		for(BayesNetNode n : variableNodes){
 			ris.put(n.getDescription() +"(" +n.getVariable()+")", (Boolean) source.get(n.getVariable()));
 		}
 		return ris;
 	}
 	
 
 	/**
 	 * Metodo di inferenza tramite simulazione stocastica LikeliHood Weighting.
 	 * @param X variabile query.
 	 * @param evidence evidenza.
 	 * @param numberOfSamples numero di campioni da generare.
 	 * @param queryMode modalità della query, può essere<br> -<b> BayesNet.MODE_DESCRIPTION</b> (consigliata) <br>-<b> BayesNet.MODE_ID</b>
 	 * @return la distribuzione di probabilità per l'evento. return[0] = % true, return[1] = % false.
 	 * @throws BayesBoxIOExc
 	 */
 	public double[] likelihoodWeighting(String X, Hashtable<String, Boolean> evidence, int numberOfSamples, int queryMode) throws BayesBoxIOExc {
 		if(queryMode == MODE_ID)
 			return likelihoodWeighting(X, evidence, numberOfSamples, new Random());
 		else
 			return likelihoodWeighting(getIdFromDescription(X), getIdFromDescription(evidence), numberOfSamples, new Random());
 	}
 
 	/**
 	 * Metodo di inferenza tramite simulazione stocastica Rejection Sampling.
 	 * @param X variabile query.
 	 * @param evidence evidenza.
 	 * @param numberOfSamples numero di campioni da generare.
 	 * @param queryMode modalità della query, può essere<br> -<b> BayesNet.MODE_DESCRIPTION</b> (consigliata) <br>-<b> BayesNet.MODE_ID</b>
 	 * @return la distribuzione di probabilità per l'evento e informazioni sul numero di campioni
 	 * consistenti.<br>return[0] = % true<br> return[1] = % false<br>return[2] = n° campioni consistenti<br> return[3] = % campioni consistenti 
 	 * @throws BayesBoxIOExc
 	 */
 	public double[] rejectionSample(String X, Hashtable<String, Boolean> evidence, int numberOfSamples, int queryMode) throws BayesBoxIOExc {
 		if(queryMode == MODE_ID)
 			return rejectionSample(X, evidence, numberOfSamples, new Random());
 		else
 			return rejectionSample(getIdFromDescription(X), getIdFromDescription(evidence), numberOfSamples, new Random());
 	}
 
 	/**
 	 * Metodo interno che esegue il campionamento PriorSample.
 	 */
 	private Hashtable<String, Boolean> getPriorSample(Random r) {
 		Hashtable<String, Boolean> h = new Hashtable<String, Boolean>();
 		List<BayesNetNode> variableNodes = getVariableNodes();
 		for (BayesNetNode node : variableNodes) {
 			h.put(node.getVariable(), node.isTrueFor(r.nextDouble(), h));
 		}
 		return h;
 	}
 
 
 	/**
 	 * Metodo interno per l'implementazione del Rejection Sampling
 	 */
 	private double[] rejectionSample(String X, Hashtable<String, Boolean> evidence, int numberOfSamples, Random r) {
 		double[] retval = new double[2];
 		int consistentSamples = 0;
 		for (int i = 0; i < numberOfSamples; i++) {
 			Hashtable<String, Boolean> sample = getPriorSample(r);
 			if (consistent(sample, evidence)) {
 				consistentSamples++;
 				boolean queryValue = ((Boolean) sample.get(X)).booleanValue();
 				if (queryValue) {
 					retval[0] += 1;
 				} else {
 					retval[1] += 1;
 				}
 			}
 		}
 		//System.out.println(retval[0] + " - " +retval[1]);
 		retval = Util.normalize(retval); //normalizzo il risultato
 
 		//preparo il risultato
 		double[] ris = new double[4];
 		ris[0] = retval[0]; 
 		ris[1] = retval[1];
 		ris[2] = consistentSamples; //n° esempi consistenti
 		ris[3] = 0.01*((int)(ris[2]/numberOfSamples*10000)); //% di esempi consistenti sul totale
 
 		return ris;
 	}
 
 	/**
 	 * Metodo interno per l'implementazione del Likelihood Weighting
 	 */
 	private double[] likelihoodWeighting(String X, Hashtable<String, Boolean> evidence, int numberOfSamples,	Random r) {
 		double[] retval = new double[2];
 		double tmpVal = 1.0;
 		List<BayesNetNode> variableNodes = getVariableNodes();
 
 		for (int i = 0; i < numberOfSamples; i++) {
 			Hashtable<String, Boolean> x = new Hashtable<String, Boolean>();
 			double w = 1.0;
 
 			for (BayesNetNode node : variableNodes) {
 				if (evidence.get(node.getVariable()) != null) {//se ho evidenza per questa var
 					tmpVal = node.probabilityOf(x);
 					if(tmpVal!= -1.0)w *=tmpVal; 
 					//w*=tmpVal;
 					//System.out.println(tmpVal);
 					//System.out.println("Evidentza trovata " +w);
 					x.put(node.getVariable(), evidence.get(node.getVariable()));
 				} else { //se non ho evidenza, simulo
 					x.put(node.getVariable(), node.isTrueFor(r.nextDouble(), x));
 				}
 			}
 			boolean queryValue = (x.get(X)).booleanValue();
 			if (queryValue) {
 				retval[0] += w;
 			} else {
 				retval[1] += w;
 			}
 		}
 		return Util.normalize(retval);
 	}
 
 
 	/**
 	 * Ottiene una lista delle variabili della rete di bayes in un ordine tale da rispettare le dipendenze interne alla rete.
 	 */
 	private List<BayesNetNode> getVariableNodes(){
 		if(variableNodes != null)return variableNodes;
 		List<BayesNetNode> topologicalOrder = new ArrayList<BayesNetNode>();
 		List<BayesNetNode> remainingChilds = new ArrayList<BayesNetNode>();
 		List<BayesNetNode> tmpChild;
 		List<BayesNetNode> currentRoots;
 		
 		//Tutte le radici posso già inserirle
 		System.out.println("##> Computing topological order...");
 		currentRoots = roots;
 		while(currentRoots.size()>0){
 			System.out.println("current roots: "+currentRoots.toString());
 			//topologicalOrder.addAll(currentRoots);
 			
 			for(BayesNetNode n : currentRoots){
				if(topologicalOrder.containsAll(n.getParents()))topologicalOrder.add(n);
 				tmpChild = n.getChildren();
 			
 				for(BayesNetNode c : tmpChild){
 				
 					if(topologicalOrder.containsAll(c.getParents())){
 						if(!topologicalOrder.contains(c)){
 							topologicalOrder.add(c);
 							for(BayesNetNode rc : c.getChildren())
 								if(!remainingChilds.contains(rc))remainingChilds.add(rc);
 						}
 					}
 					else if(!remainingChilds.contains(c))remainingChilds.add(c);
 					//else remainingChilds.add(c);
 				}
 			}
 			System.out.println("figli rimanenti "+remainingChilds.toString());
 			currentRoots = remainingChilds;
 			remainingChilds = new ArrayList<BayesNetNode>();
 		}
 		
 		    
 		variableNodes = topologicalOrder;
 		System.out.println("##> topological order: "+topologicalOrder.toString());
 		return topologicalOrder;
 	}
 
 	private List<BayesNetNode> getVariableNodes2(){
 		if(variableNodes != null)return variableNodes;
 
 		System.out.println("Calcolo LISTA con ordine consistente...");
 		List<BayesNetNode> parents = roots;
 		List<BayesNetNode> newVariableNodes = new ArrayList<BayesNetNode>();
 		List<BayesNetNode> rem = new ArrayList<BayesNetNode>();
 		//-------------PASSO 1
 		//tutti i parents iniziali, sicuramente posso metterli
 		newVariableNodes.addAll(parents);
 
 		for(BayesNetNode iterParents : parents){
 			//if(!newVariableNodes.contains(iterParents))newVariableNodes.add(iterParents);
 			List<BayesNetNode> children = iterParents.getChildren();
 			for (BayesNetNode child : children) {
 				if(newVariableNodes.containsAll(child.getParents()))newVariableNodes.add(child);
 				else rem.add(child);
 
 				List<BayesNetNode> subchild = child.getChildren();
 				for(BayesNetNode s : subchild){
 					if(!rem.contains(s))rem.add(s);
 				}
 			}
 		}
 
 		//------------PASSO 2  
 		while(rem.size()>0){
 			System.out.println("RIMANGONO: "+rem);
 			parents=rem;
 			rem = new ArrayList<BayesNetNode>();
 			for(BayesNetNode iterParents : parents){
 				if(!newVariableNodes.contains(iterParents) && newVariableNodes.containsAll(iterParents.getParents()))newVariableNodes.add(iterParents);
 
 				List<BayesNetNode> children = iterParents.getChildren();
 				for (BayesNetNode child : children) {
 					if(newVariableNodes.containsAll(child.getParents()))newVariableNodes.add(child);
 					else rem.add(child);
 
 					List<BayesNetNode> subchild = child.getChildren();
 					for(BayesNetNode s : subchild){
 						if(!rem.contains(s))rem.add(s);
 					}
 				}
 			}
 		}
 
 		System.out.println("FATTO!");
 		
 		
 		List<BayesNetNode> ris = new ArrayList<BayesNetNode>();
 		for(BayesNetNode s : newVariableNodes)
 			if(!ris.contains(s))ris.add(s);
 		
 		variableNodes = ris;
 		System.out.println("ORDINE :"+variableNodes.toString());
 		return ris; 
 	}
 
 	/*
 	 * /** DEPRECATO non rispetta una visita consistente 
 	private List<BayesNetNode> getVariableNodes2() {
 		// TODO dicey initalisation works fine but unclear . clarify
 		if (variableNodes == null) {
 			List<BayesNetNode> newVariableNodes = new ArrayList<BayesNetNode>();
 			List<BayesNetNode> parents = roots;
 			List<BayesNetNode> traversedParents = new ArrayList<BayesNetNode>();
 			while (parents.size() != 0) {
 				List<BayesNetNode> newParents = new ArrayList<BayesNetNode>();
 				for (BayesNetNode parent : parents) {
 					// if parent unseen till now
 					if (!(traversedParents.contains(parent))) {
 						newVariableNodes.add(parent);
 						// add any unseen children to next generation of parents
 						List<BayesNetNode> children = parent.getChildren();
 						for (BayesNetNode child : children) {
 							if (!newParents.contains(child)) {
 								newParents.add(child);
 							}
 						}
 						traversedParents.add(parent);
 					}
 				}
 				parents = newParents;
 			}
 			variableNodes = newVariableNodes;
 		}
 		return variableNodes;
 	}
 
 	 *
 	 */
 	
 	/**
 	 * Metodo che restituisce un campione PriorSample.
 	 * @return una tabella hash che rappresenta un campione.
 	 */
 	public Hashtable<String, Boolean>  getPriorSample() {
 		return getPriorSample(new Random());
 	}
 	
 	
 
 	/**
 	 * Ritorna il nodo della rete avente la seguente variabile.
 	 * @param y variabile
 	 * @return il nodo corrispondente (null se non esiste)
 	 */
 	private BayesNetNode getNodeOf(String y) {
 		List<BayesNetNode> variableNodes = getVariableNodes();
 		for (BayesNetNode node : variableNodes) {
 			if (node.getVariable().equals(y)) {
 				return node;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Stabilisce se il campione passato è consistente con l'evidenza.
 	 * @param sample un campione
 	 * @param evidence evidenza
 	 * @return 
 	 */
 	private boolean consistent(Hashtable<String, Boolean> sample, Hashtable<String, Boolean> evidence) {
 		Iterator<String> iter = evidence.keySet().iterator();
 		while (iter.hasNext()) {
 			String key = (String) iter.next();
 			Boolean value = (Boolean) evidence.get(key);
 			if(sample.get(key) != null){ //se non è evidenza inutile/errata
 				if (!(value.equals(sample.get(key)))) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 
 	/**
 	 * Metodo per la realizzazione di una copia profonda di una tabella hash
 	 * contenente <i>evidenza</i>
 	 * @param evidence <i>evidenza</i> da clonare
 	 * @return tabella hash contenente <i>evidenza</i> equivalente
 	 */
 	public static Hashtable<String, Boolean> cloneEvidenceVariables(
 			Hashtable<String, Boolean> evidence) {
 		Hashtable<String, Boolean> cloned = new Hashtable<String, Boolean>();
 		Iterator<String> iter = evidence.keySet().iterator();
 		while (iter.hasNext()) {
 			String key = iter.next();
 			Boolean bool = evidence.get(key);
 			if (bool.equals(Boolean.TRUE)) {
 				cloned.put(key, Boolean.TRUE);
 			} else if ((evidence.get(key)).equals(Boolean.FALSE)) {
 				cloned.put(key, Boolean.FALSE);
 			}
 		}
 		return cloned;
 	}
 
 
 	/* ##################################################
 	 * ---- Uso futuro per eliminazione di variabile ----
 	 * ##################################################
 	private Boolean truthValue(double[] ds, Random r) {
 		double value = r.nextDouble();
 		if (value < ds[0]) {
 			return Boolean.TRUE;
 		} else {
 			return Boolean.FALSE;
 		}
 
 	}
 
 	private Hashtable<String, Boolean> createRandomEvent(
 			List nonEvidenceVariables, Hashtable<String, Boolean> evidence,
 			Random r) {
 		Hashtable<String, Boolean> table = new Hashtable<String, Boolean>();
 		List<String> variables = getVariables();
 		for (String variable : variables) {
 
 			if (nonEvidenceVariables.contains(variable)) {
 				Boolean value = r.nextDouble() <= 0.5 ? Boolean.TRUE
 						: Boolean.FALSE;
 				table.put(variable, value);
 			} else {
 				table.put(variable, evidence.get(variable));
 			}
 		}
 		return table;
 	}
 
 	private List nonEvidenceVariables(Hashtable<String, Boolean> evidence,
 			String query) {
 		List<String> nonEvidenceVariables = new ArrayList<String>();
 		List<String> variables = getVariables();
 		for (String variable : variables) {
 
 			if (!(evidence.keySet().contains(variable))) {
 				nonEvidenceVariables.add(variable);
 			}
 		}
 		return nonEvidenceVariables;
 	}
 	 */
 
 
 
 	/* ##################################################
 	 *                      Markov
 	 * ##################################################
 	 * 
 	public double[] mcmcAsk(String X, Hashtable<String, Boolean> evidence,
 			int numberOfVariables, Random r) {
 		double[] retval = new double[2];
 		List nonEvidenceVariables = nonEvidenceVariables(evidence, X);
 		Hashtable<String, Boolean> event = createRandomEvent(
 				nonEvidenceVariables, evidence, r);
 		for (int j = 0; j < numberOfVariables; j++) {
 			Iterator iter = nonEvidenceVariables.iterator();
 			while (iter.hasNext()) {
 				String variable = (String) iter.next();
 				BayesNetNode node = getNodeOf(variable);
 				List<BayesNetNode> markovBlanket = markovBlanket(node);
 				Hashtable mb = createMBValues(markovBlanket, event);
 				// event.put(node.getVariable(), node.isTrueFor(
 				// r.getProbability(), mb));
 				event.put(node.getVariable(), truthValue(rejectionSample(node
 						.getVariable(), mb, 100, r), r));
 				boolean queryValue = (event.get(X)).booleanValue();
 				if (queryValue) {
 					retval[0] += 1;
 				} else {
 					retval[1] += 1;
 				}
 			}
 		}
 		return Util.normalize(retval);
 	}
 
 	public double[] mcmcAsk(String X, Hashtable<String, Boolean> evidence,
 			int numberOfVariables) {
 		return mcmcAsk(X, evidence, numberOfVariables, new Random());
 	}
 
 	private List<BayesNetNode> markovBlanket(BayesNetNode node) {
 		return markovBlanket(node, new ArrayList<BayesNetNode>());
 	}
 
 	private List<BayesNetNode> markovBlanket(BayesNetNode node,
 			List<BayesNetNode> soFar) {
 		// parents
 		List<BayesNetNode> parents = node.getParents();
 		for (BayesNetNode parent : parents) {
 			if (!soFar.contains(parent)) {
 				soFar.add(parent);
 			}
 		}
 		// children
 		List<BayesNetNode> children = node.getChildren();
 		for (BayesNetNode child : children) {
 			if (!soFar.contains(child)) {
 				soFar.add(child);
 				List<BayesNetNode> childsParents = child.getParents();
 				for (BayesNetNode childsParent : childsParents) {
 					;
 					if ((!soFar.contains(childsParent))
 							&& (!(childsParent.equals(node)))) {
 						soFar.add(childsParent);
 					}
 				}// childsParents
 			}// end contains child
 
 		}// end child
 
 		return soFar;
 	}
 
 	private Hashtable createMBValues(List<BayesNetNode> markovBlanket,
 			Hashtable<String, Boolean> event) {
 		Hashtable<String, Boolean> table = new Hashtable<String, Boolean>();
 		for (BayesNetNode node : markovBlanket) {
 			table.put(node.getVariable(), event.get(node.getVariable()));
 		}
 		return table;
 	}
 	 */
 
 
 
 }
