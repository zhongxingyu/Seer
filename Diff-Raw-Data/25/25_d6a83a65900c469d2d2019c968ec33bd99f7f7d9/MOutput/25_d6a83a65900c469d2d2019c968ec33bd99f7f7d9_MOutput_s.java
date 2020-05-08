 /*******************************************************************************
  * Copyright (c) 2013 Juuso Vilmunen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Juuso Vilmunen - initial API and implementation
  ******************************************************************************/
 package waazdoh.common.model;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.sound.sampled.AudioFileFormat;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 
 import waazdoh.MEnvironment;
 import waazdoh.WaazdohMusicInfo;
 import waazdoh.cutils.MLogger;
 
 public final class MOutput {
 	private List<WaveList> channels;
 	private List<AudioSampleStream> floatstreams = new LinkedList<AudioSampleStream>();
 
 	private long timestamp = System.currentTimeMillis();
 	//
 	private MLogger log = MLogger.getLogger(this);
 	private final MEnvironment env;
 	private AudioInfo info;
 
 	public MOutput(MEnvironment env) {
 		this.env = env;
 		channels = new LinkedList<WaveList>();
 		channels.add(new WaveList(env));
 		channels.add(new WaveList(env));
 		//
 		log.info("new MOutput");
 	}
 
 	public long getTimestamp() {
 		return timestamp;
 	}
 
 	public void add(AudioSampleStream fs) {
 		log.info("adding " + fs.toString());
 		floatstreams.add(fs);
 		update();
 	}
 
 	@Override
 	public String toString() {
 		return "Output[ts:" + floatstreams.size() + "][l:"
 				+ getAudioInfo().getInSeconds() + " sec][" + floatstreams + "]";
 	}
 
 	private void update() {
 		info = null;
 		timestamp = System.currentTimeMillis();
 	}
 
 	public synchronized AudioInfo getAudioInfo() {
 		if (info == null) {
 			int samplesPerSecond = WaazdohMusicInfo.DEFAULT_SAMPLERATE;
 
 			info = new AudioInfo(0, samplesPerSecond);
 			for (AudioSampleStream t : floatstreams) {
 				if (t != null) {
 					AudioInfo sinfo = t.getInfo();
 					if (sinfo.getSampleCount() > info.getSampleCount()) {
 						info = sinfo;
 					}
 				}
 			}
 		}
 		//
 		return info;
 	}
 
 	public boolean getSample(int i, AudioSample s) {
 		// TODO i might now be next sample in floatstream.
 		// if all floatstreams are drained, return from stored channels.
 		List<AudioSample> list = new ArrayList<AudioSample>();
 		int addindex = 0;
 		while (hasntGotEnoughData(i) && hasGotStreams()) {
 			int nindex = i + addindex;
 
 			AudioSample result = null;
 			AudioSample sample = new AudioSample();
 			for (AudioSampleStream floatstream : floatstreams) {
 				if (floatstream != null) {
 					boolean success = floatstream.read(nindex, sample);
 					if (result == null && success) {
 						result = new AudioSample();
 					}
 
 					if (result != null) {
 						result.add(sample);
 					} else {
 						log.info("sample null with " + floatstream
 								+ " at index " + nindex);
 						floatstreams.set(floatstreams.indexOf(floatstream),
 								null);
 					}
 				}
 			}
 
 			addindex++;
 
 			if (result != null) {
 				list.add(result);
 			}
 
 			if (list.size() > 1000 || result == null) {
 				if (list.size() > 0) {
 					addSamplesToChannel(list, 0);
 					addSamplesToChannel(list, 1);
 					list.clear();
 				}
 			}
 
 			if (result == null) {
 				log.info("Output DONE " + getChannel(0).getAudioInfo());
 				getChannel(0).setReady();
 				getChannel(1).setReady();
 				floatstreams.clear();
 
 				break;
 			}
 		}
 
 		Float left = getChannel(0).getSample(i);
 		Float right = getChannel(1).getSample(i);
 		if (left == null && right == null) {
 			return false;
 		} else {
 			s.fs[0] += left;
 			s.fs[1] += right;
 			return true;
 		}
 	}
 
 	private boolean hasGotStreams() {
 		for (AudioSampleStream floatstream : floatstreams) {
 			if (floatstream != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean hasntGotEnoughData(int i) {
 		return channels.get(0).getAudioInfo().getSampleCount() <= i;
 	}
 
 	private void addSamplesToChannel(List<AudioSample> list, int ichannel) {
 		float[] fs = new float[list.size()];
 		int index = 0;
 		float total = 0;
 		for (AudioSample sample : list) {
 			Float float1 = sample.fs[ichannel];
 			fs[index++] = float1;
 			total += float1;
 		}
 		getChannel(ichannel).addSamples(fs, fs.length);
 	}
 
 	private WaveList getChannel(int i) {
 		return channels.get(i);
 	}
 
 	public synchronized void writeWAV(OutputStream fos) throws IOException {
 		log.info("writing wav " + getAudioInfo() + " " + this);
 		AudioFormat format = new AudioFormat(getAudioInfo()
 				.getSamplesPerSecond(), 16, 2, true, true);
 
 		log.info("writing wav " + format);
 
 		int sampleCount = getAudioInfo().getSampleCount();
 		byte[] data = new byte[2 * 2 * sampleCount];
 		AudioSample sample = new AudioSample();
 		for (int i = 0; i < sampleCount; i++) {
 			getSample(i, sample);
 			int left = (short) (sample.fs[0] * Short.MAX_VALUE);
 			int right = (short) (sample.fs[1] * Short.MAX_VALUE);
 			// left
 			data[4 * i + 0] = (byte) (left >> 8);
 			data[4 * i + 1] = (byte) left;
 			// right
 			data[4 * i + 2] = (byte) (right >> 8);
 			data[4 * i + 3] = (byte) right;
 		}
 		//
 		ByteArrayInputStream bais = new ByteArrayInputStream(data);
 		AudioInputStream ais = new AudioInputStream(bais, format, sampleCount);
 		AudioSystem.write(ais, AudioFileFormat.Type.WAVE, fos);
 		//
 		fos.close();
 	}
 
 	public void clearMemory(int time) {
 		for (WaveList t : channels) {
 			t.clearMemory(time);
 		}
 	}
 }
