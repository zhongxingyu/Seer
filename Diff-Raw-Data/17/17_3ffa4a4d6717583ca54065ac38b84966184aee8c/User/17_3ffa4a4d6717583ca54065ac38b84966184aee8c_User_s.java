 package com.jinheyu.lite_mms.data_structures;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import com.jinheyu.lite_mms.DepartmentLeaderActivity;
 import com.jinheyu.lite_mms.TeamLeaderActivity;
 import com.jinheyu.lite_mms.LoaderMainActivity;
 import com.jinheyu.lite_mms.LogInActivity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by xc on 13-8-12.
  */
 public class User implements Parcelable {
     public static final Creator<User> CREATOR = new Creator<User>() {
         @Override
         public User createFromParcel(Parcel source) {
             return new User(source);
         }
 
         @Override
         public User[] newArray(int size) {
             return new User[0];
         }
     };
     public static final int DEPARTMENT_LEADER = 1;
     public static final int TEAM_LEADER = 2;
     public static final int LOADER = 3;
     private String userName;
     private String token;
     private int groupId;
     private int id;
     private int[] teamIdList;
     private int[] departmentIdList;
 
     public User(int id, String userName, String token, int groupId, int[] teamIdList, int[] departmentIdList) {
         this.id = id;
         this.userName = userName;
         this.token = token;
         this.groupId = groupId;
         this.teamIdList = teamIdList;
         this.departmentIdList = departmentIdList;
     }
 
     public User(Parcel parcel) {
         this.id = parcel.readInt();
         this.userName = parcel.readString();
         parcel.readIntArray(this.departmentIdList);
         parcel.readIntArray(this.teamIdList);
         this.groupId = parcel.readInt();
         this.token = parcel.readString();
     }
 
     /**
      * Describe the kinds of special objects contained in this Parcelable's
      * marshalled representation.
      *
      * @return a bitmask indicating the set of special object types marshalled
      * by the Parcelable.
      */
     @Override
     public int describeContents() {
         return 0;
     }
 
     /**
      * Flatten this object in to a Parcel.
      *
      * @param dest  The Parcel in which the object should be written.
      * @param flags Additional flags about how the object should be written.
      *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
      */
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeInt(id);
         dest.writeInt(groupId);
         dest.writeIntArray(teamIdList);
         dest.writeIntArray(departmentIdList);
         dest.writeString(userName);
         dest.writeString(token);
     }
 
     public Class getDefaultActivity() {
         switch (groupId) {
             case LOADER:
                 return LoaderMainActivity.class;
             case TEAM_LEADER:
                 return TeamLeaderActivity.class;
             case DEPARTMENT_LEADER:
                 return DepartmentLeaderActivity.class;
             default:
                 return LogInActivity.class;
         }
     }
 
     public int[] getDepartmentIdList() {
         return departmentIdList;
     }
 
     public List<Department> getDepartmentList() {
         return getDepartmentListByIds(departmentIdList);
     }
 
     private List<Department> getDepartmentListByIds(int[] departmentIdList) {
         List<Department> departmentList = new ArrayList<Department>();
         for (int i : departmentIdList) {
             departmentList.add(Department.getDepartmentById(i));
         }
         return departmentList;
     }
 
     public int getGroupId() {
         return groupId;
     }
 
     public int getId() {
         return id;
     }
 
     public int[] getTeamIdList() {
         return teamIdList;
     }
 
     public List<Team> getTeamList() {
        return getTeamListByIds(departmentIdList);
     }
 
     private List<Team> getTeamListByIds(int[] teamIdList) {
         List<Team> teamList = new ArrayList<Team>();
         for (int i : teamIdList) {
             teamList.add(Team.getTeamById(i));
         }
         return teamList;
     }
 
     public String getToken() {
         return token;
     }
 
     public String getUserName() {
         return userName;
     }
 
     public boolean isDepartmentLeader() {
         return groupId == DEPARTMENT_LEADER;
     }
 }
