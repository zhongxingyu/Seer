 package com.discoverydns.dnsapiclient.command.zone;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.annotation.JsonPropertyOrder;
 import com.fasterxml.jackson.annotation.JsonRootName;
 
 /**
  * Command sent from a {@link com.discoverydns.dnsapiclient.DNSAPIClient} to the DNSAPI server,
  * to get a list of searched Zones.
  *
  * A Zone, belonging to an Account, will be managed by the DNSAPI architecture
  * for domain names resolution purpose.
  *
  * @author Chris Wright
  */
 @JsonRootName("ZoneListCommand")
 @JsonPropertyOrder({ "searchName", "searchGroup",
 		"searchNameServerInterfaceSetId", "searchNameServerSetId",
 		"searchBrandedNameServers", "searchDNSSECSigned" })
 public class ZoneListCommand {
 
 	@JsonProperty("searchName")
 	private String searchName;
 	@JsonProperty("searchNameServerInterfaceSetId")
 	private String searchNameServerInterfaceSetId;
 	@JsonProperty("searchNameServerSetId")
 	private String searchNameServerSetId;
 	@JsonProperty("searchGroup")
 	private String searchGroup;
 	@JsonProperty("searchBrandedNameServers")
 	private Boolean searchBrandedNameServers;
 	@JsonProperty("searchDNSSECSigned")
 	private Boolean searchDNSSECSigned;
 
     /**
      * Builder used to build the desired command.
      */
 	public static class Builder {
 		private String searchName;
 		private String searchNameServerInterfaceSetId;
 		private String searchNameServerSetId;
 		private String searchGroup;
 		private Boolean searchBrandedNameServers;
 		private Boolean searchDNSSECSigned;
 
         /**
          * Sets a part of the name of the Zones to look for.
          * @param searchName The name to look for
          * @return The {@link Builder}
          */
 		public Builder withSearchName(final String searchName) {
 			this.searchName = searchName;
 			return this;
 		}
 
         /**
          * Sets the UUID of the NameServer Interface Set that the Zones to look for are associated with.
          * @param searchNameServerInterfaceSetId The UUID of the associated NameServer Interface Set
          * @return The {@link Builder}
          */
 		public Builder withSearchNameServerInterfaceSetId(
 				final String searchNameServerInterfaceSetId) {
 			this.searchNameServerInterfaceSetId = searchNameServerInterfaceSetId;
 			return this;
 		}
 
         /**
          * Sets the group of the Zones to look for.
          * @param searchGroup The group to look for
          * @return The {@link Builder}
          */
 		public Builder withSearchGroup(final String searchGroup) {
 			this.searchGroup = searchGroup;
 			return this;
 		}
		
        /**
         * Sets the UUID of the NameServer Set that the Zones to look for are associated with.
         * @param searchNameServerSetId The UUID of the associated NameServer Set
         * @return The {@link Builder}
         */
 		public Builder withSearchNameServerSetId(
 				final String searchNameServerSetId) {
 			this.searchNameServerSetId = searchNameServerSetId;
 			return this;
 		}
 
         /**
          * Sets if the Zones to look for use branded nameServers.
          * @param searchBrandedNameServers true if the Zones to look for use branded nameServers, false otherwise
          * @return The {@link Builder}
          */
 		public Builder withSearchBrandedNameServers(
 				final Boolean searchBrandedNameServers) {
 			this.searchBrandedNameServers = searchBrandedNameServers;
 			return this;
 		}
 
         /**
          * Sets if the Zones to look for are DNSSEC signed.
          * @param searchDNSSECSigned true if the Zones to look for are DNSSEC signed, false otherwise
          * @return The {@link Builder}
          */
 		public Builder withSearchDNSSECSigned(final Boolean searchDNSSECSigned) {
 			this.searchDNSSECSigned = searchDNSSECSigned;
 			return this;
 		}
 
         /**
          * Builds the {@link ZoneListCommand} from the parameters set on the {@link Builder}.
          * @return The built {@link ZoneListCommand}
          */
 		public ZoneListCommand build() {
 			final ZoneListCommand zoneListCommand = new ZoneListCommand();
 			zoneListCommand.searchName = searchName;
 			zoneListCommand.searchGroup = searchGroup;
 			zoneListCommand.searchNameServerInterfaceSetId = searchNameServerInterfaceSetId;
 			zoneListCommand.searchNameServerSetId = searchNameServerSetId;
 			zoneListCommand.searchBrandedNameServers = searchBrandedNameServers;
 			zoneListCommand.searchDNSSECSigned = searchDNSSECSigned;
 
 			return zoneListCommand;
 		}
 
 	}
 
 	private ZoneListCommand() {
 	}
 
     /**
      * @return The part of the name of the Zones to look for, set on the command.
      */
 	public String getSearchName() {
 		return searchName;
 	}
 
     /**
      * @return The UUID of the NameServer Interface Set associated to the Zones to look for, set on the command.
      */
 	public String getSearchNameServerInterfaceSetId() {
 		return searchNameServerInterfaceSetId;
 	}
 
    /**
     * @return The UUID of the NameServer Set associated to the Zones to look for, set on the command.
     */
 	public String getSearchNameServerSetId() {
 		return searchNameServerSetId;
 	}
 
     /**
      * @return The group of the Zones to look for, set on the command.
      */
 	public String getSearchGroup() {
 		return searchGroup;
 	}
 
     /**
      * @return true if the Zones to look for use branded nameServers, false otherwise
      */
 	public Boolean getSearchBrandedNameServers() {
 		return searchBrandedNameServers;
 	}
 
     /**
      * @return true if the Zones to look for are DNSSEC signed, false otherwise
      */
 	public Boolean getSearchDNSSECSigned() {
 		return searchDNSSECSigned;
 	}
 
 }
