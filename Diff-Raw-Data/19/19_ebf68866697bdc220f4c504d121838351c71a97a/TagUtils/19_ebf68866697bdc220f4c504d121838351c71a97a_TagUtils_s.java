 package com.touchableheroes.android.xml.parser;
 
 import com.touchableheroes.android.xml.parser.tag.SameTagAs;
 
 
 
 /**
  * Method to identify TAGs in an XML. Base on the idea of hierarchical
  * event-bus.
  * 
  * Enum represent an assigned state/event-type.
  * 
  * @author Andreas Siebert, ask@touchableheroes.com
  */
 public class TagUtils {
 
 	/**
 	 * 
 	 * @param parent
 	 * @param fullName without namespace.
 	 * @return
 	 */
 	public static Tag identify(final Tag parent, final String fullName) {
 		if (parent == null) {
 			final String logMsg = "Couldn't identify tag, because passend parent is NULL.";
 			throw new IllegalArgumentException(logMsg);
 		}
 		
 		if (fullName == null)
 			return null;
 
 
 		return identifyByName2(parent, fullName);
 	}
 
 	
 	/**
 	 * uses this method to identify a child of a tag by name and namespace.
 	 * same as indexByName() but with suport name-index.
 	 * should be faster, but uses more memory to hold indizes.
 	 * 
 	 * @param parent point to start to seach a child in tag-declarations.
 	 * @param ns namespace or NULL
 	 * @param name name of the current tag in the xml.
 	 * 
 	 * @return identified child of parent or NULL
 	 */
 	private static Tag identifyByName2(final Tag parent, final String fullName) {
 		if (fullName == null)
 			return null;
 
 		final Tag identified = parent.findInNameIndex(fullName);
 
 		if( identified != null )
 			return identified;
 		
 		final Tag[] patterns = parent.patterns();
 		
 		for( Tag pattern :  patterns) {
 			 if( pattern.match( fullName) )
 				 return pattern;
 		}
 		
 		return null;
//		return identifyByNameInArray(fullName, patterns);
 	}
 
 	
 	
 	public static boolean isCandidate(final Tag parentTag, final String candiadte) {
 		if (candiadte == null)
 				return false;
 
 		if( parentTag == null )
 			throw new IllegalArgumentException( "parameter:parentTag is NULL" );
 		
 		if (parentTag.isPattern() && parentTag.match(candiadte) )
 			return true;
 		
 		if (candiadte.equals( parentTag.fullName() ))
 			return true;
 
 		return false;
 	}
 
 	/**
 	 * Replace one Tag by another - is heplful to declare tag-references in the
 	 * "tag/state-grammar".
 	 * 
 	 * @param current
 	 * 
 	 * @return same Tag or another one.
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static Tag replaceBy(final Tag current) {
 		final SameTagAs ref = current.ruleRef();
 
 		if (ref != null) {
 
 			final Enum openTag = (Enum) current;
 			final Enum replaceBy = Enum.valueOf(openTag.getClass(),
 					ref.stateName());
 
 			return (Tag) replaceBy;
 		} else {
 			return current;
 		}
 	}
 	
 	
 }
