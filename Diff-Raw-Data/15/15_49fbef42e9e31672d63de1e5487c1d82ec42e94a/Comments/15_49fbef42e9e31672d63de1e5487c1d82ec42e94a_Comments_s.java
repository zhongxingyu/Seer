 /* This file is part of SoftwareFm
 /* SoftwareFm is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
 /* SoftwareFm is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
 /* You should have received a copy of the GNU General Public License along with SoftwareFm. If not, see <http://www.gnu.org/licenses/> */
 
 package org.softwareFm.collections.comments;
 
 import java.text.MessageFormat;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.softwareFm.card.card.ICardData;
 import org.softwareFm.card.card.composites.CompositeWithCardMargin;
 import org.softwareFm.card.card.composites.CompositeWithEditorIndent;
 import org.softwareFm.card.card.composites.OutlinePaintListener;
 import org.softwareFm.card.configuration.CardConfig;
 import org.softwareFm.card.title.Title;
 import org.softwareFm.card.title.TitleSpec;
 import org.softwareFm.collections.constants.CollectionConstants;
 import org.softwareFm.display.composites.IHasControl;
 import org.softwareFm.display.swt.Swts;
 import org.softwareFm.utilities.comparators.Comparators;
 import org.softwareFm.utilities.functions.Functions;
 import org.softwareFm.utilities.maps.Maps;
 import org.softwareFm.utilities.resources.IResourceGetter;
 import org.softwareFm.utilities.strings.Strings;
 
 public class Comments implements IHasControl {
 
 	static class CommentsComposite extends CompositeWithCardMargin {
 
 		private final Map<String, Object> map = Maps.newMap();
 		private final Table table;
 		private final TableColumn titleColumn;
 		private final TableColumn textColumn;
 		private final CardConfig cc;
 		private final Composite body;
 		private final Title title;
 		private String cardType;
 
 		public CommentsComposite(Composite parent, CardConfig cardConfig, final ICommentsCallback callback, Runnable addButton) {
 			super(parent, SWT.NULL, cardConfig);
 			this.cc = cardConfig;
 			Composite header = Swts.newComposite(this, SWT.NULL, "header");
			header.setLayout(Swts.titleAndRhsLayout(0));
 			title = new Title(header, cardConfig, TitleSpec.noTitleSpec(getBackground()), "Comments", "");
			Button addCommentButton = Swts.Buttons.makePushButton(header, Functions.call(cardConfig.resourceGetterFn, null), CollectionConstants.addCommentButtonTitle, addButton);
 			String imageName = IResourceGetter.Utils.getOrException(cardConfig.resourceGetterFn, null, CollectionConstants.addCommentButtonImage);
 			Image addCommentImage = Functions.call(cardConfig.imageFn, imageName);
			addCommentButton.setImage(addCommentImage);
 			this.body = new CompositeWithEditorIndent(this, SWT.NULL, cardConfig);
 			this.addPaintListener(new OutlinePaintListener(cardConfig));
 			body.setLayout(new FillLayout());
 			this.table = new Table(body, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
 			table.setLinesVisible(true);
 			this.titleColumn = new TableColumn(table, SWT.NONE);
 			this.textColumn = new TableColumn(table, SWT.NONE);
 			table.addListener(SWT.Selection, new Listener() {
 				@SuppressWarnings("unchecked")
 				@Override
 				public void handleEvent(Event event) {
 					int index = table.getSelectionIndex();
 					if (index != -1) {
 						Object object = map.get(table.getItem(index).getData());
 						if (object instanceof Map<?, ?>) {
 							Map<String, Object> entryMap = (Map<String, Object>) object;
 							String title = Strings.nullSafeToString(entryMap.get("title"));
 							String text = Strings.nullSafeToString(entryMap.get("text"));
 							callback.selected(cardType, title, text);
 						}
 					}
 				}
 			});
 		}
 
 		public void setInitialComments(String titleText, ICardData cardData, Map<String, Object> map) {
 			this.cardType = cardData.cardType();
 			this.map.clear();
 			this.map.putAll(map);
 			TitleSpec titleSpec = Functions.call(cc.titleSpecFn, cardData);
 			title.setTitleAndImage(titleText, "", titleSpec);
 			body.setBackground(titleSpec.titleColor);
 			updateDisplay();
 		}
 
 		@SuppressWarnings("unchecked")
 		private void updateDisplay() {
 			table.removeAll();
 			Map<String, Object> sorted = Maps.sortByKey(map, Comparators.invert(Comparators.compareBasedOnTagInValue(map, CollectionConstants.createdTimeKey)));
 			for (Entry<String, Object> entry : sorted.entrySet()) {
 				if (entry.getValue() instanceof Map<?, ?>) {
 					Map<String, Object> entryMap = (Map<String, Object>) entry.getValue();
 					Object title = entryMap.get("title");
 					Object text = entryMap.get("text");
 					if (title != null) {
 						TableItem item = new TableItem(table, SWT.NULL);
 						item.setData(entry.getKey());
 						item.setText(0, Strings.nullSafeToString(title));
 						item.setText(1, Strings.nullSafeToString(text));
 					}
 				}
 			}
 			titleColumn.pack();
 			textColumn.pack();
 		}
 	}
 
 	private final CommentsComposite content;
 
 	public Comments(Composite parent, CardConfig cardConfig, ICommentsCallback callback, Runnable addComment) {
 		content = new CommentsComposite(parent, cardConfig, callback, addComment);
 		content.setLayout(Swts.titleAndContentLayout(cardConfig.titleHeight));
 	}
 
 	@SuppressWarnings("unchecked")
 	public void showCommentsFor(ICardData cardData) {
 		CardConfig cardConfig = cardData.getCardConfig();
 		Object commentObject = cardData.data().get(CollectionConstants.comment);
 		if (commentObject instanceof Map<?, ?>)
 			setComment(cardData, cardConfig, CollectionConstants.commentsTitle, (Map<String, Object>) commentObject);
 		else
 			setComment(cardData, cardConfig, CollectionConstants.commentsNoTitle, Maps.stringObjectMap());
 	}
 
 	private void setComment(ICardData cardData, CardConfig cardConfig, String titleKey, Map<String, Object> map) {
 		String pattern = IResourceGetter.Utils.getOrException(cardConfig.resourceGetterFn, cardData.cardType(), titleKey);
 		String title = MessageFormat.format(pattern, cardData.url(), Strings.lastSegment(cardData.url(), "/"), cardData.cardType());
 		content.setInitialComments(title, cardData, map);
 	}
 
 	@Override
 	public Control getControl() {
 		return content;
 	}
 
 	public void selectComment(String key) {
 		Table table = content.table;
 		for (int i = 0; i < table.getItemCount(); i++) {
 			TableItem item = content.table.getItem(i);
 			if (key.equals(item.getData())) {
 				table.select(i);
 				table.showItem(item);
 				table.notifyListeners(SWT.Selection, new Event());
 				return;
 			}
 		}
 
 	}
 
 	public Table getTable() {
 		return content.table;
 	}
 
 	public Title getTitle() {
 		return content.title;
 	}
 }
