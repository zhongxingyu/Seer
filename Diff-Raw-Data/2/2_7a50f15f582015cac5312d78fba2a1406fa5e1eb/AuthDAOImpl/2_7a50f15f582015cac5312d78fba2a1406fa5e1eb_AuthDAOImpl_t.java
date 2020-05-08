 package org.openblend.prostalytics.auth.dao.impl;
 
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.common.collect.Lists;
 import org.openblend.prostalytics.auth.dao.AuthDAO;
 import org.openblend.prostalytics.auth.dao.UserDAO;
 import org.openblend.prostalytics.dao.impl.AbstractDAOImpl;
 import org.openblend.prostalytics.auth.domain.Token;
 import org.openblend.prostalytics.auth.domain.User;
 
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 /**
  * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
  */
 public class AuthDAOImpl extends AbstractDAOImpl implements AuthDAO {
 
     @Inject
     private UserDAO userDao;
 
     @Override
     public User findUserByToken(String token) {
         Query query = new Query(Token.KIND);
 
         List<Query.Filter> filters = new ArrayList<Query.Filter>();
         filters.add(new Query.FilterPredicate(Token.TOKEN, Query.FilterOperator.EQUAL, token));
         query.setFilter(new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters));
 
         PreparedQuery pq = ds.prepare(query);
         List<Token> tokens = Lists.transform(pq.asList(FetchOptions.Builder.withDefaults()), Token.FN);
 
         if (tokens.size() == 0)
             return null;
 
         long id = tokens.get(0).getUserId();
         return userDao.loadUser(id);
     }
 
     @Override
     public Token updateOrCreateToken(User user, String oldToken) {
         // TODO - Implement method
         return null;
     }
 
     @Override
     public User authenticate(String username, String passwordHash) {
         Query query = new Query(User.KIND);
 
         List<Query.Filter> filters = new ArrayList<Query.Filter>();
        filters.add(new Query.FilterPredicate(User.USERNAME, Query.FilterOperator.EQUAL, username));
         filters.add(new Query.FilterPredicate(User.PASSWORD, Query.FilterOperator.EQUAL, passwordHash));
         query.setFilter(new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters));
 
         PreparedQuery pq = ds.prepare(query);
         List<User> users = Lists.transform(pq.asList(FetchOptions.Builder.withDefaults()), User.FN);
 
         if (users.size() == 0)
             return null;
 
         return users.get(0);
     }
 
     public long saveToken(final Token token) {
         return inTx(new Callable<Long>() {
             public Long call() throws Exception {
                 return ds.put(token.toEntity()).getId();
             }
         });
     }
 }
