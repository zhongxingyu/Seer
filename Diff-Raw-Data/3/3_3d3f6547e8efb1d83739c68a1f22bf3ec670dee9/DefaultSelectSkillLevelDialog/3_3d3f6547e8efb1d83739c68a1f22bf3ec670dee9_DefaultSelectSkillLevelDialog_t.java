 package org.timadorus.webapp.client.character.ui.selectskill;
 
 import java.util.ArrayList;
import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.timadorus.webapp.beans.Character;
 import org.timadorus.webapp.beans.Skill;
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.DefaultTimadorusWebApp;
 import org.timadorus.webapp.client.character.ui.DefaultActionHandler;
 import org.timadorus.webapp.client.character.ui.DefaultDialog;
 import org.timadorus.webapp.client.character.ui.DefaultDisplay;
 
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class DefaultSelectSkillLevelDialog extends DefaultDialog<DefaultSelectSkillLevelDialog.Display> {
 
   public interface Display extends DefaultDisplay {
     public String getSelectedItemSkills();
 
     public String getSelectedItemAddedSkills();
 
     public void removeItemAddedSkills(String itemname);
 
     public void addItemAddedSkills(String item);
 
     public void readyToSave(boolean hasSelectedItems);
 
     public void reloadSkillCostTable(Set<String> addedSkillList, List<Skill> skillLevel1);
 
     public void addTextBoxHanlder(TextBoxHandler handler);
 
     public void addNextButtonHandler(DefaultActionHandler handler);
 
     public void addPrevButtonHandler(DefaultActionHandler handler);
 
     public void addAddButtonHandler(DefaultActionHandler handler);
 
     public void addRemoveButtonHandler(DefaultActionHandler handler);
 
     public void addResetButtonHandler(DefaultActionHandler handler);
 
     public void addSkillListBoxHandler(DefaultActionHandler handler);
 
     public void onNextButtonClick(DefaultTimadorusWebApp entry, Character character, User user);
 
     public void onPrevButtonClick(DefaultTimadorusWebApp entry, Character character, User user);
   }
 
   private Character character;
 
   private User user;
 
   protected Set<String> addedSkillList;
 
   private List<Skill> backupSkillList;
 
   protected List<Skill> skillList;
 
   public DefaultSelectSkillLevelDialog(Display display, DefaultTimadorusWebApp entry, Character character, User user,
                                        List<Skill> skills) {
     super(display, entry);
 
     this.character = character;
     this.user = user;
     this.skillList = skills;
     this.backupSkillList = new ArrayList<Skill>(skills);
 
     addedSkillList = new HashSet<String>();
     getDisplay().addAddButtonHandler(new DefaultActionHandler() {
 
       @Override
       public void onAction() {
         onAddButtonClick();
       }
     });
 
     getDisplay().addNextButtonHandler(new DefaultActionHandler() {
 
       @Override
       public void onAction() {
         onNextButtonClick();
       }
     });
 
     getDisplay().addPrevButtonHandler(new DefaultActionHandler() {
 
       @Override
       public void onAction() {
         onPrevButtonClick();
       }
     });
 
     getDisplay().addRemoveButtonHandler(new DefaultActionHandler() {
 
       @Override
       public void onAction() {
         onRemoveButtonClick();
       }
     });
 
     getDisplay().addResetButtonHandler(new DefaultActionHandler() {
 
       @Override
       public void onAction() {
         onResetButtonClick();
       }
     });
     getDisplay().addSkillListBoxHandler(new DefaultActionHandler() {
 
       @Override
       public void onAction() {
         onSkillListBoxClick();
       }
     });
 
     getDisplay().addTextBoxHanlder(new TextBoxHandler() {
 
       @Override
       public void onChange(String[] skInfo) {
         onTextboxChange(skInfo);
       }
     });
   }
 
   public void onResetButtonClick() {
     skillList = backupSkillList;
     // getEntry().getTestValues().setSkillsLevel1(backupSkillList);
     RootPanel.get("content").clear();
     RootPanel.get("content").add(DefaultSelectSkillLevelDialog.getDialog(getEntry(), character, user).getFormPanel());
   }
 
   public void loadSelectAppearancePanel() {
     RootPanel.get("content").clear();
   }
 
   private void onRemoveButtonClick() {
     String skillName = getDisplay().getSelectedItemAddedSkills();
 
     if (skillName != null) {
       getDisplay().removeItemAddedSkills(skillName);
       addedSkillList.remove(skillName);
       getDisplay().reloadSkillCostTable(addedSkillList, skillList);
     }
     getDisplay().readyToSave(!addedSkillList.isEmpty());
   }
 
   private void onSkillListBoxClick() {
     // show skill informations
     String skillName = getDisplay().getSelectedItemSkills();
 
     RootPanel.get("information").clear();
 
     for (Skill newSkill : skillList) {
       if (newSkill.getName().equals(skillName)) {
         RootPanel.get("information").add(new HTML("<h1>" + newSkill.getName() + "</h1><p>" + newSkill.getDescription()
                                              + "</p>" + "<p>" + newSkill.toString() + "</p>"));
       }
     }
   }
 
   public void saveSelectedSkillsInCharacter() {
     for (String skillName : addedSkillList) {
       for (Skill skill : skillList) {
         if (skill.getName().equals(skillName)) {
           character.getSkillListLevel1().add(skill);
         }
       }
     }
   }
 
   protected void onNextButtonClick() {
     saveSelectedSkillsInCharacter();
     // loadSelectAppearancePanel();
   }
 
   private void onAddButtonClick() {
     String skillName = getDisplay().getSelectedItemSkills();
 
     if (!addedSkillList.contains(skillName)) {
       getDisplay().addItemAddedSkills(skillName);
       addedSkillList.add(skillName);
       getDisplay().reloadSkillCostTable(addedSkillList, skillList);
     }
     getDisplay().readyToSave(!addedSkillList.isEmpty());
   }
 
   private void onTextboxChange(String[] skInfo) {
 
     // List<Skill> skillList = getEntry().getTestValues().getSkillsLevel1();
     for (Skill skill : skillList) {
       if (skill.getName().equals(skInfo[0])) {
         if (skInfo[1].equalsIgnoreCase("Cost")) {
           skill.setCost(Integer.valueOf(skInfo[2]));
         }
         if (skInfo[1].equalsIgnoreCase("Rank")) {
           skill.setRank(Integer.valueOf(skInfo[2]));
         }
         if (skInfo[1].equalsIgnoreCase("Rk_Bn")) {
           skill.setRk_Bn(Integer.valueOf(skInfo[2]));
         }
         if (skInfo[1].equalsIgnoreCase("Stat_Bn")) {
           skill.setStat_Bn(Integer.valueOf(skInfo[2]));
         }
         if (skInfo[1].equalsIgnoreCase("Level_Bn")) {
           skill.setLevel_Bn(Integer.valueOf(skInfo[2]));
         }
         if (skInfo[1].equalsIgnoreCase("Item")) {
           skill.setItem(Integer.valueOf(skInfo[2]));
         }
 
         skill.setTotal("update");
         skillList.remove(skill);
         skillList.add(skill);
       }
     }
   }
 
   protected void onPrevButtonClick() {
   }
 
   public Character getCharacter() {
     return character;
   }
 
   public User getUser() {
     return user;
   }
 
   public static DefaultSelectSkillLevelDialog getDialog(DefaultTimadorusWebApp entry, Character character, User user) {
     List<Skill> skills = entry.getTestValues().getSkillsLevel1();
     DefaultSelectSkillLevelDialog.Display display = new DefaultSkillLevelWidget(character, skills);
     DefaultSelectSkillLevelDialog dialog = new DefaultSelectSkillLevelDialog(display, entry, character, user, skills);
     return dialog;
   }
 }
