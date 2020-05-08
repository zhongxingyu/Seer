 package com.noodlesandwich.quacker.features;
 
 import java.time.Instant;
 import com.noodlesandwich.quacker.id.Id;
 import com.noodlesandwich.quacker.users.User;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.hamcrest.TypeSafeDiagnosingMatcher;
 
 public class InspectibleMessage {
     public final Id id;
     public final User author;
     public final String text;
     public final Instant timestamp;
 
     public InspectibleMessage(Id id, User author, String text, Instant timestamp) {
         this.id = id;
         this.author = author;
         this.text = text;
         this.timestamp = timestamp;
     }
 
     public static class InspectibleMessageMatcher extends TypeSafeDiagnosingMatcher<InspectibleMessage> {
         private final String expectedAuthorUsername;
         private final String expectedText;
 
         public InspectibleMessageMatcher(String authorUsername, String text) {
             this.expectedAuthorUsername = authorUsername;
             this.expectedText = text;
         }
 
         public static InspectibleMessageMatcherBuilder anInspectibleMessageFrom(String authorUsername) {
             return new InspectibleMessageMatcherBuilder(authorUsername);
         }
 
         @Override
         public void describeTo(Description description) {
            description.appendText("a message from ").appendValue(expectedAuthorUsername)
                        .appendText(" stating ").appendValue(expectedText);
         }
 
         @Override
         protected boolean matchesSafely(InspectibleMessage message, Description mismatchDescription) {
            String actualAuthorUsername = message.author.getUsername();
             String actualText = message.text;
 
            mismatchDescription.appendText("was a message from ").appendValue(actualAuthorUsername)
                                .appendText(" stating ").appendValue(actualText);
 
            return expectedAuthorUsername.equals(actualAuthorUsername)
                 && expectedText.equals(actualText);
         }
 
         public static class InspectibleMessageMatcherBuilder {
             private final String authorUsername;
 
             public InspectibleMessageMatcherBuilder(String authorUsername) {
                 this.authorUsername = authorUsername;
             }
 
             public Matcher<? super InspectibleMessage> stating(String text) {
                 return new InspectibleMessageMatcher(authorUsername, text);
             }
         }
     }
 }
