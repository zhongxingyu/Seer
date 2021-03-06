 /**
  * License: GPL
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  * 
  * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
  *
  */
 package mpicbg.models;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.List;
 import java.util.Collection;
 
 /**
  * Abstract class for arbitrary transformation models to be applied
  * to {@link Point Points} in n-dimensional space.
  * 
  * Model: R^n --> R^n 
  * 
  * Provides methods for generic optimization and model extraction algorithms.
  * Currently, the Random Sample Consensus \citet{FischlerB81} and a robust
  * regression method are implemented.
  *  
  * TODO: A model is planned to be a generic transformation pipeline to be
  *   applied to images, volumes or arbitrary sets of n-dimensional points.
  *   E.g. lens transformation of camera images, pose and location of mosaic
  *   tiles, non-rigid bending of confocal stacks etc.
  *  
  * 
  * BibTeX:
  * <pre>
  * &#64;article{FischlerB81,
  *	 author    = {Martin A. Fischler and Robert C. Bolles},
  *   title     = {Random sample consensus: a paradigm for model fitting with applications to image analysis and automated cartography},
  *   journal   = {Communications of the ACM},
  *   volume    = {24},
  *   number    = {6},
  *   year      = {1981},
  *   pages     = {381--395},
  *   publisher = {ACM Press},
  *   address   = {New York, NY, USA},
  *   issn      = {0001-0782},
  *   doi       = {http://doi.acm.org/10.1145/358669.358692},
  * }
  * </pre>
  * 
  * @version 0.3b
  * 
  */
 public abstract class Model< M extends Model< M > > implements CoordinateTransform
 {
 	
 	/**
 	 * @returns the minimal number of {@link PointMatch PointMatches} required
 	 *   to solve the model.
 	 */
 	abstract public int getMinNumMatches();
 	
 	/**
 	 * @deprecated "getMinSetSize" doesn't mean anything---use the more
 	 *   speaking {@link #getMinNumMatches()} instead.  
 	 */
 	@Deprecated
 	final public int getMinSetSize(){ return getMinNumMatches(); }
 	
 	// real random
 	//final Random random = new Random( System.currentTimeMillis() );
 	// repeatable results
 	final static protected Random rnd = new Random( 69997 );
 
 	
 	/**
 	 * The cost depends on what kind of algorithm is running.  It is always
 	 * true that a smaller cost is better than large cost
 	 */
 	protected double cost = Double.MAX_VALUE;
 	final public double getCost(){ return cost; }
 	final public void setCost( final double c ){ cost = c; }
 
 	/**
 	 * @deprecated The term Error may be missleading---use {@link #getCost()} instead
 	 */
 	@Deprecated
 	final public double getError(){ return getCost(); }
 	/**
 	 * @deprecated The term Error may be missleading---use {@link #getCost()} instead
 	 */
 	@Deprecated
 	final public void setError( final double e ){ setCost( e ); }
 
 	/**
 	 * "Less than" operater to make {@link Model Models} comparable.
 	 * 
 	 * @param m
 	 * @return false for {@link #cost} < 0.0, otherwise true if
 	 *   {@link #cost this.cost} is smaller than {@link #cost m.cost}
 	 */
 	final public boolean betterThan( final Model< M > m )
 	{
 		if ( cost < 0 ) return false;
 		return cost < m.cost;
 	}
 
 	/**
 	 * Randomly change the {@link Model} for some amount.
 	 * 
 	 * @param amount
 	 */
 	abstract public void shake( final float amount );
 	
 	/**
 	 * Fit the {@link Model} to a set of data points minimizing the global
 	 * transfer error.  This is assumed to be implemented as a weighted least
 	 * squares minimization.  Use
 	 * {@link #ransac(Class, List, Collection, int, double, double) ransac}
 	 * and/ or {@link #filter(Class, Collection, Collection)} to remove
 	 * outliers from your data points
 	 * 
 	 * The estimated model transfers match.p2.local to match.p1.world.
 	 * 
 	 * @param matches set of point correpondences
 	 * @throws {@link NotEnoughDataPointsException} if matches does not contain
 	 *   enough data points
 	 *   {@link IllDefinedDataPointsException} if the set of data points is
 	 *   inappropriate to solve the Model
 	 */
 	abstract public void fit( final Collection< PointMatch > matches )
 		throws NotEnoughDataPointsException, IllDefinedDataPointsException;
 
 	/**
 	 * Test the {@link Model} for a set of {@link PointMatch} candidates.
 	 * Return true if the number of inliers / number of candidates is larger
 	 * than or equal to min_inlier_ratio, otherwise false.
 	 * 
 	 * Clears inliers and fills it with the fitting subset of candidates.
 	 * 
 	 * @param candidates set of point correspondence candidates
 	 * @param inliers set of point correspondences that fit the model
 	 * @param epsilon maximal allowed transfer error
 	 * @param minInlierRatio minimal ratio of inliers (0.0 => 0%, 1.0 => 100%)
 	 */
 	final public boolean test(
 			final Collection< PointMatch > candidates,
 			final Collection< PointMatch > inliers,
 			final double epsilon,
 			final double minInlierRatio )
 	{
 		inliers.clear();
 		
 		for ( PointMatch m : candidates )
 		{
 			m.apply( this );
 			if ( m.getDistance() < epsilon ) inliers.add( m );
 		}
 		
 		final float ir = ( float )inliers.size() / ( float )candidates.size();
 		setCost( Math.max( 0.0, Math.min( 1.0, 1.0 - ir ) ) );
 		
 		return ( ir > minInlierRatio );
 	}
 		
 	/**
 	 * Estimate the {@link Model} and filter potential outliers by robust
 	 * iterative regression.
 	 * 
 	 * This method performs well on data sets with low amount of outliers.  If
 	 * you have many outliers, you can filter those with a `tolerant' RANSAC
 	 * first as done in {@link #filterRansac() filterRansac}.
 	 * 
 	 * @param candidates Candidate data points eventually inluding some outliers
 	 * @param inliers Remaining after the robust regression filter
 	 * @param maxTrust reject candidates with a cost larger than
 	 *   maxTrust * median cost
 	 * 
 	 * @return true if {@link Model} could be estimated and inliers is not
 	 *   empty, false otherwise.  If false, {@link Model} remains unchanged.
 	 */
 	final public boolean filter(
 			final Collection< PointMatch > candidates,
 			final Collection< PointMatch > inliers,
 			final float maxTrust )
 		throws NotEnoughDataPointsException
 	{
 		if ( candidates.size() < getMinNumMatches() )
 			throw new NotEnoughDataPointsException( candidates.size() + " data points are not enough to solve the Model, at least " + getMinNumMatches() + " data points required." );
 		
 		final M copy = clone();
 		
 		inliers.clear();
 		inliers.addAll( candidates );
 		final ArrayList< PointMatch > temp = new ArrayList< PointMatch >();
 		int numInliers;
 		do
 		{
 			temp.clear();
 			temp.addAll( inliers );
 			numInliers = inliers.size(); 
 			try
 			{
 				copy.fit( inliers );
 			}
 			catch ( NotEnoughDataPointsException e )
 			{
 				return false;
 			}
 			catch ( IllDefinedDataPointsException e )
 			{
 				return false;
 			}
 			final ErrorStatistic observer = new ErrorStatistic();
 			for ( final PointMatch m : temp )
 			{
 				m.apply( copy );
 				observer.add( m.getDistance() );
 			}
 			inliers.clear();
 			final double t = observer.getMedian() * maxTrust;
 			for ( final PointMatch m : temp )
 			{
				if ( m.getDistance() <= t )
 					inliers.add( m );
 			}
 			
 			copy.cost = observer.mean;
 		}
 		while ( numInliers > inliers.size() );
 		
 		if ( numInliers < copy.getMinNumMatches() )
 			return false;
 		
 		set( copy );
 		return true;
 	}
 	
 	
 	/**
 	 * Call {@link #filter(Collection, Collection, float)} with maxTrust = 4.
 	 */
 	final public boolean filter(
 			final Collection< PointMatch > candidates,
 			final Collection< PointMatch > inliers )
 		throws NotEnoughDataPointsException
 	{
 		return filter( candidates, inliers, 4f );
 	}
 	
 	
 	/**
 	 * Find the {@link Model} of a set of {@link PointMatch} candidates
 	 * containing a high number of outliers using
 	 * {@link #ransac(Class, List, Collection, int, double, double) RANSAC}
 	 * \citet[{FischlerB81}.
 	 * 
 	 * @param modelClass class of the model to be estimated
 	 * @param candidates candidate data points inluding (many) outliers
 	 * @param inliers remaining candidates after RANSAC
 	 * @param iterations number of iterations
 	 * @param epsilon maximal allowed transfer error
 	 * @param minInlierRatio minimal number of inliers to number of
 	 *   candidates
 	 * 
 	 * @return true if {@link Model} could be estimated and inliers is not
 	 *   empty, false otherwise.  If false, {@link Model} remains unchanged.
 	 */
 	final public boolean ransac(
 			final List< PointMatch > candidates,
 			final Collection< PointMatch > inliers,
 			final int iterations,
 			final double epsilon,
 			final double minInlierRatio )
 		throws NotEnoughDataPointsException
 	{
 		if ( candidates.size() < getMinNumMatches() )
 			throw new NotEnoughDataPointsException( candidates.size() + " data points are not enough to solve the Model, at least " + getMinNumMatches() + " data points required." );
 		
 		final M copy = clone();
 		final M m = clone();
 		
 		inliers.clear();
 		
 		int i = 0;
 		final HashSet< PointMatch > minMatches = new HashSet< PointMatch >();
 		
 		while ( i < iterations )
 		{
 			// choose model.MIN_SET_SIZE disjunctive matches randomly
 			minMatches.clear();
 			for ( int j = 0; j < getMinNumMatches(); ++j )
 			{
 				PointMatch p;
 				do
 				{
 					p = candidates.get( ( int )( rnd.nextDouble() * candidates.size() ) );
 				}
 				while ( minMatches.contains( p ) );
 				minMatches.add( p );
 			}
 			try { m.fit( minMatches ); }
 			catch ( IllDefinedDataPointsException e )
 			{
 				++i;
 				continue;
 			}
 			
 			final ArrayList< PointMatch > tempInliers = new ArrayList< PointMatch >();
 			
 			int numInliers = 0;
 			boolean isGood = m.test( candidates, tempInliers, epsilon, minInlierRatio );
 			while ( isGood && numInliers < tempInliers.size() )
 			{
 				numInliers = tempInliers.size();
 				try { m.fit( tempInliers ); }
 				catch ( IllDefinedDataPointsException e )
 				{
 					++i;
 					continue;
 				}
 				isGood = m.test( candidates, tempInliers, epsilon, minInlierRatio );
 			}
 			if (
 					isGood &&
 					m.betterThan( copy ) &&
 					tempInliers.size() >= getMinNumMatches() )
 			{
 				copy.set( m );
 				inliers.clear();
 				inliers.addAll( tempInliers );
 			}
 			++i;
 		}
 		if ( inliers.size() == 0 )
 			return false;
 		
 		set( copy );
 		return true;
 	}
 	
 	
 	/**
 	 * Estimate a {@link Model} from a set with many outliers by first
 	 * filtering the worst outliers with
 	 * {@link #ransac(Class, List, Collection, int, double, double) RANSAC}
 	 * \citet[{FischlerB81} and filter potential outliers by robust iterative
 	 * regression.
 	 * 
 	 * @param modelClass class of the model to be estimated
 	 * @param candidates candidate data points inluding (many) outliers
 	 * @param inliers remaining candidates after RANSAC
 	 * @param iterations number of iterations
 	 * @param maxEpsilon maximal allowed transfer error
 	 * @param minInlierRatio minimal number of inliers to number of
 	 *   candidates
 	 * @param maxTrust reject candidates with a cost larger than
 	 *   maxTrust * median cost
 	 * 
 	 * @return true if {@link Model} could be estimated and inliers is not
 	 *   empty, false otherwise.  If false, {@link Model} remains unchanged.
 	 */
 	final public boolean filterRansac(
 			final List< PointMatch > candidates,
 			final Collection< PointMatch > inliers,
 			final int iterations,
 			final float maxEpsilon,
 			final float maxInlierRatio,
 			final float maxTrust )
 		throws NotEnoughDataPointsException
 	{
 		final ArrayList< PointMatch > temp = new ArrayList< PointMatch >();
 		if (
 				ransac(
 						candidates,
 						temp,
 						iterations,
 						maxEpsilon,
 						maxInlierRatio ) &&
 				filter( temp, inliers, maxTrust ) )
 			return true;
 		return false;
 	}
 	
 	
 	/**
 	 * Call {@link #filterRansac(List, Collection, int, float, float, float)}
 	 * with maxTrust = 4.
 	 */
 	final public boolean filterRansac(
 			final List< PointMatch > candidates,
 			final Collection< PointMatch > inliers,
 			final int iterations,
 			final float maxEpsilon,
 			final float maxInlierRatio )
 		throws NotEnoughDataPointsException
 	{
 		return filterRansac(
 			candidates,
 			inliers,
 			iterations,
 			maxEpsilon,
 			maxInlierRatio,
 			4f );
 	}
 	
 		
 	/**
 	 * Create a meaningful string representation of the model for save into
 	 * text-files or display on terminals.
 	 */
 	abstract public String toString();
 	
 	
 	/**
 	 * Set the model to m
 	 * @param m
 	 */
 	abstract public void set( final M m );
 
 	
 	/**
 	 * Clone the model.
 	 */
 	abstract public M clone();
 };
