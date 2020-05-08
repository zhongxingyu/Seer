 /**
  * This file contains the ANNDriver class which is responsible for interfacing
  * an Artificial Neural Network and a user.
  *
  * Author: Cameron Vincent Chaparro.
  * Date: 23 November 2012
  */
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class NeuralNetDriver {
     // Used to get keyboard input from the user.
     private final static Keyboard kb = Keyboard.getKeyboard();
 
    private static double scale = 0;

     /**
      * Execution starts here: The user will be prompted to enter the name of a
      * text file containing data. Also, the ANN will be set up.
      *
      * Preconditions: None.
      * Postconditions: The ANN will be created, set up, used to evaluate the
      * data specified by the user, and displayed in some form. Afterwhich, the
      * program will terminate.
      */
     public static void main(String[] args) {
         // Determines when to stop one of the input loops.
         boolean done = false;
 
         // Determines when to stop one of the validation loops.
         boolean valid = false;
 
         // The file with the data that will be analyzed by the ANN.
         FileIO file = null;
 
         // The neural network that will be trained.
         NeuralNetwork ann = null;
         
         // A prompt to the user.
         String prompt = "";
 
         // A string that holds the user's input.
         String usrInput = "";
 
         // If the user entered a parameter use it as a filename and try to open
         // the file it specifies. (Note: If the parameter is not a filename, the
         // user will be prompted for a filename.
         if (args.length >= 1) {
             // Get the file.
             file = getFileForInput(args[0]);
 
             if (file != null) {
                 usrInput = args[0];
                 done = true;
             }
         }
 
         while (!done) {
             // Initialise the prompt string.
             prompt = "Enter the name of the data file whose data will be ";
             prompt += "evaluated (Q to Quit):   ";
 
             // Get the string input from the user.
             usrInput = getStringInput(prompt);
 
             // Check usrInput for the sentinel value.
             if (usrInput.equalsIgnoreCase("Q") ||
                 usrInput.equalsIgnoreCase("QUIT")) {
                 file = null;
                 done = true;
                 System.out.println();
             }
             // Open the specified file.
             else {
                 file = getFileForInput(usrInput);
 
                 if (file != null) {
                     done = true;
                 }
             }
         }
 
         if (file != null) {
             // Confirm that the file was opened.
             System.out.println("\n" + usrInput + " was opened.");
 
             // Process the file.
             ann = parseInitFile(file);
 
             if (ann != null) {
                 System.out.println("The network was created successfully.");
             }
             else {
                 System.out.println("The network could not be created.");
             }
         }
     }
 
     /**
      * Returns a NeuralNetwork setup with the information gathered from the
      * file object that is passed in.
      *
      * Preconditions: The file object must not be null. And the file must be
      * formatted as follows:
      * filename_with_data.ext
      * number_of_hidden_layers
      * number_of_nodes_in_hidden_layer_1
      * number_of_nodes_in_hidden_layer_2
      * number_of_nodes_in_hidden_layer_3
      *        ...
      * number_of_nodes_in_hidden_layer_n (where n is the number of hidden layers)
      * learning_rate
      * error_tolerance
      * Postconditions: A new NeuralNetwork object will be returned.
      */
     public static NeuralNetwork parseInitFile(FileIO file) {
         // If there is no file, do not continue.
         if (file == null) {
             return null;
         }
         else {
             // The learning rate.
             double rate = 0.0;
 
             // The error tolerance.
             double error = 0.0;
 
             // Holds the number of hidden layers.
             int numHiddenLayers = 0;
 
             // An array with the number of nodes in numHiddenLayers number of
             // hidden layers.
             int[] numNodes;
 
             // Holds the value of the strings read from the file.
             String line = null;
 
             // Get the name of the file with the data in it.
             line = file.readLine();
 
             // Open the data file.
             FileIO data = getFileForInput(line);
 
             // If the file could not be opened, let the user know and quit.
             if (data == null) {
                 System.out.println("\'" + line + "\' could not be opened.");
                 return null;
             }
 
             // Get the number of hidden layers, or nodes-per-hidden layer.
             line = file.readLine();
 
             // Convert the value to a number.
             numHiddenLayers = convertStringToInteger(line, "\'" + line + "\' is not a valid integer.");
 
             if (numHiddenLayers < 1) {
                 System.out.println("There must be at least one hidden layer.");
                 return null;
             }
 
             // Create the numNodes array.
             numNodes = new int[numHiddenLayers];
 
             // Get numHiddenLayers number of nodes-per-layer.
             for (int i = 0; i < numHiddenLayers; i++) {
                 // Get the number of nodes in the i-th hidden layer.
                 line = file.readLine();
 
                 // Convert the value to an integer.
                 numNodes[i] = convertStringToInteger(line, "\'" + line + "\' is not a valid integer.");
 
                 if (numNodes[i] < 1) {
                     System.out.println("There must be at least one node in a hidden layer.");
                     return null;
                 }
             }
 
             // Get the learning rate.
             line = file.readLine();
 
             // Convert the value to a double.
             rate = convertStringToDouble(line, "\'" + line + "\' is not a valid double.");
 
             // Make sure the value is positive.
             if (rate <= 0) {
                 System.out.println("The learning rate provided, \'" + rate + "\', is invalid.");
                 return null;
             }
 
             // Get the learning rate.
             line = file.readLine();
 
             // Convert the value to a double.
             error = convertStringToDouble(line, "\'" + line + "\' is not a valid double.");
 
             // Make sure the error value is positive.
             if (error <= 0) {
                 System.out.println("The error tolerance provided, \'" + error + "\', is invalid.");
                 return null;
             }
            FileData fData = parseDataFile(data);
            double tmpError = error;
            // If the error is not scaled, scale it.
            if (error > 1) {
                tmpError /= scale;
            }
 
            NeuralNetwork network = new NeuralNetwork(fData, numHiddenLayers, numNodes, rate, error);
 
             return network;
         }
     }
 
     /**
      * Processes the data file whose data contains an underlying function which
      * we want to learn.
      *
      * Preconditions: The data file cannot be empty, or null. Also, the data file
      * must have the following format:
      * scale_factor
      * number_of_input_nerons, number_of_output_neurons
      * x_value_of_data_point_1, y_value_of_data_point_1
      * x_value_of_data_point_2, y_value_of_data_point_2
      * x_value_of_data_point_3, y_value_of_data_point_3
      *        ...
      * x_value_of_data_point_n, y_value_of_data_point_n
      * Postconditions: The data file will be processed and a FileData class will
      * be returned containing n elements.
      * If the file contains valid data...
      * The 1st element contains: number_of_input_neurons
      * The 2nd element contains: number_of_output_neurons
      * The 3rd element contains: list_of_PointN_data_points
      * Otherwise...
      * Null will be returned.
      */
     public static FileData parseDataFile(FileIO data) {
         // If there is no data file, do not continue.
         if (data == null) {
             return null;
         }
         else {
             // The list of data points read in.
             ArrayList<PointN> points = new ArrayList<PointN>();
 
             // Stores all the data provided in the 'data' file that is needed
             // by the neural network.
             FileData fData = new FileData();
 
             // Stores the string read from the file.
             String line = "";
 
             // Get the scale value.
             line = data.readLine();
 
             // Convert the value to a double.
             scale = convertStringToDouble(line, "\'" + line + "\' is not a valid double.");
 
             // Make sure the scale is a reasonable value.
             if (scale <= 0) {
                 System.out.println("The scale provided, \'" + scale + "\', is invalid.");
                 return null;
             }
 
             // Get the number of input neurons and output neurons.
             line = data.readLine();
 
             // Convert the number of neurons to a number.
             String[] numNeurons = null;
 
             // Split line into number of input neurons and number of output neurons.
             if (line != null) {
                 numNeurons = line.split(",");
             }
             else {
                 System.out.println("The number of input / output neurons provided, \'" + line + "\', is invalid.");
                 return null;
             }
 
             // Holds the number of input neurons.
             int numInput = 0;
             // Holds the number of output neurons.
             int numOutput = 0;
 
             if (numNeurons.length == 2) {
                 if (numNeurons[0] != null) {
                     // Convert the string containing the number of input neurons to an integer.
                     numInput = convertStringToInteger(numNeurons[0], "\'" + numNeurons[0] + "\' is invalid.");
 
                     // Make sure there is at least one input neuron specified.
                     if (numInput <= 0) {
                         System.out.println("The number of input neurons provided, \'" + numNeurons[0] + "\',is invalid");
                         return null;
                     }
 
                     // Add the number of input neurons to the collection in fData.
                     fData.addData(numInput);
                 }
                 else {
                     System.out.println("\'" + numNeurons[1] + "\' is formatted improperly.");
                     return null;
                 }
 
                 if (numNeurons[1] != null) {
                     // Convert the string containing the number of output neurons to an integer.
                     numOutput = convertStringToInteger(numNeurons[1], "\'" + numNeurons[1] + "\' is invalid.");
 
                     // Make sure there is at least one input neuron specified.
                     if (numOutput <= 0) {
                         System.out.println("The number of output neurons provided, \'" + numNeurons[1] + "\', is invalid");
                         return null;
                     }
 
                     // Add the number of input neurons to the collection in fData.
                     fData.addData(numOutput);
                 }
                 else {
                     System.out.println("\'" + numNeurons[1] + "\' is formatted improperly.");
                     return null;
                 }
             }
             else {
                 System.out.println("\'" + line + "\' is formatted improperly.");
                 return null;
             }
 
             double[] x = new double[numInput];
             double[] y = new double[numOutput];
 
             // Get the data points.
             while (!data.EOF()) {
                 // A message to the user. The pound symbol will be changed with point number.
                 String message = "The point, \'#\' was formatted improperly.";
 
                 // Get the next data point.
                 line = data.readLine();
 
                 // Make sure line is not null.
                 if (line == null) {
                     break;
                 }
 
                 // Replace the pound symbol with the value of line that
                 // represents an invalid point.
                 message = message.replaceFirst("#", line);
 
                 // Split the seperate parts of the point.
                 String[] vals = line.split(",");
                 if (vals.length == numInput + numOutput) {
                     // Get all the input values.
                     for (int i = 0; i < numInput; i++) {
                         if (!vals[i].isEmpty()) {
                            x[i] = (convertStringToDouble(vals[i], message));
                         }
                         else {
                             System.out.println(message);
                             return null;
                         }
                     }
 
                     // Get all the output values.
                     for (int i = 0; i < numOutput; i++) {
                         if (!vals[numInput + i].isEmpty()) {
                             y[i] = (convertStringToDouble(vals[numInput + i], message) / scale);
                         }
                         else {
                             System.out.println(message);
                             return null;
                         }
                     }
 
                     // Add the point to the list of points.
                     points.add(new PointN(x, y));
                 }
                 else {
                     System.out.println(message);
                     return null;
                 }
             }
 
             // Add the list of data points to the collection in fData.
             if (points != null) {
                 fData.addData(points);
             }
 
             return fData;
         }
     }
 
     /**
      * Converts the integer value of the specified string, or null if the value
      * is not an integer.
      *
      * Preconditions: The string must have an integer value.
      * Postconditions: The integer value of the string parameter will be returned.
      */
     public static Integer convertStringToInteger(String string, String prompt) {
         // Convert the value to a number.
         try {
             return (Integer.parseInt(string));
         }
         catch (NumberFormatException nfe) {
             System.out.println(prompt);
             return null;
         }
     }
 
     /**
      * Converts the integer value of the specified string, or null if the value
      * is not an integer.
      *
      * Preconditions: The string must have an integer value.
      * Postconditions: The integer value of the string parameter will be returned.
      */
     public static Double convertStringToDouble(String string, String prompt) {
         // Convert the value to a double.
         try {
             return (Double.parseDouble(string));
         }
         catch (NumberFormatException nfe) {
             System.out.println(prompt);
             return null;
         }
     }
 
     /**
      * Returns a string of user input from the keyboard.
      *
      * Preconditions: None.
      * Postcondition: A string of the user's input will be returned.
      */
     public static String getStringInput(String prompt) {
       // Return the user's input
       return (kb.readString(prompt));
     }
   
     /**
      * Opens a file with the name specified through the parameter, for input.
      * Preconditions: The file must exist.
      * Postcondition: The file with the specified filename will be opened for input and
      * returned, or if it cannot be opened, null will be returned.
      */
     public static FileIO getFileForInput(String filename) {
       // Declare a FileIO object, this is the object that will be returned.
       FileIO inputFile = null;
     
       try {
         // Try to open the file for input.
         inputFile = new FileIO(filename, FileIO.FOR_READING);
       }
       catch (FileIOException fioe) {
         System.out.println("\n" + fioe + "\b, please try another file name.\n");
       }
 
       // Return the file, or null if it was not opened.
       return inputFile;
     }
 }
