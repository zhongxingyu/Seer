 /**
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.gmail;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.oauth.OAuthInvalidateAccessTokenOn;
 import org.mule.api.annotations.oauth.OAuthProtected;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 import org.mule.module.gmail.model.MailMessage;
 import org.mule.module.gmail.search.FlagCriteria;
 import org.mule.module.gmail.search.GmailFolder;
 import org.mule.module.gmail.search.SearchCriteria;
 import org.mule.modules.google.AbstractGoogleOAuthConnector;
 import org.mule.modules.google.api.datetime.DateTimeUtils;
 import org.mule.modules.google.oauth.invalidation.OAuthTokenExpiredException;
 
 import com.google.code.com.sun.mail.imap.IMAPFolder;
 import com.google.code.com.sun.mail.imap.IMAPMessage;
 import com.google.code.javax.mail.AuthenticationFailedException;
 import com.google.code.javax.mail.FetchProfile;
 import com.google.code.javax.mail.Flags.Flag;
 import com.google.code.javax.mail.Folder;
 import com.google.code.javax.mail.Message;
 import com.google.code.javax.mail.MessagingException;
 import com.google.code.javax.mail.Store;
 import com.google.code.javax.mail.internet.InternetAddress;
 import com.google.code.javax.mail.search.AndTerm;
 import com.google.code.javax.mail.search.BodyTerm;
 import com.google.code.javax.mail.search.ComparisonTerm;
 import com.google.code.javax.mail.search.FlagTerm;
 import com.google.code.javax.mail.search.FromTerm;
 import com.google.code.javax.mail.search.GmailLabelTerm;
 import com.google.code.javax.mail.search.GmailMessageIDTerm;
 import com.google.code.javax.mail.search.GmailRawSearchTerm;
 import com.google.code.javax.mail.search.GmailThreadIDTerm;
 import com.google.code.javax.mail.search.HeaderTerm;
 import com.google.code.javax.mail.search.MessageIDTerm;
 import com.google.code.javax.mail.search.MessageNumberTerm;
 import com.google.code.javax.mail.search.OrTerm;
 import com.google.code.javax.mail.search.ReceivedDateTerm;
 import com.google.code.javax.mail.search.SearchTerm;
 import com.google.code.javax.mail.search.SentDateTerm;
 import com.google.code.javax.mail.search.SizeTerm;
 import com.google.code.javax.mail.search.SubjectTerm;
 
 /**
  * 
  * @author mariano.gonzalez@mulesoft.com
  *
  */
 public abstract class BaseGmailConnector extends AbstractGoogleOAuthConnector {
 
 	protected abstract Store getStore(String username, String password) throws MessagingException;
 	
 	
 	/**
 	 * Search and returns messages on a folder of the given username
 	 * using a search criteria provided by the user via the searchTerm parameter.
 	 * 
 	 * Returns messages in the user mailbox applying optional search criterias.
 	 * The messages are returned in the form of  {@link org.mule.module.gmail.model.MailMessage} instances
 	 * which allows the connector's user to be decoupled from the IMAP protocol internals but also allows for the
 	 * mailbox connection to be closed automatically.
 	 * 
 	 * {@sample.xml ../../../doc/Gmail-connector.xml.sample gmail:advanced-search}
 	 * 
 	 * @param username the account's username
 	 * @param password only needed is you're using basic authentication. If you're using OAuth2 then this parameter is not used
 	 * @param folder the folder in which to search. If you want all of them then use ALL_MAIL
 	 * @param searchTerm an instance of {@link com.google.code.javax.mail.search.SearchTerm}. If not provided, then no filtering criteria is applied
	 * @param expunge expunges all deleted messages if this flag is true
 	 * @param includeAttachments wether or not to also download the message's attachments. Default value is false bandwidth wise.
 	 * @return a list of {@link org.mule.module.gmail.model.MailMessage} representing the found messages
 	 * @throws MessagingException if an error is found accessing the mailbox
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public List<MailMessage> advancedSearch(
 									String username,
 									@Optional String password,
 									GmailFolder folder,
 									SearchTerm searchTerm,
 									@Optional @Default("false") Boolean expunge,
 									@Optional @Default("false") Boolean includeAttachments) throws MessagingException {
 		
 		List<SearchTerm> terms = new ArrayList<SearchTerm>(1);
 		terms.add(searchTerm);
 		return this.doGetMessages(username, password, folder, terms, expunge, includeAttachments);
 	}
 	
 
 	/**
 	 * Returns messages in the user mailbox applying optional search criterias.
 	 * The messages are returned in the form of  {@link org.mule.module.gmail.model.MailMessage} instances
 	 * which allows the connector's user to be decoupled from the IMAP protocol internals but also allows for the
 	 * mailbox connection to be closed automatically.
 	 * 
 	 * {@sample.xml ../../../doc/Gmail-connector.xml.sample gmail:search}
 	 * 
 	 * @param username the account's username
 	 * @param folder the folder in which to search. If you want all of them then use ALL_MAIL
 	 * @param password only needed is you're using basic authentication. If you're using OAuth2 then this parameter is not used
 	 * @param receivedBefore a date in the specified string format giving a lower bound filter to the received date. If no dateFormat is specified, then RFC3339 is assumed
 	 * @param receivedAfter a date in the specified string format giving an upper bound filter to the received date. If no dateFormat is specified, then RFC3339 is assumed
 	 * @param sentBefore a date in the specified string format giving a lower bound filter to the sent date. If no dateFormat is specified, then RFC3339 is assumed
 	 * @param sentAfter a date in the specified string format giving an upper bound filter to the sent date. If no dateFormat is specified, then RFC3339 is assumed
 	 * @param fromAddresses a list of addresses to be found in the from attribute. This is an OR operation logic. All messages with any one of these addresses will be returned 
 	 * @param toAddresses a list of addresses to be found in the to attribute. This is an OR operation logic. All messages with any one of these addresses will be returned
 	 * @param messageNumber used to look for the message with the specified number
 	 * @param size an instance of {@link org.mule.module.gmail.search.SearchCriteria} to specify restrictions on the message's size
 	 * @param flags a list of {@link org.mule.module.gmail.search.FlagCriteria} to specify flag values that the messages should have
 	 * @param labels a list of Strings which represent the labels that the messages need to have. This is an OR operation logic. All messages with any one of these labels will be returned
 	 * @param rawSearchTerms a list of Strings which represent terms that the message should have. This is an OR operation logic. All messages with any one of these terms will be returned
 	 * @param threadId the id of the thread for which the message should belong 
 	 * @param bodyTerms a list of Strings which represent the body terms that the messages need to have. This is an OR operation logic. All messages with any one of these terms will be returned
 	 * @param headerTerms a list of Strings which represent the header terms that the messages need to have. This is an OR operation logic. All messages with any one of these terms will be returned
 	 * @param messageId used to fetch one given message by id
 	 * @param subjectTerms a list of Strings which represent the subject terms that the messages need to have. This is an OR operation logic. All messages with any one of these terms will be returned
 	 * @param dateFormat the format to be used to parse the date attributes. If no dateFormat is specified, then RFC3339 is assumed
	 * @param expunge expunges all deleted messages if this flag is true
 	 * @param includeAttachments wether or not to also download the message's attachments. Default value is false bandwidth wise.
 	 * @return a list of {@link org.mule.module.gmail.model.MailMessage}
 	 * @throws MessagingException if an error is found accessing the mailbox
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public List<MailMessage> search(
 					String username,
 					GmailFolder folder,
 					@Optional String password,
 					@Optional String receivedBefore,
 					@Optional String receivedAfter,
 					@Optional String sentBefore,
 					@Optional String sentAfter,
 					@Optional List<String> fromAddresses,
 					@Optional List<String> toAddresses,
 					@Optional Integer messageNumber,
 					@Optional SearchCriteria size,
 					@Optional List<FlagCriteria> flags,
 					@Optional List<String> labels,
 					@Optional List<String> rawSearchTerms,
 					@Optional String threadId,
 					@Optional List<String> bodyTerms,
 					@Optional List<String> headerTerms,
 					@Optional String messageId,
 					@Optional List<String> subjectTerms,
 					@Optional @Default(DateTimeUtils.RFC3339) String dateFormat,
 					@Optional @Default("false") Boolean expunge,
 					@Optional @Default("false") Boolean includeAttachments) throws MessagingException {
 		
 		List<SearchTerm> searchTerms = new ArrayList<SearchTerm>();
 		
 		if (!StringUtils.isBlank(receivedBefore)) {
 			searchTerms.add(new ReceivedDateTerm(ComparisonTerm.LE, DateTimeUtils.parseDate(receivedBefore, dateFormat)));
 		}
 		
 		if (!StringUtils.isBlank(receivedAfter)) {
 			searchTerms.add(new ReceivedDateTerm(ComparisonTerm.GE, DateTimeUtils.parseDate(receivedAfter, dateFormat)));
 		}
 		
 		if (!StringUtils.isBlank(sentBefore)) {
 			searchTerms.add(new SentDateTerm(ComparisonTerm.LE, DateTimeUtils.parseDate(sentBefore, dateFormat)));
 		}
 		
 		if (!StringUtils.isBlank(sentAfter)) {
 			searchTerms.add(new SentDateTerm(ComparisonTerm.GE, DateTimeUtils.parseDate(sentAfter, dateFormat)));
 		}
 		
 		if (!CollectionUtils.isEmpty(fromAddresses)) {
 			List<SearchTerm> from = new ArrayList<SearchTerm>(fromAddresses.size());
 			for (String address : fromAddresses) {
 				from.add(new FromTerm(new InternetAddress(address)));
 			}
 			
 			searchTerms.add(new OrTerm(from.toArray(new SearchTerm[from.size()])));
 		}
 		
 		if (!CollectionUtils.isEmpty(toAddresses)) {
 			List<SearchTerm> to = new ArrayList<SearchTerm>(toAddresses.size());
 			for (String address : toAddresses) {
 				to.add(new FromTerm(new InternetAddress(address)));
 			}
 			
 			searchTerms.add(new OrTerm(to.toArray(new SearchTerm[to.size()])));
 		}
 		
 		if (messageNumber != null) {
 			searchTerms.add(new MessageNumberTerm(messageNumber));
 		}
 		
 		if (size != null) {
 			searchTerms.add(new SizeTerm(size.getOperator().getTerm(), Integer.valueOf(size.getValue())));
 		}
 		
 		if (!CollectionUtils.isEmpty(flags)) {
 			List<SearchTerm> f = new ArrayList<SearchTerm>(flags.size());
 			for (FlagCriteria criteria : flags) {
 				f.add(new FlagTerm(criteria.getFlag().getValue(), criteria.isSet()));
 			}
 			searchTerms.add(new OrTerm(f.toArray(new SearchTerm[f.size()])));
 		}
 		
 		this.addStringTerms(searchTerms, labels, GmailLabelTerm.class);
 		this.addStringTerms(searchTerms, rawSearchTerms, GmailRawSearchTerm.class);
 		this.addStringTerms(searchTerms, bodyTerms, BodyTerm.class);
 		this.addStringTerms(searchTerms, headerTerms, HeaderTerm.class);
 		this.addStringTerms(searchTerms, subjectTerms, SubjectTerm.class);
 
 		if (!StringUtils.isBlank(threadId)) {
 			searchTerms.add(new GmailThreadIDTerm(threadId));
 		}
 		
 		if (!StringUtils.isBlank(messageId)) {
 			searchTerms.add(new MessageIDTerm(messageId));
 		}
 		
 		return this.doGetMessages(username, password, folder, searchTerms, expunge, includeAttachments);
 	}
 	
 	/**
 	 * Moves the given message to the trash and deletes it from its current folder 
 	 * 
 	 * {@sample.xml ../../../doc/Gmail-connector.xml.sample gmail:delete}
 	 * 
 	 * @param mailMessage an instance of {@link org.mule.module.gmail.model.MailMessage} representing the message to be deleted. At the very
 	 * 					least, the googleId property must be not null
 	 * @param username the mailbox username
 	 * @param password optional value only required when using basic auth. Ignore this if you're using OAuth
 	 * @throws MessagingException if the mailbox throws an exception
 	 * @throws IllegalArgumentException if the mailbox contains no email with that id
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public void delete(
 					@Optional @Default("#[payload]") MailMessage mailMessage,
 					String username,
 					@Optional String password) throws MessagingException {
 		
 		SearchTerm idTerm = new GmailMessageIDTerm(mailMessage.getGoogleId());
 		Store store = null;
 		Folder folder = null;
 		Folder trash = null;
 		
 		try {
 			store = this.getStore(username, password);
 			folder = this.openFolder(store, GmailFolder.ALL_MAIL, Folder.READ_WRITE);
 			trash = this.openFolder(store, GmailFolder.TRASH, Folder.READ_WRITE);
 			
 			Message[] msg = folder.search(idTerm);
 			
 			if (msg.length == 0) {
 				throw new IllegalArgumentException(String.format("Message with id %s was not found on the mailbox", mailMessage.getGoogleId()));
 			} else if (msg.length > 1) {
 				throw new RuntimeException(String.format("%d matches found on mailbox for id %s but only one was expected", msg.length, mailMessage.getGoogleId()));
 			}
 			
 			msg[0].setFlag(Flag.DELETED, true);
			folder.copyMessages(msg, trash);
 			
 		} finally {
 			this.closeFolder(folder, true);
 			this.closeFolder(trash, false);
 			this.closeStore(store);
 		}
 	}
 	
 	private List<MailMessage> doGetMessages(String username, String password, GmailFolder gmailFolder, List<SearchTerm> searchTerms, boolean expunge, boolean includeAttachments) throws MessagingException {
 		Store store = null;
 		Folder folder = null;
 		
 		try {
 			store = this.getStore(username, password);
 			folder = this.openFolder(store, gmailFolder);
 
 			int size = searchTerms != null ? searchTerms.size() : 0;
 			if (size == 0) {
 				return this.returnMessages(includeAttachments, folder, (IMAPMessage[]) folder.getMessages());
 			} else if (size == 1) {
 				return this.returnMessages(includeAttachments, folder, (IMAPMessage[]) folder.search(searchTerms.get(0)));
 			} else {
 				return this.returnMessages(includeAttachments, folder, (IMAPMessage[]) folder.search(new AndTerm(searchTerms.toArray(new SearchTerm[searchTerms.size()]))));
 			}
 		} catch (AuthenticationFailedException e) {
 			throw new OAuthTokenExpiredException("Authentication failed", e);
 		} finally {
 			this.closeFolder(folder, expunge);
 			this.closeStore(store);
 		}
 	}
 	
 	private void closeStore(Store store) {
 		if (store != null & store.isConnected()) {
 			try {
 				store.close();
 			} catch (MessagingException e) {
 				throw new RuntimeException("Error closing store", e);
 			}
 		}
 	}
 	
 	private void closeFolder(Folder folder, boolean expunge) {
 		if (folder != null && folder.isOpen()) {
 			try {
 				folder.close(expunge);
 			} catch (MessagingException e) {
 				throw new RuntimeException("Error closing folder", e);
 			}
 		}
 	}
 	
 	private List<MailMessage> returnMessages(boolean includeAttachments, Folder folder, IMAPMessage... messages) throws MessagingException {
 		FetchProfile fp = new FetchProfile();
 		fp.add(IMAPFolder.FetchProfileItem.X_GM_MSGID);
 		fp.add(IMAPFolder.FetchProfileItem.X_GM_THRID);
 		fp.add(IMAPFolder.FetchProfileItem.X_GM_LABELS);
 		
 		folder.fetch(messages, fp);
 		return ModelAdapter.toModel(Arrays.asList(messages), includeAttachments);
 	}
 	
 	private <T extends SearchTerm> List<SearchTerm> addStringTerms(List<SearchTerm> searchTerms, List<String> values, Class<T> termClass) {
 		if (!CollectionUtils.isEmpty(values)) {
 			List<SearchTerm> terms = new ArrayList<SearchTerm>();
 			for (String value : values) {
 				try {
 					Constructor<T> constructor = termClass.getConstructor(String.class);
 					terms.add(constructor.newInstance(value));
 				} catch (Exception e) {
 					throw new RuntimeException(e); 
 				}
 			}
 			searchTerms.add(new OrTerm(terms.toArray(new SearchTerm[terms.size()])));
 		}
 		
 		return searchTerms;
 	}
 	
 	private Folder openFolder(Store store, GmailFolder gmailFolder) throws MessagingException {
 		return this.openFolder(store, gmailFolder, Folder.READ_ONLY);
 	}
 	
 	private Folder openFolder(Store store, GmailFolder gmailFolder, int openMode) throws MessagingException {
 		String target = gmailFolder.getDescription(); 
 		for (Folder f : store.getDefaultFolder().xlist("*")) {
 			IMAPFolder folder = (IMAPFolder) f;
 			for (String attr : folder.getAttributes()) {
 				attr = attr.substring(1);
 				if (target.equals(attr)) { // xlist returns things like \Inbox so remove first character
 					f = store.getFolder(target.equals("Inbox") ? target : folder.getFullName());
 					f.open(openMode);
 					return f;
 				}
 			}
 		}
 		
 		throw new IllegalArgumentException(String.format("No such folder %s", target));
 	}
 	
 }
