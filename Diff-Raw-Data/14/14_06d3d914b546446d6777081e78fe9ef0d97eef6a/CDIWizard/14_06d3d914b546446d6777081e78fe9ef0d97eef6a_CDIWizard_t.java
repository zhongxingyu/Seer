 /*******************************************************************************
  * Copyright (c) 2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.cdi.bot.test.uiutils.wizards;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swtbot.swt.finder.SWTBot;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 
 public class CDIWizard extends Wizard {
 
 	private final CDIWizardType type;
 
 	public CDIWizard(CDIWizardType type) {
 		super(new SWTBot().activeShell().widget);
 		assert ("New " + type).equals(getText());
 		this.type = type;
 	}
 
 	public CDIWizard setName(String name) {
 		setText("Name:", name);
 		return this;
 	}
 
 	public CDIWizard setPackage(String pkg) {
 		setText("Package:", pkg);
 		return this;
 	}
 
 	public CDIWizard setInherited(boolean set) {
 		setCheckbox("Add @Inherited", set);
 		return this;
 	}
 
 	public boolean isInherited() {
 		return isCheckboxSet("Add @Inherited");
 	}
 
 	public CDIWizard setGenerateComments(boolean set) {
 		setCheckbox("Generate comments", set);
 		return this;
 	}
 
 	public boolean isGenerateComments() {
 		return isCheckboxSet("Generate comments");
 	}
 
 	public CDIWizard setNormalScope(boolean set) {
 		switch (type) {
 		case SCOPE:
 			setCheckbox("is normal scope", set);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public boolean isNormalScope() {
 		switch (type) {
 		case SCOPE:
 			return isCheckboxSet("is normal scope");
 		default:
 			return true;
 		}
 	}
 
 	public CDIWizard setPassivating(boolean set) {
 		switch (type) {
 		case SCOPE:
 			setCheckbox("is passivating", set);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public boolean isPassivating() {
 		switch (type) {
 		case SCOPE:
 			return isCheckboxSet("is passivating");
 		default:
 			return false;
 		}
 	}
 
 	public CDIWizard setAlternative(boolean set) {
 		switch (type) {
 		case STEREOTYPE:
 			setCheckbox("Add @Alternative", set);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public boolean isAlternative() {
 		switch (type) {
 		case STEREOTYPE:
 			return isCheckboxSet("Add @Alternative");
 		default:
 			return false;
 		}
 	}
 
 	public CDIWizard setNamed(boolean set) {
 		switch (type) {
 		case STEREOTYPE:
 			setCheckbox("Add @Named", set);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public boolean isNamed() {
 		switch (type) {
 		case STEREOTYPE:
 			return isCheckboxSet("Add @Named");
 		default:
 			return false;
 		}
 	}
 
 	public CDIWizard setTarget(String target) {
 		switch (type) {
 		case STEREOTYPE:
 		case INTERCEPTOR_BINDING:
 			setCombo("Target:", target);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public List<String> getTargets() {
 		return Arrays.asList(bot().comboBoxWithLabel("Target:").items());
 	}
 
 	public CDIWizard setScope(String scope) {
 		switch (type) {
 		case STEREOTYPE:
 			setCombo("Scope:", scope);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public List<String> getScopes() {
 		return Arrays.asList(bot().comboBoxWithLabel("Scope:").items());
 	}
 
 	public CDIWizard addIBinding(String ib) {
 		switch (type) {
 		case INTERCEPTOR_BINDING:
 		case STEREOTYPE:
 		case INTERCEPTOR:
 			bot().button("Add", 0).click();
 			SWTBotShell sh = bot().activeShell();
 			sh.bot().text().setText(ib);
 			sh.bot().button("OK").click();
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public List<String> getIBindings() {
 		return Arrays.asList(bot().listWithLabel("Interceptor Bindings:").getItems());
 	}
 
 	public CDIWizard addStereotype(String stereotype) {
 		switch (type) {
 		case STEREOTYPE:
 			bot().button("Add", 1).click();
 			SWTBotShell sh = bot().activeShell();
 			sh.bot().text().setText(stereotype);
 			sh.bot().button("OK").click();
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public List<String> getStereotypes() {
 		return Arrays.asList(bot().listWithLabel("Stereotypes:").getItems());
 	}
 
 	public CDIWizard setPublic(boolean isPublic) {
 		switch (type) {
 		case DECORATOR:
 			if (isPublic) {
 				bot().radio("public").click();
 			} else {
 				class Radio2 extends SWTBotRadio {
 					Radio2(Button b) {
 						super(b);
 					}
 					
 					@Override
 					public SWTBotRadio click() {
 						return (SWTBotRadio) click(true);
 					}
 				}
 				final Button b = bot().radio("default").widget;
 				new Radio2(b).click();
 			}
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public CDIWizard setFieldName(String name) {
 		switch (type) {
 		case DECORATOR:
 			setText("Delegate Field Name:", name);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 	
 	public CDIWizard addInterface(String intf) {
 		switch (type) {
 		case DECORATOR:
 			bot().button("Add...", 0).click();
 			SWTBotShell sh = bot().activeShell();
 			sh.bot().text().setText(intf);
 			sh.bot().table().getTableItem(0).select();
 			sh.bot().button("OK").click();
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public CDIWizard setAbstract(boolean isAbstract) {
 		switch (type) {
 		case DECORATOR:
 			setCheckbox("abstract", isAbstract);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public CDIWizard setFinal(boolean isFinal) {
 		switch (type) {
 		case DECORATOR:
 			setCheckbox("final", isFinal);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 
 	public CDIWizard setSuperclass(String name) {
 		switch (type) {
 		case INTERCEPTOR:
 			bot().button("Browse...", 2).click();
 			SWTBotShell sh = bot().activeShell();
 			sh.bot().text().setText(name);
 			sh.bot().table().getTableItem(0).select();
 			sh.bot().button("OK").click();
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 	
 	public CDIWizard setMethodName(String name) {
 		switch (type) {
 		case INTERCEPTOR:
 			setText("Around Invoke Method Name:", name);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		return this;
 	}
 	
 	private void setCheckbox(String label, boolean set) {
 		SWTBotCheckBox c = bot().checkBox(label);
 		if (c.isChecked() != set) {
 			if (set) {
 				c.select();
 			} else {
 				c.deselect();
 			}
 		}
 	}
 
 	private boolean isCheckboxSet(String label) {
 		SWTBotCheckBox c = bot().checkBox(label);
 		return c.isChecked();
 	}
 
 	private void setCombo(String label, String value) {
 		SWTBotCombo c = bot().comboBoxWithLabel(label);
 		c.setSelection(value);
 	}
 
 }
