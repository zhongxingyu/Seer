 package com.zenbox;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import org.puredata.android.service.PdService;
 import org.puredata.core.PdBase;
 import org.puredata.core.utils.IoUtils;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.res.Resources;
 import android.content.res.Resources.NotFoundException;
 import android.os.IBinder;
 import android.util.Log;
 
 /**
  * Singleton class to set up a connection to the PD service and facilitate
  * message passing.
  * 
  * @author brucec5
  */
 public class AudioMessenger {
 	private static final String TAG = "ZenBox::AudioMessenger";
 
 	private static final String WAV = ".wav";
 
 	private static final String PD = ".pd";
 
 	private static AudioMessenger messenger = null;
 
 	private PdService pdService;
 
 	private final ServiceConnection connection;
 
 	private Activity act;
 
 	private ArrayList<String> samples;
 	private int sampleIndex;
 
 	private AudioMessenger(Activity act) {
 		this.act = act;
 		samples = new ArrayList<String>();
 		sampleIndex = 0;
 		connection = new ServiceConnection() {
 			@Override
 			public void onServiceConnected(ComponentName name, IBinder svc) {
 				pdService = ((PdService.PdBinder) svc).getService();
 				initPd();
 			}
 
 			@Override
 			public void onServiceDisconnected(ComponentName name) {
 				// Never run, or so I am told (required for interface)
 			}
 		};
 
 		act.bindService(new Intent(act, PdService.class), connection,
 				Activity.BIND_AUTO_CREATE);
 	}
 
 	/**
 	 * Return the instance of AudioMessenger
 	 * 
 	 * @return the AudioMessenger instance
 	 */
 	public static AudioMessenger getInstance(Activity act) {
 		if (messenger == null) {
 			messenger = new AudioMessenger(act);
 		}
 		return messenger;
 	}
 
 	/**
 	 * Sends a bang to an object in the PD patch.
 	 * 
 	 * @param recv
 	 *            symbol associated with the receiver
 	 * @return error code, 0 on success
 	 */
 	public int sendBang(String recv) {
 		return PdBase.sendBang(recv);
 	}
 
 	/**
 	 * Sends a float to an object in the PD patch.
 	 * 
 	 * @param recv
 	 *            symbol associated with the receiver
 	 * @param x
 	 *            the float to send
 	 * @return error code, 0 on success
 	 */
 	public int sendFloat(String recv, float x) {
 		return PdBase.sendFloat(recv, x);
 	}
 
 	/**
 	 * Sends a list to an object in the PD patch.
 	 * 
 	 * @param recv
 	 *            symbol associated with the receiver
 	 * @param args
 	 *            A list of arguments of type Integer, Float, or String
 	 * @return error code, 0 on success
 	 */
 	public int sendList(String recv, Object... args) {
 		return PdBase.sendList(recv, args);
 	}
 
 	/**
 	 * Sends a typed message to an object in the PD patch.
 	 * 
 	 * @param recv
 	 *            symbol associated with the receiver
 	 * @param msg
 	 *            first symbol of message
 	 * @param args
 	 *            list of arguments to the message of type Integer, Float, or
 	 *            String
 	 * @return error code, 0 on success
 	 */
 	public int sendMessage(String recv, String msg, Object... args) {
 		return PdBase.sendMessage(recv, msg, args);
 	}
 
 	/**
 	 * Sends a message to set the filename of the grain source.
 	 * 
 	 * @param fileName
 	 *            Name of the file (including .wav)
 	 * 
 	 * @return error code, 0 on success
 	 */
 	public int sendSetFileName(String fileName) {
 		return PdBase.sendMessage("setfilename", "read", new Object[] {
 				"-resize", fileName, "source-array" });
 	}
 
 	/**
 	 * Cycles to the next loaded sample.
 	 * 
 	 * @return error code, 0 on success
 	 */
 	public int sendNextFileName() {
 		sampleIndex = (sampleIndex + 1) % samples.size();
 		return sendSetFileName(samples.get(sampleIndex));
 	}
 
 	/**
 	 * Cleans up the audio messenger. To be called upon exiting the activity.
 	 */
 	public void cleanup() {
 		try {
 			pdService.stopAudio();
 			act.unbindService(connection);
 			messenger = null;
 		} catch (IllegalArgumentException e) {
 			// already unbound
 			pdService = null;
 		}
 	}
 
 	/**
 	 * Given a resource ID for a file, load in the file into the cache.
 	 * 
 	 * @param id
 	 *            Resource ID for the wav file
 	 * @param type
 	 *            File extension (including dot) of the file
 	 * @param res
 	 *            Resources instance for this activity
 	 * @throws NotFoundException
 	 *             If the given ID doesn't point to a resource
 	 * @throws IOException
 	 *             If the file couldn't be extracted into the cache
 	 */
 	private File registerResource(int id, String type, Resources res)
 			throws NotFoundException, IOException {
 		InputStream in = res.openRawResource(id);
 		String name = res.getResourceEntryName(id) + type;
 		return IoUtils.extractResource(in, name, act.getCacheDir());
 	}
 
 	/**
 	 * Given a resource ID for a wav file, load in the file and add it to the
 	 * samples list
 	 * 
 	 * @param id
 	 *            Resource ID for the wav file
 	 * @param res
 	 *            Resources instance for this activity
 	 * @throws NotFoundException
 	 *             If the given ID doesn't point to a resource
 	 * @throws IOException
 	 *             If the file couldn't be extracted into the cache
 	 */
 	private File registerSoundResource(int id, Resources res)
 			throws NotFoundException, IOException {
 		File f = registerResource(id, WAV, res);
 		samples.add(f.getName());
 		return f;
 	}
 
 	/**
 	 * Load samples/patch files and initialize the PD patch
 	 */
 	private void initPd() {
 		Resources res = act.getResources();
 		File patch = null;
 
 		try {
 			PdBase.subscribe("android");
 
 			patch = registerResource(R.raw.main, PD, res);
 			registerResource(R.raw.grain, PD, res);
 			registerResource(R.raw.grainvoice, PD, res);
 			registerResource(R.raw.simplereverb, PD, res);
 			registerResource(R.raw.velocity_synth, PD, res);
 			registerResource(R.raw.velocity_synth1, PD, res);
 			registerResource(R.raw.velocity_synth2, PD, res);
 
 			registerSoundResource(R.raw.vowels, res);
 			registerSoundResource(R.raw.violin, res);
 			registerSoundResource(R.raw.guitar, res);
 			registerSoundResource(R.raw.menchoir, res);
 			registerSoundResource(R.raw.icke, res);
 
 			PdBase.openPatch(patch);
 
 			String name = res.getString(R.string.app_name);
 
 			// -1 means use default, which should work for us.
 			pdService.initAudio(-1, -1, -1, -1);
			sendSetFileName(samples.get(0));
 			pdService.startAudio(new Intent(act, ZenBoxActivity.class),
 					R.drawable.icon, name, name);
 		} catch (IOException e) {
 			Log.e(TAG, e.toString());
 		}
 	}
 
 	public static float normalize(float in, float oMax, float oMin, float inMax) {
 		return oMin + in * (oMax - oMin) / inMax;
 	}
 }
