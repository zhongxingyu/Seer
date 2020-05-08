 package az.his.controllers;
 
 import az.his.AuthUtil;
 import az.his.DBManager;
 import az.his.DBUtil;
 import az.his.persist.Account;
 import az.his.persist.Transaction;
 import az.his.persist.TransactionCategory;
 import az.his.persist.User;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.annotation.Resource;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.*;
 
 @Controller
 @RequestMapping("/account")
 public class AccountController {
 
     @Resource(name = "dbUtil")
     private DBUtil dbUtil;
 
     @RequestMapping(method = RequestMethod.GET)
     @Transactional(readOnly = true)
     public String accountPage(Model model) throws ServletException {
         List<User> usersNotMe = User.getAll(dbUtil.getDbManager());
 
         User userMe = null;
         for (User user : usersNotMe) {
             if (user.getId() == AuthUtil.getUid()) {
                 userMe = user;
                 usersNotMe.remove(user);
                 break;
             }
         }
         if (userMe == null) throw new ServletException("Where is users table?!");
         model.addAttribute("users", usersNotMe);
         model.addAttribute("me", userMe);
 
         return "account";
     }
 
     @RequestMapping(value = "/catdata")
     @Transactional(readOnly = true)
     public void categoryData(@RequestParam(value = "type", required = false) String type, HttpServletResponse resp)
             throws ServletException, IOException {
         DBManager dbman = dbUtil.getDbManager();
 
         if (type == null) type = "a";
 
         List<TransactionCategory> cats;
         JSONObject ret = new JSONObject();
         JSONArray items = new JSONArray();
         if (type.equals("e")) {
             cats = TransactionCategory.getByType(dbman, TransactionCategory.CatType.EXP);
         } else if (type.equals("i")) {
             cats = TransactionCategory.getByType(dbman, TransactionCategory.CatType.INC);
         } else {
             cats = dbman.findAll(TransactionCategory.class);
         }
 
         try {
             ret.put("identifier", "id");
             ret.put("label", "name");
 
             JSONObject item = new JSONObject();
 
             if (type.equals("a")) {
                 item.put("id", "0");
                 item.put("name", "(Все)");
                 item.put("type", "null");
                 items.put(item);
             }
 
             for (TransactionCategory cat : cats) {
                 item = new JSONObject();
 
                 item.put("id", cat.getId());
                 item.put("name", cat.getName());
                 String oType;
                 switch (cat.getType()) {
                     case EXP:
                         oType = "e";
                         break;
                     case INC:
                         oType = "i";
                         break;
                     case NONE:
                         oType = "n";
                         break;
                     default:
                         oType = "null";
                 }
                 item.put("type", oType);
 
                 items.put(item);
             }
             ret.put("items", items);
         } catch (JSONException e) {
             throw new ServletException(e);
         }
 
         resp.setCharacterEncoding("UTF-8");
         resp.setContentType("application/json");
         resp.getWriter().append(ret.toString());
     }
 
     /**
      * Adds a transaction or two (see parameters description).
      *
      * @param actId     Actor of transaction.
      * @param rawAmount Amount in basic currency (copecks).
      * @param trType    Transaction type. "i" for donation, "r" for refund, "p" for expence and "a" for expence from
      *                  account. The last value will mean that two transactions will be created.
      * @param catId     Category ID. For "i" and "r" transactions may be null.
      * @param catName   Category name. Should be not null if transaction is not of "i" or "r" type and category ID is 0.
      *                  That means new category.
      * @param rawDate   Unix date of transaction.
      * @param comment   Comment to transaction.
      * @throws ServletException Standart
      * @return Empty JSON as nothing needs to be returned
      */
     @RequestMapping(value = "/data", method = RequestMethod.POST, params = "act=put")
     @Transactional
     @ResponseBody
     public String addTransaction(
             @RequestParam("actor") int actId,
             @RequestParam("amount") float rawAmount,
             @RequestParam("type") String trType,
             @RequestParam(value = "cat", required = false) Integer catId,
             @RequestParam(value = "catname", required = false) String catName,
             @RequestParam("date") long rawDate,
             @RequestParam("comment") String comment
     ) throws ServletException {
 
        int amount = Math.round(rawAmount) * 100;
         boolean common;
         DBManager dbman = dbUtil.getDbManager();
 
         if (trType.equals("i")) {
             catId = TransactionCategory.CAT_DONATE;
             common = false;
         } else if (trType.equals("r")) {
             catId = TransactionCategory.CAT_REFUND;
             amount = -amount;
             common = false;
         } else {
             if (catId == null) throw new ServletException("Category ID should be filled (param 'cat')");
             amount = -amount;
             common = true;
         }
 
         TransactionCategory cat;
         if (catId == 0) {
             // New category if needed
             if (catName == null) throw new ServletException("Category name should be filled (param 'catname')");
 
             List<TransactionCategory> cats = dbman.findByProperty(TransactionCategory.class, "name", catName);
             if (cats.size() == 0) {
                 // New category
                 cat = new TransactionCategory();
                 cat.setName(catName);
                 cat.setType(TransactionCategory.CatType.EXP);
                 cat = dbman.merge(cat);
             } else {
                 // Reuse category
                 cat = cats.get(0);
             }
         } else {
             cat = dbman.get(TransactionCategory.class, catId);
         }
 
         Date time = new Date(rawDate);
         User actor = dbman.get(User.class, actId);
 
         Account account = Account.getCommon(dbman);
 
         Transaction trans = new Transaction();
         trans.setActor(actor);
         trans.setAccount(account);
         trans.setTimestmp(time);
         trans.setAmount(amount);
         trans.setCategory(cat);
         trans.setComment(comment);
         trans.setCommon(common);
         dbman.persist(trans);
 
         if (trType.equals("a")) {
             // Expense form account itself means we should make immediate refund
             trans = new Transaction();
             trans.setActor(actor);
             trans.setAccount(account);
             trans.setTimestmp(time);
             trans.setAmount(amount);
             trans.setCategory(dbman.get(TransactionCategory.class, TransactionCategory.CAT_REFUND));
             trans.setCommon(false);
             dbman.persist(trans);
         }
         dbman.flush();
 
         return "{}";
     }
 
     /**
      * Delete some transactions. If category becomes empty, delete it too.
      *
      * @param rawIds Transaction ID comma-separated list.
      * @throws ServletException Standart
      * @return Empty string
      */
     @RequestMapping(value = "/data", method = RequestMethod.POST, params = "act=del")
     @ResponseBody
     @Transactional
     public String delTransactions(
             @RequestParam("ids") String rawIds
     ) throws ServletException {
         String[] ids = rawIds.split(",");
         DBManager dbman = dbUtil.getDbManager();
 
         for (String id : ids) {
             try {
                 int iid = Integer.parseInt(id);
                 dbman.delete(Transaction.class, iid);
                 dbman.flush();
             } catch (Exception e) {
                 throw new ServletException(e);
             }
         }
         return "";
     }
 
     @RequestMapping(value = "/stats", method = RequestMethod.GET)
     @Transactional(readOnly = true)
     public void getStatistic(HttpServletResponse resp) throws JSONException, IOException {
         JSONObject ret = new JSONObject();
         DBManager dbman = dbUtil.getDbManager();
 
         Account acc = Account.getCommon(dbman);
         long totalExp = acc.getTotalExp(dbman);
         long eachExp = totalExp / 2;
         User user = dbman.get(User.class, AuthUtil.getUid());
         long persExp = user.getPersonalExpense(dbman, acc);
         long persDonation = user.getPersonalDonation(dbman, acc);
 
         ret.put("amount", acc.getAmountPrintable());
         ret.put("totalExp", DBUtil.formatCurrency(totalExp));
         ret.put("eachExp", DBUtil.formatCurrency(eachExp));
         ret.put("persExp", DBUtil.formatCurrency(persExp));
         ret.put("persDonation", DBUtil.formatCurrency(persDonation));
         ret.put("persSpent", DBUtil.formatCurrency(persDonation + persExp));
         ret.put("persBalance", DBUtil.formatCurrency(persDonation + persExp - eachExp));
 
         resp.setCharacterEncoding("UTF-8");
         resp.setContentType("application/json");
         resp.getWriter().append(ret.toString());
     }
 
     @RequestMapping(value = "/data", method = RequestMethod.GET)
     @Transactional(readOnly = true)
    private void getTransactions(
             HttpServletResponse resp,
             @RequestParam(value = "from", required = false) Long rawFrom,
             @RequestParam(value = "to", required = false) Long rawTo,
             @RequestParam(value = "cat", required = false) Integer cat
     ) throws JSONException, IOException {
         JSONObject ret = new JSONObject();
         DBManager dbman = dbUtil.getDbManager();
         TimeZone.setDefault(TimeZone.getTimeZone("Asia/Baku"));
 
         Calendar calendar = new GregorianCalendar();
         if (rawFrom != null && rawFrom != 0) {
             calendar.setTimeInMillis(rawFrom);
         } else {
             calendar.set(Calendar.DAY_OF_MONTH, 1);
         }
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         Date fromDate = calendar.getTime();
 
         calendar = new GregorianCalendar();
         calendar.add(Calendar.MONTH, 1);
         if (rawTo != null && rawTo != 0) {
             // Plus day to include "to" date to filter
             calendar.setTimeInMillis(rawTo);
             calendar.add(Calendar.DAY_OF_MONTH, 1);
         }
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         Date toDate = calendar.getTime();
 
         if (cat == null) cat = 0;
 
         List<Transaction> transactions = Transaction.getFiltered(dbman, fromDate, toDate, cat);
         JSONArray items = new JSONArray();
 
         ret.put("identifier", "id");
 
         for (Transaction transaction : transactions) {
             items.put(transaction.getJson());
         }
 
         ret.put("items", items);
 
         resp.setCharacterEncoding("UTF-8");
         resp.setContentType("application/json");
         resp.getWriter().append(ret.toString());
     }
 }
