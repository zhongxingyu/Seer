 package testpackage.interfaces;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 
 import testpackage.shared.ship.AI;
 import testpackage.shared.ship.BadAI;
 import testpackage.shared.ship.GoodAI;
 import testpackage.shared.ship.IntelligentAI;
 import testpackage.shared.ship.Rules;
 import translator.Translator;
 
 class SettingsChooser {
 
 	boolean finished = false;
 	boolean speerfeuerenabled = false;
 	boolean ammoenabled = false;
 	boolean moreshotsenabled = false;
 	boolean rangeenabled = false;
 	int ammospinnervalue;
 	int speerfeuerspinnervalue;
 	int w;
 	int h;
 	Class<? extends AI> chosenAI;
 	Translator translator;
 	
 	SettingsChooser(Translator translator) {
 		 this.translator = translator;
 	}
 	
 	void askForSettings(JFrame parent) {
 		
 		/* TODO
 		 * 3. board size vælger
 		 * 4. multiple shots
 		 */
 		
 		SpinnerModel ammomodel = new SpinnerNumberModel(Rules.shotsPerShipPart, 1, 20, 1);
 		SpinnerModel speerfeuermodel = new SpinnerNumberModel(((double) Rules.standardSpeerfeuerTime)/1000, 0.1, 30.0, 0.1);
 		SpinnerModel wboarddimensionmodel = new SpinnerNumberModel(Rules.defaultWidth, 4, 100 ,1);
 		SpinnerModel hboarddimensionmodel = new SpinnerNumberModel(Rules.defaultHeight, 4, 100 ,1);
 		
 		final JCheckBox rangecb = new JCheckBox(translator.translateMessage("SCRange"));
 		
 		final JSpinner ammospinner = new JSpinner(ammomodel);
 		final JSpinner speerfeuerspinner = new JSpinner(speerfeuermodel);
 		final JSpinner heightspinner = new JSpinner(hboarddimensionmodel);
 		final JSpinner widthspinner = new JSpinner(wboarddimensionmodel);
 		heightspinner.setMaximumSize(heightspinner.getPreferredSize());
 		widthspinner.setMaximumSize(widthspinner.getPreferredSize());
 		
 		ActionListener listener = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				boolean checked = ((AbstractButton) arg0.getSource()).isSelected();
 
 				if (arg0.getActionCommand().equals("munition")) {
 					ammospinner.setEnabled(checked);
 					rangecb.setEnabled(checked);
 				} else if (arg0.getActionCommand().equals("speerfeuer")) {
 					speerfeuerspinner.setEnabled(checked);					
 				}
 			}
 		};
 		
 		final JDialog d = new JDialog(parent, translator.translateMessage("SCWindowTitle"), Dialog.ModalityType.DOCUMENT_MODAL);
 		Box box = Box.createVerticalBox();
 
 		final JCheckBox moreshotscb = new JCheckBox(translator.translateMessage("SCMoreShots"));
 		moreshotscb.setSelected(moreshotsenabled);
 		
 		final JCheckBox speerfeuercb = new JCheckBox(translator.translateMessage("SCSpeerCheckBox"));
 		//speerfeuercb.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
 		speerfeuercb.setSelected(speerfeuerenabled);
 		speerfeuercb.addActionListener(listener);
 		speerfeuercb.setActionCommand("speerfeuer");
 		final JCheckBox ammocb = new JCheckBox(translator.translateMessage("SCAmmoCheckBox"));
 		ammocb.setSelected(ammoenabled);
 		ammocb.addActionListener(listener);
 		ammocb.setActionCommand("munition");
 		
 		rangecb.setEnabled(ammoenabled);
 		rangecb.setSelected(rangeenabled);
 		
 		speerfeuerspinner.setEnabled(speerfeuerenabled);
 		ammospinner.setEnabled(ammoenabled);
 		
 		@SuppressWarnings("unchecked")
 		final Class<? extends AI>[] availableAIs = new Class[] {BadAI.class, GoodAI.class, IntelligentAI.class};
 		final JComboBox aidropdown = new JComboBox();
 		for (Class<? extends AI> ai : availableAIs) {
 			String name = ai.getName();
 			if (name.lastIndexOf('.') > 0) {
 				name = name.substring(name.lastIndexOf('.')+1);
 			}
 			aidropdown.addItem(name);
 		}
 
 		float alignment = Component.LEFT_ALIGNMENT;
 		
 		heightspinner.setAlignmentX(alignment);
 		widthspinner.setAlignmentX(alignment);
 
 		JLabel heightlabel = new JLabel(translator.translateMessage("SCHeightLabel"));
 		JLabel widthlabel = new JLabel(translator.translateMessage("SCWidthLabel"));
 
 		moreshotscb.setAlignmentX(alignment);
 		speerfeuercb.setAlignmentX(alignment);
 		JLabel speerlabel = new JLabel(translator.translateMessage("SCSpeerLabel"));
 		speerfeuerspinner.setAlignmentX(alignment);
 		speerfeuerspinner.setMaximumSize(speerfeuerspinner.getPreferredSize());
 		ammocb.setAlignmentX(alignment);
 		JLabel ammolabel = new JLabel(translator.translateMessage("SCAmmoLabel"));
 		ammospinner.setAlignmentX(alignment);
 		ammospinner.setMaximumSize(ammospinner.getPreferredSize());
 		JLabel ailabel = new JLabel(translator.translateMessage("SCAILabel"));
 		aidropdown.setAlignmentX(alignment);
 		aidropdown.setMaximumSize(aidropdown.getPreferredSize());
 		JButton but = new JButton(translator.translateMessage("OK"));
 		but.setAlignmentX(alignment);
 		but.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent evt) {
 				h = (Integer) heightspinner.getValue();
 				w = (Integer) widthspinner.getValue();
				
 				moreshotsenabled = moreshotscb.isSelected();
 				speerfeuerenabled = speerfeuercb.isSelected();
 				ammoenabled = ammocb.isSelected();
 				speerfeuerspinnervalue = (int) (1000 * ((Double)speerfeuerspinner.getValue()));
 				ammospinnervalue = (Integer) ammospinner.getValue();
 				rangeenabled = ammoenabled && rangecb.isSelected();
 				chosenAI = availableAIs[aidropdown.getSelectedIndex()];
 				try {
 					if (ammoenabled) {
 						if (!chosenAI.getConstructor().newInstance().supportsAmmo()) {
 							javax.swing.JOptionPane.showMessageDialog(d, translator.translateMessage("SCAIUncapable",(String) aidropdown.getSelectedItem()), translator.translateMessage("userErrorWindowTitle"), 0);
 							return;
 						}
 						if (rangeenabled && !chosenAI.getConstructor().newInstance().supportsRange()) {
 							javax.swing.JOptionPane.showMessageDialog(d, "Selected AI doesn't support range", translator.translateMessage("userErrorWindowTitle"), 0);
							return;
 						}
 					}
 				} catch (Exception ex) {
 				}
				finished = true;
 				d.dispose();
 			}
 		});
 
 		box.add(heightlabel);
 		box.add(heightspinner);
 		box.add(widthlabel);
 		box.add(widthspinner);
 		box.add(Box.createRigidArea(new Dimension(0,15)));
 		box.add(moreshotscb);
 		box.add(Box.createRigidArea(new Dimension(0,15)));
 		box.add(speerfeuercb);
 		box.add(speerlabel);
 		box.add(speerfeuerspinner);
 		box.add(Box.createRigidArea(new Dimension(0,15)));
 		box.add(ammocb);
 		box.add(ammolabel);
 		box.add(ammospinner);
 		box.add(rangecb);
 		box.add(Box.createRigidArea(new Dimension(0,15)));
 		box.add(ailabel);
 		box.add(aidropdown);
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
 		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 		buttonPanel.add(Box.createHorizontalGlue());
 		buttonPanel.add(but);
 		box.add(Box.createVerticalGlue());
 		box.add(buttonPanel);
 
 		d.add(box, BorderLayout.CENTER);
 		d.pack();
 		d.setVisible(true);
 		//return new boolean[] {ammoenabled, speerfeuerenabled};
 	}
 }
