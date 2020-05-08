 package inra.watershed.process;
 
 /**
  *
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
  * Authors: Ignacio Arganda-Carreras, Philippe Andrey, Axel Poulet
  */
 
 import java.util.ArrayList;
 
 import ij.IJ;
 import ij.ImagePlus;
 import ij.ImageStack;
import ij.process.ImageProcessor;
 import ij.process.StackConverter;
 
 /**
  * This class calculates the connected components of a binary image
  * in 3D. This implementation is based on the C++ code by Eric Biot 
  * at INRA, and it works as well for 2D images. 
  *
  */
 public class ComponentLabelling 
 {
 	/** 2D or 3D image to be processed */
 	ImagePlus inputImage = null;
 	/** number of current labels */
 	int numLabels = 0;
 	/** list of labels */
 	ArrayList<Integer> labelTable = null;
 
 	/**
 	 * Constructor for the connected components class
 	 * @param inputImage the binary image to use
 	 */
 	public ComponentLabelling( ImagePlus inputImage )
 	{
 		this.inputImage = inputImage;
 	}
 
 	/**
 	 * Increases the number of current labels by one
 	 * @return current number of labels
 	 */
 	int newLabel()
 	{
 		++numLabels;
 		
 		labelTable.add( numLabels );
 
 		return numLabels;
 	}
 	
 	/**
 	 * Compare 3 labels and update them to the minimum one 
 	 * @param l1 label 1
 	 * @param l2 label 2
 	 * @param l3 label 3
 	 * @return minimum label between the 3
 	 */
 	int equivLabel( int l1, int l2, int l3 )
 	{
 	  int min = 0;
 
 	  if ( l1 < l2 )
 	  {
 	    min = l1;
 	    labelTable.set( l2, l1 );
 	    labelTable.set( l3, l1 );
 	  }
 	  else
 	  {
 	    min = l2;
 	    labelTable.set( l1, l2 );
 	    labelTable.set( l3, l2 );
 	  }
 
 	  if ( l3 < min )
 	  {
 	    min = l3;
 	    labelTable.set( l1, l3 );
 	    labelTable.set( l2, l3 );
 	    labelTable.set( l3, l3 );
 	  }
 
 	  return min;
 	}
 
 	/**
 	 * Apply 2-pass connected components to the input
 	 * image with 6-voxel connectivity.
 	 * @return 32-bit image with the found connected components
 	 */
 	public ImagePlus apply()
 	{
 		ImagePlus imageOutput = inputImage.duplicate();
 		// Make sure output image is 32-bit
 		if( imageOutput.getType() != ImagePlus.GRAY32 )
 		{
 			if( imageOutput.getImageStackSize() > 1 )
 				(new StackConverter( imageOutput )).convertToGray32();
 			else
			{
				ImageProcessor ip = imageOutput.getProcessor();
				ip.resetMinAndMax();	// important not to lose the ones
										// in the conversion
 				imageOutput = new ImagePlus( imageOutput.getTitle(), 
						 						ip.convertToFloat());
			}
 					
 		}
		
 		final ImageStack outputStack = imageOutput.getStack();
 		
 		
 		final ImageStack inputStack = inputImage.getStack();
 	    final int size1 = inputStack.getWidth();
 	    final int size2 = inputStack.getHeight();
 	    final int size3 = inputStack.getSize();
 	    
 		int v1, v2, v3;  // previous voxels along dimension 1, 2, 3
 		int i, j, k;
 
 		labelTable = new ArrayList<Integer>( );
 		
 		labelTable.add( 0 );
 		
 		numLabels = 0;
 
 		IJ.showStatus( "Calculated connected components..." );
 		
 
 		// first plane of the volume, first voxel
 		if ( outputStack.getVoxel( 0,0,0 ) != 0 )
 		{
 			outputStack.setVoxel( 0,0,0, newLabel() );
 		}
 
 		// first plane, first column
 		k = i = 0;
 		for (j = 1 ; j < size2; ++j)
 		{
 			if ( outputStack.getVoxel(i,j,k) != 0 )
 			{
 				v2 = (int) outputStack.getVoxel(i,j-1,k) ;
 				outputStack.setVoxel( i,j,k, v2 != 0 ? v2: newLabel() );
 			}
 		}
 
 		// rest of first plane
 		for (k = 0, i = 1; i < size1; ++i)
 		{
 			// first voxel in the column
 			if ( outputStack.getVoxel(i, 0, 0) != 0 )
 			{
 				v1 = (int)( outputStack.getVoxel(i-1,0,0) );
 				outputStack.setVoxel( i,0,0, v1 != 0 ? v1: newLabel() );
 			}
 
 			// other voxel in the column
 			for (j = 1; j < size2; ++j)
 			{
 				if ( outputStack.getVoxel( i,j,k ) != 0 )
 				{
 					v1 = (int)( outputStack.getVoxel(i-1,j,k) );
 					v2 = (int)( outputStack.getVoxel(i,j-1,k) );
 
 					if ( v1 != 0 )
 					{
 						if ( v2 != 0 )
 						{
 							while ( v1 != labelTable.get( v1 ) ) 
 								v1 = labelTable.get( v1 );
 							while ( v2 != labelTable.get( v2 ) ) 
 								v2 = labelTable.get( v2 );
 							if( v1 < v2 )
 							{
 								labelTable.set( v2, v1 );
 								outputStack.setVoxel( i,j,k, v1 );
 							}
 							else
 							{
 								labelTable.set( v1, v2 );
 								outputStack.setVoxel( i,j,k, v2 );
 							}
 							 
 						}
 						else
 						{
 							outputStack.setVoxel( i,j,k, v1 );
 						}
 					}
 					else
 					{
 						outputStack.setVoxel( i,j,k, v2 != 0 ? v2: newLabel() );
 					}
 				}
 			}
 		}
 
 
 
 		// rest of planes
 		// first column of other planes in the volume
 		for (k = 1; k < size3; ++k)
 		{
 			for (i = 0, j = 1; j < size2; ++j)
 			{
 				if ( outputStack.getVoxel( i,j,k ) != 0 )
 				{
 					v2 = (int)( outputStack.getVoxel( i, j-1, k   ) );
 					v3 = (int)( outputStack.getVoxel( i,   j, k-1 ) );
 
 					if ( v2 != 0 )
 					{
 						if ( v3 != 0 )
 						{
 							while ( v2 != labelTable.get( v2 ) ) 
 								v2 = labelTable.get( v2 );
 							while ( v3 != labelTable.get( v3 ) ) v3 = labelTable.get( v3 );
 							if( v2 < v3 )
 							{
 								outputStack.setVoxel( i,j,k, v2 );
 								labelTable.set( v3, v2 );
 							}
 							else
 							{
 								outputStack.setVoxel( i,j,k, v3 );
 								labelTable.set( v2, v3 );
 							}
 						}
 						else
 						{
 							outputStack.setVoxel( i,j,k, v2 );							
 						}
 					}
 					else
 					{
 						outputStack.setVoxel( i,j,k, v3 != 0  ? v3: newLabel() );
 					}
 				}
 			}
 		}
 
 		// rest of planes: rest of columns
 		for (k = 1; k < size3; ++k)
 		{
 			for (i = 1; i < size1; ++i)
 			{
 				// first voxel of the column
 				if ( outputStack.getVoxel(i,0,k) != 0 )
 				{
 					v1 = (int)( outputStack.getVoxel(i-1,0,k) );
 					v3 = (int)( outputStack.getVoxel(i,0,k-1) );
 
 					if ( v1 != 0 )
 					{
 						if ( v3 != 0 )
 						{
 							while ( v1 != labelTable.get( v1 ) ) 
 								v1 = labelTable.get( v1 );
 							while ( v3 != labelTable.get( v3 ) ) 
 								v3 = labelTable.get( v3 );
 							
 							if( v1 < v3 )
 							{
 								outputStack.setVoxel( i,0,k, v1);
 								labelTable.set( v3, v1 );	
 							}
 							else
 							{
 								outputStack.setVoxel( i,0,k, v3);
 								labelTable.set( v1, v3 );
 							}																					
 						}
 						else
 						{
 							outputStack.setVoxel( i,0,k, v1);
 						}
 					}
 					else
 					{
 						outputStack.setVoxel( i,0,k, v3 != 0 ? v3: newLabel() );
 					}
 				}
 
 				// other voxels of the column
 				for (j = 1; j < size2; ++j)
 				{
 					if ( outputStack.getVoxel(i,j,k) != 0 )
 					{
 						v1 = (int)( outputStack.getVoxel(i-1,j,k) );
 						v2 = (int)( outputStack.getVoxel(i,j-1,k) );
 						v3 = (int)( outputStack.getVoxel(i,j,k-1) );
 
 						if ( v2 != 0 )
 						{
 							if ( v1 != 0 )
 							{
 								if ( v3 != 0 )
 								{
 									while ( v1 != labelTable.get( v1 ) ) v1 = labelTable.get( v1 );
 									while ( v2 != labelTable.get( v2 ) ) v2 = labelTable.get( v2 );
 									while ( v3 != labelTable.get( v3 ) ) v3 = labelTable.get( v3 );
 									outputStack.setVoxel( i,j,k, equivLabel( v1, v2, v3 ) );
 								}
 								else
 								{
 									while ( v1 != labelTable.get( v1 ) ) v1 = labelTable.get( v1 );
 									while ( v2 != labelTable.get( v2 ) ) v2 = labelTable.get( v2 );
 									if( v1 < v2 )
 									{
 										outputStack.setVoxel( i,j,k, v1 );
 										labelTable.set( v2, v1 );
 									}
 									else
 									{
 										outputStack.setVoxel( i,j,k, v2 );
 										labelTable.set( v1, v2 );
 									}
 								}
 							}
 							else
 							{
 								if ( v3 != 0 )
 								{
 									while ( v2 != labelTable.get( v2 ) ) v2 = labelTable.get( v2 );
 									while ( v3 != labelTable.get( v3 ) ) v3 = labelTable.get( v3 );
 									if( v2 < v3 )
 									{
 										outputStack.setVoxel( i,j,k, v2 );
 										labelTable.set( v3, v2 );
 									}
 									else
 									{
 										outputStack.setVoxel( i,j,k, v3 );
 										labelTable.set( v2, v3 );
 									}
 								}
 								else
 								{
 									outputStack.setVoxel( i,j,k, v2 );
 								}
 							}
 						}
 						else
 						{
 							if ( v1 != 0 )
 							{
 								if ( v3 != 0 )
 								{
 									while ( v1 != labelTable.get( v1 ) ) v1 = labelTable.get( v1 );
 									while ( v3 != labelTable.get( v3 ) ) v3 = labelTable.get( v3 );
 									
 									if( v1 < v3 )
 									{
 										outputStack.setVoxel( i,j,k, v1 );
 										labelTable.set( v3, v1 );
 									}
 									else
 									{
 										outputStack.setVoxel( i,j,k, v3 );
 										labelTable.set( v1, v3 );
 									}									
 								}
 								else
 								{
 									outputStack.setVoxel( i,j,k, v1 );
 								}
 							}
 							else
 							{
 								outputStack.setVoxel( i,j,k, v3 != 0 ? v3: newLabel() );
 							}
 						}
 					}
 				}
 			}
 			
 		}
 
 		// update equivalence table
 		int numEquivalences = 0;
 		
 		for (int v = 1; v <= numLabels; v++)
 		{
 			if ( labelTable.get(v) == v )
 			{
 				labelTable.set( v, v - numEquivalences );
 			}
 			else
 			{
 				labelTable.set( v, labelTable.get( labelTable.get( v ) ) );
 				++numEquivalences;
 			}
 		}
 		numLabels -= numEquivalences;
 
 		// second sweep of the volume: update by equivalence table
 		for (k = 0; k < size3; k++)
 			for (i = 0; i < size1; i++)
 				for (j = 0; j < size2; j++)
 					outputStack.setVoxel( i,j,k, labelTable.get( (int) outputStack.getVoxel(i,j,k) ) );
 
 		imageOutput.setTitle("connected-components-" + inputImage.getTitle() );
 		return imageOutput;		
 	}
 
 }
