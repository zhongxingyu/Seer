 package edu.tamu.csce470.mir;
 
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 public class Spectrum implements Parcelable
 {
 	// NOTE: If you add any class members, remember to add them
 	// to the Parcelable functions as well.
 	private ArrayList<Integer> baselineIntensities;
 	private ArrayList<Integer> sampleIntensities;
 	float sampleRow;
 	
 	Uri baselineImageUri;
 	Uri sampleImageUri;
 	
 	enum DisplayMode { BASELINE_IMAGE, SAMPLE_IMAGE, SPECTRUM_GRAPH }
 	
 	DisplayMode displayMode;
 
 	public Spectrum()
 	{
 		baselineIntensities = null;
 		sampleIntensities = null;
 		
 		sampleRow = 0.5f;
 		
 		baselineImageUri = null;
 		sampleImageUri = null;
 		
 		displayMode = DisplayMode.BASELINE_IMAGE;
 	}
 	
 	public static final Parcelable.Creator<Spectrum> CREATOR
 			= new Parcelable.Creator<Spectrum>()
 	{
 		@SuppressWarnings("unchecked")
 		public Spectrum createFromParcel(Parcel parcel)
 		{
 			Spectrum spectrum = new Spectrum();
 			
 			Bundle bundle = parcel.readBundle();
 			
 			if (bundle.containsKey("baselineIntensities"))
 			{
 				spectrum.baselineIntensities = (ArrayList<Integer>) bundle.getSerializable("baselineIntensities");
 			}
 			
 			if (bundle.containsKey("sampleIntensities"))
 			{
 				spectrum.sampleIntensities = (ArrayList<Integer>) bundle.getSerializable("sampleIntensities");
 			}
 			
 			spectrum.sampleRow = bundle.getFloat("sampleRow");
 			
 			if (bundle.containsKey("baselineImageUri"))
 			{
 				spectrum.baselineImageUri = bundle.getParcelable("baselineImageUri");
 			}
 			
 			if (bundle.containsKey("sampleImageUri"))
 			{
 				spectrum.sampleImageUri = bundle.getParcelable("sampleImageUri");
 			}
 			
 			spectrum.displayMode = (DisplayMode) bundle.getSerializable("displayMode");
 			
 			return spectrum;
 		}
 		
 		public Spectrum[] newArray(int size)
 		{
 			return new Spectrum[size];
 		}
 	};
 	
 	@Override
 	public void writeToParcel(Parcel out, int flags)
 	{
 		Bundle bundle = new Bundle();
 		
 		if (baselineIntensities != null)
 		{
 			bundle.putSerializable("baselineIntensities", baselineIntensities);
 		}
 		
 		if (sampleIntensities != null)
 		{
 			bundle.putSerializable("sampleIntensities", sampleIntensities);
 		}
 		
 		bundle.putFloat("sampleRow", sampleRow);
 		
 		if (baselineImageUri != null)
 		{
 			bundle.putParcelable("baselineImageUri", baselineImageUri);
 		}
 		
 		if (sampleImageUri != null)
 		{
 			bundle.putParcelable("sampleImageUri", sampleImageUri);
 		}
 		
 		bundle.putSerializable("displayMode", displayMode);
 		
 		out.writeBundle(bundle);
 	}
 	
 	@Override
 	public int describeContents()
 	{
 		return 0;
 	}
 	
 	public boolean assignBaselineSpectrum(Uri imageUri)
 	{
 		boolean success = false;
 		
 		try
 		{
 			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
 			Bitmap baselineImage = BitmapFactory.decodeFile(imageUri.getPath(), bitmapOptions);
 			baselineIntensities = getIntensities(baselineImage);
 			baselineImageUri = imageUri;
 			success = true;
 		}
 		catch (Exception e)
 		{
 			Log.e("Spectrum", "Could not decode file " + imageUri.getPath() + ": " + e);
 		}
 		
 		return success;
 	}
 	
 	public boolean assignSampleSpectrum(Uri imageUri)
 	{
 		boolean success = false;
 		
 		try
 		{
 			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
 			Bitmap sampleImage = BitmapFactory.decodeFile(imageUri.getPath(), bitmapOptions);
 			sampleIntensities = getIntensities(sampleImage);
 			sampleImageUri = imageUri;
 			success = true;
 		}
 		catch (Exception e)
 		{
 			Log.e("Spectrum", "Could not decode file " + imageUri.getPath() + ": " + e);
 		}
 		
 		return success;
 	}
 	
 	public void setSampleRow(float row)
 	{
 		sampleRow = row;
 		
 		if (baselineImageUri != null)
 		{
 			assignBaselineSpectrum(baselineImageUri);
 		}
 		
 		if (sampleImageUri != null)
 		{
 			assignBaselineSpectrum(sampleImageUri);
 		}
 	}
 	
 	public ArrayList<Integer> getBaselineIntensities()
 	{
 		return baselineIntensities;
 	}
 	
 	public ArrayList<Integer> getSampleIntensities()
 	{
 		return sampleIntensities;
 	}
 	
 	public Uri getBaselineUri()
 	{
 		return baselineImageUri;
 	}
 	
 	public Uri getSampleUri()
 	{
 		return sampleImageUri;
 	}
 	
 	public void setDisplayMode(DisplayMode mode)
 	{
 		displayMode = mode;
 	}
 	
 	public DisplayMode getDisplayMode()
 	{
 		return displayMode;
 	}
 	
 	public ArrayList<Integer> getAbsorbancies()
 	{
 		if (baselineIntensities == null || sampleIntensities == null)
 		{
 			return null;
 		}
 		else
 		{
 			ArrayList<Integer> absorbancies = new ArrayList<Integer>(sampleIntensities.size());
 			
 			assert(sampleIntensities.size() == baselineIntensities.size());
 			
 			for (int i = 0; i < sampleIntensities.size(); i++)
 			{
 				int sampleI = sampleIntensities.get(i);
 				int baselineI = baselineIntensities.get(i);
 				float absorbance = 255 - 255 * sampleI / (baselineI + 1.0f);
 				absorbancies.add(Math.round(absorbance));
 			}
 			
 			return absorbancies;
 		}
 	}
 	
 	private ArrayList<Integer> getIntensities(Bitmap spectrum)
 	{
 		assert(sampleRow >= 0 && sampleRow <= 1.0);
 		
 		int sampleRowIndex = (int) ((spectrum.getHeight() - 1) * sampleRow);
 		assert(sampleRowIndex >= 0 && sampleRowIndex < spectrum.getHeight());
 		
 		ArrayList<Integer> intensities = new ArrayList<Integer>(spectrum.getHeight());
 		
 		for (int i = 0; i < spectrum.getWidth(); i++)
 		{
 			int color = spectrum.getPixel(i, sampleRowIndex);
 			assert(Color.alpha(color) == 255);
 			
 			float intensity = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3.0f;
 			
 			intensities.add(Math.round(intensity));
 		}
 		
 		return intensities;
 	}
 }
