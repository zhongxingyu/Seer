 package net.rptools.maptool.client.functions;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolVariableResolver;
 import net.rptools.maptool.model.MacroButtonProperties;
 import net.rptools.maptool.model.Token;
 import net.rptools.parser.Parser;
 import net.rptools.parser.ParserException;
 import net.rptools.parser.function.AbstractFunction;
 
 public class MacroFunctions extends AbstractFunction {
 
 	private static final MacroFunctions instance = new MacroFunctions();
 
 	private MacroFunctions() {
 		super(0, 4, "hasMacro", "createMacro", "setMacroProps", "getMacros", "getMacroProps", "getMacroIndexes",
 				     "getMacroName", "getMacroLocation", "setMacroCommand", "getMacroCommand");
 	}
 	
 	
 	public static MacroFunctions getInstance() {
 		return instance;
 	}
 
 
 	@Override
 	public Object childEvaluate(Parser parser, String functionName,
 			List<Object> parameters) throws ParserException {
 		Token token = ((MapToolVariableResolver)parser.getVariableResolver()).getTokenInContext();
 				
 		if (functionName.equals("hasMacro")) {
 			if (parameters.size() < 1) {
 				throw new ParserException("Not enough arguments for getMacro(name)");				
 			}
 			if (token.getMacroNames(false).contains(parameters.get(0).toString())) {
 				return true; 
 			} else {
 				return false;
 			}
  		} else if (functionName.equals("createMacro")){
  			if (parameters.size() < 2) {
  				throw new ParserException("Not enough arguments for createMacro(name, command)");
  			}
  			MacroButtonProperties mbp = new MacroButtonProperties(token.getMacroNextIndex());
  			mbp.setCommand(parameters.get(1).toString());
  			String delim = parameters.size() > 3 ? parameters.get(3).toString() : ";";
  			setMacroProps(mbp, parameters.get(2).toString(), delim);
  			mbp.setLabel(parameters.get(0).toString());
  			mbp.setSaveLocation("Token");
  			mbp.setTokenId(token);
  			mbp.setApplyToTokens(true);
  			mbp.save();
     		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(),
             		token);
  			return mbp.getIndex();
  		} else if (functionName.equals("getMacros")) {
  			String[] names = new String[token.getMacroNames(false).size()];
  			String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
  			return StringFunctions.getInstance().join(token.getMacroNames(false).toArray(names), delim);
  		} else if (functionName.equals("getMacroProps")) {
  			if (parameters.size() < 1) {
  				throw new ParserException("Not enough arguments to getMacroProp(index)");
  			} 
  			
  			if (!(parameters.get(0) instanceof BigDecimal)) {
  				throw new ParserException("Argument to getMacroProps(index) must be a number");
  			}
  			String delim = parameters.size() > 1 ? parameters.get(1).toString() : ";";
  			return getMacroButtonProps(token, ((BigDecimal)parameters.get(0)).intValue(), delim);
  		} else if (functionName.equals("setMacroProps")) {
  			if (parameters.size() < 2) {
  				throw new ParserException("Not enough arguments to setMacroProps(index)");
  			} 
  			
  			if ((parameters.get(0) instanceof BigDecimal)) {
  				int index = ((BigDecimal)parameters.get(0)).intValue();
  	 			
  	 			MacroButtonProperties mbp = token.getMacro(index, false); 			
  	 			String delim = parameters.size() > 2 ? parameters.get(2).toString() : ";";
  	 			setMacroProps(mbp, parameters.get(1).toString(), delim);
  	 			mbp.save();
 	 		} else {
 	 			for (MacroButtonProperties mbp : token.getMacroList(false)) {
 	 	 			String delim = parameters.size() > 2 ? parameters.get(2).toString() : ";";
 	 				if (mbp.getLabel().equals(parameters.get(0).toString())) {
 	 	 	 			setMacroProps(mbp, parameters.get(1).toString(), delim);
 	 	 	 			mbp.save();	 					
 	 				}
 	 			}
 	 		}
  			
     		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(),
             		token);
     		return ""; 			
  		} else if (functionName.equals("getMacroIndexes")) {
  			if (parameters.size() < 1) {
  				throw new ParserException("Not enough arguments to getMacroIndexes(name)");
  			} 
  			String label = parameters.get(0).toString();
  			StringBuilder sb = new StringBuilder();
  			String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
 
  			for (MacroButtonProperties mbp : token.getMacroList(false)) {
  				if (mbp.getLabel().equals(label)) {
  					if (sb.length() != 0) {
  						sb.append(delim);
  					} 
  					sb.append(mbp.getIndex());
  				}
  			}
  			return sb.toString();
  		} else if (functionName.equals("getMacroName")) {
  			return MapTool.getParser().getMacroName();
  		} else if (functionName.equals("getMacroLocation")) {
  			return MapTool.getParser().getMacroSource();
  		} else if (functionName.equals("setMacroCommand")) { 
 			if (parameters.size() < 2) {
  				throw new ParserException("Not enough arguments to setMacroCommand(index, command)");
  			} 
  
  			if (!(parameters.get(0) instanceof BigDecimal)) {
  				throw new ParserException("Argument to setMacroCommand(index, command) must be a number");
  			}
  			
  			MacroButtonProperties mbp = token.getMacro(((BigDecimal)parameters.get(0)).intValue(), false);
  			mbp.setCommand(parameters.get(1).toString());
  			mbp.save();
     		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(),
             		token);
     		return ""; 			
  		} else if (functionName.equals("getMacroCommand")) {
 			if (parameters.size() < 1) {
  				throw new ParserException("Not enough arguments to getMacroCommand(index)");
  			} 
  
  			if (!(parameters.get(0) instanceof BigDecimal)) {
  				throw new ParserException("Argument to getMacroCommand(index) must be a number");
  			}
  			
  			MacroButtonProperties mbp = token.getMacro(((BigDecimal)parameters.get(0)).intValue(), false);
  			String cmd = mbp.getCommand();
  			return cmd != null ? cmd : "";
 		} else {
  		 return token.getMacro(parameters.get(0).toString(), false).getCommand();
  		}
 	}
 	
 	public String getMacroButtonProps(Token token, int index, String delim) throws ParserException {
  		MacroButtonProperties mbp = token.getMacro(index, !MapTool.getParser().isMacroTrusted());
  		if (mbp == null) {
  			throw new ParserException("No macro at index "+ index);
  		}
  		
  		StringBuilder sb = new StringBuilder();
  		sb.append("autoExecute=").append(mbp.getAutoExecute()).append(delim);
  		sb.append("color=").append(mbp.getColorKey()).append(delim);
  		sb.append("fontColor=").append(mbp.getFontColorKey()).append(delim);
  		sb.append("group=").append(mbp.getGroup()).append(delim);
  		sb.append("includeLabel=").append(mbp.getIncludeLabel()).append(delim);
  		sb.append("sortBy=").append(mbp.getSortby()).append(delim);
  		sb.append("index=").append(mbp.getIndex()).append(delim);
  		sb.append("label=").append(mbp.getLabel()).append(delim);
  		sb.append("fontSize=").append(mbp.getFontSize()).append(delim);
  		sb.append("minWidth=").append(mbp.getMinWidth()).append(delim);
 		return sb.toString();
 	}
 	
 	public void setMacroProps(MacroButtonProperties mbp, String propString, String delim) {
  		String[]  props = propString.split(delim);
 		for (String s : props) {
  			String[] vals = s.split("=");
 	 		vals[0] = vals[0].trim();
 			vals[1] = vals[1].trim();
  			if ("autoexecute".equalsIgnoreCase(vals[0])) {
  				mbp.setAutoExecute(boolVal(vals[1]));
  			} else if ("color".equalsIgnoreCase(vals[0])) {
 				mbp.setColorKey(vals[1]);
  			} else if ("fontColor".equalsIgnoreCase(vals[0])) {
  				mbp.setFontColorKey(vals[1]);
  			} else if ("fontSize".equalsIgnoreCase(vals[0])) {
  				mbp.setFontSize(vals[1]);
  			} else if ("group".equalsIgnoreCase(vals[0])) {
  				mbp.setGroup(vals[1]);
  			} else if ("includeLabel".equalsIgnoreCase(vals[0])) {
  				mbp.setIncludeLabel(boolVal(vals[1]));
  			} else if ("sortBy".equalsIgnoreCase(vals[0])) {
  				mbp.setSortby(vals[1]);
  			} else if ("index".equalsIgnoreCase(vals[0])) {
  				mbp.setIndex(Integer.parseInt(vals[1]));
  			} else if ("label".equalsIgnoreCase(vals[0])) {
  				mbp.setLabel(vals[1]);
  			} else if ("fontSize".equalsIgnoreCase(vals[0])) {
  				mbp.setFontSize(vals[1]);
  			} else if ("minWidth=".equalsIgnoreCase(vals[0])) {
  				mbp.setMinWidth(vals[1]);
  			}
 		}
 	}
 	
 	private boolean boolVal(String val) {
 		if ("true".equalsIgnoreCase(val)) {
 			return true;
 		} 
 		
 		if ("false".equalsIgnoreCase(val)) {
 			return true;
 		}
 
 		try {
 			if (Integer.parseInt(val) == 0) {
 				return false;
 			} else { 
 				return true;
 			}
 		} catch (NumberFormatException e) {
 			return true;
 		}
 	}
 
 }
