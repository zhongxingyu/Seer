 // -----BEGIN DISCLAIMER-----
 /*******************************************************************************
  * Copyright (c) 2010 JCrypTool Team and Contributors
  *
  * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
  * Public License v1.0 which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 // -----END DISCLAIMER-----
 package org.jcryptool.visual.verifiablesecretsharing.views;
 
 import org.jcryptool.visual.verifiablesecretsharing.algorithm.VerifiableSecretSharing;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.Random;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.jcryptool.core.util.fonts.FontService;
 
 public class VerifiableSecretSharingComposite extends Composite {
 
 	public final String uZero = ("\u2080"); //$NON-NLS-1$
 	public final String uOne = ("\u2081"); //$NON-NLS-1$
 	public final String uTwo = ("\u2082"); //$NON-NLS-1$
 	public final String uThree = ("\u2083"); //$NON-NLS-1$
 	public final String uFour = ("\u2084"); //$NON-NLS-1$
 	public final String uFive = ("\u2085"); //$NON-NLS-1$
 	public final String uSix = ("\u2086"); //$NON-NLS-1$
 	public final String uSeven = ("\u2087"); //$NON-NLS-1$
 	public final String uEight = ("\u2088"); //$NON-NLS-1$
 	public final String uNine = ("\u2089"); //$NON-NLS-1$
 
 	private static final int RESTARTBUTTONHEIGHT = 30;
 	private static final int RESTARTBUTTONWIDTH = 120;
 
 	/* colors for backgrounds. */
 	private static final Color WHITE = Display.getDefault().getSystemColor(
 			SWT.COLOR_WHITE);
 	private static final Color CYAN = Display.getDefault().getSystemColor(
 			SWT.COLOR_CYAN);
 	private static final Color GREEN = Display.getDefault().getSystemColor(
 			SWT.COLOR_GREEN);
 	private static final Color RED = Display.getDefault().getSystemColor(
 			SWT.COLOR_RED);
 
 	/* number of players for reconstruction t */
 	private static int playersRecon;
 	private static int players;
 
 	/* int array coefficients */
 	private static int[] coefficientsInt;
 
 	/* instance for calculating shares */
 	private static VerifiableSecretSharing vss = new VerifiableSecretSharing();
 
 	private static int[] playerID;
 
 	StyledText stDescription;
 	private Composite inputBody;
 	private GridLayout inputBodyLayout;
 	private Group parametersGroup;
 	private Label playerLabel;
 	private Spinner playerSpinner;
 	private Label reconstructorLabel;
 	private Spinner reconstructorSpinner;
 	private Label secretLabel;
 	private Text secretText;
 	private Label moduleLabel;
 	private Text moduleText;
 	private Label primitiveRootLabel;
 	private Text primitiveRootText;
 	private Label spaceLabel;
 	private Label nextStepButtonParameters;
 	private Button determineCoefficients;
 	private RowLayout coefficientsGroupLayout;
 	private GridLayout coefficientsPolynomNextStepLayout;
 	private Group coefficientsGroup;
 	private ScrolledComposite scrolledCoefficientsGroup;
 	private Composite scrolledCoefficientsGroupContent;
 	private Label[] coefficientsLabelsCoefficients;
 	private Spinner[] coefficientsSpinnersCoefficients;
 	private GridLayout commitGenerateButtonLayout;
 	private Composite commitGenerateButtonComposite;
 	private Button commitCoefficientsButton;
 	private Button generateCoefficientsButton;
 	private Composite polynomContent;
 	private Label polynomLabel;
 	private Text polynomText;
 	private Composite nextStepContent;
 	private Label nextStepButtonCoefficients;
 	private Button calculateShares;
 	private RowLayout commitmentsGroupLayout;
 	private GridLayout commitmentsGroupGridLayout;
 	private Group commitmentsGroup;
 	private Label coefficientLabel;
 	private Label commitmentLabel;
 	private GridData seperatorData;
 	private Label horizontalSeperator;
 	private ScrolledComposite scrolledCommitmentsGroup;
 	private Composite scrolledCommitmentsGroupContent;
 	private Label[] coefficientsLabelsCommitment;
 	private Text[] coefficientsTextCommitment;
 	private Group sharesGroup;
 	private Group reconstructionGroup;
 	private Group descriptionGroup;
 	private RowLayout sharesGroupLayout;
 	private GridLayout sharesGroupGridLayout;
 	private ScrolledComposite scrolledSharesGroup;
 	private Composite scrolledSharesGroupContent;
 	private Label indexLabel;
 	private Label shareNLabel;
 	private Label[] playerLabelShares;
 	private Label[] indexLabelShares;
 	private Composite[] shareNCompositeShares;
 	private Text[] shareNTextShares;
 	private Label[] isModShares;
 	private Text[] shareModNTextShares;
 	private Button[] checkButtonShares;
 	private RowLayout reconstructionGroupLayout;
 	private GridLayout reconstructionGroupGridLayout;
 	private ScrolledComposite scrolledReconstructionGroup;
 	private Composite scrolledReconstructionGroupContent;
 	private Label[] playerLabelReconstructions;
 	private Button[] playerCheckboxReconstructions;
 	private RowLayout shareModNRowLayout;
 	private GridData nextStepSpanData;
 	private GridLayout nextStepSpanLayout;
 	private Composite nextStepParametersComposite;
 	private Button reconstructButton;
 	private Listener onlyDigits;
 
 	public VerifiableSecretSharingComposite(final Composite parent,
 			final int style,
 			VerifiableSecretSharingView verifiableSecretSharingView) {
 		super(parent, style);
 
 		onlyDigits = new Listener() {
 			public void handleEvent(Event e) {
 				String string = e.text;
 				char[] chars = new char[string.length()];
 				string.getChars(0, chars.length, chars, 0);
 				for (int i = 0; i < chars.length; i++) {
 					if (!('0' <= chars[i] && chars[i] <= '9')) {
 						e.doit = false;
 						return;
 					}
 				}
 			}
 		};
 
 		setLayout(new GridLayout());
 		createHead();
 		createBody();
 	}
 
 	/**
 	 * Generates the head of the tab. The head has a title and a description.
 	 */
 	private void createHead() {
 		final Composite head = new Composite(this, SWT.NONE);
 		head.setBackground(WHITE);
 		head.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		head.setLayout(new GridLayout());
 
 		final Label label = new Label(head, SWT.NONE);
 		label.setFont(FontService.getHeaderFont());
 		label.setBackground(WHITE);
 		label.setText(Messages.VerifiableSecretSharingComposite_tab_title);
 		stDescription = new StyledText(head, SWT.READ_ONLY | SWT.MULTI
 				| SWT.WRAP);
 		stDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				false));
 		stDescription
 				.setText(Messages.VerifiableSecretSharingComposite_description);
 	}
 
 	private void createBody() {
 		final Composite body = new Composite(this, SWT.NONE);
 		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		body.setLayout(new GridLayout());
 
 		final Button restart = new Button(body, SWT.PUSH);
 		restart.setText(Messages.VerifiableSecretSharingComposite_restart_button);
 		restart.setForeground(CYAN);
 		restart.setLayoutData(new GridData(RESTARTBUTTONWIDTH,
 				RESTARTBUTTONHEIGHT));
 
 		final Group bodyGroup = new Group(body, SWT.FILL);
 		bodyGroup.setLayout(new GridLayout());
 		bodyGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		createInputBody(bodyGroup);
 		createDescriptionGroup(bodyGroup);
 	}
 
 	private void createInputBody(Group parent) {
 		inputBody = new Composite(parent, SWT.NONE);
 		inputBodyLayout = new GridLayout(5, false);
 		inputBodyLayout.marginWidth = 0;
 		inputBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		inputBody.setLayout(inputBodyLayout);
 
 		createParametersGroup(inputBody);
 		createCoefficientsGroup(inputBody);
 		showCoefficientsGroup(false, 0);
 		createCommitmentsGroup(inputBody);
 		showCommitmentsGroup(false, 0);
 		createSharesGroup(inputBody);
 		showSharesGroup(false, 0);
 		createReconstructionGroup(inputBody);
 		showReconstructionGroup(false, 0);
 	}
 
 	private void createParametersGroup(Composite parent) {
 		parametersGroup = new Group(parent, SWT.NONE);
 		parametersGroup.setLayout(new GridLayout(2, false));
 		parametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				false));
 		parametersGroup
 				.setText(Messages.VerifiableSecretSharingComposite_parameters_title);
 
 		playerLabel = new Label(parametersGroup, SWT.NONE);
 		playerLabel
 				.setText(Messages.VerifiableSecretSharingComposite_parameters_players);
 
 		playerSpinner = new Spinner(parametersGroup, SWT.BORDER);
 		playerSpinner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				false));
 		playerSpinner.setMinimum(1);
 
 		reconstructorLabel = new Label(parametersGroup, SWT.NONE);
 		reconstructorLabel
 				.setText(Messages.VerifiableSecretSharingComposite_parameters_reconstructors);
 
 		reconstructorSpinner = new Spinner(parametersGroup, SWT.BORDER);
 		reconstructorSpinner.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
 				true, false));
 		reconstructorSpinner.setMinimum(1);
 
 		secretLabel = new Label(parametersGroup, SWT.NONE);
 		secretLabel
 				.setText(Messages.VerifiableSecretSharingComposite_parameters_secret);
 
 		secretText = new Text(parametersGroup, SWT.BORDER);
 		secretText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		secretText.addListener(SWT.Verify, new Listener() {
 			public void handleEvent(Event e) {
 				String string = e.text;
 
 				char[] chars = new char[string.length()];
 				string.getChars(0, chars.length, chars, 0);
 				for (int i = 0; i < chars.length; i++) {
 					if (!('0' <= chars[i] && chars[i] <= '9')) {
 						e.doit = false;
 						return;
 					}
 				}
 
 			}
 		});
 		secretText.addListener(SWT.Modify, new Listener() {
 			public void handleEvent(Event e) {
 				BigInteger nextPrime;
 				Random prng = new SecureRandom();
 				String text = secretText.getText();
 				if (text != "") {
 					nextPrime = BigInteger.probablePrime(
 							new BigInteger(text).bitLength() + 1, prng);
 					moduleText.setText(nextPrime.toString());
 				} else {
 					moduleText.setText(text);
 				}
 			}
 		});
 
 		moduleLabel = new Label(parametersGroup, SWT.NONE);
 		moduleLabel
 				.setText(Messages.VerifiableSecretSharingComposite_parameters_primeMod);
 
 		moduleText = new Text(parametersGroup, SWT.BORDER);
 		moduleText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		moduleText.addListener(SWT.Verify, new Listener() {
 			public void handleEvent(Event ev) {
 				String string = ev.text;
 				char[] chars = new char[string.length()];
 				string.getChars(0, chars.length, chars, 0);
 				for (int i = 0; i < chars.length; i++) {
 					if (!('0' <= chars[i] && chars[i] <= '9')) {
 						ev.doit = false;
 						return;
 					}
 				}
 
 			}
 
 		});
 		moduleText.addListener(SWT.Modify, new Listener() {
 			public void handleEvent(Event event) {
 				int primitiveRoot;
 				if (moduleText.getText().compareTo("") != 0 && new BigInteger(moduleText.getText()).isProbablePrime(3)) {
 					primitiveRoot = generatePrimitiveRoot(moduleText.getText());
 					if (primitiveRoot != -1) {
 						primitiveRootText.setText(primitiveRoot + "");
 					}
 					else {
 						primitiveRootText.setText("");
 					}
 				}
 				else {
 					primitiveRootText.setText("");
 				}
 			}
 
 			private int generatePrimitiveRoot(String p) {
 				int pInt = Integer.parseInt(p);
 				for (int i = 2; i < pInt; i++) {
 					int j = i, o = 1;
 					do {
 						o++;
 						j = j * i % pInt;
 					} while (j != 1);
 					if (o == (pInt - 1)) {
 						return i;
 					}
 				}
 				return -1;
 			}
 		});
 
 		primitiveRootLabel = new Label(parametersGroup, SWT.NONE);
 		primitiveRootLabel
 				.setText(Messages.VerifiableSecretSharingComposite_parameters_primitiveRoot);
 
 		primitiveRootText = new Text(parametersGroup, SWT.BORDER);
 		primitiveRootText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				false));
 		primitiveRootText.addListener(SWT.Verify, onlyDigits);
 
 		spaceLabel = new Label(parametersGroup, SWT.NONE);
 		spaceLabel = new Label(parametersGroup, SWT.NONE);
 
 		nextStepSpanData = new GridData(SWT.FILL, SWT.FILL, true, false);
 		nextStepSpanData.horizontalSpan = 2;
 
 		nextStepSpanLayout = new GridLayout(2, false);
 		nextStepSpanLayout.marginWidth = 0;
 		nextStepSpanLayout.marginHeight = 0;
 
 		nextStepParametersComposite = new Composite(parametersGroup, SWT.NONE);
 		nextStepParametersComposite.setLayoutData(nextStepSpanData);
 		nextStepParametersComposite.setLayout(nextStepSpanLayout);
 
 		nextStepButtonParameters = new Label(nextStepParametersComposite,
 				SWT.NONE);
 		nextStepButtonParameters
 				.setText(Messages.VerifiableSecretSharingComposite_nextStep_button);
 		determineCoefficients = new Button(nextStepParametersComposite,
 				SWT.NONE);
 		determineCoefficients
 				.setText(Messages.VerifiableSecretSharingComposite_parameters_determineCoefficients);
 		determineCoefficients.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
 				true, false));
 		determineCoefficients.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				if (playerSpinner.getText().compareTo(
 						reconstructorSpinner.getText()) >= 0) {
 					if (moduleText.getText().compareTo(secretText.getText()) > 0) {
 						BigInteger moduleTextBI = new BigInteger(moduleText
 								.getText());
 						if (moduleTextBI.isProbablePrime(3) == true) {
 							if (isSubgroup(primitiveRootText.getText(),
 									moduleText.getText()) == true) {
 								playersRecon = Integer
 										.parseInt(reconstructorSpinner
 												.getText());
 								/* initiate array and set value for secret */
 								coefficientsInt = new int[playersRecon];
 								coefficientsInt[0]=Integer.parseInt(secretText.getText());
 								/* ************************ */
 								players = Integer.parseInt(playerSpinner
 										.getText());
 								showCoefficientsGroup(true, (playersRecon - 1));
 								coefficientsSpinnersCoefficients[0].setSelection(Integer
 										.parseInt(secretText.getText()));
 							}
 						}
 					}
 				}
 			}
 		});
 	}
 
 	private void createCoefficientsGroup(Composite parent) {
 		coefficientsGroupLayout = new RowLayout();
 		coefficientsGroupLayout.type = SWT.VERTICAL;
 		coefficientsGroupLayout.marginWidth = 0;
 		coefficientsGroupLayout.marginHeight = 0;
 
 		coefficientsPolynomNextStepLayout = new GridLayout(2, false);
 		coefficientsPolynomNextStepLayout.marginWidth = 0;
 		coefficientsPolynomNextStepLayout.marginHeight = 0;
 
 		coefficientsGroup = new Group(parent, SWT.NONE);
 		coefficientsGroup.setLayout(coefficientsGroupLayout);
 		coefficientsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
 				false));
 		coefficientsGroup
 				.setText(Messages.VerifiableSecretSharingComposite_coefficients_title);
 
 		scrolledCoefficientsGroup = new ScrolledComposite(coefficientsGroup,
 				SWT.V_SCROLL);
 		scrolledCoefficientsGroup.setExpandHorizontal(true);
 		scrolledCoefficientsGroup.setLayoutData(new RowData(200, 79));
 
 		scrolledCoefficientsGroupContent = new Composite(
 				scrolledCoefficientsGroup, SWT.NONE);
 		scrolledCoefficientsGroupContent
 				.setLayout(coefficientsPolynomNextStepLayout);
 		scrolledCoefficientsGroupContent.setLayoutData(new GridData(SWT.FILL,
 				SWT.FILL, true, false));
 
 		commitGenerateButtonLayout = new GridLayout(2, true);
 		commitGenerateButtonLayout.marginWidth = 0;
 		commitGenerateButtonLayout.marginHeight = 0;
 
 		commitGenerateButtonComposite = new Composite(coefficientsGroup,
 				SWT.NONE);
 		commitGenerateButtonComposite.setLayout(commitGenerateButtonLayout);
 		commitGenerateButtonComposite.setLayoutData(new RowData(220, -1));
 
 		commitCoefficientsButton = new Button(commitGenerateButtonComposite,
 				SWT.NONE);
 		commitCoefficientsButton
 				.setText(Messages.VerifiableSecretSharingComposite_coefficients_commit_button);
 		commitCoefficientsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
 				true, false));
 
 		commitCoefficientsButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(final SelectionEvent e) {
 				showCommitmentsGroup(true, (playersRecon));
 				vss.commitment(Integer.parseInt(primitiveRootText.getText()),
 						coefficientsInt, Integer.parseInt(moduleText.getText()));
 				for (int i = 0; i < coefficientsSpinnersCoefficients.length; i++) {
 					coefficientsTextCommitment[i].setText(String.valueOf(vss
 							.getCommitments()[i]));
 				}
 			}
 		});
 
 		generateCoefficientsButton = new Button(commitGenerateButtonComposite,
 				SWT.PUSH);
 		generateCoefficientsButton
 				.setText(Messages.VerifiableSecretSharingComposite_coefficients_generate_button);
 		generateCoefficientsButton.setLayoutData(new GridData(SWT.FILL,
 				SWT.FILL, true, false));
 		generateCoefficientsButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(final SelectionEvent e) {
 				Random randomGenerator = new Random();
 				for (int i = 1; i < playersRecon; i++) {
 					coefficientsSpinnersCoefficients[i]
 							.setSelection(randomGenerator.nextInt(Integer
 									.parseInt(moduleText.getText())));
 				}
 				for (int i = 0; i < playersRecon; i++) {
 					coefficientsInt[i] = Integer
 							.parseInt(coefficientsSpinnersCoefficients[i]
 									.getText());
 				}
 				generatePolynom();
 			}
 		});
 
 		polynomContent = new Composite(coefficientsGroup, SWT.NONE);
 		polynomContent.setLayout(coefficientsPolynomNextStepLayout);
 		polynomContent.setLayoutData(new RowData(220, -1));
 
 		polynomLabel = new Label(polynomContent, SWT.NONE);
 		polynomLabel.setText("P(x)    ");
 
 		polynomText = new Text(polynomContent, SWT.BORDER);
 		polynomText.setEnabled(false);
 		polynomText
 				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 
 		nextStepContent = new Composite(coefficientsGroup, SWT.NONE);
 		nextStepContent.setLayout(coefficientsPolynomNextStepLayout);
 		nextStepContent.setLayoutData(new RowData(220, -1));
 
 		spaceLabel = new Label(nextStepContent, SWT.NONE);
 		spaceLabel = new Label(nextStepContent, SWT.NONE);
 
 		nextStepButtonCoefficients = new Label(nextStepContent, SWT.NONE);
 		nextStepButtonCoefficients
 				.setText(Messages.VerifiableSecretSharingComposite_nextStep_button);
 		calculateShares = new Button(nextStepContent, SWT.NONE);
 		calculateShares
 				.setText(Messages.VerifiableSecretSharingComposite_coefficients_calculateShares_button);
 		calculateShares.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				false));
 		calculateShares.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(final SelectionEvent e) {
 				showSharesGroup(true, players);
 				vss.calculateShares(coefficientsInt,
 						Integer.parseInt(moduleText.getText()), players);
 				for (int i = 0; i < players; i++) {
 					shareModNTextShares[i].setText(String.valueOf(vss
 							.getSharesModP()[i]));
 					shareNTextShares[i].setText(String.valueOf(vss.getShares()[i]));
 				}
 				showReconstructionGroup(true, players);
 			}
 		});
 	}
 
 	private void showCoefficientsGroup(boolean showGroup, int coefficients) {
 		if (showGroup) {
 			coefficientsGroup.setVisible(true);
 			for (Control control : scrolledCoefficientsGroupContent
 					.getChildren()) {
 				control.dispose();
 			}
 			coefficientsLabelsCoefficients = new Label[coefficients+1];
 			coefficientsSpinnersCoefficients = new Spinner[coefficients+1];
 			coefficientsLabelsCoefficients[0] = new Label(
 					scrolledCoefficientsGroupContent, SWT.NONE);
 			coefficientsLabelsCoefficients[0].setText("a"
 					+ convertIntegerToSubscript(0) + " = s");
 			coefficientsSpinnersCoefficients[0] = new Spinner(
 					scrolledCoefficientsGroupContent, SWT.BORDER);
 			coefficientsSpinnersCoefficients[0].setLayoutData(new GridData(
 					SWT.FILL, SWT.FILL, true, false));
 			coefficientsSpinnersCoefficients[0].setEnabled(false);
 			for (int i = 1; i <= coefficients; i++) {
 				coefficientsLabelsCoefficients[i] = new Label(
 						scrolledCoefficientsGroupContent, SWT.NONE);
 				coefficientsLabelsCoefficients[i].setText("a"
 						+ convertIntegerToSubscript(i));
 
 				coefficientsSpinnersCoefficients[i] = new Spinner(
 						scrolledCoefficientsGroupContent, SWT.BORDER);
 				coefficientsSpinnersCoefficients[i].setLayoutData(new GridData(
 						SWT.FILL, SWT.FILL, true, false));
 				coefficientsSpinnersCoefficients[i].setMinimum(1);
 				coefficientsSpinnersCoefficients[i].addListener(SWT.CHANGED,
 						new Listener() {
 							@Override
 							public void handleEvent(Event event) {
 								generatePolynom();
 								for (int i = 0; i < coefficientsSpinnersCoefficients.length; i++) {
 									coefficientsInt[i] = Integer
 											.parseInt(coefficientsSpinnersCoefficients[i]
 													.getText());
 								}
 							}
 						});
 			}
 			scrolledCoefficientsGroup
 					.setContent(scrolledCoefficientsGroupContent);
 			scrolledCoefficientsGroupContent.pack();
 
 		} else {
 			coefficientsGroup.setVisible(false);
 		}
 	}
 
 	private void createCommitmentsGroup(Composite parent) {
 		commitmentsGroupLayout = new RowLayout();
 		commitmentsGroupLayout.type = SWT.VERTICAL;
 
 		commitmentsGroupGridLayout = new GridLayout(2, false);
 		commitmentsGroupGridLayout.marginWidth = 0;
 		commitmentsGroupGridLayout.marginHeight = 0;
 
 		commitmentsGroup = new Group(parent, SWT.NONE);
 		commitmentsGroup.setLayout(commitmentsGroupLayout);
 		commitmentsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
 				false));
 		commitmentsGroup
 				.setText(Messages.VerifiableSecretSharingComposite_commitments_title);
 		commitmentsGroup.setData(false);
 
 		scrolledCommitmentsGroup = new ScrolledComposite(commitmentsGroup,
 				SWT.V_SCROLL);
 		scrolledCommitmentsGroup.setExpandHorizontal(true);
 		scrolledCommitmentsGroup.setLayoutData(new RowData(150, 180));
 
 		scrolledCommitmentsGroupContent = new Composite(
 				scrolledCommitmentsGroup, SWT.NONE);
 		scrolledCommitmentsGroupContent.setLayout(commitmentsGroupGridLayout);
 		scrolledCommitmentsGroupContent.setLayoutData(new GridData(SWT.FILL,
 				SWT.FILL, true, false));
 
 		coefficientLabel = new Label(scrolledCommitmentsGroupContent, SWT.NONE);
 		coefficientLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL,
 				false, true));
 		coefficientLabel
 				.setText(Messages.VerifiableSecretSharingComposite_commitments_coefficient_subtitle);
 		coefficientLabel.setData(false);
 
 		commitmentLabel = new Label(scrolledCommitmentsGroupContent, SWT.NONE);
 		commitmentLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false,
 				true));
 		commitmentLabel
 				.setText(Messages.VerifiableSecretSharingComposite_commitments_commitment_subtitle);
 		commitmentLabel.setData(false);
 
 		seperatorData = new GridData(SWT.FILL, SWT.FILL, true, false);
 		seperatorData.horizontalSpan = 2;
 
 		horizontalSeperator = new Label(scrolledCommitmentsGroupContent,
 				SWT.SEPARATOR | SWT.HORIZONTAL);
 		horizontalSeperator.setLayoutData(seperatorData);
 		horizontalSeperator.setData(false);
 
 	}
 
 	private void showCommitmentsGroup(boolean showGroup, int commitments) {
 		if (showGroup) {
 			commitmentsGroup.setVisible(true);
 			for (Control control : scrolledCommitmentsGroupContent
 					.getChildren()) {
 
 				if (control.getData() == null) {
 					control.dispose();
 				}
 			}
 
 			coefficientsLabelsCommitment = new Label[commitments];
 			coefficientsTextCommitment = new Text[commitments];
 			for (int i = 0; i < commitments; i++) {
 				coefficientsLabelsCommitment[i] = new Label(
 						scrolledCommitmentsGroupContent, SWT.NONE);
 				coefficientsLabelsCommitment[i].setText("bla"
 						+ convertIntegerToSubscript(i));
 				coefficientsLabelsCommitment[i].setLayoutData(new GridData(
 						SWT.CENTER, SWT.FILL, true, true));
 
 				coefficientsTextCommitment[i] = new Text(
 						scrolledCommitmentsGroupContent, SWT.BORDER);
 				coefficientsTextCommitment[i].setLayoutData(new GridData(
 						SWT.FILL, SWT.FILL, true, false));
 			}
 
 			scrolledCommitmentsGroup
 					.setContent(scrolledCommitmentsGroupContent);
 			scrolledCommitmentsGroupContent.pack();
 
 		} else {
 			commitmentsGroup.setVisible(false);
 
 		}
 	}
 
 	private void createSharesGroup(Composite parent) {
 		sharesGroupLayout = new RowLayout();
 		sharesGroupLayout.type = SWT.VERTICAL;
 
 		sharesGroupGridLayout = new GridLayout(3, false);
 		sharesGroupGridLayout.marginWidth = 0;
 		sharesGroupGridLayout.marginHeight = 0;
 
 		sharesGroup = new Group(parent, SWT.NONE);
 		sharesGroup.setLayout(sharesGroupLayout);
 		sharesGroup
 				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		sharesGroup
 				.setText(Messages.VerifiableSecretSharingComposite_shares_title);
 
 		scrolledSharesGroup = new ScrolledComposite(sharesGroup, SWT.V_SCROLL);
 		scrolledSharesGroup.setExpandHorizontal(true);
 		scrolledSharesGroup.setLayoutData(new RowData(250, 180));
 
 		scrolledSharesGroupContent = new Composite(scrolledSharesGroup,
 				SWT.NONE);
 		scrolledSharesGroupContent.setLayout(sharesGroupGridLayout);
 		scrolledSharesGroupContent.setLayoutData(new GridData(SWT.FILL,
 				SWT.FILL, true, false));
 
 		indexLabel = new Label(scrolledSharesGroupContent, SWT.NONE);
 		indexLabel
 				.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true));
 		indexLabel.setText(Messages.VerifiableSecretSharingComposite_playerX
 				+ " i");
 		indexLabel.setData(false);
 
 		shareNLabel = new Label(scrolledSharesGroupContent, SWT.NONE);
 		shareNLabel
 				.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
 		shareNLabel
 				.setText(Messages.VerifiableSecretSharingComposite_shares_shareNModP_subtitle);
 		shareNLabel.setData(false);
 
 		spaceLabel = new Label(scrolledSharesGroupContent, SWT.NONE);
 		spaceLabel.setData(false);
 
 		seperatorData = new GridData(SWT.FILL, SWT.FILL, true, false);
 		seperatorData.horizontalSpan = 4;
 
 		horizontalSeperator = new Label(scrolledSharesGroupContent,
 				SWT.SEPARATOR | SWT.HORIZONTAL);
 		horizontalSeperator.setLayoutData(seperatorData);
 		horizontalSeperator.setData(false);
 
 	}
 
 	private void showSharesGroup(boolean showGroup, int shares) {
 		if (showGroup) {
 			sharesGroup.setVisible(true);
 			for (Control control : scrolledSharesGroupContent.getChildren()) {
				if (control.getData() == null) {
 					control.dispose();
 				}
 			}
 			playerLabelShares = new Label[shares];
 			indexLabelShares = new Label[shares];
 			shareNCompositeShares = new Composite[shares];
 			shareNTextShares = new Text[shares];
 			isModShares = new Label[shares];
 			shareModNTextShares = new Text[shares];
 			checkButtonShares = new Button[shares];
 			playerID = new int[shares];
 
 			shareModNRowLayout = new RowLayout();
 			shareModNRowLayout.type = SWT.HORIZONTAL;
 			shareModNRowLayout.wrap = false;
 			for (int i = 0; i < shares; i++) {
 				playerID[i] = i + 1;
 				indexLabelShares[i] = new Label(scrolledSharesGroupContent,
 						SWT.NONE);
 				indexLabelShares[i]
 						.setText(Messages.VerifiableSecretSharingComposite_playerX
 								+ " " + (i + 1));
 				indexLabelShares[i].setLayoutData(new GridData(SWT.CENTER,
 						SWT.FILL, true, true));
 
 				shareNCompositeShares[i] = new Composite(
 						scrolledSharesGroupContent, SWT.NONE);
 				shareNCompositeShares[i].setLayoutData(new GridData(SWT.CENTER,
 						SWT.FILL, false, false));
 				shareNCompositeShares[i].setLayout(shareModNRowLayout);
 
 				shareNTextShares[i] = new Text(shareNCompositeShares[i],
 						SWT.BORDER);
 				shareNTextShares[i].setLayoutData(new RowData(50, -1));
 
 				isModShares[i] = new Label(shareNCompositeShares[i], SWT.NONE);
 				isModShares[i].setText("\u2261");
 
 				shareModNTextShares[i] = new Text(shareNCompositeShares[i],
 						SWT.BORDER);
 				shareModNTextShares[i].setLayoutData(new RowData(30, -1));
 
 				checkButtonShares[i] = new Button(scrolledSharesGroupContent,
 						SWT.NONE);
 				checkButtonShares[i].setLayoutData(new GridData(SWT.FILL,
 						SWT.FILL, true, true));
 				checkButtonShares[i]
 						.setText(Messages.VerifiableSecretSharingComposite_shares_check_button);
 				checkButtonShares[i].setData(i);
 				checkButtonShares[i]
 						.addSelectionListener(new SelectionAdapter() {
 							@Override
 							public void widgetSelected(SelectionEvent e) {
 								if (vss.check(Integer
 										.parseInt(primitiveRootText.getText()),
 										Integer.parseInt(moduleText.getText()),
 										playerID[(Integer) e.widget.getData()]) == true) {
 									shareModNTextShares[(Integer) e.widget
 											.getData()].setBackground(GREEN);
 								} else {
 									shareModNTextShares[(Integer) e.widget
 											.getData()].setBackground(RED);
 								}
 							}
 						});
 			}
 
 			scrolledSharesGroup.setContent(scrolledSharesGroupContent);
 			scrolledSharesGroupContent.pack();
 		} else {
 			sharesGroup.setVisible(false);
 		}
 	}
 
 	private void createReconstructionGroup(Composite parent) {
 		reconstructionGroupLayout = new RowLayout();
 		reconstructionGroupLayout.type = SWT.VERTICAL;
 
 		reconstructionGroupGridLayout = new GridLayout(2, false);
 		reconstructionGroupGridLayout.marginWidth = 0;
 		reconstructionGroupGridLayout.marginHeight = 0;
 
 		reconstructionGroup = new Group(parent, SWT.NONE);
 		reconstructionGroup.setLayout(reconstructionGroupLayout);
 		reconstructionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
 				false, false));
 		reconstructionGroup
 				.setText(Messages.VerifiableSecretSharingComposite_reconstruction_title);
 
 		scrolledReconstructionGroup = new ScrolledComposite(
 				reconstructionGroup, SWT.V_SCROLL);
 		scrolledReconstructionGroup.setExpandHorizontal(true);
 		scrolledReconstructionGroup.setLayoutData(new RowData(100, 145));
 
 		scrolledReconstructionGroupContent = new Composite(
 				scrolledReconstructionGroup, SWT.NONE);
 		scrolledReconstructionGroupContent
 				.setLayout(reconstructionGroupGridLayout);
 		scrolledReconstructionGroupContent.setLayoutData(new GridData(SWT.FILL,
 				SWT.FILL, true, false));
 
 		reconstructButton = new Button(reconstructionGroup, SWT.NONE);
 		reconstructButton
 				.setText(Messages.VerifiableSecretSharingComposite_reconstruction_reconstruct_button);
 		reconstructButton.setLayoutData(new RowData(120, -1));
 		reconstructButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				/* hier koennte ihre werbung stehen */
 				/* oder unsere reconstruct funktion */
 			}
 		});
 	}
 
 	private void showReconstructionGroup(boolean showGroup, int player) {
 		if (showGroup) {
 			reconstructionGroup.setVisible(true);
 			for (Control control : scrolledReconstructionGroupContent
 					.getChildren()) {
 				control.dispose();
 			}
 			playerLabelReconstructions = new Label[player];
 			playerCheckboxReconstructions = new Button[player];
 
 			for (int i = 0; i < player; i++) {
 				playerLabelReconstructions[i] = new Label(
 						scrolledReconstructionGroupContent, SWT.NONE);
 				playerLabelReconstructions[i]
 						.setText(Messages.VerifiableSecretSharingComposite_playerX
 								+ " " + (i + 1));
 				playerLabelReconstructions[i].setLayoutData(new GridData(
 						SWT.LEFT, SWT.FILL, true, true));
 
 				playerCheckboxReconstructions[i] = new Button(
 						scrolledReconstructionGroupContent, SWT.CHECK);
 				playerCheckboxReconstructions[i].setLayoutData(new GridData(
 						SWT.CENTER, SWT.FILL, true, false));
 			}
 
 			scrolledReconstructionGroup
 					.setContent(scrolledReconstructionGroupContent);
 			scrolledReconstructionGroupContent.pack();
 		} else {
 			reconstructionGroup.setVisible(false);
 		}
 	}
 
 	private void createDescriptionGroup(Group parent) {
 		descriptionGroup = new Group(parent, SWT.NONE);
 		descriptionGroup.setLayout(new GridLayout(2, false));
 		descriptionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				true));
 		descriptionGroup
 				.setText(Messages.VerifiableSecretSharingComposite_description_title);
 	}
 
 	private String convertIntegerToSubscript(int number) {
 		String result = "";
 		String numberString = number + "";
 		for (int i = 0; i < numberString.length(); i++) {
 			switch (numberString.charAt(i)) {
 			case '0':
 				result += "\u2080";
 				break;
 			case '1':
 				result += "\u2081";
 				break;
 			case '2':
 				result += "\u2082";
 				break;
 			case '3':
 				result += "\u2083";
 				break;
 			case '4':
 				result += "\u2084";
 				break;
 			case '5':
 				result += "\u2085";
 				break;
 			case '6':
 				result += "\u2086";
 				break;
 			case '7':
 				result += "\u2087";
 				break;
 			case '8':
 				result += "\u2088";
 				break;
 			case '9':
 				result += "\u2089";
 				break;
 			default:
 				result += "";
 			}
 		}
 		return result;
 	}
 
 	private String convertIntegerToSuperscript(int number) {
 		String result = "";
 		String numberString = number + "";
 		for (int i = 0; i < numberString.length(); i++) {
 			switch (numberString.charAt(i)) {
 			case '0':
 				result += "\u2070";
 				break;
 			case '1':
 				result += "\u00B9";
 				break;
 			case '2':
 				result += "\u00B2";
 				break;
 			case '3':
 				result += "\u00B3";
 				break;
 			case '4':
 				result += "\u2074";
 				break;
 			case '5':
 				result += "\u2075";
 				break;
 			case '6':
 				result += "\u2076";
 				break;
 			case '7':
 				result += "\u2077";
 				break;
 			case '8':
 				result += "\u2078";
 				break;
 			case '9':
 				result += "\u2079";
 				break;
 			default:
 				result += "";
 			}
 		}
 		return result; // ich bin ein kommentar. was bist denn du?
 	}
 
 	private boolean isSubgroup(String proot, String p) {
 		int pInt = Integer.parseInt(p);
 		int prootInt = Integer.parseInt(proot);
 		for (int i = 2; i < pInt; i++) {
 			int j = i, o = 1;
 			do {
 				o++;
 				j = j * i % pInt;
 			} while (j != 1);
 			if (o == (pInt - 1) && prootInt == i) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void generatePolynom() {
 		String polynom = coefficientsSpinnersCoefficients[0].getText() + " + ";
 
 		for (int i = 1; i < playersRecon; i++) {
 			polynom += coefficientsSpinnersCoefficients[i].getText() + "x"
 					+ convertIntegerToSuperscript(i) + " + ";
 		}
 
 		polynomText.setText(polynom.substring(0, polynom.length() - 3));
 
 	}
 
 }
