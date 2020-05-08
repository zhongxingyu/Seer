 package com.burningman.tictactoe;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * Created by Anton on 24.05.13.
  */
 
 //TODO: The Field class inside the Core class is completely unnecessary
 
 //The Core is the part of the app, that is responsible for saving the field and the marks
 public class Core implements Parcelable {
     private Field field;
 
     public Core() {
         field = new Field();
     }
 
     public Core(Parcel in) {
         readFromParcel(in);
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
        int[] fieldInt = new int[9];
         int counter = 0;
         for (int i = 0; i < 3; i++)
             for (int j = 0; j < 3; j++)
                 fieldInt[counter++] = (get_Field(j, i) == Mark.empty) ? 0 : (get_Field(j, i) == Mark.nought) ? -1 : 1;
         for (int i = 0; i < 3; i++)
             dest.writeIntArray(fieldInt);
     }
 
     private void readFromParcel(Parcel in) {
         int counter = 0;
         int[] tmp = new int[9];
         in.readIntArray(tmp);
         for (int i = 0; i < 2; i++) {
             Mark mark = (tmp[counter]) == 0 ? Mark.empty : ((tmp[counter++] < 0) ? Mark.nought : Mark.cross);
             set_Field(0, 3 * i, mark);
         }
     }
 
     public static final Parcelable.Creator<Core> CREATOR = new Parcelable.Creator<Core>() {
         public Core createFromParcel(Parcel in) {
             return new Core(in);
         }
 
         public Core[] newArray(int size) {
             return new Core[size];
         }
     };
 
     public Mark get_Field(int n, int m) {
         return field.get_field(n, m);
     }
 
     public Mark get_Field(int n) {
         if (n < 4)
             return field.get_field(0, n - 1);
         if (n < 7)
             return field.get_field(1, n - 4);
         return field.get_field(2, n - 7);
     }
 
     public void setField(int n, Mark mark) {
         if (n < 4)
             field.set_field(0, n - 1, mark);
         else if (n < 7)
             field.set_field(1, n - 4, mark);
         else if (n < 10)
             field.set_field(2, n - 7, mark);
     }
 
     public void set_Field(int n, int m, Mark mark) {
         field.set_field(n, m, mark);
     }
 
     public class Field {
         private Mark[][] field;
 
         public Field() {
             field = new Mark[3][3];
             for (int i = 0; i < 3; i++)
                 for (int j = 0; j < 3; j++)
                     field[i][j] = Mark.empty;
         }
 
         public void set_field(int n, int m, Mark Mark) {
             field[n][m] = Mark;
         }
 
         public Mark get_field(int n, int m) {
             return field[n][m];
         }
 
 
     }
 
     public Mark search_won() {
         for (int i = 0; i < 3; i++)
             if (field.get_field(0, i) == field.get_field(1, i) && field.get_field(0, i) == field.get_field(2, i) && field.get_field(1, i) != Mark.empty)
                 return field.get_field(0, i);
         for (int i = 0; i < 3; i++)
             if (field.get_field(i, 0) == field.get_field(i, 1) && field.get_field(i, 0) == field.get_field(i, 2) & field.get_field(i, 1) != Mark.empty)
                 return field.get_field(i, 0);
         if (field.get_field(0, 0) == field.get_field(1, 1) && field.get_field(0, 0) == field.get_field(2, 2) & field.get_field(1, 1) != Mark.empty)
             return field.get_field(0, 0);
         if (field.get_field(2, 0) == field.get_field(1, 1) && field.get_field(2, 0) == field.get_field(0, 2) & field.get_field(1, 1) != Mark.empty)
             return field.get_field(2, 0);
         return Mark.empty;
     }
 
     public boolean check_draw() {
         for (int i = 0; i < 3; i++)
             for (int j = 0; j < 3; j++)
                 if (get_Field(i, j) == Mark.empty)
                     return false;
         return true;
     }
 
 
 }
