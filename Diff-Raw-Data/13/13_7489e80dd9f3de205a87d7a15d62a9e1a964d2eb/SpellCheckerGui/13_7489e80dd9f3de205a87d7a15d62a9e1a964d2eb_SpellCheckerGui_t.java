 package com.sample.osgi.cdi.gui;
 
 import com.sample.osgi.cdi.services.SpellCheckerService;
 import com.sample.osgi.cdi.services.SpellCheckerSubstituteImpl;
 import com.sample.osgi.cdi.starter.PaymentService;
 import java.util.Collection;
 import java.util.HashSet;
import java.util.List;
 import java.util.Set;
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import org.jboss.weld.environment.osgi.integration.OSGiService;
 
 /**
  *
  * @author Mathieu ANCELIN
  */
 public class SpellCheckerGui extends JFrame {
 
     private JTextField input = null;
 
     private JButton checkButton = null;
 
     private JLabel result = null;
 
     @Inject @OSGiService
     private Collection<SpellCheckerService> spellServices;
 
     @Inject
     private PaymentService payment;
 
     @Inject
     private SpellCheckerSubstituteImpl subst;
 
     public SpellCheckerGui() {
         super();
         initComponents();      
     }
     
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         checkButton = new javax.swing.JButton();
         result = new javax.swing.JLabel();
         input = new javax.swing.JTextField();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
         getContentPane().setLayout(new java.awt.GridBagLayout());
 
         checkButton.setText("Check");
         checkButton.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 check();
                 payment.pay(4321);
                 subst.sayHello();
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         getContentPane().add(checkButton, gridBagConstraints);
 
         result.setPreferredSize(new java.awt.Dimension(175, 20));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         getContentPane().add(result, gridBagConstraints);
 
         input.setPreferredSize(new java.awt.Dimension(175, 20));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         getContentPane().add(input, gridBagConstraints);
         this.setTitle("Spellchecker Gui");
         pack();
     }
 
     private void check() {
         String text = input.getText();
         if (text == null)
             text = "";
         Set<String> wrong = new HashSet<String>();
         for (SpellCheckerService service : spellServices) {
            List<String> wrongWords = service.check(text);
            if (wrongWords != null)
                wrong.addAll(wrongWords);
         }
         if (wrong != null) {
             if (result != null) {
                 result.setText(wrong.size() + " word(s) are mispelled");
             } else {
                 result.setText("All words are correct");
             }
         }
     }
 
     @PostConstruct
     public void start() {
         this.setVisible(true);
     }
 
     public void stop() {
         this.dispose();
     }
 }
 
