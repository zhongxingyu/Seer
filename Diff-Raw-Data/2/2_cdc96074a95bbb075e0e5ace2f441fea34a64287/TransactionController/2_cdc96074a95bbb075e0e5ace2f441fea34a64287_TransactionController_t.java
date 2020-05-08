 package org.homebudget.controllers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.validation.Valid;
 
 import org.homebudget.model.Account;
 import org.homebudget.model.Category;
 import org.homebudget.model.Transaction;
 import org.homebudget.model.Transaction.TransactionType;
 import org.homebudget.services.AccountManagementService;
 import org.homebudget.services.CategoryEditor;
 import org.homebudget.services.CategoryManagementService;
 import org.homebudget.services.TransactionManagementService;
 import org.homebudget.services.TransactionValidationService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
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
 
    @Resource
    private CategoryManagementService categoryManagementService;
 
    @RequestMapping(value = "/{name}/transactions", method = RequestMethod.GET)
    public String getAllTransactions(@PathVariable("name") String accountName, Model model) {
 
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
             .getUsername());
       if (!isAuthorized) {
          return "redirect:";
       }
       final List<Transaction> transactions = transactionManagementService
             .getAllAccountTransactions(accountName);
 
       model.addAttribute(transactions);
 
       return "transactions";
    }
 
    @RequestMapping(value = "{name}/transactions/{id}", method = RequestMethod.GET)
    public String getTransaction(@PathVariable("name") String accountName,
          @PathVariable("id") Long transactionId, Model model) {
 
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
             .getUsername(), transactionId);
       if (!isAuthorized) {
          return "redirect:";
       }
       final Transaction transaction = transactionManagementService.getTransaction(transactionId);
       final List<TransactionType> transactionTypeList = new ArrayList<TransactionType>(
             Arrays.asList(TransactionType.values()));
       
       final List<Category> categories =  categoryManagementService.getAllCategories(getSessionUser().getUsername());
 
       model.addAttribute(categories);
       model.addAttribute(transactionTypeList);
       model.addAttribute(transaction);
       return "transactionDetails";
    }
 
    @RequestMapping(value = "{name}/transactions/new", method = RequestMethod.GET)
    public String createTransaction(@PathVariable("name") String accountName, Model model) {
 
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
             .getUsername());
       if (!isAuthorized) {
          return "redirect:";
       }
 
       final List<TransactionType> transactionTypeList = new ArrayList<TransactionType>(
             Arrays.asList(TransactionType.values()));
 
       final List<Category> categories = categoryManagementService.getAllCategories(getSessionUser()
             .getUsername());
 
       model.addAttribute(categories);
       model.addAttribute(transactionTypeList);
       model.addAttribute(new Transaction());
       return "transaction";
    }
 
    @RequestMapping(value = "{name}/transactions/{id}", method = RequestMethod.DELETE)
    public String deleteTransaction(@PathVariable("name") String accountName,
          @PathVariable("id") Long transactionId) {
 
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
             .getUsername(), transactionId);
       if (!isAuthorized) {
          return "redirect:";
       }
       final Transaction transaction = transactionManagementService.getTransaction(transactionId);
 
       transaction.getParent().getTransactions().remove(transaction);
 
       accountManagementService.updateAccount(transaction.getParent());
 
       if (transaction == null) {
          return "redirect:";
       }
 
       return "redirect:";
    }
 
    @RequestMapping(value = "{name}/transactions/new", method = RequestMethod.POST)
    public String postTransaction(@PathVariable("name") String accountName,
          @ModelAttribute("transaction") @Valid Transaction transaction, BindingResult result,
          Model model) {
 
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
             .getUsername());
       if (!isAuthorized) {
          return "redirect:";
       }
       Account account = accountManagementService.getAccount(accountName, getSessionUser()
             .getUsername());
       // transactionValidation.validate(transaction, result, getSessionUser().getUsername());
       // if (result.hasErrors()) {
       // return "redirect:";
       // }
       transactionManagementService.saveTransaction(transaction, accountName);
 
       return "redirect:";
    }
 
    @RequestMapping(value = "{name}/transactions", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String updateTransactionDetails(Transaction transaction,
          @PathVariable("name") String accountName, BindingResult result, Model model) {
 
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
             .getUsername(), transaction.getId());
       if (!isAuthorized) {
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
 
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
 
       binder.registerCustomEditor(Category.class, new CategoryEditor(categoryManagementService,
             getSessionUser().getUsername()));
    }
 
 }
