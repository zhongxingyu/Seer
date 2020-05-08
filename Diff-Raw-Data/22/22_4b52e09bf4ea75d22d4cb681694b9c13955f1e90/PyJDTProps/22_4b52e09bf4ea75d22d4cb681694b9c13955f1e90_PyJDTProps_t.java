 package net.kbserve.pyjdt.properties;
 
 import net.kbserve.pyjdt.properties.models.RootContainer;
 import net.kbserve.pyjdt.properties.view.PropertyComposite;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.dialogs.PropertyPage;
 
 public class PyJDTProps extends PropertyPage {
 
 	PropertyComposite propertyComposite;
 
 	/**
 	 * Constructor for SamplePropertyPage.
 	 */
 	public PyJDTProps() {
 		super();
 	}
 
 	/**
 	 * @see PreferencePage#createContents(Composite)
 	 */
 	protected Control createContents(Composite parent) {
 		propertyComposite = new PropertyComposite(parent, SWT.NONE,
 				getElement());
 		propertyComposite.pack();
 		return propertyComposite;
 	}
 
 	protected void performDefaults() {
 		super.performDefaults();
 		RootContainer.revert(getElement());
		propertyComposite.pack(true);
 	}
 
 	@Override
 	public IProject getElement() {
 		return (IProject) super.getElement().getAdapter(IProject.class);
 	}
 
 	@Override
 	public boolean performOk() {
 		try {
 			RootContainer.getRoot(getElement()).save();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return false;
 
 	}
 
 }
