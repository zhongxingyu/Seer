 package az.his.controllers;
 
 import az.his.DBUtil;
 import az.his.jaxb.JaxCategoryList;
 import az.his.jaxb.JaxTransactionList;
 import az.his.jaxb.JaxUserList;
 import az.his.persist.Account;
 import az.his.persist.Transaction;
 import az.his.persist.TransactionCategory;
 import az.his.persist.User;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.List;
 
 @Controller
 @RequestMapping("/api")
 public class ApiController {
 
     @RequestMapping("/users")
     @ResponseBody
     public JaxUserList getUsers() {
         List<User> users = User.getAll();
 
         JaxUserList jaxUserList = new JaxUserList();
         for (User dbUser : users) {
             JaxUserList.User user = new JaxUserList.User();
             user.id = dbUser.getId();
             user.name = dbUser.getName();
             jaxUserList.users.add(user);
         }
 
         return jaxUserList;
     }
 
     @RequestMapping("/cats")
     @ResponseBody
     public JaxCategoryList getCategories(@RequestParam("uid") int uid) {
        List<TransactionCategory> cats = TransactionCategory.getAll();
         JaxCategoryList catList = new JaxCategoryList();
 
         for (TransactionCategory cat : cats) {
             catList.addCategory(cat.getId(), cat.getName());
         }
 
         return catList;
     }
 
     @RequestMapping(value = "/trans", method = RequestMethod.POST)
     @Transactional
    public ResponseEntity<String> postTransactions(@RequestBody JaxTransactionList transactionList){
         int uid = transactionList.uid;
         DBUtil dbUtil = DBUtil.getInstance();
 
         User user = User.getById(uid);
        if(user == null) {
             return new ResponseEntity<>(HttpStatus.FORBIDDEN);
         }
 
         for (JaxTransactionList.Transaction tr : transactionList.transactions) {
             Transaction trans = new Transaction();
             trans.setActor(user);
             trans.setAccount(Account.getCommon());
             trans.setTimestmp(tr.date);
             trans.setAmount(tr.amount * 100);
             trans.setCategory(dbUtil.get(TransactionCategory.class, tr.cat));
             trans.setComment(null);
             trans.setCommon(true);
             dbUtil.persist(trans);
         }
 
         return new ResponseEntity<>(HttpStatus.CREATED);
     }
 }
