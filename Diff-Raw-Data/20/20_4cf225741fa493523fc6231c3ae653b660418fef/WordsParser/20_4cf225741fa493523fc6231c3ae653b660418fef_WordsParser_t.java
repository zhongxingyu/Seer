 package org.atosorigin.ctOO;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.SAXException;
 
 public class WordsParser
 {
 	private static boolean log = false;
 	Node top=new Node();
 	int maxWords=3; // TODO : calculer
 	String[] domains;
 	
 	private static class Word
 	{
 		int pos;
 		String word;
 		public Word(int pos,String word)
 		{
 			this.pos=pos;
 			this.word=word;
 		}
 		public String toString()
 		{
 			return pos+":"+word;
 		}
 	};
 	// classical version, with more memory
 	@SuppressWarnings("serial")
 	private static class HashString extends HashMap<String, Node> 
 	{ 
 		HashString()
 		{
 			super(8);
 		}
 	}
 	// memory optimization with same little less speed 
 	//	private static class HashString extends MultiStringHashMap<Node> { }
 		// Version avec automate
 	//	private static class MultiStringHashMap<T> implements Map<String, T>
 	//	{
 	//		public MultiStringHashMap()
 	//		{
 	//		}
 	//
 	//		private char c = '\0';
 	//
 	//		private MultiStringHashMap<T> next;
 	//
 	//		private MultiStringHashMap<T> brother;
 	//
 	//		private T value;
 	//
 	//		@Override
 	//		public T put(String key, final T value)
 	//		{
 	//			// For each string...
 	//			MultiStringHashMap<T> cur = this;
 	//			final int l = key.length();
 	//			// For each char...
 	//			for (int j = 0; j < l; ++j)
 	//			{
 	//				final char c = key.charAt(j);
 	//				if (cur.next == null)
 	//				{
 	//					cur = cur.next = new MultiStringHashMap<T>();
 	//					cur.c = c;
 	//				}
 	//				else
 	//				{
 	//					MultiStringHashMap<T> f = cur.next;
 	//					while (f.c != c)
 	//					{
 	//						if (f.brother == null)
 	//						{
 	//							cur = f.brother = new MultiStringHashMap<T>();
 	//							cur.c = c;
 	//							break;
 	//						}
 	//						f = f.brother;
 	//					}
 	//					if (f.c == c)
 	//						cur = f;
 	//				}
 	//
 	//			}
 	//			cur.value = value;
 	//			return value;
 	//		}
 	//
 	//		@Override
 	//		public T get(final Object key)
 	//		{
 	//			String k=(String)key;
 	//			int end = 0;
 	//			MultiStringHashMap<T> cur = this;
 	//			final int max = k.length();
 	//			here: for (int i = 0; i < max; ++i)
 	//			{
 	//				final char c = k.charAt(i);
 	//				MultiStringHashMap<T> f = cur.next;
 	//				while (f != null)
 	//				{
 	//					if (f.c == c)
 	//					{
 	//						++end;
 	//						cur = f;
 	//						if (end == max)
 	//						{
 	//							return cur.value;
 	//						}
 	//						continue here;
 	//					}
 	//					f = f.brother;
 	//				}
 	//
 	//				if (cur != this)
 	//				{
 	//					cur = this;
 	//				}
 	//				return null;
 	//			}
 	//			return null;
 	//		}
 	//
 	//		private String dump()
 	//		{
 	//			final StringBuilder b = new StringBuilder();
 	//			b.append("--------\n");
 	//			new Runnable()
 	//			{
 	//				void dump(int i, MultiStringHashMap<T> n)
 	//				{
 	//					for (int j = 0; j < i; ++j)
 	//						b.append(" ");
 	//					b.append("{" + n.c + ":" + n.value + "}\n");
 	//					if (n.next != null)
 	//						dump(
 	//							i + 1, n.next);
 	//					if (n.brother != null)
 	//					{
 	//						dump(
 	//							i, n.brother);
 	//					}
 	//				}
 	//
 	//				public void run()
 	//				{
 	//					dump(
 	//						0, MultiStringHashMap.this);
 	//				}
 	//			}.run();
 	//			b.append("\n--------\n");
 	//			return b.toString();
 	//		}
 	//
 	//		@Override
 	//		public void clear()
 	//		{
 	//			c='\0';
 	//			next=null;
 	//			brother=null;
 	//		}
 	//
 	//		@Override
 	//		public boolean containsKey(Object paramObject)
 	//		{
 	//			return get(paramObject)!=null;
 	//		}
 	//
 	//		@Override
 	//		public boolean containsValue(Object paramObject)
 	//		{
 	//			throw new NotImplementedException();
 	//		}
 	//
 	//		@Override
 	//		public Set<java.util.Map.Entry<String, T>> entrySet()
 	//		{
 	//			throw new NotImplementedException();
 	//		}
 	//
 	//		@Override
 	//		public boolean isEmpty()
 	//		{
 	//			return c=='\0' && next==null && brother==null;
 	//		}
 	//
 	//		@Override
 	//		public Set<String> keySet()
 	//		{
 	//			throw new NotImplementedException();
 	//		}
 	//
 	//		@Override
 	//		public void putAll(Map<? extends String, ? extends T> paramMap)
 	//		{
 	//			throw new NotImplementedException();
 	//		}
 	//
 	//		@Override
 	//		public T remove(Object paramObject)
 	//		{
 	//			throw new NotImplementedException();
 	//		}
 	//
 	//		@Override
 	//		public int size()
 	//		{
 	//			throw new NotImplementedException();
 	//		}
 	//
 	//		@Override
 	//		public Collection<T> values()
 	//		{
 	//			throw new NotImplementedException();
 	//		}
 	//	};
 	//
 	
 	// ----------------------------------------
 	class Node
 	{
 		/**
 		 * Un processeur de mots dans une phrase.
 		 */
 		Node()
 		{
 			this.word="";
 		}
 		
 		/**
 		 * Creation d'un noeud de l'automate.
 		 * @param word
 		 */
 		private Node(String word)
 		{
 			this.word = word;
 		}
 	
 		/**
 		 * Le mot du noeud.
 		 */
 		String word;
 	
 		/**
 		 * Les mots suivants candidats.
 		 */
 		HashString candidates = new HashString();
 	
 		/**
 		 * Le domaine associé ou null.
 		 */
 		ForeignTerm foreignTerm;
 	
 		/**
 		 * Une vision string de l'arbre.
 		 */
 		@Override
 		public String toString()
 		{
 			StringBuilder buf = new StringBuilder();
 			buf.append("[");
 			buf.append(
 				"word=").append(
 				word).append(
 				"\n");
 			buf.append(
 				"domaines=").append(
 				foreignTerm).append(
 				"\n");
 			buf.append(
 				"candidates={").append(
 				candidates).append(
 				"}\n");
 			buf.append("]");
 			return buf.toString();
 		}
 	
 		// ------------------------------
 	
 		/**
 		 * Ajoute une phrase à l'automate.
 		 * @param domaines Le domaine à associer à la phrase.
 		 * @param sentence La phrase. Les mots sont séparé par des espaces ou des tirets.
 		 */
 		public void add( ForeignTerm domaines, String sentence)
 		{
 			if (log)
 				System.out.println("add \"" + sentence + "\"");
 			add(domaines,parseSentence(sentence,true));
 		}
 		
 		/**
 		 * Ajoute une liste de mots à l'automate pour un domaine.
 		 * 
 		 * @param domaines Le domaine.
 		 * @param words La liste de mots.
 		 */
 		private void add( ForeignTerm domaines, List<Word> words)
 		{
 			// For each string...
 			if (maxWords<words.size()) maxWords=words.size();
 			Node cur = this;
 			Node secondcur=null;
 			for (final Word word : words)
 			{
 				final String theWord=word.word;
 				if (log) System.out.println("ADD \""+word+"\"");
 				final int tiret=theWord.indexOf('-');
 				if (tiret!=-1)
 				{
 					String first=theWord.substring(0,tiret);
 					String second=theWord.substring(tiret+1);
 					String withoutTiret=first+second;
 					secondcur = addOneNode(cur, new Node(withoutTiret));
 					cur=addOneNode(addOneNode(cur, new Node(first)),new Node(second));
 				}
 				else
 				{
 					final Node n=new Node(theWord);
 					cur = addOneNode(cur, n);
 					if (secondcur!=null)
 					{
 						addOneNode(secondcur,n);
 						secondcur=null;
 					}
 				}
 				
 			}
 			cur.foreignTerm = domaines;
 			if (secondcur!=null)
 			{
 				secondcur.foreignTerm=domaines;
 				secondcur=null;
 			}
 	
 			// new Runnable()
 			// {
 			// void dump(int i, NodeWord n)
 			// {
 			// for (int j = 0; j < i; ++j)
 			// System.out.print(" ");
 			// System.out.println("{" + n.word + ":"+n.domaines+"}");
 			// System.out.println(n.candidates);
 			// if (n.next != null)
 			// dump(i + 1, n.next);
 			// dumpNodeChar(n.candidates);
 			// }
 			//
 			// public void run()
 			// {
 			// dump(0, top);
 			// }
 			// }.run();
 	
 		}
 
 		private Node addOneNode(Node cur, final Node theWord)
 		{
 			if (log) System.out.println("ADDOneNode \""+theWord+"\"");
 			Node f = cur.candidates.get(theWord.word);
 			if (f == null)
 			{
 				cur.candidates.put(theWord.word, f = theWord);
 			}
 			return f;
 		}
 	
 		/**
 		 * Recherche dans la pharse composée de mots
 		 * @param words Les mots de la phrase.
 		 * @return Un ensemble de résultats.
 		 */
 		private Result[] find(final List<Word> words)
 		{
 			List<Result> results = new ArrayList<Result>();
 			Node cur = this;
 			final int max = words.size();
 			int start=-1;
 			here: for (int i = 0; i < max; ++i)
 			{
 				// System.out.println(cur.candidates.dump());
 				Word word=words.get(i);
 				Node find = cur.candidates.get(word.word);
 				// Hock pour gérer la forme fléchie avec un pluriel simple
 				if ((find==null) && word.word.charAt(word.word.length()-1)=='s') 
 				{
 					find=cur.candidates.get(word.word.substring(0,word.word.length()-1));
 				}
 				if (find != null)
 				{
 					if (log)
 						System.out.println("FIND \"" + find.word + "\"");
 					if (start==-1)
 						start=word.pos;
 					if (find.foreignTerm != null)
 					{
 						final Result result=new Result();
 						result.start=start;
 						result.len=word.pos+word.word.length()-start;
 						result.foreignTerm=find.foreignTerm;
 						results.add(result);
 						if (log)
 							System.out.println("ADD \"" + find.foreignTerm + "\" de " + result.start + " pour " + result.len);
 					}
 					cur = find;
 					continue here;
 				}
 	
 				if (cur != this)
 				{
 					cur = this;
 					start = -1;
 					--i;
 					continue here;
 				}
 //				else
 //					start = i + 1;
 			}
 			
 			Collections.sort(results,new Comparator<Result>()
 			{
 				//@Override
 				public int compare(Result o1, Result o2)
 				{
 					if (o1.start==o2.start)
 					{
 						return o2.len-o1.len;
 					}
 					else
 						return o1.start-o2.start;
 				}
 			});
 				
 			return results.toArray(new Result[results.size()]);
 		}
 	}
 	
 	// --------------
 	/**
 	 * Recherche les mots dans la phrase.
 	 * 
 	 * @param sentence La phrase où chercher les mots.
 	 * @return Un ensemble de résultats.
 	 */
 	public Result[] find(final String sentence)
 	{
 		if (log)
 			System.out.println("find \"" + sentence + "\"");
 		return top.find(parseSentence(sentence,false));
 	}
 
 	/**
 	 * Initialisation de l'automate à partir d'un flux XML.
 	 * 
 	 * @param dataInputStream Le flux.
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 */
 	public static WordsParser createFromXML(InputStream dataInputStream) 
 		throws SAXException, IOException, ParserConfigurationException
 	{
 		final WordsParser parser=new WordsParser();
 		parser.parseXML(dataInputStream);
 		return parser;
 	}
 	
 	/**
 	 * Ajout à l'automate à partir d'un flux XML.
 	 * 
 	 * @param dataInputStream Le flux.
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 */
 	private void parseXML(InputStream dataInputStream) 
 		throws SAXException, IOException,ParserConfigurationException
 	{
 		SAXParserFactory.newInstance().newSAXParser().parse(dataInputStream, new XMLDictionnaryHandler(this));
 
 	}
 
 	/**
 	 * Découpe une phrase en mots.
 	 * @param sentence La phrase.
 	 * @return Le tableau de mots.
 	 */
 	private static List<Word> parseSentence(String sentence,boolean tiret)
 	{
 		List<Word> words=new ArrayList<Word>();
 		StringBuilder builder=new StringBuilder();
 		int start=-1;
 		for (int i=0;i<sentence.length();++i)
 		{
 			char c=sentence.charAt(i);
			if (Character.isLetter(c) || (tiret && c=='-'))
 			{
 				builder.append(Character.toLowerCase(c));
 			}
 			else 
 			{
 				if (builder.length()!=0)
 				{
 					words.add(new Word(start+1,builder.toString()));
 					builder.setLength(0);
 				}
 				start=i;
 			}
 		}
 		if (builder.length()!=0)
 			words.add(new Word(start+1,builder.toString()));
 		return words;//sentence.split("[ -,.:!?]");
 	}
 	public String[] getDomains()
 	{
 		return domains;
 	}
 	public int getMaxWords()
 	{
 		return maxWords;
 	}
 }
