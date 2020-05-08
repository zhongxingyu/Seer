 /*******************************************************************************
  * Copyright (c) 2010 Tasktop Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tasktop Technologies - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.MessageFormat;
 
 import org.eclipse.epp.internal.mpc.core.service.Node;
 import org.eclipse.epp.internal.mpc.core.service.Tag;
 import org.eclipse.epp.internal.mpc.core.service.Tags;
 import org.eclipse.epp.internal.mpc.core.util.TextUtil;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUiPlugin;
 import org.eclipse.epp.internal.mpc.ui.util.Util;
 import org.eclipse.equinox.internal.p2.discovery.AbstractCatalogSource;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 import org.eclipse.equinox.internal.p2.discovery.model.Overview;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.WorkbenchUtil;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.AbstractDiscoveryItem;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.DiscoveryResources;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.layout.RowLayoutFactory;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTError;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.accessibility.AccessibleAdapter;
 import org.eclipse.swt.accessibility.AccessibleEvent;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
 
 /**
  * @author Steffen Pingel
  * @author David Green
  */
 @SuppressWarnings("unused")
 public class DiscoveryItem<T extends CatalogItem> extends AbstractDiscoveryItem<T> implements PropertyChangeListener {
 
 	/**
 	 * conditional login url for the eclipse marketplace
 	 */
 	private static final String MARKETPLACE_LOGIN_URL = "https://marketplace.eclipse.org/login/sso?redirect=node/{0}"; //$NON-NLS-1$
 
 	/**
 	 * Eclipse marketplace, which supports the login scheme in {@link #MARKETPLACE_LOGIN_URL}
 	 */
 	private static final String ECLIPSE_MARKETPLACE_URL = "http://marketplace.eclipse.org/"; //$NON-NLS-1$
 
 	private static final String INFO_HREF = "info"; //$NON-NLS-1$
 
 	private static final int DESCRIPTION_MARGIN_LEFT = 8;
 
 	private static final int DESCRIPTION_MARGIN_TOP = 8;
 
 	private static final int TAGS_MARGIN_TOP = 2;
 
 	private static final int BUTTONBAR_MARGIN_TOP = 8;
 
 	private static final int SEPARATOR_MARGIN_TOP = 8;
 
 	private static abstract class LinkListener implements MouseListener, SelectionListener {
 
 		private boolean active = false;
 
 		public void widgetSelected(SelectionEvent e) {
 			StyledText link = (StyledText) e.getSource();
 			if (link.getSelectionCount() != 0) {
 				active = false;
 			}
 		}
 
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 
 		public void mouseDoubleClick(MouseEvent e) {
 		}
 
 		public void mouseDown(MouseEvent e) {
 			StyledText link = (StyledText) e.getSource();
 			active = (e.button == 1) && link.getSelectionCount() == 0;
 		}
 
 		public void mouseUp(MouseEvent e) {
 			if (!active) {
 				return;
 			}
 			active = false;
 			if (e.button != 1) {
 				return;
 			}
 			StyledText link = (StyledText) e.getSource();
 			int offset = link.getOffsetAtLocation(new Point(e.x, e.y));
 			if (offset >= 0 && offset < link.getCharCount()) {
 				StyleRange style = link.getStyleRangeAtOffset(offset);
 				if (style != null && style.data != null) {
 					selected((String) style.data);
 				}
 			}
 		}
 
 		protected abstract void selected(String href);
 	}
 
 	private static final int MAX_IMAGE_HEIGHT = 86;
 
 	private static final int MIN_IMAGE_HEIGHT = 64;
 
 	private static final int MAX_IMAGE_WIDTH = 75;
 
 	private Composite checkboxContainer;
 
 	private final CatalogItem connector;
 
 	private StyledText description;
 
 	private Label iconLabel;
 
 	private ToolItem infoButton;
 
 	private Label nameLabel;
 
 	private Control providerLabel;
 
 	private final IShellProvider shellProvider;
 
 	private ToolItem updateButton;
 
 	private final MarketplaceViewer viewer;
 
 	private ItemButtonController buttonController;
 
 	private StyledText installInfoLink;
 
 	private final IMarketplaceWebBrowser browser;
 
 	private StyledText tagsLink;
 
 	private static Boolean browserAvailable;
 
 	private ShareSolutionLink shareSolutionLink;
 
 	public DiscoveryItem(Composite parent, int style, DiscoveryResources resources, IShellProvider shellProvider,
 			IMarketplaceWebBrowser browser, final T connector, MarketplaceViewer viewer) {
 		super(parent, style, resources, connector);
 		this.shellProvider = shellProvider;
 		this.browser = browser;
 		this.connector = connector;
 		this.viewer = viewer;
 		connector.addPropertyChangeListener(this);
 		this.addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				connector.removePropertyChangeListener(DiscoveryItem.this);
 			}
 		});
 		createContent();
 	}
 
 	private void createContent() {
 		GridLayoutFactory.swtDefaults()
 		.numColumns(4)
 		.equalWidth(false)
 		.extendedMargins(0, 0, 2, 0)
 		.spacing(0, 0)
 		.applyTo(this);
 
 		new Label(this, SWT.NONE).setText(" "); // spacer //$NON-NLS-1$
 
 		createNameLabel(this);
 		createIconControl(this);
 
 		createDescription(this);
 
 		createProviderLabel(this);
 		createTagsLabel(this);
 
 		createSocialButtons(this);
 		createInstallInfo(this);
 
 		createInstallButtons(this);
 
 		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
 		GridDataFactory.fillDefaults()
 		.indent(0, SEPARATOR_MARGIN_TOP)
 		.grab(true, false)
 		.span(4, 1)
 		.align(SWT.FILL, SWT.BEGINNING)
 		.applyTo(separator);
 	}
 
 	private void createDescription(Composite parent) {
 		description = createStyledTextLabel(parent);
 		GridDataFactory.fillDefaults()
 		.grab(true, false)
 		.indent(DESCRIPTION_MARGIN_LEFT, DESCRIPTION_MARGIN_TOP)
 		.span(3, 1)
 		.hint(100, SWT.DEFAULT)
 		.applyTo(description);
 		String descriptionText = connector.getDescription();
 		int maxDescriptionLength = 162;
 		if (descriptionText == null) {
 			descriptionText = ""; //$NON-NLS-1$
 		} else {
 			descriptionText = TextUtil.stripHtmlMarkup(descriptionText).trim();
 		}
 		if (descriptionText.length() > maxDescriptionLength) {
 			int truncationIndex = maxDescriptionLength;
 			for (int x = truncationIndex; x > 0; --x) {
 				if (Character.isWhitespace(descriptionText.charAt(x))) {
 					truncationIndex = x;
 					break;
 				}
 			}
 			descriptionText = descriptionText.substring(0, truncationIndex)
 					+ Messages.DiscoveryItem_truncatedTextSuffix;
 		}
 		descriptionText = descriptionText.replaceAll("(\\r\\n)|\\n|\\r|\\s{2,}", " "); //$NON-NLS-1$ //$NON-NLS-2$
 		description.setText(descriptionText + "  "); //$NON-NLS-1$
 		if (descriptionText.startsWith(Messages.DiscoveryItem_Promotion_Marker)) {
 			description.replaceTextRange(0, Messages.DiscoveryItem_Promotion_Marker.length(),
 					Messages.DiscoveryItem_Promotion_Display + "  - "); //$NON-NLS-1$
 			StyleRange style = new StyleRange(0, Messages.DiscoveryItem_Promotion_Display.length(), null, null,
 					SWT.ITALIC | SWT.BOLD);
 			description.setStyleRange(style);
 		}
 
 		createInfoLink(description);
 	}
 
 	/**
 	 * Create a StyledText that acts much like a Label, i.e. isn't editable and doesn't get focus
 	 */
 	private StyledText createStyledTextLabel(Composite parent) {
 		StyledText styledText = new StyledText(parent, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
 		styledText.setEditable(false);
 		styledText.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
 		styledText.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				((StyledText) e.widget).getParent().setFocus();
 			}
 		});
 		return styledText;
 	}
 
 	private void createNameLabel(Composite parent) {
		nameLabel = new Label(parent, SWT.NONE);
 		GridDataFactory.fillDefaults()
 		.indent(DESCRIPTION_MARGIN_LEFT, 0)
 		.span(3, 1)
 		.grab(true, false)
 		.align(SWT.BEGINNING, SWT.CENTER)
 		.applyTo(nameLabel);
 		nameLabel.setFont(resources.getSmallHeaderFont());
 		nameLabel.setText(TextUtil.escapeText(connector.getName()));
 	}
 
 	private void createInstallButtons(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE); // prevent the button from changing the layout of the title
 		GridDataFactory.fillDefaults().indent(0, BUTTONBAR_MARGIN_TOP).align(SWT.TRAIL, SWT.FILL).applyTo(composite);
 
 		int numColumns = 1;
 		if (hasInstallMetadata()) {
 			Button button = new Button(composite, SWT.PUSH);
 			GridDataFactory.swtDefaults().align(SWT.TRAIL, SWT.CENTER)
 			.minSize(56, SWT.DEFAULT)
 			.grab(false, true)
 			.applyTo(button);
 
 			Button secondaryButton = null;
 			if (connector.isInstalled()) {
 				secondaryButton = new Button(composite, SWT.PUSH);
 				numColumns = 2;
 				GridDataFactory.swtDefaults().align(SWT.TRAIL, SWT.CENTER)
 				.minSize(56, SWT.DEFAULT)
 				.grab(false, true)
 				.applyTo(secondaryButton);
 			}
 
 			buttonController = new ItemButtonController(viewer, this, button, secondaryButton);
 		} else {
 			installInfoLink = createStyledTextLabel(composite);
 			installInfoLink.setToolTipText(Messages.DiscoveryItem_installInstructionsTooltip);
 			StyleRange link = appendLink(installInfoLink, Messages.DiscoveryItem_installInstructions, SWT.BOLD);
 			link.data = Messages.DiscoveryItem_installInstructions;
 			hookLinkListener(installInfoLink, new LinkListener() {
 				@Override
 				protected void selected(String href) {
 					browser.openUrl(((Node) connector.getData()).getUrl());
 				}
 			});
 			GridDataFactory.swtDefaults().align(SWT.TRAIL, SWT.CENTER).grab(false, true).applyTo(installInfoLink);
 		}
 		GridLayoutFactory.fillDefaults()
 		.numColumns(numColumns)
 		.margins(0, 0)
 		.extendedMargins(0, 5, 0, 0)
 		.spacing(5, 0)
 		.applyTo(composite);
 	}
 
 	private void createInstallInfo(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NULL); // prevent the button from changing the layout of the title
 		GridDataFactory.fillDefaults()
 		.indent(DESCRIPTION_MARGIN_LEFT, BUTTONBAR_MARGIN_TOP)
 		.grab(true, false)
 		.align(SWT.BEGINNING, SWT.CENTER)
 		.applyTo(composite);
 		RowLayoutFactory.fillDefaults().type(SWT.HORIZONTAL).pack(true).applyTo(composite);
 
 		Integer installsTotal = null;
 		Integer installsRecent = null;
 		if (connector.getData() instanceof Node) {
 			Node node = (Node) connector.getData();
 			installsTotal = node.getInstallsTotal();
 			installsRecent = node.getInstallsRecent();
 		}
 
 		if (installsTotal != null || installsRecent != null) {
 			StyledText installInfo = new StyledText(composite, SWT.READ_ONLY | SWT.SINGLE);
 			String totalText = installsTotal == null ? Messages.DiscoveryItem_Unknown_Installs : MessageFormat.format(
 					Messages.DiscoveryItem_Compact_Number, installsTotal.intValue(), installsTotal * 0.001,
 					installsTotal * 0.000001);
 			String recentText = installsRecent == null ? Messages.DiscoveryItem_Unknown_Installs
 					: MessageFormat.format("{0, number}", //$NON-NLS-1$
 							installsRecent.intValue());
 			String installInfoText = NLS.bind(Messages.DiscoveryItem_Installs, totalText, recentText);
 			int formatTotalsStart = installInfoText.indexOf(totalText);
 			if (formatTotalsStart == -1) {
 				installInfo.append(installInfoText);
 			} else {
 				if (formatTotalsStart > 0) {
 					installInfo.append(installInfoText.substring(0, formatTotalsStart));
 				}
 				appendStyled(installInfo, totalText, new StyleRange(0, 0, null, null, SWT.BOLD));
 				installInfo.append(installInfoText.substring(formatTotalsStart + totalText.length()));
 			}
 		} else {
 			if (shareSolutionLink != null) {
 				shareSolutionLink.setShowText(true);
 			}
 		}
 	}
 
 	private void createSocialButtons(Composite parent) {
 		Integer favorited = null;
 		if (connector.getData() instanceof Node) {
 			Node node = (Node) connector.getData();
 			favorited = node.getFavorited();
 		}
 		if (favorited == null || getCatalogItemUrl() == null) {
 			Label spacer = new Label(this, SWT.NONE);
 			spacer.setText(" ");//$NON-NLS-1$
 
 			GridDataFactory.fillDefaults().indent(0, BUTTONBAR_MARGIN_TOP).align(SWT.CENTER, SWT.FILL).applyTo(spacer);
 
 		} else {
 			createRatingsButton(parent, favorited);
 		}
 
 		if (getCatalogItemUrl() != null) {
 			shareSolutionLink = new ShareSolutionLink(parent, connector);
 			Control shareControl = shareSolutionLink.getControl();
 			GridDataFactory.fillDefaults()
 			.indent(DESCRIPTION_MARGIN_LEFT, BUTTONBAR_MARGIN_TOP)
 			.align(SWT.BEGINNING, SWT.FILL)
 			.applyTo(shareControl);
 		} else {
 			Label spacer = new Label(this, SWT.NONE);
 			spacer.setText(" ");//$NON-NLS-1$
 			GridDataFactory.fillDefaults().indent(0, BUTTONBAR_MARGIN_TOP).align(SWT.CENTER, SWT.FILL).applyTo(spacer);
 		}
 	}
 
 	private void createRatingsButton(Composite parent, Integer favoritedCount) {
 		final Button ratingsButton = new Button(parent, SWT.PUSH);
 		ratingsButton.setImage(MarketplaceClientUiPlugin.getInstance()
 				.getImageRegistry()
 				.get(MarketplaceClientUiPlugin.ITEM_ICON_STAR));
 		//Make width more or less fixed
 		int width = SWT.DEFAULT;
 		{
 			ratingsButton.setText("999"); //$NON-NLS-1$
 			Point pSize = ratingsButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 			width = pSize.x;
 		}
 		ratingsButton.setText(favoritedCount.toString());
 		Point pSize = ratingsButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 		width = Math.max(width, pSize.x);
 
 		final String ratingDescription = NLS.bind(Messages.DiscoveryItem_Favorited_Times, ratingsButton.getText());
 		ratingsButton.setToolTipText(ratingDescription);
 		ratingsButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {
 			@Override
 			public void getName(AccessibleEvent e) {
 				e.result = ratingDescription;
 			}
 		});
 		final RatingTooltip ratingTooltip = RatingTooltip.shouldShowRatingTooltip() ? new RatingTooltip(ratingsButton,
 				new Runnable() {
 			public void run() {
 				openSolutionFavorite();
 			}
 		}) : null;
 		hookTooltip(ratingTooltip, ratingsButton, ratingsButton);
 		ratingsButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (ratingTooltip != null && RatingTooltip.shouldShowRatingTooltip()) {
 					ratingTooltip.show();
 				} else {
 					openSolutionFavorite();
 				}
 			}
 		});
 
 		GridDataFactory.fillDefaults()
 		.indent(0, BUTTONBAR_MARGIN_TOP)
 		.hint(Math.min(width, MAX_IMAGE_WIDTH), SWT.DEFAULT)
 		.align(SWT.CENTER, SWT.FILL)
 		.applyTo(ratingsButton);
 	}
 
 	protected void openSolutionFavorite() {
 		String url = getCatalogItemUrl();
 		if (url == null) {
 			MarketplaceClientUi.error(
 					NLS.bind(Messages.DiscoveryItem_missingNodeUrl, connector.getId(), connector.getName()),
 					new IllegalStateException());
 			return;
 		}
 		if (url.startsWith(ECLIPSE_MARKETPLACE_URL)) {
 			//massage url to redirect via login page
 
 			url = NLS.bind(MARKETPLACE_LOGIN_URL, connector.getId());
 		}
 		WorkbenchUtil.openUrl(url, IWorkbenchBrowserSupport.AS_EXTERNAL);
 	}
 
 	private String getCatalogItemUrl() {
 		Object data = connector.getData();
 		if (data instanceof Node) {
 			Node node = (Node) data;
 			return node.getUrl();
 		}
 		return null;
 	}
 
 	private void createInfoLink(StyledText description) {
 		// bug 323257: don't display if there's no internal browser
 		boolean internalBrowserAvailable = computeBrowserAvailable(description);
 		if (internalBrowserAvailable && (hasTooltip(connector) || connector.isInstalled())) {
 			if (hasTooltip(connector)) {
 				String descriptionLink = Messages.DiscoveryItem_More_Info;
 				StyleRange linkRange = appendLink(description, descriptionLink, SWT.BOLD);
 				linkRange.data = INFO_HREF;
 				hookTooltip(description.getParent(), description, description, description, connector.getSource(),
 						connector.getOverview(), null);
 			}
 		} else if (!internalBrowserAvailable && hasOverviewUrl(connector)) {
 			String descriptionLink = Messages.DiscoveryItem_More_Info;
 			StyleRange linkRange = appendLink(description, descriptionLink, SWT.BOLD);
 			linkRange.data = INFO_HREF;
 			hookLinkListener(description, new LinkListener() {
 				@Override
 				protected void selected(String href) {
 					if (INFO_HREF.equals(href)) {
 						WorkbenchUtil.openUrl(connector.getOverview().getUrl().trim(),
 								IWorkbenchBrowserSupport.AS_EXTERNAL);
 					}
 				}
 			});
 		}
 	}
 
 	private void createIconControl(Composite parent) {
 		checkboxContainer = new Composite(parent, SWT.NONE);
 		GridDataFactory.swtDefaults()
 		.indent(0, DESCRIPTION_MARGIN_TOP)
 		.align(SWT.CENTER, SWT.BEGINNING)
 		.hint(MAX_IMAGE_WIDTH, SWT.DEFAULT)
 		.grab(false, true)
 		.minSize(MAX_IMAGE_WIDTH, MIN_IMAGE_HEIGHT)
 		.span(1, 3)
 		.applyTo(checkboxContainer);
 		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(checkboxContainer);
 
 		iconLabel = new Label(checkboxContainer, SWT.NONE);
 		GridDataFactory.swtDefaults()
 		.align(SWT.CENTER, SWT.BEGINNING).grab(true, true)
 		.applyTo(iconLabel);
 		if (connector.getIcon() != null) {
 			try {
 				Image image = resources.getIconImage(connector.getSource(), connector.getIcon(), 32, false);
 				Rectangle bounds = image.getBounds();
 				if (bounds.width < 0.8 * MAX_IMAGE_WIDTH || bounds.width > MAX_IMAGE_WIDTH
 						|| bounds.height > MAX_IMAGE_HEIGHT) {
 					final Image scaledImage = Util.scaleImage(image, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
 					image = scaledImage;
 					iconLabel.addDisposeListener(new DisposeListener() {
 						public void widgetDisposed(DisposeEvent e) {
 							scaledImage.dispose();
 						}
 					});
 				}
 				iconLabel.setImage(image);
 			} catch (SWTException e) {
 				// ignore, probably a bad image format
 //				MarketplaceClientUi.error(NLS.bind(Messages.DiscoveryItem_cannotRenderImage_reason, connector.getIcon()
 //						.getImage32(), e.getMessage()), e);
 			}
 		}
 		if (iconLabel.getImage() == null) {
 			iconLabel.setImage(MarketplaceClientUiPlugin.getInstance()
 					.getImageRegistry()
 					.get(MarketplaceClientUiPlugin.NO_ICON_PROVIDED));
 		}
 	}
 
 	private StyleRange appendLink(StyledText styledText, String text, int style) {
 		StyleRange range = new StyleRange(0, 0, styledText.getForeground(), null, style);
 		range.underline = true;
 		range.underlineStyle = SWT.UNDERLINE_LINK;
 
 		appendStyled(styledText, text, range);
 
 		return range;
 	}
 
 	private void appendStyled(StyledText styledText, String text, StyleRange style) {
 		style.start = styledText.getCharCount();
 		style.length = text.length();
 
 		styledText.append(text);
 		styledText.setStyleRange(style);
 	}
 
 	private boolean hasOverviewUrl(CatalogItem connector) {
 		return connector.getOverview() != null && connector.getOverview().getUrl() != null
 				&& connector.getOverview().getUrl().trim().length() > 0;
 	}
 
 	private synchronized boolean computeBrowserAvailable(Composite composite) {
 		if (browserAvailable == null) {
 			// SWT Snippet148: detect if a browser is available by attempting to create one
 			// SWTError is thrown if not available.
 			try {
 				Browser browser = new Browser(composite, SWT.NULL);
 				browser.dispose();
 				browserAvailable = true;
 			} catch (SWTError e) {
 				browserAvailable = false;
 			}
 		}
 		return browserAvailable;
 	}
 
 	private boolean hasInstallMetadata() {
 		return !connector.getInstallableUnits().isEmpty() && connector.getSiteUrl() != null;
 	}
 
 	protected void createProviderLabel(Composite parent) {
 		Link providerLabel = new Link(parent, SWT.NONE);
 		GridDataFactory.fillDefaults()
 		.indent(DESCRIPTION_MARGIN_LEFT, DESCRIPTION_MARGIN_TOP)
 		.span(3, 1)
 		.align(SWT.BEGINNING, SWT.CENTER)
 		.grab(true, false)
 		.applyTo(providerLabel);
 		// always disabled color to make it less prominent
 		providerLabel.setForeground(resources.getColorDisabled());
 
 		providerLabel.setText(NLS.bind(Messages.DiscoveryItem_byProviderLicense, connector.getProvider(),
 				connector.getLicense()));
 	}
 
 	protected void createTagsLabel(Composite parent) {
 		tagsLink = createStyledTextLabel(parent);
 		tagsLink.setEditable(false);
 		GridDataFactory.fillDefaults()
 		.indent(DESCRIPTION_MARGIN_LEFT, TAGS_MARGIN_TOP)
 		.span(3, 1)
 		.align(SWT.BEGINNING, SWT.BEGINNING)
 		.grab(true, false)
 		.applyTo(tagsLink);
 
 		Tags tags = ((Node) connector.getData()).getTags();
 		if (tags == null) {
 			return;
 		}
 		for (Tag tag : tags.getTags()) {
 			String tagName = tag.getName();
 			appendLink(tagsLink, tagName, SWT.NORMAL).data = tagName;
 			tagsLink.append(" "); //$NON-NLS-1$
 		}
 		hookLinkListener(tagsLink, new LinkListener() {
 			@Override
 			protected void selected(String href) {
 				viewer.doQueryForTag(href);
 			}
 		});
 	}
 
 	private void hookLinkListener(StyledText link, LinkListener listener) {
 		link.addSelectionListener(listener);
 		link.addMouseListener(listener);
 	}
 
 	protected boolean hasTooltip(CatalogItem connector) {
 		return connector.getOverview() != null && connector.getOverview().getSummary() != null
 				&& connector.getOverview().getSummary().length() > 0;
 	}
 
 	protected boolean maybeModifySelection(Operation operation) {
 		viewer.modifySelection(connector, operation);
 		return true;
 	}
 
 	@Override
 	public boolean isSelected() {
 		return getData().isSelected();
 	}
 
 	public Operation getOperation() {
 		return viewer.getSelectionModel().getOperation(getData());
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (!isDisposed()) {
 			getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					if (!isDisposed()) {
 						refresh();
 					}
 				}
 			});
 		}
 	}
 
 	@Override
 	protected void refresh() {
 		Color foreground = getForeground();
 
 		nameLabel.setForeground(foreground);
 		description.setForeground(foreground);
 		if (installInfoLink != null) {
 			installInfoLink.setForeground(foreground);
 		}
 		if (buttonController != null) {
 			buttonController.refresh();
 		}
 	}
 
 	private void hookRecursively(Control control, Listener listener) {
 		control.addListener(SWT.Dispose, listener);
 		control.addListener(SWT.MouseHover, listener);
 		control.addListener(SWT.MouseMove, listener);
 		control.addListener(SWT.MouseExit, listener);
 		control.addListener(SWT.MouseDown, listener);
 		control.addListener(SWT.MouseWheel, listener);
 		if (control instanceof Composite) {
 			for (Control child : ((Composite) control).getChildren()) {
 				hookRecursively(child, listener);
 			}
 		}
 	}
 
 	@Override
 	protected void hookTooltip(final Control parent, final Widget tipActivator, final Control exitControl,
 			final Control titleControl, AbstractCatalogSource source, Overview overview, Image image) {
 		final OverviewToolTip toolTip = new OverviewToolTip(parent, browser, source, overview, image);
 		hookTooltip(toolTip, tipActivator, exitControl);
 
 		if (image != null) {
 			Listener listener = new Listener() {
 				public void handleEvent(Event event) {
 					toolTip.show(titleControl);
 				}
 			};
 			tipActivator.addListener(SWT.MouseHover, listener);
 		}
 
 		if (tipActivator instanceof StyledText) {
 			StyledText link = (StyledText) tipActivator;
 
 			hookLinkListener(link, new LinkListener() {
 				@Override
 				protected void selected(String href) {
 					toolTip.show(titleControl);
 				}
 			});
 		} else {
 			Listener selectionListener = new Listener() {
 				public void handleEvent(Event event) {
 					toolTip.show(titleControl);
 				}
 			};
 			tipActivator.addListener(SWT.Selection, selectionListener);
 		}
 	}
 
 	private void hookTooltip(final ToolTip toolTip, Widget tipActivator, final Control exitControl) {
 		if (toolTip == null) {
 			return;
 		}
 		Listener listener = new Listener() {
 			public void handleEvent(Event event) {
 				toolTip.hide();
 			}
 		};
 		tipActivator.addListener(SWT.Dispose, listener);
 		tipActivator.addListener(SWT.MouseWheel, listener);
 		Listener exitListener = new Listener() {
 			public void handleEvent(Event event) {
 				switch (event.type) {
 				case SWT.MouseWheel:
 					toolTip.hide();
 					break;
 				case SWT.MouseExit:
 					/*
 					 * Check if the mouse exit happened because we move over the tooltip
 					 */
 					Rectangle containerBounds = exitControl.getBounds();
 					Point displayLocation = exitControl.getParent().toDisplay(containerBounds.x, containerBounds.y);
 					containerBounds.x = displayLocation.x;
 					containerBounds.y = displayLocation.y;
 					//be a bit relaxed about this - there's a small gap between control and tooltip
 					containerBounds.height += 3;
 					Point cursorLocation = Display.getCurrent().getCursorLocation();
 					if (containerBounds.contains(cursorLocation)) {
 						break;
 					}
 					Shell tipShell = (Shell) toolTip.getData(Shell.class.getName());
 					if (tipShell != null) {
 						Rectangle tipBounds = tipShell.getBounds();
 						if (tipBounds.contains(cursorLocation)) {
 							break;
 						}
 					}
 					toolTip.hide();
 					break;
 				}
 			}
 		};
 		hookRecursively(exitControl, exitListener);
 	}
 }
