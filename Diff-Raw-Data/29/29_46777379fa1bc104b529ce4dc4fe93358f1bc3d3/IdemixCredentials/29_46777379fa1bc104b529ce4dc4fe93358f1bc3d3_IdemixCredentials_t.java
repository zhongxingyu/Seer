 /**
  * IdemixCredentials.java
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
  * Copyright (C) Pim Vullers, Radboud University Nijmegen, May 2012.
  */
 
 package credentials.idemix;
 
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import net.sourceforge.scuba.smartcards.CardService;
 import net.sourceforge.scuba.smartcards.CardServiceException;
 import service.IdemixService;
 
 import com.ibm.zurich.idmx.issuance.Issuer;
 import com.ibm.zurich.idmx.issuance.Message;
 import com.ibm.zurich.idmx.showproof.Proof;
 import com.ibm.zurich.idmx.showproof.Verifier;
 
 import credentials.Attributes;
 import credentials.BaseCredentials;
 import credentials.CredentialsException;
 import credentials.idemix.spec.IdemixIssueSpecification;
 import credentials.idemix.spec.IdemixVerifySpecification;
 import credentials.spec.IssueSpecification;
 import credentials.spec.VerifySpecification;
 
 /**
  * An Idemix specific implementation of the credentials interface.
  */
 public class IdemixCredentials extends BaseCredentials {
 	//TODO: remove later
 	IdemixService service;
 
 	public IdemixCredentials() {
 		// TODO
 	}
 
 	public IdemixCredentials(CardService cs) {
 		super(cs);
		// FIXME: derive ID in better way
		service = new IdemixService(cs, (short) 4);
 	}
 
 	/**
 	 * Issue a credential to the user according to the provided specification
 	 * containing the specified values.
 	 *
 	 * @param specification of the issuer and the credential to be issued.
 	 * @param values to be stored in the credential.
 	 * @throws CredentialsException if the issuance process fails.
 	 */
 	@Override
 	public void issue(IssueSpecification specification, Attributes values)
 	throws CredentialsException {
 		if (!(specification instanceof IdemixIssueSpecification)) {
 			throw new CredentialsException(
 					"specification is not an IdemixIssueSpecification");
 		}
 		IdemixIssueSpecification spec = (IdemixIssueSpecification) specification;
 
 		// Initialise the issuer
 		Issuer issuer = new Issuer(spec.getIssuerKey(), spec.getIssuanceSpec(), null, null, spec.getValues(values));
         
 		// Initialise the recipient
 		try {
             service.open();
             service.setIssuanceSpecification(spec.getIssuanceSpec());
             service.setAttributes(spec.getIssuanceSpec(), spec.getValues(values));
 		} catch (CardServiceException e) {
         	throw new CredentialsException("Failed to issue the credential (SCUBA)");
 		}            
          
 		// Issue the credential
         Message msgToRecipient1 = issuer.round0();
         if (msgToRecipient1 == null) {
             throw new CredentialsException("Failed to issue the credential (0)");
         }
 
         Message msgToIssuer1 = service.round1(msgToRecipient1);
         if (msgToIssuer1 == null) {
             throw new CredentialsException("Failed to issue the credential (1)");
         }
 
         Message msgToRecipient2 = issuer.round2(msgToIssuer1);
         if (msgToRecipient2 == null) {
             throw new CredentialsException("Failed to issue the credential (2)");
         }
 
         service.round3(msgToRecipient2);
 	}
 
 	/**
 	 * Get a blank IssueSpecification matching this Credentials provider.
 	 * 
 	 * @return a blank specification matching this provider.
 	 */
 	public IssueSpecification issueSpecification() {
 		return new IdemixIssueSpecification();
 	}
 	
 	/**
 	 * Verify a number of attributes listed in the specification. 
 	 * 
 	 * @param specification of the credential and attributes to be verified.
 	 * @return the attributes disclosed during the verification process.
 	 * @throws CredentialsException if the verification process fails.
 	 */
 	public Attributes verify(VerifySpecification specification)
 	throws CredentialsException {
 		if (!(specification instanceof IdemixVerifySpecification)) {
 			throw new CredentialsException(
 					"specification is not an IdemixVerifySpecification");
 		}
 		IdemixVerifySpecification spec = (IdemixVerifySpecification) specification;
 		
 		// Get a nonce from the verifier
         BigInteger nonce = Verifier.getNonce(
         		spec.getProofSpec().getGroupParams().getSystemParams());
 
         // Initialise the prover
         try {
             service.open();
 		} catch (CardServiceException e) {
         	throw new CredentialsException("Failed to verify the attributes (SCUBA)");
 		}            
 
         // Construct the proof
         Proof proof = service.buildProof(nonce, spec.getProofSpec());
 
         // Initialise the verifier and verify the proof
         Verifier verifier = new Verifier(spec.getProofSpec(), proof, nonce);
         if (!verifier.verify()) {
         	throw new CredentialsException("Failed to verify the attributes (invalid proof)");
         }
 
         // Return the attributes that have been revealed during the proof        
         Attributes attributes = new Attributes();
         HashMap<String, BigInteger> values = verifier.getRevealedValues();
         Iterator<String> i = values.keySet().iterator();
         while (i.hasNext()) {
         	String id = i.next();
         	attributes.add(id, values.get(id).toByteArray());
         }
 
 		return attributes;
 	}
 	
 	/**
 	 * Get a blank VerifySpecification matching this Credentials provider.
 	 * TODO: proper implementation or remove it
 	 *
 	 * @return a blank specification matching this provider.
 	 */
 	@Override
 	public VerifySpecification verifySpecification() {
 		return null;
 	}
 }
