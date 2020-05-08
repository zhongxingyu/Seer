 package com.stefankopieczek.audinance.conversion.multiplexers;
 import java.util.Arrays;
 
 import com.stefankopieczek.audinance.audiosources.DecodedSource;
 import com.stefankopieczek.audinance.audiosources.NoMoreDataException;
 import com.stefankopieczek.audinance.formats.*;
 
 public class SimpleMultiplexer implements Multiplexer
 {
 	/**
 	 * Remix the specified audio, returning a copy with precisely the specified
 	 * number of channels. If it has too few, we add channels equal to the mix
 	 * of all existing channels. If it has too many, we flatten the last few
 	 * channels into a single track.  
 	 */
 	public DecodedAudio toNChannels(DecodedAudio result, 
 	                                Integer targetNumChannels)
 	{
 		DecodedSource[] oldChannels = result.getChannels();
		DecodedSource[] newChannels = new DecodedSource[targetNumChannels];
 		
 		if (targetNumChannels > oldChannels.length)
 		{
 			// More channels have been requested than currently exist.
 			
 			// Copy the existing channels unchanged.
 			for (int idx = 0; idx < oldChannels.length; idx++)
 			{
 				newChannels[idx] = oldChannels[idx];
 			}
 			
 			// Make up the total by adding channels which are simply a 
 			// flattened copy of all existing channels.
 			for (int idx = oldChannels.length; idx < targetNumChannels; idx++)
 			{
 				newChannels[idx] = new CombinedAudio(oldChannels);
 			}							
 		}
 		else
 		{
 			// Fewer channels have been requested than currently exist.
 			// We don't want to lose audio data, so instead of dropping the
 			// surplus channels, we flatten them into a single channel.
 			
 			// Copy the first n-1 channels unchanged.
 			for (int idx = 0; idx < targetNumChannels - 1; idx++)
 			{
 				newChannels[idx] = oldChannels[idx];
 			}
 		
 			// Flatten all remaining channels into a single track, and add it.
 			newChannels[targetNumChannels-1] =
 				new CombinedAudio(Arrays.copyOfRange(oldChannels,
 						                             targetNumChannels, 
 						                             oldChannels.length));
 		}
 		
 		DecodedAudio newAudio = new DecodedAudio(result.getFormat(), 
                                                  newChannels);
 		return newAudio;
 	}
 	
 	private class CombinedAudio extends DecodedSource
 	{
 		DecodedSource[] mSources;
 		
 		public CombinedAudio(DecodedSource... sources)
 		{
 			mSources = sources;
 		}
 		
 		public double getSample(int idx)
 		{
 			double sampleValue = 0;
 			
 			// Mixing audio is as simple as adding the values of each channel
 			// in the frame together.
 			// TODO: Scale audio if it clips.
 			for (DecodedSource source : mSources)
 			{
 				try
 				{
 					// TODO: Test for numeric overflow.
 					sampleValue += source.getSample(idx);
 				}
 				catch (NoMoreDataException e)
 				{
 					// This source has no data at this instant, so makes no
 					// contribution to the mixed audio.
 				}							
 			}
 			
 			return sampleValue;
 		}
 	}
 	
 }
