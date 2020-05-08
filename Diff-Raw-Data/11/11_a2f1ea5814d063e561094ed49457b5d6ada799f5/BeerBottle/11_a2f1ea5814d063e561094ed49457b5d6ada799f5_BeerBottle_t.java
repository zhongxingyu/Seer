 package eu.silvere;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 public class BeerBottle implements Parcelable {
 
 	private String mName;
 	private String mBitmapPath;
 	private String mDescription;
 
 	private Bitmap mBitmap;
 
 	/**
 	 * The array of slopes for each line
 	 */
 	private List<Float> mA;
 	/**
 	 * The array of y-intercept for each line
 	 */
 	private List<Float> mB;
 	/**
 	 * The array of reference frequencies (in Hz)
 	 */
 	private List<Float> mFreq;
 	/**
 	 * The array of reference volumes (in mL)
 	 */
 	private List<Float> mVol;
 
 	public BeerBottle(String name) {
 		this.mName = name;
 		mFreq = new ArrayList<Float>();
 		mVol = new ArrayList<Float>();
 	}
 
 	/**
 	 * Constructor for BeerParameters
 	 * 
 	 * It computes slopes and y-intercept for each couple of (volume, freq)
 	 * points
 	 * 
 	 * @param param
 	 *            The array of parameters (0-index = volume, 1-index = freq)
 	 */
 	public BeerBottle(String name, float[][] param) {
 
 		this.mName = name;
 
 		mFreq = new ArrayList<Float>(param[0].length);
 		mVol = new ArrayList<Float>(param[0].length);
 
 		for (int i = 0; i < param[0].length; i++) {
 			mVol.add(param[0][i]);
 			mFreq.add(param[1][i]);
 		}
 
 		computeCoef();
 
 	}
 
 	public BeerBottle() {
 		mFreq = new ArrayList<Float>();
 		mVol = new ArrayList<Float>();
 	}
 
 	/**
 	 * Compute the volume of beer from frequency in accordance to the models
 	 * 
 	 * @param freq
 	 *            The frequency
 	 * @return The computed volume
 	 */
 	public float computeVolume(float freq) {
 
 		float v = 0f;
 
 		if (freq <= mFreq.get(0))
 			v = mA.get(0) * freq + mB.get(0);
 		else if (freq >= mFreq.get(mFreq.size() - 1))
 			v = mA.get(mA.size() - 1) * freq + mB.get(mB.size() - 1);
 		else {
 			for (int i = 0; i < mA.size(); i++) {
 				if (freq >= mFreq.get(i) && freq <= mFreq.get(i + 1))
 					v = mA.get(i) * freq + mB.get(i);
 			}
 		}
 
 		Log.d("Beer", "Beer freq " + freq + " Hz, vol " + v);
 
 		return v;
 	}
 
 	/**
 	 * Compute the coefficients (slopes and y-intercepts)
 	 */
 	public void computeCoef() {
 
 		mA = new ArrayList<Float>(mFreq.size() - 1);
 		mB = new ArrayList<Float>(mFreq.size() - 1);
 		boolean sorted = false;
 
 		while (!sorted) {
 			sorted = true;
 			for (int i = 0; i < mVol.size() - 1; i++) {
 				if (mVol.get(i) > mVol.get(i + 1)) {
 					float tmpVol = mVol.get(i);
 					float tmpFreq = mFreq.get(i);
 					mVol.set(i, mVol.get(i + 1));
 					mVol.set(i + 1, tmpVol);
 					mFreq.set(i, mFreq.get(i + 1));
 					mFreq.set(i + 1, tmpFreq);
 					sorted = false;
 				}
 			}
 		}
 
 		for (int i = 0; i < mVol.size() - 1; i++) {
 			mA.add(i, (mVol.get(i + 1) - mVol.get(i)) / (mFreq.get(i + 1) - mFreq.get(i)));
 			mB.add(i, mVol.get(i) - mA.get(i) * mFreq.get(i));
 		}
 
 	}
 
 	public void addFrequency(Float volume, Float frequency) {
 
 		mFreq.add(frequency);
 		mVol.add(volume);
 
 	}
 
 	public String getName() {
 		return mName;
 	}
 
 	public void setName(String name) {
 		this.mName = name;
 	}
 
 	public Bitmap getBitmap() {
 		return mBitmap;
 	}
 
 	public void setBitmapPath(String path) {
 		this.mBitmapPath = path;
 		loadBitmap();
 	}
 
 	public void loadBitmap() {
 		this.mBitmap = BitmapFactory.decodeFile(mBitmapPath);
 
 	}
 
 	public String getDescription() {
 		return mDescription;
 	}
 
 	public void setDescription(String description) {
 		this.mDescription = description;
 	}
 
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 
 		dest.writeString(mName);
 		dest.writeString(mBitmapPath);
 		dest.writeString(mDescription);
 
 		dest.writeList(mFreq);
 		dest.writeList(mVol);
 	}
 
 	public static final Parcelable.Creator<BeerBottle> CREATOR = new Parcelable.Creator<BeerBottle>() {
 		public BeerBottle createFromParcel(Parcel in) {
 			return new BeerBottle(in);
 		}
 
 		public BeerBottle[] newArray(int size) {
 			return new BeerBottle[size];
 		}
 	};
 
 	private BeerBottle(Parcel in) {
 		mName = in.readString();
 		mBitmapPath = in.readString();
 		mDescription = in.readString();
 
		mFreq = new ArrayList<Float>();
		mVol = new ArrayList<Float>();

 		in.readList(mFreq, null);
 		in.readList(mVol, null);
 
 		computeCoef();
 
 		loadBitmap();
 
 	}
 
 }
