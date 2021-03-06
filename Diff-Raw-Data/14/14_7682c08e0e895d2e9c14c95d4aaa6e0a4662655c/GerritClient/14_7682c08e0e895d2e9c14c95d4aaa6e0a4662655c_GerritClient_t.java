 /*********************************************************************
  * Copyright (c) 2010, 2013 Sony Ericsson/ST Ericsson and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  *  Contributors:
  *      Sony Ericsson/ST Ericsson - initial API and implementation
  *      Tasktop Technologies - improvements
  *      Sascha Scholz (SAP) - improvements
  *      GitHub, Inc. - fixes for bug 354753
  *      Christian Trutz - improvements
  *********************************************************************/
 package org.eclipse.mylyn.internal.gerrit.core.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Type;
 import java.net.URLEncoder;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.httpclient.Cookie;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.mylyn.commons.net.AbstractWebLocation;
 import org.eclipse.mylyn.commons.net.WebUtil;
 import org.eclipse.mylyn.internal.gerrit.core.GerritCorePlugin;
 import org.eclipse.mylyn.internal.gerrit.core.GerritUtil;
 import org.eclipse.mylyn.internal.gerrit.core.client.GerritHttpClient.Request;
 import org.eclipse.mylyn.internal.gerrit.core.client.GerritService.GerritRequest;
 import org.eclipse.mylyn.internal.gerrit.core.client.compat.ChangeDetailService;
 import org.eclipse.mylyn.internal.gerrit.core.client.compat.ChangeDetailX;
 import org.eclipse.mylyn.internal.gerrit.core.client.compat.ChangeManageService;
 import org.eclipse.mylyn.internal.gerrit.core.client.compat.GerritConfigX;
 import org.eclipse.mylyn.internal.gerrit.core.client.compat.PatchDetailService;
 import org.eclipse.mylyn.internal.gerrit.core.client.compat.PatchSetPublishDetailX;
 import org.eclipse.mylyn.internal.gerrit.core.client.compat.ProjectAdminService;
 import org.eclipse.mylyn.internal.gerrit.core.client.compat.ProjectDetailX;
 import org.eclipse.mylyn.internal.gerrit.core.client.data.GerritQueryResult;
 import org.eclipse.mylyn.internal.gerrit.core.remote.GerritRemoteFactoryProvider;
 import org.eclipse.mylyn.reviews.core.model.IRepository;
 import org.eclipse.mylyn.reviews.core.model.IReview;
 import org.eclipse.mylyn.reviews.core.spi.ReviewsClient;
 import org.eclipse.mylyn.reviews.core.spi.remote.emf.AbstractRemoteEmfFactoryProvider;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.osgi.util.NLS;
 import org.osgi.framework.Version;
 
 import com.google.gerrit.common.data.AccountDashboardInfo;
 import com.google.gerrit.common.data.AccountService;
 import com.google.gerrit.common.data.ChangeDetail;
 import com.google.gerrit.common.data.ChangeInfo;
 import com.google.gerrit.common.data.ChangeListService;
 import com.google.gerrit.common.data.GerritConfig;
 import com.google.gerrit.common.data.PatchScript;
 import com.google.gerrit.common.data.PatchSetDetail;
 import com.google.gerrit.common.data.ReviewerResult;
 import com.google.gerrit.common.data.SingleListChangeInfo;
 import com.google.gerrit.reviewdb.Account;
 import com.google.gerrit.reviewdb.AccountDiffPreference;
 import com.google.gerrit.reviewdb.AccountDiffPreference.Whitespace;
 import com.google.gerrit.reviewdb.ApprovalCategoryValue;
 import com.google.gerrit.reviewdb.Change;
 import com.google.gerrit.reviewdb.ContributorAgreement;
 import com.google.gerrit.reviewdb.Patch;
 import com.google.gerrit.reviewdb.PatchLineComment;
 import com.google.gerrit.reviewdb.PatchSet;
 import com.google.gerrit.reviewdb.PatchSet.Id;
 import com.google.gerrit.reviewdb.Project;
 import com.google.gson.reflect.TypeToken;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwtjsonrpc.client.RemoteJsonService;
 import com.google.gwtjsonrpc.client.VoidResult;
 
 /**
  * Facade to the Gerrit RPC API.
  * 
  * @author Mikael Kober
  * @author Thomas Westling
  * @author Steffen Pingel
  * @author Christian Trutz
  * @author Sascha Scholz
  * @author Miles Parker
  */
 public class GerritClient extends ReviewsClient {
 
 	private static final Pattern GERRIT_VERSION_PATTERN = Pattern.compile("Powered by Gerrit Code Review (.+)</p>"); //$NON-NLS-1$
 
 	private abstract class Operation<T> implements AsyncCallback<T> {
 
 		private Throwable exception;
 
 		private T result;
 
 		public abstract void execute(IProgressMonitor monitor) throws GerritException;
 
 		public Throwable getException() {
 			return exception;
 		}
 
 		public T getResult() {
 			return result;
 		}
 
 		public void onFailure(Throwable exception) {
 			if (isAuthenticationException(exception)) {
 				// invalidate login cookie to force re-authentication
 				client.setXsrfCookie(null);
 			}
 			this.exception = exception;
 		}
 
 		public void onSuccess(T result) {
 			setResult(result);
 		}
 
 		protected void setResult(T result) {
 			this.result = result;
 		}
 
 		public void reset() {
 			this.result = null;
 			this.exception = null;
 		}
 
 	}
 
 	public boolean isAuthenticationException(Throwable exception) {
 		if (exception instanceof GerritException) {
 			return ((GerritException) exception).getCode() == -32603
 					&& "Invalid xsrfKey in request".equals(((GerritException) exception).getMessage());
 		}
 		return false;
 	}
 
 	public boolean isNotSignedInException(Throwable exception) {
 		if (exception instanceof GerritException) {
 			return ((GerritException) exception).getCode() == -32603
 					&& "not signed in".equalsIgnoreCase(((GerritException) exception).getMessage());
 		}
 		return false;
 	}
 
 	// XXX belongs in GerritConnector
 	public static GerritAuthenticationState authStateFromString(String token) {
 		try {
 			JSonSupport support = new JSonSupport();
 			return support.getGson().fromJson(token, GerritAuthenticationState.class);
 		} catch (Exception e) {
 			// ignore
 			return null;
 		}
 	}
 
 	// XXX belongs in GerritConnector
 	public static String authStateToString(GerritAuthenticationState authState) {
 		if (authState == null) {
 			return null;
 		}
 		try {
 			JSonSupport support = new JSonSupport();
 			return support.getGson().toJson(authState);
 		} catch (Exception e) {
 			// ignore
 			return null;
 		}
 	}
 
 	private final GerritHttpClient client;
 
 	private volatile GerritConfiguration config;
 
 	private Account myAcount;
 
 	private AccountDiffPreference myDiffPreference;
 
 	private Version myVersion;
 
 //	private GerritConfig createDefaultConfig() {
 //		GerritConfig config = new GerritConfig();
 //		List<ApprovalType> approvals = new ArrayList<ApprovalType>();
 //
 //		ApprovalCategory category = new ApprovalCategory(new ApprovalCategory.Id("VRIF"), "Verified");
 //		category.setAbbreviatedName("V");
 //		category.setPosition((short) 0);
 //		List<ApprovalCategoryValue> values = new ArrayList<ApprovalCategoryValue>();
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) -1), "Fails"));
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) 0), "No score"));
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) 1), "Verified"));
 //		approvals.add(new ApprovalType(category, values));
 //
 //		category = new ApprovalCategory(new ApprovalCategory.Id("CRVW"), "Code Review");
 //		category.setAbbreviatedName("R");
 //		category.setPosition((short) 1);
 //		values = new ArrayList<ApprovalCategoryValue>();
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) -2),
 //				"Do not submit"));
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) -1),
 //				"I would prefer that you didn\u0027t submit this"));
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) 0), "No score"));
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) 1),
 //				"Looks good to me, but someone else must approve"));
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) 2),
 //				"Looks good to me, approved"));
 //		approvals.add(new ApprovalType(category, values));
 //
 //		category = new ApprovalCategory(new ApprovalCategory.Id("IPCL"), "IP Clean");
 //		category.setAbbreviatedName("I");
 //		category.setPosition((short) 2);
 //		values = new ArrayList<ApprovalCategoryValue>();
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) -1),
 //				"Unclean IP, do not check in"));
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) 0), "No score"));
 //		values.add(new ApprovalCategoryValue(new ApprovalCategoryValue.Id(category.getId(), (short) 1),
 //				"IP review completed"));
 //		approvals.add(new ApprovalType(category, values));
 //
 //		List<ApprovalType> actions = new ArrayList<ApprovalType>();
 //
 //		ApprovalTypes approvalTypes = new ApprovalTypes(approvals, actions);
 //		config.setApprovalTypes(approvalTypes);
 //		return config;
 //	}
 
 	private final Map<Class<? extends RemoteJsonService>, RemoteJsonService> serviceByClass;
 
 	private volatile boolean configRefreshed;
 
 	/**
 	 * The GWT query API was removed in Gerrit 2.5 and replaced with a REST API. If this flag is true, the REST API is
 	 * used.
 	 */
 	private boolean restQueryAPIEnabled;
 
 	public GerritClient(TaskRepository repository, AbstractWebLocation location) {
 		this(repository, location, null, null, null);
 	}
 
 	public GerritClient(TaskRepository repository, AbstractWebLocation location, GerritConfiguration config,
 			GerritAuthenticationState authState) {
 		this(repository, location, config, authState, null);
 	}
 
 	public GerritClient(TaskRepository repository, AbstractWebLocation location, GerritConfiguration config,
 			GerritAuthenticationState authState, String xsrfKey) {
 		super(repository);
 		this.client = new GerritHttpClient(location) {
 			@Override
 			protected void sessionChanged(Cookie cookie) {
 				GerritAuthenticationState authState = new GerritAuthenticationState();
 				authState.setCookie(cookie);
 				authStateChanged(authState);
 			}
 		};
 		if (authState != null) {
 			client.setXsrfCookie(authState.getCookie());
 		}
 		if (xsrfKey != null) {
 			client.setXsrfKey(xsrfKey);
 		}
 		this.serviceByClass = new HashMap<Class<? extends RemoteJsonService>, RemoteJsonService>();
 		this.config = config;
 	}
 
 	public PatchLineComment saveDraft(Patch.Key patchKey, String message, int line, short side, String parentUuid,
 			IProgressMonitor monitor) throws GerritException {
 		PatchLineComment.Key id = new PatchLineComment.Key(patchKey, null);
 		final PatchLineComment comment = new PatchLineComment(id, line, getAccount(monitor).getId(), parentUuid);
 		comment.setMessage(message);
 		comment.setSide(side);
 		return execute(monitor, new Operation<PatchLineComment>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getPatchDetailService(monitor).saveDraft(comment, this);
 			}
 		});
 	}
 
 	public ChangeDetail abandon(String reviewId, int patchSetId, final String message, IProgressMonitor monitor)
 			throws GerritException {
 		final PatchSet.Id id = new PatchSet.Id(new Change.Id(id(reviewId)), patchSetId);
 		return execute(monitor, new Operation<ChangeDetail>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getChangeManageService(monitor).abandonChange(id, message, this);
 			}
 		});
 	}
 
 	/**
 	 * Returns the details for a specific review.
 	 */
 	public ChangeDetailX getChangeDetail(int reviewId, IProgressMonitor monitor) throws GerritException {
 		final Change.Id id = new Change.Id(reviewId);
 		return execute(monitor, new Operation<ChangeDetailX>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getChangeDetailService(monitor).changeDetailX(id, this);
 			}
 		});
 	}
 
 	public void loadPatchSetContent(PatchSetContent patchSetContent, IProgressMonitor monitor) throws GerritException {
 			Id baseId = (patchSetContent.getBase() != null) ? patchSetContent.getBase().getId() : null;
 			Id targetId = patchSetContent.getTarget().getId();
 			if (patchSetContent.getTargetDetail() == null) {
 				PatchSetDetail targetDetail = getPatchSetDetail(baseId, targetId, monitor);
 				patchSetContent.setTargetDetail(targetDetail);
 			}
 			for (Patch patch : patchSetContent.getTargetDetail().getPatches()) {
 				PatchScript patchScript = getPatchScript(patch.getKey(), baseId, targetId, monitor);
 				if (patchScript != null) {
 					patchSetContent.putPatchScriptByPatchKey(patch.getKey(), patchScript);
 				}
 			}
 	}
 
 	public GerritConfigX getGerritConfig() {
 		return config == null ? null : config.getGerritConfig();
 	}
 
 	public GerritConfiguration getConfiguration() {
 		return config;
 	}
 
	/**
	 * @deprecated Do not use, this method is going to be removed in 2.1. See bug 413630.
	 */
	@Deprecated
 	public AccountDiffPreference getDiffPreference(IProgressMonitor monitor) throws GerritException {
 		synchronized (this) {
 			if (myDiffPreference != null) {
 				return myDiffPreference;
 			}
 		}
 		AccountDiffPreference diffPreference = execute(monitor, new Operation<AccountDiffPreference>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getAccountService(monitor).myDiffPreferences(this);
 			}
 		});
 
 		synchronized (this) {
 			myDiffPreference = diffPreference;
 		}
 		return myDiffPreference;
 	}
 
 	public GerritSystemInfo getInfo(IProgressMonitor monitor) throws GerritException {
 		Version version = getCachedVersion(monitor);
 		List<ContributorAgreement> contributorAgreements = null;
 		Account account = null;
 		if (!isAnonymous()) {
 //			contributorAgreements = execute(monitor, new GerritOperation<List<ContributorAgreement>>() {
 //				@Override
 //				public void execute(IProgressMonitor monitor) throws GerritException {
 //					getSystemInfoService().contributorAgreements(this);
 //				}
 //			});
 			account = getAccount(monitor);
 		} else {
 			// XXX should run some more meaningful validation as anonymous, for now any call is good to validate the URL etc.
 			executeQuery(monitor, "status:open"); //$NON-NLS-1$
 		}
 		refreshConfigOnce(monitor);
 		return new GerritSystemInfo(version, contributorAgreements, account);
 	}
 
 	public PatchScript getPatchScript(final Patch.Key key, final PatchSet.Id leftId, final PatchSet.Id rightId,
 			IProgressMonitor monitor) throws GerritException {
 		//final AccountDiffPreference diffPrefs = getDiffPreference(monitor);
 		//final AccountDiffPreference diffPrefs = new AccountDiffPreference(getAccount(monitor).getId());
 		final AccountDiffPreference diffPrefs = createAccountDiffPreference();
 		return execute(monitor, new Operation<PatchScript>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getPatchDetailService(monitor).patchScript(key, leftId, rightId, diffPrefs, this);
 			}
 		});
 	}
 
 	private AccountDiffPreference createAccountDiffPreference() {
 		AccountDiffPreference diffPrefs = new AccountDiffPreference((Account.Id) null);
 		diffPrefs.setLineLength(Integer.MAX_VALUE);
 		diffPrefs.setTabSize(4);
 		diffPrefs.setContext(AccountDiffPreference.WHOLE_FILE_CONTEXT);
 		diffPrefs.setIgnoreWhitespace(Whitespace.IGNORE_NONE);
 		diffPrefs.setIntralineDifference(false);
 		return diffPrefs;
 	}
 
 	public PatchSetDetail getPatchSetDetail(final PatchSet.Id idBase, final PatchSet.Id idTarget,
 			IProgressMonitor monitor) throws GerritException {
 		PatchSetDetail result = null;
 		try {
 			// Gerrit 2.4+
 			result = execute(monitor, new Operation<PatchSetDetail>() {
 				@Override
 				public void execute(IProgressMonitor monitor) throws GerritException {
 					getChangeDetailService(monitor).patchSetDetail2(idBase, idTarget, createAccountDiffPreference(),
 							this);
 				}
 			});
 		} catch (GerritException e) {
 			try {
 				// fallback for Gerrit < 2.1.7
 				if (isNoSuchServiceError(e)) {
 					result = execute(monitor, new Operation<PatchSetDetail>() {
 						@Override
 						public void execute(IProgressMonitor monitor) throws GerritException {
 							getChangeDetailService(monitor).patchSetDetail(idTarget, this);
 						}
 					});
 				} else {
 					throw e;
 				}
 			} catch (GerritException e2) {
 				// fallback for Gerrit 2.1.7
 				String message = e2.getMessage();
 				if (message != null && message.contains("Error parsing request")) { //$NON-NLS-1$
 					result = execute(monitor, new Operation<PatchSetDetail>() {
 						@Override
 						public void execute(IProgressMonitor monitor) throws GerritException {
 							getChangeDetailService(monitor).patchSetDetail(idBase, idTarget,
 									createAccountDiffPreference(), this);
 						}
 					});
 				} else {
 					throw e2;
 				}
 			}
 		}
 		return result;
 	}
 
 	boolean isNoSuchServiceError(GerritException e) {
 		String message = e.getMessage();
 		return message != null && message.contains("No such service method");
 	}
 
 	public PatchSetPublishDetailX getPatchSetPublishDetail(final PatchSet.Id id, IProgressMonitor monitor)
 			throws GerritException {
 		PatchSetPublishDetailX publishDetail = execute(monitor,
 				new Operation<org.eclipse.mylyn.internal.gerrit.core.client.compat.PatchSetPublishDetailX>() {
 					@Override
 					public void execute(IProgressMonitor monitor) throws GerritException {
 						getChangeDetailService(monitor).patchSetPublishDetailX(id, this);
 					}
 				});
 		return publishDetail;
 	}
 
 	public GerritChange getChange(String reviewId, IProgressMonitor monitor) throws GerritException {
 		GerritChange change = new GerritChange();
 		int id;
 		try {
 			id = id(reviewId);
 		} catch (GerritException e) {
 			List<GerritQueryResult> result = executeQuery(monitor, reviewId);
 			if (result.size() == 1) {
 				id = result.get(0).getNumber();
 			} else {
 				throw e;
 			}
 		}
 		ChangeDetailX changeDetail = getChangeDetail(id, monitor);
 		List<PatchSetDetail> patchSets = new ArrayList<PatchSetDetail>(changeDetail.getPatchSets().size());
 		Map<PatchSet.Id, PatchSetPublishDetailX> patchSetPublishDetailByPatchSetId = new HashMap<PatchSet.Id, PatchSetPublishDetailX>();
 		for (PatchSet patchSet : changeDetail.getPatchSets()) {
 			try {
 				PatchSetDetail patchSetDetail = getPatchSetDetail(null, patchSet.getId(), monitor);
 				patchSets.add(patchSetDetail);
 				if (!isAnonymous()) {
 					PatchSetPublishDetailX patchSetPublishDetail = getPatchSetPublishDetail(patchSet.getId(), monitor);
 					patchSetPublishDetailByPatchSetId.put(patchSet.getId(), patchSetPublishDetail);
 				}
 			} catch (GerritException e) {
 				handleMissingPatchSet("Patch Set " + patchSet.getPatchSetId() + " items for Review " + reviewId, e);
 			}
 		}
 		change.setChangeDetail(changeDetail);
 		change.setPatchSets(patchSets);
 		change.setPatchSetPublishDetailByPatchSetId(patchSetPublishDetailByPatchSetId);
 		return change;
 	}
 
 	private void handleMissingPatchSet(String desc, GerritException e) {
 		GerritCorePlugin.logWarning("Couldn't load " + desc
 				+ ". (Perhaps the Patch Set has been removed from repository?)", e);
 	}
 
 	public int id(String id) throws GerritException {
 		if (id == null) {
 			throw new GerritException("Invalid ID (null)");
 		}
 		try {
 			return Integer.parseInt(id);
 		} catch (NumberFormatException e) {
 			throw new GerritException(NLS.bind("Invalid ID (''{0}'')", id));
 		}
 	}
 
 	public void publishComments(String reviewId, int patchSetId, final String message,
 			final Set<ApprovalCategoryValue.Id> approvals, IProgressMonitor monitor) throws GerritException {
 		final PatchSet.Id id = new PatchSet.Id(new Change.Id(id(reviewId)), patchSetId);
 		execute(monitor, new Operation<VoidResult>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getPatchDetailService(monitor).publishComments(id, message, approvals, this);
 			}
 		});
 	}
 
 	public ReviewerResult addReviewers(String reviewId, final List<String> reviewers, IProgressMonitor monitor)
 			throws GerritException {
 		final Change.Id id = new Change.Id(id(reviewId));
 		try {
 			return execute(monitor, new Operation<ReviewerResult>() {
 				@Override
 				public void execute(IProgressMonitor monitor) throws GerritException {
 					getPatchDetailService(monitor).addReviewers(id, reviewers, this);
 				}
 			});
 		} catch (GerritException e) {
 			// Gerrit 2.2
 			String message = e.getMessage();
 			if (message != null && message.contains("Error parsing request")) { //$NON-NLS-1$
 				return execute(monitor, new Operation<ReviewerResult>() {
 					@Override
 					public void execute(IProgressMonitor monitor) throws GerritException {
 						getPatchDetailService(monitor).addReviewers(id, reviewers, false, this);
 					}
 				});
 			} else {
 				throw e;
 			}
 		}
 	}
 
 	/**
 	 * Returns the latest 25 reviews.
 	 */
 	public List<GerritQueryResult> queryAllReviews(IProgressMonitor monitor) throws GerritException {
 		return executeQuery(monitor, "status:open"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the latest 25 reviews for the given project.
 	 */
 	public List<GerritQueryResult> queryByProject(IProgressMonitor monitor, final String project)
 			throws GerritException {
 		return executeQuery(monitor, "status:open project:" + project); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns changes associated with the logged in user. This includes all open, closed and review requests for the
 	 * user. On Gerrit 2.4 and earlier closed reviews are not included.
 	 */
 	public List<GerritQueryResult> queryMyReviews(IProgressMonitor monitor) throws GerritException {
 		if (hasJsonRpcApi(monitor) && !restQueryAPIEnabled) {
 			try {
 				final Account account = getAccount(monitor);
 				AccountDashboardInfo ad = execute(monitor, new Operation<AccountDashboardInfo>() {
 					@Override
 					public void execute(IProgressMonitor monitor) throws GerritException {
 						getChangeListService(monitor).forAccount(account.getId(), this);
 					}
 				});
 
 				List<ChangeInfo> allMyChanges = ad.getByOwner();
 				allMyChanges.addAll(ad.getForReview());
 				allMyChanges.addAll(ad.getClosed());
 				return convert(allMyChanges);
 			} catch (GerritException e) {
 				if (isNoSuchServiceError(e)) {
 					restQueryAPIEnabled = true;
 				} else {
 					throw e;
 				}
 			}
 		}
 		// the "self" alias is only supported in Gerrit 2.5 and later
 		return executeQueryRest(monitor, "owner:self OR reviewer:self"); //$NON-NLS-1$
 	}
 
 	private boolean hasJsonRpcApi(IProgressMonitor monitor) throws GerritException {
 		Version version = getCachedVersion(monitor);
 		return !GerritVersion.isVersion26OrLater(version);
 	}
 
 	/**
 	 * Returns watched changes of the currently logged in user
 	 */
 	public List<GerritQueryResult> queryWatchedReviews(IProgressMonitor monitor) throws GerritException {
 		return executeQuery(monitor, "is:watched status:open"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Retrieves the root URL for the Gerrit instance and attempts to parse the configuration from the JavaScript
 	 * portion of the page.
 	 */
 	private GerritConfigX refreshGerritConfig(final IProgressMonitor monitor) throws GerritException {
 		try {
 			GerritConfigX gerritConfig = client.execute(new Request<GerritConfigX>() {
 				@Override
 				public HttpMethodBase createMethod() throws IOException {
 					return new GetMethod(client.getUrl() + "/"); //$NON-NLS-1$
 				}
 
 				@Override
 				public GerritConfigX process(HttpMethodBase method) throws IOException {
 					InputStream in = WebUtil.getResponseBodyAsStream(method, monitor);
 					try {
 						GerritHtmlProcessor processor = new GerritHtmlProcessor();
 						processor.parse(in, method.getResponseCharSet());
 						return processor.getConfig();
 					} finally {
 						in.close();
 					}
 				}
 			}, monitor);
 
 			if (gerritConfig == null) {
 				throw new GerritException("Failed to obtain Gerrit configuration");
 			}
 			return gerritConfig;
 		} catch (UnknownHostException cause) {
 			GerritException e = new GerritException("Unknown host: " + cause.getMessage());
 			e.initCause(cause);
 			throw e;
 		} catch (IOException cause) {
 			GerritException e = new GerritException(cause.getMessage());
 			e.initCause(cause);
 			throw e;
 		}
 	}
 
 	public GerritConfiguration refreshConfig(IProgressMonitor monitor) throws GerritException {
 		configRefreshed = true;
 		GerritConfigX gerritConfig = refreshGerritConfig(monitor);
 		List<Project> projects = getVisibleProjects(monitor, gerritConfig);
 		Account account = null;
 		try {
 			account = getAccount(monitor);
 		} catch (GerritException e) {
 			if (!isNotSignedInException(e)) {
 				throw e;
 			}
 		}
 		config = new GerritConfiguration(gerritConfig, projects, account);
 		configurationChanged(config);
 		return config;
 	}
 
 	public GerritConfiguration refreshConfigOnce(IProgressMonitor monitor) throws GerritException {
 		if (!configRefreshed && config == null) {
 			try {
 				refreshConfig(monitor);
 			} catch (GerritException e) {
 				// don't fail validation in case config parsing fails
 			}
 		}
 		return getConfiguration();
 	}
 
	/**
	 * @deprecated Do not use, this method is going to be removed in 2.1. See bug 413630.
	 */
	@Deprecated
 	public ChangeDetail publish(String reviewId, int patchSetId, IProgressMonitor monitor) throws GerritException {
 		final PatchSet.Id id = new PatchSet.Id(new Change.Id(id(reviewId)), patchSetId);
 		return execute(monitor, new Operation<ChangeDetail>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getChangeManageService(monitor).publish(id, this);
 			}
 		});
 	}
 
 	public ChangeDetail rebase(String reviewId, int patchSetId, IProgressMonitor monitor) throws GerritException {
 		final PatchSet.Id id = new PatchSet.Id(new Change.Id(id(reviewId)), patchSetId);
 		return execute(monitor, new Operation<ChangeDetail>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getChangeManageService(monitor).rebaseChange(id, this);
 			}
 		});
 	}
 
 	public ChangeDetail restore(String reviewId, int patchSetId, final String message, IProgressMonitor monitor)
 			throws GerritException {
 		final PatchSet.Id id = new PatchSet.Id(new Change.Id(id(reviewId)), patchSetId);
 		return execute(monitor, new Operation<ChangeDetail>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getChangeManageService(monitor).restoreChange(id, message, this);
 			}
 		});
 	}
 
	/**
	 * @deprecated Do not use, this method is going to be removed in 2.1. See bug 413630.
	 */
	@Deprecated
 	public ChangeDetail revert(String reviewId, int patchSetId, final String message, IProgressMonitor monitor)
 			throws GerritException {
 		final PatchSet.Id id = new PatchSet.Id(new Change.Id(id(reviewId)), patchSetId);
 		return execute(monitor, new Operation<ChangeDetail>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getChangeManageService(monitor).revertChange(id, message, this);
 			}
 		});
 	}
 
 	public ChangeDetail submit(String reviewId, int patchSetId, final String message, IProgressMonitor monitor)
 			throws GerritException {
 		final PatchSet.Id id = new PatchSet.Id(new Change.Id(id(reviewId)), patchSetId);
 		return execute(monitor, new Operation<ChangeDetail>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getChangeManageService(monitor).submit(id, this);
 			}
 		});
 	}
 
 	public List<GerritQueryResult> executeQuery(IProgressMonitor monitor, final String queryString)
 			throws GerritException {
 		if (hasJsonRpcApi(monitor) && !restQueryAPIEnabled) {
 			try {
 				SingleListChangeInfo sl = execute(monitor, new Operation<SingleListChangeInfo>() {
 					@Override
 					public void execute(IProgressMonitor monitor) throws GerritException {
 						getChangeListService(monitor).allQueryNext(queryString, "z", -1, this); //$NON-NLS-1$
 					}
 				});
 				return convert(sl.getChanges());
 			} catch (GerritException e) {
 				if (isNoSuchServiceError(e)) {
 					restQueryAPIEnabled = true;
 				} else {
 					throw e;
 				}
 			}
 		}
 
 		return executeQueryRest(monitor, queryString);
 	}
 
 	private List<GerritQueryResult> convert(List<ChangeInfo> changes) {
 		List<GerritQueryResult> results = new ArrayList<GerritQueryResult>(changes.size());
 		for (ChangeInfo changeInfo : changes) {
 			GerritQueryResult result = new GerritQueryResult(changeInfo);
 			results.add(result);
 		}
 		return results;
 	}
 
 	public List<GerritQueryResult> executeQueryRest(IProgressMonitor monitor, final String queryString)
 			throws GerritException {
 		return execute(monitor, new Operation<List<GerritQueryResult>>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				try {
 					Request<List<GerritQueryResult>> request = new Request<List<GerritQueryResult>>() {
 						@Override
 						public HttpMethodBase createMethod() throws IOException {
 							GetMethod method = new GetMethod(client.getUrl()
 									+ "/changes/?q=" + URLEncoder.encode(queryString, "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$ 
 							method.setRequestHeader("Accept", "application/json"); //$NON-NLS-1$//$NON-NLS-2$
 							return method;
 						}
 
 						@Override
 						public List<GerritQueryResult> process(HttpMethodBase method) throws IOException {
 							JSonSupport json = new JSonSupport();
 							// Gerrit 2.5 prepends the output with bogus characters
 							// see http://code.google.com/p/gerrit/issues/detail?id=1648
 							String content = method.getResponseBodyAsString();
 							if (content.startsWith(")]}'\n")) { //$NON-NLS-1$
 								content = content.substring(5);
 							}
 							Type type = new TypeToken<List<GerritQueryResult>>() {
 							}.getType();
 							return json.getGson().fromJson(content, type);
 						}
 					};
 					List<GerritQueryResult> result = client.execute(request, monitor);
 					onSuccess(result);
 				} catch (Exception e) {
 					onFailure(e);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Returns the (possibly cached) account for this client.
 	 */
 	public Account getAccount(IProgressMonitor monitor) throws GerritException {
 //		LoginResult result = execute(monitor, new GerritOperation<LoginResult>() {
 //			@Override
 //			public void execute(IProgressMonitor monitor) throws GerritException {
 //				getService(UserPassAuthService.class).authenticate("steffen.pingel", null, this);
 //			}
 //		});
 
 		synchronized (this) {
 			if (myAcount != null) {
 				return myAcount;
 			}
 		}
 		Account account = executeAccount(monitor);
 
 		synchronized (this) {
 			myAcount = account;
 		}
 		return myAcount;
 	}
 
 	public Account executeAccount(IProgressMonitor monitor) throws GerritException {
 		return execute(monitor, new Operation<Account>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				getAccountService(monitor).myAccount(this);
 			}
 		});
 	}
 
 	private AccountService getAccountService(IProgressMonitor monitor) {
 		return getService(AccountService.class, monitor);
 	}
 
 	private ChangeDetailService getChangeDetailService(IProgressMonitor monitor) {
 		return getService(ChangeDetailService.class, monitor);
 	}
 
 	private ChangeListService getChangeListService(IProgressMonitor monitor) {
 		return getService(ChangeListService.class, monitor);
 	}
 
 	private ChangeManageService getChangeManageService(IProgressMonitor monitor) {
 		return getService(ChangeManageService.class, monitor);
 	}
 
 	private PatchDetailService getPatchDetailService(IProgressMonitor monitor) {
 		return getService(PatchDetailService.class, monitor);
 	}
 
 	private List<Project> getVisibleProjects(IProgressMonitor monitor, GerritConfig gerritConfig)
 			throws GerritException {
 		List<Project> result = new ArrayList<Project>();
 		try {
 			List<ProjectDetailX> projectDetails = execute(monitor, new Operation<List<ProjectDetailX>>() {
 				@Override
 				public void execute(IProgressMonitor monitor) throws GerritException {
 					getProjectAdminService(monitor).visibleProjectDetails(this);
 				}
 			});
 			for (ProjectDetailX projectDetail : projectDetails) {
 				if (!GerritUtil.isPermissionOnlyProject(projectDetail, gerritConfig)) {
 					result.add(projectDetail.project);
 				}
 			}
 		} catch (GerritException e) {
 			// Gerrit <= 2.2.1
 			if (isNoSuchServiceError(e)) {
 				List<Project> projects = execute(monitor, new Operation<List<Project>>() {
 					@Override
 					public void execute(IProgressMonitor monitor) throws GerritException {
 						getProjectAdminService(monitor).visibleProjects(this);
 					}
 				});
 				for (Project project : projects) {
 					ProjectDetailX projectDetail = new ProjectDetailX();
 					projectDetail.setProject(project);
 					if (!GerritUtil.isPermissionOnlyProject(projectDetail, gerritConfig)) {
 						result.add(project);
 					}
 				}
 			} else {
 				throw e;
 			}
 		}
 		return result;
 	}
 
 	private ProjectAdminService getProjectAdminService(IProgressMonitor monitor) {
 		return getService(ProjectAdminService.class, monitor);
 	}
 
 	public boolean isAnonymous() {
 		return client.isAnonymous();
 	}
 
 	protected void configurationChanged(GerritConfiguration config) {
 	}
 
 	protected void authStateChanged(GerritAuthenticationState config) {
 	}
 
 	protected <T> T execute(IProgressMonitor monitor, Operation<T> operation) throws GerritException {
 		try {
 			GerritRequest.setCurrentRequest(new GerritRequest(monitor));
 			try {
 				return executeOnce(monitor, operation);
 			} catch (GerritException e) {
 				if (isAuthenticationException(e)) {
 					operation.reset();
 					return executeOnce(monitor, operation);
 				}
 				throw e;
 			}
 		} finally {
 			GerritRequest.setCurrentRequest(null);
 		}
 	}
 
 	private <T> T executeOnce(IProgressMonitor monitor, Operation<T> operation) throws GerritException {
 		operation.execute(monitor);
 		if (operation.getException() instanceof GerritException) {
 			throw (GerritException) operation.getException();
 		} else if (operation.getException() instanceof OperationCanceledException) {
 			throw (OperationCanceledException) operation.getException();
 		} else if (operation.getException() instanceof RuntimeException) {
 			throw (RuntimeException) operation.getException();
 		} else if (operation.getException() != null) {
 			GerritException e = new GerritException();
 			e.initCause(operation.getException());
 			throw e;
 		}
 		return operation.getResult();
 	}
 
 	protected synchronized <T extends RemoteJsonService> T getService(Class<T> clazz, IProgressMonitor monitor) {
 		Version version = Version.emptyVersion;
 		try {
 			version = getCachedVersion(monitor);
 		} catch (GerritException e) {
 			// ignore, continue with emptyVersion
 		}
 		RemoteJsonService service = serviceByClass.get(clazz);
 		if (service == null) {
 			service = GerritService.create(clazz, client, version);
 			serviceByClass.put(clazz, service);
 		}
 		return clazz.cast(service);
 	}
 
 	@Override
 	public AbstractRemoteEmfFactoryProvider<IRepository, IReview> createFactoryProvider() {
 		return new GerritRemoteFactoryProvider(this);
 	}
 
 	private Version getCachedVersion(IProgressMonitor monitor) throws GerritException {
 		synchronized (this) {
 			if (myVersion != null) {
 				return myVersion;
 			}
 		}
 		Version version = getVersion(monitor);
 
 		synchronized (this) {
 			myVersion = version;
 		}
 		return myVersion;
 	}
 
 	public Version getVersion(IProgressMonitor monitor) throws GerritException {
 		return execute(monitor, new Operation<Version>() {
 			@Override
 			public void execute(IProgressMonitor monitor) throws GerritException {
 				try {
 					Request<String> request = new Request<String>() {
 						@Override
 						public HttpMethodBase createMethod() throws IOException {
 							return new GetMethod(client.getUrl() + "/tools/hooks/"); //$NON-NLS-1$
 						}
 
 						@Override
 						public String process(HttpMethodBase method) throws IOException {
 							String content = method.getResponseBodyAsString();
 							Matcher matcher = GERRIT_VERSION_PATTERN.matcher(content);
 							if (matcher.find()) {
 								return matcher.group(1);
 							}
 							return null;
 						}
 					};
 					String result = client.execute(request, false, monitor);
 					Version version = GerritVersion.parseGerritVersion(result);
 					onSuccess(version);
 				} catch (Exception e) {
 					onFailure(e);
 				}
 			}
 		});
 	}
 }
