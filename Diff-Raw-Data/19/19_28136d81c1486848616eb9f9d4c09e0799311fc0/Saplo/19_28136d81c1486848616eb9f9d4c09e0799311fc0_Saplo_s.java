 package com.voltvoodoo.saplo4j;
 
 import static com.voltvoodoo.saplo4j.utils.JsonUtils.jsonParams;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.voltvoodoo.saplo4j.async.SaploCallback;
 import com.voltvoodoo.saplo4j.async.impl.AddDocumentCallback;
 import com.voltvoodoo.saplo4j.async.impl.CreateCorpusCallback;
 import com.voltvoodoo.saplo4j.async.impl.DeleteCorpusCallback;
 import com.voltvoodoo.saplo4j.async.impl.DeleteMatchCallback;
 import com.voltvoodoo.saplo4j.async.impl.GetCorpusIdsCallback;
 import com.voltvoodoo.saplo4j.async.impl.GetCorpusInfoCallback;
 import com.voltvoodoo.saplo4j.async.impl.GetDocumentCallback;
 import com.voltvoodoo.saplo4j.async.impl.GetEntityTagsCallback;
 import com.voltvoodoo.saplo4j.async.impl.GetMatchCallback;
 import com.voltvoodoo.saplo4j.async.impl.GetSimilarDocumentsCallback;
 import com.voltvoodoo.saplo4j.async.impl.UpdateDocumentCallback;
 import com.voltvoodoo.saplo4j.exception.SaploConnectionException;
 import com.voltvoodoo.saplo4j.http.SaploConnection;
 import com.voltvoodoo.saplo4j.http.SaploRequest;
 import com.voltvoodoo.saplo4j.model.Language;
 import com.voltvoodoo.saplo4j.model.SaploCorpus;
 import com.voltvoodoo.saplo4j.model.SaploDocument;
 import com.voltvoodoo.saplo4j.model.SaploSimilarity;
 import com.voltvoodoo.saplo4j.model.SaploTag;
 
 /**
  * Java API for Saplo semantic analysis service.
  * 
  * Almost all of the methods within the API come in two versions, a synchronous
  * one and an asynchronous one. The asynchrounous API is, of course, a magnitude
  * faster when it comes to handling many calls at the same time and is most
  * often to be preferred.
  * 
  * The synchronous API is convenient when you handle few requests and don't want
  * to deal with concurrency problems and creating callbacks.
  * 
  * @author Jacob Hansson <jacob@voltvoodoo.com>, progre55, Fredrik Hörte
  *         <fredrik@saplo.com>,
  * 
  */
 public class Saplo {
 
 	private static int MAX_WAIT_SECONDS = 180;
 
 	public static String SAPLO_URL = "http://api.saplo.com/";
 	public static String API_RESOURCE = "/rpc/json";
 
 	private SaploConnection saploConnection;
 
 	//
 	// CONSTRUCTORS
 	//
 
 	public Saplo(String apiKey, String secretKey) {
 		saploConnection = new SaploConnection(apiKey, secretKey, SAPLO_URL,
 				API_RESOURCE);
 
 		saploConnection.open();
 	}
 
 	//
 	// CONNECTION MANAGEMENT
 	//
 
 	public void close() {
 		saploConnection.close();
 	}
 
 	//
 	// CORPUS MANAGEMENT API
 	// Synchronous
 
 	public SaploCorpus.Id createCorpus(String name, String description,
 			Language lang) {
 
 		CreateCorpusCallback cb = new CreateCorpusCallback();
 		call("corpus.createCorpus", jsonParams(name, description, lang), cb);
 
 		cb.awaitResponse(MAX_WAIT_SECONDS * 1000);
 
 		if (cb.corpusId != null) {
 			return cb.corpusId;
 		} else {
 			throw cb.exception;
 		}
 	}
 
 	public boolean deleteCorpus(SaploCorpus.Id id) {
 
 		DeleteCorpusCallback cb = new DeleteCorpusCallback();
 		call("corpus.deleteCorpus", jsonParams(id), cb);
 
 		cb.awaitResponse(MAX_WAIT_SECONDS * 1000);
 
 		if (cb.exception != null) {
 			throw cb.exception;
 		} else {
 			return cb.result;
 		}
 	}
 
 	public List<SaploCorpus> getAllCorpuses() {
 
 		GetCorpusIdsCallback idCb = new GetCorpusIdsCallback();
 		call("corpus.getPermissions", jsonParams(), idCb);
 
 		idCb.awaitResponse(MAX_WAIT_SECONDS * 1000);
 
 		if (idCb.getException() != null) {
 			throw idCb.getException();
 		}
 		
 		ArrayList<SaploCorpus> corpuses = new ArrayList<SaploCorpus>();
 		for(SaploCorpus.Id id : idCb.getCorpusIds()) {
 			corpuses.add(getCorpus(id));
 		}
 
 		return corpuses;
 	}
 	
 	public SaploCorpus getCorpus(SaploCorpus.Id id) {
 		GetCorpusInfoCallback cb = new GetCorpusInfoCallback(id);
 		call("corpus.getInfo", jsonParams(id), cb);
 
 		cb.awaitResponse(MAX_WAIT_SECONDS * 1000);
 
 		if (cb.getException() != null) {
 			throw cb.getException();
 		} else {
 			return cb.getCorpus();
 		}
 	}
 
 	//
 	// DOCUMENT MANAGEMENT API
 	// Synchronous
 
 	public SaploDocument.Id addDocument(SaploCorpus.Id corpusId,
 			String headline, String body, Language lang) {
 
 		AddDocumentCallback cb = new AddDocumentCallback();
 		addDocument(corpusId, headline, body, lang, cb);
 		cb.awaitResponse(MAX_WAIT_SECONDS * 1000);
 
 		if (cb.documentId != null) {
 			return cb.documentId;
 		} else {
 			throw cb.exception;
 		}
 	}
 
 	public boolean updateDocument(SaploCorpus.Id corpusId, SaploDocument.Id id,
 			String headline, String body, Language lang) {
 		UpdateDocumentCallback cb = new UpdateDocumentCallback();
 		updateDocument(corpusId, id, headline, body, lang, cb);
 		cb.awaitResponse(10000);
 
 		if (cb.exception == null) {
 			return true;
 		} else {
 			throw cb.exception;
 		}
 	}
 
 	public SaploDocument getDocument(SaploCorpus.Id corpusId,
 			SaploDocument.Id id) {
 		GetDocumentCallback cb = new GetDocumentCallback();
 		getDocument(corpusId, id, cb);
 		cb.awaitResponse(10000);
 
 		if (cb.document != null) {
 			return cb.document;
 		} else {
 			throw cb.exception;
 		}
 	}
 
 	//
 	// DOCUMENT MANAGEMENT API
 	// Async
 
 	public void addDocument(SaploCorpus.Id corpusId, String headline,
 			String body, Language lang, SaploCallback<SaploDocument.Id> callback) {
 		addDocument(corpusId, headline, body, lang, new AddDocumentCallback(
 				callback));
 	}
 
 	public void updateDocument(SaploCorpus.Id corpusId, SaploDocument.Id id,
 			String headline, String body, Language lang,
 			SaploCallback<Boolean> callback) {
 		updateDocument(corpusId, id, headline, body, lang,
 				new UpdateDocumentCallback(callback));
 	}
 
 	public void getDocument(SaploCorpus.Id corpusId, SaploDocument.Id id,
 			SaploCallback<SaploDocument> callback) {
 		getDocument(corpusId, id, new GetDocumentCallback(callback));
 	}
 
 	//
 	// DOCUMENT MANAGEMENT API
 	// Underlying implementation
 
 	private void addDocument(SaploCorpus.Id corpusId, String headline,
 			String body, Language lang, AddDocumentCallback callback) {
 		call("corpus.addArticle",
 				jsonParams(corpusId, headline, "", body, "", "", "", lang),
 				callback);
 	}
 
 	private void updateDocument(SaploCorpus.Id corpusId, SaploDocument.Id id,
 			String headline, String body, Language lang,
 			UpdateDocumentCallback callback) {
 		call("corpus.updateArticle",
 				jsonParams(corpusId, id, headline, "", body, "", "", "", lang),
 				callback);
 	}
 
 	private void getDocument(SaploCorpus.Id corpusId, SaploDocument.Id id,
 			GetDocumentCallback callback) {
 		call("corpus.getArticle", jsonParams(corpusId, id), callback);
 	}
 
 	//
 	// MATCH API
 	// Synchronous
 
 	public ArrayList<SaploSimilarity> getSimilarDocuments(
 			SaploCorpus.Id corpusId, SaploDocument.Id id) {
 
 		GetSimilarDocumentsCallback cb = new GetSimilarDocumentsCallback(id,
 				corpusId);
 
 		getSimilarDocuments(corpusId, id, cb);
 
 		while (cb.exception == null && cb.similarDocuments == null) {
 			cb.awaitResponse(MAX_WAIT_SECONDS * 1000);
 		}
 
 		if (cb.similarDocuments != null) {
 			return cb.similarDocuments;
 		} else {
 			throw cb.exception;
 		}
 	}
 
 	public SaploSimilarity getMatch(SaploCorpus.Id corpusId,
 			SaploSimilarity.Id id, SaploDocument.Id documentId) {
 
 		GetMatchCallback cb = new GetMatchCallback(corpusId, id, documentId);
 		getMatch(corpusId, id, documentId, cb);
 
 		while (cb.exception == null && cb.similarity == null) {
 			cb.awaitResponse(MAX_WAIT_SECONDS * 1000);
 		}
 
 		if (cb.similarity != null) {
 			return cb.similarity;
 		} else {
 			throw cb.exception;
 		}
 	}
 
 	public boolean deleteMatch(SaploCorpus.Id corpusId, SaploSimilarity.Id id,
 			SaploDocument.Id documentId) {
 
 		DeleteMatchCallback cb = new DeleteMatchCallback();
 
 		deleteMatch(corpusId, id, documentId, cb);
 
 		while (cb.exception == null && cb.result == null) {
 			cb.awaitResponse(MAX_WAIT_SECONDS * 1000);
 		}
 
 		if (cb.result != null) {
 			return cb.result;
 		} else {
 			throw cb.exception;
 		}
 
 	}
 
 	//
 	// MATCH API
 	// Async
 
 	public void getSimilarDocuments(SaploCorpus.Id corpusId,
 			SaploDocument.Id id,
 			SaploCallback<ArrayList<SaploSimilarity>> callback) {
 
 		getSimilarDocuments(corpusId, id, new GetSimilarDocumentsCallback(id,
 				corpusId, callback));
 	}
 
 	public void getMatch(SaploCorpus.Id corpusId, SaploSimilarity.Id id,
 			SaploDocument.Id documentId, SaploCallback<SaploSimilarity> callback) {
 
 		getMatch(corpusId, id, documentId, new GetMatchCallback(corpusId, id,
 				documentId, callback));
 	}
 
 	public void deleteMatch(SaploCorpus.Id corpusId, SaploSimilarity.Id id,
 			SaploDocument.Id documentId, SaploCallback<Boolean> callback) {
 
 		deleteMatch(corpusId, id, documentId, new DeleteMatchCallback(callback));
 	}
 
 	//
 	// MATCH API
 	// Underlying implementation
 
 	private void getSimilarDocuments(SaploCorpus.Id corpusId,
 			SaploDocument.Id id, GetSimilarDocumentsCallback callback) {
 
 		call("match.getSimilarArticles",
				jsonParams(corpusId, id, MAX_WAIT_SECONDS, 50, 0.1),
 				callback);
 	}
 
 	private void getMatch(SaploCorpus.Id corpusId, SaploSimilarity.Id id,
 			SaploDocument.Id documentId, GetMatchCallback callback) {
 
 		call("match.getMatch", jsonParams(corpusId, documentId, id), callback);
 	}
 
 	private void deleteMatch(SaploCorpus.Id corpusId, SaploSimilarity.Id id,
 			SaploDocument.Id documentId, DeleteMatchCallback callback) {
 
 		call("match.getMatch", jsonParams(corpusId, documentId, id), callback);
 	}
 
 	//
 	// TAG API
 	// Synchronous
 
 	public List<SaploTag> getEntityTags(SaploCorpus.Id corpusId,
 			SaploDocument.Id documentId) {
 
 		GetEntityTagsCallback cb = new GetEntityTagsCallback(corpusId,
 				documentId);
 
 		getEntityTags(corpusId, documentId, cb);
 
 		while (cb.exception == null && cb.result == null) {
 			cb.awaitResponse(MAX_WAIT_SECONDS * 1000);
 		}
 
 		if (cb.result != null) {
 			return cb.result;
 		} else {
 			throw cb.exception;
 		}
 
 	}
 
 	//
 	// TAG API
 	// Async
 
 	public void getEntityTags(SaploCorpus.Id corpusId,
 			SaploDocument.Id documentId, SaploCallback<List<SaploTag>> callback) {
 
 		getEntityTags(corpusId, documentId, new GetEntityTagsCallback(corpusId,
 				documentId, callback));
 
 	}
 
 	//
 	// TAG API
 	// Underlying implementation
 
 	private void getEntityTags(SaploCorpus.Id corpusId,
 			SaploDocument.Id documentId, GetEntityTagsCallback callback) {
 		call("tags.getEntityTags",
 				jsonParams(corpusId, documentId, MAX_WAIT_SECONDS), callback);
 	}
 
 	//
 	// OTHER
 	//
 
 	/**
 	 * Low-level interface for communicating directly with Saplo API. This can
 	 * be used in case you want to pass optional parameters that are not
 	 * included in the high-level methods. It can also be used to call methods
 	 * that are not yet implemented.
 	 * 
 	 * @param method
 	 *            is the Saplo method name, for example "corpus.addArticle"
 	 * @param params
 	 *            can be almost anything. This gets converted to JSON. Use the
 	 *            static method params ftw.
 	 * @param callback
 	 *            is a SaploCallback that will get the response JSON object, or
 	 *            any exceptions.
 	 */
 	public void call(String method, Object params,
 			SaploCallback<Object> callback) throws SaploConnectionException {
 
 		call(new SaploRequest(method, params, callback, saploConnection));
 
 	}
 
 	/**
 	 * Perform a low-level call to Saplo using a SaploRequest object.
 	 * 
 	 * This is convenient if you want to redo a request that has failed. Use the
 	 * getRequest() method that all SaploException objects have, and simply call
 	 * this method with the request. All your callbacks etc will be used as they
 	 * were in the first request.
 	 * 
 	 * @param request
 	 * @throws SaploConnectionException
 	 */
 	public void call(SaploRequest request) throws SaploConnectionException {
 		saploConnection.call(request);
 	}
 
 	//
 	// SETTINGS
 	//
 
 	/**
 	 * This is used to "hide" API request limit reached errors. In essesence,
 	 * set this to true and you will not get those error. When a request limit
 	 * error is encountered, the request will be retried after an hour.
 	 * 
 	 * Turning this on will, if you are doing large amounts of requests, lead to
 	 * some requests taking up to an hour to complete.
 	 * 
 	 * Default value for this is false.
 	 */
 	public Saplo setHideRequestLimit(boolean hideRequestLimit) {
 		saploConnection.setHideRequestLimit(hideRequestLimit);
 		return this;
 	}
 
 	public boolean isHidingRequestLimit() {
 		return saploConnection.isHidingRequestLimit();
 	}
 
 }
