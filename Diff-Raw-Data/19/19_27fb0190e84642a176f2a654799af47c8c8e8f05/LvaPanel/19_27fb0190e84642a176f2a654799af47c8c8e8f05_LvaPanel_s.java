 package at.ac.tuwien.sepm.ui.lehrangebot;
 
 import at.ac.tuwien.sepm.entity.LVA;
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.service.LVAService;
 import at.ac.tuwien.sepm.service.MetaLVAService;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.service.impl.ValidationException;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.StandardInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import at.ac.tuwien.sepm.ui.metaLva.LvaDisplayPanel;
 import at.ac.tuwien.sepm.ui.metaLva.MetaLVADisplayPanel;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo
  * Date: 05.06.13
  * Time: 19:05
  * To change this template use File | Settings | File Templates.
  */
 @UI
 @Scope("singleton")
 public class LvaPanel extends StandardInsidePanel {
     private LVAService lvaService;
     private MetaLVAService metaLVAService;
 
     private List<MetaLVA> metaLVAs;
     private List<LVA> lvas;
 
     private MetaLVADisplayPanel metaPane;
     private LvaDisplayPanel lvaPane;
 
     private Rectangle paneMeta = new Rectangle(12,55,490,413);
     private Rectangle paneLva = new Rectangle(510,55,490,413);
 
     private JButton refresh;
     private JButton addLva;
     private JButton addMeta;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
 
     @Autowired
     public LvaPanel(LVAService lvaService, MetaLVAService metaLVAService) {
         this.setLayout(null);
         this.setOpaque(false);
         loadFonts();
         setBounds((int) startCoordinateOfWhiteSpace.getX(), (int) startCoordinateOfWhiteSpace.getY(),(int) whiteSpace.getWidth(),(int) whiteSpace.getHeight());
         this.lvaService=lvaService;
         this.metaLVAService=metaLVAService;
         addContent();
         addTitles();
         addButtons();
     }
 
     private void addTitles() {
         JLabel metaTitle = new JLabel("LVAs");
         metaTitle.setFont(standardSmallerTitleFont);
         metaTitle.setBounds((int)whiteSpace.getWidth()/2-(int)paneMeta.getWidth()/2-75, 5, 150,30);
         this.add(metaTitle);
 
        JLabel lvatitle = new JLabel("einzelne Semester");
         lvatitle.setFont(standardSmallerTitleFont);
        lvatitle.setBounds((int)whiteSpace.getWidth()/2+(int)paneLva.getWidth()/2-90, 5, 180,30);
         this.add(lvatitle);
     }
 
     private void addButtons() {
         refresh = new JButton("Aktualisieren");
         refresh.setFont(standardButtonFont);
         refresh.setBounds((int)whiteSpace.getWidth()/2-75, (int)paneMeta.getY() + (int)paneMeta.getHeight()+8, 150,30);
         refresh.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 LvaPanel.this.refresh();
             }
         });
         //this.add(refresh);
 
         addMeta = new JButton("Erstellen");
         addMeta.setFont(standardButtonFont);
         addMeta.setBounds((int)paneMeta.getX(), (int)paneMeta.getY() + (int)paneMeta.getHeight()+8, 150,30);
         addMeta.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 PanelTube.backgroundPanel.viewMetaLva(null);
             }
         });
         this.add(addMeta);
         addLva = new JButton("Erstellen");
         addLva.setFont(standardButtonFont);
         addLva.setBounds((int)paneLva.getX() + (int)paneLva.getWidth()-150, (int)paneMeta.getY() + (int)paneMeta.getHeight()+8, 150,30);
         addLva.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     if (metaLVAService.readAll() == null) {
                         PanelTube.backgroundPanel.viewInfoText("Es muss eine MetaLva existieren.", SmallInfoPanel.Error);
                     } else {
                         PanelTube.backgroundPanel.viewLva(null);
                     }
                 } catch (ServiceException e1) {
                     log.error("Exception: "+ e1.getMessage());
                 } catch (ValidationException e1) {
                     log.error("Exception: "+ e1.getMessage());
                 }
             }
         });
         this.add(addLva);
     }
 
     private void addContent() {
         metaLVAs = new ArrayList<MetaLVA>();
         //metaLVAs = metaLVAService.readAll();
         metaPane = new MetaLVADisplayPanel(metaLVAs, (int)paneMeta.getWidth(), (int)paneMeta.getHeight());
         metaPane.setBounds(paneMeta);
         this.add(metaPane);
 
         //lvas = new ArrayList<LVA>();
         //lvas = lvaService.readAll();
         lvaPane = new LvaDisplayPanel(new ArrayList<LVA>(0), (int)paneLva.getWidth(), (int)paneLva.getHeight());
         lvaPane.setBounds(paneLva);
         metaPane.setLvaDisplayPanel(lvaPane);
         this.add(lvaPane);
     }
 
 
     @Override
     //@Scheduled(fixedDelay = 20000)
     public synchronized void refresh() {
         //threading, so the user doesn't have to wait for the window to pop up
         new Thread(){
             @Override
             public void run(){
                 try {
                     log.info("Thread for loading LVAs started");
                     metaLVAs = metaLVAService.readAll();
                     Collections.sort(metaLVAs,MetaLVA.getAlphabeticalNameComparator());
                     log.info("loaded LVAs");
                     //lvaPane.refresh(new ArrayList<LVA>(0));
                 } catch (ServiceException e) {
                     log.info("Exception caught while loading LVAs");
                     log.error("Exception: " + e.getMessage());
                 } catch (ValidationException e) {
                     log.info("Exception caught while loading LVAs");
                     log.error("Exception: " + e.getMessage());
                 }
                 metaPane.refresh(metaLVAs);
             }
         }.start();
 
     }
 }
