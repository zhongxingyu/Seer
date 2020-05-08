 package ecologylab.generic;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CoderResult;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import ecologylab.collections.CollectionTools;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.XMLTools;
 
 /**
  * A set of lovely convenience methods for doing operations on {@link java.lang.String String} s and
  * {@link java.lang.StringBuffer StringBuffer}s.
  */
 public class StringTools extends Debug
 {
 	private static final String	UTF_8														= "UTF-8";
 
 	static final String[]				oneDotDomainStrings							=
 																															{ "com", "edu", "gov", "org", "net",
 			"tv", "info"																						};
 
 	static final HashMap				oneDotDomains										= CollectionTools.buildHashMapFromStrings(oneDotDomainStrings);
 
 	public static final String	EMPTY_STRING										= "";
 
 	public static final int			AVERAGE_PARAM_NAME_VALUE_LENGTH	= 8;
 
 	/**
 	 * Changes the StringBuffer to lower case, in place, without any new storage allocation.
 	 */
 	public static final void toLowerCase(StringBuilder buffer)
 	{
 		int length = buffer.length();
 		for (int i = 0; i < length; i++)
 		{
 			char c = buffer.charAt(i);
 			if (Character.isUpperCase(c))
 			// A = 0x41, Z = 0x5A; a = 0x61, z = 0x7A
 			// if ((c >='A') && (c <= 'Z'))
 			{
 				// c += 0x20;
 				c = Character.toLowerCase(c);
 				buffer.setCharAt(i, c);
 			}
 		}
 	}
 
 	public static final String capitalize(String cs)
 	{
 		if (cs == null || (cs.length() == 0))
 			return "";
 		char firstUC = Character.toUpperCase(cs.charAt(0));
 		String result = Character.toString(firstUC);
 		if (cs.length() > 1)
 			result += cs.substring(1);
 
 		return result;
 	}
 
 	public static final void trim(StringBuilder buffy)
 	{
 		for (int i = 0; i < buffy.length(); i++)
 		{
 			char c = buffy.charAt(0);
 			if (Character.isWhitespace(c))
 				buffy.deleteCharAt(i);
 			else
 				break;
 		}
 		for (int i = buffy.length() - 1; i >= 0; i--)
 		{
 			char c = buffy.charAt(0);
 			if (Character.isWhitespace(c))
 				buffy.deleteCharAt(i);
 			else
 				break;
 		}
 	}
 
 	public static String trimUntil(String input, String s, boolean include)
 	{
 		int p = input.indexOf(s);
 		if (p >= 0)
 			return input.substring(p + (include ? s.length() : 0));
 		return input;
 	}
 	
 	public static String trimAfter(String input, String s, boolean include)
 	{
 		int p = input.indexOf(s);
 		if (p >= 0)
 			return input.substring(0, p + (include ? 0 : s.length()));
 		return input;
 	}
 	
 	public static final boolean sameDomain(URL url1, URL url2)
 	{
 		return domain(url1).equals(domain(url2));
 	}
 
 	public static final String domain(URL url)
 	{
 		if (url.getProtocol().equals("file"))
 			return "filesystem.local";
 		else
 			return domain(url.getHost());
 	}
 
 	/**
 	 * Useful for finding common domains.
 	 */
 	public static final String domain(String hostString)
 	{
 		if ((hostString == null) || (hostString.length() == 0))
 			return null;
 		int lastDot = hostString.lastIndexOf('.');
 		if (lastDot == -1)
 			return hostString; // for example, localhost
 
 		int prevDot = hostString.lastIndexOf('.', lastDot - 1);
 		if (prevDot == -1)
 			return hostString; // for example, earth-netone.com
 
 		String suffix = hostString.substring(lastDot + 1);
 		boolean international = !oneDotDomains.containsKey(suffix);
 		int domainStart;
 		if (!international)
 		{
 			domainStart = prevDot;
 		}
 		else
 		{
 			int prevPrevDot = hostString.lastIndexOf('.', prevDot - 1);
 			domainStart = (prevPrevDot != -1) ? prevPrevDot : prevDot;
 		}
 		String result = hostString.substring(domainStart + 1);
 		return result;
 	}
 
 	/**
 	 * Use this method to efficiently get a <code>String</code> from a <code>StringBuffer</code> on
 	 * those occassions when you plan to keep using the <code>StringBuffer</code>, and want an
 	 * efficiently made copy. In those cases, <i>much</i> better than
 	 * <code>new String(StringBuffer)</code>
 	 */
 	public static final String toString(StringBuffer buffer)
 	{
 		return buffer.substring(0);
 	}
 
 	/**
 	 * Use this method to efficiently get a <code>String</code> from a <code>StringBuilder</code>, by
 	 * copying, on those occasions when you plan to keep using the <code>StringBuilder</code>, and
 	 * want an efficiently made copy.
 	 * <p/>
 	 * Resets the StringBuilder for re-use.
 	 */
 	public static final String toString(StringBuilder buffy)
 	{
 		// String result = buffy.substring(0);
 		String result = buffy.toString();
 		return result;
 	}
 
 	public static final boolean contains(String in, String toMatch)
 	{
 		return (in == null) ? false : in.indexOf(toMatch) != -1;
 	}
 
 	public static final boolean contains(StringBuilder in, String toMatch)
 	{
 		return (in == null) ? false : in.indexOf(toMatch) != -1;
 	}
 
 	public static final boolean contains(String in, char toMatch)
 	{
 		return (in == null) ? false : in.indexOf(toMatch) != -1;
 	}
 
 	/**
 	 * Very efficiently forms String representation of url (better than
 	 * <code>URL.toExternalForm(), URL.toString()</code>). Doesn't include query or anchor.
 	 */
 	public static final String noAnchorNoQueryPageString(URL url)
 	{
 		return noAnchorPageString(url, false);
 	}
 
 	/**
 	 * Very efficiently forms String representation of url (better than
 	 * <code>URL.toExternalForm(), URL.toString()</code>). Doesn't include anchor.
 	 */
 	public static final String noAnchorPageString(URL url)
 	{
 		return noAnchorPageString(url, true);
 	}
 
 	/**
 	 * Very efficiently forms String representation of url (better than
 	 * <code>URL.toExternalForm(), URL.toString()</code>). Doesn't include anchor. May include query,
 	 * depending on param 2.
 	 * 
 	 * @param url
 	 *          Input URL.
 	 * @param includeQuery
 	 *          include query or not.
 	 * 
 	 * @return String representation of URL.
 	 */
 	public static final String noAnchorPageString(URL url, boolean includeQuery)
 	{
 		if (url == null)
 			return "";
 
 		String protocol = url.getProtocol();
 		String authority = url.getAuthority(); // authority is host:port
 		String path = url.getPath(); // doesn't include query
 		String query = includeQuery ? url.getQuery() : null;
 
 		int pathLength = (path == null) ? 0 : path.length();
 		int queryLength = (query == null) ? 0 : query.length();
 		includeQuery = includeQuery && queryLength > 0;
 
 		// pre-compute length of StringBuffer
 		int length = 0;
 		try
 		{
 			length = protocol.length() + 3 /* :// */+ authority.length() + pathLength;
 			if (includeQuery)
 				length += 1/* ? */+ queryLength;
 		}
 		catch (Exception e)
 		{
 			Debug.println("protocol=" + protocol + " authority=" + authority + " " + url.toExternalForm());
 			e.printStackTrace();
 		}
 
 		StringBuilder result = new StringBuilder(length);
 		result.append(protocol).append("://").append(authority).append(path);
 		if (includeQuery)
 			result.append("?").append(query);
 
 		return result.toString();
 	}
 
 	public static final String pageString(URL u)
 	{
 		String protocol = u.getProtocol();
 		String authority = u.getAuthority(); // authority is host:port
 		String path = u.getPath(); // doesn't include query
 		String query = u.getQuery();
 		String anchor = u.getRef();
 
 		int pathLength = (path == null) ? 0 : path.length();
 		int queryLength = (query == null) ? 0 : query.length();
 		int anchorLength = (anchor == null) ? 0 : anchor.length();
 		int authorityLength = (authority == null) ? 0 : authority.length();
 
 		// pre-compute length of StringBuffer
 		int length = 0;
 
 		try
 		{
 			length = protocol.length() + 3 /* :// */+ authorityLength + pathLength + 1 /* ? */
 					+ queryLength + 1 /* # */+ anchorLength;
 		}
 		catch (Exception e)
 		{
 			Debug.println("protocol=" + protocol + " authority=" + authority + u.toExternalForm());
 			e.printStackTrace();
 		}
 
 		StringBuffer result = new StringBuffer(length);
 		result.append(protocol).append("://");
 		if (authority != null)
 			result.append(authority);
 		if (path != null)
 			result.append(path);
 		if (query != null)
 			result.append("?").append(query);
 		if (anchor != null)
 			result.append("#").append(anchor);
 
 		return new String(result);
 	}
 
 	public static final URL urlRemoveAnchorIfNecessary(URL source)
 	{
 		// Below operation is already in the urlNoAnchor();
 		// String anchor = source.getRef();
 		// return (anchor == null) ? source : urlNoAnchor(source);
 		return urlNoAnchor(source);
 
 	}
 
 	public static final URL urlNoAnchor(URL source)
 	{
 		URL result = null;
 
 		if (source == null)
 			return result;
 
 		if (source.getRef() == null)
 			return source;
 
 		try
 		{
 			result = new URL(source.getProtocol(), source.getHost(), source.getPort(), source.getFile());
 		}
 		catch (MalformedURLException e)
 		{
 			e.printStackTrace();
 			throw new RuntimeException("Cant form noHashUrl from " + source.toString());
 		}
 		return result;
 	}
 
 	/*
 	 * public static void main(String[] args) { for (int i=0; i<args.length; i++)
 	 * println(pageString(Generic.getURL(args[i], "oops " + i)));
 	 * 
 	 * }
 	 */
 
 	/**
 	 * Parse file name or variable name spellings, to convert to a set of words.
 	 * 
 	 * @param in
 	 *          input <code>String</code>, for example: "isFileName".
 	 * 
 	 * @return An array of <code>String</code>s, for example: "is", "file", "name".
 	 */
 	public static String[] seperateLowerUpperCase(String in)
 	{
 		int n = in.length();
 		// pass 1 -- just find out how many transitions there are?
 		int numWords = 1;
 		for (int i = 0; i < n; i++)
 		{
 			char thisChar = in.charAt(i);
 			if (Character.isUpperCase(thisChar) && (i != 0))
 				numWords++;
 		}
 		// pass 2 -- create the result set and fill it in
 		String result[] = new String[numWords];
 		int resultIndex = 0;
 		int transition = 0;
 		char[] buffer = new char[n];
 		for (int i = 0; i < n; i++)
 		{
 			char thisChar = in.charAt(i);
 			if (Character.isUpperCase(thisChar))
 			{
 				thisChar = Character.toLowerCase(thisChar);
 				if (i > 0)
 				{
 					result[resultIndex++] = new String(buffer, transition, (i - transition));
 					// result[resultIndex++] = in.substring(transition, i);
 					transition = i;
 				}
 			}
 			buffer[i] = thisChar;
 		}
 		result[resultIndex] = new String(buffer, transition, (n - transition));
 		return result;
 	}
 
 	/**
 	 * Remove all instances of @param c from @arg string
 	 * 
 	 * TODO this method seems horribly inefficient; possibly should replace w/ NIO regex
 	 */
 	public static String remove(String string, char c)
 	{
 		int index;
 
 		while ((index = string.indexOf(c)) > -1)
 		{
 			int length = string.length();
 			if (index == 0)
 				string = string.substring(1);
 			else if (index == (length - 1))
 				string = string.substring(0, length - 1);
 			else
 				string = string.substring(0, index) + string.substring(index + 1);
 		}
 		return string;
 	}
 
 	public static final String	FIND_PUNCTUATION_REGEX	= "(:)|(\\d)|(\\.)|(/++)|(=)|(\\?)|(\\-)|(\\+)|(_)|(%)|(\\,)";
 
 	/**
 	 * Turn punctuation into space delimiters.
 	 */
 	public static String removePunctuation(String s)
 	{
 		int length = s.length();
 		StringBuffer buffy = new StringBuffer(length);
 
 		boolean wasSpace = true;
 
 		for (int i = 0; i < length; i++)
 		{
 			char c = s.charAt(i);
 			if (Character.isLetter(c))
 			{
 				buffy.append(c);
 				wasSpace = false;
 			}
 			else
 			{
 				if (!wasSpace)
 					// buffy.append('-');
 					buffy.append(' ');
 				wasSpace = true;
 			}
 		}
 		return new String(buffy);
 	}
 
 	public static String removePunctuation2(String s)
 	{
 		return s.replaceAll(FIND_PUNCTUATION_REGEX, " ");
 	}
 
 	public static void main(String[] s)
 	{
 		String test = "<html><head></head><body bgcolor=3D\"#FFFFFF\"><div></div><div><h1 style=3D\"f=\n" + 
 				"ont-size:16pt\">SPOTRep</h1><table style=3D\"border:1px solid #ddd\" width=3D\"=\n" + 
 				"100%\" cellspacing=3D\"0\"><tbody><tr><td style=3D\"vertical-align:top;padding:=\n" + 
 				"5px;width:30%;font-weight:bold;border-bottom:1px solid #ddd;background-colo=\n" + 
 				"r:#fafafa\">";
 
 		System.out.println(decodeQuotedPrintable(test));
 		// String a = "39";
 		// String b = "3D";
 		//
 		// System.out.println((char) (Integer.parseInt(a, 16)));
 
 		/* create ParsedURL from url string. */
 		// ParsedURL u =
 		// ParsedURL.getAbsolute("http://www.bbc.co.uk/eastenders/images/navigation/icon_bbc_one.gif",
 		// "foo");
 		// //
 		// println(removePunctuation("http://www.bbc.co.uk/eastenders/images/navigation/icon_bbc_one.gif"));
 		// println(u.removePunctuation());
 	}
 
 	public static void main2(String[] s)
 	{
 		for (int i = 0; i < s.length; i++)
 		{
 			String[] result = seperateLowerUpperCase(s[i]);
 			System.out.print(s[i] + " -> ");
 			for (int j = 0; j < result.length; j++)
 				System.out.print(result[j] + " ");
 			System.out.println();
 		}
 	}
 
 	/**
 	 * Reset the StringBuffer, so that is empty and ready for reuse. Do this with a minimum of
 	 * overhead, given the latest vagaries of the JDK implementation.
 	 */
 	public static final void clear(StringBuffer buffy)
 	{
 		// as of JDK1-4 .setLength(0) initiates horrible re-allocation of
 		// a tiny buffer, so use this weirdness, which looks like the
 		// most reasonable option
 		// int length = buffy.length();
 		// if (length > 0)
 		// buffy.delete(0, length);
 		// BUT JDK 1-5 fixes this!!!
 		buffy.setLength(0);
 	}
 
 	/**
 	 * Reset the StringBuffer, so that is empty and ready for reuse. Do this with a minimum of
 	 * overhead, given the latest vagaries of the JDK implementation.
 	 */
 	public static final void clear(StringBuilder buffy)
 	{
 		// as of JDK1-4 .setLength(0) initiates horrible re-allocation of
 		// a tiny buffer, so use this weirdness, which looks like the
 		// most reasonable option
 		buffy.setLength(0);
 	}
 
 	/**
 	 * Return true iff all the characters in the argument are lower case.
 	 * 
 	 * @param s
 	 * @return
 	 */
 	public static boolean isLowerCase(String s)
 	{
 		int length = s.length();
 		for (int i = 0; i < length; i++)
 		{
 			if (!Character.isLowerCase(s.charAt(i)))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Return true iff all the characters in the first argument are lower case, except chars in the
 	 * second argument.
 	 * 
 	 * @param s
 	 * @param ignoreChars
 	 * @return
 	 */
 	public static boolean isLowerCaseExcept(String s, String ignoreChars)
 	{
 		int length = s.length();
 		for (int i = 0; i < length; i++)
 		{
 			char c = s.charAt(i);
 			if (!Character.isLowerCase(c) && ignoreChars.indexOf(c) < 0)
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Return true iff all the characters in the argument are upper case.
 	 * 
 	 * @param s
 	 * @return
 	 */
 	public static boolean isUpperCase(String s)
 	{
 		int length = s.length();
 		for (int i = 0; i < length; i++)
 		{
 			if (!Character.isUpperCase(s.charAt(i)))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @param path
 	 * @return true if the String ends with a forward slash, like a nice directory.
 	 */
 	public static boolean endsWithSlash(String path)
 	{
 		return path.charAt(path.length() - 1) == '/';
 	}
 
 	/**
 	 * Find the last parenthesis given the location of the first one
 	 * 
 	 * @param relationFrag
 	 *          The relation
 	 * @param startLoc
 	 *          The open parenthesis location in the string
 	 * @return The location of the close parenthesis matching the open one
 	 */
 	public static int findMatchingParenLoc(String relationFrag, int startLoc)
 	{
 		final char L_PARENTHESIS = '(';
 		final char R_PARENTHESIS = ')';
 
 		/**
 		 * Bad start location
 		 */
 		if (startLoc >= relationFrag.length() || relationFrag.charAt(startLoc) != L_PARENTHESIS)
 			return -1;
 
 		char[] relation = relationFrag.toCharArray();
 		int numUnmatchedParens = 0;
 		for (int i = startLoc; i < relation.length; i++)
 		{
 			if (relation[i] == L_PARENTHESIS)
 				numUnmatchedParens++;
 			else if (relation[i] == R_PARENTHESIS)
 				numUnmatchedParens--;
 
 			if (numUnmatchedParens == 0)
 				return i;
 		}
 
 		// didn't find a matching parenthesis
 		return -1;
 	}
 
 	public static final String hash(String message)
 	{
 		try
 		{
 			MessageDigest encrypter = MessageDigest.getInstance("SHA-256");
 
 			encrypter.update(message.toLowerCase().getBytes());
 
 			// convert to normal characters and return as a String
 			return new String((new Base64Coder()).encode(encrypter.digest()));
 
 		}
 		catch (NoSuchAlgorithmException e)
 		{
 			// this won't happen in practice, once we have the right one! :D
 			e.printStackTrace();
 		}
 
 		// this should never occur
 		return null;
 	}
 
 	/**
 	 * Return true if all the chars in the CharSequence are whitespace.
 	 * 
 	 * @param charSequence
 	 * @return
 	 */
 	public static final boolean isWhiteSpace(CharSequence charSequence)
 	{
 		int length = charSequence.length();
 		for (int i = 0; i < length; i++)
 		{
 			if (!Character.isWhitespace(charSequence.charAt(i)))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param a
 	 * @param b
 	 * 
 	 * @return true if the arguments represent the same string, and both are non-null.
 	 */
 	public static boolean equals(CharSequence a, CharSequence b)
 	{
 		if ((a == null) || (b == null))
 			return false;
 
 		int length = a.length();
 		if (length != b.length())
 			return false;
 
 		for (int i = 0; i < length; i++)
 			if (a.charAt(i) != b.charAt(i))
 				return false;
 
 		return true;
 	}
 
 	/**
 	 * The number of concurrent threads we expect will use the decodeUTF8() method.
 	 */
 	private static final int				DECODER_POOL_SIZE	= 10;
 
 	public static final Charset			UTF8_CHARSET			= Charset.forName(UTF_8);
 
 	static final CharsetDecoderPool	utf8DecoderPool		= new CharsetDecoderPool(	UTF8_CHARSET,
 																																							DECODER_POOL_SIZE);
 
 	/**
 	 * Reusable char[] arrays for the decodeUTF8() method.
 	 */
 	static final CharArrayPool			charArrayPool			= new CharArrayPool(512, DECODER_POOL_SIZE);
 
 	public static CharsetDecoder acquireUTF8Encoder()
 	{
 		// FIXME -- use a pool!
 		// return UTF8_CHARSET.newDecoder();
 		return utf8DecoderPool.acquire();
 	}
 
 	/**
 	 * Take a subset of an array of bytes, assumed coded as UTF-8. Translate as efficiently as
 	 * possible into a StringBuilder.
 	 * 
 	 * @param bytes
 	 * @param offset
 	 * @param length
 	 * @return
 	 */
 	public static StringBuilder decodeUTF8(StringBuilder result, byte[] bytes, int offset, int length)
 	{
 		CharsetDecoder decoder = acquireUTF8Encoder();
 
 		int scaledLength = (int) ((double) length * (double) decoder.maxCharsPerByte());
 		char[] chars = charArrayPool.acquire();
 
 		if (chars.length < scaledLength)
 			chars = new char[scaledLength]; // we'll swap buffers on the pool, let the smaller one get
 																			// gc'ed
 		CharBuffer cb = CharBuffer.wrap(chars);
 
 		ByteBuffer bb = ByteBuffer.wrap(bytes, offset, length);
 		CoderResult cr = decoder.decode(bb, cb, true);
 
 		// cb.rewind(); // reset for re-use --
 
 		int resultLength = cb.position();
 
 		result.append(chars, 0, resultLength);
 		decoder.flush(cb);
 
 		utf8DecoderPool.release(decoder);
 		charArrayPool.release(chars);
 
 		return result;
 	}
 
 	private static final StringBuilderPool	RELATIVE_PATH_STRINGS	= new StringBuilderPool(25);
 
 	public static String getPathRelativeTo(String absoluteFile, String relativeToFile,
 			char separatorChar)
 	{
 		/*
 		 * if (separatorChar == '\\') { absoluteFile = absoluteFile.replace('\\', '/'); relativeToFile =
 		 * relativeToFile.replace('\\', '/'); separatorChar = '/'; }
 		 */
 
 		String result = null;
 		boolean windowsFile = separatorChar == '\\';
 		String separator = !windowsFile ? Character.toString(separatorChar) : "\\\\";
 
 		String[] relativeTo = relativeToFile.split(separator);
 		String[] absolutePath = absoluteFile.split(separator);
 
 		int length = absolutePath.length < relativeTo.length ? absolutePath.length : relativeTo.length;
 
 		int lastCommonRoot = -1;
 		int index = 0;
 
 		for (index = 0; index < length; index++)
 		{
 			if (absolutePath[index].equals(relativeTo[index]))
 				lastCommonRoot = index;
 			else
 				break;
 		}
 
 		if (lastCommonRoot != -1)
 		{
 			StringBuilder relativePath = RELATIVE_PATH_STRINGS.acquire();
 
 			for (index = lastCommonRoot + 1; index < relativeTo.length; index++)
 			{
 				if (relativeTo[index].length() > 0)
 					relativePath.append(".." + separator);
 			}
 
 			for (index = lastCommonRoot + 1; index < absolutePath.length - 1; index++)
 			{
 				relativePath.append(absolutePath[index] + separator);
 			}
 
 			relativePath.append(absolutePath[absolutePath.length - 1]);
 
 			result = relativePath.toString();
 			if (windowsFile)
 				result = result.replace("\\\\", "/");
 
 			RELATIVE_PATH_STRINGS.release(relativePath);
 		}
 
 		return result;
 	}
 
 	/**
 	 * performs 1) UnescapeXML 2) StringTools.toLowerCase 3) toString(buffy)
 	 * 
 	 * @param buffy
 	 * @return
 	 */
 	public static String unescapeAndLowerCaseStringBuilder(StringBuilder buffy)
 	{
 		String processedString;
 		XMLTools.unescapeXML(buffy);
 		StringTools.toLowerCase(buffy);
 		processedString = StringTools.toString(buffy);
 		return processedString;
 	}
 
 	/**
 	 * Test to see if the String is null, then, if not, empty. Works with Android or Java SE.
 	 * 
 	 * @param string
 	 *          The input to test.
 	 * 
 	 * @return true if null, or if not null, if length is 0.
 	 */
 	public static boolean isNullOrEmpty(String string)
 	{
 		return string == null || string.length() == 0;
 	}
 
 	/**
 	 * Extract a bunch of name value pairs from an input string from the query string of a URL.
 	 * 
 	 * @param input
 	 *          The argument string.
 	 * @param keepEmptyParams TODO
 	 * @return Map with arg names as keys, and arg values as values. Or null if no arg name/values
 	 *         were extracted.
 	 */
 	public static HashMap<String, String> doubleSplit(URL input, boolean keepEmptyParams)
 	{
 		return doubleSplit(input.getQuery(), "&", "=", true, keepEmptyParams);
 	}
 
 	/**
 	 * Extract a bunch of name value pairs from an input string from a URL.
 	 * 
 	 * @param input
 	 *          The argument string.
 	 * @return Map with arg names as keys, and arg values as values. Or null if no arg name/values
 	 *         were extracted.
 	 */
 	public static HashMap<String, String> doubleSplit(String input)
 	{
 		return doubleSplit(input, "&", "=", true);
 	}
 
 	/**
 	 * Extract a bunch of name value pairs from an input string.
 	 * 
 	 * @param input
 	 *          The argument string.
 	 * @param regex1
 	 *          Delimiter between argument pairs. For URLs, this is "&".
 	 * @param regex2
 	 *          Delimiter between name and value amidst an argument pair. For URLs and Cookies, this
 	 *          is "=".
 	 * @param uudecodeArgs
 	 *          TODO
 	 * @return Map with arg names as keys, and arg values as values. Or null if no arg name/values
 	 *         were extracted.
 	 */
 	public static HashMap<String, String> doubleSplit(String input, String regex1, String regex2,
 			boolean uudecodeArgs)
 	{
 		return doubleSplit(input, regex1, regex2, uudecodeArgs, false);
 	}
 	
 	public static HashMap<String, String> doubleSplit(String input, String regex1, String regex2,
 			boolean uudecodeArgs, boolean keepKeys)
 	{
 		HashMap<String, String> result = null;
 		if (input != null && input.length() > 2)
 		{
 			String[] split1 = input.split(regex1);
 			for (String argPair : split1)
 			{
 				String[] split2 = argPair.split(regex2);
 				if (split2.length == 2 || split2.length == 1 && keepKeys)
 				{
 					if (result == null)
 						result = new HashMap<String, String>(split1.length);
 					String value = split2.length == 2 ? split2[1] : "";
 					if (uudecodeArgs)
 					{
 						try
 						{
 							value = URLDecoder.decode(value, UTF_8);
 						}
 						catch (UnsupportedEncodingException e)
 						{
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						;
 					}
 					result.put(split2[0], value);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Assemble a String of URL parameters from a map of parameters.
 	 * 
 	 * @param parametersMap
 	 *          Map of parameters, with names as keys and values as values.
 	 * 
 	 * @return Output String of parameters, or null, if the map was empty.
 	 */
 	public static String unDoubleSplit(HashMap<String, String> parametersMap)
 	{
 		return unDoubleSplit(parametersMap, "&", "=", true);
 	}
 
 	/**
 	 * Assemble a String of parameters from a map of parameters.
 	 * 
 	 * @param parametersMap
 	 *          Map of parameters, with names as keys and values as values.
 	 * @param delim1
 	 * @param delim2
 	 * @param uuencodeArgs
 	 *          if true, UUEncode each parameter before adding it to the output String.
 	 * 
 	 * @return Output String of parameters, or null, if the map was empty.
 	 */
 	public static String unDoubleSplit(HashMap<String, String> parametersMap, String delim1,
 			String delim2, boolean uuencodeArgs)
 	{
 		if (parametersMap == null)
 			return null;
 
 		int size = parametersMap.size();
 		if (size == 0)
 			return null;
 
 		StringBuilder buffy = new StringBuilder(size * AVERAGE_PARAM_NAME_VALUE_LENGTH);
 		Set<String> keySet = parametersMap.keySet();
 		for (String key : keySet)
 		{
 			if (buffy.length() > 0) // (not the 1st time, though)
 				buffy.append(delim1); // append outer delimiter after the previous key/value append
 
 			String value = parametersMap.get(key);
 			try
 			{
 				value = URLEncoder.encode(value, UTF_8);
 			}
 			catch (UnsupportedEncodingException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			;
 			buffy.append(key).append(delim2).append(value);
 		}
 		return buffy.toString();
 	}
	
	public static String join(String delim, String... parts)
	{
	  StringBuilder sb = new StringBuilder();
	  for (int i = 0; i < parts.length; ++i)
	  {
	    if (i > 0)
	      sb.append(delim);
      sb.append(parts[i]);
	  }
	  return sb.toString();
	}
 
 	public static final String	QUOTED_PRINTABLE_CAPTURE_PATTERN_STRING	= "(=.{2})*(=\\s)*";
 
 	public static final Pattern	QUOTED_PRINTABLE_CAPTURE_PATTERN				= Pattern.compile(QUOTED_PRINTABLE_CAPTURE_PATTERN_STRING);
 
 	// public static final String QUOTED_PRINTABLE_CAPTURE_PATTERN = ".*[(\\=\\s)(\\=.{2})]*";
 
 	/**
 	 * Takes a String in quoted printable format and converts it back to its original String. See
 	 * http://en.wikipedia.org/wiki/Quoted-printable for details.
 	 * 
 	 * XXX I am not convinced this is the cheapest way to do this, but it is thorough.
 	 * 
 	 * @param input
 	 * @return
 	 */
 	public static String decodeQuotedPrintable(String input)
 	{
 		HashMap<String, String> replacements = new HashMap<String, String>();
 		replacements.put("=\n", "");
 
 		Matcher m = QUOTED_PRINTABLE_CAPTURE_PATTERN.matcher(input);
 
 		while (m.find())
 		{
 			String group = m.group();
 
 			if (!replacements.containsKey(group))
 			{
 				replacements.put(	group,
 													(group.length() > 2 ? String.valueOf((char) (Integer.parseInt(group.substring(1,3), 16)))
 															: ""));
 			}
 		}
 
 		for (String s : replacements.keySet())
 		{
 			input = input.replace(s, replacements.get(s));
 		}
 
 		return input;
 	}
 }
