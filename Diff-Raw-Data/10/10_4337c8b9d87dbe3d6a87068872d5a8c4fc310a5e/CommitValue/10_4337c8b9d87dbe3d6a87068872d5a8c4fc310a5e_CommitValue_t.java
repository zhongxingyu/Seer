 package com.github.nyao.gwtgithub.client.values.gitdata;
 
 import com.github.nyao.gwtgithub.client.values.GHValue;
 import com.github.nyao.gwtgithub.client.values.ValueProp;
 
 
 public class CommitValue extends GHValue<CommitValue.Prop> {
 
     public static enum Prop implements ValueProp {
         Message("message"),
         Author("author"),
         Parents("parents"),
         Tree("tree"),
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
     
     public void setMessage(String message) {
         prop.put(Prop.Message, message);
     }
 
     public void setAuthor(CommiterValue author) {
         prop.put(Prop.Author, author);
     }
    
    public void setParent(String parent) {
        String[] v = {parent};
        prop.put(Prop.Parents, v);
    }
 
    public void setParents(String[] parents) {
        prop.put(Prop.Parents, parents);
     }
 
     public void setTree(String tree) {
         prop.put(Prop.Tree, tree);
     }
 }
