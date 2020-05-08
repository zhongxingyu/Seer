 package com.snda.grand.space.as.rest.account.impl;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static org.apache.commons.lang.StringUtils.isBlank;
 import static org.springframework.data.mongodb.core.query.Criteria.where;
 import static org.springframework.data.mongodb.core.query.Query.query;
 
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.query.Update;
 import org.springframework.stereotype.Service;
 
 import com.google.common.collect.Lists;
 import com.snda.grand.space.as.exception.AccountAlreadyExistException;
 import com.snda.grand.space.as.exception.InvalidAvailableParamException;
 import com.snda.grand.space.as.exception.InvalidDisplayNameException;
 import com.snda.grand.space.as.exception.InvalidEmailException;
 import com.snda.grand.space.as.exception.InvalidSndaIdException;
 import com.snda.grand.space.as.exception.NoSuchAccountException;
 import com.snda.grand.space.as.exception.NotModifiedException;
 import com.snda.grand.space.as.mongo.model.MongoCollections;
 import com.snda.grand.space.as.mongo.model.PojoAccount;
 import com.snda.grand.space.as.mongo.model.PojoApplication;
 import com.snda.grand.space.as.mongo.model.PojoAuthorization;
 import com.snda.grand.space.as.rest.account.AccountResource;
 import com.snda.grand.space.as.rest.model.Account;
 import com.snda.grand.space.as.rest.model.Application;
 import com.snda.grand.space.as.rest.model.Authorization;
 import com.snda.grand.space.as.rest.util.ApplicationKeys;
 import com.snda.grand.space.as.rest.util.Preconditions;
 import com.snda.grand.space.as.rest.util.Rule;
 
 @Service
 @Path("account")
 public class AccountResourceImpl implements AccountResource {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(AccountResourceImpl.class);
 	private final MongoOperations mongoOps;
 
 	public AccountResourceImpl(MongoOperations mongoOperations) {
 		checkNotNull(mongoOperations, "MongoTemplate is null.");
 		LOGGER.info("AccountResourceImpl initialized.");
 		mongoOps = mongoOperations;
 	}
 
 	@Override
 	@POST
 	@Path("create/{snda_id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Account create(@PathParam("snda_id") String sndaId,
 			@QueryParam("display_name") String displayName,
 			@QueryParam("email") String email,
 			@QueryParam("locale") String locale) {
 		checkSndaId(sndaId);
 		checkDisplayName(displayName);
 		Preconditions.checkEmail(email);
 		if (Preconditions.getAccountBySndaId(mongoOps, sndaId) != null) {
 			throw new AccountAlreadyExistException();
 		}
 		String uid = ApplicationKeys.generateAccessKeyId();
 		if (isBlank(locale)) {
 			locale = "zh_CN";
 		}
 		long creationTime = System.currentTimeMillis();
 		PojoAccount account = new PojoAccount(sndaId, uid, email, displayName,
 				email, locale, creationTime, creationTime, true);
 		mongoOps.insert(account, MongoCollections.ACCOUNT_COLLECTION_NAME);
 		return account.getAccount();
 	}
 
 	@Override
 	@POST
 	@Path("modify/{snda_id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Account modify(@PathParam("snda_id") String sndaId,
 			@QueryParam("display_name") String displayName,
 			@QueryParam("email") String email,
 			@QueryParam("locale") String locale) {
 		checkSndaId(sndaId);
 		PojoAccount account = mongoOps.findOne(query(where(MongoCollections.Account.SNDA_ID).is(sndaId)),
 				PojoAccount.class, MongoCollections.ACCOUNT_COLLECTION_NAME);
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Account : {}", account);
 		}
 		if (account == null) {
 			throw new NoSuchAccountException();
 		}
 		if (isBlank(displayName) && isBlank(email) && isBlank(locale)) {
 			throw new NotModifiedException();
 		}
 		if (email != null && !Rule.checkEmail(email)) {
 			throw new InvalidEmailException();
 		}
 
 		long modifiedTime = System.currentTimeMillis();
 		Update update = new Update();
 		String modifiedDisplayName = isBlank(displayName) ? account.getDisplayName() : displayName;
 		String modifiedEmail = isBlank(email) ? account.getEmail() : email;
 		String modifiedLocale = isBlank(locale) ? account.getLocale() : locale;
 		update.set(MongoCollections.Account.DISPLAY_NAME, modifiedDisplayName)
 			  .set(MongoCollections.Account.EMAIL, modifiedEmail)
 			  .set(MongoCollections.Account.LOCALE, modifiedLocale)
 			  .set(MongoCollections.Account.MODIFIED_TIME, modifiedTime);
 		account.setDisplayName(modifiedDisplayName)
 			   .setEmail(modifiedEmail)
 			   .setLocale(modifiedLocale)
 			   .setCreationTime(modifiedTime);
 		mongoOps.updateFirst(query(where(MongoCollections.Account.SNDA_ID)
 				.is(sndaId)), update, MongoCollections.ACCOUNT_COLLECTION_NAME);
 		return account.getAccount();
 	}
 
 	@Override
 	@POST
 	@Path("available/{snda_id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Account available(@PathParam("snda_id") String sndaId,
 			@QueryParam("available") String available) {
 		checkSndaId(sndaId);
 		boolean enable = checkAvailableParam(available);
 		if (Preconditions.getAccountBySndaId(mongoOps, sndaId) == null) {
 			throw new NoSuchAccountException();
 		}
 		long modifiedTime = System.currentTimeMillis();
 		Update update = new Update();
 		update.set(MongoCollections.Account.AVAILABLE, enable)
 			  .set(MongoCollections.Account.MODIFIED_TIME, modifiedTime);
 		mongoOps.updateFirst(query(where(MongoCollections.Account.SNDA_ID)
 				.is(sndaId)), update, MongoCollections.ACCOUNT_COLLECTION_NAME);
 		PojoAccount account = mongoOps.findOne(
 				query(where(MongoCollections.Account.SNDA_ID).is(sndaId)),
 				PojoAccount.class, MongoCollections.ACCOUNT_COLLECTION_NAME);
 		return account.getAccount();
 	}
 
 	@Override
 	@GET
 	@Path("status/{snda_id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Account status(@PathParam("snda_id") String sndaId) {
 		checkSndaId(sndaId);
 		PojoAccount account = Preconditions.getAccountBySndaId(mongoOps, sndaId);
 		if (account == null) {
 			throw new NoSuchAccountException();
 		}
 		return account.getAccount();
 	}
 
 	@Override
 	@GET
 	@Path("applications/{snda_id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<Application> applications(@PathParam("snda_id") String sndaId) {
 		checkSndaId(sndaId);
 		PojoAccount account = Preconditions.getAccountBySndaId(mongoOps, sndaId);
 		if (account == null) {
 			throw new NoSuchAccountException();
 		}
 		List<PojoApplication> apps = mongoOps.find(
 				query(where(MongoCollections.Application.OWNER).is(account.getUid())),
 				PojoApplication.class, MongoCollections.APPLICATION_COLLECTION_NAME);
 		return PojoApplication.getApplications(apps);
 	}
 	
 	@Override
 	@GET
 	@Path("authorizations/{snda_id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<Authorization> listAuthorizations(@PathParam("snda_id") String sndaId) {
 		checkSndaId(sndaId);
 		PojoAccount account = Preconditions.getAccountBySndaId(mongoOps, sndaId);
 		if (account == null) {
 			throw new NoSuchAccountException();
 		}
 		List<PojoAuthorization> pojoAuthorizations = Preconditions
 				.getAuthorizationsByUid(mongoOps, account.getUid());
 		List<Authorization> authorizations = Lists.newArrayList();
 		for (PojoAuthorization pojoAuthorization : pojoAuthorizations) {
 			PojoApplication pojoApplication = Preconditions
 					.getApplicationByAppId(mongoOps,
 							pojoAuthorization.getAppId());
 			authorizations.add(getAuthorization(pojoApplication,
 					pojoAuthorization));
 		}
 		return authorizations;
 	}
 	
 	private void checkSndaId(String sndaId) {
 		if (isBlank(sndaId)) {
 			throw new InvalidSndaIdException();
 		}
 	}
 	
 	private void checkDisplayName(String displayName) {
 		if (isBlank(displayName)) {
 			throw new InvalidDisplayNameException();
 		}
 	}
 	
 	private boolean checkAvailableParam(String available) {
 		if (available == null 
 				|| (!"true".equalsIgnoreCase(available) 
 						&& !"false".equalsIgnoreCase(available))) {
 			throw new InvalidAvailableParamException();
 		}
 		return Boolean.valueOf(available);
 	}
 	
 	private Authorization getAuthorization(PojoApplication pojoApplication,
 			PojoAuthorization pojoAuthorization) {
 		Authorization authorization = null;
		if (pojoApplication.getAppid().equals(pojoAuthorization.getAppId())) {
 			authorization = new Authorization();
 			authorization.setAppId(pojoApplication.getAppid());
 			authorization.setAuthorizedTime(new DateTime(pojoAuthorization.getAuthorizedTime()));
 			authorization.setPublisherName(pojoApplication.getPublisherName());
 			authorization.setRefreshToken(pojoAuthorization.getRefreshToken());
 			authorization.setScope(pojoApplication.getScope());
 			authorization.setUid(pojoAuthorization.getUid());
 		}
 		return authorization;
 	}
 	
 }
