 /*
  * Copyright 2007-2012 The Europeana Foundation
  *
  *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
  *  by the European Commission;
  *  You may not use this work except in compliance with the Licence.
  * 
  *  You may obtain a copy of the Licence at:
  *  http://joinup.ec.europa.eu/software/page/eupl
  *
  *  Unless required by applicable law or agreed to in writing, software distributed under
  *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
  *  any kind, either express or implied.
  *  See the Licence for the specific language governing permissions and limitations under
  *  the Licence.
  */
 package eu.europeana.uim.mintclient.service;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import com.rabbitmq.client.AMQP;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.DefaultConsumer;
 import com.rabbitmq.client.Envelope;
 import eu.europeana.uim.mintclient.ampq.MintAMPQClientASync;
 import eu.europeana.uim.mintclient.ampq.MintAMPQClientSync;
 import eu.europeana.uim.mintclient.ampq.MintClientFactory;
 import eu.europeana.uim.mintclient.jibxbindings.CreateImportCommand;
 import eu.europeana.uim.mintclient.jibxbindings.CreateOrganizationAction;
 import eu.europeana.uim.mintclient.jibxbindings.CreateOrganizationCommand;
 import eu.europeana.uim.mintclient.jibxbindings.CreateOrganizationResponse;
 import eu.europeana.uim.mintclient.jibxbindings.CreateUserAction;
 import eu.europeana.uim.mintclient.jibxbindings.CreateUserCommand;
 import eu.europeana.uim.mintclient.jibxbindings.CreateUserResponse;
 import eu.europeana.uim.mintclient.jibxbindings.ErrorResponse;
 import eu.europeana.uim.mintclient.jibxbindings.OrganizationExistsCommand;
 import eu.europeana.uim.mintclient.jibxbindings.OrganizationExistsResponse;
 import eu.europeana.uim.mintclient.service.exceptions.MintOSGIClientException;
 import eu.europeana.uim.mintclient.service.exceptions.MintRemoteException;
 import eu.europeana.uim.mintclient.utils.MintClientUtils;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.ControlledVocabularyProxy;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.logging.LoggingEngine;
 import eu.europeana.uim.Registry;
 import eu.europeana.uim.orchestration.Orchestrator;
 import eu.europeana.uim.storage.StorageEngineException;
 import eu.europeana.uim.sugar.SugarCrmService;
 
 /**
  * Base Class for implementing the UIM Mint connectivity
  * 
  * @author Georgios Markakis <gwarkx@hotmail.com>
  * @since 6 Mar 2012
  */
 public class MintUIMServiceImpl implements MintUIMService {
 
 	private static MintAMPQClientSync synchronousClient;
 	private static MintAMPQClientASync asynchronousClient;
 	private static Registry registry;
 	private static Orchestrator<?> orchestrator;
 	private static SugarCrmService sugservice;
 	private static LoggingEngine<?> logger;
 	private static ReponseHandler resphandler;
 	private static Set<String> providerlock;
 
 	private final static String HEADERERRORMESSAGE = "hasError";
 	private final static String HEADERCORRELATIONID = "correlation_id";
 
 	/**
 	 * Private constructor, instantiated via private factory method
 	 */
 	public MintUIMServiceImpl(Registry registry, SugarCrmService sugservice) {
 		MintUIMServiceImpl.registry = registry;
 		MintUIMServiceImpl.orchestrator = registry.getOrchestrator();
 		MintUIMServiceImpl.sugservice = sugservice;
 		MintUIMServiceImpl.logger = (LoggingEngine<?>) (registry != null ? registry
 				.getLoggingEngine() : null);
 		providerlock = Collections.synchronizedSet(new HashSet<String>());
 		MintUIMServiceImpl.resphandler = new ReponseHandler(registry,
 				orchestrator, sugservice, providerlock);
 	}
 
 	/**
 	 * Factory method currently used for initialising the service instance
 	 * (currently used by Spring).
 	 * 
 	 * @param registryref
 	 * @param orchestratorref
 	 * @param service
 	 * @return
 	 */
 	public static MintUIMServiceImpl createService(Registry registryref,
 			SugarCrmService service) {
 
 		MintClientFactory factory = new MintClientFactory();
 		try {
 			synchronousClient = (MintAMPQClientSync) factory.syncMode()
 					.createClient();
 			asynchronousClient = (MintAMPQClientASync) factory.asyncMode(
 					MintUIMServiceImpl.UIMConsumerListener.class)
 					.createClient();
 			return new MintUIMServiceImpl(registryref, service);
 		} catch (MintOSGIClientException e) {
 			registryref.getLoggingEngine().logFailed(Level.SEVERE,
 					"MintUIMServiceImpl", e,
 					"Error instaniating service, client threw an exception");
 		} catch (MintRemoteException e) {
 			registryref
 					.getLoggingEngine()
 					.logFailed(Level.SEVERE, "MintUIMServiceImpl", e,
 							"Error instaniating service, remote Mint Service threw an exception");
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.mintclient.service.MintUIMService#createMintOrganization
 	 * (eu.europeana.uim.store.Provider)
 	 */
 	@Override
 	public void createMintOrganization(Provider provider)
 			throws MintOSGIClientException, MintRemoteException,
 			StorageEngineException {
 
 		String mintID = provider.getValue(ControlledVocabularyProxy.MINTID);
 
 		if (mintID == null) {
 			performOrgCreation(provider);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.mintclient.service.MintUIMService#createMintOrganization
 	 * (eu.europeana.uim.store.Provider)
 	 */
 	@Override
 	public void createMintOrganization(Provider provider, String enforce)
 			throws MintOSGIClientException, MintRemoteException,
 			StorageEngineException {
 
 		String mintID = provider.getValue(ControlledVocabularyProxy.MINTID);
 
 		if (mintID == null) {
 			performOrgCreation(provider);
 		}
 
 		else if (mintID != null) {
 
 			if ("true".equals(enforce)) {
 
 				OrganizationExistsCommand org = new OrganizationExistsCommand();
 				org.setOrganizationId(mintID);
 
 				OrganizationExistsResponse orgresp = synchronousClient
 						.organizationExists(org);
 
 				if (!orgresp.isExists()) {
 					performOrgCreation(provider);
 				}
 
 			}
 		}
 
 	}
 
 	/**
 	 * @param provider
 	 * @throws MintOSGIClientException
 	 * @throws MintRemoteException
 	 * @throws StorageEngineException
 	 */
 	private void performOrgCreation(Provider provider)
 			throws MintOSGIClientException, MintRemoteException,
 			StorageEngineException {
 
 		boolean hasLock = providerlock.contains(provider.getMnemonic());
 
 		if (!hasLock) {
 			providerlock.add(provider.getMnemonic());
 			CreateOrganizationCommand command = new CreateOrganizationCommand();
 			command.setCountry(provider
 					.getValue(ControlledVocabularyProxy.PROVIDERCOUNTRY));
 			command.setEnglishName(provider.getName());
 			command.setName(provider.getName());
 			command.setType(provider
 					.getValue(ControlledVocabularyProxy.PROVIDERTYPE));
 			String userID = provider
 					.getValue(ControlledVocabularyProxy.PROVIDERMINTUSERID);
 			command.setUserId(userID);
 			CreateOrganizationResponse resp = synchronousClient
 					.createOrganization(command);
 			CreateOrganizationAction action = new CreateOrganizationAction();
 			action.setCreateOrganizationResponse(resp);
 			resphandler.handleResponse(action, provider);
 		} else {
 			try {
 				Thread.sleep(50000);
 				performOrgCreation(provider);
 
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.mintclient.service.MintUIMService#createMintAuthorizedUser
 	 * (eu.europeana.uim.store.Provider)
 	 */
 	@Override
 	public void createMintAuthorizedUser(Provider provider)
 			throws MintOSGIClientException, MintRemoteException,
 			StorageEngineException {
 		CreateUserCommand command = new CreateUserCommand();
 
 		command.setEmail(provider
 				.getValue(ControlledVocabularyProxy.PROVIDERTYPE));
 		command.setFirstName(provider
 				.getValue(ControlledVocabularyProxy.PROVIDERMINTUSERFIRSTNAME));
 		command.setLastName(provider
 				.getValue(ControlledVocabularyProxy.PROVIDERMINTUSERLASTNAME));
 		command.setUserName(provider
 				.getValue(ControlledVocabularyProxy.PROVIDERMINTUSERID));
 		command.setPassword(provider
 				.getValue(ControlledVocabularyProxy.PROVIDERMINTUSERPASSWORD));
 		command.setPhone(provider
 				.getValue(ControlledVocabularyProxy.PROVIDERMINTPHONE));
 		command.setOrganization(provider
 				.getValue(ControlledVocabularyProxy.MINTID));
 		CreateUserResponse response = synchronousClient.createUser(command);
 
 		CreateUserAction action = new CreateUserAction();
 		action.setCreateUserResponse(response);
 		resphandler.handleResponse(action, provider);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.mintclient.service.MintUIMService#createMappingSession
 	 * (eu.europeana.uim.store.Collection)
 	 */
 	@Override
 	public void createMappingSession(Collection collection)
 			throws MintOSGIClientException, MintRemoteException,
 			StorageEngineException {
 		CreateImportCommand command = new CreateImportCommand();
 
 		Provider provider = collection.getProvider();
 		// command.setUserId(provider
 		// .getValue(ControlledVocabularyProxy.PROVIDERMINTUSERID));
 
 		command.setUserId("1000");
		command.setOrganizationId(provider
 				.getValue(ControlledVocabularyProxy.MINTID));
 
 		String repoxID = collection.getValue(ControlledVocabularyProxy.REPOXID);
 
 		if (repoxID == null) {
 			throw new MintOSGIClientException(
 					"Cannot create mapping session because"
 							+ "there is not a repox datasource registered in UIM which can"
 							+ "provide the data for the mapping session");
 		}
 
 		command.setRepoxTableName(repoxID);
 		asynchronousClient.createImports(command, repoxID);
 	}
 
 	/**
 	 * Public static nested class implementing a Listener for incoming messages
 	 * 
 	 * @author Georgios Markakis <gwarkx@hotmail.com>
 	 * @since 6 Mar 2012
 	 */
 	public static class UIMConsumerListener extends DefaultConsumer {
 
 		/**
 		 * Default Constructor
 		 * 
 		 * @param channel
 		 */
 		public UIMConsumerListener(Channel channel) {
 			super(channel);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * com.rabbitmq.client.DefaultConsumer#handleDelivery(java.lang.String,
 		 * com.rabbitmq.client.Envelope,
 		 * com.rabbitmq.client.AMQP.BasicProperties, byte[])
 		 */
 		@Override
 		public void handleDelivery(String consumerTag, Envelope envelope,
 				AMQP.BasicProperties properties, byte[] body)
 				throws IOException {
 			String routingKey = envelope.getRoutingKey();
 			String contentType = properties.getContentType();
 
 			Map<String, Object> maprops = properties.getHeaders();
 			String correlationid = properties.getCorrelationId();
 			boolean hasError = (Boolean) maprops.get(HEADERERRORMESSAGE);
 
 			try {
 				if (hasError) {
 
 					ErrorResponse err = MintClientUtils.marshallobject(
 							new String(body), ErrorResponse.class);
 
 					// Log Error
 					StringBuilder sb = new StringBuilder();
 					sb.append("Operation Name:");
 					sb.append(err.getCommand());
 					sb.append("Error Description:");
 					sb.append(err.getErrorMessage());
 					sb.append("Correlation ID:");
 					sb.append(correlationid);
 
 					logger.logFailed(Level.SEVERE,
 							"RemoteServer has thrown an exception:",
 							new MintRemoteException(sb.toString()));
 
 				} else {
 
 					resphandler.handleResponse(new String(body), correlationid);
 				}
 
 			} catch (MintOSGIClientException e) {
 				logger.logFailed(Level.SEVERE,
 						"Incoming message caused an exception to the client", e);
 			} catch (StorageEngineException e) {
 				logger.logFailed(Level.SEVERE,
 						"Incoming message has thrown a storage exception", e);
 			} catch (Exception e) {
 				logger.logFailed(Level.SEVERE,
 						"Incoming message has thrown an unknown exception", e);
 			}
 
 		}
 
 	}
 }
