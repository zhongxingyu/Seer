 package org.cloudlet.web.core.client;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
 import com.google.inject.Inject;
 
 import com.sencha.gxt.core.client.ValueProvider;
 import com.sencha.gxt.data.shared.PropertyAccess;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 
 import org.cloudlet.web.core.Book;
 import org.cloudlet.web.core.BookFeed;
 import org.cloudlet.web.core.Media;
 
 import java.util.List;
 
 public class BookGrid extends ResourceGrid<Book, BookFeed> {
 
   interface BookPorperties extends PropertyAccess<Book> {
     ValueProvider<Book, Media> cover();
 
     ValueProvider<Book, String> title();
   }
 
   @Inject
   BookSearch bookSearch;
 
   private static BookPorperties properties = GWT.create(BookPorperties.class);
 
   @Override
   public Class<BookFeed> getResourceType() {
     return BookFeed.class;
   }
 
   @Override
   protected AbstractSafeHtmlRenderer<Book> getCell() {
     return new AbstractSafeHtmlRenderer<Book>() {
       @Override
       public SafeHtml render(final Book book) {
         return ResourceGrid.r.renderItem(book.getTitle(), getCoverUrl(book.getCover()), ResourceGrid.resources.css());
       }
     };
   }
 
   @Override
   protected void initColumn(List<ColumnConfig<Book, ?>> l) {
     ColumnConfig<Book, Media> coverColumn = new ColumnConfig<Book, Media>(properties.cover(), 8, "Cover");
     coverColumn.setCell(new AbstractCell<Media>() {
       @Override
       public void render(com.google.gwt.cell.client.Cell.Context context, Media value, SafeHtmlBuilder sb) {
         StringBuilder imageUrl = new StringBuilder();
        imageUrl.append("<div><img style='width: 65px;height: 65px;' src='").append(getCoverUrl(value)).append("'></div>");
         sb.append(SafeHtmlUtils.fromSafeConstant(imageUrl.toString()));
       }
     });
     l.add(coverColumn);
     l.add(new ColumnConfig<Book, String>(properties.title(), 100, "Title"));
   }
 
   @Override
   protected ResourceSearch<Book, BookFeed> initSearch() {
     return bookSearch;
   }
 
   @Override
   protected void initView() {
     setHeadingHtml("Book Grid");
     super.initView();
   }
 }
