 package com.potatorental.service;
 
 import com.potatorental.model.Account;
 import com.potatorental.model.Account.AccountType;
 import com.potatorental.model.Customer;
 import com.potatorental.model.Movie;
 import com.potatorental.repository.AccountDao;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 import javax.sql.DataSource;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: milky
  * Date: 5/8/13
  * Time: 5:08 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AccountDaoImpl implements AccountDao {
 
     private JdbcTemplate jdbcTemplate;
 
     @Autowired
     public AccountDaoImpl(DataSource dataSource) {
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Override
     public AccountType getType(Customer customer) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Account getAccount(Customer customer) {
         String sql = "select * from account where customer = ?";
         return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Account.class), customer.getSsn());
     }
 
     @Override
     public List<Movie> getQueue(Account account) {
         String sql = "select m.name, m.id, m.type, m.distrfee, m.numcopies, m.rating " +
                 "from movie m, movieq q " +
                 "where m.id = q.movieid and accountid = ? ";
 
         return PotatoService.addMoviesFromMap(jdbcTemplate.queryForList(sql, account.getId()));
     }
 
     @Override
     public List<Movie> getHistory(Account account) {
        String sql = "select m.name, m.id, p.datetime, p.returndate, p.id " +
                 "from rental r, purchase p, movie m " +
                 "where m.id = r.movieid and p.id = r.purchid and r.accountid = ?";
         return PotatoService.addMoviesFromMap(jdbcTemplate.queryForList(sql, account.getId()));
     }
 
     @Override
     public void createAccount(Customer customer, Account account) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
