 package MaratonPackage;
 
 /**
  *
  * @author krille
  */
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 class Gui extends JFrame implements ActionListener {
 
     JLabel häls = new JLabel("DSV Kista Marathon");
     JLabel namn = new JLabel("DSV Kista Marathon");
     JList list;
     JTextField fält = new JTextField(8);
     JRadioButton val1 = new JRadioButton("Testa text");
     JRadioButton val2 = new JRadioButton("Testa text2");
     JRadioButton val3 = new JRadioButton("Testa text3");
     JButton timeBut = new JButton("Tid");
     JButton show = new JButton("Visa");
     JButton add = new JButton("Lägg till");
     JRadioButton sortStartNr = new JRadioButton("Startnr");
     JRadioButton sortName = new JRadioButton("Namn");
     JRadioButton sortAge = new JRadioButton("Ålder");
     JRadioButton sortTime = new JRadioButton("Tid");
     JPanel panel;
     JScrollPane pane;
     Marathon m;
     FillOut f;
     FillOutTime f2;
     private static int startNr = 1;
 
     Gui(Marathon m) {
         super("Hej!");
         this.m = m;
         initUI();
     }
 
     private void initUI() {
         UIManager.put("OptionPane.noButtonText", "Avbryt");
         UIManager.put("OptionPane.yesButtonText", "Spara");
 
         f2 = new FillOutTime();
 
         panel = new JPanel(new BorderLayout());
 
         JPanel top = new JPanel();
         JPanel bottom = new JPanel();
         JPanel east = new JPanel(new GridLayout(0, 1));
         JPanel center = new JPanel();
         JRadioButton radioBut = new JRadioButton("Testa text");
         panel.add(top, BorderLayout.NORTH);
         panel.add(center, BorderLayout.CENTER);
         panel.add(bottom, BorderLayout.SOUTH);
         panel.add(east, BorderLayout.EAST);
 
         panel.setBorder(new EmptyBorder(new Insets(20, 20, 20, 20)));
         //  panel.setLayout(null);
         // String[] ar = {"one", "two", "three"};
         list = new JList();         //Gör listan och event för markerade saker.
        /*
          list.addListSelectionListener(new ListSelectionListener() {
          @Override
          public void valueChanged(ListSelectionEvent e) {
          if (!e.getValueIsAdjusting()) {
          //do stuff
          }
          }
          });
          */
 
 
         sortStartNr.addActionListener(this);
         sortTime.addActionListener(this);
         sortAge.addActionListener(this);
         sortName.addActionListener(this);
         sortStartNr.setSelected(true);
 
         ButtonGroup group = new ButtonGroup();
         group.add(sortStartNr);
         group.add(sortName);
         group.add(sortAge);
         group.add(sortTime);
 
 
         east.add(sortStartNr);
         east.add(sortName);
         east.add(sortAge);
         east.add(sortTime);
 
         pane = new JScrollPane();
 
         pane.setPreferredSize(new Dimension(250, 300));
 
         center.add(pane);
         String[] listaa = {"hej", "svej"};
         pane.getViewport().add(list);
 
         getContentPane().add(panel);
         //exBut.setBounds(50,60,80,30);
 
         timeBut.addActionListener(this);
         top.add(namn);
 
 
         show.addActionListener(this);
         add.addActionListener(this);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         bottom.add(add);
         bottom.add(show);
         bottom.add(timeBut);
 
 
         setTitle("Fina programemt");
         setSize(400, 500);
         setLocationRelativeTo(null);
 
         //setVisible(true);
     }
 
     public void actionPerformed(ActionEvent e) {
 
         if (e.getSource() == timeBut) {
             System.out.println("Hm");

             int i = JOptionPane.showConfirmDialog(panel, f2, "popup", JOptionPane.YES_OPTION);
             if (i == JOptionPane.YES_OPTION) {
                 System.out.println("Yes: " + i);
                 m.addTime(f2.getStartNrText(), f2.getTimeText());
             }
             f2.emptyFields();

            list.setListData(m.getSortAge().toArray());
            pane.getViewport().setView(list);



         } else if (e.getSource() == show) {
             //System.out.println("Show");
             list.setListData(m.getRunnersArray().toArray());
             //     pane.getViewport().add(list);
             pane.getViewport().setView(list);
         } else if (e.getSource() == add) {
 
             f = new FillOut();
             int i = JOptionPane.showConfirmDialog(panel, f, "popup", JOptionPane.YES_OPTION);
             if (i == JOptionPane.YES_OPTION) {
                 System.out.println("Yes: " + i);
                 m.newRunner(f.getStartNr(), f.getNameText(), f.getNationalityText(), f.getAgeText());
                 f.addOneStartNr();
                 pane.getViewport().setView(list);

                 System.out.println("STARTNR:: " + f.getStartNr());
             }
            
            list.setListData(m.getSortAge().toArray());
             pane.getViewport().setView(list);
 
 


         } else if (e.getSource() == sortStartNr) {
             System.out.println("Start");
             list.setListData(m.getRunnersArray().toArray());
             pane.getViewport().setView(list);
         } else if (e.getSource() == sortName) {
             System.out.println("Name");
             list.setListData(m.getSortName().toArray());
             pane.getViewport().setView(list);
             //   list.setListData(listData);
         } else if (e.getSource() == sortAge) {
             System.out.println("Age");
             list.setListData(m.getSortAge().toArray());
             pane.getViewport().setView(list);
             System.out.println(m.toString());
         } else if (e.getSource() == sortTime) {
             System.out.println("time");
             list.setListData(m.getSortTime().toArray());
             pane.getViewport().setView(list);
         }
     }
 
     public void addRunner() {
         System.out.println("Inne i addRunner");
     }
 }
