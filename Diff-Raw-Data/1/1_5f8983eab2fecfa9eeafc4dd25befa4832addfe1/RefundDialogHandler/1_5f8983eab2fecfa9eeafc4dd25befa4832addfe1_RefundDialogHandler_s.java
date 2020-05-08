 package com.lavida.swing.handler;
 
 import com.lavida.service.entity.ArticleJdo;
 import com.lavida.swing.LocaleHolder;
 import com.lavida.swing.dialog.RefundDialog;
 import com.lavida.swing.service.ArticleServiceSwingWrapper;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import java.util.Date;
 
 /**
  * Created: 21:50 18.08.13
  * The RefundDialogHandler is a handler for the RefundDialog.
  * @author Ruslan
  */
 
 @Component
 public class RefundDialogHandler {
 
     @Resource
     RefundDialog refundDialog;
 
     @Resource
     protected LocaleHolder localeHolder;
 
     @Resource
     private ArticleServiceSwingWrapper articleServiceSwingWrapper;
 
     /**
      * Refunds article from  a consumer to the stock. Makes the article not sold.
      * @param articleJdo the selected articleJdo to be refunded.
      */
     public void refundButtonClicked (ArticleJdo articleJdo){
         articleJdo.setSold(null);
         articleJdo.setRefundDate(new Date());
         articleJdo.setComment(articleJdo.getComment() + ((refundDialog.getCommentTextArea().getText() == null)? null :
                 ("; " + refundDialog.getCommentTextArea().getText())));
         articleServiceSwingWrapper.update(articleJdo);
         try {
             articleServiceSwingWrapper.updateToSpreadsheet(articleJdo);
         } catch (Exception e) {        // todo change to Custom exception
             e.printStackTrace();
             articleJdo.setPostponedOperationDate(new Date());
             articleServiceSwingWrapper.update(articleJdo);
             refundDialog.getMainForm().getHandler().showPostponedOperationsMessage();
             refundDialog.showMessage("mainForm.exception.message.dialog.title", "sellDialog.handler.sold.article.not.saved.to.worksheet");
             refundDialog.hide();
             refundDialog.getSoldProductsDialog().getDialog().repaint();
             refundDialog.getSoldProductsDialog().show();
         }
         refundDialog.hide();
         refundDialog.getSoldProductsDialog().getDialog().repaint();
         refundDialog.getSoldProductsDialog().show();
     }
 }
