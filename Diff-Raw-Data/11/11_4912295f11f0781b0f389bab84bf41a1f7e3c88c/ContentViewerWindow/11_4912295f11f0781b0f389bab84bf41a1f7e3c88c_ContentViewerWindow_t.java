 package org.myftp.gattserver.grass3.windows.template;
 
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.myftp.gattserver.grass3.model.dto.ContentNodeDTO;
 import org.myftp.gattserver.grass3.model.dto.NodeDTO;
 import org.myftp.gattserver.grass3.template.Breadcrumb;
 import org.myftp.gattserver.grass3.template.Breadcrumb.BreadcrumbElement;
 import org.myftp.gattserver.grass3.util.URLIdentifierUtils;
 import org.myftp.gattserver.grass3.windows.CategoryWindow;
 import org.myftp.gattserver.grass3.windows.TagWindow;
 import org.myftp.gattserver.grass3.windows.template.TwoColumnWindow;
 
 import com.vaadin.terminal.DownloadStream;
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Link;
 import com.vaadin.ui.VerticalLayout;
 
 public abstract class ContentViewerWindow extends TwoColumnWindow {
 
 	private static final long serialVersionUID = 5078280973817331002L;
 
 	private ContentNodeDTO content;
 	private Class<? extends GrassWindow> contentViewerClass;
 	private Label contentNameLabel;
 	private Label contentAuthorNameLabel;
 	private Label contentCreationDateNameLabel;
 	private Label contentLastModificationDateLabel;
 	private HorizontalLayout tagsListLayout;
 	private CssLayout operationsListLayout;
 
 	public ContentViewerWindow(Class<? extends GrassWindow> contentViewerClass) {
 		this.contentViewerClass = contentViewerClass;
 	}
 
 	/**
 	 * Breadcrumb
 	 */
 	private Breadcrumb breadcrumb;
 
 	@Override
 	protected void createLeftColumnContent(VerticalLayout layout) {
 
 		layout.setMargin(true);
 
 		// info - přehled
 		VerticalLayout infoLayout = new VerticalLayout();
 		infoLayout.setMargin(false, false, true, false);
 		layout.addComponent(infoLayout);
 		infoLayout
 				.addComponent(new Label("<h2>Info</h2>", Label.CONTENT_XHTML));
 
 		GridLayout gridLayout = new GridLayout(2, 3);
 		infoLayout.addComponent(gridLayout);
 
 		gridLayout.setSpacing(true);
 
 		gridLayout.addComponent(new Label("<strong>Autor:</strong>",
 				Label.CONTENT_XHTML), 0, 0);
 		gridLayout.addComponent(contentAuthorNameLabel = new Label(), 1, 0);
 		gridLayout.addComponent(new Label("<strong>Vytvořeno:</strong>",
 				Label.CONTENT_XHTML), 0, 1);
 		gridLayout.addComponent(contentCreationDateNameLabel = new Label(), 1,
 				1);
 		gridLayout.addComponent(new Label("<strong>Upraveno:</strong>",
 				Label.CONTENT_XHTML), 0, 2);
 		gridLayout.addComponent(contentLastModificationDateLabel = new Label(),
 				1, 2);
 
 		gridLayout.setComponentAlignment(contentAuthorNameLabel,
 				Alignment.TOP_RIGHT);
 		gridLayout.setComponentAlignment(contentCreationDateNameLabel,
 				Alignment.TOP_RIGHT);
 		gridLayout.setComponentAlignment(contentLastModificationDateLabel,
 				Alignment.TOP_RIGHT);
 
 		contentLastModificationDateLabel.setContentMode(Label.CONTENT_XHTML);
 
 		contentAuthorNameLabel.setSizeUndefined();
 		contentCreationDateNameLabel.setSizeUndefined();
 		contentLastModificationDateLabel.setSizeUndefined();
 
 		// tagy
 		VerticalLayout tagsLayout = new VerticalLayout();
 		tagsLayout.setMargin(false, false, true, false);
 		layout.addComponent(tagsLayout);
 		tagsLayout
 				.addComponent(new Label("<h2>Tagy</h2>", Label.CONTENT_XHTML));
 
 		tagsListLayout = new HorizontalLayout();
 		tagsLayout.addComponent(tagsListLayout);
 		tagsListLayout.setSpacing(true);
 
 		// nástrojová lišta
 		VerticalLayout operationsLayout = new VerticalLayout();
 		layout.addComponent(operationsLayout);
 		operationsLayout.addComponent(new Label("<h2>Operace s obsahem</h2>",
 				Label.CONTENT_XHTML));
 
 		operationsListLayout = new CssLayout();
 		operationsLayout.addComponent(operationsListLayout);
 		operationsListLayout.addStyleName("tools_css_menu");
 
 	}
 
 	@Override
 	protected void createRightColumnContent(VerticalLayout layout) {
 
 		layout.setMargin(true);
 		layout.setSpacing(true);
 
 		layout.addComponent(breadcrumb = new Breadcrumb());
 
 		// Název obsahu
 		VerticalLayout nameLayout = new VerticalLayout();
 		layout.addComponent(nameLayout);
 		nameLayout.addComponent(contentNameLabel = new Label());
 		contentNameLabel.setContentMode(Label.CONTENT_XHTML);
 
 		// samotný obsah
 		createContent(layout);
 
 	}
 
 	protected abstract void createContent(VerticalLayout layout);
 
 	@Override
 	public DownloadStream handleURI(URL context, String relativeUri) {
 
 		content = getContentNodeDTO();
 
 		updateBreadcrumb(content);
 
 		return super.handleURI(context, relativeUri);
 	}
 
 	protected abstract ContentNodeDTO getContentNodeDTO();
 
 	private void updateBreadcrumb(ContentNodeDTO content) {
 
 		// pokud zjistím, že cesta neodpovídá, vyhodím 302 (přesměrování) na
 		// aktuální polohu cílové kategorie
 		List<BreadcrumbElement> breadcrumbElements = new ArrayList<BreadcrumbElement>();
 
 		/**
 		 * obsah
 		 */
 		breadcrumbElements.add(new BreadcrumbElement(content.getName(),
 				new ExternalResource(getWindow(contentViewerClass).getURL()
 						+ URLIdentifierUtils.createURLIdentifier(
								content.getContentID(), content.getName()))));
 
 		/**
 		 * kategorie
 		 */
 		NodeDTO parent = nodeFacade.getNodeById(content.getParentID());
 		while (true) {
 
 			// nejprve zkus zjistit, zda předek existuje
 			if (parent == null)
 				showError404();
 
 			breadcrumbElements.add(new BreadcrumbElement(parent.getName(),
 					new ExternalResource(getWindow(CategoryWindow.class)
 							.getURL()
 							+ URLIdentifierUtils.createURLIdentifier(
 									parent.getId(), parent.getName()))));
 
 			// pokud je můj předek null, pak je to konec a je to všechno
 			if (parent.getParentID() == null)
 				break;
 
 			parent = nodeFacade.getNodeById(parent.getParentID());
 		}
 
 		breadcrumb.resetBreadcrumb(breadcrumbElements);
 	}
 
 	@Override
 	protected void onShow() {
 		super.onShow();
 
 		SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy HH:mm:ss");
 
 		contentNameLabel.setValue("<h2>" + content.getName() + "</h2>");
 		contentAuthorNameLabel.setValue(content.getAuthor().getName());
 		contentCreationDateNameLabel.setValue(dateFormat.format(content
 				.getCreationDate()));
 		contentLastModificationDateLabel.setValue(content
 				.getLastModificationDate() == null ? "<em>-neupraveno-</em>"
 				: dateFormat.format(content.getLastModificationDate()));
 
 		tagsListLayout.removeAllComponents();
 		for (String contentTag : content.getContentTags()) {
 			Link tagLink = new Link(contentTag, new ExternalResource(getWindow(
 					TagWindow.class).getURL()
 					+ contentTag));
 			tagLink.addStyleName("content_tag");
 			tagsListLayout.addComponent(tagLink);
 		}
 
 		operationsListLayout.removeAllComponents();
 		updateOperationsList(operationsListLayout);
 
 	}
 
 	protected abstract void updateOperationsList(
 			CssLayout operationsListLayout);
 }
