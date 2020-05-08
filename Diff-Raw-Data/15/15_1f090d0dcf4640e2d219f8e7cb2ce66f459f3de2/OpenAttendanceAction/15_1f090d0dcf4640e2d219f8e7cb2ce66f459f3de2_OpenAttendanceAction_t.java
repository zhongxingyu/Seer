 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package census.presentation.actions;
 
 import census.business.ClientsService;
 import census.business.OrdersService;
 import census.business.SessionsService;
 import census.business.StorageService;
 import census.business.api.BusinessException;
 import census.business.api.ValidationException;
 import census.business.dto.ClientDTO;
 import census.presentation.dialogs.CensusDialog;
 import census.presentation.CensusFrame;
 import census.presentation.dialogs.EditOrderDialog;
 import census.presentation.dialogs.OpenAttendanceDialog;
 import census.presentation.util.NotificationException;
 import java.awt.event.ActionEvent;
 import java.beans.Beans;
 import java.math.BigDecimal;
 import java.text.MessageFormat;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.ResourceBundle;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.joda.time.DateMidnight;
 import org.joda.time.Days;
 
 
 /**
  *
  * @author daniel
  */
 public class OpenAttendanceAction extends CensusAction implements Observer {
     private ResourceBundle bundle = ResourceBundle.getBundle("census/presentation/resources/Strings");
     private Logger logger = Logger.getLogger(OpenAttendanceAction.class.getName());
 
     public OpenAttendanceAction() {
         if(!Beans.isDesignTime()) {
             update(null, null);
         }
         
         setText(bundle.getString("Text.Entry"));
         setIcon(new ImageIcon(getClass().getResource("/census/presentation/resources/open.png")));
 
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         try {
             
             StorageService storageService = StorageService.getInstance();
 
             storageService.beginTransaction();
 
             /*
              * OpenAttendance
              */
             OpenAttendanceDialog openAttendanceDialog = new OpenAttendanceDialog(getFrame());
             openAttendanceDialog.setVisible(true);
 
             if (openAttendanceDialog.getResult().equals(CensusDialog.RESULT_EXCEPTION)) {
                 throw openAttendanceDialog.getException();
             }
 
             if (openAttendanceDialog.getResult().equals(CensusDialog.RESULT_CANCEL)) {
                 storageService.rollbackTransaction();
                 return;
             }
 
             /*
              * If requested, EditFinancialActivty
              */
             if (openAttendanceDialog.isFinancialActivityDialogRequested()) {
 
                 Short activityId = null;
                 EditOrderDialog editFinancialActivityDialog = new EditOrderDialog(getFrame());
 
                 try {
                     if (openAttendanceDialog.isAnonymous()) {
                         activityId = OrdersService.getInstance().findForAttendanceById(openAttendanceDialog.getAttendanceId());
                     } else {
                         activityId = OrdersService.getInstance().findByClientIdAndDate(openAttendanceDialog.getClientId(), new DateMidnight(), true);
                     }
                 } catch (ValidationException ex) {
                     throw new RuntimeException(ex);
                 }
 
                 editFinancialActivityDialog.setOrderId(activityId);
                 editFinancialActivityDialog.setFullPaymentForced(false);
                 
                 editFinancialActivityDialog.setVisible(true);
 
                 if (editFinancialActivityDialog.getResult().equals(CensusDialog.RESULT_EXCEPTION)) {
                     throw editFinancialActivityDialog.getException();
                 }
 
                 if (editFinancialActivityDialog.getResult().equals(CensusDialog.RESULT_CANCEL)) {
                     storageService.rollbackTransaction();
                     return;
                 }
             } else if (!openAttendanceDialog.isAnonymous()) {
                 ClientDTO client;
                 try {
                     client = ClientsService.getInstance().getById(openAttendanceDialog.getClientId());
                 } catch (ValidationException ex) {
                     throw new RuntimeException(ex);
                 }
                 if (client.getMoneyBalance().compareTo(BigDecimal.ZERO) < 0) {
                     CensusFrame.getGlobalCensusExceptionListenersStack().peek().processException(new NotificationException(MessageFormat.format(bundle.getString("Message.ClientHasDebt.withDebtAmount"), new Object[] {client.getMoneyBalance().negate().setScale(2)})));
                 }
                 if (client.getAttendancesBalance() == 0) {
                     CensusFrame.getGlobalCensusExceptionListenersStack().peek().processException(new NotificationException(bundle.getString("Message.ThisIsClientsLastAttendance")));
                 }
                 Days days = Days.daysBetween(new DateMidnight(), client.getExpirationDate());
                 if (days.getDays() < 7) {
                     CensusFrame.getGlobalCensusExceptionListenersStack().peek().processException(new NotificationException(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("census/presentation/resources/Strings").getString("Message.ClientHasThisManyDaysBeforeExpiration.withDays"), new Object[] {days.getDays()})));
                 }
 
             }
 
             storageService.commitTransaction();
 
         } catch (BusinessException ex) {
             StorageService.getInstance().rollbackTransaction();
             JOptionPane.showMessageDialog(getFrame(), ex.getMessage(), "Message", JOptionPane.INFORMATION_MESSAGE);
             return;
         } catch (RuntimeException ex) {
             logger.error("RuntimeException", ex);
             JOptionPane.showMessageDialog(getFrame(), bundle.getString("Message.ProgramEncounteredError"), bundle.getString("Title.Error"), JOptionPane.ERROR_MESSAGE);
             if(StorageService.getInstance().isTransactionActive()) {
                 StorageService.getInstance().rollbackTransaction();
             }
             return;
         }
 
     }
     @Override
     public final void update(Observable o, Object arg) {
         if (o == null) {
             SessionsService.getInstance().addObserver(this);
         }
         Boolean open = SessionsService.getInstance().hasOpenSession();
         setEnabled(open);
     }
 }
