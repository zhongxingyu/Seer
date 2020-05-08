 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bean;
 
 import bean.log.ApplicationLogger;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.List;
import javax.ejb.EJB;
 import javax.ejb.Singleton;
 import javax.enterprise.context.ApplicationScoped;
 import javax.faces.component.UIInput;
 import javax.faces.event.ValueChangeEvent;
 import javax.inject.Named;
 
 /**
  *
  * @author Brian GOHIER
  */
 @Named(value = "city")
 @ApplicationScoped
 public class City
 {
     private static ArrayList<String[]> LIST=new ArrayList<String[]>();
     private static boolean LOADED=false;
     
     public City()
     {
         City.load();
     }
     
     private static synchronized boolean saveCitiesAsList()
     {
         try
         {
             ObjectOutputStream oos=new ObjectOutputStream(
                     new FileOutputStream("resources/cities.lst"));
             oos.writeObject(City.LIST);
             oos.close();
             return true;
         }
         catch (FileNotFoundException ex)
         {
             ApplicationLogger.writeError("Fichier des code postaux "
                     + "introuvable", ex);
         }
         catch (IOException ex)
         {
             ApplicationLogger.writeError("Erreur lors du chargement du  "
                     + "fichier des code postaux", ex);
         }
         return false;
     }
     
     @SuppressWarnings("unchecked")
     private static synchronized boolean loadCitiesAsList()
     {
         
         try
         {
             ObjectInputStream ois=new ObjectInputStream(
                     new FileInputStream("resources/cities.lst"));
             City.LIST=(ArrayList<String[]>) ois.readObject();
             ois.close();
             return true;
         }
         catch (FileNotFoundException ex)
         {
             ApplicationLogger.writeError("Fichier des code postaux "
                     + "introuvable", ex);
         }
         catch (IOException ex)
         {
             ApplicationLogger.writeError("Erreur lors du chargement du  "
                     + "fichier des code postaux", ex);
         }
         catch (ClassNotFoundException ex)
         {
             ApplicationLogger.writeError("Erreur de conversion lors du "
                     + "chargement du fichier des code postaux", ex);
         }
         return false;
     }
     
     /**
      * Charge la liste des villes et codes postaux à partir du fichier texte
      * <code><u>/!\</u> Processus très long <u>/!\</u></code>
      */
     private static synchronized void loadAsFile()
     {
         InputStreamReader isr = null;
         try
         {
             isr=new InputStreamReader(new FileInputStream("resources/cities.txt"),"UTF-8");
             BufferedReader br=new BufferedReader(isr);
             String line;
             int counter=1;
             while((line=br.readLine())!=null)
             {
                 if(!line.isEmpty())
                 {
                     String[] item=line.split("=");
                     if(item.length==2)
                     {
                         if(City.containsKey(item[0]))
                         {
                             int i=0;
                             String newKey=item[0];
                             while(City.containsKey(newKey))
                             {
                                 newKey=item[0]+String.valueOf(i++);
                             }
                             item[0]=newKey;
                         }
                         if(counter%1000==0)
                         {
                             System.err.println("Line: "+counter);
                         }
                         counter++;
                         City.LIST.add(item);
                     }
                 }
             }
         }
         catch (UnsupportedEncodingException ex)
         {
             ApplicationLogger.writeError("Type d'encodage incorrect pour le "
                     + "fichier des code postaux", ex);
         }
         catch (FileNotFoundException ex)
         {
             ApplicationLogger.writeError("Fichier des code postaux "
                     + "introuvable", ex);
         }
         catch (IOException ex)
         {
             ApplicationLogger.writeError("Erreur lors du chargement du  "
                     + "fichier des code postaux", ex);
         }
         if(isr!=null)
         {
             try
             {
                 isr.close();
             }
             catch (IOException ex)
             {
                 ApplicationLogger.writeError("Erreur lors de la fermeture du "
                         + "fichier des code postaux", ex);
             }
         }
     }
     
     public static synchronized void load()
     {
         if(!City.LOADED)
         {
             City.loadCitiesAsList(); // Par fichier binaire
 //            City.loadAsFile(); // Par fichier texte
 //            City.saveCitiesAsList();
             ApplicationLogger.writeInfo("Fichier des codes postaux chargé");
             City.LOADED=true;
         }
     }
     
     public static List<String> beginWith(String query)
     {
         List<String> list=new ArrayList<String>();
         String value=query.toLowerCase();
         for(String[] item:City.LIST)
         {
             String c=item[1];
             String city=c.toLowerCase();
             if(city.startsWith(value))
             {
                 list.add(c);
             }
         }
         return list;
     }
     
     public static List<String> likeCity(String query)
     {
         List<String> list=new ArrayList<String>();
         String value=City.getNormalizedCity(query.toLowerCase());
         for(String[] item:City.LIST)
         {
             String c=item[1];
             String city=c.toLowerCase();
             if(city.startsWith("le ")||
                     city.startsWith("la "))
             {
                 if(city.substring(3,c.length()).startsWith(value))
                 {
                     list.add(c);
                 }
             }
             else if(value.startsWith("le ")||
                     value.startsWith("la "))
             {
                 if(city.startsWith(value.substring(3,query.length())))
                 {
                     list.add(c);
                 }
             }
         }
         return list;
     }
     
     public static String getZipCode(String postalCode)
     {
         if(postalCode!=null&&postalCode.length()>=5)
         {
             return postalCode.substring(0, 5);
         }
         return "";
     }
     
     public static String getPostalCode(String city)
     {
         for(String[] item:City.LIST)
         {
             if(item[1].equalsIgnoreCase(city))
             {
                 if(item[0].length()>=5)
                 {
                     return item[0];
                 }
                 else
                 {
                     return city;
                 }
             }
         }
         List<String> like=City.likeCity(city);
         if(like!=null)
         {
             return City.getPostalCode(like.get(0));
         }
         return "";
     }
     
     public static String getCity(String postalCode)
     {
         for(String[] item:City.LIST)
         {
             if(item[0].equals(postalCode))
             {
                 return item[1];
             }
         }
         return "";
     }
     
     public static List<String> getPostalCodes()
     {
         List<String> postalCodes=new ArrayList<String>();
         for(String[] item:City.LIST)
         {
             postalCodes.add(item[0]);
         }
         return postalCodes;
     }
     
     public static List<String> getCityNames()
     {
         List<String> cityNames=new ArrayList<String>();
         for(String[] item:City.LIST)
         {
             cityNames.add(item[1]);
         }
         return cityNames;
     }
    
     private static boolean containsKey(String key)
     {
         return City.getValue(key)!=null;
     }
     
     private static boolean containsValue(String value)
     {
         return City.getKey(value)!=null;
     }
     
     private static String getValue(String key)
     {
         List<String> values=City.getValues(key);
         if(!values.isEmpty())
         {
             return values.get(0);
         }
         return null;
     }
     
     private static List<String> getValues(String key)
     {
         List<String> result=new ArrayList<String>();
         for(String[] item:City.LIST)
         {
             if(item[0].equalsIgnoreCase(key))
             {
                 result.add(item[1]);
             }
         }
         return result;
     }
     
     private static String getKey(String value)
     {
         for(String[] item:City.LIST)
         {
             if(item[1].equalsIgnoreCase(value))
             {
                 return item[0];
             }
         }
         return null;
     }
     
     public static void setCity(ValueChangeEvent event)
     {
         UIInput cityInput=(UIInput)event.getComponent().getAttributes().get("cityInput");
         String value=String.valueOf((Integer)event.getNewValue());
         if(City.containsKey(value))
         {
             cityInput.setValue(City.getValue(value));
         }
 //        ((UIInput)event.getComponent()).setValue(City.getZipCode(value));
     }
     
     public static void setPostalCode(ValueChangeEvent event)
     {
         UIInput postalCodeInput=(UIInput)event.getComponent().getAttributes().get("postalCodeInput");
         String value=(String) event.getNewValue();
         if(City.containsValue(City.getNormalizedCity(value).toUpperCase()))
         {
             String newValue=City.getPostalCode(
                     City.getNormalizedCity(value).toUpperCase());
             postalCodeInput.setValue(newValue);
         }
         else
         {
             List<String> list=City.likeCity(value);
             if(list!=null&&!list.isEmpty())
             {
                 String newValue=City.getPostalCode(list.get(0));
                 postalCodeInput.setValue(newValue);
             }
         }
     }
     
     public static String getNormalizedCity(String city)
     {
         String value=Normalizer.normalize(city, Normalizer.Form.NFD)
                 .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                 .replaceAll("-", " ");
         return value;
     }
 }
