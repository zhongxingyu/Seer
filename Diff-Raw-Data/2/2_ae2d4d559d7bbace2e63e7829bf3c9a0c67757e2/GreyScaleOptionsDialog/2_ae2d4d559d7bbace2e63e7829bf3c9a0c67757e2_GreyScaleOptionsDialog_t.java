 package br.ufrj.dcc.compgraf.im.ui.options;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import br.ufrj.dcc.compgraf.im.color.GreyScale;
 import br.ufrj.dcc.compgraf.im.color.GreyScale.GreyScaleType;
 import br.ufrj.dcc.compgraf.im.ui.SwingUtils;
 import br.ufrj.dcc.compgraf.im.ui.UIContext;
 
 public class GreyScaleOptionsDialog extends JDialog
 {
 
   public GreyScaleOptionsDialog()
   {
     super(UIContext.instance().getMainWindow(), "Choose the grey scaling type", true);
     
     setSize(240, 100);
     
     SwingUtils.configureDialog(this);
     
     initLayout();
   }
 
   private void initLayout()
   {
     JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
     panel.setOpaque(true);
     setContentPane(panel);
     
     final JComboBox typesCombo = new JComboBox();
     for (GreyScaleType greyScaleType : GreyScaleType.values())
     {
       typesCombo.addItem(greyScaleType);
     }
     
     JButton applyButton = new JButton("Apply");
     applyButton.addActionListener(new ActionListener()
     {
       @Override
       public void actionPerformed(ActionEvent e)
       {
         int selected = typesCombo.getSelectedIndex();
         GreyScaleType selectedType = GreyScaleType.values()[selected];
         
         BufferedImage newImg = new GreyScale().toGreyScale(UIContext.instance().getCurrentImage(), selectedType);
         UIContext.instance().changeCurrentImage(newImg);
         
         dispose();
       }
     });
     
    add(new JLabel("Type:"));
     add(typesCombo);
     add(applyButton);
   }
   
 }
