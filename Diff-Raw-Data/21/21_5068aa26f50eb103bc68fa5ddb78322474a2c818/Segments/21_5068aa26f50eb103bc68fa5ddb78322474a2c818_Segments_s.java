 /**
  * 
  */
 package net.sf.okapi.common.resource;
 
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.okapi.common.Range;
 
 public interface Segments extends Iterable<Segment> {
 	
 	/**
 	 * Gets an iterator for the segments of this container.
 	 * This iterator does not iterate through non-segment parts of the content.
 	 * Use {@link #iterator()} for accessing both segments and non-segments parts.
 	 * @return an iterator for the segments of this container.
 	 */
 	@Override
 	public Iterator<Segment> iterator();
 	
 	/**
 	 * Get all segments. Excludes intersegment parts.
 	 * @return List of segments.
 	 */
 	public List<Segment> asList();
 
 	/**
 	 * Swaps two segments in this container.
 	 * <p>For example, if you have a container "[segment1] [segment2]" and call
 	 * <code>swap(0,1)</code> the resulting container becomes: "[segment2] [segment1]".
 	 * <p>Note that the segments identifiers stay with their segment.
 	 * @param segIndex1 the segment index of the first segment to swap.
 	 * @param segIndex2 the segment index of the second segment to swap.
 	 */
 	public void swap (int segIndex1, int segIndex2);
 	
 	/**
 	 * Appends a segment at the end of this container.
 	 * If there is no content after the last segment, and the last segment is empty,
 	 * the new segment replaces the last one (including its id, and the new id is validated).
 	 * Otherwise the new segment is appended to the content as a new segment part and its
 	 * id is validated.
 	 * @param segment the segment to append.
 	 */
 	public void append (Segment segment);
 	
 	/**
 	 * Appends a segment at the end of this container, with an optional non-segment part just before.
 	 * If there is no content after the last segment, and the last segment is empty,
 	 * the new segment replaces the last one (including its id, and the new id is validated).
 	 * Otherwise the new segment is appended to the content as a new segment part and its
 	 * id is validated.
 	 * @param segment the segment to append.
 	 * @param textBefore the text of the non-segment part before the segment (can be null).
 	 */
 	public void append (Segment segment, String textBefore);
 	
 	/**
 	 * Appends a TextFragment as a segment at the end of this container.
 	 * If there is content after the last segment, and the last segment is empty,
 	 * the new segment replaces the last one.
 	 * Otherwise the new segment is appended to the content as a new segment part.
 	 * In all case the id of the new segment is set automatically.
 	 * @param fragment the fragment to append as a segment.
 	 */
 	public void append (TextFragment fragment);
 	
 	/**
 	 * Creates a set of segments in this container. Use {@link TextContainer#getCodedText()}
 	 * to get the coded text to use as the base for the segment boundaries.
 	 * If the content is already segmented, it is automatically un-segmented before the new
 	 * segmentation is applied.
 	 * @param ranges the ranges of the segments to create. The ranges must be ordered from the lesser
 	 * position to the higher one (i.e. from left to right). If this parameter is empty or null, no
 	 * modification is done.
 	 * @return the number of parts (segments and non-segments) created during the operation.
 	 */
 	public int create (List<Range> ranges);
 
 	/**
 	 * Creates a segment in this container. Use {@link TextContainer#getCodedText()}
 	 * to get the coded text to use as the base for the segment boundaries.
 	 * If the content is already segmented, it is automatically un-segmented before the new
 	 * segmentation is applied.
 	 * If start and end position are the same, no segment is created for those boundaries.
 	 * <p>For example:
 	 * <ul>
 	 * <li>calling createSegment(2,3) on "a b c" will result in: "a [b] c".
 	 * <li>calling createSegment(2,3) on "[a b] [c]" will result in: "a [b] c".
 	 * </ul>
 	 * @param start the start of the segment.
 	 * @param end the position just after the last character of the the segment.
 	 * @return the number of parts (segments and non-segments) created during the operation.
 	 */
 	public int create (int start, int end);
 
 	/**
 	 * Gets the number of segments in this container.
 	 * This method always returns at least 1.
	 * Use {@link #hasText(boolean, boolean)} to check for text presence.
	 * Use {@link #isEmpty()} to verify if the content is empty of not.
 	 * @return the number of segments in the container.
 	 * @see TextContainer#count()
 	 */
 	public int count ();
 	
 	/**
 	 * Gets the content of the first segment of this container.
 	 * @return the content of the first segment of this container.
 	 * @see TextContainer#getFirstContent()
 	 * @see #getLastContent()
 	 * @see TextContainer#getLastContent()
 	 */
 	public TextFragment getFirstContent ();
 	
 	/**
 	 * Gets the content of last segment of this container.
 	 * @return the content of the last segment of this container.
 	 * @see #getLastContent()
 	 * @see TextContainer#getFirstContent()
 	 * @see TextContainer#getFirstContent()
 	 */
 	public TextFragment getLastContent ();
 	
 	/**
 	 * Gets the last {@link Segment} of the container. 
 	 * @return the last Segment or null if no segment is found.
 	 */
 	public Segment getLast();
 	
 	/**
 	 * Gets the segment for a given identifier.
 	 * @param id the identifier of the segment to retrieve.
 	 * @return the segment for the given identifier or null if no segment is found.
 	 */
 	public Segment get (String id);
 	
 	/**
 	 * Gets the segment for a given segment index.
 	 * @param index the index of the segment to retrieve. The first
 	 * segment has the index 0, the second has the index 1, etc.
 	 * Note that the index value used here is not necessarily the same index as for a part.
 	 * That is: <code>getSegment(0)</code> returns the same segment as <code>getPart(0)</code> only if
 	 * the first part of the container is a segment. 
 	 * @return the segment for the given index.
 	 * <p>Use {@link #get(String)} to retrieve by segment identifier.
 	 * @throws IndexOutOfBoundsException if the index is out of bounds.
 	 * @see #get(String)
 	 * @see TextContainer#get(int)
 	 * #see {@link #iterator()}
 	 */
 	public Segment get (int index);
 	
 	/**
 	 * Merges back together all segments of this TextContainer object, and clear the 
 	 * list of segments.
 	 * The content becomes a single segment content.
 	 * @see #joinAll(List)
 	 */
 	public void joinAll ();
 	
 	/**
 	 * Merges back together all segments of this TextContainer object, and clear the 
 	 * list of segments. If required, the existing segment boundaries are saved in a given
 	 * list of ranges.
 	 * The content becomes a single segment content.
 	 * @param ranges a list of Ranges where to save the segments ranges, use null to 
 	 * not save the ranges.
 	 */
 	public void joinAll (List<Range> ranges);
 
 	/**
 	 * Gets the list of the boundaries for the current segments in this container. 
 	 * @return the list of the current segment boundaries. 
 	 */
 	public List<Range> getRanges ();
 	
 	/**
 	 * Joins to a given segments all the parts between that segment and the next, as well as
 	 * the next segment.
 	 * <p>For example for the content: " [seg1] [seg2] ", the call joinWithNext(0)
 	 * will give the result: " [seg1 seg2] ". And the call joinWithNext(1)
 	 * will give the result: " [seg1] [seg2] " (no change because there is no segment after
 	 * the segment 1.
 	 * @param segmentIndex index of the segment (not the part) where to append the next segment. 
 	 * @return the number of parts joined to the given segment (and removed from the list of parts).
 	 */
 	public int joinWithNext (int segmentIndex);
 
 	/**
 	 * Gets the part index for a given segment index.
 	 * <p>For example in the container "[segment1] [segment2] [segment3]" the sgment index for "[segment2]" is
 	 * 1 and its part index is 2 because there is one non-segment part before.
 	 * @param segIndex the segment index to convert to part index.
 	 * @return the index of the part for the given segment index.
 	 */
 	public int getPartIndex (int segIndex);
 	
 }
