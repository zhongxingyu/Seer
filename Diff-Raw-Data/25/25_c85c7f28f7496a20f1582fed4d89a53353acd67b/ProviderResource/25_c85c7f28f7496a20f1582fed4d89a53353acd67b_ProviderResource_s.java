 /**
  *
  * SIROCCO
  * Copyright (C) 2013 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *
  */
 package org.ow2.sirocco.cimi.server.resource.extension;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.ow2.sirocco.cimi.domain.extension.Location;
 import org.ow2.sirocco.cimi.domain.extension.Provider;
 import org.ow2.sirocco.cimi.domain.extension.ProviderAccount;
 import org.ow2.sirocco.cimi.domain.extension.ProviderProfile;
 import org.ow2.sirocco.cimi.server.resource.ResourceInterceptorBinding;
 import org.ow2.sirocco.cimi.server.resource.RestResourceAbstract;
 import org.ow2.sirocco.cloudmanager.core.api.ICloudProviderManager;
 import org.ow2.sirocco.cloudmanager.core.api.IdentityContext;
 import org.ow2.sirocco.cloudmanager.core.api.exception.CloudProviderException;
 import org.ow2.sirocco.cloudmanager.core.api.exception.ResourceNotFoundException;
 import org.ow2.sirocco.cloudmanager.model.cimi.extension.CloudProvider;
 import org.ow2.sirocco.cloudmanager.model.cimi.extension.CloudProviderAccount;
 import org.ow2.sirocco.cloudmanager.model.cimi.extension.CloudProviderLocation;
 import org.ow2.sirocco.cloudmanager.model.cimi.extension.CloudProviderProfile;
 
 @ResourceInterceptorBinding
 @RequestScoped
 @Path("/providers")
 public class ProviderResource extends RestResourceAbstract {
     @EJB
     private ICloudProviderManager providerManager;
 
     @Context
     UriInfo uri;
 
     @Inject
     IdentityContext identityContext;
 
     @GET
     @Produces({MediaType.APPLICATION_JSON})
     public Provider.Collection getProviders() {
         Provider.Collection result = new Provider.Collection();
         List<Provider> providers = new ArrayList<Provider>();
         result.setProviders(providers);
         try {
             for (CloudProvider provider : this.providerManager.getCloudProviders()) {
                 providers.add(this.toApiProvider(provider));
             }
         } catch (CloudProviderException e) {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         return result;
     }
 
     @GET
     @Path("/{id}")
     @Produces({MediaType.APPLICATION_JSON})
     public Provider getProvider(@PathParam("id") final String providerId) {
         try {
             CloudProvider provider = this.providerManager.getCloudProviderByUuid(providerId);
             return this.toApiProvider(provider);
         } catch (ResourceNotFoundException e) {
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         } catch (CloudProviderException e) {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 
     @POST
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces({MediaType.APPLICATION_JSON})
     public Response createProvider(final Provider apiProvider) {
         try {
             CloudProvider provider = this.toProvider(apiProvider);
             this.providerManager.createCloudProvider(provider);
             apiProvider.setId(provider.getUuid());
             apiProvider.setHref(this.uri.getBaseUri() + "providers/" + apiProvider.getId());
         } catch (CloudProviderException e) {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         return Response.status(Response.Status.CREATED).entity(apiProvider).build();
     }
 
     @DELETE
     @Path("/{id}")
     public void deleteProvider(@PathParam("id") final String providerId) {
         try {
             this.providerManager.deleteCloudProvider(providerId);
         } catch (ResourceNotFoundException e) {
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         } catch (CloudProviderException e) {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 
     @GET
     @Path("{providerId}/locations")
     @Produces({MediaType.APPLICATION_JSON})
     public Location.Collection getProviderLocations(@PathParam("providerId") final String providerId) {
         Location.Collection result = new Location.Collection();
         List<Location> locations = new ArrayList<Location>();
         result.setLocations(locations);
         try {
             CloudProvider provider = this.providerManager.getCloudProviderByUuid(providerId);
             for (CloudProviderLocation loc : provider.getCloudProviderLocations()) {
                 locations.add(ProviderResource.toLocation(loc));
             }
         } catch (ResourceNotFoundException e) {
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         } catch (CloudProviderException e) {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         return result;
     }
 
     @POST
     @Path("{providerId}/locations")
     @Consumes({MediaType.APPLICATION_JSON})
     public void addLocationToProvider(@PathParam("providerId") final String providerId, final Location location) {
         try {
             this.providerManager.addLocationToCloudProvider(providerId, this.toCloudProviderLocation(location));
         } catch (ResourceNotFoundException e) {
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         } catch (CloudProviderException e) {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 
     @GET
     @Path("accounts")
     @Produces({MediaType.APPLICATION_JSON})
     public ProviderAccount.Collection getProviderAccounts() {
         ProviderAccount.Collection result = new ProviderAccount.Collection();
         List<ProviderAccount> accounts = new ArrayList<ProviderAccount>();
         result.setProviderAccounts(accounts);
         try {
            for (CloudProviderAccount account : this.providerManager.getCloudProviderAccountsByTenant(this.identityContext
                .getTenantId())) {
                 accounts.add(this.toApiProviderAccount(account));
             }
         } catch (CloudProviderException e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         return result;
     }
 
     @GET
     @Path("accounts/{accountId}")
     @Produces({MediaType.APPLICATION_JSON})
     public ProviderAccount getProviderAccount(@PathParam("accountId") final String accountId) {
         try {
             CloudProviderAccount account = this.providerManager.getCloudProviderAccountByUuid(accountId);
             if (account == null) {
                 throw new WebApplicationException(Response.Status.NOT_FOUND);
             }
             return this.toApiProviderAccount(account);
         } catch (ResourceNotFoundException e) {
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         } catch (CloudProviderException e) {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 
     @POST
     @Path("{providerId}/accounts")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces({MediaType.APPLICATION_JSON})
     public Response createProviderAccount(@PathParam("providerId") final String providerId, final ProviderAccount apiAccount) {
         try {
             CloudProviderAccount account = this.toProviderAccount(apiAccount);
             this.providerManager.createCloudProviderAccount(providerId, account);
             apiAccount.setId(account.getUuid());
             apiAccount.setHref(this.uri.getBaseUri() + "providers/" + apiAccount.getId());
         } catch (CloudProviderException e) {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         return Response.status(Response.Status.CREATED).entity(apiAccount).build();
     }
 
     @DELETE
     @Path("accounts/{accountId}")
     public void deleteProviderAccount(@PathParam("id") final String providerId, @PathParam("accountId") final String accountId) {
         try {
             this.providerManager.deleteCloudProviderAccount(accountId);
         } catch (ResourceNotFoundException e) {
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         } catch (CloudProviderException e) {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 
     @POST
     @Path("profiles")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces({MediaType.APPLICATION_JSON})
     public Response createProviderProfile(final ProviderProfile profile) {
        CloudProviderProfile created = this.providerManager.createCloudProviderProfile(this.toCloudProviderProfile(profile));
        return Response.status(Response.Status.CREATED).entity(this.toApiProviderProfile(created)).build();
     }
 
     @POST
     @Path("profiles/{profileId}/metadata")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces({MediaType.APPLICATION_JSON})
     public Response addProviderProfileMetadata(@PathParam("profileId") final String profileId,
         final ProviderProfile.AccountParameter metadata) {
         try {
             this.providerManager.addCloudProviderProfileMetadata(profileId, this.toProviderProfileMetadata(metadata));
         } catch (ResourceNotFoundException e) {
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         }
         return Response.status(Response.Status.CREATED).build();
     }
 
     @GET
     @Path("profiles")
     @Produces({MediaType.APPLICATION_JSON})
     public ProviderProfile.Collection getProviderProfiles() {
         ProviderProfile.Collection result = new ProviderProfile.Collection();
         List<ProviderProfile> profiles = new ArrayList<>();
         result.setProviderProfiles(profiles);
         for (CloudProviderProfile profile : this.providerManager.getCloudProviderProfiles()) {
             profiles.add(this.toApiProviderProfile(profile));
         }
         return result;
     }
 
     private Provider toApiProvider(final CloudProvider provider) {
         Provider p = new Provider();
         p.setId(provider.getUuid());
         p.setEndpoint(provider.getEndpoint());
         p.setApi(provider.getCloudProviderType());
         p.setDescription(provider.getDescription());
         p.setName(provider.getDescription());
         p.setHref(this.uri.getBaseUri() + "providers/" + p.getId());
         return p;
     }
 
     private CloudProvider toProvider(final Provider apiProvider) {
         CloudProvider provider = new CloudProvider();
         provider.setDescription(apiProvider.getDescription());
         provider.setEndpoint(apiProvider.getEndpoint());
         provider.setCloudProviderType(apiProvider.getApi());
         provider.setProperties(apiProvider.getProperties());
         return provider;
     }
 
     private ProviderAccount toApiProviderAccount(final CloudProviderAccount account) {
         ProviderAccount a = new ProviderAccount();
         a.setId(account.getUuid());
         a.setProviderId(account.getCloudProvider().getUuid());
         a.setIdentity(account.getLogin());
         a.setCredential(account.getPassword());
         a.setProperties(account.getProperties());
         a.setHref(this.uri.getBaseUri() + "providers/" + account.getCloudProvider().getUuid() + "/accounts/" + a.getId());
         return a;
     }
 
     private CloudProviderAccount toProviderAccount(final ProviderAccount apiAccount) {
         CloudProviderAccount account = new CloudProviderAccount();
         account.setLogin(apiAccount.getIdentity());
         account.setPassword(apiAccount.getCredential());
         account.setProperties(apiAccount.getProperties());
         return account;
     }
 
     static Location toLocation(final CloudProviderLocation location) {
         Location result = new Location();
         result.setIso3166_1(location.getIso3166_1());
         result.setIso3166_2(location.getIso3166_2());
         result.setCountryName(location.getCountryName());
         result.setRegionName(location.getStateName());
         result.setCityName(location.getCityName());
         return result;
     }
 
     private CloudProviderLocation toCloudProviderLocation(final Location location) {
         CloudProviderLocation result = new CloudProviderLocation();
         result.setIso3166_1(location.getIso3166_1());
         result.setIso3166_2(location.getIso3166_2());
         result.setCountryName(location.getCountryName());
         result.setStateName(location.getRegionName());
         result.setCityName(location.getCityName());
         return result;
     }
 
     private CloudProviderProfile.AccountParameter toProviderProfileMetadata(final ProviderProfile.AccountParameter from) {
         CloudProviderProfile.AccountParameter to = new CloudProviderProfile.AccountParameter();
         to.setName(from.getName());
         to.setAlias(from.getAlias());
         to.setDescription(from.getDescription());
         to.setRequired(from.isRequired());
         to.setType(from.getType());
         return to;
     }
 
     private CloudProviderProfile toCloudProviderProfile(final ProviderProfile profile) {
         CloudProviderProfile result = new CloudProviderProfile();
         result.setUuid(profile.getId());
         result.setDescription(profile.getDescription());
         result.setConnectorClass(profile.getConnectorClass());
         result.setType(profile.getType());
         result.setAccountParameters(new ArrayList<CloudProviderProfile.AccountParameter>());
         if (profile.getAccountParameters() != null) {
             for (ProviderProfile.AccountParameter from : profile.getAccountParameters()) {
                 CloudProviderProfile.AccountParameter to = new CloudProviderProfile.AccountParameter();
                 to.setName(from.getName());
                 to.setAlias(from.getAlias());
                 to.setDescription(from.getDescription());
                 to.setRequired(from.isRequired());
                 to.setType(from.getType());
                 result.getAccountParameters().add(to);
             }
         }
         return result;
     }
 
     private ProviderProfile.AccountParameter toApiProviderProfileMetadata(final CloudProviderProfile.AccountParameter from) {
         ProviderProfile.AccountParameter to = new ProviderProfile.AccountParameter();
         to.setName(from.getName());
         to.setAlias(from.getAlias());
         to.setDescription(from.getDescription());
         to.setRequired(from.isRequired());
         to.setType(from.getType());
         return to;
     }
 
     private ProviderProfile toApiProviderProfile(final CloudProviderProfile profile) {
         ProviderProfile result = new ProviderProfile();
         if (profile.getUuid() != null) {
             result.setId(profile.getUuid());
         }
         result.setDescription(profile.getDescription());
         result.setConnectorClass(profile.getConnectorClass());
         result.setType(profile.getType());
         result.setAccountParameters(new ArrayList<ProviderProfile.AccountParameter>());
         if (profile.getAccountParameters() != null) {
             for (CloudProviderProfile.AccountParameter from : profile.getAccountParameters()) {
                 result.getAccountParameters().add(this.toApiProviderProfileMetadata(from));
             }
         }
         return result;
     }
 }
