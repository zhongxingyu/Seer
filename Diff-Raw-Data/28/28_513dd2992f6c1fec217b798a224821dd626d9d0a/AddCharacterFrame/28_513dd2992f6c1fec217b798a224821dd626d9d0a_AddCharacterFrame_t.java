 package nl.minicom.evenexus.gui.panels.accounts.dialogs;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import nl.minicom.evenexus.eveapi.ApiParser;
 import nl.minicom.evenexus.eveapi.ApiParser.Api;
 import nl.minicom.evenexus.eveapi.exceptions.SecurityNotHighEnoughException;
 import nl.minicom.evenexus.eveapi.exceptions.WarnableException;
 import nl.minicom.evenexus.eveapi.importers.ImportManager;
 import nl.minicom.evenexus.gui.panels.accounts.AccountsPanel;
 import nl.minicom.evenexus.gui.utils.dialogs.BugReportDialog;
 import nl.minicom.evenexus.gui.utils.dialogs.CustomDialog;
 import nl.minicom.evenexus.gui.utils.dialogs.DialogTitle;
 import nl.minicom.evenexus.gui.validation.StateRule;
 import nl.minicom.evenexus.gui.validation.ValidationListener;
 import nl.minicom.evenexus.gui.validation.ValidationRule;
 import nl.minicom.evenexus.persistence.Database;
 import nl.minicom.evenexus.persistence.dao.ApiKey;
 import nl.minicom.evenexus.persistence.interceptor.Transactional;
 
 import org.hibernate.Session;
 import org.mortbay.xml.XmlParser.Node;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AddCharacterFrame extends CustomDialog {
 
 	private static final long serialVersionUID = 5630084784234969950L;
 	
 	private static final Logger LOG = LoggerFactory.getLogger(AddCharacterFrame.class);
 
 	private final AccountsPanel panel;
 	private final Provider<ApiParser> apiParserProvider;
 	private final CharacterPanel characterPanel;
 	private final ImportManager importManager;
 	private final Database database;
 	private final BugReportDialog dialog;
 	
 	@Inject
 	public AddCharacterFrame(AccountsPanel panel, 
 			Provider<ApiParser> apiParserProvider,
 			CharacterPanel characterPanel,
 			ImportManager importManager,
 			Database database,
 			BugReportDialog dialog) {
 		
 		super(DialogTitle.CHARACTER_ADD_TITLE, 400, 375);
 		this.apiParserProvider = apiParserProvider;
 		this.characterPanel = characterPanel;
 		this.importManager = importManager;
 		this.database = database;
 		this.panel = panel;
 		this.dialog = dialog;
 	}
 	
 	public void initialize() {
 		buildGui();
 		setVisible(true);
 	}
 
 	@Override
 	public void createGui(JPanel guiPanel) {		
 		final JLabel userIDLabel = new JLabel("User ID");
 		final JLabel apiKeyLabel = new JLabel("API Key");
 		final JTextField userIDField = new JTextField();
 		final JTextField apiKeyField = new JTextField();
 		final JButton check = new JButton("Check API credentials");
 		final JButton add = new JButton("Add character(s)");
		
		characterPanel.initialize(add);
		
 		check.setEnabled(false);
 		add.setEnabled(false);
 		
 		userIDLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
 		userIDField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
 		apiKeyLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
 		apiKeyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
 		characterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
 		check.setMaximumSize(new Dimension(160, 32));
 		add.setMaximumSize(new Dimension(160, 32));
 		
 		GroupLayout layout = new GroupLayout(guiPanel);
 		guiPanel.setLayout(layout);        
         layout.setHorizontalGroup(
         	layout.createSequentialGroup()
     		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addComponent(userIDLabel)
 				.addComponent(userIDField)
 				.addComponent(apiKeyLabel)
 				.addComponent(apiKeyField)
 				.addComponent(check)
 				.addComponent(characterPanel)
 				.addComponent(add)
     		)
     	);
     	layout.setVerticalGroup(
     		layout.createSequentialGroup()
     		.addComponent(userIDLabel)
 			.addComponent(userIDField)
 			.addGap(7)
 			.addComponent(apiKeyLabel)
 			.addComponent(apiKeyField)
 			.addGap(7)
 			.addComponent(check)
 			.addGap(7)
 			.addComponent(characterPanel)
 			.addGap(7)
 			.addComponent(add)
     	);
     	
     	check.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				new Thread() {
 					@Override
 					public void run() {
 						check.setEnabled(false);
 						
 						Map<String, String> request = new TreeMap<String, String>();
 						request.put("userID", userIDField.getText());
 						request.put("apiKey", apiKeyField.getText());
 						
 						characterPanel.clear();
 						List<EveCharacter> characters = checkCredentials(request);
 						if (characters != null) {
 							for (EveCharacter character : characters) {
 								characterPanel.addCharacter(character);
 							}
 						}
 						
 						check.setEnabled(true);
 					}
 				}.start();
 			}
 		});
     	
     	add.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				new Thread() {
 					@Override
 					public void run() {
 						List<EveCharacter> characters = characterPanel.getSelectedCharacters();
 						try {
 							for (EveCharacter character : characters) {
 								ApiKey apiKey = insertCharacter(character);
 								importManager.addCharacterImporter(apiKey);
 							}
 						}
 						catch (Exception e) {
 							LOG.error(e.getLocalizedMessage(), e);
 							dialog.setVisible(true);
 						}
 						finally {
 							panel.reloadTab();
 						}
 					}
 				}.start();
 				dispose();
 			}
 		});
 					
 		setValidation(userIDField, apiKeyField, check);
 	}
 
 	@Transactional
 	private ApiKey insertCharacter(final EveCharacter character) {
 		Session session = database.getCurrentSession();
 		ApiKey api = new ApiKey();
 		api.setApiKey(character.getApiKey());
 		api.setCharacterID(character.getCharacterId());
 		api.setCharacterName(character.getName());
 		api.setUserID(character.getUserId());
 		session.saveOrUpdate(api);
 		return api;
 	}
 	
 	private List<EveCharacter> checkCredentials(Map<String, String> request) {
 		List<EveCharacter> characters = getCharacterList(request);
 		if (characters == null || characters.isEmpty()) {
 			LOG.warn("Account has no characters listed!");
 			return null;
 		}
 		
 		request.put("characterID", Long.toString(characters.get(0).getCharacterId()));
 		if (hasSecurityClearance(request)) {
 			return characters;
 		}
 		else {
 			LOG.warn("API key does not have FULL clearance!");
 		}
 		
 		return null;
 	}
 	
 	private List<EveCharacter> getCharacterList(Map<String, String> request) {
 		try {
 			ApiParser parser = apiParserProvider.get();
 			Node root = parser.parseApi(Api.CHAR_LIST, null, request);
 			return processParser(root, request);
 		}
 		catch (WarnableException e) {
 			LOG.warn(e.getLocalizedMessage(), e);
 		}
 		catch (Throwable e) {
 			LOG.error(e.getLocalizedMessage(), e);
 			dialog.setVisible(true);
 		}
 		return null;
 	}
 	
 	//This should throw an exception when user enters a limited API.
 	private boolean hasSecurityClearance(Map<String, String> request) {
 		try {
 			ApiParser parser = apiParserProvider.get();
 			Node response = parser.parseApi(Api.CHAR_BALANCE, null, request);
 			ApiParser.isAvailable(response);
 		}
 		catch (SecurityNotHighEnoughException e) {
 			LOG.warn(e.getLocalizedMessage(), e);
 			return false;
 		}
 		catch (Exception e) {
 			LOG.error(e.getLocalizedMessage(), e);
 			dialog.setVisible(true);
 		}
 		return true;
 	}
 
 	protected List<EveCharacter> processParser(Node root, Map<String, String> request) throws Exception {
 		
 		List<EveCharacter> characters = new ArrayList<EveCharacter>();
 		if (ApiParser.isAvailable(root)) {
 			Node node = root.get("result").get("rowset");
 			if (node != null) {
 				for (int i = 0; i < node.size(); i++) {
 					EveCharacter character = processRow(node, i, request);
 					if (character != null) {
 						characters.add(character);
 					}
 				}
 			}
 		}
 		return characters;
 	}		
 
 	private EveCharacter processRow(Node root, int index, Map<String, String> request) throws SQLException {
 		
 		EveCharacter character = null;
 		if (root.get(index) instanceof Node) {
 			Node row = (Node) root.get(index);
 			if (row != null && row.getTag().equals("row")) {
 				String name = row.getAttribute("name");
 				int userID = Integer.parseInt(request.get("userID"));
 				String apiKey = request.get("apiKey");
 				long characterID = Long.parseLong(row.getAttribute("characterID"));
 				character = new EveCharacter(name, userID, apiKey, characterID);
 			}
 		}
 		return character;
 	}
 
 	private void setValidation(final JTextField userIDField, final JTextField apiKeyField, final JButton check) {
 		
 		ValidationRule userIdRule = new ValidationRule() {			
 			@Override
 			public boolean isValid() {
 				return ValidationRule.isNotEmpty(userIDField.getText()) 
 					&& ValidationRule.isDigit(userIDField.getText());
 			}
 		};
 		
 		ValidationRule apiKeyRule = new ValidationRule() {			
 			@Override
 			public boolean isValid() {
 				return ValidationRule.hasLength(apiKeyField.getText(), 64) 
 					&& ValidationRule.isLetterOrDigit(apiKeyField.getText());
 			}
 		};
 		
 		new StateRule(userIdRule, apiKeyRule) {
 			@Override
 			public void onValid(boolean isValid) {
 				check.setEnabled(isValid);
 			}
 		};
 		
 		userIDField.addKeyListener(new ValidationListener(userIdRule));
 		apiKeyField.addKeyListener(new ValidationListener(apiKeyRule));
 	}	
 }
