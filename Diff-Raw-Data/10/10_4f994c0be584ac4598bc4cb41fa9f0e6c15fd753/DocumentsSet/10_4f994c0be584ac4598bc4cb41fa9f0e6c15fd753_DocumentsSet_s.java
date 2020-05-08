 package tuwien.aic12.server.twitter.semantics.documents;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import tuwien.aic12.server.twitter.semantics.classifier.Preprocesser;
 import tuwien.aic12.server.twitter.semantics.util.ArffFileCreator;
 import tuwien.aic12.server.twitter.semantics.util.Options;
 
 /**
  * class representing a tweets' dataset
  */
 public class DocumentsSet {
 
     private Features _feats;
     private Map<Integer, List<Integer>> _doc2feat_test;
     private Map<Integer, List<Integer>> _doc2feat_train;
     private Map<Integer, List<String>> _doc2feat_train_str;
     private List<String> _polarity;
 
     public DocumentsSet() {
         this._feats = new Features();
         this._feats.setDs(this);
     }
 
     /**
      * gets a map for train that associates a tweet's id to a list of terms
      *
      * @return a map for train that associates a tweet's id to a list of terms
      */
     public Map<Integer, List<String>> getD2f_train_str() {
         return _doc2feat_train_str;
     }
 
     /**
      * sets a given map for train that associates a tweet's id to a list of
      * terms
      *
      * @param d2fTrainStr map for train that associates a tweet's id to a list
      * of terms
      */
     public void setD2f_train_str(Map<Integer, List<String>> d2fTrainStr) {
         _doc2feat_train_str = d2fTrainStr;
     }
 
     /**
      * gets a map for train that associates a tweet's id to a list of terms's id
      *
      * @return a map for train that associates a tweet's id to a list of terms's
      * id
      */
     public Map<Integer, List<Integer>> getD2f_train() {
         return _doc2feat_train;
     }
 
     /**
      * gets a map for test that associates a tweet's id to a list of terms's id
      *
      * @return a map for test that associates a tweet's id to a list of terms's
      * id
      */
     public Map<Integer, List<Integer>> getD2f_test() {
         return _doc2feat_test;
     }
 
     /**
      * sets a map for test that associates a tweet's id to a list of terms's id
      *
      * @param d2fTest a map for test that associates a tweet's id to a list of
      * terms's id
      */
     public void setD2f_test(Map<Integer, List<Integer>> d2fTest) {
         _doc2feat_test = d2fTest;
     }
 
     /**
      * sets a map for test that associates a tweet's id to a list of terms's id
      *
      * @param d2f2 a map for test that associates a tweet's id to a list of
      * terms's id
      */
     public void setD2f2(Map<Integer, List<Integer>> d2f2) {
         this._doc2feat_train = d2f2;
     }
 
     /**
      * gets an object that represents feature of all tweets
      *
      * @return an object that represents features of all tweets
      */
     public Features getFeat() {
         return _feats;
     }
 
     /**
      * gets a list of string that contains polarities of all tweets
      *
      * @return a list of string that contains polarities of all tweets
      */
     public List<String> getPols() {
         return _polarity;
     }
 
     public FileOutputStream createFileOutputStream(String path) {
         FileOutputStream out = null;
         try {
             InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
             String[] split = path.split("/");
             out = new FileOutputStream(new File(split[split.length - 1]));
             int read = 0;
             byte[] bytes = new byte[1024];
             while ((read = inputStream.read(bytes)) != -1) {
                 out.write(bytes, 0, read);
             }
             inputStream.close();
             out.flush();
             return out;
         } catch (IOException ex) {
             Logger.getLogger(ArffFileCreator.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 out.close();
             } catch (IOException ex) {
                 Logger.getLogger(ArffFileCreator.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return null;
     }
 
     /**
      * creates a file containing all preprocessed tweets
      *
      * @param path_input path of input file containing the initial tweets'
      * dataset
      * @param path_output path of output file that will contain the tweets'
      * dataset after preprocessing
      * @param opt options for preprocessing
      */
     public void createFilePreprocessed(String path_input, String path_output, Options opt) {
         List<String> p = new LinkedList<String>();
         try {
             Preprocesser pr = new Preprocesser(opt);
             InputStream realPath = getClass().getClassLoader().getResourceAsStream(path_input);
             DataInputStream in = new DataInputStream(realPath);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             String str, pol;
             FileOutputStream prova = createFileOutputStream(path_output);
             PrintStream scrivi = new PrintStream(prova);
             while ((strLine = br.readLine()) != null) {
                 String[] items = strLine.split(";;");
                 str = items[5].toLowerCase();
                 pol = items[0];
                 p.add(pol);
                 scrivi.println(pr.preprocessDocument(str));
             }
             in.close();
         } catch (Exception e) {
             System.err.println("Errore: " + e.getMessage());
         }
         _polarity = p;
     }
 
     /**
      * creates a map that associates test tweets' id to terms' id
      *
      * @param path_input path of file containing the preprocessed tweets'
      * dataset
      * @throws IOException
      */
     public void createIndexTest(String path_input) throws IOException {
         int i = 0;
         Map<Integer, List<Integer>> index = new HashMap<Integer, List<Integer>>();
         Map<String, Integer> f2i = this._feats.getF2i();
         try {
             InputStream realPath = getClass().getClassLoader().getResourceAsStream(path_input);
             DataInputStream in = new DataInputStream(realPath);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             while ((strLine = br.readLine()) != null) {
                 StringTokenizer st = new StringTokenizer(strLine, " 		\";%{}|");
                 index.put(i, new LinkedList<Integer>());
                 while (st.hasMoreTokens()) {
                     String tmp;
                     int num;
                     tmp = st.nextToken();
                     if (f2i.get(tmp) != null) {
                         num = f2i.get(tmp);
                        //solo se non lo contiene gi�
                         if (!index.get(i).contains(num)) {
                             index.get(i).add(num);
                         }
                     }
                 }
                 Collections.sort(index.get(i));
                 i++;
             }
             in.close();
         } catch (Exception e) {
             System.err.println("Errore: " + e.getMessage());
         }
         this._doc2feat_test = index;
     }
 
     /**
      * creates a map that associates test tweets' id to terms' id
      *
      * @param path_input path of file containing the preprocessed tweets'
      * dataset
      * @throws IOException
      */
     public void createIndexTrain(String path_input) throws IOException {
         this._feats.createListStopwords();
         int i = 0;
         int j = 0;
         Map<Integer, List<Integer>> index = new HashMap<Integer, List<Integer>>();
         Map<Integer, List<String>> index_str = new HashMap<Integer, List<String>>();
         Map<Integer, String> i2f = new HashMap<Integer, String>();
         Map<String, Integer> f2i = new HashMap<String, Integer>();
         try {
             InputStream realPath = getClass().getClassLoader().getResourceAsStream(path_input);
             DataInputStream in = new DataInputStream(realPath);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             while ((strLine = br.readLine()) != null) {
                 StringTokenizer st = new StringTokenizer(strLine, " 		\";%{}|");
                 index.put(i, new LinkedList<Integer>());
                 index_str.put(i, new LinkedList<String>());
                 while (st.hasMoreTokens()) {
                     String tmp;
                     int num;
                     tmp = st.nextToken();
                     if (f2i.get(tmp) != null) {
                         num = f2i.get(tmp);
                        //solo se non lo contiene gi�
                         if (!index.get(i).contains(num)) {
                             index.get(i).add(num);
                             index_str.get(i).add(tmp);
                         }
                     } else {//if(!this.feat.getStopwords().contains(tmp)){
                         f2i.put(tmp, j);
                         i2f.put(j, tmp);
                         index.get(i).add(j);
                         index_str.get(i).add(tmp);
                         j++;
                     }
                 }
                 Collections.sort(index.get(i));
                 i++;
             }
             in.close();
         } catch (Exception e) {
             System.err.println("Errore: " + e.getMessage());
         }
         this._doc2feat_train = index;
         this._doc2feat_train_str = index_str;
         this._feats.setF2i(f2i);
         this._feats.setI2f(i2f);
     }
 }
