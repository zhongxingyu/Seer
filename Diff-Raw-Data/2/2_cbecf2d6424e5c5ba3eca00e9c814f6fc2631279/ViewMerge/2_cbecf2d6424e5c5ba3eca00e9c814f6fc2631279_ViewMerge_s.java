 package at.ac.tuwien.sepm.ui.entityViews;
 
 import at.ac.tuwien.sepm.entity.LVA;
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.entity.Module;
 import at.ac.tuwien.sepm.service.*;
 import at.ac.tuwien.sepm.service.impl.LVAUtil;
 import at.ac.tuwien.sepm.service.impl.ValidationException;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.StandardSimpleInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import at.ac.tuwien.sepm.ui.metaLva.MetaLVADisplayPanel;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import com.toedter.calendar.JDateChooser;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.text.DefaultCaret;
 import javax.swing.text.JTextComponent;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 
 /**
  * Author: Georg Plaz
  */
 @UI
 public class ViewMerge extends StandardSimpleInsidePanel {
 
     private MetaLVAService metaLVAService;
     private LVAService lvaService;
     private ModuleService moduleService;
 
     private MetaLVADisplayPanel metaLVAPanel;
 
     private List<MetaLVA> oldIntersectingMetaLVAs;
     private List<MetaLVA> newIntersectingMetaLVAs;
 
     private JTextArea description;
     private JButton takeNewFromMergePanel;
 
     private JLabel metaLVALabel;
     private JLabel labelTakeOldNew;
     private JButton takeOld;
     private JButton takeNew;
 
     private JLabel intersectLabel;
     private JLabel oldDataLabel;
     private JLabel newDataLabel;
 
     private MetaLVAMergePanel mergePanel;
 
     private JButton delete;
 
     private JLabel fromLabel;
     private JLabel toLabel;
     //private JLabel intersectLabel;
 
     private JCheckBox intersectable;
     private JDateChooser from;
     private JSpinner fromTime;
     private JDateChooser to;
     private JSpinner toTime;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Autowired
     public ViewMerge(MetaLVAService metaLVAService,ModuleService moduleService,LVAService lvaService) {
         this.metaLVAService = metaLVAService;
         this.moduleService = moduleService;
         this.lvaService = lvaService;
         init();
         addImage();
         addTitle("Konflikte mergen");
         addReturnButton();
         addContent();
         addButtons();
     }
 
     public void setIntersectingMetaLVAs(List<MetaLVA> oldMetaLVAs, List<MetaLVA> newMetaLVAs) {
         this.oldIntersectingMetaLVAs = new ArrayList<>(oldMetaLVAs);
         this.newIntersectingMetaLVAs = new ArrayList<>(newMetaLVAs);
 
         //oldIntersectingMetaLVAs = new ArrayList<MetaLVA>();
         metaLVAPanel.refresh(oldIntersectingMetaLVAs);
         if(metaLVAPanel.getTable().getRowCount()>0){
             metaLVAPanel.getTable().setRowSelectionInterval(0,0);
         }
         log.info("got old MetaLVAs for merging:\n"+ LVAUtil.formatDetailedMetaLVA(oldMetaLVAs,1));
         log.info("got new MetaLVAs for merging:\n"+ LVAUtil.formatDetailedMetaLVA(newMetaLVAs,1));
         //mergePanel.refresh(oldMetaLVAs.get(0), newMetaLVAs.get(1));
 
     }
 
     private void setDeleteButton(boolean showDeleteButton) {
         if (showDeleteButton) {
             delete.setVisible(true);
         } else {
             delete.setVisible(false);
         }
     }
 
     private void addButtons() {
         takeNewFromMergePanel = new JButton("Diese neuen Daten übernehmen");
         takeNewFromMergePanel.setFont(standardTextFont);
         takeNewFromMergePanel.setBounds(mergePanel.getX()+mergePanel.getWidth()-takeNewFromMergePanel.getPreferredSize().width, mergePanel.getY()+mergePanel.getHeight()+10, takeNewFromMergePanel.getPreferredSize().width, 40);
         takeNewFromMergePanel.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 int selectedRow = metaLVAPanel.getTable().getSelectedRow();
                 try {
                     MetaLVA merged = mergePanel.getMetaLVA();
                     metaLVAService.update(merged);
                     lvaService.update(merged.getLVAs().get(0));
                 } catch (ServiceException e1) {
                     e1.printStackTrace();
                     PanelTube.backgroundPanel.viewInfoText("Beim Speichern ist ein Fehler passiert!", SmallInfoPanel.Error);
                 } catch (ValidationException e1) {
                     e1.printStackTrace();
                     PanelTube.backgroundPanel.viewInfoText("Beim Speichern ist ein Fehler passiert!", SmallInfoPanel.Error);
                 } catch (EscapeException e) {
                     //action cancled, user should already be informed!
                 }
                 newIntersectingMetaLVAs.remove(selectedRow);
                 oldIntersectingMetaLVAs.remove(selectedRow);
                 metaLVAPanel.refresh(oldIntersectingMetaLVAs);
                 if( metaLVAPanel.getTable().getRowCount()>0){
                     if(selectedRow>0){
                         metaLVAPanel.getTable().setRowSelectionInterval(selectedRow-1,selectedRow-1);
                     }else{
                         metaLVAPanel.getTable().setRowSelectionInterval(0,0);
                     }
                 }else{
                     PanelTube.backgroundPanel.viewInfoText("Die Daten wurden erfolgreich zusammengeführt!", SmallInfoPanel.Success);
                     PanelTube.backgroundPanel.showLastComponent();
                 }
             }
         });
         this.add(takeNewFromMergePanel);
     }
 
     private void addContent() {
         int verticalSpace = 10;
         metaLVALabel = new JLabel("Module");
         metaLVALabel.setFont(standardTextFont);
         metaLVALabel.setBounds((int)simpleWhiteSpace.getX()+20,(int)simpleWhiteSpace.getY()+20,(int)simpleWhiteSpace.getWidth()/3,25);
         this.add(metaLVALabel);
         
         metaLVAPanel = new MetaLVADisplayPanel(new ArrayList<MetaLVA>(0),(int)simpleWhiteSpace.getWidth()/3,(int)simpleWhiteSpace.getHeight()-40);
         metaLVAPanel.setBounds(metaLVALabel.getX(), metaLVALabel.getY() + metaLVALabel.getHeight() + 10, metaLVALabel.getWidth(), (int) simpleWhiteSpace.getHeight() - 40 - 100);
         this.add(metaLVAPanel);
         metaLVAPanel.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 if(metaLVAPanel.getTable().getSelectedRowCount()>0){
                     mergePanel.refresh(oldIntersectingMetaLVAs.get(metaLVAPanel.getTable().getSelectedRow()),newIntersectingMetaLVAs.get(metaLVAPanel.getTable().getSelectedRow()));
                 }
             }
         });
 
         //metaLVAPanel.setBounds((int)simpleWhiteSpace.getX()+(int)simpleWhiteSpace.getWidth()*1/3, (int)simpleWhiteSpace.getY()+20,400,300);
 
         labelTakeOldNew = new JLabel("Aktion auf ausgewählte");
         labelTakeOldNew.setFont(standardTextFont);
         labelTakeOldNew.setBounds(metaLVAPanel.getX(), metaLVAPanel.getY() + metaLVAPanel.getHeight() + 10, metaLVAPanel.getWidth(), 25);
         this.add(labelTakeOldNew);
 
         takeOld = new JButton("Übernehme alte");
         takeOld.setFont(standardButtonFont);
         takeOld.setBounds(labelTakeOldNew.getX(),labelTakeOldNew.getY()+labelTakeOldNew.getHeight(), metaLVAPanel.getWidth()/2 -5,25);
         this.add(takeOld);
         takeOld.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 int[] selected = metaLVAPanel.getTable().getSelectedRows();
                 for(int i=selected.length-1;i>=0;i--){
                     //System.out.println("delete: "+i+", ..: "+selected[i]);
                     newIntersectingMetaLVAs.remove(selected[i]);
                     oldIntersectingMetaLVAs.remove(selected[i]);
                 }
                 metaLVAPanel.refresh(oldIntersectingMetaLVAs);
                 if( metaLVAPanel.getTable().getRowCount()>0){
                     metaLVAPanel.getTable().setRowSelectionInterval(0,0);
                 }else{
                     PanelTube.backgroundPanel.viewInfoText("Die Daten wurden erfolgreich zusammengeführt!", SmallInfoPanel.Success);
                     PanelTube.backgroundPanel.showLastComponent();
                 }
             }
         });
 
         takeNew = new JButton("Übernehme neue");
         takeNew.setFont(standardButtonFont);
         takeNew.setBounds(takeOld.getX()+takeOld.getWidth()+10,takeOld.getY(),takeOld.getWidth(),takeOld.getHeight());
         this.add(takeNew);
         takeNew.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 int[] selected = metaLVAPanel.getTable().getSelectedRows();
                 for(int i=selected.length-1;i>=0;i--){
                     try {
                         metaLVAService.update(newIntersectingMetaLVAs.get(selected[i]));
                     } catch (ServiceException e1) {
                         PanelTube.backgroundPanel.viewInfoText("Beim Speichern ist ein Fehler passiert!", SmallInfoPanel.Error);
                         e1.printStackTrace();
                     }
                     newIntersectingMetaLVAs.remove(selected[i]);
                     oldIntersectingMetaLVAs.remove(selected[i]);
                 }
                 metaLVAPanel.refresh(oldIntersectingMetaLVAs);
                 if( metaLVAPanel.getTable().getRowCount()>0){
                     metaLVAPanel.getTable().setRowSelectionInterval(0,0);
                 }else{
                     PanelTube.backgroundPanel.viewInfoText("Die Daten wurden erfolgreich zusammengeführt!", SmallInfoPanel.Success);
                     PanelTube.backgroundPanel.showLastComponent();
                 }
             }
         });
 
         intersectLabel = new JLabel("Konflikte");
         intersectLabel.setFont(standardTextFont);
         intersectLabel.setBounds(metaLVALabel.getX() + metaLVALabel.getWidth() + 20, metaLVALabel.getY(), (int) (simpleWhiteSpace.getX() + simpleWhiteSpace.getWidth()) - (metaLVALabel.getX() + metaLVALabel.getWidth() + 20)-20, 25);
         this.add(intersectLabel);
 
         oldDataLabel = new JLabel("Alte Daten");
         oldDataLabel.setFont(standardTextFont);
         oldDataLabel.setBounds(intersectLabel.getX(), intersectLabel.getY()+intersectLabel.getHeight()+10, intersectLabel.getWidth()/2, 25);
         this.add(oldDataLabel);
 
         newDataLabel = new JLabel("Neue Daten");
         newDataLabel.setFont(standardTextFont);
         newDataLabel.setBounds(oldDataLabel.getX()+oldDataLabel.getWidth()+15,oldDataLabel.getY(), oldDataLabel.getWidth(), oldDataLabel.getHeight());
         this.add(newDataLabel);
 
 
 
         mergePanel = new MetaLVAMergePanel(intersectLabel.getWidth(),(takeOld.getY()+takeOld.getHeight())-intersectLabel.getY());
         this.add(mergePanel);
         int tempYPos=oldDataLabel.getY()+oldDataLabel.getHeight();
         mergePanel.setBounds(intersectLabel.getX(),tempYPos,intersectLabel.getWidth(),(metaLVAPanel.getY()+metaLVAPanel.getHeight())-tempYPos);
 
 
     }
     public class MetaLVAMergePanel extends JScrollPane{
 
         private HashSet<Component> allChangedFields = new HashSet<Component>();
         //private HashSet<JComboBox> allChangedDrops = new HashSet<JComboBox>();
         private int width;
         private int height;
 
         private HashMap<Integer,Integer> allModulesMap;
         private List<Module> allModules;
         // META LVA
         private JTextArea newName;
         private JComboBox<LvaType> newType;
         private JTextArea newNr;
         private JTextArea newECTS;
 
         // LVA
         private JTextArea newDescription;
         private JComboBox<String> newModule;
         private JTextArea newContent;
         private JTextArea newAdditionalInfo1;
         private JTextArea newGoals;
         private JTextArea newAdditionalInfo2;
         private JTextArea newLanguage;
         private JTextArea newInstitute;
         private JTextArea newPerformanceRecord;
 
         private JPanel grid;
         private JPanel myPanel;
         private MetaLVA currentOld;
         private MetaLVA currentNew;
 
 
 
         public MetaLVAMergePanel(int width, int height){
             this.width=width-20;
             this.height=height-20;
             setBackground(Color.WHITE);
             //setBorder(BorderFactory.createEmptyBorder());
             init();
         }
         /*@Override
         public Component add(Component c){
             return getViewport().add(c);
         }*/
         public void refresh(MetaLVA oldMetaLVA, MetaLVA newMetaLVA){
             currentOld=oldMetaLVA;
             currentNew=newMetaLVA;
             getViewport().removeAll();
             //this.add(new JLabel("test"));
             //this.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
 
             grid = new JPanel(new GridBagLayout());
             myPanel = new JPanel();
             myPanel.setLayout(null);
             //myPanel.setBackground(Color.BLUE);
             GridBagConstraints c = new GridBagConstraints();
 
             c.gridx=0;
             c.gridy=0;
             c.weighty=1;
             c.anchor = GridBagConstraints.NORTHWEST;
             int height=0;
             //META LVA
 
 
 
             if(newMetaLVA.getNr() != null && !oldMetaLVA.getNr().equals(newMetaLVA.getNr())){
                 height = addSectionTextArea("LVA-Nummer", oldMetaLVA.getNr(), newNr, newMetaLVA.getNr(), false, height);
             }if(newMetaLVA.getName() != null && !oldMetaLVA.getName().equals(newMetaLVA.getName())){
                 height= addSectionTextArea("Name", oldMetaLVA.getName(), newName, newMetaLVA.getName(), false, height);
             }if(newMetaLVA.getType() != null && !oldMetaLVA.getType().equals(newMetaLVA.getType())){
                 height=addSectionDrops("Typ", oldMetaLVA.getType().ordinal(), newType, newMetaLVA.getType().ordinal(), height);
             }if(Math.abs(oldMetaLVA.getECTS()- newMetaLVA.getECTS())> 0.00001){
                 height=addSectionTextArea("ECTS", "" + oldMetaLVA.getECTS(), newECTS, "" + newMetaLVA.getECTS(), false, height);
             }if(oldMetaLVA.getModule()!= newMetaLVA.getModule()){
                 height=addSectionDrops("Modul", allModulesMap.get(oldMetaLVA.getModule()), newModule, allModulesMap.get(newMetaLVA.getModule()), height);
             }
 
             
             LVA newLVA = newMetaLVA.getLVAs().get(0);
             LVA oldLVA = oldMetaLVA.getLVA(newLVA.getYear(),newLVA.getSemester());
             if(newLVA != null && oldLVA != null){ //todo if any of the two MetaLVAs have no LVA assigned, this will be skipped
                 // LVA
 
                 //year
 
                 if(newLVA.getDescription()!=null && !newLVA.getDescription().equals(oldLVA.getDescription())){
                     height = addSectionTextArea("Beschreibung", oldLVA.getDescription(), newDescription, newLVA.getDescription(), true, height);
                 }if(newLVA.getContent()!=null && !newLVA.getContent().equals(oldLVA.getContent())){
                     height = addSectionTextArea("Inhalt", oldLVA.getContent(), newContent, newLVA.getContent(), true, height);
                 }if(newLVA.getAdditionalInfo1()!=null && !newLVA.getAdditionalInfo1().equals(oldLVA.getAdditionalInfo1())){
                     height = addSectionTextArea("Zusätzliche Info 1", oldLVA.getAdditionalInfo1(), newAdditionalInfo1, newLVA.getAdditionalInfo1(), true, height);
                 }if(newLVA.getAdditionalInfo2()!=null && !newLVA.getAdditionalInfo2().equals(oldLVA.getAdditionalInfo2())){
                     height = addSectionTextArea("Zusätzliche Info 2", oldLVA.getAdditionalInfo2(), newAdditionalInfo2, newLVA.getAdditionalInfo2(), true, height);
                 }if(newLVA.getGoals()!=null && !newLVA.getGoals().equals(oldLVA.getGoals())){
                     height = addSectionTextArea("Ziele", oldLVA.getGoals(), newGoals, newLVA.getGoals(), true, height);
                 }if(newLVA.getPerformanceRecord()!=null && !newLVA.getPerformanceRecord().equals(oldLVA.getPerformanceRecord())){
                     height = addSectionTextArea("Performance", oldLVA.getPerformanceRecord(), newPerformanceRecord, newLVA.getPerformanceRecord(), true, height);
                 }if(newLVA.getLanguage()!=null && !newLVA.getLanguage().equals(oldLVA.getLanguage())){
                     height = addSectionTextArea("Sprache", oldLVA.getLanguage(), newLanguage, newLVA.getLanguage(), false, height);
                 }if(newLVA.getInstitute()!=null && !newLVA.getInstitute().equals(oldLVA.getInstitute())){
                     height = addSectionTextArea("Institut", oldLVA.getInstitute(), newInstitute, newLVA.getInstitute(), false, height);
                 }
                 //todo missing attributes from LVA
             }
 
             /**how to:
              *  height = addSectionTextArea( *title* , *old-lva value*, *text-area*, *new-lva value*, *4-lines or 1line*, height);
              *
              *  height = addSectionDrops( *title* , *old-lva index*, *JComboBox*, *new-lva index*, height);
              *
              *  das attribut height einfach immer übergeben.
              */
 
             grid.setPreferredSize(new Dimension(width,grid.getPreferredSize().height));
             myPanel.setPreferredSize(new Dimension(width,height));
             myPanel.setSize(new Dimension(width, myPanel.getPreferredSize().height));
             myPanel.setMinimumSize(new Dimension(width, myPanel.getPreferredSize().height));
             myPanel.setBackground(Color.WHITE);
             getViewport().add(myPanel);
             //myPanel.repaint();
             //revalidate();
             //repaint();
         }
         public MetaLVA getMetaLVA() throws EscapeException {
             MetaLVA toReturn = new MetaLVA();
             toReturn.setId(currentOld.getId());
             //System.out.println("my debug: "+toReturn.getId());
             toReturn.setPriority(currentOld.getPriority());
 
             //  META-LVA
             if(allChangedFields.contains(newName)){
                 if(newName.getText().trim().length()==0){
                     PanelTube.backgroundPanel.viewInfoText("Der angegebene Name darf nicht leer sein!", SmallInfoPanel.Warning);
                     throw new EscapeException();
                 }
                 toReturn.setName(newName.getText());
             }else{
                 toReturn.setName(currentOld.getName());
             }
             if(allChangedFields.contains(newNr)){
                 if(newNr.getText().trim().length()==0){
                     PanelTube.backgroundPanel.viewInfoText("Die angegebene LVA-Nummer darf nicht leer sein!", SmallInfoPanel.Warning);
                     throw new EscapeException();
                 }
                 toReturn.setNr(newNr.getText());
             }else{
                 toReturn.setNr(currentOld.getNr());
             }
             if(allChangedFields.contains(newType)){
                 toReturn.setType((LvaType) newType.getSelectedItem());
             }else{
                 toReturn.setType(currentOld.getType());
             }
             if(allChangedFields.contains(newModule)){
                 toReturn.setModule(allModules.get(newModule.getSelectedIndex()).getId());
             }else{
                 toReturn.setModule(currentOld.getModule());
             }
             try{
 
                 if(allChangedFields.contains(newECTS)){
                     float ects = Float.parseFloat(newECTS.getText());
                     if(ects<0){
                         PanelTube.backgroundPanel.viewInfoText("Die angegebenen ECTS müssen positiv sein!", SmallInfoPanel.Warning);
                         throw new EscapeException();
                     }
                     toReturn.setECTS(ects);
                 }else{
                     toReturn.setECTS(currentOld.getECTS());
                 }
             } catch(NumberFormatException e){
                 PanelTube.backgroundPanel.viewInfoText("Die angegebenen ECTS sind ungültig!", SmallInfoPanel.Warning);
                 throw new EscapeException();
             }
 
             //  LVA
             LVA temp = new LVA();
             LVA oldLVA = currentOld.getLVAs().get(0);
             temp.setId(oldLVA.getId());
             temp.setMetaLVA(currentOld);
             temp.setSemester(oldLVA.getSemester());
             temp.setYear(oldLVA.getYear());
             temp.setGrade(oldLVA.getGrade());
             temp.setInStudyProgress(oldLVA.isInStudyProgress());
             if(allChangedFields.contains(newDescription)){
                 temp.setDescription(newDescription.getText());
             }if(allChangedFields.contains(newAdditionalInfo1)){
                 temp.setAdditionalInfo1(newAdditionalInfo1.getText());
             }if(allChangedFields.contains(newAdditionalInfo2)){
                 temp.setAdditionalInfo2(newAdditionalInfo2.getText());
             }if(allChangedFields.contains(newContent)){
                 temp.setContent(newContent.getText());
             }if(allChangedFields.contains(newGoals)){
                 temp.setGoals(newGoals.getText());
             }if(allChangedFields.contains(newLanguage)){
                 temp.setLanguage(newLanguage.getText());
             }if(allChangedFields.contains(newInstitute)){
                 temp.setInstitute(newInstitute.getText());
             }if(allChangedFields.contains(newPerformanceRecord)){
                temp.setInstitute(newPerformanceRecord.getText());
             }
             //todo add missing attributes
             toReturn.setLVA(temp);
             return toReturn;
         }
         public void init(){
             // <META LVA>
             newName = new JTextArea();
             newType = new JComboBox<LvaType>(LvaType.values());
             newNr = new JTextArea();
             newECTS = new JTextArea();
             // </META LVA>
 
             // <Module laden>
             allModules = null;
             allModulesMap = new HashMap<Integer, Integer>();
             /*try {
                 allModules = moduleService.readAll();
             } catch (ServiceException e) {
                 //todo user balbla
             }*/
             allModules=new ArrayList<Module>(0);
             String[] moduleName = new String[allModules.size()];
             int i=0;
             for(Module m:allModules){
                 allModulesMap.put(m.getId(),i);
                 moduleName[i++] = m.getName();
             }
             // </Module laden>
 
             // <LVA>
 
             newDescription = new JTextArea();
             newModule = new JComboBox<String>(moduleName);
             newContent = new JTextArea();
             newAdditionalInfo1 = new JTextArea();
             newGoals = new JTextArea();
             newAdditionalInfo2 = new JTextArea();
             newLanguage = new JTextArea();
             newInstitute = new JTextArea();
             newPerformanceRecord = new JTextArea();
             //todo missing lva-attributes initialisieren
             // </LVA>
         }
 
         public int addSectionDrops(String head, int oldSelected, JComboBox newDrop, int newSelected, int height){
             allChangedFields.add(newDrop);
             newDrop.setSelectedIndex(newSelected);
             //newTextArea.setPreferredSize(new Dimension(width/2, 25*lines));
             newDrop.setFont(standardTextFont);
             newDrop.setPreferredSize(new Dimension(width / 2, newDrop.getPreferredSize().height));
 
 
             JTextArea oldField = new JTextArea(newDrop.getItemAt(oldSelected).toString());
             oldField.setEditable(false);
             oldField.setFont(standardTextFont);
             oldField.setPreferredSize(new Dimension(width / 2, newDrop.getPreferredSize().height));
             oldField.setBorder(new JTextField().getBorder());
             JTextField temp = new JTextField();
             temp.setEnabled(false);
             oldField.setBackground(new Color(230,230,230));
 
             JLabel headLabel = new JLabel(head);
             headLabel.setFont(standardTextFont);
 
 
             JButton tempButton = new JButton();
 
             try {
                 tempButton.setIcon(new ImageIcon(ImageIO.read(new File("src/main/resources/img/navright.png"))));
             } catch (IOException e) {
                 e.printStackTrace();
             }
             tempButton.setBorder(null);
             tempButton.setBorderPainted(false);
             tempButton.setMargin(new Insets(0, 0, 0, 0));
             tempButton.setOpaque(false);
             tempButton.setContentAreaFilled(false);
             tempButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
             tempButton.addActionListener(new MyOtherListener(oldSelected,newDrop));
 
 
             myPanel.add(headLabel);
             myPanel.add(oldField);
             myPanel.add(tempButton);
             myPanel.add(newDrop);
 
             headLabel.setBounds(0,height,width,25);
             height+=headLabel.getHeight();
             Dimension size = tempButton.getPreferredSize();
             oldField.setBounds(0,height,width/2-size.width/2,oldField.getPreferredSize().height);
             tempButton.setBounds(width/2-size.width/2-5,height-5,size.width,oldField.getPreferredSize().height);
             newDrop.setBounds(width / 2 + size.width / 2, height, width / 2 - size.width / 2, newDrop.getPreferredSize().height);
             return height+newDrop.getHeight();
         }
         public int addSectionTextArea(String head, String oldText, JTextArea newTextArea, String newText, boolean multiLine, int height){
             allChangedFields.add(newTextArea);
             int lines=1;
             if(multiLine){
                 newTextArea.setLineWrap(true);
                 lines=4;
             }
             newTextArea.setText(newText);
             newTextArea.setFont(standardTextFont);
             newTextArea.setWrapStyleWord(true);
             newTextArea.setAutoscrolls(true);
             JScrollPane newTextScrollPane = new JScrollPane();
             newTextScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
             newTextScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
             newTextScrollPane.getViewport().add(newTextArea);
             newTextArea.setCaretPosition(0);
 
 
             DefaultCaret caret = (DefaultCaret) newTextArea.getCaret();
             caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 
             JTextArea oldTextArea = new JTextArea(oldText);
             oldTextArea.setEditable(false);
             oldTextArea.setFont(standardTextFont);
             oldTextArea.setLineWrap(newTextArea.getLineWrap());
             oldTextArea.setWrapStyleWord(newTextArea.getWrapStyleWord());
             JTextField temp = new JTextField();
             temp.setEnabled(false);
             oldTextArea.setBackground(new Color(230, 230, 230));
 
             JScrollPane oldTextScrollPane = new JScrollPane();
             oldTextScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
             oldTextScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
             oldTextScrollPane.getViewport().add(oldTextArea);
             oldTextArea.setCaretPosition(0);
 
             JLabel headLabel = new JLabel(head);
             headLabel.setFont(standardTextFont);
 
             JButton tempButton = new JButton();
 
             try {
                 tempButton.setIcon(new ImageIcon(ImageIO.read(new File("src/main/resources/img/navright.png"))));
             } catch (IOException e) {
                 e.printStackTrace();
             }
             tempButton.setBorder(null);
             tempButton.setBorderPainted(false);
             tempButton.setMargin(new Insets(0, 0, 0, 0));
             tempButton.setOpaque(false);
             tempButton.setContentAreaFilled(false);
             tempButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
             tempButton.addActionListener(new MyListener(oldTextArea,newTextArea));
 
 
 
             myPanel.add(headLabel);
             myPanel.add(oldTextScrollPane);
             myPanel.add(tempButton);
             myPanel.add(newTextScrollPane);
 
             headLabel.setBounds(0,height,width,25);
             height+=headLabel.getHeight();
             Dimension size = tempButton.getPreferredSize();
             oldTextScrollPane.setBounds(0,height,width/2-size.width/2,25*lines);
             tempButton.setBounds(width/2-size.width/2-5,oldTextScrollPane.getY()+oldTextScrollPane.getHeight()/2-tempButton.getPreferredSize().height/2,size.width,tempButton.getPreferredSize().height);
             newTextScrollPane.setBounds(width/2+size.width/2,height,width/2-size.width/2,25*lines);
             return height+newTextScrollPane.getHeight();
         }
 
         private class MyListener implements ActionListener{
             private JTextComponent oldC;
             private JTextComponent newC;
             public MyListener(JTextComponent oldC, JTextComponent newC){
                 this.oldC=oldC;
                 this.newC=newC;
             }
             @Override
             public void actionPerformed(ActionEvent e) {
                 newC.setText(oldC.getText());
             }
         }private class MyOtherListener implements ActionListener{
 
             private int oldSelection;
             private JComboBox newDrop;
 
             public MyOtherListener(int oldSelection, JComboBox newDrop){
                 this.oldSelection = oldSelection;
                 this.newDrop = newDrop;
             }
             @Override
             public void actionPerformed(ActionEvent e) {
                 newDrop.setSelectedIndex(oldSelection);
             }
         }
     }
 
 }
