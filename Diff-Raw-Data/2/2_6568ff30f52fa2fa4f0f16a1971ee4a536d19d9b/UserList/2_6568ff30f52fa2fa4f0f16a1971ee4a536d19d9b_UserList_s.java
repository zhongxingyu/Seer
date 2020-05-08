 /*
  * Copyright 2013 The Last Crusade ContactLastCrusade@gmail.com
  * 
  * This file is part of SoundStream.
  * 
  * SoundStream is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SoundStream is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SoundStream.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.lastcrusade.soundstream.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 import com.lastcrusade.soundstream.util.DefaultParcelableCreator;
 
 public class UserList implements Parcelable{
 
     //this is REQUIRED for Parcelable to work properly
     public static final Parcelable.Creator<UserList> CREATOR = new DefaultParcelableCreator(UserList.class);
 
     
     public static final String ACTION_USER_LIST_UPDATE = UserList.class.getName() + ".action.UserList";
 
     private List<User> connectedUsers;    
     private UserColors userColors;
     private final String TAG = UserList.class.toString();
 
     public UserList(){
         userColors = new UserColors();
         connectedUsers = new ArrayList<User>();
     }
 
     public UserList(String bluetoothID, String macAddress){
        //calls the constructor that initializes everything
         this();
         connectedUsers.add(new User(bluetoothID, macAddress, userColors.getNextAvailableColor()));
     }
     
     public UserList(Parcel in){
         //calls the constructor that initializes everything
         this();
         int numUsers = in.readInt();
         for(int i=0; i<numUsers; i++){
             addUser(in.readString(), in.readString());
         }
     }
 
     public void addUser(String bluetoothID, String macAddress){
         //check to make sure that the user isn't already in the list before adding
         if(getUserByMACAddress(macAddress)==null){
             connectedUsers.add(new User(bluetoothID, macAddress, userColors.getNextAvailableColor()));
         }
     }
 
     //untested
     public void removeUser(String macAddress){
         int removeIndex = -1;
         for(int i=0; i<connectedUsers.size(); i++){
             if(connectedUsers.get(i).getMacAddress().equals(macAddress)){
                 removeIndex = i;
             }
         }
         if(removeIndex >-1){
             int color = connectedUsers.get(removeIndex).getColor();
             userColors.returnColor(color);
             connectedUsers.remove(removeIndex);
         }
         
         reassignColors();
         
     }
     
    public void reassignColors(){
         userColors.clear();
         for(User u: connectedUsers){
             u.setColor(userColors.getNextAvailableColor());
         }
     }
 
     public void copyFrom(UserList userList) {
         this.userColors = userList.userColors;
         this.connectedUsers.clear();
         this.connectedUsers.addAll(userList.connectedUsers);
     }
 
     public List<User> getUsers(){
         return connectedUsers;
     }
 
     //get a list of bluetoothIDs of the connected users
     public List<String> getBluetoothIDs(){
         ArrayList<String> bluetoothIDs = new ArrayList<String>();
 
         for(User u:connectedUsers){
             bluetoothIDs.add(u.getBluetoothID());
         }
         
         return bluetoothIDs;
     }
     
     //get a list of macAddresses of the connected users
     public List<String> getMacAddresses(){
         ArrayList<String> macAddresses = new ArrayList<String>();
 
         for(User u:connectedUsers){
             macAddresses.add(u.getMacAddress());
         }
         
         return macAddresses;
     }
     
     //using macAddress instead of bluetooth id to make sure that
     //it is unique
     public User getUserByMACAddress(String macAddress){
         User user = null;
         if(macAddress == null){
             //TODO May want to make sure this is a valid mac as well and
             // possibly throw an exception
             Log.wtf(TAG, "Cannot check for null mac address");
         } else {
             for (User u : connectedUsers) {
                 if (u.getMacAddress().equals(macAddress)) {
                     user = u;
                 }
             }
         }
         return user;
     }
 
     @Override
     public String toString() {
         String users;
         if(connectedUsers == null){
             users = "No connected users";
         } else {
             StringBuilder sb = new StringBuilder();
             for(User user: connectedUsers){
                 sb.append(user.getBluetoothID());
                 sb.append(':');
                 sb.append(user.getMacAddress());
                 sb.append('\n');
             }
             users = sb.toString();
         }
         return users;
     }
 
     //required for parcelable
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeInt(connectedUsers.size());
         
         for(User u:connectedUsers){
             dest.writeString(u.getBluetoothID());
             dest.writeString(u.getMacAddress());
         }
         
     }
 
     public void clear() {
         this.connectedUsers.clear();
         this.userColors.clear();
     }
 }
