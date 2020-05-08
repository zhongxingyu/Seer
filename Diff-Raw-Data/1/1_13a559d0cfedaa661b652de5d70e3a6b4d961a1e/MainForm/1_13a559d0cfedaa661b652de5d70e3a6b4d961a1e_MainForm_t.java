 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Vector;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Alla
  * Date: 04.01.11
  * Time: 23:11
  * To change this template use File | Settings | File Templates.
  */
 public class MainForm {
     private JTabbedPane tabbedPane1;
     private JPanel mainPanel;
     private JTable workersTable;
     private JButton saveWorkersBtn;
     private JButton addWorkerBtn;
     private JButton delWorkerBtn;
     private JTabbedPane quarterPlan_divisions;
     private JTabbedPane kindWorksTabbedPane;
     private JButton savePlanBtn;
     private JButton loadPlanBtn;
     private JButton printPlanBtn;
     private JButton отчетButton;
     private JTable planPartTable;
     private JTable workersInPlanTable;
     private JButton addWorkBtn;
     private JButton delWorkBtn;
     private JButton addWorkerToWorkBtn;
     private JButton delWorkerFromWorkBtn;
     private JButton moveUpPlanPartBtn;
     private JButton moveDownPlanPartBtn;
     private JTabbedPane monthTabbedPane;
     private JComboBox quarterComboBox;
     private JButton monthReportBtn;
     private JTable monthWorksTable;
     private JTable monthWorkersPerWorkTable;
     private JTabbedPane quarterPlan_month;
     private JFormattedTextField yearEdit;
     private JButton editWorkBtn;
     private JButton createNewPlanBtn;
 
     public int getSelectedQuarter() {
         return quarterComboBox.getSelectedIndex() + 1;
     }
 
     public void setSelectedQuarter(int quarter) {
         if ((quarter > -1) && (quarter < 4)) {
             quarterComboBox.setSelectedIndex(quarter);
         }
     }
 
     public String getYear() {
         return yearEdit.getText();
     }
 
     public void setYear(String year) {
         yearEdit.setText(year);
     }
 
     public Vector<Worker> getWorkers() {
         return workers;
     }
 
     //
     private Vector<Worker> workers = new Vector<Worker>();
 
     public ArrayList<PlanPart> getPlan() {
         return plan;
     }
 
     private ArrayList<PlanPart> plan = new ArrayList<PlanPart>();
     //
 
     public MainForm() {
         //
         Worker worker1 = new Worker("Рогов П.А.", 0.0);
         workers.add(worker1);
         Worker worker2 = new Worker("Золотов А.В.", 0.0);
         workers.add(worker2);
         workers.add(new Worker("Полулях А.В.", 0.0));
         workers.add(new Worker("Горелов А.Г.", 0.0));
         workers.add(new Worker("Воронин Р.М.", 0.0));
         workers.add(new Worker("Гуськов С.С.", 0.0));
         workers.add(new Worker("Конев Д.С.", 0.0));
         workers.add(new Worker("Шумский Ю.Н.", 0.0));
         workers.add(new Worker("Мироненко Н.Л.", 0.0));
         workers.add(new Worker("Мармер В.В.", -1.0));
         workers.add(new Worker("Зореев В.П.", -1.0));
         workers.add(new Worker("Шварцман А.М.", 0.0));
         workers.add(new Worker("Никитина Н.Е.", 0.0));
         workers.add(new Worker("Хехнева А.В.", 0.0));
         workers.add(new Worker("Конашина О.А.", 0.0));
         workers.add(new Worker("Косарев В.В.", 0.0));
         //
         plan.add(new PlanPart("Собств.работы - новые", "1. Собственные работы (по основному процессу подразделения)"));
         plan.add(new PlanPart("Собств.работы - продолжение", "1а). Собственные работы (завершение работ по предыдущим квартальным планам)"));
         plan.add(new PlanPart("Внешн.заказчик", "3. Работы по внешней кооперации (с другими организациями)"));
         plan.add(new PlanPart("СМК", "4. Разработка документации по СМК"));
         plan.add(new PlanPart("Корр.мероприятия", "5. Корректирующие и предупреждающие действия"));
         //
         plan.get(0).getWorks().add(new WorkInPlan("Работа 1", "Описание 1\nСтрока2\n]]", "10.10.2010", "", "Предоставлено \n куча \n всего \n ]]"));
         plan.get(0).getWorks().get(0).getWorkersInPlan().add(new WorkerInPlan(worker1, 3.5));
         plan.get(0).getWorks().get(0).getWorkersInPlan().add(new WorkerInPlan(worker2, 5.5));
         plan.get(0).getWorks().add(new WorkInPlan("Работа 2", "Описание 2"));
         plan.get(0).getWorks().add(new WorkInPlan("Работа 3", "Описание 3"));
         //
         yearEdit.setText(new SimpleDateFormat("yyyy").format(new Date()));
         //
         makePlanPartTabs();
         quarterPlan_divisions.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 if (quarterPlan_divisions.getSelectedIndex() != -1) {
                     PlanPart currPart = plan.get(quarterPlan_divisions.getSelectedIndex());
                     ((PlanPartModel) planPartTable.getModel()).setPlanPart(currPart);
                     // Сбрасываем еще список работников
                     ((WorkersInPlanTableModel) workersInPlanTable.getModel()).clearWorkInPlan();
                 }
             }
         });
         //
         workersTable.setModel(new WorkersTableModel(workers));
         workersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         //
         PlanPartModel planPartModel = new PlanPartModel();
         planPartTable.setModel(planPartModel);
         planPartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         planPartModel.setPlanPart(plan.get(0));
         planPartTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 PlanPart currPart = plan.get(quarterPlan_divisions.getSelectedIndex());
                 if ((planPartTable.getSelectedRow() > -1) && (planPartTable.getSelectedRow() < currPart.getWorks().size())) {
                     ((WorkersInPlanTableModel) workersInPlanTable.getModel()).setWorkInPlan(currPart.getWorks().get(planPartTable.getSelectedRow()));
                 }
             }
         });
         //
         workersInPlanTable.setModel(new WorkersInPlanTableModel());
         workersInPlanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         TableColumn workerColumn = workersInPlanTable.getColumnModel().getColumn(0);
         JComboBox workerSelectCB = new JComboBox();
         workerSelectCB.setModel(new DefaultComboBoxModel(workers));
         workerColumn.setCellEditor(new DefaultCellEditor(workerSelectCB));
         //
         addWorkBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 PlanPart currPart = plan.get(quarterPlan_divisions.getSelectedIndex());
                 currPart.getWorks().add(new WorkInPlan("", ""));
                 ((PlanPartModel) planPartTable.getModel()).fireTableDataChanged();
                 ((WorkersInPlanTableModel) workersInPlanTable.getModel()).clearWorkInPlan();
             }
         });
         delWorkBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (planPartTable.getSelectedRow() != -1) {
                     if (JOptionPane.showConfirmDialog(null, "Вы уверены что хотите удалить выбранную работу?", "Удаление", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                         PlanPart currPart = plan.get(quarterPlan_divisions.getSelectedIndex());
                         currPart.getWorks().remove(planPartTable.getSelectedRow());
                         ((PlanPartModel) planPartTable.getModel()).fireTableDataChanged();
                         ((WorkersInPlanTableModel) workersInPlanTable.getModel()).clearWorkInPlan();
                     }
                 }
             }
         });
         addWorkerBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 workers.add(new Worker("", 0.0));
                 ((WorkersTableModel) workersTable.getModel()).fireTableDataChanged();
             }
         });
         delWorkerBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if ((workersTable.getSelectedRow() != -1) && (workersTable.getSelectedRow() < workers.size())) {
                     // Тут вначале проверим - а может у нас этот работник фигурирует в какой работе?!
                     Worker worker = workers.get(workersTable.getSelectedRow());
                     boolean presentInWork = false;
                     for (PlanPart planPart : plan) {
                         for (WorkInPlan work : planPart.getWorks()) {
                             for (WorkerInPlan workerInPlan : work.getWorkersInPlan()) {
                                 if (workerInPlan.getWorker() == worker) {
                                     presentInWork = true;
                                     break;
                                 }
                             }
                         }
                     }
                     if (presentInWork) {
                         JOptionPane.showMessageDialog(null, "Работник занят в какой-то работе. Вначале удалите его из всех работ",
                                 "Удаление работника", JOptionPane.ERROR_MESSAGE);
                     } else {
                         if (JOptionPane.showConfirmDialog(null, "Вы уверены, что хотите удалить выбранного работника?",
                                 "Удаление работника", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                             workers.remove(worker);
                             ((WorkersTableModel) workersTable.getModel()).fireTableDataChanged();
                         }
                     }
                 }
             }
         });
         //
         addWorkerToWorkBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 PlanPart currPart = plan.get(quarterPlan_divisions.getSelectedIndex());
                 if ((planPartTable.getSelectedRow() > -1) && (planPartTable.getSelectedRow() < currPart.getWorks().size())) {
                     WorkInPlan work = currPart.getWorks().get(planPartTable.getSelectedRow());
                     // Теперь надо из списка работников добавить ПЕРВОГО, кого тут нет
                     for (Worker worker : workers) {
                         boolean found = false;
                         for (WorkerInPlan workerInPlan : work.getWorkersInPlan()) {
                             if (workerInPlan.getWorker() == worker) {
                                 found = true;
                                 break;
                             }
                         }
                         if (!found) {
                             work.getWorkersInPlan().add(new WorkerInPlan(worker, 0.0));
                             ((WorkersInPlanTableModel) workersInPlanTable.getModel()).fireTableDataChanged();
                             break;
                         }
                     }
                 }
             }
         });
         delWorkerFromWorkBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 PlanPart currPart = plan.get(quarterPlan_divisions.getSelectedIndex());
                 if ((planPartTable.getSelectedRow() > -1) && (planPartTable.getSelectedRow() < currPart.getWorks().size())) {
                     WorkInPlan work = currPart.getWorks().get(planPartTable.getSelectedRow());
                     if ((workersInPlanTable.getSelectedRow() > -1) && (workersInPlanTable.getSelectedRow() < work.getWorkersInPlan().size())) {
                         if (JOptionPane.showConfirmDialog(null, "Вы уверены, что хотите удалить выбранного работника из работы?",
                                 "Удаление работника из работы", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                             WorkerInPlan worker = work.getWorkersInPlan().get(workersInPlanTable.getSelectedRow());
                             work.getWorkersInPlan().remove(worker);
                             ((WorkersInPlanTableModel) workersInPlanTable.getModel()).fireTableDataChanged();
                         }
                     }
                 }
             }
         });
         moveUpPlanPartBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 movePlanPart(-1);
             }
         });
         moveDownPlanPartBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 movePlanPart(1);
             }
         });
         //
         quarterComboBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 switch (quarterComboBox.getSelectedIndex()) {
                     case 0: {
                         monthTabbedPane.setTitleAt(0, "Январь");
                         monthTabbedPane.setTitleAt(1, "Февраль");
                         monthTabbedPane.setTitleAt(2, "Март");
                         break;
                     }
                     case 1: {
                         monthTabbedPane.setTitleAt(0, "Апрель");
                         monthTabbedPane.setTitleAt(1, "Май");
                         monthTabbedPane.setTitleAt(2, "Июнь");
                         break;
                     }
                     case 2: {
                         monthTabbedPane.setTitleAt(0, "Июль");
                         monthTabbedPane.setTitleAt(1, "Август");
                         monthTabbedPane.setTitleAt(2, "Сентябрь");
                         break;
                     }
                     case 3: {
                         monthTabbedPane.setTitleAt(0, "Октябрь");
                         monthTabbedPane.setTitleAt(1, "Ноябрь");
                         monthTabbedPane.setTitleAt(2, "Декабрь");
                         break;
                     }
                 }
             }
         });
         quarterComboBox.setSelectedIndex(0);
         //
         monthTabbedPane.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 if ((monthTabbedPane.getSelectedIndex() > -1) && (monthTabbedPane.getSelectedIndex() < 4)) {
                     ((MonthWorksTableModel) monthWorksTable.getModel()).setMonth(monthTabbedPane.getSelectedIndex());
                     int i = monthWorksTable.getSelectedRow();
                     ((MonthWorksTableModel) monthWorksTable.getModel()).fireTableDataChanged();
                     if (i > -1) monthWorksTable.getSelectionModel().setSelectionInterval(i, i);
                     ((MonthWorkersTableModel) monthWorkersPerWorkTable.getModel()).setMonth(monthTabbedPane.getSelectedIndex());
                 }
             }
         });
         monthTabbedPane.setSelectedIndex(0);
         //
         quarterPlan_month.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 if (quarterPlan_month.getSelectedIndex() != -1) {
                     PlanPart currPart = plan.get(quarterPlan_month.getSelectedIndex());
                     ((MonthWorksTableModel) monthWorksTable.getModel()).setPlanPart(currPart);
                     // Сбрасываем еще список работников
                     ((MonthWorkersTableModel) monthWorkersPerWorkTable.getModel()).clearWorkInPlan();
                 }
             }
         });
         //
         monthWorkersPerWorkTable.setModel(new MonthWorkersTableModel());
         monthWorkersPerWorkTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         //
         MonthWorksTableModel monthWorksTableModel = new MonthWorksTableModel();
         monthWorksTable.setModel(monthWorksTableModel);
         monthWorksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         monthWorksTableModel.setPlanPart(plan.get(0));
         monthWorksTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 PlanPart currPart = plan.get(quarterPlan_month.getSelectedIndex());
                 if ((monthWorksTable.getSelectedRow() > -1) && (monthWorksTable.getSelectedRow() < currPart.getWorks().size())) {
                     ((MonthWorkersTableModel) monthWorkersPerWorkTable.getModel()).setWorkInPlan(currPart.getWorks().get(monthWorksTable.getSelectedRow()));
                 }
             }
         });
         kindWorksTabbedPane.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 // Запустим обновление всего чего можно тама...
                 ((MonthWorksTableModel) monthWorksTable.getModel()).fireTableDataChanged();
                 ((MonthWorkersTableModel) monthWorkersPerWorkTable.getModel()).clearWorkInPlan();
             }
         });
         //
         editWorkBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 PlanPart currPart = plan.get(quarterPlan_divisions.getSelectedIndex());
                 if ((planPartTable.getSelectedRow() > -1) && (planPartTable.getSelectedRow() < currPart.getWorks().size())) {
                     WorkInPlan currWork = currPart.getWorks().get(planPartTable.getSelectedRow());
                     WorkInPlan resWork = WorkParamForm.showDialog(planPartTable, planPartTable, currWork);
                     if (resWork != null) {
                         currWork.setName(resWork.getName());
                         currWork.setDesc(resWork.getDesc());
                         currWork.setEndDate(resWork.getEndDate());
                         currWork.setReserve(resWork.getReserve());
                         currWork.setFinishDoc(resWork.getFinishDoc());
                         // Fire change
                         ((PlanPartModel) planPartTable.getModel()).fireTableCellUpdated(planPartTable.getSelectedRow(), 0);
                     }
                 }
             }
         });
         //
         savePlanBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 // Вначале - выбираем куда сохранять
                 PlanUtils.savePlanToFile();
             }
         });
         loadPlanBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (PlanUtils.loadPlanFromFile()) {
                     // Пересчитаем общую трудоемкость по всем
                     for (Worker worker : workers) {
                         updateTotalWorkerLabor(worker);
                     }
                     // Теперь еще перезапустим все модели
                     makePlanPartTabs();
                     ((AbstractTableModel) workersTable.getModel()).fireTableDataChanged();
                     ((AbstractTableModel) planPartTable.getModel()).fireTableDataChanged();
                     ((AbstractTableModel) workersInPlanTable.getModel()).fireTableDataChanged();
                     ((AbstractTableModel) monthWorksTable.getModel()).fireTableDataChanged();
                     ((AbstractTableModel) monthWorkersPerWorkTable.getModel()).fireTableDataChanged();
                 }
             }
         });
         //
         createNewPlanBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (JOptionPane.showConfirmDialog(null, "Вы уверены, что хотите создать новый план? Все работы текущего плана будут удалены!",
                         "Создание нового плана", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                     for (PlanPart part : plan) {
                         part.getWorks().clear();
                     }
                     ((PlanPartModel) planPartTable.getModel()).fireTableDataChanged();
                     ((WorkersInPlanTableModel) workersInPlanTable.getModel()).fireTableDataChanged();
                     for (Worker worker : workers) {
                         worker.setLaborContentTotal(0.0);
                     }
                     ((WorkersTableModel) workersTable.getModel()).fireTableDataChanged();
                 }
             }
         });
         printPlanBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 try {
                     PlanUtils.makeQuarterPlan();
                    ReportViewer.showPreview(kindWorksTabbedPane, kindWorksTabbedPane, "quarterPlan");
                 } catch (IOException e1) {
                     JOptionPane.showMessageDialog(null, "Ошибка ввода-вывода");
                 } catch (TransformerException e1) {
                     JOptionPane.showMessageDialog(null, "Ошибка сохранения XML документа");
                 } catch (ParserConfigurationException e1) {
                     JOptionPane.showMessageDialog(null, "Ошибка работы с XML документом");
                 }
             }
         });
     }
 
     /**
      * Создает табы по видам работ
      */
     private void makePlanPartTabs() {
         quarterPlan_divisions.removeAll();
         quarterPlan_month.removeAll();
         for (PlanPart part : plan) {
             quarterPlan_divisions.addTab(part.getName(), null);
             quarterPlan_month.addTab(part.getName(), null);
         }
     }
 
 
     /**
      * Двигает выбранный элемент в planPartTable вверх или вниз
      *
      * @param moveDiff -1 - сдвинуть вверх, 1 - сдвинуть вниз.
      *                 При других значениях или при невозможности двигать - ничего не делает
      */
     private void movePlanPart(int moveDiff) {
         PlanPart currPart = plan.get(quarterPlan_divisions.getSelectedIndex());
         if (((moveDiff == -1) && ((planPartTable.getSelectedRow() > 0) && (planPartTable.getSelectedRow() < currPart.getWorks().size()))) ||
                 ((moveDiff == 1) && ((planPartTable.getSelectedRow() > -1) && (planPartTable.getSelectedRow() < (currPart.getWorks().size() - 1))))) {
             // Значит тут можно попробовать подвигать...
             int selPos = planPartTable.getSelectedRow();
             WorkInPlan work = currPart.getWorks().get(selPos);
             currPart.getWorks().remove(work);
             currPart.getWorks().add(selPos + moveDiff, work);
             ((PlanPartModel) planPartTable.getModel()).fireTableDataChanged();
             planPartTable.getSelectionModel().setSelectionInterval(selPos + moveDiff, selPos + moveDiff);
         }
     }
 
     public JPanel getMainPanel() {
         return mainPanel;
     }
 
     public void planPartTotalLaborChanged() {
         if (planPartTable.getSelectedRow() != -1) {
             ((PlanPartModel) planPartTable.getModel()).fireTableCellUpdated(planPartTable.getSelectedRow(), 1);
         } else {
             ((PlanPartModel) planPartTable.getModel()).fireTableDataChanged();
         }
     }
 
     public void updateTotalWorkerLabor(Worker worker) {
         double total = 0;
         for (PlanPart planPart : plan) {
             for (WorkInPlan work : planPart.getWorks()) {
                 for (WorkerInPlan workerInPlan : work.getWorkersInPlan()) {
                     if (workerInPlan.getWorker() == worker) {
                         total = total + workerInPlan.getLaborContent();
                     }
                 }
             }
         }
         worker.setLaborContentTotal(total);
         ((WorkersTableModel) workersTable.getModel()).fireTableCellUpdated(workers.indexOf(worker), 1);
     }
 
     /**
      * Обновляет информацию о оставшейся трудоемкости в monthWorksTable
      */
     public void updateRestLabor() {
         if (monthWorksTable.getSelectedRow() != -1) {
             ((MonthWorksTableModel) monthWorksTable.getModel()).fireTableCellUpdated(monthWorksTable.getSelectedRow(), 2);
         } else {
             ((MonthWorksTableModel) monthWorksTable.getModel()).fireTableDataChanged();
             ((MonthWorkersTableModel) monthWorkersPerWorkTable.getModel()).clearWorkInPlan();
         }
     }
 
 }
