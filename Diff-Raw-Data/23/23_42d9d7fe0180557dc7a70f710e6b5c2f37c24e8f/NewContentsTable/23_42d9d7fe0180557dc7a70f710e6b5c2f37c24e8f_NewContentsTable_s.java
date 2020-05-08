 package org.myftp.gattserver.grass3.windows.template;
 
 import java.util.List;
 
 import org.myftp.gattserver.grass3.ServiceHolder;
 import org.myftp.gattserver.grass3.model.dto.NodeDTO;
 import org.myftp.gattserver.grass3.service.IContentService;
 import org.myftp.gattserver.grass3.util.ComparableLink;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.Table;
 
 public class NewContentsTable extends Table {
 
 	private static final long serialVersionUID = -2220485504407844582L;
 
 	/**
 	 * Přehled sloupců tabulky nových obsahů
 	 */
 	private enum ColumnId {
 
 		IKONA, NÁZEV
 
 	}
 
 	public void populateTable(NodeDTO node, GrassWindow window) {
 
 		IndexedContainer container = new IndexedContainer();
 		container.addContainerProperty(ColumnId.IKONA, Embedded.class, null);
 		container.addContainerProperty(ColumnId.NÁZEV, ComparableLink.class,
 				null);
 		setContainerDataSource(container);
 		setColumnWidth(ColumnId.IKONA, 16);
 		setColumnHeader(ColumnId.IKONA, "");
 
 		// jaké služby obsahů mám k dispozici ?
 		List<IContentService> contentServices = ServiceHolder.getInstance()
 				.getContentServices();
 
 		// položky
 		for (IContentService contentService : contentServices) {
 
 			Item item = addItem(contentService);
			item.getItemProperty(ColumnId.NÁZEV)
					.setValue(
							new ComparableLink(
									contentService.getCreateNewContentLabel(),
									new ExternalResource(
											window.getWindow(
													contentService
															.getContentEditorWindowClass())
													.getURL()
													+ "/"
													+ node.getId()
													+ "-"
													+ node.getName())));
 
 			Embedded icon = new Embedded();
 			icon.setSource(contentService.getContentIcon());
 			item.getItemProperty(ColumnId.IKONA).setValue(icon);
 		}
 
 	}
 }
