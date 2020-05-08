 package com.bis.web.controller;
 
 import com.bis.common.DateUtils;
 import com.bis.core.services.ItemMasterService;
 import com.bis.core.services.VendorMasterService;
 import com.bis.domain.*;
 import com.bis.inventory.services.StockService;
 import com.bis.procurement.services.ProcurementBillingService;
 import com.bis.procurement.services.ProcurementTransactionService;
 import com.bis.web.ProcurementTransactionHandler;
 import com.bis.web.viewmodel.TransactionDetailGrid;
 import com.bis.web.viewmodel.TransactionDetailRow;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 import java.util.*;
 
 @Controller
 @RequestMapping("/procurement")
 public class ProcurementTransactionController extends BaseController {
 
     private ProcurementTransactionService procurementTransactionService;
     private ItemMasterService itemMasterService;
     private VendorMasterService vendorMasterService;
     private ProcurementTransactionHandler procurementTransactionHandler;
     private ProcurementBillingService procurementBillingService;
 
     protected ProcurementTransactionController() {
     }
 
     @Autowired
     public ProcurementTransactionController(ProcurementTransactionService procurementTransactionService, ItemMasterService itemMasterService, VendorMasterService vendorMasterService, StockService stockService, ProcurementBillingService procurementBillingService) {
         this.procurementTransactionService = procurementTransactionService;
         this.itemMasterService = itemMasterService;
         this.vendorMasterService = vendorMasterService;
         this.procurementBillingService = procurementBillingService;
         this.procurementTransactionHandler = new ProcurementTransactionHandler(procurementTransactionService, stockService);
     }
 
     ProcurementTransactionController(ProcurementTransactionService procurementTransactionService, ItemMasterService itemMasterService, VendorMasterService vendorMasterService, ProcurementTransactionHandler procurementTransactionHandler) {
         this.procurementTransactionService = procurementTransactionService;
         this.itemMasterService = itemMasterService;
         this.vendorMasterService = vendorMasterService;
         this.procurementTransactionHandler = procurementTransactionHandler;
     }
 
     @RequestMapping(value = "/show/{id}", method = RequestMethod.GET)
     public ModelAndView show(@PathVariable("id") int transactionId) {
         ProcurementTransaction procurementTransaction = procurementTransactionService.getProcurementTransaction(transactionId);
         return new ModelAndView("procurement/show", "procurementTransaction", procurementTransaction);
     }
 
     @RequestMapping(value = "/createForm/{type}", method = RequestMethod.GET)
     public ModelAndView createForm(@PathVariable("type") String transactionType) {
         TransactionDetailGrid procurementTransaction = new TransactionDetailGrid();
         if ("returns".equalsIgnoreCase(transactionType))
             procurementTransaction.setType(ProcurementTransactionType.RETURNS.getCode());
         else
             procurementTransaction.setType(ProcurementTransactionType.PURCHASE.getCode());
         procurementTransaction.setTransactionDate(DateUtils.currentDate());
         ModelAndView modelAndView = new ModelAndView("procurement/processTransaction", "procurementTransaction", procurementTransaction);
         modelAndView.addObject("vendors", vendorMasterService.getAll());
         return modelAndView;
     }
 
     @Transactional
     @RequestMapping(value = "/addProcurementTransaction", method = RequestMethod.POST)
     public ModelAndView addProcurementTransaction(@Valid TransactionDetailGrid transactionDetailGrid, BindingResult bindingResult, Model uiModel) {
         BillingProcurement lastBill = procurementBillingService.getLastBill(vendorMasterService.get(transactionDetailGrid.getTargetId()));
         List<String> errors = validateGrid(transactionDetailGrid);
         if (!errors.isEmpty()) {
             transactionDetailGrid.getErrors().addAll(errors);
             return new ModelAndView("procurement/processTransaction", "procurementTransaction", transactionDetailGrid);
         }
         uiModel.asMap().clear();
         ProcurementTransaction procurementTransaction = transactionDetailGrid.buildProcurementTransaction();
         if (procurementTransaction.getTransactionId() == null || procurementTransaction.getTransactionId() < 1) {
             procurementTransactionHandler.addNewTransaction(procurementTransaction);
         } else {
             procurementTransactionHandler.updateTransaction(procurementTransaction);
         }
         return new ModelAndView("redirect:/procurement/show/" + procurementTransaction.getTransactionId());
     }
 
     @RequestMapping(value = "/updateForm/{id}", method = RequestMethod.GET)
     public ModelAndView updateForm(@PathVariable("id") int transactionId) {
         ProcurementTransaction procurementTransaction = procurementTransactionService.getProcurementTransaction(transactionId);
         TransactionDetailGrid transactionDetailGrid = new TransactionDetailGrid(procurementTransaction);
         BillingProcurement lastBill = procurementBillingService.getLastBill(procurementTransaction.getVendor());
         if (lastBill != null && DateUtils.isGreaterOrEqual(lastBill.getEndDate(), procurementTransaction.getDate())) {
             transactionDetailGrid.setEditable(false);
             transactionDetailGrid.getErrors().add("This transaction has already been billed, hence cannot be edited");
         }
         ModelAndView modelAndView = new ModelAndView("procurement/processTransaction", "procurementTransaction", transactionDetailGrid);
         modelAndView.addObject("vendors", Arrays.asList(procurementTransaction.getVendor()));
         return modelAndView;
     }
 
     @RequestMapping(value = "/list", method = RequestMethod.GET)
     public ModelAndView list() {
         return new ModelAndView("procurement/list");
     }
 
     @RequestMapping(value = "/transactionsInRange", method = RequestMethod.GET)
     public ModelAndView transactionsBetween(@RequestParam(value = "fromDate", required = true) Date fromDate, @RequestParam(value = "toDate", required = true) Date toDate, @RequestParam(value = "vendorId", required = false, defaultValue = "-1") int vendorId) {
 
         List<ProcurementTransaction> procurementTransactions;
         if (vendorId < 1) {
             procurementTransactions = procurementTransactionService.getProcurementTransactions(fromDate, toDate);
         } else {
             Vendor vendor = vendorMasterService.get(vendorId);
             procurementTransactions = procurementTransactionService.getProcurementTransactions(fromDate, toDate, vendor);
         }
         return new ModelAndView("procurement/transactionsInRange", "procurementTransactions", procurementTransactions);
     }
 
     @RequestMapping(value = "/fetchTransactionDetails", method = RequestMethod.POST)
     public ModelAndView fetchTransactionDetails(@Valid TransactionDetailGrid procurementTransactionGrid, BindingResult bindingResult, Model uiModel) {
         if (procurementTransactionGrid.getTransactionId() != null && procurementTransactionGrid.getTransactionId() > 0) {
             ProcurementTransaction procurementTransaction = procurementTransactionService.getProcurementTransaction(procurementTransactionGrid.getTransactionId());
             for (PtDetails ptDetails : procurementTransaction.getTransactionDetails()) {
                 procurementTransactionGrid.addProcurementTransactionDetail(ptDetails);
             }
         } else {
             Float vendorDiscount = procurementTransactionGrid.getTargetId() != null && procurementTransactionGrid.getTargetId() > 0 ? vendorMasterService.get(procurementTransactionGrid.getTargetId()).getVendorDiscount() : null;
             procurementTransactionGrid.addNewItem(vendorDiscount);
         }
         return modelAndViewForProcurementTransactionDetails(procurementTransactionGrid);
     }
 
 
     @RequestMapping(value = "/addItem", method = RequestMethod.POST)
     public ModelAndView addNewItem(@Valid TransactionDetailGrid procurementTransactionGrid, BindingResult bindingResult, Model uiModel) {
         Float vendorDiscount = procurementTransactionGrid.getTargetId() != null && procurementTransactionGrid.getTargetId() > 0 ? vendorMasterService.get(procurementTransactionGrid.getTargetId()).getVendorDiscount() : null;
         procurementTransactionGrid.addNewItem(vendorDiscount);
         return modelAndViewForProcurementTransactionDetails(procurementTransactionGrid);
     }
 
     @RequestMapping(value = "/deleteItem", method = RequestMethod.POST)
     public ModelAndView deleteItem(@Valid TransactionDetailGrid procurementTransactionGrid, BindingResult bindingResult, Model uiModel) {
         procurementTransactionGrid.deleteItems();
         return modelAndViewForProcurementTransactionDetails(procurementTransactionGrid);
     }
 
     @RequestMapping(value = "/itemChanged", method = RequestMethod.POST)
     public ModelAndView itemChanged(@Valid TransactionDetailGrid procurementTransactionGrid, BindingResult bindingResult, Model uiModel) {
         TransactionDetailRow effectedRow = procurementTransactionGrid.getEffectedRow();
         if (effectedRow != null)
             effectedRow.updateItemPrice(itemMasterService.get(effectedRow.getItemCode()).getDefaultPrice());
         return modelAndViewForProcurementTransactionDetails(procurementTransactionGrid);
     }
 
     @RequestMapping(value = "/itemDiscountChanged", method = RequestMethod.POST)
     public ModelAndView itemDiscountChanged(@Valid TransactionDetailGrid procurementTransactionGrid, BindingResult bindingResult, Model uiModel) {
         TransactionDetailRow effectedRow = procurementTransactionGrid.getEffectedRow();
         if (effectedRow != null) effectedRow.updateDiscount();
         return modelAndViewForProcurementTransactionDetails(procurementTransactionGrid);
     }
 
     @RequestMapping(value = "/itemPriceChanged", method = RequestMethod.POST)
     public ModelAndView itemPriceChanged(@Valid TransactionDetailGrid procurementTransactionGrid, BindingResult bindingResult, Model uiModel) {
         TransactionDetailRow effectedRow = procurementTransactionGrid.getEffectedRow();
         if (effectedRow != null) effectedRow.updatePricePerItem();
         return modelAndViewForProcurementTransactionDetails(procurementTransactionGrid);
     }
 
     @RequestMapping(value = "/itemQuantityChanged", method = RequestMethod.POST)
     public ModelAndView itemQuantityChanged(@Valid TransactionDetailGrid procurementTransactionGrid, BindingResult bindingResult, Model uiModel) {
         TransactionDetailRow effectedRow = procurementTransactionGrid.getEffectedRow();
         if (effectedRow != null) effectedRow.updateQuantity();
         return modelAndViewForProcurementTransactionDetails(procurementTransactionGrid);
     }
 
     @RequestMapping(value = "/vendorChanged", method = RequestMethod.POST)
     public ModelAndView vendorChanged(@Valid TransactionDetailGrid procurementTransactionGrid, BindingResult bindingResult, Model uiModel) {
         for (TransactionDetailRow row : procurementTransactionGrid.getTransactionDetails()) {
             row.updateVendorDiscount(vendorMasterService.get(procurementTransactionGrid.getTargetId()).getVendorDiscount());
         }
         return modelAndViewForProcurementTransactionDetails(procurementTransactionGrid);
     }
 
     @RequestMapping(value = "/validateGrid", method = RequestMethod.POST)
     public ModelAndView validateGrid(@Valid TransactionDetailGrid procurementTransactionGrid, BindingResult bindingResult, Model uiModel, HttpServletResponse response) {
         return json(validateGrid(procurementTransactionGrid), response);
     }
 
     @ModelAttribute("procurementTransactionType")
     public Map<Character, String> procurementTransactionType() {
         Map<Character, String> procurementTransactionTypes = new HashMap<Character, String>();
         for (ProcurementTransactionType procurementTransactionType : ProcurementTransactionType.values()) {
             procurementTransactionTypes.put(procurementTransactionType.getCode(), procurementTransactionType.toString());
         }
         return procurementTransactionTypes;
     }
 
     private ModelAndView modelAndViewForProcurementTransactionDetails(TransactionDetailGrid procurementTransactionGrid) {
         for (TransactionDetailRow transactionDetailRow : procurementTransactionGrid.getTransactionDetails()) {
             if (transactionDetailRow.getItemCode() != null && transactionDetailRow.getItemCode() > 0)
                 transactionDetailRow.setIssueDates(procurementTransactionHandler.getStockDetails(itemMasterService.get(transactionDetailRow.getItemCode())));
         }
         ModelAndView modelAndView = new ModelAndView("procurement/editProcurementTransactionDetails", "procurementTransactionGrid", procurementTransactionGrid);
         if (ProcurementTransactionType.PURCHASE.getCode() == procurementTransactionGrid.getType()) {
             modelAndView.addObject("items", itemMasterService.getAll());
         } else if (ProcurementTransactionType.RETURNS.getCode() == procurementTransactionGrid.getType()) {
             modelAndView.addObject("items", itemMasterService.getAllReturnableItems());
         }
         return modelAndView;
     }
 
     private List<String> validateGrid(TransactionDetailGrid grid) {
         List<String> errors = new ArrayList<String>();
         Vendor vendor = vendorMasterService.get(grid.getTargetId());
         BillingProcurement lastBill = procurementBillingService.getLastBill(vendor);
         if (lastBill != null && DateUtils.isGreaterOrEqual(lastBill.getEndDate(), DateUtils.addTimeToDate(grid.getTransactionDate()))) {
             errors.add("Invalid transaction date, Bill has already been generated.");
         }
         return errors;
     }
 }
