 package at.ac.tuwien.sepm.ui.entityViews;
 
 import at.ac.tuwien.sepm.entity.LVA;
 import at.ac.tuwien.sepm.entity.LvaDate;
 import at.ac.tuwien.sepm.entity.LvaDateType;
 import at.ac.tuwien.sepm.service.LVAService;
 import at.ac.tuwien.sepm.service.LvaDateService;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.service.TimeFrame;
 import at.ac.tuwien.sepm.service.impl.ValidationException;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import at.ac.tuwien.sepm.ui.template.SelectItem;
 import at.ac.tuwien.sepm.ui.StandardSimpleInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import at.ac.tuwien.sepm.ui.template.WideComboBox;
 import com.toedter.calendar.JDateChooser;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 import java.util.List;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo
  * Date: 01.06.13
  * Time: 22:42
  * To change this template use File | Settings | File Templates.
  */
 @UI
 public class ViewLvaDate extends StandardSimpleInsidePanel {
     private LvaDate lvaDate;
 
     private JTextArea description;
     private JButton save;
 
     private JLabel fromLabel;
     private JLabel toLabel;
     private JLabel typeLabel;
     private JLabel attendanceRequiredLabel;
    // private JLabel attendedLabel;
     private WideComboBox lva;
     private List<LVA> lvas;
     private LVAService lvaService;
 
     private JCheckBox attendanceRequired;
    // private JCheckBox attended;
     private JComboBox type;
     private JDateChooser from;
     private JDateChooser to;
     private JSpinner fromTime;
     private JSpinner toTime;
     private JButton delete;
     private JLabel lvaLabel;
 
     private LvaDateService lvaDateService;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Autowired
     public ViewLvaDate(LvaDateService lvaDateService, LVAService lvaService) {
         init();
         addImage();
         this.lvaService=lvaService;
         this.lvaDateService=lvaDateService;
         lvaDate = new LvaDate();
         addEditableTitle(lvaDate.getName());
         addReturnButton();
         addContent();
         addButtons();
     }
 
     public void setLVADateEntity(LvaDate lvaDate, DateTime dateTime) {
         if (lvaDate == null) {
             this.lvaDate = new LvaDate();
             changeTitle("Neuer Lva Termin");
             description.setText("Bitte Beschreibung einfügen");
             type.setSelectedItem(false);
             attendanceRequired.setSelected(true);
             //attended.setSelected(false);
             if (dateTime != null) {
                 from.setDate(dateTime.toDate());
                 to.setDate(dateTime.toDate());
                 fromTime.setValue(dateTime.toDate());
                 toTime.setValue(dateTime.toDate());
             } else {
                 from.setDate(new Date());
                 to.setDate(new Date());
                 fromTime.setValue(new Date());
                 toTime.setValue(new Date());
             }
             setDeleteButton(false);
         } else {
             this.lvaDate=lvaDate;
             changeTitle(lvaDate.getName());
             description.setText(lvaDate.getDescription());
             type.setSelectedItem(lvaDate.getType());
             for(int i = 0; i < lva.getModel().getSize(); i++)           //todo
                 if (((LvaSelectItem) lva.getItemAt(i)).get().getId() == lvaDate.getLva()) {
                     lva.setSelectedIndex(i);
                     break;
                 }
             attendanceRequired.setSelected(lvaDate.getAttendanceRequired());
             //attended.setSelected(lvaDate.getWasAttendant());
             from.setDate(lvaDate.getStart().toDate());
             to.setDate(lvaDate.getStop().toDate());
             fromTime.setValue(lvaDate.getStart().toDate());
             toTime.setValue(lvaDate.getStop().toDate());
             setDeleteButton(true);
         }
         refresh();
     }
 
     private void setDeleteButton(boolean showDeleteButton) {
         if (showDeleteButton) {
             delete.setVisible(true);
         } else {
             delete.setVisible(false);
         }
     }
 
     private void addButtons() {
         delete = new JButton("Löschen");
         delete.setFont(standardTextFont);
         delete.setBounds((int)simpleWhiteSpace.getX()+(int)simpleWhiteSpace.getWidth()*1/3-145, (int)simpleWhiteSpace.getY() + (int)simpleWhiteSpace.getHeight()-60, 130,40);
         delete.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
                     if (lvaDateService.readById(lvaDate.getId()) != null) {
                         int i = JOptionPane.showConfirmDialog(ViewLvaDate.this, "Wollen sie diesen Termin wirklich löschen?", "", JOptionPane.YES_NO_OPTION);
                         if (i == 0) {
                             lvaDateService.delete(lvaDate.getId());
                             PanelTube.backgroundPanel.viewInfoText("Der Termin wurde gelöscht.", SmallInfoPanel.Info);
                         }
                     } else {
                         PanelTube.backgroundPanel.viewInfoText("Dieser Termin ist noch nicht in der Datenbank.", SmallInfoPanel.Error);
                     }
                     setVisible(false);
                     PanelTube.backgroundPanel.showLastComponent();
                 } catch (ServiceException e) {
                     log.error("LvaDateEntity is invalid.");
                     PanelTube.backgroundPanel.viewInfoText("Die Angaben sind ungültig.", SmallInfoPanel.Error);
                 } catch (ValidationException e) {
                     log.error("LvaDateEntity is invalid.");
                     PanelTube.backgroundPanel.viewInfoText("Die Angaben sind ungültig.", SmallInfoPanel.Error);
                 }
             }
         });
         this.add(delete);
 
         save = new JButton("Speichern");
         save.setFont(standardTextFont);
         save.setBounds((int)simpleWhiteSpace.getX()+(int)simpleWhiteSpace.getWidth()*1/3-170-120, (int)simpleWhiteSpace.getY() + (int)simpleWhiteSpace.getHeight()-60, 130,40);
         save.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
                     lvaDate.setName(title.getText());
                     lvaDate.setDescription(description.getText());
                     //lvaDate.setWasAttendant(attended.isSelected());
                     lvaDate.setType((LvaDateType) type.getSelectedItem());
                     lvaDate.setLva(((LvaSelectItem) lva.getSelectedItem()).get().getId());
                     lvaDate.setAttendanceRequired(attendanceRequired.isSelected());
                     if (convertDateAndTime(fromTime, from).isAfter(convertDateAndTime(toTime, to))) {
                         PanelTube.backgroundPanel.viewInfoText("Das Start-Datum muss vor dem End-Datum liegen.", SmallInfoPanel.Info);
                         return;
                     }
                     lvaDate.setTime(new TimeFrame(convertDateAndTime(fromTime, from), convertDateAndTime(toTime, to)));
                     if (lvaDate.getId() != null) {
                         if (lvaDateService.readById(lvaDate.getId()) != null)
                             lvaDateService.update(lvaDate);
                     } else {
                         lvaDateService.create(lvaDate);
                     }
                     PanelTube.backgroundPanel.viewInfoText("Der Termin wurde gespeichert.", SmallInfoPanel.Info);
                     setVisible(false);
                     PanelTube.backgroundPanel.showLastComponent();
                 } catch (ServiceException e) {
                     log.error("LvaDateEntity is invalid.");
                     PanelTube.backgroundPanel.viewInfoText("Die Angaben sind ungültig.", SmallInfoPanel.Error);
                 } catch (ValidationException e) {
                     log.error("LvaDateEntity is invalid.");
                     PanelTube.backgroundPanel.viewInfoText("Die Angaben sind ungültig.", SmallInfoPanel.Error);
                 }
             }
         });
         this.add(save);
     }
 
     private void addContent() {
         int verticalSpace = 10;
 
         description = new JTextArea(lvaDate.getDescription());
         description.setLineWrap(true);
         description.setWrapStyleWord(true);
         description.setFont(standardTextFont);
         description.setBounds((int)simpleWhiteSpace.getX()+(int)simpleWhiteSpace.getWidth()*1/3, (int)simpleWhiteSpace.getY()+20,(int)simpleWhiteSpace.getWidth()*2/3 - 20,(int)simpleWhiteSpace.getHeight()-40);
         JScrollPane scroll = new JScrollPane(description);
         scroll.setBounds((int)simpleWhiteSpace.getX()+(int)simpleWhiteSpace.getWidth()*1/3, (int)simpleWhiteSpace.getY()+20,(int)simpleWhiteSpace.getWidth()*2/3 - 20,(int)simpleWhiteSpace.getHeight()-40);
         this.add(scroll);
 
 
 
         fromLabel = new JLabel("Von");
         fromLabel.setFont(standardTextFont);
         fromLabel.setBounds((int)simpleWhiteSpace.getX() + 20,(int)simpleWhiteSpace.getY() + 10,50,25);
         this.add(fromLabel);
 
         from = new JDateChooser();
         from.setFont(standardButtonFont);
         from.setBounds(fromLabel.getX(), fromLabel.getY() + fromLabel.getHeight() + verticalSpace, 100,25);
         this.add(from);
 
         fromTime = new JSpinner(new SpinnerDateModel());
         JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(fromTime, "HH:mm");
         fromTime.setEditor(timeEditor);
         fromTime.setFont(standardButtonFont);
         fromTime.setBounds(from.getX() + from.getWidth() + 5, from.getY(), 65,25);
         this.add(fromTime);
 
 
         toLabel = new JLabel("Bis");
         toLabel.setFont(standardTextFont);
         toLabel.setBounds(from.getX(), from.getY() + from.getHeight() + verticalSpace*2, 50,25);
         this.add(toLabel);
 
         to = new JDateChooser();
         to.setFont(standardButtonFont);
         to.setBounds(toLabel.getX(), toLabel.getY() + toLabel.getHeight() + verticalSpace, 100,25);
         this.add(to);
 
         toTime = new JSpinner(new SpinnerDateModel());
         timeEditor = new JSpinner.DateEditor(toTime, "HH:mm");
         toTime.setEditor(timeEditor);
         toTime.setFont(standardButtonFont);
         toTime.setBounds(to.getX() + to.getWidth() + 5, to.getY(), 65,25);
         this.add(toTime);
 
 
 
         typeLabel = new JLabel("Typ");
         typeLabel.setFont(standardTextFont);
         typeLabel.setBounds(to.getX(), to.getY() + to.getHeight() + verticalSpace*2, 50,25);
         this.add(typeLabel);
 
         type = new JComboBox();
         for (LvaDateType t : LvaDateType.values()) {
             type.addItem(t);
         }
         type.setFont(standardTextFont);
        type.setBounds(typeLabel.getX() + typeLabel.getWidth() + 20, typeLabel.getY(), 100,25);
         this.add(type);
 
 
         lvaLabel = new JLabel("Lva");
         lvaLabel.setFont(standardTextFont);
         lvaLabel.setBounds(typeLabel.getX(), typeLabel.getY() + typeLabel.getHeight() + verticalSpace*2, 60,25);
         this.add(lvaLabel);
 
         lva = new WideComboBox();
         try {
             int year = DateTime.now().getYear();
             boolean isWinterSemester = (DateTime.now().getMonthOfYear() > 7 || DateTime.now().getMonthOfYear() < 2);
             lvas = lvaService.readByYearAndSemester(year, isWinterSemester);
         } catch(ServiceException e) {
             log.error(e.getMessage());
         } catch(ValidationException e) {
             log.error(e.getMessage());
         }
 
         for (LVA t : lvas) {
             lva.addItem(new LvaSelectItem(t));
         }
         lva.setFont(standardTextFont);
         lva.setBounds(lvaLabel.getX() + lvaLabel.getWidth() + 10, lvaLabel.getY(), 200,25);
         this.add(lva);
 
 
         attendanceRequiredLabel = new JLabel("Anwesenheitspflicht");
         attendanceRequiredLabel.setFont(standardTextFont);
         attendanceRequiredLabel.setBounds(lvaLabel.getX(), lvaLabel.getY() + lvaLabel.getHeight() + verticalSpace*2, 180,25);
         this.add(attendanceRequiredLabel);
 
         attendanceRequired = new JCheckBox();
         attendanceRequired.setSelected(lvaDate.getAttendanceRequired() != null ? lvaDate.getAttendanceRequired() : false);
         attendanceRequired.setBackground(new Color(0,0,0,0));
         attendanceRequired.setBounds(attendanceRequiredLabel.getX() + attendanceRequiredLabel.getWidth() + 5, attendanceRequiredLabel.getY(), 20, 20);
         this.add(attendanceRequired);
 
 
         /*attendedLabel = new JLabel("War anwesend");
         attendedLabel.setFont(standardTextFont);
         attendedLabel.setBounds(attendanceRequiredLabel.getX(), attendanceRequiredLabel.getY() + attendanceRequiredLabel.getHeight() + verticalSpace*2, 180,25);
         this.add(attendedLabel);
 
         attended  = new JCheckBox();
         attended.setSelected(lvaDate.getWasAttendant() != null ? lvaDate.getWasAttendant() : false);
         attended.setBackground(new Color(0,0,0,0));
         attended.setBounds(attendedLabel.getX() + attendedLabel.getWidth() + 5, attendedLabel.getY(), 20, 20);
         this.add(attended);
 
         roomLabel = new JLabel("Raum");
         roomLabel.setFont(standardTextFont);
         roomLabel.setBounds(attendedLabel.getX(), attendedLabel.getY() + attendedLabel.getHeight() + verticalSpace*2, 100,25);
         this.add(roomLabel);  */
 
         this.revalidate();
         this.repaint();
     }
 
     private static class LvaSelectItem extends SelectItem<LVA> {
         LvaSelectItem(LVA item) {
             super(item);
         }
 
         @Override
         public String toString() {
             return item.getMetaLVA().getName();
         }
     }
 
     @Override
     public void refresh() {
         try {
             int year = DateTime.now().getYear();
             boolean isWinterSemester = (DateTime.now().getMonthOfYear() > 7|| DateTime.now().getMonthOfYear() < 2);
             lvas = lvaService.readByYearAndSemester(year, isWinterSemester);
         } catch(ServiceException e) {
             log.error(e.getMessage());
         } catch(ValidationException e) {
             log.error(e.getMessage());
         }
         lva.removeAllItems();
         for (LVA t : lvas) {
             lva.addItem(new LvaSelectItem(t));
         }
     }
 }
