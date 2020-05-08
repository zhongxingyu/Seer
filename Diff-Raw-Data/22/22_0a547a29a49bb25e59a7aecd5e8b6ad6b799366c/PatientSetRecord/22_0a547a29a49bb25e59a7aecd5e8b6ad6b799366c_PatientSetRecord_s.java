 package Barramundi;
 
 import java.util.Vector;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 import java.lang.StringBuffer;
 import java.lang.ClassNotFoundException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import javax.swing.*;
 import java.awt.*;
 import javax.swing.event.*;
 
 /** 
 	A set of patient records to run through the simulator.
 	toString() and fromString() implement the parsing interface required for DBs.
 	generatePatients() converts a set into a form usable by the simulator.
 */
 class PatientSetRecord implements DatabaseRecord {
 	/** Id of the patient set                            */ String name;
 	/** Input data file containing patient thresholds    */ String sourceFilename;
 	/** Number of patients to read from the file (0=all) */ int numberOfRecords;
 	/** First record number from source file to read     */ int startRecord;
 	/** Contains the seen() method                       */ Seen seenObject;
 	/** name of seen class                               */ String seenClassName;
 	/** False Positive rate                              */ String falsePositiveRate;
 	/** false pos rate                                   */ String falseNegativeRate;
 
 	/** These will be converted to Static Modifiers      */ Vector patientModifiers;
 
 	/**
 		Hold the patient modifier attributes read from the database 
 		in case they are nedded for the simulation.
 	*/
 	class PatientModifier {
 		String name;
 		Double parameters[];
 		PatientModifier(String n, Double p[]) {name=n;parameters=p;}
 	}
 	
 	/** 
 		Convert a PatientSet into a String
 	*/
 	public String toString() {
 		StringTokenizer t = new StringTokenizer(seenObject.getClass().getName(), "$");
 		String seenName = new String();
 		StringBuffer s = new StringBuffer();  // the string representation to return
 		
 			// extract the name of the seen function by stripping off the 
 			// "Barramundi.SeenFactory$" prefix
 		if (t.countTokens() != 2)
 			seenName = new String(seenObject.getClass().getName());
 		else {
 			while (t.hasMoreTokens() )
 			seenName = new String(t.nextToken());
 		}
 
 		s = new StringBuffer(name + ":  " + 
 				Integer.toString(numberOfRecords) + " records from file " + 
 				sourceFilename + " beginning at record " + 
 				Integer.toString(startRecord) + ".  " + 
 				seenName + ".  ");
 
 			// extract the names of the patient modifiers
		Enumeration e = patientModifiers.elements();
		while (e.hasMoreElements())
			s.append(((PatientModifier)e.nextElement()).name +" ");
 		
 		return s.toString();
 	}// toString()
 
 	/**	Convert a String into a PatientSet	*/
 	public void fromString(String s, String delimiter) throws DatabaseException { 
 		String temp;
 		
    		StringTokenizer t = new StringTokenizer(s, delimiter);
    		
        	name              = t.nextToken();
    		numberOfRecords   = Integer.parseInt(t.nextToken());
    		startRecord       = Integer.parseInt(t.nextToken());
        	sourceFilename    = t.nextToken();
        	seenClassName     = t.nextToken();
   		falsePositiveRate = t.nextToken();
   		falseNegativeRate = t.nextToken();
        	while (t.hasMoreTokens()) {
        		String name          = t.nextToken();
        		int    numParameters = Integer.parseInt(t.nextToken());
 			Double params[] = new Double[numParameters];
 			for(int i = 0 ; i < numParameters ; i++)
 		       	params[i] = new Double(Double.parseDouble(t.nextToken()));
 		    patientModifiers.addElement(new PatientModifier(name, params));
        	}// while
        	   	   			
    			// get the seen object from the seen factory
 		try {
 			final Class seenClass   = Class.forName("Barramundi.SeenFactory");
 			final Method seenMethod = seenClass.getMethod("get"+seenClassName, new Class[] {Class.forName("java.lang.Double"), Class.forName("java.lang.Double")});
 			final Object params[]   = new Object[] {new Double(Double.parseDouble(falsePositiveRate)), new Double(Double.parseDouble(falseNegativeRate))};
 			seenObject        = (Seen)seenMethod.invoke(null, params);
 		} catch (ClassNotFoundException e) {
 			JOptionPane.showMessageDialog(null,"Could not find Barramundi.SeenFactory", "Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 		} catch (NoSuchMethodException e) {
 			JOptionPane.showMessageDialog(null,"SeenFactory does not contain a get"+seenClassName+"() method","Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 		} catch (SecurityException e) {
 			JOptionPane.showMessageDialog(null,"Attempting to find the get"+seenClassName+"\n"+e,"Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 		} catch (IllegalAccessException e) {
 			JOptionPane.showMessageDialog(null,"Could not execute get"+seenClassName+" method\n"+e,"Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 		} catch (IllegalArgumentException e) {
 			JOptionPane.showMessageDialog(null,"Could not execute get"+seenClassName+" method\n"+e,"Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 		} catch (InvocationTargetException e) {
 			JOptionPane.showMessageDialog(null,"Could not execute get"+seenClassName+" method\n"+e,"Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 		}
     }// fromString()	
     
     /**	Convert this record into a String for writing to database */
 	public String toDBString(String delimiter) throws DatabaseException { 
 		StringBuffer temp = new StringBuffer(
 			name + delimiter +
 			Integer.toString(numberOfRecords) + delimiter +
 			Integer.toString(startRecord) + delimiter +
 			sourceFilename + delimiter +
        		seenObject.getClass().getName() + delimiter +
        		falsePositiveRate + delimiter +
        		falseNegativeRate);
        		
        	Enumeration e = patientModifiers.elements();
        	while (e.hasMoreElements()) {
        		PatientModifier pm = (PatientModifier)e.nextElement();
        		temp.append(delimiter + pm.name);
        		temp.append(delimiter + pm.parameters.length);
        		for(int i = 0 ; i < pm.parameters.length ; i++)
        			temp.append(delimiter + pm.parameters[i].toString());
        	}
        	
        	return temp.toString();
     }// toDBString()
 
 	/**
 		Construct a PatientSet from a String
 		@param s 		 A delimited string of fields.
 		@param delimiter The field delimiter.
 	*/
 	PatientSetRecord(String s, String delimiter) throws DatabaseException	{ 
 		patientModifiers = new Vector();
 		fromString(s, delimiter);
 	}
 	PatientSetRecord() {patientModifiers = new Vector(); }
 	
 	/**
 		This fills in a patient array by reading the data from the specified file 
 		and applying static modifiers.  
 	*/
 	public Patient[] generatePatients() throws PatientModifierException, DatabaseException {
 		Patient patient[] = readPatientData();
 		
 			// create a list of PatientModifier objects (patMods)
 		Vector patMods   = new Vector();			
		Enumeration enum = patientModifiers.elements();
		while(enum.hasMoreElements())
			patMods.addElement(createPatientModifier((PatientModifier)enum.nextElement()));
 
 			// apply each static modifier.
 		try {
 			for(int m = 0 ; m < patMods.size() ; m++) {
 				Class intfaces[] = patMods.elementAt(m).getClass().getInterfaces();
 				for(int i = 0 ; i < intfaces.length ; i++)
 					if (intfaces[i] == Class.forName("Barramundi.StaticModifier")) {
 						StaticModifier sm = (StaticModifier)patMods.elementAt(m);
 						for(int j = 0 ; j < numberOfRecords ; j++)
 							sm.massage(patient[j]);
 					} else
 						throw new PatientModifierException("Class "+patMods.elementAt(m).getClass().getClass()
 											+ " does not implement StaticModifier interafaces.");
 			}
 					
         } catch (ClassNotFoundException e) {
         	throw new PatientModifierException("Patient modifier class " + e + " cannot be found.");
         } catch (Exception e) {System.err.println(e);System.exit(1);}
 		
 					
 		return patient;
 	}
 	
 	/**
 		Read in patient data from "filename".  Patient data file is treated as a database 
 		of PatientDataRecords. If numberOfRecords == 0 then it is reset to the maximum 
 		number of records in the database.
 	*/
 	Patient[] readPatientData() throws DatabaseException {
 		PatientData pdb   = new PatientData(sourceFilename);
 		
 		if (numberOfRecords == 0)
 			numberOfRecords = pdb.list.size();
 
 		Patient[] patient = new Patient[numberOfRecords];	
 		
 		for(int i = 0, j = startRecord; (i < numberOfRecords) && (j < pdb.list.size()) ; i++, j++) {
 			PatientDataRecord pdr = (PatientDataRecord)pdb.list.elementAt(j);
 			
 			patient[i] = new Patient(pdr.name, this.name, pdr.age, seenObject);
 			
 			patient[i].threshold = new double[pdr.numThresholds];
 			for(int k = 0 ; k < pdr.numThresholds ; k++)
 				patient[i].threshold[k] = pdr.threshold[k];
 		}
 		
 		return patient;
 	}// readPatientData()
 
 	/** 
 		Given the name and parameters of the modifier, create an instance and return it.
 	*/
 	Object createPatientModifier(PatientModifier patientModifier) throws PatientModifierException {
 		int numParameters = patientModifier.parameters.length;
 		try { 
 			Class c = Class.forName(patientModifier.name); 
 			
 			Class  paramClasses[] = new Class[numParameters];
 			Object params[]       = new Object[numParameters];
 			for(int i = 0 ; i < numParameters ; i++) {
 	       		paramClasses[i] = Class.forName("java.lang.Double");
 	       		params[i]       = patientModifier.parameters[i];
 	       	}	
 
 			Constructor cons = c.getDeclaredConstructor(paramClasses);
 			
 			return cons.newInstance(params);
 				
 		} catch (NoSuchMethodException e) {
 			throw new PatientModifierException("Patient modifier class " + patientModifier.name +
 					" should have a constructor with "+numParameters+
 					" double parameters.");
 		} catch (SecurityException e) {
 			throw new PatientModifierException("Patient modifier class " + patientModifier.name +
 					" should have a constructor with "+numParameters+
 					" double parameters.");
         } catch (InstantiationException e) {
         	throw new PatientModifierException("Could not instantiate patient modifier "+ patientModifier.name+ e);
         } catch (InvocationTargetException e) {
         	throw new PatientModifierException("Trouble invoking patient modifier " +patientModifier.name+ e);
         } catch (IllegalAccessException e) {
         	throw new PatientModifierException("Illegal access when instantiating patient modifier class "+patientModifier.name + e);
         } catch (ClassNotFoundException e) {
         	throw new PatientModifierException("Patient modifier class " + patientModifier.name + " cannot be found." + e);
         }
 	}// createPatientModifier()
 	
 	/**	
 		Provide a GUI to edit this object's variables.  Note that static modifiers
 		are not handled by this GUI, so they should be entered into the text file 
 		underlying the database by hand.
 		@param parentFrame The container to hold any GUI widgets.
 	*/
 	public boolean edit(JFrame parentFrame) {
     	JTextField  nameField, numRecsField, startRecField, sourceFilenameField, 
     				seenClassField, falsePField, falseNField;
     	JTextField  numStaticModifiersField;
     	
     	JPanel      thePanel;
     	
     	class MyListener implements DocumentListener {
     		JFrame frame;
     		MyListener(JFrame f) { frame = f;}
     		public void insertUpdate(DocumentEvent e) {
 				JOptionPane.showMessageDialog(frame,
 					"Haven't implemented this yet.\nEnter static modifiers by hand.",
 					"Barramundi error",
 					JOptionPane.ERROR_MESSAGE);
     			//alterUIForStaticModifiers(Integer.parseInt(e.getDocument().getText(0,e.getDocument().getLength())));
     		}
     		public void changedUpdate(DocumentEvent e) {
 				JOptionPane.showMessageDialog(frame,
 					"Haven't implemented this yet.\nEnter static modifiers by hand.",
 					"Barramundi error",
 					JOptionPane.ERROR_MESSAGE);
     		}
     		public void removeUpdate(DocumentEvent e) {
 				JOptionPane.showMessageDialog(frame,
 					"Haven't implemented this yet.\nEnter static modifiers by hand.",
 					"Barramundi error",
 					JOptionPane.ERROR_MESSAGE);
     		}
     	}
 
 		thePanel = new JPanel(false);
 		thePanel.setLayout(new BoxLayout(thePanel, BoxLayout.X_AXIS));
 
 		JPanel namePanel = new JPanel(false);
 		namePanel.setLayout(new GridLayout(0, 1));
 		namePanel.add(new JLabel("Patient set name ", JLabel.RIGHT));
 		namePanel.add(new JLabel("Patient data filename ", JLabel.RIGHT));
 		namePanel.add(new JLabel("Number of records to read (0 for all) ", JLabel.RIGHT));
 		namePanel.add(new JLabel("Starting record (starting at 0) ", JLabel.RIGHT));
 		namePanel.add(new JLabel("Name of Seen Class (as in SeenFactory) ", JLabel.RIGHT));
 		namePanel.add(new JLabel("False Positive rate (between 0 and 1) ", JLabel.RIGHT));
 		namePanel.add(new JLabel("False Negative rate (between 0 and 1) ", JLabel.RIGHT));
 		namePanel.add(new JLabel("Number of static modifiers ", JLabel.RIGHT));
 
 		if (falsePositiveRate == null) falsePositiveRate = new String("0.0");
 		if (falseNegativeRate == null) falseNegativeRate = new String("0.0");
 		
 		JPanel fieldPanel = new JPanel(false);
 		fieldPanel.setLayout(new GridLayout(0, 1));
 		fieldPanel.add(nameField               = new JTextField(name,10));
 		fieldPanel.add(sourceFilenameField     = new JTextField(sourceFilename));
 		fieldPanel.add(numRecsField            = new JTextField(Integer.toString(numberOfRecords)));
 		fieldPanel.add(startRecField           = new JTextField(Integer.toString(startRecord)));
 		fieldPanel.add(seenClassField          = new JTextField(seenClassName));
 		fieldPanel.add(falsePField             = new JTextField(falsePositiveRate));
 		fieldPanel.add(falseNField             = new JTextField(falseNegativeRate));
 		fieldPanel.add(new JLabel("Not implemented yet.  Add static modifiers by hand"));
 
 //    	numStaticModifiersField.getDocument().addDocumentListener(new MyListener(parentFrame));
     	
     	thePanel.add(namePanel);
 		thePanel.add(fieldPanel);
 
 	    	/**
     	 	* Brings up a JDialog using JOptionPane containing the parameters.
      		* If the user clicks on the 'Done' button then record is altered and true is returned, else false.
 	     	*/
     	if(JOptionPane.showOptionDialog(parentFrame, thePanel, "Add new Stimulus",
 			   JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
    	                null, new String[] {"Done", "Cancel"}, "Done") == 0) {
 		       	name              = nameField.getText();
    				numberOfRecords   = Integer.parseInt(numRecsField.getText());
 		   		startRecord       = Integer.parseInt(startRecField.getText());
 		       	sourceFilename    = sourceFilenameField.getText();
 		       	seenClassName     = seenClassField.getText();
 		  		falsePositiveRate = falsePField.getText();
 		  		falseNegativeRate = falseNField.getText();
 		  		if (seenObject == null) // get the seen object from the seen factory
 				try {
 					final Class seenClass   = Class.forName("Barramundi.SeenFactory");
 					final Method seenMethod = seenClass.getMethod("get"+seenClassName, new Class[] {Class.forName("java.lang.Double"), Class.forName("java.lang.Double")});
 					final Object params[]   = new Object[] {new Double(Double.parseDouble(falsePositiveRate)), new Double(Double.parseDouble(falseNegativeRate))};
 					seenObject        = (Seen)seenMethod.invoke(null, params);
 					return true;
 				} catch (ClassNotFoundException e) {
 					JOptionPane.showMessageDialog(null,"Could not find Barramundi.SeenFactory", "Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 				} catch (NoSuchMethodException e) {
 					JOptionPane.showMessageDialog(null,"SeenFactory does not contain a get"+seenClassName+"() method","Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 				} catch (SecurityException e) {
 					JOptionPane.showMessageDialog(null,"Attempting to find the get"+seenClassName+"\n"+e,"Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 				} catch (IllegalAccessException e) {
 					JOptionPane.showMessageDialog(null,"Could not execute get"+seenClassName+" method\n"+e,"Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 				} catch (IllegalArgumentException e) {
 					JOptionPane.showMessageDialog(null,"Could not execute get"+seenClassName+" method\n"+e,"Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 				} catch (InvocationTargetException e) {
 					JOptionPane.showMessageDialog(null,"Could not execute get"+seenClassName+" method\n"+e,"Barramundi - Error",JOptionPane.ERROR_MESSAGE);
 				}
 		}
 		return false;
 	}// edit()
 }// end PatientSetRecord class
