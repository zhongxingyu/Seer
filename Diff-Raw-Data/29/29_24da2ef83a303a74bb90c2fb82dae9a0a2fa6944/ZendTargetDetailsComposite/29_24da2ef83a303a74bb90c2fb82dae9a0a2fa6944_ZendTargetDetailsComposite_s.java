 package org.zend.php.zendserver.deployment.ui.targets;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.zend.php.zendserver.deployment.core.targets.TargetsManagerService;
 import org.zend.php.zendserver.deployment.ui.Messages;
 import org.zend.sdklib.internal.target.ZendTarget;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.IZendTarget;
 
 /**
  * Basic zend target details composite, consisting of Host, Key and Key secret.
  */
 public class ZendTargetDetailsComposite extends AbstractTargetDetailsComposite {
 
 	private Text hostText;
 	private Text keyText;
 	private Text secretText;
 
 	public Composite create(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		composite.setLayout(new GridLayout(2, false));
 
 		Label label = new Label(composite, SWT.NONE);
 		label.setText(Messages.TargetDialog_Host);
 		hostText = new Text(composite, SWT.BORDER);
 		hostText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
 		hostText.setToolTipText(Messages.TargetDialog_HostTooltip);
 
 		label = new Label(composite, SWT.NONE);
 		label.setText(Messages.TargetDialog_KeyName);
 		keyText = new Text(composite, SWT.BORDER);
 		keyText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
 		keyText.setToolTipText(Messages.TargetDialog_KeyTooltip);
 
 		label = new Label(composite, SWT.NONE);
 		label.setText(Messages.TargetDialog_KeySecret);
 		secretText = new Text(composite, SWT.BORDER);
 		secretText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
 				false));
 		secretText.setToolTipText(Messages.TargetDialog_SecretTooltip);
 
 		return composite;
 	}
 
 	public void setDefaultTargetSettings(IZendTarget defaultTarget) {
 		if (hostText != null) {
 			hostText.setText(defaultTarget.getHost().toString());
 		}
 		if (keyText != null) {
 			keyText.setText(defaultTarget.getKey());
 		}
 		if (secretText != null) {
 			secretText.setText(defaultTarget.getSecretKey());
 		}
 	}
 
 	public String[] getData() {
 		return new String[] { hostText.getText(), keyText.getText(), secretText.getText(), };
 	}
 
	public IZendTarget createTarget(String[] data) {
 		URL host = null;
 		try {
 			host = new URL(data[0]);
 		} catch (MalformedURLException e) {
			// should be checked earlier
 		}
 
 		TargetsManager tm = TargetsManagerService.INSTANCE.getTargetManager();
 		String id = tm.createUniqueId(null);
 		return new ZendTarget(id, host, data[1], data[2]);
 	}
 
 	@Override
 	public boolean hasPage() {
 		return true;
 	}
 
 }
