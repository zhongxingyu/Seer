 package edu.cshl.schatz.jnomics.manager.client;
 
 import edu.cshl.schatz.jnomics.manager.client.ann.Flag;
 import edu.cshl.schatz.jnomics.manager.client.ann.FunctionDescription;
 import edu.cshl.schatz.jnomics.manager.client.ann.Parameter;
 import org.apache.commons.lang3.StringUtils;
 
 import java.lang.reflect.Field;
 import java.util.*;
 
 /**
  * User: james
  */
 public class Utility {
 
     public static String nullToString(String n){
         return n == null ? "" : n;
     }
 
     public static String helpFromParameters(Class<? extends ClientFunctionHandler> c){
         StringBuilder builder = new StringBuilder();
         builder.append("\n");
 
         FunctionDescription functionDescription;
         if(null != (functionDescription = c.getAnnotation(FunctionDescription.class))){
             builder.append(functionDescription.description());
             builder.append("\n");
         }
 
         HashMap<String,ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();
         for(Field field: c.getDeclaredFields()){
             Parameter p = field.getAnnotation(Parameter.class);
             Flag f = field.getAnnotation(Flag.class);
             if(null != p){
                 builder.append(String.format("%-50s %-50s\n",
                         StringUtils.join(new String[]{p.shortForm(),p.longForm()},","),p.description()));
             }else if(null != f){
                 String flagstr = String.format("%-50s %-50s\n",
                         f.shortForm(), f.description());
                 if(!f.group().isEmpty()){
                     if(!groups.containsKey(f.group())){
                         groups.put(f.group(),new ArrayList<String>());
                     }
                     groups.get(f.group()).add(flagstr);
                 }else{
                     builder.append(flagstr);
                 }
             }
         }
 
         
         for(Map.Entry<String,ArrayList<String>> entry: groups.entrySet()){
             builder.append(entry.getKey() + ":\n");
             for(String l : entry.getValue()){
                 builder.append(l);
             }
             builder.append("\n");
         }
         
         return builder.toString();
 
     }
 
     public static CreatedHandler handlerFromArgs(List<String> args,
                                                         Class<? extends ClientFunctionHandler> t)throws Exception{
 
         ClientFunctionHandler c = t.newInstance();
         HashMap<Integer,Boolean> del_idxs = new HashMap<Integer, Boolean>();
         for(Field field: c.getClass().getDeclaredFields()){
             Parameter param = null;
             Flag flag = null;
             if(null != (flag = field.getAnnotation(Flag.class))){
                 int i = 0;
                 for(String a: args){
                     if(a.equals(flag.shortForm()) || a.equals(flag.longForm())){
                         field.setBoolean(c,true);
 
                         //ensure help gets propagated to final command
                         if(!a.equals("-h") && !a.equals("--help"))
                             del_idxs.put(i, true);
                     }
                     i+=1;
                 }
             }else if(null != (param = field.getAnnotation(Parameter.class))){
                 int j = 0;
                 for(String a : args){
                    if(a.startsWith(param.shortForm() + "=") || a.startsWith(param.longForm()+"==")){
                         String value = a.substring(a.indexOf("=")+1);
                         field.set(c,value);
                         del_idxs.put(j,true);
                     }
                     j+=1;
                 }
             }
         }
 
         List<String> newArgs = new ArrayList<String>();
 
         int i = 0;
         for(String a: args){
             if(!del_idxs.containsKey(i))
                 newArgs.add(a);
             i+=1;
         }
         
         return new CreatedHandler(c,newArgs);
     }
 }
