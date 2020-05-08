 package ch.ethz.mlmq.server.processing;
 
 import java.sql.SQLException;
 import java.util.List;
 import java.util.logging.Logger;
 
 import ch.ethz.mlmq.dto.ClientDto;
 import ch.ethz.mlmq.dto.MessageDto;
 import ch.ethz.mlmq.dto.QueueDto;
 import ch.ethz.mlmq.exception.MlmqException;
 import ch.ethz.mlmq.net.request.CreateQueueRequest;
 import ch.ethz.mlmq.net.request.DeleteQueueRequest;
 import ch.ethz.mlmq.net.request.DequeueMessageRequest;
 import ch.ethz.mlmq.net.request.LookupClientRequest;
 import ch.ethz.mlmq.net.request.LookupQueueRequest;
 import ch.ethz.mlmq.net.request.PeekMessageRequest;
 import ch.ethz.mlmq.net.request.QueuesWithPendingMessagesRequest;
 import ch.ethz.mlmq.net.request.RegistrationRequest;
 import ch.ethz.mlmq.net.request.Request;
 import ch.ethz.mlmq.net.request.SendClientMessageRequest;
 import ch.ethz.mlmq.net.request.SendMessageRequest;
 import ch.ethz.mlmq.net.response.ClientResponse;
 import ch.ethz.mlmq.net.response.DeleteQueueResponse;
 import ch.ethz.mlmq.net.response.MessageResponse;
 import ch.ethz.mlmq.net.response.QueueResponse;
 import ch.ethz.mlmq.net.response.QueuesWithPendingMessagesResponse;
 import ch.ethz.mlmq.net.response.RegistrationResponse;
 import ch.ethz.mlmq.net.response.Response;
 import ch.ethz.mlmq.net.response.SendClientMessageResponse;
 import ch.ethz.mlmq.net.response.SendMessageResponse;
 import ch.ethz.mlmq.server.ClientApplicationContext;
 import ch.ethz.mlmq.server.db.DbConnection;
 import ch.ethz.mlmq.server.db.DbConnectionPool;
 import ch.ethz.mlmq.server.db.dao.ClientDao;
 import ch.ethz.mlmq.server.db.dao.MessageDao;
 import ch.ethz.mlmq.server.db.dao.QueueDao;
 
 public class RequestProcessor {
 
 	private final Logger logger = Logger.getLogger(RequestProcessor.class.getSimpleName());
 
 	public Response process(ClientApplicationContext clientApplicationContext, Request request, DbConnectionPool pool) throws MlmqException {
 		if (!clientApplicationContext.isRegistered() && !(request instanceof RegistrationRequest)) {
 			throw new MlmqException("Client not yet registere");
 		}
 
 		logger.fine("Process Request " + request);
 
 		if (request instanceof CreateQueueRequest) {
 			return processCreateQueueRequest((CreateQueueRequest) request, pool);
 
 		} else if (request instanceof LookupQueueRequest) {
 			return processLookupQueueRequest((LookupQueueRequest) request, clientApplicationContext, pool);
 
 		} else if (request instanceof LookupClientRequest) {
 			return processLookupClientRequest((LookupClientRequest) request, clientApplicationContext, pool);
 
 		} else if (request instanceof QueuesWithPendingMessagesRequest) {
 			return processQueuesWithPendingMessagesRequest((QueuesWithPendingMessagesRequest) request, clientApplicationContext, pool);
 
 		} else if (request instanceof RegistrationRequest) {
 			return processRegistrationRequest((RegistrationRequest) request, clientApplicationContext, pool);
 
 		} else if (request instanceof DeleteQueueRequest) {
 			return processDeleteQueueRequest((DeleteQueueRequest) request, pool);
 
 		} else if (request instanceof DequeueMessageRequest) {
 			return processDequeueMessageRequest((DequeueMessageRequest) request, clientApplicationContext, pool);
 
 		} else if (request instanceof PeekMessageRequest) {
 			return processPeekMessageRequest((PeekMessageRequest) request, clientApplicationContext, pool);
 
 		} else if (request instanceof SendMessageRequest) {
 			return processSendMessageRequest((SendMessageRequest) request, clientApplicationContext, pool);
 
 		} else if (request instanceof SendClientMessageRequest) {
 			return processSendClientMessageRequest((SendClientMessageRequest) request, clientApplicationContext, pool);
 
 		} else {
 			throw new MlmqException("Unexpected Request to process " + request.getClass().getSimpleName() + " - " + request);
 		}
 	}
 
 	private Response processSendClientMessageRequest(SendClientMessageRequest request, ClientApplicationContext clientApplicationContext, DbConnectionPool pool)
 			throws MlmqException {
 
 		DbConnection connection = null;
 		try {
 			connection = pool.getConnection();
 
 			QueueDao queueDao = connection.getQueueDao();
 
 			QueueDto receivingClientQueue = queueDao.getQueueByClientId(request.getClientId());
 			if (receivingClientQueue == null) {
 				throw new MlmqException("Could not find Queue for ClientId " + request.getClientId());
 			}
 			long receivingClientQueueId = receivingClientQueue.getId();
 
 			MessageDao messageDao = connection.getMessageDao();
 
 			Long context = request.getConversationContext();
 			if (request.isConversation() && context == null) {
 				context = messageDao.generateNewConversationContext();
 			}
 			messageDao.insertMessage(receivingClientQueueId, clientApplicationContext.getClient().getId(), request.getContent(), request.getPrio(), context);
 			SendClientMessageResponse response = new SendClientMessageResponse();
 			response.setConversationContext(context);
 			return response;
 
 		} catch (SQLException ex) {
 			connection.close();
 			throw new MlmqException(ex);
 		} finally {
 			if (connection != null) {
 				pool.returnConnection(connection);
 			}
 		}
 
 	}
 
 	private Response processQueuesWithPendingMessagesRequest(QueuesWithPendingMessagesRequest request, ClientApplicationContext clientApplicationContext,
 			DbConnectionPool pool) throws MlmqException {
 
 		DbConnection connection = null;
 		try {
 			connection = pool.getConnection();
 			MessageDao msgDao = connection.getMessageDao();
 
 			List<QueueDto> queues = msgDao.getPublicQueuesContainingMessages(request.getMaxNumQueues());
 			int numMessagesInMyQueue = msgDao.getNumberOfMessages(clientApplicationContext.getClientQueue().getId());
 
 			// create response message
 			QueuesWithPendingMessagesResponse response = new QueuesWithPendingMessagesResponse();
 			response.setQueues(queues);
 			response.setNumMessagesInMyQueue(numMessagesInMyQueue);
 
 			return response;
 		} catch (SQLException ex) {
 			connection.close();
 			throw new MlmqException(ex);
 		} finally {
 			if (connection != null) {
 				pool.returnConnection(connection);
 			}
 		}
 
 	}
 
 	private Response processPeekMessageRequest(PeekMessageRequest request, ClientApplicationContext clientApplicationContext, DbConnectionPool pool)
 			throws MlmqException {
 
 		DbConnection connection = null;
 		try {
 			connection = pool.getConnection();
 			MessageDao messageDao = connection.getMessageDao();
 
 			MessageDto message = messageDao.peekMessage(request.getMessageQueryInfo());
 			return new MessageResponse(message);
 		} catch (SQLException ex) {
 			connection.close();
 			throw new MlmqException(ex);
 		} finally {
 			if (connection != null) {
 				pool.returnConnection(connection);
 			}
 		}
 	}
 
 	private Response processDequeueMessageRequest(DequeueMessageRequest request, ClientApplicationContext clientApplicationContext, DbConnectionPool pool)
 			throws MlmqException {
 
 		DbConnection connection = null;
 		try {
 			connection = pool.getConnection();
 			MessageDao messageDao = connection.getMessageDao();
 
 			MessageDto message = messageDao.dequeueMessage(request.getMessageQueryInfo());
 			return new MessageResponse(message);
 		} catch (SQLException ex) {
 			connection.close();
 			throw new MlmqException(ex);
 		} finally {
 			if (connection != null) {
 				pool.returnConnection(connection);
 			}
 		}
 	}
 
 	private Response processSendMessageRequest(SendMessageRequest request, ClientApplicationContext clientApplicationContext, DbConnectionPool pool)
 			throws MlmqException {
 		DbConnection connection = null;
 		try {
 			connection = pool.getConnection();
 
 			MessageDao messageDao = connection.getMessageDao();
 			messageDao.insertMessage(request, clientApplicationContext);
 			SendMessageResponse response = new SendMessageResponse();
 			return response;
 		} catch (SQLException ex) {
 			connection.close();
 			throw new MlmqException(ex);
 		} finally {
 			if (connection != null) {
 				pool.returnConnection(connection);
 			}
 		}
 	}
 
 	private Response processDeleteQueueRequest(DeleteQueueRequest request, DbConnectionPool pool) throws MlmqException {
 
 		DbConnection connection = null;
 		try {
 			connection = pool.getConnection();
 
 			QueueDao queueDao = connection.getQueueDao();
 
 			long queueIdToDelete = request.getQueueId();
 			queueDao.deleteQueue(queueIdToDelete);
 
 			return new DeleteQueueResponse();
 
 		} catch (SQLException ex) {
 			connection.close();
 			throw new MlmqException(ex);
 		} finally {
 			if (connection != null) {
 				pool.returnConnection(connection);
 			}
 		}
 
 	}
 
 	private QueueResponse processLookupQueueRequest(LookupQueueRequest request, ClientApplicationContext clientApplicationContext, DbConnectionPool pool)
 			throws MlmqException {
 		DbConnection connection = null;
 		try {
 			connection = pool.getConnection();
 
 			QueueDao queueDao = connection.getQueueDao();
 
 			Long clientId = request.getClientId();
 			QueueDto queue = null;
 
 			if (clientId != null) {
 				queue = queueDao.getQueueByClientId(clientId);
 			} else {
 				queue = queueDao.getQueueByName(request.getQueueName());
 			}
 
 			QueueResponse response = new QueueResponse(queue);
 			return response;
 
 		} catch (SQLException ex) {
 			connection.close();
 			throw new MlmqException("Error looking up Queue " + request.getQueueName(), ex);
 		} finally {
 			if (connection != null) {
 				pool.returnConnection(connection);
 			}
 		}
 	}
 
 	private ClientResponse processLookupClientRequest(LookupClientRequest request, ClientApplicationContext clientApplicationContext, DbConnectionPool pool)
 			throws MlmqException {
 		DbConnection connection = null;
 		try {
 			connection = pool.getConnection();
 
 			ClientDao clientDao = connection.getClientDao();
 
 			Integer clientId = clientDao.getClientId(request.getClientName());
 
 			ClientDto client = null;
 			if (clientId != null) {
 				client = new ClientDto(clientId, request.getClientName());
 			}
 
 			ClientResponse response = new ClientResponse(client);
 			return response;
 		} catch (SQLException ex) {
 			connection.close();
 			throw new MlmqException("Error looking up Client " + request.getClientName(), ex);
 		} finally {
 			if (connection != null) {
 				pool.returnConnection(connection);
 			}
 		}
 	}
 
 	private Response processCreateQueueRequest(CreateQueueRequest request, DbConnectionPool pool) throws MlmqException {
 		DbConnection connection = null;
 		try {
 			connection = pool.getConnection();
 
 			QueueDao queueDao = connection.getQueueDao();
 
 			// try to lookup queue

			// TODO Concurrency issue here
 			QueueDto queue = queueDao.getQueueByName(request.getQueueName());
 			if (queue == null) {
 				// Queue not found - actually create queue
 				queue = queueDao.createQueue(request.getQueueName());
 			}
 
 			QueueResponse response = new QueueResponse(queue);
 			return response;
 
 		} catch (SQLException ex) {
 			connection.close();
 			throw new MlmqException("Error creating Queue " + request.getQueueName(), ex);
 		} finally {
 			if (connection != null) {
 				pool.returnConnection(connection);
 			}
 		}
 	}
 
 	private Response processRegistrationRequest(RegistrationRequest request, ClientApplicationContext clientApplicationContext, DbConnectionPool pool)
 			throws MlmqException {
 
 		DbConnection connection = null;
 		try {
 			connection = pool.getConnection();
 
 			// insert new Client
 			ClientDao clientDao = connection.getClientDao();
 			QueueDao queueDao = connection.getQueueDao();
 
 			String name = request.getClientName();
 
 			Integer clientId = clientDao.getClientId(name);
 			QueueDto clientQueue;
 			if (clientId == null) {
 				int newClientId = clientDao.insertNewClient(name);
 				clientId = newClientId;
 
 				// insert new ClientQueue
 				clientQueue = queueDao.createClientQueue(newClientId, name);
 			} else {
 
 				QueueDto queue = queueDao.getQueueByClientId(clientId);
 				if (queue == null) {
 					throw new MlmqException("Could not find Queue for ClientId " + clientId);
 				}
 				long queueId = queue.getId();
 				clientQueue = new QueueDto(queueId);
 
 				logger.info("Welcome back " + name + " ClientId [" + clientId + "] ClientQueue [" + queueId + "]");
 			}
 
 			ClientDto clientDto = new ClientDto(clientId);
 			clientDto.setName(request.getClientName());
 
 			clientApplicationContext.setClient(clientDto);
 			clientApplicationContext.setClientQueue(clientQueue);
 
 			RegistrationResponse response = new RegistrationResponse(clientDto);
 
 			return response;
 
 		} catch (SQLException ex) {
 			connection.close();
 			throw new MlmqException(ex);
 		} finally {
 			if (connection != null) {
 				pool.returnConnection(connection);
 			}
 		}
 	}
 }
