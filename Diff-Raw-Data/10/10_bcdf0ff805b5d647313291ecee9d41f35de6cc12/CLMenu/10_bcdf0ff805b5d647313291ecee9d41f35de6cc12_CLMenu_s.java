 package commandline;
 import java.util.Map;
 import java.util.TreeMap;
 
 
 public class CLMenu extends CLCompositeOption {
 	
 	Map<String, CLOption> options = new TreeMap<String, CLOption>();
 	int optionsIndex = 1;
 	
 	private String message;
 	private boolean finished;
 	
 	public CLMenu(String optionText) {
 		this(optionText, null);
 	}
 
 	public CLMenu(String optionText, String text) {
 		super(optionText, text);
 	}
 
 	public void addOption(CLOption option) {
 		option.setSuperMenu(this);
 		options.put(Integer.toString(optionsIndex), option);
 		optionsIndex++;
 		
 	}
 
 	@Override
 	public String getText() {
 		StringBuffer screen = new StringBuffer();
 		
 		if (message != null) {
 			screen.append(message + "\n");
 			message = null;
 		}
 		
 		String menuText = super.getText();
 		if (menuText != null) {
 			screen.append(Constants.TEXT_TITLE);
 			screen.append(menuText);
 			screen.append("\n");
 			screen.append(Constants.TEXT_NORMAL);
 		}
 		
 		for (String key: options.keySet()) {
 			CLOption op = options.get(key);
 			screen.append(Constants.TEXT_OPTIONS);
 			screen.append(key + " - ");
 			screen.append(op.getName());
 			screen.append("\n");
 		}
 		
 		screen.append(optionsIndex + " - ");
 		screen.append("Cancelar");
 		screen.append("\n");
 		
 		screen.append(Constants.TEXT_NORMAL);
 		
 		return screen.toString();
 		
 	}
 
 	@Override
 	public CLCompositeOption answer(String key) {
 		
 		try {
 			// cancel option choosen
 			if (Integer.parseInt(key) == optionsIndex) {
 				finished=true;
 				return getSuperMenu();
 			}
 			
 		} catch (NumberFormatException e) {
 			message = "Opção inválida!";
 			return this;
 		}
 		
 		
 		CLOption option =  options.get(key);
 		
 		if (option == null) {
			//TODO create methods setMessage and setErrorMessage
			message = Constants.TEXT_ERROR + "Opção inválida!" + Constants.TEXT_NORMAL;
 			return this;
 		}
 			
 		if (option instanceof CLCompositeOption) {
 			return (CLCompositeOption) option;
 			
 		} else {
 			message = option.run();
 			return getSuperMenu();
 			
 		}
 			
 		
 	}
 
 	public boolean finished() {
 		return finished;
 	}
 
 
 	
 }
