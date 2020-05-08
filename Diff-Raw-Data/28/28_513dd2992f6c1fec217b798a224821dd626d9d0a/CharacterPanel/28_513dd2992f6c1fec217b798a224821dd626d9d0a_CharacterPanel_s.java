 package nl.minicom.evenexus.gui.panels.accounts.dialogs;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import javax.inject.Inject;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.border.LineBorder;
 
 import nl.minicom.evenexus.gui.validation.StateRule;
 import nl.minicom.evenexus.gui.validation.ValidationListener;
 import nl.minicom.evenexus.gui.validation.ValidationRule;
 import nl.minicom.evenexus.persistence.Database;
 import nl.minicom.evenexus.persistence.dao.ApiKey;
 
 import org.hibernate.Session;
 
 public class CharacterPanel extends JPanel {
 	
 	private static final long serialVersionUID = 2461165378577113441L;
 	
 	private final Map<EveCharacter, JCheckBox> characterMap;
 	private final Database database;
	private final StateRule addRule;
 	
 	@Inject
	public CharacterPanel(final JButton add, Database database) {
 		super();
 		this.database = database;
 		this.characterMap = new TreeMap<EveCharacter, JCheckBox>();
 		setBorder(new LineBorder(Color.GRAY, 1));
 		setLayout(null);
		
 		addRule = new StateRule() {
 			@Override
 			public void onValid(boolean isValid) {
 				add.setEnabled(isValid);
 			}
 		};
 	}
 	
 	public void addCharacter(EveCharacter c) {
 		if (!characterExists(c.getCharacterId())) {
 			characterMap.put(c, new JCheckBox(c.getName()));
 		}
 		reload();
 	}
 
 	private boolean characterExists(final long characterId) {
 		Session session = database.getCurrentSession();
 		return session.get(ApiKey.class, characterId) != null;
 	}
 	
 	public List<EveCharacter> getSelectedCharacters() {
 		List<EveCharacter> characters = new ArrayList<EveCharacter>();
 		for (Entry<EveCharacter, JCheckBox> entry : characterMap.entrySet()) {
 			if (entry.getValue().isSelected()) {
 				characters.add(entry.getKey());
 			}
 		}
 		return Collections.unmodifiableList(characters);
 	}
 
 	private void reload() {
 		removeAll();
 		int position = 0;
 		
 		ValidationRule validationRule = new ValidationRule(addRule) {				
 			@Override
 			public boolean isValid() {
 				for (EveCharacter c : characterMap.keySet()) {
 					final JCheckBox box = characterMap.get(c);
 					if (box.isSelected()) {
 						return true;
 					}
 				}
 				return false;
 			}
 		};
 		
 		for (EveCharacter c : characterMap.keySet()) {
 			final JCheckBox box = characterMap.get(c);
 			box.setBounds(3, 6 + 18 * position, 250, 15);
 			box.addActionListener(new ValidationListener(validationRule));
 			add(box);
 			position++;
 		}		
 		
 		invalidate();
 		repaint();		
 	}
 
 	public void clear() {
 		characterMap.clear();
 		reload();
 	}	
 
 }
