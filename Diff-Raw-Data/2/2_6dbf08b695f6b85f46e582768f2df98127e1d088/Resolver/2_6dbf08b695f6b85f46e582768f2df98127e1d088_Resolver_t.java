 /*
  * WoTNS - Resolver.java - Copyright © 2011 David Roden
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
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.wotns.main;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 
 import net.pterodactylus.util.object.Default;
 import net.pterodactylus.wotns.freenet.wot.Identity;
 import net.pterodactylus.wotns.freenet.wot.IdentityManager;
 import net.pterodactylus.wotns.freenet.wot.OwnIdentity;
 import net.pterodactylus.wotns.freenet.wot.Trust;
 import freenet.keys.FreenetURI;
 
 /**
  * TODO
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class Resolver {
 
 	private final IdentityManager identityManager;
 
 	private String ownIdentityId;
 
 	public Resolver(IdentityManager identityManager) {
 		this.identityManager = identityManager;
 	}
 
 	public void setOwnIdentityId(String ownIdentityId) {
 		this.ownIdentityId = ownIdentityId;
 	}
 
 	//
 	// ACTIONS
 	//
 
 	public FreenetURI resolveURI(String shortUri) throws MalformedURLException {
 		int firstSlash = shortUri.indexOf('/');
 		if (firstSlash == -1) {
 			throw new MalformedURLException("At least one slash is required.");
 		}
 		String shortName = shortUri.substring(0, firstSlash);
 		String target = shortUri.substring(firstSlash + 1);
 		Identity identity = locateIdentity(shortName);
 		System.out.println("located identity: " + identity);
 		if (identity == null) {
 			return null;
 		}
 		return new FreenetURI(identity.getProperty("tns." + target));
 	}
 
 	//
 	// PRIVATE METHODS
 	//
 
 	private Identity locateIdentity(String shortName) {
 		int atSign = shortName.indexOf('@');
 		String identityName = shortName;
 		String keyStart = "";
 		if (atSign > -1) {
 			identityName = shortName.substring(0, atSign);
 			keyStart = shortName.substring(atSign + 1);
 		}
 		@SuppressWarnings("hiding")
 		final OwnIdentity ownIdentity;
 		if (this.ownIdentityId == null) {
 			Set<OwnIdentity> ownIdentities = identityManager.getAllOwnIdentities();
 			if (!ownIdentities.isEmpty()) {
 				ownIdentity = ownIdentities.iterator().next();
 			} else {
 				ownIdentity = null;
 			}
 		} else {
 			ownIdentity = identityManager.getOwnIdentity(ownIdentityId);
 		}
 		if (ownIdentity == null) {
 			return null;
 		}
 		System.out.println("using own identity " + ownIdentity + " to resolve " + shortName);
		Set<Identity> trustedIdentities = Default.forNull(identityManager.getTrustedIdentities(ownIdentity), Collections.<Identity> emptySet());
 		List<Identity> matchingIdentities = new ArrayList<Identity>();
 		System.out.println("checking " + trustedIdentities);
 		for (Identity identity : trustedIdentities) {
 			if (identity.getNickname().equals(identityName) && identity.getId().startsWith(keyStart)) {
 				matchingIdentities.add(identity);
 			}
 		}
 		if (matchingIdentities.isEmpty()) {
 			return null;
 		}
 		Collections.sort(matchingIdentities, new Comparator<Identity>() {
 
 			@Override
 			public int compare(Identity leftIdentity, Identity rightIdentity) {
 				Trust leftTrust = leftIdentity.getTrust(ownIdentity);
 				Trust rightTrust = rightIdentity.getTrust(ownIdentity);
 				int leftTrustCombined = ((leftTrust.getExplicit() != null) ? leftTrust.getExplicit() : 0) + ((leftTrust.getImplicit() != null) ? leftTrust.getImplicit() : 0);
 				int rightTrustCombined = ((rightTrust.getExplicit() != null) ? rightTrust.getExplicit() : 0) + ((rightTrust.getImplicit() != null) ? rightTrust.getImplicit() : 0);
 				return leftTrustCombined - rightTrustCombined;
 			}
 		});
 		return matchingIdentities.get(0);
 	}
 
 }
