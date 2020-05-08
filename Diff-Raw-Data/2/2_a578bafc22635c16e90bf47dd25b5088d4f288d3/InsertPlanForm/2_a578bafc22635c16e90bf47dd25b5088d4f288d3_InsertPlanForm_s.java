 package edu.thu.cslab.footwith.form;
 
 import edu.thu.cslab.footwith.server.Mediator;
 import edu.thu.cslab.footwith.server.TextFormatException;
 
 import org.json.JSONException;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Vector;
 import java.util.logging.SimpleFormatter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: wangjiayu
  * Date: 13-3-16
  * Time: 下午4:03
  * To change this template use File | Settings | File Templates.
  */
 public class InsertPlanForm extends JFrame {
 
     private  JTextField siteName = new JTextField();
     private  JTextField startTime = new JTextField();
     private  JTextField endTime = new JTextField();
     private  JTextField organizer = new JTextField();
     private  JTextField numbers = new JTextField();
 
     public  InsertPlanForm() {
         this.setBounds(200,200,300,200);
         init();
         this.setTitle("订制行程");
         this.setVisible(true);
     }
 
     private void init() {
 
         final  BorderLayout borderLayout = new BorderLayout();
         borderLayout.setVgap(5);
 
         getContentPane().setLayout(borderLayout);
 
 
         JPanel mainPanel = new JPanel();
         mainPanel.setBorder(new EmptyBorder(5,10,5,10)); // need to understand
         final  GridLayout gridLayout = new GridLayout(4,4);
         gridLayout.setVgap(5);
         gridLayout.setHgap(5);
         mainPanel.setLayout(gridLayout);
         getContentPane().add(mainPanel,"North");
 
         JLabel organizerLabel = new JLabel("组织者");
         mainPanel.add(organizerLabel);
         final JTextField organizer = new JTextField();
         mainPanel.add(organizer);
 
         JLabel groupNumMaxLabel = new JLabel("人数");
         mainPanel.add(groupNumMaxLabel);
         final JTextField groupNumMax = new JTextField();
         mainPanel.add(groupNumMax);
 
         JLabel siteIdLb1 = new JLabel(("具体景点信息"));
         mainPanel.add(siteIdLb1);
         final  JTextField siteName1 = new JTextField();
         mainPanel.add(siteName1);
 
         JLabel siteIdLb2 = new JLabel("景点2");
         final  JTextField siteName2 = new JTextField();
         mainPanel.add(siteIdLb2);
         mainPanel.add(siteName2);
 
         JLabel siteIdLb3 = new JLabel("景点3");
         final  JTextField siteName3 = new JTextField();
         mainPanel.add(siteIdLb3);
         mainPanel.add(siteName3);
 
 
         JLabel siteIdOther = new JLabel("其它景点");
         final  JTextField siteNameText = new JTextField();
         siteNameText.setText("请以，间隔");
         siteNameText.setEnabled(false);
 
         mainPanel.add(siteIdOther);
         mainPanel.add(siteNameText);
 
         JLabel startTimeLb = new JLabel("开始时间");
         SimpleDateFormat myfmt = new SimpleDateFormat("yyyy-MM-dd");
         final JFormattedTextField startTime  = new JFormattedTextField(myfmt);
         startTime.setValue(new java.util.Date());
         mainPanel.add(startTimeLb);
         mainPanel.add(startTime);
 
         JLabel endTimeLb = new JLabel("结束时间");
         final JFormattedTextField endTime = new JFormattedTextField(myfmt);
         endTime.setValue(new Date());
         mainPanel.add(endTimeLb);
         mainPanel.add(endTime);
 
 
         final  JPanel bottomPanel = new JPanel();
         bottomPanel.setBorder(new LineBorder(SystemColor.activeCaptionBorder,1,false));
         getContentPane().add(bottomPanel,"South") ;
         final  FlowLayout flowLayout = new FlowLayout();
         flowLayout.setVgap(2);
         flowLayout.setHgap(30);
         flowLayout.setAlignment(FlowLayout.RIGHT);
         bottomPanel.setLayout(flowLayout);
         final JButton btnAdd = new JButton("增加");
         final JButton btnReset = new JButton("重置");
         bottomPanel.add(btnAdd);
         bottomPanel.add(btnReset);
 
         //       MyActionActionLister myBtnAddActionLister = new MyActionActionLister();
 
         btnAdd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
               SimpleDateFormat smf = new SimpleDateFormat("yyyy-MM-dd");
               String  siteNames = siteName.getText() + ","+ siteName1.getText()+ ","+siteName2.getText()+ ","+siteName3.getText();
                 try {
                     if( smf.parse(startTime.getValue().toString()).before(smf.parse(endTime.getValue().toString()))) {
                          JOptionPane.showMessageDialog(null,"时间不对");
                      }   else if(siteNames.length() == 0 || organizer.getText().length() ==0 || groupNumMax.getText().length() == 0)
                      {
                          JOptionPane.showMessageDialog(null,"信息不全");
                      } else{
                          int response =  JOptionPane.showConfirmDialog(null,"确定提交","are you sure",JOptionPane.YES_NO_OPTION);
                          if(response == JOptionPane.YES_OPTION){
                              Mediator pm=new Mediator();
                              try{
                                 pm.addPlanFromForm(siteName.getText(), startTime.getText(),endTime.getText(),organizer.getText());
                              }catch (TextFormatException e1) {
 
                                  e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                              } catch (SQLException e1) {
 
 
                                  e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                              } catch (JSONException e1) {
 
                                  e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                              }
                          }
                       }
                   } catch (ParseException e2) {
                     e2.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
             });
         btnReset.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                     siteName.setText("");
                    // startTime.setText("");
                    // endTime.setText("");
                     organizer.setText("");
                     groupNumMax.setText("");
 
             }
         });
 
 
         
     }
 
 
     class MyActionActionLister implements  ActionListener{
         @Override
         public void actionPerformed(ActionEvent e) {
 
         }
 
     }
 }
