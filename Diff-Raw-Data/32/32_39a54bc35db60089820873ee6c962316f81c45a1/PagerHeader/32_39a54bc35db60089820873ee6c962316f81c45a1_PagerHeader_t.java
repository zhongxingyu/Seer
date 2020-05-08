 package ch.cern.atlas.apvs.client.widget;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.cell.client.ActionCell.Delegate;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.CompositeCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.HasCell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.resources.client.ImageResource.ImageOptions;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.AbstractPager;
 import com.google.gwt.user.cellview.client.Header;
 import com.google.gwt.user.client.ui.ImageResourceRenderer;
 import com.google.gwt.view.client.HasRows;
 import com.google.gwt.view.client.Range;
 
 public class PagerHeader extends AbstractPager {
 
 	private Header<String> header;
 	private static int DEFAULT_FAST_FORWARD_ROWS = 1000;
 	private static Resources DEFAULT_RESOURCES;
 
 	private final ImageButton fastForward;
 	private final int fastForwardRows;
 	private final ImageButton firstPage;
 	private final LabelCell label;
 	private final ImageButton lastPage;
 	private final ImageButton nextPage;
 	private final ImageButton prevPage;
 	private final Style style;
 
 	public static enum TextLocation {
 		CENTER, LEFT, RIGHT;
 	}
 
 	public static interface Resources extends ClientBundle {
 		@ImageOptions(flipRtl = true)
 		ImageResource pagerHeaderFastForward();
 
 		@ImageOptions(flipRtl = true)
 		ImageResource pagerHeaderFastForwardDisabled();
 
 		@ImageOptions(flipRtl = true)
 		ImageResource pagerHeaderFirstPage();
 
 		@ImageOptions(flipRtl = true)
 		ImageResource pagerHeaderFirstPageDisabled();
 
 		@ImageOptions(flipRtl = true)
 		ImageResource pagerHeaderLastPage();
 
 		@ImageOptions(flipRtl = true)
 		ImageResource pagerHeaderLastPageDisabled();
 
 		@ImageOptions(flipRtl = true)
 		ImageResource pagerHeaderNextPage();
 
 		@ImageOptions(flipRtl = true)
 		ImageResource pagerHeaderNextPageDisabled();
 
 		@ImageOptions(flipRtl = true)
 		ImageResource pagerHeaderPreviousPage();
 
 		@ImageOptions(flipRtl = true)
 		ImageResource pagerHeaderPreviousPageDisabled();
 
 		@Source("PagerHeader.css")
 		Style style();
 	}
 
 	public static interface Style extends CssResource {
 
 		/**
 		 * Applied to buttons.
 		 */
 		String button();
 
 		/**
 		 * Applied to disabled buttons.
 		 */
 		String disabledButton();
 
 		/**
 		 * Applied to the details text.
 		 */
 		String pageDetails();
 	}
 
 	public static Resources getDefaultResources() {
 		if (DEFAULT_RESOURCES == null) {
 			DEFAULT_RESOURCES = GWT.create(Resources.class);
 		}
 		return DEFAULT_RESOURCES;
 	}
 
 	public PagerHeader() {
 		this(TextLocation.CENTER);
 	}
 
 	public PagerHeader(TextLocation location) {
 		this(location, getDefaultResources(), true, DEFAULT_FAST_FORWARD_ROWS,
 				true);
 	}
 
 	public PagerHeader(TextLocation location, Resources resources,
 			boolean showFastForwardButton, final int fastForwardRows,
 			boolean showLastPageButton) {
 		this.fastForwardRows = fastForwardRows;
 		this.style = resources.style();
 		this.style.ensureInjected();
 
 		// Create the buttons.
 		firstPage = new ImageButton("first", resources.pagerHeaderFirstPage(),
 				resources.pagerHeaderFirstPageDisabled(),
 				new Delegate<ImageResource>() {
 					@Override
 					public void execute(ImageResource object) {
 						firstPage();
 					}
 				});
 		nextPage = new ImageButton("next", resources.pagerHeaderNextPage(),
 				resources.pagerHeaderNextPageDisabled(),
 				new Delegate<ImageResource>() {
 					@Override
 					public void execute(ImageResource object) {
 						nextPage();
 					}
 				});
 		prevPage = new ImageButton("previous",
 				resources.pagerHeaderPreviousPage(),
 				resources.pagerHeaderPreviousPageDisabled(),
 				new Delegate<ImageResource>() {
 					@Override
 					public void execute(ImageResource object) {
 						previousPage();
 					}
 				});
 		lastPage = new ImageButton("last", resources.pagerHeaderLastPage(),
 				resources.pagerHeaderLastPageDisabled(),
 				new Delegate<ImageResource>() {
 					@Override
 					public void execute(ImageResource object) {
 						lastPage();
 					}
 				});
 		fastForward = new ImageButton("forward",
 				resources.pagerHeaderFastForward(),
 				resources.pagerHeaderFastForwardDisabled(),
 				new Delegate<ImageResource>() {
 					@Override
 					public void execute(ImageResource object) {
 						setPage(getPage() + getFastForwardPages());
 					}
 				});
 
 		label = new LabelCell();
 
 		// Construct the widget.
 		final List<HasCell<String, ?>> cells = new ArrayList<HasCell<String, ?>>();
 		// layout.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 
 		if (location == TextLocation.RIGHT) {
 			cells.add(label);
 		}
 		cells.add(firstPage);
 		cells.add(prevPage);
 		if (location == TextLocation.CENTER) {
 			cells.add(label);
 		}
 		cells.add(nextPage);
 		if (showFastForwardButton) {
 			cells.add(fastForward);
 		}
 		if (showLastPageButton) {
 			cells.add(lastPage);
 		}
 		if (location == TextLocation.LEFT) {
 			cells.add(label);
 		}
 
 		// Disable the buttons by default.
 		setDisplay(null);
 
 		header = new Header<String>(new CompositeCell<String>(cells)) {
 
 			@Override
 			public String getValue() {
 				return "Composite";
 			}
 		};
 	}
 
 	public Header<String> getHeader() {
 		return header;
 	}
 
 	@Override
 	public void firstPage() {
 		super.firstPage();
 	}
 
 	@Override
 	public int getPage() {
 		return super.getPage();
 	}
 
 	@Override
 	public int getPageCount() {
 		return super.getPageCount();
 	}
 
 	@Override
 	public boolean hasNextPage() {
 		return super.hasNextPage();
 	}
 
 	@Override
 	public boolean hasNextPages(int pages) {
 		return super.hasNextPages(pages);
 	}
 
 	@Override
 	public boolean hasPage(int index) {
 		return super.hasPage(index);
 	}
 
 	@Override
 	public boolean hasPreviousPage() {
 		return super.hasPreviousPage();
 	}
 
 	@Override
 	public boolean hasPreviousPages(int pages) {
 		return super.hasPreviousPages(pages);
 	}
 
 	@Override
 	public void lastPage() {
 		super.lastPage();
 	}
 
 	@Override
 	public void lastPageStart() {
 		super.lastPageStart();
 	}
 
 	@Override
 	public void nextPage() {
 		super.nextPage();
 	}
 
 	@Override
 	public void previousPage() {
 		super.previousPage();
 	}
 
 	@Override
 	public void setDisplay(HasRows display) {
 		// Enable or disable all buttons.
 		boolean disableButtons = (display == null);
 		setFastForwardDisabled(disableButtons);
 		setNextPageButtonsDisabled(disableButtons);
 		setPrevPageButtonsDisabled(disableButtons);
 		super.setDisplay(display);
 	}
 
 	@Override
 	public void setPage(int index) {
		if (index < 0) {
			index = 0;
		}
 		super.setPage(index);
 	}
 
 	@Override
 	public void setPageSize(int pageSize) {
 		super.setPageSize(pageSize);
 	}
 
 	@Override
 	public void setPageStart(int index) {
 		super.setPageStart(index);
 	}
 
 	public void startLoading() {
 		getDisplay().setRowCount(0, true);
 		label.setText("");
 	}
 
 	protected String createText() {
 		// Default text is 1 based.
 		NumberFormat formatter = NumberFormat.getFormat("######");
 		HasRows display = getDisplay();
 		Range range = display.getVisibleRange();
 		int pageStart = range.getStart() + 1;
 		int pageSize = range.getLength();
 		int dataSize = display.getRowCount();
 		int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
 		endIndex = Math.max(pageStart, endIndex);
 		boolean exact = display.isRowCountExact();
 		return formatter.format(pageStart) + "-" + formatter.format(endIndex)
 				+ (exact ? " of " : " of over ") + formatter.format(dataSize);
 	}
 
 	@Override
 	protected void onRangeOrRowCountChanged() {
 		HasRows display = getDisplay();
 		label.setText(createText());
 
 		// Update the prev and first buttons.
 		setPrevPageButtonsDisabled(!hasPreviousPage());
 
 		// Update the next and last buttons.
 		if (isRangeLimited() || !display.isRowCountExact()) {
 			setNextPageButtonsDisabled(!hasNextPage());
 			setFastForwardDisabled(!hasNextPages(getFastForwardPages()));
 		}
 	}
 
 	private int getFastForwardPages() {
 		int pageSize = getPageSize();
 		return pageSize > 0 ? fastForwardRows / pageSize : 0;
 	}
 
 	private void setFastForwardDisabled(boolean disabled) {
 		fastForward.setDisabled(disabled);
 	}
 
	public void setNextPageButtonsDisabled(boolean disabled) {
 		nextPage.setDisabled(disabled);
 		lastPage.setDisabled(disabled);
 	}
 
 	private void setPrevPageButtonsDisabled(boolean disabled) {
 		firstPage.setDisabled(disabled);
 		prevPage.setDisabled(disabled);
 	}
 
 	private class LabelCell implements HasCell<String, String> {
 
 		TextCell cell = new TextCell() {
 			public void render(Cell.Context context, SafeHtml value,
 					SafeHtmlBuilder sb) {
 				sb.appendHtmlConstant("<span class=\"" + style.pageDetails()
 						+ "\">");
 				super.render(context, value, sb);
 				sb.appendHtmlConstant("</span>");
 			};
 		};
 		String text = "";
 
 		@Override
 		public Cell<String> getCell() {
 			return cell;
 		}
 
 		@Override
 		public FieldUpdater<String, String> getFieldUpdater() {
 			return null;
 		}
 
 		@Override
 		public String getValue(String object) {
 			return text;
 		}
 
 		public void setText(String text) {
 			this.text = text;
 		}
 	}
 
 	private class ImageButton implements HasCell<String, ImageResource> {
 		private ImageButtonCell cell;
 		private ImageResource resource;
 		private ImageResource disabledResource;
 		private boolean disabled;
 
 		public ImageButton(String text, ImageResource resource,
 				ImageResource disabledResource, Delegate<ImageResource> delegate) {
 			this.resource = resource;
 			this.disabledResource = disabledResource;
 
 			cell = new ImageButtonCell(delegate) {
 				@Override
 				public void render(Context context, ImageResource value,
 						SafeHtmlBuilder sb) {
 					sb.appendHtmlConstant("<span class=\"" + style.button()
 							+ (disabled ? " " + style.disabledButton() : "")
 							+ "\">");
 					super.render(context, value, sb);
 					sb.appendHtmlConstant("</span>");
 				}
 			};
 		}
 
 		@Override
 		public Cell<ImageResource> getCell() {
 			return cell;
 		}
 
 		@Override
 		public FieldUpdater<String, ImageResource> getFieldUpdater() {
 			return null;
 		}
 
 		@Override
 		public ImageResource getValue(String object) {
 			return disabled ? disabledResource : resource;
 		}
 
 		public void setDisabled(boolean isDisabled) {
 			this.disabled = isDisabled;
 		}
 
 	}
 
 	private static class ImageButtonCell extends AbstractCell<ImageResource> {
 		private static ImageResourceRenderer renderer = new ImageResourceRenderer();
 		private Delegate<ImageResource> delegate;
 
 		public ImageButtonCell(Delegate<ImageResource> delegate) {
 			super("click");
 			this.delegate = delegate;
 		}
 
 		@Override
 		public void render(Context context, ImageResource value,
 				SafeHtmlBuilder sb) {
 			if (value != null) {
 				sb.append(renderer.render(value));
 			}
 		}
 
 		@Override
 		public void onBrowserEvent(Context context, Element parent,
 				ImageResource value, NativeEvent event,
 				ValueUpdater<ImageResource> valueUpdater) {
 			super.onBrowserEvent(context, parent, value, event, valueUpdater);
 			delegate.execute(value);
 		}
 	}
 }
