 package token.probabilitydistribution;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.zip.GZIPInputStream;
 
 import util.GetProperties;
 import util.TreeTagger;
 import web.util.html.HTML2Text;
 
 /**
  * Distribution de probabilité de termes dans un document (fichier, page web, chaîne, corpus, ...).
  * Elle est du type p[element] = proba d'apparition de l'élément dans le document.
  * Propose des calculs de ngrammes sur divers type de document et/ou corpus,  
  * @author Ludovic Bonnefoy (ludovic.bonnefoy@gmail.com)
  */
 public class NGramsProbabilityDistribution implements Serializable
 {
 	private static final long serialVersionUID = -4539352062797597509L;
 
 	/** Nombre d'éléments total dans la collection */
 	protected Long _total;
 
 	/** Liste des éléments présents dans le corpus associé à son nombre d'occurences. */
 	protected HashMap<String, Long> _freqs; 
 
 	/**n de n-grammes */
 	private Integer _n;
 
 	/**
 	 * Initialisation d'une nouvelle distribution de termes.
 	 */
 	public NGramsProbabilityDistribution()
 	{
 		_freqs = new HashMap<String, Long>();
 	}
 
 	/**
 	 * Copie d'une distribution de termes.
 	 * @param ngpd Distribution à copier.
 	 */
 	@SuppressWarnings("unchecked")
 	public NGramsProbabilityDistribution(NGramsProbabilityDistribution ngpd)
 	{
 		System.err.println("ll");
 		_freqs = (HashMap<String, Long>) ngpd.getFrequenciesMap().clone();
 		System.err.println("ll");
 		_total = ngpd.getVocabularySize();
 		_n = ngpd.getN();
 	}
 	
 	public void loadFromFreqFile(File file, Integer n)
 	{
 		_freqs = new HashMap<String, Long>();
 		_total = new Long(0);
 		try {
 			BufferedReader unigrams = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
 			String unigram;
 			while((unigram = unigrams.readLine()) != null) //pour chaque token
 			{
 				String[] elements = unigram.split(" "); //on récupère les infos
 				if(elements.length != n+1)
 				{
 					System.err.println("erreur");
 					System.exit(1);
 				}
 				
 				String ngram = "";
 				int i = 0;
 				for(; i < n; i++)
 					ngram += elements[i];
 				
 				_freqs.put(ngram, new Long(elements[i])); 
 				_total += new Long(elements[i]);
 			}
 			_n = n;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Récupération d'une distribution sérialisée.
 	 * @param serializedNGramsProbabilityDistribution Fichier contenant la distribution sérialisée.
 	 */
 	@SuppressWarnings("unchecked")
 	public NGramsProbabilityDistribution(File serializedNGramsProbabilityDistribution)
 	{
 		try {
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serializedNGramsProbabilityDistribution));
 			NGramsProbabilityDistribution tmp = (NGramsProbabilityDistribution)(ois.readObject());
 
 			_freqs = (HashMap<String, Long>) tmp.getFrequenciesMap().clone();
 			_total = tmp.getVocabularySize();
 			_n = tmp.getN();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Retourne le nombre d'éléments par n-grammes.
 	 * @return n de n-grammes.
 	 */
 	public Integer getN()
 	{
 		return _n;
 	}
 
 	/**
 	 * Permet de récupérer la probabilité d'apparition du ngramme.
 	 * @param ngram Ngramme dont on veux la probabilité d'apparition.
 	 * @return Probabilité d'apparition du ngramme.
 	 */
 	public Double get(String ngram)
 	{
 		if(_freqs.containsKey(ngram))
 			return new Double(_freqs.get(ngram) / new Double(_total));
 		else
 			return new Double(0);
 	}
 
 
 	/**
 	 * Ajout d'un nombre d'occurence d'un ngramme.
 	 * @param ngram Ngramme dont on veut rajouter des occurences.
 	 * @param occ Nombre d'occurences.
 	 */
 	public void add(String ngram, Long occ)
 	{
 		Long count = _freqs.containsKey(ngram) ? _freqs.get(ngram) : 0; //on récupère le compte précédent de ce n-gramme
 		_freqs.put(ngram, count + occ);
 		_total += occ;
 	}
 
 	/**
 	 * Ajout d'occurences de ngrammes.
 	 * @param ngrams Association ngramme/nombre d'occurences.
 	 */
 	public void add(HashMap<String, Long> ngrams)
 	{
 		for(String ngram : ngrams.keySet())
 			add(ngram, ngrams.get(ngram));
 	}
 
 	/**
 	 * Contrairement à add, cette méthode REMPLACE la valeur précédente associée à un ngramme par un nouveau nombre d'occurences.
 	 * Même effet si le ngramme n'était pas encore présent.
 	 * @param ngram Ngramme que l'on veux ajouter.
 	 * @param occ Nombre d'occurences associé.
 	 */
 	public void put(String ngram, Long occ)
 	{
 		if(_freqs.containsKey(ngram)) //si une valeur est déjà dans la map
 			_total -= _freqs.get(ngram); //retire la valeur du compteur
 
 		_freqs.put(ngram, occ);
 		_total += occ;
 	}
 
 	/**
 	 * Contrairement à add, cette méthode REMPLACE les valeurs précédentes associées à des ngrammes par de nouveaux nombres d'occurences.
 	 * Même effet si les ngrammes n'étaient pas encore présents.
 	 * @param ngrams Association ngramme/nombre d'occurences.
 	 */
 	public void put(HashMap<String, Long> ngrams)
 	{
 		for(String ngram : ngrams.keySet())
 			put(ngram, ngrams.get(ngram));
 	}
 
 	/**
 	 * Retourne true si le ngramme est présent.
 	 * @param ngram Ngramme recherché.
 	 * @return true si le ngramme est présent.
 	 */
 	public boolean containsKey(String ngram)
 	{
 		return _freqs.containsKey(ngram);
 	}
 
 	/**
 	 * Permet de récupérer la liste des ngrammes présents.
 	 * @return Liste des ngrammes présents.
 	 */
 	public Set<String> keySet()
 	{
 		return _freqs.keySet();
 	}
 
 	/**
 	 * Renvoie l'ensemble des ngrammes présents avec leur nombre d'occurences.
 	 * @return Ensemble des couples ngrammes/nombre d'occurences.
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap<String, Long> getFrequenciesMap()
 	{
 		return (HashMap<String, Long>)_freqs.clone();
 	}
 
 	/**
 	 * Retourne le nombre total de ngrammes présents (somme du nombre d'occurences pour chaque ngramme). 
 	 * @return Nombre total de ngrammes présents.
 	 */
 	public Long getVocabularySize()
 	{
 		return _total;
 	}
 	
 	/**
 	 * Ajoute les ngrammes et leur nombre d'occurences d'une distribution à l'instance.
 	 * @param ndpg Distribution de laquelle on va récupérer des ngrammes et leur nombre d'occurences et les ajouter à l'instance.
 	 */
 	public void merge(NGramsProbabilityDistribution ndpg)
 	{
 		HashMap<String, Long> ndpgFreqs = ndpg.getFrequenciesMap();
 		
 		Set<String> ngrams = ndpgFreqs.keySet();
 		for(String ngram : ngrams)
 			add(ngram, ndpgFreqs.get(ngram));
 		
 		_total += ndpg.getVocabularySize();
 	}
 
 	/**
 	 * Sérialise l'objet au chemin indiqué (chemin complet = contenant le nom).
 	 * @param path Chemin complet où doit être stocké l'objet sérialisé.
 	 */
 	public void serialize(String path)
 	{
 		try {
 			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
 			out.writeObject(this);
 			out.flush();
 			out.close();
 			//writeObject(new ObjectOutputStream(new FileOutputStream(path)));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Calcul de la probabilité d'apparition des n-grammes dans un fichier.
 	 * @param file Fichier où compter les n-grammes.
 	 * @param n La taille des n-grammes.
 	 */
 	public void fromFile(File file, Integer n)
 	{
 		_freqs = new HashMap<String, Long>();  
 		_n = n;
		_total = new Long(0);
 
 		TreeTagger treeTagger = new TreeTagger(GetProperties.getInstance().getProperty("treeTaggerPath"));
 		ArrayList<ArrayList<String>> tokensTag = treeTagger.tag(file);
		System.out.println(tokensTag.toString());
		
 		ArrayList<String> ngramList = new ArrayList<String>();
		
		for(int i = 0; i < n; i++)
			ngramList.add("");
 
 		for(ArrayList<String> tokenTag : tokensTag)
 		{
 			String word = tokenTag.get(0).toLowerCase(); //passage en minuscule
 
 			if(tokenTag.get(1).equals("CD")) //si on a un chiffre  
 				word = "@card@"; //on remplace la valeur par @card@, on ne prend pas en compte la valeur
 
 			if(!tokenTag.get(2).equals("<unknown>")) //si on a le lemme
 				word = tokenTag.get(2).toLowerCase(); //on prend le lemme
 
 			//on met à jour le n-grammes
 			ngramList.remove(0); //pour cela on supprime le premier élément
 			ngramList.add(word); //et on rajoute ce terme à la fin
 
 			if(!ngramList.contains("")) //permet de vérifier que l'on est bien un n-gramme complet (pour les premiers cas)
 			{
 				String ngram = new String(); //on va mettre "à plat" le n-gramme
 
 				for(String ngramWord : ngramList) //pour chaque élément du n-gramme
 					ngram += ngramWord+" "; //on concatène le terme
 				add(ngram.trim(), new Long(1));
 			}
 		}
 	}
 
 	/** 
 	 * Calcul de la probabilité d'apparition des n-grammes dans une chaîne.
 	 * @param text Chaîne où compter les n-grammes.
 	 * @param n La taille des n-grammes.
 	 */
 	public void fromString(String text, Integer n)
 	{
 		try
 		{
 			//Ecrit le texte dans un fichier
 			PrintWriter pw = new PrintWriter(new OutputStreamWriter (new FileOutputStream ("/tmp/NgramsFromString"),"ISO-8859-1"));
 			pw.print(text);
 			pw.flush();
 			pw.close();
 
 		}catch(Exception e){
 			System.err.println(e);
 		}
 
 		fromFile(new File("/tmp/NgramsFromString"), n);
 	}
 
 	/**
 	 * Calcul de la probabilité d'apparition des n-grammes dans un corpus composé de pages web.
 	 * Attention, l'intégralité du corpus est copié dans un unique fichier.. si il est trop volumineux, 
 	 * créer plusieurs distributions avec des sous-parties du corpus et combiner les avec la méthode "merge".
 	 * @param corpusDirectory Corpus où compter les n-grammes.
 	 * @param n La taille des n-grammes.
 	 */
 	public void fromWebCorpus(File corpusDirectory, Integer n)
 	{
 		try
 		{
 			if(corpusDirectory.exists())  //vérifie que le dossier du corpus existe bien
 			{ 
 				File[] files = corpusDirectory.listFiles(); //chargement de la liste des fichiers composant le corpus
 
 				PrintWriter pw = new PrintWriter(new OutputStreamWriter (new FileOutputStream ("/tmp/NgramsFromCorpus"),"ISO-8859-1")); //fichier qui va contenir l'ensemble du texte "propre" du corpus
 				
 				for(int i=0; i<files.length; ++i) //pour chaque fichier 
 				{
 					HTML2Text parser = new HTML2Text(); //va permettre de nettoyer le texte des balises html
 					try{
 						parser.parse(new FileReader(files[i])); //suppression des balises
 						String text = parser.getCleanText(); //on a ici le texte du document web, sans les balises
 
 						pw.print(text); //écriture dans le fichier
 					}catch (Exception e) {}
 					catch (StackOverflowError sofe) {}
 
 					pw.flush();
 				}
 
 				pw.close(); //fermeture du flux vers le fichier
 			}
 		}catch(Exception e){
 			System.err.println(e);
 		}
 
 		fromFile(new File("/tmp/NgramsFromCorpus"), n);
 	}	
 
 	/**
 	 * Calcul de la probabilité d'apparition des n-grammes dans un ensemble de fichier composé de pages web.
 	 * Attention, l'intégralité des fichiers est copié dans un unique fichier.. si ils sont trop volumineux, 
 	 * créer plusieurs distributions avec des sous-parties de cet ensemble et combiner les avec la méthode "merge".
 	 * @param files Documents où compter les n-grammes.
 	 * @param n La taille des n-grammes.
 	 */
 	public void fromWebCorpus(ArrayList<File> files, Integer n)
 	{
 		try
 		{
 			PrintWriter pw = new PrintWriter(new OutputStreamWriter (new FileOutputStream ("/tmp/NgramsFromCorpus"),"ISO-8859-1")); //fichier qui va contenir l'ensemble du texte "propre" du corpus
 
 			for(File file : files) //pour chaque fichier 
 			{
 				HTML2Text parser = new HTML2Text(); //va permettre de nettoyer le texte des balises html
 				try{
 					parser.parse(new FileReader(file)); //suppression des balises
 					String text = parser.getCleanText(); //on a ici le texte du document web, sans les balises
 
 					pw.print(text); //écriture dans le fichier
 				}catch (Exception e) {}
 				catch (StackOverflowError sofe) {}
 
 				pw.flush();
 			}
 
 			pw.close(); //fermeture du flux vers le fichier
 		}catch(Exception e){
 			System.err.println(e);
 		}
 
 		fromFile(new File("/tmp/NgramsFromCorpus"), n);
 	}	
 
 	/**
 	 * Calcul de la probabilité d'apparition des n-grammes dans un corpus composé de documents.
 	 * Attention, l'intégralité du corpus est copié dans un unique fichier.. si il est trop volumineux, 
 	 * créer plusieurs distributions avec des sous-parties du corpus et combiner les avec la méthode "merge".
 	 * @param corpusDirectory Corpus où compter les n-grammes.
 	 * @param n La taille des n-grammes.
 	 */
 	public void fromCorpus(File corpusDirectory, Integer n)
 	{
 		try
 		{
 			if(corpusDirectory.exists())  //vérifie que le dossier du corpus existe bien
 			{ 
 				File[] files = corpusDirectory.listFiles(); //chargement de la liste des fichiers composant le corpus
 
 				PrintWriter pw = new PrintWriter(new OutputStreamWriter (new FileOutputStream ("/tmp/NgramsFromWebCorpus"),"ISO-8859-1")); //fichier qui va contenir l'ensemble du texte "propre" du corpus
 
 				for(int i=0; i<files.length; ++i) //pour chaque fichier 
 				{
 					String text = util.FileReader.fileToString(files[i]);
 					pw.print(text); //écriture dans le fichier
 					pw.flush();
 				}
 
 				pw.close(); //fermeture du flux vers le fichier
 			}
 		}catch(Exception e){
 			System.err.println(e);
 		}
 
 		fromFile(new File("/tmp/NgramsFromWebCorpus"), n);
 	}	
 	
 	/**
 	 * Calcul de la probabilité d'apparition des n-grammes dans un ensemble de fichier composé de documents.
 	 * Attention, l'intégralité des fichiers est copié dans un unique fichier.. si ils sont trop volumineux, 
 	 * créer plusieurs distributions avec des sous-parties de cet ensemble et combiner les avec la méthode "merge".
 	 * @param files Documents où compter les n-grammes.
 	 * @param n La taille des n-grammes.
 	 */
 	public void fromCorpus(ArrayList<File> files, Integer n)
 	{
 		try
 		{
 			PrintWriter pw = new PrintWriter(new OutputStreamWriter (new FileOutputStream ("/tmp/NgramsFromCorpus"),"ISO-8859-1")); //fichier qui va contenir l'ensemble du texte "propre" du corpus
 
 			for(File file : files) //pour chaque fichier 
 			{
 				String text = util.FileReader.fileToString(file);
 				pw.print(text); //écriture dans le fichier
 				pw.flush();
 			}
 
 			pw.close(); //fermeture du flux vers le fichier
 		}catch(Exception e){
 			System.err.println(e);
 		}
 
 		fromFile(new File("/tmp/NgramsFromCorpus"), n);
 	}	
 
 	/**
 	 * Calcul de la probabilité d'apparition des n-grammes dans un fichier warc (composé de pages web).
 	 * @param warcFile Document où compter les n-grammes.
 	 * @param n La taille des n-grammes.
 	 */
 	public void fromWarc(File warcFile, Integer n)
 	{
 		try{
 			PrintWriter pw = new PrintWriter(new OutputStreamWriter (new FileOutputStream ("/tmp/NgramsFromWarc"),"ISO-8859-1")); //écriture du contenu des documents dans un fichier
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(warcFile)),"UTF-8")); //lecture du fichier warc
 			String line;
 
 			String webPage = new String(); //le contenu d'une page web du fichier warc
 			Integer compt = -3; //permet de déterminer quand le contenu d'une page commence (pour passe les entêtes du fichier warc)
 
 			while(( line= br.readLine()) != null)
 			{
 				if(line.equals("WARC/0.18")) //nouvelle page web
 				{
 					compt = -3; //réinitialisation du compteur
 
 					if(webPage.equals("")) //si on est dans le premier cas
 						continue; //il n'y a encore rien à faire
 
 					//Sinon il faut nettoyer le fichier et ensuite le rajouter dans un fichier texte
 					HTML2Text parser = new HTML2Text();
 					try{
 						parser.parse(new StringReader(webPage)); //suppression des balises html
 						pw.println(parser.getCleanText()); //écriture du texte dans un fichier
 					}catch (Exception e) {}
 					catch (StackOverflowError sofe) {}
 
 					webPage = new String();
 				}
 
 				else if(compt >= 0) //on est dans la partie texte
 					webPage += line+"\n";
 
 				else if(compt >= -2) //attends une ligne en particulier
 				{
 					if(line.contains("Content-Length"))
 						++compt;
 				}
 
 				else if(line.contains("WARC-TREC-ID")) //attends cette ligne
 					++compt;
 			}
 
 			pw.close();
 			br.close();
 
 		}catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 
 		fromFile(new File("/tmp/NgramsFromWarc"), n);  //retourne le résultat de la fonction qui gère les fichier
 	}
 }
