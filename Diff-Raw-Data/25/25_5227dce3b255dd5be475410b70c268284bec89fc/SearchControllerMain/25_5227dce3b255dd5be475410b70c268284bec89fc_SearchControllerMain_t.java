 package svm.logic.abstraction;
 
import svm.logic.abstraction.controller.ISearchController;
import svm.logic.abstraction.exception.IllegalGetInstanceException;
import svm.logic.abstraction.transferobjects.ITransferMember;
import svm.persistence.abstraction.exceptions.ExistingTransactionException;
import svm.persistence.abstraction.exceptions.NoSessionFoundException;
import svm.persistence.abstraction.exceptions.NoTransactionException;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

 /**
  * Projectteam: Team C
  * Date: 30.10.12
  */
 public class SearchControllerMain {
 
    public static void main(String[] args) throws IllegalGetInstanceException, NoSessionFoundException, ExistingTransactionException, NoTransactionException {
        ISearchController search = LogicFacade.getSearchController();
        search.start();
        int date = 1;
        int month = Calendar.SEPTEMBER;
        int year = 1953;
        Date d = new GregorianCalendar(year, month, date).getTime();
        for (ITransferMember member : search.getMembers(d, new Date())) {
            System.out.println(member.getFirstName() + " " + member.getLastName());
        }
        search.commit();
     }
 
 }
