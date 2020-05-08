 package com.vacuumhead.wesplit.tables;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Vinay
  * Date: 23/3/13
  * Time: 2:17 AM
  * To change this template use File | Settings | File Templates.
  */
 @Entity
@Table(name="GROUP")
 public class Group implements Serializable {
     @Id
     @Column(name="GROUP_ID")
     @GeneratedValue(strategy = GenerationType.AUTO)
     private int groupId=1;
 
     @Column(name="GROUP_NAME")
     private String groupName;
 
     @ManyToMany(mappedBy = "groupList",cascade = CascadeType.ALL)
     private List<User> userList=new ArrayList<User>();
 
     public Group(String groupName) {
         this.groupName = groupName;
     }
 
     public Group() {
     }
 
     public List<User> getUserList() {
         return userList;
     }
 
     public void setUserList(List<User> userList) {
         this.userList = userList;
     }
 
     public int getGroupId() {
         return groupId;
     }
 
     public void setGroupId(int groupId) {
         this.groupId = groupId;
     }
 
     public String getGroupName() {
         return groupName;
     }
 
     public void setGroupName(String groupName) {
         this.groupName = groupName;
     }
 }
