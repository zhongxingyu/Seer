 package de.g18.BitBank.Gui;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import javax.swing.AbstractListModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 import de.g18.BitBank.BankController;
 import de.g18.BitBank.Kunde;
 import de.g18.BitBank.Gui.Listener.KundenAuswahlListener;
 
 /**
  * Gui Klasse zur erleichterten Kundenauswahl.
  * 
  * @author it1-markde
  * @since JRE6
  */
 
 public class KundenAuswahl extends JDialog {
 	private static final long serialVersionUID = -785098997227623108L;
 	private JTextField kundenNummerField;
 
 	public KundenAuswahl(final BankController controller,
 			final JTextField kundenNummerField) {
 		this.kundenNummerField = kundenNummerField;
 		this.setLocationRelativeTo(null);
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(5, 5, 5, 5);
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setTitle("Kundenauswahl");
 		this.setSize(new Dimension(200, 300));
 		this.setModal(true);
 
 		JButton auswaehlenButton = new JButton("Auswählen");
 
 		final JList<Object> liste = new JList<Object>(
 				new AbstractListModel<Object>() {
 					private static final long serialVersionUID = -7562748123613036992L;
 
 					@Override
 					public int getSize() {
 						return controller.getKundenCount();
 					}
 
 					@Override
 					public Object getElementAt(final int index) {
 						Kunde k = controller.getKundeByIndex(index);
 						return k.getKundenNummmer() + " - " + k.getName();
 					}
 				});
 
 		liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		JScrollPane listeScrollPane = new JScrollPane(liste);
 
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 5;
 		c.weighty = 5;
 		c.fill = GridBagConstraints.BOTH;
 		this.add(listeScrollPane, c);
 
 		c.gridx = 0;
 		c.gridy = 2;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.fill = GridBagConstraints.BOTH;
 		this.add(auswaehlenButton, c);
 
 		auswaehlenButton.addActionListener(new KundenAuswahlListener(liste,
 				this, controller));
 
 		this.setVisible(true);
 	}
 
 	public final void setKundenNummerField(final Kunde kunde) {
		try {
			this.kundenNummerField.setText("" + (kunde.getKundenNummmer()));
		} catch (java.lang.NullPointerException e) {
		}
 	}
 }
