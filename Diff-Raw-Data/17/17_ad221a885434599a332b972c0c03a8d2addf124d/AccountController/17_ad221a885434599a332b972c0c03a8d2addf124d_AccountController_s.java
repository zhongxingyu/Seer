 package org.homebudget.controllers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import javax.annotation.Resource;
 import javax.validation.Valid;
 import org.homebudget.model.Account;
 import org.homebudget.model.Currency;
 import org.homebudget.services.AccountManagementService;
 import org.homebudget.services.AccountValidationService;
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
 public class AccountController extends AbstractController {
 
    @Autowired
    private AccountValidationService accountValidationService;
 
    @Resource
    private AccountManagementService accountManagementService;
 
    @RequestMapping(method = RequestMethod.GET)
    public List<Account> getAccounts(Model model) {
 
       final List<Account> accounts = accountManagementService.getAllUserAccounts(getSessionUser()
             .getUsername());
 
       model.addAttribute(accounts);
 
       return accounts;
    }
 
    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public String getAccount(@PathVariable("name") String accountName, Model model) {
 
       final List<Currency> currencyList = new ArrayList<Currency>(Arrays.asList(Currency.values()));
 
       final Account account = accountManagementService.getAccount(accountName, getSessionUser()
             .getUsername());
 
       model.addAttribute(currencyList);
       model.addAttribute(account);
       return "accountDetails";
    }
 
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String getAccount(Model model) {
 
       final List<Currency> currencyList = new ArrayList<Currency>(Arrays.asList(Currency.values()));
 
       model.addAttribute(currencyList);
 
       model.addAttribute(new Account());
       return "account";
    }
 
    @RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
    public String deleteAccount(@PathVariable("name") String accountName) {
 
       final String sessionUsername = getSessionUser().getUsername();
 
       final Account account = accountManagementService.getAccount(accountName, sessionUsername);
 
       if (account == null) return "redirect:";
 
       accountManagementService.deleteAccount(account);
 
       return "redirect:";
    }
 
    @RequestMapping(value = "new", method = RequestMethod.POST)
    public String postAccount(@ModelAttribute("account") @Valid Account account,
          BindingResult result, Model model) {
 
       accountValidationService.validate(account, result, getSessionUser().getUsername());
       if (result.hasErrors()) {
          final List<Currency> currencyList = new ArrayList<Currency>(Arrays.asList(Currency
                .values()));
          model.addAttribute(currencyList);
          return "account";
       }
       if (account != null) {
 
         accountManagementService.saveAccount(account, getSessionUser().getUsername());
       }
       return "redirect:";
    }
 
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String updateAccountDetails(@Valid Account account, BindingResult result, Model model) {
 
       final String sessionUsername = getSessionUser().getUsername();
 
       Account oldAccount = accountManagementService.getAccount(account.getId(), sessionUsername);
 
       if (oldAccount == null) return "redirect:accounts";
 
       accountManagementService.updateAccountDetails(oldAccount, account);
 
       return "redirect:accounts";
 
    }
 
    public AccountManagementService getAccountManagementService() {
 
       return accountManagementService;
    }
 
    public void setAccountManagementService(AccountManagementService accountManagementService) {
 
       this.accountManagementService = accountManagementService;
    }
 
    public AccountValidationService getAccountValidationService() {
 
       return accountValidationService;
    }
 
    public void setAccountValidationService(AccountValidationService accountValidationService) {
 
       this.accountValidationService = accountValidationService;
    }
 }
