 package at.fhv.audioracer.client.android.aui;
 
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.os.Handler;
 import android.util.Log;
 import at.fhv.audioracer.core.util.Position;
 import at.fhv.audioracer.core.util.Vector;
 
 /**
  * Sound Player for top down 2d (left/right, move to/ move away) sound with a moving observer and a static sound source.
  * 
  * @author Stephan
  * 
  */
 public class SoundPlayer2D {
 	
 	Handler handler = new Handler();
 	
 	private final double duration = 0.25; // seconds
 	private final int sampleRate = 22500; // samples per second
 	private final double samples[] = new double[sampleRate / 16 + 1]; // buffer for sample minimum frequency allowed 16hz
 	private byte generatedSnd[] = new byte[samples.length * 2 * 2]; // raw sound dataArray for two channels and two byte/sample
 	private long _oldTime; // last position change
 	private boolean _stop; // sound play/stop
 	private double _velocity; // calculated velocity of moving observer
 	
 	/**
 	 * base frequency of checkpoint tone
 	 */
 	private int _baseFrequency;
 	
 	/**
 	 * 2D coordinate of next checkpoint
 	 */
 	private Position _position;
 	
 	/**
 	 * maxDistance to checkpoint, needed for volume calculation
 	 */
 	private double _maxDistance;
 	
 	/**
 	 * AudioTrack which is used for streaming the generated sound
 	 */
 	private AudioTrack _audioTrack;
 	
 	private double _scaleOfVelocity;
 	
 	/**
 	 * 
 	 * @param maxDistance
 	 *            maximum distance to next checkpoint if 0 than 100 is assumed
 	 * @param scaleOfVelocity
 	 *            scales Velocity to higher values to increase doppler effect it is not checked against to high values
 	 */
 	public SoundPlayer2D(double maxDistance, double scaleOfVelocity) {
 		_baseFrequency = 440;
 		
 		_maxDistance = maxDistance;
 		
 		if (_maxDistance == 0) {
 			_maxDistance = 100;
 		}
 		
 		_scaleOfVelocity = scaleOfVelocity;
 		if (scaleOfVelocity == 0) {
 			scaleOfVelocity = 1;
 		}
 		
 		_audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
 				generatedSnd.length * 5, AudioTrack.MODE_STREAM);
 		
 	}
 	
 	/**
 	 * verification flag to ensure that play thread is only started once
 	 */
 	private boolean playStarted = false;
 	
 	/**
 	 * starts AudioTrack stream, start again only after calling stop()
 	 */
 	public void play() {
 		
 		if (playStarted) {
 			Log.d("audioracer", "SoundPlayer2D.test allready started!");
 			return;
 		}
 		playStarted = true;
 		
 		_stop = false;
 		
 		Thread soundThread = new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				_audioTrack.play();
 				
 				while (!_stop) {
 					if (_position == null) {
 						try {
 							long sleepTime = (long) (duration * 2.0 * 1000.0);
 							Thread.sleep(sleepTime);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						continue;
 					}
 					genTone();
 					
 					Vector zero = new Vector(new float[] { 1, 0 });
 					float cos = _position.multiplication(zero) / _position.getLength();
 					// cos 1 --> 90° left
 					// cos 0 --> 0° in front
 					// cos -1 --> 90° right
 					float directionVolumeFactor = cos / 2;
 					float distanceVolumeFactor = Math.max(0f, _position.getLength() / (float) _maxDistance);
					distanceVolumeFactor = 1 - distanceVolumeFactor;
					distanceVolumeFactor = (float) Math.pow(distanceVolumeFactor, 2);
 					
					float leftVolume = (0.5f + directionVolumeFactor) * distanceVolumeFactor;
					float rightVolume = (0.5f - directionVolumeFactor) * distanceVolumeFactor;
 					_audioTrack.setStereoVolume(leftVolume, rightVolume);
 					
 					int samplesToWrite = (int) (sampleRate * duration) + 1;
 					int samplesPerPeriod = generatedSnd.length / 2; // generating 2 periods, one for each channel
 					for (int i = 0; i < samplesToWrite; i += samplesPerPeriod) {
 						_audioTrack.write(generatedSnd, 0, generatedSnd.length);
 					}
 					
 					try {
 						long sleepTime = (long) (duration * 2.0 * 1000.0);
 						Thread.sleep(sleepTime);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				playStarted = false;
 			}
 		});
 		soundThread.start();
 		
 	}
 	
 	/**
 	 * flushes and stops audioTrack
 	 */
 	public void stop() {
 		
 		_audioTrack.flush();
 		_audioTrack.stop();
 		_stop = true;
 		
 	}
 	
 	/**
 	 * Sets relative position of moving object to static object
 	 * 
 	 * @param position
 	 *            of next checkpoint in meters
 	 */
 	synchronized public void setPosition(Position position) {
 		if (_position == null) {
 			_position = new Position(position.getPosX(), position.getPosY());
 			_oldTime = System.currentTimeMillis();
 		}
 		long newTime = System.currentTimeMillis();
 		// car to coin: if distance is reduced then it is positive velocity
 		double calcVelocity = Math.abs(_position.getLength() - position.getLength()) / ((newTime - _oldTime) / 1000.0);
 		_oldTime = newTime;
 		_position.setPosX(position.getPosX());
 		_position.setPosY(position.getPosY());
 		_velocity = calcVelocity * _scaleOfVelocity;
 		
 	}
 	
 	/**
 	 * generates tone according to current relative velocity (trajectory speed) of moving object tot static object
 	 * 
 	 * tone is generated with speedOfSound in air and 20°C which is 343m/s
 	 * 
 	 */
 	private void genTone() {
 		
 		/**
 		 * local variable for velocity, important for thread safety
 		 */
 		double velocity;
 		
 		synchronized (this) {
 			velocity = _velocity;
 		}
 		
 		double speedOfSound = 343; // 343m/s air with 20°C
 		
 		/**
 		 * set frequency to base frequency
 		 */
 		double frequency = _baseFrequency;
 		if (!Double.isNaN(velocity) && !Double.isInfinite(velocity)) {
 			frequency = _baseFrequency / (1 - (velocity / speedOfSound));
 		}
 		/**
 		 * check for supersonic speed
 		 */
 		if (Double.isNaN(frequency)) {
 			Log.d("audioracer", "Help!");
 		}
 		// generate tone for next sound loop
 		genTone(frequency);
 	}
 	
 	/**
 	 * generate tone with a given frequency
 	 * 
 	 * @param freqOfTone
 	 */
 	private void genTone(double freqOfTone) {
 		boolean periodFinished = false;
 		double currentValue;
 		int i = 1;
 		samples[0] = 0;
 		while (!periodFinished) {
 			currentValue = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
 			// check for end of period --> last sample below 0
 			if (currentValue >= 0 && samples[i - 1] < 0) {
 				periodFinished = true;
 			} else {
				if (i >= samples.length) {
					break;
				}
 				samples[i] = currentValue;
 				i++;
 			}
 		}
 		generatedSnd = new byte[i * 4]; // i samples 2 byte per sample 2 channels
 		int idx = 0;
 		for (int count = 0; count < i; count++) {
 			short val = (short) ((samples[count] * 32767));
 			// in 16 bit wav PCM, first byte is the low order byte
 			// first channel
 			generatedSnd[idx++] = (byte) (val & 0x00ff);
 			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
 			// second channel
 			generatedSnd[idx++] = (byte) (val & 0x00ff);
 			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
 			
 		}
 	}
 	
 	/**
 	 * flag for ensuring that test thread is started only once
 	 */
 	private boolean testStarted = false;
 	
 	/**
 	 * testing method for playing drive through (sound source straight ahead), driveBy(sound source only left, only right) ...
 	 */
 	public void test() {
 		// TODO: delete after testing
 		
 		final SoundPlayer2D soundPlayer = this;
 		
 		if (testStarted) {
 			Log.d("audioracer", "SoundPlayer2D.test allready started!");
 			return;
 		}
 		testStarted = true;
 		
 		Thread t = new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				// drive through
 				Position testPosition = new Position(100, 50);
 				soundPlayer.play();
 				int step = 5;
 				while (true) {
 					float currentPosX = testPosition.getPosX();
 					if (currentPosX >= 100.0) {
 						step = -5;
 					}
 					if (currentPosX <= -100.0) {
 						step = 5;
 					}
 					float nextPosX = currentPosX + step;
 					testPosition.setPosX(nextPosX);
 					soundPlayer.setPosition(testPosition);
 					
 					// for (double velocity = 0; velocity <= 100; velocity += 0.1) {
 					// // testPosition.setPosY((testPosition.getPosY() - velocity) % 1000);
 					// soundPlayer.setVector(testPosition, velocity);
 					//
 					try {
 						long sleepTime = (long) (duration * 2 * 1000);
 						Thread.sleep(sleepTime);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					// }
 				}
 			}
 		});
 		
 		t.start();
 		
 	}
 }
