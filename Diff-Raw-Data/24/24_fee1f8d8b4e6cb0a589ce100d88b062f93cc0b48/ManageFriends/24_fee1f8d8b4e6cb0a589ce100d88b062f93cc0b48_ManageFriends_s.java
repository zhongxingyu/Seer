 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package facebin.Profile;
 
 import facebin.userData.UserModel;
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author Jorge
  */
 public class ManageFriends {
     
     RandomAccessFile myFriends;
     UserModel MainUser;
     Friend[] friendsList;
     
     public ManageFriends(UserModel user)
     {
         MainUser = user;
         Prepare();
     }
     
     private void Prepare()
     {
         try
         {
             myFriends = new RandomAccessFile(new File("Users/" + MainUser.getEmail() + "/Friends.fbn"), "rws");
         }
         catch(Exception e)
         {
             
         }
     
     }
     
     public Friend[] getFriendsList()
     {
         return this.friendsList;
     }
     
     public void sendUserRequest(String email) throws IOException
     {
         if(RequestSent(email)==false)
         {
             RandomAccessFile friend = new RandomAccessFile(new File("Users/" + email + "/Friends.fbn"), "rws");
             friend.seek(friend.length());
             friend.writeUTF(email);
             friend.writeBoolean(false);
             friend.writeUTF(MainUser.getEmail());
             myFriends.seek(myFriends.length());
             myFriends.writeUTF(email);
             myFriends.writeBoolean(false);
             myFriends.writeUTF(MainUser.getEmail());
         }
         
     }
     
     public void getMyFriends() throws IOException
     {
         int cuantos = 0;
         
         while(myFriends.getFilePointer() < myFriends.length())
         {
             myFriends.readUTF();
             myFriends.readBoolean();
             myFriends.readUTF();
             cuantos += 1;
         }
         
         friendsList = new Friend[cuantos];
         int pos = 0;
         myFriends.seek(0);
         
         while(myFriends.getFilePointer() < myFriends.length())
         {
             friendsList[pos] = new Friend(myFriends.readUTF(), myFriends.readBoolean(), myFriends.readUTF());
             pos += 1;
         }
        
         
     }
     
     private boolean RequestSent(String email) throws IOException
     {
         boolean sent = false;
         
         myFriends.seek(0);
         
         while(myFriends.getFilePointer() < myFriends.length())
         {
             String friendemail = myFriends.readUTF();
             myFriends.readBoolean();
             myFriends.readUTF();
            myFriends.readUTF();
             
             if(friendemail.equals(email))
             {
                 sent = true;
                 myFriends.seek(myFriends.length());
             }
         }
         
         return sent;
     }
     
     public void RespondRequest(String email) throws IOException
     {
             RandomAccessFile friend = new RandomAccessFile(new File("Users/" + email + "/Friends.fbn"), "rws");
             
             long position = LookForRequest(friend, email);
             if(position!=-1)
             {
                 friend.seek(position);
                 friend.readUTF();
                 friend.writeBoolean(true);
                 String who = friend.readUTF();
                 position = LookForRequest(myFriends, email);
                 myFriends.seek(position);
                 myFriends.readUTF();
                 myFriends.writeBoolean(true);
                 myFriends.readUTF();
             
                 JOptionPane.showMessageDialog(null, "I responded to " + who + "'s Request");
             }
         
     }
     
     private long LookForRequest(RandomAccessFile a, String email) throws IOException
     {
         long pos = -1;
         a.seek(0);
         
         while(a.getFilePointer() < a.length())
         {
             pos = a.getFilePointer();
             a.readUTF();
             a.readBoolean();
             String e = a.readUTF();
             
             if(e.equals(email));
             {
                 return pos;
             }
         }
         
         return pos;
         
     }
      
     
 }
