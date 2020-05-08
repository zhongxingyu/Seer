 package edu.ius.robotics.robots.boards;
 
 /* The following is taken pretty much directly from the 
  * PMB5010 serial protocol documentation
  */
 
 public class PMB5010
 {
 	public static class ADPCM 
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
 		
 		public ADPCM()
 		{
 			adpcmState.previousOutput = 0;
 			adpcmState.index = 0;
 		}
 		
 		// lpInData points to msg[6], nLen = Data_Len
 		public short[] dealWithAudio(byte[] input)
 		{
 			return decodeADPCM(input);
 		}
 		
 		public short[] decodeADPCM(byte[] input)
 		{
 			int outputLength = 2*input.length;
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
 		
 		public byte[] encodeADPCM(short[] input)
 		{
 			int outputLength = 2*input.length;
 			byte[] output = new byte[outputLength];
 			short predictedOutput = adpcmState.previousOutput;
 			char index = adpcmState.index;
 			int step = stepsizeTable[index];
 			
 			int outputHolder = 0;
 			boolean bufferStep = true;
 			int inputIndex = 0;
 			int outputIndex = 0;
 			for (int i = 2*input.length; 0 < i; --i)
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
 				output[outputIndex++] = (byte) outputHolder;
 			}
 			
 			adpcmState.previousOutput = predictedOutput;
 			adpcmState.index = index;
 			
 			return output;
 		}
 	}
 	
 	/* Packet Info */
 	public static final int HEADER_LENGTH = 6;
 	public static final int FOOTER_LENGTH = 3;
 	public static final int DID_OFFSET = 4;
 	
 	/* Start transmission, End transmission */
 	public static final byte STX0 = 94; // 0x5e
 	public static final byte STX1 = 2; // 0x02
 	public static final byte ETX0 = 94; // 0x5e
 	public static final byte ETX1 = 13; // 0x13
 	
 	/* RID */
 	public static final byte RID_HOST = 0x00;
 	public static final byte RID_PMB5010 = 0x08;
 	
 	/* RESERVED */
 	public static final byte RESERVED = 0x00;
 	
 	/* FLAG */
 	public static final byte FLAG_VALUE = 0x06;
 	
 	/* DID Listing */
 	public static final byte AUDIO_PACKET = 0x0A;
 	public static final byte START_AUDIO_RECORDING = 0x0B;
 	public static final byte STOP_AUDIO_RECORDING = 0x0C;
 	public static final byte START_AUDIO_PLAYBACK = 0x0D;
 	public static final byte STOP_AUDIO_PLAYBACK = 0x0E;
 	public static final byte TAKE_PHOTO = 0x20;
 	
     /*
      * calcCRC method comes directly from PMB5010 Protocol documentation.
      */
     public static byte calcCRC(byte[] buf) 
     {
 		byte shift_reg, sr_lsb, data_bit, v;
 		byte fb_bit;
 		int z;
 		shift_reg = 0; // initialize the shift register
 		z = buf.length - 5;
 		
 		for (int i = 0; i < z; ++i) 
 		{
 		    v = (byte) (buf[2 + i]); // start from RID
 		    
 		    // for each bit
 		    for (int j = 0; j < 8; ++j) 
 		    {
 				// isolate least sign bit
 				data_bit = (byte) ((v & 0x01) & 0xff);
 				sr_lsb = (byte) ((shift_reg & 0x01) & 0xff);
 				// calculate the feed back bit
 				fb_bit = (byte) (((data_bit ^ sr_lsb) & 0x01) & 0xff);
 				shift_reg = (byte) ((shift_reg & 0xff) >>> 1);
 				
 				if (fb_bit == 1)
 				{
 				    shift_reg = (byte) ((shift_reg ^ 0x8C) & 0xff);
 				}
 				
 				v = (byte) ((v & 0xff) >>> 1);
 		    }
 		}
 		
 		return shift_reg;
     }
     
     public static byte[] startAudioRecording(byte voiceSegmentLength)
     {
     	byte[] msg = new byte[10];
     	
     	msg[0] = STX0;
     	msg[1] = STX1;
     	msg[2] = RID_PMB5010;
     	msg[3] = RESERVED;
     	msg[4] = START_AUDIO_RECORDING;
     	msg[5] = 1; // len
     	msg[6] = voiceSegmentLength;
     	msg[7] = calcCRC(msg);
     	msg[8] = ETX0;
     	msg[9] = ETX1;
     	
     	return msg;
     }
     
     public static byte[] stopAudioRecording(byte voiceSegmentLength)
     {
     	byte[] msg = new byte[9];
     	
     	msg[0] = STX0;
     	msg[1] = STX1;
     	msg[2] = RID_PMB5010;
     	msg[3] = RESERVED;
     	msg[4] = STOP_AUDIO_RECORDING;
     	msg[5] = 0;
     	msg[6] = calcCRC(msg);
     	msg[7] = ETX0;
     	msg[8] = ETX1;
     	
     	return msg;
     }
     
     public static byte[] startAudioPlayback(short sampleLength)
     {
     	byte[] msg = new byte[10];
     	
     	int length = 4;
     	
     	if (sampleLength < 16000)
     	{
     		length = 1;
     	}
     	else if (sampleLength < 16000*20)
     	{
     		length = 2;
     	}
     	else if (sampleLength < 16000*32)
     	{
     		length = 3;
     	}
     	
     	msg[0] = STX0;
     	msg[1] = STX1;
     	msg[2] = RID_PMB5010;
     	msg[3] = 0; // TODO SEQ
     	msg[4] = START_AUDIO_PLAYBACK;
     	msg[5] = 1;
     	msg[6] = (byte) (length & 0xff);
     	msg[7] = calcCRC(msg);
     	msg[8] = ETX0;
     	msg[9] = ETX1;
     	
     	return msg;
     }
     
     public static byte[] stopAudioPlayback()
     {
     	byte[] msg = new byte[9];
     	
     	msg[0] = STX0;
     	msg[1] = STX1;
     	msg[2] = RID_PMB5010;
     	msg[3] = 0;
     	msg[4] = STOP_AUDIO_PLAYBACK;
     	msg[5] = 0;
     	msg[6] = calcCRC(msg);
     	msg[7] = ETX0;
     	msg[8] = ETX1;
     	
     	return msg;
     }
     
    public static byte[] continueAudioPlayback(short[] audioSample)
     {
     	ADPCM adpcm = new ADPCM();
     	
    	byte[] encodedAudioSample = adpcm.encodeADPCM(audioSample);
    	int z = encodedAudioSample.length;
    	
    	byte[] msg = new byte[8+z];
     	
     	msg[0] = STX0;
     	msg[1] = STX1;
     	msg[2] = RID_PMB5010;
     	msg[3] = 0;
     	msg[4] = AUDIO_PACKET;
     	msg[5] = (byte) (z & 0xff);
     	
     	for (int i = 0; i < z; ++i)
     	{
    		msg[6+i] = encodedAudioSample[i];
     	}
     	
     	msg[6+z] = calcCRC(msg);
     	msg[7+z] = ETX0;
     	msg[8+z] = ETX1;
     	
     	return msg;
     }
 }
