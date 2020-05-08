 /**
  * 
  */
 package hu.modembed.pic;
 
 import hexfile.Entry;
 import hexfile.HexFile;
 import hexfile.HexfileFactory;
 import hu.modembed.hexfile.persistence.HexFileResource;
 import hu.modembed.model.pic.ConfigField;
 import hu.modembed.model.pic.ConfigLiteral;
 import hu.modembed.model.pic.ConfigWord;
 import hu.modembed.model.pic.ConfigurationSelection;
 import hu.modembed.model.pic.PICConfigurationModel;
 import hu.modembed.model.pic.PICConfigurationValueModel;
 import hu.modembed.model.pic.PicFactory;
 
 /**
  * @author balazs.grill
  *
  */
 public class ConfigurationUtils {
 
 	public static PICConfigurationValueModel extract(HexFile hexfile, PICConfigurationModel configModel){
 		PICConfigurationValueModel config = PicFactory.eINSTANCE.createPICConfigurationValueModel();
 		config.setDefinition(configModel);
 		
 		for(ConfigWord cw : configModel.getConfigWords()){
 			long address = cw.getAddress();
 			long size = ((cw.getSize()-1)/8)+1;
 			long value = 0;
 			boolean good = true;
 			for(int i=0;i<size;i++){
 				long baddress = (address*size)+i;
 				int v = ConfigUtils.getByte(hexfile, baddress);
 				if (v >= 0){
 					value += ((long)v)<<(8l*i);
 				}else{
 					/* The hex file does not contain this configuration data */
 					good = false;
 				}
 			}
 			
 			if (good){
 				for(ConfigField field : cw.getFields()){
 					long mask = ConfigUtils.mask(field.getSize());
 					long fvalue = (value>>field.getStart()) & mask;
 					ConfigLiteral literal = null;
 					for(ConfigLiteral l : field.getLiterals()){
 						if (l.getValue() == fvalue){
 							literal = l;
 						}
 					}
 					if (literal != null){
 						ConfigurationSelection selection = PicFactory.eINSTANCE.createConfigurationSelection();
 						selection.setField(field);
 						selection.setSelection(literal);
 						config.getValues().add(selection);
 					}
 				}
 			}
 		}
 		return config;
 	}
 	
 	public static HexFile toBinary(PICConfigurationValueModel config){
 
 		HexFile hexFile = HexfileFactory.eINSTANCE.createHexFile();
 		Entry entry = HexfileFactory.eINSTANCE.createEntry();
 		hexFile.getEntries().add(entry);
 
 		PICConfigurationModel architecture = config.getDefinition();
 		long startAddress = Long.MAX_VALUE;
 		long endAddress = 0l;
 		long[] values = new long[architecture.getConfigWords().size()];
 		int i = 0;
 
 		for(ConfigWord word : architecture.getConfigWords()){
 			int size = (int)((word.getSize()-1)/8+1);
 			if (size != 1){
 				entry.setBlocksize(size);
 			}
 			startAddress = Math.min(startAddress, word.getAddress()*size);
 			long end = word.getAddress()*size+size;
 			endAddress = Math.max(endAddress, end);
 
 			long wordValue = word.getDefaultValue();
 			long implMask = ConfigUtils.configImplMask(word);
 			for(ConfigField cf : word.getFields()){
 				for(ConfigurationSelection cs : config.getValues()){
 					if (cs.getSelection() != null && cf.equals(cs.getField())){
 						long fieldValue = cs.getSelection().getValue();
 						wordValue = ConfigUtils.insertValue(wordValue, cf.getStart(), cf.getSize(), fieldValue);
 					}
 				}
 			}
 			values[i] = wordValue & implMask;
 			i++;
 		}
 
 		if (startAddress < endAddress){
 			byte[] data = new byte[(int)(endAddress-startAddress)];
 			for(int j = 0;j<data.length;j++) data[j] = 0;
 			i=0;
 			for(ConfigWord word : architecture.getConfigWords()){
 				int size = (int)((word.getSize()-1)/8+1);
 				long startIndex = (word.getAddress()*size) - startAddress;
 				long value = values[i];
 
 				for(int j=0;j<size;j++){
 					int bv = (int)((value>>(8*j)) & 0xFF);
 					data[(int)startIndex+j] = HexFileResource.intToByte(bv);
 				}
 
 				i++;
 			}
 			entry.setData(data);
 			entry.setAddress((int)startAddress);
 		}
 		
 		return hexFile;
 	}
 	
 }
