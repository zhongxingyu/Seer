 package mpicbg.spim.data.sequence;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 /**
  * A SPIM sequence consisting of a list of timepoints and a list of view setups.
  * Every (timepoint, setup) pair is a view (i.e., an image stack acquired at that time with that setup).
  *
  * @param <T>
  *            {@link TimePoint} type
  * @param <V>
  *            {@link ViewSetup} type
  *
  * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
  */
 public class SequenceDescription< T extends TimePoint, V extends ViewSetup >
 {
 	/**
 	 * timepoint id for every timepoint index.
 	 */
 	final protected TimePoints< T > timepoints;
 
 	final private TimePoints< T > unmodifiableTimepoints;
 
 	/**
 	 * angle and illumination setup for every view-setup index.
 	 */
 	final protected ArrayList< V > setups;
 
 	final private List< V > unmodifiableSetups;
 
 	/**
 	 * TODO
 	 * may be null.
 	 */
 	final protected MissingViews missingViews;
 
 	/**
 	 * load images for every view. might be null.
 	 */
 	final protected ImgLoader imgLoader;
 
 	final protected ArrayList< ViewDescription< T, V > > viewDescriptions;
 
 	public SequenceDescription( final TimePoints< T > timepoints, final List< ? extends V > setups, final MissingViews missingViews, final ImgLoader imgLoader )
 	{
 		this.timepoints = timepoints;
 		this.unmodifiableTimepoints = timepoints.getUnmodifiableTimePoints();
 		this.setups = new ArrayList< V >( setups );
 		this.unmodifiableSetups = Collections.unmodifiableList( this.setups );
 		this.missingViews = missingViews;
 		this.imgLoader = imgLoader;
 		this.viewDescriptions = createViewDescriptions();
 	}
 
 	protected ArrayList< ViewDescription< T, V > > createViewDescriptions()
 	{
 		final ArrayList< ViewDescription< T, V > > descs = new ArrayList< ViewDescription< T, V > >();
 		for ( int ti = 0; ti < numTimePoints(); ++ti )
 			for ( int si = 0; si < numViewSetups(); ++si )
				descs.add( new ViewDescription< T, V >( this, true ) );
 		if ( missingViews != null )
 			return missingViews.markMissingViews( descs );
 		else
 			return descs;
 	}
 
 	/**
 	 * Get number of timepoints in this sequence.
 	 *
 	 * @return number of timepoints
 	 */
 	final public int numTimePoints()
 	{
 		return timepoints.getTimePointList().size();
 	}
 
 	final public TimePoints< T > getTimePoints()
 	{
 		return unmodifiableTimepoints;
 	}
 
 	public ImgLoader getImgLoader()
 	{
 		return imgLoader;
 	}
 
 	/**
 	 * TODO
 	 * may be null.
 	 * @return
 	 */
 	public MissingViews getMissingViews()
 	{
 		return missingViews;
 	}
 
 	/**
 	 * Get number of view setups in this sequence.
 	 *
 	 * @return number of view setups
 	 */
 	final public int numViewSetups()
 	{
 		return setups.size();
 	}
 
 	final public List< V > getViewSetups()
 	{
 		return unmodifiableSetups;
 	}
 	
 	/**
 	 * @return All {@link ViewDescription}s, the respective isPresent flag defines if it is available for the respective timepoint
 	 */
 	public ArrayList< ViewDescription< T, V > > getViewDescriptions() { return viewDescriptions; } 
 
 	/**
 	 * @param timepoint
 	 * @param setup
 	 * @return A specific {@link ViewDescription} for a certain combination of timepoint and setup, 
 	 * its isPresent flag defines if it is available for this timepoint  
 	 */
 	public ViewDescription< T, V > getViewDescription( final int timepoint, final int setup )
 	{
 		return viewDescriptions.get( timepoint * numViewSetups() + setup );
 	}
 }
