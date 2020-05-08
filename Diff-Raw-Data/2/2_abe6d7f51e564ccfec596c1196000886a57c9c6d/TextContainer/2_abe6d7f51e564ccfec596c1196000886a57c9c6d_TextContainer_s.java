 /*===========================================================================
   Copyright (C) 2008-2010 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.resource;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import net.sf.okapi.common.Range;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.Annotations;
 import net.sf.okapi.common.annotation.IAnnotation;
 
 /**
  * Provides methods for storing the content of a paragraph-type unit, to handling its properties,
  * annotations and segmentation.
  * <p>The TextContainer is made of a collection of parts: Some are simple {@link TextPart} objects,
  * others are special {@link TextPart} objects called {@link Segment}.
  * <p>A TextContainer has always at least one {@link Segment} part.
  */
 public class TextContainer implements Iterable<TextPart> {
 
 	private static final String PARTSEP1 = "\u0091";
 	private static final String PARTSEP2 = "\u0092";
 	private static final char PARTSEP1CHAR = 0x0091;
 
 	private Hashtable<String, Property> properties;
 	private Annotations annotations;
 	private List<TextPart> parts;
 	private boolean segApplied;
 
 	private final Segments segments = new Segments() {
 		
 		public Iterator<Segment> iterator() {
 			return new Iterator<Segment>() {
 				int current = foundNext(-1);
 				private int foundNext (int start) {
 					for ( int i=start+1; i<parts.size(); i++ ) {
 						if ( parts.get(i).isSegment() ) {
 							return i;
 						}
 					}
 					return -1;
 				}
 				
 				@Override
 				public void remove () {
 					throw new UnsupportedOperationException("The method remove() not supported.");
 				}
 				
 				@Override
 				public Segment next () {
 					if ( current == -1 ) {
 						throw new NoSuchElementException("No more content parts.");
 					}
 					int n = current;
 					// Get next here because hasNext() could be called several times
 					current = foundNext(current);
 					// Return 'previous' current
 					return (Segment)parts.get(n);
 				}
 				
 				@Override
 				public boolean hasNext () {
 					return (current != -1);
 				}
 			};
 		};
 		
 		@Override
 		public List<Segment> asList() {
 			ArrayList<Segment> segments = new ArrayList<Segment>();
 			for ( TextPart part : parts ) {
 				if ( part.isSegment() ) {
 					segments.add((Segment)part);
 				}
 			}
 			return segments;
 		}
 
 		@Override
 		public void swap (int segIndex1, int segIndex2) {
 			int partIndex1 = getPartIndex(segIndex1);
 			int partIndex2 = getPartIndex(segIndex2);
 			if (( partIndex1 == -1 ) || ( partIndex2 == -1 )) {
 				return; // At least one index is wrong: do nothing
 			}
 			TextPart tmp = parts.get(partIndex1);
 			parts.set(partIndex1, parts.get(partIndex2));
 			parts.set(partIndex2, tmp);
 		}
 		
 
 		@Override
 		public void append (Segment segment) {
 			append(segment, null);
 		}
 		
 		@Override
 		public void append (Segment segment, String textBefore) {
 			// Add the text before if needed
 			if ( !Util.isEmpty(textBefore) ) {
 				if (( parts.get(parts.size()-1).getContent().isEmpty() )
 					&& !parts.get(parts.size()-1).isSegment() )
 				{
 					parts.set(parts.size()-1, new TextPart(textBefore));
 				}
 				else {
 					parts.add(new TextPart(textBefore));
 				}
 			}
 			
 			// If the last segment is empty and at the end of the content: re-use it
 			if (( parts.get(parts.size()-1).getContent().isEmpty() )
 				&& parts.get(parts.size()-1).isSegment() )
 			{
 				parts.set(parts.size()-1, segment);
 			}
 			else {
 				parts.add(segment);
 			}
 			validateSegmentId(segment);
 			segApplied = true;
 		}
 		
 		@Override
 		public void append (TextFragment fragment) {
 			append(new Segment(null, fragment));
 		}
 		
 		@Override
 		public int create (List<Range> ranges) {
 			// Do nothing if null or empty
 			if (( ranges == null ) || ranges.isEmpty() ) return 0;
 
 			// If the current content is a single segment we start from it
 			TextFragment holder; 
 			if ( parts.size() == 1  ) {
 				holder = parts.get(0).getContent();
 			}
 			else {
 				holder = createJoinedContent(null);
 			}
 			
 			// Reset the segments
 			parts = new ArrayList<TextPart>();
 
 			// Extract the segments using the ranges
 			int start = 0;
 			int id = 0;
 			for ( Range range : ranges ) {
 				if ( range.end == -1 ) {
 					range.end = holder.text.length();
 				}
 				// Check boundaries
 				if ( range.end < range.start ) {
 					throw new InvalidPositionException(String.format(
 						"Invalid segment boundaries: start=%d, end=%d.", range.start, range.end));
 				}
 				if ( start > range.start ) {
 					throw new InvalidPositionException("Invalid range order.");
 				}
 				if ( range.end == range.start ) {
 					// Empty range, skip it
 					continue;
 				}
 				// If there is an interstice: creates the corresponding part
 				if ( start < range.start ) {
 					parts.add(new TextPart(holder.subSequence(start, range.start)));
 				}
 				// Create the part for the segment
 				parts.add(new Segment(String.valueOf(id++),
 					holder.subSequence(range.start, range.end)));
 				start = range.end;
 				segApplied = true;
 			}
 
 			// Check if we have remaining text after the last segment
 			if ( start < holder.text.length() ) {
 				if ( start == 0 ) { // If the remain is the whole content: make it a segment
 					parts.add(new Segment(String.valueOf(id), holder));
 				}
 				else { // Otherwise: make it an interstice
 					parts.add(new TextPart(holder.subSequence(start, -1)));
 				}
 			}
 
 			return parts.size();
 		}
 	
 		@Override
 		public int create (int start, int end) {
 			ArrayList<Range> range = new ArrayList<Range>();
 			range.add(new Range(start, end));
 			return create(range); 
 		}
 		
 		@Override
 		public int count () {
 			int count = 0;
 			for ( TextPart part : parts ) {
 				if ( part.isSegment() ) {
 					count++;
 				}
 			}
 			return count;
 		}
 		
 		@Override
 		public TextFragment getFirstContent () {
 			for ( TextPart part : parts ) {
 				if ( part.isSegment() ) {
 					return part.getContent();
 				}
 			}
 			// Should never occur
 			return null;
 		}
 
 		@Override
 		public TextFragment getLastContent () {
 			for ( int i=parts.size()-1; i>=0; i-- ) {
 				if ( parts.get(i).isSegment() ) {
 					return parts.get(i).getContent();
 				}
 			}
 			// Should never occur
 			return null;
 		}
 
 		@Override
 		public Segment getLast() {
 			for ( int i=parts.size()-1; i>=0; i-- ) {
 				if ( parts.get(i).isSegment() ) {
 					return (Segment)parts.get(i);
 				}
 			}
 			// Should never occur
 			return null;
 		}
 
 		@Override
 		public Segment get (String id) {
 			for ( TextPart part : parts ) {
 				if ( part.isSegment() ) {
 					if ( ((Segment)part).id.equals(id) ) return (Segment)part;
 				}
 			}
 			// Should never occur
 			return null;
 		}
 
 		@Override
 		public Segment get (int index) {
 			int tmp = -1;
 			for ( TextPart part : parts ) {
 				if ( part.isSegment() ) {
 					if ( ++tmp == index ) {
 						return (Segment)part;
 					}
 				}
 			}
 			// Should never occur
 			return null;
 		}
 
 		@Override
 		public void joinAll () {
 			// Merge but don't remember the ranges
 			setContent(createJoinedContent(null));
 		}
 
 		@Override
 		public void joinAll (List<Range> ranges) {
 			setContent(createJoinedContent(ranges));
 		}
 
 		@Override
 		public List<Range> getRanges () {
 			List<Range> ranges = new ArrayList<Range>();
 			createJoinedContent(ranges);
 			return ranges;
 		}
 
 		@Override
 		public int joinWithNext (int segmentIndex) {
 			// Check if we have something to join to
 			if ( parts.size() == 1 ) {
 				return 0; // Nothing to do
 			}
 			
 			// Find the segment to join
 			int start = -1;
 			int tmp = -1;
 			for ( TextPart part : parts ) {
 				if ( part.isSegment() ) {
 					if ( ++tmp == segmentIndex ) {
 						start = tmp;
 						break;
 					}
 				}
 			}
 			
 			// Check if we have a segment at such index
 			if ( start == -1 ) {
 				//TODO: some kind of error???
 				return 0; // Not found
 			}
 			
 			// Find the next segment
 			int end = -1;
 			for ( int i=start+1; i<parts.size(); i++ ) {
 				if ( parts.get(i).isSegment() ) {
 					end = i;
 					break;
 				}
 			}
 			
 			// Check if we have a next segment
 			if ( end == -1 ) {
 				// No more segment to join
 				return 0;
 			}
 			
 			TextFragment tf = parts.get(start).getContent();
 			int count = (end-start);
 			int i = 0;
 			while ( i < count ) {
 				tf.append(parts.get(start+1).getContent());
 				parts.remove(start+1);
 				i++;
 			}
 
 			// Do not reset segApplied if one part only: keep the info that is was segmented
 			return count;
 		}
 
 		@Override
 		public int getPartIndex (int segIndex) {
 			int n = -1;
 			for ( int i=0; i<parts.size(); i++ ) {
 				if ( parts.get(i).isSegment() ) {
 					n++;
 					if ( n == segIndex ) return i;
 				}
 			}
 			return -1; // Not found
 		}
 		
 	};
 	
 	public Segments getSegments() {
 		return segments;
 	}
 	
 	/**
 	 * Creates a string that stores the content of a given container. Only the content is saved (not
 	 * the properties, annotations, etc.). Use {@link #stringToContent(String)} to create the container
 	 * back from the string.
 	 * @param tc the container holding the content to store. 
 	 * @return a string representing the content of the given container.
 	 */
 	public static String contentToString (TextContainer tc) {
 		StringBuilder tmp = new StringBuilder();
 		tmp.append(tc.hasBeenSegmented() ? '1' : '0');
 		for ( TextPart part : tc ) {
 			// part to string
 			tmp.append(part.isSegment() ? '1' : '0');
 			tmp.append(part.text.getCodedText());
 			tmp.append(PARTSEP1);
 			tmp.append(Code.codesToString(part.text.getCodes()));
 			tmp.append(PARTSEP1);
 			if ( part.isSegment() ) {
 				tmp.append(((Segment)part).id);
 			}
 			// end of part to string: next part
 			tmp.append(PARTSEP2);
 		}
 		return tmp.toString();
 	}
 	
 	/**
 	 * Converts a string created by {@link #contentToString(TextContainer)} back into a TextContainer.
 	 * @param data the string to process.
 	 * @return a new TextConatiner with the stored content re-created.
 	 */
 	public static TextContainer stringToContent (String data) {
 		TextContainer tc = new TextContainer();
 		tc.setHasBeenSegmentedFlag((data.charAt(0)=='1'));
 		String[] chunks = data.substring(1).split(PARTSEP2, 0);
 		tc.parts.clear();
 		for ( String chunk : chunks ) {
 			int n1 = chunk.indexOf(PARTSEP1CHAR);
 			int n2 = chunk.indexOf(PARTSEP1CHAR, n1+1);
 			TextFragment tf = new TextFragment(chunk.substring(1, n1),
 				Code.stringToCodes(chunk.substring(n1+1, n2)));
 			tc.parts.add((chunk.charAt(0)=='1')
 				? new Segment(chunk.substring(n2+1), tf)
 				: new TextPart(tf));
 		}
 		return tc;
 	}
 	
 	/**
 	 * Creates a new empty TextContainer object.
 	 */
 	public TextContainer () {
 		createSingleSegment(null);
 	}
 
 	/**
 	 * Creates a new TextContainer object with some initial text.
 	 * @param text the initial text.
 	 */
 	public TextContainer (String text) {
 		createSingleSegment(text);
 	}
 
 	/**
 	 * Creates a new TextContainer object with an initial TextFragment.
 	 * @param fragment the initial TextFragment.
 	 */
 	public TextContainer (TextFragment fragment) {
 		setContent(fragment);
 	}
 	
 	/**
 	 * Creates a new TextConatiner object with an initial segment.
 	 * If the id of the segment is null it will be set automatically.
 	 * @param segment the initial segment.
 	 */
 	public TextContainer (Segment segment) {
 		if ( segment.text == null ) {
 			segment.text = new TextFragment();
 		}
 		parts = new ArrayList<TextPart>();
 		parts.add(segment);
 		validateSegmentId(segment);
 	}
 
 	/**
 	 * Creates a new TextContainer object with optional text.
 	 * @param text the text, or null for not text.
 	 */
 	private void createSingleSegment (String text) {
 		parts = new ArrayList<TextPart>();
 		// Note: don't use appendSegment() as it uses createSingleSegment().
 		Segment seg = new Segment("0", new TextFragment(text));
 		parts.add(seg);
 		segApplied = false;
 	}
 	
 	/**
 	 * Gets the string representation of this container.
 	 * If the container is segmented, the representation shows the merged
 	 * segments.
 	 * @return the string representation of this container.
 	 */
 	@Override
 	public String toString () {
 		if ( parts.size() == 1 ) {
 			return parts.get(0).getContent().toString();
 		}
 		// Else: merge to a temporary content
 		return createJoinedContent(null).toString();
 	}
 	
 	/**
 	 * Creates an iterator to loop through the parts (segments and non-segments) of this container.
 	 * @return a new iterator all for the parts of this container.
 	 */
 	public Iterator<TextPart> iterator () {
 		return new Iterator<TextPart>() {
 			int current = 0;
 			
 			@Override
 			public void remove () {
 				throw new UnsupportedOperationException("The method remove() not supported.");
 			}
 			
 			@Override
 			public TextPart next () {
 				if ( current >= parts.size() ) {
 					throw new NoSuchElementException("No more content parts.");
 				}
 				return parts.get(current++);
 			}
 			
 			@Override
 			public boolean hasNext () {
 				return (current<parts.size());
 			}
 		};
 	}
 
 	/**
 	 * Compares this container with another one. Note: This is a costly operation if
 	 * the two containers have segments and no text differences.
 	 * @param cont the other container to compare this one with.
 	 * @param codeSensitive true if the codes need to be compared as well.
 	 * @return a value 0 if the objects are equals.
 	 */
 	public int compareTo (TextContainer cont,
 		boolean codeSensitive)
 	{
 		int res = 0;
 		if ( cont.contentIsOneSegment() ) {
 			if ( contentIsOneSegment() ) {
 				// No ranges to compare
 				return getFirstContent().compareTo(cont.getFirstContent(), codeSensitive);
 			}
 			else {
 				res = getUnSegmentedContentCopy().compareTo(cont.getFirstContent(), codeSensitive);
 			}
 		}
 		else {
 			if ( contentIsOneSegment() ) {
 				res = getFirstContent().compareTo(cont.getUnSegmentedContentCopy(), codeSensitive);
 			}
 			else {
 				res = getUnSegmentedContentCopy().compareTo(cont.getUnSegmentedContentCopy(), codeSensitive);
 			}
 		}
 		if ( res != 0 ) return res;
 		
 		// If the content is the same, check the segment boundaries
 		StringBuilder tmp1 = new StringBuilder();
 		for ( Range range : segments.getRanges() ) {
 			tmp1.append(range.toString());
 		}
 		StringBuilder tmp2 = new StringBuilder();
 		for ( Range range : cont.getSegments().getRanges() ) {
 			tmp2.append(range.toString());
 		}
 		return tmp1.toString().compareTo(tmp2.toString());
 	}
 	
 	/**
 	 * Indicates if a segmentation has been applied to this container. Note that it does not
 	 * mean there is more than one segment or one part. Use {@link #contentIsOneSegment()} to
 	 * check if the container counts only one segment (whether is is the result of a segmentation
 	 * or simply the default single segment).
 	 * <p>This method return true if any method that may cause the content to be segmented
 	 * has been called, and no operation has resulted in un-segmenting the content since that call,
 	 * or if the content has more than one part.
 	 * @return true if a segmentation has been applied to this container.
 	 * @see #setHasBeenSegmentedFlag(boolean)
 	 */
 	public boolean hasBeenSegmented () {
 		return segApplied;
 	}
 	
 	/**
 	 * Sets the flag indicating if the content of this container has been segmented.
 	 * @param hasBeenSegmented true to flag the content has having been segmented, false to set it
 	 * has not having been segmented.
 	 * @see #hasBeenSegmented()
 	 */
 	public void setHasBeenSegmentedFlag (boolean hasBeenSegmented) {
 		segApplied = hasBeenSegmented;
 	}
 	
 	/**
 	 * Indicates if this container is made of a single segment that holds the
 	 * whole content (i.e. there is no other parts).
 	 * <p>When this method returns true, the methods {@link #getFirstContent()},
 	 * {@link #Segments.getFirstContent()}, {@link #getLastContent()} and
 	 * {@link #Segments.getLastContent()} return the same result.
 	 * @return true if the whole content of this container is in a single segment.
 	 * @see #count()
 	 * @see #Segments.count()
 	 */
 	public boolean contentIsOneSegment () {
 		return (( parts.size() == 1 ) && parts.get(0).isSegment() );
 	}
 	
 	/**
 	 * Changes the type of a given part.
 	 * If the part was a segment this makes it a non-segment (except if this is the only part
 	 * in the content. In that case the part remains unchanged). If this part was not a segment
 	 * this makes it a segment (with its identifier automatically set).
 	 * @param partIndex the index of the part to change. Note that even if the part is a segment
 	 * this index must be the part index not the segment index.
 	 */
 	public void changePart (int partIndex) {
 		if ( parts.get(partIndex).isSegment() ) {
 			// If it's a segment, make it a non-segment
 			if ( hasOnlyOneSegment() ) {
 				// Except if it's the only segment, to ensure at-least-1-segment
 				return; 
 			}
 			parts.set(partIndex, new TextPart(parts.get(partIndex).text));
 		}
 		else {
 			// If it's a non-segment, make it a segment (with auto-id)
 			Segment seg = new Segment(null, parts.get(partIndex).text);
 			validateSegmentId(seg);
 			parts.set(partIndex, seg);
 			segApplied = true;
 		}
 	}
 	
 	/**
 	 * Inserts a given part (segment or non-segment) at a given position.
 	 * If the position is already occupied that part and all the parts to
 	 * it right are shifted to the right.
 	 * <p>If the part to insert is a segment, its id is validated.
 	 * @param partIndex the position where to insert the new part.
 	 * @param part the part to insert.
 	 */
 	public void insert (int partIndex, TextPart part) {
 		parts.add(partIndex, part);
 		if ( part.isSegment() ) {
 			validateSegmentId((Segment)part);
 		}
 		segApplied = true;
 	}
 	
 	/**
 	 * Removes the part at s given position.
 	 * <p>If the selected part is the last segment in the content, the part
 	 * is only cleared, not removed.
 	 * @param partIndex the position of the part to remove. 
 	 */
 	public void remove (int partIndex) {
 		if ( parts.get(partIndex).isSegment() && hasOnlyOneSegment() ){
 			// If it's the last segment, just clear it, don't remove it.
 			parts.get(partIndex).text.clear();
 		}
 		else {
 			parts.remove(partIndex);
 		}
 	}
 	
 	/**
 	 * Appends a part at the end of this container.
 	 * If the current last part (segment or non-segment) is empty,
 	 * the TextFragment is appended to that part. Otherwise the
 	 * TextFragment is appended to the content as a new non-segment part.
 	 * <p>Important: If the container is empty, the appended part becomes
 	 * a segment, as the container has always at least one segment.
 	 * @param fragment the text fragment to append.
 	 */
 	public void append (TextFragment fragment) {
 		//If the last part is empty we append to it
 		if ( parts.get(parts.size()-1).getContent().isEmpty() ) {
 			// Append the fragment to the segment or non-segment part
 			parts.get(parts.size()-1).text.append(fragment);
 		}
 		else { // Else: like appending a TextPart
 			append(new TextPart(fragment));
 		} 
 	}
 	
 	/**
 	 * Appends a part with a given text at the end of this container.
 	 * If the current last part (segment or non-segment) is empty,
 	 * the text is appended to that part. Otherwise the
 	 * text is appended to the content as a new non-segment part.
 	 * @param text the text to append.
 	 */
 	public void append (String text) {
 		append(new TextPart(text));
 	}
 	
 	/**
 	 * Appends a {@link TextPart} (segment or non-segment) at the end of this container.
 	 * If the current last part (segment or non-segment) is empty,
 	 * the part replaces the last part, otherwise the part is
 	 * appended to the content as it.
 	 * If the result of the operation would result in a container without segment, the
 	 * first part is automatically converted to a fragment.
 	 * @param part the TextPart to append.
 	 */
 	public void append (TextPart part) {
 		// If the last part is empty we append to it
 		if ( parts.get(parts.size()-1).getContent().isEmpty() ) {
 			parts.set(parts.size()-1, part);
 		}
 		else {
 			parts.add(part);
 		}
 		if ( segments.count() == 0 ) {
 			// We need to ensure there is at least one segment
 			changePart(0);
 		}
 	}
 	
 	
 	/**
 	 * Gets the coded text of the whole content (segmented or not).
 	 * Use this method to compute segment boundaries that will be applied using
 	 * {@link #createSegment(int, int)} or {@link #createSegments(List)} or other methods.
 	 * @return the coded text of the whole content to use for segmentation template.
 	 * @see #createSegment(int, int)
 	 * @see #createSegments(List)
 	 */
 	public String getCodedText () {
 		if ( parts.size() == 1 ) {
 			return parts.get(0).getContent().getCodedText();
 		}
 		else {
 			return createJoinedContent(null).getCodedText();
 		}
 	}
 
 	/**
 	 * Splits a given part into two or three parts.
 	 * <ul>
 	 * <li>If end == start or end or -1 : A new part is created on the right side of the position.
 	 * It has the same type as the original part.
 	 * <li>If start == 0: A new part is created on the left side of the original part.
 	 * <li>If the specified span is empty at at either end of the part, or if it is equals to the
 	 * whole length of the part: No change (it would result in an empty part).
 	 * It has the type specified by spannedPartIsSegment.
 	 * </ul>
 	 * @param partIndex index of the part to split.
 	 * @param start start of the middle part to create.
 	 * @param end position just after the last character of the middle part to create.
 	 * @param spannedPartIsSegment true if the new middle part should be a segment,
 	 * false if it should be a non-segment.
 	 */
 	public void split (int partIndex,
 		int start,
 		int end,
 		boolean spannedPartIsSegment)
 	{
 		// Get the part and adjust the end==-1 if needed
 		TextPart part = parts.get(partIndex);
 		if ( end == -1 ) {
 			end = part.text.text.length();
 		}
 		if ( end < start ) {
 			throw new InvalidPositionException(String.format(
 				"Invalid segment boundaries: start=%d, end=%d.", start, end));
 		}
 		// If span is empty and at either ends
 		if (( end-start == 0 ) && (( start == 0 ) || ( end == part.text.text.length() ))) {
 			return; // Nothing to do
 		}
 		// If span is the same as the part
 		if ( end-start >= part.text.text.length() ) {
 			return; // Nothing to do
 		}
 
 		// Determine the index where to insert the new part
 		int newPartIndex = partIndex+1;
 		if ( start == 0 ) {
 			newPartIndex = partIndex;
 		}
 		// Determine the type of the new part
 		boolean newPartIsSegment = spannedPartIsSegment;
 		if ( start == end ) {
 			newPartIsSegment = part.isSegment();
 			// And it's like inserting on the right
 			end = part.text.text.length();
 		}
 		
 		// If span starts at 0, or ends at fragment ends:
 		// We need only to split in two parts
 		if (( start == 0 ) || ( end == part.text.text.length() )) { 
 			// Create the new part and copy the relevant content
 			if ( newPartIsSegment ) {
 				parts.add(newPartIndex, new Segment(null, part.text.subSequence(start, end)));
 				validateSegmentId((Segment)parts.get(newPartIndex));
 			}
 			else {
 				parts.add(newPartIndex, new TextPart(part.text.subSequence(start, end)));
 			}
 			// Removes from the given part the content that was copied into the new part
 			part.text.remove(start, end);
 		}
 		// Else: Span with content: A middle part (the new part) and a right part are to be created 
 		else {
 			// Create the middle part and copy the relevant content
 			if ( newPartIsSegment ) {
 				parts.add(newPartIndex, new Segment(null, part.text.subSequence(start, end)));
 				validateSegmentId((Segment)parts.get(newPartIndex));
 			}
 			else {
 				parts.add(newPartIndex, new TextPart(part.text.subSequence(start, end)));
 			}
 			// Then create the additional new part:
 			// On the right of the new part, and of the type of the old part
 			if ( part.isSegment() ) {
 				parts.add(newPartIndex+1, new Segment(null, part.text.subSequence(end, -1)));
 				validateSegmentId((Segment)parts.get(newPartIndex+1));
 			}
 			else {
 				parts.add(newPartIndex+1, new TextPart(part.text.subSequence(end, -1)));
 			}
 			// Removes from the given part the content that was copied into the two new parts
 			part.text.remove(start, -1);
 		}
 		segApplied = true;
 	}
 
 	/**
 	 * Unwraps the content of this container.
 	 * <p>This method replaces any sequences of white-spaces by a single space character.
 	 * If also remove leading and trailing white-spaces if the parameter
 	 * trimEnds is set to true.
 	 * Empty non-segment parts are removed. Empty segments are left. 
 	 * @param trimEnds true to remove leading and trailing white-spaces.
 	 */
 	public void unwrap (boolean trimEnds) {
 		boolean wasWS = trimEnds; // Removes leading white-spaces
 		for ( int i=0; i<parts.size(); i++ ) {
 			StringBuilder text = parts.get(i).text.text;
 			
 			// Normalize the part
 			for ( int j=0; j<text.length(); j++ ) {
 				switch ( text.charAt(j) ) {
 				case TextFragment.MARKER_OPENING:
 				case TextFragment.MARKER_CLOSING:
 				case TextFragment.MARKER_ISOLATED:
 					j++;
 					wasWS = false;
 					//TODO: Do we need to do something for inline between WS?
 					break;
 				case ' ':
 				case '\t':
 				case '\r':
 				case '\n':
 					if ( wasWS ) {
 						text.deleteCharAt(j);
 						j--; // Adjust
 					}
 					else {
 						text.setCharAt(j, ' ');
 						wasWS = true;
 					}
 					break;
 				default:
 					wasWS = false;
 					break;
 				}
 			}
 			
 			// Remove the part if it's empty and not a segment
 			if ( text.length() == 0 ) {
 				if ( !parts.get(i).isSegment() ) {
 					parts.remove(i);
 					i--; // Adjust
 				}
 			}
 		}
 
 		// Trim the tail parts
 		if ( trimEnds ) {
 			for ( int i=parts.size()-1; i>=0; i-- ) {
 				TextPart part = parts.get(i);
 				if ( part.text.getCodedText().endsWith(" ") ) {
 					// Remove the trailing space
 					part.text.text.deleteCharAt(part.text.text.length()-1);
 					// Stop if not empty, or remove empty non-segment
 					if ( part.text.text.length() == 0 ) {
 						if ( !parts.get(i).isSegment() ) {
 							parts.remove(i);
 							i++; // Adjust
 						}
 					}
 					else break;
 				}
 				else break;
 			}
 		}
 	}
 	
 	/**
 	 * Gets the content of the first part (segment or non-segment) of this container.
 	 * <p>This method always returns the same result as {@link #getFirstSegmentContent()} if {@link #contentIsOneSegment()}.
 	 * @return the content of the first part (segment or non-segment) of this container.
 	 * @see #getFirstSegmentContent()
 	 * @see #getLastContent()
 	 * @see #getLastSegmentContent()
 	 */
 	public TextFragment getFirstContent () {
		return parts.get(parts.size()-1).text;
 	}
 	
 	/**
 	 * Gets the content of the last part (segment or non-segment) of this container. 
 	 * <p>This method always returns the same result as {@link #getLastSegmentContent()} if {@link #contentIsOneSegment()}.
 	 * @return the content of the last part (segment or non-segment) of this container.
 	 * @see #getLastSegmentContent()
 	 * @see #getFirstContent()
 	 * @see #getFirstSegmentContent()
 	 */
 	public TextFragment getLastContent () {
 		return parts.get(parts.size()-1).text;
 	}
 	
 	/**
 	 * Clones this TextContainer, including the properties.
 	 * @return A new TextContainer object that is a copy of this one. 
 	 */
 	@Override
 	public TextContainer clone () {
 		return clone(true);
 	}
 
 	/**
 	 * Clones this container, with or without its properties. 
 	 * @param cloneProperties indicates if the properties should be cloned.
 	 * @return A new TextContainer object that is a copy of this one.
 	 */
 	public TextContainer clone (boolean cloneProperties) {
 		TextContainer newCont = new TextContainer();
 		// Clone segments
 		newCont.parts = new ArrayList<TextPart>();
 		for ( TextPart part : parts ) {
 			newCont.parts.add(part.clone());
 		}
 		newCont.segApplied = segApplied; 
 		// Clone the properties
 		if ( cloneProperties && ( properties != null )) {
 			newCont.properties = new Hashtable<String, Property>();
 			for ( Property prop : properties.values() ) {
 				newCont.properties.put(prop.getName(), prop.clone()); 
 			}
 		}
 		// Clone the annotations
 		if ( annotations != null ) {
 			newCont.annotations = annotations.clone();
 		}
 		// Returns the new container
 		return newCont;
 	}
 	
 	/**
 	 * Gets a new TextFragment representing the un-segmented content of this container. 
 	 * @return the un-segmented content of this container.
 	 */
 	public TextFragment getUnSegmentedContentCopy () {
 		return createJoinedContent(null);
 	}
 
 	/**
 	 * Sets the content of this TextContainer.
 	 * Any existing segmentation is removed.
 	 * The content becomes a single segment content.
 	 * @param content the new content to set.
 	 */
 	public void setContent (TextFragment content) {
 		createSingleSegment(null);
 		((Segment)parts.get(0)).text = content;
 	}
 
 	/**
 	 * Clears this TextContainer, removes any existing segments.
 	 * The content becomes a single empty segment content.
 	 * Keeps annotations.
 	 */
 	public void clear () {
 		createSingleSegment(null);
 	}
 	
 	/**
 	 * Indicates if this container contains at least one character.
 	 * Inline codes and annotation markers do not count as characters.
 	 * <ul>
 	 * <li>If the whole content is a single segment the check is performed on that
 	 * content and the option lookInSegments is ignored.
 	 * <li>If the content has several segments or if the single segment is not
 	 * the whole content, each segment is checked only if lookInSegment is set.
 	 * <li>The holder is always checked if no text is found in the segments.
 	 * </ul>
 	 * @param lookInSegments indicates if the possible segments in this containers should be
 	 * looked at. If this parameter is set to false, the segment marker are treated as codes.
 	 * @param whiteSpacesAreText indicates if whitespaces should be considered 
 	 * text characters or not.
 	 * @return true if this container contains at least one character according the
 	 * given options.
 	 */
 	public boolean hasText (boolean lookInSegments,
 		boolean whiteSpacesAreText)
 	{
 		for ( TextPart part : parts ) {
 			if ( part.isSegment() ) {
 				if ( lookInSegments ) {
 					if ( part.getContent().hasText(whiteSpacesAreText) ) return true;
 				}
 			}
 			else {
 				if ( part.getContent().hasText(whiteSpacesAreText) ) return true;
 			}
 		}
 		return false; // No text
 	}
 
 	/**
 	 * Indicates if this container contains at least one character that is not a whitespace.
 	 * All parts (segments and non-segments) are checed.
 	 * @param whiteSpacesAreText indicates if whitespaces should be considered 
 	 * text characters or not.
 	 * @return true if this container contains at least one character that is not a whitespace.
 	 */
 	public boolean hasText (boolean whiteSpacesAreText) {
 		for ( TextPart part : parts ) {
 			if ( part.getContent().hasText(whiteSpacesAreText) ) return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Indicates if this container contains at least one character that is not a whitespace.
 	 * This method has the same result as calling {@link #hasText(boolean, boolean)} with the parameters true and false.
 	 * @return true if this container contains at least one character that is not a whitespace.
 	 */
 	public boolean hasText () {
 		return hasText(false);
 	}
 	
 	/**
 	 * Indicates if this container is empty (no text and no codes).
 	 * @return true if this container is empty.
 	 */
 	public boolean isEmpty () {
 		for ( TextPart part : parts ) {
 			if ( !part.getContent().isEmpty() ) return false;
 		}
 		return true;
 	}
 	
 	public boolean hasProperty (String name) {
 		return (getProperty(name) != null);
 	}
 	
 	public Property getProperty (String name) {
 		if ( properties == null ) return null;
 		return properties.get(name);
 	}
 
 	public Property setProperty (Property property) {
 		if ( properties == null ) properties = new Hashtable<String, Property>();
 		properties.put(property.getName(), property);
 		return property;
 	}
 	
 	public void removeProperty (String name) {
 		if ( properties != null ) {
 			properties.remove(name);
 		}
 	}
 	
 	public Set<String> getPropertyNames () {
 		if ( properties == null ) properties = new Hashtable<String, Property>();
 		return properties.keySet();
 	}
 
 	public Annotations getAnnotations() {
 		return (annotations == null) ? new Annotations() : annotations;
 	}
 
 	public <A extends IAnnotation> A getAnnotation (Class<A> type) {
 		if ( annotations == null ) return null;
 		return annotations.get(type);
 	}
 
 	public void setAnnotation (IAnnotation annotation) {
 		if ( annotations == null ) annotations = new Annotations();
 		annotations.set(annotation);
 	}
 	
 	/**
 	 * Gets the part (segment or non-segment) for a given part index.
 	 * @param index the index of the part to retrieve. the first part has the index 0,
 	 * the second has the index 1, etc.
 	 * @return the part (segment or non-segment) for the given index.
 	 * @throws IndexOutOfBoundsException if the index is out of bounds.
 	 * @see #Segments.get(int)
 	 */
 	public TextPart get (int index) {
 		return parts.get(index);
 	}
 	
 	/**
 	 * Gets the number of parts (segments and non-segments) in this container.
 	 * This method always returns at least 1.
 	 * @return the number of parts (segments and non-segments) in this container.
 	 * @see #Segments.count()
 	 */
 	public int count () {
 		return parts.size();
 	}
 	
 	/**
 	 * Indicates if this container has a single segment. Note that it may have also non-segment parts.
 	 * Use {@link #contentIsOneSegment()} to check if the container is made of a asingle segment without
 	 * any additional non-segment parts. 
 	 * @return true if this container has a single segment.
 	 * @see #contentIsOneSegment()
 	 */
 	private boolean hasOnlyOneSegment () {
 		return (segments.count() == 1);
 	}
 	
 	private TextFragment createJoinedContent (List<Range> ranges) {
 		// Clear the ranges if needed
 		if ( ranges != null ) {
 			ranges.clear();
 		}
 		// Join all segment into a new TextFragment
 		int start = 0;
 		TextFragment tf = new TextFragment();
 		for ( TextPart part : parts ) {
 			if (( ranges != null ) && part.isSegment() ) {
 				ranges.add(new Range(start, start+part.text.text.length()));
 			}
 			start += part.text.text.length();
 			tf.append(part.getContent());
 		}
 		return tf;
 	}
 
 	/**
 	 * Joins a given part with a specified number of its following parts.
 	 * <p>If the resulting part is the only part in the container and is not a segment,
 	 * it is set automatically changed into a segment. 
 	 * <p>joinWithNext(0, -1) has the same effect as joinAll();
 	 * @param partIndex the index of the part where to append the following parts.
 	 * @param partCount the number of parts to join. You can use -1 to indicate all the parts
 	 * after the initial one. 
 	 * @return the number of parts joined to the given part (and removed from the list of parts). 
 	 */
 	public int joinWithNext (int partIndex,
 		int partCount)
 	{
 		if ( parts.size() == 1 ) {
 			return 0; // Nothing to do
 		}
 		
 		TextFragment tf = parts.get(partIndex).getContent();
 		int max = (parts.size()-partIndex)-1;
 		if (( partCount == -1 ) || ( partCount > max )) {
 			partCount = max;
 		}
 		int i = 0;
 		while ( i < partCount ) {
 			tf.append(parts.get(partIndex+1).getContent());
 			parts.remove(partIndex+1);
 			i++;
 		}
 
 		// Check single part case
 		if ( parts.size() == 1 ) {
 			if ( !parts.get(0).isSegment() ) {
 				// Ensure we have always at least one segment
 				parts.set(0, new Segment(null, parts.get(0).text));
 			}
 			// Do not reset segApplied if one part only: keep the info that is was segmented
 		}
 		return i;
 	}
 
 	/**
 	 * Checks if the id of a given segment is empty, null or a duplicate. If it is, the id
 	 * is automatically set to a new value auto-generated.
 	 * @param seg the segment to verify.
 	 */
 	private void validateSegmentId (Segment seg) {
 		if ( !Util.isEmpty(seg.id) ) {
 			// If not null or empty: check if it is a duplicate
 			boolean duplicate = false;
 			for ( TextPart tmp : parts ) {
 				if ( !tmp.isSegment() ) continue;
 				if ( seg == tmp ) continue;
 				if ( seg.id.equals(((Segment)tmp).id) ) {
 					duplicate = true;
 					break;
 				}
 			}
 			if ( !duplicate ) return; // Not a duplicate, nothing to do
 		}
 		
 		// If duplicate or empty or null: assign a default id
 		int value = 0;
 		for ( TextPart tmp : parts ) {
 			if ( tmp == seg ) continue; // Skip over the actual segment
 			if ( !tmp.isSegment() ) continue; // Skip over non-segment
 			// If it starts with a digit, it's probably a number
 			if ( Character.isDigit(((Segment)tmp).id.charAt(0)) ) {
 				// try to convert the id to a integer
 				try {
 					int val = Integer.parseInt(((Segment)tmp).id);
 					// Make the new id the same +1
 					if ( value <= val ) value = val+1;
 				}
 				catch ( NumberFormatException ignore ) {
 					// Not really an error, just a non-numeric id
 				}
 			}
 		}
 		// Set the auto-value
 		seg.id = String.valueOf(value);
 	}
 
 }
