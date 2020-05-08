 package plugins.adufour.ezplug;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.HashMap;
 
 import plugins.adufour.ezplug.EzMessage.MessageType;
 import plugins.adufour.ezplug.EzMessage.OutputType;
 
 
 /**
  * Class allowing EzPlug to load/save parameters from/to disk in XML format
  * 
  * @author Alexandre Dufour
  *
  */
 class EzParameterIO
 {
 	@SuppressWarnings("rawtypes")
 	static synchronized void save(HashMap<String, EzVar<?>> varMap, File f)
 	{
 		HashMap<String, Object> parameters = new HashMap<String, Object>();
 		
 		for (String id : varMap.keySet())
 		{
 			EzVar<?> var = varMap.get(id);
 			
 			if (var instanceof EzVar.Storable)
 			{
 				parameters.put(id, ((EzVar.Storable) var).getXMLValue());
 			}
 			else
 			{
 				EzMessage.message("variable " + id + " has not been saved (unsupported variable type)", MessageType.WARNING, OutputType.SYSTEM_OUT);
 			}
 		}
 		
 		try
 		{
 			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(f)));
 			encoder.writeObject(parameters);
 			encoder.close();
 			EzMessage.message("Parameters have been saved to " + f.getAbsolutePath(), MessageType.INFORMATION, OutputType.DIALOG);
 		}
 		catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
	static synchronized void load(File f, HashMap<String, EzVar<?>> varMap)
 	{
 		try
 		{
 			XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(f)));
 			Object object = decoder.readObject();
 			decoder.close();
 			
 			HashMap<String, Object> valueMap = (HashMap<String, Object>) object;
 			
 			for (String id : valueMap.keySet())
 			{
 				@SuppressWarnings("rawtypes")
 				EzVar.Storable var = (EzVar.Storable) varMap.get(id);
 				Object value = valueMap.get(id);
 				var.setXMLValue(value);
 			}
 			
 			EzMessage.message("Parameters have been loaded from " + f.getAbsolutePath(), MessageType.INFORMATION, OutputType.SYSTEM_OUT);
 		}
 		catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 		catch (ClassCastException cce)
 		{
 			System.err.println("Invalid parameter file");
 		}
 	}
 }
