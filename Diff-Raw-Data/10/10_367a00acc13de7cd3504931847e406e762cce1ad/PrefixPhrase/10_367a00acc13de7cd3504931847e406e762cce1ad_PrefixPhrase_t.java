 /**
  * 
  */
 package ecologylab.collections;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import ecologylab.generic.Debug;
 import ecologylab.net.ParsedURL;
 
 /**
  * Recursive unit (bucket) for prefix pattern matching.
  * 
  * @author andruid
  *
  */
 public class PrefixPhrase extends Debug
 {
 	final	String			phrase;
 	
 	final	PrefixPhrase	parent;
 	
 	HashMap<String, PrefixPhrase>	childPhraseMap	= new HashMap<String, PrefixPhrase>();
 
 	/**
 	 * 
 	 */
 	public PrefixPhrase(PrefixPhrase parent, String phrase)
 	{
 		this.parent		= parent;
 		this.phrase		= phrase;
 	}
 
 	public PrefixPhrase add(String string, char separator)
 	{
 		return add(string, 0, separator);
 	}
 	
 	protected PrefixPhrase add(String string, int start, char separator)
 	{
 		int end				= string.length();
 		boolean terminate	= false;
 		
 		if (start == end)
 			terminate		= true;
 		else
 		{		
 			if (string.charAt(start) == separator)
 				start++;
 			if (start == end)
 				terminate	= true;
 		}
 		if (terminate)
 		{
 			clear();
 			return this;
 		}
 		int nextSeparator	= string.indexOf(separator, start);
 		if (nextSeparator == -1)
 			nextSeparator	= end;
 		
 		if (nextSeparator > -1)
 		{
 			String phraseString	= string.substring(start, nextSeparator);
 			// extra round of messing with synch, because we need to know if we
 			// are creating a new Phrase
 			PrefixPhrase nextPrefixPhrase	= getPrefix(this, phraseString);
 			if (nextPrefixPhrase != null)
 			{
 				return nextPrefixPhrase.add(string, nextSeparator, separator);
 			}
 			else
 			{
 				// done!
 				PrefixPhrase newTerminal	= lookupChild(phraseString);
				//newTerminal.clear();
 				return newTerminal;
 //				synchronized (this)
 //				{
 //					nextPrefixPhrase	= getPrefix(this, phraseString);
 //					if (nextPrefixPhrase == null)
 //					{
 //						nextPrefixPhrase	= childPhraseMap.getOrCreateAndPutIfNew(phraseString, this);
 //						result				= nextPrefixPhrase;
 //					}
 //				}
 			}
 		}
 		else
 		{
 			Debug.println("help! wrong block!!!");
 			// last segment
 			return null;
 		}
 	}
 	
 	public boolean match(String string, char separator)
 	{
 		return match(string, 0, separator);
 	}
 	protected boolean match(String string, int start, char separator)
 	{
 		if (isTerminal())
 			return true;
 		
 		int end				= string.length();
 		boolean terminate	= false;
 		
 		if (start == end)
 			terminate		= true;
 		else
 		{		
 			if (string.charAt(start) == separator)
 				start++;
 			if (start == end)
 				terminate	= true;
 		}
 		if (terminate)
 		{
 			return false;
 		}
 		
 		int nextSeparator	= string.indexOf(separator, start);
 		if (nextSeparator == -1)
 			nextSeparator	= end;
 		
 //		String phraseString	= string.substring(start, nextSeparator);
 //		PrefixPhrase nextPrefixPhrase	= lookupChild(phraseString);
 		PrefixPhrase nextPrefixPhrase	= matchChild(string, start, nextSeparator);
 		if (nextPrefixPhrase != null)
 		{
 			return nextPrefixPhrase.match(string, nextSeparator, separator);
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/**
 	 * Match child prefix by iterating, instead of using HashMap, to avoid allocating substring keys.
 	 * 
 	 * @param source	String to get key from.
 	 * @param start		start of substring for key in string
 	 * @param end		end of substring for key in string
 	 * 
 	 * @return			Matching PrefixPhrase for the substring key from source, or null if there is no match.
 	 */
 	private PrefixPhrase matchChild(String source, int start, int end)
 	{
 		// FIXME concurrent modification exception :-(
 		Set<String> keySet = childPhraseMap.keySet();
 		for (String thatPhrase : keySet)
 		{
 			if (match(thatPhrase, source, start, end))
 				return childPhraseMap.get(thatPhrase);
 		}
 		return null;
 	}
 	/**
 	 * 
 	 * @param target
 	 * @param source
 	 * @param start
 	 * @param end
 	 * 
 	 * @return	true if the substring of source running from start to end is the same String as target.
 	 */
 	private static boolean match(String target, String source, int start, int end)
 	{
 		int	targetLength	= target.length();
 		int sourceLength	= end - start;
 		if (targetLength != sourceLength)
 			return false;
 		for (int i=0; i<sourceLength; i++)
 		{
 			if (source.charAt(start++) != target.charAt(i))
 				return false;
 		}
 		return true;
 	}
 	/**
 	 * Seek the PrefixPhrase corresponding to the argument.
 	 * If it does not exist, return it.
 	 * <p/>
 	 * If it does exist, does it have 0 children?
 	 * 		If so, return null. No need to insert for the argument's phrase.
 	 * 		If not, return it.
 	 * 
 	 * @param prefixPhrase
 	 * @return
 	 */
 	protected PrefixPhrase getPrefix(PrefixPhrase parent, String prefixPhrase)
 	{
 		PrefixPhrase domainPrefix	= childPhraseMap.get(prefixPhrase);
 		boolean createNew			= false;
 		
 		if (domainPrefix == null)
 		{
 			synchronized (childPhraseMap)
 			{
 				if (domainPrefix == null)
 				{
 					domainPrefix	= new PrefixPhrase(parent, prefixPhrase);
 					childPhraseMap.put(prefixPhrase, domainPrefix);
 					createNew		= true;
 				}
 			}
 		}
		if (!createNew && (domainPrefix.isTerminal()|| (phrase!=null && prefixPhrase.equals("*"))))
 			return null;
 		
 		return domainPrefix;
 	}
 
 	protected PrefixPhrase lookupChild(String prefix)
 	{
 		return childPhraseMap.get(prefix);
 	}
 	
 	protected void clear()
 	{
 		childPhraseMap.clear();
 	}
 
 	public void toStringBuilder(StringBuilder buffy, char separator)
 	{
 		if (parent != null)
 		{
 			parent.toStringBuilder(buffy, separator);
 			buffy.append(separator);
 		}
 		buffy.append(phrase);
 	}
 	
 	/**
 	 * From this root, find eac the terminal children.
 	 * 
 	 * @param buffy
 	 * @param separator
 	 */
 	void findTerminals(ArrayList<PrefixPhrase> phraseSet)
 	{
 		if (phrase == null)
 			return;
 		if (isTerminal())
 		{
 			phraseSet.add(this);
 		}
 		else
 		{
 			for (PrefixPhrase childPhrase: childPhraseMap.values())
 				childPhrase.findTerminals(phraseSet);
 		}
 	}
 	
 	public int numChildren()
 	{
 		return childPhraseMap.size();
 	}
 	
 	/**
 	 * Is the end of a prefix.
 	 * 
 	 * @return
 	 */
 	public boolean isTerminal()
 	{
 		return numChildren() == 0;
 	}
 	
 	public ArrayList<String> values(char separator)
 	{
 		if (phrase == null)
 			return new ArrayList<String>(0);
 		
 		ArrayList<PrefixPhrase>	terminalPrefixPhrases	= new ArrayList<PrefixPhrase>();
 		findTerminals(terminalPrefixPhrases);
 		
 		ArrayList<String>	result	= new ArrayList<String>(terminalPrefixPhrases.size());
 		StringBuilder 		buffy	= new StringBuilder();//TODO StringBuilderUtils.acquire(buffy)
 		
 		for (PrefixPhrase thatPhrase : terminalPrefixPhrases)
 		{
 			buffy.setLength(0);
 			thatPhrase.toStringBuilder(buffy, separator);
 			result.add(buffy.substring(0, buffy.length()));
 		}
 	//TODO StringBuilderUtils.release(buffy)
 		return result;
 	}
 	public String getMatchingPhrase(String purl,char seperator)
 	{
 		StringBuilder 		buffy	= new StringBuilder();//TODO StringBuilderUtils.acquire(buffy)
 		getMatchingPhrase(buffy, purl, seperator);
 		String result						= buffy.toString();
 		buffy.setLength(0);
 		//TODO StringBuilderUtils.release(buffy)
 		return result;
 	}
 	/**
 	 * This function returns the whole matching path which you have
 	 * followed to reach the PrefixPhrase.
 	 * @param purl
 	 * @param seperator
 	 * @return
 	 */
 	public void getMatchingPhrase(StringBuilder buffy, String purl,char seperator)
 	{
 		String returnValue="";
 		int seperatorIndex	= purl.indexOf(seperator);
 		if(seperatorIndex>0)
 		{
 			String key 					= purl.substring(0, seperatorIndex);
 			String phrase 			= purl.substring(seperatorIndex+1,purl.length());
 			PrefixPhrase childPrefixPhrase= childPhraseMap.get(key);
 			
 			if(childPrefixPhrase==null)
 			{
 				// try getting it using wildcard as key
 				childPrefixPhrase = childPhraseMap.get("*");
 				key="*";
 			}
 			if(childPrefixPhrase!=null)
 			{
				buffy.append(returnValue).append(key).append(seperator);
 				buffy.append(childPrefixPhrase.getMatchingPhrase(phrase, seperator));
 			}
 		}
 	}
 }
