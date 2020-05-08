 package com.in6k.mypal.controller;
 
 import com.in6k.mypal.dao.TransactionDAO;
 import com.in6k.mypal.dao.UserDao;
 import com.in6k.mypal.domain.Transaction;
 import com.in6k.mypal.domain.User;
 import com.in6k.mypal.service.TransactionValidator;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Collection;
 
 @Controller
 @RequestMapping(value = "/transaction")
 public class TransactionController {
 
     @RequestMapping(value = "/create", method = RequestMethod.GET)
     public String creationForm(ModelMap model) {
         Collection<User> users = UserDao.list();
 
         model.addAttribute("users", users);
 
         return "transaction/create";
     }
 
     @RequestMapping(value = "/create", method = RequestMethod.POST)
     public String create(HttpServletRequest request) throws IOException {
         TransactionValidator transactionValidator = new TransactionValidator();
 
         transactionValidator.setCredit(UserDao.getById(Integer.parseInt(request.getParameter("credit"))));
         transactionValidator.setDebit(UserDao.getByEmail(request.getParameter("debit")));
         transactionValidator.setInputSum(request.getParameter("sum"));
 
 
        /*if (transactionValidator.validate().size() == 0) {*/
             Transaction transaction = new Transaction();
             transaction.setDebit(transactionValidator.getDebit());
             transaction.setCredit(transactionValidator.getCredit());
            transaction.setSum(Double.parseDouble(transactionValidator.getInputSum()));
 
             TransactionDAO.create(transaction);
 
            //return "transaction/create";
        /*}*/
         return "transaction/create";
     }
 
     @RequestMapping(value = "/list")
     public String list(ModelMap model) throws IOException, SQLException {
         //Collection<Transaction> transactions = TransactionDAO.findAllForUser(UserDao.getById(1));
 
        model.addAttribute("transactions", TransactionDAO.findAllForUser(UserDao.getById(1)));
 
         return "transaction/list";
     }
 
     @RequestMapping(value = "/delete")
     public String delete(@RequestParam("id") int id) throws SQLException {
 
         TransactionDAO.delete(id);
         return "transaction/list";
     }
 
 }
