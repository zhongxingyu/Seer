 
 package org.paxle.desktop.impl.dialogues.settings;
 
 import java.awt.GridLayout;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
 import org.osgi.service.metatype.AttributeDefinition;
 import org.paxle.desktop.impl.Messages;
 import org.paxle.desktop.impl.event.MultipleChangesListener;
 
 class BooleanAttrConfig extends AbstractAttrConfig<Boolean> {
 	
 	private JRadioButton rbTrue = new JRadioButton();
 	private JRadioButton rbFalse = new JRadioButton();
 	
 	public BooleanAttrConfig(final AttributeDefinition ad) {
 		super(ad);
 	}
 	
 	@Override
 	protected JComponent createOptionComp(Object value, MultipleChangesListener mcl) {
 		final JPanel op = new JPanel(new GridLayout(1, 3, 5, 5));
		rbTrue = new JRadioButton(Messages.getString("booleanAttrConfig.true")); //$NON-NLS-1$
		rbFalse = new JRadioButton(Messages.getString("booleanAttrConfig.false")); //$NON-NLS-1$
 		final ButtonGroup bg = new ButtonGroup();
 		bg.add(rbTrue);
 		bg.add(rbFalse);
 		op.add(rbTrue);
 		op.add(rbFalse);
 		mcl.addComp2Monitor(rbTrue);
 		return op;
 	}
 	
 	@Override
 	public Boolean getValue() {
 		return Boolean.valueOf(rbTrue.isSelected());
 	}
 	
 	@Override
 	protected Boolean fromObject(Object value) {
 		return (value instanceof Boolean) ? (Boolean)value : Boolean.valueOf(value.toString());
 	}
 	
 	@Override
 	public void setValue(Boolean value) {
 		((value.booleanValue()) ? rbTrue : rbFalse).setSelected(true);
 	}
 }
