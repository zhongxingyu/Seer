 package org.fhw.asta.kasse.server.service;
 
 import java.util.List;
 
 import org.fhw.asta.kasse.server.dao.ArticleDao;
 import org.fhw.asta.kasse.server.dao.BillOrderDao;
 import org.fhw.asta.kasse.shared.basket.BasketItem;
 import org.fhw.asta.kasse.shared.model.BillOrder;
 import org.fhw.asta.kasse.shared.service.billorder.BillOrderService;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 @Singleton
 public class BillOrderServiceEndpoint extends RemoteServiceServlet implements BillOrderService {
 
   /**
 	 * 
 	 */
   private static final long serialVersionUID = 1L;
 
   @Inject
   private ArticleDao articleDao;
 
   @Inject
   private BillOrderDao billOrderDao;
 
   @Override
   public BillOrder getBillOrder(int id) {
     return this.billOrderDao.getBillOrder(id);
   }
 
   @Override
   public List<BasketItem> getBillOrderArticles(int id) {
     final BillOrder bo = this.getBillOrder(id);
 
   }
 
 }
