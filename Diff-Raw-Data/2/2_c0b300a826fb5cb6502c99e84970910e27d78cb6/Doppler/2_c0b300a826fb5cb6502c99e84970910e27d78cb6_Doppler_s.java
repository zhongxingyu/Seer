 package net.indiespot.audio;
 
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.util.Arrays;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.openal.AL;
 
 import craterstudio.math.EasyMath;
 import craterstudio.math.Vec2;
 import craterstudio.util.HighLevel;
 
 import static org.lwjgl.openal.AL10.*;
 
 public class Doppler {
 	public static interface Sampler {
 		public float sample(double t);
 	}
 
 	static final int future = 3;
 	static final int hires = 4;
 	static final float uninited = -13371337.0f;
 	static final int sampleRate = 48000;
 	static final double soundDistancePerSecond = 334.0;
 	static final double soundDistancePerSample = soundDistancePerSecond / sampleRate;
 
 	private static void lowPassFilter(float[] src, float[] dst, int bits) {
 		float mul = 1.0f / (1 << bits);
 		float sum = 0.0f;
		float filtered = src[0];
 		for (int i = 0; i < src.length; i++) {
 			sum = sum - filtered + src[i];
 			filtered = sum * mul;
 			dst[i] = filtered;
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		Sampler noise = new Sampler() {
 
 			@Override
 			public float sample(double t) {
 				if (t < 0.0) {
 					return 0.0f;
 				}
 
 				float sum = 0.0f;
 				sum += Math.cos(t * 110 * (Math.PI * 2.0));
 				sum += Math.sin(t * 440 * (Math.PI * 2.0));
 				sum += Math.cos(t * 220 * (Math.PI * 2.0));
 				sum += Math.sin(t * 330 * (Math.PI * 2.0));
 				sum += Math.cos(t * 660 * (Math.PI * 2.0));
 				sum += Math.sin(t * 880 * (Math.PI * 2.0));
 				return sum;
 			}
 
 			private double pow(double v, double e) {
 				boolean isneg = v < 0.0;
 				v = Math.pow(isneg ? -v : +v, e);
 				return isneg ? -v : +v;
 			}
 		};
 
 		int bufferSampleCount = 1310;
 
 		float[] audioHF = new float[bufferSampleCount];
 		float[] audioMix = audioHF.clone();
 		float[] composeL = new float[sampleRate * hires * future];
 		float[] composeR = new float[sampleRate * hires * future];
 		float[] renderL = new float[bufferSampleCount];
 		float[] renderR = new float[bufferSampleCount];
 
 		Arrays.fill(composeL, uninited);
 		Arrays.fill(composeR, uninited);
 
 		// create the audio sample
 		double t = 0.0;
 		for (int i = 0; i < audioHF.length; i++) {
 			audioHF[i] = noise.sample(t) * 100;
 			t += 1.0 / sampleRate;
 		}
 		EasyMath.map(audioHF, -1.0f, +1.0f);
 
 		// apply low pass filter
 		float[] audioLF;
 		boolean lowpass = true;
 		if (lowpass) {
 			float[] audio4 = new float[audioHF.length * 4];
 			for (int i = 0; i < 4; i++) {
 				System.arraycopy(audioHF, 0, audio4, audioHF.length * i, audioHF.length);
 			}
 
 			float[] audio4lp = new float[audioHF.length * 4];
 			lowPassFilter(audio4, audio4lp, 5);
 			for (int i = 0; i < 1; i++) {
 				lowPassFilter(audio4lp, audio4, 5);
 				lowPassFilter(audio4, audio4lp, 3);
 			}
 
 			audioLF = Arrays.copyOfRange(audio4lp, audioHF.length * 1, audioHF.length * 2);
 			EasyMath.map(audioLF, -1.0f, +1.0f);
 		} else {
 			audioLF = audioHF.clone();
 		}
 
 		// visualize samples
 		{
 			SampleVisualizer.show(768, 512, audioHF, audioLF);
 		}
 
 		// make it noisy (distance attenuation compensated)
 		float noiseMul = 140;
 		for (int i = 0; i < audioHF.length; i++) {
 			audioHF[i] *= noiseMul;
 			audioLF[i] *= noiseMul;
 		}
 
 		// where are our ears, were is the audio source (moving)
 		boolean stationary = false;
 		Vec2 earL = new Vec2(-0.075f, 0);
 		Vec2 earR = new Vec2(+0.075f, 0);
 		Vec2 vel = new Vec2(20 / 3.6f, 0);
 		Vec2 currPos = new Vec2(-25, 5);
 		Vec2 nextPos = new Vec2();
 
 		if (stationary) {
 			currPos.load(-1, 2);
 			vel.load(0, 0);
 		}
 
 		AL.create();
 		int alSource = alGenSources();
 		while (true) {
 
 			// crappy OpenAL code
 			{
 
 				if (alGetSourcei(alSource, AL_SOURCE_STATE) != AL_PLAYING) {
 					alSourcePlay(alSource);
 				}
 
 				int queued = alGetSourcei(alSource, AL_BUFFERS_QUEUED);
 				if (queued > 3) {
 					int unused = alGetSourcei(alSource, AL_BUFFERS_PROCESSED);
 					if (unused > 0) {
 						IntBuffer buffers = BufferUtils.createIntBuffer(unused);
 						alSourceUnqueueBuffers(alSource, buffers);
 						while (buffers.hasRemaining()) {
 							alDeleteBuffers(buffers.get());
 						}
 					}
 
 					HighLevel.sleep(10);
 					continue;
 				}
 			}
 
 			// System.out.println();
 			System.out.println(currPos);
 
 			// where are we going to end up at the end of this sample
 			nextPos.load(vel);
 			nextPos.mul((float) audioHF.length / sampleRate);
 			nextPos.add(currPos);
 
 			// lerp between normal and lowpass version
 			float distance1 = Vec2.distance(earL, currPos);
 			float distance2 = Vec2.distance(earL, nextPos);
 			float avgDistance = (distance1 + distance2) * 0.5f;
 
 			final float lfDistance = 25;
 			final float hfDistance = 5;
 			float[] audio;
 			if (avgDistance < hfDistance) {
 				audio = audioHF;
 			} else if (avgDistance > lfDistance) {
 				audio = audioLF;
 			} else {
 				float ratio = EasyMath.invLerp(avgDistance, hfDistance, lfDistance);
 				for (int i = 0; i < audioMix.length; i++) {
 					audioMix[i] = EasyMath.lerp(audioHF[i], audioLF[i], ratio);
 				}
 				audio = audioMix;
 			}
 
 			// scatter audio samples into future
 			scatterAudioSamples(audio, composeL, 0, earL, currPos, nextPos);
 			scatterAudioSamples(audio, composeR, 0, earR, currPos, nextPos);
 
 			// gather audio samples from future
 			gatherAudioSamples(composeL, 0, renderL);
 			gatherAudioSamples(composeR, 0, renderR);
 
 			// discard used composed samples
 			compactClear(composeL, audio.length * hires);
 			compactClear(composeR, audio.length * hires);
 
 			// advance our location
 			currPos.load(nextPos);
 
 			// push to the audio card
 			ByteBuffer sampleBuffer = toBuf(renderL, renderR, 0, audioHF.length);
 			int alBuffer = alGenBuffers();
 			alBufferData(alBuffer, AL_FORMAT_STEREO16, sampleBuffer, sampleRate);
 			alSourceQueueBuffers(alSource, alBuffer);
 		}
 
 		// alDeleteBuffers(alBuffer);
 		// alDeleteSources(alSource);
 		// AL.destroy();
 	}
 
 	private static final Vec2 tmp2d = new Vec2();
 
 	private static void compactClear(float[] samples, int off) {
 		final int end = samples.length - off;
 		System.arraycopy(samples, off, samples, 0, end);
 		Arrays.fill(samples, end, samples.length, uninited);
 	}
 
 	private static final void scatterAudioSamples(float[] lowresInput, float[] highresOutput, int off, Vec2 ear, Vec2 pos1, Vec2 pos2) {
 
 		// do the following, but with increased resolution:
 		// ..
 		// create N sound sources
 		// these all emit sound at the same time
 		// but are progressively further away (causing latency)
 		// so that the sound reaches us in a stream of samples
 
 		final int sources = lowresInput.length * hires;
 		for (int i = 0; i < sources; i++) {
 			float ratio = (float) i / (sources - 1);
 			double logicalDistance = soundDistancePerSample * i / hires;
 
 			tmp2d.x = EasyMath.lerp(pos1.x, pos2.x, ratio);
 			tmp2d.y = EasyMath.lerp(pos1.y, pos2.y, ratio);
 			float realDistance = Vec2.distance(ear, tmp2d);
 			logicalDistance += realDistance;
 
 			int srcIndex = i / hires;
 			int dstIndex = off + (int) (logicalDistance / soundDistancePerSample * hires);
 
 			if (dstIndex >= highresOutput.length) {
 				// reached end of write buffer: sample too far in the future
 				break;
 			}
 
 			float sample = lowresInput[srcIndex];
 			// attenuation
 			sample /= realDistance * realDistance;
 			highresOutput[dstIndex] = sample;
 		}
 	}
 
 	private static float saturate(float value) {
 		boolean isneg = value < 0.0f;
 		float negVolume = isneg ? value : -value;
 		value = (float) (1.0 - Math.exp(negVolume * 0.1));
 		return value * (isneg ? -1 : +1);
 	}
 
 	private static final void gatherAudioSamples(float[] highresInput, int off, float[] lowresOutput) {
 		for (int i = 0; i < lowresOutput.length; i++) {
 			float sum = 0.0f;
 			int filledCount = 0;
 			for (int k = 0; k < hires; k++) {
 				float sample = highresInput[off + i * hires + k];
 				if (sample != uninited) {
 					sum += sample;
 					filledCount++;
 				}
 			}
 			float volume = (filledCount == 0) ? 0.0f : (sum / filledCount);
 			lowresOutput[i] = saturate(volume);
 		}
 	}
 
 	private static final ByteBuffer toBuf(float[] data, int off, int len) {
 		ByteBuffer sampleBuffer = BufferUtils.createByteBuffer(len * 2);
 		for (int i = 0; i < len; i++) {
 			sampleBuffer.putShort((short) (data[off + i] * Short.MAX_VALUE));
 		}
 		sampleBuffer.flip();
 		return sampleBuffer;
 	}
 
 	private static final ByteBuffer toBuf(float[] dataL, float[] dataR, int off, int len) {
 		ByteBuffer sampleBuffer = BufferUtils.createByteBuffer(len * 2 * 2);
 		for (int i = 0; i < len; i++) {
 			sampleBuffer.putShort((short) (dataL[off + i] * Short.MAX_VALUE));
 			sampleBuffer.putShort((short) (dataR[off + i] * Short.MAX_VALUE));
 		}
 		sampleBuffer.flip();
 		return sampleBuffer;
 	}
 }
