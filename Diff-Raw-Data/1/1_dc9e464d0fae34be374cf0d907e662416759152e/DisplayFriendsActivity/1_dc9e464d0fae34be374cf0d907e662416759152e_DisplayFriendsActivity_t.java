 /* Copyright (c) 2008 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sample;
 
 import android.os.Bundle;
 import android.widget.TextView;
 import org.opensocial.client.OpenSocialBatch;
 import org.opensocial.client.OpenSocialClient;
 import org.opensocial.client.OpenSocialProvider;
 import org.opensocial.client.OpenSocialRequest;
 import org.opensocial.client.Token;
 import org.opensocial.data.OpenSocialPerson;
 import org.opensocial.android.OpenSocialActivity;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Demo class. Prompts the user to choose an OpenSocial provider and then displays all of
  * their friends.
  *
  * @author Cassandra Doll
  */
 public class DisplayFriendsActivity extends OpenSocialActivity {
   private static String ANDROID_SCHEME = "x-opensocial-demo-app";
   private static Map<OpenSocialProvider, Token> SUPPORTED_PROVIDERS
       = new HashMap<OpenSocialProvider, Token>();
 
   static {
     // Setup all the OpenSocial containers you want to integrate with
     // and set your specific consumer token, if the container requires one.
     // example: SUPPORTED_PROVIDERS.put(OpenSocialProvider.MYSPACE,
     //     new Token(<your consumer key like "http://www.myspace.com/..." >, <your secret>));
     SUPPORTED_PROVIDERS.put(OpenSocialProvider.PLAXO, new Token("anonymous", ""));
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     OpenSocialClient client = getOpenSocialClient(SUPPORTED_PROVIDERS, ANDROID_SCHEME);
 
     // If the client is null the OpenSocialChooserActivity will be started
     if (client != null) {
       showContacts(client);
     }
   }
 
   private void showContacts(OpenSocialClient c) {
     List<OpenSocialPerson> friends;
     try {
       if (provider.isOpenSocial) {
         friends = c.fetchFriends();
       } else {
         OpenSocialBatch batch = new OpenSocialBatch();
         batch.addRequest(new OpenSocialRequest("@me/@all", ""), "friends");
         friends = batch.send(c).getItemAsPersonCollection("friends");
       }
     } catch (Exception e) {
       // TODO: Reduce the number of exceptions the library throws
       throw new RuntimeException(e);
     }
 
     if (friends.size() > 0) {
       FriendListView contactsView = new FriendListView(this);
       contactsView.setFriends(friends);
       setContentView(contactsView);
     } else {
       TextView textView = new TextView(this);
       textView.setText("No contacts found.");
       setContentView(textView);
     }
   }
 
 }
