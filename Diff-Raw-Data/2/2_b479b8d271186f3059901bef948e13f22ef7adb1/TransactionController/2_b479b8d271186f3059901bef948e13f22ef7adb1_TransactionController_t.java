 package org.homebudget.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.annotation.Resource;
 import javax.validation.Valid;
 import org.homebudget.model.Account;
 import org.homebudget.model.Transaction;
 import org.homebudget.services.AccountManagementService;
 import org.homebudget.services.TransactionManagementService;
 import org.homebudget.services.TransactionValidationService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 @Controller
 @RequestMapping("/accounts")
 public class TransactionController extends AbstractController {
 
    @Autowired
    private TransactionValidationService transactionValidation;
 
    @Resource
    private TransactionManagementService transactionManagementService;
 
    @Resource
    private AccountManagementService accountManagementService;
 
    @RequestMapping(value = "/{name}/transactions", method = RequestMethod.GET)
    public List<Transaction> getAllTransactions(@PathVariable("name") String accountName,
        Model model) {
       Account account = getAccountForSessionUser(accountName);
       if (account == null) {
          return new ArrayList<Transaction>();
       }
       final List<Transaction> transactions = transactionManagementService.getAllAccountTransactions(
           accountName);
 
       model.addAttribute(transactions);
 
       return transactions;
    }
 
    @RequestMapping(value = "{name}/transactions/{id}", method = RequestMethod.GET)
    public String getTransaction(@PathVariable("name") String accountName,
        @PathVariable("id") Long transactionId, Model model) {
       Account account = getAccountForSessionUser(accountName);
       if (account == null) {
          return "redirect:";
       }
       final Transaction transaction = transactionManagementService.getTransaction(transactionId);
 
       model.addAttribute(transaction);
       return "transaction";
    }
 
   @RequestMapping(value = "{name}/transactions/new", method = RequestMethod.GET)
    public String createTransaction(@PathVariable("name") String accountName, Model model) {
       Account account = getAccountForSessionUser(accountName);
       if (account == null) {
          return "redirect:";
       }
       model.addAttribute(new Transaction());
       return "transaction";
    }
 
    @RequestMapping(value = "{name}/transactions/{id}", method = RequestMethod.DELETE)
    public String deleteTransaction(@PathVariable("name") String accountName,
        @PathVariable("id") Long transactionId) {
       final Transaction transaction = transactionManagementService.getTransaction(transactionId);
       Account account = getAccountForSessionUser(accountName);
       if (account == null || transaction == null) {
          return "redirect:";
       }
 
       transactionManagementService.deleteTransaction(transaction);
 
       return "redirect:";
    }
 
    @RequestMapping(value = "{name}/transactions/new", method = RequestMethod.POST)
    public String postTransaction(@PathVariable("name") String accountName, @ModelAttribute(
        "transaction") @Valid Transaction transaction,
        BindingResult result, Model model) {
       Account account = getAccountForSessionUser(accountName);
       transactionValidation.validate(transaction, result, getSessionUser().getUsername());
       if (result.hasErrors()) {
          return "forward:new";
       }
       if (transaction == null || account == null) {
          return "redirect:";
       }
       transactionManagementService.saveTransaction(transaction, getSessionUser().getUsername());
 
       return "redirect:";
    }
 
    @RequestMapping(value = "{name}/transactions/{id}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String updateTransactionDetails(@Valid Transaction transaction, @PathVariable("name") String accountName,
        @PathVariable("id") Long transactionId, BindingResult result,
        Model model) {
       
       Account account = getAccountForSessionUser(accountName);
       if(account == null){
          return "redirect:";
       }
       
       Transaction oldTransaction = transactionManagementService.getTransaction(transaction.getId(),
           accountName);
 
       if (oldTransaction == null) {
          return "redirect:transactions";
       }
 
       transactionManagementService.updateTransactionDetails(oldTransaction, transaction);
 
       return "redirect:transactions";
 
    }
 
    public TransactionManagementService getTransactionManagementService() {
 
       return transactionManagementService;
    }
 
    public void setTransactionManagementService(
        TransactionManagementService transactionManagementService) {
 
       this.transactionManagementService = transactionManagementService;
    }
 
    public TransactionValidationService getTransactionValidation() {
       return transactionValidation;
    }
 
    public void setTransactionValidation(TransactionValidationService transactionValidation) {
       this.transactionValidation = transactionValidation;
    }
 
    private Account getAccountForSessionUser(String accountName) {
       String userName = getSessionUser().getUsername();
       Account account = accountManagementService.getAccount(accountName, userName);
       return account;
    }
 
 }
