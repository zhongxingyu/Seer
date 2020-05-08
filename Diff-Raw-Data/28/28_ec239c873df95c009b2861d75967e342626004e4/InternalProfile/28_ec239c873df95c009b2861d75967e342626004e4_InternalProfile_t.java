 package edu.ucsb.nceas.ezid.profile;
 
 /**
  * EZID profile for internal metadata fields
  * @see http://n2t.net/ezid/doc/apidoc.html#internal-metadata
  * @author leinfelder
  * 
  * The internal elements, those marked with [X] are modifiable by clients.
  * [X]	Element		Definition	Example
  *		_owner		The identifier's owner.	jsmith
  *		_ownergroup	The identifier's owning group, which is often but not necessarily the identifier's owner's current group.	ucla
  * X	_coowners	The identifier's co-owners separated by semicolons (";", U+003B). Modifiable only by the identifier's owner.	manny ; moe ; jack
  *		_created	The time the identifier was created expressed as a Unix timestamp.	1300812337
  *		_updated	The time the identifier was last modified expressed as a Unix timestamp.	1300913550
  * X	_target		The identifier's target URL. Defaults to the identifier's EZID URL. That is, the default target URL for identifier foo is the self-referential URL http://n2t.net/ezid/id/foo.
  * 		_shadows	Shadow ARKs only. The shadowed identifier.	doi:10.9999/TEST
  *		_shadowedby	Shadowed identifiers only. The identifier's shadow ARK.	ark:/b9999/test
  * X	_profile	The identifier's preferred metadata profile (see Metadata profiles next).	erc
  * X	_status		The identifier's status (see Identifier status above).	unavailable | withdrawn by author
  * X	_export		Determines if the identifier is publicized by exporting it to external indexing and harvesting services. Must be "yes" or "no"; defaults to "yes".	yes
  * 
 * Service results are also included in this enumeration:
 * SUCCESS	success
 * ERROR	error
 * 
  * EZID internal profile metadata fields
  */
 public enum InternalProfile {
 	OWNER("_owner"),
 	OWNER_GROUP("_ownergroup"),
 	CO_OWNERS("_coowners"),
 	CREATED("_created"),
 	UPDATED("_updated"),
 	TARGET("_target"),
 	SHADOWS("_shadows"),
 	SHADOWED_BY("_shadowedby"),
 	PROFILE("_profile"),
 	STATUS("_status"),
	EXPORT("_export"),
	ERROR("error"),
	SUCCESS("success");
 	
     private final String value;
 
     private InternalProfile(String value) {
         this.value = value;
     }
 
     @Override
     public String toString() {
         return value;
     }
 
 }
