 package ecologylab.xml.internaltranslators.cocoa;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import com.sun.org.apache.bcel.internal.classfile.Field;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.net.ParsedURL;
 import ecologylab.standalone.xmlpolymorph.BItem;
 import ecologylab.standalone.xmlpolymorph.SchmItem;
 import ecologylab.standalone.xmlpolymorph.Schmannel;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.FieldDescriptor;
 import ecologylab.xml.ClassDescriptor;
 import ecologylab.xml.TranslationScope;
 import ecologylab.xml.XMLTools;
 import ecologylab.xml.library.rss.*;
 import ecologylab.services.messages.*;
 import ecologylab.tutorials.HistoryEchoRequest;
 import ecologylab.tutorials.HistoryEchoResponse;
 
 /**
  * This class is the main class which provides the functionality of translation
  * of Java classes into the objective class header files.
  * 
  * <p>
  * It uses the same syntactical annotations used the by {@code ecologylab.xml}
  * to translate Java objects into xml files. Since it uses the same annotations
  * the data types supported for translation are also the same. The entry point
  * functions into the class are.
  * <ul>
  * <li>{@code translateToObjC(Class<? extends ElementState>, Appendable)}</li>
  * </ul>
  * </p>
  * 
  * @author Nabeel Shahzad
  * @version 1.0
  */
 public class CocoaTranslator
 {
 /**
     * Using this internal class for the calling the hook method after
     * translation of a Java class to C This basically used for {@code
     * @xml_nested} attribute from the {@code ecologylab.xml} When ever we find
     * an {@code @xml_nested} attribute we want to generate the Objective-C
     * header file for the nested class as well
     * 
     * @author Nabeel Shahzad
     */
    private class NestedTranslationHook
    {
       /**
        * Class on which this class will fire a hook method to generate
        * Objective-C class.
        */
       private Class<?>   inputClass;
 
       /**
        * The appendable object on which the hook method will write the generated
        * code.
        */
       private Appendable appendable;
 
       /**
        * Constructor method. Takes the {@code Class} for which it will generate
        * the Objective-C header file and also the {@code Appendable} object on
        * which it will append the generated code.
        * 
        * @param inputClass
        * @param appendable
        */
       public NestedTranslationHook(Class<?> inputClass, Appendable appendable)
       {
          this.inputClass = inputClass;
          this.appendable = appendable;
       }
 
       /**
        * The main hook method. It simple instantiates and object of the {@code
        * CocoaTranslator }and calls the entry point method of {@code
        * translateToObjC(Class<?extends ElementState>, Appendable)} on the
        * already populated member fields.
        * 
        * @throws IOException
        * @throws CocoaTranslationException
        */
       public void execute() throws IOException, CocoaTranslationException
       {
          CocoaTranslator ct = new CocoaTranslator();
 
          ct.translateToObjC(inputClass.asSubclass(ElementState.class), appendable);
 
          if (appendable instanceof BufferedWriter)
          {
             ((BufferedWriter) appendable).close();
          }
       }
    }
 
 /**
     * Member variable to hold the list of the hooks. For each {@code
     * @xml_nested} attribute encountered during the translation to Objective-C
     * file a {@code NestedTranslationHook} is registered which takes care of
     * translating the nested object.
     */
    private ArrayList<NestedTranslationHook> nestedTranslationHooks;
 
    private boolean                          isRecursive;
 
    File                                directoryLocation;
 
    /**
     * Constructor method
     * <p>
     * Initializes the {@code nestedTranslationHooks} member of the class
     * </p>
     */
    public CocoaTranslator()
    {
       nestedTranslationHooks = new ArrayList<NestedTranslationHook>();
       isRecursive = false;
       directoryLocation = null;
    }
 
    /**
     * The main entry function into the class. Goes through a sequence of steps
     * to convert the Java class file into Objective-C header file. It mainly
     * looks for {@code @xml_attribute} , {@code @xml_collection} and {@code
     * @xml_nested} attributes of the {@code ecologylab.xml}.
     * <p>
     * This function will <b>not</b> try to generate the header file for the
     * Class whose objects are present in the current Java file and annotated by
     * {@code ecologylab.xml} attributes.
     * </p>
     * <p>
     * See {@code translateToObjCRecursive()} if you want to generate nested
     * objects
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    public void translateToObjC(Class<? extends ElementState> inputClass, Appendable appendable) throws IOException, CocoaTranslationException
    {
 	  
 	  ClassDescriptor<?> classDescriptor = ClassDescriptor.getClassDescriptor(inputClass);
 	  TranslationConstants.INHERITENCE_OBJECT = classDescriptor.getSuperClassName();	  
 	   
       HashMapArrayList<String, FieldDescriptor> attributes = classDescriptor.getFieldDescriptorsByFieldName();
  
       openHeaderFile(inputClass, appendable);
 
       if (attributes.size() > 0)
       {
          openFieldDeclartion(appendable);
 
          for (FieldDescriptor fieldAccessor : attributes)
          {
         	if(fieldAccessor.belongsTo(classDescriptor))        	
         		appendFieldAsObjectiveCAttribute(fieldAccessor, appendable);
          }
 
          closeFieldDeclartion(appendable);
 
          for (FieldDescriptor fieldAccessor : attributes)
          {
         	if(fieldAccessor.belongsTo(classDescriptor))
         		 appendPropertyOfField(fieldAccessor, appendable);
          }
       }
 
       closeHeaderFile(appendable);
       
       openImplementationFile(inputClass, appendable);
       
       if (attributes.size() > 0)
       {    
          for (FieldDescriptor fieldAccessor : attributes)
          {
         	if(fieldAccessor.belongsTo(classDescriptor))        	
         		appendSynthesizedField(fieldAccessor, appendable);
          }
       }
       
       closeImplementationFile(appendable);
 
       if (isRecursive)
       {
 
          for (NestedTranslationHook nestedTranslationHook : nestedTranslationHooks)
          {
             nestedTranslationHook.execute();
          }
       }
    }
    
    /**
     * The main entry function into the class. Goes through a sequence of steps
     * to convert the Java class file into Objective-C header file. It mainly
     * looks for {@code @xml_attribute} , {@code @xml_collection} and {@code
     * @xml_nested} attributes of the {@code ecologylab.xml}.
     * <p>
     * This function will <b>not</b> try to generate the header file only for the
     * Class whose objects are present in the current Java file and annotated by
     * {@code ecologylab.xml} attributes.
     * </p>
     * <p>
     * See {@code translateToObjCRecursive()} if you want to generate nested
     * objects
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    public void translateToObjCHeader(Class<? extends ElementState> inputClass, Appendable appendable) throws IOException, CocoaTranslationException
    {	   
 	  ClassDescriptor<?> classDescriptor = ClassDescriptor.getClassDescriptor(inputClass);
 	  TranslationConstants.INHERITENCE_OBJECT = classDescriptor.getSuperClassName();	  
 	   
       HashMapArrayList<String, FieldDescriptor> attributes = classDescriptor.getFieldDescriptorsByFieldName();
  
       openHeaderFile(inputClass, appendable);      
 
       if (attributes.size() > 0)
       {
          openFieldDeclartion(appendable);
 
          for (FieldDescriptor fieldAccessor : attributes)
          {
         	if(fieldAccessor.belongsTo(classDescriptor))        	
         		appendFieldAsObjectiveCAttribute(fieldAccessor, appendable);
          }
 
          closeFieldDeclartion(appendable);
 
          for (FieldDescriptor fieldAccessor : attributes)
          {
         	if(fieldAccessor.belongsTo(classDescriptor))
         		 appendPropertyOfField(fieldAccessor, appendable);
          }
       }
       
       for (FieldDescriptor fieldAccessor : attributes)
       {
    	  if(fieldAccessor.belongsTo(classDescriptor) &&  fieldAccessor.isScalar() && fieldAccessor.getScalarType().isPrimitive()
    	    		&& fieldAccessor.getField().getType() != String.class)   	
     		appendFieldSetterFunctionDefinition(appendable, fieldAccessor);
       }
 
       closeHeaderFile(appendable);
       
       if (isRecursive)
       {
 
          for (NestedTranslationHook nestedTranslationHook : nestedTranslationHooks)
          {
             nestedTranslationHook.execute();
          }
       }
    }   
    
    /**
     * The main entry function into the class. Goes through a sequence of steps
     * to convert the Java class file into Objective-C header file. It mainly
     * looks for {@code @xml_attribute} , {@code @xml_collection} and {@code
     * @xml_nested} attributes of the {@code ecologylab.xml}.
     * <p>
     * This function will <b>not</b> try to generate the implementation file only for the
     * Class whose objects are present in the current Java file and annotated by
     * {@code ecologylab.xml} attributes.
     * </p>
     * <p>
     * See {@code translateToObjCRecursive()} if you want to generate nested
     * objects
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    public void translateToObjCImplementation(Class<? extends ElementState> inputClass, Appendable appendable) throws IOException, CocoaTranslationException
    {
 	  
 	  ClassDescriptor<?> classDescriptor = ClassDescriptor.getClassDescriptor(inputClass);
 	  TranslationConstants.INHERITENCE_OBJECT = classDescriptor.getSuperClassName();	  
 	   
       HashMapArrayList<String, FieldDescriptor> attributes = classDescriptor.getFieldDescriptorsByFieldName();
  
       openImplementationFile(inputClass, appendable);
       
       if (attributes.size() > 0)
       {    
          for (FieldDescriptor fieldAccessor : attributes)
          {
         	if(fieldAccessor.belongsTo(classDescriptor))        	
         		appendSynthesizedField(fieldAccessor, appendable);
          }
       }
       
       generateInitializationFunction(inputClass, appendable);
       
       for (FieldDescriptor fieldAccessor : attributes)
       {
    	  if(fieldAccessor.belongsTo(classDescriptor) &&  fieldAccessor.isScalar() && fieldAccessor.getScalarType().isPrimitive()
  	    		&& fieldAccessor.getField().getType() != String.class)  
     		appendFieldSetterFunctionImplementation(appendable, fieldAccessor);
       }
       
       closeImplementationFile(appendable);
    }
 
    
 
 /**
     * Recursive version of the main function. Will also be generating
     * Objective-C header outputs for {@code @xml_nested} objects
     * <p>
     * The main entry function into the class. Goes through a sequence of steps
     * to convert the Java class file into Objective-C header file. It mainly
     * looks for {@code @xml_attribute} , {@code @xml_collection} and {@code
     * @xml_nested} attributes of the {@code ecologylab.xml}.
     * </P>
     * <p>
     * This function will also try to generate the header file for the Class
     * whose objects are present in the current Java file and annotated by
     * {@code ecologylab.xml} attributes.
     * </p>
     * <p>
     * Currently this function is implemented in such a way as to maintain the
     * directory structure of the Java classes mentioned by the package
     * specifiers.
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    public void translateToObjCRecursive(Class<? extends ElementState> inputClass, Appendable appendable) throws IOException, CocoaTranslationException
    {
       isRecursive = true;
       translateToObjC(inputClass, appendable);
    }
 
    /**
     * Takes an input class to generate an Objective-C version of the file. Takes
     * the {@code directoryLocation} of the files where the file needs to be
     * generated.
     * <p>
     * This function internally calls the {@code translateToObjC} main entry
     * function to generate the required files
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    public void translateToObjC(Class<? extends ElementState> inputClass, File directoryLocation) throws IOException, CocoaTranslationException
    {
 	   translateToObjCHeader(inputClass, directoryLocation);
 	   translateToObjCImplementation(inputClass, directoryLocation);
    }
    
    /**
     * Takes an input class to generate an Objective-C version of the file. Takes
     * the {@code directoryLocation} of the files where the file needs to be
     * generated.
     * <p>
     * This function internally calls the {@code translateToObjC} main entry
     * function to generate the required files
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    public void translateToObjC(File directoryLocation, Class<? extends ElementState>... classes) throws IOException, CocoaTranslationException
    {
 	   int length = classes.length;
 	   for(int i = 0; i < length; i++){
 		   translateToObjCHeader(classes[i], directoryLocation);
 		   translateToObjCImplementation(classes[i], directoryLocation);
 	   }	   
    }
    
    /**
     * Takes an input class to generate an Objective-C version of the file. Takes
     * the {@code directoryLocation} of the files where the file needs to be
     * generated.
     * <p>
     * This function internally calls the {@code translateToObjC} main entry
     * function to generate the required files
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    public void translateToObjC(File directoryLocation, TranslationScope tScope) throws IOException, CocoaTranslationException
    {
 	   ArrayList<Class<? extends ElementState>> classes = tScope.getAllClasses();
 	   int length = classes.size();
 	   for(int i = 0; i < length; i++){
 		   translateToObjCHeader(classes.get(i), directoryLocation);
 		   translateToObjCImplementation(classes.get(i), directoryLocation);
 	   }	   
    }
    
    
    /**
     * Takes an input class to generate an Objective-C version of the file. Takes
     * the {@code directoryLocation} of the files where the file needs to be
     * generated.
     * <p>
     * This function internally calls the {@code translateToObjC} main entry
     * function to generate the required files
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    public void translateToObjCHeader(Class<? extends ElementState> inputClass, File directoryLocation) throws IOException, CocoaTranslationException
    {
       File outputFile = createHeaderFileWithDirectoryStructure(inputClass, directoryLocation);
       BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
 
       translateToObjCHeader(inputClass, bufferedWriter);
       bufferedWriter.close();
    }
    
    /**
     * Takes an input class to generate an Objective-C version of the file. Takes
     * the {@code directoryLocation} of the files where the file needs to be
     * generated.
     * <p>
     * This function internally calls the {@code translateToObjC} main entry
     * function to generate the required files
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    public void translateToObjCImplementation(Class<? extends ElementState> inputClass, File directoryLocation) throws IOException, CocoaTranslationException
    {
       File outputFile = createImplementationFileWithDirectoryStructure(inputClass, directoryLocation);
       BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
 
       translateToObjCImplementation(inputClass, bufferedWriter);
       bufferedWriter.close();
    } 
    
 
    /**
     * Recursive function to generate output files of the {@code @xml_nested}
     * objects
     * <p>
     * Takes an input class to generate an Objective-C version of the file. Takes
     * the {@code directoryLocation} of the files where the file needs to be
     * generated.
     * </p>
     * <p>
     * This function internally calls the {@code translateToObjC} main entry
     * function to generate the required files
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     * @throws Exception
     */
    public void translateToObjCRecursive(Class<? extends ElementState> inputClass, File directoryLocation) throws IOException, CocoaTranslationException
    {
       isRecursive = true;
       this.directoryLocation = directoryLocation;
 
       File outputFile = createHeaderFileWithDirectoryStructure(inputClass, directoryLocation);
       BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
 
       translateToObjC(inputClass, bufferedWriter);
       bufferedWriter.close();
    }
 
    /**
     * Simple private function implements the syntax for opening an Objective-C
     * header file. Uses constants and appends them to the appendable object for
     * output.
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     */
    private void openHeaderFile(Class<? extends ElementState> inputClass, Appendable appendable) throws IOException
    {
       appendable.append(TranslationConstants.FOUNDATION_HEADER);
       appendable.append(TranslationConstants.SINGLE_LINE_BREAK);
       appendable.append(TranslationConstants.INCLUDE_OBJECT.replace("@", TranslationConstants.INHERITENCE_OBJECT));
       appendable.append(TranslationConstants.DOUBLE_LINE_BREAK);
       appendable.append(TranslationConstants.INTERFACE);
       appendable.append(TranslationConstants.SPACE);
       appendable.append(XMLTools.getClassName(inputClass));
       appendable.append(TranslationConstants.SPACE);
       appendable.append(TranslationConstants.INHERITENCE_OPERATOR);
       appendable.append(TranslationConstants.SPACE);
       appendable.append(TranslationConstants.INHERITENCE_OBJECT);
       appendable.append(TranslationConstants.SINGLE_LINE_BREAK);
    }
 
    /**
     * Simple private function implements the syntax for closing an Objective-C
     * header file. Uses constants and appends them to the appendable object for
     * output.
     * 
     * @param appendable
     * @throws IOException
     */
    private void closeHeaderFile(Appendable appendable) throws IOException
    {
       appendable.append(TranslationConstants.SINGLE_LINE_BREAK);
       appendable.append(TranslationConstants.END);
       appendable.append(TranslationConstants.DOUBLE_LINE_BREAK);
    }
 
    /**
     * Simple private function implements the syntax for opening the field
     * declaration in Objective-C. Uses constants and appends them to the
     * appendable object for output.
     * 
     * @param appendable
     * @throws IOException
     */
    private void openFieldDeclartion(Appendable appendable) throws IOException
    {
       appendable.append(TranslationConstants.OPENING_CURLY_BRACE);
       appendable.append(TranslationConstants.SINGLE_LINE_BREAK);
    }
 
    /**
     * Simple private function implements the syntax for closing the field
     * declaration in Objective-C. Uses constants and appends them to the
     * appendable object for output.
     * 
     * @param appendable
     * @throws IOException
     */
    private void closeFieldDeclartion(Appendable appendable) throws IOException
    {
       appendable.append(TranslationConstants.CLOSING_CURLY_BRACE);
       appendable.append(TranslationConstants.DOUBLE_LINE_BREAK);
    }
    
    /**
     * Simple private function implements the syntax for opening an Objective-C
     * implementation file. Uses constants and appends them to the appendable object for
     * output.
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     */
    private void openImplementationFile(Class<? extends ElementState> inputClass, Appendable appendable) throws IOException
    {
       appendable.append(TranslationConstants.INCLUDE_OBJECT.replace("@", XMLTools.getClassName(inputClass)));
       appendable.append(TranslationConstants.DOUBLE_LINE_BREAK);
       appendable.append(TranslationConstants.IMPLEMENTATION);
       appendable.append(TranslationConstants.SPACE);
       appendable.append(XMLTools.getClassName(inputClass));
       appendable.append(TranslationConstants.DOUBLE_LINE_BREAK);
    }
    
    /**
     * Simple private function implements the syntax for opening an Objective-C
     * implementation file. Uses constants and appends them to the appendable object for
     * output.
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     */
    private void appendFieldSetterFunctionDefinition(Appendable appendable, FieldDescriptor fieldAccessor) throws IOException, CocoaTranslationException {
 	   
 	   appendable.append(TranslationConstants.SINGLE_LINE_BREAK);
 	   appendable.append("- (void) ");
 	   appendable.append("set" + fieldAccessor.getFieldName().substring(0, 1).toUpperCase());
 	   appendable.append(fieldAccessor.getFieldName().substring(1, fieldAccessor.getFieldName().length()));
 	   appendable.append("WithReference: ");
 	   appendable.append("(" + TranslationUtilities.getObjectiveCType(fieldAccessor.getFieldType()) + " *)");
 	   appendable.append(" p_" + fieldAccessor.getFieldName());
 	   appendable.append(";");
 	   appendable.append(TranslationConstants.SINGLE_LINE_BREAK);
 	}
    
    /**
     * Simple private function implements the syntax for opening an Objective-C
     * implementation file. Uses constants and appends them to the appendable object for
     * output.
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     */
    private void appendFieldSetterFunctionImplementation(Appendable appendable, FieldDescriptor fieldAccessor) throws IOException, CocoaTranslationException {
 	   
 	   appendable.append(TranslationConstants.SINGLE_LINE_BREAK);
 	   appendable.append("- (void) ");
 	   appendable.append("set" + fieldAccessor.getFieldName().substring(0, 1).toUpperCase());
 	   appendable.append(fieldAccessor.getFieldName().substring(1, fieldAccessor.getFieldName().length()));
 	   appendable.append("WithReference: ");
 	   appendable.append("(" + TranslationUtilities.getObjectiveCType(fieldAccessor.getFieldType()) + " *)");
 	   appendable.append(" p_" + fieldAccessor.getFieldName());
 	   appendable.append(" {");
 	   appendable.append(TranslationConstants.SINGLE_LINE_BREAK);
 	   appendable.append("\t" + fieldAccessor.getFieldName() + " = " + "*p_" + fieldAccessor.getFieldName());
 	   appendable.append(";");
 	   appendable.append(TranslationConstants.SINGLE_LINE_BREAK);
 	   appendable.append("}");
 	}
    
    /**
     * Simple private function implements the syntax for closing an Objective-C
     * implemenatation file. Uses constants and appends them to the appendable object for
     * output.
     * 
     * @param appendable
     * @throws IOException
     */
    private void closeImplementationFile(Appendable appendable) throws IOException
    {
       appendable.append(TranslationConstants.DOUBLE_LINE_BREAK);
       appendable.append(TranslationConstants.END);
       appendable.append(TranslationConstants.DOUBLE_LINE_BREAK);
    }
 
    /**
     * Appends an attribute in the Objective-C header file for the corresponding
     * attribute in the Java class file. The attribute can be a primitive type or
     * reference type. Reference type can be a single object, a collection or a
     * nested class object.
     * 
     * @param fieldAccessor
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    private void appendFieldAsObjectiveCAttribute(FieldDescriptor fieldAccessor, Appendable appendable) throws IOException, CocoaTranslationException
    {
       if (fieldAccessor.isCollection())
       {
          appendFieldAsReference(fieldAccessor, appendable);
       }
       else if (fieldAccessor.isScalar())
       {
          if (fieldAccessor.getScalarType().isPrimitive() && fieldAccessor.getField().getType() != String.class)
          {
             appendFieldAsPrimitive(fieldAccessor, appendable);
          }
          else if (fieldAccessor.getScalarType().isReference() || fieldAccessor.getField().getType() == String.class)
          {
             appendFieldAsReference(fieldAccessor, appendable);
          }
       }
       else if (fieldAccessor.isNested())
       {
          appendFieldAsNestedAttribute(fieldAccessor, appendable);
 
          if (isRecursive)
          {
             NestedTranslationHook nestedTranslationHook;
 
             if (directoryLocation == null)
             {
                nestedTranslationHook = new NestedTranslationHook(fieldAccessor.getFieldType(), appendable);
                nestedTranslationHooks.add(nestedTranslationHook);
             }
             else
             {
                File outputFile = createHeaderFileWithDirectoryStructure(fieldAccessor.getFieldType(), directoryLocation);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
                nestedTranslationHook = new NestedTranslationHook(fieldAccessor.getFieldType(), bufferedWriter);
                nestedTranslationHooks.add(nestedTranslationHook);
             }
          }
       }
    }
 
    /**
     * Appends the property of each field using the Objective-C property
     * directive. The object can be a primitive or reference type. Reference type
     * can be a single object, a collection or a nested class object
     * 
     * @param fieldAccessor
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    private void appendPropertyOfField(FieldDescriptor fieldAccessor, Appendable appendable) throws IOException, CocoaTranslationException
    {
       if (fieldAccessor.isCollection())
       {
          appendPropertyAsReference(fieldAccessor, appendable);
       }
       else if (fieldAccessor.isScalar())
       {
          if (fieldAccessor.getScalarType().isPrimitive() && fieldAccessor.getField().getType() != String.class)
          {
             appendPropoertyAsPrimitive(fieldAccessor, appendable);
          }
          else if (fieldAccessor.getScalarType().isReference() || fieldAccessor.getField().getType() == String.class)
          {
             appendPropertyAsReference(fieldAccessor, appendable);
          }
       }
       else if (fieldAccessor.isNested())
       {
     	  appendPropertyAsNestedAttribute(fieldAccessor, appendable);
       }
    }
    
    /**
     * Appends an attribute in the Objective-C header file for the corresponding
     * attribute in the Java class file. The attribute can be a primitive type or
     * reference type. Reference type can be a single object, a collection or a
     * nested class object.
     * 
     * @param fieldAccessor
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    private void appendSynthesizedField(FieldDescriptor fieldAccessor, Appendable appendable) throws IOException, CocoaTranslationException
    {
 	   StringBuilder synthesizeDeclaration = new StringBuilder();
 	   
 	   synthesizeDeclaration.append(TranslationConstants.SYNTHESIZE);
 	   synthesizeDeclaration.append(TranslationConstants.SPACE);
 	   synthesizeDeclaration.append(fieldAccessor.getFieldName());
 	   synthesizeDeclaration.append(TranslationConstants.TERMINATOR);
 	   synthesizeDeclaration.append(TranslationConstants.SINGLE_LINE_BREAK);
 	   
 	   appendable.append(synthesizeDeclaration);
 	   
    }
 
 
    /**
     * Appends a reference type field in the output Objective-C header file
     * 
     * @param fieldAccessor
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    private void appendFieldAsReference(FieldDescriptor fieldAccessor, Appendable appendable) throws IOException, CocoaTranslationException
    {
       StringBuilder fieldDeclaration = new StringBuilder();
 
       fieldDeclaration.append(TranslationConstants.TAB);
       fieldDeclaration.append(TranslationUtilities.getObjectiveCType(fieldAccessor.getField().getType()));
       fieldDeclaration.append(TranslationConstants.SPACE);
       fieldDeclaration.append(TranslationConstants.REFERENCE);
       fieldDeclaration.append(fieldAccessor.getFieldName());
       fieldDeclaration.append(TranslationConstants.TERMINATOR);
       fieldDeclaration.append(TranslationConstants.SINGLE_LINE_BREAK);
 
       appendable.append(fieldDeclaration);
    }
 
    /**
     * Appends a primitive type field in the output Objective-C header file
     * 
     * @param fieldAccessor
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    private void appendFieldAsPrimitive(FieldDescriptor fieldAccessor, Appendable appendable) throws IOException, CocoaTranslationException
    {
       StringBuilder fieldDeclaration = new StringBuilder();
 
       fieldDeclaration.append(TranslationConstants.TAB);
       fieldDeclaration.append(TranslationUtilities.getObjectiveCType(fieldAccessor.getField().getType()));
       fieldDeclaration.append(TranslationConstants.SPACE);
       fieldDeclaration.append(fieldAccessor.getFieldName());
       fieldDeclaration.append(TranslationConstants.TERMINATOR);
       fieldDeclaration.append(TranslationConstants.SINGLE_LINE_BREAK);
 
       appendable.append(fieldDeclaration);
    }
 
    /**
     * Appends a reference type nested field in the output Objective-C header
     * file
     * 
     * @param fieldAccessor
     * @param appendable
     * @throws IOException
     */
    private void appendFieldAsNestedAttribute(FieldDescriptor fieldAccessor, Appendable appendable) throws IOException
    {
       StringBuilder fieldDeclaration = new StringBuilder();
 
       fieldDeclaration.append(TranslationConstants.TAB);
       fieldDeclaration.append(TranslationUtilities.classSimpleName(fieldAccessor.getFieldType()));
       fieldDeclaration.append(TranslationConstants.SPACE);
       fieldDeclaration.append(TranslationConstants.REFERENCE);
       fieldDeclaration.append(fieldAccessor.getFieldName());
       fieldDeclaration.append(TranslationConstants.TERMINATOR);
       fieldDeclaration.append(TranslationConstants.SINGLE_LINE_BREAK);
 
       appendable.append(fieldDeclaration);
    }
 
    /**
     * Appends a reference type attributes property in the output Objective-C
     * header file
     * 
     * @param fieldAccessor
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    private void appendPropertyAsReference(FieldDescriptor fieldAccessor, Appendable appendable) throws IOException, CocoaTranslationException
    {
       StringBuilder propertyDeclaration = new StringBuilder();
 
       propertyDeclaration.append(TranslationConstants.PROPERTY_REFERENCE);
       propertyDeclaration.append(TranslationConstants.SPACE);      
       propertyDeclaration.append(TranslationUtilities.getObjectiveCType(fieldAccessor.getField().getType()));      
       propertyDeclaration.append(TranslationConstants.SPACE);
       propertyDeclaration.append(TranslationConstants.REFERENCE);
       propertyDeclaration.append(fieldAccessor.getFieldName());
       propertyDeclaration.append(TranslationConstants.TERMINATOR);
       propertyDeclaration.append(TranslationConstants.SINGLE_LINE_BREAK);
 
       appendable.append(propertyDeclaration);
    }
    
    /**
     * Appends a reference type attributes property in the output Objective-C
     * header file
     * 
     * @param fieldAccessor
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    private void appendPropertyAsNestedAttribute(FieldDescriptor fieldAccessor, Appendable appendable) throws IOException, CocoaTranslationException
    {
       StringBuilder propertyDeclaration = new StringBuilder();
 
       propertyDeclaration.append(TranslationConstants.PROPERTY_REFERENCE);
       propertyDeclaration.append(TranslationConstants.SPACE);      
       propertyDeclaration.append(TranslationUtilities.classSimpleName(fieldAccessor.getFieldType()));      
       propertyDeclaration.append(TranslationConstants.SPACE);
       propertyDeclaration.append(TranslationConstants.REFERENCE);
       propertyDeclaration.append(fieldAccessor.getFieldName());
       propertyDeclaration.append(TranslationConstants.TERMINATOR);
       propertyDeclaration.append(TranslationConstants.SINGLE_LINE_BREAK);
 
       appendable.append(propertyDeclaration);
    }
 
    /**
     * Appends a primitive type attributes property in the output Objective-C
     * header file
     * 
     * @param fieldAccessor
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
    private void appendPropoertyAsPrimitive(FieldDescriptor fieldAccessor, Appendable appendable) throws IOException, CocoaTranslationException
    {
       StringBuilder propertyDeclaration = new StringBuilder();
 
       propertyDeclaration.append(TranslationConstants.PROPERTY_PRIMITIVE);
       propertyDeclaration.append(TranslationConstants.SPACE);
       propertyDeclaration.append(TranslationUtilities.getObjectiveCType(fieldAccessor.getField().getType()));
       propertyDeclaration.append(TranslationConstants.SPACE);
       propertyDeclaration.append(fieldAccessor.getFieldName());
       propertyDeclaration.append(TranslationConstants.TERMINATOR);
       propertyDeclaration.append(TranslationConstants.SINGLE_LINE_BREAK);
 
       appendable.append(propertyDeclaration);
    }
    
    private void generateInitializationFunction(Class<? extends ElementState> inputClass, Appendable appendable) throws IOException {
 	   
 	   StringBuilder initializationFunction = new StringBuilder();
 	   
 	   initializationFunction.append(TranslationConstants.SINGLE_LINE_BREAK);
 	   initializationFunction.append(TranslationConstants.PLUS);
 	   initializationFunction.append(TranslationConstants.SPACE);
 	   initializationFunction.append(TranslationConstants.OPENING_BRACE);
 	   initializationFunction.append(TranslationConstants.VOID);
 	   initializationFunction.append(TranslationConstants.CLOSING_BRACE);
 	   initializationFunction.append(TranslationConstants.SPACE);	   
 	   initializationFunction.append(TranslationConstants.INITIALIZE);
 	   initializationFunction.append(TranslationConstants.SPACE);
 	   initializationFunction.append(TranslationConstants.OPENING_CURLY_BRACE);
 	   initializationFunction.append(TranslationConstants.SINGLE_LINE_BREAK);
 	   initializationFunction.append(TranslationConstants.TAB);
 	   initializationFunction.append(TranslationConstants.OPENING_SQUARE_BRACE);
 	   initializationFunction.append(inputClass.getSimpleName());	   
 	   initializationFunction.append(TranslationConstants.SPACE);
 	   initializationFunction.append(TranslationConstants.CLASS);
 	   initializationFunction.append(TranslationConstants.CLOSING_SQUARE_BRACE);
 	   initializationFunction.append(TranslationConstants.TERMINATOR);
 	   initializationFunction.append(TranslationConstants.SINGLE_LINE_BREAK);
 	   initializationFunction.append(TranslationConstants.CLOSING_CURLY_BRACE);	   
 	   initializationFunction.append(TranslationConstants.SINGLE_LINE_BREAK);
 	   
 	   appendable.append(initializationFunction);
 	   
    }
 
    /**
     * Creates a directory structure from the path of the given by the {@code
     * directoryLocation} parameter Uses the class and package names from the
     * parameter {@code inputClass}
     * <p>
     * This function deletes the files if the files with same class existed
     * inside the directory structure and creates a new file for that class
     * </p>
     * 
     * @param inputClass
     * @param directoryLocation
     * @return
     * @throws IOException
     */
    private File createHeaderFileWithDirectoryStructure(Class<?> inputClass, File directoryLocation) throws IOException
    {
       String packageName = XMLTools.getPackageName(inputClass);
       String className = XMLTools.getClassName(inputClass);
       String currentDirectory = directoryLocation.toString() + TranslationConstants.FILE_PATH_SEPARATOR;
 
       String[] arrayPackageNames = packageName.split(TranslationConstants.PACKAGE_NAME_SEPARATOR);
 
       for (String directoryName : arrayPackageNames)
       {
          currentDirectory += directoryName + TranslationConstants.FILE_PATH_SEPARATOR;
       }
 
       File directory = new File(currentDirectory);
       directory.mkdirs();
 
       File currentFile = new File(currentDirectory + className + TranslationConstants.HEADER_FILE_EXTENSION);
 
       if (currentFile.exists())
       {
          currentFile.delete();
       }
 
       currentFile.createNewFile();
 
       return currentFile;
    }
    
    /**
     * Creates a directory structure from the path of the given by the {@code
     * directoryLocation} parameter Uses the class and package names from the
     * parameter {@code inputClass}
     * <p>
     * This function deletes the files if the files with same class existed
     * inside the directory structure and creates a new file for that class
     * </p>
     * 
     * @param inputClass
     * @param directoryLocation
     * @return
     * @throws IOException
     */
    private File createImplementationFileWithDirectoryStructure(Class<?> inputClass, File directoryLocation) throws IOException
    {
       String packageName = XMLTools.getPackageName(inputClass);
       String className = XMLTools.getClassName(inputClass);
       String currentDirectory = directoryLocation.toString() + TranslationConstants.FILE_PATH_SEPARATOR;
 
       String[] arrayPackageNames = packageName.split(TranslationConstants.PACKAGE_NAME_SEPARATOR);
 
       for (String directoryName : arrayPackageNames)
       {
          currentDirectory += directoryName + TranslationConstants.FILE_PATH_SEPARATOR;
       }
 
       File directory = new File(currentDirectory);
       directory.mkdirs();
 
       File currentFile = new File(currentDirectory + className + TranslationConstants.IMPLEMENTATION_FILE_EXTENSION);
 
       if (currentFile.exists())
       {
          currentFile.delete();
       }
 
       currentFile.createNewFile();
 
       return currentFile;
    }
    
 
    /**
     * Main method to test the working of the library.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception
    {
       CocoaTranslator c = new CocoaTranslator();
 //      c.translateToObjC(Item.class, new ParsedURL(new File("/")));
       //c.translateToObjC(new ParsedURL(new File("/")), Schmannel.class, BItem.class, SchmItem.class, RssState.class, Item.class, Channel.class);
       c.translateToObjCImplementation(RssState.class, System.out);
    }
 
 }
