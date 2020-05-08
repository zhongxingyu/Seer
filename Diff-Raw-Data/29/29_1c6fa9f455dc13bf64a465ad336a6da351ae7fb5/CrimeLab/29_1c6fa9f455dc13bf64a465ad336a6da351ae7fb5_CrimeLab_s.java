 package com.bignerdranch.android.criminalintent;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import android.content.Context;
 import android.util.Log;
 
 public class CrimeLab {
 
 	private static final String TAG = "CrimeLab";
 	private static final String FILENAME = "crimes.json";
 	
 	private CriminalIntentJSONSerializer mSerializer;
 	
 	private static CrimeLab sCrimeLab;
 	private Context mAppContext;
 
 	private ArrayList<Crime> mCrimes;
 
 	private CrimeLab(Context appContext) { 
 		mAppContext = appContext;
 //		mCrimes = new ArrayList<Crime>(); loading a JSON file instead of making an empty array
 		mSerializer = new CriminalIntentJSONSerializer(mAppContext, FILENAME);
 		
 		try {
 			mCrimes = mSerializer.loadCrimesExternal();
 		} catch (Exception e) {
 			mCrimes = new ArrayList<Crime>();
 			Log.e(TAG, "Error loading crimes: ", e);
 		}
 	}
 
 	public void addCrime(Crime crime) {
 		mCrimes.add(crime);
 	}
 	
 	public void deleteCrime(Crime crime) {
 		mCrimes.remove(crime);
 	}
 	
 	public static CrimeLab getInstance(Context c) {
 
 		if (sCrimeLab == null) {
 			sCrimeLab = new CrimeLab(c.getApplicationContext());
 		}
 
 		return sCrimeLab;
 	}
 
 	public ArrayList<Crime> getCrimes() {
 		return mCrimes;
 	}
 
 	public Crime getCrime(UUID id) {
 		for (Crime crime : mCrimes) {
 			if (crime.getId().equals(id)) { 
 				return crime; 
 			}
 		}
 		return null;
 	}
 	
 	public boolean saveCrimes() {
 		renameEmptyTitles();
 		try {
 			mSerializer.saveCrimesExternal(mCrimes);
 			Log.d(TAG, "crimes saved to file.");
 			return true;
 		} catch (Exception e) {
 			Log.e(TAG, "Error saving crimes: ", e);
 			return false;
 		}
 	}
 
 	private void renameEmptyTitles() {
 		for (Crime crime : mCrimes) {
 			if (crime.getTitle() == null || crime.getTitle().isEmpty() || crime.getTitle().length() == 0) {
 				crime.setTitle(mAppContext.getResources().getString(R.string.crime_title_default));
 			}
 		}
 	}
 	
 }
