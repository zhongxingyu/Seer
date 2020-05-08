 package cz.muni.fi.pa165.library.services;
 
 import cz.muni.fi.pa165.library.daos.BookDao;
 import cz.muni.fi.pa165.library.daos.LoanDao;
 import cz.muni.fi.pa165.library.dtos.BookTO;
 import cz.muni.fi.pa165.library.dtos.CustomerTO;
 import cz.muni.fi.pa165.library.dtos.LoanTO;
 import cz.muni.fi.pa165.library.entities.Book;
 import cz.muni.fi.pa165.library.entities.Loan;
 import cz.muni.fi.pa165.library.utils.Convertor;
 import java.util.ArrayList;
 import java.util.List;
 import org.joda.time.LocalDate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Matej
  */
 @Service
 @Transactional
 public class LoanServiceImpl implements LoanService {
 
     @Autowired
     private LoanDao loanDao;
     @Autowired
     private BookDao bookDao;
     @Autowired
     private Convertor convertor;
 
    @Secured({"ADMIN"})
     public void createLoan(LoanTO loanTO) {
         Loan loanEntity = convertor.convert(loanTO);
         loanDao.createLoan(loanEntity);
         loanTO.setId(loanEntity.getId());
     }
 
    @Secured({"ADMIN"})
     public List<LoanTO> findAllLoans() {
         List<LoanTO> loanTOs = new ArrayList<LoanTO>();
         List<Loan> loanEntities = loanDao.findAllLoans();
         for (Loan loan : loanEntities) {
             loanTOs.add(convertor.convert(loan));
         }
         return loanTOs;
     }
 
     public LoanTO findLoanById(Long id) {
         return convertor.convert(loanDao.findLoanById(id));
     }
 
    @Secured({"ADMIN"})
     public void deleteLoan(LoanTO loanTO) {
         loanDao.deleteLoan(convertor.convert(loanTO));
         loanTO.setId(null);
     }
 
    @Secured({"ADMIN"})
     public void updateLoan(LoanTO loanTO) {
         loanDao.updateLoan(convertor.convert(loanTO));
     }
 
     public void setDao(LoanDao loanDao) {
         this.loanDao = loanDao;
     }
 
     public List<LoanTO> findLoansForCustomer(CustomerTO customer) {
         List<LoanTO> loanTOs = new ArrayList<LoanTO>();
         List<Loan> loanEntities = loanDao.findLoansForCustomer(convertor.convert(customer));
         for (Loan loan : loanEntities) {
             loanTOs.add(convertor.convert(loan));
         }
         return loanTOs;
     }
 
    @Secured({"ADMIN"})
     public List<LoanTO> findLoansForBook(BookTO book) {
         List<LoanTO> loanTOs = new ArrayList<LoanTO>();
         List<Loan> loanEntities = loanDao.findLoansForBook(convertor.convert(book));
         for (Loan loan : loanEntities) {
             loanTOs.add(convertor.convert(loan));
         }
         return loanTOs;
     }
 
    @Secured({"ADMIN"})
     public void returnBook(LoanTO loan) {
         Loan l = convertor.convert(loan);
         Book b = l.getBook();
         b.setCondition(l.getConditionReturned());
         bookDao.updateBook(b);
         l.setToDate(new LocalDate());
         loanDao.updateLoan(l);
     }
 }
