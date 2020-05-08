 /*******************************************************************************
  * Copyright (c) 2006-2007, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 package org.eclipse.b3.aggregator.engine;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.equinox.internal.p2.metadata.BasicVersion;
 import org.eclipse.equinox.internal.p2.metadata.VersionedId;
 
 /**
  * @author Thomas Hallgren
  */
 public class VersionSuffixGenerator {
 	public static final String VERSION_QUALIFIER = "qualifier";
 
 	private static void appendEncodedCharacter(StringBuilder buffer, int c) {
 		while(c > 62) {
 			buffer.append('z');
 			c -= 63;
 		}
 		buffer.append(base64Character(c));
 	}
 
 	// Integer to character conversion in our base-64 encoding scheme. If the
 	// input is out of range, an illegal character will be returned.
 	//
 	private static char base64Character(int number) {
 		return (number < 0 || number > 63)
 				? ' '
 				: BASE_64_ENCODING.charAt(number);
 	}
 
 	private static int charValue(char c) {
 		int index = BASE_64_ENCODING.indexOf(c);
 		// The "+ 1" is very intentional. For a blank (or anything else that
 		// is not a legal character), we want to return 0. For legal
 		// characters, we want to return one greater than their position, so
 		// that a blank is correctly distinguished from '-'.
 		return index + 1;
 	}
 
 	// Encode a non-negative number as a variable length string, with the
 	// property that if X > Y then the encoding of X is lexicographically
 	// greater than the enocding of Y. This is accomplished by encoding the
 	// length of the string at the beginning of the string. The string is a
 	// series of base 64 (6-bit) characters. The first three bits of the first
 	// character indicate the number of additional characters in the string.
 	// The last three bits of the first character and all of the rest of the
 	// characters encode the actual value of the number. Examples:
 	// 0 --> 000 000 --> "-"
 	// 7 --> 000 111 --> "6"
 	// 8 --> 001 000 001000 --> "77"
 	// 63 --> 001 000 111111 --> "7z"
 	// 64 --> 001 001 000000 --> "8-"
 	// 511 --> 001 111 111111 --> "Dz"
 	// 512 --> 010 000 001000 000000 --> "E7-"
 	// 2^32 - 1 --> 101 011 111111 ... 111111 --> "fzzzzz"
 	// 2^45 - 1 --> 111 111 111111 ... 111111 --> "zzzzzzzz"
 	// (There are some wasted values in this encoding. For example,
 	// "7-" through "76" and "E--" through "E6z" are not legal encodings of
 	// any number. But the benefit of filling in those wasted ranges would not
 	// be worth the added complexity.)
 	private static String lengthPrefixBase64(long number) {
 		int length = 7;
 		for(int i = 0; i < 7; ++i) {
 			if(number < (1L << ((i * 6) + 3))) {
 				length = i;
 				break;
 			}
 		}
 		StringBuilder result = new StringBuilder(length + 1);
 		result.append(base64Character((length << 3) + (int) ((number >> (6 * length)) & 0x7)));
 		while(--length >= 0) {
 			result.append(base64Character((int) ((number >> (6 * length)) & 0x3f)));
 		}
 		return result.toString();
 	}
 
 	private Map<String, Integer> contextQualifierLengths;
 
 	private static final int QUALIFIER_SUFFIX_VERSION = 1;
 
 	// The 64 characters that are legal in a version qualifier, in lexicographical order.
 	private static final String BASE_64_ENCODING = "-0123456789_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"; //$NON-NLS-1$
 
 	private static int computeNameSum(String name) {
 		int sum = 0;
 		int top = name.length();
 		int lshift = 20;
 		for(int idx = 0; idx < top; ++idx) {
 			int c = name.charAt(idx) & 0xffff;
 			if(c == '.' && lshift > 0)
 				lshift -= 4;
 			else
 				sum += c << lshift;
 		}
 		return sum;
 	}
 
 	private final int maxVersionSuffixLength;
 
 	private final int significantDigits;
 
 	public VersionSuffixGenerator() {
 		this(-1, -1);
 	}
 
 	public VersionSuffixGenerator(int maxVersionSuffixLenght, int significantDigits) {
 		this.maxVersionSuffixLength = maxVersionSuffixLenght < 0
 				? 28
 				: maxVersionSuffixLenght;
 		this.significantDigits = significantDigits < 0
 				? Integer.MAX_VALUE
 				: significantDigits;
 	}
 
 	public void addContextQualifierLength(String context, int length) {
 		if(contextQualifierLengths == null)
 			contextQualifierLengths = new HashMap<String, Integer>();
 		contextQualifierLengths.put(context, Integer.valueOf(length));
 	}
 
 	/**
 	 * Version suffix generation. Modeled after
 	 * {@link org.eclipse.pde.internal.build.builder.FeatureBuildScriptGenerator#generateFeatureVersionSuffix(org.eclipse.pde.internal.build.site.BuildTimeFeature buildFeature)}
 	 * 
 	 * @return The generated suffix or <code>null</code>
 	 * @throws CoreException
 	 */
 	public String generateSuffix(List<VersionedId> features, List<VersionedId> bundles) {
 		if(maxVersionSuffixLength <= 0)
 			return null; // do nothing
 
 		long majorSum = 0L;
 		long minorSum = 0L;
 		long serviceSum = 0L;
 		long nameCharsSum = 0L;
 
 		// Include the version of this algorithm as part of the suffix, so that
 		// we have a way to make sure all suffixes increase when the algorithm
 		// changes.
 		//
 		majorSum += QUALIFIER_SUFFIX_VERSION;
 
 		int numElements = features.size() + bundles.size();
 		if(numElements == 0)
 			//
 			// This feature is empty so there will be no suffix
 			//
 			return null;
 
 		String[] qualifiers = new String[numElements];
 
 		// Loop through the included features, adding the version number parts
 		// to the running totals and storing the qualifier suffixes.
 		//
 		int idx = 0;
 		for(VersionedId refFeature : features) {
 			BasicVersion version = (BasicVersion) refFeature.getVersion();
 			try {
 				majorSum += version.getMajor();
 			}
 			catch(UnsupportedOperationException e) {
 				// ignore, i.e. "add zero"
 			}
 			try {
 				minorSum += version.getMinor();
 			}
 			catch(UnsupportedOperationException e) {
 				// ignore, i.e. "add zero"
 			}
 			try {
 				serviceSum += version.getMicro();
 			}
 			catch(UnsupportedOperationException e) {
 				// ignore, i.e. "add zero"
 			}
 
 			String qualifier = null;
 			try {
				version.getQualifier();
 			}
 			catch(UnsupportedOperationException e) {
 				// ignore, i.e. let the qualifier be null
 			}
 
 			Integer ctxLen = contextQualifierLengths == null
 					? null
 					: contextQualifierLengths.get(refFeature.getId());
 			int contextLength = (ctxLen == null)
 					? -1
 					: ctxLen.intValue();
 			++contextLength; // account for the '-' separating the context qualifier and suffix
 
 			// The entire qualifier of the nested feature is often too long to
 			// include in the suffix computation for the containing feature,
 			// and using it would result in extremely long qualifiers for
 			// umbrella features. So instead we want to use just the suffix
 			// part of the qualifier, or just the context part (if there is no
 			// suffix part). See bug #162022.
 			//
 			if(qualifier != null && contextLength > 0 && qualifier.length() > contextLength)
 				qualifier = qualifier.substring(contextLength);
 
 			qualifiers[idx++] = qualifier;
 			nameCharsSum = computeNameSum(refFeature.getId());
 		}
 
 		// Loop through the included plug-ins and fragments, adding the version
 		// number parts to the running totals and storing the qualifiers.
 		//
 		for(VersionedId refBundle : bundles) {
 			BasicVersion version = (BasicVersion) refBundle.getVersion();
 			majorSum += version.getMajor();
 			minorSum += version.getMinor();
 			serviceSum += version.getMicro();
 
 			String qualifier = version.isOSGiCompatible()
 					? version.getQualifier()
 					: null;
 			if(qualifier != null && qualifier.endsWith(VERSION_QUALIFIER)) {
 				int resultingLength = qualifier.length() - VERSION_QUALIFIER.length();
 				if(resultingLength > 0) {
 					if(qualifier.charAt(resultingLength - 1) == '.')
 						resultingLength--;
 					qualifier = resultingLength > 0
 							? qualifier.substring(0, resultingLength)
 							: null;
 				}
 				else
 					qualifier = null;
 			}
 			qualifiers[idx++] = qualifier;
 		}
 
 		// Limit the qualifiers to the specified number of significant digits,
 		// and figure out what the longest qualifier is.
 		//
 		int longestQualifier = 0;
 		while(--idx >= 0) {
 			String qualifier = qualifiers[idx];
 			if(qualifier == null)
 				continue;
 
 			if(qualifier.length() > significantDigits) {
 				qualifier = qualifier.substring(0, significantDigits);
 				qualifiers[idx] = qualifier;
 			}
 			if(qualifier.length() > longestQualifier)
 				longestQualifier = qualifier.length();
 		}
 
 		StringBuilder result = new StringBuilder();
 
 		// Encode the sums of the first three parts of the version numbers.
 		result.append(lengthPrefixBase64(majorSum));
 		result.append(lengthPrefixBase64(minorSum));
 		result.append(lengthPrefixBase64(serviceSum));
 		result.append(lengthPrefixBase64(nameCharsSum));
 
 		if(longestQualifier > 0) {
 			// Calculate the sum at each position of the qualifiers.
 			int[] qualifierSums = new int[longestQualifier];
 			for(idx = 0; idx < numElements; ++idx) {
 				String qualifier = qualifiers[idx];
 				if(qualifier == null)
 					continue;
 
 				int top = qualifier.length();
 				for(int j = 0; j < top; ++j)
 					qualifierSums[j] += charValue(qualifier.charAt(j));
 			}
 
 			// Normalize the sums to be base 65.
 			int carry = 0;
 			for(int k = longestQualifier - 1; k >= 1; --k) {
 				qualifierSums[k] += carry;
 				carry = qualifierSums[k] / 65;
 				qualifierSums[k] = qualifierSums[k] % 65;
 			}
 			qualifierSums[0] += carry;
 
 			// Always use one character for overflow. This will be handled
 			// correctly even when the overflow character itself overflows.
 			result.append(lengthPrefixBase64(qualifierSums[0]));
 			for(int m = 1; m < longestQualifier; ++m)
 				appendEncodedCharacter(result, qualifierSums[m]);
 		}
 
 		// If the resulting suffix is too long, shorten it to the designed length.
 		//
 		if(result.length() > maxVersionSuffixLength)
 			result.setLength(maxVersionSuffixLength);
 
 		// It is safe to strip any '-' characters from the end of the suffix.
 		// (This won't happen very often, but it will save us a character or
 		// two when it does.)
 		//
 		int len = result.length();
 		while(len > 0 && result.charAt(len - 1) == '-')
 			result.setLength(--len);
 		return result.toString();
 	}
 }
