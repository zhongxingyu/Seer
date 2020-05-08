 /*************************************************************************
  * 
  *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
  *  
  *  The Contents of this file are made available subject to
  *  the terms of European Union Public License (EUPL) version 1.1
  *  as published by the European Community.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the EUPL.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  EUPL for more details.
  *
  *  You should have received a copy of the EUPL along with this
  *  program.  If not, see:
  *  https://www.osor.eu/eupl, http://ec.europa.eu/idabc/eupl.
  *
  ************************************************************************/
 
 package it.plio.ext.oxsit.ooo.ui;
 
 import java.util.LinkedList;
 
 import it.plio.ext.oxsit.ooo.GlobConstant;
 import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
 import it.plio.ext.oxsit.ooo.ui.BasicDialog;
 import it.plio.ext.oxsit.ooo.ui.ControlDims;
 import it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor.TreeNodeType;
 import it.plio.ext.oxsit.ooo.ui.test.SignatureStateInDocumentKOCertSignature;
 import it.plio.ext.oxsit.ooo.ui.test.SignatureStateInDocumentKODocument;
 import it.plio.ext.oxsit.ooo.ui.test.SignatureStateInDocumentKODocumentAndSignature;
 import it.plio.ext.oxsit.ooo.ui.test.SignatureStateInDocumentKOSignature2;
 import it.plio.ext.oxsit.ooo.ui.test.SignatureStateInDocumentOK;
 
 import com.sun.star.awt.ActionEvent;
 import com.sun.star.awt.PushButtonType;
 import com.sun.star.awt.XActionListener;
 import com.sun.star.awt.XControl;
 import com.sun.star.awt.XControlModel;
 import com.sun.star.awt.XDialog;
 import com.sun.star.awt.XItemListener;
 import com.sun.star.awt.XMouseListener;
 import com.sun.star.awt.XTextComponent;
 import com.sun.star.awt.XWindow;
 import com.sun.star.awt.XWindowPeer;
 import com.sun.star.awt.tree.XMutableTreeDataModel;
 import com.sun.star.awt.tree.XMutableTreeNode;
 import com.sun.star.awt.tree.XTreeControl;
 import com.sun.star.awt.tree.XTreeExpansionListener;
 import com.sun.star.beans.PropertyVetoException;
 import com.sun.star.beans.UnknownPropertyException;
 import com.sun.star.beans.XMultiPropertySet;
 import com.sun.star.beans.XPropertySet;
 import com.sun.star.deployment.PackageInformationProvider;
 import com.sun.star.deployment.XPackageInformationProvider;
 import com.sun.star.frame.XFrame;
 import com.sun.star.lang.IllegalArgumentException;
 import com.sun.star.lang.WrappedTargetException;
 import com.sun.star.lang.XComponent;
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.script.BasicErrorException;
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 import com.sun.star.view.XSelectionChangeListener;
 
 /**
  * @author beppe
  *
  */
 public class DialogCertificateTree extends BasicDialog implements
 		XActionListener, XMouseListener, XItemListener, XTreeExpansionListener, XSelectionChangeListener {
 
 	private static final String DLG_CERT_TREE = "DialogCertificateTree";
 
 	private static final String sTree = "certtree";
 	private static final String sVerify = "verifyb";
 	private static final String sAdd = "addb";
 	private static final String sRemove = "remob";
 	private static final String sCountSig = "countsigb";
 
 	//graphic indications
 	private String sSignatureOK; //signature ok
 	private String sSignatureNotValidated; //signature ok, but certificate not valid
 	private String sSignatureBroken; //signature does not mach content: document changed after signature
 	private String sSignatureInvalid; //signature cannot be validated
 	private String sSignatureAdding;
 	private String sSignatureRemoving;
 
 
 	private String sCertificateValid = null; //
 	private String sCertificateNotValidated = null; //
 	
 	private String	sCertificateElementWarning = null;
 
 	private XTreeControl m_xTreeControl = null;
 
 	private static final String	m_sDispElemsName	= "dispelems";  // the control general, with descriptive text in it
 	// the following two fields are needed to be able to change
 	// the font at run-time
 	private Object				m_xDisplElementModel;				// the service "com.sun.star.awt.UnoControlEditModel"
 	@SuppressWarnings("unused")
 	private XTextComponent		m_xDisplElement;					// the XTextComponent interface of the control
 																	// of the above model
 	private XMutableTreeNode m_aTheOldNode = null;
 
 	private String				m_sBtnOKLabel;
 	private String				m_sBtn_CancelLabel;
 	private String 				m_sBtn_Verify;
 	private String 				m_sBtn_AddCertLabel;
 	private String 				m_sDlgListCertTitle;	
 	private String 				m_sFt_Hint_Doc;
 	private String 				m_sBtn_RemoveCertLabel;
 	private String				m_sBtn_AddCountCertLabel;
 	private String				m_sBtn_CreateReport;
 	
 	private String 				sCertificateElementError;
 	private String 				sCertificateElementBroken;
 
 	private String sSignatureInvalid2;
 
 	private static final String sEmptyText = "notextcontrol";		//the control without text
 	private static final String sEmptyTextLine = "notextcontrolL";		//the 1st line superimposed to the empty text contro
 //	public static final int	NUMBER_OF_DISPLAYED_TEST_LINES = 14;
 	
 	/**
 	 * Note on the display:
 	 * two ways on right pane:
 	 * - a six line text on a white background for generals
 	 * - a multiline text for internal elements.
 	 * 
 	 * tree structure:
 	 *  tree ()
 	 *      node ()
 	 *      	
 	 * 
 	 * @param frame
 	 * @param context
 	 * @param _xmcf
 	 */
 	public DialogCertificateTree(XFrame frame, XComponentContext context,
 			XMultiComponentFactory _xmcf) {
 		super(frame, context, _xmcf);
 		// TODO Auto-generated constructor stub
 		m_logger.enableLogging();
 		m_logger.ctor();
 //fill string for graphics
 		XPackageInformationProvider xPkgInfo = PackageInformationProvider.get( context );
 		if(xPkgInfo != null) {
 			String sLoc = xPkgInfo.getPackageLocation( GlobConstant.m_sEXTENSION_IDENTIFIER );
 			if(sLoc != null){
 				String aSize = "_26.png"; //for large icons toolbar
 //				String aSize = "_16.png"; //for small icons toolbar
 				String m_imagesUrl = sLoc + "/images";
 //main, depends from application, for now. To be changed
 				//TODO change to a name not depending from the application
 				sSignatureOK = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_OK+aSize; //signature ok
 				sSignatureNotValidated = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_WARNING+aSize; //signature ok, but certificate not valid
 				sSignatureBroken = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_INVALID+aSize; //signature does not mach content: document changed after signature
 				sSignatureInvalid = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_BROKEN2+aSize; //
 				sSignatureInvalid2 = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_BROKEN+aSize; //
 				sSignatureAdding = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_ADDING+aSize; //
 				sSignatureRemoving = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_REMOVING+aSize; //
 
 				sCertificateValid = m_imagesUrl + GlobConstant.m_nCERTIFICATE+aSize;
 				sCertificateNotValidated = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_INVALID +aSize;
 				sCertificateElementWarning = m_imagesUrl + "/"+GlobConstant.m_nCERT_ELEM_WARNING +aSize;
 				sCertificateElementError = m_imagesUrl + "/"+GlobConstant.m_nCERT_ELEM_INVALID +aSize;
 				sCertificateElementBroken = m_imagesUrl + "/"+GlobConstant.m_nCERT_ELEM_BROKEN +aSize;
 			}
 			else
 				printlnName("no package location !");
 		}
 		else
 			printlnName("No pkginfo!");
 		fillLocalizedString();
 		// the next value should be read from configuration
 //		CertifTreeDlgDims.setDialogSize(0, 0); //to test
 		CertifTreeDlgDims.setDialogSize(300, 100, 0);
 	}
 
 	/**
 	 * prepare the strings for the dialogs
 	 */
 	private void fillLocalizedString() {
 		MessageConfigurationAccess m_aRegAcc = null;
 		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);
 
 		try {
 			m_sBtnOKLabel = m_aRegAcc.getStringFromRegistry( "id_ok" );			
 			m_sBtn_CancelLabel = m_aRegAcc.getStringFromRegistry( "id_cancel" );
 			m_sBtn_Verify = m_aRegAcc.getStringFromRegistry( "id_pb_verif_sign" );
 			m_sBtn_AddCertLabel = m_aRegAcc.getStringFromRegistry( "id_pb_add_cert" );
 			m_sBtn_RemoveCertLabel = m_aRegAcc.getStringFromRegistry( "id_pb_rem_cert" );
 			m_sBtn_AddCountCertLabel = m_aRegAcc.getStringFromRegistry( "id_pb_count_sign" );
 			m_sDlgListCertTitle = m_aRegAcc.getStringFromRegistry( "id_title_cert_tree" );
 			m_sFt_Hint_Doc = m_aRegAcc.getStringFromRegistry( "id_title_cert_treew" );
 			m_sBtn_CreateReport = m_aRegAcc.getStringFromRegistry( "id_pb_cert_report" );
 		} catch (com.sun.star.uno.Exception e) {
 			e.printStackTrace();
 		}
 		m_aRegAcc.dispose();	
 	}
 
 	public void initialize(int _nPosX, int _nPosY ) throws BasicErrorException {
 		initialize(null, _nPosX, _nPosY );
 	}
 
 	/** Inizializza la finestra di dialogo
 	 *  per la visualizzazione strutturata delle firme presenti
 	 * @throws BasicErrorException 
 	 * 
 	 */
 	public void initialize(XWindowPeer _xParentWindow, int posX, int posY) throws BasicErrorException {
 		try {
 			super.initialize(DLG_CERT_TREE, m_sDlgListCertTitle, CertifTreeDlgDims.dsHeigh(), CertifTreeDlgDims.dsWidth(), posX, posY);
 
 /*	        XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
 	                XPropertySet.class, m_xDialogControl.getModel());
 
 	        if (xProp != null)
 	        	xProp.setPropertyValue(new String("BackgroundColor"),
 	        			new Integer(ControlDims.DLG_ABOUT_BACKG_COLOR));*/
 
 			insertFixedText(this,
 					CertifTreeDlgDims.DS_COL_1(),
 					CertifTreeDlgDims.DS_ROW_0(), 
 					CertifTreeDlgDims.dsWidth(), 
 					0,
 					m_sFt_Hint_Doc
 					);
 	//inserts the control elements needed to display properties
 	//multiline text control used as a light yellow background
 			//multiline text control for details
 			Object oEdit = insertEditFieldModel(this, this,
 					CertifTreeDlgDims.dsTextFieldColumn(),
 					CertifTreeDlgDims.DS_ROW_1(),
 					CertifTreeDlgDims.DS_ROW_3()-CertifTreeDlgDims.DS_ROW_1(),
 					CertifTreeDlgDims.dsTextFieldWith(),
 					2,
 					"", sEmptyText, true, true, false, false);
 
 	//now change its background color
 			XPropertySet xPSet = (XPropertySet) UnoRuntime
 						.queryInterface( XPropertySet.class, oEdit );
 			xPSet.setPropertyValue(new String("BackgroundColor"), new Integer(ControlDims.DLG_CERT_TREE_BACKG_COLOR));	
 			//insert the fixed text lines over the above mentioned element
 			insertDisplayLinesOfText();
 
 	//multiline text control for details
 			m_xDisplElementModel = insertEditFieldModel(this, this,
 					CertifTreeDlgDims.dsTextFieldColumn(),
 					CertifTreeDlgDims.DS_ROW_1(),
 					CertifTreeDlgDims.DS_ROW_3()-CertifTreeDlgDims.DS_ROW_1(),
 					CertifTreeDlgDims.dsTextFieldWith(),
 					2,
 					"", m_sDispElemsName, true, true, true, true);
 
 			xPSet = (XPropertySet) UnoRuntime
 						.queryInterface( XPropertySet.class, m_xDisplElementModel );
 			xPSet.setPropertyValue(new String("BackgroundColor"), new Integer(ControlDims.DLG_CERT_TREE_BACKG_COLOR));	
 
 	//Insert the tree control		
 			m_xTreeControl = insertTreeControl(this,
 					CertifTreeDlgDims.DS_COL_0(), 
 					CertifTreeDlgDims.DS_ROW_1(), 
 					CertifTreeDlgDims.DS_ROW_3()-CertifTreeDlgDims.DS_ROW_1(),
 					CertifTreeDlgDims.dsTreeControlWith(), //CertifTreeDlgDims.DS_COL_4() - CertifTreeDlgDims.DS_COL_0(),
 					sTree,
 					m_sFt_Hint_Doc, 20);
 			
 			// ora recupera il textcomponent
 			XControl xTFControl = m_xDlgContainer.getControl(m_sDispElemsName);
 			// add a textlistener that is notified on each change of the
 			// controlvalue...
 			m_xDisplElement = (XTextComponent) UnoRuntime.queryInterface(
 					XTextComponent.class, xTFControl);
 	
 			insertButton(this,
 					CertifTreeDlgDims.DS_COL_PB2(),
 //					CertifTreeDlgDims.DS_COL_1()+CertifTreeDlgDims.DS_BTNWIDTH_1()/2,
 					CertifTreeDlgDims.DS_ROW_4(),
 					CertifTreeDlgDims.dsBtnWidthCertTree(),
 					sVerify,
 					m_sBtn_Verify,
 					(short) PushButtonType.STANDARD_value);
 	
 			insertButton(this,
 					CertifTreeDlgDims.DS_COL_PB3(),
 					CertifTreeDlgDims.DS_ROW_4(),
 					CertifTreeDlgDims.dsBtnWidthCertTree(),
 					sAdd,
 					m_sBtn_AddCertLabel,
 					(short) PushButtonType.STANDARD_value);
 	
 			insertButton(this,
 					CertifTreeDlgDims.DS_COL_PB4(),
 					CertifTreeDlgDims.DS_ROW_4(),
 					CertifTreeDlgDims.dsBtnWidthCertTree(),
 					sRemove,
 					m_sBtn_RemoveCertLabel,
 					(short) PushButtonType.STANDARD_value);
 			
 /*			insertButton(this,
 					CertifTreeDlgDims.DS_COL_PB4(),
 					CertifTreeDlgDims.DS_ROW_4(),
 					CertifTreeDlgDims.dsBtnWidthCertTree(),
 					sCountSig,
 					m_sBtn_AddCountCertLabel,
 					(short) PushButtonType.STANDARD_value);*/
 			
 			insertButton(this,
 					CertifTreeDlgDims.DS_COL_PB5(),
 					CertifTreeDlgDims.DS_ROW_4(),
 //					CertifTreeDlgDims.DS_COL_8()-CertifTreeDlgDims.DS_COL_7(),
 					CertifTreeDlgDims.dsBtnWidthCertTree(),
 					"sprint",
 					m_sBtn_CreateReport,
 					(short) PushButtonType.STANDARD_value);
 	
 			insertHorizontalFixedLine(
 					0, 
 					CertifTreeDlgDims.DLGS_BOTTOM_FL_Y(CertifTreeDlgDims.dsHeigh()), 
 					CertifTreeDlgDims.dsWidth(), "");		
 	
 	//cancel button
 			insertButton(this,
 					CertifTreeDlgDims.DLGS_BOTTOM_HELP_X(CertifTreeDlgDims.dsWidth()),
 					CertifTreeDlgDims.DLGS_BOTTOM_BTN_Y(CertifTreeDlgDims.dsHeigh()),
 					ControlDims.RSC_CD_PUSHBUTTON_WIDTH,
 					"cancb",
 					m_sBtn_CancelLabel,
 					(short) PushButtonType.CANCEL_value);
 	// ok button
 			insertButton(this,
 					CertifTreeDlgDims.DLGS_BOTTOM_CANCEL_X(CertifTreeDlgDims.dsWidth()),
 					CertifTreeDlgDims.DLGS_BOTTOM_BTN_Y(CertifTreeDlgDims.dsHeigh()),
 					ControlDims.RSC_CD_PUSHBUTTON_WIDTH,
 					"okb",
 					m_sBtnOKLabel,
 					(short) PushButtonType.OK_value);
 	
 			xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, super.m_xDialogControl);		
 			createWindowPeer();
 		center();
 		} catch (UnknownPropertyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (PropertyVetoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (WrappedTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void insertDisplayLinesOfText() {
 		//now inserts the fixed text lines over the above mentioned element
 		for(int i = 0; i < SignatureStateInDocument.m_nMAXIMUM_FIELDS; i++) {
 			insertFixedText(this,
 					CertifTreeDlgDims.TEXT_0X(),
 					CertifTreeDlgDims.TEXT_L0Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT*i,
 					CertifTreeDlgDims.dsWidth()-CertifTreeDlgDims.TEXT_0X()-ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT,
 					0,
 					"checkit"+i, // a dummy text
 					sEmptyTextLine+i
 					);
 		}
 	}
 	@Override
 	public void actionPerformed(ActionEvent rEvent) {
 		// TODO Auto-generated method stub
 		try {
 			// get the control that has fired the event,
 			XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class,
 					rEvent.Source);
 			XControlModel xControlModel = xControl.getModel();
 			XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(
 					XPropertySet.class, xControlModel);
 			String sName = (String) xPSet.getPropertyValue("Name");
 			// just in case the listener has been added to several controls,
 			// we make sure we refer to the right one
 			System.out.println("action: "+sName);
 			if (sName.equals("???")) {
 				System.out.println("Vai alla pagina dei dettagli");
 				// Helper.setUnoPropertyValue(m_xMSFDialogModel, "Step", new
 				// Integer(1));
 			} else {
 				System.out.println("Activated: " + sName);
 			}
 		} catch (com.sun.star.uno.Exception ex) {
 			/*
 			 * perform individual exception handling here. Possible exception
 			 * types are: com.sun.star.lang.WrappedTargetException,
 			 * com.sun.star.beans.UnknownPropertyException,
 			 * com.sun.star.uno.Exception
 			 */
 			ex.printStackTrace(System.out);
 		}
 
 		
 	}
 
 	@Override
 	public short executeDialog() throws BasicErrorException {
 		// TODO Auto-generated method stub
 		return super.executeDialog();
 	}
 
 	public XTreeControl insertTreeControl(XSelectionChangeListener _xActionListener,
 			int _nPosX,
 			int _nPosY,
 			int _nHeight,
 			int _nWidth,
 			String _sName,
 			String _sLabel,
 			int _nStep) {
 
 		XTreeControl xTree = null;
 
 		// create a controlmodel at the multiservicefactory of the dialog
 		// model...
 		try {
 //			Object oTreeModel = m_xMSFDialogModel.createInstance( "com.sun.star.awt.tree.MutableTreeDataModel" );
 			Object oTreeDataModel = m_xMCF.createInstanceWithContext("com.sun.star.awt.tree.MutableTreeDataModel", m_xContext);
 			if(oTreeDataModel == null) {
 				printlnName("the com.sun.star.awt.tree.MutableTreeDataModel wasn't created!");
 				return null;
 			}
 
 			XMutableTreeDataModel xTreeDataModel = (XMutableTreeDataModel)UnoRuntime.queryInterface( XMutableTreeDataModel.class, oTreeDataModel );
 			if(xTreeDataModel == null) {
 				printlnName("the XMutableTreeDataModel not available!");
 				return null;
 			}
 
 			XMutableTreeNode xaNode = xTreeDataModel.createNode(_sLabel, true);
 			if(xaNode == null) {
 				printlnName("the Node not available!");
 				return null;
 			}
 			xTreeDataModel.setRoot(xaNode);
 
 //insert dummy certificates
 			// TEST:
 			SignatureStateInDocument aSignState = new SignatureStateInDocumentOK("Giacomo", "Verdi");
 //contruct a certificate
 			addDummySignatureState(xTreeDataModel, xaNode, aSignState,sSignatureOK);
 
 			aSignState = new SignatureStateInDocumentKOCertSignature("John","Doe");// add a warning on certification path
 			addDummySignatureStateKOCertPath(xTreeDataModel, xaNode, aSignState,sSignatureNotValidated);
 
 			aSignState = new SignatureStateInDocumentKOSignature2();			
 			addDummySignatureStateKOCertPath(xTreeDataModel, xaNode, aSignState,sSignatureInvalid);
 
 			aSignState = new SignatureStateInDocumentOK("Lorenzo", "Verdi");
 			addDummySignatureStateKOExtenCrit(xTreeDataModel, xaNode, aSignState,sSignatureInvalid2); // add an error on date
 
 			aSignState = new SignatureStateInDocumentKODocumentAndSignature();
 			addDummySignatureState(xTreeDataModel, xaNode, aSignState,sSignatureBroken);//add an error on Extension 
 
 			aSignState = new SignatureStateInDocumentOK("Vittorio", "Manzi");
 			addDummySignatureState(xTreeDataModel, xaNode, aSignState,sSignatureAdding);
 
 			aSignState = new SignatureStateInDocumentOK("Loredana", "Bianchi");
 			addDummySignatureState(xTreeDataModel, xaNode, aSignState,sSignatureRemoving);
 
 //now create the TreeControlModel and add it to the dialog
 			Object oTreeModel = m_xMSFDialogModel.createInstance( "com.sun.star.awt.tree.TreeControlModel" );
 			if(oTreeModel == null) {
 				printlnName("the oTreeModel not available!");
 				return null;
 			}
 			XMultiPropertySet xTreeMPSet = (XMultiPropertySet) UnoRuntime
 								.queryInterface( XMultiPropertySet.class, oTreeModel );
 			if(xTreeMPSet == null ) {
 				printlnName("no XMultiPropertySet");
 				return null;
 			}
 
 //			Utilities.showProperties(this, xTreeMPSet);
 			// Set the properties at the model - keep in mind to pass the
 			// property names in alphabetical order!
 			xTreeMPSet.setPropertyValues( new String[] {
 					"BackgroundColor",
 					"DataModel",
 					"Editable",
 					"Height", 
 //			"Label", 
 					"Name",
 					"PositionX", 
 					"PositionY", 
 					"RootDisplayed",
 //					"Step",
 					"SelectionType",
 					"Width"
 					},
 					new Object[] {
 					new Integer( ControlDims.DLG_CERT_TREE_BACKG_COLOR ),
 					oTreeDataModel,
 					new Boolean( true ),
 					new Integer( _nHeight ),
 //			_sLabel,
 					_sName,
 					new Integer( _nPosX ),
 					new Integer( _nPosY ),
 					new Boolean( false ),
 	//				new Integer( _nStep ),
 					new Integer(com.sun.star.view.SelectionType.SINGLE_value),
 					new Integer( _nWidth )					
 					} );
 			
 			// add the model to the NameContainer of the dialog model
 			m_xDlgModelNameContainer.insertByName( _sName, oTreeModel );
 			XControl xTreeControl = m_xDlgContainer.getControl( _sName );
 			
 			xTree = (XTreeControl) UnoRuntime.queryInterface( XTreeControl.class, xTreeControl );
 			// An ActionListener will be notified on the activation of the
 			// button...
 //			xTree.addActionListener( _xActionListener );
 			xTree.addSelectionChangeListener(_xActionListener);
 
 		} catch (com.sun.star.uno.Exception ex) {
 			// TODO Auto-generated catch block
 			ex.printStackTrace( System.out );
 		}
 		return xTree;
 	}
 
 	private void addLinesDisplayElement(TreeNodeDescriptor aDesc) {
 		//get the list for visual elements needed for this node
 		LinkedList<XControl> aList = aDesc.getList();
 		XControl xTFControl = m_xDlgContainer.getControl( sEmptyText );
 		aList.add(xTFControl);
 		//...and the descriptive text controls
 		for(int i=0; i < SignatureStateInDocument.m_nMAXIMUM_FIELDS; i++ ) {
 			xTFControl = m_xDlgContainer.getControl( sEmptyTextLine+i );
 			aList.add(xTFControl);		
 		}
 	}
 
 	private void addMultiLineTextDisplayElement(TreeNodeDescriptor aDesc) {		
 		// a signature type, add the fillable text control
 		XControl xTFControl = m_xDlgContainer.getControl( m_sDispElemsName );
 		LinkedList<XControl> aList = aDesc.getList();			
 		aList.add(xTFControl);
 	}
 
 /**
  * adds a dummy certificate starting from the provided node
  */
 	public XMutableTreeNode addDummySignatureState(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode aStartNode,
 			SignatureStateInDocument aCert, String sGraphic) {
 		XMutableTreeNode aretValue = null;
 		try {
 			XMutableTreeNode xaCNode = xTreeDataModel.createNode(aCert.getUser(), true);
 			if(sGraphic != null)
 				xaCNode.setNodeGraphicURL(sGraphic);
 
 			TreeNodeDescriptor aDesc = new TreeNodeDescriptor(TreeNodeDescriptor.TreeNodeType.SIGNATURE,aCert);
 			addLinesDisplayElement(aDesc);
 			xaCNode.setDataValue(aDesc);
 
 			aStartNode.appendChild(xaCNode);
 			aretValue = xaCNode;
 			if(sCertificateValid != null)
 				addDummyCertificateFields(xTreeDataModel, xaCNode, aCert,
 						(aCert.isCertificateValid())?sCertificateValid : sCertificateNotValidated);
 
 // add the certification path			
 			XMutableTreeNode xaDNode;
 			xaDNode = xTreeDataModel.createNode("Percorso di certificazione", true);
 			aDesc = new TreeNodeDescriptor(TreeNodeDescriptor.TreeNodeType.CERTIFICATION_PATH,aCert);
 			xaDNode.setDataValue(aDesc);			
 			xaCNode.appendChild(xaDNode);
 
 			//add fake certificate to the certification path
 			SignatureStateInDocument aCert2 = new CertificateDataCA();
 			addDummyPathCertificates(xTreeDataModel, xaDNode, aCert2);
 
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return aretValue;
 	}
 
 	//FIXME: just to simulate the error, shoul be removed after the fact
 	private XMutableTreeNode addDummySignatureStateKOCertPath(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode aStartNode,
 			SignatureStateInDocument aCert, String sGraphic) {
 		XMutableTreeNode aretValue = null;
 		try {
 			XMutableTreeNode xaCNode = xTreeDataModel.createNode(aCert.getUser(), true);
 			if(sGraphic != null)
 				xaCNode.setNodeGraphicURL(sGraphic);
 
 			TreeNodeDescriptor aDesc = new TreeNodeDescriptor(TreeNodeDescriptor.TreeNodeType.SIGNATURE,aCert);
 			addLinesDisplayElement(aDesc);
 			xaCNode.setDataValue(aDesc);
 
 			aStartNode.appendChild(xaCNode);
 			aretValue = xaCNode;
 			if(sCertificateValid != null)
 				addDummyCertificateFields(xTreeDataModel, xaCNode, aCert,
 						(aCert.isCertificateValid())?sCertificateValid : sCertificateNotValidated);
 
 // add the certification path			
 			XMutableTreeNode xaDNode;
 			xaDNode = xTreeDataModel.createNode("Percorso di certificazione", true);
 			aDesc = new TreeNodeDescriptor(TreeNodeDescriptor.TreeNodeType.CERTIFICATION_PATH,aCert);
 // add the string displaying the graphic for warning signal			sCertificateElementWarning
 			if(sCertificateElementWarning != null)
 				xaDNode.setNodeGraphicURL(sCertificateElementWarning);
 
 			xaDNode.setDataValue(aDesc);			
 			xaCNode.appendChild(xaDNode);
 
 			//add fake certificate to the certification path
 			SignatureStateInDocument aCert2 = new CertificateDataCA();
 			addDummyPathCertificates(xTreeDataModel, xaDNode, aCert2);
 
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return aretValue;
 	}
 	
 	//FIXME: just to simulate the error, should be removed after the fact
 	private XMutableTreeNode addDummySignatureStateKOExtenCrit(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode aStartNode,
 			SignatureStateInDocument aCert, String sGraphic) {
 		XMutableTreeNode aretValue = null;
 		try {
 			XMutableTreeNode xaCNode = xTreeDataModel.createNode(aCert.getUser(), true);
 			if(sGraphic != null)
 				xaCNode.setNodeGraphicURL(sGraphic);
 
 			TreeNodeDescriptor aDesc = new TreeNodeDescriptor(TreeNodeDescriptor.TreeNodeType.SIGNATURE,aCert);
 			addLinesDisplayElement(aDesc);
 			xaCNode.setDataValue(aDesc);
 
 			aStartNode.appendChild(xaCNode);
 			aretValue = xaCNode;
 			if(sCertificateValid != null)
 				addDummyCertificateFieldsKOExtCrit(xTreeDataModel, xaCNode, aCert,
 						(aCert.isCertificateValid())?sCertificateValid : sCertificateNotValidated);
 
 // add the certification path			
 			XMutableTreeNode xaDNode;
 			xaDNode = xTreeDataModel.createNode("Percorso di certificazione", true);
 			aDesc = new TreeNodeDescriptor(TreeNodeDescriptor.TreeNodeType.CERTIFICATION_PATH,aCert);
 
 			xaDNode.setDataValue(aDesc);			
 			xaCNode.appendChild(xaDNode);
 
 			//add fake certificate to the certification path
 			SignatureStateInDocument aCert2 = new CertificateDataCA();
 			addDummyPathCertificates(xTreeDataModel, xaDNode, aCert2);
 
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return aretValue;
 	}
 	
 	public XMutableTreeNode addDummyPathCertificates(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode aStartNode,
 				SignatureStateInDocument aCert ) {
 		XMutableTreeNode aretValue = null;
 		try {
 			XMutableTreeNode xaCNode = xTreeDataModel.createNode(aCert.getUser(), true);
 			if(sCertificateValid != null)
 				xaCNode.setNodeGraphicURL((aCert.isCertificateValid())?sCertificateValid : sCertificateNotValidated);
 			aStartNode.appendChild(xaCNode);
 			aretValue = xaCNode;
 
 			addDummyCertificateFields(xTreeDataModel, xaCNode, aCert, null);
 /*			XMutableTreeNode xaDNode;
 			xaDNode = xTreeDataModel.createNode("{ nome e cognome firmatario}", false);			
 			aretValue.appendChild(xaDNode);
 */
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return aretValue;
 	}
 
 	/**
 	 * 
 	 * @param xTreeDataModel the node factory
 	 * @param aStartNode the parent done
 	 * @param aCert the certificate dato to add
 	 * @param sGraphic
 	 */
 	public void addDummyCertificateFields(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode aStartNode, SignatureStateInDocument aCert, String sGraphic) {
 		/*try {*/
 			XMutableTreeNode xaDNode;
 
 			
 			xaDNode = aStartNode;//xTreeDataModel.createNode("Dettagli del certificato", true);
 /*			if(sGraphic != null)
 				xaDNode.setNodeGraphicURL(sGraphic);
 
 			TreeNodeDescriptor aDesc = new TreeNodeDescriptor(TreeNodeDescriptor.TreeNodeType.CERTIFICATE,aCert);
 			addLinesDisplayElement(aDesc);			
 			xaDNode.setDataValue(aDesc);
 
 			aStartNode.appendChild(xaDNode);*/
 /**
  * to get information from a certificate:
  * openssl x509 -inform DER -in CNIPA1.cer -noout -text
  * 
  */
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Versione", TreeNodeDescriptor.TreeNodeType.VERSION, aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Numero di serie", TreeNodeDescriptor.TreeNodeType.SERIAL_NUMBER,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Emittente", TreeNodeDescriptor.TreeNodeType.ISSUER,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Valido da", TreeNodeDescriptor.TreeNodeType.VALID_FROM,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Valido fino a", TreeNodeDescriptor.TreeNodeType.VALID_TO,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Soggetto", TreeNodeDescriptor.TreeNodeType.SUBJECT,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Algoritmo del soggetto", TreeNodeDescriptor.TreeNodeType.SUBJECT_ALGORITHM,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Chiave pubblica", TreeNodeDescriptor.TreeNodeType.PUBLIC_KEY,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Algoritmo di firma", TreeNodeDescriptor.TreeNodeType.SIGNATURE_ALGORITHM,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Impronta SHA1", TreeNodeDescriptor.TreeNodeType.THUMBPRINT_SHA1,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Impronta MD5", TreeNodeDescriptor.TreeNodeType.THUMBPRINT_MD5,aCert);
 //insert critical extension
 			appendMultilineNodeCriticalExtensions(xTreeDataModel, xaDNode, aCert);
 			//insert non critical extensions
 			appendMultilineNodeNonCriticalExtensions(xTreeDataModel, xaDNode, aCert);
 /*		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 	}
 
 	/**
 	 * 
 	 * @param xTreeDataModel the node factory
 	 * @param aStartNode the parent done
 	 * @param aCert the certificate dato to add
 	 * @param sGraphic
 	 */
 	public void addDummyCertificateFieldsKOExtCrit(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode aStartNode, SignatureStateInDocument aCert, String sGraphic) {
 		/*try {*/
 			XMutableTreeNode xaDNode;
 
 			
 			xaDNode = aStartNode;//xTreeDataModel.createNode("Dettagli del certificato", true);
 /*			if(sGraphic != null)
 				xaDNode.setNodeGraphicURL(sGraphic);
 
 			TreeNodeDescriptor aDesc = new TreeNodeDescriptor(TreeNodeDescriptor.TreeNodeType.CERTIFICATE,aCert);
 			addLinesDisplayElement(aDesc);			
 			xaDNode.setDataValue(aDesc);
 
 			aStartNode.appendChild(xaDNode);*/
 /**
  * to get information from a certificate:
  * openssl x509 -inform DER -in CNIPA1.cer -noout -text
  * 
  */
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Versione", TreeNodeDescriptor.TreeNodeType.VERSION, aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Numero di serie", TreeNodeDescriptor.TreeNodeType.SERIAL_NUMBER,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Emittente", TreeNodeDescriptor.TreeNodeType.ISSUER,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Valido da", TreeNodeDescriptor.TreeNodeType.VALID_FROM,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Valido fino a", TreeNodeDescriptor.TreeNodeType.VALID_TO,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Soggetto", TreeNodeDescriptor.TreeNodeType.SUBJECT,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Algoritmo del soggetto", TreeNodeDescriptor.TreeNodeType.SUBJECT_ALGORITHM,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Chiave pubblica", TreeNodeDescriptor.TreeNodeType.PUBLIC_KEY,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Algoritmo di firma", TreeNodeDescriptor.TreeNodeType.SIGNATURE_ALGORITHM,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Impronta SHA1", TreeNodeDescriptor.TreeNodeType.THUMBPRINT_SHA1,aCert);
 			appendMultilineNodeDescription(xTreeDataModel, xaDNode, "Impronta MD5", TreeNodeDescriptor.TreeNodeType.THUMBPRINT_MD5,aCert);
 			//insert critical extension
 			appendMultilineNodeCriticalExtensionsKO(xTreeDataModel, xaDNode, aCert);
 			//insert non critical extensions
 			appendMultilineNodeNonCriticalExtensions(xTreeDataModel, xaDNode, aCert);
 /*		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 	}
 
 	private void appendMultilineNodeDescription(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode xaDNode, String sNodeTitle,
 			TreeNodeType treeNodeType, SignatureStateInDocument certxTreeDataModel) {
 		try {
 			// add version display field
 			XMutableTreeNode xaENode = xTreeDataModel.createNode(sNodeTitle, false);
 			TreeNodeDescriptor aDesc = new TreeNodeDescriptor(treeNodeType,certxTreeDataModel);
 			addMultiLineTextDisplayElement(aDesc);
 			xaENode.setDataValue(aDesc);			
 			xaDNode.appendChild(xaENode);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void appendMultilineNodeNonCriticalExtensions(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode xaDNode,
 			SignatureStateInDocument certxTreeDataModel) {
 		try {
 			// add version display field
 			XMutableTreeNode xaENode = xTreeDataModel.createNode("Estensioni non critiche", false);
 			TreeNodeDescriptor aDesc = new TreeNodeDescriptor(TreeNodeType.EXTENSIONS_NON_CRITICAL,certxTreeDataModel);
 			addMultiLineTextDisplayElement(aDesc);
 			xaENode.setDataValue(aDesc);			
 			xaDNode.appendChild(xaENode);
 			
 //now create our child nodes			
 			appendMultilineNodeNonCriticalExtensionsHelper(xTreeDataModel, xaENode,
 					"X509v3 Certificate Policies",TreeNodeType.X509V3_CERTIFICATE_POLICIES,
 						certxTreeDataModel);
 			appendMultilineNodeNonCriticalExtensionsHelper(xTreeDataModel, xaENode, 
 					"X509v3 CRL Distribution Points", TreeNodeType.X509V3_CRL_DISTRIBUTION_POINTS ,
 					certxTreeDataModel);
 			appendMultilineNodeNonCriticalExtensionsHelper(xTreeDataModel, xaENode, 
 					"X509v3 Authority Key Identifier", TreeNodeType.X509V3_AUTHORITY_KEY_IDENTIFIER ,
 					certxTreeDataModel);
 			appendMultilineNodeNonCriticalExtensionsHelper(xTreeDataModel, xaENode, 
 					"X509v3 Subject Key Identifier", TreeNodeType.X509V3_SUBJECT_KEY_IDENTIFIER ,
 					certxTreeDataModel);
 			appendMultilineNodeNonCriticalExtensionsHelper(xTreeDataModel, xaENode, 
 					"qcStatements", TreeNodeType.QC_STATEMENTS, 
 					certxTreeDataModel);
 			appendMultilineNodeNonCriticalExtensionsHelper(xTreeDataModel, xaENode,
 					"X509v3 Subject Directory Attributes", TreeNodeType.X509V3_SUBJECT_DIRECTORY_ATTRIBUTES,
 						certxTreeDataModel);
 			appendMultilineNodeNonCriticalExtensionsHelper(xTreeDataModel, xaENode,
 					"Authority Information Access", TreeNodeType.AUTHORITY_INFORMATION_ACCESS,
 						certxTreeDataModel);
 			appendMultilineNodeNonCriticalExtensionsHelper(xTreeDataModel, xaENode, 
 					"X509v3 Issuer Alternative Name", TreeNodeType.X509V3_ISSUER_ALTERNATIVE_NAME ,
 					certxTreeDataModel);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void appendMultilineNodeNonCriticalExtensionsHelper(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode xaParentNode,
 			String sTitle, TreeNodeType eTreeNodeType, SignatureStateInDocument certxTreeDataModel) {
 		
 		try {
 			XMutableTreeNode xaENodeChild = xTreeDataModel.createNode(sTitle, false);
 			TreeNodeDescriptor aDesc = new TreeNodeDescriptor(eTreeNodeType, certxTreeDataModel);
 			addMultiLineTextDisplayElement(aDesc);
 			xaENodeChild.setDataValue(aDesc);			
 			xaParentNode.appendChild(xaENodeChild);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 	
 	private void appendMultilineNodeCriticalExtensions(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode xaDNode, 
 					SignatureStateInDocument certxTreeDataModel) {
 		try {
 			// add version display field
 			XMutableTreeNode xaENode = xTreeDataModel.createNode("Estensioni critiche", false);
 			TreeNodeDescriptor aDesc = new TreeNodeDescriptor(TreeNodeType.EXTENSIONS_CRITICAL,certxTreeDataModel);
 			addMultiLineTextDisplayElement(aDesc);
 			xaENode.setDataValue(aDesc);			
 			xaDNode.appendChild(xaENode);
 
 			XMutableTreeNode xaENodeChild = xTreeDataModel.createNode("X509v3 Key Usage", false);
 			aDesc = new TreeNodeDescriptor(TreeNodeType.X509V3_KEY_USAGE,certxTreeDataModel);
 			addMultiLineTextDisplayElement(aDesc);		
 			xaENodeChild.setDataValue(aDesc);			
 			xaENode.appendChild(xaENodeChild);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	//fake function added to be able to write specifications...
 	//simulates error on certificate critical extensions
 	private void appendMultilineNodeCriticalExtensionsKO(XMutableTreeDataModel xTreeDataModel, XMutableTreeNode xaDNode, 
 			SignatureStateInDocument certxTreeDataModel) {
 		try {
 			// add version display field
 			XMutableTreeNode xaENode = xTreeDataModel.createNode("Estensioni critiche", false);
 			TreeNodeDescriptor aDesc = new TreeNodeDescriptor(TreeNodeType.EXTENSIONS_CRITICAL,certxTreeDataModel);
 			addMultiLineTextDisplayElement(aDesc);
 			// add the string displaying the graphic for broken signal			sCertificateElementWarning
 			if(sCertificateElementBroken != null)
 				xaENode.setNodeGraphicURL(sCertificateElementBroken);
 			xaENode.setDataValue(aDesc);			
 			xaDNode.appendChild(xaENode);
 			
 			XMutableTreeNode xaENodeChild = xTreeDataModel.createNode("X509v3 Key Usage", false);
			aDesc = new TreeNodeDescriptor(TreeNodeType.X509V3_KEY_USAGE,certxTreeDataModel);
 			addMultiLineTextDisplayElement(aDesc);
 			// add the string displaying the graphic for broken signal			sCertificateElementWarning
 			if(sCertificateElementBroken != null)
 				xaENodeChild.setNodeGraphicURL(sCertificateElementBroken);
 		
 			xaENodeChild.setDataValue(aDesc);			
 			xaENode.appendChild(xaENodeChild);
 			
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void disableNamedControl(String sTheName) {
 		XControl xTFControl = m_xDlgContainer.getControl( sTheName );
 		if(xTFControl != null){
 			XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
 			if(xaWNode != null )
 				xaWNode.setVisible(false);
 		}
 	}
 
 	private void disableAllNamedControls() {
 		disableNamedControl(m_sDispElemsName);
 		disableNamedControl(sEmptyText);
 		for(int i = 0; i < SignatureStateInDocument.m_nMAXIMUM_FIELDS; i++)
 			disableNamedControl(sEmptyTextLine+i);
 	}	
 
 	public void selectionChanged( com.sun.star.lang.EventObject arg0 ) {
 		Object oObject = m_xTreeControl.getSelection();
 // check if it's a node		
 		XMutableTreeNode xaENode = (XMutableTreeNode)UnoRuntime.queryInterface( XMutableTreeNode.class, 
 				oObject );
 		Object oTreeNodeObject = null;
 		TreeNodeDescriptor aCurrentNode = null;
 		XComponent xTheCurrentComp = null;
 		//disable the previous Node
 		if(m_aTheOldNode != null) {
 			//disable it, that is un-display it
 			oTreeNodeObject  = m_aTheOldNode.getDataValue();
 			xTheCurrentComp = (XComponent)UnoRuntime.queryInterface( XComponent.class, oTreeNodeObject );
 			if(xTheCurrentComp != null) {
 				aCurrentNode = (TreeNodeDescriptor)oTreeNodeObject;
 				aCurrentNode.EnableDisplay(false);
 			}
 		}
 		else {// old node null, disable all all the display elements
 			disableAllNamedControls();
 //disable some of the pushbutton as well			
 		}
 		//...and set the new old node
 		m_aTheOldNode = xaENode;
 
 		if(xaENode != null) {
 			oTreeNodeObject  = xaENode.getDataValue();
 			xTheCurrentComp = (XComponent)UnoRuntime.queryInterface( XComponent.class, oTreeNodeObject );
 			if(xTheCurrentComp != null) {
 // get node type and enable/disable	the pushbutton
 				aCurrentNode = (TreeNodeDescriptor)oTreeNodeObject;
 				boolean bEnableButton = false;
 				if(aCurrentNode.getType() == TreeNodeType.SIGNATURE) {
 					bEnableButton = true;
 				}
 				enableSingleButton(sVerify,bEnableButton);
 				enableSingleButton(sRemove,bEnableButton);				
 				aCurrentNode.EnableDisplay(true);
 			}
 		}		
 	}
 
 	private void enableSingleButton(String sButtonName, boolean bEnable) {
 		//grab the button...
 		XControl xTFControl = m_xDlgContainer.getControl( sButtonName );
 		if(xTFControl != null){
 //			Utilities.showInterfaces(xTFControl);
 			//...and set state accordingly
 			XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
 			if(xaWNode != null )
 				xaWNode.setEnable(bEnable);
 		}
 	}
 }
