 /*
  * Copyright (c) <2013> <Jim Johnson jimj@jimj.net>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package net.jimj.automaton;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import net.jimj.automaton.model.User;
 import org.mongojack.DBCursor;
 import org.mongojack.DBUpdate;
 import org.mongojack.JacksonDBCollection;
 
import java.util.Map;

 public class UserAccess {
     private static final String USER_NICK = "nick";
     private static final String USER_PREFS = "preferences";
 
     private JacksonDBCollection<User, String> users;
 
     public UserAccess(DBCollection users) {
         this.users = JacksonDBCollection.wrap(users, User.class, String.class);
     }
 
     public User findUser(String nick) {
         User user = new User(nick);
 
         DBCursor<User> userCur = users.find(new BasicDBObject(USER_NICK, nick.toLowerCase()));
         if(userCur.hasNext()) {
             user = userCur.next();
             user.setKnown(true);
         }
         return user;
     }
 
     public void addUser(String nick, String realName) {
         User user = new User(nick);
         user.setRealName(realName);
         users.insert(user);
     }
 
     public void updateUserPreference(User user) {
         users.findAndModify(new BasicDBObject(USER_NICK, user.getNick().toLowerCase()),
             DBUpdate.set(USER_PREFS,user.getPreferences()));
     }
 }
