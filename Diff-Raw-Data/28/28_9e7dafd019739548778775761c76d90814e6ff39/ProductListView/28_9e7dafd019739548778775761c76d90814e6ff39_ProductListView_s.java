 package com.japancuccok.common.wicket.component;
 
 import com.japancuccok.common.domain.image.IImage;
 import com.japancuccok.common.domain.product.Product;
 import com.japancuccok.db.DAOService;
 import com.japancuccok.db.GenericGaeDAO;
 import com.japancuccok.main.detail.ProductDetail;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 import java.util.List;
 import java.util.logging.Logger;
 
import static com.japancuccok.common.infrastructure.tools.ImageResourceUtil.getImage;
 /**
  * Created with IntelliJ IDEA.
  * User: Nagy Gergely
  * Date: 2012.07.08.
  * Time: 1:16
  */
public class ProductListView extends PageableListView<IImage> {
 
     private static final long serialVersionUID = -4839647587005663893L;
     
    private static final int ITEMS_PER_PAGE = 8;

     transient private static final Logger logger = Logger.getLogger(ProductListView.class.getName());
 
     public ProductListView(String id, IModel<? extends List<? extends IImage>> iModel) {
        super(id, iModel, ITEMS_PER_PAGE);
     }
 
     @Override
     protected void populateItem(ListItem<IImage> listItem) {
         setOutputMarkupId(true);
 
         IImage image = listItem.getModelObject();
         WebMarkupContainer productItem = new WebMarkupContainer("productItem");
         productItem.add(getImageLink(image));
         listItem.add(productItem);
     }
 
     private Link<ProductDetail> getImageLink(IImage image) {
         PageParameters parameters = new PageParameters();
         Product product = getProduct(image);
         parameters.add("id", product.getId());
         BookmarkablePageLink<ProductDetail> imageLink =
                 new BookmarkablePageLink<ProductDetail>("productItemAnchor", ProductDetail.class, parameters);
         imageLink.setOutputMarkupId(true);
         imageLink.add(getImage(image, "productItemImg", this));
         imageLink.add(getName(product));
         imageLink.add(getPrice(product));
         return imageLink;
     }
 
     private Product getProduct(IImage image) {
         GenericGaeDAO<Product> productDAO = DAOService.productDao;
         return productDAO.find(image.getKey().getKey());
     }
 
     private Label getPrice(Product product) {
         return new Label("productItemPrice", product.getPrice()+" HUF");
     }
 
     private Label getName(Product product) {
         return new Label("productItemName", product.getName());
     }
 
 }
