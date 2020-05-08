 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fichier;
 
 import bean.ParcBean;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import persistance.ParcBeanPersist;
 import sun.net.www.protocol.http.HttpURLConnection;
 
 /**
  *
  * @author Nomyx
  */
 public final class GestionFichier {
 				File cheminStockage;
 				private String urlFichier;
     private String cheminFile;
 				private int taille;
     InputStream input = null;
     URL url;
     BufferedWriter props;
     String tempFile;
     String propertiesFile;
 				
 				public GestionFichier(String url, String chemin){
 								urlFichier = url;
 								cheminStockage = new File(chemin);
         cheminFile = chemin;
 				}
     
     private String getUniqueNum(int fileLength) {
         try {
             Calendar cal = Calendar.getInstance();
             int num1 = cal.get(Calendar.MILLISECOND);
 
             int num2 = cal.get(Calendar.YEAR);
             int num3 = cal.get(Calendar.MONTH);
             int num4 = cal.get(Calendar.DAY_OF_WEEK);
             int num5 = cal.get(Calendar.YEAR);
             int num6 = cal.get(Calendar.HOUR_OF_DAY);
             int num7 = cal.get(Calendar.MINUTE);
             int num8 = cal.get(Calendar.SECOND);
             String secureNum = num8 + num7 + num1 + fileLength + num1 + num8 + num8 + num2 + num1 + num3 + num4 + num5 + num1 + num6 +
                     "0" + fileLength + (num2 * 1) + num7 + fileLength + num7;
             return secureNum;
         } catch (Exception e) {
             return null;
         }
 
     }
     
     private boolean writeCounter(int counter) {
         try {
             props.write("-" + String.valueOf(counter));
             props.flush();
             return true;
         } catch (Exception e) {
             return false;
         }
     }
     
     private boolean downloadFile(HttpURLConnection connection, int totalSize, String nameOfFile) {
         
         try {
             boolean success = false;
             byte[] abyte0;
             long startTime = System.currentTimeMillis();
             int j,counter = 0;
             String secureNum = getUniqueNum(nameOfFile.length());
 
             System.gc();
             String BUFFER_SIZE = "12096";
            String tempDirectory = "";
             String slash = "/";
             
             if (BUFFER_SIZE != null && BUFFER_SIZE.trim().length() > 1) {
                 abyte0 = new byte[Integer.parseInt(BUFFER_SIZE)];
             } else {
                 abyte0 = new byte[4096];
             }
 
             tempFile = tempDirectory + slash + nameOfFile + "!" + secureNum + "R";
             propertiesFile = tempDirectory + slash + "prop!" + secureNum + "$";
             input = connection.getInputStream();
             BufferedInputStream inputs = new BufferedInputStream(input);
 
             FileOutputStream out = new FileOutputStream(tempFile);
             props = new BufferedWriter(new FileWriter(propertiesFile));
 
             props.write(nameOfFile);
             props.newLine();
             props.write("Taille:" + connection.getContentLength());
             props.newLine();
             props.write(url.toString());
             props.newLine();
             boolean finished = false;
 
             while (((j = inputs.read(abyte0)) != -1)) {
 
                 counter += j;
 
 
                 if (counter <= 1024) {
                     
                 } else if (counter >= 1025 && counter < 1025000) {
                     double theSize = ((double) counter / 1024D);
                     String temp = String.valueOf(theSize);
                     int index2 = temp.lastIndexOf(".");
                     if (index2 != -1) {
                         temp = temp.substring(0, index2);
                     }
                     
                 } else if (counter >= 1025001) {
                     double theSize = ((double) counter / 1024D);
                     theSize = theSize / 1024D;
                     String temp = String.valueOf(theSize);
                     int index2 = temp.lastIndexOf(".");
                     if (index2 == -1) {
                         index2 = temp.length();
                     }
                     if ((index2 + 3) <= temp.length()) {
                         temp = temp.substring(0, (index2 + 3));
                     } else if ((index2 + 2) <= temp.length()) {
                         temp = temp.substring(0, (index2 + 2));
                     } else {
                         temp = temp.substring(0, (index2 + 1));
                     }
 
                 }
                 
 
                 out.write(abyte0, 0, j);
                 writeCounter(counter);
                 finished = true;
             }
 
 
             props.flush();
             props.close();
             out.flush();
             out.close();
             return true;
         } catch (Exception e) {
             return false;
         }
         
     }
     
     private boolean renameTempFile(String file, String temp1) {
         try {
             System.gc();
             File tempFile = new File(temp1);
             if (tempFile.exists()) {
                 String fileName = "" + file.substring(0, file.length());
                 File f2 = new File(fileName);
                 if (f2.exists()) {
                     boolean delete = f2.delete();
                     if (!delete) {
                         System.out.println("Impossible de supprimer le fichier dans '" + fileName + "' Supprimer le manuelement");
                     }
                 }
                 return tempFile.renameTo(f2);
             } else
                 return false;
         } catch (Exception e) {
             return false;
         }
     }
     
         /** supprime le fichier temporaire
      * @param propertiesFile propriet�s du fichier
      * @return succ�s du t�lechargement
      */
     private boolean deletePropFileOnCompletion(String propertiesFile) {
         try {
             System.gc();
             if (propertiesFile != null)
                 return new File(propertiesFile).delete();
             else
                 return false;
         } catch (Exception e) {
             return false;
         }
     }
 				
 				//@Schedule(dayOfMonth = "1",month="1")
 				//@Schedule(second="*/30", minute="*", hour="*")
 				public void getFile()
 				{
 								try
 								{
             url = new URL(this.urlFichier);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.connect();
             input = connection.getInputStream();
             int fileLength = connection.getContentLength();
             boolean downloadSuccess = downloadFile(connection, fileLength, cheminFile);
             if (downloadSuccess) {
                     downloadSuccess = renameTempFile(cheminFile, tempFile);
                     if (downloadSuccess) {
                         downloadSuccess = deletePropFileOnCompletion(propertiesFile);
                         
                     }
                 }
 								}
 								catch (IOException e)
 								{
 												System.out.println("Error while trying to download the file.");
 												e.printStackTrace();
 								}
 								finally
 								{
 												try
 												{
 																input.close();
 												}
 												catch (IOException e){
 																e.printStackTrace();
 												}
 								}
 				}
 				
 
 				public void parsingFile() throws MalformedURLException, IOException{
 								ParcBeanPersist pp = new ParcBeanPersist();
 								URL url = new URL(this.urlFichier);
								Reader reader = new InputStreamReader(new FileInputStream(cheminFile), "utf-8");
 								BufferedReader br = new BufferedReader(reader);
 								
 								String ligne = null;
 								String[] data = {};
 								
 								
 								Pattern p = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
 								//requetes.executerRequete(requeteSuppr);
 								//requetes.executerRequete(requeteCreation);
 								int numLigne = 1;
         if((ligne = br.readLine()) != null) {
             taille = ligne.split(",").length+1;
             numLigne++;
         }
         
         String[] donnees = new String[taille];
         
 								while ((ligne = br.readLine()) != null)
 									{
 										if (numLigne > 1){
 												// Retourner la ligne dans un tableau
 												data = p.split(ligne);
 												int i = 0;
 												for (String val : data){
                 if(i%(taille)==3){
 //                    val = [0].split("]")[0];
                     val = val.split("]")[0].substring(2);
                     donnees[i] = val.split(",")[0];
                     i++;
                     donnees[i] = val.split(",")[1];
                     i++;
                 } else if(i%(taille)==18){
                     int size = val.length()-1;
                     if(size>255){
                         size = 255;
                     }
                     donnees[i]=val.substring(1, size);
                     i++;
                 } else {
                     int size = val.length()-1;
                     if(size>255){
                         size = 255;
                     }
                     donnees[i]=val.substring(1, size);
                     i++;
                 }
 												}
 												if (data.length!=taille-1){
 																donnees[taille-1]="";
 												}
 												ParcBean parc = new ParcBean(donnees);
 												pp.enregistrer(parc);
 										}
 										numLigne++;
 								}
 								br.close();
 								
 				}
 				
 				public void charger() throws FileNotFoundException, MalformedURLException, IOException{
 								this.getFile();
 //								this.unzip();
 								this.parsingFile();
 				}
 				
 				public void main() throws FileNotFoundException, MalformedURLException, IOException{
 //								GestionFichier gf = new GestionFichier("http://data.nantes.fr/fileadmin/data/datastore/3-publication/environnement/jardins_parcs/jardins_parcs_csv.zip","jardins_parcs_csv.zip","");
 //								gf.charger();
 				}
 }
