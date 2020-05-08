 //-----BEGIN DISCLAIMER-----
 /*******************************************************************************
 * Copyright (c) 2010 JCrypTool Team and Contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
 //-----END DISCLAIMER-----
 package org.jcryptool.visual.verifiablesecretsharing.views;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  *
  * This class defines variables, which are used in the GUI. The values of this
 * variables are defined in the messges.properties and messages_de.properties.
  * This is an easy way to change text printed in the GUI, without doing anything
  * in the actual code.
  *
  * @author Dulghier Christoph, Reisinger Kerstin, Tiefenbacher Stefan, Wagner Thomas
  */
 
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.jcryptool.visual.verifiablesecretsharing.views.messages"; //$NON-NLS-1$
 
 	public static String VerifiableSecretSharingComposite_tab_title;
 	public static String VerifiableSecretSharingComposite_description;
 	public static String VerifiableSecretSharingComposite_restart_button;
 	public static String VerifiableSecretSharingComposite_parameters_title;
 	public static String VerifiableSecretSharingComposite_coefficients_title;
 	public static String VerifiableSecretSharingComposite_commitments_title;
 	public static String VerifiableSecretSharingComposite_shares_title;
 	public static String VerifiableSecretSharingComposite_reconstruction_title;
 	public static String VerifiableSecretSharingComposite_description_title;
 	public static String VerifiableSecretSharingComposite_parameters_players;
 	public static String VerifiableSecretSharingComposite_parameters_reconstructors;
 	public static String VerifiableSecretSharingComposite_parameters_secret;
 	public static String VerifiableSecretSharingComposite_parameters_primeMod;
 	public static String VerifiableSecretSharingComposite_parameters_primitiveRoot;
 	public static String VerifiableSecretSharingComposite_parameters_determineCoefficients;
 	
 	//Next step button for Parameters and Coefficients
 	public static String VerifiableSecretSharingComposite_nextStep_button;
 	public static String VerifiableSecretSharingComposite_coefficients_generate_button;
 	public static String VerifiableSecretSharingComposite_coefficients_commit_button;
 	public static String VerifiableSecretSharingComposite_coefficients_calculateShares_button;
 	
 	public static String VerifiableSecretSharingComposite_commitments_coefficient_subtitle;
 	public static String VerifiableSecretSharingComposite_commitments_commitment_subtitle;
 	
 	public static String VerifiableSecretSharingComposite_shares_shareNModP_subtitle;
	//playerX for Shares and Reconstructin
 	public static String VerifiableSecretSharingComposite_playerX;
 	public static String VerifiableSecretSharingComposite_shares_check_button;
 	
 	public static String VerifiableSecretSharingComposite_reconstruction_reconstruct_button;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
