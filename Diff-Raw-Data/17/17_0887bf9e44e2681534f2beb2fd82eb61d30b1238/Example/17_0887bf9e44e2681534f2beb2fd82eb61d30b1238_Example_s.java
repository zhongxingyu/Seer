 package me.nrubin29.skypeapi;
 
 public class Example {
 
     public static void main(String[] args) {
 
        /* Open Skype and get focus. */

        SkypeAPI.open();

        /* * * * */

         /* Get a Skype user. */
 
         User user = SkypeAPI.getUser("name");
 
         /* Call the user. */
 
         user.call(CallType.VIDEO);
 
         /* Open a chat with the user. */
 
         user.chat();
 
         /* * * * */
 
         /* Create a Skype group. */
 
         Group group = SkypeAPI.createGroup("topic", "person1", "person2", "person3");
 
         /* Call the group. */
 
         group.call(CallType.VIDEO);
 
         /* Open a chat with the group. */
 
         group.chat();
 
         /* * * * */
     }
 }
