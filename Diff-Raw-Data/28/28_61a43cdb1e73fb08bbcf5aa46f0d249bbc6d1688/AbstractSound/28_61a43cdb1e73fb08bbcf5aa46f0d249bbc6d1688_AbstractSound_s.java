 package yang.sound;
 
 public abstract class AbstractSound {
 
 	protected AbstractSoundManager mManager;
 	private float mVolume;
 	private float mBalance;
 	private float mSpeed;
 	private int mRepeatCount;
 
 	public AbstractSound(AbstractSoundManager mgr) {
 		mManager = mgr;
 		mVolume = 1.0f;
 		mSpeed = 1.0f;
 	}
 
 	public abstract void stop();
 	public abstract void play(float volume, float balance, float speed, int repeat);
 
 	public void play() {
 		play(mVolume, mBalance, mSpeed, mRepeatCount);
 	}
 
 	public void playLoop() {
 		play(mVolume, mBalance, mSpeed, -1);
 	}
 
 	public void play(float volume) {
		setVolume(volume);
		play();
 	}
 
 	public void play(float volume,float balance) {
		setVolume(volume);
		setBalance(balance);
		play();
 	}
 
 	/**
 	 * Sets the volume of this sound file.
 	 * @param volume range: 0.0 - 1.0
 	 */
 	public void setVolume(float volume) {
 		mVolume = volume;
 	}
 
 	/**
 	 * Sets the speed of this sound file.
 	 * @param speed 1.0 is default
 	 */
 	public void setSpeed(float speed) {
 		mSpeed = speed;
 	}
 
 	/**
 	 * Sets the amount of times this sound is repeated until it stops.
 	 * @param count Amount of repeats, -1 for infinite
 	 */
 	public void setRepeatCount(int count) {
 		mRepeatCount = count;
 	}
 
 	/**
 	 * Sets the sound balance of this file.
 	 * @param balance -1.0 left only ... 1.0 right only
 	 */
 	public void setBalance(float balance) {
 		mBalance = balance;
 	}
 }
