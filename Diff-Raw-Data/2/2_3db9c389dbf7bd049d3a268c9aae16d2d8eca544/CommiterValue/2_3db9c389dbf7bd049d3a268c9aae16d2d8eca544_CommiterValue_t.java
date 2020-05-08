 package com.github.nyao.gwtgithub.client.values.gitdata;
 
 import com.github.nyao.gwtgithub.client.values.GHValue;
 import com.github.nyao.gwtgithub.client.values.ValueProp;
 
 
 public class CommiterValue extends GHValue<CommiterValue.Prop> {
 
     public static enum Prop implements ValueProp {
         Date("date"),
        Name("name"),
         Email("email"),
         ;
         public final String value;
 
         private Prop(String value) {
             this.value = value;
         }
 
         @Override
         public String value() {
             return value;
         }
     }
     
     public void setDate(String date) {
         prop.put(Prop.Date, date);
     }
 
     public void setName(String name) {
         prop.put(Prop.Name, name);
     }
 
     public void setEmail(String email) {
         prop.put(Prop.Email, email);
     }
 }
