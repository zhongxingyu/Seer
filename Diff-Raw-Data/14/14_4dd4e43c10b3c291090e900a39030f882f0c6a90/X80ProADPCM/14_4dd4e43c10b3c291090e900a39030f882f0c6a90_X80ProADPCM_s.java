 package edu.ius.robotics.robots.codecs;
 
 public class X80ProADPCM
 {
 	public static int[] indexTable = 
 	{
 		-1, -1, -1, -1, 2, 4, 6, 8, 
 		-1, -1, -1, -1, 2, 4, 6, 8
 	};
 	
 	public static int[] stepsizeTable = 
 	{
 		7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 
 		19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 
 		50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 
 		130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 
 		337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 
 		876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 
 		2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 
 		5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 
 		15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
 	};
 	
 	public static class ADPCMState 
 	{
 		/* ignoring the convention to wrap these with getters and setters */
 		public short previousOutput; /* previous output value */
 		public char index; /* index into step size table */
 	}
 	
 	ADPCMState adpcmState;
 	
 	public void init()
 	{
 		adpcmState.previousOutput = 0;
 		adpcmState.index = 0;
 	}
 	
 	public X80ProADPCM()
 	{
 		adpcmState = new ADPCMState();
 	}
 	
 	public short[] decode(byte[] input, byte length)
 	{
 		int outputLength = 2*(length & 0xFF);
 		short[] output = new short[outputLength];
 		
 		short predictedOutput = adpcmState.previousOutput;
 		char index = adpcmState.index;
 		int step = stepsizeTable[index];
 		
 		short predictedOutputDelta;
 		int sign;
 		int delta;
 		
 		boolean bufferStep = false;
 		int inputIndex = 0;
 		int outputIndex = 0;
 		
 		for (int i = output.length; 0 < i; i--)
 		{
 			/* Step 1: Get the data value */
 			if (bufferStep)
 			{
				delta = (byte) (input[inputIndex] & 0xff);
 			}
 			else
 			{
				delta = (byte) (input[++inputIndex] >>> 4 & 0xff);
 			}
 			bufferStep = !bufferStep;
 			
 			/* Step 2: Find new index value (for later) */
 			index += indexTable[delta];
 			if (index < 0)
 			{
 				index = 0;
 			}
 			else if (88 < index)
 			{
 				index = 88;
 			}
 			
 			/* Step 3: Separate sign and magnitude */
 			sign = delta & 0x08;
 			delta = delta & 0x07;
 			
 			/* Step 4: Combine difference and new predicted value */
 			/*
 			 * Computes predictedOutputDelta = (delta + 0.5)*step/4, but see comment
 			 * in encodeADPCM();
 			 */
 			
			predictedOutputDelta = (short) (step >>> 3);
 			if (0 < (delta & 0x04))
 			{
 				predictedOutputDelta += step;
 			}
 			
 			if (0 < (delta & 0x02))
 			{
 				predictedOutputDelta += step >> 1;
 			}
 			
 			if (0 < (delta & 0x01))
 			{
 				predictedOutputDelta += step >> 2;
 			}
 			
 			if (0 < sign)
 			{
 				predictedOutput -= predictedOutputDelta;
 			}
 			else
 			{
 				predictedOutput += predictedOutputDelta;
 			}
 			
 			/* Step 5: Clamp output value */
 			if (32767 < predictedOutput)
 			{
 				predictedOutput = 32767;
 			}
 			else if (predictedOutput < -32768)
 			{
 				predictedOutput = -32768;
 			}
 			
 			/* Step 6: Update step value */
 			step = stepsizeTable[index];
 			
 			/* Step 7: Output value */
 			output[outputIndex++] = predictedOutput;
 		}
 		
 		adpcmState.previousOutput = predictedOutput;
 		adpcmState.index = index;
 		
 		return output;
 	}
 	
 	public byte[] encode(short[] input, short length)
 	{
 		int outputLength = 2*(length & 0xFF);
 		byte[] output = new byte[outputLength];
 		short predictedOutput = adpcmState.previousOutput;
 		char index = adpcmState.index;
 		int step = stepsizeTable[index];
 		int outputHolder = 0;
 		
 		boolean bufferStep = true;
 		int inputIndex = 0;
 		int outputIndex = 0;
 		for (int i = outputLength; 0 < i; --i)
 		{
 			short inputValue = input[inputIndex++];
 			
 			/* Step 1: Compute difference with previous value */
 			int diff = inputValue - predictedOutput;
 			int sign = (diff < 0) ? 8 : 0;
 			if (0 < sign)
 			{
 				diff = -diff;
 			}
 			
 			/* Step 2: Divide and clamp */
 			/* Note
 			 * This code *approximately* computes:
 			 * 	delta = diff*4/step;
 			 * 	predictedOutputDelta = (0.5*delta)*step/4
 			 * but in shift step bits are dropped. The net results of this is
 			 * that even if you have fast mul/div hardware you cannot put it to
 			 * good use since the fixup would be too expensive.
 			 */
 			int delta = 0;
 			int predictedOutputDelta = step >>> 3;
 			
 			if (step <= diff)
 			{
 				delta = 4;
 				diff -= step;
 				predictedOutputDelta += step;
 			}
 			
 			step >>= 1;
 			if (step <= diff)
 			{
 				delta |= 2;
 				diff -= step;
 				predictedOutputDelta += step;
 			}
 			
 			step >>= 1;
 			if (step <= diff)
 			{
 				delta |= 1;
 				predictedOutputDelta += step;
 			}
 			
 			/* Step 3: Update previous value */
 			if (0 < sign)
 			{
 				predictedOutput -= predictedOutputDelta;
 			}
 			else
 			{
 				predictedOutput += predictedOutputDelta;
 			}
 			
 			/* Step 4: Clamp previous value to 16 bits */
 			if (32767 < predictedOutput)
 			{
 				predictedOutput = 32767;
 			}
 			else if (predictedOutput < -32768)
 			{
 				predictedOutput = -32768;
 			}
 			
 			/* Step 5: Assemble value, update index and step values */
 			delta |= sign;
 			
 			index += indexTable[delta];
 			if (index < 0)
 			{
 				index = 0;
 			}
 			else if (88 < index)
 			{
 				index = 88;
 			}
 			
 			/* Step 6: Output value */
 			if (bufferStep)
 			{
				outputHolder = (delta << 4) & 0xf0;
 			}
 			else
 			{
				output[outputIndex++] = (byte) (((delta & 0x0f) | outputHolder) & 0xff);
 			}
 			bufferStep = !bufferStep;
 		}
 		
 		/* Output last step, if needed */
 		if (!bufferStep)
 		{
 			output[outputIndex++] = (byte) (outputHolder & 0xFF);
 		}
 		
 		adpcmState.previousOutput = predictedOutput;
 		adpcmState.index = index;
 		
 		return output;
 	}
 }
