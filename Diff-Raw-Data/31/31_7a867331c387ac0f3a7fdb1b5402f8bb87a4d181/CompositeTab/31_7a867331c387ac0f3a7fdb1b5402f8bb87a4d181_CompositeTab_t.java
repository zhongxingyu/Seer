 package com.gmail.bobagold.plugin1;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.ILaunchConfigurationDialog;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.List;
 
 public class CompositeTab implements ILaunchConfigurationTab {
 	private Control control;
 	private List list_from;
 	private List list_to;
 
 	@Override
 	public void createControl(Composite parent) {
 		Group container = new Group(parent, SWT.CENTER);
 		container.setText("Select desired launch configurations");
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		container.setLayout(layout);
 		// TODO Auto-generated method stub
 		list_from = new List(container, SWT.SINGLE);
 		for (String lc : collectLaunchConfigurationNames())
 			list_from.add("lc: " + lc);
 
 		Group buttons = new Group(container, 0);
 		buttons.setLayout(new GridLayout());
 		Button to_from = new Button(buttons, 0);
 		Button to_to = new Button(buttons, 0);
 		to_from.setText("<<==");
 		to_from.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
				moveSelection(list_to, list_from);
 			}
 		});
 		to_to.setText("==>>");
 		to_to.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
				moveSelection(list_from, list_to);
 			}
 		});
 //		for (String lct : collectLaunchConfigurationTypes())
 //			control.add("lct: " + lct);
 		list_to = new List(container, SWT.SINGLE);
 		list_to.add("ok");
 		control = container;
 	}
 
	protected void refresh() {
		// TODO Auto-generated method stub
		list_from.pack(true);
		list_to.pack(true);
		control.pack(true);
	}

 	private String[] collectLaunchConfigurationNames() {
 		// TODO Auto-generated method stub
 		ArrayList<String> list = new ArrayList<String>();
 		for (ILaunchConfiguration lc : collectLaunchConfigurations())
 			list.add(lc.getName());
 		return list.toArray(new String[]{});
 	}
 	public static ILaunchConfiguration[] collectLaunchConfigurations() {
 		// TODO Auto-generated method stub
 		ArrayList<ILaunchConfiguration> list = new ArrayList<ILaunchConfiguration>();
 		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
 		try {
 			for (ILaunchConfiguration lc : manager.getLaunchConfigurations())
 				if (!lc.getType().getName().equals("Composite"))
 					list.add(lc);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return list.toArray(new ILaunchConfiguration[]{});
 	}
 	private String[] collectLaunchConfigurationTypes() {
 		// TODO Auto-generated method stub
 		ArrayList<String> list = new ArrayList<String>();
 		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
 		for (ILaunchConfigurationType lc : manager.getLaunchConfigurationTypes())
 			list.add(lc.getName());
 
 		return list.toArray(new String[]{});
 	}
 
 	@Override
 	public Control getControl() {
 		// TODO Auto-generated method stub
 		return control;
 	}
 
 	@Override
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public String getErrorMessage() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getMessage() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isValid(ILaunchConfiguration launchConfig) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public boolean canSave() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void launched(ILaunch launch) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return "Launch configurations";
 	}
 
 	@Override
 	public Image getImage() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
 		// TODO Auto-generated method stub
 
 	}
 
	private void moveSelection(List list_from, List list_to) {
		String[] selected = list_from.getSelection();
		for (String selection : selected) {
			list_to.add(selection);
			list_from.remove(selection);
		}
		list_from.setSelection(new String[] {});
		list_to.setSelection(selected);
		refresh();
	}

 }
