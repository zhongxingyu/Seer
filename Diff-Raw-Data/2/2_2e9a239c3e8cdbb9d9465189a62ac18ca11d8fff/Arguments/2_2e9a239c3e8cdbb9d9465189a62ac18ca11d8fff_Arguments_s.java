 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.sourceforge.frcsimulator;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  *
  * @author bpylko2015
  */
 public class Arguments {
     private String[] configStringArray;
     private Properties args;
     private Comparator<String> argTypeComparator = new Comparator<String>(){
 
         @Override
         public int compare(String o1, String o2) {
             return (o2.contains("#") ? 0 : 1) - (o1.contains("#") ? 0 : 1);
         }
         
     };
     // configString format: @argname#numparams
     // @0,@1 etc are unnamed arguments (eg a filename passed to the program)
     // order does not matter
     // eg @--gui#0 @0 is the same as @0 @--gui#0
     public Arguments(String[] args, String configString){
         this.args = new Properties();
         configStringArray = configString.split(" ");
         Arrays.sort(configStringArray,argTypeComparator);
         String tempName = "";
         String tempParamList = "";
         int tempLengthDifference = 0;
         for(int i = 0; i < configStringArray.length;i++){
             if(configStringArray[i].matches("\\@[0-9][0-9]")){
                 this.args.put(configStringArray[i].substring(1),args[i]);
             } else{
                 tempName = configStringArray[i].split("\\#")[0].substring(1);
                 if(Integer.parseInt(configStringArray[i].substring(configStringArray[i].indexOf("#"))) == 0){this.args.put(tempName, Arrays.binarySearch(args, tempName) < 1 ? 0 : 1);}
                 else{
                    tempLengthDifference = Integer.MAX_VALUE;
                     for(int j = 0; j < configStringArray.length;j++){
                         tempLengthDifference = Arrays.binarySearch(args, configStringArray[j].split("\\#")[0].substring(1)) - Arrays.binarySearch(args,tempName) < tempLengthDifference &&Arrays.binarySearch(args, configStringArray[j].split("\\#")[0].substring(1)) - Arrays.binarySearch(args,tempName) > 0 ? Arrays.binarySearch(args, configStringArray[j].split("\\#")[0].substring(1)) - Arrays.binarySearch(args,tempName) : 0;
                     }
                     for(int j = Arrays.binarySearch(args,tempName); j < Arrays.binarySearch(args,tempName)+tempLengthDifference; j++){
                         tempParamList += args[j] + " ";
                     }
                     tempParamList.trim();
                     this.args.put(tempName,tempParamList);
                 }
             }
         }
     }
     public String get(String key){
         return (String) args.get(key);
     }
 }
