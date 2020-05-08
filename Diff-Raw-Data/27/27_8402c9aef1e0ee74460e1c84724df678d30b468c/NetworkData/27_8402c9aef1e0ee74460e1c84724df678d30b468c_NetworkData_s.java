 package org.renci.databridge.util;
 import org.renci.databridge.util.Dataset;
 import org.renci.databridge.util.DatasetSerializer;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.net.*;
 import java.util.*;
 import cern.colt.matrix.DoubleMatrix2D;
 import cern.colt.matrix.impl.RCDoubleMatrix2D;
 import cern.colt.matrix.impl.AbstractMatrix2D;
 import cern.colt.matrix.*;
 import cern.colt.list.IntArrayList;
 import cern.colt.list.DoubleArrayList;
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.io.Output;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.serializers.MapSerializer;
 import com.esotericsoftware.kryo.serializers.CollectionSerializer;
 
 
 /**
  * This class holds the data for a single network in 
  * the DataBridge system.
  * 
  * @author Howard Lander -RENCI (www.renci.org)
  * 
  */
 public class NetworkData {
 
      /** A map to store properties such as the name of the network and the technique used
          to produce the network */
      private HashMap<String, String> properties; 
 
      /** A string to store the dbID for the network - this becomes the databridge identifier
 	 for all edges in the network. This dbID is unique amongst edges sharing the same nodes. */
      private String dbID;
 
      /** A list of Hashmaps for the individual nodes.We may want this to
          eventually be a list of structs so we can have more info, such
          as a display name or a URL. At the moment we are going to use
          hdl handles */
      private ArrayList<Dataset> datasets;
 
      /** The similarity matrix stored as a Compressed row format.Note that
          we only need the top half of the matrix though we are currently storing it all.
          Also note that the x and y dimensions of original matrix have to be equal */
      private RCDoubleMatrix2D similarityMatrix;
 
      /** The size of the matrix.  Note that we only need one dimension, because
          the x and y dimensions of original matrix have to be equal */
      private int arraySize;
 
      /**
       * NetworkData constructor with no parameter. Note that this does not allocate the matrix
       * since there is not a zero argument constructor for the RCDoubleMatrix2D type.
       * This constructor should only be used when reading a network from disk, else a dbID should
       * always be provided.
       */
      public NetworkData() {
         properties = new HashMap<String, String>();
         datasets = new  ArrayList<Dataset>();
         similarityMatrix = null;
         this.arraySize = 0;
      }
 
      /**
       * NetworkData constructor with a single parameter: the dbID for the network.
       * Note that this does not allocate the matrix
       * since there is not a zero argument constructor for the RCDoubleMatrix2D type.
       *
       */
      public NetworkData(String dbID) {
         properties = new HashMap<String, String>();
         datasets = new  ArrayList<Dataset>();
         similarityMatrix = null;
         this.arraySize = 0;
 	this.dbID = dbID;
      }
 
      /**
       * NetworkData constructor which takes a two parameters: the size of each
       * dimension of the similarity matrix and the dbID for the edges that make 
       * up the network
       *
       * @param arraySize The dimension of one side of the similarity matrix
       * @param dbID The dbID for the network
       */
      public NetworkData(int arraySize, String dbID) {
         properties = new HashMap<String, String>();
         datasets = new  ArrayList<Dataset>();
         similarityMatrix = new RCDoubleMatrix2D(arraySize, arraySize);
         this.arraySize = arraySize;
 	this.dbID = dbID;
      }
 
      /**
       * NetworkData constructor which takes two parameters: a 2 dimensional
       * array of doubles containing the similarity matrix, and the dbID for 
       * the edges that make up the network.
       *
       * @param values A 2 dimensional array of doubles.  Note that the dimensions of the array
       *                must be equal or an IllegalArgumentException is thrown.
       */
      public NetworkData(double[][] values, String dbID) throws IllegalArgumentException {
         similarityMatrix = new RCDoubleMatrix2D(values);
 
         /* Remember that this is a similarity matrix, meaning that, in the non-sparse, form
            each dataset is compared to every other data set.  That means the matrix has to be
            square.  If it isn't we throw an IllegalArgumentException.
          */ 
         if (similarityMatrix.rows() != similarityMatrix.columns()) {
             String exceptionString = 
                new String("number of rows (" + similarityMatrix.rows() + 
                            ") != number of columns (" + similarityMatrix.columns() + ")");
             throw new IllegalArgumentException(exceptionString);
         }
         properties = new HashMap<String, String>();
         datasets = new  ArrayList<Dataset>();
         arraySize = values.length;
 	this.dbID = dbID;
      }
 
      /**
       * This method populates this NetworkData object from the data stored in the
       * specified local file. The filePath must be accessible from where the code is called.
       *
       * @param filePath The file from which the network data should be read.
       *                
       */
      public void populateFromDisk(String filePath) throws Exception {
          // read from the requested file
          try {
              FileInputStream fos = new FileInputStream(new File(filePath));
              BufferedInputStream theStream = new BufferedInputStream(fos);
              Input input = new Input(theStream);
              readTheNetworkFromInputObject(input);
 
           } catch (Exception e) {
               throw e;
           }
      }
 
      /**
       * This method populates this NetworkData object from the data stored in the
       * specified file URL . The file URL must be accessible from where the code is called.
       *
       * @param userURL The file URL from which the data should be read.
       *                
       */
      public void populateFromURL(String userURL) throws Exception {
          // read from the requested file
          try {
              URL theURL = new URL(userURL);
              URLConnection theConnection = theURL.openConnection();
              BufferedInputStream theStream = 
                 new BufferedInputStream(theConnection.getInputStream());
              Input input = new Input(theStream);
              readTheNetworkFromInputObject(input);
 
           } catch (Exception e) {
               throw e;
           }
      }
 
      /**
       * This is the lower level method that actually does the work of reading
       * the network data from the input object and storing it in the NetworkData
       * structure.  Because the input object just requires a stream, this same
       * function can read data from both a file and a URL.
       *
       * @param input The Kryo Input object from which the data is read.
       *                
       */
      private void readTheNetworkFromInputObject(Input input) throws Exception {
 
          // Setup the kryo
          Kryo kryo = new Kryo();
 
          // NOTE: the cardinality of the rows, cols and vals arrays are all the same.
          // This is because what is being stored in the RCDoubleMatrix2D is a set of
          // (row, column, value) tuples using 3 "companion" arrays.
          IntArrayList rows = new IntArrayList(); 
          IntArrayList cols = new IntArrayList(); 
          DoubleArrayList vals = new DoubleArrayList(); 
          try {
              this.arraySize = input.readInt();
              similarityMatrix = new RCDoubleMatrix2D(this.arraySize, this.arraySize);
 
              int nTuples = input.readInt();
              for (int i = 0; i < nTuples; i++) {
                 rows.add(input.readInt());
              }
 
              for (int i = 0; i < nTuples; i++) {
                 cols.add(input.readInt());
              }
 
              for (int i = 0; i < nTuples; i++) {
                 vals.add(input.readDouble());
              }
 
              for (int i = 0; i < nTuples; i++) {
                  this.similarityMatrix.setQuick(rows.get(i), cols.get(i), vals.get(i));
              }
 
              // Read the properties using the Kryo MapSerializer class
              MapSerializer theSerializer = new MapSerializer();
              this.properties = kryo.readObject(input, HashMap.class, theSerializer);
 
              // Read the datasets
              // Declare a DatasetSerializer
              DatasetSerializer theDatasetSerializer = new DatasetSerializer();
              CollectionSerializer theCollectionSerializer = 
                 new CollectionSerializer(Dataset.class, theDatasetSerializer);
              this.datasets = kryo.readObject(input, ArrayList.class, theCollectionSerializer);
 
          } catch (Exception e) {
              throw e;
          }
      }
 
 
      /**
       * Method for writing the network to a file. Right now we are using a package from
       * esoteric software to do the packing.
       * 
       *
       * @param filePath The file to which the network data should be written.
       *                
       */
      public void writeToDisk(String filePath) throws Exception {
          // Get the rows, cols and vals from the matrix so we can serialize them
 
          // NOTE: the cardinality of the rows, cols and vals arrays are all the same.
          // This is because what is being stored in the RCDoubleMatrix2D is a set of
          // (row, column, value) tuples using 3 "companion" arrays.
          IntArrayList rows = new IntArrayList(); 
          IntArrayList cols = new IntArrayList(); 
          DoubleArrayList vals = new DoubleArrayList(); 
          this.similarityMatrix.getNonZeros(rows, cols, vals);
 
          // Setup the kryo
          Kryo kryo = new Kryo();
          kryo.register(RCDoubleMatrix2D.class);
 
          // Set up a buffered output stream for the kryo output object.
          BufferedOutputStream theStream = null;
          try {
             FileOutputStream fos = new FileOutputStream(new File(filePath));
             theStream = new BufferedOutputStream(fos);
          } catch (Exception e) {
              throw e;
          }
 
          Output output = new Output(theStream);
          // Write the size of the array, Note that this could be, in theory, different from
          // the cardinality of the array. In practice, I don't think it ever will be
          // but I can't convince myself, so I am storing it separately.
          output.writeInt(this.getArraySize());
 
          // Write the rows: Start with the number of rows.
          int nTuples = rows.size();
          output.writeInt(nTuples);
          for (int i = 0; i < nTuples; i++) {
             output.writeInt(rows.get(i));
          }
 
          // Write the cols:
          for (int i = 0; i < nTuples; i++) {
             output.writeInt(cols.get(i));
          }
 
          // Write the vals:
          for (int i = 0; i < nTuples; i++) {
             output.writeDouble(vals.get(i));
          }
 
          // Write the properties using the Kryo MapSerializer class
          MapSerializer theSerializer = new MapSerializer();
          kryo.writeObject(output, this.properties, theSerializer);
 
          // Write the datasets
          // Declare a DatasetSerializer
          DatasetSerializer theDatasetSerializer = new DatasetSerializer();
          CollectionSerializer theCollectionSerializer = 
             new CollectionSerializer(Dataset.class, theDatasetSerializer);
          kryo.writeObject(output, this.datasets, theCollectionSerializer);
 
          output.close();
          
 
          // write it to the requested file
          try {
              theStream.write(output.getBuffer(), 0, output.total());
              theStream.close();
          } catch (Exception e) {
              throw e;
          }
 
      }
 
      /**
       * Get a property
       *
       * @param key The key for which to retrieve the value
       * @return value The value for the requested key
       */
      public String getAProperty(String key)
      {
          return  this.properties.get(key);
      }
      
      /**
       * Add a property
       *
       * @param key The key for the property
       * @param value The value for the property
       */
      public void addAProperty(String key, String value)
      {
          this.properties.put(key, value);
      }
 
      
      /**
       * Get properties.
       *
       * @return properties as Map.
       */
      public Map getProperties()
      {
          return properties;
      }
      
      /**
       * Set properties.
       *
       * @param properties the value to set.
       */
      public void setProperties(HashMap properties)
      {
          this.properties = properties;
      }
 
      /**
       * Get dbID.
       *
       * @return dbID as String.
       */
      public String getDbID()
      {
	 return dbID();
      }
 
      /**
       * Set dbID.
       *
       * @param dbID the value to set.
       */
      public void setDbID(String dbID)
      {
 	 this.dbID = dbID;
      }
 
      /**
       * Get datasets.
       *
       * @return datasets as ArrayList.
       */
      public ArrayList getDatasets()
      {
          return datasets;
      }
      
      /**
       * Set datasets.
       *
       * @param datasets the value to set.
       */
      public void setDatasets(ArrayList datasets)
      {
          this.datasets = datasets;
      }
 
      /**
       * add a Dataset object to the ArrayList of Datasets.
       *
       * @param theDataset the Dataset to add
       */
      public void addADataset(Dataset theDataset)
      {
          this.datasets.add(theDataset);
      }
 
      /**
       * get the Dataset object corresonding to the specified index.
       *
       * @param index the index of the Dataset to retrieve.
       */
      public Dataset getADataset(int index)
      {
          return this.datasets.get(index);
      }
 
 
 
     /**
      * Get similarityMatrix.
      *
      * @return similarityMatrix as RCDoubleMatrix2D.
      */
     public RCDoubleMatrix2D getSimilarityMatrix()
     {
         return similarityMatrix;
     }
 
     /**
      * Set similarityMatrix.
      *
      * @param similarityMatrix the value to set.
      */
     public void setSimilarityMatrix(RCDoubleMatrix2D similarityMatrix)
     {
         this.similarityMatrix = similarityMatrix;
     }
      
      /**
       * Get arraySize.
       *
       * @return arraySize as int.
       */
      public int getArraySize()
      {
          return arraySize;
      }
      
      /**
       * Set arraySize.
       *
       * @param arraySize the value to set.
       */
      public void setSize(int arraySize)
      {
          this.arraySize = arraySize;
      }
 }
