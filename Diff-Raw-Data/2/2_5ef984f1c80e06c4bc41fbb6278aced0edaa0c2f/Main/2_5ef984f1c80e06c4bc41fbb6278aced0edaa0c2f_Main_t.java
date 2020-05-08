 package com.thestaticvoid.mdretirement;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.TypedEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 
 public class Main implements SelectionListener, FocusListener {
 	private static final int MIN_AGE = 18, MAX_AGE = 55;
 	private static final int MIN_SALARY = 30000, MAX_SALARY = 150000;
 	private static final int MIN_SERVICE = 1, MAX_SERVICE = 42;
 	private static final int MIN_RETAGE = 60, MAX_RETAGE = 70;
 	private static final int MIN_DEATHAGE = 75, MAX_DEATHAGE = 100;
 	private static final int MIN_RETURN = 0, MAX_RETURN = 10;
 	
 	private enum Source {
 		SCALE,
 		TEXT,
 	}
 	
 	private Display display;
 	private Shell shell;
 	private Scale ageScale, salaryScale, serviceScale, retageScale, deathageScale, returnScale;
 	private Text ageText, salaryText, serviceText, retageText, deathageText, returnText;
 	private Label srpsLabel, orpLabel;
 	
 	public Main() {
 		display = new Display();
 		
 		shell = new Shell(display);
 		shell.setText("Maryland Retirement Comparison");
 		GridLayout gridLayout = new GridLayout(2, false);
 		shell.setLayout(gridLayout);
 		
 		Composite varsComp = new Composite(shell, SWT.NONE);
 		gridLayout = new GridLayout(2, false);
 		gridLayout.verticalSpacing = 0;
 		varsComp.setLayout(gridLayout);
 		varsComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		GridData gridData = new GridData();
 		gridData.horizontalSpan = 2;
 		
 		Label label = new Label(varsComp, SWT.NONE);
 		label.setText("Age");
 		label.setLayoutData(gridData);
 		ageScale = new Scale(varsComp, SWT.HORIZONTAL);
 		ageScale.setMinimum(MIN_AGE);
 		ageScale.setMaximum(MAX_AGE);
 		ageScale.setSelection(MIN_AGE);
 		ageScale.addSelectionListener(this);
 		ageScale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		ageText = new Text(varsComp, SWT.BORDER);
 		ageText.addSelectionListener(this);
 		ageText.addFocusListener(this);
 		
 		label = new Label(varsComp, SWT.NONE);
 		label.setText("Salary");
 		label.setLayoutData(gridData);
 		salaryScale = new Scale(varsComp, SWT.HORIZONTAL);
 		salaryScale.setMinimum(MIN_SALARY / 1000);
 		salaryScale.setMaximum(MAX_SALARY / 1000);
 		salaryScale.setSelection(MIN_SALARY/ 1000);
 		salaryScale.addSelectionListener(this);
 		salaryScale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		salaryText = new Text(varsComp, SWT.BORDER);
 		salaryText.addSelectionListener(this);
 		salaryText.addFocusListener(this);
 		
 		label = new Label(varsComp, SWT.NONE);
 		label.setText("Expected Length of Service");
 		label.setLayoutData(gridData);
 		serviceScale = new Scale(varsComp, SWT.HORIZONTAL);
 		serviceScale.setMinimum(MIN_SERVICE);
 		serviceScale.setMaximum(MAX_SERVICE);
 		serviceScale.setSelection(MIN_SERVICE);
 		serviceScale.addSelectionListener(this);
 		serviceScale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		serviceText = new Text(varsComp, SWT.BORDER);
 		serviceText.addSelectionListener(this);
 		serviceText.addFocusListener(this);
 		
 		label = new Label(varsComp, SWT.NONE);
 		label.setText("Retirement Age");
 		label.setLayoutData(gridData);
 		retageScale = new Scale(varsComp, SWT.HORIZONTAL);
 		retageScale.setMinimum(MIN_RETAGE);
 		retageScale.setMaximum(MAX_RETAGE);
 		retageScale.setSelection(65);
 		retageScale.addSelectionListener(this);
 		retageScale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		retageText = new Text(varsComp, SWT.BORDER);
 		retageText.addSelectionListener(this);
 		retageText.addFocusListener(this);
 		
 		label = new Label(varsComp, SWT.NONE);
 		label.setText("Money Required Until");
 		label.setLayoutData(gridData);
 		deathageScale = new Scale(varsComp, SWT.HORIZONTAL);
 		deathageScale.setMinimum(MIN_DEATHAGE);
 		deathageScale.setMaximum(MAX_DEATHAGE);
 		deathageScale.setSelection(95);
 		deathageScale.addSelectionListener(this);
 		deathageScale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		deathageText = new Text(varsComp, SWT.BORDER);
 		deathageText.addSelectionListener(this);
 		deathageText.addFocusListener(this);
 		
 		label = new Label(varsComp, SWT.NONE);
 		label.setText("Expected Inflation-Adjusted Rate of Return");
 		label.setLayoutData(gridData);
 		returnScale = new Scale(varsComp, SWT.HORIZONTAL);
 		returnScale.setMinimum(MIN_RETURN);
 		returnScale.setMaximum(MAX_RETURN);
 		returnScale.setSelection(5);
 		returnScale.addSelectionListener(this);
 		returnScale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		returnText = new Text(varsComp, SWT.BORDER);
 		returnText.addSelectionListener(this);
 		returnText.addFocusListener(this);
 		
 		Composite resultComp = new Composite(shell, SWT.NONE);
 		gridLayout = new GridLayout(1, true);
 		resultComp.setLayout(gridLayout);
 		gridData = new GridData();
 		gridData.verticalAlignment = SWT.CENTER;
 		resultComp.setLayoutData(gridData);
 		
 		label = new Label(resultComp, SWT.NONE);
 		label.setText("Monthly Pre-Tax Spending Power in Retirement");
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.CENTER;
 		label.setLayoutData(gridData);
 		
 		Group srpsGroup = new Group(resultComp, SWT.NONE);
 		srpsGroup.setText("SRPS");
 		srpsGroup.setLayout(new GridLayout());
 		
 		srpsLabel = new Label(srpsGroup, SWT.NONE);
 		srpsLabel.setText("Not Possible");
 		FontData fontData = new FontData();
 		fontData.setHeight(36);
 		srpsLabel.setFont(new Font(display, fontData));
 		
 		Group orpGroup = new Group(resultComp, SWT.NONE);
 		orpGroup.setText("ORP");
 		orpGroup.setLayout(new GridLayout());
 		
 		orpLabel = new Label(orpGroup, SWT.NONE);
 		orpLabel.setText("Not Possible");
 		fontData = new FontData();
 		fontData.setHeight(36);
 		orpLabel.setFont(new Font(display, fontData));
 		
 		shell.pack();
 		
 		setVar(ageScale, ageText, Source.SCALE, MIN_AGE, MAX_AGE);
 		setVar(salaryScale, salaryText, Source.SCALE, MIN_SALARY, MAX_SALARY);
 		setVar(serviceScale, serviceText, Source.SCALE, MIN_SERVICE, MAX_SERVICE);
 		setVar(retageScale, retageText, Source.SCALE, MIN_RETAGE, MAX_RETAGE);
 		setVar(deathageScale, deathageText, Source.SCALE, MIN_DEATHAGE, MAX_DEATHAGE);
 		setVar(returnScale, returnText, Source.SCALE, MIN_RETURN, MAX_RETURN);
 		
 		shell.setMinimumSize(700, shell.getSize().y);
 		shell.open();
 		
 		while (!shell.isDisposed())
 			if (!display.readAndDispatch())
 				display.sleep();
 		
 		display.dispose();
 	}
 	
 	private void recalculate() {
 		String srpsValue = "";
 		String orpValue = "";
 		
 		int age = ageScale.getSelection();
 		int salary = 0;
 		try {
 			salary = Integer.parseInt(salaryText.getText());
 		} catch (NumberFormatException nfe) {
 			// ignore
 		}
 		int service = serviceScale.getSelection();
 		int retage = retageScale.getSelection();
 		int deathage = deathageScale.getSelection();
 		int returnRate = returnScale.getSelection();
 		
 		if (age + service > retage) {
 			srpsValue = "Not Possible";
 			orpValue = "Not Possible";
 		} else {
 			// SRPS
 			if (age + service >= 90 || (retage >= 65 && service >= 10) || (retage >= 60 && service >= 15)) {
 				double allowance = 0.015 * salary * service;
 				
 				if (retage >= 60 && retage < 65 && service >= 15) {
 					for (int i = 0; i < ((65 - retage) * 12); i++) {
 						allowance -= allowance * 0.005;
 					}
 				}
 				
 				srpsValue = "$" + Math.round(allowance / 12);
 			} else {
 				srpsValue = "Not Vested";
 			}
 			
 			// ORP
 			double value = 0;
 			
 			for (int i = 0; i < service; i++) {
 				value *= 1 + returnRate / 100.0;
 				value += 0.1425 * salary;
 			}
 			
			for (int i = age + service; i < retage; i++) {
 				value *= 1 + returnRate / 100.0;
 			}
 			
 			orpValue = "$" + Math.round(value / ((deathage - retage + 1) * 12));
 		}
 		
 		srpsLabel.setText(srpsValue);
 		srpsLabel.pack();
 		
 		orpLabel.setText(orpValue);
 		orpLabel.pack();
 	}
 	
 	private void setVar(Scale scale, Text text, Source source, int min, int max) {
 		int value;
 		
 		if (source == Source.SCALE) {
 			value = scale.getSelection();
 			text.setText("" + (scale == salaryScale ? value * 1000 : value));
 		} else {
 			try {
 				value = Integer.parseInt(text.getText());
 			} catch (NumberFormatException nfe) {
 				text.setText("" + (scale == salaryScale ? scale.getSelection() * 1000 : scale.getSelection()));
 				return;
 			}
 			
 			if (value < min || value > max) {
 				text.setText("" + (scale == salaryScale ? scale.getSelection() * 1000 : scale.getSelection()));
 				return;
 			}
 			
 			scale.setSelection(scale == salaryScale ? value / 1000 : value);
 		}
 		
 		recalculate();
 	}
 	
 	private void event(TypedEvent e) {
 		if (e.getSource() == ageScale) {
 			setVar(ageScale, ageText, Source.SCALE, MIN_AGE, MAX_AGE);
 		} else if (e.getSource() == ageText) {
 			setVar(ageScale, ageText, Source.TEXT, MIN_AGE, MAX_AGE);
 		} else if (e.getSource() == salaryScale) {
 			setVar(salaryScale, salaryText, Source.SCALE, MIN_SALARY, MAX_SALARY);
 		} else if (e.getSource() == salaryText) {
 			setVar(salaryScale, salaryText, Source.TEXT, MIN_SALARY, MAX_SALARY);
 		} else if (e.getSource() == serviceScale) {
 			setVar(serviceScale, serviceText, Source.SCALE, MIN_SERVICE, MAX_SERVICE);
 		} else if (e.getSource() == serviceText) {
 			setVar(serviceScale, serviceText, Source.TEXT, MIN_SERVICE, MAX_SERVICE);
 		} else if (e.getSource() == retageScale) {
 			setVar(retageScale, retageText, Source.SCALE, MIN_RETAGE, MAX_RETAGE);
 		} else if (e.getSource() == retageText) {
 			setVar(retageScale, retageText, Source.TEXT, MIN_RETAGE, MAX_RETAGE);
 		} else if (e.getSource() == deathageScale) {
 			setVar(deathageScale, deathageText, Source.SCALE, MIN_DEATHAGE, MAX_DEATHAGE);
 		} else if (e.getSource() == deathageText) {
 			setVar(deathageScale, deathageText, Source.TEXT, MIN_DEATHAGE, MAX_DEATHAGE);
 		} else if (e.getSource() == returnScale) {
 			setVar(returnScale, returnText, Source.SCALE, MIN_RETURN, MAX_RETURN);
 		} else if (e.getSource() == returnText) {
 			setVar(returnScale, returnText, Source.TEXT, MIN_RETURN, MAX_RETURN);
 		}		
 	}
 
 	public void widgetSelected(SelectionEvent e) {
 		event(e);
 	}
 
 	public void widgetDefaultSelected(SelectionEvent e) {
 		event(e);
 	}
 	
 	public void focusGained(FocusEvent e) {
 		// empty
 	}
 
 	public void focusLost(FocusEvent e) {
 		event(e);
 	}
 	
 	public static void main(String[] args) {
 		new Main();
 	}
 }
