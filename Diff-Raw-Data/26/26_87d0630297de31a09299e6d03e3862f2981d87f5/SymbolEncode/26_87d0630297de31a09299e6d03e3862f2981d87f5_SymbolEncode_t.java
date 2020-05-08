 package com.pjf.mat.config.converters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.pjf.mat.api.AttrSysType;
 import com.pjf.mat.api.Attribute;
 import com.pjf.mat.api.MatElementDefs;
 import com.pjf.mat.api.util.AttrConfigGenerator;
 import com.pjf.mat.api.util.ConfigItem;
 
 /**
  * Encode config for a symbol with ID as a number of config items
  * 
  * @author pjf
  *
  */
 public class SymbolEncode implements AttrConfigGenerator {
 
 	@Override
 	public List<ConfigItem> generate(Attribute attr) throws Exception {
 		List<ConfigItem> configs = new ArrayList<ConfigItem>();
 		String val = attr.getValue();
 		if (!val.isEmpty()) {
 			String[] symbols = val.split(",");
 			for (String sym : symbols) {
 				String[] args = sym.split(":");
 				if (args.length != 2) {				
					throw new Exception("Bad value for config item: " + attr.getName() + " on CB " + attr.getParent().getShortName());
 				}
 				String symName = args[0];
 				String idStr = args[1];
 				symName = symName + "        ";
 				symName = symName.substring(0,8);
 				int symId = Integer.parseInt(idStr);
				int elId = attr.getParent().getId();
 				ConfigItem cfg1 = new ConfigItem(elId,AttrSysType.NORMAL,
 						MatElementDefs.EL_MDF_C_ISYM_L,	symName.substring(0,4));
 				ConfigItem cfg2 = new ConfigItem(elId,AttrSysType.NORMAL,
 						MatElementDefs.EL_MDF_C_ISYM_R,	symName.substring(4,8));
 				ConfigItem cfg3 = new ConfigItem(elId,AttrSysType.NORMAL,
 						MatElementDefs.EL_MDF_C_ISYM_ID, symId);
 				configs.add(cfg1);
 				configs.add(cfg2);
 				configs.add(cfg3);	
 			}
 		}
 		return configs;
 	}
 
 }
