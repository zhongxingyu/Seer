 package controllers.services;
 
 import models.Bowl;
 import models.Expense;
 import models.Participant;
 import models.User;
 import util.Pagination;
 import util.controller.GenericController;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ben
  * Date: 9/22/12
  * Time: 4:20 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class ExpensesController extends GenericController {
 
     public static void create( Long id, Expense expense ) {
         Bowl bowl = Bowl.findById( id );
         if( expense.payer != null ) {
             expense.payer = User.findById( expense.payer.id );
         }
 
         expense.bowl = bowl;
         expense.save();
 
         if( expense.payer != null ) {
             expense.addParticipant( new Participant( expense.payer, expense ) );
         }
 
         bowl.addExpense(expense);
 
         renderJSON(bowl);
     }
 
     public static void delete( Long id ) {
         Expense expense = Expense.findById( id );
 
         if( expense == null ) {
             notFound();
         }
 
         Bowl bowl = expense.bowl;
         if( bowl.cost != null ) {
             bowl.cost -= expense.cost;
         } else {
             bowl.cost = 0F;
         }
 
         expense.delete();
         bowl.save();
 
         ok();
     }
 
     public static void update( Long id, Expense expense ) {
         Expense e = Expense.findById( id );
 
         if( e == null ) {
             notFound();
         }
 
         if( expense.cost != null ) {
             e.cost = expense.cost;
         }
         if( expense.description != null ) {
             e.description = expense.description;
         }
         if( expense.date != null ) {
             e.date = expense.date;
         }
         e.save();
 
         renderJSON(e);
     }
 
     public static void addParticipant( Long id, Long pId ) {
         Expense expense = Expense.findById( id );
         User user = User.findById(pId);
         Participant participant = new Participant( user, expense );
 
         expense.addParticipant(participant);
 //        expense.save();
 
         renderJSON(expense);
     }
 
     public static void deleteParticipant( Long id, Long pId ) {
         Expense expense = Expense.findById( id );
         Participant participant = Participant.findById( pId );
 
         participant.delete();
 
         expense.recalculateShares();
 
         renderJSON(expense);
     }
 
     public static void addAllParticipants( Long id ) {
         Expense expense = Expense.findById( id );
 
         List<User> users = User.findAllNonParticipantUsers(expense, new Pagination(1, 10));
         Participant participant;
         for( User user : users ) {
             participant = new Participant( user, expense );
             expense.addParticipant(participant);
         }
 
         renderJSON(expense);
     }
 
 }
