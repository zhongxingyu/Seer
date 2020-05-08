 package at.ac.tuwien.sepm.ui.entityViews;
 
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.entity.Module;
 import at.ac.tuwien.sepm.service.EscapeException;
 import at.ac.tuwien.sepm.service.MetaLVAService;
 import at.ac.tuwien.sepm.service.ModuleService;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.service.impl.ValidationException;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.StandardSimpleInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import at.ac.tuwien.sepm.ui.metaLva.MetaLVADisplayPanel;
 import at.ac.tuwien.sepm.ui.template.HintTextField;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import at.ac.tuwien.sepm.ui.template.SelectItem;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Georg
  * Date: 04.06.13
  * Time: 13:57
  * To change this template use File | Settings | File Templates.
  */
 @UI
 public class ViewModule extends StandardSimpleInsidePanel {
     //private JLabel nameLabel;
     //private JTextField name;
     private JLabel nameLabel;
     private HintTextField nameInput;
 
     private JLabel descriptionLabel;
     private JTextArea descriptionInput;
 
     private JTextArea priorityLabel;
     private HintTextField priorityInput;
 
     private JLabel metaLVALabel;
     private MetaLVADisplayPanel metaLVADisplayPanel;
 
     private Rectangle paneModule = new Rectangle(520,95,490,465);
 
     private JButton save;
 
     private ModuleService moduleService;
     private MetaLVAService metaLVAService;
     private Module module;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Autowired
     public ViewModule(ModuleService moduleService,MetaLVAService metaLVAService) {
         this.moduleService=moduleService;
         this.metaLVAService=metaLVAService;
         init();
         addImage();
         this.module = new Module();
         module.setMetaLvas(new ArrayList<MetaLVA>(0));
         addTitle("Neues Modul");
         addReturnButton();
         addContent();
         addButtons();
         this.repaint();
         this.revalidate();
     }
 
     private void addButtons() {
         save = new JButton("Speichern");
         save.setFont(standardTextFont);
         save.setBounds((int)simpleWhiteSpace.getX()+(int)simpleWhiteSpace.getWidth()*1/3-170-120, (int)simpleWhiteSpace.getY() + (int)simpleWhiteSpace.getHeight()-60, 130,40);
         save.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
 
                     if(!priorityInput.getText().trim().equals("")){
                         float newPriority = Float.parseFloat(priorityInput.getText());
 
                         for(MetaLVA mLVA : module.getMetaLvas()){
                             if(mLVA.getPriority()>(newPriority+0.001) || mLVA.getPriority()<(newPriority-0.001)){
                                 int optionChosen = JOptionPane.showConfirmDialog(ViewModule.this, "Wollen sie wirklich die Priorität aller LVAs aus dem Modul: "+module.getName()+" auf " + newPriority + " setzen?", "Fehler", JOptionPane.OK_CANCEL_OPTION);
                                 if(optionChosen==JOptionPane.OK_OPTION){
                                     for(MetaLVA metaLVA:module.getMetaLvas()){
                                         metaLVA.setPriority(newPriority);
                                         metaLVAService.update(metaLVA);
                                     }
                                 } else {
                                    throw new EscapeException();
                                 }
                                 break;
                             }
                         }
                     }
                     module.setName(title.getText());
                     module.setDescription(descriptionInput.getText());
                     if (module.getId() != null) {
                         moduleService.update(module);
                     } else {
                         moduleService.create(module);
                     }
                     PanelTube.backgroundPanel.viewInfoText("Das Modul wurde gespeichert.", SmallInfoPanel.Info);
                     setVisible(false);
                     PanelTube.backgroundPanel.showLastComponent();
                 } catch (ServiceException e) {
                     log.error("Module is invalid.");
                     PanelTube.backgroundPanel.viewInfoText("Die Angaben sind ungültig.", SmallInfoPanel.Error);
                 } catch (ValidationException e) {
                     log.error("Module is invalid.");
                     PanelTube.backgroundPanel.viewInfoText("Die Angaben sind ungültig.", SmallInfoPanel.Error);
                 } catch (NumberFormatException e){
                     log.error("Input priority is invalid.");
                     PanelTube.backgroundPanel.viewInfoText("Die angegebene Priorität ist ungültig!", SmallInfoPanel.Error);
                 } catch (EscapeException ignore){
                     log.info("user cancelled saving");
                 }
             }
         });
         this.add(save);
     }
 
     private void addContent() {
         int verticalSpace = 10;
         nameLabel = new JLabel("Name:");
         nameLabel.setFont(standardTextFont);
         nameLabel.setBounds((int) simpleWhiteSpace.getX() + 20, (int) paneModule.getY(), 110, 25);
 
         this.add(nameLabel);
 
         nameInput = new HintTextField("");
         nameInput.setFont(standardTextFont);
         nameInput.setBounds(nameLabel.getX() + nameLabel.getWidth() + 20, nameLabel.getY(), 250, 25);
         //nameInput.setBorder(new JTextField().getBorder());
         nameInput.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 changeTitle(nameInput.getText());
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 changeTitle(nameInput.getText());
             }
 
             @Override
             public void changedUpdate(DocumentEvent e) {
                 changeTitle(nameInput.getText());
             }
         });
 
         this.add(nameInput);
 
 
         descriptionLabel = new JLabel("Beschreibung:");
         descriptionLabel.setFont(standardTextFont);
         descriptionLabel.setBounds( nameLabel.getX(), nameLabel.getY()+nameLabel.getHeight()+verticalSpace, 110, 25);
         this.add(descriptionLabel);
 
         descriptionInput = new JTextArea();
         descriptionInput.setFont(standardTextFont);
         descriptionInput.setBounds(descriptionLabel.getX() + descriptionLabel.getWidth() + 20, descriptionLabel.getY(), 250, 25 * 10 );
         descriptionInput.setBorder(new JTextField().getBorder());
         this.add(descriptionInput);
 
         priorityLabel = new JTextArea("Priorität\nder LVAs:");
         priorityLabel.setEditable(false);
         priorityLabel.setCursor(null);
         priorityLabel.setOpaque(false);
         priorityLabel.setFocusable(false);
         priorityLabel.setFont(standardTextFont);
         priorityLabel.setBounds(descriptionLabel.getX(), descriptionLabel.getY() + descriptionInput.getHeight() + verticalSpace, 110, 25 * 2);
         this.add(priorityLabel);
 
         priorityInput = new HintTextField("");
         //priority.setText("5");
         priorityInput.setFont(standardTextFont);
         priorityInput.setBounds(priorityLabel.getX() + priorityLabel.getWidth() + 20, priorityLabel.getY() + (25), 250, 25);
         this.add(priorityInput);
 
         metaLVALabel = new JLabel("LVAs in diesem Modul:");
         metaLVALabel.setFont(standardTextFont);
         metaLVALabel.setBounds((int)paneModule.getX(),(int)paneModule.getY(),200,25);
         this.add(metaLVALabel);
 
         metaLVADisplayPanel = new MetaLVADisplayPanel(module.getMetaLvas(),(int)paneModule.getWidth(),(int)paneModule.getHeight());
         metaLVADisplayPanel.setBounds(metaLVALabel.getX(),metaLVALabel.getY()+metaLVALabel.getHeight()+20,(int)paneModule.getWidth(),(int)paneModule.getHeight()-(metaLVALabel.getY()+metaLVALabel.getHeight()));
         this.add(metaLVADisplayPanel);
 
     }
 
     public void setModule(Module module) {
         priorityInput.setText("");
         if (module == null) {
 
             this.module = new Module();
             this.module.setMetaLvas(new ArrayList<MetaLVA>(0));
             changeTitle("Neues Modul");
             nameInput.setText("Neues Modul");
             descriptionInput.setText("");
             priorityInput.setHint("Keine LVA");
         } else {
             this.module = module;
             changeTitle(module.getName());
             nameInput.setText(module.getName());
 
             if(module.getMetaLvas().isEmpty()){
                 priorityInput.setHint("Keine LVAs in diesem Modul");
             }else{
                 boolean prioritiesMatch = true;
                 float foundPriority = module.getMetaLvas().get(0).getPriority();
                 for(MetaLVA mLVA : module.getMetaLvas()){
                     if(mLVA.getPriority()!=foundPriority){
                         prioritiesMatch=false;
                         break;
                     }
                 }
                 if(prioritiesMatch){
                     priorityInput.setText("" + foundPriority);
                 }else{
                     priorityInput.setHint("verschiedene Werte");
                 }
             }
             descriptionInput.setText(module.getDescription());
         }
         metaLVADisplayPanel.refresh(this.module.getMetaLvas());
     }
 
     private static class ModuleSelectItem extends SelectItem<Module> {
         ModuleSelectItem(Module item) {
             super(item);
         }
 
         @Override
         public String toString() {
             return item.getName();
         }
 
 
     }
 }
