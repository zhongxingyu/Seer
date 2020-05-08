 /*******************************************************************************
  * Copyright (c) 2011 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tasktop Technologies - initial API and implementation
  *     Yatta Solutions - news (bug 401721)
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.epp.internal.mpc.core.service.DefaultMarketplaceService;
 import org.eclipse.epp.internal.mpc.core.service.Node;
 import org.eclipse.epp.internal.mpc.ui.CatalogRegistry;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.epp.internal.mpc.ui.commands.MarketplaceWizardCommand;
 import org.eclipse.epp.mpc.ui.CatalogDescriptor;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 /**
  * @author David Green
  * @author Benjamin Muskalla
  * @author Carsten Reckord
  */
 public abstract class MarketplaceUrlHandler {
 
 	public static final String DESCRIPTOR_HINT = "org.eclipse.epp.mpc.descriptorHint"; //$NON-NLS-1$
 
 	public static final String MPC_INSTALL_URI = "/mpc/install?"; //$NON-NLS-1$
 
 	public static final String SITE_SEARCH_URI = "/search/site"; //$NON-NLS-1$
 
 	private static final Pattern CONTENT_URL_PATTERN = Pattern.compile("/content/([^/#?]+)"); //$NON-NLS-1$
 
 	private static final Pattern NODE_URL_PATTERN = Pattern.compile("/node/([^/#?]+)"); //$NON-NLS-1$
 
 	public static class SolutionInstallationInfo {
 		private String installId;
 
 		private String state;
 
 		private CatalogDescriptor catalogDescriptor;
 
 		public String getInstallId() {
 			return installId;
 		}
 
 		public String getState() {
 			return state;
 		}
 
 		public CatalogDescriptor getCatalogDescriptor() {
 			return catalogDescriptor;
 		}
 	}
 
 	protected static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
 
 	private static final String PARAM_SPLIT_REGEX = "&"; //$NON-NLS-1$
 
 	private static final String EQUALS_REGEX = "="; //$NON-NLS-1$
 
 	private static final String MPC_STATE = "mpc_state"; //$NON-NLS-1$
 
 	private static final String MPC_INSTALL = "mpc_install"; //$NON-NLS-1$
 
 	public static SolutionInstallationInfo createSolutionInstallInfo(String url) {
 		String query;
 		try {
 			query = new URL(url).getQuery();
 		} catch (MalformedURLException e) {
 			return null;
 		}
 		if (query == null) {
 			return null;
 		}
 		String[] params = query.split(PARAM_SPLIT_REGEX);
 		String installId = null;
 		String state = null;
 		for (String param : params) {
 			String[] keyValue = param.split(EQUALS_REGEX);
 			if(keyValue.length == 2) {
 				String key = keyValue[0];
 				String value = keyValue[1];
 				if (key.equals(MPC_INSTALL)) {
 					installId = value;
 				} else if (key.equals(MPC_STATE)) {
 					state = value;
 				}
 			}
 		}
 		if (installId != null) {
 			CatalogDescriptor descriptor = CatalogRegistry.getInstance().findCatalogDescriptor(url);
 			SolutionInstallationInfo info = new SolutionInstallationInfo();
 			info.installId = installId;
 			info.state = state;
 			if (descriptor != null) {
 				info.catalogDescriptor = descriptor;
 			} else {
 				try {
 					info.catalogDescriptor = new CatalogDescriptor(new URL(url), DESCRIPTOR_HINT);
 				} catch (MalformedURLException e) {
 					return null;
 				}
 			}
 			return info;
 		}
 		return null;
 	}
 
 	public static boolean isPotentialSolution(String url) {
 		return url != null && url.contains(MPC_INSTALL);
 	}
 
 	public static void triggerInstall(SolutionInstallationInfo info) {
 		String installId = info.getInstallId();
 		String mpcState = info.getState();
 		CatalogDescriptor catalogDescriptor = info.getCatalogDescriptor();
 		MarketplaceWizardCommand command = new MarketplaceWizardCommand();
 		command.setSelectedCatalogDescriptor(catalogDescriptor);
 		try {
 			if (mpcState != null) {
 				command.setWizardState(URLDecoder.decode(mpcState, UTF_8));
 			}
 			Map<String, Operation> nodeIdToOperation = new HashMap<String, Operation>();
 			nodeIdToOperation.put(URLDecoder.decode(installId, UTF_8), Operation.INSTALL);
 			command.setOperationByNodeId(nodeIdToOperation);
 		} catch (UnsupportedEncodingException e1) {
 			throw new IllegalStateException(e1);
 		}
 		try {
 			command.execute(new ExecutionEvent());
 		} catch (ExecutionException e) {
 			IStatus status = MarketplaceClientUi.computeStatus(e,
 					Messages.MarketplaceBrowserIntegration_cannotOpenMarketplaceWizard);
 			StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
 		}
 	}
 
 	public boolean handleUri(String uri) {
		if (uri.contains(MPC_INSTALL_URI) && isPotentialSolution(uri)) {
 			SolutionInstallationInfo installInfo = createSolutionInstallInfo(uri);
 			if (installInfo != null) {
 				return handleInstallRequest(installInfo, uri);
 			}
 		}
 
 		CatalogDescriptor descriptor = CatalogRegistry.getInstance().findCatalogDescriptor(uri);
 		if (descriptor == null) {
 			descriptor = handleUnknownCatalog(uri);
 			if (descriptor == null) {
 				return false;
 			}
 		}
 
 		String baseUri;
 		try {
 			baseUri = descriptor.getUrl().toURI().toString();
 			if (!baseUri.endsWith("/")) { //$NON-NLS-1$
 				baseUri += '/';
 			}
 		} catch (URISyntaxException e) {
 			// should be unreachable
 			throw new IllegalStateException(e);
 		}
 
 		String relativeUri = uri.substring(baseUri.length());
 		if (relativeUri.startsWith(DefaultMarketplaceService.API_FAVORITES_URI)) {
 			return handleFavorites(descriptor, relativeUri);
 		} else if (relativeUri.startsWith(DefaultMarketplaceService.API_FEATURED_URI)) {
 			return handleFeatured(descriptor, relativeUri);
 		} else if (relativeUri.startsWith(DefaultMarketplaceService.API_NODE_CONTENT_URI)) {
 			return handleNodeContent(descriptor, relativeUri);
 		} else if (relativeUri.startsWith(DefaultMarketplaceService.API_NODE_URI)) {
 			return handleNode(descriptor, relativeUri);
 		} else if (relativeUri.startsWith(DefaultMarketplaceService.API_POPULAR_URI)) {
 			return handlePopular(descriptor, relativeUri);
 		} else if (relativeUri.startsWith(DefaultMarketplaceService.API_RECENT_URI)) {
 			return handleRecent(descriptor, relativeUri);
 		} else if (relativeUri.startsWith(DefaultMarketplaceService.API_SEARCH_URI)
 				|| relativeUri.startsWith(DefaultMarketplaceService.API_SEARCH_URI_FULL)) {
 			return handleSolrSearch(descriptor, relativeUri);
 		} else if (relativeUri.startsWith(SITE_SEARCH_URI)) {
 			return handleSiteSearch(descriptor, relativeUri);
 		} else {
 			return handleUnknownPath(descriptor, relativeUri);
 		}
 	}
 
 	protected boolean handleUnknownPath(CatalogDescriptor descriptor, String url) {
 		return false;
 	}
 
 	private boolean handleSolrSearch(CatalogDescriptor descriptor, String url) {
 		try {
 			Map<String, String> params = new HashMap<String, String>();
 			String searchString = parseSearchQuery(descriptor, url, params);
 			return handleSearch(descriptor, url, searchString, params);
 		} catch (MalformedURLException e) {
 			// don't handle malformed URLs
 			return false;
 		} catch (URISyntaxException e) {
 			// don't handle malformed URLs
 			return false;
 		}
 	}
 
 	private boolean handleSiteSearch(CatalogDescriptor descriptor, String url) {
 		try {
 			Map<String, String> params = new HashMap<String, String>();
 			String searchString = parseSearchQuery(descriptor, url, params);
 
 			// convert queries of this format
 			//   f[0]=im_taxonomy_vocabulary_1:38&f[1]=im_taxonomy_vocabulary_3:31
 			// to internal solr format
 			//   filter=tid:38 tid:31
 			StringBuilder filter = new StringBuilder();
 			for (Iterator<String> i = params.values().iterator(); i.hasNext();) {
 				String str = i.next();
 				if (str.startsWith("im_taxonomy_vocabulary_")) { //$NON-NLS-1$
 					int sep = str.indexOf(':');
 					if (sep != -1) {
 						String tid = str.substring(sep + 1);
 						if (filter.length() > 0) {
 							filter.append(' ');
 						}
 						filter.append(tid);
 						i.remove();
 					}
 				}
 			}
 			return handleSearch(descriptor, url, searchString, params);
 		} catch (MalformedURLException e) {
 			// don't handle malformed URLs
 			return false;
 		} catch (URISyntaxException e) {
 			// don't handle malformed URLs
 			return false;
 		}
 	}
 
 	private String parseSearchQuery(CatalogDescriptor descriptor, String url, Map<String, String> params)
 			throws URISyntaxException, MalformedURLException {
 		URI searchUri = new URL(descriptor.getUrl(), url).toURI();
 		String path = searchUri.getPath();
 		if (path.endsWith("/")) { //$NON-NLS-1$
 			path = path.substring(0, path.length() - 1);
 		}
 		int sep = path.lastIndexOf('/');
 		String searchString = path.substring(sep + 1);
 		String query = searchUri.getQuery();
 		if (query != null) {
 			extractParams(query, params);
 		}
 		return searchString;
 	}
 
 	protected boolean handleSearch(CatalogDescriptor descriptor, String url, String searchString,
 			Map<String, String> params) {
 		return false;
 	}
 
 	private void extractParams(String query, Map<String, String> params) {
 		final String[] paramStrings = query.split("&"); //$NON-NLS-1$
 		for (String param : paramStrings) {
 			final String[] parts = param.split("="); //$NON-NLS-1$
 			if (parts.length == 2) {
 				params.put(parts[0], parts[1]);
 			}
 		}
 	}
 
 	protected boolean handleRecent(CatalogDescriptor descriptor, String url) {
 		return false;
 	}
 
 	protected boolean handlePopular(CatalogDescriptor descriptor, String url) {
 		return false;
 	}
 
 	private boolean handleNode(CatalogDescriptor descriptor, String url) {
 		Matcher matcher = NODE_URL_PATTERN.matcher(url);
 		String id = null;
 		if (matcher.find()) {
 			id = matcher.group(1);
 		}
 		Node node = new Node();
 		node.setId(id);
 		return handleNode(descriptor, url, node);
 	}
 
 	private boolean handleNodeContent(CatalogDescriptor descriptor, String url) {
 		Matcher matcher = CONTENT_URL_PATTERN.matcher(url);
 		String title = null;
 		if (matcher.find()) {
 			title = matcher.group(1);
 		}
 		Node node = new Node();
 		node.setUrl(url);
 		if (title != null) {
 			int titleEnd = matcher.end();
 			if (titleEnd > -1) {
 				//clean the url of other query parameters
 				node.setUrl(url.substring(0, titleEnd));
 			} else {
 				//unknown format, leave as-is
 				node.setUrl(url);
 			}
 		}
 		return handleNode(descriptor, url, node);
 	}
 
 	protected boolean handleNode(CatalogDescriptor descriptor, String url, Node node) {
 		return false;
 	}
 
 	private boolean handleFeatured(CatalogDescriptor descriptor, String url) {
 		Matcher matcher = Pattern.compile("/featured/(\\d+)(?:,(\\d+))?").matcher(url); //$NON-NLS-1$
 		String cat = null;
 		String market = null;
 		if (matcher.find()) {
 			cat = matcher.group(1);
 			if (matcher.groupCount() > 1) {
 				market = matcher.group(2);
 			}
 		}
 		return handleFeatured(descriptor, url, cat, market);
 	}
 
 	protected boolean handleFeatured(CatalogDescriptor descriptor, String url, String category, String market) {
 		return false;
 	}
 
 	protected boolean handleFavorites(CatalogDescriptor descriptor, String url) {
 		return false;
 	}
 
 	protected CatalogDescriptor handleUnknownCatalog(String url) {
 		return null;
 	}
 
 	protected boolean handleInstallRequest(SolutionInstallationInfo installInfo, String url) {
 		return false;
 	}
 }
