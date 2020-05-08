 /**
  * AndroidWalker.java
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright (C) Wouter Lueks, Radboud University Nijmegen, Februari 2013.
  */
 
 package org.irmacard.android.util.credentials;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.irmacard.credentials.info.CredentialDescription;
 import org.irmacard.credentials.info.DescriptionStore;
 import org.irmacard.credentials.info.InfoException;
 import org.irmacard.credentials.info.IssuerDescription;
 import org.irmacard.credentials.info.TreeWalkerI;
 import org.irmacard.credentials.info.VerificationDescription;
 
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 public class AndroidWalker implements TreeWalkerI {
 	static final String IRMA_CORE = "irma_configuration/";
 	static final String TAG = "AWalker";
 	
 	DescriptionStore descriptionStore;
 	AssetManager assetManager;
 	
 	public AndroidWalker(AssetManager assetManager) {
 		this.assetManager = assetManager;
 	}
 
 	@Override
 	public void parseConfiguration(DescriptionStore descriptionStore)
 			throws InfoException {
 		this.descriptionStore = descriptionStore;
 		
 		InputStream s;
 		try {
 			s = assetManager.open(IRMA_CORE + "android/issuers.txt");
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(s));
 			String issuer = null;
 			while ((issuer = in.readLine()) != null) {
 				Log.i("filecontent", "Found issuer: " + issuer);
 				String issuerDesc = IRMA_CORE + issuer + "/description.xml";
 				
 				Log.i("filecontent", "Parsing log of: " + issuerDesc);
 				IssuerDescription id = new IssuerDescription(assetManager.open(issuerDesc));
 				descriptionStore.addIssuerDescription(id);
 				Log.i("filecontent", "Issuer added to result");
 				
 				tryProcessCredentials(issuer);
 			}
 		} catch (IOException e) {
 			new InfoException(e,
 					"Failed to read (from) android/issuers.txt file");
 		}
 
 		try {
 			s = assetManager.open(IRMA_CORE + "android/verifiers.txt");
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(s));
 			String issuer = null;
 			while ((issuer = in.readLine()) != null) {
 				// FIXME: this will duplicate stuff in the store, or at least do extra work.
 				Log.i("filecontent", "Found verifier: " + issuer);
 				String issuerDesc = IRMA_CORE + issuer + "/description.xml";
 
 				Log.i("filecontent", "Parsing log of: " + issuerDesc);
 				IssuerDescription id = new IssuerDescription(assetManager.open(issuerDesc));
 				descriptionStore.addIssuerDescription(id);
 				Log.i("filecontent", "Issuer added to result");
 
 				tryProcessVerifications(issuer);
 			}
 		} catch (IOException e) {
 			new InfoException(e,
 					"Failed to read (from) android/issuers.txt file");
 		}
 	}
 	
 	private void tryProcessCredentials(String issuer) throws InfoException {
 		String path = IRMA_CORE + issuer + "/Issues";
 		
 		try {
 			Log.i("credential", "Examining path " + path);
 			for(String cred : assetManager.list(path)) {
 				String credentialSpec = path + "/" + cred + "/description.xml";
 				Log.i("issuer+credential", "Reading credential " + credentialSpec);
 				CredentialDescription cd = new CredentialDescription(assetManager.open(credentialSpec));
 				descriptionStore.addCredentialDescription(cd);
 				Log.i("credential", cd.toString());
 			}
 		} catch (IOException e) {
 			new InfoException(e,
 					"Failed to read credentials issued by " + issuer + ".");
 		}
 	}
 	
 	private void tryProcessVerifications(String verifier) throws InfoException {
 		String path = IRMA_CORE + verifier + "/Verifies";
 
 		try {
 			Log.i("credential", "Examining path " + path);
 			for(String cred : assetManager.list(path)) {
 				String proofSpec = path + "/" + cred + "/description.xml";
 				Log.i("issuer+credential", "Reading proofSpec " + proofSpec);
 				VerificationDescription vd = new VerificationDescription(assetManager.open(proofSpec));
 				descriptionStore.addVerificationDescription(vd);
 				Log.i("credential", vd.toString());
 			}
 		} catch (IOException e) {
 			new InfoException(e,
 					"Failed to read verifications used by " + verifier + ".");
 		}
 	}
 
 	@Override
 	public InputStream retrieveFile(URI path) throws InfoException {
 		try {
 			return assetManager.open(IRMA_CORE + path.toString());
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new InfoException(e, "reading file " + path);
 		}
 	}
 
 	public Bitmap getIssuerLogo(IssuerDescription issuer) {
 		Bitmap logo = null;
 		String issuerID = issuer.getID();
 
 		try {
 			logo = BitmapFactory.decodeStream(retrieveFile(new URI(issuerID + "/logo.png")));
 		} catch (InfoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return logo;
 	}
 	
	public Bitmap getVerificationLogo(VerificationDescription verification) {
 		Bitmap logo = null;
		// TODO: is this correct?
		String issuerID = verification.getIssuerID();
 
 		try {
			logo = BitmapFactory.decodeStream(retrieveFile(new URI(issuerID + "/logo.png")));
 		} catch (InfoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return logo;
 	}
 }
