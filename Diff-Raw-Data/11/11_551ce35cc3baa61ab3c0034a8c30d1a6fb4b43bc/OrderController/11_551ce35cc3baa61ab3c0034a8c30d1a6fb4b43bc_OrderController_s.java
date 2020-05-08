 package oshop.web.api.rest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Criteria;
 import org.hibernate.SQLQuery;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.MatrixVariable;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import oshop.dao.GenericDao;
 import oshop.model.Order;
 import oshop.model.OrderHasOrderStates;
 import oshop.model.Product;
 import oshop.web.api.rest.adapter.EntityDetachingRestCallbackAdapter;
 import oshop.web.api.rest.adapter.ListReturningRestCallbackAdapter;
 import oshop.web.api.rest.adapter.ValidationRestCallbackAdapter;
 import oshop.web.api.rest.adapter.VoidRestCallbackAdapter;
 import oshop.web.converter.EntityConverter;
 
 import javax.annotation.Resource;
 import javax.validation.Valid;
 import java.util.List;
 
 @Controller
 @RequestMapping("/api/orders")
 @Transactional(readOnly = true)
 public class OrderController extends BaseController<Order, Integer> {
 
     private static final Log log = LogFactory.getLog(OrderController.class);
 
     @Resource
     private GenericDao<Order, Integer> orderDao;
 
     @Resource
     private GenericDao<Product, Integer> productDao;
 
     @Resource(name = "orderToDTOConverter")
     private EntityConverter<Order, Integer> converter;
 
     @Resource(name = "orderHasStateToDTOConverter")
     private EntityConverter<OrderHasOrderStates, Integer> orderHasStateConverter;
 
     @Resource(name = "productToDTOConverter")
     private EntityConverter<Product, Integer> productConverter;
 
     @Override
     protected GenericDao<Order, Integer> getDao() {
         return orderDao;
     }
 
     @Override
     protected EntityConverter<Order, Integer> getToDTOConverter() {
         return converter;
     }
 
     // /api/orders/1/products/batch;ids=1,2/delete
     @RequestMapping(
             value = "/{id}/products/{batch}/delete",
             method = RequestMethod.DELETE)
     @Transactional(readOnly = false)
     public ResponseEntity<?> deleteOrderProduct(
             @PathVariable final Integer id,
             @MatrixVariable(value = "ids", pathVar="batch", required = true) final List<Integer> ids) {
 
         return new VoidRestCallbackAdapter() {
             @Override
             protected void perform() throws Exception {
                 getDao().executeQuery(
                         "delete from order_products where order_id = :id and product_id in :ids",
                         new GenericDao.SQLQueryManipulator() {
                             @Override
                             public void manipulateWithQuery(SQLQuery query) {
                                 query.setParameterList("ids", ids).setParameter("id", id);
                             }
                         });
 
                 getDao().getSession().flush();
             }
         }.invoke();
     }
 
     // /api/orders/1/products/batch;ids=1,2/add
     @RequestMapping(
             value = "/{id}/products/{batch}/add",
             method = RequestMethod.POST
             )
     @ResponseBody
     @Transactional(readOnly = false)
     public ResponseEntity<?> addOrderProduct(
             @PathVariable final Integer id,
             @MatrixVariable(value = "ids", pathVar="batch", required = true) final List<Integer> ids) {
 
         return new VoidRestCallbackAdapter() {
             @Override
             protected void perform() throws Exception {
                 Order order = getDao().get(id);
 
                 Criteria productsCriteria = productDao.createCriteria();
                 productsCriteria.add(Restrictions.in("id", ids));
                 List<Product> products = productDao.list(productsCriteria);
 
 
                 order.getProducts().addAll(products);
                 getDao().update(order);
             }
         }.invoke();
     }
 
     @RequestMapping(
             value = "/{id}/products",
             method = RequestMethod.GET,
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     @Transactional(readOnly = false)
     public ResponseEntity<?> listOrderProduct(
             @PathVariable final Integer id) {
 
         return new ListReturningRestCallbackAdapter<List<Product>>() {
 
             private Order order = getDao().get(id);
 
             @Override
             protected Long getSize() throws Exception {
                 return (long)order.getProducts().size();
             }
 
             @Override
             protected List<Product> getList() throws Exception {
                 return productConverter.convert(order.getProducts());
             }
         }.invoke();
     }
 
     @RequestMapping(
             value = "/{id}/orderHasStates",
             method = RequestMethod.POST,
             consumes = {MediaType.APPLICATION_JSON_VALUE},
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     @Transactional(readOnly = false)
     public ResponseEntity<?> addOrderHasState(
             @PathVariable final Integer id,
             @RequestBody @Valid final OrderHasOrderStates entity, final BindingResult result) {
 
         return new ValidationRestCallbackAdapter(result,
                 new EntityDetachingRestCallbackAdapter<OrderHasOrderStates, Integer>(orderHasStateConverter) {
                     @Override
                     protected OrderHasOrderStates getResult() throws Exception {
                         Order order = getDao().get(id);
                        order.getStates().add(entity);
                         entity.setOrder(order);
                        getDao().update(order);
 
                        return order.getStates().get(order.getStates().size() - 1);
                     }
                 }).invoke();
     }
 
     @RequestMapping(
             value = "/{id}/orderHasStates",
             method = RequestMethod.GET,
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     @Transactional(readOnly = false)
     public ResponseEntity<?> listOrderHasStates(
             @PathVariable final Integer id) {
 
         return new ListReturningRestCallbackAdapter<List<OrderHasOrderStates>>() {
 
             private Order order = getDao().get(id);
 
             @Override
             protected Long getSize() throws Exception {
                 return (long)order.getStates().size();
             }
 
             @Override
             protected List<OrderHasOrderStates> getList() throws Exception {
                 return orderHasStateConverter.convert(order.getStates());
             }
         }.invoke();
     }
 }
